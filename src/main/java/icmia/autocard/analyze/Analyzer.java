package icmia.autocard.analyze;


import icmia.autocard.upload.Uploader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by vako on 12/3/14.
 */
public class Analyzer {

    private int totalFilesCount = 0;
    private Uploader uploader;
    private String path;
    private Structure structure;

    private ArrayList<File> skippedFiles = new ArrayList<>();
    private ArrayList<MyException> exceptions = new ArrayList<>();
    private ArrayList<File> notRenamed = new ArrayList<>();

    public Analyzer(String path, Structure structure, Uploader uploader) {
        this.path = path;
        this.structure = structure;
        this.uploader = uploader;
    }

    public void analyze() {
        File rootDir = new File(path);
        System.out.println(rootDir.getAbsolutePath());

        if (rootDir.isDirectory()) {
            File[] files = rootDir.listFiles();
            int length = files.length;
            System.out.println("სულ: " + length + " ფოლდერი");

            File file;
            for (int i = 0; i < length; i++) {
                file = files[i];
                if (file.isDirectory()) {
                    System.out.println("(" + (i + 1) + " of " + length + ") reading file: " + file.getName());
                    getFiles(file);
                } else {
                    skippedFiles.add(file);
                }
            }
        }
        printSummary();
    }

    private void printSummary() {
        System.out.println("\n\n");
        System.out.println("totalFilesCount: " + totalFilesCount);
        System.out.println(skippedFiles.size() + "  Files skipped");
        for (File skippedFile : skippedFiles) {
            System.out.println("Skipped " + (skippedFile.isDirectory() ? "Folder: " : "File: ") + skippedFile.getAbsolutePath());
        }
        System.out.println("\n");
        System.out.println(exceptions.size() + "  exceptions");
        for (Exception exception : exceptions) {
            if (exception instanceof MyException) {
                System.out.println(((MyException) exception).getFile().toString());
            } else {
                System.out.println(exception.getCause().toString());
            }
        }
        System.out.println("\n\n");
    }

    private void getFiles(File directory) {
        String directoryName = directory.getName();
        if (directoryName.length() == 3) {
            System.out.println(directoryName);
        } else {
            skippedFiles.add(directory);
            return;
        }

        File[] files = directory.listFiles();
        totalFilesCount += files.length;

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareTo(t1.getName());
            }
        });
        for (File file : files) {
            if (!file.isDirectory()) {
//                tab();
                printInfo(file);
            } else {
                skippedFiles.add(file);
            }
        }
    }

    private void printInfo(File file) {
        String[] split;
        try {
            split = splitFileName(file);
        } catch (MyException e) {
            exceptions.add(e);
            return;
        }

        try {
            switch (structure) {
                case ONE:
                    if (checkIfValidNumber(Integer.parseInt(split[0])))
                        System.out.println("From: " + split[0]);
                    break;
                case TWO:
                    if (checkIfValidNumber(Integer.parseInt(split[0])) && checkIfValidNumber(Integer.parseInt(split[1]))) {
                        System.out.println("From: " + split[0] + " To: " + split[1]);
                    }
                    break;
                case THREE:
                    if (checkIfValidNumber(Integer.parseInt(split[1])) && checkIfValidNumber(Integer.parseInt(split[2]))) {
                        System.out.println("Serie: " + split[0] + " From: " + split[1] + " To: " + split[2]);
                        uploader.addFile(file);
                    }
                    break;
            }
        } catch (Exception ex) {
            exceptions.add(new MyException(ex, file));
        }

    }

    private boolean checkIfValidNumber(int i) throws MyException {
        if (i < 1 || i > 1000)
            throw new MyException(null);
        return true;
    }

    public static String[] splitFileName(File file) throws MyException {
        String fileName = file.getName();

        String[] tmp = fileName.split("\\.");
        if (tmp.length < 2 || !tmp[1].equals("pdf")) {
            throw new MyException(file);
        }
        return tmp[0].split("-");
    }

    private void tab() {
        System.out.print("\t\t\t\t\t\t");
    }

    public void rename() {
        File rootDir = new File(path);
        if (rootDir.isDirectory()) {
            File[] files = rootDir.listFiles();
            int length = files.length;
            System.out.println("სულ: " + length + " ფოლდერი");

            File directory;
            for (int i = 0; i < length; i++) {
                directory = files[i];
                if (directory.isDirectory() && directory.getName().length() == 3) {
                    String fileName = directory.getName().toUpperCase();
                    System.out.println("(" + (i + 1) + " of " + length + ") renaming files in: " + fileName);

                    doRename(fileName, directory.listFiles());
                } else {
                    notRenamed.add(directory);
                }
            }
        }
        System.out.println("\n");
        System.out.println("=================================== notRenamed: " + notRenamed.size());
        for (File file : notRenamed) {
            System.out.println("Not Renamed: " + (file.isDirectory() ? "Folder: " : "File: ") + file.getAbsolutePath());
        }
    }

    private void doRename(String fileName, File[] files) {
        String destPath;
        switch (this.structure) {
            case ONE:
                Arrays.sort(files, new Comparator<File>() {
                    @Override
                    public int compare(File file, File t1) {
                        return file.getName().compareTo(t1.getName());
                    }
                });
                File currentFile;
                String nextFileName;
                for (int i = 0; i < files.length; i++) {
                    currentFile = files[i];
                    try {
                        nextFileName = fillNumberWithCharacter(String.valueOf(Integer.parseInt(files[i + 1].getName().split("\\.")[0]) - 1));
                    } catch (ArrayIndexOutOfBoundsException e) {
                        nextFileName = "999";
                    }
                    destPath = currentFile.getParentFile().getPath() + "/" + fileName + "-" + currentFile.getName().split("\\.")[0] + "-" + nextFileName + ".pdf";
                    if (!currentFile.renameTo(new File(destPath))) {
                        notRenamed.add(currentFile);
                    }
                }
                break;
            case TWO:
                for (File file : files) {
                    destPath = file.getParentFile().getPath() + "/" + fileName + "-" + file.getName();
                    if (!file.renameTo(new File(destPath))) {
                        notRenamed.add(file);
                    }
                }
                break;
            default:
                System.out.println("UNKNOWN STRUCTURE: " + structure.name() + "\t\t" + fileName);
                break;
        }
    }

    private String fillNumberWithCharacter(String number) {
        String myString = "" + number;
        while (myString.length() < 3) {
            myString = "0" + myString;
        }
        return myString;
    }
}
