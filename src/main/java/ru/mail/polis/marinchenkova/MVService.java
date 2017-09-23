package ru.mail.polis.marinchenkova;

import com.sun.net.httpserver.HttpServer;
import ru.mail.polis.KVService;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class MVService implements KVService {

    private final static String PATTERN_ENTITY = Pattern.quote("/v0/entity?id=");
    private final static String PATTERN_STATUS = Pattern.quote("/v0/status");
    private final static String ONLINE = "ONLINE";
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String DELETE = "DELETE";


    private final int port = 0;
    private HttpServer server;


    public MVService(int port){
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.createContext("/", httpExchange -> {
            String method = httpExchange.getRequestMethod();
            switch (method) {
                case GET:
                    String response = idFromURI(httpExchange.getRequestURI());
                    if(response.equals(ONLINE)) {
                        httpExchange.sendResponseHeaders(200, response.length());
                        httpExchange.getResponseBody().write(response.getBytes());
                    } else {
                        httpExchange.sendResponseHeaders(404, 0);
                    }
                    break;

                case PUT:
                    httpExchange.sendResponseHeaders(201, 0);
                    break;

                case DELETE:
                    httpExchange.sendResponseHeaders(202, 0);
                    break;
            }
            httpExchange.close();
        });
    }

    /**
     * Анализ URI.
     * @param uri
     * @return {@link String} ONLINE, если запрос /v0/status или /v0/status и любые символы далее,
     * id, если запрос /v0/entity?id=
     * в остальных случая -  исключение
     * @throws IllegalArgumentException, если запрос не удовлетворяет шаблонам /v0/status и /v0/entity?id=
     */
    private String idFromURI(URI uri) throws IllegalArgumentException {
        Pattern status = Pattern.compile(PATTERN_STATUS + ".*");
        Matcher matcher = status.matcher(uri.toString());
        if(matcher.matches()) return ONLINE;

        matcher.usePattern(Pattern.compile(PATTERN_ENTITY + ".*"));
        if(matcher.matches()) {
            matcher.usePattern(Pattern.compile(PATTERN_ENTITY));
            return matcher.replaceFirst("");
        } else {
            throw new IllegalArgumentException("Request is not supported");
        }
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
