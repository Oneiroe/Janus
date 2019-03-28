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
public class ExactlyOne extends ExistenceConstraint {
	@Override
	public String getRegularExpressionTemplate() {
		return "[^%1$s]*([%1$s][^%1$s]*){1,1}[^%1$s]*";
	}

	protected ExactlyOne() {
    	super();
    }

	public ExactlyOne(TaskChar param1, double support) {
		super(param1, support);
	}
	public ExactlyOne(TaskChar param1) {
		super(param1);
	}
	public ExactlyOne(TaskCharSet param1, double support) {
		super(param1, support);
	}
	public ExactlyOne(TaskCharSet param1) {
		super(param1);
	}

	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		return new Participation(getBase());
	}
	
	@Override
	public Constraint[] suggestImpliedConstraints() {
		Constraint[] impliCons = null;
		Constraint[] inheritedImpliCons = super.suggestImpliedConstraints();
		int i = 0;

		if (inheritedImpliCons != null) {
			impliCons = new Constraint[inheritedImpliCons.length + 1];
			for (Constraint impliCon : inheritedImpliCons) {
				impliCons[i++] = impliCon;
			}
		} else {
			impliCons = new Constraint[1];
		}
		impliCons[i++] = new AtMostOne(getBase());
		
		return impliCons;
	}

	@Override
	public ExistenceConstraintSubFamily getSubFamily() {
		return ExistenceConstraintSubFamily.NUMEROSITY;
	}

	@Override
	public Constraint copy(TaskChar... taskChars) {
		super.checkParams(taskChars);
		return new ExactlyOne(taskChars[0]);
	}

	@Override
	public Constraint copy(TaskCharSet... taskCharSets) {
		super.checkParams(taskCharSets);
		return new ExactlyOne(taskCharSets[0]);
	}

	@Override
	public SeparatedAutomaton buildParametricSeparatedAutomaton() {
		char[] alphabet = {'a', 'z'};
		char[] others = {alphabet[1]};
		Automaton activator = Utils.getExistentialActivatorAutomaton(alphabet);

		List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

		Automaton futureAutomaton = Utils.getPreciseParticipationAutomaton(alphabet[0], others, 1);
		ConjunctAutomata conjunctAutomaton = new ConjunctAutomata(null, null, futureAutomaton);

		disjunctAutomata.add(conjunctAutomaton);
		SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
		res.setNominalID(this.type);
		return res;
	}
}