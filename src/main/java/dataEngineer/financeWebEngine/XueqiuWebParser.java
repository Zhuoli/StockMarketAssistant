package dataEngineer.financeWebEngine;

import dataEngineer.SharesQuote;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Assert;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by zhuolil on 2/2/17.
 */
public class XueqiuWebParser implements IWebParser {

    final static String URL_BASE = "https://xueqiu.com/S";

    final static String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

    final static String PRICE = "price";

    final static String OPEN_PRICE = "今开";
    final static String HIGHEST_PRICE = "最高";
    final static String LOWEST_PRICE = "最低";
    final static String CLOSE_PRICE = "昨收";

    final static String DEAL_VOLUM = "成交量";
    final static String DEAL_VALUE = "成交额";
    final static String MARKET_CAP = "总市值";
    final static String TRADING_CAP = "流通股本";

    final static String OSCILLATION = "振幅";
    final static String PTE = "市盈率(静)/(动)";
    final static String PTB = "市净率(动)";

    final static String DOUBLE_REGEX_STRING = "^\\d+(\\.\\d+)*$";
    final static Pattern DOUBLE_PATTERN = Pattern.compile(DOUBLE_REGEX_STRING);

    public XueqiuWebParser() {
        // Does nothing
    }

    public SharesQuote queryCompanyStock(String symbol) throws IOException {
        Map<String, String> map = this.queryTableDetail(symbol);
        SharesQuote sharesQuote =
                SharesQuote
                        .builder()
                        .currentPrice(
                                DOUBLE_PATTERN.matcher(map.get(PRICE)).find() ? Double.valueOf(map
                                        .get(PRICE)) : 0)
                        .closePrice(
                                DOUBLE_PATTERN.matcher(map.get(CLOSE_PRICE)).find() ? Double
                                        .parseDouble(map.get(CLOSE_PRICE)) : 0)
                        .highestPrice(DOUBLE_PATTERN.matcher(map.get(HIGHEST_PRICE)).find() ? Double.parseDouble(map
                                .get(HIGHEST_PRICE)) : 0)
                        .lowestPrice(DOUBLE_PATTERN.matcher(map.get(LOWEST_PRICE)).find() ? Double.parseDouble(map
                                .get(LOWEST_PRICE)) : 0)
                        .openPrice(DOUBLE_PATTERN.matcher(map.get(OPEN_PRICE)).find() ? Double.parseDouble(map
                                .get(OPEN_PRICE)) : 0)
                        .dealVolum(map.get(DEAL_VOLUM))
                        .dealValue(map.get(DEAL_VALUE))
                        .marketCap(map.get(MARKET_CAP))
                        .tradingCap(map.get(TRADING_CAP))
                        .oscillation(map.get(OSCILLATION))
                        .price2BookRatio(DOUBLE_PATTERN.matcher(map.get(PTB)).find() ? Double.parseDouble(map.get(PTB)) : 0)
                        .price2EarningRatio(DOUBLE_PATTERN.matcher(map.get(PTE)).find() ? Double.parseDouble(map.get(PTE)) : 0)
                        .listingDate(new Date(System.currentTimeMillis()))
                        .build();
        return sharesQuote;
    }

    private Map<String, String> queryTableDetail(String stocId) {
        String reportUrl = XueqiuWebParser.URL_BASE + "/" + stocId;
        try {

            // Parse html to get target element
            Document dom = Jsoup.connect(reportUrl).userAgent(XueqiuWebParser.USER_AGENT).get();

            Elements elements = dom.getElementsByAttribute("data-current");
            String currentPrice = "0";
            if (elements.size() == 1)
                currentPrice = elements.first().text();

            if (currentPrice.startsWith("￥"))
                currentPrice = currentPrice.substring(1);

            elements = dom.getElementsByClass("topTable");
            if (elements.size() != 1) {
                throw new Exception("element size should equal to one.");
            }

            Element element = elements.first();
            String tableValue = element.text();

            String[] keyValuePairs = tableValue.split(" ");

            HashMap<String, String> map = new HashMap<>();
            map.put(PRICE, currentPrice);
            for (String keyValue : keyValuePairs) {
                String[] keyValuePair = keyValue.split("：");
                Assert.assertTrue("Xueqiu Table Keyvalue length should be 2. but is '" + keyValue
                        + "'", keyValuePair.length == 2);
                map.put(keyValuePair[0].trim(), keyValuePair[1].trim());
            }

            // Fix value format
            if (map.containsKey(PTE) && map.get(PTE).contains("/"))
                map.put(PTE, map.get(PTE).substring(0, map.get(PTE).indexOf("/")));

            return map;
        } catch (Exception exc) {
            System.err.println("Error while processing url: " + reportUrl);
            exc.printStackTrace();
        }
        return null;
    }

}
