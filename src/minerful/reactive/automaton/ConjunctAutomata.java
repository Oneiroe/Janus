package minerful.reactive.automaton;

import dk.brics.automaton.Automaton;


/**
 * Parametric conjunct Automata
 */
public class ConjunctAutomata {
    private Automaton pastAutomaton;
    private Automaton presentAutomaton;
    private Automaton futureAutomaton;

    /**
     * Separation theorem result may be a disjunction of separated automata.
     * Put null if one of the automaton is not present
     */
    public ConjunctAutomata(Automaton pastAutomaton, Automaton presentAutomaton, Automaton futureAutomaton) {
        this.pastAutomaton = pastAutomaton;
        this.presentAutomaton = presentAutomaton;
        this.futureAutomaton = futureAutomaton;
//        TODO optimization of automata (e.g. minimization, completion, ...)

    }

    public Automaton getPastAutomaton() {
        return pastAutomaton;
    }

    public Automaton getPresentAutomaton() {
        return presentAutomaton;
    }

    public Automaton getFutureAutomaton() {
        return futureAutomaton;
    }

    /**
     * @return true if the past automaton is present, false otherwise
     */
    public boolean hasPast() {
        return pastAutomaton != null;
    }

    /**
     * @return true if the present automaton is present, false otherwise
     */
    public boolean hasPresent() {
        return presentAutomaton != null;
    }

    /**
     * @return true if the future automaton is present, false otherwise
     */
    public boolean hasFuture() {
        return futureAutomaton != null;
    }
}
