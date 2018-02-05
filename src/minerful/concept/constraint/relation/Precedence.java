/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.separated.automaton.ConjunctAutomata;
import minerful.separated.automaton.SeparatedAutomaton;

@XmlRootElement
public class Precedence extends RespondedExistence {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%2$s].*[%1$s])*[^%1$s]*";
	}

	protected Precedence() {
		super();
	}

	public Precedence(TaskChar param1, TaskChar param2) {
		super(param2, param1);
		this.invertOrderOfParams();
	}

	public Precedence(TaskChar param1, TaskChar param2, double support) {
		super(param2, param1, support);
		this.invertOrderOfParams();
	}

	public Precedence(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param2, param1, support);
		this.invertOrderOfParams();
	}

	public Precedence(TaskCharSet param1, TaskCharSet param2) {
		super(param2, param1);
		this.invertOrderOfParams();
	}

	protected void invertOrderOfParams() {
		Collections.reverse(this.parameters);
	}

	@Override
	public ConstraintImplicationVerse getImplicationVerse() {
		return ConstraintImplicationVerse.BACKWARD;
	}

	@Override
	public int getHierarchyLevel() {
		return super.getHierarchyLevel() + 1;
	}

	@Override
	public Integer getMaximumExpectedDistance() {
		if (this.isExpectedDistanceConfidenceIntervalProvided())
			return (int) Math.min(-1, StrictMath.round(expectedDistance + confidenceIntervalMargin));
		return null;
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new RespondedExistence(base, implied);
	}

	@Override
	protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		if (this.getFamily().equals(ConstraintFamily.RELATION)) {
			this.base = this.getParameters().get(1);
			this.implied = this.getParameters().get(0);
		}
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new Precedence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new Precedence(taskCharSets[0], taskCharSets[1]);
	}

	@Override
	public SeparatedAutomaton getParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'b', 'z'};
		Automaton activator = getSingleCharActivatorAutomaton(alphabet[1], alphabet);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

		char[] others = {alphabet[1], alphabet[2]};
		Automaton pastAutomaton = getEventualityAutomaton(alphabet[0], others);

		ConjunctAutomata conjunctAutomaton = new ConjunctAutomata(pastAutomaton, null, null);

		disjunctAutomata.add(conjunctAutomaton);
		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}

	/**
	 * Returns an automaton accepting only if the transition is equal to a specific activator character
	 *
	 * @param activator parametric character representing the activator in the parametric automaton
	 * @param others    all the parametric characters of the alphabet but the activator
	 * @return activator automaton
	 */
	private static Automaton getSingleCharActivatorAutomaton(char activator, char[] others) {
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
	private static Automaton getEventualityAutomaton(char desired, char[] others) {
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
}