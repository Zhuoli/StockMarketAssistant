package dataEngineer;
import dataEngineer.data.SharesQuote;
import mongoDb.MongoDBConnector;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.MarketConstant;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Interacts with AWS Database through JOOQ package.
 */
public class DatabaseManager {

    private MongoDBConnector mongoDBConnector = new MongoDBConnector();

    private DatabaseManager(){
        this.mongoDBConnector.connect();
    }

    public  List<SharesQuote> RetrieveCollection(String tableName){
        return this.mongoDBConnector.retrieveDcouments(tableName);
    }

    /**
     * Reads credential and authenticate SQL connection.
     *
     * @return
     */
    public static DatabaseManager initializeDataManager() {
        try {
            return new DatabaseManager()
                    .Authenticate();
        } catch (Exception exc) {
            exc.printStackTrace();
            return null;
        }
    }

    public DatabaseManager Authenticate() {
        if(this.mongoDBConnector == null){
            this.mongoDBConnector = new MongoDBConnector();
        }
        this.mongoDBConnector.connect();
        return this;
    }

    public void close(){
        this.mongoDBConnector.close();
    }

    public void insertDocument(String tablename, SharesQuote... sharesQuotes){
        this.mongoDBConnector.insertDocument(tablename, sharesQuotes);
    }
}
