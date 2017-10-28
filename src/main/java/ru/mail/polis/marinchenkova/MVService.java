package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.marinchenkova.util.Query;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author Marinchenko V. A.
 */
public class MVService implements KVService {

    private final static String ENTITY = "/v0/entity";
    private final static String STATUS = "/v0/status";

    private final static String ONLINE = "ONLINE";
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String DELETE = "DELETE";

    @NotNull
    private final HttpServer server;

    @NotNull
    private final IDataBase dataBase;


    public MVService(final int port,
                     @NotNull final IDataBase dBase,
                     @NotNull final Set<String> topology) throws IOException {
        this.dataBase = dBase;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        //Status context
        this.server.createContext(STATUS, this::statusQuery);

        //Entity context
        this.server.createContext(ENTITY, http -> {
            Query query = new Query(http.getRequestURI().getQuery());

            if (query.id == null) http.sendResponseHeaders(HttpStatus.SC_NOT_FOUND, 0);
            else if (query.id.isEmpty()) http.sendResponseHeaders(HttpStatus.SC_BAD_REQUEST, 0);
            else {
                String method = http.getRequestMethod();
                switch (method) {
                    case GET:
                        getQuery(http, query.id);
                        break;

                    case PUT:
                        putQuery(http, query.id);
                        break;

                    case DELETE:
                        deleteQuery(http, query.id);
                        break;

                    default:
                        unknownQuery(http);
                        break;
                }
            }
            http.close();
        });
    }

    private void statusQuery(@NotNull final HttpExchange http) {
        try {
            String response = ONLINE;
            http.sendResponseHeaders(HttpStatus.SC_OK, response.length());
            http.getResponseBody().write(response.getBytes());
            http.close();
        } catch (IOException e) {
            http.close();
        }
    }

    private void getQuery(@NotNull final HttpExchange http,
                          @NotNull final String id) {
        try {
            byte[] data = this.dataBase.get(id);
            if (data != null) {
                http.sendResponseHeaders(HttpStatus.SC_OK, data.length);
                http.getResponseBody().write(data);
            } else {
                http.sendResponseHeaders(HttpStatus.SC_NOT_FOUND, 0);
            }

        } catch (IOException e) {
            failedQuery(http, id);
        }
    }

    private void putQuery(@NotNull final HttpExchange http,
                          @NotNull final String id) {
        try {
            int available = http.getRequestBody().available();
            byte[] data = new byte[available];

            int r = http.getRequestBody().read(data);
            if(r != data.length && r != -1) {
                throw new IOException("Can't read " + id + " in one go!");
            }

            this.dataBase.upsert(id, data);
            http.sendResponseHeaders(HttpStatus.SC_CREATED, 0);

        } catch (IOException e) {
            failedQuery(http, id);
        }
    }

    private void deleteQuery(@NotNull final HttpExchange http,
                             @NotNull final String id) {
        try {
            this.dataBase.remove(id);
            http.sendResponseHeaders(HttpStatus.SC_ACCEPTED, 0);

        } catch (IOException e) {
            failedQuery(http, id);
        }
    }

    private void unknownQuery(@NotNull final HttpExchange http) {
        try {
            http.sendResponseHeaders(HttpStatus.SC_METHOD_NOT_ALLOWED, 0);
        } catch (IOException e) {
            failedQuery(http, "");
        }
    }

    private void failedQuery(@NotNull final HttpExchange http,
                             @NotNull final String id) {
        try {
            String response = http.getRequestMethod() + " " + id + " failed";
            http.getResponseBody().write(response.getBytes());
            http.sendResponseHeaders(HttpStatus.SC_INTERNAL_SERVER_ERROR, response.length());
        } catch (IOException e) {
            http.close();
        }
    }

    @Override
    public void start() {
        this.server.start();
    }

    @Override
    public void stop() {
        this.server.stop(0);
    }
}
