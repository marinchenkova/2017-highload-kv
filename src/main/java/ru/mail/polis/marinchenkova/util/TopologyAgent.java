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
    private final static Pattern LOCALHOST = Pattern.compile("http://127\\.0\\.0\\.1:(\\d*)");

    @NotNull
    private final String local;
    @NotNull
    private final Set<String> topology;
    @NotNull
    private final Set<String> topologyGlobal;
    @NotNull
    private final IDataBase dataBase;
    @NotNull
    private final Map<Query, Set<String>> missedWrites = new HashMap<>();

    public TopologyAgent(@NotNull final Set<String> topology,
                         @NotNull final IDataBase dataBase,
                         final int port) {
        this.size = topology.size();
        this.topology = topology;
        this.topologyGlobal = getGlobal();
        this.dataBase = dataBase;
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
        return process(
                masterResponseCode,
                data,
                query,
                method,
                this.topologyGlobal);
    }


    @NotNull
    private Response process(final int masterResponseCode,
                            @Nullable final byte[] data,
                            @NotNull final Query query,
                            @NotNull final String method,
                            @NotNull final Set<String> addrs) {
        int responsesNum = responseNumInit(masterResponseCode, method);
        int failedConnectionsNum = 0;
        final int ack = query.getAck();
        final int from = query.getFrom();
        final Response response;

        if (this.size == 1) response = new Response(masterResponseCode, data);
        else {
            final Set<String> failedAddrs = new HashSet<>();
            for (String addr : addrs) {
                if (from - failedConnectionsNum < ack) break;
                if (method.equals(PUT) && responsesNum >= from) break;
                if (addr.equals(this.local)) continue;

                try {
                    final URL urlStatus = new URL(addr + STATUS);
                    final HttpURLConnection connection = (HttpURLConnection) urlStatus.openConnection();
                    connection.setConnectTimeout(CONNECT_TIMEOUT);
                    connection.connect();

                    if (connection.getResponseCode() == HttpStatus.SC_OK) {
                        final URL urlEntity = new URL(addr + ENTITY + "?" + query.getFull());
                        final HttpURLConnection request = (HttpURLConnection) urlEntity.openConnection();
                        request.setConnectTimeout(CONNECT_TIMEOUT);
                        request.setRequestMethod(method);

                        if (masterResponseCode == HttpStatus.SC_PARTIAL_CONTENT) {
                            request.setRequestProperty(PARTIAL, SLAVE);
                        } else request.setRequestProperty(REPLICA, SLAVE);

                        responsesNum += processMethod(method, request, data);

                    } else {
                        failedConnectionsNum++;
                        failedAddrs.add(addr);
                    }

                } catch (Exception e) {
                    System.err.println("Inner connection to " + addr + " failed: " + e.getMessage());
                    failedConnectionsNum++;
                    failedAddrs.add(addr);
                }
            }

            if (method.equals(PUT) && responsesNum < from) {
                addMissedWrite(query, failedAddrs, from - responsesNum);
            }

            if (method.equals(GET) && responsesNum < ack && this.size - failedConnectionsNum >= ack) {
                response = new Response(HttpStatus.SC_NOT_FOUND, null);
            } else if (responsesNum < ack) {
                response = new Response(HttpStatus.SC_GATEWAY_TIMEOUT, null);
            } else response = new Response(successResponseCode(method), data);
        }

        return response;
    }


    private int processMethod(@NotNull final String method,
                              @NotNull final HttpURLConnection connection,
                              @Nullable final byte[] data) {
        try {
            if (method.equals(PUT)) {
                connection.setDoOutput(true);
                OutputStream out = new DataOutputStream(connection.getOutputStream());
                out.write(data == null ? new byte[]{} : data);
                out.flush();
            }

            connection.connect();
            final int code = connection.getResponseCode();
            connection.disconnect();

            if (code == Response.successResponseCode(method)) return 1;

        } catch (IOException e) {
            System.err.println("Inner request error: " + e.getMessage());
            return 0;
        }

        return 0;
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
                                @NotNull Set<String> failed,
                                final int left) {
        final Query missed = query.changeParams(left, left, this.size);
        this.missedWrites.put(missed, failed);
        this.dataBase.upsertMissedWrite(missed.getQueryForFile(), failed);
    }

    private void deleteMissedWrite(@NotNull final Query query) {
        this.missedWrites.remove(query);
        this.dataBase.removeMissedWrite(query.getQueryForFile());
    }

    private void initMissedWrites() {
        this.missedWrites.putAll(this.dataBase.getMissedWrites(this.size));
    }

    public void sendMissedWrites() {
        missedWrites.forEach((missed, addrs) -> {
            final byte[] dataCheckMaster = this.dataBase.get(missed.getId());
            if (dataCheckMaster != null) {
                final Response putResponse = process(
                        HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        dataCheckMaster,
                        missed,
                        PUT,
                        addrs
                );
                if (putResponse.getCode() == HttpStatus.SC_CREATED) deleteMissedWrite(missed);
            }
        });
    }

    private Set<String> getGlobal() {
        final Set<String> global = new HashSet<>();
        for (String addr : topology) {
            global.add(addr.replace("localhost", "127.0.0.1"));
        }
        return global;
    }

    private String getLocal(final int port) {
        for (String addr : topologyGlobal) {
            if (getPort(addr) == port) return addr;
        }
        return "";
    }

    private int getPort(@NotNull final String addr) {
        final Matcher matcher = LOCALHOST.matcher(addr);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
    }
}
