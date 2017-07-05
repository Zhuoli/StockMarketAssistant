package MarketUS;

import dataEngineer.DatabaseManager;
import dataEngineer.data.SharesQuote;
import dataEngineer.StockCompanyCollection;
import dataEngineer.financeWebEngine.NasdaqWebParser;
import mongoDb.MongoDBConnector;
import org.apache.commons.cli.CommandLine;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import util.MarketConstant;
import util.RunmeFuture;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


/**
 * US market master.
 */
public class USMarketMaster {

    CommandLine cmd;
    SharesQuote[] companies;
    boolean isInited = false;

    public USMarketMaster(CommandLine cmd) {
        this.cmd = cmd;
    }

    public void init() {
        Assert.assertNotNull(cmd);
        StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
        this.companies =
                companyCollection.queryCompanyListUS(cmd
                        .hasOption(MarketConstant.IS_UNDER_INTELLIJ));
        this.isInited = true;
    }

    /**
     * Assigns the task and collects the result.
     *
     */
    public void querryAndUpdate() {
        DatabaseManager databaseManager = null;
        try {
            System.out.println("HHa alive");

            databaseManager = DatabaseManager.initializeDataManager().Authenticate();
            if (databaseManager == null) {
                System.err
                        .println("Failed to initialize database manager, please check database credential. \t Quit.");
                System.exit(1);
            }
            SharesQuote[] existingCompanyRecords = databaseManager.RetrieveCollection(MongoDBConnector.US_TABLE).toArray(new SharesQuote[0]);

            System.out.println("Company size: " + companies.length);
            System.out.println("Querying company stock from webpage....");

            ExecutorService executorService =
                    Executors.newFixedThreadPool(MarketConstant.WEB_PARSER_SIZE);

            System.out.println(LocalDateTime.now().toString() + " "
                    + (companies.length - existingCompanyRecords.length)
                    + " Companies remain to be added to database.");
            this.sortArray(existingCompanyRecords, companies);

            List<RunmeFuture> futureList = new LinkedList<>();

            // Submit company query task
            for (SharesQuote companyObject : companies) {
                RunmeFuture runmeFuture =
                        new RunmeFuture(executorService::submit, companyObject,
                                new NasdaqWebParser());
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
                                .filter(future -> !(future.startTimeMillis.isPresent()
                                        && (future.startTimeMillis.get()
                                                + MarketConstant.TASK_MAXIMUM_TIMEOUT < System
                                                    .currentTimeMillis()) && !future.result
                                        .isPresent()))
                                .collect(Collectors.toList());

                System.out.println("Remain future list size: " + futureList.size());
                DateTime now =
                        new DateTime(System.currentTimeMillis(),
                                DateTimeZone.forID("America/New_York"));

                for (RunmeFuture runmeFuture : futureList) {
                    if (!runmeFuture.result.isPresent())
                        continue;
                    SharesQuote sharesQuote = runmeFuture.result.get();
                    runmeFuture.isResultConsumed = true;
                    try {
                        databaseManager.insertDocument(MongoDBConnector.US_TABLE, sharesQuote);
                        System.out.println(now.toString() + ": Succeed on update company: "
                                + sharesQuote.getCompanyname() + ";  StockID: "
                                + sharesQuote.get_id());
                    } catch (Exception exc) {
                        System.err.println(sharesQuote);
                        exc.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            if(databaseManager!=null)
                databaseManager.close();
        }
    }

    /**
     *
     */
    public void run() {
        System.out.println("Now in US market Master running.");

        if (!this.isInited)
            this.init();

        while (true) {
            DateTime now =
                    new DateTime(System.currentTimeMillis(), DateTimeZone.forID("America/New_York"));
            try {
                if (now.getDayOfWeek() > 5) {
                    System.out.println(now.toString()
                            + ": Nasdaq assistant: Sleep 2 hours on weekend.");
                    Thread.sleep(2 * 60 * 60 * 1000);
                    continue;
                }
                if (now.getHourOfDay() < 8 || now.getHourOfDay() > 17) {
                    System.out.println(now.toString()
                            + ": Nasdaq assistant: Sleep 5 minutes off hour.");
                    Thread.sleep(5 * 60 * 1000);
                    continue;
                }
            } catch (InterruptedException exc) {
                System.out
                        .println("Nasdaq assistant: Interrupted exception received, gonna launch querryAndUpdate...");
            }
            this.querryAndUpdate();
            System.out.println(LocalDateTime.now().toString()
                    + "Nasdaq assistant: One loop Job done.");
        }
    }

    /**
     * 1: Sort company array so that those unsearched company moved to head of array and those
     * companies already in databaes moved to tail.
     *
     * 2: Sort companies which is in database by lastUpdateDatetime order, e.g: the latest updated
     * company move to tail.
     *
     * @param existingCompanyRecords
     *            : company records in database.
     * @param array
     *            : All the companies to be sorted.
     */
    private void sortArray(SharesQuote[] existingCompanyRecords, SharesQuote[] array) {

        if (existingCompanyRecords == null || array == null)
            return;

        Set<String> stockIdSet =
                Arrays.stream(existingCompanyRecords)
                        .map(record -> record.get_id())
                        .collect(Collectors.toSet());


        // 1: Sort company array so that those unsearched company moved to head of array and those
        // companies already in databaes moved to tail.
        int nextSeenIdx = SharesQuote.moveUnsearchedDataAhead(stockIdSet, array);

        // 2: Sort companies which is in database by lastUpdateDatetime order, e.g: the latest
        // updated company move to tail.
        HashMap<String, SharesQuote> stockIdCompanyRecordMap = new HashMap<>();
        HashMap<String, SharesQuote> stociIdSharesQuoteMap = new HashMap<>();
        for (SharesQuote companyRecord : existingCompanyRecords) {
            stockIdCompanyRecordMap.put(companyRecord.get_id(), companyRecord);
        }
        for (int idx = nextSeenIdx + 1; idx < array.length; idx++) {
            stociIdSharesQuoteMap.put(array[idx].get_id(), array[idx]);
        }

        // These two map size should be equal otherwise the first sort method would be wrong
        Assert.assertEquals(stociIdSharesQuoteMap.size(), stockIdCompanyRecordMap.size());

        // Sort existingCompanyRecords
        Arrays.sort(existingCompanyRecords, Comparator.comparing(SharesQuote::getLastUpdatedTime));

        // Insertion sort the rest part of array based on the order of existingCompanyRecords
        for (int idx = nextSeenIdx + 1; idx < array.length; idx++) {
            array[idx] =
                    stociIdSharesQuoteMap.get(existingCompanyRecords[idx - nextSeenIdx - 1]
                            .get_id());
        }
    }
}
