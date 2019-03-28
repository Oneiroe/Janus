package minerful.reactive.automaton;

import dk.brics.automaton.Automaton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parametric Separated Automaton (i.e. disjunction of conjunction of past/present/future automata)
 */
public class SeparatedAutomaton {

    private char[] parametricAlphabet;
    private Set<ConjunctAutomata> disjunctAutomata; //memo. Separation theorem result is a disjunction of separated automata
    private Automaton activator;
    private String nominalID;

    /* TODO for Version > 0.1 @Alessio
     * The activator is represented as automaton for future extensions.
     * In this version should be way better to directly check if
     * the current trace character is equal to the activation one.
     * */


    /**
     * Initialize an empty separated automaton
     */
    public SeparatedAutomaton() {
        this.disjunctAutomata = new HashSet<ConjunctAutomata>();
    }

    /**
     * Initialize a separated automaton with an activator automaton
     *
     * @param activator automaton
     */
    public SeparatedAutomaton(Automaton activator) {
        this.activator = activator;
        this.disjunctAutomata = new HashSet<ConjunctAutomata>();
    }

    /**
     * Initialize a separated automaton with a lis of conjunct automata and an activator
     *
     * @param disjunctionOf collection of conjunct automata, in disjunction within this separated automaton
     * @param activator     automaton
     */
    public SeparatedAutomaton(Automaton activator, List<ConjunctAutomata> disjunctionOf) {
        this.activator = activator;
        this.disjunctAutomata = new HashSet<ConjunctAutomata>();
        this.disjunctAutomata.addAll(disjunctionOf);
    }

    /**
     * Initialize a separated automaton with a lis of conjunct automata, an activator, and the parametric alphabet
     *
     * @param disjunctionOf collection of conjunct automata, in disjunction within this separated automaton
     * @param activator     automaton
     */
    public SeparatedAutomaton(Automaton activator, List<ConjunctAutomata> disjunctionOf, char[] parametricAlphabet) {
        this.activator = activator;
        this.disjunctAutomata = new HashSet<ConjunctAutomata>();
        this.disjunctAutomata.addAll(disjunctionOf);
        this.parametricAlphabet = parametricAlphabet;
    }

    /**
     * @return Set of disjunct automata
     */
    public Set<ConjunctAutomata> getDisjunctAutomata() {
        return disjunctAutomata;
    }

    /**
     * Ad a new conjunct automata to the disjunction set
     *
     * @param newConjunction triple of conjunct automata to be added
     */
    public void addDisjunctionAutomata(ConjunctAutomata newConjunction) {
        this.disjunctAutomata.add(newConjunction);
    }

    /**
     * @return activator automaton
     */
    public Automaton getActivator() {
        return activator;
    }

    /**
     * @param activator new activator automaton
     */
    public void setActivator(Automaton activator) {
        this.activator = activator;
    }

    /**
     * @return ordered list of the characters of the alphabet used by the parametric automaton
     */
    public char[] getParametricAlphabet() {
        return parametricAlphabet;
    }

    /**
     * @param parametricAlphabet ordered list of character to be used by the parametric automaton
     */
    public void setParametricAlphabet(char[] parametricAlphabet) {
        this.parametricAlphabet = parametricAlphabet;
    }

    /**
     * @return nominal name of the automaton is set
     */
    @Override
    public String toString() {
        if (nominalID != null) {
            return nominalID;
        } else {
            return super.toString();
        }
    }

    /**
     * @return nominal ID of the automaton
     */
    public String getNominalID() {
        return this.nominalID;
    }

    /**
     * "human" Name of the automaton for toString function
     * @param nominalID new nominal ID of the automaton
     */
    public void setNominalID(String nominalID) {
        this.nominalID = nominalID;
    }
}
