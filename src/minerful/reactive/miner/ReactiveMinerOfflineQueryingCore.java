package minerful.reactive.miner;

import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.miner.params.MinerFulCmdParameters;
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
    public static int[][] runTrace(LogTraceParser logTraceParser, List<SeparatedAutomatonOfflineRunner> automata) {
        int[][] results = new int[automata.size()][2]; // [0]fulfilled activations number, [1] activations number
//        reset automata for a clean run
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            automatonOfflineRunner.reset();
        }

//      retrieve the entire trace
        logTraceParser.init();
        char[] trace = logTraceParser.encodeTrace().toCharArray();

//        evaluate the trace with each constraint (i.e. separated automaton)
        int i = 0;
        byte[] currentAutomatonResults;
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            currentAutomatonResults = new byte[trace.length];
            automatonOfflineRunner.runTrace(trace, trace.length, currentAutomatonResults);
//            truth degree and activations
            int fullfilments = 0;
            int activations = 0;
            for (byte eventEvaluation : currentAutomatonResults) {
                switch (eventEvaluation) {
                    case 2:
                        activations++;
                    case 3:
                        activations++;
                        fullfilments++;
                    default:
                        continue;
                }
            }
            results[i][0] = fullfilments;
            results[i][1] = activations;

            i++;
        }

        return results;
    }

    /**
     * Run a set of separatedAutomata over a full Log
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
        double[] finalResults = new double[automata.size()]; // TODO case length=0

        int currentTraceNumber = 0;
        int[] activeTraces = new int[automata.size()];

        int numberOfTotalTraces = logParser.length();

        Instant start = Instant.now();
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        int samplingInterval = 300;
        System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);

        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            int[][] partialResult = runTrace(tr, automata);
            currentTraceNumber++;

//            if (currentTraceNumber % samplingInterval == 0) {
            System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);  // Status counter "current trace/total trace"
//            }
            for (int i = 0; i < finalResults.length; i++) {
                if (partialResult[i][1] > 0) {
                    finalResults[i] += partialResult[i][0] / partialResult[i][1];
                    activeTraces[i]++;
                }
            }
        }
        System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);
        System.out.println();

        // Support and confidence of each constraint which respect to te log
        for (int i = 0; i < automata.size(); i++) {
            double support = finalResults[i] / currentTraceNumber;
            double confidence = finalResults[i] / activeTraces[i];
            this.bag.getConstraintOfOfflineRunner(automata.get(i)).setSupport(support);
            this.bag.getConstraintOfOfflineRunner(automata.get(i)).setConfidence(confidence);
            this.bag.getConstraintOfOfflineRunner(automata.get(i)).setInterestFactor(confidence);
        }
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
