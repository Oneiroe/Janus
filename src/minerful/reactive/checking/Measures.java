package minerful.reactive.checking;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * Class containing the measurement functions
 */
public class Measures {

	/**
	 * Retrieve the probabilities of both activator and target formula of a reactive constraint.
	 *
	 * @param reactiveConstraintEvaluation byte array of {0,1,2,3} encoding the bolean evaluation of both the activator and the target of a reactive constraint
	 * @return
	 */
	public static double[] getReactiveProbabilities(byte[] reactiveConstraintEvaluation) {
		double[] result = {0, 0};  // result[0]: activation, result[1]: target
		if (reactiveConstraintEvaluation.length == 0) return result;
		for (byte eval : reactiveConstraintEvaluation) {
			result[0] += eval / 2; // the activator is true if the byte is >1, i.e. 2 or 3
			result[1] += eval % 2; // the target is true if the byte is obb, i,e, 1 or 3
		}
		result[0] /= reactiveConstraintEvaluation.length;
		result[1] /= reactiveConstraintEvaluation.length;
		return result;
	}

	/**
	 * Retrieve the probability of a formula holding true in a trace given its evaluation on the trace.
	 * BEWARE: this probability is defined for a single formula, not the entire reactive constraint A->B
	 *
	 * @param formulaEvaluation Byte array (representing a bit array) of 0s and 1s
	 * @return
	 */
	public static double getFormulaProbability(byte[] formulaEvaluation) {
		if (formulaEvaluation.length == 0) return 0;
		double result = 0;
		for (byte eval : formulaEvaluation) {
			result += eval;
		}
		return result / formulaEvaluation.length;
	}

	/**
	 * retrieve the support measure of a constraint for a given trace.
	 * <p>
	 * The support measure is defined as:
	 * Supp(A->T) = P(A' intersection T') =
	 *
	 * @return
	 */
	public static double getTraceSupport(byte[] reactiveConstraintEvaluation) {
		if (reactiveConstraintEvaluation.length == 0) return 0;
		double result = 0;
		for (byte eval : reactiveConstraintEvaluation) {
			result += eval / 3; // activator and target are both true when the byte is 3
		}
		return result / reactiveConstraintEvaluation.length;
	}

	/**
	 * retrieve the confidence of a constraint for a given trace.
	 * <p>
	 * The confidence measure is defined as:
	 * Conf(A->T) = P(T'|A') =  P(T' intersection A') / P(A') = Supp(A'->T')/P(A')
	 *
	 * @return
	 */
	public static double getTraceConfidence(byte[] reactiveConstraintEvaluation) {
		byte[] activatorEval = getActivatorEvaluation(reactiveConstraintEvaluation);
		double denominator = getFormulaProbability(activatorEval);
		if (denominator == 0) return 0;
		return getTraceSupport(reactiveConstraintEvaluation) / denominator;
	}

	/**
	 * Retrieve the Lovinger's Measure of a constraint for a given trace.
	 * <p>
	 * The Lovinger's measure is defined as:
	 * Lov(A->T) = ( P(T'|A') - P(T')) / (1 - P(T')) = (Conf(A'->T') - P(T')) / (1 - P(T'))
	 *
	 * @return
	 */
	public static double getTraceLovinger(byte[] reactiveConstraintEvaluation) {
		double conf = getTraceConfidence(reactiveConstraintEvaluation);
		double probabilityTarget = getFormulaProbability(getTargetEvaluation(reactiveConstraintEvaluation));

		// TODO check behaviour when denominator=0
		return (conf - probabilityTarget) / (1 - probabilityTarget);
	}

	/**
	 * return the support measure for a given constraint over the entire log
	 *
	 * @return
	 */
//	@Deprecated
	public static double getLogSupport(int constraintIndex, MegaMatrixMonster matrix) {
		return getMeasureAverage(constraintIndex, 0, matrix.getMeasures());
//		return getLogDuckTapeMeasures(constraintIndex, 0, matrix.getMatrix());
	}

