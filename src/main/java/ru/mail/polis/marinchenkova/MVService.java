package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.marinchenkova.util.Query;
import ru.mail.polis.marinchenkova.util.TopologyAgent;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @NotNull
    private final TopologyAgent topAgent;


    public MVService(final int port,
                     @NotNull final IDataBase dataBase,
                     @NotNull final Set<String> topology) throws IOException {
        this.dataBase = dataBase;
        this.topAgent = new TopologyAgent(topology);
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        this.server.createContext(STATUS, this::statusQuery);
        this.server.createContext(ENTITY, this::entityQuery);
    }

    private void statusQuery(@NotNull final HttpExchange http) {
        try {
            final String response = ONLINE;
            http.sendResponseHeaders(HttpStatus.SC_OK, response.length());
            http.getResponseBody().write(response.getBytes());
            http.close();
        } catch (Exception e) {
            http.close();
        }
    }

    private void entityQuery(@NotNull final HttpExchange http) {
        try {
            final Query query = new Query(http.getRequestURI().getQuery(), topAgent.from);
            if (validQuery(http, query)) {
                final String method = http.getRequestMethod();
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
        } catch (Exception e) {
            http.close();
        }
    }

    private boolean validQuery(@NotNull HttpExchange http,
                               @NotNull final Query query) {
        if (query.id == null || query.id.isEmpty()) {
            final int code = query.id == null ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_BAD_REQUEST;

            try {
                http.sendResponseHeaders(code, 0);
                http.close();
            } catch (Exception e) {
                http.close();
            }

            return false;

        } else return true;
    }

    private void getQuery(@NotNull final HttpExchange http,
                          @NotNull final String id) {
        try {
            final byte[] data = this.dataBase.get(id);

            if (data != null) {
                http.sendResponseHeaders(HttpStatus.SC_OK, data.length);
                http.getResponseBody().write(data);
            } else {
                http.sendResponseHeaders(HttpStatus.SC_NOT_FOUND, 0);
            }
            http.close();

        } catch (Exception e) {
            http.close();
        }
    }

    private void putQuery(@NotNull final HttpExchange http,
                          @NotNull final String id) {
        try {
            this.dataBase.upsert(id, DataBase.readByteArray(http.getRequestBody()));
            http.sendResponseHeaders(HttpStatus.SC_CREATED, 0);
            http.close();

        } catch (Exception e) {
            http.close();
        }
    }

    private void deleteQuery(@NotNull final HttpExchange http,
                             @NotNull final String id) {
        try {
            this.dataBase.remove(id);
            http.sendResponseHeaders(HttpStatus.SC_ACCEPTED, 0);
            http.close();

        } catch (Exception e) {
            http.close();
        }
    }

    private void unknownQuery(@NotNull final HttpExchange http) {
        try {
            final String response = http.getRequestMethod() + " not allowed!";
            http.sendResponseHeaders(HttpStatus.SC_METHOD_NOT_ALLOWED, response.length());
            http.getResponseBody().write(response.getBytes());
            http.close();

        } catch (Exception e) {
            http.close();
        }
    }

    @NotNull
    public IDataBase getDataBase() {
        return this.dataBase;
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
