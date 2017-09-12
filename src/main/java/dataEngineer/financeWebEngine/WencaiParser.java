package dataEngineer.financeWebEngine;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by zhuoli on 2017/9/12.
 */
public class WencaiParser {
    static String StockSearch = "http://www.iwencai.com/stockpick/search?typed=1&preParams=&ts=1&f=1&qs=index_rewrite&selfsectsn=&querytype=&searchfilter=&tid=stockpick&w=";


    public static List<String> retrieveConcepts(String symbol) {
        try {
            if (symbol.toLowerCase().contains("sz") || symbol.toLowerCase().contains("sh"))
                symbol = symbol.substring(2);
            WebDriver driver = new HtmlUnitDriver(BrowserVersion.CHROME, true);
            driver.get(WencaiParser.StockSearch + symbol);
            WebElement moreElement = driver.findElement(By.cssSelector("div[class='em alignCenter split'] a[class='ml5 moreSplit fr']"));
            moreElement.click();
            List<WebElement> conceptElements = driver.findElements(By.cssSelector("div[class='em alignCenter split'] span a"));
            return conceptElements.stream().map(element -> element.getText()).collect(Collectors.toList());
        }catch (Exception exc){
            exc.printStackTrace();
            return new LinkedList<>();
        }
    }
}
