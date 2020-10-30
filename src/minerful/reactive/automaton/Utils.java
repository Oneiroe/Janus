package minerful.reactive.automaton;

import dk.brics.automaton.*;

import java.util.*;

public class Utils {
    /**
     * Returns the automaton accepting the reversed language of the input automaton.
     *
     * @param inAutomaton
     * @param alphabet
     * @return
     */
    public static Automaton getReversedAutomaton(Automaton inAutomaton, Set<Character> alphabet) {
//        Init result automaton as NFA (because the reversion result of a DFA is a NFA)
        Automaton result = new Automaton();
        result.setDeterministic(false);

//        automaton reversion
        State resInitialState = new State();
        resInitialState.setAccept(true);
        result.setInitialState(resInitialState);
        Set<State> newNFAInitStates = SpecialOperations.reverse(inAutomaton);
        Collection<StatePair> initialSet = new LinkedList<>();
        for (State init : newNFAInitStates) {
            initialSet.add(new StatePair(resInitialState, init));
        }
        result.addEpsilons(initialSet); //the initial state of the NFA is a state with epsilon transitions to all the initial states

//        determinize NFA
        BasicOperations.determinize(result);

        // automaton completion
        completeAutomaton(result, alphabet);

        return result;
    }

    /**
     * Complete the input automaton (side effect).
     * An automaton is completed when each state has a transition for each word of the alphabet
     *
     * @param inAutomaton
     * @param alphabet
     */
    public static void completeAutomaton(Automaton inAutomaton, Set<Character> alphabet) {
        // add default character to the alphabet
        alphabet.add('z');  // TODO make it parametric somewhere instead of hardcoding
        // initializing sink node
        State sink = new State();
        for (char transition : alphabet) {
            sink.addTransition(new Transition(transition, sink));
        }

        for (State st : inAutomaton.getStates()) {
            Set<Character> stateTransitions = new TreeSet<>();
            for (Transition trans : st.getTransitions()) {
                stateTransitions.add(trans.getMin());
            }
            for (char transition : alphabet) {
                if (stateTransitions.contains(transition)) continue;
                st.addTransition(new Transition(transition, sink));
            }
        }
    }

    /**
     * Returns an automaton accepting only if the transition is equal to a specific activator character
     *
     * @param activator parametric character representing the activator in the parametric automaton
     * @param others    all the parametric characters of the alphabet but the activator
     * @return activator automaton
     */
    public static Automaton getSingleCharActivatorAutomaton(char activator, char[] others) {
        State accepting = new State();
        accepting.setAccept(true);

        State notAccepting = new State();

        notAccepting.addTransition(new Transition(activator, accepting));
        accepting.addTransition(new Transition(activator, accepting));

        for (char o : others) {
            notAccepting.addTransition(new Transition(o, notAccepting));
            accepting.addTransition(new Transition(o, notAccepting));
        }

        Automaton res = new Automaton();

        res.setInitialState(notAccepting);
        return res;
    }

    /**
     * Returns an automaton accepting only at the beginning of the trace
     *
     * @param all all the parametric characters of the alphabet
     * @return activator automaton
     */
    public static Automaton getExistentialActivatorAutomaton(char[] all) {
        State initial = new State();
        initial.setAccept(true);
        State accepting = new State();
        accepting.setAccept(true);

        State notAccepting = new State();

        for (char o : all) {
            initial.addTransition(new Transition(o, accepting));
            accepting.addTransition(new Transition(o, notAccepting));
            notAccepting.addTransition(new Transition(o, notAccepting));
        }

        Automaton res = new Automaton();

        res.setInitialState(initial);
        return res;
    }

    /**
     * Returns an automaton accepting only if the transition is contained in a set of activator characters
     * e.g. A or B -> C , in this case the constrain is activated is the transition is A or B
     *
     * @param activators all the parametric characters representing the possible activators in the parametric automaton
     * @param others     all the parametric characters of the alphabet but the activators
     * @return activator automaton
     */
    public static Automaton getMultiCharActivatorAutomaton(char[] activators, char[] others) {
        State accepting = new State();
        accepting.setAccept(true);

        State notAccepting = new State();

        for (char activator : activators
        ) {
            notAccepting.addTransition(new Transition(activator, accepting));
            accepting.addTransition(new Transition(activator, accepting));
        }

        for (char o : others) {
            notAccepting.addTransition(new Transition(o, notAccepting));
            accepting.addTransition(new Transition(o, notAccepting));
        }

        Automaton res = new Automaton();

        res.setInitialState(notAccepting);
        return res;
    }

