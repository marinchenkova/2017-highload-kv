package ru.mail.polis.marinchenkova;

import com.sun.istack.internal.Nullable;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import ru.mail.polis.KVService;
import ru.mail.polis.marinchenkova.util.Query;
import ru.mail.polis.marinchenkova.util.TopologyAgent;

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
            final Query query = validateQuery(http);
            if (query != null) {
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

    @Nullable
    private Query validateQuery(@NotNull final HttpExchange http) {
        final int code;
        final Query query;

        try {
             query = new Query(http.getRequestURI().getQuery(), topAgent.from);

        } catch (IllegalArgumentException e) {
            switch (e.getMessage()) {
                case Query.MSG_BAD_REQUEST:
                    code = HttpStatus.SC_BAD_REQUEST;
                    break;

                case Query.MSG_NOT_FOUND:
                    code = HttpStatus.SC_NOT_FOUND;
                    break;

                default:
                    code = 0;
            }

            try {
                http.sendResponseHeaders(code, 0);
                http.close();
            } catch (Exception ex) {
                http.close();
            }

            return null;
        }

        return query;
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
