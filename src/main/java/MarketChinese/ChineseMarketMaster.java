package MarketChinese;

import JooqORM.tables.records.ChineseMarketCompanyRecord;
import JooqORM.tables.records.CmarketearningRecord;
import dataEngineer.DatabaseManager;
import dataEngineer.data.FinancialData;
import dataEngineer.data.SharesQuote;
import dataEngineer.StockCompanyCollection;
import dataEngineer.data.WebParserData;
import dataEngineer.financeWebEngine.XueqiuWebParser;
import org.apache.commons.cli.CommandLine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import util.MarketConstant;
import util.RunmeFuture;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static JooqORM.Tables.CHINESE_MARKET_COMPANY;
import static JooqORM.Tables.CMARKETEARNING;

/**
 * Chinese stock market master.
 */
public class ChineseMarketMaster {

    CommandLine cmd;
    SharesQuote[] companiesInCsvFile;
    boolean isInited = false;
    public ChineseMarketMaster(CommandLine cmd){
        this.cmd = cmd;
    }

    /**
     * Read company list from .csv file
     */
    public void init(){
        Assert.assertNotNull(cmd);
        StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
        this.companiesInCsvFile = companyCollection.queryCompanyListChinese(cmd.hasOption(MarketConstant.IS_UNDER_INTELLIJ));
        this.isInited = true;
    }

    public void run() {

        System.out.println("Now in Chinese market Master running.");

        if(!this.isInited)
            this.init();

        while (true) {
            DateTime now = new DateTime(System.currentTimeMillis(), DateTimeZone.forID("Asia/Shanghai"));
            try {
                if (now.getDayOfWeek() > 5) {
                    System.out.println(now.toString() + ": Sleep 2 hours on weekend.");
                    Thread.sleep(2 * 60 * 60 * 1000);
                    continue;
                }
                if (now.getHourOfDay() < 8 || now.getHourOfDay() > 17) {
                    System.out.println(now.toString() + ": Sleep 5 minutes off hour.");
                    Thread.sleep(5 * 60 * 1000);
                    continue;
                }
            } catch (InterruptedException exc) {
                System.out.println("Interrupted exception received, gonna launch querryAndUpdate...");
            }
            this.querryAndUpdate();
            System.out.println(LocalDateTime.now().toString() + " One loop Job done.");
        }
    }


    public void parseAndWriteFinancialDate() {
        System.out.println("parseAndWriteFinancialDate");

        DatabaseManager databaseManager = DatabaseManager.initializeDataManager();
        if (databaseManager == null) {
            System.err.println("Failed to initialize database manager, please check database credential. \t Quit.");
            System.exit(1);
        }

        // Retrieve existing records in database and sort the toBeUpdated company array
        String[] toBeUpdatedCompanyIds =
                Arrays.stream(databaseManager.getExistingStocksChinese()).map(record -> record.getStockid()).toArray(String[]::new);
        Set<String> existingCmarketearningRecordSet =
                Arrays.stream(databaseManager.getExistingCmarketEarning()).map(record -> record.getStockid()).collect(Collectors.toSet());
        WebParserData.moveUnsearchedItemAhead(existingCmarketearningRecordSet, toBeUpdatedCompanyIds);

        XueqiuWebParser xueqiuWebParser = new XueqiuWebParser();
        for(String stockId : toBeUpdatedCompanyIds) {
            try {
                FinancialData financialData = xueqiuWebParser.queryFinancialData(stockId);
                databaseManager.insertOnDuplicateUpdate(CMARKETEARNING, financialData);
            } catch (IOException exc) {
                System.err.println("Failed to parse symbol: '" + stockId + "'");
                exc.printStackTrace();
            }catch (ClassNotFoundException classNotFoundException){
                System.err.println("Failed to parse symbol: '" + stockId + "'");
                classNotFoundException.printStackTrace();
            } catch (SQLException sqlException){
                System.err.println("Failed to parse symbol: '" + stockId + "'");
                sqlException.printStackTrace();
            }
        }
    }

