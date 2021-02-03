package minerful.reactive.params;

import minerful.params.ParamsManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;

public class JanusVariantCmdParameters extends ParamsManager {
    //      Variant specific
    public static final String INPUT_LOGFILE_1_PATH_PARAM_NAME = "iLF1";  // first log variant to analyse
    public static final String INPUT_LOGFILE_2_PATH_PARAM_NAME = "iLF2";  // second log variant to analyse
    public static final String P_VALUE_NAME = "pValue";  // p-value treshold for statistical relevance of the results. default: 0.05
    public static final Double DEFAULT_P_VALUE = 0.05;  // p-value treshold for statistical relevance of the results. default: 0.05
    public static final String MEASURE_NAME = "measure";  // measure to use for the comparison, default: "confidence"
    public static final String DEFAULT_MEASURE = "Confidence";  // measure to use for the comparison, default: "confidence"
    public static final String MEASURE_THRESHOLD_NAME = "measureThreshold";  // threshold for the measure to consider it relevant, default: "0.8"
    public static final Double DEFAULT_MEASURE_THRESHOLD = 0.8;  // threshold for the measure to consider it relevant, default: "0.8"
    //      Log managing fom MINERful
    public static final EventClassification DEFAULT_EVENT_CLASSIFICATION = EventClassification.name;
    public static final InputEncoding DEFAULT_INPUT_ENCODING = InputEncoding.xes;
    public static final String INPUT_LOG_1_ENCODING_PARAM_NAME = "iLE1";  // second log variant to analyse
    public static final String INPUT_LOG_2_ENCODING_PARAM_NAME = "iLE2";  // second log variant to analyse
    public static final String EVENT_CLASSIFICATION_PARAM_NAME = "iLClassif";
    public static final String N_PERMUTATIONS_PARAM_NAME = "permutations";

    public enum InputEncoding {
        /**
         * For XES logs (also compressed)
         */
        xes,
        /**
         * For MXML logs (also compressed)
         */
        mxml,
        /**
         * For string-encoded traces, where each character is assumed to be a task symbol
         */
        strings;
    }

    public enum EventClassification {
        name, logspec
    }

    /**
     * file of the first log variant to analyse
     */
    public File inputLogFile1;
    /**
     * file of the second log variant to analyse
     */
    public File inputLogFile2;
    /**
     * Encoding language for the first input event log
     */
    public InputEncoding inputLanguage1;
    /**
     * Encoding language for the second input event log
     */
    public InputEncoding inputLanguage2;
    /**
     * Classification policy to relate events to event classes, that is the task names
     */
    public EventClassification eventClassification;
    /**
     * p-value treshold for statistical relevance of the results. default: 0.05
     */
    public double pValue;
    /**
     * measure to use for the comparison, default: "Confidence"
     */
    public String measure;
    /**
     * threshold for the measure to consider it relevant, default: "0.8"
     */
    public double measureThreshold;
    /**
     * number of permutations to perform, default: 1000
     */
    public int nPermutations;

    public JanusVariantCmdParameters() {
        super();
        this.inputLanguage1 = DEFAULT_INPUT_ENCODING;
        this.inputLanguage2 = DEFAULT_INPUT_ENCODING;
        this.eventClassification = DEFAULT_EVENT_CLASSIFICATION;
        this.inputLogFile1 = null;
        this.inputLogFile2 = null;
        this.pValue = DEFAULT_P_VALUE;
        this.measure = DEFAULT_MEASURE;
        this.measureThreshold = DEFAULT_MEASURE_THRESHOLD;
        this.nPermutations = 1000;
    }

    public JanusVariantCmdParameters(Options options, String[] args) {
        this();
        // parse the command line arguments
        this.parseAndSetup(options, args);
    }

    public JanusVariantCmdParameters(String[] args) {
        this();
        // parse the command line arguments
        this.parseAndSetup(new Options(), args);
    }

