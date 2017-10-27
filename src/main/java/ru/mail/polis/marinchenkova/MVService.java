package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.marinchenkova.util.Query;

import java.io.IOException;
import java.net.InetSocketAddress;

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
                     @NotNull final IDataBase dBase) throws IOException {
        this.dataBase = dBase;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        //Status context
        this.server.createContext(STATUS, http -> {
            try {
                statusQuery(http);
            } catch (IOException e) {
                http.close();
            }
        });

        //Entity context
        this.server.createContext(ENTITY, http -> {
            Query query = new Query(http.getRequestURI().getQuery());

            if (query.id == null) http.sendResponseHeaders(404, 0);
            else if (query.id.isEmpty()) http.sendResponseHeaders(400, 0);
            else {
                String method = http.getRequestMethod();
                switch (method) {
                    case GET:
                        try {
                            getQuery(http, query.id);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                            http.sendResponseHeaders(404, 0);
                        }
                        break;

                    case PUT:
                        try {
                            putQuery(http, query.id);
                        } catch (IOException e) {
                            http.close();
                        }
                        break;

                    case DELETE:
                        try {
                            deleteQuery(http, query.id);
                        } catch (IOException e) {
                            http.close();
                        }
                        break;

                    default:
                        try {
                            unknownQuery(http);
                        } catch (IOException e) {
                            http.close();
                        }
                        break;
                }
            }
            http.close();
        });
    }

    private void statusQuery(@NotNull final HttpExchange http) throws IOException {
        String response = ONLINE;
        http.sendResponseHeaders(200, response.length());
        http.getResponseBody().write(response.getBytes());
        http.close();
    }

    private void getQuery(@NotNull final HttpExchange http,
                          @NotNull final String id) throws IOException {
        byte[] data = this.dataBase.get(id);
        http.sendResponseHeaders(200, data.length);
        http.getResponseBody().write(data);
    }

    private void putQuery(@NotNull final HttpExchange http,
                          @NotNull final String id) throws IOException {
        int available = http.getRequestBody().available();
        byte[] data = new byte[available];
        http.getRequestBody().read(data);

        this.dataBase.upsert(id, data);
        http.sendResponseHeaders(201, 0);
    }

    private void deleteQuery(@NotNull final HttpExchange http,
                             @NotNull final String id) throws IOException {
        this.dataBase.remove(id);
        http.sendResponseHeaders(202, 0);
    }

    private void unknownQuery(@NotNull final HttpExchange http) throws IOException {
        http.sendResponseHeaders(405, 0);
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
