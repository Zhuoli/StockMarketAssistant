import JooqORM.tables.records.CompanyRecord;
import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.apache.commons.cli.*;
import org.junit.Assert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Entry point.
 */
public class RunMe {
    final static int WEB_PARSER_SIZE = 10;
    final static long TASK_MAXIMUM_TIMEOUT = 3 * 60 * 1000;

    private CommandLine cmd;
    private final static String DEBUG_OPTION = "d";

    public static void main(String[] args) {
        RunMe runMe = new RunMe(args);

        if (runMe.cmd == null) {
            System.err.println("Failed on CommandLine initialization, system exit.");
            System.exit(1);
        }
        while(true) {
            DateTime now = new DateTime(System.currentTimeMillis(), DateTimeZone.forID("Asia/Shanghai"));
            try {
                if (now.getDayOfWeek() > 5) {
                    System.out.println(now.toString()+ ": Sleep 2 hours on weekend.");
                    Thread.sleep(2 * 60 * 60 * 1000);
                    continue;
                }
                if(now.getHourOfDay()<8 || now.getHourOfDay()>17){
                    System.out.println(now.toString() + ": Sleep 5 minutes off hour.");
                    Thread.sleep(5 * 60 * 1000);
                    continue;
                }
            } catch (InterruptedException exc) {
                System.out.println("Interrupted exception received, gonna launch querryAndUpdate...");
            }
            runMe.querryAndUpdate(runMe.cmd.hasOption(RunMe.DEBUG_OPTION));
            System.out.println(LocalDateTime.now().toString() + " One loop Job done.");
        }
    }

    /**
     * Constructor.
     * @param args
     */
    private RunMe(String[] args) {
        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(RunMe.DEBUG_OPTION, false, "Is running under IDE or not");
        CommandLineParser parser = new DefaultParser();

        try {
            this.cmd = parser.parse(options, args);
        } catch (ParseException exc) {
            System.err.println("Arguments parse exception: " + exc.getMessage());
        }
    }

    /**
     * Assigns the task and collects the result.
     * @param isIde : Is in Intellij model or terminal model.
     */
    public void querryAndUpdate(boolean isIde) {
        try {
            System.out.println("HHa alive");

            StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
            SharesQuote[] companies = companyCollection.queryCompanyList(isIde);

            DatabaseManager databaseManager = this.initializeDataManager();
            if (databaseManager == null) {
                System.err.println("Failed to initialize database manager. \t Quit.");
                System.exit(1);
            }

            CompanyRecord[] existingCompanyRecords = databaseManager.getExistingStocks();

            System.out.println("Company size: " + companies.length);
            System.out.println("Querying company stock from webpage....");

            ExecutorService executorService = Executors.newFixedThreadPool(RunMe.WEB_PARSER_SIZE);

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
                                        + RunMe.TASK_MAXIMUM_TIMEOUT < System.currentTimeMillis()) && !future.result.isPresent()))
                                .collect(Collectors.toList());

                System.out.println("Remain future list size: " + futureList.size());

                for( RunmeFuture runmeFuture : futureList) {
                    if (!runmeFuture.result.isPresent())
                        continue;
                    SharesQuote sharesQuote = runmeFuture.result.get();
                    runmeFuture.isResultConsumed = true;
                    try {
                        databaseManager.insertOnDuplicateUpdate(sharesQuote);
                        System.out.println(LocalDateTime.now().toString()
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
                }catch (Exception exc){

                }
            }
        } finally {
            DatabaseManager.close();
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
}
