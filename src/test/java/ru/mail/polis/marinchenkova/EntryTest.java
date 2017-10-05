package ru.mail.polis.marinchenkova;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.mail.polis.marinchenkova.entry.Entry;

import java.io.IOException;

import static ru.mail.polis.TestBase.randomKey;
import static ru.mail.polis.TestBase.randomValue;

/**
 * @author Marinchenko V. A.
 */
public class EntryTest {

    private static final String DATA = "DATA:";
    private static final String KEY = "KEY:";
    private static final String ENTRY = "ENTRY:";


    private static Entry entry;
    private static int num;
    private static String key;
    private static byte[] data;

    @BeforeClass
    public static void beforeAll() throws IOException, InterruptedException {
        num = 1;
        key = randomKey();
        data = randomValue();

        entry = new Entry(num, key, data);
    }

    @Test
    public void bytesToStringArray() throws Exception {
        printArray(Entry.toStringArray(key), KEY);
        printArray(Entry.toStringArray(data), DATA);
    }

    @Test
    public void entryArray() throws Exception {
        printArray(entry.entryArray(), ENTRY);
    }

    @Test
    public void keyArray() throws Exception {
        printArray(entry.keyArray(), KEY);
    }

    @Test
    public void dataArray() throws Exception {
        printArray(entry.dataArray(), DATA);
    }

    private void printArray(String[] arr, String what){
        System.out.println("\n*********");
        System.out.println(what);
        for(String s: arr) {
            System.out.println(s);
        }
        System.out.println("*********\n");
    }
}