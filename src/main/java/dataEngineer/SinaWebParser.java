package dataEngineer;


import com.joanzapata.utils.Strings;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhuolil on 1/10/17.
 */
public class SinaWebParser {

    final static String SinaFinanceBase = "http://finance.sina.com.cn/realstock/company/%s/nc.shtml";

    final  static  String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

    final static String PHANTOM_PATH = new File(".").getAbsolutePath() + "/src/main/resources/phantomjs/bin/phantomjs";

    public SinaWebParser(){
//        PhantomJsDriverManager.getInstance().setup();
    }

    public double QuoteSymbolePrice(String symbol) {
        DesiredCapabilities caps = new DesiredCapabilities();
        caps.setJavascriptEnabled(true);
        caps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, PHANTOM_PATH);
        PhantomJSDriver driver = new PhantomJSDriver(caps);
        String url = String.format(SinaFinanceBase, symbol);
        return Double.parseDouble(this.GetElementText(driver, url).replaceAll("[^\\d.]+", ""));
    }

    public String GetElementText(PhantomJSDriver driver, String url) {
        driver.get(url);
        By searchInput = By.id("searchInput");
        String price = driver.findElementById("price").getText();
        try {
            Document doc = Jsoup.connect(url).userAgent(SinaWebParser.USER_AGENT).get();
            Elements stockTableElement = doc.select("#hq");
            price = stockTableElement.select("#price").text();
            return stockTableElement.toString();
        }
        catch (HttpStatusException exc)
        {
            Logger.getGlobal().log(Level.WARNING, Strings.format("Http status exception \"{errorCode}\" from \"{url}\".").with("errorCode", exc.getStatusCode()).with("url", url).build(), exc);
            return "";
        }
        catch (Exception exc) {
            Logger.getGlobal().log(Level.WARNING, Strings.format("Failed to resolve the ElementText of element ID  from \"{url}\".").with("url", url).build(), exc);
            return "";
        }
    }
}
