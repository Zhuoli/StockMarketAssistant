package mongoDb.domains;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * Created by zhuol on 2017/7/4.
 */
@Data
public class USStockMarket extends ICollection{
    public final static String TABLE_NAME = "usstockcompany";

    public String companyName;

    public double currentPrice;

    public Date lastUpdatedTime;
    @Builder
    private USStockMarket(String _id, String companyName, double currentPrice, Date lastUpdatedTime){
        super(_id);
        this.companyName = companyName;
        this.currentPrice = currentPrice;
        this.lastUpdatedTime = lastUpdatedTime;
    }
}
