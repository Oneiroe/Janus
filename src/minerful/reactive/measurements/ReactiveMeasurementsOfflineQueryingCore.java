package minerful.reactive.measurements;

import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.reactive.params.JanusMeasurementsCmdParameters;
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
public class ReactiveMeasurementsOfflineQueryingCore implements Callable<MegaMatrixMonster> {

    protected static Logger logger;
    private final LogParser logParser;
    private final JanusMeasurementsCmdParameters janusCheckingParams;
    private final PostProcessingCmdParameters postPrarams;
    private final TaskCharArchive taskCharArchive; // alphabet
    private final GlobalStatsTable globalStatsTable;
    private final ConstraintsBag bag;  // rules to mine
    private final int jobNum;
    private MegaMatrixMonster megaMonster; // £d byte matrix with fine grain result

    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveMeasurementsOfflineQueryingCore.class.getCanonicalName());
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
    public ReactiveMeasurementsOfflineQueryingCore(int jobNum, LogParser logParser, JanusMeasurementsCmdParameters janusCheckingParams,
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
     * Run a set of separatedAutomata over a single trace
     *
     * @param logTraceParser reader for a trace
     * @param automata       set of separatedAutomata to test over the trace
     * @return boolean matrix with the evaluation in each single event of all the constraints
     */
    public static void runTraceLite(LogTraceParser logTraceParser, List<SeparatedAutomatonOfflineRunner> automata, int[][] results) {
//        reset automata for a clean run
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            automatonOfflineRunner.reset();
        }

//      retrieve the entire trace
        logTraceParser.init();
        char[] trace = logTraceParser.encodeTrace().toCharArray();

//        evaluate the trace with each constraint (i.e. separated automaton)
        int i = 0;
        byte[] temp;
        for (SeparatedAutomatonOfflineRunner automatonOfflineRunner : automata) {
            temp = new byte[trace.length];
            automatonOfflineRunner.runTrace(trace, trace.length, temp);
            results[i] = getTraceCounters(temp);
            i++;
        }

    }

    private static int[] getTraceCounters(byte[] reactiveConstraintEvaluation) {
        int[] result = new int[9];
        if (reactiveConstraintEvaluation.length == 0) return result;

        // result { 0: activation, 1: target, 2: no activation, 3: no target}
        // result {4: 00, 5: 01, , 6: 10, 7:11}
        for (byte eval : reactiveConstraintEvaluation) {
            result[0] += eval / 2; // the activator is true if the byte is >1, i.e. 2 or 3
            result[1] += eval % 2; // the target is true if the byte is odd, i,e, 1 or 3
            result[eval + 4]++;
        }
        int l = reactiveConstraintEvaluation.length;
        result[2] = l - result[0];
        result[3] = l - result[1];
        result[8] = l;

        return result;
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
        byte[][][] finalResults = new byte[logParser.length()][automata.size() + 1][]; // TODO case length=0
        logger.info("Basic result matrix created! Size: [" + logParser.length() + "][" + (automata.size() + 1) + "][*]");

        int currentTraceNumber = 0;
        int numberOfTotalTraces = logParser.length();

        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            runTrace(tr, automata, finalResults[currentTraceNumber]);

            // MODEL TRACE EVALUATION
            computeModelTraceEvaluation(finalResults[currentTraceNumber]);

            currentTraceNumber++;
            System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);  // Status counter "current trace/total trace"
        }
        System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);
        System.out.println();

        this.megaMonster = new MegaMatrixMonster(finalResults, this.logParser, this.bag.getSeparatedAutomataOfflineRunners());
    }

    /**
     * Compute the P2P evaluation of the trace joining the results of each separated automaton.
     * <p>
     * The rationale is: select only the activated automata and check if no activated target is violated
     * <p>
     * Evaluation, ie each point i
     * if      10 in at least one i -> 10 : 2
     * elif    11 in at least one i -> 11 : 3
     * elif    01 in at least one i -> 01 : 1
     * else                         -> 00 : 0
     *
     * @param finalResult
     */
    private void computeModelTraceEvaluation(byte[][] finalResult) {
        int traceLen = finalResult[0].length;
        int modelIndex = finalResult.length - 1;
        finalResult[modelIndex] = new byte[traceLen];
        for (int i = 0; i < traceLen; i++) {
            for (int c = 0; c < modelIndex ; c++) {
                if (finalResult[c][i] == 2) {
                    finalResult[modelIndex][i] = 2;
                    break;
                }
                finalResult[modelIndex][i] |= finalResult[c][i];
            }
        }
    }

    /**
     * Run a set of separatedAutomata over a full LogJanusModelCheckLauncher
     * <p>
     * About variable matrixLite (int[][][]) meaning:
     * * compact version of the byte[][][] where instead of saving the result for each event, we keep only what is required for the traces measures computation.
     * * Each int stores the counter of the results of a combination of Activator and target of a given constraint in a specific trace.
     * * In details:
     * * COUNTER INDEX -> Explanation
     * * 0 -> Number of Activator: True [#]
     * * 1 -> Number of Target: True [#]
     * * 2 -> Number of Activator: False
     * * 3 -> Number of Target: False
     * * 4 -> Number of  Activator: False, Target: False
     * * 5 -> Number of  Activator: False, Target: true
     * * 6 -> Number of  Activator: True,  Target: False
     * * 7 -> Number of  Activator: True,  Target: True [#]
     * * 8 -> Trace lenght [#]
     * * <p>
     * * Note. Supposedly only 4 value (marked with #) are enough to derive all the others, but lets try to keep all 9 for now
     *
     * @param logParser log reader
     * @param automata  set of separatedAutomata to test over the log
     * @return ordered Array of supports for the full log for each automaton
     */
    public void runLogLite(LogParser logParser, List<SeparatedAutomatonOfflineRunner> automata) {
        int[][][] finalResults = new int[logParser.length()][automata.size()][9]; // TODO case length=0
        logger.info("Basic result matrix-LITE created! Size: [" + logParser.length() + "][" + automata.size() + "][9]");

        int currentTraceNumber = 0;
        int numberOfTotalTraces = logParser.length();

        Instant start = Instant.now();
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        int samplingInterval = 300;
        int TracesTillSampling = 1;

        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            runTraceLite(tr, automata, finalResults[currentTraceNumber]);
            currentTraceNumber++;

            if (currentTraceNumber != 1 & currentTraceNumber % samplingInterval == 0) {
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
                TracesTillSampling = currentTraceNumber;
            }
            System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);  // Status counter "current trace/total trace"
        }
        System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);
        System.out.println();

        this.megaMonster = new MegaMatrixMonster(finalResults, this.logParser, this.bag.getSeparatedAutomataOfflineRunners());
    }

    /**
     * Launcher for model checking
     *
     * @return
     */
    public MegaMatrixMonster check() {
        if (janusCheckingParams.liteFlag) {
            runLogLite(this.logParser, this.bag.getSeparatedAutomataOfflineRunners());
        } else {
            runLog(this.logParser, this.bag.getSeparatedAutomataOfflineRunners());
        }

        return this.megaMonster;
    }

    @Override
    public MegaMatrixMonster call() throws Exception {
        return this.check();
    }
}
