package ru.mail.polis.marinchenkova;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public interface IDataBase {
    byte[] get(@NotNull final String key);
    boolean upsert(@NotNull final String key,
                @NotNull final byte[] data);
    boolean remove(@NotNull final String key);
}
