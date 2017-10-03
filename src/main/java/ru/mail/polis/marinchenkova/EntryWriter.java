package ru.mail.polis.marinchenkova;

import com.sun.jmx.remote.internal.ArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Marinchenko V. A.
 */
public class EntryWriter {

    private final static String PATH_DB = "D:\\MailRu_DB";

    private final static int maxStrings = 50_000;

    private int filesCount = 0;
    private File last;
    private int lastSize;

    private FileWriter fileWriter;
    //private Timer timer = new Timer();
    //private TimerTask checkTask = new CheckQueueIsEmptyTask();
    private ArrayDeque<Entry> queue = new ArrayDeque<>();

    @NotNull
    public static String randomKey() {
        return Long.toHexString(ThreadLocalRandom.current().nextLong());
    }

    @NotNull
    public static byte[] randomValue() {
        final byte[] result = new byte[100000];
        ThreadLocalRandom.current().nextBytes(result);
        return result;
    }

    public static void main(String[] args) {
        EntryWriter ew = new EntryWriter();

        for(int i = 0; i < 100; i++){
            Entry entry = new Entry(i, "KEY_" + i, randomValue());
            System.out.println(i);
            ew.writeEntry(entry);
        }
    }

    public EntryWriter(){

    }


    public void checkFilesCount(){
        File path_db = new File(PATH_DB);
        if(path_db.exists()) {
            filesCount = path_db.listFiles().length;
        }
    }

    public void setEntryToQueue(Entry entry){
        queue.add(entry);
        writeEntry(queue.poll());
    }

    private void writeEntry(Entry entry) {
        checkFilesCount();
        if (filesCount == 0) filesCount++;

        last = new File(PATH_DB + "\\" + String.valueOf(filesCount) + ".txt");
        /*
        try {
            fileWriter = new FileWriter(last, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */

        if (!last.exists()) try {
            last.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        lastSize = getSize(last);
        if (lastSize >= 0 && lastSize < maxStrings) {
            writeStrings(entry.entryArray());
        }
    }

    public void writeStrings(String[] text){
        try {
            FileWriter fw = new FileWriter(last, true);

            for (String str : text) {
                //Write 1 string
                fw.write(str);
                fw.write("\r\n");

                //Check size of file
                if (++lastSize == maxStrings) {
                    fw.flush();
                    last = new File(PATH_DB + "\\" + String.valueOf(++filesCount) + ".txt");
                    last.createNewFile();
                    lastSize = 0;

                    fw = new FileWriter(last, true);
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

    public static int getSize(File file) {
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
