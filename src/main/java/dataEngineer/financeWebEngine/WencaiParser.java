package dataEngineer.financeWebEngine;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by zhuoli on 2017/9/12.
 */
public class WencaiParser {
    static String StockSearch = "http://www.iwencai.com/stockpick/search?typed=1&preParams=&ts=1&f=1&qs=index_rewrite&selfsectsn=&querytype=&searchfilter=&tid=stockpick&w=";

    {
        java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
    }
    public static List<String> retrieveConcepts(String symbol) {
        WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME, true);
        try {
            if (symbol.toLowerCase().contains("sz") || symbol.toLowerCase().contains("sh"))
                symbol = symbol.substring(2);
            driver.get(WencaiParser.StockSearch + symbol);
            try {
                WebElement moreElement = driver.findElement(By.cssSelector("div[class='em alignCenter split'] a[class='ml5 moreSplit fr']"));
                moreElement.click();
            }catch (org.openqa.selenium.NoSuchElementException noexc){
                // Ignore
            }
            List<WebElement> conceptElements = driver.findElements(By.cssSelector("div[class='em alignCenter split'] span a"));
            return conceptElements.stream().map(element -> element.getText()).collect(Collectors.toList());
        }catch (Exception exc){
            exc.printStackTrace();
            return new LinkedList<>();
        }
    }
}
