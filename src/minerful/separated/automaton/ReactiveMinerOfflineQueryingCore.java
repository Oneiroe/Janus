package minerful.separated.automaton;

import com.google.gson.GsonBuilder;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.gson.Gson;

/**
 * Class to manage and organize the run of automata over a Log/Trace
 */
public class ReactiveMinerOfflineQueryingCore implements Callable<ConstraintsBag> {

    protected static Logger logger;
    private final LogParser logParser;
    private final MinerFulCmdParameters minerFulParams;
    private final PostProcessingCmdParameters postPrarams;
    private final TaskCharArchive taskCharArchive; // alphabet
    private final GlobalStatsTable globalStatsTable;
    private final ConstraintsBag bag;  // rules to mine
    private final int jobNum;

    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveMinerOfflineQueryingCore.class.getCanonicalName());
        }
    }

    /**
     * Constructor
     *
     * @param jobNum
     * @param logParser
     * @param minerFulParams
     * @param postPrarams
     * @param taskCharArchive
     * @param globalStatsTable
     * @param bag
     */
    public ReactiveMinerOfflineQueryingCore(int jobNum, LogParser logParser, MinerFulCmdParameters minerFulParams,
                                            PostProcessingCmdParameters postPrarams, TaskCharArchive taskCharArchive,
                                            GlobalStatsTable globalStatsTable, ConstraintsBag bag) {
        this.jobNum = jobNum;
        this.logParser = logParser;
        this.minerFulParams = minerFulParams;
        this.postPrarams = postPrarams;
        this.taskCharArchive = taskCharArchive;
        this.globalStatsTable = globalStatsTable;
        this.bag = bag;
    }

    /**
     * Run a set of separatedAutomata over a single trace
     *
     * @param logTraceParser reader for a trace
     * @param automata       set of separatedAutomata to test over the trace
     * @return boolean matrix with the evaluation in each single event of all the constraints
     */
    public static void runTrace(LogTraceParser logTraceParser, List<SeparatedAutomatonOfflineRunner> automata, byte[][] results) {
//        reset automata for a clean run
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            automatonOfflineRunner.reset();
        }

//      retrieve the entire trace
        logTraceParser.init();
        char[] trace = logTraceParser.encodeTrace().toCharArray();

//        evaluate the trace with each constraint (i.e. separated automaton)
        int i = 0;
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            results[i] = new byte[trace.length];
            automatonOfflineRunner.runTrace(trace, trace.length, results[i++]);
        }

//        return results;
    }

    /**
     * Run a set of separatedAutomata over a full Log
     *
     * About variable finalResult (byte[][][]) bytes meaning:
     * Each byte stores the results of both Activator and target of a given constraint in a specific trace.
     * The left bit is for the activator, the right bit for the target,i.e.,[activator-bit][target-bit]
     * In details:
     *  0 -> 00 -> Activator: False, Target: False
     *  1 -> 01 -> Activator: False, Target: true
     *  2 -> 10 -> Activator: True,  Target: False
     *  3 -> 11 -> Activator: True,  Target: True
     *
     * @param logParser log reader
     * @param automata  set of separatedAutomata to test over the log
     * @return ordered Array of supports for the full log for each automaton
     */
    public void runLog(LogParser logParser, List<SeparatedAutomatonOfflineRunner> automata) {
        byte[][][] finalResults = new byte[logParser.length()][automata.size()][]; // TODO case length=0
        System.out.println("Basic result matrix created! [" + logParser.length() + "][" + automata.size() + "][*]");

        int currentTraceNumber = 0;
        int numberOfTotalTraces = logParser.length();

        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            runTrace(tr, automata, finalResults[currentTraceNumber]);
            currentTraceNumber++;
            System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);  // Status counter "current trace/total trace"
        }
        System.out.println();

