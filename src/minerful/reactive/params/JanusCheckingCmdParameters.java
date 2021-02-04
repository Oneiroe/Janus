package minerful.reactive.params;

import minerful.params.ParamsManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class JanusCheckingCmdParameters extends ParamsManager {

    public static final String NaN_TRACE_SUBSTITUTE_FLAG_PARAM_NAME = "nanTraceSubstitute";
    public static final String NaN_TRACE_SUBSTITUTE_VALUE_PARAM_NAME = "nanTraceValue";
    public static final String NaN_LOG_SKIP_FLAG_PARAM_NAME = "nanLogSkip";
    public static final String LITE_FLAG_PARAM_NAME = "lite";

    /**
     * decide if a NaN should be kept as-is in a measure-trace evaluation should be substituted with a certain value
     */
    public boolean nanTraceSubstituteFlag;
    public double nanTraceSubstituteValue;
    /**
     * decide if a NaN should be skipped or not during the computation of the log level aggregated measures
     */
    public boolean nanLogSkipFlag;
    /**
     * decide if to use the MEgaMatrixMonster (details for singles events) or the MegaMatrixLite (space reduction, only traces results)
     */
    public boolean liteFlag;

    public JanusCheckingCmdParameters() {
        super();
        this.nanTraceSubstituteFlag = false;
        this.nanTraceSubstituteValue = 0;
        this.nanLogSkipFlag = false;
        this.liteFlag = false;
    }

    public JanusCheckingCmdParameters(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue, boolean nanLogSkipFlag) {
        super();
        this.nanTraceSubstituteFlag = nanTraceSubstituteFlag;
        this.nanTraceSubstituteValue = nanTraceSubstituteValue;
        this.nanLogSkipFlag = nanLogSkipFlag;
    }

    public JanusCheckingCmdParameters(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue, boolean nanLogSkipFlag, boolean liteFlag) {
        super();
        this.nanTraceSubstituteFlag = nanTraceSubstituteFlag;
        this.nanTraceSubstituteValue = nanTraceSubstituteValue;
        this.nanLogSkipFlag = nanLogSkipFlag;
        this.liteFlag = liteFlag;
    }

    public JanusCheckingCmdParameters(Options options, String[] args) {
        this();
        // parse the command line arguments
        this.parseAndSetup(options, args);
    }

    public JanusCheckingCmdParameters(String[] args) {
        this();
        // parse the command line arguments
        this.parseAndSetup(new Options(), args);
    }

    public boolean isNanTraceSubstituteFlag() {
        return nanTraceSubstituteFlag;
    }

    public void setNanTraceSubstituteFlag(boolean nanTraceSubstituteFlag) {
        this.nanTraceSubstituteFlag = nanTraceSubstituteFlag;
    }

    public double getNanTraceSubstituteValue() {
        return nanTraceSubstituteValue;
    }

    public void setNanTraceSubstituteValue(double nanTraceSubstituteValue) {
        this.nanTraceSubstituteValue = nanTraceSubstituteValue;
    }

    public boolean isNanLogSkipFlag() {
        return nanLogSkipFlag;
    }

    public void setNanLogSkipFlag(boolean nanLogSkipFlag) {
        this.nanLogSkipFlag = nanLogSkipFlag;
    }

    public boolean isLiteFlag() {
        return liteFlag;
    }

    public void setLiteFlag(boolean liteFlag) {
        this.liteFlag = liteFlag;
    }

    @Override
    protected void setup(CommandLine line) {
        this.nanTraceSubstituteFlag = line.hasOption(NaN_TRACE_SUBSTITUTE_FLAG_PARAM_NAME);
        this.nanTraceSubstituteValue = Double.parseDouble(line.getOptionValue(
                NaN_TRACE_SUBSTITUTE_VALUE_PARAM_NAME,
                Double.toString(this.nanTraceSubstituteValue)
                )
        );
        this.nanLogSkipFlag = line.hasOption(NaN_LOG_SKIP_FLAG_PARAM_NAME);
        this.liteFlag=line.hasOption(LITE_FLAG_PARAM_NAME);
    }

    @Override
    public Options addParseableOptions(Options options) {
        Options myOptions = listParseableOptions();
        for (Object myOpt : myOptions.getOptions())
            options.addOption((Option) myOpt);
        return options;
    }

    @Override
    public Options listParseableOptions() {
        return parseableOptions();
    }

    @SuppressWarnings("static-access")
    public static Options parseableOptions() {
        Options options = new Options();
        options.addOption(
                Option.builder(NaN_TRACE_SUBSTITUTE_FLAG_PARAM_NAME)
                        .longOpt("nan-trace-substitute")
                        .desc("Flag to substitute or not the NaN values when computing trace measures")
                        .build()
        );
        options.addOption(
                Option.builder(NaN_TRACE_SUBSTITUTE_VALUE_PARAM_NAME)
                        .hasArg().argName("number")
                        .longOpt("nan-trace-value")
                        .desc("Value to be substituted to NaN values in trace measures")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(NaN_LOG_SKIP_FLAG_PARAM_NAME)
                        .longOpt("nan-log-skip")
                        .desc("Flag to skip or not NaN values when computing log measures")
                        .build()
        );
        options.addOption(
                Option.builder(LITE_FLAG_PARAM_NAME)
                        .longOpt("lite-flag")
                        .desc("Flag to use the space saving data structure")
                        .build()
        );
        return options;
    }

}