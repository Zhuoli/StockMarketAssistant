/**
 * This class is generated by jOOQ
 */
package JooqORM.tables;


import JooqORM.Keys;
import JooqORM.Stockmarket;
import JooqORM.tables.records.ChineseMarketCompanyRecord;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.4"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ChineseMarketCompany extends TableImpl<ChineseMarketCompanyRecord> {

    private static final long serialVersionUID = -179307100;

    /**
     * The reference instance of <code>StockMarket.chinese_market_company</code>
     */
    public static final ChineseMarketCompany CHINESE_MARKET_COMPANY = new ChineseMarketCompany();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ChineseMarketCompanyRecord> getRecordType() {
        return ChineseMarketCompanyRecord.class;
    }

    /**
     * The column <code>StockMarket.chinese_market_company.stockid</code>. 股票ID
     */
    public final TableField<ChineseMarketCompanyRecord, String> STOCKID = createField("stockid", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "股票ID");

    /**
     * The column <code>StockMarket.chinese_market_company.capitalizationvalue</code>. 流通市值
     */
    public final TableField<ChineseMarketCompanyRecord, String> CAPITALIZATIONVALUE = createField("capitalizationvalue", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "流通市值");

    /**
     * The column <code>StockMarket.chinese_market_company.close_price</code>. 昨日收盘价
     */
    public final TableField<ChineseMarketCompanyRecord, Double> CLOSE_PRICE = createField("close_price", org.jooq.impl.SQLDataType.DOUBLE, this, "昨日收盘价");

    /**
     * The column <code>StockMarket.chinese_market_company.companyname</code>. 公司名称
     */
    public final TableField<ChineseMarketCompanyRecord, String> COMPANYNAME = createField("companyname", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "公司名称");

    /**
     * The column <code>StockMarket.chinese_market_company.currentprice</code>. Current Price
     */
    public final TableField<ChineseMarketCompanyRecord, Double> CURRENTPRICE = createField("currentprice", org.jooq.impl.SQLDataType.DOUBLE, this, "Current Price");

    /**
     * The column <code>StockMarket.chinese_market_company.currentpricetimestamp</code>.
     */
    public final TableField<ChineseMarketCompanyRecord, Timestamp> CURRENTPRICETIMESTAMP = createField("currentpricetimestamp", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * The column <code>StockMarket.chinese_market_company.highest_price</code>. 最高价
     */
    public final TableField<ChineseMarketCompanyRecord, Double> HIGHEST_PRICE = createField("highest_price", org.jooq.impl.SQLDataType.DOUBLE, this, "最高价");

    /**
     * The column <code>StockMarket.chinese_market_company.last_update_date_time</code>. 最后一次更新时间
     */
    public final TableField<ChineseMarketCompanyRecord, Timestamp> LAST_UPDATE_DATE_TIME = createField("last_update_date_time", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "最后一次更新时间");

    /**
     * The column <code>StockMarket.chinese_market_company.lowest_price</code>. 最低价
     */
    public final TableField<ChineseMarketCompanyRecord, Double> LOWEST_PRICE = createField("lowest_price", org.jooq.impl.SQLDataType.DOUBLE, this, "最低价");

    /**
     * The column <code>StockMarket.chinese_market_company.marketcap</code>. 总市值
     */
    public final TableField<ChineseMarketCompanyRecord, String> MARKETCAP = createField("marketcap", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "总市值");

    /**
     * The column <code>StockMarket.chinese_market_company.openprice</code>. 开盘价
     */
    public final TableField<ChineseMarketCompanyRecord, Double> OPENPRICE = createField("openprice", org.jooq.impl.SQLDataType.DOUBLE, this, "开盘价");

    /**
     * The column <code>StockMarket.chinese_market_company.oscillation</code>. 股票振幅
     */
    public final TableField<ChineseMarketCompanyRecord, String> OSCILLATION = createField("oscillation", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "股票振幅");

    /**
     * The column <code>StockMarket.chinese_market_company.pbr</code>. 市净率 Price-to-book ratio
     */
    public final TableField<ChineseMarketCompanyRecord, Double> PBR = createField("pbr", org.jooq.impl.SQLDataType.DOUBLE, this, "市净率 Price-to-book ratio");

    /**
     * The column <code>StockMarket.chinese_market_company.per</code>. 市盈率
     */
    public final TableField<ChineseMarketCompanyRecord, Double> PER = createField("per", org.jooq.impl.SQLDataType.DOUBLE, this, "市盈率");

    /**
     * The column <code>StockMarket.chinese_market_company.tradingvalue</code>. 成交额
     */
    public final TableField<ChineseMarketCompanyRecord, String> TRADINGVALUE = createField("tradingvalue", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "成交额");

    /**
     * The column <code>StockMarket.chinese_market_company.tradingvolume</code>. 成交量
     */
    public final TableField<ChineseMarketCompanyRecord, String> TRADINGVOLUME = createField("tradingvolume", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "成交量");

    /**
     * The column <code>StockMarket.chinese_market_company.turnoverrate</code>. 换手率
     */
    public final TableField<ChineseMarketCompanyRecord, String> TURNOVERRATE = createField("turnoverrate", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "换手率");

    /**
     * The column <code>StockMarket.chinese_market_company.listing_date</code>.
     */
    public final TableField<ChineseMarketCompanyRecord, Timestamp> LISTING_DATE = createField("listing_date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false), this, "");

    /**
     * Create a <code>StockMarket.chinese_market_company</code> table reference
     */
    public ChineseMarketCompany() {
        this("chinese_market_company", null);
    }

    /**
     * Create an aliased <code>StockMarket.chinese_market_company</code> table reference
     */
    public ChineseMarketCompany(String alias) {
        this(alias, CHINESE_MARKET_COMPANY);
    }

    private ChineseMarketCompany(String alias, Table<ChineseMarketCompanyRecord> aliased) {
        this(alias, aliased, null);
    }

    private ChineseMarketCompany(String alias, Table<ChineseMarketCompanyRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Stockmarket.STOCKMARKET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ChineseMarketCompanyRecord> getPrimaryKey() {
        return Keys.KEY_CHINESE_MARKET_COMPANY_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ChineseMarketCompanyRecord>> getKeys() {
        return Arrays.<UniqueKey<ChineseMarketCompanyRecord>>asList(Keys.KEY_CHINESE_MARKET_COMPANY_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChineseMarketCompany as(String alias) {
        return new ChineseMarketCompany(alias, this);
    }

    /**
     * Rename this table
     */
    public ChineseMarketCompany rename(String name) {
        return new ChineseMarketCompany(name, null);
    }
}