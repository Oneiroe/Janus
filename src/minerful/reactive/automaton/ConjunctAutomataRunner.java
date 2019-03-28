package minerful.reactive.automaton;

import dk.brics.automaton.State;

/**
 * Object to run a trace over conjunct automata
 */
public class ConjunctAutomataRunner {
    private ConjunctAutomata automata;

    private State currentPastState = null;
    private State currentPresentState = null;

    //    This collection it cannot be a set as different token in same position must be considered separately
//    private List<State> currentFutureTokens = null; //ATokens!!!


    /**
     * Initialize a runner for a given conjunct automata
     *
     * @param automata Conjunct Automata to be run
     */
    public ConjunctAutomataRunner(ConjunctAutomata automata) {
        this.automata = automata;

        if (automata.hasPast()) this.currentPastState = automata.getPastAutomaton().getInitialState();
        if (automata.hasPresent()) this.currentPresentState = automata.getPresentAutomaton().getInitialState();
    }

    /**
     * Perform a single step in the automata using the given transition
     */
    public void step(char transition) {
//        PAST step
        if (currentPastState != null) {
            currentPastState = currentPastState.step(transition);
        }

//        PRESENT step not needed because it's only evaluated from its starting state at evaluation/activation time
//        (see getCurrentResult method)
//            Future Research: present violation imply activation?

//        FUTURE step carried out by AToken object
    }

    /**
     * @return current state pointer of the past automaton
     */
    public State getCurrentPastState() {
        return currentPastState;
    }

    /**
     * Add a new token in starting state of future automaton and give the reference to AToken object
     *
     * @return the new token state
     */
    public State getAToken() {
        if (automata.hasFuture()) {
//            TODO BEWARE! side effect on State object? AKA: are we giving to each one the same object reference?
            return automata.getFutureAutomaton().getInitialState();
        } else return null;
    }

    /**
     * Reset the automata state to make it ready for a new trace
     */
    public void reset() {
        if (automata.hasPast()) this.currentPastState = automata.getPastAutomaton().getInitialState();
        if (automata.hasPresent()) this.currentPresentState = automata.getPresentAutomaton().getInitialState();
    }

    /**
     * If no future automaton is present is possible to retrieve the result of this conjunct automata immediately
     *
     * @return true if in the current state it is possible to have a certain result
     */
    public boolean hasClearResult() {
        return !automata.hasFuture();
    }

    /**
     * BEWARE before calling this method be sure to check hasClearResult() returns true
     *
     * @return true is the current state is accepting, false otherwise
     */
    public boolean getCurrentResult(char transition) {
        Boolean res = true;
        if (automata.hasPast()) {
            res = res && currentPastState.isAccept();
        }
        if (automata.hasPresent()) {
            res = res && currentPresentState.step(transition).isAccept();
        }
        return res;
    }
}
