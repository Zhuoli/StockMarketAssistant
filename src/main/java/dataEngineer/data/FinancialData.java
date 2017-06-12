package dataEngineer.data;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zhuolil on 6/11/17.
 */
@Builder
@Data
public class FinancialData {

    private String stockId;

    private String reporturl;

    // 销售毛利率
    private double grossMargin;
}
