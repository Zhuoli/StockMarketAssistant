package dataEngineer;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Represents an CompanyObject's Shares Quote.
 */

@Builder
@Data
public class SharesQuote {

    private String stockid;
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
}
