package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class MVService implements KVService {

    private final static Pattern IDD = Pattern.compile("id=.*");
    private final static Pattern ID = Pattern.compile("id=");

    private final static String ENTITY = "/v0/entity";
    private final static String STATUS = "/v0/status";

    private final static String ONLINE = "ONLINE";
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String DELETE = "DELETE";

    private final HttpServer server;
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
            String id = idFromQuery(http.getRequestURI().getQuery());

            if ("".equals(id) || id == null) http.sendResponseHeaders(400, 0);
            else {
                String method = http.getRequestMethod();
                switch (method) {
                    case GET:
                        try {
                            getQuery(http, id);
                        } catch (IOException e) {
                            send404Closed(http);
                        }
                        break;

                    case PUT:
                        try {
                            putQuery(http, id);
                        } catch (IOException e) {
                            http.close();
                        }
                        break;

                    case DELETE:
                        try {
                            deleteQuery(http, id);
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

    private void send404Closed(@NotNull final HttpExchange http) throws IOException {
        http.sendResponseHeaders(404, 0);
        http.close();
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
        //HTTP: Method not allowed
        http.sendResponseHeaders(405, 0);
    }

    /**
     * Извлечение id из запроса.
     * @param query запрос
     * @return {@link String} id или пустую строку, если запрос пустой
     */
    @Nullable
    public static String idFromQuery(@NotNull final String query) {
        Matcher matcher = IDD.matcher(query);

        if (matcher.matches()) {
            matcher = ID.matcher(query);
            return matcher.replaceFirst("");
        } else return null;
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
