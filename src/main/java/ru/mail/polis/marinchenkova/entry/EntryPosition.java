package ru.mail.polis.marinchenkova.entry;

import static ru.mail.polis.marinchenkova.entry.Entry.tagsNum;

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

    public EntryPosition(int fileNum, int lineNum) {
        this.fileNum = fileNum;
        this.lineNum = lineNum;
    }

    public void setBody(String key, int keyCount, int dataCount){
        this.key = key;
        this.keyCount = keyCount;
        this.dataCount = dataCount;
        this.sum = keyCount + dataCount + tagsNum;
    }
}
