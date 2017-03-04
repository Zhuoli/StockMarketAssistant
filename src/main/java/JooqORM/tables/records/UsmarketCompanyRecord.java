/**
 * This class is generated by jOOQ
 */
package JooqORM.tables.records;


import JooqORM.tables.UsmarketCompany;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record18;
import org.jooq.Row18;
import org.jooq.impl.UpdatableRecordImpl;


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
public class UsmarketCompanyRecord extends UpdatableRecordImpl<UsmarketCompanyRecord> implements Record18<String, String, Double, String, Double, Timestamp, Double, Timestamp, Double, String, Double, String, Double, Double, String, String, String, Timestamp> {

    private static final long serialVersionUID = -248778606;

    /**
     * Setter for <code>StockMarket.usmarket_company.stockid</code>. 股票ID
     */
    public void setStockid(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.stockid</code>. 股票ID
     */
    public String getStockid() {
        return (String) get(0);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.capitalizationvalue</code>. 流通市值
     */
    public void setCapitalizationvalue(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.capitalizationvalue</code>. 流通市值
     */
    public String getCapitalizationvalue() {
        return (String) get(1);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.close_price</code>. 昨日收盘价
     */
    public void setClosePrice(Double value) {
        set(2, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.close_price</code>. 昨日收盘价
     */
    public Double getClosePrice() {
        return (Double) get(2);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.companyname</code>. 公司名称
     */
    public void setCompanyname(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.companyname</code>. 公司名称
     */
    public String getCompanyname() {
        return (String) get(3);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.currentprice</code>. Current Price
     */
    public void setCurrentprice(Double value) {
        set(4, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.currentprice</code>. Current Price
     */
    public Double getCurrentprice() {
        return (Double) get(4);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.currentpricetimestamp</code>.
     */
    public void setCurrentpricetimestamp(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.currentpricetimestamp</code>.
     */
    public Timestamp getCurrentpricetimestamp() {
        return (Timestamp) get(5);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.highest_price</code>. 最高价
     */
    public void setHighestPrice(Double value) {
        set(6, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.highest_price</code>. 最高价
     */
    public Double getHighestPrice() {
        return (Double) get(6);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.last_update_date_time</code>. 最后一次更新时间
     */
    public void setLastUpdateDateTime(Timestamp value) {
        set(7, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.last_update_date_time</code>. 最后一次更新时间
     */
    public Timestamp getLastUpdateDateTime() {
        return (Timestamp) get(7);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.lowest_price</code>. 最低价
     */
    public void setLowestPrice(Double value) {
        set(8, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.lowest_price</code>. 最低价
     */
    public Double getLowestPrice() {
        return (Double) get(8);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.marketcap</code>. 总市值
     */
    public void setMarketcap(String value) {
        set(9, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.marketcap</code>. 总市值
     */
    public String getMarketcap() {
        return (String) get(9);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.openprice</code>. 开盘价
     */
    public void setOpenprice(Double value) {
        set(10, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.openprice</code>. 开盘价
     */
    public Double getOpenprice() {
        return (Double) get(10);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.oscillation</code>. 股票振幅
     */
    public void setOscillation(String value) {
        set(11, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.oscillation</code>. 股票振幅
     */
    public String getOscillation() {
        return (String) get(11);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.pbr</code>. 市净率 Price-to-book ratio
     */
    public void setPbr(Double value) {
        set(12, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.pbr</code>. 市净率 Price-to-book ratio
     */
    public Double getPbr() {
        return (Double) get(12);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.per</code>. 市盈率
     */
    public void setPer(Double value) {
        set(13, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.per</code>. 市盈率
     */
    public Double getPer() {
        return (Double) get(13);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.tradingvalue</code>. 成交额
     */
    public void setTradingvalue(String value) {
        set(14, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.tradingvalue</code>. 成交额
     */
    public String getTradingvalue() {
        return (String) get(14);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.tradingvolume</code>. 成交量
     */
    public void setTradingvolume(String value) {
        set(15, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.tradingvolume</code>. 成交量
     */
    public String getTradingvolume() {
        return (String) get(15);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.turnoverrate</code>. 换手率
     */
    public void setTurnoverrate(String value) {
        set(16, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.turnoverrate</code>. 换手率
     */
    public String getTurnoverrate() {
        return (String) get(16);
    }

    /**
     * Setter for <code>StockMarket.usmarket_company.listing_date</code>.
     */
    public void setListingDate(Timestamp value) {
        set(17, value);
    }

    /**
     * Getter for <code>StockMarket.usmarket_company.listing_date</code>.
     */
    public Timestamp getListingDate() {
        return (Timestamp) get(17);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record18 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row18<String, String, Double, String, Double, Timestamp, Double, Timestamp, Double, String, Double, String, Double, Double, String, String, String, Timestamp> fieldsRow() {
        return (Row18) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row18<String, String, Double, String, Double, Timestamp, Double, Timestamp, Double, String, Double, String, Double, Double, String, String, String, Timestamp> valuesRow() {
        return (Row18) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return UsmarketCompany.USMARKET_COMPANY.STOCKID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return UsmarketCompany.USMARKET_COMPANY.CAPITALIZATIONVALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field3() {
        return UsmarketCompany.USMARKET_COMPANY.CLOSE_PRICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return UsmarketCompany.USMARKET_COMPANY.COMPANYNAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field5() {
        return UsmarketCompany.USMARKET_COMPANY.CURRENTPRICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field6() {
        return UsmarketCompany.USMARKET_COMPANY.CURRENTPRICETIMESTAMP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field7() {
        return UsmarketCompany.USMARKET_COMPANY.HIGHEST_PRICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field8() {
        return UsmarketCompany.USMARKET_COMPANY.LAST_UPDATE_DATE_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field9() {
        return UsmarketCompany.USMARKET_COMPANY.LOWEST_PRICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field10() {
        return UsmarketCompany.USMARKET_COMPANY.MARKETCAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field11() {
        return UsmarketCompany.USMARKET_COMPANY.OPENPRICE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field12() {
        return UsmarketCompany.USMARKET_COMPANY.OSCILLATION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field13() {
        return UsmarketCompany.USMARKET_COMPANY.PBR;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Double> field14() {
        return UsmarketCompany.USMARKET_COMPANY.PER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field15() {
        return UsmarketCompany.USMARKET_COMPANY.TRADINGVALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field16() {
        return UsmarketCompany.USMARKET_COMPANY.TRADINGVOLUME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field17() {
        return UsmarketCompany.USMARKET_COMPANY.TURNOVERRATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field18() {
        return UsmarketCompany.USMARKET_COMPANY.LISTING_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getStockid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getCapitalizationvalue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value3() {
        return getClosePrice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getCompanyname();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value5() {
        return getCurrentprice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value6() {
        return getCurrentpricetimestamp();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value7() {
        return getHighestPrice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value8() {
        return getLastUpdateDateTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value9() {
        return getLowestPrice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value10() {
        return getMarketcap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value11() {
        return getOpenprice();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value12() {
        return getOscillation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value13() {
        return getPbr();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double value14() {
        return getPer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value15() {
        return getTradingvalue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value16() {
        return getTradingvolume();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value17() {
        return getTurnoverrate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value18() {
        return getListingDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value1(String value) {
        setStockid(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value2(String value) {
        setCapitalizationvalue(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value3(Double value) {
        setClosePrice(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value4(String value) {
        setCompanyname(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value5(Double value) {
        setCurrentprice(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value6(Timestamp value) {
        setCurrentpricetimestamp(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value7(Double value) {
        setHighestPrice(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value8(Timestamp value) {
        setLastUpdateDateTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value9(Double value) {
        setLowestPrice(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value10(String value) {
        setMarketcap(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value11(Double value) {
        setOpenprice(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value12(String value) {
        setOscillation(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value13(Double value) {
        setPbr(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value14(Double value) {
        setPer(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value15(String value) {
        setTradingvalue(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value16(String value) {
        setTradingvolume(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value17(String value) {
        setTurnoverrate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord value18(Timestamp value) {
        setListingDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UsmarketCompanyRecord values(String value1, String value2, Double value3, String value4, Double value5, Timestamp value6, Double value7, Timestamp value8, Double value9, String value10, Double value11, String value12, Double value13, Double value14, String value15, String value16, String value17, Timestamp value18) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        value10(value10);
        value11(value11);
        value12(value12);
        value13(value13);
        value14(value14);
        value15(value15);
        value16(value16);
        value17(value17);
        value18(value18);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UsmarketCompanyRecord
     */
    public UsmarketCompanyRecord() {
        super(UsmarketCompany.USMARKET_COMPANY);
    }

    /**
     * Create a detached, initialised UsmarketCompanyRecord
     */
    public UsmarketCompanyRecord(String stockid, String capitalizationvalue, Double closePrice, String companyname, Double currentprice, Timestamp currentpricetimestamp, Double highestPrice, Timestamp lastUpdateDateTime, Double lowestPrice, String marketcap, Double openprice, String oscillation, Double pbr, Double per, String tradingvalue, String tradingvolume, String turnoverrate, Timestamp listingDate) {
        super(UsmarketCompany.USMARKET_COMPANY);

        set(0, stockid);
        set(1, capitalizationvalue);
        set(2, closePrice);
        set(3, companyname);
        set(4, currentprice);
        set(5, currentpricetimestamp);
        set(6, highestPrice);
        set(7, lastUpdateDateTime);
        set(8, lowestPrice);
        set(9, marketcap);
        set(10, openprice);
        set(11, oscillation);
        set(12, pbr);
        set(13, per);
        set(14, tradingvalue);
        set(15, tradingvolume);
        set(16, turnoverrate);
        set(17, listingDate);
    }
}