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
public class ChainPrecedence extends AlternatePrecedence {
    
	@Override
	public String getRegularExpressionTemplate() {
//		return "[^%2$s]*(%1$s%2$s[^%2$s]*)*[^%2$s]*";
		return "[^%1$s]*([%2$s][%1$s][^%1$s]*)*[^%1$s]*";
	}
	
	protected ChainPrecedence() {
		super();
	}

    public ChainPrecedence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }
    public ChainPrecedence(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }
    public ChainPrecedence(TaskCharSet param1, TaskCharSet param2, double support) {
		super(param1, param2, support);
	}
	public ChainPrecedence(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }
	
	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new AlternatePrecedence(implied, base);
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ChainPrecedence(taskChars[0], taskChars[1]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ChainPrecedence(taskCharSets[0], taskCharSets[1]);
	}

	@Override
	public SeparatedAutomaton buildParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'b', 'z'};
		char[] alphabetOthers = {alphabet[0], alphabet[2]};
		Automaton activator = Utils.getSingleCharActivatorAutomaton(alphabet[1], alphabetOthers);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

		char[] others = {alphabet[1],alphabet[2]};
		Automaton pastAutomaton = Utils.getReversedNextAutomaton(alphabet[0], others);

		ConjunctAutomata conjunctAutomaton = new ConjunctAutomata(pastAutomaton, null, null);

		disjunctAutomata.add(conjunctAutomaton);
		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}
}