package ru.mail.polis.marinchenkova;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public interface IDataBase {
    Value get(String id) throws NoSuchElementException, IOException;
    void put(String id, Value val) throws IOException;
    void delete(String id) throws IOException;
}
