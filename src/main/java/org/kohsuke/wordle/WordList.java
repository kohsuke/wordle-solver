package org.kohsuke.wordle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Read only immutable list of words.
 *
 * @author Kohsuke Kawaguchi
 */
public class WordList implements Iterable<String> {
    private final Set<String> words;

    public WordList(Set<String> words) {
        this.words = words;
    }

    @Override
    public Iterator<String> iterator() {
        return words.iterator();
    }

    /**
     * Creates a sub list that matches the given criteria.
     */
    public WordList select(Predicate<String> p) {
        return new WordList(words.stream().filter(p).collect(Collectors.toSet()));
    }

    /**
     * Loads a word list from a resource file.
     */
    public static WordList fromResource(String fileName) throws IOException {
        var words = new HashSet<String>();

        try (var r = new BufferedReader(new InputStreamReader(WordList.class.getResourceAsStream(fileName),"UTF-8"))) {
            String s;
            while ((s=r.readLine())!=null) {
                words.add(s);
            }
            return new WordList(words);
        }
    }

    public int size() {
        return words.size();
    }

    public List<String> sample(int upTo) {
        var sample = new ArrayList<String>(upTo);
        for (String w : words) {
            sample.add(w);
            if (sample.size()==upTo)
                break;
        }
        return sample;
    }

    public WordList join(WordList that) {
        var words = new HashSet<>(this.words);
        words.addAll(that.words);
        return new WordList(words);
    }

    public boolean contains(String w) {
        return words.contains(w);
    }

    public Stream<String> stream() {
        return words.stream();
    }
}
