package minerful;

import minerful.concept.ProcessModel;
import minerful.concept.TaskCharArchive;
import minerful.io.params.OutputModelParameters;
import minerful.logparser.LogEventClassifier;
import minerful.logparser.LogParser;
import minerful.logparser.StringLogParser;
import minerful.logparser.XesLogParser;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.reactive.params.JanusVariantCmdParameters;
import minerful.reactive.variant.ReactiveVariantAnalysisCore;
import minerful.utils.MessagePrinter;

import java.io.File;
import java.util.Map;

/**
 * Class for launching Janus variant analysis on two logs
 */
public class JanusVariantAnalysisLauncher {
    public static MessagePrinter logger = MessagePrinter.getInstance(JanusModelCheckLauncher.class);

    private PostProcessingCmdParameters discoveryParams;
    private JanusVariantCmdParameters janusParams;
    private SystemCmdParameters systemParams;
    private ViewCmdParameters viewParams;

    private LogParser eventLog1;
    private ProcessModel processSpecification1;
    private Map<String, Float> measurementsSpecification1;
    private LogParser eventLog2;
    private ProcessModel processSpecification2;
    private Map<String, Float> measurementsSpecification2;


    public JanusVariantAnalysisLauncher(JanusVariantCmdParameters janusParams, SystemCmdParameters systemParams) {
        this.janusParams = janusParams;
        this.systemParams = systemParams;

        this.eventLog1 = deriveLogParserFromLogFile(janusParams.inputLanguage1, janusParams.inputLogFile1, janusParams.eventClassification, null);
        this.eventLog2 = deriveLogParserFromLogFile(janusParams.inputLanguage2, janusParams.inputLogFile2, janusParams.eventClassification, eventLog1.getTaskCharArchive());
//        this is a bit redundant, but to make sure that both have the same alphabet we recompute the first parser with the alphabet of the second, which now has both alphabets
        this.eventLog1 = deriveLogParserFromLogFile(janusParams.inputLanguage1, janusParams.inputLogFile1, janusParams.eventClassification, eventLog2.getTaskCharArchive());
        this.discoveryParams = new PostProcessingCmdParameters();
        this.discoveryParams.confidenceThreshold = 0.8;
        this.discoveryParams.supportThreshold = 0.1;
    }

    public JanusVariantAnalysisLauncher(JanusVariantCmdParameters janusParams, SystemCmdParameters systemParams, ProcessModel processSpecification1, ProcessModel processSpecification2) {
        this(janusParams, systemParams);

        this.processSpecification1 = processSpecification1;
        this.processSpecification2 = processSpecification2;
    }

    public JanusVariantAnalysisLauncher(JanusVariantCmdParameters janusParams, SystemCmdParameters systemParams, PostProcessingCmdParameters discoveryParams) {
        this(janusParams, systemParams);
        this.discoveryParams = discoveryParams;
        this.viewParams = new ViewCmdParameters();
    }

    public JanusVariantAnalysisLauncher(JanusVariantCmdParameters janusParams, ViewCmdParameters viewParams, SystemCmdParameters systemParams, PostProcessingCmdParameters discoveryParams) {
        this(janusParams, systemParams);
        this.discoveryParams = discoveryParams;
        this.viewParams = viewParams;
    }

