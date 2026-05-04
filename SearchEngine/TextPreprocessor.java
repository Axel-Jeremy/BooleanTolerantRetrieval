import java.util.ArrayList;
import java.util.List;

public class TextPreprocessor {
    private Stemmer stemmer;
    private Tokenizer tokenizer;

    public TextPreprocessor() {
        this.stemmer = new Stemmer();
        this.tokenizer = new Tokenizer();
    }

    public List<String> process(String text) {
        List<String> result = tokenize(text);
        result = stem(result);
        return result;
    }

    private List<String> stem(List<String> terms) {
        List<String> res = new ArrayList<>();
        for (String term : terms) {
            String stemmedWord = stemmer.porterStemmer(term);
            if (stemmedWord != null)
                res.add(stemmedWord);
        }
        return res;
    }

    private List<String> tokenize(String tokens) {
        List<String> res = new ArrayList<>();
        List<String> terms = tokenizer.process(tokens);
        if (terms == null)
            return res;
        while (!terms.isEmpty()) { // ← cek isEmpty(), bukan != null
            res.add(terms.removeFirst());
        }
        return res;
    }
}
