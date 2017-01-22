import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;
import dataEngineer.sinaFinance.SinaWebParser;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created by zhuolil on 1/10/17.
 */
public class RunMe
{
    public static void main(String[] args){
        System.out.println("HHa alive");

        StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
        StockCompanyCollection.CompanyObject[] companies = companyCollection.queryCompanyList();
        DatabaseManager databaseManager = null;
        SinaWebParser sinaWebParser = new SinaWebParser();

        try {
            databaseManager = DatabaseManager.GetDatabaseManagerInstance("resourceConfig.xml").Authenticate();
            databaseManager.insertOnDuplicateUpdate(companies);
        }catch (SQLException exc){
            exc.printStackTrace();
            System.exit(1);
        }catch (ClassNotFoundException exc){
            exc.printStackTrace();
            System.exit(1);
        }

        for(StockCompanyCollection.CompanyObject companyObject : companies) {

            SharesQuote quote = sinaWebParser.queryCompanyStock(companyObject.aMargetCode);
            companyObject.PBR = quote.price2BookRatio;
            companyObject.PER = quote.price2EarningRatio;
            companyObject.currentprice = quote.currentPrice;
        }
        System.exit(0);
    }
}
