package minerful.reactive.automaton;

import dk.brics.automaton.State;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Object to run a trace over a separated automata
 */
public class SeparatedAutomatonRunner {
	private SeparatedAutomaton automaton;
	private State activatorPointer;

	//    REMEMBER that separated automaton is a disjunction of conjunction!!!
	private List<ConjunctAutomataRunner> disjunctAutomataRunners; //ATokens!!!
	private List<ATokenRunner> aTokensRunners;

	private int activationCounter;
	private int fulfilledActivationCounter;

	private List<Character> specificAlphabet;
	private Map<Character, Character> parametricMapping;


	/**
	 * Initialize a runner object to run trace on a given separated automaton.
	 * For each disjunct automata of the spared automaton is initialized a specific runner
	 *
	 * @param automaton        on which running the analysis
	 * @param specificAlphabet ordered array of character from the trace to be used in the parametric automaton
	 */
	public SeparatedAutomatonRunner(SeparatedAutomaton automaton, List<Character> specificAlphabet) {
		this.automaton = automaton;
		this.disjunctAutomataRunners = new ArrayList<ConjunctAutomataRunner>();
		this.aTokensRunners = new ArrayList<ATokenRunner>();
		this.parametricMapping = new HashMap<Character, Character>();

		this.specificAlphabet = specificAlphabet;
		char[] par = automaton.getParametricAlphabet();
		for (int i = 0; i < specificAlphabet.size(); i++) {
			parametricMapping.put(specificAlphabet.get(i), par[i]);
		}
//        it is better to put the present automaton as first of the list for performance speedup
//        BUT pasts must be carried on any way
		for (ConjunctAutomata ca : automaton.getDisjunctAutomata()) {
			this.disjunctAutomataRunners.add(new ConjunctAutomataRunner(ca));
		}

		this.activationCounter = 0;
		this.fulfilledActivationCounter = 0;

		this.activatorPointer = automaton.getActivator().getInitialState();
	}


	/**
	 * @return Number of total activation of the constraint
	 */
	public int getActivationCounter() {
		return activationCounter;
	}

	/**
	 * @return Number of fulfilled activation
	 */
	public int getFulfilledActivationCounter() {
		return fulfilledActivationCounter;
	}

	/**
	 * The current degree of truth of the constraint represented by the separated automaton and the trace run on it.
	 * It is the ration between the fulfilled activation and the total number of activations.
	 *
	 * @return support of the constraint represented by the separated automaton wrt the given trace.
	 */
	public double getDegreeOfTruth() {
//        TODO full support formula
		if (activationCounter == 0) {
			return 0.0;
		}
		int aTokenFullfilled = 0;
		for (ATokenRunner atr : aTokensRunners) {
			if (atr.getCurrentResult()) aTokenFullfilled++;
		}
		return (double) (fulfilledActivationCounter + aTokenFullfilled) / activationCounter;
	}

	/**
	 * @return True if the constraint has been activated at least nce in the trace
	 */
	public boolean isActivated() {
		return (activationCounter != 0);
	}

	/**
	 * Perform a single step in the separated automata using the given transition
	 */
	public void step(char realTransition) {
//      MEMO. we are using a parametric automaton:
//          the transition from the real trace must be translated into the generic one
//        getOrDefault java8 required
//        TODO parametrize the default character instead of hardcoding
		char transition = parametricMapping.getOrDefault(realTransition, 'z');
		this.activatorPointer = this.activatorPointer.step(transition);
//        Activation step
		if (this.activated(transition)) {
			activationCounter++;
			AToken standReadyAToken = new AToken();
			ArrayList<State> standReadyATokensTemp = new ArrayList<>();
			boolean solved = false;
			boolean unclear = false;
			for (ConjunctAutomataRunner car : disjunctAutomataRunners) {
				//    step in the past (anyway)
				car.step(transition);
				// if we can retrieve a clear positive result no need for AToken to be launched
				if (!solved) {
					/* TODO for Version > 0.1 @Alessio
					 * Not suitable for parallel computation, force to a semaphore check at each step
					 * */
					if (car.hasClearResult()) {
						if (car.getCurrentResult(transition)) {
							solved = true;
							fulfilledActivationCounter++;
						}

					} else {
						unclear = true; //if at least one solution need to be checked in the future (if no positive certain answer)
//						standReadyAToken.addTokenToCollection(car.getAToken());
						//  check the future of the conjunction only if past and present are ok (or absent)
						if(car.getCurrentResult(transition)){
							standReadyATokensTemp.add(car.getAToken());
						}
					}
				}

			}
			// If no positive certain result and at least one result need to be checked in the future, launch AToken!
			if (!solved && unclear) {
				for (State s : standReadyATokensTemp) {
					standReadyAToken.addTokenToCollection(s);
				}
				aTokensRunners.add(new ATokenRunner(standReadyAToken));
			}

			// ATokens Step in the future
			for (ATokenRunner a : aTokensRunners) {
				a.step(transition);
			}
		} else {
			// step in the past
			for (ConjunctAutomataRunner car : disjunctAutomataRunners) {
				car.step(transition);
			}
			// ATokens Step in the future
			for (ATokenRunner a : aTokensRunners) {
				a.step(transition);
				/* TODO for Version > 0.1 @Alessio
				 * Check if a token ends up in a permanent violation/satisfaction state.
				 * - satisfaction: stop all tokens and return positive result
				 * - violation: remove token. if no token remaining return negative result
				 * */
			}


		}

	}

	private boolean activated(char transition) {
//		return automaton.getActivator().getInitialState().step(transition).isAccept();
		return activatorPointer.isAccept();
	}


	/**
	 * Reset the automaton state to make it ready for a new trace
	 */
	public void reset() {
		activationCounter = 0;
		fulfilledActivationCounter = 0;
		aTokensRunners = new ArrayList<ATokenRunner>();
		for (ConjunctAutomataRunner car : disjunctAutomataRunners) {
			car.reset();
		}
		activatorPointer = automaton.getActivator().getInitialState();
	}

	/**
	 * @return nominal name of the automaton concatenated with the specific letter used
	 */
	@Override
	public String toString() {
		StringBuffer a = new StringBuffer("(");
		for (char c : specificAlphabet) {
			a.append(c + ",");
		}
		return automaton.toString() + a.substring(0, a.length() - 1) + ")";
	}

}
