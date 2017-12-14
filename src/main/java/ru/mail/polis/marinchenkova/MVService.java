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
import ru.mail.polis.marinchenkova.util.Util;

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
        final int code = HttpStatus.SC_OK;
        sendResponse(http, "status", ONLINE.getBytes(), code);
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
        final Query query = new Query(http.getRequestURI().getQuery(), topologyAgent.size);
        final int code = query.checkCorrect();

        if (code == HttpStatus.SC_OK) {
            return query;

        } else {
            sendResponse(http, query.getFull(), code);
            return null;
        }
    }

    private void getQuery(@NotNull final HttpExchange http,
                          @NotNull final Query query) {
        if (http.getRequestHeaders().containsKey(PARTIAL)) {
            http.close();
            topologyAgent.sendMissedWrites();

        } else {
            byte[] data = this.dataBase.get(query.getId());
            int code = data == null ? HttpStatus.SC_NOT_FOUND : HttpStatus.SC_OK;

            if (!http.getRequestHeaders().containsKey(REPLICA)) {
                final Response response = topologyAgent.process(
                        code,
                        data,
                        query,
                        GET);
                code = response.getCode();
                data = response.getData();
            }

            sendResponse(http, query.getFull(), data, code);
        }
    }

    private void putQuery(@NotNull final HttpExchange http,
                          @NotNull final Query query) {
        final byte data[] = Util.readByteArray(http.getRequestBody());
        final boolean created = this.dataBase.upsert(query.getId(), data);
        int code = created ? HttpStatus.SC_CREATED : HttpStatus.SC_INTERNAL_SERVER_ERROR;

        if (!http.getRequestHeaders().containsKey(REPLICA)) {
            final Response response = topologyAgent.process(
                    code,
                    data,
                    query,
                    PUT);
            code = response.getCode();
        }

        sendResponse(http, query.getFull(), code);
    }

    private void deleteQuery(@NotNull final HttpExchange http,
                             @NotNull final Query query) {
        this.dataBase.remove(query.getId());
        int code = HttpStatus.SC_ACCEPTED;

        if (!http.getRequestHeaders().containsKey(REPLICA)) {
            final Response response = topologyAgent.process(
                    code,
                    null,
                    query,
                    DELETE);

            code = response.getCode();
        }

        sendResponse(http, query.getFull(), code);
    }

    private void unknownQuery(@NotNull final HttpExchange http) {
        final String response = http.getRequestMethod() + " not allowed!";
        final int code = HttpStatus.SC_METHOD_NOT_ALLOWED;
        sendResponse(http, "unknown", response.getBytes(), code);
    }

    private void sendResponse(@NotNull final HttpExchange http,
                              @NotNull final String query,
                              final int code) {
        sendResponse(http, query, null, code);
    }

    private void sendResponse(@NotNull final HttpExchange http,
                              @NotNull final String query,
                              @Nullable final byte[] data,
                              final int code) {
        try {
            final boolean withData = data != null;
            http.sendResponseHeaders(code, withData ? data.length : 0);
            if (withData) http.getResponseBody().write(data);

        } catch (Exception e) {
            System.err.println("Invalid request: " + query + ": " + e.getMessage());
        } finally {
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
