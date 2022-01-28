package org.kohsuke.wordle;

import java.io.IOException;
import java.util.Collections;

public class App {
    public static void main(String[] args) throws IOException {
        String answer = "perky";

        GameState g = new GameState(Collections.emptyList(),
            WordList.fromResource("answers.txt"),
            WordList.fromResource("guesses.txt"));

        while (true) {
            var guess = g.chooseNextGuess();
            var hints = Hint.make(answer, guess);

            System.out.printf("%s -> %s%n",guess, Hint.print(hints));

            if (guess.equals(answer))
                return;

            g = g.nextState(new Guess(guess,hints));
        }
    }
}
