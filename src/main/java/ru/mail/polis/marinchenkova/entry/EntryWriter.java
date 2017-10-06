package ru.mail.polis.marinchenkova.entry;

import java.util.ArrayDeque;

/**
 * @author Marinchenko V. A.
 */
public class EntryWriter {

    private RandomAccessDBAgent agent;

    //private FileWriter fileWriter;
    //private Timer timer = new Timer();
    //private TimerTask checkTask = new CheckQueueIsEmptyTask();
    private ArrayDeque<Entry> queue = new ArrayDeque<>();

    public EntryWriter(RandomAccessDBAgent agent){
        this.agent = agent;
    }

    public void setEntryToQueue(Entry entry){
        queue.add(entry);
        writeEntry(queue.poll());
    }

    public void writeEntry(Entry entry) {
        agent.open(RandomAccessDBAgent.MODE_WRITE);
        agent.write(entry.entryArray());
    }

    public void rewriteEntry(EntryReader er, Entry newEntry) {
        //if(remove(er, newEntry.getKey())) writeEntry(newEntry);
    }
/*
    public boolean remove(EntryReader er, String key) {

        try {
            EntryPosition ep = er.search(key);
            lastFile = new File(filePath(ep.getFileNum()));

            File temp = File.createTempFile("file", ".txt", lastFile.getParentFile());
            FileInputStream instream = new FileInputStream(lastFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(instream));

            FileWriter writer = new FileWriter(temp, true);

            for(int i = 0; i < getSize(lastFile); i++) {
                String line = reader.readLine();

               // if(i < ep.lineNum || i > ep.lineNum + ep.sum - 2) writer.write(line + "\r\n");
            }

            reader.close();
            writer.close();

            lastFile.delete();
            temp.renameTo(lastFile);
            return true;

        } catch (IOException | NoSuchElementException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

    }
*/

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
