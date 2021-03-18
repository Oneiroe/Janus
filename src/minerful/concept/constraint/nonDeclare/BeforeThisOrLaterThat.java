package minerful.concept.constraint.nonDeclare;

import dk.brics.automaton.Automaton;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.reactive.automaton.ConjunctAutomata;
import minerful.reactive.automaton.SeparatedAutomaton;
import minerful.reactive.automaton.Utils;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class BeforeThisOrLaterThat extends NonDeclareConstraint {

    public BeforeThisOrLaterThat() {
        super();
    }

    public BeforeThisOrLaterThat(TaskChar param1, TaskChar param2, TaskChar param3) {
        super();
        this.parameters.add(new TaskCharSet(param1));
        this.parameters.add(new TaskCharSet(param2));
        this.parameters.add(new TaskCharSet(param3));
        this.base = new TaskCharSet(param1);
    }

    public BeforeThisOrLaterThat(TaskChar param1, TaskChar param2, TaskChar param3, double support) {
        super();
        this.parameters.add(new TaskCharSet(param1));
        this.parameters.add(new TaskCharSet(param2));
        this.parameters.add(new TaskCharSet(param3));
        this.base = new TaskCharSet(param1);
        this.support = support;
    }

    public BeforeThisOrLaterThat(TaskCharSet param1, TaskCharSet param23, double support) {
        super(param1, param23, support);
    }

    public BeforeThisOrLaterThat(TaskCharSet param1, TaskCharSet param23) {
        super(param1, param23);
    }

    @Override
    public String getRegularExpressionTemplate() {
        return "([^%1$s]*)|([^%1$s]*%1$s[^%2$s]*)|([^%1$s]*%1$s[^%2$s]*%2$s[^%3$s]*%3$s[^%1$s]*)";
        // "([^A]*)|([^A]*A[^B]*)|([^A]*A[^B]*B[^X]*X[^A]*)";
    }

    @Override
    public String getRegularExpression() {
        return String.format(this.getRegularExpressionTemplate(),
                this.parameters.get(0).toPatternString(),
                this.parameters.get(1).toPatternString(),
                this.parameters.get(2).toPatternString()
        );
    }

    @Override
    public ConstraintFamily.ConstraintImplicationVerse getImplicationVerse() {
        return ConstraintFamily.ConstraintImplicationVerse.FORWARD;
    }

    @Override
    public TaskCharSet getImplied() {
        return null;
    }

    @Override
    public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
        return null;
    }

    @Override
    public Constraint copy(TaskChar... taskChars) {
        this.checkParams(taskChars);
        return new BeforeThisOrLaterThat(taskChars[0], taskChars[1], taskChars[2]);
    }

    @Override
    public Constraint copy(TaskCharSet... taskCharSets) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean checkParams(TaskChar... taskChars)
            throws IllegalArgumentException {
        return true;
    }

    @Override
    public boolean checkParams(TaskCharSet... taskCharSets)
            throws IllegalArgumentException {
        return true;
    }

    @Override
    public ConstraintFamily getFamily() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public int compareTo(Constraint t) {
        int result = super.compareTo(t);
        if (result == 0) {
            result = this.getClass().getCanonicalName().compareTo(t.getClass().getCanonicalName());
        }
        return result;
    }

    @Override
    public boolean isBranched() {
        return false;
    }


    @Override
    public SeparatedAutomaton buildParametricSeparatedAutomaton() {
        char[] alphabet = {'a', 'b', 'c', 'z'};
        Automaton activator = Utils.getSingleCharActivatorAutomaton(alphabet[0], alphabet);

        List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

//		Eventually in the future B
        char[] othersFut = {alphabet[0], alphabet[2], alphabet[3]};
        Automaton futureAutomaton = Utils.getEventualityAutomaton(alphabet[1], othersFut);
        ConjunctAutomata conjunctAutomatonFut = new ConjunctAutomata(null, null, futureAutomaton);
        disjunctAutomata.add(conjunctAutomatonFut);

//		Eventually in the past C
        char[] othersPast = {alphabet[0], alphabet[1], alphabet[3]};
        Automaton pastAutomaton = Utils.getEventualityAutomaton(alphabet[2], othersPast);
        ConjunctAutomata conjunctAutomatonPast = new ConjunctAutomata(pastAutomaton, null, null);
        disjunctAutomata.add(conjunctAutomatonPast);

        SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
        res.setNominalID(this.type);
        return res;
    }

}