package org.kohsuke.wordle;

import java.io.IOException;
import java.util.Collections;

public class App {
    private static final WordList ANSWERS;
    private static final WordList GUESSES;
    private static final GameState INITIAL_GAME;

    static {
        try {
            ANSWERS = WordList.fromResource("answers.txt");
            GUESSES = WordList.fromResource("guesses.txt").join(ANSWERS);
            INITIAL_GAME = new GameState(Collections.emptyList(), ANSWERS, GUESSES);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public static void main(String[] args) {
        computeAverage();
    }

    /**
     * Compute the average number of guesses across all the possible answers.
     */
    private static void computeAverage() {
        var firstGuess = INITIAL_GAME.chooseNextGuess();
        var average = ANSWERS.stream().parallel().mapToDouble(answer -> {
            GameState g = INITIAL_GAME;
            var guess = firstGuess; // this is the most time consuming step so let's cache that.

            for (int i=1; i<10; i++) {
                var hints = Hint.make(answer, guess);

                if (guess.equals(answer))
                    return i;

                g = g.nextState(new Guess(guess,hints));
                guess = g.chooseNextGuess();
            }
            throw new AssertionError("Couldn't solve: "+answer);
        }).average().getAsDouble();

        System.out.printf("Expected to solve this puzzle in %f guesses%n", average);
    }

    /**
     * Given the correct answer, solve the game.
     */
    public static void solve(String answer) {
        GameState g = INITIAL_GAME;
        for (int i=0; i<6; i++) {
            var guess = g.chooseNextGuess();
            var hints = Hint.make(answer, guess);

            System.out.printf("Candidates: %d, such as %s%n", g.candidates.size(), g.candidates.sample(5));
            System.out.printf("%s -> %s%n",guess, Hint.print(hints));

            if (guess.equals(answer))
                return;

            g = g.nextState(new Guess(guess,hints));
        }
    }
}
