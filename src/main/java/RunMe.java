import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;
import dataEngineer.sinaFinance.SinaWebParser;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Created by zhuolil on 1/10/17.
 */
public class RunMe {
    final static int WEB_PARSER_SIZE = 10;

    public static void main(String[] args) {
        new RunMe().run(args);
    }

    public void run(String[] args)
    {
        System.out.println("HHa alive");
        boolean isDebug = args.length>=1;

        StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
        SharesQuote[] companies = companyCollection.queryCompanyList(isDebug);

        DatabaseManager databaseManager = null;

        System.out.println("Company size: " + companies.length);
        System.out.println("Registering company to database... time: " + LocalDateTime.now());
        this.initializeDatabaseData(databaseManager, companies);
        System.out.println("Register Done... Time: "+ LocalDateTime.now());

        System.out.println("Done.");
        System.out.println("Querying company stock from webpage....");

        ExecutorService executorService = Executors.newFixedThreadPool(WEB_PARSER_SIZE);

        LinkedBlockingQueue<SharesQuote> sharesQuoteList =
                new LinkedBlockingQueue<>(companies.length * 2);


        // Submit company query task
        for (SharesQuote companyObject : companies) {
            executorService.submit(() -> {
                try {
                    SinaWebParser sinaWebParser = new SinaWebParser();
                    SharesQuote quote = sinaWebParser.queryCompanyStock(companyObject.stockid);
                    quote.stockid = companyObject.stockid;
                    quote.companyname = companyObject.companyname;
                    quote.officialWebUrl = companyObject.officialWebUrl;
                    sharesQuoteList.offer(quote);
                }catch (IOException exc){
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
                System.out.println("Succeed on update company: " + sharesQuote.companyname + ";  StockID: " + sharesQuote.stockid);

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
    }

    private void initializeDatabaseData(DatabaseManager databaseManager ,SharesQuote[] companies){
        try {
            databaseManager =
                    DatabaseManager.GetDatabaseManagerInstance("resourceConfig.xml").Authenticate();
            databaseManager.insertOnDuplicateUpdate(companies);
        } catch (SQLException exc) {
            exc.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException exc) {
            exc.printStackTrace();
            System.exit(1);
        }
    }
}
