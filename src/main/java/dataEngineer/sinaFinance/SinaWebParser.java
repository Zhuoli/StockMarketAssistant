package dataEngineer.sinaFinance;

import dataEngineer.SharesQuote;
import org.junit.Assert;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.HashMap;

/**
 * Represents a Sina Finance Web Parser.
 */
public class SinaWebParser {

    final static String SinaFinanceBase = "http://finance.sina.com.cn/realstock/company/%s/nc.shtml";

    // TODO: Hardcode current PHANTOM_PATH, may want update later.
    final static String PHANTOM_PATH = new File(".").getAbsolutePath() + "/src/main/resources/phantomjs/bin/phantomjs";

    final static String PRICE = "price";

    final static String OPEN_PRICE = "今开";
    final static String HIGHEST_PRICE = "最高";
    final static String LOWEST_PRICE = "最低";
    final static String CLOSE_PRICE = "昨收";

    final static String DEAL_VOLUM = "成交量";
    final static String DEAL_VALUE = "成交额";
    final static String MARKET_CAP = "总市值";
    final static String TRADING_CAP = "流通市值";

    final static String OSCILLATION = "振幅";
    final static String EXCHANGE_RATIO = "换手率";
    final static String PTE = "市盈率TTM";
    final static String PTB = "市净率";

    // Initialize Web driver
    static DesiredCapabilities caps = new DesiredCapabilities();
    static {
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOM_PATH);
    }

    // Set web drover to PhantomJSDriver
    final static PhantomJSDriver WebDriver = new PhantomJSDriver(caps);

    public SinaWebParser(){
        // Does nothing
    }

    public SharesQuote queryCompanyStock(String symbol){
        HashMap<String, String> map = this.quoteCompanyDetail(symbol);
        SharesQuote sharesQuote = new SharesQuote();
        sharesQuote.currentPrice = Double.parseDouble(map.get(PRICE));
        sharesQuote.closePrice = Double.parseDouble(map.get(CLOSE_PRICE));
        sharesQuote.highestPrice = Double.parseDouble(map.get(HIGHEST_PRICE));
        sharesQuote.lowestPrice = Double.parseDouble(map.get(LOWEST_PRICE));
        sharesQuote.openPrice = Double.parseDouble(map.get(OPEN_PRICE));

        sharesQuote.dealVolum = map.get(DEAL_VOLUM);
        sharesQuote.dealValue = map.get(DEAL_VALUE);
        sharesQuote.marketCap = map.get(MARKET_CAP);
        sharesQuote.tradingCap = map.get(TRADING_CAP);

        sharesQuote.oscillation = map.get(OSCILLATION);
        sharesQuote.exchangeRatio = map.get(EXCHANGE_RATIO);
        sharesQuote.price2EarningRatio = Double.parseDouble(map.get(PTE));
        sharesQuote.price2BookRatio = Double.parseDouble(map.get(PTB));

        return sharesQuote;
    }

    public HashMap<String, String> quoteCompanyDetail(String symbol) {
        HashMap<String, String> tableMap = new HashMap<>();
        String url = String.format(SinaFinanceBase, symbol);
        System.out.println("Quoting url: " + url);
        WebDriver.get(url);
        String currentPrice = WebDriver.findElementById("price").getText();
        tableMap.put(PRICE, currentPrice);

        // Retrieve table
        WebElement detailTable = WebDriver.findElementById("hqDetails"); // Detail table
        String tableStr = detailTable.getText();
        tableStr = tableStr.replace("  ", "");
        String[] keyValuePairs = tableStr.split(" |\n");

        // Data validation
        Assert.assertTrue("Key value length should be even", keyValuePairs.length % 2 ==0);
        for(int idx =0; idx<keyValuePairs.length/2; idx++){

            // Value must contains decimal or integer otherwise replace it to -1
            String value = keyValuePairs[idx*2+1].matches("[0-9]+(\\.[0-9]*)?.*") ? keyValuePairs[idx*2+1].trim() : "-1";
            tableMap.put(keyValuePairs[idx*2].replace("：",""), value);
        }
        return tableMap;
    }
}
