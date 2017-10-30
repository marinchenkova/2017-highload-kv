package ru.mail.polis.marinchenkova.util;

import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class TopologyAgent {

    private final static Pattern LOCALHOST = Pattern.compile("http://localhost:(\\d*)");

    public final int from;

    @NotNull
    private final Set<String> topology;


    public TopologyAgent(@NotNull final Set<String> topology) {
        this.topology = topology;
        this.from = topology.size();
    }

    public Set<HttpServer> getServers() {
        final Set<HttpServer> servers = new LinkedHashSet<>();
        for(String addr : this.topology) {
            try {
                servers.add(HttpServer.create(new InetSocketAddress(getPort(addr)), 0));
            } catch (IOException e) {
                //TODO
            }
        }
        return servers;
    }

    private int getPort(@NotNull final String addr) {
        final Matcher matcher = LOCALHOST.matcher(addr);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : -1;
    }
}
