package ru.mail.polis.marinchenkova.util;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.marinchenkova.DataBase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;
import java.util.regex.Pattern;

import static ru.mail.polis.marinchenkova.MVService.*;

/**
 * @author Marinchenko V. A.
 */
public class TopologyAgent {

    public final int size;

    private final static Pattern LOCALHOST = Pattern.compile("http://localhost:(\\d*)");
    private final static int CONNECT_TIMEOUT = 300;

    @NotNull
    private final Set<String> topology;


    public TopologyAgent(@NotNull final Set<String> topology) {
        this.size = topology.size();
        this.topology = topology;
    }

    @NotNull
    public Response process(final int masterResponseCode,
                            @Nullable final byte[] data,
                            @NotNull final Query query,
                            @NotNull final String local,
                            @NotNull final String method) {

        int responsesNum = responseNumInit(masterResponseCode, method);
        int failedConnectionsNum = 0;
        final Response response;

        if (this.size == 1) response = new Response(masterResponseCode, data);
        else {
            for (String addr : this.topology) {
                if (query.from - failedConnectionsNum < query.ack) break;
                if (responsesNum >= query.from) break;

                final String globalAddr = addr.replace("localhost", "127.0.0.1");
                final String master = "http:/" + local;
                if (globalAddr.equals(master)) continue;
                    try {
                        final URL urlStatus = new URL(globalAddr + STATUS);
                        final HttpURLConnection connection = (HttpURLConnection) urlStatus.openConnection();
                        connection.setConnectTimeout(CONNECT_TIMEOUT);
                        connection.connect();

                        if (connection.getResponseCode() == HttpStatus.SC_OK) {
                            final URL urlEntity = new URL(globalAddr + ENTITY + "?" + query.full);
                            final HttpURLConnection request = (HttpURLConnection) urlEntity.openConnection();
                            request.setConnectTimeout(CONNECT_TIMEOUT);
                            request.setRequestMethod(method);
                            request.setRequestProperty(REPLICA, SLAVE);

                            switch (method) {
                                case GET:
                                    final Response get = get(request);
                                    if (get.code == HttpStatus.SC_OK) responsesNum++;
                                    break;

                                case PUT:
                                    final Response put = put(request, data);
                                    if (put.code == HttpStatus.SC_CREATED) responsesNum++;
                                    break;

                                case DELETE:
                                    final Response delete = delete(request);
                                    if (delete.code == HttpStatus.SC_ACCEPTED) responsesNum++;
                                    break;
                            }

                        } else {
                            failedConnectionsNum++;
                        }

                    } catch (Exception e) {
                        failedConnectionsNum++;
                    }

            }

            if (method.equals(GET) && responsesNum < query.ack && this.size - failedConnectionsNum >= query.ack) {
                response = new Response(HttpStatus.SC_NOT_FOUND, null);
            } else if (responsesNum < query.ack) {
                response = new Response(HttpStatus.SC_GATEWAY_TIMEOUT, null);
            } else response = new Response(successResponseCode(method), data);
        }

        return response;
    }

    @NotNull
    private Response get(@NotNull final HttpURLConnection connection) {
        try {
            connection.connect();
            final int code = connection.getResponseCode();
            connection.disconnect();
            final byte data[];

            if (code == HttpStatus.SC_OK) {
                connection.setDoInput(true);
                final InputStream in = new DataInputStream(connection.getInputStream());
                data = DataBase.readByteArray(in);
                in.close();
            } else data = null;


            return new Response(code, data);

        } catch (IOException e) {
            return new Response(HttpStatus.SC_NOT_FOUND, null);
        }
    }

    @NotNull
    private Response put(@NotNull final HttpURLConnection connection,
                         @NotNull final byte[] data) {
        try {
            connection.setDoOutput(true);
            OutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(data);
            out.flush();

            connection.connect();
            final int code = connection.getResponseCode();
            connection.disconnect();

            return new Response(code, null);

        } catch (IOException e) {
            return new Response(HttpStatus.SC_METHOD_FAILURE, null);
        }
    }

    @NotNull
    private Response delete(@NotNull final HttpURLConnection connection) {
        try {
            connection.connect();
            final int code = connection.getResponseCode();
            connection.disconnect();

            return new Response(code, null);

        } catch (IOException e) {
            return new Response(HttpStatus.SC_METHOD_FAILURE, null);
        }
    }


    private int responseNumInit(final int masterResponseCode,
                                @NotNull final String method) {
        switch (method) {
            case GET:
                return masterResponseCode == HttpStatus.SC_OK ? 1 : 0;

            case PUT:
                return masterResponseCode == HttpStatus.SC_CREATED ? 1 : 0;

            case DELETE:
                return masterResponseCode == HttpStatus.SC_ACCEPTED ? 1 : 0;

            default:
                return 0;
        }
    }

    private int successResponseCode(@NotNull final String method) {
        switch (method) {
            case GET:
                return HttpStatus.SC_OK;

            case PUT:
                return HttpStatus.SC_CREATED;

            case DELETE:
                return HttpStatus.SC_ACCEPTED;

            default:
                return 0;
        }
    }
}
