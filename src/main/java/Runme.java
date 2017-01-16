import dataEngineer.SharesQuote;
import dataEngineer.sinaFinance.SinaWebParser;

import java.util.HashMap;

/**
 * Created by zhuolil on 1/10/17.
 */
public class Runme {

    public static void main(String[] args){
        System.out.println("HHa alive");
        SinaWebParser sinaWebParser = new SinaWebParser();
        SharesQuote quote = sinaWebParser.queryCompanyStock("sh601633");
        System.exit(0);
    }
}
