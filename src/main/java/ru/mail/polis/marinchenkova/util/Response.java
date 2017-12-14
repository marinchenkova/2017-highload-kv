package ru.mail.polis.marinchenkova.util;

import org.apache.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static ru.mail.polis.marinchenkova.MVService.DELETE;
import static ru.mail.polis.marinchenkova.MVService.GET;
import static ru.mail.polis.marinchenkova.MVService.PUT;

/**
 * @author Marinchenko V. A.
 */
public class Response {
    @Nullable
    private final byte[] data;
    private final int code;

    public Response(final int code,
                    @Nullable final byte[] data) {
        this.code = code;
        this.data = data;
    }

    public static int successResponseCode(@NotNull final String method) {
        switch (method) {
            case GET:
                return HttpStatus.SC_OK;

            case PUT:
                return HttpStatus.SC_CREATED;

            case DELETE:
                return HttpStatus.SC_ACCEPTED;

            default:
                return 0;
        }
    }

    public static int responseNumInit(final int masterResponseCode,
                                      @NotNull final String method) {
        switch (method) {
            case GET:
                return masterResponseCode == HttpStatus.SC_OK ? 1 : 0;

            case PUT:
                return masterResponseCode == HttpStatus.SC_CREATED ? 1 : 0;

            case DELETE:
                return masterResponseCode == HttpStatus.SC_ACCEPTED ? 1 : 0;

            default:
                return 0;
        }
    }

    @Nullable
    public byte[] getData() {
        return data;
    }

    public int getCode() {
        return code;
    }
}
