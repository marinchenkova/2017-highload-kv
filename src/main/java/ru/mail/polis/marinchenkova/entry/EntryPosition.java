package ru.mail.polis.marinchenkova.entry;

/**
 * @author Marinchenko V. A.
 */
public class EntryPosition {

    public int fileNum;
    public int lineNum;
    public int dataCount;
    public int keyCount;
    public int sum;
    public String key;

    public EntryPosition(int fileNum) {
        this.fileNum = fileNum;
    }

    public void set(int lineNum, int keyCount, int dataCount, String key){
        this.lineNum = lineNum;
        this.keyCount = keyCount;
        this.dataCount = dataCount;
        sum = keyCount + dataCount + 10;
        this.key = key;
    }
}
