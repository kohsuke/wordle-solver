package org.kohsuke.wordle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
//        play();
        computeAverage();
        // average 3.481210

//        solve("perky");
    }

    /**
     * Compute the average number of guesses across all the possible answers.
     */
    private static void computeAverage() {
        var frequencies = new AtomicInteger[7];
        for (int i=1; i<=6; i++)
            frequencies[i] = new AtomicInteger(0);

        var firstGuess = INITIAL_GAME.chooseNextGuess();
        ANSWERS.stream().parallel().forEach(answer -> {
            GameState g = INITIAL_GAME;
            var guess = firstGuess; // this is the most time consuming step so let's cache that.

            for (int i=1; i<=6; i++) {
                var hints = Hint.make(answer, guess);

                if (guess.equals(answer)) {
                    frequencies[i].incrementAndGet();
                    return;
                }

                g = g.nextState(new Guess(guess,hints));
                guess = g.chooseNextGuess();
            }
            throw new AssertionError("Couldn't solve: "+answer);
        });

        int sum=0;
        for (int i=1; i<=6; i++) {
            var f = frequencies[i].get();
            System.out.printf("%d guesses: %d%n", i, f);
            sum += f*i;
        }

        System.out.printf("Average in %f guesses%n", ((double)sum)/ANSWERS.size());
    }

    /**
     * Interactive play mode to solve the puzzle.
     */
    private static void play() {
        var r = new BufferedReader(new InputStreamReader(System.in));

        GameState g = INITIAL_GAME;
        for (int i=0; i<6; i++) {
            System.out.printf("Candidates: %d, such as %s%n", g.candidates.size(), g.candidates.sample(5));
            var guess = g.chooseNextGuess();
            System.out.printf("Guess: %s%n",guess);

            List<Hint> hints;
            while (true) {
                try {
                    System.out.print("Hint: ");
                    hints = Hint.parse(r.readLine());
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    // continue
                }
            }

            g = g.nextState(new Guess(guess,hints));
        }
    }

    /**
     * Given the correct answer, see how the solver solves this puzzle.
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
