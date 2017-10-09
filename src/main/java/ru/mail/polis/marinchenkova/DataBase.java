package ru.mail.polis.marinchenkova;

import org.jetbrains.annotations.NotNull;
import ru.mail.polis.marinchenkova.entry.Entry;
import ru.mail.polis.marinchenkova.entry.EntryReadWriteAgent;
import ru.mail.polis.marinchenkova.entry.EntryReader;
import ru.mail.polis.marinchenkova.entry.EntryWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Marinchenko V. A.
 */
public class DataBase implements IDataBase{
    //TODO DataBase

    @NotNull
    public static String randomKey() {
        return Long.toHexString(ThreadLocalRandom.current().nextLong());
    }

    @NotNull
    public static byte[] randomValue(int bytes) {
        final byte[] result = new byte[bytes];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    private EntryReadWriteAgent agent;

    public DataBase(){
        agent = new EntryReadWriteAgent();
    }


    /**
     * Вернуть значение по ключу, если он есть,
     * выбросить исключение, если его нет.
     * @param key {@link String} ключ
     * @return данные
     * @throws NoSuchElementException если такого ключа нет
     */
    public byte[] get(String key) throws NoSuchElementException, IOException{
        if(agent.containsKey(key)) return agent.read(key);
        else throw new NoSuchElementException("Can not return: no such element");
    }

    /**
     * Добавить значение {@link Value}.
     * @param key {@link String} ключ
     * @param data новые данные
     */
    public void put(String key, byte[] data) throws IOException {
        if(agent.containsKey(key)) {
            agent.rewriteEntry(key, data);
        } else agent.writeEntry(key, data);
    }

    /**
     * Удалить значение по ключу, если он есть,
     * ничего не делать, если значения нет.
     * @param key {@link String} ключ
     */
    public void delete(String key) throws IOException {
       agent.remove(key);
    }

}
