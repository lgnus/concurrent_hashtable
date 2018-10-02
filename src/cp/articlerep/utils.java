package cp.articlerep;

import java.util.Collection;

/**
 * Created by rramalho on 30-11-2016.
 */
public class utils {
    /**
     * Join a collection of strings and add delimiters.
     */
    public static String join(Collection<String> words, String delimiter) {
        StringBuilder wordList = new StringBuilder();
        for (String word : words) {
            wordList.append(word);
            wordList.append(delimiter);
        }
        return new String(wordList.deleteCharAt(wordList.length() - 1));
    }
}
