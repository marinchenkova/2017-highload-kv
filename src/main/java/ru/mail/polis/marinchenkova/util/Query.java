package ru.mail.polis.marinchenkova.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class Query {

    private final static Pattern QUERY = Pattern.compile("id=([\\w]*)(&replicas=(\\d*)/(\\d*))?");

    public final String id;
    public final int ack;
    public final int from;

    
    public Query(@NotNull final String query,
                 final int from) {
        final Matcher matcher = QUERY.matcher(query);

        if (matcher.matches()) {
            this.id = matcher.group(1);
            this.from = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : from;
            this.ack = quorum(from);

        } else {
            this.id = null;
            this.ack = 0;
            this.from = 0;
        }
    }

    private int quorum(final int from) {
        return from / 2 + 1;
    }
}
