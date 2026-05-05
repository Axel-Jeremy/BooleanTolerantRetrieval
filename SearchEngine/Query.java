import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class Query {
    private String query;
    private Stack<String> orderProcess;
    private Stack<List<PostingNode>> resultStack;
    private TextPreprocessor preprocessor;
    private BooleanModel model;
    private TolerantRetrieval tolerant;
    private static InvertedIndex invertedIndex;

    public Query(String query) {
        this.query = query.trim().toLowerCase();
        this.orderProcess = new Stack<>();
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

    public boolean isOperator(String kata) {
        return kata.equals("not") || kata.equals("and") || kata.equals("or"); 
    }

    // public List<PostingNode> processQuery(List<String> terms) {
    // return model.process(terms);
    // }

    public List<PostingNode> preProcess() {
        List<String> tokens = splitQuery();

        // FASE 1: Shunting-Yard → ubah infix ke postfix
        List<String> postfix = shuntingYard(tokens);

        // FASE 2: Evaluasi postfix
        return evaluate(postfix);
    }

    private List<String> shuntingYard(List<String> tokens) {
        Queue<String> outputQueue = new LinkedList<>();
        Deque<String> operatorStack = new ArrayDeque<>();

        for (String raw : tokens) {
            String token = isOperator(raw) ? raw.toLowerCase() : raw;

            if (isOperator(token)) {
                // Pop operator lama yang prioritasnya >= token ini
                while (!operatorStack.isEmpty()
                        && isOperator(operatorStack.peek())
                        && getPriority(operatorStack.peek()) >= getPriority(token)) {
                    outputQueue.add(operatorStack.pop());
                }
                operatorStack.push(token);

            } else if (token.equals("(")) {
                operatorStack.push(token);

            } else if (token.equals(")")) {
                // Pop sampai ketemu "("
                while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                    outputQueue.add(operatorStack.pop());
                }
                if (!operatorStack.isEmpty()) {
                    operatorStack.pop(); // buang "("
                }

            } else {
                // Term biasa → stem dulu, lalu masuk output queue
                List<String> stemmed = preprocessor.process(token);
                for (String s : stemmed)
                    outputQueue.add(tolerant.correct(s));
            }
        }

        // Pop sisa operator ke output
        while (!operatorStack.isEmpty()) {
            outputQueue.add(operatorStack.pop());
        }

        return new ArrayList<>(outputQueue);
    }

    private List<PostingNode> evaluate(List<String> postfix) {
        Deque<List<PostingNode>> resultStack = new ArrayDeque<>();

        for (String token : postfix) {
            if (token.equals("not")) {
                if (resultStack.isEmpty())
                    return new ArrayList<>();
                List<PostingNode> operand = resultStack.pop();
                if (operand.isEmpty())
                    return new ArrayList<>();
                resultStack.push(assignPointer(model.negate(operand.getFirst())));

            } else if (token.equals("and")) {
                if (resultStack.size() < 2)
                    return new ArrayList<>();
                List<PostingNode> right = resultStack.pop();
                List<PostingNode> left = resultStack.pop();
                if (right.isEmpty() || left.isEmpty()) {
                    resultStack.push(new ArrayList<>());
                } else {
                    resultStack.push(assignPointer(
                            model.intersect(left.getFirst(), right.getFirst())));
                }

            } else if (token.equals("or")) {
                if (resultStack.size() < 2)
                    return new ArrayList<>();
                List<PostingNode> right = resultStack.pop();
                List<PostingNode> left = resultStack.pop();
                if (right.isEmpty() && left.isEmpty()) {
                    resultStack.push(new ArrayList<>());
                } else if (right.isEmpty()) {
                    resultStack.push(left);
                } else if (left.isEmpty()) {
                    resultStack.push(right);
                } else {
                    resultStack.push(assignPointer(
                            model.union(left.getFirst(), right.getFirst())));
                }

            } else {
                // Term biasa → ambil posting list dari index
                List<PostingNode> posting = invertedIndex.getPostingList(token);
                resultStack.push(posting != null ? posting : new ArrayList<>());
            }
        }

        return resultStack.isEmpty() ? new ArrayList<>() : resultStack.pop();
    }

    private int getPriority(String operator) {
        switch (operator.toLowerCase()) {
            case "or":
                return 1;
            case "and":
                return 2;
            case "not":
                return 3;
            default:
                return 0;
        }
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
