import java.util.ArrayList;
import java.util.List;

public class BooleanModel {
    private static InvertedIndex invertedIndex;
    private static int maxDocID;

    public BooleanModel(){
        maxDocID = 0;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public List<PostingNode> process(List<String> preProcessedQuery) {
        // o'neill and bryan
        // o neill and bryan
        List<PostingNode> result = null;

        // jika didalam query
        // semua dihubungkan dengan AND (... and ... and ... and ...)
        // atau semua dihubungkan dengan OR (... or ... or ... or ...)
        // dan tidak ada NOT

        if (!preProcessedQuery.contains("not")
                && !preProcessedQuery.contains("or")) {
            return intersects(preProcessedQuery);
        } else if (!preProcessedQuery.contains("not")
                && !preProcessedQuery.contains("and")) {
            return unions(preProcessedQuery);
        }

        // while (!preProcessedQuery.isEmpty()) {
        //     String p1 = preProcessedQuery.removeFirst();
        //     String kueri = preProcessedQuery.removeFirst();
        //     String p2 = preProcessedQuery.removeFirst();
        //     // (... and not ... and ... and ...) -> intersects if not contains OR
        //     // (... and ... or ...) -> intersect if contains OR
        //     if (p1.equals("not")) {
                
        //     }
        // }
        // return toList(result);

        //contoh kasus
        /*
         * Query: "axel and not alek or budi"
         * 
         * i=0: token="axel" → result = posting(axel)
         * i=1: token="and" → pendingOperator="and"
         * i=2: token="not" → i++, ambil "alek", current = negate(posting(alek))
         * → result = intersect(result, current)
         * i=4: token="or" → pendingOperator="or"
         * i=5: token="budi" → current = posting(budi)
         * → result = union(result, current)
         */
        String pendingOperator = null;
        List<String> query = new ArrayList<>(preProcessedQuery);

        int i = 0;
        while (i < query.size()) {
            String token = query.get(i);

            if (token.equals("and") || token.equals("or")) {
                pendingOperator = token;
                i++;
                continue;
            }

            List<PostingNode> current;

            if (token.equals("not")) {
                i++;
                String term = query.get(i);
                PostingNode p = invertedIndex.getPostingList(term).getFirst();
                // negate menghasilkan list baru → perlu assignPointer
                current = assignPointer(negate(p));
            } else {
                // Dari invertedIndex → skip pointer sudah ada, tidak perlu assignPointer
                current = invertedIndex.getPostingList(token);
            }

            if (result == null) {
                result = current;
            } else {
                if ("or".equals(pendingOperator)) {
                    // union menghasilkan list baru → perlu assignPointer
                    result = assignPointer(union(result.getFirst(), current.getFirst()));
                } else {
                    // intersect menghasilkan list baru → perlu assignPointer
                    result = assignPointer(intersect(result.getFirst(), current.getFirst()));
                }
                pendingOperator = null;
            }

            i++;
        }

        return result != null ? result : new ArrayList<>();
    }

    private List<Integer> toList(List<PostingNode> node) {
        List<Integer> list = new ArrayList<>();
        while (!node.isEmpty()) {
            list.add(node.removeFirst().getDocID());
        }
        return list;
    }

    private List<PostingNode> assignPointer(List<PostingNode> nodes) {
        if (nodes == null || nodes.isEmpty())
            return nodes;

        int n = nodes.size();
        int skipInterval = (int) Math.sqrt(n);

        for (int i = 0; i < n - 1; i++) {
            nodes.get(i).setNext(nodes.get(i + 1));
        }
        nodes.get(n - 1).setNext(null);

        for (int i = 0; i < n; i++) {
            int skipTarget = i + skipInterval;
            if (skipTarget < n) {
                nodes.get(i).setSkip(nodes.get(skipTarget));
            } else {
                nodes.get(i).setSkip(null);
            }
        }

        return nodes;
    }

    public List<PostingNode> intersects(List<String> terms) {
        terms.sort((a, b) -> invertedIndex.getPostingList(a).size() - invertedIndex.getPostingList(b).size()); //sort dari posting list yang paling pendek

        List<PostingNode> res = invertedIndex.getPostingList(terms.removeFirst());
        while (!terms.isEmpty()) {
            res = intersect(res.getFirst(), invertedIndex.getPostingList(terms.removeFirst()).getFirst());
        }
        return res;
    }

    // AND
    private List<PostingNode> intersect(PostingNode p1, PostingNode p2) {
        List<PostingNode> answer = new ArrayList<>();

        while (p1 != null && p2 != null) {
            int doc1 = p1.getDocID();
            int doc2 = p2.getDocID();
            //axel : 1 -> 5 -> 7
            //alek : 1 -> 7

            //axel and alek
            //res =  1 -> 7
            
            if (doc1 == doc2) {
                answer.add(new PostingNode(p1.getDocID()));
                p1 = p1.getNext();
                p2 = p2.getNext();
            } else if (doc1 < doc2) {
                if (p1.getSkip() != null
                        && p1.getSkip().getDocID() <= p2.getDocID()) {
                    p1 = p1.getSkip();
                } else {
                    p1 = p1.getNext();
                }
            } else {
                if (p2.getSkip() != null
                        && p2.getSkip().getDocID() <= p1.getDocID()) {
                    p2 = p2.getSkip();
                } else {
                    p2 = p2.getNext();
                }
            }
        }
        return answer;
    }

    public List<PostingNode> unions(List<String> terms) {
        List<PostingNode> res = invertedIndex.getPostingList(terms.removeFirst());
        while (!terms.isEmpty()) {
            res = union(res.getFirst(), invertedIndex.getPostingList(terms.removeFirst()).getFirst());
        }
        return res;
    }

    // OR
    private List<PostingNode> union(PostingNode p1, PostingNode p2) {
        List<PostingNode> answer = new ArrayList<>();

        while (p1 != null && p2 != null) {
            int doc1 = p1.getDocID();
            int doc2 = p2.getDocID();

            if (doc1 == doc2) {
                answer.add(new PostingNode(p1.getDocID()));
                p1 = p1.getNext();
                p2 = p2.getNext();
            } else if (doc1 < doc2) {
                answer.add(new PostingNode(p1.getDocID()));
                p1 = p1.getNext();
            } else {
                answer.add(new PostingNode(p2.getDocID()));
                p2 = p2.getNext();
            }
        }
        while (p1 != null) {
            answer.add(new PostingNode(p1.getDocID()));
            p1 = p1.getNext();
        }
        while (p2 != null) {
            answer.add(new PostingNode(p2.getDocID()));
            p2 = p2.getNext();
        }

        return answer;
    }

    public List<PostingNode> negate(PostingNode p1) {
        List<PostingNode> result = new ArrayList<>();

        int j = p1.getDocID();
        for (int i = 1; i <= maxDocID; i++) {
            if (i != j)
                result.add(new PostingNode(i));
            if (i >= j) {
                p1 = p1.getNext();
                if (p1 != null) {
                    j = p1.getDocID();
                } else {
                    j = maxDocID + 1;
                }
            }
        }
        return result;
    }
}