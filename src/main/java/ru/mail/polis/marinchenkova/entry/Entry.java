package ru.mail.polis.marinchenkova.entry;

import java.util.Arrays;

/**
 * Запись в базу данных. Структура:
 *
 * <entry>
 * <key>
 * KEY
 * </key>
 * <data>
 * DATA
 * </data>
 * </entry>
 *
 * @author Marinchenko V. A.
 */
public class Entry {

    final static String ENTRY_BEGIN = "<entry>";
    final static String ENTRY_END = "</entry>";

    final static String KEY_BEGIN = "<key>";
    final static String KEY_END = "</key>";

    final static String DATA_BEGIN = "<data>";
    final static String DATA_END = "</data>";

    final static int tagsNum = 6;
    private final int helpStringsNum = tagsNum + 1;

    private final String key;
    private final byte[] data;

    public Entry(String key, byte[] data){
        this.key = key;
        this.data= data;
    }

    private static String[] toStringArray(String text, byte[] data){
        boolean isString = text != null;

        int size = (isString ? text.length() : data.length) / 100 + 1;
        String dataArray[] = new String[size];

        for(int i = 0; i < size - 1; i++){
            if(isString) dataArray[i] = text.substring(i * 100, (i + 1)*100 - 1);
            else dataArray[i] = Arrays.toString(Arrays.copyOfRange(data, i * 100, (i + 1)*100));
        }

        if(isString) dataArray[size - 1] = text.substring((size - 1)*100, text.length());
        else dataArray[size - 1] = Arrays.toString(Arrays.copyOfRange(data, (size - 1)*100, data.length));

        return dataArray;
    }

    public static String[] toStringArray(byte data[]) {
        return toStringArray(null, data);
    }

    public static String[] toStringArray(String text) {
        return toStringArray(text, null);
    }


    public String[] entryArray(){
        String[] keyStringArray = toStringArray(key);
        String[] dataStringArray = toStringArray(data);

        String[] allArray = new String[helpStringsNum + keyStringArray.length + dataStringArray.length];

        allArray[0] = ENTRY_BEGIN;
        allArray[1] = KEY_BEGIN;
        int p = 2;

        for(int i = 0; i < keyStringArray.length; i++){
            allArray[p++] = keyStringArray[i];
        }

        allArray[p++] = KEY_END;
        allArray[p++] = DATA_BEGIN;

        for(int i = 0; i < dataStringArray.length; i++){
            allArray[p++] = dataStringArray[i];
        }

        allArray[p++] = DATA_END;
        allArray[p++] = ENTRY_END;
        allArray[p] = "";

        return allArray;
    }

    public String getKey(){
        return key;
    }

    public byte[] getData(){
        return data;
    }
}
