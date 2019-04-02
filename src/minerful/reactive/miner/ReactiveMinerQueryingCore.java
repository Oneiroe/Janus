package minerful.reactive.miner;

import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.reactive.automaton.SeparatedAutomatonRunner;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
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
	 * @return ordered Array of interestingness Degree for the trace for each automaton
	 */
	public static double[] runTrace(LogTraceParser logTraceParser, List<SeparatedAutomatonRunner> automata) {
		double[] results = new double[automata.size()];
//        reset automata for a clean run
		for (SeparatedAutomatonRunner automatonRunner : automata) {
			automatonRunner.reset();
		}
//        Step by step run of the automata
		logTraceParser.init();
		while (!logTraceParser.isParsingOver()) {
			char transition = logTraceParser.parseSubsequentAndEncode();
			for (SeparatedAutomatonRunner automatonRunner : automata) {
				automatonRunner.step(transition);
			}
		}

//        Retrieve result
		int i = 0;
		for (SeparatedAutomatonRunner automatonRunner : automata) {
			results[i] = automatonRunner.getDegreeOfTruth();
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
	public void runLog(LogParser logParser, List<SeparatedAutomatonRunner> automata) {
		double[] finalResults = new double[automata.size()]; // TODO case length=0

		int currentTraceNumber = 0;
		int[] activeTraces = new int[automata.size()];

		int numberOfTotalTraces = logParser.length();

		for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
			LogTraceParser tr = it.next();
			double[] partialResults = runTrace(tr, automata);
			currentTraceNumber++;
			System.out.print("\rTraces: " + currentTraceNumber + "/" + numberOfTotalTraces);  // Status counter "current trace/total trace"
			for (int i = 0; i < finalResults.length; i++) {
				finalResults[i] += partialResults[i];
				if (automata.get(i).isActivated()) activeTraces[i]++;
			}
		}
        System.out.println();

		// Support and confidence of each constraint which respect to te log
		for (int i = 0; i < finalResults.length; i++) {
			double support = finalResults[i] / currentTraceNumber;
			double confidence = finalResults[i] / activeTraces[i];
			this.bag.getConstraintOfRunner(automata.get(i)).setSupport(support);
			this.bag.getConstraintOfRunner(automata.get(i)).setConfidence(confidence);
			this.bag.getConstraintOfRunner(automata.get(i)).setInterestFactor(confidence);
		}
	}

	/**
	 * Launcher for mining
	 *
	 * @return
	 */
	public ConstraintsBag discover() {
		runLog(this.logParser, this.bag.getSeparatedAutomataRunners());
		return this.bag;
	}

	@Override
	public ConstraintsBag call() throws Exception {
		return this.discover();
	}
}
