package minerful.reactive.params;

import minerful.params.ParamsManager;
import minerful.reactive.measurements.Measures;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class JanusMeasurementsCmdParameters extends ParamsManager {

    public static final String NaN_TRACE_SUBSTITUTE_FLAG_PARAM_NAME = "nanTraceSubstitute";
    public static final String NaN_TRACE_SUBSTITUTE_VALUE_PARAM_NAME = "nanTraceValue";
    public static final String NaN_LOG_SKIP_FLAG_PARAM_NAME = "nanLogSkip";
    public static final String LITE_FLAG_PARAM_NAME = "lite";

    public static final String MEASURE_NAME = "measure";  // measure to use for the measurements, default: "all"
    public static final String DEFAULT_MEASURE = "all";

    public static final String DETAILS_LEVEL_PARAM_NAME = "detailsLevel";
    public static final DetailLevel DEFAULT_DETAILS_LEVEL = DetailLevel.all;

    public enum DetailLevel {
        /**
         * Compute and return only the events evaluation.
         * Note.    Regardless if it is returned in output or not,
         * the events evaluation is computed anyway because it is the foundation of all the the other measures
         */
        event,
        /**
         * Compute and return only the traces measurements
         */
        trace,
        /**
         * Compute and return only the traces measurements descriptive statistics across the event log
         */
        traceStats,
        /**
         * Compute and return only the log measurements
         */
        log,
        /**
         * Compute and return only the traces measurements and their descriptive statistics across the event log
         */
        allTrace,
        /**
         * Compute and return only the log measurements and the traces measurements descriptive statistics across the event log
         */
        allLog,
        /**
         * Compute and return the measures everything [DEFAULT]
         */
        all;
    }

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
    /**
     * parameter to set to output only the traces result, the aggregated measures, or both. default= both
     **/
    public DetailLevel detailsLevel;
    /**
     * measure to use for the comparison, default: "Confidence"
     */
    public String measure;

    public JanusMeasurementsCmdParameters() {
        super();
        this.nanTraceSubstituteFlag = false;
        this.nanTraceSubstituteValue = 0;
        this.nanLogSkipFlag = false;
        this.liteFlag = false;
        this.detailsLevel = DEFAULT_DETAILS_LEVEL;
        this.measure = DEFAULT_MEASURE;
    }

    public JanusMeasurementsCmdParameters(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue, boolean nanLogSkipFlag) {
        super();
        this.nanTraceSubstituteFlag = nanTraceSubstituteFlag;
        this.nanTraceSubstituteValue = nanTraceSubstituteValue;
        this.nanLogSkipFlag = nanLogSkipFlag;
    }

    public JanusMeasurementsCmdParameters(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue, boolean nanLogSkipFlag, boolean liteFlag) {
        super();
        this.nanTraceSubstituteFlag = nanTraceSubstituteFlag;
        this.nanTraceSubstituteValue = nanTraceSubstituteValue;
        this.nanLogSkipFlag = nanLogSkipFlag;
        this.liteFlag = liteFlag;
    }

    public JanusMeasurementsCmdParameters(DetailLevel detailsLevel) {
        super();
        this.detailsLevel = detailsLevel;
    }

    public JanusMeasurementsCmdParameters(Options options, String[] args) {
        this();
        // parse the command line arguments
        this.parseAndSetup(options, args);
    }

    public JanusMeasurementsCmdParameters(String[] args) {
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

    public static String getDefaultMeasure() {
        return DEFAULT_MEASURE;
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
        this.liteFlag = line.hasOption(LITE_FLAG_PARAM_NAME);
        this.detailsLevel = DetailLevel.valueOf(
                line.getOptionValue(
                        DETAILS_LEVEL_PARAM_NAME,
                        this.detailsLevel.toString()
                )
        );
        this.measure = line.getOptionValue(
                MEASURE_NAME,
                DEFAULT_MEASURE
        );
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
        options.addOption(
                Option.builder(DETAILS_LEVEL_PARAM_NAME)
                        .hasArg().argName("name")
                        .longOpt("details-level")
                        .desc(("levels of details of the measures to compute. {event(only events evaluation), trace(only trace measures), traceStats(only trace measures log stats), log(only log measures), allTrace(traces measures and their stats), allLog(log measures and traces measures stats), all}. Default: all")
                                + printDefault(fromEnumValueToString(DEFAULT_DETAILS_LEVEL)))
                        .type(String.class)
                        .build()
        );
        StringBuilder allMeasures= new StringBuilder();
        allMeasures.append("'all'");
        for (String m:Measures.MEASURE_NAMES) {
            allMeasures.append(",'"+m+"'");
        }
        options.addOption(
                Option.builder(MEASURE_NAME)
                        .hasArg().argName("name")
                        .longOpt("measure")
                        .desc(("measure to compute, either a specific one or all the supported ones. {"+ allMeasures +"}")
                                + printDefault(fromEnumValueToString(DEFAULT_MEASURE)))
                        .type(String.class)
                        .build()
        );
        return options;
    }

}