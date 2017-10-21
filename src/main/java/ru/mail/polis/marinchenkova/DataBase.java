package ru.mail.polis.marinchenkova;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mail.polis.marinchenkova.entry.EntryReadWriteAgent;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class DataBase implements IDataBase{

    private final EntryReadWriteAgent agent;

    public DataBase(@NotNull final File pathDB){
        this.agent = new EntryReadWriteAgent(pathDB);
    }

    /**
     * Вернуть значение по ключу, если он есть,
     * выбросить исключение, если его нет.
     * @param key {@link String} ключ
     * @return данные
     * @throws NoSuchElementException если такого ключа нет
     */
    @Nullable
    public byte[] get(@NotNull final String key) throws IOException, NoSuchElementException {
        if (this.agent.containsKey(key)) return this.agent.read(key);
        else throw new NoSuchElementException("Can not return: no such element");
    }

    /**
     * Добавить значение.
     * @param key {@link String} ключ
     * @param data новые данные
     */
    public void upsert(@NotNull final String key,
                       final byte[] data) throws IOException {
        if (this.agent.containsKey(key)) {
            this.agent.rewriteEntry(key, data);
        } else this.agent.writeEntry(key, data);
    }

    /**
     * Удалить значение по ключу, если он есть,
     * ничего не делать, если значения нет.
     * @param key {@link String} ключ
     */
    public void remove(@NotNull final String key) throws IOException {
        this.agent.remove(key);
    }

}
