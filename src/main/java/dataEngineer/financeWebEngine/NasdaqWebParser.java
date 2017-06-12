package dataEngineer.financeWebEngine;

import com.joanzapata.utils.Strings;
import dataEngineer.data.FinancialData;
import dataEngineer.data.SharesQuote;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by zhuolil on 2/25/17.
 */
public class NasdaqWebParser implements IWebParser {

    final static String URL_BASE = "http://www.nasdaq.com/symbol";

    final static String USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

    final static String PRICE_ID = "qwidget_lastsale";
    final static String QUOTE_TABLE_ID = "quotes_content_left_InfoQuotesResults";

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

    private Document dom;

    public NasdaqWebParser() {
        // Does nothing
    }

    public SharesQuote queryCompanyStock(String symbol) throws IOException {
        SharesQuote sharesQuote =
                SharesQuote
                        .builder()
                        .currentPrice(this.quoteDoubleElement(symbol, NasdaqWebParser.PRICE_ID))
                        .listingDate(new Date(System.currentTimeMillis()))
                        .build();
        List<Element> tableRows = this.getTableRows(NasdaqWebParser.URL_BASE + "/" + symbol);
        if (tableRows.size() < 10)
            return sharesQuote;
        sharesQuote.setOneYearTargetPrice(tableRows.get(1).child(1).text());
        String[] highestLowest = tableRows.get(2).child(1).text().split("/");
        String highestPrice = highestLowest[0].replace('$', ' ').trim();
        sharesQuote.setHighestPrice(DOUBLE_PATTERN.matcher(highestPrice).find() ? Double
                .parseDouble(highestPrice) : 0);
        String lowestPrice = highestLowest[1].replace('$', ' ').trim();
        sharesQuote.setLowestPrice(DOUBLE_PATTERN.matcher(lowestPrice).find() ? Double
                .parseDouble(lowestPrice) : 0);
        String p2e = tableRows.get(8).child(1).text();
        sharesQuote.setPrice2EarningRatio(DOUBLE_PATTERN.matcher(p2e).find() ? Double
                .parseDouble(p2e) : 0);
        // sharesQuote.closePrice =
        // sharesQuote.highestPrice =
        // sharesQuote.lowestPrice =
        // sharesQuote.openPrice =
        //
        // sharesQuote.dealVolum =
        // sharesQuote.dealValue =
        // sharesQuote.marketCap =
        // sharesQuote.tradingCap =
        //
        // sharesQuote.oscillation =
        // sharesQuote.price2EarningRatio =
        // sharesQuote.price2BookRatio =

        return sharesQuote;
    }

    @Override
    public FinancialData queryFinancialData(String symbol) throws IOException {
        throw new IOException("Method not implemented yet.");
    }

    public double quoteDoubleElement(String symbol, String elementID) {
        return Double.parseDouble(this.getElementText(NasdaqWebParser.URL_BASE + "/" + symbol,
                NasdaqWebParser.PRICE_ID).replaceAll("[^\\d.]+", ""));
    }

    public String quoteStringElement(String symbol, String elementID) {
        return this.getElementText(NasdaqWebParser.URL_BASE + "/" + symbol,
                NasdaqWebParser.PRICE_ID).replaceAll("[^\\d.]+", "");
    }

    private List<Element> getTableRows(String url) {
        Optional<Element> element = this.getElementById(url, NasdaqWebParser.QUOTE_TABLE_ID);
        if (!element.isPresent())
            return new LinkedList<>();
        return element.get().getElementsByTag("table").get(1).getElementsByTag("tr");
    }

    private Optional<Element> getElementById(String url, String elementID) {
        try {
            if (this.dom == null) {
                this.dom = Jsoup.connect(url).userAgent(NasdaqWebParser.USER_AGENT).get();
            }
            return Optional.ofNullable(this.dom.getElementById(elementID));
        } catch (HttpStatusException exc) {
            Logger.getGlobal().log(
                    Level.WARNING,
                    Strings.format("Http status exception \"{errorCode}\" from \"{url}\".")
                            .with("errorCode", exc.getStatusCode())
                            .with("url", url)
                            .build(), exc);
            return Optional.empty();
        } catch (Exception exc) {
            Logger.getGlobal()
                    .log(Level.WARNING,
                            Strings.format(
                                    "Failed to resolve the ElementText of element ID : {id} from \"{url}\".")
                                    .with("id", elementID)
                                    .with("url", url)
                                    .build(), exc);
            return Optional.empty();
        }
    }

    public String getElementText(String url, String elementID) {
        try {
            if (this.dom == null) {
                this.dom = Jsoup.connect(url).userAgent(NasdaqWebParser.USER_AGENT).get();
            }
            Element element = dom.getElementById(elementID);
            if (element.hasText()) {
                return element.text();
            }
            return element.html();
        } catch (HttpStatusException exc) {
            Logger.getGlobal().log(
                    Level.WARNING,
                    Strings.format("Http status exception \"{errorCode}\" from \"{url}\".")
                            .with("errorCode", exc.getStatusCode())
                            .with("url", url)
                            .build(), exc);
            return "";
        } catch (Exception exc) {
            Logger.getGlobal()
                    .log(Level.WARNING,
                            Strings.format(
                                    "Failed to resolve the ElementText of element ID : {id} from \"{url}\".")
                                    .with("id", elementID)
                                    .with("url", url)
                                    .build(), exc);
            return "";
        }
    }
}
