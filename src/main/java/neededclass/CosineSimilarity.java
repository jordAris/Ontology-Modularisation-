package neededclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CosineSimilarity implements SimilarityMeasure {

    private Map<String, Integer> wordCounts;

    public CosineSimilarity() {
        wordCounts = new HashMap<>();
    }

    public double calculateSimilarity(String s1, String s2) {
        // Calculate the vector representations of s1 and s2.
        List<Double> v1 = getVectorRepresentation(s1);
        List<Double> v2 = getVectorRepresentation(s2);

        // Calculate the cosine similarity between v1 and v2.
        double similarity = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            similarity += v1.get(i) * v2.get(i);
        }
        similarity /= Math.sqrt(v1.size()) * Math.sqrt(v2.size());

        return similarity;
    }

    private List<Double> getVectorRepresentation(String s) {
        List<Double> vector = new ArrayList<>();
        for (String word : s.toLowerCase().split(" ")) {
            if (wordCounts.containsKey(word)) {
                vector.add(Double.valueOf(wordCounts.get(word)));
            } else {
                vector.add(0.0);
            }
        }
        return vector;
    }

    public void addWord(String word) {
        if (!wordCounts.containsKey(word)) {
            wordCounts.put(word, 0);
        }
        wordCounts.put(word, wordCounts.get(word) + 1);
    }
}
