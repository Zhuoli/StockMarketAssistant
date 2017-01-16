import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;
import dataEngineer.sinaFinance.SinaWebParser;

import java.util.HashMap;

/**
 * Created by zhuolil on 1/10/17.
 */
public class RunMe
{
    public static void main(String[] args){
        System.out.println("HHa alive");
        StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
        StockCompanyCollection.Company[] companies = companyCollection.queryCompanyList();
        SinaWebParser sinaWebParser = new SinaWebParser();
        SharesQuote quote = sinaWebParser.queryCompanyStock("sh601633");
        System.exit(0);
    }
}
