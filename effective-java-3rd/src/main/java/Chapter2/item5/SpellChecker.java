package Chapter2.item5;

import java.util.List;
import java.util.Objects;

public class SpellChecker {

    private final Lexicon dictionary;

    public SpellChecker(Lexicon dictionary) {
        this.dictionary = Objects.requireNonNull(dictionary);
    }

    public static boolean isValid(String word) {
        throw new UnsupportedOperationException();
    }

    public static List<String> suggestion(String typo) {
        throw new UnsupportedOperationException();
    }
}

interface Lexicon {}

class KoreanDictionary implements Lexicon {}