    /**
     * Get the automaton representing the <>A eventuality constraint for a desired letter of an alphabet
     *
     * @param desired desired character
     * @param others  alphabet without the desired character
     * @return automaton for <>desired
     */
    public static Automaton getEventualityAutomaton(char desired, char[] others) {
        State NonAcceptingState = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        NonAcceptingState.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            NonAcceptingState.addTransition(new Transition(other, NonAcceptingState));
        }
        AcceptingState.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, AcceptingState));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the !<>A negative eventuality constraint for a desired letter of an alphabet
     *
     * @param desired desired character
     * @param others  alphabet without the desired character
     * @return automaton for <>desired
     */
    public static Automaton getNegativeEventualityAutomaton(char desired, char[] others) {
        State NonAcceptingState = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        AcceptingState.addTransition(new Transition(desired, NonAcceptingState));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, AcceptingState));
        }
        NonAcceptingState.addTransition(new Transition(desired, NonAcceptingState));
        for (char other : others) {
            NonAcceptingState.addTransition(new Transition(other, NonAcceptingState));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(AcceptingState);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the ()(!<>A) constraint for a desired letter of an alphabet
     *
     * @param notDesired desired character
     * @param others     alphabet without the desired character
     * @return automaton for ()(!<>desired)
     */
    public static Automaton getNextNegativeEventualityAutomaton(char notDesired, char[] others) {
        State NonAcceptingStateInitial = new State();
        State NonAcceptingStateSink = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        NonAcceptingStateInitial.addTransition(new Transition(notDesired, AcceptingState));
        for (char other : others) {
            NonAcceptingStateInitial.addTransition(new Transition(other, AcceptingState));
        }
        AcceptingState.addTransition(new Transition(notDesired, NonAcceptingStateSink));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, AcceptingState));
        }
        NonAcceptingStateSink.addTransition(new Transition(notDesired, NonAcceptingStateSink));
        for (char other : others) {
            NonAcceptingStateSink.addTransition(new Transition(other, NonAcceptingStateSink));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingStateInitial);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the reverse of ()(!<>A) constraint for a desired letter of an alphabet
     *
     * @param notDesired desired character
     * @param others     alphabet without the desired character
     * @return reversed automaton for ()(!<>desired)
     */
    public static Automaton getReversedNextNegativeEventualityAutomaton(char notDesired, char[] others) {
        State NonAcceptingStateInitial = new State();
        State NonAcceptingStateSink = new State();
        State AcceptingStateOk = new State();
        State AcceptingStateLast = new State();
        AcceptingStateOk.setAccept(true);
        AcceptingStateLast.setAccept(true);

        NonAcceptingStateInitial.addTransition(new Transition(notDesired, AcceptingStateLast));
        for (char other : others) {
            NonAcceptingStateInitial.addTransition(new Transition(other, AcceptingStateOk));
        }
        AcceptingStateOk.addTransition(new Transition(notDesired, AcceptingStateLast));
        for (char other : others) {
            AcceptingStateOk.addTransition(new Transition(other, AcceptingStateOk));
        }
        AcceptingStateLast.addTransition(new Transition(notDesired, NonAcceptingStateSink));
        for (char other : others) {
            AcceptingStateLast.addTransition(new Transition(other, NonAcceptingStateSink));
        }
        NonAcceptingStateSink.addTransition(new Transition(notDesired, NonAcceptingStateSink));
        for (char other : others) {
            NonAcceptingStateSink.addTransition(new Transition(other, NonAcceptingStateSink));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingStateInitial);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the reverse of !A Until B constraint for two desired letters of an alphabet
     *
     * @param notHold character to hold false Until halt
     * @param halt    halting character
     * @param others  alphabet without the characters involved in the operation
     * @return reversed automaton for !A Until B
     */
    public static Automaton getReversedNegativeUntilAutomaton(char notHold, char halt, char[] others) {
        State NonAcceptingState = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        NonAcceptingState.addTransition(new Transition(halt, AcceptingState));
        NonAcceptingState.addTransition(new Transition(notHold, NonAcceptingState));
        for (char other : others) {
            NonAcceptingState.addTransition(new Transition(other, NonAcceptingState));
        }
        AcceptingState.addTransition(new Transition(notHold, NonAcceptingState));
        AcceptingState.addTransition(new Transition(halt, AcceptingState));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, AcceptingState));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the ()(!A Until B) constraint for two desired letters of an alphabet
     *
     * @param notHold character to hold false Until halt
     * @param halt    halting character
     * @param others  alphabet without the characters involved in the operation
     * @return automaton for ()(!A Until B)
     */
    public static Automaton getNextNegativeUntilAutomaton(char notHold, char halt, char[] others) {
        State NonAcceptingState_initial = new State();
        State NonAcceptingState = new State();
        State NonAcceptingState_sink = new State();
        State AcceptingState_b = new State();
        AcceptingState_b.setAccept(true);

        NonAcceptingState_initial.addTransition(new Transition(halt, NonAcceptingState));
        NonAcceptingState_initial.addTransition(new Transition(notHold, NonAcceptingState));
        for (char other : others) {
            NonAcceptingState_initial.addTransition(new Transition(other, NonAcceptingState));
        }

        NonAcceptingState.addTransition(new Transition(halt, AcceptingState_b));
        NonAcceptingState.addTransition(new Transition(notHold, NonAcceptingState_sink));
        for (char other : others) {
            NonAcceptingState.addTransition(new Transition(other, NonAcceptingState));
        }

        NonAcceptingState_sink.addTransition(new Transition(halt, NonAcceptingState_sink));
        NonAcceptingState_sink.addTransition(new Transition(notHold, NonAcceptingState_sink));
        for (char other : others) {
            NonAcceptingState_sink.addTransition(new Transition(other, NonAcceptingState_sink));
        }

        AcceptingState_b.addTransition(new Transition(halt, AcceptingState_b));
        AcceptingState_b.addTransition(new Transition(notHold, AcceptingState_b));
        for (char other : others) {
            AcceptingState_b.addTransition(new Transition(other, AcceptingState_b));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState_initial);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the reverse of ()(!A Until B) constraint for two desired letters of an alphabet
     *
     * @param notHold character to hold false Until halt
     * @param halt    halting character
     * @param others  alphabet without the characters involved in the operation
     * @return reversed automaton for ()(!A Until B)
     */
    public static Automaton getReversedNextNegativeUntilAutomaton(char notHold, char halt, char[] others) {
        State NonAcceptingState_initial = new State();
        State NonAcceptingState_b = new State();
        State AcceptingState_b = new State();
        State AcceptingState_a = new State();
        AcceptingState_b.setAccept(true);
        AcceptingState_a.setAccept(true);

        NonAcceptingState_initial.addTransition(new Transition(halt, NonAcceptingState_b));
        NonAcceptingState_initial.addTransition(new Transition(notHold, NonAcceptingState_initial));
        for (char other : others) {
            NonAcceptingState_initial.addTransition(new Transition(other, NonAcceptingState_initial));
        }

        NonAcceptingState_b.addTransition(new Transition(halt, AcceptingState_b));
        NonAcceptingState_b.addTransition(new Transition(notHold, AcceptingState_a));
        for (char other : others) {
            NonAcceptingState_b.addTransition(new Transition(other, AcceptingState_b));
        }

        AcceptingState_b.addTransition(new Transition(halt, AcceptingState_b));
        AcceptingState_b.addTransition(new Transition(notHold, AcceptingState_a));
        for (char other : others) {
            AcceptingState_b.addTransition(new Transition(other, AcceptingState_b));
        }

        AcceptingState_a.addTransition(new Transition(halt, NonAcceptingState_b));
        AcceptingState_a.addTransition(new Transition(notHold, NonAcceptingState_initial));
        for (char other : others) {
            AcceptingState_a.addTransition(new Transition(other, NonAcceptingState_initial));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState_initial);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the ()A constraint for the desired letter of an alphabet
     *
     * @param desired character
     * @param others  alphabet without the desired character
     * @return automaton for ()desired
     */
    public static Automaton getNextAutomaton(char desired, char[] others) {
        State NonAcceptingState_initial = new State();
        State NonAcceptingState_middle = new State();
        State NonAcceptingState_sink = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        NonAcceptingState_initial.addTransition(new Transition(desired, NonAcceptingState_middle));
        for (char other : others) {
            NonAcceptingState_initial.addTransition(new Transition(other, NonAcceptingState_middle));
        }
        NonAcceptingState_middle.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            NonAcceptingState_middle.addTransition(new Transition(other, NonAcceptingState_sink));
        }
        AcceptingState.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, AcceptingState));
        }
        NonAcceptingState_sink.addTransition(new Transition(desired, NonAcceptingState_sink));
        for (char other : others) {
            NonAcceptingState_sink.addTransition(new Transition(other, NonAcceptingState_sink));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState_initial);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the reverse of ()A constraint for the desired letter of an alphabet
     *
     * @param desired character
     * @param others  alphabet without the desired character
     * @return reversed automaton for ()desired
     */
    public static Automaton getReversedNextAutomaton(char desired, char[] others) {
        State NonAcceptingState_initial = new State();
        State NonAcceptingState_middle = new State();
        State AcceptingState_b = new State();
        State AcceptingState_c = new State();
        AcceptingState_b.setAccept(true);
        AcceptingState_c.setAccept(true);

        NonAcceptingState_initial.addTransition(new Transition(desired, NonAcceptingState_middle));
        for (char other : others) {
            NonAcceptingState_initial.addTransition(new Transition(other, NonAcceptingState_initial));
        }
        NonAcceptingState_middle.addTransition(new Transition(desired, AcceptingState_b));
        for (char other : others) {
            NonAcceptingState_middle.addTransition(new Transition(other, AcceptingState_c));
        }
        AcceptingState_b.addTransition(new Transition(desired, AcceptingState_b));
        for (char other : others) {
            AcceptingState_b.addTransition(new Transition(other, AcceptingState_c));
        }
        AcceptingState_c.addTransition(new Transition(desired, NonAcceptingState_middle));
        for (char other : others) {
            AcceptingState_c.addTransition(new Transition(other, NonAcceptingState_initial));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState_initial);

        return resAutomaton;
    }


    /**
     * Get the automaton representing the !()A constraint for the undesired letter of an alphabet
     *
     * @param undesired character
     * @param others    alphabet without the undesired character
     * @return automaton for !()undesired
     */
    public static Automaton getNegativeNextAutomaton(char undesired, char[] others) {
        State NonAcceptingState_initial = new State();
        State NonAcceptingState_middle = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);
        State NonAcceptingState_sink = new State();

        NonAcceptingState_initial.addTransition(new Transition(undesired, NonAcceptingState_middle));
        for (char other : others) {
            NonAcceptingState_initial.addTransition(new Transition(other, NonAcceptingState_middle));
        }
        NonAcceptingState_middle.addTransition(new Transition(undesired, NonAcceptingState_sink));
        for (char other : others) {
            NonAcceptingState_middle.addTransition(new Transition(other, AcceptingState));
        }
        NonAcceptingState_sink.addTransition(new Transition(undesired, NonAcceptingState_sink));
        for (char other : others) {
            NonAcceptingState_sink.addTransition(new Transition(other, NonAcceptingState_sink));
        }
        AcceptingState.addTransition(new Transition(undesired, AcceptingState));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, AcceptingState));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState_initial);

        return resAutomaton;
    }


    /**
     * Get the automaton representing the reverse of !()A constraint for the undesired letter of an alphabet
     *
     * @param undesired character
     * @param others    alphabet without the undesired character
     * @return reversed automaton for !()undesired
     */
    public static Automaton getNegativeReversedNextAutomaton(char undesired, char[] others) {
        State AcceptingState_initial = new State();
        State AcceptingState_middle = new State();
        AcceptingState_initial.setAccept(true);
        AcceptingState_middle.setAccept(true);
        State NonAcceptingState_b = new State();
        State NonAcceptingState_c = new State();

        AcceptingState_initial.addTransition(new Transition(undesired, AcceptingState_middle));
        for (char other : others) {
            AcceptingState_initial.addTransition(new Transition(other, AcceptingState_initial));
        }
        AcceptingState_middle.addTransition(new Transition(undesired, NonAcceptingState_b));
        for (char other : others) {
            AcceptingState_middle.addTransition(new Transition(other, NonAcceptingState_c));
        }
        NonAcceptingState_b.addTransition(new Transition(undesired, NonAcceptingState_b));
        for (char other : others) {
            NonAcceptingState_b.addTransition(new Transition(other, NonAcceptingState_c));
        }
        NonAcceptingState_c.addTransition(new Transition(undesired, AcceptingState_middle));
        for (char other : others) {
            NonAcceptingState_c.addTransition(new Transition(other, AcceptingState_initial));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(AcceptingState_initial);

        return resAutomaton;
    }


    /**
     * Get the automaton representing the []<>A constraint for a desired letter of an alphabet
     *
     * @param desired desired character
     * @param others  alphabet without the desired character
     * @return automaton for []<>desired
     */
    public static Automaton getLastAutomaton(char desired, char[] others) {
        State NonAcceptingState = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        NonAcceptingState.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            NonAcceptingState.addTransition(new Transition(other, NonAcceptingState));
        }
        AcceptingState.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, NonAcceptingState));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState);

        return resAutomaton;
    }

    /**
     * Get the automaton representing the []<->A constraint FROM START for a desired letter of an alphabet
     *
     * @param desired desired character
     * @param others  alphabet without the desired character
     * @return automaton for []<->desired
     */
    public static Automaton getFirstAutomaton(char desired, char[] others) {
        State NonAcceptingStateInitial = new State();
        State NonAcceptingState = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        NonAcceptingStateInitial.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            NonAcceptingStateInitial.addTransition(new Transition(other, NonAcceptingState));
        }
        NonAcceptingState.addTransition(new Transition(desired, NonAcceptingState));
        for (char other : others) {
            NonAcceptingState.addTransition(new Transition(other, NonAcceptingState));
        }
        AcceptingState.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            AcceptingState.addTransition(new Transition(other, AcceptingState));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingStateInitial);

        return resAutomaton;
    }


    /**
     * Get the automaton representing the "exactly N occurrences of A" constraint for a desired letter of an alphabet
     *
     * @param desired desired character
     * @param others  alphabet without the desired character
     * @param n       precise number of participation desired
     * @return automaton for "exactly N occurrences of desired"
     */
    public static Automaton getPreciseParticipationAutomaton(char desired, char[] others, int n) {
        /* TODO check n>0*/
        State NonAcceptingStateInitial = new State();
        State NonAcceptingStateSink = new State();

        State lastState = NonAcceptingStateInitial;

        for (int i = 0; i < n; i++) {
            for (char other : others) {
                lastState.addTransition(new Transition(other, lastState));
            }

            State nextState = new State();

            lastState.addTransition(new Transition(desired, nextState));
            lastState = nextState;
        }

        lastState.setAccept(true);

        lastState.addTransition(new Transition(desired, NonAcceptingStateSink));
        for (char other : others) {
            lastState.addTransition(new Transition(other, lastState));
        }

        NonAcceptingStateSink.addTransition(new Transition(desired, NonAcceptingStateSink));
        for (char other : others) {
            NonAcceptingStateSink.addTransition(new Transition(other, NonAcceptingStateSink));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingStateInitial);

        return resAutomaton;
    }

    /**
     * Get the automaton checking if the current transition is equal to a desired letter of an alphabet
     * i.e. checks the present
     *
     * @param desired desired character
     * @param others  alphabet without the desired character
     * @return automaton for []<->desired
     */
    public static Automaton getPresentAutomaton(char desired, char[] others) {
        State NonAcceptingState_initial = new State();
        State AcceptingState = new State();
        AcceptingState.setAccept(true);

        NonAcceptingState_initial.addTransition(new Transition(desired, AcceptingState));
        AcceptingState.addTransition(new Transition(desired, AcceptingState));
        for (char other : others) {
            NonAcceptingState_initial.addTransition(new Transition(other, NonAcceptingState_initial));
            AcceptingState.addTransition(new Transition(other, NonAcceptingState_initial));
        }

        Automaton resAutomaton = new Automaton();
        resAutomaton.setInitialState(NonAcceptingState_initial);

        return resAutomaton;
    }

}
