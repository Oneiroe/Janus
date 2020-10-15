package minerful.reactive.params;

import minerful.params.ParamsManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class JanusCheckingCmdParameters extends ParamsManager {

    public static final String NaN_TRACE_SUBSTITUTE_FLAG_PARAM_NAME = "nanTraceSubstitute";
    public static final String NaN_TRACE_SUBSTITUTE_VALUE_PARAM_NAME = "nanTraceValue";
    public static final String NaN_LOG_SKIP_FLAG_PARAM_NAME = "nanLogSkip";


    /**
     * decide if a NaN should be kept as-is in a measure-trace evaluation should be substituted with a certain value
     */
    public boolean nanTraceSubstituteFlag;
    public double nanTraceSubstituteValue;
    /**
     * decide if a NaN should be skipped or not during the computation of the log level aggregated measures
     */
    public boolean nanLogSkipFlag;

    public JanusCheckingCmdParameters() {
        super();
        this.nanTraceSubstituteFlag = false;
        this.nanTraceSubstituteValue = 0;
        this.nanLogSkipFlag = false;
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

    @Override
    protected void setup(CommandLine line) {
        this.nanTraceSubstituteFlag = line.hasOption(NaN_TRACE_SUBSTITUTE_FLAG_PARAM_NAME);
        this.nanTraceSubstituteValue = Double.parseDouble(line.getOptionValue(
                NaN_TRACE_SUBSTITUTE_VALUE_PARAM_NAME,
                Double.toString(this.nanTraceSubstituteValue)
                )
        );
        this.nanLogSkipFlag = line.hasOption(NaN_LOG_SKIP_FLAG_PARAM_NAME);
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
        return options;
    }

}