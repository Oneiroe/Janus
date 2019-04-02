package minerful.reactive.automaton;

import dk.brics.automaton.*;

import java.util.*;


/**
 * Object to run a trace over conjunct automata
 */
public class ConjunctAutomataOfflineRunner {
    private ConjunctAutomata automata;
    private Collection<Character> alphabet;

    private State currentPastState = null;
    private State currentPresentState = null;
    private State currentFutureState = null;


    private State initialPastState = null;
    private State initialPresentState = null;
    private State initialFutureState = null;


    /**
     * Initialize a runner for a given conjunct automata
     *  @param automata Conjunct Automata to be run
     * @param alphabet
     */
    public ConjunctAutomataOfflineRunner(ConjunctAutomata automata, Collection<Character> alphabet) {
        this.automata = automata;
        this.alphabet =alphabet;

        if (automata.hasPast()) {
            this.initialPastState = automata.getPastAutomaton().getInitialState();
            this.currentPastState = this.initialPastState;
        }
        if (automata.hasPresent()) {
            this.initialPresentState = automata.getPresentAutomaton().getInitialState();
            this.currentPresentState = this.initialPresentState;
        }
        if (automata.hasFuture()) {
//            Reversed future for offline settings
            Automaton newFut = Utils.getReversedAutomaton(automata.getFutureAutomaton(), (Set) alphabet);
            this.initialFutureState = newFut.getInitialState();
            this.currentFutureState = this.initialFutureState;
        }
    }

    /**
     * replay a trace on the automata and return a vector with the acceptance of each state
     *
     * @param trace trace as char[] to be evaluate by the conjunct automata.
     */
    public boolean[] evaluateTrace(char[] trace, int traceLength, Map<Character, Character> parametricMapping) {

        boolean[] result = new boolean[traceLength];
        Arrays.fill(result, Boolean.TRUE);

        for (int i = 0; i < traceLength; i++) {
            char transition_onward = parametricMapping.getOrDefault(trace[i], 'z');

            //        PAST
            if (currentPastState != null) {
                currentPastState = currentPastState.step(transition_onward);
                result[i] &= currentPastState.isAccept();
            }
            //        PRESENT
            if (currentPresentState != null) {
                currentPresentState = currentPresentState.step(transition_onward);
                result[i] &= currentPresentState.isAccept();
            }
            //        FUTURE (backward)
            if (currentFutureState != null) {
                currentFutureState = currentFutureState.step(parametricMapping.getOrDefault(trace[traceLength - 1 - i], 'z'));
                result[traceLength - 1 - i] &= currentFutureState.isAccept();
            }
        }

        return result;
    }


    /**
     * Reset the automata state to make it ready for a new trace
     */
    public void reset() {
        this.currentPastState = this.initialPastState;
        this.currentPresentState = this.initialPresentState;
        this.currentFutureState = this.initialFutureState;
    }

}
