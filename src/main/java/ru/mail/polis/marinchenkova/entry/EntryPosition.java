package ru.mail.polis.marinchenkova.entry;

/**
 * @author Marinchenko V. A.
 */
public class EntryPosition {

    public final int fileNum;
    public final int lineNum;
    public final int dataCount;
    public final int keyCount;
    public final int sum;
    public final String key;


    public EntryPosition(int file, int lineNum, int keyCount, int dataCount, String key){
        this.fileNum = file;
        this.lineNum = lineNum;
        this.keyCount = keyCount;
        this.dataCount = dataCount;
        sum = keyCount + dataCount + 10;
        this.key = key;
    }
}
