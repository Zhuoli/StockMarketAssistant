package dataEngineer.data;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by zhuolil on 6/11/17.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class FinancialData extends WebParserData{

    private String reporturl;

    // 销售毛利率
    private double grossMargin;

    // 净资产收益率
    private double roe;

    @Builder
    private FinancialData(String stockId, String reportUrl, double grossMargin, double roe){
        super(stockId);
        this.reporturl = reportUrl;
        this.grossMargin = grossMargin;
        this.roe = roe;
    }
}
