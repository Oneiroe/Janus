package minerful.reactive.checking;

import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.reactive.params.JanusCheckingCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import org.apache.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class to manage and organize the run of automata over a Log/Trace
 */
public class ReactiveCheckingOfflineQueryingCore implements Callable<MegaMatrixMonster> {

    protected static Logger logger;
    private final LogParser logParser;
    private final JanusCheckingCmdParameters janusCheckingParams;
    private final PostProcessingCmdParameters postPrarams;
    private final TaskCharArchive taskCharArchive; // alphabet
    private final GlobalStatsTable globalStatsTable;
    private final ConstraintsBag bag;  // rules to mine
    private final int jobNum;
    private MegaMatrixMonster megaMonster; // £d byte matrix with fine grain result

    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveCheckingOfflineQueryingCore.class.getCanonicalName());
        }
    }

    /**
     * Constructor
     *
     * @param jobNum
     * @param logParser
     * @param janusCheckingParams
     * @param postPrarams
     * @param taskCharArchive
     * @param globalStatsTable
     * @param bag
     */
    public ReactiveCheckingOfflineQueryingCore(int jobNum, LogParser logParser, JanusCheckingCmdParameters janusCheckingParams,
                                               PostProcessingCmdParameters postPrarams, TaskCharArchive taskCharArchive,
                                               GlobalStatsTable globalStatsTable, ConstraintsBag bag) {
        this.jobNum = jobNum;
        this.logParser = logParser;
        this.janusCheckingParams = janusCheckingParams;
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

    }

    /**
     * Run a set of separatedAutomata over a full LogJanusModelCheckLauncher
     * <p>
     * About variable finalResult (byte[][][]) bytes meaning:
     * Each byte stores the results of both Activator and target of a given constraint in a specific trace.
     * The left bit is for the activator, the right bit for the target,i.e.,[activator-bit][target-bit]
     * In details:
     * 0 -> 00 -> Activator: False, Target: False
     * 1 -> 01 -> Activator: False, Target: true
     * 2 -> 10 -> Activator: True,  Target: False
     * 3 -> 11 -> Activator: True,  Target: True
     *
     * @param logParser log reader
     * @param automata  set of separatedAutomata to test over the log
     * @return ordered Array of supports for the full log for each automaton
     */
    public void runLog(LogParser logParser, List<SeparatedAutomatonOfflineRunner> automata) {
        byte[][][] finalResults = new byte[logParser.length()][automata.size()][]; // TODO case length=0
        logger.info("Basic result matrix created! Size: [" + logParser.length() + "][" + automata.size() + "][*]");

        int currentTraceNumber = 0;
        int numberOfTotalTraces = logParser.length();

        Instant start = Instant.now();
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        int samplingInterval = 300;
        int TracesTillSampling = 1;

        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            runTrace(tr, automata, finalResults[currentTraceNumber]);
            currentTraceNumber++;

            if (currentTraceNumber != 1 & currentTraceNumber % samplingInterval == 0) {
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
                TracesTillSampling = currentTraceNumber;
            }
            System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces +
                    " [ estimated time left: " + Duration.ofMillis((numberOfTotalTraces - currentTraceNumber) / TracesTillSampling * timeElapsed.toMillis()) + " ] ");  // Status counter "current trace/total trace"
        }
        System.out.println();

        this.megaMonster = new MegaMatrixMonster(finalResults, this.logParser, this.bag.getSeparatedAutomataOfflineRunners());
    }

    /**
     * Launcher for model checking
     *
     * @return
     */
    public MegaMatrixMonster check() {
        runLog(this.logParser, this.bag.getSeparatedAutomataOfflineRunners());
        return this.megaMonster;
    }

    @Override
    public MegaMatrixMonster call() throws Exception {
        return this.check();
    }
}
