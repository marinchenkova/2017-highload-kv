package ru.mail.polis.marinchenkova;

import java.util.Arrays;

/**
 * Запись в базу данных. Структура:
 *
 * <entry>
 * <num>
 *     1
 * </num>
 * <key>
 *     KEY
 * </key>
 * <data>
 *     DATA
 * </data>
 * </entry>
 *
 * @author Marinchenko V. A.
 */
public class Entry {

    private final static String ENTRY_BEGIN = "<entry>";
    private final static String ENTRY_END = "</entry>";

    private final static String NUM_BEGIN = "<num>";
    private final static String NUM_END = "</num>";

    private final static String KEY_BEGIN = "<key>";
    private final static String KEY_END = "</key>";

    private final static String DATA_BEGIN = "<data>";
    private final static String DATA_END = "</data>";

    private final static String SPACE4 = "    ";
    private final static String SPACE8 = "        ";



    private final int num;
    private final String key;
    private final byte[] data;

    private String[] entryStringArray;
    private String[] keyStringArray;
    private String[] dataStringArray;


    public Entry(int num, String key, byte[] data){
        this.num = num;
        this.key = key;
        this.data= data;

        fillArrays();
    }

    public void fillArrays(){

        keyStringArray = keyArray();
        dataStringArray = dataArray();
        entryStringArray = entryArray();
    }

    public static String[] toStringArray(byte[] text){
        int size = text.length / 100 + 1;
        String dataArray[] = new String[size];

        for(int i = 0; i < size - 1; i++){
            dataArray[i] = Arrays.toString(
                    Arrays.copyOfRange(text, i * 100, (i + 1)*100 - 1));
        }

        dataArray[size - 1] = Arrays.toString(
                Arrays.copyOfRange(text, (size - 1)*100, text.length));

        return dataArray;
    }

    public static String[] toStringArray(String text){
        int size = text.length() / 100 + 1;
        String dataArray[] = new String[size];


        for(int i = 0; i < size - 1; i++){
            dataArray[i] = text.substring(i * 100, (i + 1)*100 - 1);
        }

        dataArray[size - 1] = text.substring((size - 1)*100, text.length());

        return dataArray;
    }

    public String[] entryArray(){
        if(entryStringArray == null) {
            if(keyStringArray == null){
                keyStringArray = keyArray();
            } else if (dataStringArray == null) {
                dataStringArray = dataArray();
            }

            String[] allArray = new String[10 + keyStringArray.length + dataStringArray.length];

            allArray[0] = ENTRY_BEGIN;
            allArray[1] = SPACE4 + NUM_BEGIN;
            allArray[2] = SPACE8 + String.valueOf(num);
            allArray[3] = SPACE4 + NUM_END;
            allArray[4] = SPACE4 + KEY_BEGIN;
            int p = 5;

            for(int i = 0; i < keyStringArray.length; i++){
                allArray[p++] = SPACE8 + keyStringArray[i];
            }

            allArray[p++] = SPACE4 + KEY_END;
            allArray[p++] = SPACE4 + DATA_BEGIN;

            for(int i = 0; i < dataStringArray.length; i++){
                allArray[p++] = SPACE8 + dataStringArray[i];
            }

            allArray[p++] = SPACE4 + DATA_END;
            allArray[p++] = ENTRY_END;
            allArray[p] = "";

            entryStringArray = allArray;
        }
        return entryStringArray;
    }

    public String[] dataArray(){
        if(dataStringArray == null) return toStringArray(data);
        else return dataStringArray;
    }

    public String[] keyArray(){
        if(keyStringArray == null) return toStringArray(key);
        else return keyStringArray;
    }

    public int getNum(){
        return num;
    }

    public String getKey(){
        return key;
    }

    public byte[] getData(){
        return data;
    }
}
