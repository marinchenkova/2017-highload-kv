package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;
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
        this.server.createContext(STATUS, httpExchange -> {
            String response = ONLINE;
            httpExchange.sendResponseHeaders(200, response.length());
            httpExchange.getResponseBody().write(response.getBytes());
            httpExchange.close();
        });

        //Entity context
        this.server.createContext(ENTITY, httpExchange -> {
            String id = idFromQuery(httpExchange.getRequestURI().getQuery());
            if ("".equals(id) || id == null) httpExchange.sendResponseHeaders(400, 0);
            else {
                String method = httpExchange.getRequestMethod();
                switch (method) {
                    case GET:
                        try {
                            byte[] data = this.dataBase.get(id);
                            httpExchange.sendResponseHeaders(200, data.length);
                            httpExchange.getResponseBody().write(data);
                        } catch (NoSuchElementException | IOException e) {
                            httpExchange.sendResponseHeaders(404, 0);
                        }
                        break;

                    case PUT:
                        int available = httpExchange.getRequestBody().available();
                        byte[] data = new byte[available];

                        httpExchange.getRequestBody().read(data);

                        this.dataBase.upsert(id, data);
                        httpExchange.sendResponseHeaders(201, 0);
                        break;

                    case DELETE:
                        this.dataBase.remove(id);
                        httpExchange.sendResponseHeaders(202, 0);
                        break;

                    default:
                        //HTTP: Method not allowed
                        httpExchange.sendResponseHeaders(405, 0);
                        break;
                }
            }
            httpExchange.close();
        });
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
