package ru.mail.polis.marinchenkova.entry;

/**
 * @author Marinchenko V. A.
 */
public class EntryReadWriteAgent {

    private int cnt = 0;

    private EntryWriter entryWriter;
    private EntryReader entryReader;

    public EntryReadWriteAgent(){
        entryReader = new EntryReader();
        entryWriter = new EntryWriter();
    }

    public boolean containsKey(String key) {
        return entryReader.containsKey(key);
    }

    public byte[] read(String key) {
        return entryReader.read(key);
    }


    public void writeEntry(String key, byte[] data) {
        entryWriter.writeEntry(new Entry(cnt++, key, data));
    }

    public void rewriteEntry(String key, byte[] data) {
        entryWriter.rewriteEntry(entryReader, new Entry(cnt, key, data));
    }

    public void remove(String key) {
        if(entryWriter.remove(entryReader, key)) cnt--;
    }
}
