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
public class CoExistence extends MutualRelationConstraint {
    @Override
    public String getRegularExpressionTemplate() {
        return "[^%1$s%2$s]*(([%1$s].*[%2$s].*)|([%2$s].*[%1$s].*))*[^%1$s%2$s]*";
    }

    protected CoExistence() {
        super();
    }

    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint, double support) {
        this(forwardConstraint.getBase(), forwardConstraint.getImplied(), support);
        if (!this.ckeckConsistency(forwardConstraint, backwardConstraint)) {
            throw new IllegalArgumentException("Illegal constraints combination: provided " + forwardConstraint + " and " + backwardConstraint + " resp. as forward and backward constraints of " + this);
        }
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }

    public CoExistence(RespondedExistence forwardConstraint, RespondedExistence backwardConstraint) {
        this(forwardConstraint.getBase(), forwardConstraint.getImplied());
        this.forwardConstraint = forwardConstraint;
        this.backwardConstraint = backwardConstraint;
    }


    public CoExistence(TaskCharSet param1, TaskCharSet param2, double support) {
        super(param1, param2, support);
    }

    public CoExistence(TaskCharSet param1, TaskCharSet param2) {
        super(param1, param2);
    }

    public CoExistence(TaskChar param1, TaskChar param2, double support) {
        super(param1, param2, support);
    }

    public CoExistence(TaskChar param1, TaskChar param2) {
        super(param1, param2);
    }

    @Override
    public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
        return null;
    }

    @Override
    public RespondedExistence getPossibleForwardConstraint() {
        return new RespondedExistence(base, implied);
    }

    @Override
    public RespondedExistence getPossibleBackwardConstraint() {
        return new RespondedExistence(implied, base);
    }

    @Override
    public Constraint copy(TaskChar... taskChars) {
        super.checkParams(taskChars);
        return new CoExistence(taskChars[0], taskChars[1]);
    }

    @Override
    public Constraint copy(TaskCharSet... taskCharSets) {
        super.checkParams(taskCharSets);
        return new CoExistence(taskCharSets[0], taskCharSets[1]);
    }

    @Override
    public SeparatedAutomaton buildParametricSeparatedAutomaton() {
        char[] alphabet = {'a', 'b', 'z'};
        char[] alphabetActivators = {alphabet[0], alphabet[1]};
        char[] alphabetOthers = {alphabet[2]};
        Automaton activator = Utils.getMultiCharActivatorAutomaton(alphabetActivators, alphabetOthers);

        List<ConjunctAutomata> disjunctAutomata = new ArrayList<ConjunctAutomata>();

        char[] others_0 = {alphabet[1], alphabet[2]}; // B Z
        char[] others_1 = {alphabet[0], alphabet[2]};  //A Z

        Automaton presentAutomaton_0 = Utils.getPresentAutomaton(alphabet[0], others_0); // now A
        Automaton presentAutomaton_1 = Utils.getPresentAutomaton(alphabet[1], others_1); // now B

        Automaton futureAutomaton_0 = Utils.getEventualityAutomaton(alphabet[0], others_0); // eventually future A
        Automaton futureAutomaton_1 = Utils.getEventualityAutomaton(alphabet[1], others_1); // eventually future B

        Automaton pastAutomaton_0 = Utils.getEventualityAutomaton(alphabet[0], others_0);  // eventually past A
        Automaton pastAutomaton_1 = Utils.getEventualityAutomaton(alphabet[1], others_1); // eventually past B

        //		A & Eventually in the future B
        ConjunctAutomata conjunctAutomatonFut_0 = new ConjunctAutomata(null, presentAutomaton_0, futureAutomaton_1);
        disjunctAutomata.add(conjunctAutomatonFut_0);

        //		A & Eventually in the past B
        ConjunctAutomata conjunctAutomatonPast_0 = new ConjunctAutomata(pastAutomaton_1, presentAutomaton_0, null);
        disjunctAutomata.add(conjunctAutomatonPast_0);

        //		B & Eventually in the future A
        ConjunctAutomata conjunctAutomatonFut_1 = new ConjunctAutomata(null, presentAutomaton_1, futureAutomaton_0);
        disjunctAutomata.add(conjunctAutomatonFut_1);

        //		B & Eventually in the past A
        ConjunctAutomata conjunctAutomatonPast_1 = new ConjunctAutomata(pastAutomaton_0, presentAutomaton_1, null);
        disjunctAutomata.add(conjunctAutomatonPast_1);

        SeparatedAutomaton res = new SeparatedAutomaton(activator, disjunctAutomata, alphabet);
        res.setNominalID(this.type);
        return res;
    }
}