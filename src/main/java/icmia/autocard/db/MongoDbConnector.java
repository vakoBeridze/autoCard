package icmia.autocard.db;

import com.mongodb.*;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import icmia.autocard.analyze.Analyzer;
import icmia.autocard.analyze.MyException;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by vako on 12/3/14.
 */
public class MongoDbConnector {


    private DB db;
    private GridFS gridFS;

    public MongoDbConnector(String ip, String port, String db) throws UnknownHostException {
        MongoClient client = new MongoClient(ip, Integer.parseInt(port));
        this.db = client.getDB(db);
        this.gridFS = new GridFS(this.db);
    }

    public DBCursor getFiles() {
        DBCollection collection = db.getCollection("fs.files");
        return collection.find();
    }

    public void removeAll() {
        System.out.println("remove All");
        int i = 0;
        for (DBObject dbObject : getFiles()) {
            gridFS.remove(dbObject);
//            System.out.println("Removed file " + (++i) + ": " + dbObject.toString());
            System.out.println("Removed file " + (++i));
        }
        System.out.println("removed All");
    }

    public void upload(File file) throws MyException, IOException {
        String[] names = Analyzer.splitFileName(file);
        if (names.length == 3) {
            GridFSInputFile inputFile = gridFS.createFile(file);
            for (int i = 0; i < 3; i++) {
                inputFile.put("series", names[0]);
                inputFile.put("numberFrom", Integer.parseInt(names[1]));
                inputFile.put("numberTo", Integer.parseInt(names[2]));
                inputFile.put("numberType", 2); // 1 ჩვეულებრივი მანქანა, 2 - მოტოციკლეტი, 3 - მისაბმელი
                inputFile.setContentType("application/pdf");
            }
            inputFile.save();
        } else {
            throw new MyException(file);
        }
    }
}
