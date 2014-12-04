package icmia.autocard.analyze;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vako on 12/3/14.
 */
public class AnalyzerOld {

    private static final String PATH = "/media/vako/9ff703fc-de93-4ccd-a733-ecf3867c3c3b/SHEMOWMEBULI BARATEBI SRULI-MANQANEBI-3 ASO 3 CIFRI/NOMERI-NOMERI";

    private static ArrayList<Exception> exceptions = new ArrayList<Exception>();
    private static ArrayList<File> skippedFiles = new ArrayList<File>();
    private static ArrayList<File> badFiles = new ArrayList<File>();

    private static int badFilesCount;
    private static int skippedFilesCount;

    public void analyze() {
        File rootDir = new File(PATH);

        System.out.println(rootDir.isDirectory());
        System.out.println(rootDir.getAbsolutePath());

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
                skippedFilesCount++;
            }
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printSummary();
    }

    private void printSummary() {
        System.err.println(skippedFilesCount + " Files skipped");
        for (File skippedFile : skippedFiles) {
            if (skippedFile.isDirectory()) {
                System.err.println("Skipped Folder: " + skippedFile.getAbsolutePath());
            } else {
                System.err.println("Skipped File: " + skippedFile.getAbsolutePath());
            }
        }
        System.err.println("\n\n" + badFilesCount + " bad files");
        for (File badFile : badFiles) {
            System.err.println("Bad File: " + badFile.getAbsolutePath());
        }

        System.err.println("\n\n\n" + exceptions.size() + " Exceptions");

        for (Exception exception : exceptions) {
            if (exception instanceof MyException) {
                System.err.println(((MyException) exception).getFile().toString());
            } else {
                System.out.println(exception.getCause().toString());
            }
        }
    }

    private void getFiles(File directory) {

        File[] files = directory.listFiles();
        int filesCount = files.length;

        Structure structure = detectStructure(files);
        if (structure == Structure.BAD) {
            printError("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + directory.getName());
        }
        System.out.println("structure: " + structure.name());

        File file;
        for (int i = 0; i < filesCount; i++) {

            file = files[i];
            if (!file.isDirectory()) {
                tab();
                printInfo(file, structure);
            } else {
                skippedFilesCount++;
                skippedFiles.add(file);
            }
        }
    }

    private void printInfo(File file, Structure structure) {
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
                    System.out.println("To: " + split[0]);
                    break;
                case TWO:
                    checkIfValidNumber(Integer.parseInt(split[0]));
                    checkIfValidNumber(Integer.parseInt(split[1]));
                    System.out.println("From: " + split[0] + " To: " + split[1]);
                    break;
                case THREE:
                    checkIfValidNumber(Integer.parseInt(split[1]));
                    checkIfValidNumber(Integer.parseInt(split[2]));
                    System.out.println("Serie: " + split[0] + " From: " + split[1] + " To: " + split[2]);
                    break;
                case BAD:
                    badFilesCount++;
                    badFiles.add(file);
                    break;
                default:
                    System.out.println("");
                    exceptions.add(new MyException(file));
                    break;
            }
        } catch (Exception ex) {
            printError(" >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> EXCEPTION  HERE <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<  File:" + file.getName());
            MyException myException = new MyException(ex, file);
            myException.setFile(file);
            exceptions.add(myException);
        }
    }

    private void checkIfValidNumber(int i) throws Exception {
        if (i < 1 || i > 1000)
            throw new Exception();
    }

    private Structure detectStructure(File[] files) {
        Structure s = Structure.BAD;
        int badCount = 0, oneCount = 0, twoCount = 0, threeCount = 0;
        for (File file : files) {
            if (file.isDirectory())
                continue;
            String[] names;
            try {
                names = splitFileName(file);
            } catch (MyException e) {
                exceptions.add(e);
                return Structure.BAD;
            }

            switch (names.length) {
                case 1:
                    oneCount++;
                    break;
                case 2:
                    twoCount++;
                    break;
                case 3:
                    threeCount++;
                    break;
                default:
                    badCount++;
                    break;
            }
        }

        int max = badCount;
        if (oneCount > max) {
            max = oneCount;
            s = Structure.ONE;
        }
        if (twoCount > max) {
            max = twoCount;
            s = Structure.TWO;
        }
        if (threeCount > max) {
            max = threeCount;
            s = Structure.THREE;
        }
        return s;
    }

    private String[] splitFileName(File file) throws MyException {
        String fileName = file.getName();

        String[] tmp = fileName.split("\\.");
        if (tmp.length < 2) {
            throw new MyException(file);
        }
        return tmp[0].split("-");
    }

    private void tab() {
        System.out.print("\t\t\t\t\t\t");
    }

    private void printError(String s) {
        System.out.println("ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR: " + s);
    }


}
