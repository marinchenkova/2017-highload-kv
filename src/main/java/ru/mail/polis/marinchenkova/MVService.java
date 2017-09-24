package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpServer;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class MVService implements KVService {

    private final static String PATTERN_ENTITY = Pattern.quote("/v0/entity?id=");
    private final static String PATTERN_STATUS = Pattern.quote("/v0/status");

    private final static String ENTITY = "/v0/entity?id=";
    private final static String STATUS = "/v0/status";


    private final static String ONLINE = "ONLINE";
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String DELETE = "DELETE";
    private final static String INCORRECT_REQUEST = "INCORRECT_REQUEST";


    private final int port = 0;
    private HttpServer server;
    private DataBase dataBase = new DataBase();


    public MVService(int port){
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        server.createContext("/", httpExchange -> {
            String response = idFromURI(httpExchange.getRequestURI().toString());

            try {
                if (response.equals("")) httpExchange.sendResponseHeaders(400, 0);
                else {
                    String method = httpExchange.getRequestMethod();
                    switch (method) {
                        case GET:
                            if (response.equals(ONLINE)) {
                                httpExchange.sendResponseHeaders(200, response.length());
                                httpExchange.getResponseBody().write(response.getBytes());
                            } else {
                                try {
                                    Value val = dataBase.get(response);
                                    httpExchange.sendResponseHeaders(200, val.toString().length());
                                    httpExchange.getResponseBody().write(val.getBytes());
                                } catch (NoSuchElementException e) {
                                    httpExchange.sendResponseHeaders(404, 0);
                                }
                            }
                            break;

                        case PUT:
                            //TODO case PUT
                            int available = httpExchange.getRequestBody().available();
                            byte[] data = new byte[available];

                            Reader in = new InputStreamReader(httpExchange.getRequestBody());

                            int read = httpExchange.getRequestBody().read(data);
                            while (read > 0) read = in.read();

                            dataBase.put(response, new Value(data));

                            httpExchange.sendResponseHeaders(201, read);
                            break;

                        case DELETE:
                            dataBase.delete(response);
                            httpExchange.sendResponseHeaders(202, 0);
                            break;
                    }
                    httpExchange.close();
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Анализ URI.
     * @param uri
     * @return {@link String} ONLINE, если запрос /v0/status или /v0/status и любые символы далее;
     * id, если запрос /v0/entity?id=;
     * в остальных случая -  исключение.
     */
    public static String idFromURI(String uri) throws IllegalArgumentException {
        Pattern status = Pattern.compile(PATTERN_STATUS + ".*");
        Matcher matcher = status.matcher(uri);
        if(matcher.matches()) return ONLINE;

        matcher.usePattern(Pattern.compile(PATTERN_ENTITY + ".*"));
        if(matcher.matches()) {
            matcher.usePattern(Pattern.compile(PATTERN_ENTITY));
            return matcher.replaceFirst("");
        } else throw new IllegalArgumentException("Incorrect request");
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
