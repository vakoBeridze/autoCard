package icmia.autocard.upload;

import icmia.autocard.analyze.MyException;
import icmia.autocard.db.MongoDbConnector;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by vako on 12/3/14.
 */
public class Uploader {

    MongoDbConnector mongoDbConnector;
    private ArrayList<File> files = new ArrayList<File>();
    private ArrayList<MyException> notUploaded = new ArrayList<MyException>();

    public Uploader() {
    }

    public void addFile(File file) {
        files.add(file);
    }

    public void printFiles(boolean namesToo) {
        System.out.println("სულ: " + files.size());
        if (namesToo) {
            for (File file : files) {
                System.out.println(file.getName());
            }
        }
    }

    public void openConnection(String ip, String port, String db) throws UnknownHostException {
        mongoDbConnector = new MongoDbConnector(ip, port, db);
    }

    public void upload() {
        int total = files.size();
        System.out.println("uploading files " + total);

        File file;
        for (int i = 0; i < total; i++) {
            file = files.get(i);
            try {
                mongoDbConnector.upload(file);
                System.out.println("uploaded file: " + (i + 1) + " of " + total + "\t" + (total - i - 1) + " left");
            } catch (MyException | IOException ex) {
                notUploaded.add(new MyException(ex, file));
            }
        }

        System.out.println("\n\nupload finished");
        System.out.println("\n");
        System.out.println(notUploaded.size() + "  notUploaded");
        for (MyException myException : notUploaded) {
            System.out.println(myException.getFile().toString());
        }
        System.out.println("\n\nBye :)");
    }


    public void clearAll() {
        mongoDbConnector.removeAll();
    }
}