    /**
     * Assigns the task and collects the result.
     *
     */
    public void querryAndUpdate() {
        try {
            System.out.println("HHa alive");


            DatabaseManager databaseManager = DatabaseManager.initializeDataManager();
            if (databaseManager == null) {
                System.err.println("Failed to initialize database manager, please check database credential. \t Quit.");
                System.exit(1);
            }

            ChineseMarketCompanyRecord[] existingCompanyRecordsInDatabase = databaseManager.getExistingStocksChinese();

            System.out.println("Company size: " + companiesInCsvFile.length);
            System.out.println("Querying company stock from webpage....");

            ExecutorService executorService = Executors.newFixedThreadPool(MarketConstant.WEB_PARSER_SIZE);

            System.out.println(LocalDateTime.now().toString() + " "
                    + (companiesInCsvFile.length - existingCompanyRecordsInDatabase.length)
                    + " Companies remain to be added to database.");
            this.sortCompanyArray(existingCompanyRecordsInDatabase, companiesInCsvFile);

            List<RunmeFuture> futureList = new LinkedList<>();

            // Submit company query task
            for (SharesQuote companyObject : companiesInCsvFile) {
                RunmeFuture runmeFuture = new RunmeFuture(executorService::submit, companyObject, new XueqiuWebParser());
                futureList.add(runmeFuture);
            }

            while (!executorService.isTerminated() && !futureList.isEmpty()) {

                // Filter out the tasks that have been consumed
                futureList =
                        futureList
                                .stream()
                                .filter(future -> !future.isResultConsumed)
                                .collect(Collectors.toList());

                // Remove timeout task
                futureList =
                        futureList
                                .stream()
                                .filter(future -> !(future.startTimeMillis.isPresent() && (future.startTimeMillis.get()
                                        + MarketConstant.TASK_MAXIMUM_TIMEOUT < System.currentTimeMillis()) && !future.result.isPresent()))
                                .collect(Collectors.toList());

                System.out.println("Remain future list size: " + futureList.size());
                DateTime now = new DateTime(System.currentTimeMillis(), DateTimeZone.forID("Asia/Shanghai"));

                for (RunmeFuture runmeFuture : futureList) {
                    if (!runmeFuture.result.isPresent())
                        continue;
                    SharesQuote sharesQuote = runmeFuture.result.get();
                    runmeFuture.isResultConsumed = true;
                    try {
                        databaseManager.insertOnDuplicateUpdate(CHINESE_MARKET_COMPANY, sharesQuote);
                        System.out.println(now.toString()
                                + ": Succeed on update company: " + sharesQuote.getCompanyname()
                                + ";  StockID: " + sharesQuote.getStockId());
                    } catch (SQLException exc) {
                        exc.printStackTrace();
                        System.out.println(sharesQuote);
                        System.exit(1);
                    } catch (ClassNotFoundException exc) {
                        System.out.println(sharesQuote);
                        exc.printStackTrace();
                        System.exit(1);
                    }catch (Exception exc){
                        System.out.println(sharesQuote);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception exc) {

                }
            }
        } catch (Exception exc){
            exc.printStackTrace();
        }
        finally {
            DatabaseManager.close();
        }
    }




    /**
     * 1: Sort company array so that those unsearched company moved to head of array and those
     * companies already in databaes moved to tail.
     *
     * 2: Sort companiesInCsvFile which is in database by lastUpdateDatetime order, e.g: the latest updated
     * company move to tail.
     *
     * @param existingCompanyRecords
     *            : company records in database.
     * @param companyArray
     *            : All the companiesInCsvFile to be sorted.
     */
    private void sortCompanyArray(ChineseMarketCompanyRecord[] existingCompanyRecords, SharesQuote[] companyArray) {

        if (existingCompanyRecords == null || companyArray == null)
            return;

        Set<String> stockIdSet =
                Arrays.stream(existingCompanyRecords)
                        .map(record -> record.getStockid())
                        .collect(Collectors.toSet());


        // 1: Sort company array so that those unsearched company moved to head of array and those
        // companiesInCsvFile already in databaes moved to tail.
        int nextSeenIdx = WebParserData.moveUnsearchedDataAhead(stockIdSet, companyArray);


        // 2: Sort companiesInCsvFile which is in database by lastUpdateDatetime order, e.g: the latest
        // updated company move to tail.
        HashMap<String, ChineseMarketCompanyRecord> stockIdCompanyRecordMap = new HashMap<>();
        HashMap<String, SharesQuote> stociIdSharesQuoteMap = new HashMap<>();
        for (ChineseMarketCompanyRecord companyRecord : existingCompanyRecords) {
            stockIdCompanyRecordMap.put(companyRecord.getStockid(), companyRecord);
        }
        for (int idx = nextSeenIdx + 1; idx < companyArray.length; idx++) {
            stociIdSharesQuoteMap.put(companyArray[idx].getStockId(), companyArray[idx]);
        }

        // These two map size should be equal otherwise the first sort method would be wrong
        Assert.assertEquals(stociIdSharesQuoteMap.size(), stockIdCompanyRecordMap.size());

        // Sort existingCompanyRecords
        Arrays.sort(existingCompanyRecords, (ChineseMarketCompanyRecord a, ChineseMarketCompanyRecord b) -> a
                .getLastUpdateDateTime()
                .toLocalDateTime()
                .compareTo(b.getLastUpdateDateTime().toLocalDateTime()));

        // Insertion sort the rest part of array based on the order of existingCompanyRecords
        for (int idx = nextSeenIdx + 1; idx < companyArray.length; idx++) {
            companyArray[idx] =
                    stociIdSharesQuoteMap.get(existingCompanyRecords[idx - nextSeenIdx - 1]
                            .getStockid());
        }
    }
}
