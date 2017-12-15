package ru.mail.polis.marinchenkova.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Marinchenko V. A.
 */
public class Util {
    @Nullable
    public static byte[] readByteArray(@NotNull final InputStream in) {
        final byte buffer[] = new byte[1024];
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()){
            int j;
            while ((j = in.read(buffer)) != -1) out.write(buffer, 0, j);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    public static Set<String> parseMissedWriteAddrs(@Nullable final byte[] data) {
        final String whole = new String(data == null ? new byte[]{} : data);
        final String addrs[] = whole.split("\\?");
        return new HashSet<>(Arrays.asList(addrs));
    }
}
