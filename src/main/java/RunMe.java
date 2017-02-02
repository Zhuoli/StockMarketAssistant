import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import dataEngineer.financeWebEngine.XueqiuWebParser;
import org.apache.commons.cli.*;

/**
 * Created by zhuolil on 1/10/17.
 */
public class RunMe {
    final static int WEB_PARSER_SIZE = 5;

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

            StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
            SharesQuote[] companies = companyCollection.queryCompanyList(isIde);

            DatabaseManager databaseManager = this.initializeDataManager();
            if (databaseManager == null) {
                System.err.println("Failed to initialize database manager. \t Quit.");
                System.exit(1);
            }

            Set<String> existingStockIDsSet = databaseManager.getExistingStockIDs();

            System.out.println("Company size: " + companies.length);
            System.out.println("Querying company stock from webpage....");

            ExecutorService executorService = Executors.newFixedThreadPool(WEB_PARSER_SIZE);

            LinkedBlockingQueue<SharesQuote> sharesQuoteList =
                    new LinkedBlockingQueue<>(companies.length);

            this.sortArray(existingStockIDsSet, companies);

            // Submit company query task
            for (SharesQuote companyObject : companies) {
                executorService.submit(() -> {
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
                });
            }

            while (!executorService.isTerminated()) {

                try {
                    // Poll wait for three minutes
                    SharesQuote sharesQuote = sharesQuoteList.poll(5, TimeUnit.MINUTES);
                    if (sharesQuote == null)
                        continue;
                    databaseManager.insertOnDuplicateUpdate(sharesQuote);
                    System.out.println("Succeed on update company: " + sharesQuote.companyname
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

            System.out.println("Job done. Time: " + LocalDateTime.now());
            System.exit(0);
        } finally {
            DatabaseManager.close();
        }
    }

    private void sortArray(Set<String> set, SharesQuote[] array) {

        if (set == null || array == null)
            return;

        int nextSeenIdx = array.length - 1;
        for (int idx = 0; idx <= nextSeenIdx; idx++) {
            // If current stock is seen in records
            if (set.contains(array[idx].stockid)) {
                // Move this stock to tail
                SharesQuote tmp = array[nextSeenIdx];
                array[nextSeenIdx] = array[idx];
                array[idx] = tmp;
                nextSeenIdx--;
                idx--;
            }
        }
    }

    private DatabaseManager initializeDataManager() {
        try {
            return DatabaseManager.GetDatabaseManagerInstance("resourceConfig.xml").Authenticate();
        } catch (SQLException exc) {
            exc.printStackTrace();
            return null;
        }
    }
}
