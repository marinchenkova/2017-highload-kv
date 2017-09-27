package ru.mail.polis.marinchenkova;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Marinchenko V. A.
 */
public class MVServiceTest {

    @Test
    public void idFromQuery() throws Exception {
        //Check ENTITY
        String id = "abc";
        String prefix = "id=";
        assertEquals(id, MVService.idFromQuery(prefix + id));
        assertEquals("", MVService.idFromQuery(prefix));
    }

}