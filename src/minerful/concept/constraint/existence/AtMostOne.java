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
public class AtMostOne extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){0,1}[^%1$s]*";
	}

	protected AtMostOne() {
    	super();
    }

	public AtMostOne(TaskChar param1, double support) {
		super(param1, support);
	}
	public AtMostOne(TaskChar param1) {
		super(param1);
	}
	public AtMostOne(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public AtMostOne(TaskCharSet param1) {
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
		super.checkParams(taskChars);
		return new AtMostOne(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new AtMostOne(taskCharSets[0]);
	}

	@Override
	public SeparatedAutomaton buildParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'z'};
		char[] others = {alphabet[1]};
		Automaton activator = Utils.getSingleCharActivatorAutomaton(alphabet[0], others);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

		Automaton futureAutomaton = Utils.getNextNegativeEventualityAutomaton(alphabet[0], others);
		Automaton pastAutomaton = Utils.getReversedNextNegativeEventualityAutomaton(alphabet[0], others);
		ConjunctAutomata conjunctAutomaton = new ConjunctAutomata(pastAutomaton, null, futureAutomaton);

		disjunctAutomata.add(conjunctAutomaton);
		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}
}