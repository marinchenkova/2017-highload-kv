package ru.mail.polis.marinchenkova.entry;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class EntryWriter {

    private RandomAccessDBAgent agent;

    public EntryWriter(RandomAccessDBAgent agent){
        this.agent = agent;
    }

    public void writeEntry(Entry entry) {
        try {
            agent.open(RandomAccessDBAgent.MODE_WRITE);
            agent.writeArray(entry.entryArray());
            agent.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rewriteEntry(EntryReader er, Entry newEntry) {
        //if(remove(er, newEntry.getKey())) writeEntry(newEntry);
    }

    public boolean remove(EntryReader eReader, String key) {
        try {
            EntryPosition ep = eReader.search(key);
            agent.open(RandomAccessDBAgent.MODE_FULL);
            agent.removeEntry(key, ep);
            agent.close();
            return true;

        } catch (NoSuchElementException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
