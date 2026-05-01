import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InvertedIndex {
    private Map<String, List<PostingNode>> postingList;
    private HashMap<String, Integer> postingLengths;
    private static int maxDocID;
    
    public InvertedIndex() {
        this.postingList = new HashMap<>();
        maxDocID = 0;
    }

    public void addDocument(String term, int docID) {
        if (postingList.containsKey(term)) {
            List<PostingNode> newList = new LinkedList<>();
            newList.add(new PostingNode(docID));
            postingList.put(term, newList);
        } else {
            if (!postingList.get(term).contains(docID)) {
                postingList.get(term).getLast().setNext(new PostingNode(docID));
            }
        }

        maxDocID = Math.max(maxDocID, docID);
    }

    /*
     * public void addDocument(String term, int docID) {
     * if (!postingList.containsKey(term)) {
     * // Term belum ada → buat posting list baru
     * LinkedList<PostingNode> newList = new LinkedList<>();
     * newList.add(new PostingNode(docID));
     * postingList.put(term, newList);
     * } else {
     * // Term sudah ada → insert docID secara sorted
     * LinkedList<PostingNode> list = postingList.get(term);
     * 
     * // Cek duplikat
     * boolean exists = list.stream()
     * .anyMatch(node -> node.getDocID() == docID);
     * if (exists) return;
     * 
     * // Cari posisi insert agar tetap sorted ascending
     * ListIterator<PostingNode> iterator = list.listIterator();
     * boolean inserted = false;
     * 
     * while (iterator.hasNext()) {
     * PostingNode current = iterator.next();
     * if (docID < current.getDocID()) {
     * iterator.previous(); // mundur satu posisi
     * iterator.add(new PostingNode(docID));
     * inserted = true;
     * break;
     * }
     * }
     * 
     * // Jika docID terbesar, tambahkan di akhir
     * if (!inserted) {
     * list.addLast(new PostingNode(docID));
     * }
     * }
     * }
     */

    public void assignSkipPointer() {
        for (String term : postingList.keySet()) {
            int length = postingLengths.get(term);

            int skipInterval = (int) Math.sqrt(length);

            PostingNode[] nodes = new PostingNode[length];
            PostingNode current = postingList.get(term).getFirst();
            for (int i = 0; i < length; i++) {
                nodes[i] = current;
                current = current.getNext();
            }

            for (int i = 0; i + skipInterval < length; i += skipInterval) {
                nodes[i].setSkip(nodes[i + skipInterval]);
            }
        }
    }

    public List<PostingNode> intersects(List<String> terms){
        terms.sort(null);

        List<PostingNode> res = postingList.get(terms.removeFirst());
        while(!terms.equals(null)){
            res = intersect(res.getFirst(), postingList.get(terms.removeFirst()).getFirst());
        }
        return res;
    }

    // AND
    public List<PostingNode> intersect(PostingNode p1, PostingNode p2) {
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
                        && p1.getSkip().getDocID() < p2.getDocID()) {
                    p1 = p1.getSkip();
                }
                p1 = p1.getNext();
            } else {
                if (p2.getSkip() != null
                        && p2.getSkip().getDocID() < p1.getDocID()) {
                    p2 = p2.getSkip();
                }
                p2 = p2.getNext();
            }
        }
        return answer;
    }

    // OR
    public List<Integer> union(PostingNode p1, PostingNode p2) {
        List<Integer> answer = new ArrayList<>();

        while (p1 != null && p2 != null) {
            int doc1 = p1.getDocID();
            int doc2 = p2.getDocID();

            if (doc1 == doc2) {
                if (!answer.contains(p1.getDocID())) {
                    answer.add(p1.getDocID());
                }
                p1 = p1.getNext();
                p2 = p2.getNext();
            } else if (doc1 < doc2) {
                if (!answer.contains(p1.getDocID())) {
                    answer.add(p1.getDocID());
                }
                p1 = p1.getNext();
            } else {
                if (!answer.contains(p2.getDocID())) {
                    answer.add(p2.getDocID());
                }
                p2 = p2.getNext();
            }
        }
        while (p1 != null) {
            answer.add(p1.getDocID());
            p1 = p1.getNext();
        }
        while (p2 != null) {
            answer.add(p2.getDocID());
            p2 = p2.getNext();
        }

        return answer;
    }

    public List<Integer> negate(PostingNode p1) {
        List<Integer> result = new ArrayList<>();
 
        int j = p1.getDocID();
        for(int i = 1; i <= maxDocID; i++){
            if(i != j) result.add(i);
            if(i >= j) {
                j = p1.getNext().getDocID();
                p1 = p1.getNext();
            } 
        }
        return result;
    }
}
