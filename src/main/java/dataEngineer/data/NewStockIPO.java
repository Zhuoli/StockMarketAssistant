package dataEngineer.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by zhulian on 8/29/2017.
 */

@AllArgsConstructor
@Data
public class NewStockIPO {
    // 申购代码
    private String id;

    // 股票简称
    private String companyName;

    // 申购日期
    private String date;
}
