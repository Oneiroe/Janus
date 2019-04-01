package minerful.reactive.checking;

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
	 * Supp(U->V) = P(U' intersection V') =
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
	 * Conf(U->V) = P(V'|U') =  P(V' intersection U') / P(U')
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
	 * given an evaluation array of a reactive constraint, extract the result of only the activator as an array of 0s and 1s
	 *
	 * @param reactiveConstraintEvaluation
	 * @return
	 */
	private static byte[] getActivatorEvaluation(byte[] reactiveConstraintEvaluation) {
		byte[] result = reactiveConstraintEvaluation;
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (result[i] / 2); // the activator is true if the byte is >1, i.e. 2 or 3
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
		byte[] result = reactiveConstraintEvaluation;
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte) (result[i] % 2); // the target is true if the byte is obb, i,e, 1 or 3
		}
		return result;
	}

}
