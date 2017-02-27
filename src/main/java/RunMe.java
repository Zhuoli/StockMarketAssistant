import MarketChinese.ChineseMarketMaster;
import MarketUS.USMarketMaster;
import org.apache.commons.cli.*;
import util.MarketConstant;

/**
 * Entry point.
 */
public class RunMe {
    private CommandLine cmd;

    public static void main(String[] args) {
        RunMe runMe = new RunMe(args);

        if (runMe.cmd == null) {
            System.err.println("Failed on CommandLine initialization, system exit.");
            System.exit(1);
        }

        ChineseMarketMaster chineseMarketMaster = new ChineseMarketMaster(runMe.cmd);
        USMarketMaster usMarketMaster = new USMarketMaster(runMe.cmd);

        // Run one round of query & update despite the current time
        if (runMe.cmd.hasOption(MarketConstant.DEBUG)) {
            usMarketMaster.init();
            usMarketMaster.querryAndUpdate();
//            chineseMarketMaster.init();
//            chineseMarketMaster.querryAndUpdate();
        }

        chineseMarketMaster.run();
    }

    /**
     * Constructor.
     * 
     * @param args
     */
    private RunMe(String[] args) {
        // create Options object
        Options options = new Options();

        // add t option
        options.addOption(MarketConstant.IS_UNDER_INTELLIJ, false, "Is running under IDE or not");
        options.addOption(MarketConstant.DEBUG, false, "Is running under Debug model or not");
        CommandLineParser parser = new DefaultParser();

        try {
            this.cmd = parser.parse(options, args);
        } catch (ParseException exc) {
            System.err.println("Arguments parse exception: " + exc.getMessage());
        }
    }
}
