package org.kohsuke.wordle;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public enum Hint {
    /**
     * Position and the character matched.
     */
    GREEN('*',"\uD83D\uDFE9"),
    /**
     * Character matched but not the position.
     */
    YELLOW('-',"\uD83D\uDFE8"),
    /**
     * Invalid character.
     */
    GREY('.',"⬜");

    /**
     * ASCII version of this hint for easier parsing
     */
    final char letter;

    /**
     * String that renders to a colored box.
     */
    final String box;

    Hint(char letter, String box) {
        this.letter = letter;
        this.box = box;
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
            s.append(h.box);
        }
        return s.toString();
    }

    /**
     * Reverse of {@link #print(List)}
     */
    public static List<Hint> parse(String s) throws ParseException {
        var results = new ArrayList<Hint>(s.length());

        OUTER:
        for (int i=0; i<s.length(); i++) {
            char ch = s.charAt(i);
            for (Hint h : values()) {
                if (ch==h.letter) {
                    results.add(h);
                    continue OUTER;
                }
            }
            throw new ParseException("Unable to parse: "+s, i);
        }
        return results;
    }
}
