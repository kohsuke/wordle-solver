package org.kohsuke.wordle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public enum Hint {
    /**
     * Position and the character matched.
     */
    GREEN('*'),
    /**
     * Character matched but not the position.
     */
    YELLOW('-'),
    /**
     * Invalid character.
     */
    GREY(' ');

    final char letter;

    Hint(char letter) {
        this.letter = letter;
    }

    static List<Hint> make(String answer, String guess) {
        List<Character> bag = new ArrayList<>(answer.length());
        List<Hint> hints = new ArrayList<>(guess.length());

        // figure out where are greens. Letters not matched up this way can be used to provide yellows
        for (int i=0; i<guess.length(); i++) {
            char a = answer.charAt(i);
            char g = guess.charAt(i);

            if (a==g) {
                hints.add(GREEN);
            } else {
                hints.add(GREY);    // will flip this to yellow later
                bag.add(a);
            }
        }

        // figure out yellows. the rest are grey
        for (int i=0; i<guess.length(); i++) {
            Character g = guess.charAt(i);

            if (hints.get(i)==GREEN)
                continue;

            if (bag.remove(g))
                hints.set(i, YELLOW);
        }

        return hints;
    }

    public static String print(List<Hint> hints) {
        var s = new StringBuilder(hints.size());
        for (Hint h : hints) {
            s.append(h.letter);
        }
        return s.toString();
    }
}
