package ru.mail.polis.marinchenkova.entry;

import java.io.*;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static ru.mail.polis.marinchenkova.entry.RandomAccessDBAgent.MODE_READ;
import static ru.mail.polis.marinchenkova.entry.RandomAccessDBAgent.filesCount;
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

        int fileNum = 1;
        int lineNum = 0;

        int dataCount = 0;
        int keyCount = 0;

        if(goOn) {
            fileNum = lastPos.fileNum;
            lineNum = lastPos.lineNum;
        }

        try {
            agent.open(MODE_READ);
            for(int i = fileNum; i <= filesCount; i++) {
                for (int j = lineNum; j < agent.getFullSize(); j++) {
                    String line = agent.readLine(i, j).trim();

                    if (keyFound) {
                        if (data && !line.equals(DATA_END)) {
                            dataCount++;
                            rawData = rawData.concat(line);
                        }

                        if (line.equals(DATA_BEGIN)) data = true;
                        else if (line.equals(DATA_END)) {
                            lastPos = new EntryPosition(
                                    i,
                                    j - dataCount - keyCount - 7,
                                    keyCount,
                                    dataCount,
                                    key);
                            return parseData(rawData);
                        }

                    } else {
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
                }
            }
            agent.close();

        } catch (IOException | InstantiationException e){
            e.printStackTrace();
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
