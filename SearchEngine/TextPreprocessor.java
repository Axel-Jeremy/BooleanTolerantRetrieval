import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextPreprocessor {
    private Stemmer stemmer;
    private Set<String> stopWords;

    public TextPreprocessor() {
        this.stemmer = new Stemmer();
        this.stopWords = loadStopWords();
    }

    public List<String> process(String text) {
        List<String> tokens = tokenize(text);
        tokens = caseFolding(tokens);
        tokens = removeStopWords(tokens);
        tokens = stem(tokens);
        return tokens;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();

        String cleaned = text.replaceAll("[^a-zA-Z]", " ");
        String[] words = cleaned.split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    private List<String> caseFolding(List<String> tokens) {
        List<String> result = new ArrayList<>();
        for (String token : tokens) {
            result.add(token.toLowerCase());
        }
        return result;
    }

    private List<String> removeStopWords(List<String> tokens) {
        List<String> res = new ArrayList<>();
        for (String token : tokens) {
            if (!stopWords.contains(token)) {
                res.add(token);
            }
        }
        return res;
    }

    private List<String> stem(List<String> tokens) {
        List<String> res = new ArrayList<>();
        for (String token : tokens) {
            String stemmedWord = stemmer.porterStemmer(token);
            if (stemmedWord != null)
                res.add(stemmedWord);
        }

        return res;
    }

    private Set<String> loadStopWords() {
        Set<String> sw = new HashSet<>(Arrays.asList(
                "a", "about", "above", "after", "again", "against", "all", "am",
                "an", "and", "any", "are", "aren't", "as", "at", "be", "because",
                "been", "before", "being", "below", "between", "both", "but", "by",
                "can't", "cannot", "could", "couldn't", "did", "didn't", "do",
                "does", "doesn't", "doing", "don't", "down", "during", "each",
                "few", "for", "from", "further", "get", "got", "had", "hadn't",
                "has", "hasn't", "have", "haven't", "having", "he", "he'd",
                "he'll", "he's", "her", "here", "here's", "hers", "herself",
                "him", "himself", "his", "how", "how's", "i", "i'd", "i'll",
                "i'm", "i've", "if", "in", "into", "is", "isn't", "it", "it's",
                "its", "itself", "let's", "me", "more", "most", "mustn't", "my",
                "myself", "no", "nor", "not", "of", "off", "on", "once", "only",
                "or", "other", "ought", "our", "ours", "ourselves", "out", "over",
                "own", "same", "shan't", "she", "she'd", "she'll", "she's",
                "should", "shouldn't", "so", "some", "such", "than", "that",
                "that's", "the", "their", "theirs", "them", "themselves", "then",
                "there", "there's", "these", "they", "they'd", "they'll",
                "they're", "they've", "this", "those", "through", "to", "too",
                "under", "until", "up", "very", "was", "wasn't", "we", "we'd",
                "we'll", "we're", "we've", "were", "weren't", "what", "what's",
                "when", "when's", "where", "where's", "which", "while", "who",
                "who's", "whom", "why", "why's", "will", "with", "won't",
                "would", "wouldn't", "you", "you'd", "you'll", "you're",
                "you've", "your", "yours", "yourself", "yourselves"));
        return sw;
    }

    // public static void main(String[] args) {
    //     DocumentReader reader = new DocumentReader("DataSet");
    //     Map<Integer, String> docs = reader.readAll();

    //     if (docs.isEmpty()) {
    //         System.out.println("File tidak ditemukan.");
    //         return;
    //     }

    //     TextPreprocessor preprocessor = new TextPreprocessor();
    //     List<String> terms = null;
    //     for (Map.Entry<Integer, String> entry : docs.entrySet()) {
    //         int docID = entry.getKey();
    //         String content = entry.getValue();

    //         // Preprocessing: tokenisasi → case folding → stop word → stemming
    //         terms = preprocessor.process(content);
    //     }
    //     while (terms != null && !terms.isEmpty()) {
    //         System.out.println(terms.removeFirst());
    //     }
    // }
}
