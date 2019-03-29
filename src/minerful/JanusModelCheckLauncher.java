package minerful;

import minerful.checking.params.CheckingCmdParameters;
import minerful.concept.ProcessModel;
import minerful.io.ProcessModelLoader;
import minerful.io.params.InputModelParameters;
import minerful.logparser.LogParser;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.reactive.checking.MegaMatrixMonster;
import minerful.reactive.miner.ReactiveMinerOfflineQueryingCore;
import minerful.utils.MessagePrinter;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

/**
 * Class for launching JanusZ model checker
 */
public class JanusModelCheckLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(JanusModelCheckLauncher.class);

	private ProcessModel processSpecification;
	private LogParser eventLog;
	private CheckingCmdParameters chkParams;

	private JanusModelCheckLauncher(CheckingCmdParameters chkParams) {
		this.chkParams = chkParams;
	}

	public JanusModelCheckLauncher(AssignmentModel declareMapModel, LogParser inputLog, CheckingCmdParameters chkParams) {
		this(chkParams);
		this.processSpecification = new ProcessModelLoader().loadProcessModel(declareMapModel);
		this.eventLog = inputLog;
	}

	public JanusModelCheckLauncher(ProcessModel minerFulProcessModel, LogParser inputLog, CheckingCmdParameters chkParams) {
		this(chkParams);
		this.processSpecification = minerFulProcessModel;
		this.eventLog = inputLog;
	}

	public JanusModelCheckLauncher(InputModelParameters inputParams, PostProcessingCmdParameters preProcParams,
								   InputLogCmdParameters inputLogParams, CheckingCmdParameters chkParams, SystemCmdParameters systemParams) {
		this(chkParams);

		if (inputParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process model file missing!");
			System.exit(1);
		}
		this.eventLog = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputLogParams);

		// Load the process specification from the file
		this.processSpecification =
				new ProcessModelLoader().loadProcessModel(inputParams.inputLanguage, inputParams.inputFile, this.eventLog.getTaskCharArchive());
		// Apply some preliminary pruning
//		MinerFulPruningCore pruniCore = new MinerFulPruningCore(this.processSpecification, preProcParams);
//		this.processSpecification.bag = pruniCore.massageConstraints();

		MessagePrinter.configureLogging(systemParams.debugLevel);
	}

	public ProcessModel getProcessSpecification() {
		return processSpecification;
	}

	public LogParser getEventLog() {
		return eventLog;
	}

	/**
	 * Check the input model against the input log.
	 */
	public MegaMatrixMonster checkModel() {
		processSpecification.bag.initAutomataBag();
		ReactiveMinerOfflineQueryingCore minerFulQueryingCore = new ReactiveMinerOfflineQueryingCore(
				0, eventLog, null, null, eventLog.getTaskCharArchive(), null, processSpecification.bag);
		double before = System.currentTimeMillis();
		MegaMatrixMonster result = minerFulQueryingCore.check();
		double after = System.currentTimeMillis();

		logger.info("Total KB checking time: " + (after - before));

		return result;
	}
}