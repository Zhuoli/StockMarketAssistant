package dataEngineer.data;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by zhuolil on 6/11/17.
 */
@Data
@Builder
public class FinancialData{
    private String reporturl;
    // 销售毛利率
    private double grossMargin;

    // 净资产收益率
    private double roe;
}
