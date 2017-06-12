package dataEngineer.financeWebEngine;

import dataEngineer.data.FinancialData;
import dataEngineer.data.SharesQuote;

import java.io.IOException;

/**
 * Interface.
 */
public interface IWebParser {
    public SharesQuote queryCompanyStock(String symbol) throws IOException;

    public FinancialData queryFinancialData(String symbol) throws IOException;
}
