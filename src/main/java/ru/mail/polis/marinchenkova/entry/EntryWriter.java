package ru.mail.polis.marinchenkova.entry;

import java.io.*;
import java.util.ArrayDeque;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class EntryWriter {

    final static String PATH_DB = "D:\\MailRu_DB";

    final static int maxStrings = 50_000;

    static int filesCount = 0;

    private File lastFile;
    private int lastSize;

    //private FileWriter fileWriter;
    //private Timer timer = new Timer();
    //private TimerTask checkTask = new CheckQueueIsEmptyTask();
    private ArrayDeque<Entry> queue = new ArrayDeque<>();

    public EntryWriter(){
        checkFilesCount();
    }


    private void checkFilesCount(){
        File path_db = new File(PATH_DB);
        if(path_db.exists()) {
            filesCount = path_db.listFiles().length;
        }
    }

    public void setEntryToQueue(Entry entry){
        queue.add(entry);
        writeEntry(queue.poll());
    }

    public void writeEntry(Entry entry) {
        checkFilesCount();
        if (filesCount == 0) filesCount++;

        lastFile = new File(filePath(filesCount));
        /*
        try {
            fileWriter = new FileWriter(lastFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        if (!lastFile.exists()) try {
            lastFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastSize = getSize(lastFile);
        if (lastSize >= 0 && lastSize < maxStrings) {
            writeStrings(entry.entryArray());
        }
    }

    private void writeStrings(String[] text){
        try {
            FileWriter fw = new FileWriter(lastFile, true);

            for (String str : text) {
                //Write 1 string
                fw.write(str);
                fw.write("\r\n");

                //Check size of fileNum
                if (++lastSize == maxStrings) {
                    fw.flush();
                    lastFile = new File(filePath(++filesCount));
                    lastFile.createNewFile();
                    lastSize = 0;

                    fw = new FileWriter(lastFile, true);
                }

                //if(queue.isEmpty())

                //timer.schedule(checkTask, CHECK_TIME);
            }

            fw.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }

    }

    static String filePath(int num){
        return PATH_DB + "\\" + String.valueOf(num) + ".txt";
    }

    static int getSize(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader);

            int lineNumber = 0;
            while (lineNumberReader.readLine() != null){
                lineNumber++;
            }

            lineNumberReader.close();
            return lineNumber;

        } catch(IOException e) {
            return -1;
        }
    }

    public void rewriteEntry(EntryReader er, Entry newEntry) {
        if(remove(er, newEntry.getKey())) writeEntry(newEntry);
    }

    public boolean remove(EntryReader er, String key) {
        try {
            EntryPosition ep = er.search(key);
            lastFile = new File(filePath(ep.fileNum));

            File temp = File.createTempFile("file", ".txt", lastFile.getParentFile());
            FileInputStream instream = new FileInputStream(lastFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

            FileWriter writer = new FileWriter(temp, true);

            for(int i = 0; i < getSize(lastFile); i++) {
                String line = reader.readLine();

                if(i < ep.lineNum || i > ep.lineNum + ep.sum - 2) writer.write(line + "\r\n");
            }

            reader.close();
            writer.close();

            lastFile.delete();
            temp.renameTo(lastFile);
            return true;

        } catch (IOException | NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
    }


/*
    private class CheckQueueIsEmptyTask extends TimerTask{
        @Override
        public void run() {
            if(queue.isEmpty()) try {
                fileWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    */
}
