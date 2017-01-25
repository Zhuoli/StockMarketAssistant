package dataEngineer;

/**
 * Represents an CompanyObject's Shares Quote.
 */
public class SharesQuote {

    public String stockid;
    public String companyname;

    public double currentPrice;

    public double openPrice;
    public double highestPrice;
    public double lowestPrice;
    public double closePrice;

    // 成交量
    public String dealVolum;

    // 成交额
    public String dealValue;
    // 总市值
    public String marketCap;
    // 流通市值
    public String tradingCap;

    // 振幅
    public String oscillation;
    // 换手率
    public String exchangeRatio;
    // 市盈率
    public double price2EarningRatio;
    // 市净率
    public double price2BookRatio;


    public String officialWebUrl;
}
