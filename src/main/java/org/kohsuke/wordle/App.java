package org.kohsuke.wordle;

import java.io.IOException;
import java.util.Collections;

public class App {
    public static void main(String[] args) throws IOException {
        String answer = "mount";

        var answers = WordList.fromResource("answers.txt");
        GameState g = new GameState(Collections.emptyList(),
            answers,
            WordList.fromResource("guesses.txt").join(answers));

        for (int i=0; i<5; i++) {
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
