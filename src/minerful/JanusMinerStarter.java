package minerful;

import minerful.concept.ProcessModel;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharArchive;
import minerful.concept.constraint.ConstraintsBag;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogParser;
import minerful.miner.core.MinerFulKBCore;
import minerful.miner.core.MinerFulPruningCore;
import minerful.miner.core.MinerFulQueryingCore;
import minerful.miner.params.MinerFulCmdParameters;
import minerful.miner.stats.GlobalStatsTable;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.reactive.miner.ReactiveMinerOfflineQueryingCore;
import minerful.reactive.miner.ReactiveMinerPruningCore;
import minerful.reactive.miner.ReactiveMinerQueryingCore;
import minerful.utils.MessagePrinter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Start Janus-classic (online ready) mining from command line
 */
public class JanusMinerStarter extends AbstractMinerFulStarter {
	protected static final String PROCESS_MODEL_NAME_PATTERN = "Process model discovered from %s";
	protected static final String DEFAULT_ANONYMOUS_MODEL_NAME = "Discovered process model";
	private static MessagePrinter logger = MessagePrinter.getInstance(JanusMinerStarter.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options minerfulOptions = MinerFulCmdParameters.parseableOptions(),
				inputOptions = InputLogCmdParameters.parseableOptions(),
				systemOptions = SystemCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions(),
				outputOptions = OutputModelParameters.parseableOptions(),
				postProptions = PostProcessingCmdParameters.parseableOptions();
		
    	for (Object opt: postProptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: minerfulOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: outputOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
		
		return cmdLineOptions;
	}

	/**
	 * @param args
	 *            the command line arguments: [regular expression] [number of
	 *            strings] [minimum number of characters per string] [maximum
	 *            number of characters per string] [alphabet]...
	 */
	public static void main(String[] args) {
		JanusMinerStarter minerMinaStarter = new JanusMinerStarter();
		Options cmdLineOptions = minerMinaStarter.setupOptions();

		InputLogCmdParameters inputParams =
				new InputLogCmdParameters(
						cmdLineOptions,
						args);
		MinerFulCmdParameters minerFulParams =
				new MinerFulCmdParameters(
						cmdLineOptions,
						args);
		ViewCmdParameters viewParams =
				new ViewCmdParameters(
						cmdLineOptions,
						args);
		OutputModelParameters outParams =
				new OutputModelParameters(
						cmdLineOptions,
						args);
		SystemCmdParameters systemParams =
				new SystemCmdParameters(
						cmdLineOptions,
						args);
		PostProcessingCmdParameters postParams =
				new PostProcessingCmdParameters(
						cmdLineOptions,
						args);

		if (systemParams.help) {
			systemParams.printHelp(cmdLineOptions);
			System.exit(0);
		}
		if (!isEventLogGiven(cmdLineOptions, inputParams, systemParams)) {
			System.exit(1);
		}

		MessagePrinter.configureLogging(systemParams.debugLevel);

		logger.info("Loading log...");

		LogParser logParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(
				inputParams,
				minerFulParams);

		TaskCharArchive taskCharArchive = logParser.getTaskCharArchive();

		ProcessModel processModel = minerMinaStarter.mine(logParser, inputParams, minerFulParams, postParams, taskCharArchive);

		new MinerFulOutputManagementLauncher().manageOutput(processModel, viewParams, outParams, systemParams, logParser);
	}

	public static boolean isEventLogGiven(Options cmdLineOptions, InputLogCmdParameters inputParams,
			SystemCmdParameters systemParams) {
		if (inputParams.inputLogFile == null) {
			systemParams.printHelpForWrongUsage("Input log file missing! Please use the " +
					InputLogCmdParameters.INPUT_LOGFILE_PATH_PARAM_NAME +
					" option.",
					cmdLineOptions);
			return false;
		}
		return true;
	}

	public ProcessModel mine(LogParser logParser,
			MinerFulCmdParameters minerFulParams,
			PostProcessingCmdParameters postParams, Character[] alphabet) {
		return this.mine(logParser, null, minerFulParams, postParams, alphabet);
	}

	public ProcessModel mine(LogParser logParser,
			InputLogCmdParameters inputParams, MinerFulCmdParameters minerFulParams,
			PostProcessingCmdParameters postParams, Character[] alphabet) {
		TaskCharArchive taskCharArchive = new TaskCharArchive(alphabet);
		return this.mine(logParser, inputParams, minerFulParams, postParams, taskCharArchive);
	}

	public ProcessModel mine(LogParser logParser,
			MinerFulCmdParameters minerFulParams, 
			PostProcessingCmdParameters postParams, TaskCharArchive taskCharArchive) {
		return this.mine(logParser, null, minerFulParams, postParams, taskCharArchive);
	}

	public ProcessModel mine(LogParser logParser,
			InputLogCmdParameters inputParams, MinerFulCmdParameters minerFulParams, PostProcessingCmdParameters postParams, TaskCharArchive taskCharArchive) {
//		GlobalStatsTable globalStatsTable = new GlobalStatsTable(taskCharArchive, minerFulParams.branchingLimit);
//		globalStatsTable = computeKB(logParser, minerFulParams,
//				taskCharArchive, globalStatsTable);

		System.gc();

		ProcessModel proMod = ProcessModel.generateNonEvaluatedBinaryModel(taskCharArchive);
		
		proMod.setName(makeDiscoveredProcessName(inputParams));

		/* Substitution of mining core with the Janus reactiveMiner */
//      proMod.bag = queryForConstraints(logParser, minerFulParams, postParams, taskCharArchive, globalStatsTable, proMod.bag); // MINERful
		proMod.bag = reactiveQueryForConstraints(logParser, minerFulParams, postParams, taskCharArchive, null, proMod.bag);
//        proMod.bag = reactiveOfflineQueryForConstraints(logParser, minerFulParams, postParams, taskCharArchive, globalStatsTable, proMod.bag);

		System.gc();

		/* TODO take back the full post processing and adapt it to the separation technique*/
//		pruneConstraints(proMod, minerFulParams, postParams);
		new ReactiveMinerPruningCore(proMod,  minerFulParams, postParams).pruneNonActiveConstraints();
		return proMod;
	}

	public static String makeDiscoveredProcessName(InputLogCmdParameters inputParams) {
		return (inputParams != null && inputParams.inputLogFile != null ) ?
			String.format(JanusMinerStarter.PROCESS_MODEL_NAME_PATTERN, inputParams.inputLogFile.getName()) :
				DEFAULT_ANONYMOUS_MODEL_NAME;
	}

	protected GlobalStatsTable computeKB(LogParser logParser,
			MinerFulCmdParameters minerFulParams,
			TaskCharArchive taskCharArchive, GlobalStatsTable globalStatsTable) {
		int coreNum = 0;
		long before = 0, after = 0;
		if (minerFulParams.isParallelKbComputationRequired()) {
			// Slice the log
			List<LogParser> listOfLogParsers = logParser
					.split(minerFulParams.kbParallelProcessingThreads);
			List<MinerFulKBCore> listOfMinerFulCores = new ArrayList<MinerFulKBCore>(
					minerFulParams.kbParallelProcessingThreads);

			// Associate a dedicated KB-computing core to each log slice
			for (LogParser slicedLogParser : listOfLogParsers) {
				listOfMinerFulCores.add(new MinerFulKBCore(
						coreNum++,
						slicedLogParser,
						minerFulParams, taskCharArchive));
			}

			ExecutorService executor = Executors
					.newFixedThreadPool(minerFulParams.kbParallelProcessingThreads);

//			ForkJoinPool executor = new ForkJoinPool(minerFulParams.kbParallelProcessingThreads);

			try {
				before = System.currentTimeMillis();
				for (Future<GlobalStatsTable> statsTab : executor
						.invokeAll(listOfMinerFulCores)) {
					globalStatsTable.mergeAdditively(statsTab.get());
				}
				after = System.currentTimeMillis();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				System.exit(1);
			}
			executor.shutdown();
		} else {
			MinerFulKBCore minerFulKbCore = new MinerFulKBCore(
					coreNum++,
					logParser,
					minerFulParams, taskCharArchive);
			before = System.currentTimeMillis();
			globalStatsTable = minerFulKbCore.discover();
			after = System.currentTimeMillis();
		}
		logger.info("Total KB construction time: " + (after - before));
		return globalStatsTable;
	}

	private ConstraintsBag reactiveQueryForConstraints(
			LogParser logParser, MinerFulCmdParameters minerFulParams,
			PostProcessingCmdParameters postPrarams, TaskCharArchive taskCharArchive,
			GlobalStatsTable globalStatsTable, ConstraintsBag bag) {
		int coreNum = 0;
		long before, after = 0;
		if (minerFulParams.isParallelQueryProcessingRequired() && minerFulParams.isBranchingRequired()) {
			logger.warn("Parallel querying of branched constraints not yet implemented. Proceeding with the single-core operations...");
		}

		/* JReactiveMiner Querying Core
		 *
		 * @author Alessio
		 * */
		ReactiveMinerQueryingCore minerFulQueryingCore = new ReactiveMinerQueryingCore(coreNum++,
				logParser, minerFulParams, postPrarams, taskCharArchive,
				globalStatsTable, bag);
		before = System.currentTimeMillis();
		minerFulQueryingCore.discover();
		after = System.currentTimeMillis();

		logger.info("Total KB querying time: " + (after - before));
		return bag;
	}

}