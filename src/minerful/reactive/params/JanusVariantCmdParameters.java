package minerful.reactive.params;

import minerful.io.params.InputModelParameters;
import minerful.params.ParamsManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;

public class JanusVariantCmdParameters extends ParamsManager {
    //      Variant specific
    public static final String INPUT_LOGFILE_1_PATH_PARAM_NAME = "iLF1";  // first log variant to analyse
    public static final String INPUT_LOGFILE_2_PATH_PARAM_NAME = "iLF2";  // second log variant to analyse
    public static final String P_VALUE_NAME = "pValue";  // p-value treshold for statistical relevance of the results. default: 0.01
    public static final Double DEFAULT_P_VALUE = 0.01;  // p-value treshold for statistical relevance of the results. default: 0.01
    public static final String MEASURE_NAME = "measure";  // measure to use for the comparison, default: "confidence"
    public static final String DEFAULT_MEASURE = "Confidence";  // measure to use for the comparison, default: "confidence"
    public static final String MEASURE_THRESHOLD_NAME = "measureThreshold";  // threshold for the measure to consider it relevant, default: "0.8"
    public static final Double DEFAULT_MEASURE_THRESHOLD = 0.0;  // threshold for the measure to consider it relevant, default: "0.0"
    public static final String DIFFERENCE_THRESHOLD_NAME = "differenceThreshold";  // threshold for the difference of the variants constraints measurement to be considered relevant. default= 0.01
    public static final Double DEFAULT_DIFFERENCE_THRESHOLD = 0.01;  // threshold for the measure to consider it relevant, default: "0.8"
    public static final String SIMPLIFICATION_FLAG = "simplify";  // flag to simplify the result rules list according to their hierarchy
    public static final String BEST_N_RESULTS_NAME = "bestNresults";  // number of rules in the TOP result list. default= 10
    public static final Integer DEFAULT_BEST_N_RESULTS_VALUE = 10;  // number of rules in the TOP result list. default= 10
    //      Log managing fom MINERful
    public static final EventClassification DEFAULT_EVENT_CLASSIFICATION = EventClassification.name;
    public static final LogInputEncoding DEFAULT_INPUT_LOG_ENCODING = LogInputEncoding.xes;
    public static final String INPUT_LOG_1_ENCODING_PARAM_NAME = "iLE1";  // second log variant to analyse
    public static final String INPUT_LOG_2_ENCODING_PARAM_NAME = "iLE2";  // second log variant to analyse
    public static final String EVENT_CLASSIFICATION_PARAM_NAME = "iLClassif";
    public static final String N_PERMUTATIONS_PARAM_NAME = "permutations";
    public static final String OUTPUT_FILE_CSV_PARAM_NAME = "oCSV";
    public static final String OUTPUT_FILE_JSON_PARAM_NAME = "oJSON";
    public static final String OUTPUT_KEEP_FLAG_NAME = "oKeep";
    public static final String SAVE_MODEL_1_AS_CSV_PARAM_NAME = "oModel1CSV";
    public static final String SAVE_MODEL_2_AS_CSV_PARAM_NAME = "oModel2CSV";
    public static final String SAVE_MODEL_1_AS_JSON_PARAM_NAME = "oModel1JSON";
    public static final String SAVE_MODEL_2_AS_JSON_PARAM_NAME = "oModel2JSON";
    public static final String ENCODE_OUTPUT_TASKS_FLAG = "encodeTasksFlag";
    public static final String INPUT_MODELFILE_1_PATH_PARAM_NAME = "iMF1";
    public static final String INPUT_MODEL_ENCODING_1_PARAM_NAME = "iME1";
    public static final String INPUT_MODELFILE_2_PATH_PARAM_NAME = "iMF2";
    public static final String INPUT_MODEL_ENCODING_2_PARAM_NAME = "iME2";
    public static final InputModelParameters.InputEncoding DEFAULT_INPUT_MODEL_ENCODING = InputModelParameters.InputEncoding.JSON;


