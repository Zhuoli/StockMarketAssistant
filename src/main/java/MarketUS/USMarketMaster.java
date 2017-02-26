package MarketUS;

import dataEngineer.SharesQuote;
import dataEngineer.StockCompanyCollection;
import org.apache.commons.cli.CommandLine;
import org.junit.Assert;
import util.MarketConstant;

/**
 * Created by zhuolil on 2/25/17.
 */
public class USMarketMaster {

    CommandLine cmd;
    SharesQuote[] companies;
    boolean isInited = false;

    public USMarketMaster(CommandLine cmd){
        this.cmd = cmd;
    }


    public void init(){
        Assert.assertNotNull(cmd);
        StockCompanyCollection companyCollection = StockCompanyCollection.getInstance();
        this.companies = companyCollection.queryCompanyListUS(cmd.hasOption(MarketConstant.IS_UNDER_INTELLIJ));
        this.isInited = true;
    }
}
