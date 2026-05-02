import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Query {
    String query;
    Stack<String> orderProcess;
    TextPreprocessor preprocessor;

    public Query(String query) {
        this.query = query;
        this.query.trim();
        this.query.toLowerCase();
        this.orderProcess = new Stack<>();
        this.preprocessor = new TextPreprocessor();
    }

    public List<String> processQuery(){
        List<String> splittedQuery = splitQuery();
        
        for(String kata : splittedQuery){
            if(kata.charAt(0) != ')'){
                orderProcess.push(kata);
            }
            else{
                String queryNoBracket = "";
                while(!orderProcess.peek().equals("(")){
                    queryNoBracket = orderProcess.pop() + " " + queryNoBracket;
                    // (a and b and c)
                    //not a and b 
                    //term1 = not a
                    //term2 = b
                    //process(term1, term2);
                }
                orderProcess.pop();

                String[] queries = queryNoBracket.trim().split(" ");
                List<String> terms = new ArrayList<>();
                for (int i = 0; i < queries.length; i++) {
                    if (!queries[i].equals("not")
                    && !queries[i].equals("and")
                    && !queries[i].equals("or")) {
                        List<String> res = preprocessor.process(queries[i]);
                        for(String term : res) terms.add(term);
                    }
                }
                return terms;
            }
        }
        return null;
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
