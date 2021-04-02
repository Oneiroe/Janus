package minerful;

import minerful.checking.params.CheckingCmdParameters;
import minerful.concept.ProcessModel;
import minerful.io.ProcessModelLoader;
import minerful.io.params.InputModelParameters;
import minerful.logparser.LogParser;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.reactive.measurements.MegaMatrixMonster;
import minerful.reactive.measurements.ReactiveMeasurementsOfflineQueryingCore;
import minerful.reactive.params.JanusMeasurementsCmdParameters;
import minerful.utils.MessagePrinter;
import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

/**
 * Class for launching JanusZ model checker
 */
public class JanusMeasurementsLauncher {
    public static MessagePrinter logger = MessagePrinter.getInstance(JanusMeasurementsLauncher.class);

    private ProcessModel processSpecification;
    private LogParser eventLog;
    private CheckingCmdParameters chkParams;
    private JanusMeasurementsCmdParameters janusParams;

    private JanusMeasurementsLauncher(CheckingCmdParameters chkParams, JanusMeasurementsCmdParameters janusParams) {
        this.chkParams = chkParams;
        this.janusParams = janusParams;
    }

    public JanusMeasurementsLauncher(AssignmentModel declareMapModel, LogParser inputLog, CheckingCmdParameters chkParams, JanusMeasurementsCmdParameters janusParams) {
        this(chkParams, janusParams);
        this.processSpecification = new ProcessModelLoader().loadProcessModel(declareMapModel);
        this.eventLog = inputLog;
    }

    public JanusMeasurementsLauncher(ProcessModel minerFulProcessModel, LogParser inputLog, CheckingCmdParameters chkParams, JanusMeasurementsCmdParameters janusParams) {
        this(chkParams, janusParams);
        this.processSpecification = minerFulProcessModel;
        this.eventLog = inputLog;
    }

    public JanusMeasurementsLauncher(InputModelParameters inputParams, InputLogCmdParameters inputLogParams, CheckingCmdParameters chkParams, SystemCmdParameters systemParams, JanusMeasurementsCmdParameters janusParams) {
        this(chkParams, janusParams);

        if (inputParams.inputFile == null) {
            systemParams.printHelpForWrongUsage("Input process model file missing!");
            System.exit(1);
        }
        this.eventLog = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputLogParams);

        // Load the process specification from the file
        this.processSpecification =
                new ProcessModelLoader().loadProcessModel(inputParams.inputLanguage, inputParams.inputFile, this.eventLog.getTaskCharArchive());
        // Apply some preliminary pruning
//        PostProcessingCmdParameters preProcParams; //from input
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
        // the events evaluation must be computed in any case
        processSpecification.bag.initAutomataBag();
        ReactiveMeasurementsOfflineQueryingCore reactiveMeasurementsOfflineQueryingCore = new ReactiveMeasurementsOfflineQueryingCore(
                0, eventLog, janusParams, null, eventLog.getTaskCharArchive(), null, processSpecification.bag);
        double before = System.currentTimeMillis();
        MegaMatrixMonster result = reactiveMeasurementsOfflineQueryingCore.check();
        double after = System.currentTimeMillis();

        logger.info("Total events evaluation time: " + (after - before));

//        Compute the measures at the detail level selected in input
        before = System.currentTimeMillis();
        switch (janusParams.detailsLevel) {
            case event:
                break;
            case trace:
                result.computeAllTraceMeasures(janusParams.nanTraceSubstituteFlag, janusParams.nanTraceSubstituteValue);
                break;
            case allTrace:
            case traceStats:
                result.computeAllTraceMeasures(janusParams.nanTraceSubstituteFlag, janusParams.nanTraceSubstituteValue);
                result.computeAllTraceMeasuresStats(janusParams.nanLogSkipFlag);
                break;
            case log:
                result.computeAllLogMeasures();
                break;
            case allLog:
            case all:
                result.computeAllTraceMeasures(janusParams.nanTraceSubstituteFlag, janusParams.nanTraceSubstituteValue);
                result.computeAllTraceMeasuresStats(janusParams.nanLogSkipFlag);
                result.computeAllLogMeasures();
                break;
        }
        after = System.currentTimeMillis();

        logger.info("Total measurement retrieval time: " + (after - before));

        return result;
    }
}