    /**
     * Returns the logParser of a given input log
     *
     * @param inputLanguage       file format of the input event log
     * @param inputLogFile        path to the input file of the event log
     * @param eventClassification
     * @return LogParser of the input log
     */
    public static LogParser deriveLogParserFromLogFile(JanusVariantCmdParameters.InputEncoding inputLanguage, File inputLogFile, JanusVariantCmdParameters.EventClassification eventClassification, TaskCharArchive taskCharArchive) {
        LogParser logParser = null;
        switch (inputLanguage) {
            case xes:
            case mxml:
                LogEventClassifier.ClassificationType evtClassi = fromInputParamToXesLogClassificationType(eventClassification);
                try {
                    logParser = new XesLogParser(inputLogFile, evtClassi, taskCharArchive);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                // Let us try to free memory from the unused XesDecoder!
                System.gc();
                break;
            case strings:
                try {
                    logParser = new StringLogParser(inputLogFile, LogEventClassifier.ClassificationType.NAME, taskCharArchive);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                break;
            default:
                throw new UnsupportedOperationException("This encoding (" + inputLanguage + ") is not yet supported");
        }

        return logParser;
    }

    /**
     * Returns the classification type for a given event log encoding
     *
     * @param evtClassInputParam
     * @return
     */
    public static LogEventClassifier.ClassificationType fromInputParamToXesLogClassificationType(JanusVariantCmdParameters.EventClassification evtClassInputParam) {
        switch (evtClassInputParam) {
            case name:
                return LogEventClassifier.ClassificationType.NAME;
            case logspec:
                return LogEventClassifier.ClassificationType.LOG_SPECIFIED;
            default:
                throw new UnsupportedOperationException("Classification strategy " + evtClassInputParam + " not yet implemented");
        }
    }

    public ProcessModel getProcessSpecification1() {
        return processSpecification1;
    }

    public void setProcessSpecification1(ProcessModel processSpecification1) {
        this.processSpecification1 = processSpecification1;
    }

    public ProcessModel getProcessSpecification2() {
        return processSpecification2;
    }

    public void setProcessSpecification2(ProcessModel processSpecification2) {
        this.processSpecification2 = processSpecification2;
    }

    public TaskCharArchive getAlphabetDecoder() {
        return eventLog2.getTaskCharArchive();
    }

    /**
     * analyse the DECLARE rules differences of the two input log with statistical guarantees
     *
     * @return
     */
    public Map<String, Float> checkVariants() {
//        1. Rules discovery for each log
        if (processSpecification1 == null || processSpecification2 == null) {
            logger.info("Models discovery for input variants");
            double before = System.currentTimeMillis();
            // Variant 1 discovery
            TaskCharArchive taskCharArchive1 = eventLog1.getTaskCharArchive();
            JanusOfflineMinerStarter minerMinaStarter = new JanusOfflineMinerStarter();
            processSpecification1 = minerMinaStarter.mine(eventLog1, taskCharArchive1, discoveryParams.supportThreshold, discoveryParams.confidenceThreshold);
            // Variant 1 discovered model (optional) output
            OutputModelParameters model1params = new OutputModelParameters();
            model1params.fileToSaveAsJSON = janusParams.fileToSaveModel1AsJSON;
            model1params.fileToSaveConstraintsAsCSV = janusParams.fileToSaveModel1AsCSV;
            new MinerFulOutputManagementLauncher().manageOutput(processSpecification1, viewParams, model1params, systemParams, eventLog1);

            // variant 2 discovery
            TaskCharArchive taskCharArchive2 = eventLog2.getTaskCharArchive();
            minerMinaStarter = new JanusOfflineMinerStarter();
            processSpecification2 = minerMinaStarter.mine(eventLog2, taskCharArchive2, discoveryParams.supportThreshold, discoveryParams.confidenceThreshold);
            // Variant 2 discovered model (optional) output
            OutputModelParameters model2params = new OutputModelParameters();
            model2params.fileToSaveAsJSON = janusParams.fileToSaveModel2AsJSON;
            model2params.fileToSaveConstraintsAsCSV = janusParams.fileToSaveModel2AsCSV;
            new MinerFulOutputManagementLauncher().manageOutput(processSpecification2, viewParams, model2params, systemParams, eventLog2);

            double after = System.currentTimeMillis();
            logger.info("Variants constraints discovery time: " + (after - before));
        }

        ReactiveVariantAnalysisCore variantAnalysisCore = new ReactiveVariantAnalysisCore(
                eventLog1, processSpecification1, eventLog2, processSpecification2, janusParams
        );

        Map<String, Float> result = variantAnalysisCore.check();

        storeOriginalVariantsResults(variantAnalysisCore);

        return result;
    }

    /**
     * Stores the measurement of the original variants for external access via respective get functions
     *
     * @param variantAnalysisCore
     */
    private void storeOriginalVariantsResults(ReactiveVariantAnalysisCore variantAnalysisCore) {
        measurementsSpecification1 = variantAnalysisCore.getMeasurementsVar1(true);
        measurementsSpecification2 = variantAnalysisCore.getMeasurementsVar2(true);
    }

    public Map<String, Float> getMeasurementsSpecification1() {
        return measurementsSpecification1;
    }

    public Map<String, Float> getMeasurementsSpecification2() {
        return measurementsSpecification2;
    }
}
