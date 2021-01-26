package minerful.reactive.variant;

import minerful.concept.ProcessModel;
import minerful.logparser.LogParser;
import minerful.reactive.checking.ReactiveCheckingOfflineQueryingCore;
import minerful.reactive.params.JanusVariantCmdParameters;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

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

    private Object lCoded; // encoded log for efficient permutations
    private Set mTot;  // total set of constraints to analyse, i.e., union of process specification 1 adn 2
    private Set mDiffs;  // initial differences of specification 1 and 2 to be checked through the analysis
    private Object theMatrixRes;  // encoded partial result


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
        double after = System.currentTimeMillis();

        logger.info("Preprocessing time: " + (after - before));
//        PERMUTATION TEST
        before = System.currentTimeMillis();

        after = System.currentTimeMillis();
        logger.info("Permutation test time: " + (after - before));
//        POSTPROCESSING / RESULTS
        before = System.currentTimeMillis();

        after = System.currentTimeMillis();
        logger.info("Postprocessing time: " + (after - before));
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
        mDiffs = new HashSet();
        HashSet temp1 = new HashSet(processSpecification1.getAllConstraints());
        HashSet temp2 = new HashSet(processSpecification2.getAllConstraints());
        temp1.removeAll(processSpecification2.getAllConstraints());
        temp2.removeAll(processSpecification1.getAllConstraints());
        mDiffs.addAll(temp1);
        mDiffs.addAll(temp2);
        return mDiffs;
    }


}
