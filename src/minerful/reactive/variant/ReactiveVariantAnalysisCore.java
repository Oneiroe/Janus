package minerful.reactive.variant;

import minerful.concept.ProcessModel;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.checking.MegaMatrixMonster;
import minerful.reactive.checking.ReactiveCheckingOfflineQueryingCore;
import minerful.reactive.params.JanusCheckingCmdParameters;
import minerful.reactive.params.JanusVariantCmdParameters;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Class to organize the variant analysis
 */
public class ReactiveVariantAnalysisCore {

    protected static Logger logger;
    private final LogParser logParser_1;  // original log1 parser
    private ProcessModel processSpecification1;  // original set of constraints mined from log1
    private final LogParser logParser_2; // original log2 parser
    private ProcessModel processSpecification2;  // original set of constraints mined from log2
    private final JanusVariantCmdParameters janusVariantParams;  // input parameter of the analysis

    private Map lCoded; // encoded log for efficient permutations
    private Set mTot;  // total set of constraints to analyse, i.e., union of process specification 1 adn 2
    private ProcessModel processSpecificationUnion;  // total set of constraints to analyse, i.e., union of process specification 1 adn 2
    private Set mDiffs;  // initial differences of specification 1 and 2 to be checked through the analysis
    private ProcessModel processSpecificationDifference;  // initial differences of specification 1 and 2 to be checked through the analysis
    private Object theMatrixRes;  // encoded partial result
    private static final String MEASURE = "Confidence";  // expose measure selection to the user
    private static final int MEASURE_INDEX = 1;

    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveCheckingOfflineQueryingCore.class.getCanonicalName());
        }
    }

    /**
     * Constructor
     *
     * @param logParser_1
     * @param logParser_2
     * @param janusVariantParams
     */
    public ReactiveVariantAnalysisCore(LogParser logParser_1, ProcessModel processSpecification1, LogParser logParser_2, ProcessModel processSpecification2, JanusVariantCmdParameters janusVariantParams) {
        this.logParser_1 = logParser_1;
        this.processSpecification1 = processSpecification1;
        this.logParser_2 = logParser_2;
        this.processSpecification2 = processSpecification2;
        this.janusVariantParams = janusVariantParams;
    }

    /**
     * Launcher for variant analysis of two logs
     */
    public void check() {
//        PREPROCESSING
        double before = System.currentTimeMillis();
//        1. Models differences
        setModelsDifferences(processSpecification1, processSpecification2);
//        2. Models Union (total set of rules to check afterwards)
        setModelsUnion(processSpecification1, processSpecification2);
//        3. Encode log (create efficient log structure for the permutations)
//        4. Precompute all possible results for the Encoded Log
        encodeLogs(logParser_1, logParser_2, processSpecificationUnion);
        double after = System.currentTimeMillis();

        logger.info("Preprocessing time: " + (after - before));
//        PERMUTATION TEST
        before = System.currentTimeMillis();
        // TODO WIP
        logger.info("HERE");

        after = System.currentTimeMillis();
        logger.info("Permutation test time: " + (after - before));
//        POSTPROCESSING / RESULTS
        before = System.currentTimeMillis();

        after = System.currentTimeMillis();
        logger.info("Postprocessing time: " + (after - before));
    }


    private Map<String, Map<String, Double>> encodeLog(LogParser logParser, ProcessModel model) {
        Map<String, Map<String, Double>> result = new HashMap();
        JanusCheckingCmdParameters janusCheckingParams = new JanusCheckingCmdParameters(false, 0, true);
        ReactiveCheckingOfflineQueryingCore reactiveCheckingOfflineQueryingCore = new ReactiveCheckingOfflineQueryingCore(
                0, logParser, janusCheckingParams, null, logParser.getTaskCharArchive(), null, model.bag);
        double before = System.currentTimeMillis();
        MegaMatrixMonster measures = reactiveCheckingOfflineQueryingCore.check();
        double after = System.currentTimeMillis();

        logger.info("Total KB checking time: " + (after - before));

//      TODO maybe compute only the desired measure
        measures.computeMeasures(janusCheckingParams.nanTraceSubstituteFlag, janusCheckingParams.nanTraceSubstituteValue, janusCheckingParams.nanLogSkipFlag);

        int currentTrace = 0;
        for (Iterator<LogTraceParser> it = logParser.traceIterator(); it.hasNext(); ) {
            LogTraceParser tr = it.next();
            String stringTrace = tr.printStringTrace();
            result.put(stringTrace, new HashMap<String, Double>());
            int cIndex = 0;
            for (SeparatedAutomatonOfflineRunner c : measures.getAutomata()) {
                result.get(stringTrace).put(
                        c.toString(),
                        measures.getSpecificMeasure(currentTrace, cIndex, MEASURE_INDEX)
                );
                cIndex++;
            }
            currentTrace++;
        }

        return result;
    }

    /**
     * Encode the input traces for efficient permutation.
     * the result is a Map where the keys are the hash of the traces and the content in another map with key:value as constrain:measure.
     * In this way we check only here the constraints in each trace and later we permute only the results
     *
     * @param logParser_1
     * @param logParser_2
     * @param model
     * @return
     */
    private Map encodeLogs(LogParser logParser_1, LogParser logParser_2, ProcessModel model) {
        lCoded = new HashMap<String, Map>();
        lCoded.putAll(encodeLog(logParser_1, model));
        lCoded.putAll(encodeLog(logParser_2, model));
        return lCoded;
    }

    /**
     * Computes the union of the two models. It store the results in mTot and returns it in output
     *
     * @param processSpecification1
     * @param processSpecification2
     * @return
     */
    private Set setModelsUnion(ProcessModel processSpecification1, ProcessModel processSpecification2) {
        mTot = new HashSet();
        mTot.addAll(processSpecification1.getAllConstraints());
        mTot.addAll(processSpecification2.getAllConstraints());
        processSpecificationUnion = ProcessModel.union(processSpecification1, processSpecification2);
        return mTot;
    }


    /**
     * Computes the differences of the two models. It store the results in mDiffs and returns it in output
     *
     * @param processSpecification1
     * @param processSpecification2
     * @return
     */
    private Set setModelsDifferences(ProcessModel processSpecification1, ProcessModel processSpecification2) {
        mDiffs = new HashSet<ProcessModel>();
        HashSet<ProcessModel> temp1 = new HashSet(processSpecification1.getAllConstraints());
        HashSet<ProcessModel> temp2 = new HashSet(processSpecification2.getAllConstraints());
        temp1.removeAll(processSpecification2.getAllConstraints());
        temp2.removeAll(processSpecification1.getAllConstraints());
        mDiffs.addAll(temp1);
        mDiffs.addAll(temp2);

        processSpecificationDifference = ProcessModel.difference(processSpecification1, processSpecification2);
        return mDiffs;
    }


}
