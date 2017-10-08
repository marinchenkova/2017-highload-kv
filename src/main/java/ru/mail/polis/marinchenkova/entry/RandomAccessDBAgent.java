package ru.mail.polis.marinchenkova.entry;

import java.io.*;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class RandomAccessDBAgent {

    private final static int maxStrings = 10_0;
    public final static String MODE_READ = "r";
    public final static String MODE_WRITE = "w";
    public final static String MODE_FULL = "rw";

    public static int filesCount = 0;
    private static String pathDB;

    private File writeFile;
    private File readFile;
    private int writeFileSize;
    private int readFileCount;

    private EntryPosition start;

    private FileWriter fileWriter;
    private BufferedReader fileReader;

    public RandomAccessDBAgent(String dataBasePath){
        pathDB = dataBasePath;
        start = new EntryPosition(1);
        start.set(0, 0, 0, "");
        checkFilesCount();
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


    private void setFileReader(File newFile) {
        try {
            if(fileReader != null) fileReader.close();
            FileInputStream fstream = new FileInputStream(newFile);
            fileReader = new BufferedReader(new InputStreamReader(fstream));

        } catch (IOException e) {

        }
    }

    private void setFileWriter(File newFile) {
        try {
            if(fileWriter != null) fileWriter.flush();
            newFile.createNewFile();
            fileWriter = new FileWriter(newFile, true);

        } catch (IOException e) {

        }
    }

    private void readMode() throws IOException {
        checkFilesCount();
        if (filesCount == 0) throw new FileNotFoundException("Nothing to read!");
        else {
            readFile = new File(filePath(1));
            setFileReader(readFile);
        }
    }

    private void writeMode() throws IOException {
        checkFilesCount();
        if (filesCount == 0) filesCount++;
        writeFile = new File(filePath(filesCount));

        if (writeFile.exists() && getSize(writeFile) >= maxStrings) {
            writeFile = new File(filePath(++filesCount));
        } else if(!writeFile.exists()){
            try {
                writeFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setFileWriter(writeFile);
    }

    public void writeArray(String[] text) {
        writeFileSize = getSize(writeFile);
        for(String s : text) {
            writeLine(s);
        }
    }

    private void writeLine(String str) {
        try {

            if (writeFileSize >= maxStrings) {
                writeFile = new File(filePath(++filesCount));
                writeFile.createNewFile();
                writeFileSize = 0;
                setFileWriter(writeFile);
            }

            writeFileSize++;
            writeLine(str, writeFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeLine(String str, File file) throws IOException {
        if(!writeFile.equals(file)) {
            writeFile = file;
            setFileWriter(writeFile);
        }

        fileWriter.write(str);
        fileWriter.write("\r\n");
    }

    private void skipReadLines(int lines) {
        try {
            for(int i = 0; i < lines; i++) readLine(readFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setReaderToPosition(EntryPosition pos) {
        readFileCount = pos.fileNum;
        readFile = new File(filePath(readFileCount));

        setFileReader(readFile);

        skipReadLines(pos.lineNum);
    }

    public void setReaderToBegin() {
        setReaderToPosition(start);
    }

    public String readLine(){
        try {
            String line = readLine(readFile);

            if(line == null) {
                readFile = new File(filePath(++readFileCount));
                setFileReader(readFile);
                line = readLine(readFile);
            }
            return line;

        } catch (IOException e) {
            return null;
        }
    }

    private String readLine(File file) throws IOException {
        if(!readFile.equals(file)) {
            readFile = file;
            setFileReader(readFile);
        }

        return fileReader.readLine();
    }

    public boolean removeEntry(EntryPosition ep){
        try {
            boolean done = false;

            for(int i = ep.fileNum; i < filesCount; i++) {
                readFile = new File(filePath(i));
                writeFile = new File(filePath(i));

                File temp = File.createTempFile("file", ".txt", writeFile.getParentFile());

                setFileReader(readFile);
                setFileWriter(temp);

                String line = readLine(readFile);
                int j = 0;
                while (line != null) {
                    if(j < ep.lineNum || j > ep.lineNum + ep.sum)
                        fileWriter.write(line + "\r\n");
                    if(j == ep.lineNum + ep.sum) done = true;
                    j++;
                    line = fileReader.readLine();
                }

                fileWriter.close();
                fileReader.close();

                writeFile.delete();
                temp.renameTo(writeFile);

                if(done) break;
            }

            return true;

        } catch (IOException |NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getReadFileCount(){
        return readFileCount;
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

    private static String filePath(int num){
        return  pathDB + "\\" + String.valueOf(num) + ".txt";
    }


    private void checkFilesCount(){
        File db = new File(pathDB);
        if(db.exists()) {
            filesCount = db.listFiles().length;
        }
    }
}
