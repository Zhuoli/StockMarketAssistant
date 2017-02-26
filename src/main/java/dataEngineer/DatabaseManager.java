package dataEngineer;

import JooqORM.tables.records.ChinesemarketcompanyRecord;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.junit.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static JooqORM.Tables.CHINESEMARKETCOMPANY;

/**
 * Interacts with AWS Database through JOOQ package.
 */
public class DatabaseManager {

    private static DatabaseManager databaseManager;

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
    }

    private DatabaseManager() {
        // Does nothing
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

    public DatabaseManager Authenticate() throws SQLException {
        this.getDBJooqCreate();
        this.conn.close();
        return this;
    }

    public static void close() {
        if (DatabaseManager.databaseManager == null)
            return;

        if (DatabaseManager.databaseManager.globalCreate != null)
            DatabaseManager.databaseManager.globalCreate.close();
        if (DatabaseManager.databaseManager.conn != null)
            try {
                DatabaseManager.databaseManager.conn.close();
            } catch (Exception exc) {

            }
    }

    private Connection conn = null;
    private DSLContext globalCreate = null;

    /**
     * Sets up and maintains SQL connection
     *
     * @return DSLContext instance
     * @throws SQLException
     */
    public DSLContext getDBJooqCreate() throws SQLException {

        // Reuse sql connection
        if (this.conn != null && !this.conn.isClosed() && this.globalCreate != null)
            return this.globalCreate;

        this.conn = DriverManager.getConnection(this.url, this.userName, this.password);
        this.globalCreate = DSL.using(this.conn, SQLDialect.MYSQL);
        Optional<Table<?>> table = this.GetTable(this.globalCreate, CHINESEMARKETCOMPANY.getName());
        if (!table.isPresent()) {
            this.globalCreate.createTable(CHINESEMARKETCOMPANY).columns(CHINESEMARKETCOMPANY.fields()).execute();
        }
        return this.globalCreate;
    }

    public Optional<Table<?>> GetTable(DSLContext create, String tableName) {
        return create
                .meta()
                .getTables()
                .stream()
                .filter(p -> p.getName().equals(tableName))
                .findFirst();
    }

    final static int BATCH_SIZE = 1000;

    /**
     * Inserts on duplicate ignore.
     * 
     * @param companies
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void insertOnDuplicateIgnore(SharesQuote... companies) throws SQLException,
            ClassNotFoundException {
        Iterator<SharesQuote> companyObjectIterator = Arrays.asList(companies).iterator();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            throw e;
        }

        DSLContext creator = this.getDBJooqCreate();

        // Get next batch of company
        SharesQuote[] companyObjects = this.getNextBatch(companyObjectIterator, BATCH_SIZE);

        // Batch insert companies
        while (companyObjects != null && companyObjects.length > 0) {

            // Convert companyobject array to JOOQ query array
            List<InsertFinalStep<ChinesemarketcompanyRecord>> list =
                    Arrays.stream(companyObjects)
                            .map(companyObject -> creator
                                    .insertInto(CHINESEMARKETCOMPANY, CHINESEMARKETCOMPANY.STOCKID, CHINESEMARKETCOMPANY.COMPANYNAME,
                                            CHINESEMARKETCOMPANY.CURRENTPRICETIMESTAMP,
                                            CHINESEMARKETCOMPANY.LAST_UPDATE_DATE_TIME, CHINESEMARKETCOMPANY.PBR,
                                            CHINESEMARKETCOMPANY.PER, CHINESEMARKETCOMPANY.CURRENTPRICE)
                                    .values(companyObject.stockid, companyObject.companyname,
                                            new Timestamp(System.currentTimeMillis()),
                                            new Timestamp(System.currentTimeMillis()),
                                            companyObject.price2BookRatio,
                                            companyObject.price2EarningRatio,
                                            companyObject.currentPrice)
                                    .onDuplicateKeyIgnore())
                            .collect(Collectors.toList());

            LocalDateTime start = LocalDateTime.now();
            System.out.println("Batch start time: " + start);
            creator.batch(list).execute();
            System.out.println("Batch end time: " + LocalDateTime.now());
            System.out.println("Time cost in mins: "
                    + ChronoUnit.MINUTES.between(start, LocalDateTime.now()));

            companyObjects = this.getNextBatch(companyObjectIterator, BATCH_SIZE);
        }
    }

    /**
     * Insert or update companies.
     * 
     * @param companies
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void insertOnDuplicateUpdate(SharesQuote... companies) throws SQLException,
            ClassNotFoundException {
        Iterator<SharesQuote> companyObjectIterator = Arrays.asList(companies).iterator();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            throw e;
        }

        DSLContext creator = this.getDBJooqCreate();

        // Get next batch of company
        SharesQuote[] companyObjects = this.getNextBatch(companyObjectIterator, BATCH_SIZE);

        // Batch insert companies
        while (companyObjects != null && companyObjects.length > 0) {

            // Convert companyobject array to JOOQ query array
            List<InsertOnDuplicateSetMoreStep<ChinesemarketcompanyRecord>> list =
                    Arrays.stream(companyObjects)
                            .map(companyObject -> creator
                                    .insertInto(CHINESEMARKETCOMPANY, CHINESEMARKETCOMPANY.STOCKID, CHINESEMARKETCOMPANY.COMPANYNAME,
                                            CHINESEMARKETCOMPANY.CURRENTPRICE, CHINESEMARKETCOMPANY.CURRENTPRICETIMESTAMP,
                                            CHINESEMARKETCOMPANY.HIGHEST_PRICE, CHINESEMARKETCOMPANY.LOWEST_PRICE,
                                            CHINESEMARKETCOMPANY.CLOSEPRICE, CHINESEMARKETCOMPANY.LAST_UPDATE_DATE_TIME,
                                            CHINESEMARKETCOMPANY.PBR, CHINESEMARKETCOMPANY.PER, CHINESEMARKETCOMPANY.CAPITALIZATIONVALUE,
                                            CHINESEMARKETCOMPANY.MARKETCAP, CHINESEMARKETCOMPANY.TRADINGVOLUME,
                                            CHINESEMARKETCOMPANY.TRADINGVALUE, CHINESEMARKETCOMPANY.OSCILLATION,
                                            CHINESEMARKETCOMPANY.TURNOVERRATE)
                                    .values(companyObject.stockid, companyObject.companyname,
                                            companyObject.currentPrice,
                                            new Timestamp(System.currentTimeMillis()),
                                            companyObject.highestPrice, companyObject.lowestPrice,
                                            companyObject.closePrice,
                                            new Timestamp(System.currentTimeMillis()),
                                            companyObject.price2BookRatio,
                                            companyObject.price2EarningRatio,
                                            companyObject.tradingCap, companyObject.marketCap,
                                            companyObject.dealVolum, companyObject.dealValue,
                                            companyObject.oscillation, companyObject.exchangeRatio)
                                    .onDuplicateKeyUpdate()
                                    .set(CHINESEMARKETCOMPANY.CURRENTPRICE, companyObject.currentPrice)
                                    .set(CHINESEMARKETCOMPANY.PBR, companyObject.price2BookRatio)
                                    .set(CHINESEMARKETCOMPANY.PER, companyObject.price2EarningRatio)
                                    .set(CHINESEMARKETCOMPANY.OPENPRICE, companyObject.openPrice)
                                    .set(CHINESEMARKETCOMPANY.HIGHEST_PRICE, companyObject.highestPrice)
                                    .set(CHINESEMARKETCOMPANY.LOWEST_PRICE, companyObject.lowestPrice)
                                    .set(CHINESEMARKETCOMPANY.CLOSEPRICE, companyObject.closePrice)
                                    .set(CHINESEMARKETCOMPANY.CURRENTPRICETIMESTAMP,
                                            new Timestamp(System.currentTimeMillis()))
                                    .set(CHINESEMARKETCOMPANY.LAST_UPDATE_DATE_TIME,
                                            new Timestamp(System.currentTimeMillis()))
                                    .set(CHINESEMARKETCOMPANY.CAPITALIZATIONVALUE, companyObject.tradingCap)
                                    .set(CHINESEMARKETCOMPANY.MARKETCAP, companyObject.marketCap)
                                    .set(CHINESEMARKETCOMPANY.TRADINGVALUE, companyObject.dealValue)
                                    .set(CHINESEMARKETCOMPANY.TRADINGVOLUME, companyObject.dealVolum)
                                    .set(CHINESEMARKETCOMPANY.OSCILLATION, companyObject.oscillation)
                                    .set(CHINESEMARKETCOMPANY.TURNOVERRATE, companyObject.exchangeRatio))
                            .collect(Collectors.toList());

            creator.batch(list).execute();

            companyObjects = this.getNextBatch(companyObjectIterator, BATCH_SIZE);
        }
    }

    /**
     * Gets existing stock IDs from Database, returns null if caught exception.
     * @return Stock Id set.
     */
    public Set<String> getExistingStockIDs() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            DSLContext creator = this.getDBJooqCreate();
            Result<Record1<String>> r = creator.select(CHINESEMARKETCOMPANY.STOCKID).from(CHINESEMARKETCOMPANY).fetch();
            r.toArray(new Record[0])[0].get(CHINESEMARKETCOMPANY.field(CHINESEMARKETCOMPANY.STOCKID));

            return Arrays
                    .stream(r.toArray(new Record[0]))
                    .map(record -> record.get(CHINESEMARKETCOMPANY.field(CHINESEMARKETCOMPANY.STOCKID)))
                    .collect(Collectors.toSet());
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "DB driver not found", e);
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.SEVERE, "SQL Exception", e);
        }
        return null;
    }

    /**
     * Gets existing stock IDs from Database, returns null if caught exception.
     * @return Stock Id set.
     */
    public ChinesemarketcompanyRecord[] getExistingStocks() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            DSLContext creator = this.getDBJooqCreate();
            Result<ChinesemarketcompanyRecord> records = creator.selectFrom(CHINESEMARKETCOMPANY).fetch();
            return records.toArray(new ChinesemarketcompanyRecord[0]);
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "DB driver not found", e);
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.SEVERE, "SQL Exception", e);
        }
        return null;
    }

    /**
     * Get next batch company
     *
     * @param iterator
     * @param size
     * @return
     */
    private SharesQuote[] getNextBatch(Iterator<SharesQuote> iterator, int size) {
        List<SharesQuote> list = new LinkedList<>();

        int count = 0;
        while (count++ < size && iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list.toArray(new SharesQuote[0]);
    }
}
