package org.kohsuke.wordle;

import java.util.ArrayList;
import java.util.List;

import static org.kohsuke.wordle.Hint.*;

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

    /**
     * True if the given string could be a valid answer consistent with this hint.
     */
    public boolean isConsistentWith(String answer) {
        return Hint.make(answer,guess).equals(hints);
    }
}
