package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.KVService;
import ru.mail.polis.marinchenkova.util.Query;
import ru.mail.polis.marinchenkova.util.Response;
import ru.mail.polis.marinchenkova.util.TopologyAgent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Set;

/**
 * @author Marinchenko V. A.
 */
public class MVService implements KVService {

    public final static String ENTITY = "/v0/entity";
    public final static String STATUS = "/v0/status";


    public final static String REPLICA = "REPLICA";
    public final static String SLAVE = "SLAVE";
    public final static String PARTIAL = "PARTIAL";
    public final static String ONLINE = "ONLINE";
    public final static String GET = "GET";
    public final static String PUT = "PUT";
    public final static String DELETE = "DELETE";

    @NotNull
    private final HttpServer server;
    @NotNull
    private final IDataBase dataBase;
    @NotNull
    private final TopologyAgent topologyAgent;


    public MVService(final int port,
                     @NotNull final IDataBase dataBase,
                     @NotNull final Set<String> topology) throws IOException {
        this.dataBase = dataBase;
        this.topologyAgent = new TopologyAgent(topology, dataBase, port);
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
                        getQuery(http, query);
                        break;

                    case PUT:
                        putQuery(http, query);
                        break;

                    case DELETE:
                        deleteQuery(http, query);
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
             query = new Query(http.getRequestURI().getQuery(), topologyAgent.size);

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
                          @NotNull final Query query) {
        if (http.getRequestHeaders().containsKey(PARTIAL)) {
            http.close();
            topologyAgent.sendMissedWrites();

        } else {
            byte[] data = this.dataBase.get(query.id);
            int code = data == null ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_OK;
            final Response response;

            if (!http.getRequestHeaders().containsKey(REPLICA)) {
                response = topologyAgent.process(
                        code,
                        data,
                        query,
                        GET);
                code = response.code;
                data = response.data;
            }

            try {
                http.sendResponseHeaders(code, code == HttpStatus.SC_OK ? data.length : 0);
                if (code == HttpStatus.SC_OK) http.getResponseBody().write(data);
                http.close();

            } catch (Exception e) {
                http.close();
            }
        }
    }

    private void putQuery(@NotNull final HttpExchange http,
                          @NotNull final Query query) {
        final byte data[] = DataBase.readByteArray(http.getRequestBody());
        final boolean created = this.dataBase.upsert(query.id, data);
        final Response response;
        int code = created ? HttpStatus.SC_CREATED : HttpStatus.SC_INTERNAL_SERVER_ERROR;

        if (!http.getRequestHeaders().containsKey(REPLICA)) {
            response = topologyAgent.process(
                    code,
                    data,
                    query,
                    PUT);
            code = response.code;
        }

        try {
            http.sendResponseHeaders(code, 0);
            http.close();

        } catch (Exception e) {
            http.close();
        }
    }

    private void deleteQuery(@NotNull final HttpExchange http,
                             @NotNull final Query query) {
        this.dataBase.remove(query.id);
        final Response response;
        int code = HttpStatus.SC_ACCEPTED;

        if (!http.getRequestHeaders().containsKey(REPLICA)) {
            response = topologyAgent.process(
                    code,
                    null,
                    query,
                    DELETE);

            code = response.code;
        }

        try {
            http.sendResponseHeaders(code, 0);
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

    @Override
    public void start() {
        this.server.start();
        this.topologyAgent.start();
    }

    @Override
    public void stop() {
        this.server.stop(0);
    }
}
