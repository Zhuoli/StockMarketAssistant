import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;
import dataEngineer.sinaFinance.SinaWebParser;

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


//
//
//        String hair = Arrays.stream(companies).filter(f -> f.shortName.equals("青岛海尔")).findFirst().get().aMargetCode;
//        String pingan = Arrays.stream(companies).filter(f -> f.shortName.equals("平安银行")).findFirst().get().aMargetCode;
//        SinaWebParser sinaWebParser = new SinaWebParser();
//
//
//        // sh601633
//        SharesQuote quote = sinaWebParser.queryCompanyStock(hair);
//        SharesQuote pinganprice = sinaWebParser.queryCompanyStock(pingan);
//

        System.exit(0);
    }
}
