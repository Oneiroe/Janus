package minerful.reactive.checking;

import minerful.logparser.LogParser;
import minerful.reactive.automaton.SeparatedAutomatonOfflineRunner;
import minerful.reactive.miner.ReactiveMinerOfflineQueryingCore;
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

	private double[][] constraintLogMeasures; // [constraint index][measure index]

	private int MEASURE_NUM = 3;

	{
		if (logger == null) {
			logger = Logger.getLogger(ReactiveMinerOfflineQueryingCore.class.getCanonicalName());
		}
	}

	public MegaMatrixMonster(byte[][][] matrix, LogParser log, Collection<SeparatedAutomatonOfflineRunner> automata) {
		this.matrix = matrix;
		this.log = log;
		this.automata = automata;
		measures = new double[matrix.length][automata.size()][MEASURE_NUM];
		constraintLogMeasures = new double[automata.size()][MEASURE_NUM];
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
	 * Get the support of a specific trace for a specific constraint
	 *
	 * @param trace
	 * @param constraint
	 * @return
	 */
	public double getSpecificSupport(int trace, int constraint) {
		return measures[trace][constraint][0];
	}

	/**
	 * Get the confidence of a specific trace for a specific constraint
	 *
	 * @param trace
	 * @param constraint
	 * @return
	 */
	public double getSpecificConfidence(int trace, int constraint) {
		return measures[trace][constraint][1];
	}

	/**
	 * Get the Lovinger's measure of a specific trace for a specific constraint
	 *
	 * @param trace
	 * @param constraint
	 * @return
	 */
	public double getSpecificLovinger(int trace, int constraint) {
		return measures[trace][constraint][2];
	}

	/**
	 * retrieve the measurements for the current matrix
	 * <p>
	 * Current supported measures:
	 * - support
	 * - confidence
	 */
	public void computeMeasures() {
//		TRACE MEASURES
		//        for the entire log
		for (int trace = 0; trace < matrix.length; trace++) {
//              for each trace
			for (int constraint = 0; constraint < matrix[trace].length; constraint++) {
//                  for each constraint
				measures[trace][constraint][0] = Measures.getTraceSupport(matrix[trace][constraint]);
				measures[trace][constraint][1] = Measures.getTraceConfidence(matrix[trace][constraint]);
				measures[trace][constraint][2] = Measures.getTraceLovinger(matrix[trace][constraint]);
			}
		}

//		LOG MEASURES
		for (int constraint = 0; constraint < matrix[0].length; constraint++) {
			for (int measure = 0; measure < MEASURE_NUM; measure++) {
				constraintLogMeasures[constraint][measure] = Measures.getMeasureAverage(constraint, measure, measures);
//				constraintLogMeasures[constraint][measure] = Measures.getLogDuckTapeMeasures(constraint, measure, matrix);
			}
		}
	}


	public double[][] getConstraintLogMeasures() {
		return constraintLogMeasures;
	}
}
