package ru.mail.polis.marinchenkova.util;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marinchenko V. A.
 */
public class Query {

    private final static Pattern QUERY = Pattern.compile("id=[\\w]*(&replicas=\\d*\\/\\d*)?");
    private final static Pattern IDD = Pattern.compile("id=[\\w]*");
    private final static Pattern ID = Pattern.compile("id=");

    public final String id;
    public final int ack;
    public final int from;

    public Query(@NotNull final String query,
                 final int from) {
        Matcher matcher = QUERY.matcher(query);

        // Query is valid
        if (matcher.matches()) {
            matcher = IDD.matcher(query);

            // Query has no replicas part
            if (matcher.matches()) {
                this.id = ID.matcher(query).replaceAll("");
                this.from = from;
                this.ack = quorum(from);
            } else {
                String strs[] = query.split("id=|&replicas=|/");
                this.id = strs[1];
                this.ack = Integer.parseInt(strs[2]);
                this.from = Integer.parseInt(strs[3]);
            }

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
