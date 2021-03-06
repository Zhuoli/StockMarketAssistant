package dataEngineer.data;

import lombok.Builder;
import lombok.Data;
import org.junit.Assert;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Represents an CompanyObject's Shares Quote.
 */
@Data
@Builder
public class SharesQuote{
    private String _id;

    private String dateFirstIPO;
    public String getDateFirstIPO(){
        if(dateFirstIPO!=null && dateFirstIPO.contains("/")){
            String[] strs = dateFirstIPO.split("/");
            if(strs.length==3)
                dateFirstIPO=strs[2]+"-" +strs[0] + "-" + strs[1];
        }
        return dateFirstIPO;
    }

    private String companyname;

    private String currentPrice;

    private String openPrice;
    private double highestPrice;
    private double lowestPrice;
    private double closePrice;

    private double changePercent;

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

    private String oneYearTargetPrice;

    private Date listingDate;

    private Date lastUpdatedTime;

    private FinancialData financialData;

    private List<String> concepts;

    public static int moveUnsearchedDataAhead(Set<String> updatedStockIdSet, SharesQuote[] array){

        Assert.assertNotNull(updatedStockIdSet);
        Assert.assertNotNull(array);

        int nextSeenIdx = array.length - 1;

        // Sort company array so that those unsearched company moved to head of array and those
        // companies already in databaes moved to tail.
        for (int idx = 0; idx <= nextSeenIdx; idx++) {
            // If current stock is seen in records
            if (updatedStockIdSet.contains(array[idx].get_id())) {
                // Move this stock to tail
                SharesQuote tmp = array[nextSeenIdx];
                array[nextSeenIdx] = array[idx];
                array[idx] = tmp;
                nextSeenIdx--;
                idx--;
            }
        }

        return nextSeenIdx;
    }
}
