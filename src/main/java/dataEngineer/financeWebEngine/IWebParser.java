package dataEngineer.financeWebEngine;

import dataEngineer.SharesQuote;

import java.io.IOException;

/**
 * Interface.
 */
public interface IWebParser {
    public SharesQuote queryCompanyStock(String symbol) throws IOException;
}
