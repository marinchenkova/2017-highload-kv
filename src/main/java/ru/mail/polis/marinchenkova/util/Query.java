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
                 final int nodesNum) {
        Matcher matcher = QUERY.matcher(query);

        // Query is valid
        if (matcher.matches()) {
            matcher = IDD.matcher(query);

            // Query has no replicas part
            if (matcher.matches()) {
                id = ID.matcher(query).replaceAll("");
                from = nodesNum;
                ack = from / 2 + 1;
            } else {
                String strs[] = query.split("id=|&replicas=|/");
                id = strs[1];
                ack = Integer.parseInt(strs[2]);
                from = Integer.parseInt(strs[3]);
            }

        } else {
            id = null;
            ack = 0;
            from = 0;
        }
    }
}
