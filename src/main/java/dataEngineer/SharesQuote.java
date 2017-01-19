package dataEngineer;

/**
 * Represents an CompanyObject's Shares Quote.
 */
public class SharesQuote {
    public double currentPrice;

    public double openPrice;
    public double highestPrice;
    public double lowestPrice;
    public double closePrice;

    public String dealVolum;
    public String dealValue;
    // 总市值
    public String marketCap;
    // 流通市值
    public String tradingCap;

    public String oscillation;
    // 换手率
    public String exchangeRatio;
    // 市盈率
    public double price2EarningRatio;
    // 市净率
    public double price2BookRatio;
}
