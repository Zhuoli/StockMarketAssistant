package MarketChinese;

import JooqORM.tables.records.CompanyRecord;
import dataEngineer.DatabaseManager;
import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;
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
 * Created by zhuolil on 2/25/17.
 */
public class ChineseMarketMaster {

    public void run(CommandLine cmd) {
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
            this.querryAndUpdate(cmd.hasOption(MarketConstant.IS_UNDER_INTELLIJ));
            System.out.println(LocalDateTime.now().toString() + " One loop Job done.");
        }
    }


    /**
     * Assigns the task and collects the result.
     *
     * @param isIde : Is in Intellij model or terminal model.
     */
    public void querryAndUpdate(boolean isIde) {
        try {
            System.out.println("HHa alive");

            StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
            SharesQuote[] companies = companyCollection.queryCompanyList(isIde);

            DatabaseManager databaseManager = this.initializeDataManager();
            if (databaseManager == null) {
                System.err.println("Failed to initialize database manager, please check database credential. \t Quit.");
                System.exit(1);
            }

            CompanyRecord[] existingCompanyRecords = databaseManager.getExistingStocks();

            System.out.println("Company size: " + companies.length);
            System.out.println("Querying company stock from webpage....");

            ExecutorService executorService = Executors.newFixedThreadPool(MarketConstant.WEB_PARSER_SIZE);

            System.out.println(LocalDateTime.now().toString() + " "
                    + (companies.length - existingCompanyRecords.length)
                    + " Companies remain to be added to database.");
            this.sortArray(existingCompanyRecords, companies);

            List<RunmeFuture> futureList = new LinkedList<>();

            // Submit company query task
            for (SharesQuote companyObject : companies) {
                RunmeFuture runmeFuture = new RunmeFuture(executorService::submit, companyObject);
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
                        databaseManager.insertOnDuplicateUpdate(sharesQuote);
                        System.out.println(now.toString()
                                + ": Succeed on update company: " + sharesQuote.companyname
                                + ";  StockID: " + sharesQuote.stockid);
                    } catch (SQLException exc) {
                        exc.printStackTrace();
                        System.exit(1);
                    } catch (ClassNotFoundException exc) {
                        exc.printStackTrace();
                        System.exit(1);
                    }
                }

                try {
                    Thread.sleep(1000);
                } catch (Exception exc) {

                }
            }
        } finally {
            DatabaseManager.close();
        }
    }


    /**
     * Reads credential and authenticate SQL connection.
     *
     * @return
     */
    private DatabaseManager initializeDataManager() {
        try {
            return DatabaseManager.GetDatabaseManagerInstance("resourceConfig.xml").Authenticate();
        } catch (SQLException exc) {
            exc.printStackTrace();
            return null;
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
    private void sortArray(CompanyRecord[] existingCompanyRecords, SharesQuote[] array) {

        if (existingCompanyRecords == null || array == null)
            return;

        Set<String> stockIdSet =
                Arrays.stream(existingCompanyRecords)
                        .map(record -> record.getStockid())
                        .collect(Collectors.toSet());

        int nextSeenIdx = array.length - 1;

        // 1: Sort company array so that those unsearched company moved to head of array and those
        // companies already in databaes moved to tail.
        for (int idx = 0; idx <= nextSeenIdx; idx++) {
            // If current stock is seen in records
            if (stockIdSet.contains(array[idx].stockid)) {
                // Move this stock to tail
                SharesQuote tmp = array[nextSeenIdx];
                array[nextSeenIdx] = array[idx];
                array[idx] = tmp;
                nextSeenIdx--;
                idx--;
            }
        }

        // 2: Sort companies which is in database by lastUpdateDatetime order, e.g: the latest
        // updated company move to tail.
        HashMap<String, CompanyRecord> stockIdCompanyRecordMap = new HashMap<>();
        HashMap<String, SharesQuote> stociIdSharesQuoteMap = new HashMap<>();
        for (CompanyRecord companyRecord : existingCompanyRecords) {
            stockIdCompanyRecordMap.put(companyRecord.getStockid(), companyRecord);
        }
        for (int idx = nextSeenIdx + 1; idx < array.length; idx++) {
            stociIdSharesQuoteMap.put(array[idx].stockid, array[idx]);
        }

        // These two map size should be equal otherwise the first sort method would be wrong
        Assert.assertEquals(stociIdSharesQuoteMap.size(), stockIdCompanyRecordMap.size());

        // Sort existingCompanyRecords
        Arrays.sort(existingCompanyRecords, (CompanyRecord a, CompanyRecord b) -> a
                .getLastUpdateDateTime()
                .toLocalDateTime()
                .compareTo(b.getLastUpdateDateTime().toLocalDateTime()));

        // Insertion sort the rest part of array based on the order of existingCompanyRecords
        for (int idx = nextSeenIdx + 1; idx < array.length; idx++) {
            array[idx] =
                    stociIdSharesQuoteMap.get(existingCompanyRecords[idx - nextSeenIdx - 1]
                            .getStockid());
        }
    }
}
