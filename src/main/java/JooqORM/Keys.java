/**
 * This class is generated by jOOQ
 */
package JooqORM;


import JooqORM.tables.Company;
import JooqORM.tables.records.CompanyRecord;

import javax.annotation.Generated;

import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;


/**
 * A class modelling foreign key relationships between tables of the <code>ChineseStock</code> 
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

    public static final UniqueKey<CompanyRecord> KEY_COMPANY_PRIMARY = UniqueKeys0.KEY_COMPANY_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<CompanyRecord> KEY_COMPANY_PRIMARY = createUniqueKey(Company.COMPANY, "KEY_Company_PRIMARY", Company.COMPANY.STOCKID);
    }
}