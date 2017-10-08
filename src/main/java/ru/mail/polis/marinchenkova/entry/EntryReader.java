package ru.mail.polis.marinchenkova.entry;

import java.io.*;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static ru.mail.polis.marinchenkova.entry.RandomAccessDBAgent.MODE_READ;
import static ru.mail.polis.marinchenkova.entry.Entry.*;

/**
 * @author Marinchenko V. A.
 */
public class EntryReader {

    private EntryPosition lastPos;
    private RandomAccessDBAgent agent;

    public EntryReader(RandomAccessDBAgent agent){
        this.agent = agent;
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
        if(lastPos != null && lastPos.key.equals(key)) return search(key, true);
        else return search(key, false);
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

            if(goOn) agent.setReaderToPosition(lastPos);
            else agent.setReaderToBegin();

            String line = agent.readLine().trim();
            int j = 0;

            while (line != null) {
                j++;

                if (keyFound) {
                    if (data && !line.equals(DATA_END)) {
                        dataCount++;
                        rawData = rawData.concat(line);
                    }

                    if (line.equals(DATA_BEGIN)) data = true;
                    else if (line.equals(DATA_END)) {
                        lastPos.setBody(keyCount, dataCount);
                        agent.close();
                        return parseData(rawData);
                    }

                } else {
                    if(line.equals(ENTRY_BEGIN)){
                        lastPos = new EntryPosition(
                                key,
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
