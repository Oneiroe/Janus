package minerful.separated.automaton;

import dk.brics.automaton.State;
import minerful.logparser.LogTraceParser;

import java.util.Arrays;
import java.util.Map;


/**
 * Object to run a trace over conjunct automata
 */
public class ConjunctAutomataOfflineRunner {
    private ConjunctAutomata automata;

    private State currentPastState = null;
    private State currentPresentState = null;
    private State currentFutureState = null;


    /**
     * Initialize a runner for a given conjunct automata
     *
     * @param automata Conjunct Automata to be run
     */
    public ConjunctAutomataOfflineRunner(ConjunctAutomata automata) {
        this.automata = automata;

        if (automata.hasPast()) this.currentPastState = automata.getPastAutomaton().getInitialState();
        if (automata.hasPresent()) this.currentPresentState = automata.getPresentAutomaton().getInitialState();
        if (automata.hasFuture()) this.currentFutureState = automata.getFutureAutomaton().getInitialState();
    }

    /**
     * replay a trace on the automata and return a vector with the acceptance of each state
     *
     * @param trace trace as char[] to be evaluate by the conjunct automata.
     */
    public boolean[] evaluateTrace(char[] trace, Map<Character, Character> parametricMapping) {
//        BitSet result = new BitSet(trace.length);
        boolean[] result = new boolean[trace.length];
        Arrays.fill(result, Boolean.TRUE);

        //        run the trace onward for the past and present evaluation

        for (int i = 0; i < trace.length; i++) {
            char transition_onward = parametricMapping.getOrDefault(trace[i], 'z');
            char transition_backward = parametricMapping.getOrDefault(trace[trace.length-1-i], 'z');

            //        PAST
            if (currentPastState != null) {
                currentPastState = currentPastState.step(transition_onward);
                result[i] = result[i] && currentPastState.isAccept();
            }
            //        PRESENT
            if (currentPresentState != null) {
                currentPresentState = currentPresentState.step(transition_onward);
                result[i] = result[i] && currentPresentState.isAccept();
            }
            //        FUTURE (backward)
            if (currentFutureState != null) {
                currentFutureState = currentFutureState.step(transition_backward);
                result[trace.length-1-i] = result[trace.length-1-i] && currentFutureState.isAccept();
            }
        }

        return result;
    }


    /**
     * Reset the automata state to make it ready for a new trace
     */
    public void reset() {
        if (automata.hasPast()) this.currentPastState = automata.getPastAutomaton().getInitialState();
        if (automata.hasPresent()) this.currentPresentState = automata.getPresentAutomaton().getInitialState();
        if (automata.hasFuture()) this.currentFutureState = automata.getFutureAutomaton().getInitialState();
    }

}
