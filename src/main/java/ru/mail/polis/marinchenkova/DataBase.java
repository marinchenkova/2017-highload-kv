package ru.mail.polis.marinchenkova;

import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class DataBase implements IDataBase{
    //TODO DataBase

    private HashMap<String, Value> map = new HashMap<>();

    public DataBase(){
    }

    /**
     * Вернуть значение по ключу, если он есть,
     * выбросить исключение, если его нет.
     * @param key {@link String} ключ
     * @return {@link Value} значение
     * @throws NoSuchElementException если такого ключа нет
     */
    public Value get(String key) throws NoSuchElementException, IOException{
        if(map.containsKey(key)) return map.get(key);
        else throw new NoSuchElementException("Can not return: no such element");
    }

    /**
     * Добавить значение {@link Value}.
     * @param key {@link String} ключ
     * @param val {@link Value} новое значение
     */
    public void put(String key, Value val) throws IOException {
        map.put(key, val);
    }

    /**
     * Удалить значение по ключу, если он есть,
     * ничего не делать, если значения нет.
     * @param key {@link String} ключ
     */
    public void delete(String key) throws IOException {
        map.remove(key);
    }

}
