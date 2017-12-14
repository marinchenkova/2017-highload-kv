package ru.mail.polis.marinchenkova.util;

import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class Query {

    private final static Pattern QUERY = Pattern.compile("id=([\\w]*)(&replicas=(\\d*)/(\\d*))?");
    private final static Pattern QUERY_FOR_FILE = Pattern.compile("id=([\\w]*)(&replicas=(\\d*)-(\\d*))?");

    private final String full;
    private final String id;
    private final int ack;
    private final int from;
    
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
    }

    public Query changeParams(final int newAck,
                              final int newFrom,
                              final int size) {
        return new Query("id=" + id + "&replicas=" + newAck + "/" + newFrom, size);
    }

    public String getQueryForFile() {
        return "id=" + this.id + "&replicas=" + this.ack + "-" + this.from;
    }

    public int checkCorrect() {
        if (this.id == null) {
            return HttpStatus.SC_NOT_FOUND;
        } else if (this.id.isEmpty() || this.ack == 0 || this.from == 0 || this.ack > this.from) {
            return  HttpStatus.SC_BAD_REQUEST;
        } return HttpStatus.SC_OK;
    }

    public String getFull() { return this.full; }

    public String getId() { return this.id; }

    public int getAck() { return this.ack; }

    public int getFrom() { return this.from; }

    private int quorum(final int from) { return from / 2 + 1; }
}
