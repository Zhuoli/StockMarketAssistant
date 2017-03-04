package util;

import dataEngineer.SharesQuote;
import dataEngineer.financeWebEngine.IWebParser;
import dataEngineer.financeWebEngine.XueqiuWebParser;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Wrapper of future task.
 */
public class RunmeFuture {
    public Future future;
    public Optional<Long> startTimeMillis = Optional.empty();

    public Optional<SharesQuote> result = Optional.empty();

    public boolean isResultConsumed = false;

    public RunmeFuture(Function<Runnable, Future> submit, SharesQuote companyObject, IWebParser webParser) {
        this.future = submit.apply(() -> {

            // Mark task start time
            startTimeMillis = Optional.of(System.currentTimeMillis());

            try {
                SharesQuote quote = webParser.queryCompanyStock(companyObject.getStockid());
                quote.setStockid(companyObject.getStockid());
                quote.setCompanyname(companyObject.getCompanyname());
                quote.setOfficialWebUrl(companyObject.getOfficialWebUrl());
                this.result = Optional.of(quote);
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        });
    }
}
