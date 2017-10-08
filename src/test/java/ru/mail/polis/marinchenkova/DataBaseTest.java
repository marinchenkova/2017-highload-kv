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
        byte empty[] = new byte[0];

        dataBase.put(key, data);
        assertArrayEquals(data, dataBase.get(key));

        dataBase.put(key, empty);
        assertArrayEquals(empty, dataBase.get(key));
    }

    @Test
    public void delete() throws Exception {

    }

}