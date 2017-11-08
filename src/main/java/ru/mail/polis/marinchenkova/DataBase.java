package ru.mail.polis.marinchenkova;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

/**
 * @author Marinchenko V. A.
 */
public class DataBase implements IDataBase{

    @NotNull
    private final File path;

    public DataBase(@NotNull final File path){
        this.path = path;
    }

    /**
     * Вернуть значение по ключу, если он есть,
     * выбросить исключение, если его нет.
     * @param key {@link String} ключ
     * @return данные, null если не удалось прочитать
     */
    @Nullable
    public byte[] get(@NotNull final String key) {
        final File file = getFile(key);
        try (InputStream fileInputStream = new FileInputStream(file)) {
            return readByteArray(fileInputStream);

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Добавить значение.
     * @param key {@link String} ключ
     * @param data новые данные
     */
    public boolean upsert(@NotNull final String key,
                       @NotNull final byte[] data) {
        try (OutputStream fileOutputStream = new FileOutputStream(getFile(key))) {
            fileOutputStream.write(data);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Удалить значение по ключу, если он есть,
     * ничего не делать, если значения нет.
     * @param key {@link String} ключ
     */
    public boolean remove(@NotNull final String key) {
        final File file = getFile(key);
        return file.delete();
    }

    private File getFile(@NotNull final String name) {
        return new File(this.path, name);
    }

    @Nullable
    public static byte[] readByteArray(@NotNull final InputStream in) {
        final byte buffer[] = new byte[1024];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            int j;
            while ((j = in.read(buffer)) != -1) {
                out.write(buffer, 0, j);
            }
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }
}
