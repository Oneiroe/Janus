/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import javax.xml.bind.annotation.XmlRootElement;

import dk.brics.automaton.Automaton;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.reactive.automaton.ConjunctAutomata;
import minerful.reactive.automaton.SeparatedAutomaton;
import minerful.reactive.automaton.Utils;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NotSuccession extends NotChainSuccession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%2$s]*)*[^%1$s%2$s]*";
	}
	
	protected NotSuccession() {
		super();
	}

    public NotSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public NotSuccession(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public NotSuccession(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param1, param2, support);
	}
	public NotSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    @Override
    public void setOpposedTo(RelationConstraint opposedTo) {
        super.setOpponent(opposedTo, Succession.class);
    }
    

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new NotChainSuccession(base, implied);
	}

	@Override
	public Constraint getSupposedOpponentConstraint() {
		return new Succession(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new NotSuccession(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new NotSuccession(taskCharSets[0], taskCharSets[1]);
	}

	@Override
	public SeparatedAutomaton buildParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'b', 'z'};
		char[] alphabetActivators = {alphabet[0], alphabet[1]};
		char[] alphabetOthers = {alphabet[2]};
		Automaton activator = Utils.getMultiCharActivatorAutomaton(alphabetActivators, alphabetOthers);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

		char[] others_0 = {alphabet[1], alphabet[2]};  // B Z
		char[] others_1 = {alphabet[0], alphabet[2]};  // A Z

		Automaton presentAutomaton_0 = Utils.getPresentAutomaton(alphabet[0], others_0); // now A
		Automaton presentAutomaton_1 = Utils.getPresentAutomaton(alphabet[1], others_1); // now B
		Automaton futureAutomaton_1 = Utils.getNegativeEventualityAutomaton(alphabet[1], others_1); // not eventually future B
		Automaton pastAutomaton_0 = Utils.getReversedNextNegativeEventualityAutomaton(alphabet[0], others_0);  // not eventually past A

//		A & not eventually in the future B
		ConjunctAutomata conjunctAutomatonFut_0 = new ConjunctAutomata(null, presentAutomaton_0, futureAutomaton_1);
		disjunctAutomata.add(conjunctAutomatonFut_0);

//		B & not eventually in the past A
		ConjunctAutomata conjunctAutomatonPast_1 = new ConjunctAutomata(pastAutomaton_0, presentAutomaton_1, null);
		disjunctAutomata.add(conjunctAutomatonPast_1);

		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}

}