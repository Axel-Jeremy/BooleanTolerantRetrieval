import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Query {
    private String query;
    private Stack<List<PostingNode>> resultStack;
    private TextPreprocessor preprocessor;
    private BooleanModel model;
    private TolerantRetrieval tolerant;
    private static InvertedIndex invertedIndex;

    public Query(String query) {
        this.query = query.trim().toLowerCase();
        this.resultStack = new Stack<>();
        this.preprocessor = new TextPreprocessor();
        this.model = new BooleanModel();
        this.tolerant = new TolerantRetrieval();
    }

    public void setModel(BooleanModel model) {
        this.model = model;
    }

    public void setTolerantModel(TolerantRetrieval tolerant) {
        this.tolerant = tolerant;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        Query.invertedIndex = invertedIndex;
    }

    // public List<PostingNode> processQuery(List<String> terms) {
    // return model.process(terms);
    // }
    private static final Map<String, Integer> ORDER = new HashMap<>();
    static {
        ORDER.put("OR", 1);
        ORDER.put("AND", 2);
        ORDER.put("NOT", 3);
    }

    public boolean isOperator(String kata) {
        return kata.equals("and") || kata.equals("or") || kata.equals("not");
    }

    public List<PostingNode> preProcess() {
        List<String> splittedQuery = splitQuery();
        // List<String> terms = null;

        Stack<String> orderProcess = new Stack<>();
        Stack<String> operator = new Stack<>();

        for (String kata : splittedQuery) {
            if (kata.charAt(0) == '(') {
                operator.push(kata);
            } else if (kata.equals(")")) {
                while (!operator.isEmpty() && !operator.peek().equals("(")) {
                    orderProcess.push(operator.pop());
                }
                operator.push(kata);
            } else if (isOperator(kata)) {
                while (!operator.isEmpty()
                        && isOperator(operator.peek())
                        && ORDER.get(operator.peek()) >= ORDER.get(kata)) {
                    orderProcess.push(operator.pop());
                }
                operator.push(kata);
            }
        }

         while (!operator.isEmpty()) {
            orderProcess.add(operator.pop());
        }

        // return new ArrayList<>(orderProcess);
        return null;
    }

    private List<PostingNode> assignPointer(List<PostingNode> nodes) {
        return model.assignPointer(nodes);
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
