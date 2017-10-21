package ru.mail.polis.marinchenkova;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public interface IDataBase {
    byte[] get(String key) throws NoSuchElementException, IOException;
    void upsert(String key, byte[] data) throws IOException;
    void remove(String key) throws IOException;
}
