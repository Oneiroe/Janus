/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.existence;

import javax.xml.bind.annotation.XmlRootElement;

import dk.brics.automaton.Automaton;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;
import minerful.reactive.automaton.ConjunctAutomata;
import minerful.reactive.automaton.SeparatedAutomaton;
import minerful.reactive.automaton.Utils;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class Init extends Participation {
	@Override
	public String getRegularExpressionTemplate() {
		return "[%1$s].*";
	}
	
    protected Init() {
    	super();
    }

    public Init(TaskChar param1, double support) {
		super(param1, support);
	}
	public Init(TaskChar param1) {
        super(param1);
    }
	public Init(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public Init(TaskCharSet param1) {
		super(param1);
	}

	@Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel() + 1;
    }

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new Participation(this.base);
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.POSITION;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);	// check that parameters are OK
		return new Init(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);	// check that parameters are OK
		return new Init(taskCharSets[0]);
	}

	@Override
	public SeparatedAutomaton buildParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'z'};
		Automaton activator = Utils.getExistentialActivatorAutomaton(alphabet);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

		char[] others = {alphabet[1]};
		Automaton futureAutomaton = Utils.getFirstAutomaton(alphabet[0], others);
		ConjunctAutomata conjunctAutomaton = new ConjunctAutomata(null, null, futureAutomaton);

		disjunctAutomata.add(conjunctAutomaton);
		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}
}