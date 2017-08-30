package MarketChinese;
import dataEngineer.DatabaseManager;
import dataEngineer.data.CompanyInfoFileAccessor;
import dataEngineer.data.FinancialData;
import dataEngineer.data.SharesQuote;
import dataEngineer.financeWebEngine.XueqiuWebParser;
import mongoDb.MongoDBConnector;
import org.apache.commons.cli.CommandLine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import util.MarketConstant;
import util.RunmeFuture;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Chinese stock market master.
 */
public class ChineseMarketMaster {

    CommandLine cmd;
    SharesQuote[] companiesInCsvFile;
    CompanyInfoFileAccessor companyInfoFileAccessor;
    boolean isInited = false;
    public ChineseMarketMaster(CommandLine cmd){
        this.cmd = cmd;
        companyInfoFileAccessor = CompanyInfoFileAccessor.getInstance(cmd.hasOption(MarketConstant.IS_UNDER_INTELLIJ));
    }

    /**
     * Read company list from .csv file
     */
    public void init(){
        Assert.assertNotNull(cmd);
        this.companiesInCsvFile = companyInfoFileAccessor.queryCompanyListChinese();
        this.isInited = true;
    }

    public void run() {

        System.out.println("Now in Chinese market Master running.");

        if(!this.isInited)
            this.init();

        boolean isNewDay = true;
        while (true) {
            DateTime now = new DateTime(System.currentTimeMillis(), DateTimeZone.forID("Asia/Shanghai"));
            try {
                if (now.getDayOfWeek() > 5) {
                    System.out.println(now.toString() + ": Sleep 2 hours on weekend.");
                    Thread.sleep(2 * 60 * 60 * 1000);
                    continue;
                }
                if (now.getHourOfDay() < 8 || now.getHourOfDay() > 17) {
                    isNewDay = true;
                    System.out.println(now.toString() + ": Sleep 5 minutes off hour.");
                    Thread.sleep(5 * 60 * 1000);
                    continue;
                }

                // Retrieve IPO companies once daily
                if (isNewDay){
                    isNewDay = false;
                    this.retrieveIPOstocks();
                }
            } catch (InterruptedException exc) {
                System.out.println("Interrupted exception received, gonna launch querryAndUpdate...");
            }
            this.querryAndUpdate();
            System.out.println(LocalDateTime.now().toString() + " One loop Job done.");
        }
    }

    public void retrieveIPOstocks(){
        XueqiuWebParser xueqiuWebParser = new XueqiuWebParser();
        List<SharesQuote> newIPOCompanies = xueqiuWebParser.parseNewIPOCompanies();
        try {
            this.companyInfoFileAccessor.appendChineseIPO(newIPOCompanies.toArray(new SharesQuote[0]));
        }catch (IOException exc){
            System.err.println("Failed on write back new IPOs.\n");
            exc.printStackTrace();
        }

        // update in memory data
        this.companiesInCsvFile = companyInfoFileAccessor.queryCompanyListChinese();
    }


    public void parseAndWriteFinancialDate() {
        System.out.println("parseAndWriteFinancialDate");

        DatabaseManager databaseManager = DatabaseManager.initializeDataManager();
        if (databaseManager == null) {
            System.err.println("Failed to initialize database manager, please check database credential. \t Quit.");
            System.exit(1);
        }

        // Retrieve existing records in database and sort the toBeUpdated company array
        SharesQuote[] chineseStockMarkets = databaseManager.RetrieveCollection(MongoDBConnector.CHINESE_TABLE).toArray(new SharesQuote[0]);
        Set<String> existingCmarketearningRecordSet =
                Arrays.stream(chineseStockMarkets)
                        .filter(sharesQuote -> sharesQuote.getFinancialData() != null)
                        .map(sharesQuote -> sharesQuote.get_id())
                        .collect(Collectors.toSet());
        SharesQuote.moveUnsearchedDataAhead(existingCmarketearningRecordSet, chineseStockMarkets);

        XueqiuWebParser xueqiuWebParser = new XueqiuWebParser();
        for(SharesQuote sharesQuote : chineseStockMarkets) {
            try {
                FinancialData financialData = xueqiuWebParser.queryFinancialData(sharesQuote.get_id());
                sharesQuote.setFinancialData(financialData);
                databaseManager.insertDocument(MongoDBConnector.CHINESE_TABLE, sharesQuote);
            } catch (IOException exc) {
                System.err.println("Failed to parse symbol: '" + sharesQuote.get_id() + "'");
                exc.printStackTrace();
            }
        }
    }

    /**
     * Assigns the task and collects the result.
     *
     */
    public void querryAndUpdate() {
        DatabaseManager databaseManager = null;
        try {
            System.out.println("HHa alive");
            databaseManager = DatabaseManager.initializeDataManager();
            if (databaseManager == null) {
                System.err.println("Failed to initialize database manager, please check database credential. \t Quit.");
                System.exit(1);
            }

            SharesQuote[] existingCompanyRecordsInDatabase = databaseManager.RetrieveCollection(MongoDBConnector.CHINESE_TABLE).toArray(new SharesQuote[0]);

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
                        databaseManager.insertDocument(MongoDBConnector.CHINESE_TABLE, sharesQuote);
                        System.out.println(now.toString()
                                + ": Succeed on update company: " + sharesQuote.getCompanyname()
                                + ";  StockID: " + sharesQuote.get_id());
                    } catch (Exception exc){
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
            if (databaseManager!=null)
                databaseManager.close();
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
    private void sortCompanyArray(SharesQuote[] existingCompanyRecords, SharesQuote[] companyArray) {

        if (existingCompanyRecords == null || companyArray == null)
            return;

        Set<String> stockIdSet =
                Arrays.stream(existingCompanyRecords)
                        .map(record -> record.get_id())
                        .collect(Collectors.toSet());


        // 1: Sort company array so that those unsearched company moved to head of array and those
        // companiesInCsvFile already in databaes moved to tail.
        int nextSeenIdx = SharesQuote.moveUnsearchedDataAhead(stockIdSet, companyArray);


        // 2: Sort companiesInCsvFile which is in database by lastUpdateDatetime order, e.g: the latest
        // updated company move to tail.
        HashMap<String, SharesQuote> stockIdCompanyRecordMap = new HashMap<>();
        HashMap<String, SharesQuote> stociIdSharesQuoteMap = new HashMap<>();
        for (SharesQuote companyRecord : existingCompanyRecords) {
            stockIdCompanyRecordMap.put(companyRecord.get_id(), companyRecord);
        }
        for (int idx = nextSeenIdx + 1; idx < companyArray.length; idx++) {
            stociIdSharesQuoteMap.put(companyArray[idx].get_id(), companyArray[idx]);
        }

        // These two map size should be equal otherwise the first sort method would be wrong
        Assert.assertEquals(stociIdSharesQuoteMap.size(), stockIdCompanyRecordMap.size());

        // Sort existingCompanyRecords
        Arrays.sort(existingCompanyRecords, Comparator.comparing(SharesQuote::getLastUpdatedTime));

        // Insertion sort the rest part of array based on the order of existingCompanyRecords
        for (int idx = nextSeenIdx + 1; idx < companyArray.length; idx++) {
            companyArray[idx] =
                    stociIdSharesQuoteMap.get(existingCompanyRecords[idx - nextSeenIdx - 1]
                            .get_id());
        }
    }
}
