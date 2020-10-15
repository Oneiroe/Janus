package minerful.reactive.checking;

import minerful.logparser.LogParser;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.miner.ReactiveMinerOfflineQueryingCore;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Data structure for the fine grain evaluation result of constraints in each event of a log traces
 * <p>
 * About variable matrix (byte[][][]) bytes meaning:
 * Each byte stores the results of both Activator and target of a given constraint in a specific trace.
 * The left bit is for the activator, the right bit for the target,i.e.,[activator-bit][target-bit]
 * In details:
 * 0 -> 00 -> Activator: False, Target: False
 * 1 -> 01 -> Activator: False, Target: true
 * 2 -> 10 -> Activator: True,  Target: False
 * 3 -> 11 -> Activator: True,  Target: True
 */
public class MegaMatrixMonster {
    protected static Logger logger;
    private final byte[][][] matrix; // [trace index][constraint index][event index]
    private final LogParser log;
    private final Collection<SeparatedAutomatonOfflineRunner> automata;

    private double[][][] measures; // [trace index][constraint index][measure index] -> support:0, confidence:1, lovinger: 2

    private DescriptiveStatistics[][] constraintLogMeasures; // [constraint index][measure index]

    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveMinerOfflineQueryingCore.class.getCanonicalName());
        }
    }

    public MegaMatrixMonster(byte[][][] matrix, LogParser log, Collection<SeparatedAutomatonOfflineRunner> automata) {
        this.matrix = matrix;
        this.log = log;
        this.automata = automata;
        measures = new double[matrix.length][automata.size()][Measures.MEASURE_NUM];
        constraintLogMeasures = new DescriptiveStatistics[automata.size()][Measures.MEASURE_NUM];
    }

    public byte[][][] getMatrix() {
        return matrix;
    }

    public LogParser getLog() {
        return log;
    }

    public Collection<SeparatedAutomatonOfflineRunner> getAutomata() {
        return automata;
    }

    public double[][][] getMeasures() {
        return measures;
    }

    /**
     * Get the specific measure of a specific trace for a specific constraint
     *
     * @param trace
     * @param constraint
     * @param measureIndex
     * @return
     */
    public double getSpecificMeasure(int trace, int constraint, int measureIndex) {
        return measures[trace][constraint][measureIndex];
    }

    /**
     * retrieve the measurements for the current matrix
     */
    public void computeMeasures(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue, boolean nanLogSkipFlag) {
//		TRACE MEASURES
        logger.info("Retrieving Trace Measures...");
        //        for the entire log
        for (int trace = 0; trace < matrix.length; trace++) {
//              for each trace
            for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
//                  for each constraint
                for (int measure = 0; measure < Measures.MEASURE_NUM; measure++) {
                    measures[trace][constraint][measure] = Measures.getTraceMeasure(matrix[trace][constraint], measure, nanTraceSubstituteFlag, nanTraceSubstituteValue);
                }
            }
        }

        logger.info("Retrieving Log Measures...");
//		LOG MEASURES
        for (int constraint = 0; constraint < matrix[0].length; constraint++) {
            for (int measure = 0; measure < Measures.MEASURE_NUM; measure++) {
//				constraintLogMeasures[constraint][measure] = Measures.getMeasureAverage(constraint, measure, measures);
                constraintLogMeasures[constraint][measure] = Measures.getMeasureDistributionObject(constraint, measure, measures, nanLogSkipFlag);
//				constraintLogMeasures[constraint][measure] = Measures.getLogDuckTapeMeasures(constraint, measure, matrix);
            }
        }
    }


    public DescriptiveStatistics[][] getConstraintLogMeasures() {
        return constraintLogMeasures;
    }

    /**
     * Get the name of the i-th measure
     *
     * @return
     */
    public String getMeasureName(int measureIndex) {
        return Measures.MEASURE_NAMES[measureIndex];
    }

    /**
     * Get the names of all the measures
     *
     * @return
     */
    public String[] getMeasureNames() {
        return Measures.MEASURE_NAMES;
    }


}