	/**
	 * return the given measure of a constraint over the entire log using the "tape" method:
	 * Consider the log as a single trace and compute the measure with the trace methods
	 *
	 * @param constraintIndex
	 * @param measureIndex
	 * @param bytesMatrix
	 * @return
	 */
	public static double getLogDuckTapeMeasures(int constraintIndex, int measureIndex, byte[][][] bytesMatrix) {
		double result = 0;
		byte[] tapeLog = {};

		for (byte[][] trace : bytesMatrix) {
			tapeLog = ArrayUtils.addAll(tapeLog, trace[constraintIndex]);
		}

//		TODO remove this hard-coded shame
		switch (measureIndex) {
			case 0:
//				support
				result = getTraceSupport(tapeLog);
				break;
			case 1:
//				confidence
				result = getTraceConfidence(tapeLog);
				break;
			case 2:
//				Lovinger
				result = getTraceLovinger(tapeLog);
				break;
		}

		return result;
	}

	/**
	 * Compute the probability for a SINGLE constraint over the entire log seen as a single (duck)tape
	 *
	 * @return
	 */
	public static double getLogDuckTapeProbability() {
//		TODO
		return 0;
	}

	/**
	 * return the X measure of a constraint over the entire log as the average of the support within all the traces
	 *
	 * @return
	 */
//	@Deprecated
	public static double getMeasureAverage(int constraintIndex, int measureIndex, double[][][] traceMeasuresMatrix) {
		double result = 0;
		for (double[][] traceEval : traceMeasuresMatrix) {
			result += traceEval[constraintIndex][measureIndex];
		}

		return result / traceMeasuresMatrix.length;
	}


	/**
	 * retieve the measure distribution info.
	 * it takes the results of all the traces and draw the distribution properties.
	 * i.e. average value, standard deviation, quartile, max, min
	 *
	 * @param traceMeasures array containing the measure value for each trace
	 * @return array with the distribution values
	 */
	public static double[] getMeasureDistribution(double[] traceMeasures) {
		DescriptiveStatistics measureDistribution = new DescriptiveStatistics();
		for (double measure : traceMeasures) {
			measureDistribution.addValue(measure);
		}
		double[] result = {
				measureDistribution.getMean(),
				measureDistribution.getGeometricMean(),
				measureDistribution.getVariance(),
				measureDistribution.getPopulationVariance(),
				measureDistribution.getStandardDeviation(),
				measureDistribution.getPercentile(75),
				measureDistribution.getMax(),
				measureDistribution.getMin()
		};

		return result;
	}

	/**
	 * Returns an object containing the statistic of the measure distribution
	 *
	 * @param constraintIndex
	 * @param measureIndex
	 * @param traceMeasuresMatrix
	 *
	 * @return
	 */
	public static DescriptiveStatistics getMeasureDistributionObject(int constraintIndex, int measureIndex, double[][][] traceMeasuresMatrix){
		DescriptiveStatistics measureDistribution = new DescriptiveStatistics();

		for (double[][] traceEval : traceMeasuresMatrix) {
			measureDistribution.addValue(traceEval[constraintIndex][measureIndex]);
		}

		return measureDistribution;
	}

	/**
	 * given an evaluation array of a reactive constraint, extract the result of only the activator as an array of 0s and 1s
	 *
	 * @param reactiveConstraintEvaluation
	 * @return
	 */
	private static byte[] getActivatorEvaluation(byte[] reactiveConstraintEvaluation) {
		byte[] result = new byte[reactiveConstraintEvaluation.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (reactiveConstraintEvaluation[i] / 2); // the activator is true if the byte is >1, i.e. 2 or 3
		}
		return result;
	}

	/**
	 * given an evaluation array of a reactive constraint, extract the result of only the target as an array of 0s and 1s
	 *
	 * @param reactiveConstraintEvaluation
	 * @return
	 */
	private static byte[] getTargetEvaluation(byte[] reactiveConstraintEvaluation) {
		byte[] result = new byte[reactiveConstraintEvaluation.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (reactiveConstraintEvaluation[i] % 2); // the target is true if the byte is obb, i,e, 1 or 3
		}
		return result;
	}


}
