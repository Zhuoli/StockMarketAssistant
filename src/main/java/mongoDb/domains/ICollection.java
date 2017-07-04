package mongoDb.domains;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by zhuol on 2017/7/4.
 */
@AllArgsConstructor
@Data
public abstract class ICollection {
    public static String TABLE_NAME;
    public String _id;
}
