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
        exportToJson(finalResults);

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
     * Serialize the 3D matrix into a Json file
     *
     * @param dataMatrix
     */
    private void exportToJson(byte[][][] dataMatrix) {
//        Gson gson=new Gson();
        System.out.print("JSON serialization...");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            gson.toJson(dataMatrix, new FileWriter("tests-janus-offline/log-offline-test-00.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("DONE!");

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