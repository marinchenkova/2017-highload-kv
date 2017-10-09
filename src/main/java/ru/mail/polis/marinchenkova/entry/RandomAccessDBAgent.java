package ru.mail.polis.marinchenkova.entry;

import java.io.*;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class RandomAccessDBAgent {

    private final static int maxStrings = 10_000;
    public final static String MODE_READ = "r";
    public final static String MODE_WRITE = "w";
    public final static String MODE_FULL = "rw";

    public static int filesCount = 0;

    private static String pathDB;

    private File writeFile;
    private File readFile;

    private int readFileCount;
    private int readLineCount;

    private int writeFileCount = 0;
    private int writeFileSize;

    private EntryPosition start;

    private FileWriter fileWriter;
    private BufferedReader fileReader;

    public RandomAccessDBAgent(String dataBasePath){
        pathDB = dataBasePath;
        start = new EntryPosition("", 1, -1);
        start.setBody(0, 0);
        checkFilesCount(false);
    }

    public void open(String mode) throws IllegalArgumentException, IOException {
        switch (mode) {
            case MODE_READ:
                readMode();
                break;

            case MODE_WRITE:
                writeMode();
                break;

            case MODE_FULL:
                readMode();
                writeMode();
                break;
            default:
                throw new IllegalArgumentException("Wrong RandomAccessDBAgent mode!");
        }
    }

    public void close(){
        try {
            if(fileReader != null) fileReader.close();
            if(fileWriter != null) fileWriter.flush();
        } catch (IOException e) {

        }
    }

    private void readMode() throws IOException {
        checkFilesCount(false);
        if (filesCount == 0) throw new FileNotFoundException("Nothing to read!");
        else {
            readFile = new File(filePath("", 1));
            setFileReader(readFile);
        }
    }

    private void writeMode() throws IOException {
        checkFilesCount(false);
        if (filesCount == 0) filesCount++;
        writeFile = new File(filePath("", filesCount));

        if (writeFile.exists() && getSize(writeFile) >= maxStrings) {
            writeFile = new File(filePath("", ++filesCount));
        } else if(!writeFile.exists()){
            try {
                writeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setFileWriter(writeFile);
    }

    private void setFileReader(File newFile) {
        try {
            if(fileReader != null) fileReader.close();

            readFile = newFile;
            FileInputStream fstream = new FileInputStream(readFile);
            fileReader = new BufferedReader(new InputStreamReader(fstream));

        } catch (IOException e) {

        }
    }

    private void setFileWriter(File newFile) {
        try {
            if(fileWriter != null) fileWriter.close();

            writeFile = newFile;
            if(!writeFile.exists()) writeFile.createNewFile();
            fileWriter = new FileWriter(writeFile, true);

        } catch (IOException e) {

        }
    }

    public void writeArray(String[] text) {
        checkFilesCount(false);
        writeFileCount = filesCount;
        writeArray(text, "");
    }

    private void writeArray(String[] text, String startFileName) {
        try {
            File file = new File(filePath(startFileName, writeFileCount));
            setFileWriter(file);

            writeFileSize = getSize(writeFile);

            for(String s : text) {
                if (writeFileSize >= maxStrings) {
                    fileWriter.close();
                    File next = new File(filePath(startFileName, ++writeFileCount));
                    setFileWriter(next);
                    writeFileSize = 0;
                }

                writeFileSize++;
                fileWriter.write(s + "\r\n");
            }

            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void skipReadLines(int lines) {
        try {
            for(int i = 0; i < lines; i++) {
                readLineCount++;
                readLine(readFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReaderToPosition(EntryPosition pos) {
        readFileCount = pos.fileNum;
        readLineCount = pos.lineNum;

        File file = new File(filePath("", readFileCount));
        setFileReader(file);

        skipReadLines(pos.lineNum);
    }

    public void setReaderToBegin() {
        setReaderToPosition(start);
    }

    public String readLine(){
        try {
            String line = readLine(readFile);

            if(line == null) {
                File file = new File(filePath("", ++readFileCount));
                setFileReader(file);
                line = readLine(readFile);
                readLineCount = -1;
            }

            readLineCount++;
            return line;

        } catch (IOException e) {
            return null;
        }
    }

    private String readLine(File file) throws IOException {
        if(!readFile.equals(file)) setFileReader(file);

        return fileReader.readLine();
    }

    public boolean removeEntry(EntryPosition ep){
        try {
            boolean done = false;
            String tempName = "temp";
            int strings = 0;

            for (int i = ep.fileNum; i <= filesCount; i++) {
                File read = new File(filePath("", i));
                File temp = new File(filePath(tempName, i));

                setFileReader(read);
                setFileWriter(temp);

                ArrayList<String> text = new ArrayList<>();
                int j = 0;
                int off = strings;
                String line = readLine(readFile);

                while (line != null) {
                    strings++;

                    if(j + off < ep.lineNum ||
                            j + off > ep.lineNum + ep.sum) {
                        text.add(line);
                    }

                    if(j + off == ep.lineNum + ep.sum) done = true;
                    j++;
                    line = readLine(readFile);
                }

                writeFileCount = i;
                writeArray(text.toArray(new String[0]), tempName);

                close();

                read.delete();
                temp.renameTo(readFile);

                if(getSize(read) == 0) read.delete();

                if(done) break;
            }
            checkFilesCount(true);
            return true;

        } catch (IOException |NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getReadFileCount(){
        return readFileCount;
    }

    public int getReadLineCount() {
        return readLineCount;
    }

    private static int getSize(File file) {
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

    private static String filePath(String start, int num) {
        return  pathDB + "\\" + start + fileNameNumber(num) + ".txt";
    }

    private static String fileNameNumber(int num){
        return String.valueOf(1_000_000 + num).substring(1);
    }

    private void checkFilesCount(boolean rename){
        File db = new File(pathDB);
        if(db.exists()) {
            File files[] = db.listFiles();
            filesCount = files.length;
            if(rename) {
                for(int i = 0; i < filesCount; i++) {
                    files[i].renameTo(new File(filePath("", i + 1)));
                }
            }
        }
    }
}
