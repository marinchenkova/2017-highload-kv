package ru.mail.polis.marinchenkova;

import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class DataBase {
    //TODO DataBase
    private ArrayList<String> keys = new ArrayList<>();
    private ArrayList<Value> vals = new ArrayList<>();

    public DataBase(){

    }

    /**
     * Вернуть значение по ключу, если он есть,
     * выбросить исключение, если его нет.
     * @param key {@link String} ключ
     * @return {@link Value} значение
     * @throws NoSuchElementException если такого ключа нет
     */
    public Value get(String key) throws NoSuchElementException{
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i).equals(key)) return vals.get(i);
        }
        throw new NoSuchElementException("Can not return: no such element");
    }

    /**
     * Добавить значение {@link Value}.
     * @param key {@link String} ключ
     * @param val {@link Value} новое значение
     */
    public void put(String key, Value val){
        keys.add(key);
        vals.add(val);
    }

    /**
     * Удалить значение по ключу, если он есть,
     * ничего не делать, если значения нет.
     * @param key {@link String} ключ
     */
    public void delete(String key) {
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i).equals(key)) {
                keys.remove(i);
                vals.remove(i);
            }
        }
    }

}
