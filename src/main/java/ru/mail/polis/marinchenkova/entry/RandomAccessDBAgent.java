package ru.mail.polis.marinchenkova.entry;

import java.io.*;

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

    private File lastFile;
    private int lastSize;
    private int readFile;
    private int readLine;

    private FileWriter fileWriter;
    private BufferedReader fileReader;

    public RandomAccessDBAgent(String dataBasePath){
        pathDB = dataBasePath;
        checkFilesCount();
    }

    public void open(String mode) throws  IllegalArgumentException{
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
                throw new IllegalArgumentException("Wrong mode!");
        }



    }

    private void changeFile(File newFile){
        try {
            if(fileReader != null) fileReader.close();
            FileInputStream fstream = new FileInputStream(newFile);
            fileReader = new BufferedReader(new InputStreamReader(fstream));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMode(){
        checkFilesCount();
        changeFile(new File(filePath(readFile = 1)));
    }

    private void writeMode(){
        checkFilesCount();
        if (filesCount == 0) filesCount++;
        lastFile = new File(filePath(filesCount));

        if (lastFile.exists() && getSize(lastFile) >= maxStrings) {
            lastFile = new File(filePath(++filesCount));
        } else if(!lastFile.exists()){
            try {
                lastFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    public void write(String[] text) {
        try {
            fileWriter = new FileWriter(lastFile, true);
            lastSize = getSize(lastFile);

            for (String str : text) {
                //Write 1 string
                fileWriter.write(str);
                fileWriter.write("\r\n");

                //Check size of fileNum
                if (++lastSize >= maxStrings) {
                    fileWriter.flush();
                    lastFile = new File(filePath(++filesCount));
                    lastFile.createNewFile();
                    lastSize = 0;

                    fileWriter = new FileWriter(lastFile, true);
                }
            }

            fileWriter.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            fileReader.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readLine(int fileNum) throws IOException{
        if(readFile != fileNum) {
            readFile = fileNum;
            changeFile(new File(filePath(fileNum)));
        }

        return fileReader.readLine();
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
