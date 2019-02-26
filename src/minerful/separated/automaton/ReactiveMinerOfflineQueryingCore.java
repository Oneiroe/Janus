package minerful.separated.automaton;

import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;


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
    public static boolean[][] runTrace(LogTraceParser logTraceParser, List<SeparatedAutomatonOfflineRunner> automata) {
        boolean[][] results = new boolean[automata.size()][logTraceParser.length()];
//        reset automata for a clean run
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            automatonOfflineRunner.reset();
        }

//      retrieve the entire trace
        logTraceParser.init();
        char[] trace = logTraceParser.encodeTrace().toCharArray();
//        char[] trace = new char[logTraceParser.length()];
//        int i = 0;
//        while (!logTraceParser.isParsingOver()) {
////            char transition = logTraceParser.parseSubsequentAndEncode();
//            trace[i++] = logTraceParser.parseSubsequentAndEncode();
//        }

//        evaluate the trace with each constraint (i.e. separated automaton)
        int i = 0;
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            results[i++] = automatonOfflineRunner.runTrace(trace, logTraceParser);
        }

        return results;
    }

    /**
     * Run a set of separatedAutomata over a full Log
     *
     * @param logParser log reader
     * @param automata  set of separatedAutomata to test over the log
     * @return ordered Array of supports for the full log for each automaton
     */
    public void runLog(LogParser logParser, List<SeparatedAutomatonOfflineRunner> automata) {
        boolean[][][] finalResults = new boolean[logParser.length()][automata.size()][]; // TODO case length=0

        int currentTraceNumber = 0;
        int numberOfTotalTraces = logParser.length();

        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            finalResults[currentTraceNumber] = runTrace(tr, automata);
            currentTraceNumber++;
            System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);  // Status counter "current trace/total trace"
        }
        System.out.println();

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
     * Compute the support measure of a constraint wrt a log
     *
     * @param constraintFinalResult
     * @return
     */
    private double computeSupport(boolean[][][] constraintFinalResult, int constraintNumber) {
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
    private double computeConfidence(boolean[][][] constraintFinalResult, int constraintNumber) {
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
