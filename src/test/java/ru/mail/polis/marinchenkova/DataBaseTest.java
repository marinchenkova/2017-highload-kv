package ru.mail.polis.marinchenkova;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.*;
import static ru.mail.polis.TestBase.randomKey;
import static ru.mail.polis.TestBase.randomValue;

/**
 * @author Marinchenko V. A.
 */
public class DataBaseTest {
    private DataBase dataBase = new DataBase();

    @Test
    public void getPutTest() throws Exception {
        String key = randomKey();
        byte data[] = randomValue();

        Value val = new Value(data);
        Value empty = new Value(new byte[0]);

        dataBase.put(key, val);
        assertArrayEquals(val.getBytes(), dataBase.get(key).getBytes());

        dataBase.put(key, empty);
        assertArrayEquals(empty.getBytes(), dataBase.get(key).getBytes());
    }

    @Test
    public void delete() throws Exception {

    }

}