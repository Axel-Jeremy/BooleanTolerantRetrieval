import java.util.ArrayList;
import java.util.List;

public class Tokenizer {
    public Tokenizer() {
    }

    public List<String> process(String text) {
        List<String> tokens = tokenize(text);
        tokens = caseFolding(tokens);
        return tokens;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();

        // ngubah non alphabet jadi spasi
        String cleaned = text.replaceAll("[^a-zA-Z ]", ""); 

        //berguna untuk menghapus semua whitespace baik dari spasi, tab, ataupun newLine
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
