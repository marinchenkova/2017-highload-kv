package ru.mail.polis.marinchenkova;

/**
 * @author Marinchenko V. A.
 */
public class Value {
    private final byte[] data;
    public Value(byte[] data){
        this.data = data;
    }
    public byte[] getBytes(){
        return data;
    }
    @Override
    public String toString(){
        return "VALUE";
    }
}
