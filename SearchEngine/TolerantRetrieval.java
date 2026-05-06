import java.util.List;
import java.util.Set;

public class TolerantRetrieval {
    private InvertedIndex invertedIndex;
    private int threshold;
    private TextPreprocessor preprocessor;

    public TolerantRetrieval() {
        this.threshold = 2;
        this.preprocessor = new TextPreprocessor();
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public String correct(String rawTerm) {
        Set<String> rawVocab = invertedIndex.getRawVocabulary();

        // Kalau sudah ada di raw vocab → tidak perlu koreksi
        if (rawVocab.contains(rawTerm)) {
            return rawTerm;
        }

        String bestCandidate = rawTerm;
        int minDistance = Integer.MAX_VALUE;

        for (String candidate : rawVocab) {
            if (Math.abs(candidate.length() - rawTerm.length()) > threshold) {
                continue;
            }

            int distance = editDistance(rawTerm, candidate);
            if (distance < minDistance) {
                minDistance = distance;
                bestCandidate = candidate;
            }
            if (minDistance == 0)
                break;
        }

        return minDistance <= threshold ? bestCandidate : rawTerm;
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
