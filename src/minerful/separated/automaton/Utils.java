package minerful.separated.automaton;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class Utils {
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
	 * Get the automaton representing the reverse of !A Until B constraint for two desired letters of an alphabet
	 *
	 * @param notHold character to hold false Until halt
	 * @param halt halting character
	 * @param others alphabet without the characters involved in the operation
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
	 * Get the automaton representing the reverse of ()(!A Until B) constraint for two desired letters of an alphabet
	 *
	 * @param notHold character to hold false Until halt
	 * @param halt halting character
	 * @param others alphabet without the characters involved in the operation
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




}