    @Override
    protected void setup(CommandLine line) {
        this.inputLogFile1 = openInputFile(line, INPUT_LOGFILE_1_PATH_PARAM_NAME);
        this.inputLogFile2 = openInputFile(line, INPUT_LOGFILE_2_PATH_PARAM_NAME);

        this.inputLanguage1 = InputEncoding.valueOf(
                line.getOptionValue(
                        INPUT_LOG_1_ENCODING_PARAM_NAME,
                        this.inputLanguage1.toString()
                )
        );
        this.inputLanguage2 = InputEncoding.valueOf(
                line.getOptionValue(
                        INPUT_LOG_2_ENCODING_PARAM_NAME,
                        this.inputLanguage2.toString()
                )
        );

        this.eventClassification = EventClassification.valueOf(
                line.getOptionValue(
                        EVENT_CLASSIFICATION_PARAM_NAME,
                        this.eventClassification.toString()
                )
        );

        this.pValue = Double.parseDouble(
                line.getOptionValue(
                        P_VALUE_NAME,
                        Double.toString(this.pValue)
                )
        );

        this.measure = line.getOptionValue(
                MEASURE_NAME,
                DEFAULT_MEASURE
        );

        this.measureThreshold = Double.parseDouble(
                line.getOptionValue(
                        MEASURE_THRESHOLD_NAME,
                        Double.toString(this.measureThreshold)
                )
        );

        this.nPermutations = Integer.parseInt(
                line.getOptionValue(
                        N_PERMUTATIONS_PARAM_NAME,
                        Integer.toString(this.nPermutations)
                )
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
                Option.builder(INPUT_LOGFILE_1_PATH_PARAM_NAME)
                        .hasArg().argName("path")
//                .isRequired(true) // Causing more problems than not
                        .longOpt("in-log-1-file")
                        .desc("path to read the log file from")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(INPUT_LOGFILE_2_PATH_PARAM_NAME)
                        .hasArg().argName("path")
//                .isRequired(true) // Causing more problems than not
                        .longOpt("in-log-2-file")
                        .desc("path to read the log file from")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(INPUT_LOG_1_ENCODING_PARAM_NAME)
                        .hasArg().argName("language")
                        .longOpt("in-log-1-encoding")
                        .desc("input encoding language " + printValues(InputEncoding.values())
                                + printDefault(fromEnumValueToString(DEFAULT_INPUT_ENCODING)))
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(INPUT_LOG_2_ENCODING_PARAM_NAME)
                        .hasArg().argName("language")
                        .longOpt("in-log-2-encoding")
                        .desc("input encoding language " + printValues(InputEncoding.values())
                                + printDefault(fromEnumValueToString(DEFAULT_INPUT_ENCODING)))
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(EVENT_CLASSIFICATION_PARAM_NAME)
                        .hasArg().argName("class")
                        .longOpt("in-log-evt-classifier")
                        .desc("event classification (resp., by activity name, or according to the log-specified pattern) " + printValues(EventClassification.values())
                                + printDefault(fromEnumValueToString(DEFAULT_EVENT_CLASSIFICATION)))
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(MEASURE_NAME)
                        .hasArg().argName("name")
                        .longOpt("measure")
                        .desc("measure to use for the comparison of the variants. default: Confidence")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(MEASURE_THRESHOLD_NAME)
                        .hasArg().argName("number")
                        .longOpt("measure-threshold")
                        .desc("threshold to consider the measure relevant. default: 0.8")
                        .type(Double.class)
                        .build()
        );
        options.addOption(
                Option.builder(P_VALUE_NAME)
                        .hasArg().argName("number")
                        .longOpt("p-value")
                        .desc("p-value threshold for statistical relevance of the results. default: 0.05")
                        .type(Double.class)
                        .build()
        );
        options.addOption(
                Option.builder(N_PERMUTATIONS_PARAM_NAME)
                        .hasArg().argName("number")
                        .longOpt("number-of-permutations")
                        .desc("number of permutations to perform during the statistical test. default: 1000")
                        .type(Double.class)
                        .build()
        );
        return options;
    }
}
