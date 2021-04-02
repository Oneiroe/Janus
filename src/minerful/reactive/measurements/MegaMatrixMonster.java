package minerful.reactive.measurements;

import minerful.logparser.LogParser;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.miner.ReactiveMinerOfflineQueryingCore;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;

import java.io.*;
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
 *
 * <p>
 * About variable matrixLite (int[][][]) meaning:
 * compact version of the byte[][][] where instead of saving the result for each event, we keep only what is required for the traces measures computation.
 * Each int stores the counter of the results of a combination of Activator and target of a given constraint in a specific trace.
 * In details:
 * COUNTER INDEX -> Explanation
 * 0 -> Number of Activator: True [#]
 * 1 -> Number of Target: True [#]
 * 2 -> Number of Activator: False
 * 3 -> Number of Target: False
 * 4 -> Number of  Activator: False, Target: False
 * 5 -> Number of  Activator: False, Target: true
 * 6 -> Number of  Activator: True,  Target: False
 * 7 -> Number of  Activator: True,  Target: True [#]
 * 8 -> Trace lenght [#]
 * <p>
 * Note. Supposedly only 4 value (marked with #) are enough to derive all the others, but lets try to keep all 9 for now
 * <p>
 * About model measures:
 * the model measures are computed considering the model as a constraint itself.
 * It is always the last constraint, thus all the automata.size()+1 along this class.
 * <p>
 * The rationale of the trace evaluation of the model is:
 * take all the activated automata in one instant of the trace and check if their targets are satisfied.
 * Practically speaking:
 * if there is at least one 10, then the entire model evaluates to 10,
 * else if there is at least one 11, then the entire model evaluates to 11,
 * else if there is at least one 01, then the entire model evaluates to 01,
 * otherwise the entire model evaluates to 00.
 */
public class MegaMatrixMonster {
    protected static Logger logger;
    private final LogParser log;
    private final Collection<SeparatedAutomatonOfflineRunner> automata;

    private byte[][][] eventsEvaluationMatrix; // [trace index][constraint index][event index]
    private int[][][] eventsEvaluationMatrixLite; // [trace index][constraint index][counter index]

    private float[][][] traceMeasuresMatrix; // [trace index][constraint index][measure index] -> support:0, confidence:1, lovinger: 2

    private SummaryStatistics[][] traceMeasuresDescriptiveStatistics; // [constraint index][measure index]

    private float[][] logMeasuresMatrix; // [constraint index][measure index]


    {
        if (logger == null) {
            logger = Logger.getLogger(ReactiveMinerOfflineQueryingCore.class.getCanonicalName());
        }
    }

    public MegaMatrixMonster(LogParser log, Collection<SeparatedAutomatonOfflineRunner> automata) {
        this.log = log;
        this.automata = automata;
    }

    public MegaMatrixMonster(byte[][][] matrix, LogParser log, Collection<SeparatedAutomatonOfflineRunner> automata) {
        this(log, automata);
        this.eventsEvaluationMatrix = matrix;
        System.gc();
    }

    public MegaMatrixMonster(int[][][] matrixLite, LogParser log, Collection<SeparatedAutomatonOfflineRunner> automata) {
        this(log, automata);
        this.eventsEvaluationMatrixLite = matrixLite;
        System.gc();
    }

    /**
     * Return the space required to serialize the current results of the Mega Matrix Monster
     *
     * @return
     * @throws IOException
     */
    public double getSpaceConsumption(String filePath) throws IOException {
        double result = 0.0;
        //        events
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        FileOutputStream fos = new FileOutputStream(filePath, true);
//        fos.write("traces;events-TOT;Constraints;Measures;EventsSpace;TracesSpace;LogSpace\n".getBytes());
        if (eventsEvaluationMatrixLite != null)
            fos.write(("" + eventsEvaluationMatrixLite.length + ";" + log.numberOfEvents() + ";" + eventsEvaluationMatrixLite[0].length + ";" + traceMeasuresMatrix[0][0].length + ";").getBytes());
        else
            fos.write(("" + eventsEvaluationMatrix.length + ";" + log.numberOfEvents() + ";" + eventsEvaluationMatrix[0].length + ";" + traceMeasuresMatrix[0][0].length + ";").getBytes());

        try {
            oos = new ObjectOutputStream(baos);
            if (eventsEvaluationMatrixLite != null)
                oos.writeObject(eventsEvaluationMatrixLite);
            else
                oos.writeObject(eventsEvaluationMatrix);
            oos.flush();
            oos.close();

            logger.info("size of events measures data structure : " + baos.size() / 1024d / 1024d + " MB");
            fos.write(("" + baos.size() / 1024d / 1024d + " MB;").getBytes());
            result += baos.size();
        } catch (IOException | OutOfMemoryError e) {
            logger.error("size of events measures data structure TOO BIG for serialization");
            fos.write(("outOfMem").getBytes());
            e.printStackTrace();
        }
        //        traces
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(traceMeasuresMatrix);
            oos.flush();
            oos.close();
            logger.info("size of traces measures data structure : " + baos.size() / 1024d / 1024d + " MB");
            fos.write(("" + baos.size() / 1024d / 1024d + " MB;").getBytes());
            result += baos.size();
        } catch (IOException | OutOfMemoryError e) {
            logger.error("size of traces measures data structure TOO BIG for serialization");
            fos.write(("outOfMem").getBytes());
            e.printStackTrace();
        }
        //        TRACE STATSS
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(traceMeasuresDescriptiveStatistics);
            oos.flush();
            oos.close();
            logger.info("size of trace measures stats data structure : " + baos.size() / 1024d / 1024d + " MB");
            fos.write(("" + baos.size() / 1024d / 1024d + " MB\n").getBytes());
            result += baos.size();
        } catch (IOException | OutOfMemoryError e) {
            logger.error("size of trace measures stats data structure TOO BIG for serialization");
            fos.write(("outOfMem\n").getBytes());
            e.printStackTrace();
        }
        // NEU LOG
        //        log
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(logMeasuresMatrix);
            oos.flush();
            oos.close();
            logger.info("size of log measures data structure : " + baos.size() / 1024d / 1024d + " MB");
            fos.write(("" + baos.size() / 1024d / 1024d + " MB\n").getBytes());
            result += baos.size();
        } catch (IOException | OutOfMemoryError e) {
            logger.error("size of log measures data structure TOO BIG for serialization");
            fos.write(("outOfMem\n").getBytes());
            e.printStackTrace();
        }

        logger.info("Size of MegaMatrixMonster results : " + result / 1024d / 1024d + " MB");
        fos.close();

        return result / 1024d / 1024d;
    }

    public float[][] getLogMeasuresMatrix() {
        return logMeasuresMatrix;
    }

    public byte[][][] getEventsEvaluationMatrix() {
        return eventsEvaluationMatrix;
    }

    public int[][][] getEventsEvaluationMatrixLite() {
        return eventsEvaluationMatrixLite;
    }

    public LogParser getLog() {
        return log;
    }

    public Collection<SeparatedAutomatonOfflineRunner> getAutomata() {
        return automata;
    }

    public float[][][] getTraceMeasuresMatrix() {
        return traceMeasuresMatrix;
    }

    /**
     * Return the number of constraints in the matrix
     *
     * @return
     */
    public int getConstraintsNumber() {
        if (eventsEvaluationMatrixLite == null) {
            return eventsEvaluationMatrix[0].length;
        } else {
            return eventsEvaluationMatrixLite[0].length;
        }
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
        return traceMeasuresMatrix[trace][constraint][measureIndex];
    }

    /**
     * retrieve the measurements for the current matrix/matrixLite
     *
     * @param nanTraceSubstituteFlag
     * @param nanTraceSubstituteValue
     * @param nanLogSkipFlag
     */
    public void computeAllMeasures(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue, boolean nanLogSkipFlag) {
        logger.info("Initializing measures matrix...");

        //		TRACE MEASURES
        logger.info("Retrieving Trace Measures...");
        if (eventsEvaluationMatrixLite == null) {
            traceMeasuresMatrix = new float[eventsEvaluationMatrix.length][automata.size() + 1][Measures.MEASURE_NUM];  //the space problem is here, not in the byte matrix
            computeTraceMeasuresMonster(nanTraceSubstituteFlag, nanTraceSubstituteValue);
        } else {
            traceMeasuresMatrix = new float[eventsEvaluationMatrixLite.length][automata.size() + 1][Measures.MEASURE_NUM];  //the space problem is here, not in the byte matrix
            computeTraceMeasuresLite(nanTraceSubstituteFlag, nanTraceSubstituteValue);
        }

        System.gc();
        logger.info("Retrieving Trace measures log statistics...");
        //		trace measure LOG STATISTICS
        int constraintsNum = automata.size() + 1;
        for (int constraint = 0; constraint < (automata.size() + 1); constraint++) {
            System.out.print("\rConstraint: " + constraint + "/" + constraintsNum);  // Status counter "current trace/total trace"
            for (int measure = 0; measure < Measures.MEASURE_NUM; measure++) {
                traceMeasuresDescriptiveStatistics[constraint][measure] = Measures.getMeasureDistributionObject(constraint, measure, traceMeasuresMatrix, nanLogSkipFlag);
            }
        }
        System.out.print("\rConstraint: " + constraintsNum + "/" + constraintsNum);  // Status counter "current trace/total trace"
        System.out.println();

        System.gc();
        logger.info("Retrieving NEW Log Measures...");
        //		LOG MEASURES
        logMeasuresMatrix = new float[automata.size() + 1][Measures.MEASURE_NUM];
        computeAllLogMeasures();

    }

    /**
     * retrieve the measurements for the current matrix/matrixLite
     *
     * @param nanTraceSubstituteFlag
     * @param nanTraceSubstituteValue
     */
    public void computeAllTraceMeasures(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue) {
        logger.info("Initializing measures matrix...");

        //		TRACE MEASURES
        logger.info("Retrieving Trace Measures...");
        if (eventsEvaluationMatrixLite == null) {
            traceMeasuresMatrix = new float[eventsEvaluationMatrix.length][automata.size() + 1][Measures.MEASURE_NUM];  //the space problem is here, not in the byte matrix
            computeTraceMeasuresMonster(nanTraceSubstituteFlag, nanTraceSubstituteValue);
        } else {
            traceMeasuresMatrix = new float[eventsEvaluationMatrixLite.length][automata.size() + 1][Measures.MEASURE_NUM];  //the space problem is here, not in the byte matrix
            computeTraceMeasuresLite(nanTraceSubstituteFlag, nanTraceSubstituteValue);
        }
        System.gc();
    }

    /**
     * retrieve the measurements for the current matrix/matrixLite
     *
     * @param nanLogSkipFlag
     */
    public void computeAllTraceMeasuresStats(boolean nanLogSkipFlag) {
        logger.info("Retrieving Trace measures log statistics...");
        traceMeasuresDescriptiveStatistics = new SummaryStatistics[automata.size() + 1][Measures.MEASURE_NUM];
        //		trace measure LOG STATISTICS
        int constraintsNum = automata.size() + 1;
        for (int constraint = 0; constraint < (automata.size() + 1); constraint++) {
            System.out.print("\rConstraint: " + constraint + "/" + constraintsNum);  // Status counter "current trace/total trace"
            for (int measure = 0; measure < Measures.MEASURE_NUM; measure++) {
                traceMeasuresDescriptiveStatistics[constraint][measure] = Measures.getMeasureDistributionObject(constraint, measure, traceMeasuresMatrix, nanLogSkipFlag);
            }
        }
        System.out.print("\rConstraint: " + constraintsNum + "/" + constraintsNum);  // Status counter "current trace/total trace"
        System.out.println();

        System.gc();
    }

    /**
     * Calculate a specific measure at the traces level for all the constraints, given its name.
     * The measurements are returned in output and not stored into the object.
     *
     * @param nanTraceSubstituteFlag
     * @param nanTraceSubstituteValue
     * @param measureName
     */
    public float[][] computeTracesSingleMeasure(String measureName, boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue) {
        return computeTracesSingleMeasure(Measures.getMeasureIndex(measureName), nanTraceSubstituteFlag, nanTraceSubstituteValue);
    }

    /**
     * Calculate a specific measure at the traces level for all the constraints, given its index.
     * The measurements are returned in output and not stored into the object.
     *
     * @param nanTraceSubstituteFlag
     * @param nanTraceSubstituteValue
     * @param measureIndex
     */
    public float[][] computeTracesSingleMeasure(int measureIndex, boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue) {
        logger.info("Initializing traces measure matrix...");
        float[][] measureResult = new float[eventsEvaluationMatrix.length][automata.size() + 1];  //the space problem is here, not in the byte matrix

        logger.info("Retrieving Trace Measures...");
        if (eventsEvaluationMatrixLite == null) {
            //        for the entire log
            for (int trace = 0; trace < eventsEvaluationMatrix.length; trace++) {
                System.out.print("\rTraces: " + trace + "/" + eventsEvaluationMatrix.length);  // Status counter "current trace/total trace"
//              for each trace
                for (int constraint = 0; constraint < eventsEvaluationMatrix[trace].length; constraint++) {
                    measureResult[trace][constraint] = Measures.getTraceMeasure(eventsEvaluationMatrix[trace][constraint], measureIndex, nanTraceSubstituteFlag, nanTraceSubstituteValue);
                }
            }
            System.out.print("\rTraces: " + eventsEvaluationMatrix.length + "/" + eventsEvaluationMatrix.length);  // Status counter "current trace/total trace"
            System.out.println();
        } else {
            //        for the entire log
            for (int trace = 0; trace < eventsEvaluationMatrixLite.length; trace++) {
                System.out.print("\rTraces: " + trace + "/" + eventsEvaluationMatrix.length);  // Status counter "current trace/total trace"
//              for each trace
                for (int constraint = 0; constraint < eventsEvaluationMatrixLite[trace].length; constraint++) {
//                  for each constraint
                    measureResult[trace][constraint] = Measures.getTraceMeasure(eventsEvaluationMatrixLite[trace][constraint], measureIndex, nanTraceSubstituteFlag, nanTraceSubstituteValue);
                }
            }
            System.out.print("\rTraces: " + eventsEvaluationMatrix.length + "/" + eventsEvaluationMatrix.length);  // Status counter "current trace/total trace"
            System.out.println();
        }
        return measureResult;
    }

    /**
     * Calculate a specific measure at the log level for all the constraints, given its specific trace measurements.
     * The measurements are returned in output and not stored into the object.
     *
     * @param nanLogSkipFlag
     */
    public SummaryStatistics[] computeSingleMeasureLog(float[][] traceMeasures, boolean nanLogSkipFlag) {
        logger.info("Initializing log measure matrix...");
        int constraintsNum = automata.size() + 1;
        SummaryStatistics[] logMeasuresresult = new SummaryStatistics[constraintsNum];

        logger.info("Retrieving Log Measures...");
        for (int constraint = 0; constraint < constraintsNum; constraint++) {
            System.out.print("\rConstraint: " + constraint + "/" + constraintsNum);  // Status counter "current trace/total trace"
            logMeasuresresult[constraint] = Measures.getMeasureDistributionObject(constraint, traceMeasures, nanLogSkipFlag);
        }
        System.out.println();
        return logMeasuresresult;
    }


    /**
     * retrieve the measurements for the current matrix
     *
     * @param nanTraceSubstituteFlag
     * @param nanTraceSubstituteValue
     */
    private void computeTraceMeasuresMonster(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue) {
        //        for the entire log
        for (int trace = 0; trace < eventsEvaluationMatrix.length; trace++) {
            System.out.print("\rTraces: " + trace + "/" + eventsEvaluationMatrix.length);  // Status counter "current trace/total trace"
//              for each trace
            for (int constraint = 0; constraint < eventsEvaluationMatrix[trace].length; constraint++) {
//                  for each constraint
                for (int measure = 0; measure < Measures.MEASURE_NUM; measure++) {
                    traceMeasuresMatrix[trace][constraint][measure] = Measures.getTraceMeasure(eventsEvaluationMatrix[trace][constraint], measure, nanTraceSubstituteFlag, nanTraceSubstituteValue);
                }
            }
        }
        System.out.print("\rTraces: " + eventsEvaluationMatrix.length + "/" + eventsEvaluationMatrix.length);  // Status counter "current trace/total trace"
        System.out.println();
    }

    /**
     * retrieve the measurements for the current matrix/matrixLite
     */
    public void computeAllLogMeasures() {
        logger.info("Retrieving Log Measures...");

        logMeasuresMatrix = new float[automata.size() + 1][Measures.MEASURE_NUM];

        int constraintsNum = automata.size() + 1;
        int tracesNum = log.wholeLength();

//        for each constraint
        for (int constraint = 0; constraint < constraintsNum; constraint++) {
            System.out.print("\rConstraint: " + constraint + "/" + constraintsNum);  // Status counter "current trace/total trace"
//            for each measure
            float[] currentTraceProbabilities = new float[9];
            float ATgivenA = 0;
            float AnotTgivenA = 0;
            float notATgivenNotA = 0;
            float notAnotTgivenNotA = 0;

            for (int trace = 0; trace < tracesNum; trace++) {
                // result { 0: activation, 1: target, 2: no activation, 3: no target}
                // result {4: 00, 5: 01, , 6: 10, 7:11}
                // result {8: trace length}
//                    A/n	-A/n	T/n	    -T/n	AT/n	A-T/n	-AT/n	-A-T/n  N
//                    0	    2	    1   	3   	7   	6   	5   	4       8
                if (eventsEvaluationMatrixLite == null) {
                    currentTraceProbabilities = Measures.getTraceProbabilities(eventsEvaluationMatrix[trace][constraint]);
                } else {
                    currentTraceProbabilities = Measures.getTraceProbabilities(eventsEvaluationMatrixLite[trace][constraint]);
                }
//                    AT|A	A-T|A	-AT|-A	-A-T|-A
                if (Float.isNaN(currentTraceProbabilities[7] / currentTraceProbabilities[0])) {
                    notATgivenNotA += currentTraceProbabilities[5] / currentTraceProbabilities[2];
                    notAnotTgivenNotA += currentTraceProbabilities[4] / currentTraceProbabilities[2];
                } else {
                    ATgivenA += currentTraceProbabilities[7] / currentTraceProbabilities[0];
                    AnotTgivenA += currentTraceProbabilities[6] / currentTraceProbabilities[0];
                }
            }
            ATgivenA /= tracesNum;
            AnotTgivenA /= tracesNum;
            notATgivenNotA /= tracesNum;
            notAnotTgivenNotA /= tracesNum;
            float A = ATgivenA + AnotTgivenA;
            float notA = notATgivenNotA + notAnotTgivenNotA;
            float T = ATgivenA + notATgivenNotA;
            float notT = AnotTgivenA + notAnotTgivenNotA;
            float n = tracesNum;

//            float pA = p[0];
//            float pT = p[1];
//            float pnA = p[2];
//            float pnT = p[3];
//            float pnAnT = p[4];
//            float pnAT = p[5];
//            float pAnT = p[6];
//            float pAT = p[7];
            float[] currentLogProbabilities = {A, T, notA, notT, notAnotTgivenNotA, notATgivenNotA, AnotTgivenA, ATgivenA, n};
            for (int measure = 0; measure < Measures.MEASURE_NUM; measure++) {
                logMeasuresMatrix[constraint][measure] = Measures.getLogMeasure(currentLogProbabilities, measure);
            }
        }
        System.out.print("\rConstraint: " + constraintsNum + "/" + constraintsNum);  // Status counter "current trace/total trace"
        System.out.println();

        System.gc();
    }


    /**
     * retrieve the measurements for the current matrixLite
     *
     * @param nanTraceSubstituteFlag
     * @param nanTraceSubstituteValue
     */
    private void computeTraceMeasuresLite(boolean nanTraceSubstituteFlag, double nanTraceSubstituteValue) {
        //        for the entire log
        for (int trace = 0; trace < eventsEvaluationMatrixLite.length; trace++) {
//              for each trace
            for (int constraint = 0; constraint < eventsEvaluationMatrixLite[trace].length; constraint++) {
//                  for each constraint
                for (int measure = 0; measure < Measures.MEASURE_NUM; measure++) {
                    traceMeasuresMatrix[trace][constraint][measure] = Measures.getTraceMeasure(eventsEvaluationMatrixLite[trace][constraint], measure, nanTraceSubstituteFlag, nanTraceSubstituteValue);
                }
            }
        }
    }


    public SummaryStatistics[][] getTraceMeasuresDescriptiveStatistics() {
        return traceMeasuresDescriptiveStatistics;
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
