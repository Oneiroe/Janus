package minerful.reactive.automaton;

import dk.brics.automaton.State;

/**
 * Class to run traces over a set of tokens (AToken!) related to the same activation
 */
public class ATokenRunner {
    private AToken aToken;

    /**
     * Initialize a runner for a specific AToken
     *
     * @param aToken object containing the tokens related to an activation
     */
    public ATokenRunner(AToken aToken) {
        this.aToken = aToken;
    }

    /**
     * @return the AToken associated to the runner
     */
    public AToken getaToken() {
        return aToken;
    }

    /**
     * move the tokens of one step according to the given transition
     *
     * @param transition transition to perform
     */
    public void step(char transition) {
        AToken newAToken = new AToken();
        for (State token : aToken.getTokensCollection()) {
            newAToken.addTokenToCollection(token.step(transition));
        }
        aToken = newAToken;
    }


    /**
     * Retrieve the result of AToken set in the current state
     *
     * @return true is at least one token is in accepting state
     */
    public boolean getCurrentResult() {
        for (State token : aToken.getTokensCollection()) {
            if (token.isAccept()) return true;
        }
        return false;
    }

}
