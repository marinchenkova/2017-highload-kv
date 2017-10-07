package ru.mail.polis.marinchenkova.entry;

import java.io.*;
import java.util.NoSuchElementException;

/**
 * @author Marinchenko V. A.
 */
public class RandomAccessDBAgent {

    private final static int maxStrings = 1_000;
    public final static String MODE_READ = "r";
    public final static String MODE_WRITE = "w";
    public final static String MODE_FULL = "rw";

    public static int filesCount = 0;
    private static String pathDB;

    private File writeFile;
    private File readFile;
    private int writeFileSize;
    private int readFileCount;

    private FileWriter fileWriter;
    private BufferedReader fileReader;

    public RandomAccessDBAgent(String dataBasePath){
        pathDB = dataBasePath;
        checkFilesCount();
    }

    public void open(String mode) throws IllegalArgumentException,
                                         InstantiationException {
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
            e.printStackTrace();
        }
    }


    private void setFileReader(File newFile){
        try {
            if(fileReader != null) fileReader.close();
            FileInputStream fstream = new FileInputStream(newFile);
            fileReader = new BufferedReader(new InputStreamReader(fstream));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setFileWriter(File newFile){
        try {
            if(fileWriter != null) fileWriter.flush();
            newFile.createNewFile();
            fileWriter = new FileWriter(newFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMode() throws InstantiationException {
        checkFilesCount();
        if (filesCount == 0) throw new InstantiationException("Nothing to read!");
        else {
            readFile = new File(filePath(1));
            setFileReader(readFile);
        }
    }

    private void writeMode(){
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

    public String readLine(int fileNum, int lineNum) throws IOException {
        readFile = new File(filePath(fileNum));
        for(int i = 0; i < lineNum; i++) readLine(readFile);
        return readLine(readFile);
    }

    public String readLine(File file) throws IOException {
        if(!readFile.equals(file)) {
            readFile = file;
            setFileReader(readFile);
        }

        return fileReader.readLine();
    }

    public boolean removeEntry(String key, EntryPosition ep){
        try {

            for(int i = ep.fileNum; i < filesCount; i++) {
                File temp = File.createTempFile("file", ".txt", writeFile.getParentFile());
                readFile = new File(filePath(i));

                setFileReader(readFile);
                setFileWriter(temp);

                for(int j = 0; j < getSize(readFile); j++) {
                    String line = fileReader.readLine();
                    if(i < ep.lineNum || i > ep.lineNum + ep.sum - 2) fileWriter.write(line + "\r\n");
                }


            }


/*
            reader.close();
            writer.close();

            writeFile.delete();
            temp.renameTo(writeFile);
*/
            return true;

        } catch (IOException |NoSuchElementException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getFullSize(){
        int sum = 0;
        for(int i = 1; i <= filesCount; i++) {
            sum += getSize(new File(filePath(i)));
        }
        return sum;
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
