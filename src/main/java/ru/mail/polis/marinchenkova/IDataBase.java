package ru.mail.polis.marinchenkova;

import org.jetbrains.annotations.NotNull;
import ru.mail.polis.marinchenkova.util.Query;
import java.util.Map;
import java.util.Set;

/**
 * @author Marinchenko V. A.
 */
public interface IDataBase {
    byte[] get(@NotNull final String key);
    boolean upsert(@NotNull final String key,
                   @NotNull final byte[] data);
    boolean remove(@NotNull final String key);
    Map<Query, Set<String>> getMissedWrites(final int size);
    boolean upsertMissedWrite(@NotNull final String key,
                              @NotNull final Set<String> failed);
    boolean removeMissedWrite(@NotNull final String key);
}
