package dataEngineer;

import JooqORM.tables.ChineseMarketCompany;
import JooqORM.tables.Cmarketearning;
import JooqORM.tables.UsmarketCompany;
import JooqORM.tables.records.ChineseMarketCompanyRecord;
import JooqORM.tables.records.CmarketearningRecord;
import JooqORM.tables.records.UsmarketCompanyRecord;
import dataEngineer.data.FinancialData;
import dataEngineer.data.SharesQuote;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;
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
import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static JooqORM.Tables.CHINESE_MARKET_COMPANY;
import static JooqORM.Tables.CMARKETEARNING;
import static JooqORM.Tables.USMARKET_COMPANY;

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
     * Reads credential and authenticate SQL connection.
     *
     * @return
     */
    public static DatabaseManager initializeDataManager() {
        try {
            return DatabaseManager
                    .GetDatabaseManagerInstance(MarketConstant.RESOURCE_CONFIG)
                    .Authenticate();
        } catch (SQLException exc) {
            exc.printStackTrace();
            return null;
        }
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
        Optional<Table<?>> table =
                this.GetTable(this.globalCreate, CHINESE_MARKET_COMPANY.getName());
        if (!table.isPresent()) {
            this.globalCreate
                    .createTable(CHINESE_MARKET_COMPANY)
                    .columns(CHINESE_MARKET_COMPANY.fields())
                    .execute();
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
        List<SharesQuote> companyObjects = this.getNextBatch(companyObjectIterator, BATCH_SIZE);

        // Batch insert companies
        while (companyObjects != null && companyObjects.size() > 0) {

            // Convert companyobject array to JOOQ query array
            List<InsertFinalStep<ChineseMarketCompanyRecord>> list =
                    Arrays.stream(companyObjects.toArray(new SharesQuote[companyObjects.size()]))
                            .map(companyObject -> creator
                                    .insertInto(CHINESE_MARKET_COMPANY,
                                            CHINESE_MARKET_COMPANY.STOCKID,
                                            CHINESE_MARKET_COMPANY.COMPANYNAME,
                                            CHINESE_MARKET_COMPANY.CURRENTPRICETIMESTAMP,
                                            CHINESE_MARKET_COMPANY.LAST_UPDATE_DATE_TIME,
                                            CHINESE_MARKET_COMPANY.PBR, CHINESE_MARKET_COMPANY.PER,
                                            CHINESE_MARKET_COMPANY.CURRENTPRICE)
                                    .values(companyObject.getStockId(),
                                            companyObject.getCompanyname(),
                                            new Timestamp(System.currentTimeMillis()),
                                            new Timestamp(System.currentTimeMillis()),
                                            companyObject.getPrice2BookRatio(),
                                            companyObject.getPrice2EarningRatio(),
                                            companyObject.getCurrentPrice())
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
    public void insertOnDuplicateUpdate(TableImpl table, SharesQuote... companies)
            throws SQLException, ClassNotFoundException {

        // Data validation
        Assert.assertNotNull(table);
        Assert.assertNotNull(companies);

        Iterator<SharesQuote> companyObjectIterator = Arrays.asList(companies).iterator();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            throw e;
        }

        // Get next batch of company
        List<SharesQuote> companyObjects = this.getNextBatch(companyObjectIterator, BATCH_SIZE);

        // Batch insert companies
        while (companyObjects != null && companyObjects.size() > 0) {
            if (table instanceof ChineseMarketCompany) {
                this.insertToChineseMarketTable(companyObjects.toArray(new SharesQuote[companyObjects.size()]));
            } else if (table instanceof UsmarketCompany) {
                this.insertToUSMarketTable(companyObjects.toArray(new SharesQuote[companyObjects.size()]));
            } else {
                throw new SQLException("Unspecified Table type.");
            }
            companyObjects = this.getNextBatch(companyObjectIterator, BATCH_SIZE);
        }
    }

    /**
     * Insert or update companies.
     *
     * @param financialDatas
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void insertOnDuplicateUpdate(Cmarketearning table, FinancialData... financialDatas)
            throws SQLException, ClassNotFoundException {

        // Data validation
        Assert.assertNotNull(table);
        Assert.assertNotNull(financialDatas);

        Iterator<FinancialData> financialObjectIterator = Arrays.asList(financialDatas).iterator();

        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "", e);
            throw e;
        }

        // Get next batch of company
        List<FinancialData> financialObjects = this.getNextBatch(financialObjectIterator, BATCH_SIZE);

        // Batch insert companies
        while (financialObjects != null && financialObjects.size() > 0) {
            this.insertToChineseMarketEaringTable(financialObjects.toArray(new FinancialData[financialObjects.size()]));
            financialObjects = this.getNextBatch(financialObjectIterator, BATCH_SIZE);
        }
    }

    /**
     * Insert companies to Chinese Market table.
     * 
     * @param companyObjects
     * @throws SQLException
     */
    private void insertToChineseMarketTable(SharesQuote[] companyObjects) throws SQLException {

        DSLContext creator = this.getDBJooqCreate();

        // Convert companyobject array to JOOQ query array
        List<InsertOnDuplicateSetMoreStep<ChineseMarketCompanyRecord>> list =
                Arrays.stream(companyObjects)
                        .map(companyObject -> creator
                                .insertInto(CHINESE_MARKET_COMPANY, CHINESE_MARKET_COMPANY.STOCKID,
                                        CHINESE_MARKET_COMPANY.COMPANYNAME,
                                        CHINESE_MARKET_COMPANY.CURRENTPRICE,
                                        CHINESE_MARKET_COMPANY.CURRENTPRICETIMESTAMP,
                                        CHINESE_MARKET_COMPANY.HIGHEST_PRICE,
                                        CHINESE_MARKET_COMPANY.LOWEST_PRICE,
                                        CHINESE_MARKET_COMPANY.CLOSE_PRICE,
                                        CHINESE_MARKET_COMPANY.LAST_UPDATE_DATE_TIME,
                                        CHINESE_MARKET_COMPANY.PBR, CHINESE_MARKET_COMPANY.PER,
                                        CHINESE_MARKET_COMPANY.CAPITALIZATIONVALUE,
                                        CHINESE_MARKET_COMPANY.MARKETCAP,
                                        CHINESE_MARKET_COMPANY.TRADINGVOLUME,
                                        CHINESE_MARKET_COMPANY.TRADINGVALUE,
                                        CHINESE_MARKET_COMPANY.OSCILLATION,
                                        CHINESE_MARKET_COMPANY.TURNOVERRATE,
                                        CHINESE_MARKET_COMPANY.LISTING_DATE)
                                .values(companyObject.getStockId(), companyObject.getCompanyname(),
                                        companyObject.getCurrentPrice(),
                                        new Timestamp(System.currentTimeMillis()),
                                        companyObject.getHighestPrice(),
                                        companyObject.getLowestPrice(),
                                        companyObject.getClosePrice(),
                                        new Timestamp(System.currentTimeMillis()),
                                        companyObject.getPrice2BookRatio(),
                                        companyObject.getPrice2EarningRatio(),
                                        companyObject.getTradingCap(),
                                        companyObject.getMarketCap(), companyObject.getDealVolum(),
                                        companyObject.getDealValue(),
                                        companyObject.getOscillation(),
                                        companyObject.getExchangeRatio(),
                                        new Timestamp(companyObject.getListingDate().getTime()))
                                .onDuplicateKeyUpdate()
                                .set(CHINESE_MARKET_COMPANY.CURRENTPRICE,
                                        companyObject.getCurrentPrice())
                                .set(CHINESE_MARKET_COMPANY.PBR, companyObject.getPrice2BookRatio())
                                .set(CHINESE_MARKET_COMPANY.PER,
                                        companyObject.getPrice2EarningRatio())
                                .set(CHINESE_MARKET_COMPANY.OPENPRICE, companyObject.getOpenPrice())
                                .set(CHINESE_MARKET_COMPANY.HIGHEST_PRICE,
                                        companyObject.getHighestPrice())
                                .set(CHINESE_MARKET_COMPANY.LOWEST_PRICE,
                                        companyObject.getLowestPrice())
                                .set(CHINESE_MARKET_COMPANY.CLOSE_PRICE,
                                        companyObject.getClosePrice())
                                .set(CHINESE_MARKET_COMPANY.CURRENTPRICETIMESTAMP,
                                        new Timestamp(System.currentTimeMillis()))
                                .set(CHINESE_MARKET_COMPANY.LAST_UPDATE_DATE_TIME,
                                        new Timestamp(System.currentTimeMillis()))
                                .set(CHINESE_MARKET_COMPANY.CAPITALIZATIONVALUE,
                                        companyObject.getTradingCap())
                                .set(CHINESE_MARKET_COMPANY.MARKETCAP, companyObject.getMarketCap())
                                .set(CHINESE_MARKET_COMPANY.TRADINGVALUE,
                                        companyObject.getDealValue())
                                .set(CHINESE_MARKET_COMPANY.TRADINGVOLUME,
                                        companyObject.getDealVolum())
                                .set(CHINESE_MARKET_COMPANY.OSCILLATION,
                                        companyObject.getOscillation())
                                .set(CHINESE_MARKET_COMPANY.TURNOVERRATE,
                                        companyObject.getExchangeRatio()))
                        .collect(Collectors.toList());
        creator.batch(list).execute();
    }

    private void insertToChineseMarketEaringTable(FinancialData[] financialDatas) throws SQLException {
        DSLContext creator = this.getDBJooqCreate();
        // Convert companyobject array to JOOQ query array
        List<InsertOnDuplicateSetMoreStep<CmarketearningRecord>> list =
                Arrays.stream(financialDatas)
                        .map(financialObject -> creator
                                .insertInto(CMARKETEARNING, CMARKETEARNING.STOCKID, CMARKETEARNING.GROSSMARGIN, CMARKETEARNING.REPORTURL)
                                .values(financialObject.getStockId(),
                                        financialObject.getGrossMargin(),
                                        financialObject.getReporturl())
                                .onDuplicateKeyUpdate()
                                .set(CMARKETEARNING.GROSSMARGIN, financialObject.getGrossMargin())
                                .set(CMARKETEARNING.REPORTURL, financialObject.getReporturl()))
                        .collect(Collectors.toList());
        creator.batch(list).execute();
    }

    /**
     * Insert companies to U.S. Market table.
     * 
     * @param companyObjects
     * @throws SQLException
     */
    private void insertToUSMarketTable(SharesQuote[] companyObjects) throws SQLException {

        DSLContext creator = this.getDBJooqCreate();

        // Convert companyobject array to JOOQ query array
        List<InsertOnDuplicateSetMoreStep<UsmarketCompanyRecord>> list =
                Arrays.stream(companyObjects)
                        .map(companyObject -> creator
                                .insertInto(USMARKET_COMPANY, USMARKET_COMPANY.STOCKID,
                                        USMARKET_COMPANY.COMPANYNAME,
                                        USMARKET_COMPANY.CURRENTPRICE,
                                        USMARKET_COMPANY.CURRENTPRICETIMESTAMP,
                                        USMARKET_COMPANY.HIGHEST_PRICE,
                                        USMARKET_COMPANY.LOWEST_PRICE,
                                        USMARKET_COMPANY.CLOSE_PRICE,
                                        USMARKET_COMPANY.LAST_UPDATE_DATE_TIME,
                                        USMARKET_COMPANY.PBR, USMARKET_COMPANY.PER,
                                        USMARKET_COMPANY.CAPITALIZATIONVALUE,
                                        USMARKET_COMPANY.MARKETCAP, USMARKET_COMPANY.TRADINGVOLUME,
                                        USMARKET_COMPANY.TRADINGVALUE,
                                        USMARKET_COMPANY.OSCILLATION,
                                        USMARKET_COMPANY.TURNOVERRATE,
                                        USMARKET_COMPANY.LISTING_DATE)
                                .values(companyObject.getStockId(), companyObject.getCompanyname(),
                                        companyObject.getCurrentPrice(),
                                        new Timestamp(System.currentTimeMillis()),
                                        companyObject.getHighestPrice(),
                                        companyObject.getLowestPrice(),
                                        companyObject.getClosePrice(),
                                        new Timestamp(System.currentTimeMillis()),
                                        companyObject.getPrice2BookRatio(),
                                        companyObject.getPrice2EarningRatio(),
                                        companyObject.getTradingCap(),
                                        companyObject.getMarketCap(), companyObject.getDealVolum(),
                                        companyObject.getDealValue(),
                                        companyObject.getOscillation(),
                                        companyObject.getExchangeRatio(),
                                        new Timestamp(companyObject.getListingDate().getTime()))
                                .onDuplicateKeyUpdate()
                                .set(USMARKET_COMPANY.CURRENTPRICE, companyObject.getCurrentPrice())
                                .set(USMARKET_COMPANY.PBR, companyObject.getPrice2BookRatio())
                                .set(USMARKET_COMPANY.PER, companyObject.getPrice2EarningRatio())
                                .set(USMARKET_COMPANY.OPENPRICE, companyObject.getOpenPrice())
                                .set(USMARKET_COMPANY.HIGHEST_PRICE,
                                        companyObject.getHighestPrice())
                                .set(USMARKET_COMPANY.LOWEST_PRICE, companyObject.getLowestPrice())
                                .set(USMARKET_COMPANY.CLOSE_PRICE, companyObject.getClosePrice())
                                .set(USMARKET_COMPANY.CURRENTPRICETIMESTAMP,
                                        new Timestamp(System.currentTimeMillis()))
                                .set(USMARKET_COMPANY.LAST_UPDATE_DATE_TIME,
                                        new Timestamp(System.currentTimeMillis()))
                                .set(USMARKET_COMPANY.CAPITALIZATIONVALUE,
                                        companyObject.getTradingCap())
                                .set(USMARKET_COMPANY.MARKETCAP, companyObject.getMarketCap())
                                .set(USMARKET_COMPANY.TRADINGVALUE, companyObject.getDealValue())
                                .set(USMARKET_COMPANY.TRADINGVOLUME, companyObject.getDealVolum())
                                .set(USMARKET_COMPANY.OSCILLATION, companyObject.getOscillation())
                                .set(USMARKET_COMPANY.TURNOVERRATE,
                                        companyObject.getExchangeRatio()))
                        .collect(Collectors.toList());
        creator.batch(list).execute();
    }

    /**
     * Gets existing stock IDs from Database, returns null if caught exception.
     * 
     * @return Stock Id set.
     */
    public ChineseMarketCompanyRecord[] getExistingStocksChinese() {
        return this.getExistingItemsInDatabase(new ChineseMarketCompanyRecord[0], CHINESE_MARKET_COMPANY);
    }

    public CmarketearningRecord[] getExistingCmarketEarning(){
        return this.getExistingItemsInDatabase(new CmarketearningRecord[0], CMARKETEARNING);
    }

    /**
     * Gets existing stock IDs from Database, returns null if caught exception.
     * 
     * @return Stock Id set.
     */
    public UsmarketCompanyRecord[] getExistingStocksUS() {
        return this.getExistingItemsInDatabase(new UsmarketCompanyRecord[0], USMARKET_COMPANY);
    }

    /**
     * Retrieve existing items from database in the given table instance.
     * @param arr : return array type
     * @param tableInstance
     * @param <T>
     * @return
     */
    private <T extends Record> T[] getExistingItemsInDatabase(T[] arr, TableImpl tableInstance){
        try {
            Class.forName("com.mysql.jdbc.Driver");

            DSLContext creator = this.getDBJooqCreate();
            Result<T> records =
                    creator.selectFrom(tableInstance).fetch();
            return records.toArray(arr);
        } catch (ClassNotFoundException e) {
            Logger.getGlobal().log(Level.SEVERE, "DB driver not found", e);
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.SEVERE, "SQL Exception", e);
        }
        return arr;
    }

    /**
     * Get next batch company
     *
     * @param iterator
     * @param size
     * @return
     */
    private <T> List<T> getNextBatch(Iterator<T> iterator, int size) {
        List<T> list = new LinkedList<>();
        int count = 0;
        while (count++ < size && iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}
