package icmia.autocard;

import icmia.autocard.analyze.Analyzer;
import icmia.autocard.analyze.Structure;
import icmia.autocard.upload.Uploader;

import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * <br>
 * <b>      Workflow: </b>
 * <p>
 * Analyze -> Rename -> Analyze -> Upload
 * </p>
 * <br>
 * Created by vako on 12/3/14.
 */
public class Main {

    //    private static final String PATH = "/media/vako/9ff703fc-de93-4ccd-a733-ecf3867c3c3b/SHEMOWMEBULI BARATEBI SRULI-MANQANEBI-3 ASO 3 CIFRI/SERIA-NOMERI-NOMERI";
//    private static final String PATH = "/media/vako/9ff703fc-de93-4ccd-a733-ecf3867c3c3b/Baratebi test/test";
    private static final String PATH = "/media/vako/9ff703fc-de93-4ccd-a733-ecf3867c3c3b/SABOLOOOOOOOOOOOO/MOTOCIKLEBI-2 ASO 4 CIFRI";


    public static void main(String[] args) {
//        new AnalyzerOld().analyze();
        Uploader uploader = new Uploader();
        Analyzer analyzer = new Analyzer(PATH, Structure.THREE, uploader);

        String answer;
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n\nwhat to do? (analyze / rename)\n\n");
        answer = scanner.next();
        switch (answer) {
            case "analyze":
                analyzer.analyze();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                uploader.printFiles(false);

                System.out.println("\n\nstart uploading ? (yes / no)\n\n");
                answer = scanner.next();
                if (answer.equalsIgnoreCase("yes") || answer.equalsIgnoreCase("y")) {

                    String ip, port, db;
                    System.out.println("enter IP: ");
                    ip = scanner.next();
                    System.out.println("enter port: ");
                    port = scanner.next();
                    System.out.println("enter DB: ");
                    db = scanner.next();

                    try {
                        uploader.openConnection(ip, port, db);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        return;
                    }
                    uploader.upload();
                } else {
                    System.out.println("okay, upload canceled");
                }
                break;
            case "rename":
                analyzer.rename();
                break;
            default:
                System.out.println("Exiting program, Bye :/");
                return;
        }
    }
}
