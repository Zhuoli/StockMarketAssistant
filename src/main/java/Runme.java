import dataEngineer.sinaFinance.SinaWebParser;

/**
 * Created by zhuolil on 1/10/17.
 */
public class Runme {

    public static void main(String[] args){
        System.out.println("HHa alive");
        SinaWebParser sinaWebParser = new SinaWebParser();
        System.out.println("Symbol Price: " + sinaWebParser.QuoteSymbolePrice("sh601633"));
        System.exit(0);
    }
}
