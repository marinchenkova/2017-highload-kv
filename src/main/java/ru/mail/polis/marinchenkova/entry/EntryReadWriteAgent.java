package ru.mail.polis.marinchenkova.entry;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static ru.mail.polis.marinchenkova.entry.Entry.*;
import static ru.mail.polis.marinchenkova.entry.Entry.KEY_BEGIN;
import static ru.mail.polis.marinchenkova.entry.Entry.KEY_END;
import static ru.mail.polis.marinchenkova.entry.RandomAccessDBAgent.MODE_READ;

/**
 * @author Marinchenko V. A.
 */
public class EntryReadWriteAgent {

    private EntryPosition lastPos;
    private final RandomAccessDBAgent agent;


    public EntryReadWriteAgent(File pathDB){
        this.agent = new RandomAccessDBAgent(pathDB);
    }


    public boolean containsKey(String key) {
        try {
            search(key, false);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public byte[] read(String key) throws NoSuchElementException {
        if(lastPos != null && lastPos.key.equals(key)) {
            lastPos = null;
            return search(key, true);
        } else {
            lastPos = null;
            return search(key, false);
        }
    }

    public EntryPosition search(String key) throws NoSuchElementException {
        search(key, false);
        return lastPos;
    }

    private byte[] search(String  key, boolean goOn) throws NoSuchElementException {
        String rawKey = "";
        String rawData = "";

        boolean data = false;
        boolean keyFound = false;

        int dataCount = 0;
        int keyCount = 0;

        try {
            agent.open(MODE_READ);

            if(goOn && lastPos != null) agent.setReaderToPosition(lastPos);
            else agent.setReaderToBegin();

            String line = agent.readLine().trim();

            while (line != null) {

                if (keyFound) {
                    if (data && !line.equals(DATA_END)) {
                        dataCount++;
                        rawData = rawData.concat(line);
                    }

                    if (line.equals(DATA_BEGIN)) data = true;
                    else if (line.equals(DATA_END)) {
                        lastPos.setBody(key, keyCount, dataCount);
                        agent.close();
                        return parseData(rawData);
                    }

                } else {
                    if(line.equals(ENTRY_BEGIN)){
                        keyCount = 0;
                        lastPos = new EntryPosition(
                                agent.getReadFileCount(),
                                agent.getReadLineCount());
                    }

                    if (data && !line.equals(KEY_END)) {
                        keyCount++;
                        rawKey = rawKey.concat(line);
                    }

                    if (line.equals(KEY_BEGIN)) {
                        rawKey = "";
                        data = true;
                    } else if (line.equals(KEY_END)) {
                        data = false;
                        if (rawKey.equals(key)){
                            keyFound = true;
                        }
                    }
                }

                line = agent.readLine().trim();
            }

            agent.close();

        } catch (IOException | NullPointerException e) {
            agent.close();
        }

        throw new NoSuchElementException();
    }

    public void writeEntry(String key, byte[] data) {
        Entry entry = new Entry(key, data);
        try {
            agent.open(RandomAccessDBAgent.MODE_WRITE);
            agent.writeArray(entry.entryArray());
            agent.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void rewriteEntry(String key, byte[] data) {
        if(remove(key)) writeEntry(key, data);
    }

    public boolean remove(String key) {
        try {
            EntryPosition ep = search(key);
            agent.open(RandomAccessDBAgent.MODE_FULL);
            boolean removed = agent.removeEntry(ep);
            agent.close();
            return removed;

        } catch (NoSuchElementException | IOException e) {
            return false;
        }
    }

    private static byte[] parseData(String rawData) {
        String tokens[] = rawData.split("\\[|\\]|,\\s");

        byte data[] = new byte[tokens.length];

        int j = 0;
        for(int i = 0; i < tokens.length; i++){
            if(!tokens[i].equals("")) {
                data[j++] = Byte.valueOf(tokens[i]);
            }
        }
        data = Arrays.copyOfRange(data, 0, j);
        return data;
    }
}
