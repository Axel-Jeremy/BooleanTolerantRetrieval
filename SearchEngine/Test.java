import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Test {
    private String query;
    private Stack<String> orderProcess;
    private Stack<List<PostingNode>> resultStack;
    private TextPreprocessor preprocessor;
    private BooleanModel model;
    private static InvertedIndex invertedIndex;

    public Test(String query) {
        this.query = query.trim().toLowerCase();
        this.orderProcess = new Stack<>();
        this.resultStack = new Stack<>();
        this.preprocessor = new TextPreprocessor();
        this.model = new BooleanModel();
    }

    public void setModel(BooleanModel model) {
        this.model = model;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        Test.invertedIndex = invertedIndex;
    }

    public List<PostingNode> preProcess() {
        List<String> splittedQuery = splitQuery();
        List<String> terms = null;

        for (String kata : splittedQuery) {
            if (kata.charAt(0) != ')') {
                orderProcess.push(kata);
            } else {
                // Kumpulkan token di dalam bracket
                String queryNoBracket = "";
                while (!orderProcess.peek().equals("(")) {
                    queryNoBracket = orderProcess.pop() + " " + queryNoBracket;
                }
                orderProcess.pop(); // buang "("

                String[] queries = queryNoBracket.trim().split("\\s+");
                terms = buildTerms(queries);

                boolean startsWithOperator = !terms.isEmpty()
                        && (terms.get(0).equals("and")
                                || terms.get(0).equals("or")
                                || terms.get(0).equals("not"));

                if (startsWithOperator && !resultStack.isEmpty()) {
                    // Bracket ini operan kanan, prevResult dari resultStack adalah operan kiri
                    List<PostingNode> prevResult = resultStack.pop();
                    resultStack.push(model.process(terms, prevResult));
                } else {
                    resultStack.push(model.process(terms));
                }
            }
        }

        // Jika orderProcess masih ada isi (token di luar semua bracket)
        if (!this.orderProcess.isEmpty()) {
            List<String> remaining = new ArrayList<>();
            while (!orderProcess.isEmpty()) {
                remaining.add(0, orderProcess.pop()); // pertahankan urutan
            }

            terms = buildTerms(remaining.toArray(new String[0]));

            if (!resultStack.isEmpty()) {
                String lastToken  = terms.get(terms.size() - 1);
                String firstToken = terms.get(0);

                boolean endsWithOperator  = lastToken.equals("and")
                        || lastToken.equals("or")
                        || lastToken.equals("not");
                boolean startsWithOperator = firstToken.equals("and")
                        || firstToken.equals("or")
                        || firstToken.equals("not");

                if (terms.size() == 1 && terms.get(0).equals("not")) {
                    // ── ["not"] saja → NOT (bracket) ────────────────────────────────
                    // Contoh: "not (empirical)"
                    // ────────────────────────────────────────────────────────────────
                    List<PostingNode> toNegate = resultStack.pop();
                    resultStack.push(
                        model.assignPointer(
                            model.negate(model.assignPointer(toNegate).getFirst())
                        )
                    );

                } else if (startsWithOperator && firstToken.equals("not")
                        && terms.size() >= 2
                        && (terms.get(1).equals("and") || terms.get(1).equals("or"))) {
                    // ── "not (bracket) and/or rightTerms" ───────────────────────────
                    // Contoh: "not (evaluation and effects) and empirical"
                    //   terms = ["not", "and", "empirical"]
                    //   → negate bracket, lalu gabung dengan sisi kanan pakai linkOp
                    // ────────────────────────────────────────────────────────────────
                    terms.remove(0);                  // buang "not"
                    String linkOp = terms.remove(0);  // ambil "and" / "or"

                    List<PostingNode> bracketResult = resultStack.pop();
                    List<PostingNode> negated = model.assignPointer(
                        model.negate(model.assignPointer(bracketResult).getFirst())
                    );

                    if (terms.isEmpty()) {
                        resultStack.push(negated);
                    } else {
                        List<PostingNode> rightResult = model.process(terms);
                        if ("or".equals(linkOp)) {
                            if (negated.isEmpty()) {
                                resultStack.push(rightResult);
                            } else if (rightResult.isEmpty()) {
                                resultStack.push(negated);
                            } else {
                                resultStack.push(model.assignPointer(
                                    model.union(negated.getFirst(), rightResult.getFirst())
                                ));
                            }
                        } else { // "and"
                            if (negated.isEmpty() || rightResult.isEmpty()) {
                                resultStack.push(new ArrayList<>());
                            } else {
                                resultStack.push(model.assignPointer(
                                    model.intersect(negated.getFirst(), rightResult.getFirst())
                                ));
                            }
                        }
                    }

                } else if (endsWithOperator && lastToken.equals("not")) {
                    // ── Diakhiri "not" → prefix NOT untuk bracket di kanan ───────────
                    // Contoh: "empirical and not (evaluation and effects)"
                    //   terms = ["empirical", "and", "not"]
                    //   → pisahkan "not" dan "and", proses sisi kiri, negate bracket
                    // ────────────────────────────────────────────────────────────────
                    terms.remove(terms.size() - 1); // buang "not"

                    String linkOp = null;
                    if (!terms.isEmpty()) {
                        String beforeNot = terms.get(terms.size() - 1);
                        if (beforeNot.equals("and") || beforeNot.equals("or")) {
                            linkOp = terms.remove(terms.size() - 1);
                        }
                    }

                    List<PostingNode> rightResult = resultStack.pop();
                    List<PostingNode> negated = model.assignPointer(
                        model.negate(model.assignPointer(rightResult).getFirst())
                    );

                    if (terms.isEmpty() || linkOp == null) {
                        resultStack.push(negated);
                    } else {
                        List<PostingNode> leftResult = model.process(terms);
                        if ("or".equals(linkOp)) {
                            if (leftResult.isEmpty()) {
                                resultStack.push(negated);
                            } else if (negated.isEmpty()) {
                                resultStack.push(leftResult);
                            } else {
                                resultStack.push(model.assignPointer(
                                    model.union(leftResult.getFirst(), negated.getFirst())
                                ));
                            }
                        } else { // "and"
                            if (leftResult.isEmpty() || negated.isEmpty()) {
                                resultStack.push(new ArrayList<>());
                            } else {
                                resultStack.push(model.assignPointer(
                                    model.intersect(leftResult.getFirst(), negated.getFirst())
                                ));
                            }
                        }
                    }

                } else if (endsWithOperator) {
                    // ── Bracket ada di KANAN, operator biner and/or di akhir ─────────
                    // Contoh: "not effects or (empirical and evaluation)"
                    //   terms = ["not", "effects", "or"]
                    //   → proses sisi kiri dulu, lalu gabung dengan bracket
                    // ────────────────────────────────────────────────────────────────
                    String pendingOp = terms.remove(terms.size() - 1);

                    List<PostingNode> leftResult = terms.isEmpty()
                            ? new ArrayList<>()
                            : model.process(terms);

                    List<PostingNode> rightResult = resultStack.pop();

                    if ("or".equals(pendingOp)) {
                        if (leftResult.isEmpty()) {
                            resultStack.push(rightResult);
                        } else if (rightResult.isEmpty()) {
                            resultStack.push(leftResult);
                        } else {
                            resultStack.push(model.assignPointer(
                                model.union(leftResult.getFirst(), rightResult.getFirst())
                            ));
                        }
                    } else if ("and".equals(pendingOp)) {
                        if (leftResult.isEmpty() || rightResult.isEmpty()) {
                            resultStack.push(new ArrayList<>());
                        } else {
                            resultStack.push(model.assignPointer(
                                model.intersect(leftResult.getFirst(), rightResult.getFirst())
                            ));
                        }
                    } else {
                        resultStack.push(leftResult);
                    }

                } else if (startsWithOperator) {
                    // ── Bracket ada di KIRI, terms diawali and/or ────────────────────
                    // Contoh: "(empirical AND evaluation) OR NOT effects"
                    //   terms = ["or", "not", "effects"]
                    //   prevResult = hasil bracket kiri
                    // ────────────────────────────────────────────────────────────────
                    List<PostingNode> prevResult = resultStack.pop();
                    resultStack.push(model.process(terms, prevResult));

                } else {
                    resultStack.push(model.process(terms));
                }
            } else {
                resultStack.push(model.process(terms));
            }
        }

        return resultStack.isEmpty() ? new ArrayList<>() : resultStack.pop();
    }

    /**
     * Konversi array token mentah ke List<String> terms,
     * dengan preprocessing untuk non-operator.
     */
    private List<String> buildTerms(String[] queries) {
        List<String> terms = new ArrayList<>();
        for (String q : queries) {
            if (!q.equals("not") && !q.equals("and") && !q.equals("or")) {
                List<String> res = preprocessor.process(q);
                terms.addAll(res);
            } else {
                terms.add(q);
            }
        }
        return terms;
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
            } else if (c == ' ') {
                if (temp.length() > 0) {
                    token.add(temp);
                    temp = "";
                }
            } else {
                temp = temp + c;
            }
        }
        if (temp.length() > 0) {
            token.add(temp);
        }

        return token;
    }
}