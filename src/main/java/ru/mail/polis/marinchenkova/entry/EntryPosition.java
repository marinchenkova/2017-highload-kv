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

    public EntryPosition(String key, int fileNum, int lineNum) {
        this.key = key;
        this.fileNum = fileNum;
        this.lineNum = lineNum;
    }

    public void setBody(int keyCount, int dataCount){
        this.keyCount = keyCount;
        this.dataCount = dataCount;
        sum = keyCount + dataCount + 9;
    }
}
