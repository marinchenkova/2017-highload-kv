package ru.mail.polis.marinchenkova.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class Query {

    public final static String MSG_NOT_FOUND = "not found";
    public final static String MSG_BAD_REQUEST = "bad request";

    private final static Pattern QUERY = Pattern.compile("id=([\\w]*)(&replicas=(\\d*)/(\\d*))?");
    private final static Pattern QUERY_FOR_FILE = Pattern.compile("id=([\\w]*)(&replicas=(\\d*)-(\\d*))?");

    public final String full;
    public final String id;
    public final int ack;
    public final int from;
    
    public Query(@NotNull final String query,
                 final int size) throws IllegalArgumentException {
        full = query;
        final Matcher matcher = QUERY.matcher(query);
        final Matcher matcher2 = QUERY_FOR_FILE.matcher(query);
        final Matcher need = matcher.matches() ? matcher : matcher2.matches() ? matcher2 : null;

        if (need != null) {
            this.id = need.group(1);
            this.from = need.group(4) != null ? Integer.parseInt(need.group(4)) : size;
            this.ack = need.group(3) != null ? Integer.parseInt(need.group(3)) : quorum(size);
        } else {
            this.id = null;
            this.ack = 0;
            this.from = 0;
        }

        checkCorrect();
    }

    public Query changeParams(final int newAck,
                              final int newFrom,
                              final int size) {
        return new Query("id=" + id + "&replicas=" + newAck + "/" + newFrom, size);
    }

    public String getQueryForFile() {
        return "id=" + this.id + "&replicas=" + this.ack + "-" + this.from;
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
