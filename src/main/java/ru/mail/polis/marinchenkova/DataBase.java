package ru.mail.polis.marinchenkova;

import ru.mail.polis.marinchenkova.entry.EntryReadWriteAgent;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class DataBase implements IDataBase{

    private EntryReadWriteAgent agent;

    public DataBase(File pathDB){
        agent = new EntryReadWriteAgent(pathDB);
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
     * Добавить значение.
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
