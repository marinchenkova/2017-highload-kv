package ru.mail.polis.marinchenkova;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.marinchenkova.util.Util;

import java.io.*;

/**
 * @author Marinchenko V. A.
 */
public class DataBase implements IDataBase{

    private final static String MISSED_WRITE_DIR = "missedWrites";

    @NotNull
    private final File missedWritePath;
    @NotNull
    private final File path;

    public DataBase(@NotNull final File path){
        this.path = path;
        this.missedWritePath = initDirMissedWrite(path);
    }

    /**
     * Вернуть значение по ключу, если он есть,
     * выбросить исключение, если его нет.
     * @param key {@link String} ключ
     * @return данные, null если не удалось прочитать
     */
    @Nullable
    public byte[] get(@NotNull final String key) {
        return get(key, false);
    }

    /**
     * Добавить значение.
     * @param key {@link String} ключ
     * @param data новые данные
     */
    public boolean upsert(@NotNull final String key,
                          @NotNull final byte[] data) {
        return upsert(key, data, false);
    }

    /**
     * Удалить значение по ключу, если он есть,
     * ничего не делать, если значения нет.
     * @param key {@link String} ключ
     */
    public boolean remove(@NotNull final String key) {
        return remove(key, false);
    }

    private File getFile(@NotNull final String name,
                         final boolean missedWrite) {
        final File file;
        if (missedWrite) file = new File(this.missedWritePath, name);
        else file = new File(this.path, name);
        return file;
    }

    private File initDirMissedWrite(@NotNull final File path) {
        final File dir = new File(path, MISSED_WRITE_DIR);
        if (!dir.exists()) dir.mkdir();
        return dir;
    }

    public String[] getMissedWrites() {
        final File[] missedWriteFiles = this.missedWritePath.listFiles();
        final String[] missedWrites = new String[missedWriteFiles.length];
            for (int i = 0; i < missedWriteFiles.length; i++) {
                missedWrites[i] = missedWriteFiles[i].getName();
            }
        return missedWrites;
    }

    public boolean upsertMissedWrite(@NotNull final String key) {
        return upsert(key, new byte[]{}, true);
    }

    public boolean removeMissedWrite(@NotNull final String key) {
        return remove(key, true);
    }

    @Nullable
    private byte[] get(@NotNull final String key,
                       final boolean missedWrite) {
        final File file = getFile(key, missedWrite);

        try (InputStream fileInputStream = new FileInputStream(file)) {
            return Util.readByteArray(fileInputStream);

        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return null;
        }
    }

    private boolean upsert(@NotNull final String key,
                           @NotNull final byte[] data,
                           final boolean missedWrite) {
        final File file = getFile(key, missedWrite);

        try (OutputStream fileOutputStream = new FileOutputStream(file)) {
            fileOutputStream.write(data);
            return true;
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    private boolean remove(@NotNull final String key,
                           final boolean missedWrite) {
        final File file = getFile(key, missedWrite);
        return file.delete();
    }
}
