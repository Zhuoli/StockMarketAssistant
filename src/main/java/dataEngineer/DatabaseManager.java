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

    private static DatabaseManager databaseManager;
    private MongoDBConnector mongoDBConnector;

    private String url;
    private String userName;
    private String password;

    private DatabaseManager(String dbUrl, String database, String userName, String password) {
        Assert.assertNotNull(dbUrl);
        Assert.assertNotNull(database);
        Assert.assertNotNull(userName);
        Assert.assertNotNull(password);

        this.url =
                "jdbc:mysql://" + dbUrl + "/" + database
                        + "?useUnicode=yes&characterEncoding=UTF-8";
        this.userName = userName;
        this.password = password;
        this.mongoDBConnector = new MongoDBConnector();
    }

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
            return DatabaseManager
                    .GetDatabaseManagerInstance(MarketConstant.RESOURCE_CONFIG)
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

    /**
     * Gets Database Manager instance.
     *
     * @param pathString
     * @return
     */
    public static DatabaseManager GetDatabaseManagerInstance(String pathString) {

        if (DatabaseManager.databaseManager == null) {

            Path currentPath = Paths.get("./");
            System.out.println("Currrent path: " + currentPath.toAbsolutePath());
            Path path = Paths.get("src", "main", "resources", pathString);

            if (!Files.exists(path, LinkOption.NOFOLLOW_LINKS))
                path = Paths.get(pathString);

            if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                try {

                    // Create XML object and read values from the given path
                    DocumentBuilder builder =
                            DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document doc =
                            builder.parse(new DataInputStream(new FileInputStream(path.toFile())));
                    Element documentElement = doc.getDocumentElement();
                    Element databasesNode =
                            (Element) documentElement.getElementsByTagName("Databases").item(0);

                    String url = databasesNode.getElementsByTagName("Url").item(0).getTextContent();
                    String database =
                            databasesNode.getElementsByTagName("Database").item(0).getTextContent();
                    String user =
                            databasesNode.getElementsByTagName("User").item(0).getTextContent();
                    String password =
                            databasesNode.getElementsByTagName("Password").item(0).getTextContent();

                    return new DatabaseManager(url, database, user, password);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE,
                            "Failed to read configuration file from " + pathString, e);
                }
                DatabaseManager.databaseManager = new DatabaseManager();
            } else {
                DatabaseManager.databaseManager = new DatabaseManager();
            }
        }

        return DatabaseManager.databaseManager;
    }
}
