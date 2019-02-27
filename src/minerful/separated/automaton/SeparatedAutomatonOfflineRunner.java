package minerful.separated.automaton;

import dk.brics.automaton.State;
import minerful.logparser.LogTraceParser;

import java.util.*;

/**
 * Object to run a trace over a separated automata in offline setting.
 * This is done in O(2n) time through the double versersion technique, i.e., one past going forward and one future going backward.
 * It is assumed that the automata are already properly reversed.
 */
public class SeparatedAutomatonOfflineRunner {
    private SeparatedAutomaton automaton;

    //    REMEMBER that separated automaton is a disjunction of conjunction!!!
    private List<ConjunctAutomataOfflineRunner> disjunctAutomataOfflineRunners; //  it takes care of past and present

    private List<Character> specificAlphabet;
    private Map<Character, Character> parametricMapping;


    /**
     * Initialize a runner object to run trace on a given separated automaton.
     * For each disjunct automata of the spared automaton is initialized a specific runner
     *
     * @param automaton        on which running the analysis
     * @param specificAlphabet ordered array of character from the trace to be used in the parametric automaton
     */
    public SeparatedAutomatonOfflineRunner(SeparatedAutomaton automaton, List<Character> specificAlphabet) {
        this.automaton = automaton;
        this.disjunctAutomataOfflineRunners = new ArrayList<ConjunctAutomataOfflineRunner>();
        this.parametricMapping = new HashMap<Character, Character>();

        this.specificAlphabet = specificAlphabet;
        char[] par = automaton.getParametricAlphabet();
        for (int i = 0; i < specificAlphabet.size(); i++) {
            parametricMapping.put(specificAlphabet.get(i), par[i]);
        }
//        it is better to put the present automaton as first of the list for performance speedup
//        BUT pasts must be carried on any way
        for (ConjunctAutomata ca : automaton.getDisjunctAutomata()) {
            this.disjunctAutomataOfflineRunners.add(new ConjunctAutomataOfflineRunner(ca));
        }

    }

    /**
     * Perform a single step in the separated automata using the given transition
     */
    public boolean[] runTrace(char[] trace, int traceLength, LogTraceParser logTraceParser) {
        boolean[] result = new boolean[traceLength];
        for (ConjunctAutomataOfflineRunner car : disjunctAutomataOfflineRunners) {
            int i = 0;
            for (boolean eval : car.evaluateTrace(trace, traceLength, parametricMapping)) {
                result[i] |= eval;
                i++;
            }
        }
        return result;
    }


    /**
     * Reset the automaton state to make it ready for a new trace
     */
    public void reset() {
        for (ConjunctAutomataOfflineRunner car : disjunctAutomataOfflineRunners) {
            car.reset();
        }
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
