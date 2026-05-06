import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvertedIndex {
    private Map<String, List<PostingNode>> postingList;
    private HashMap<String, Integer> postingLengths;
    private Set<String> rawVocabulary = new HashSet<>();
    private int maxDocID;

    public InvertedIndex() {
        this.postingList = new HashMap<>();
        this.postingLengths = new HashMap<>();
        maxDocID = 0;
    }

    public List<PostingNode> getPostingList(String term) {
        return postingList.getOrDefault(term, new ArrayList<>());
    }

    public int getMaxDocID() {
        return maxDocID;
    }

    public Set<String> getAllTerms() {
        return postingList.keySet();
    }

    public boolean isDocIdExist(String term, int targetDocId) {
        List<PostingNode> nodes = postingList.get(term);

        if (nodes == null)
            return false;

        for (PostingNode node : nodes) {
            if (node.getDocID() == targetDocId)
                return true;
        }
        return false;
    }

    public void addRawTerm(String rawTerm) {
        rawVocabulary.add(rawTerm.toLowerCase());
    }

    public Set<String> getRawVocabulary() {
        return rawVocabulary;
    }

    public void addDocument(String term, int docID) {
        if (!postingList.containsKey(term)) {
            List<PostingNode> newList = new LinkedList<>();
            newList.add(new PostingNode(docID));
            postingList.put(term, newList);
            postingLengths.put(term, postingLengths.getOrDefault(term, 0) + 1);
        } else {
            if (!isDocIdExist(term, docID)) {
                PostingNode newNode = new PostingNode(docID);
                postingList.get(term).getLast().setNext(newNode); // hubungkan via linked list
                postingList.get(term).add(newNode); // ← tambah ke List juga
                postingLengths.put(term, postingLengths.get(term) + 1);
            }
        }
        maxDocID = Math.max(maxDocID, docID);
    }

    public void assignSkipPointer() {
        for (String term : postingList.keySet()) {
            // Hitung panjang aktual dengan traverse linked list
            int length = postingList.get(term).size();

            if (length < 3)
                continue; // skip pointer tidak berguna untuk list pendek

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

}