    public enum LogInputEncoding {
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
    public LogInputEncoding inputLogLanguage1;
    /**
     * Encoding language for the second input event log
     */
    public LogInputEncoding inputLogLanguage2;
    /**
     * file of the process model for the first log variant to analyse
     */
    public File inputModelFile1;
    /**
     * file of the process model for the second log variant to analyse
     */
    public File inputModelFile2;
    /**
     * Encoding language for the first input model
     */
    public InputModelParameters.InputEncoding inputModelLanguage1;
    /**
     * Encoding language for the second input model
     */
    public InputModelParameters.InputEncoding inputModelLanguage2;
    /**
     * Classification policy to relate events to event classes, that is the task names
     */
    public EventClassification eventClassification;
    /**
     * p-value treshold for statistical relevance of the results. default: 0.01
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
     * threshold for the difference of the variants constraints measurement to be considered relevant, default: "0.01"
     */
    public double differenceThreshold;
    /**
     * number of permutations to perform, default: 1000
     */
    public int nPermutations;
    /**
     * output file in CSV format
     */
    public File outputCvsFile;
    /**
     * output file in JSON format
     */
    public File outputJsonFile;
    /**
     * keep the irrelevant results in output
     */
    public boolean oKeep;
    /**
     * File in which discovered constraints for variant 1 are printed in CSV format. Keep it equal to <code>null</code> for avoiding such print-out.
     */
    public File fileToSaveModel1AsCSV;
    /**
     * File in which discovered constraints for variant 2 are printed in CSV format. Keep it equal to <code>null</code> for avoiding such print-out.
     */
    public File fileToSaveModel2AsCSV;
    /**
     * File in which the discovered process model for variant 1 is saved as a JSON file. Keep it equal to <code>null</code> for avoiding such print-out.
     */
    public File fileToSaveModel1AsJSON;
    /**
     * File in which the discovered process model for variant 2 is saved as a JSON file. Keep it equal to <code>null</code> for avoiding such print-out.
     */
    public File fileToSaveModel2AsJSON;
    /**
     * Flag if the output tasks/events should be encoded (e.g., A B C D E...) or not (original names as in log)
     **/
    public boolean encodeOutputTasks;
    /**
     * Flag if the rules set returned from the permutation test should be simplified according to the rules hierarchy. Default=false
     **/
    public boolean simplify;
    /**
     * Number of rules for the TOP results. Default=10
     **/
    public int bestNresults;

