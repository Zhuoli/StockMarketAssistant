package dataEngineer.financeWebEngine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import dataEngineer.SharesQuote;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Represents a Sina Finance Web Parser.
 */
public class SinaWebParser implements IWebParser {

    final static String SinaFinanceBase =
            "http://finance.sina.com.cn/realstock/company/%s/nc.shtml";

    // TODO: Hardcode current PHANTOM_PATH, may want update later.
    final static String PHANTOM_PATH = new File(".").getAbsolutePath()
            + "/src/main/resources/phantomjs/bin/phantomjs";

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

    public SinaWebParser() {
        // Does nothing
    }

    public SharesQuote queryCompanyStock(String symbol) throws IOException {
        HashMap<String, String> map = this.quoteCompanyDetail(symbol);
        SharesQuote sharesQuote =
                SharesQuote
                        .builder()
                        .currentPrice(Double.parseDouble(map.get(PRICE)))
                        .closePrice(Double.parseDouble(map.get(CLOSE_PRICE)))
                        .highestPrice(Double.parseDouble(map.get(HIGHEST_PRICE)))
                        .lowestPrice(Double.parseDouble(map.get(LOWEST_PRICE)))
                        .openPrice(Double.parseDouble(map.get(OPEN_PRICE)))
                        .dealVolum(map.get(DEAL_VOLUM))
                        .dealValue(map.get(DEAL_VALUE))
                        .marketCap(map.get(MARKET_CAP))
                        .tradingCap(map.get(TRADING_CAP))
                        .oscillation(map.get(OSCILLATION))
                        .exchangeRatio(map.get(EXCHANGE_RATIO))
                        .price2EarningRatio(Double.parseDouble(map.get(PTE)))
                        .price2BookRatio(Double.parseDouble(map.get(PTB)))
                        .listingDate(new Date(Long.MIN_VALUE))
                        .build();
        return sharesQuote;
    }

    public HashMap<String, String> quoteCompanyDetail(String symbol) throws IOException {

        HashMap<String, String> tableMap = new HashMap<>();
        String url = String.format(SinaFinanceBase, symbol);
        System.out.println("Quoting url: " + url);
        LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");

        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

        final WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setCssEnabled(false);
        long starttime = System.currentTimeMillis();
        final HtmlPage page = webClient.getPage(url);
        long endtime = System.currentTimeMillis();
        System.out
                .println((endtime - starttime) / 1000.0 + " seconds slipped to get page content.");

        // Normalize current price
        String currentPrice = page.getElementById("price").getTextContent();
        currentPrice = currentPrice.matches("[0-9]+(\\.[0-9]*)?.*") ? currentPrice.trim() : "0";

        tableMap.put(PRICE, currentPrice);

        // Retrieve table
        DomElement detailTable = page.getElementById("hqDetails"); // Detail table
        String tableStr = detailTable.getTextContent();
        tableStr = tableStr.replaceAll("  ", "");
        String[] keyValuePairs = tableStr.split(" |\n");
        keyValuePairs =
                Arrays.stream(keyValuePairs)
                        .filter(str -> !str.equals(""))
                        .map(str -> str.replaceAll("[：|  |]", ""))
                        .toArray(String[]::new);

        // Data validation
        Assert.assertTrue("Key value length should be even", keyValuePairs.length % 2 == 0);
        for (int idx = 0; idx < keyValuePairs.length / 2; idx++) {

            // Value must contains decimal or integer otherwise replace it to -1
            String value =
                    keyValuePairs[idx * 2 + 1].matches("[0-9]+(\\.[0-9]*)?.*") ? keyValuePairs[idx * 2 + 1]
                            .trim() : "0";
            tableMap.put(keyValuePairs[idx * 2], value);
        }

        // Closes all opened windows, stopping all background JavaScript processing
        webClient.close();
        return tableMap;
    }
}
