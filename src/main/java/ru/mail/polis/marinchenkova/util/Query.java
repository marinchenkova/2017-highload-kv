package ru.mail.polis.marinchenkova.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class Query {

    private final static Pattern QUERY = Pattern.compile("id=([\\w]*)(&replicas=(\\d*)/(\\d*))?");
    public final static String MSG_NOT_FOUND = "not found";
    public final static String MSG_BAD_REQUEST = "bad request";

    public final String full;
    public final String id;
    public final int ack;
    public final int from;
    
    public Query(@NotNull final String query,
                 final int from) throws IllegalArgumentException {
        full = query;
        final Matcher matcher = QUERY.matcher(query);

        if (matcher.matches()) {
            this.id = matcher.group(1);
            this.from = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : from;
            this.ack = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : quorum(from);
        } else {
            this.id = null;
            this.ack = 0;
            this.from = 0;
        }

        checkCorrect();
    }

    private void checkCorrect() throws IllegalArgumentException {
        if (this.id == null) {
            throw new IllegalArgumentException(MSG_NOT_FOUND);
        } else if (this.id.isEmpty() || this.ack == 0 || this.from == 0 || this.ack > this.from) {
            throw new IllegalArgumentException(MSG_BAD_REQUEST);
        }
    }

    private int quorum(final int from) {
        return from / 2 + 1;
    }
}
