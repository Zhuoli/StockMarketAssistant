import JooqORM.tables.records.CompanyRecord;
import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import dataEngineer.financeWebEngine.XueqiuWebParser;
import org.apache.commons.cli.*;
import org.junit.Assert;

/**
 * Created by zhuolil on 1/10/17.
 */
public class RunMe {
    final static int WEB_PARSER_SIZE = 10;

    public static void main(String[] args) {

        // create Options object
        Options options = new Options();

        // add t option
        options.addOption("d", false, "Is running under IDE or not");
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);

            new RunMe().run(cmd.hasOption("d"));
        } catch (ParseException exc) {
            System.err.println("Arguments parse exception: " + exc.getMessage());
            System.exit(1);
        }
    }

    public void run(boolean isIde) {
        try {
            System.out.println("HHa alive");

            try {
                new XueqiuWebParser().queryCompanyStock("sz002162");
            }catch (Exception exc){

            }

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

            ExecutorService executorService = Executors.newFixedThreadPool(WEB_PARSER_SIZE);

            LinkedBlockingQueue<SharesQuote> sharesQuoteList =
                    new LinkedBlockingQueue<>(companies.length);

            System.out.println(LocalDateTime.now().toString() + " "
                    + (companies.length - existingCompanyRecords.length)
                    + " Companies remain to be added to database.");
            this.sortArray(existingCompanyRecords, companies);

            // Submit company query task
            for (SharesQuote companyObject : companies) {
                executorService.submit(() -> {

                    Runnable childTask = () -> {

                        try {
                            XueqiuWebParser webParser = new XueqiuWebParser();
                            SharesQuote quote = webParser.queryCompanyStock(companyObject.stockid);
                            quote.stockid = companyObject.stockid;
                            quote.companyname = companyObject.companyname;
                            quote.officialWebUrl = companyObject.officialWebUrl;
                            sharesQuoteList.offer(quote);
                        } catch (IOException exc) {
                            exc.printStackTrace();
                        }
                    };

                    try {
                        Thread thread = new Thread(childTask);
                        thread.start();
                        thread.join(3 * 60 * 1000);
                    }catch (InterruptedException exc){
                        exc.printStackTrace();
                    }
                });
            }

            while (!executorService.isTerminated()) {

                try {
                    // Poll wait for three minutes
                    SharesQuote sharesQuote = sharesQuoteList.poll(2, TimeUnit.MINUTES);
                    if (sharesQuote == null)
                        continue;
                    databaseManager.insertOnDuplicateUpdate(sharesQuote);
                    System.out.println(LocalDateTime.now().toString()
                            + ": Succeed on update company: " + sharesQuote.companyname
                            + ";  StockID: " + sharesQuote.stockid);

                } catch (InterruptedException exc) {
                    exc.printStackTrace();

                    // Break while loop if time out and shareQuoteList is zero
                    if (sharesQuoteList.size() == 0)
                        break;
                } catch (SQLException exc) {
                    exc.printStackTrace();
                    System.exit(1);
                } catch (ClassNotFoundException exc) {
                    exc.printStackTrace();
                    System.exit(1);
                }
            }

            System.out.println(LocalDateTime.now().toString() + "  Job done.");
            System.exit(0);
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