    public JanusVariantCmdParameters() {
        super();
        this.inputLogLanguage1 = DEFAULT_INPUT_LOG_ENCODING;
        this.inputLogLanguage2 = DEFAULT_INPUT_LOG_ENCODING;
        this.inputModelLanguage1 = DEFAULT_INPUT_MODEL_ENCODING;
        this.inputModelLanguage2 = DEFAULT_INPUT_MODEL_ENCODING;
        this.eventClassification = DEFAULT_EVENT_CLASSIFICATION;
        this.inputLogFile1 = null;
        this.inputLogFile2 = null;
        this.inputModelFile1 = null;
        this.inputModelFile2 = null;
        this.pValue = DEFAULT_P_VALUE;
        this.measure = DEFAULT_MEASURE;
        this.measureThreshold = DEFAULT_MEASURE_THRESHOLD;
        this.nPermutations = 1000;
        this.outputCvsFile = null;
        this.outputJsonFile = null;
        this.oKeep = false;
        this.fileToSaveModel1AsCSV = null;
        this.fileToSaveModel2AsCSV = null;
        this.fileToSaveModel1AsJSON = null;
        this.fileToSaveModel2AsJSON = null;
        this.encodeOutputTasks = false;
        this.simplify = false;
        this.differenceThreshold = DEFAULT_DIFFERENCE_THRESHOLD;
        this.bestNresults = DEFAULT_BEST_N_RESULTS_VALUE;
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

        this.inputLogLanguage1 = LogInputEncoding.valueOf(
                line.getOptionValue(
                        INPUT_LOG_1_ENCODING_PARAM_NAME,
                        this.inputLogLanguage1.toString()
                )
        );
        this.inputLogLanguage2 = LogInputEncoding.valueOf(
                line.getOptionValue(
                        INPUT_LOG_2_ENCODING_PARAM_NAME,
                        this.inputLogLanguage2.toString()
                )
        );

        this.eventClassification = EventClassification.valueOf(
                line.getOptionValue(
                        EVENT_CLASSIFICATION_PARAM_NAME,
                        this.eventClassification.toString()
                )
        );


        this.inputModelFile1 = openInputFile(line, INPUT_MODELFILE_1_PATH_PARAM_NAME);
        this.inputModelFile2 = openInputFile(line, INPUT_MODELFILE_2_PATH_PARAM_NAME);

        this.inputModelLanguage1 = InputModelParameters.InputEncoding.valueOf(
                line.getOptionValue(
                        INPUT_MODEL_ENCODING_1_PARAM_NAME,
                        this.inputModelLanguage1.toString()
                )
        );
        this.inputModelLanguage2 = InputModelParameters.InputEncoding.valueOf(
                line.getOptionValue(
                        INPUT_MODEL_ENCODING_2_PARAM_NAME,
                        this.inputModelLanguage2.toString()
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

        this.differenceThreshold = Double.parseDouble(
                line.getOptionValue(
                        DIFFERENCE_THRESHOLD_NAME,
                        Double.toString(this.differenceThreshold)
                )
        );

        this.nPermutations = Integer.parseInt(
                line.getOptionValue(
                        N_PERMUTATIONS_PARAM_NAME,
                        Integer.toString(this.nPermutations)
                )
        );
        this.outputCvsFile = openOutputFile(line, OUTPUT_FILE_CSV_PARAM_NAME);
        this.outputJsonFile = openOutputFile(line, OUTPUT_FILE_JSON_PARAM_NAME);
        this.oKeep = line.hasOption(OUTPUT_KEEP_FLAG_NAME);
        this.inputLogFile1 = openInputFile(line, INPUT_LOGFILE_1_PATH_PARAM_NAME);
        this.inputLogFile2 = openInputFile(line, INPUT_LOGFILE_2_PATH_PARAM_NAME);

        this.fileToSaveModel1AsCSV = openOutputFile(line, SAVE_MODEL_1_AS_CSV_PARAM_NAME);
        this.fileToSaveModel2AsCSV = openOutputFile(line, SAVE_MODEL_2_AS_CSV_PARAM_NAME);
        this.fileToSaveModel1AsJSON = openOutputFile(line, SAVE_MODEL_1_AS_JSON_PARAM_NAME);
        this.fileToSaveModel2AsJSON = openOutputFile(line, SAVE_MODEL_2_AS_JSON_PARAM_NAME);
        this.encodeOutputTasks = line.hasOption(OUTPUT_KEEP_FLAG_NAME);
        this.simplify = line.hasOption(SIMPLIFICATION_FLAG);

        this.bestNresults = Integer.parseInt(
                line.getOptionValue(
                        BEST_N_RESULTS_NAME,
                        Integer.toString(this.bestNresults)
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
                        .desc("input encoding language " + printValues(LogInputEncoding.values())
                                + printDefault(fromEnumValueToString(DEFAULT_INPUT_LOG_ENCODING)))
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(INPUT_LOG_2_ENCODING_PARAM_NAME)
                        .hasArg().argName("language")
                        .longOpt("in-log-2-encoding")
                        .desc("input encoding language " + printValues(LogInputEncoding.values())
                                + printDefault(fromEnumValueToString(DEFAULT_INPUT_LOG_ENCODING)))
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
                Option.builder(INPUT_MODELFILE_1_PATH_PARAM_NAME)
                        .hasArg().argName("path")
//                .isRequired(true) // Causing more problems than not
                        .longOpt("in-model-1-file")
                        .desc("path to read the input model file from")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(INPUT_MODELFILE_2_PATH_PARAM_NAME)
                        .hasArg().argName("path")
//                .isRequired(true) // Causing more problems than not
                        .longOpt("in-model-2-file")
                        .desc("path to read the input model file from")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(INPUT_MODEL_ENCODING_1_PARAM_NAME)
                        .hasArg().argName("language")
                        .longOpt("in-model-1-encoding")
                        .desc("input encoding language " + printValues(InputModelParameters.InputEncoding.values())
                                + printDefault(fromEnumValueToString(DEFAULT_INPUT_MODEL_ENCODING)))
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(INPUT_MODEL_ENCODING_2_PARAM_NAME)
                        .hasArg().argName("language")
                        .longOpt("in-model-2-encoding")
                        .desc("input encoding language " + printValues(InputModelParameters.InputEncoding.values())
                                + printDefault(fromEnumValueToString(DEFAULT_INPUT_MODEL_ENCODING)))
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
                        .desc("threshold to consider the measure relevant. default: 0.0")
                        .type(Double.class)
                        .build()
        );
        options.addOption(
                Option.builder(DIFFERENCE_THRESHOLD_NAME)
                        .hasArg().argName("number")
                        .longOpt("difference-threshold")
                        .desc("threshold for the difference of the variants constraints measurement to be considered relevant, default: 0.01")
                        .type(Double.class)
                        .build()
        );
        options.addOption(
                Option.builder(P_VALUE_NAME)
                        .hasArg().argName("number")
                        .longOpt("p-value")
                        .desc("p-value threshold for statistical relevance of the results. default: 0.01")
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
        options.addOption(
                Option.builder(OUTPUT_FILE_CSV_PARAM_NAME)
                        .hasArg().argName("path")
//                .isRequired(true) // Causing more problems than not
                        .longOpt("out-csv-file")
                        .desc("path to output CSV file")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(OUTPUT_FILE_JSON_PARAM_NAME)
                        .hasArg().argName("path")
//                .isRequired(true) // Causing more problems than not
                        .longOpt("out-json-file")
                        .desc("path to output JSON file")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(OUTPUT_KEEP_FLAG_NAME)
//                .isRequired(true) // Causing more problems than not
                        .longOpt("output-keep")
                        .desc("keep irrelevant results in output")
                        .type(Boolean.class)
                        .build()
        );
        options.addOption(
                Option.builder(SAVE_MODEL_1_AS_CSV_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-model-1-as-csv")
                        .desc("print discovered model 1 in CSV format into the specified file")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(SAVE_MODEL_2_AS_CSV_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-model-2-as-csv")
                        .desc("print discovered model 2 in CSV format into the specified file")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(SAVE_MODEL_1_AS_JSON_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-model-1-as-json")
                        .desc("print discovered model 1 in JSON format into the specified file")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(SAVE_MODEL_2_AS_JSON_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-model-2-as-json")
                        .desc("print discovered model 2 in JSON format into the specified file")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(ENCODE_OUTPUT_TASKS_FLAG)
//                .isRequired(true) // Causing more problems than not
                        .longOpt("flag-encoding-tasks")
                        .desc("Flag if the output tasks/events should be encoded")
                        .type(Boolean.class)
                        .build()
        );
        options.addOption(
                Option.builder(SIMPLIFICATION_FLAG)
//                .isRequired(true) // Causing more problems than not
                        .longOpt("simplification-flag")
                        .desc("Flag if the output rules set shoul dbe simplified according to rules hierarchy. Default: false")
                        .type(Boolean.class)
                        .build()
        );
        options.addOption(
                Option.builder(BEST_N_RESULTS_NAME)
                        .hasArg().argName("number")
                        .longOpt("number-of-best-results")
                        .desc("Number of rules to return in among the best results. Default: 10")
                        .type(Integer.class)
                        .build()
        );
        return options;
    }
}
