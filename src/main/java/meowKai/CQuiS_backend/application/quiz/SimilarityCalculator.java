package meowKai.CQuiS_backend.application.quiz;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimilarityCalculator {


    // 레벤슈타인 거리를 측정
    private static int levenshteinDistance(String s1, String s2) {
        if (s1.length() < s2.length()) {
            return levenshteinDistance(s2, s1);
        }
        if(s2.isEmpty()) {
            return s1.length();
        }

        int[] previousRow = new int[s2.length() + 1];
        for(int i = 0; i <= s2.length(); i++) {
            previousRow[i] = i;
        }

        for (int i = 0; i < s1.length(); i++) {
            int[] currentRow = new int[s2.length() + 1];
            currentRow[0] = i + 1;

            for (int j = 0; j < s2.length(); j++) {
                int insertions = previousRow[j + 1] + 1;
                int deletions = currentRow[j] + 1;
                int substitutions = previousRow[j] + (s1.charAt(i) != s2.charAt(j) ? 1 : 0);

                currentRow[j + 1] = Math.min(Math.min(insertions, deletions), substitutions);
            }
            previousRow = currentRow;
        }
        return previousRow[s2.length()];
    }

    // 레벤슈타인 거리를 바탕으로 레벤슈타인 유사도를 계산
    private static double levenshteinSimilarity(String s1, String s2) {
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());

        return 1.0 - ((double) distance / maxLength);
    }


    private static final double SCALING_FACTOR = 0.1;
    // Jaro 거리를 측정
    private static double jaroDistance(String s1, String s2) {
        if(s1.isEmpty() && s2.isEmpty()) return 1.0;
        if(s1.isEmpty() || s2.isEmpty()) return 0.0;

        int matchDistance = (Math.max(s1.length(), s2.length()) / 2) - 1;
        boolean[] s1Matches = new boolean[s1.length()];
        boolean[] s2Matches = new boolean[s2.length()];
        int matches = 0;

        for (int i = 0; i < s1.length(); i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, s2.length());

            for (int j = start; j < end; j++) {
                if (s2Matches[j]) {
                    continue;
                }
                if(s1.charAt(i) != s2.charAt(j)) {
                    continue;
                }

                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }
        if(matches == 0) {
            return 0.0;
        }

        int transpositions = 0;
        int temp = 0;
        for (int i = 0; i < s1.length(); i++) {
            if(!s1Matches[i]) {
                continue;
            }
            while(temp < s2.length() && !s2Matches[temp]) {
                temp++;
            }
            if (temp < s2.length() && s1.charAt(i) != s2.charAt(temp)) {
                transpositions++;
            }
            temp++;
        }

        return ((double) matches / s1.length() +
                (double) matches / s2.length() +
                (double) (matches - transpositions / 2.0) / matches) / 3.0;
    }

    // Jaro 거리를 바탕으로 문자열의 앞부분에 가중치를 둔 Jaro-Winkler 유사도 계산
    private static double jaroWinklerSimilarity(String s1, String s2, double scalingFactor) {
        double jaroDist = jaroDistance(s1, s2);

        int prefixLength = 0;
        int maxPrefixLength = Math.min(Math.min(s1.length(), s2.length()), 4);

        for (int i = 0; i < maxPrefixLength; i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefixLength++;
            } else {
                break;
            }
        }

        return jaroDist + (scalingFactor * prefixLength * (1.0 - jaroDist));
    }

    // 한글 특성을 반영하여 원본 문자열과 분해된 문자열의 통합 Jaro-Winkler 유사도를 계산
    private static double integratedJaroWinklerSimilarity(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        // 원본 문자열의 Jaro-Winkler 유사도
        double originalSim = jaroWinklerSimilarity(word1, word2, SCALING_FACTOR);

        // 자,모음 분해된 문자열의 Jaro-Winkler 유사도
        String decomposed1 = KoreanAnalyzer.decompose(word1);
        String decomposed2 = KoreanAnalyzer.decompose(word2);
        double decomposedSim = jaroWinklerSimilarity(decomposed1, decomposed2, SCALING_FACTOR);

        return 0.6 * originalSim + 0.4 * decomposedSim;
    }

    public static double nGramSimilarity(String s1, String s2) {
        int n = 2;

        // 각 문자열의 N-gram 빈도수 계산
        Map<String, Integer> s1Ngrams = getNGrams(s1, n);
        Map<String, Integer> s2Ngrams = getNGrams(s2, n);

        // 교집합과 합집합 계산
        int intersection = 0;
        int union = 0;

        // 교집합 계산
        for (Map.Entry<String, Integer> entry : s1Ngrams.entrySet()) {
            String ngram = entry.getKey();
            if (s2Ngrams.containsKey(ngram)) {
                intersection += Math.min(entry.getValue(), s2Ngrams.get(ngram));
            }
        }

        // 합집합 계산
        for (Map.Entry<String, Integer> entry : s1Ngrams.entrySet()) {
            String ngram = entry.getKey();
            if (s2Ngrams.containsKey(ngram)) {
                union += Math.max(entry.getValue(), s2Ngrams.get(ngram));
            } else {
                union += entry.getValue();
            }
        }

        // s2에만 있는 n-gram 추가
        for (Map.Entry<String, Integer> entry : s2Ngrams.entrySet()) {
            if (!s1Ngrams.containsKey(entry.getKey())) {
                union += entry.getValue();
            }
        }

        // Jaccard 유사도 계산
        return union == 0 ? 0 : (double) intersection / union;
    }

    // 문자열에서 N-gram 생성하고 빈도수 계산
    private static Map<String, Integer> getNGrams(String str, int n) {
        Map<String, Integer> ngrams = new HashMap<>();

        if (str.length() < n) {
            return ngrams;
        }

        // N-gram 생성 및 빈도수 계산
        for (int i = 0; i <= str.length() - n; i++) {
            String ngram = str.substring(i, i + n);
            ngrams.merge(ngram, 1, Integer::sum);
        }

        return ngrams;
    }

    // cosine 유사도를 측정
    public static double cosineSimilarity(String s1, String s2) {
        // 문자열을 문자 빈도 벡터로 변환
        Map<Character, Integer> vec1 = getCharacterFrequency(s1);
        Map<Character, Integer> vec2 = getCharacterFrequency(s2);

        // 두 벡터의 공통 문자 찾기
        Set<Character> intersection = new HashSet<>(vec1.keySet());
        intersection.retainAll(vec2.keySet());

        // 내적 계산
        double numerator = intersection.stream()
                .mapToDouble(ch -> vec1.get(ch) * vec2.get(ch))
                .sum();

        // 벡터의 크기 계산
        double sum1 = vec1.values().stream()
                .mapToDouble(count -> Math.pow(count, 2))
                .sum();
        double sum2 = vec2.values().stream()
                .mapToDouble(count -> Math.pow(count, 2))
                .sum();

        double denominator = Math.sqrt(sum1 * sum2);

        return denominator == 0 ? 0.0 : numerator / denominator;
    }

    // LCS 길이를 계산
    public static int lcsLength(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int[][] L = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    L[i][j] = L[i - 1][j - 1] + 1;
                } else {
                    L[i][j] = Math.max(L[i - 1][j], L[i][j - 1]);
                }
            }
        }

        return L[m][n];
    }

    // LCS 길이를 바탕으로 유사도를 계산
    public static double lcsSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            throw new IllegalArgumentException("입력 문자열이 null일 수 없습니다.");
        }

        int lcsLen = lcsLength(s1, s2);
        return (double) lcsLen / Math.max(s1.length(), s2.length());
    }

    // 문자열의 문자별 빈도수를 계산
    private static Map<Character, Integer> getCharacterFrequency(String str) {
        Map<Character, Integer> frequency = new HashMap<>();
        for (char ch : str.toCharArray()) {
            frequency.merge(ch, 1, Integer::sum);
        }
        return frequency;
    }

    public static double comprehensiveSimilarity(String word1, String word2, double[] weights) {
        String decomposed1 = KoreanAnalyzer.decompose(word1.toLowerCase());
        String decomposed2 = KoreanAnalyzer.decompose(word2.toLowerCase());

        double[] similarities = new double[] {
                levenshteinSimilarity(decomposed1, decomposed2),
                integratedJaroWinklerSimilarity(word1, word2),
                nGramSimilarity(decomposed1, decomposed2),
                cosineSimilarity(decomposed1, decomposed2),
                lcsSimilarity(decomposed1, decomposed2)
        };

        double totalSimilarity = 0.0;
        for (int i = 0; i < weights.length; i++) {
            totalSimilarity += weights[i] * similarities[i];
        }

        return totalSimilarity;
    }

}
