package dataEngineer.sinaFinance;

import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

/**
 * Represents a Sina Finance Web Parser.
 */
public class SinaWebParser {

    final static String SinaFinanceBase = "http://finance.sina.com.cn/realstock/company/%s/nc.shtml";

    // TODO: Hardcode current PHANTOM_PATH, may want update later.
    final static String PHANTOM_PATH = new File(".").getAbsolutePath() + "/src/main/resources/phantomjs/bin/phantomjs";

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

    public double QuoteSymbolePrice(String symbol) {
        String url = String.format(SinaFinanceBase, symbol);
        WebDriver.get(url);
        return Double.parseDouble(WebDriver.findElementById("price").getText().replaceAll("[^\\d.]+", ""));
    }
}
