package org.kohsuke.wordle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public class Score implements Comparable<Score> {
        /**
         * Word considered as the next guess.
         */
        final String word;
        /**
         * Expected size of the candidates in the next round.
         */
        final int expectedSize;

        final Map<List<Hint>, Integer> clusterSizes;

        private Score(String word, Map<List<Hint>, Integer> clusterSizes) {
            this.word = word;
            this.clusterSizes = clusterSizes;

            int sz = 0;
            for (var size : clusterSizes.values()) {
                sz += size * size;
            }
            this.expectedSize = sz;
        }

        @Override
        public int compareTo(Score that) {
            // smaller the better
            return Integer.compare(this.expectedSize, that.expectedSize);
        }

        /**
         * What is the worst outcome of choosing this option?
         */
        public List<Hint> worst() {
            return clusterSizes.entrySet().stream().max(Comparator.comparing(Map.Entry::getValue)).get().getKey();
        }

        public float expectedSize() {
            return ((float)expectedSize)/candidates.size();
        }

        @Override
        public String toString() {
            var clusters = clusterSizes.entrySet().stream()
                .sorted(Comparator.comparing(x -> -x.getValue()))
                .map(x -> String.format("%d:%s", x.getValue(), Hint.print(x.getKey())))
                .collect(Collectors.joining(","));

            return String.format("guess:%s -> expectedSizes:%f (%s)", word, expectedSize(), clusters);
        }

        public boolean is(String answer) {
            return word.equals(answer);
        }
    }

    /**
     * Figure out the best guess to attempt next.
     */
    public Score chooseNextGuess() {
        return scoreStream().min(Comparator.naturalOrder()).get();
    }

    /**
     * Figure out the guesses from best to worst.
     */
    public Stream<Score> nextGuesses() {
        return scoreStream().sorted().sequential();
    }

    private Stream<Score> scoreStream() {
        return options.stream().parallel().map(this::score);
    }

    /**
     * Compute the score of the proposed guess.
     */
    public Score score(String guess) {
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
        var clusterSizes = new HashMap<List<Hint>, Integer>();

        for (String c : candidates) {
            // c==o creates a cluster of size 1, but in that case the game ends, so really it'll create a cluster of zero.
            if (c.equals(guess))    continue;

            var h = Hint.make(c, guess);
            clusterSizes.merge(h, 1, Integer::sum);
        }

        return new Score(guess, clusterSizes);
    }

    /**
     * Advance the game state to the next.
     */
    public GameState nextState(Guess guess, boolean hardMode) {
        var ng = new ArrayList<>(guesses);
        ng.add(guess);

        return new GameState(ng,
            candidates.select(guess::isConsistentWith),
            hardMode ? options.select(guess::allowsInHardMode) : options);
    }

    @Override
    public String toString() {
        return String.format("Candidates: %d, such as %s%n", candidates.size(), candidates.sample(5));
    }
}
