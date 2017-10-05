package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpServer;
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

    private final static String PATTERN_ID = "id=";

    private final static String ENTITY = "/v0/entity";
    private final static String STATUS = "/v0/status";


    private final static String ONLINE = "ONLINE";
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String DELETE = "DELETE";



    private final int port = 0;
    private final HttpServer server;
    private final IDataBase dataBase;


    public MVService(int port, IDataBase dBase) throws IOException {
        this.dataBase = dBase;
        server = HttpServer.create(new InetSocketAddress(port), 0);

        //Status context
        server.createContext(STATUS, httpExchange -> {
            String response = ONLINE;
            httpExchange.sendResponseHeaders(200, response.length());
            httpExchange.getResponseBody().write(response.getBytes());
            httpExchange.close();
        });

        //Entity context
        server.createContext(ENTITY, httpExchange -> {
            String id = idFromQuery(httpExchange.getRequestURI().getQuery());
            if (id.equals("")) httpExchange.sendResponseHeaders(400, 0);
            else {
                String method = httpExchange.getRequestMethod();
                switch (method) {
                    case GET:
                        try {
                            byte[] data = dataBase.get(id);
                            httpExchange.sendResponseHeaders(200, data.length);
                            httpExchange.getResponseBody().write(data);
                        } catch (NoSuchElementException | IOException e) {
                            httpExchange.sendResponseHeaders(404, 0);
                        }
                        break;

                    case PUT:
                        int available = httpExchange.getRequestBody().available();
                        byte[] data = new byte[available];

                        //TODO case PUT: safe search InputStream
                        int read = httpExchange.getRequestBody().read(data);

                        dataBase.put(id, data);
                        httpExchange.sendResponseHeaders(201, 0);
                        break;

                    case DELETE:
                        dataBase.delete(id);
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
     * @return {@link String} id или пустую строку, если запрос начинается на "id=",
     * в остальных случая -  исключение.
     * @throws IllegalArgumentException если запрос не начинается с "id="
     */
    public static String idFromQuery(String query) throws IllegalArgumentException {
        Pattern id = Pattern.compile(PATTERN_ID + ".*");
        Matcher matcher = id.matcher(query);

        if(matcher.matches()) {
            matcher.usePattern(Pattern.compile(PATTERN_ID));
            return matcher.replaceFirst("");
        }
        else throw new IllegalArgumentException("Incorrect request");
    }


    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }
}
