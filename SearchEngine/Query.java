import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Query {
    private String query;
    private Stack<String> orderProcess;
    private Stack<List<PostingNode>> resultStack;
    private TextPreprocessor preprocessor;
    private BooleanModel model;
    private static InvertedIndex invertedIndex;

    public Query(String query) {
        this.query = query.trim().toLowerCase();
        this.orderProcess = new Stack<>();
        this.resultStack = new Stack<>();
        this.preprocessor = new TextPreprocessor();
        this.model = new BooleanModel();
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public List<PostingNode> processQuery(List<String> terms) {
        return model.process(terms);
    }

    public List<PostingNode> preProcess() {
        List<String> splittedQuery = splitQuery();
        List<String> terms = null;
        for (String kata : splittedQuery) {
            if (kata.charAt(0) != ')') {
                orderProcess.push(kata);
            } else {
                String queryNoBracket = "";
                while (!orderProcess.peek().equals("(")) {
                    queryNoBracket = orderProcess.pop() + " " + queryNoBracket;
                }
                orderProcess.pop();

                String[] queries = queryNoBracket.trim().split("\\s+");
                terms = new ArrayList<>();

                for (int i = 0; i < queries.length; i++) {
                    if (!queries[i].equals("not")
                            && !queries[i].equals("and")
                            && !queries[i].equals("or")) {
                        List<String> res = preprocessor.process(queries[i]);
                        for (String term : res)
                            terms.add(term);
                    } else {
                        terms.add(queries[i]);
                    }
                }

                // process sub bagian query, taro
                resultStack.push(processQuery(terms));
            }
        }

        // jika order process masih ada isi
        if (!this.orderProcess.isEmpty()) {
            String queryNoBracket = "";
            while (!orderProcess.isEmpty()) {
                queryNoBracket = orderProcess.pop() + " " + queryNoBracket;
            }

            String[] queries = queryNoBracket.trim().split("\\s+");
            terms = new ArrayList<>();

            for (int i = 0; i < queries.length; i++) {
                if (!queries[i].equals("not")
                        && !queries[i].equals("and")
                        && !queries[i].equals("or")) {
                    List<String> res = preprocessor.process(queries[i]);
                    for (String term : res)
                        terms.add(term);
                } else {
                    terms.add(queries[i]);
                }
            }

            // process, simpen
        }

        return resultStack.isEmpty() ? new ArrayList<>() : resultStack.pop();

    }

    public List<String> splitQuery() {
        List<String> token = new ArrayList<>();
        String temp = "";

        for (char c : query.toCharArray()) {
            if (c == '(' || c == ')') {
                if (temp.length() > 0) {
                    token.add(temp);
                    temp = "";
                }

                token.add(c + "");
            }

            else if (c == ' ') {
                if (temp.length() > 0) {
                    token.add(temp);
                    temp = "";
                }
            }

            else {
                temp = temp + c;
            }
        }
        if (temp.length() > 0) {
            token.add(temp);
        }

        return token;
    }
    // (())
    // if index i == 'AND', query.processAND(kata1,kata2)
    // (unpar and (fakultas and informatika) or prabowo) and axel
    // (, unpar, and, (, fakultas, and, informatika, ), or, prabowo, ), and, axel

}
