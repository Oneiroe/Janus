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
import java.util.Set;
import java.util.concurrent.Callable;


/**
 * Class to manage and organize the run of automata over a Log/Trace
 */
public class ReactiveMinerQueryingCore implements Callable<ConstraintsBag> {

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
			logger = Logger.getLogger(ReactiveMinerQueryingCore.class.getCanonicalName());
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
	public ReactiveMinerQueryingCore(int jobNum, LogParser logParser, MinerFulCmdParameters minerFulParams,
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
	 * @return ordered Array of supports for the trace for each automaton
	 */
	public static double[] runTrace(LogTraceParser logTraceParser, List<SeparatedAutomatonRunner> automata) {
		double[] results = new double[automata.size()];
//        reset automata for a clean run
		for (SeparatedAutomatonRunner automatonRunner : automata) {
			automatonRunner.reset();
		}

//        Step by step run of the automata
		while (!logTraceParser.isParsingOver()){
			char transition = logTraceParser.parseSubsequentAndEncode();
			for (SeparatedAutomatonRunner automatonRunner : automata) {
				automatonRunner.step(transition);
			}
		}

//        Retrieve result
		int i = 0;
		for (SeparatedAutomatonRunner automatonRunner : automata) {
			results[i] = automatonRunner.getSupport();
			i++;
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
	public static double[] runLog(LogParser logParser, List<SeparatedAutomatonRunner> automata) {
		double[] finalResults = new double[automata.size()]; // TODO case length=0
		double[] finalConidences = new double[automata.size()]; // TODO
		int numberOfTraces = 0;
		for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
			LogTraceParser tr = it.next();
			numberOfTraces++;
			double[] partialResults = runTrace(tr, automata);
			for (int i = 0; i < finalResults.length; i++) {
				finalResults[i] += partialResults[i];
			}
		}

		// Support of each constraint which respect to te log
		for (int i = 0; i < finalResults.length; i++) {
			finalResults[i] = finalResults[i] / numberOfTraces;
			logger.info(automata.get(i).toString() + " = " + finalResults[i]);
		}

		return finalResults;
	}

	/**
	 * Launcher for mining
	 * @return
	 */
	public ConstraintsBag discover() {
		runLog(this.logParser, this.bag.getSeparatedAutomataRunners());
		return null;
	}

	@Override
	public ConstraintsBag call() throws Exception {
		return this.discover();
	}
}
