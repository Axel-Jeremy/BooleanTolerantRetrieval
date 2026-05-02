import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooleanModel {
    private InvertedIndex invertedIndex;
    private int maxDocID;

    public BooleanModel(InvertedIndex invertedIndex, int maxDocID){
        this.invertedIndex = invertedIndex;
        this.maxDocID = maxDocID;
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

            if (doc1 == doc2) {
                answer.add(p1);
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
                answer.add(p1);
                p1 = p1.getNext();
                p2 = p2.getNext();
            } else if (doc1 < doc2) {
                answer.add(p1);
                p1 = p1.getNext();
            } else {
                answer.add(p2);
                p2 = p2.getNext();
            }
        }
        while (p1 != null) {
            answer.add(p1);
            p1 = p1.getNext();
        }
        while (p2 != null) {
            answer.add(p2);
            p2 = p2.getNext();
        }

        return answer;
    }

    public List<Integer> negate(PostingNode p1) {
        List<Integer> result = new ArrayList<>();

        int j = p1.getDocID();
        for (int i = 1; i <= maxDocID; i++) {
            if (i != j)
                result.add(i);
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
