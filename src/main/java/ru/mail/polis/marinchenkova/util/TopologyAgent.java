package ru.mail.polis.marinchenkova.util;

import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.marinchenkova.DataBase;
import ru.mail.polis.marinchenkova.IDataBase;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.mail.polis.marinchenkova.MVService.*;
import static ru.mail.polis.marinchenkova.util.Response.*;

/**
 * @author Marinchenko V. A.
 */
public class TopologyAgent {

    public final int size;

    private final static int CONNECT_TIMEOUT = 100;

    private final static Pattern LOCALHOST = Pattern.compile("http://localhost:(\\d*)");

    @NotNull
    private final String local;
    @NotNull
    private final Set<String> topology;
    @NotNull
    private final DataBase dataBase;
    @NotNull
    private final Deque<Query> missedWrites = new ArrayDeque<>();

    public TopologyAgent(@NotNull final Set<String> topology,
                         @NotNull final IDataBase dataBase,
                         final int port) {
        this.size = topology.size();
        this.topology = topology;
        this.dataBase = (DataBase) dataBase;
        this.local = getLocal(port);
    }

    public void start() {
        initMissedWrites();
        requestMissedWrites();
    }

    @NotNull
    public Response process(final int masterResponseCode,
                             @Nullable final byte[] data,
                             @NotNull final Query query,
                             @NotNull final String method) {
        int responsesNum = responseNumInit(masterResponseCode, method);
        int failedConnectionsNum = 0;
        final Response response;

        if (this.size == 1) response = new Response(masterResponseCode, data);
        else {
            for (String addr : this.topology) {
                if (query.from - failedConnectionsNum < query.ack) break;
                if (method.equals(PUT) && responsesNum >= query.from) break;

                final String globalAddr = addr.replace("localhost", "127.0.0.1");
                if (globalAddr.equals(this.local)) continue;

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

                        if (masterResponseCode == HttpStatus.SC_PARTIAL_CONTENT) {
                            request.setRequestProperty(PARTIAL, SLAVE);
                        } else request.setRequestProperty(REPLICA, SLAVE);

                        responsesNum += processMethod(method, request, data);

                    } else {
                        failedConnectionsNum++;
                    }

                } catch (Exception e) {
                    failedConnectionsNum++;
                }
            }

            if (method.equals(PUT) && responsesNum < query.from) {
                addMissedWrite(query, query.from - responsesNum);
            }

            if (method.equals(GET) && responsesNum < query.ack && this.size - failedConnectionsNum >= query.ack) {
                response = new Response(HttpStatus.SC_NOT_FOUND, null);
            } else if (responsesNum < query.ack) {
                response = new Response(HttpStatus.SC_GATEWAY_TIMEOUT, null);
            } else response = new Response(successResponseCode(method), data);
        }

        return response;
    }

    private int processMethod(@NotNull final String method,
                              @NotNull final HttpURLConnection request,
                              @Nullable final byte[] data) {
        int responsesNum = 0;
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
        return responsesNum;
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

    private void requestMissedWrites() {
        final String strQuery = "id=missed&replicas=1/" + this.size;
        final Query getQuery = new Query(strQuery, this.size);
        process(
                HttpStatus.SC_PARTIAL_CONTENT,
                null,
                getQuery,
                GET
        );
    }

    private void addMissedWrite(@NotNull final Query query,
                                final int left) {
        final Query missed = query.changeParams(left, left, this.size);
        this.missedWrites.add(missed);
        this.dataBase.upsertMissedWrite(missed.getQueryForFile());
    }

    private void deleteMissedWrite(@NotNull final Query query) {
        this.dataBase.removeMissedWrite(query.getQueryForFile());
    }

    private void initMissedWrites() {
        final String[] fullQueries = this.dataBase.getMissedWrites();
        final List<Query> queries = new ArrayList<>();
        for (String fullQuery : fullQueries) {
            queries.add(new Query(fullQuery, this.size));
        }
        missedWrites.addAll(queries);
    }

    public void sendMissedWrites() {
        if (this.missedWrites.size() > 0) {
            for (int i = 0; i < this.missedWrites.size(); i++) {
                final Query missed = this.missedWrites.poll();
                final byte[] dataCheckMaster = this.dataBase.get(missed.id);

                if (dataCheckMaster != null) {
                    final Response putResponse = process(
                            HttpStatus.SC_INTERNAL_SERVER_ERROR,
                            dataCheckMaster,
                            missed,
                            PUT
                    );
                    if (putResponse.code == HttpStatus.SC_CREATED) {
                        deleteMissedWrite(missed);
                    }
                }
            }
        }
    }

    private String getLocal(final int port) {
        for (String addr : topology) {
            if (getPort(addr) == port) {
                return addr.replace("localhost", "127.0.0.1");
            }
        }
        return "";
    }

    private int getPort(@NotNull final String addr) {
        final Matcher matcher = LOCALHOST.matcher(addr);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
    }
}
