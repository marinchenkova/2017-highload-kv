package ru.mail.polis.marinchenkova.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.marinchenkova.DataBase;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.mail.polis.marinchenkova.MVService.*;

/**
 * @author Marinchenko V. A.
 */
public class TopologyAgent {

    private final static Pattern LOCALHOST = Pattern.compile("http://localhost:(\\d*)");


    @NotNull
    private final Set<String> topology;
    public final int from;

    public TopologyAgent(@NotNull final Set<String> topology) {
        this.from = topology.size();
        this.topology = topology;
    }

    @NotNull
    public Response getQuery(final int masterResponseCode,
                             @Nullable final byte[] data,
                             @NotNull final Query query,
                             @NotNull final String local,
                             @NotNull final String method) {
        int responsesNum = masterResponseCode == HttpStatus.SC_OK ? 1 : 0;
        int failuresNum = masterResponseCode == HttpStatus.SC_NOT_FOUND ? 1 : 0;

        if (responsesNum == query.ack) return new Response(HttpStatus.SC_OK, data);
        else {
            for (String addr : this.topology) {
                if (!addr.equals(local)) {
                    if (query.from - failuresNum < query.ack) break;

                    final String url = addr + ENTITY + "?" + query.full;
                    try {
                        final HttpURLConnection connection = (HttpURLConnection)
                                (new URL(addr + STATUS)).openConnection();

                        int code = connection.getResponseCode();
                        if (code == HttpStatus.SC_OK) {
                            switch (method) {
                                case GET:
                                    Response response = get(url, local);
                                    if (response.code == HttpStatus.SC_OK) responsesNum++;
                                    if (responsesNum == query.ack) return response;

                                case PUT:

                                case DELETE:

                            }

                        } else failuresNum++;

                    } catch (IOException e) {
                        failuresNum++;
                    }
                }
            }
            if (responsesNum > 0) return new Response(HttpStatus.SC_GATEWAY_TIMEOUT, null);
            return new Response(HttpStatus.SC_NOT_FOUND, null);
        }
    }

    @NotNull
    private Response get(@NotNull final String url,
                       @NotNull final String master) {
        final byte data[];
        final int code;
        try {
            final HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod(GET);
            if (!url.equals(master)) connection.setRequestProperty(REPLICA, SLAVE);

            code = connection.getResponseCode();
            if (code == HttpStatus.SC_OK) {
                connection.setDoInput(true);
                InputStream in = new DataInputStream(connection.getInputStream());
                data = DataBase.readByteArray(in);
                in.close();
            } else data = null;

            return new Response(code, data);

        } catch (IOException e) {
            return new Response(HttpStatus.SC_NOT_FOUND, null);
        }
    }
}
