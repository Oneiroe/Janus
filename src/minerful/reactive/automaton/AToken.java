package minerful.reactive.automaton;

import dk.brics.automaton.State;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class to manage a set of token of different future automata related by the same activation.
 */
public class AToken {
    private Set<State> tokensCollection;


    /**
     * Initialize an empty AToken
     */
    public AToken() {
        this.tokensCollection = new TreeSet<State>();
    }

    /**
     * Initialize an AToken with a collection of state pointers
     *
     * @param tokensCollection
     */
    public AToken(Set<State> tokensCollection) {
        this.tokensCollection = tokensCollection;
    }

    /**
     * @return the token collection
     */
    public Set<State> getTokensCollection() {
        return tokensCollection;
    }

    /**
     * @param token new token
     */
    public void addTokenToCollection(State token) {
        this.tokensCollection.add(token);
    }

    /**
     * @param token to be removed
     */
    public void removeTokenFromCollection(State token) {
        this.tokensCollection.remove(token);
    }
}
