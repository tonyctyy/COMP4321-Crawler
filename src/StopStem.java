import IRUtilities.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;

public class StopStem {
    private Porter porter;
    private HashSet<String> stopWords;

    // Constructor
    public StopStem(String str) {
        super();
        porter = new Porter();
        stopWords = new HashSet<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(str));
            String line;
            // Read stop words from file and add them to HashSet
            while ((line = in.readLine()) != null) {
                stopWords.add(line);
            }
            in.close();
        } catch (IOException ioe) {
            System.err.println(ioe.toString());
        }
    }

    // Check if a word is a stop word
    public boolean isStopWord(String str) {
        return stopWords.contains(str);
    }

    // Stem a word using Porter Stemmer
    public String stem(String str) {
        return porter.stripAffixes(str);
    }
}
