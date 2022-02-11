package org.kohsuke.wordle;

import java.util.ArrayList;
import java.util.List;

/**
 * A word that was tried and matches given to it.
 *
 * @author Kohsuke Kawaguchi
 */
public class Guess {
    final String guess;
    final List<Hint> hints;

    public Guess(String guess, List<Hint> hints) {
        assert hints.size()==guess.length();

        this.guess = guess;
        this.hints = hints;
    }

    public Guess(String guess, String answer) {
        this(guess,Hint.make(answer,guess));
    }

    /**
     * True if the given string could be a valid answer consistent with this hint.
     */
    public boolean isConsistentWith(String answer) {
        return Hint.make(answer,guess).equals(hints);
    }

    /**
     * Returns true if the given word 'w' is acceptable next guess
     * within the constraint of this guess as the current guess.
     */
    public boolean allowsInHardMode(String w) {
        List<Character> bag = new ArrayList<>(guess.length());

        // make sure green constraints are honored. Letters not matched up this way can be used to provide yellows
        for (int i=0; i<hints.size(); i++) {
            if (hints.get(i)==Hint.GREEN) {
                if (w.charAt(i) != guess.charAt(i))
                    return false;
            } else {
                bag.add(w.charAt(i));
            }
        }

        // make sure yellow hints are used
        for (int i=0; i<hints.size(); i++) {
            if (hints.get(i)==Hint.YELLOW) {
                Character g = guess.charAt(i);
                if (!bag.remove(g))
                    return false;
            }
        }

        return true;
    }
}
