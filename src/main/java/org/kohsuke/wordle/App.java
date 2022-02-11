package org.kohsuke.wordle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class App {
    private static final boolean HARD_MODE = true;
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

    public static void main(String[] args) throws ParseException {
//        firstPlayAnalysis();

//        play();
        computeAverage();
        // average 3.481210

//        solve("perky");
    }

    /**
     * What first choice would yield good results?
     */
    private static void firstPlayAnalysis() {
        var all = INITIAL_GAME.nextGuesses().collect(Collectors.toList());
        all.subList(20,40).forEach(s -> {
            System.out.println(s);
            computeAverage(s);
        });
    }

    /**
     * At the given game state, compare the impact of choosing two different guesses
     */
    private static void assessGuess(GameState g, String guess) {
        var s = g.score(guess);
        System.out.println(s);
        System.out.printf("Worst case: %s%n", g.nextState(new Guess(guess,s.worst()),HARD_MODE));
    }

    /**
     * Compute the average number of guesses across all the possible answers.
     */
    private static void computeAverage() {
        computeAverage(INITIAL_GAME.score("salet"));
    }

    private static void computeAverage(GameState.Score firstGuess) {
        final int maxTurn = 8;  // game only allows up to 6 but in the hard mode the worst case is longer than that.
        var frequencies = new AtomicInteger[maxTurn+1];
        for (int i=1; i<=maxTurn; i++)
            frequencies[i] = new AtomicInteger(0);

        ANSWERS.stream().parallel().forEach(answer -> {
            GameState g = INITIAL_GAME;
            var guess = firstGuess; // this is the most time consuming step so let's cache that.

            for (int i=1; i<=maxTurn; i++) {
                var hints = Hint.make(answer, guess.word);

                if (guess.is(answer)) {
                    frequencies[i].incrementAndGet();
                    return;
                }

                g = g.nextState(new Guess(guess.word,hints),HARD_MODE);
                guess = g.chooseNextGuess();
            }
            throw new AssertionError("Couldn't solve: "+answer+"\n"+g);
        });

        int sum=0;
        for (int i=1; i<=maxTurn; i++) {
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
            var guess = i==0 ? g.score("salet") : g.chooseNextGuess();
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

            g = g.nextState(new Guess(guess.word,hints),HARD_MODE);
        }
    }

    /**
     * Given the correct answer, see how the solver solves this puzzle.
     */
    public static void solve(String answer) {
        GameState g = INITIAL_GAME;
        for (int i=0; i<6; i++) {
            var guess = g.chooseNextGuess();
            var hints = Hint.make(answer, guess.word);

            System.out.println(g);
            System.out.printf("%s -> %s%n",guess, Hint.print(hints));

            if (guess.equals(answer))
                return;

            g = g.nextState(new Guess(guess.word,hints),HARD_MODE);
        }
    }
}
