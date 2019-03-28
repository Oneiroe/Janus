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
public class Participation extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){1,}[^%1$s]*";
	}

    protected Participation() {
    	super();
    }
	
	public Participation(TaskChar param1, double support) {
		super(param1, support);
	}
	public Participation(TaskChar param1) {
		super(param1);
	}
	public Participation(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public Participation(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return null;
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);	// check that parameters are OK
		return new Participation(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);	// check that parameters are OK
		return new Participation(taskCharSets[0]);
	}

	@Override
	public SeparatedAutomaton buildParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'z'};
		char[] others = {alphabet[1]};
		Automaton activator = Utils.getExistentialActivatorAutomaton(alphabet);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

		Automaton futureAutomaton = Utils.getEventualityAutomaton(alphabet[0], others);
		ConjunctAutomata conjunctAutomaton = new ConjunctAutomata(null, null, futureAutomaton);

		disjunctAutomata.add(conjunctAutomaton);
		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}
}
