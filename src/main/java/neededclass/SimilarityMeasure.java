package neededclass;

import java.util.*;

public interface SimilarityMeasure {

    /**
     * Calculates the similarity between two strings.
     *
     * @param s1 The first string.
     * @param s2 The second string.
     * @return The similarity between s1 and s2.
     */
    double calculateSimilarity(String s1, String s2);
}


