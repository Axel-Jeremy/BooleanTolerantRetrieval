import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Query {
    String query;
    Stack<String> orderProcess;
    Tokenizer tokenizer;
    Stemmer stemmer;

    public Query(String query) {
        this.query = query;
        this.query.trim();
        this.query.toLowerCase();
        this.tokenizer = new Tokenizer();
        this.stemmer = new Stemmer();
        this.orderProcess = new Stack<>();
    }

    public List<Integer> processQuery(){
        List<String> splittedQuery = splitQuery();
        
        for(String kata : splittedQuery){
            if(kata.charAt(0) != ')'){
                orderProcess.push(kata);
            }
            else{
                String temp = "";
                while(!orderProcess.peek().equals("(")){
                    temp = orderProcess.pop() + " " + temp;
                    // (a and b and c)
                    //not a and b 
                    //term1 = not a
                    //term2 = b
                    //process(term1, term2);
                }
                orderProcess.pop();

                String[] terms = temp.trim().split(" ");

                for (int i = 0; i < terms.length; i++) {
                    if (!terms[i].equals("not")
                    && !terms[i].equals("and")
                    && !terms[i].equals("or")) {
                        terms[i] = this.stemmer.porterStemmer(terms[i]);
                    }
                }
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
