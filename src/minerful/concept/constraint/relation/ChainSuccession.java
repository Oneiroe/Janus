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
public class ChainSuccession extends AlternateSuccession {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s%2$s]*([%1$s][%2$s][^%1$s%2$s]*)*[^%1$s%2$s]*";
	}
	
	protected ChainSuccession() {
		super();
	}

    public ChainSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        super(forwardConstraint, backwardConstraint);
    }
    public ChainSuccession(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        super(forwardConstraint, backwardConstraint, support);
    }
    public ChainSuccession(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public ChainSuccession(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public ChainSuccession(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param1, param2, support);
	}
	public ChainSuccession(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AlternateSuccession(base, implied);
	}

	@Override
	public ChainResponse getPossibleForwardConstraint() {
		return new ChainResponse(base, implied);
	}

	@Override
	public ChainPrecedence getPossibleBackwardConstraint() {
		return new ChainPrecedence(base, implied);
	}
	
	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ChainSuccession(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ChainSuccession(taskCharSets[0], taskCharSets[1]);
	}

	@Override
	public SeparatedAutomaton buildParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'b', 'z'};
		char[] alphabetActivators = {alphabet[0], alphabet[1]};
		char[] alphabetOthers = {alphabet[2]};
		Automaton activator = Utils.getMultiCharActivatorAutomaton(alphabetActivators, alphabetOthers);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

//		B & previous A
		char[] others_0 = {alphabet[1], alphabet[2]};  // B Z
		char[] others_0p = {alphabet[0], alphabet[2]};
		Automaton presentAutomaton_0 = Utils.getPresentAutomaton(alphabet[1], others_0p); // now B
		Automaton pastAutomaton_0 = Utils.getReversedNextAutomaton(alphabet[0], others_0);

		ConjunctAutomata conjunctAutomatonPast_0 = new ConjunctAutomata(pastAutomaton_0, presentAutomaton_0, null);
		disjunctAutomata.add(conjunctAutomatonPast_0);

//		A & next B
		char[] others_1 = {alphabet[0], alphabet[2]};  // A Z
		char[] others_1p = {alphabet[1], alphabet[2]};
		Automaton presentAutomaton_1 = Utils.getPresentAutomaton(alphabet[0], others_1p); // now A
		Automaton futureAutomaton_1 = Utils.getNextAutomaton(alphabet[1] ,others_1);

		ConjunctAutomata conjunctAutomatonFut_1 = new ConjunctAutomata(null, presentAutomaton_1, futureAutomaton_1);
		disjunctAutomata.add(conjunctAutomatonFut_1);


		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}
}