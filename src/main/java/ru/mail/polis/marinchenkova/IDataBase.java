package ru.mail.polis.marinchenkova;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public interface IDataBase {
    byte[] get(String key) throws NoSuchElementException, IOException;
    void put(String key, byte[] data) throws IOException;
    void delete(String key) throws IOException;
}
