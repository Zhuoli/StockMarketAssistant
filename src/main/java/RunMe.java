import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;
import dataEngineer.sinaFinance.SinaWebParser;

import java.util.Arrays;

/**
 * Created by zhuolil on 1/10/17.
 */
public class RunMe
{
    public static void main(String[] args){
        System.out.println("HHa alive");
        StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
        StockCompanyCollection.Company[] companies = companyCollection.queryCompanyList();
        String hair = Arrays.stream(companies).filter(f -> f.shortName.equals("青岛海尔")).findFirst().get().aMargetCode;
        String pingan = Arrays.stream(companies).filter(f -> f.shortName.equals("平安银行")).findFirst().get().aMargetCode;
        SinaWebParser sinaWebParser = new SinaWebParser();
        // sh601633
        SharesQuote quote = sinaWebParser.queryCompanyStock(hair);
        SharesQuote pinganprice = sinaWebParser.queryCompanyStock(pingan);
        System.exit(0);
    }
}
