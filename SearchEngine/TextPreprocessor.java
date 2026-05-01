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
        List<String> preprocessing = tokenizer.process(text);
        preprocessing = stem(preprocessing);
        return preprocessing;
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
}
