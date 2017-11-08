package ru.mail.polis.marinchenkova.util;

import org.jetbrains.annotations.Nullable;

/**
 * @author Marinchenko V. A.
 */
public class Response {
    @Nullable
    public final byte[] data;
    public final int code;

    public Response(final int code,
                    @Nullable final byte[] data) {
        this.code = code;
        this.data = data;
    }
}
