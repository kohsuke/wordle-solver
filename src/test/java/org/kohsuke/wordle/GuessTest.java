package org.kohsuke.wordle;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.kohsuke.wordle.Hint.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class GuessTest {
    @Test
    public void isConsistentWith() {
        assertThat(new Guess("mount", List.of(GREY,GREY,GREY,GREY,GREY)).isConsistentWith("perky"),is(true));
        assertThat(new Guess("deals", List.of(GREY,GREEN,GREY,GREY,GREY)).isConsistentWith("perky"),is(true));
        assertThat(new Guess("fiber", List.of(GREY,GREY,GREY,YELLOW,YELLOW)).isConsistentWith("perky"),is(true));
        assertThat(new Guess("weepy", List.of(GREY,GREEN,GREY,YELLOW,GREEN)).isConsistentWith("perky"),is(true));
        assertThat(new Guess("perky", List.of(GREEN,GREEN,GREEN,GREEN,GREEN)).isConsistentWith("perky"),is(true));
    }

}
