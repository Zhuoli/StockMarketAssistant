package dataEngineer.data;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Represents an CompanyObject's Shares Quote.
 */
@Data
public class SharesQuote extends WebParserData{

    private String companyname;

    private double currentPrice;

    private double openPrice;
    private double highestPrice;
    private double lowestPrice;
    private double closePrice;

    // 成交量
    private String dealVolum;

    // 成交额
    private String dealValue;
    // 总市值
    private String marketCap;
    // 流通市值
    private String tradingCap;

    // 振幅
    private String oscillation;
    // 换手率
    private String exchangeRatio;
    // 市盈率
    private double price2EarningRatio;
    // 市净率
    private double price2BookRatio;

    private String officialWebUrl;

    private String oneYearTargetPrice;

    private Date listingDate;

    @Builder
    private SharesQuote(String stockId, String companyname, double currentPrice, double openPrice, double highestPrice,
                        double lowestPrice, double closePrice, String dealVolum, String dealValue, String marketCap,
                        String tradingCap, String oscillation, String exchangeRatio, double price2EarningRatio,
                        double price2BookRatio, String officialWebUrl, String oneYearTargetPrice, Date listingDate){
        super(stockId);
        this.companyname = companyname;
        this.currentPrice = currentPrice;
        this.openPrice = openPrice;
        this.highestPrice = highestPrice;
        this.lowestPrice = lowestPrice;
        this.closePrice = closePrice;
        this.dealVolum = dealVolum;
        this.dealValue = dealValue;
        this.marketCap = marketCap;
        this.tradingCap = tradingCap;
        this.oscillation = oscillation;
        this.exchangeRatio = exchangeRatio;
        this.price2EarningRatio = price2EarningRatio;
        this.price2BookRatio = price2BookRatio;
        this.officialWebUrl = officialWebUrl;
        this.oneYearTargetPrice = oneYearTargetPrice;
        this.listingDate = listingDate;
    }

}
