package minerful.reactive.automaton;

import dk.brics.automaton.State;

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
//        (parametric)Alphabet required in order to reverse the future automata
        LinkedHashSet<Character> alphabet =  new LinkedHashSet<>(parametricMapping.values());
        for (ConjunctAutomata ca : automaton.getDisjunctAutomata()) {
            this.disjunctAutomataOfflineRunners.add(new ConjunctAutomataOfflineRunner(ca, alphabet));
        }

    }

    /**
     * run the separatedAutomaton on the given trace
     */
    public void runTrace(char[] trace, int traceLength, byte[] result) {
        //        Target
        for (ConjunctAutomataOfflineRunner car : disjunctAutomataOfflineRunners) {
            int i = 0;
            for (boolean eval : car.evaluateTrace(trace, traceLength, parametricMapping)) {
                result[i] |= (eval) ? 1 : 0;
                i++;
            }
        }
        //        Activation
        State activatorPointer = automaton.getActivator().getInitialState();
        for (int i = 0; i < traceLength; i++) {
            char transition_onward = parametricMapping.getOrDefault(trace[i], 'z');
            activatorPointer = activatorPointer.step(transition_onward);
            result[i] += (activatorPointer.isAccept()) ? 2 : 0; // we are adding the second bit on the left, i.e., [activator-bit][target-bit]
        }

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

    /**
     * @return nominal name of the automaton concatenated with the specific letters substituted with the real events
     */
    public String toStringDecoded(Map map) {
        StringBuffer a = new StringBuffer("(");
        for (char c : specificAlphabet) {
            a.append(map.get(c) + ",");
        }
        return automaton.toString() + a.substring(0, a.length() - 1) + ")";
    }

    /**
     * Get the Separated automaton object of the runner
     *
     * @return
     */
    public SeparatedAutomaton getAutomaton() {
        return automaton;
    }

}
