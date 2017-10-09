package ru.mail.polis.marinchenkova.entry;

import java.io.File;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class EntryReadWriteAgent {

    private int cnt = 0;

    private EntryWriter entryWriter;
    private EntryReader entryReader;

    public EntryReadWriteAgent(File pathDB){
        RandomAccessDBAgent agent = new RandomAccessDBAgent(pathDB);
        entryReader = new EntryReader(agent);
        entryWriter = new EntryWriter(agent);
    }

    public boolean containsKey(String key) throws NoSuchElementException  {
        return entryReader.containsKey(key);
    }

    public byte[] read(String key) throws NoSuchElementException {
        return entryReader.read(key);
    }


    public void writeEntry(String key, byte[] data) {
        entryWriter.writeEntry(new Entry(cnt++, key, data));
    }

    public void rewriteEntry(String key, byte[] data) {
        entryWriter.rewriteEntry(entryReader, new Entry(cnt, key, data));
    }

    public boolean remove(String key) {
        if(entryWriter.remove(entryReader, key)) {
            cnt--;
            return true;
        } else return false;
    }
}