//        EXPORT MegaMonster Data Structure to XML/JSON
        long before = System.currentTimeMillis();
        exportEncodedReadable3DMatrixToJson(finalResults, "output.json"); // TODO remove hard-coded output path
        long after = System.currentTimeMillis();
        logger.info("Total JSON serialization time: " + (after - before));

        // Support and confidence of each constraint which respect to te log
        for (int i = 0; i < automata.size(); i++) {
            double support = computeSupport(finalResults, i);
            double confidence = computeConfidence(finalResults, i);
            this.bag.getConstraintOfOfflineRunner(automata.get(i)).setSupport(support);
            this.bag.getConstraintOfOfflineRunner(automata.get(i)).setConfidence(confidence);
            this.bag.getConstraintOfOfflineRunner(automata.get(i)).setInterestFactor(confidence);
        }
    }

    /**
     * Serialize the 3D matrix as-is into a Json file
     *
     * @param dataMatrix
     */
    private void exportRaw3DMatrixToJson(byte[][][] dataMatrix, String outputPath) {
        logger.info("JSON serialization...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fw = new FileWriter(outputPath);
            gson.toJson(dataMatrix, fw);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON serialization...DONE!");

    }

    /**
     * Serialize the 3D matrix into a Json file to have a readable result, but encoded events
     *
     * @param dataMatrix
     */
    private void exportEncodedReadable3DMatrixToJson(byte[][][] dataMatrix, String outputPath) {
        logger.info("JSON encoded readable serialization...");
        try {
            FileWriter fw = new FileWriter(outputPath);
            fw.write("{\n");

            Iterator<LogTraceParser> it = logParser.traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = this.bag.getSeparatedAutomataOfflineRunners();

//        for the entire log
            for (int trace = 0; trace < dataMatrix.length; trace++) {
                LogTraceParser tr = it.next();
                String traceString = tr.encodeTrace();
                fw.write("\t\"" + traceString + "\": [\n");

//              for each trace
                for (int constraint = 0; constraint < dataMatrix[trace].length; constraint++) {

//                  for each constraint
                    String constraintString = automata.get(constraint).toString();
                    fw.write("\t\t{\"" + constraintString + "\": [ ");
                    for (int eventResult = 0; eventResult < dataMatrix[trace][constraint].length; eventResult++) {
                        char event = traceString.charAt(eventResult);
                        byte result = dataMatrix[trace][constraint][eventResult];
                        if (eventResult == (dataMatrix[trace][constraint].length - 1)) {
                            fw.write("{\"" + event + "\": " + result + "}");
                        } else {
                            fw.write("{\"" + event + "\": " + result + "},");
                        }
                    }
                    if (constraint == (dataMatrix[trace].length - 1)) {
                        fw.write(" ]}\n");
                    } else {
                        fw.write(" ]},\n");
                    }

                }

                if (trace == (dataMatrix.length - 1)) {
                    fw.write("\t]\n");
                } else {
                    fw.write("\t],\n");
                }

            }
            fw.write("}");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON encoded readable serialization...DONE!");

    }

    /**
     * Serialize the 3D matrix into a Json file to have a readable result
     *
     * @param dataMatrix
     */
    private void exportReadable3DMatrixToJson(byte[][][] dataMatrix, String outputPath) {
        logger.info("JSON readable serialization...");
        try {
            FileWriter fw = new FileWriter(outputPath);
            fw.write("{\n");

            Iterator<LogTraceParser> it = logParser.traceIterator();
            List<SeparatedAutomatonOfflineRunner> automata = this.bag.getSeparatedAutomataOfflineRunners();

//        for the entire log
            for (int trace = 0; trace < dataMatrix.length; trace++) {
                LogTraceParser tr = it.next();
                tr.init();
                fw.write("\t\"<");
                int i = 0;
                while (i < tr.length()) {
                    String traceString = tr.parseSubsequent().getEvent().getTaskClass().toString();
                    if (i == (tr.length() - 1)) {
                        fw.write(traceString);
                    } else {
                        fw.write(traceString + ",");
                    }
                    i++;
                }
                fw.write(">\": [\n");

//              for each trace
                for (int constraint = 0; constraint < dataMatrix[trace].length; constraint++) {
                    tr.init();
//                  for each constraint
                    String constraintString = automata.get(constraint).toStringDecoded(tr.getLogParser().getTaskCharArchive().getTranslationMapById());

                    fw.write("\t\t{\"" + constraintString + "\": [ ");
                    for (int eventResult = 0; eventResult < dataMatrix[trace][constraint].length; eventResult++) {
                        String event = tr.parseSubsequent().getEvent().getTaskClass().toString();
                        byte result = dataMatrix[trace][constraint][eventResult];
                        if (eventResult == (dataMatrix[trace][constraint].length - 1)) {
                            fw.write("{\"" + event + "\": " + result + "}");
                        } else {
                            fw.write("{\"" + event + "\": " + result + "},");
                        }
                    }
                    if (constraint == (dataMatrix[trace].length - 1)) {
                        fw.write(" ]}\n");
                    } else {
                        fw.write(" ]},\n");
                    }

                }

                if (trace == (dataMatrix.length - 1)) {
                    fw.write("\t]\n");
                } else {
                    fw.write("\t],\n");
                }

            }
            fw.write("}");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("JSON readable serialization...DONE!");

    }


    /**
     * Compute the support measure of a constraint wrt a log
     *
     * @param constraintFinalResult
     * @return
     */
    private double computeSupport(byte[][][] constraintFinalResult, int constraintNumber) {
//        TODO
//        constraintFinalResult[][i][]
        return 0.0;
    }

    /**
     * Compute the confidence measure of a constraint wrt a log
     *
     * @param constraintFinalResult
     * @return
     */
    private double computeConfidence(byte[][][] constraintFinalResult, int constraintNumber) {
//        TODO
//        constraintFinalResult[][i][]
        return 0.0;
    }

    /**
     * Launcher for mining
     *
     * @return
     */
    public ConstraintsBag discover() {
        runLog(this.logParser, this.bag.getSeparatedAutomataOfflineRunners());
        return this.bag;
    }

    @Override
    public ConstraintsBag call() throws Exception {
        return this.discover();
    }
}
