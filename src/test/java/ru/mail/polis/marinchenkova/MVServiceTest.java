package ru.mail.polis.marinchenkova;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Marinchenko V. A.
 */
public class MVServiceTest {

    private final static String ENTITY = "/v0/entity?id=";
    private final static String STATUS = "/v0/status";


    private final static String ONLINE = "ONLINE";
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String DELETE = "DELETE";
    private final static String INCORRECT_REQUEST = "INCORRECT_REQUEST";

    @Test
    public void idFromURI() throws Exception {
        //Check ONLINE
        assertEquals(ONLINE, MVService.idFromURI(STATUS));
        assertEquals(ONLINE, MVService.idFromURI(STATUS + "a"));

        //Check ENTITY
        String id = "abc";
        assertEquals(id, MVService.idFromURI(ENTITY + id));
        assertEquals("", MVService.idFromURI(ENTITY));
    }

}