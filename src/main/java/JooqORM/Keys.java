/**
 * This class is generated by jOOQ
 */
package JooqORM;


import JooqORM.tables.Chinesemarketcompany;
import JooqORM.tables.Usmarketcompany;
import JooqORM.tables.records.ChinesemarketcompanyRecord;
import JooqORM.tables.records.UsmarketcompanyRecord;

import javax.annotation.Generated;

import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling foreign key relationships between tables of the <code>StockMarket</code> 
 * schema
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<ChinesemarketcompanyRecord> KEY_CHINESEMARKETCOMPANY_PRIMARY = UniqueKeys0.KEY_CHINESEMARKETCOMPANY_PRIMARY;
    public static final UniqueKey<UsmarketcompanyRecord> KEY_USMARKETCOMPANY_PRIMARY = UniqueKeys0.KEY_USMARKETCOMPANY_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<ChinesemarketcompanyRecord> KEY_CHINESEMARKETCOMPANY_PRIMARY = createUniqueKey(Chinesemarketcompany.CHINESEMARKETCOMPANY, "KEY_ChineseMarketCompany_PRIMARY", Chinesemarketcompany.CHINESEMARKETCOMPANY.STOCKID);
        public static final UniqueKey<UsmarketcompanyRecord> KEY_USMARKETCOMPANY_PRIMARY = createUniqueKey(Usmarketcompany.USMARKETCOMPANY, "KEY_USMarketCompany_PRIMARY", Usmarketcompany.USMARKETCOMPANY.STOCKID);
    }
}
