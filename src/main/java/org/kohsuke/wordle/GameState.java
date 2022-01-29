package org.kohsuke.wordle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Current game state, which consists of ...
 *
 * @author Kohsuke Kawaguchi
 */
public class GameState {
    /**
     * Guesses that we tried and hints given to them.
     */
    final List<Guess> guesses;
    /**
     * Possible answer words that are assumed to be equally likely
     */
    final WordList candidates;

    /**
     * Words that can be used as a guess.
     */
    final WordList options;

    public GameState(List<Guess> guesses, WordList candidates, WordList options) {
        this.guesses = guesses;
        this.candidates = candidates;
        this.options = options;
    }

    private static class Score implements Comparable<Score> {
        /**
         * Word considered as the next guess.
         */
        final String option;
        /**
         * Expected size of the candidates in the next round.
         */
        final int expectedSize;

        public Score(String option, int expectedSize) {
            this.option = option;
            this.expectedSize = expectedSize;
        }

        @Override
        public int compareTo(Score that) {
            // smaller the better
            return Integer.compare(this.expectedSize, that.expectedSize);
        }
    }

    /**
     * Figure out the best guess to attempt next.
     */
    public String chooseNextGuess() {
        return options.stream().parallel().map( o -> {
            /*
                The goal here is to narrow down the size of candidates as quickly as possible.

                when we try an option 'o', candidates would produce hints, but some candidates
                may produce identical hints. So the expected size of the next round of candidates is

                    Sum          { Probability of 'h' happening (Ph) * Size of candidates that are consistent with 'h' (Sh) }
                for each hint h

                Assuming all candidates are equally likely, Ph = Sh / Size of all candidates.
                Since we are just going to compare the expected size of the next round of candidates for each 'o'
                and pick the best one, the "Size of all candidates" part is a constant that multiplier that can be ignored.

                Thus, this basically boils down to grouping clusters by the hint they produce against 'o',
                and comparing their score:

                    Sum          Sh^2
                for each hint h

             */
            var clusterSizes = new HashMap<List<Hint>, AtomicInteger>();

            for (String c : candidates) {
                // c==o creates a cluster of size 1, but in that case the game ends, so really it'll create a cluster of zero.
                if (c.equals(o))    continue;

                var h = Hint.make(c, o);
                clusterSizes.computeIfAbsent(h, key -> new AtomicInteger(0)).incrementAndGet(); // just incrementing
            }

            int expectedSize = 0;
            for (var size : clusterSizes.values()) {
                expectedSize += size.get() * size.get();
            }

            return new Score(o, expectedSize);
        }).min(Comparator.naturalOrder()).get().option;
    }

    /**
     * Advance the game state to the next.
     */
    public GameState nextState(Guess guess) {
        var ng = new ArrayList<>(guesses);
        ng.add(guess);

        return new GameState(ng,
            candidates.select(guess::isConsistentWith),
            options);
    }
}
