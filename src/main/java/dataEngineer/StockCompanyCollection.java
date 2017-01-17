package dataEngineer;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import org.junit.Assert;

/**
 * Created by zhuolil on 1/16/17.
 */
public final class StockCompanyCollection {

    public class Company{
        String code;
        String shortName;
        String fullName;
        String aMargetCode;
        String officialWebUrl;

    }
    private static final String STOCK_LIST_PATH = "./src/main/resources/SZAstockList.csv";
    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private  static StockCompanyCollection thisInstance = null;
    private static Company[] ShenZhenStockCompanyCollection = null;

    private StockCompanyCollection(){
        // Does nothing
    }

    public static StockCompanyCollection getInstance(){
        if (thisInstance == null){
            thisInstance = new StockCompanyCollection();
        }
        return thisInstance;
    }

    public Company[] queryCompanyList(){
        if (ShenZhenStockCompanyCollection == null){
            ShenZhenStockCompanyCollection = this.initializeCompanyList(STOCK_LIST_PATH);
        }
        return ShenZhenStockCompanyCollection;
    }

    private Company[] initializeCompanyList(String csvFile){
        System.out.println("Current path: " + Paths.get(".").toAbsolutePath());
        List<Company> companyList = new LinkedList<>();
        try(Scanner scanner = new Scanner(new File(csvFile)))
        {
            if (scanner.hasNext()){
                List<String> headers = parseLine(scanner.nextLine());
                Assert.assertNotNull(headers);
                Assert.assertEquals("公司代码", headers.get(0).substring(1));
                Assert.assertEquals("公司简称", headers.get(1));
                Assert.assertEquals("公司全称", headers.get(2));
                Assert.assertEquals("A股代码", headers.get(5));
                Assert.assertEquals("公司网址", headers.get(19));
            }
            while (scanner.hasNext()) {
                List<String> line = parseLine(scanner.nextLine());
                Assert.assertEquals(line.stream().reduce("Line-> ", (a,b) -> a+"\nB: -> "+b), 20, line.size());
                Company company = new Company();
                company.code = line.get(0);
                company.shortName = line.get(1);
                company.fullName = line.get(2);
                company.aMargetCode = line.get(5);
                company.officialWebUrl = line.get(19);
                companyList.add(company);
            }

        }catch (Exception exc){
            System.err.println("Exception on idx: " + companyList.size() + "\n" + exc.getMessage());
        }
        return companyList.toArray(new Company[0]);
    }

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        //if empty, return!
        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuffer curVal = new StringBuffer();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {

            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {

                    //Fixed : allow "" in custom quote enclosed
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }

                }
            } else {
                if (ch == customQuote) {

                    inQuotes = true;

                    //Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    //double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    //ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    //the end, break!
                    break;
                } else {
                    curVal.append(ch);
                }
            }

        }

        result.add(curVal.toString());

        return result;
    }
}
