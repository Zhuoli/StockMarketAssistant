package dataEngineer.financeWebEngine;

import com.joanzapata.utils.Strings;
import dataEngineer.SharesQuote;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.junit.Assert;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuolil on 2/2/17.
 */
public class XueqiuWebParser implements IWebParser{

    final static String URL_BASE =
            "https://xueqiu.com/S";

    final  static  String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";


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

    public XueqiuWebParser() {
        // Does nothing
    }
    public SharesQuote queryCompanyStock(String symbol) throws IOException{
        Map<String, String> map = this.queryTableDetail(symbol);
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
        sharesQuote.price2EarningRatio = Double.parseDouble(map.get(PTE));
        sharesQuote.price2BookRatio = Double.parseDouble(map.get(PTB));

        return sharesQuote;
    }

    private Map<String, String> queryTableDetail(String stocId){
        String reportUrl = XueqiuWebParser.URL_BASE + "/" + stocId;
        try {

            // Parse html to get target element
            Document dom = Jsoup.connect(reportUrl).userAgent(XueqiuWebParser.USER_AGENT).get();

            String currentPrice = dom.getElementsByAttribute("data-current").first().text();
            if (currentPrice.startsWith("￥"))
                currentPrice = currentPrice.substring(1);

            Elements elements = dom.getElementsByClass("topTable");
            if (elements.size()!=1){
                throw new Exception("element size should equal to one.");
            }

            Element element = elements.first();
            String tableValue = element.text();

            String[] keyValuePairs = tableValue.split(" ");

            HashMap<String, String> map = new HashMap<>();
            map.put(PRICE, currentPrice);
            for(String keyValue : keyValuePairs){
                String[] keyValuePair = keyValue.split("：");
                Assert.assertTrue("Xueqiu Table Keyvalue length should be 2. but is '" + keyValue + "'", keyValuePair.length == 2);
                map.put(keyValuePair[0].trim(), keyValuePair[1].trim());
            }

            // Fix value format
            if (map.containsKey(PTE) && map.get(PTE).contains("/"))
                map.put(PTE, map.get(PTE).substring(0, map.get(PTE).indexOf("/")));

            return map;
        }
        catch (Exception exc)
        {

        }
        return null;
    }

}
