package dataEngineer.data;

import org.junit.Assert;
import sun.security.provider.SHA;
import util.MarketConstant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by zhuolil on 1/16/17.
 */
public final class CompanyInfoFileAccessor {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';
    private static CompanyInfoFileAccessor thisInstance = null;
    private static Collection<SharesQuote> companyObjectCollection = null;

    private String SZFilePath, SHFilePath, USFilePath;

    private CompanyInfoFileAccessor() {
        // Does nothing
    }

    public static CompanyInfoFileAccessor getInstance(boolean isDebug) {
        if (thisInstance == null) {
            thisInstance = new CompanyInfoFileAccessor();
        }
        String msg = isDebug ? "Application is run in IDE" : "Application is run in jar";
        System.out.println(msg);
        thisInstance.SZFilePath = isDebug ? "./src/main/resources/"
                + MarketConstant.SZ_STOCK_LIST_PATH : "./"
                + MarketConstant.SZ_STOCK_LIST_PATH;
        thisInstance.SHFilePath = isDebug ? "./src/main/resources/"
                + MarketConstant.SH_STOCK_LIST_PATH : "./"
                + MarketConstant.SH_STOCK_LIST_PATH;
        thisInstance.USFilePath = isDebug ? "./src/main/resources/" + MarketConstant.NASDAQ_STOCK_LIST_PATH : "./"
                + MarketConstant.NASDAQ_STOCK_LIST_PATH;
        return thisInstance;
    }

    public SharesQuote[] queryCompanyListChinese() {

        if (companyObjectCollection == null) {
            companyObjectCollection = new LinkedList<>();
            companyObjectCollection.addAll(this
                    .readStockCompanyList(this.SZFilePath, "sz"));
            companyObjectCollection.addAll(this
                    .readStockCompanyList(this.SHFilePath, "sh"));
        }
        return companyObjectCollection.toArray(new SharesQuote[0]);
    }

    public SharesQuote[] queryCompanyListUS(boolean isDebug) {
        String msg = isDebug ? "Application is run in IDE" : "Application is run in jar";
        System.out.println(msg);
        return this.readNasdaqCompanyList(thisInstance.USFilePath).toArray(new SharesQuote[0]);
    }

    /**
     * Reads Nasdaq company list.
     *
     * @param csvFile
     * @return list of shares quote.
     */
    private List<SharesQuote> readNasdaqCompanyList(String csvFile) {
        System.out.println("Current path: " + Paths.get(".").toAbsolutePath());
        Assert.assertTrue("File not exist: " + csvFile, Files.exists(Paths.get(csvFile)));
        List<SharesQuote> companyObjectList = new LinkedList<>();
        try (Scanner scanner = new Scanner(new File(csvFile))) {
            if (scanner.hasNext()) {
                List<String> headers = parseLine(scanner.nextLine());
                Assert.assertNotNull(headers);
                Assert.assertTrue(headers.size() > 9);
                Assert.assertEquals("Symbol", headers.get(0));
                Assert.assertEquals("Name", headers.get(1));
                Assert.assertEquals("IPOyear", headers.get(5));
            }

            // Read row
            while (scanner.hasNext()) {
                List<String> line = parseLine(scanner.nextLine());
                Assert.assertEquals(line.stream().reduce("Line-> ", (a, b) -> a + "\nB: -> " + b),
                        10, line.size());
                SharesQuote companyObject =
                        SharesQuote
                                .builder()
                                ._id(line.get(0).trim())
                                .companyname(line.get(1).trim())
                                .build();
                companyObjectList.add(companyObject);
            }

        } catch (IOException exc) {
            System.err.println("Exception on idx: " + companyObjectList.size() + "\n"
                    + exc.getMessage());
            System.exit(1);
        }
        return companyObjectList;
    }

    /**
     * Reads ShenZhen Stock A Market.
     *
     * @param csvFile
     * @return List of company.
     */
    private List<SharesQuote> readStockCompanyList(String csvFile, String idPrefix) {
        System.out.println("Current path: " + Paths.get(".").toAbsolutePath());
        Assert.assertTrue("File not exist: " + csvFile, Files.exists(Paths.get(csvFile)));
        List<SharesQuote> companyObjectList = new LinkedList<>();
        try (Scanner scanner = new Scanner(new File(csvFile))) {
            if (scanner.hasNext()) {
                List<String> headers = parseLine(scanner.nextLine());
                Assert.assertNotNull(headers);
                Assert.assertEquals("A股代码", headers.get(0));
                Assert.assertEquals("A股简称", headers.get(1));
                Assert.assertEquals("A股上市日期", headers.get(2));
            }
            while (scanner.hasNext()) {
                List<String> line = parseLine(scanner.nextLine());
                Assert.assertEquals(line.stream().reduce("Line-> ", (a, b) -> a + "\nB: -> " + b),
                        3, line.size());
                SharesQuote companyObject =
                        SharesQuote
                                .builder()
                                ._id(
                                        idPrefix + line.get(0))
                                .companyname(line.get(1))
                                .dateFirstIPO(line.get(2))
                                .lastUpdatedTime(new Date(Long.MIN_VALUE))
                                .build();
                companyObjectList.add(companyObject);
            }

        } catch (Exception exc) {
            System.err.println("Exception on idx: " + companyObjectList.size() + "\n"
                    + exc.getMessage());
            System.exit(1);
        }
        return companyObjectList;
    }

    public void writeToStockCSV(SharesQuote... sharesQuotes){
        for(SharesQuote sharesQuote : sharesQuotes){

        }
    }

    public static List<String> parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static List<String> parseLine(String cvsLine, char separators, char customQuote) {

        List<String> result = new ArrayList<>();

        // if empty, return!
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

                    // Fixed : allow "" in custom quote enclosed
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

                    // Fixed : allow "" in empty quote enclosed
                    if (chars[0] != '"' && customQuote == '\"') {
                        curVal.append('"');
                    }

                    // double quotes in column will hit this!
                    if (startCollectChar) {
                        curVal.append('"');
                    }

                } else if (ch == separators) {

                    result.add(curVal.toString());

                    curVal = new StringBuffer();
                    startCollectChar = false;

                } else if (ch == '\r') {
                    // ignore LF characters
                    continue;
                } else if (ch == '\n') {
                    // the end, break!
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
