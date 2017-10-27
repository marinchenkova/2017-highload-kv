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
                 final int defaultAck,
                 final int defaultFrom) {
        Matcher matcher = QUERY.matcher(query);

        // Query is valid
        if (matcher.matches()) {
            matcher = IDD.matcher(query);

            // Query has no replicas part
            if (matcher.matches()) {
                id = ID.matcher(query).replaceAll("");
                ack = defaultAck;
                from = defaultFrom;
            } else {
                String strs[] = query.split("id=|&replicas=|\\/");
                id = strs[0];
                ack = Integer.parseInt(strs[1]);
                from = Integer.parseInt(strs[2]);
            }

        } else {
            id = null;
            ack = 0;
            from = 0;
        }
    }

    public Query(@NotNull final String query) {
        this(query, 1, 1);
    }
}
