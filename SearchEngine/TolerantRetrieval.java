import java.util.Set;

public class TolerantRetrieval {
    private InvertedIndex invertedIndex;
    private int threshold;

    public TolerantRetrieval() {
        this.threshold = 2;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public String correct(String term) {
        Set<String> vocabulary = invertedIndex.getAllTerms();

        // kalau term sudah ada, return langsung
        if (vocabulary.contains(term)) {
            return term;
        }

        String bestCandidate = term;
        int minDistance = Integer.MAX_VALUE;

        for (String candidate : vocabulary) {

            // pruning sederhana: beda panjang terlalu jauh skip
            if (Math.abs(candidate.length() - term.length()) > threshold) {
                continue;
            }

            int distance = editDistance(term, candidate);

            if (distance < minDistance) {
                minDistance = distance;
                bestCandidate = candidate;
            }

            // exact impossible but just in case
            if (minDistance == 0) {
                break;
            }
        }

        // hanya koreksi jika distance cukup kecil
        return minDistance <= threshold ? bestCandidate : term; 
    }

    private int editDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {

                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int insert = dp[i][j - 1];
                    int delete = dp[i - 1][j];
                    int replace = dp[i - 1][j - 1];

                    dp[i][j] = 1 + Math.min(insert,
                            Math.min(delete, replace));
                }
            }
        }

        return dp[m][n];
    }
}