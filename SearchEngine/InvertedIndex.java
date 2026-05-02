import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InvertedIndex {
    private Map<String, List<PostingNode>> postingList;
    private HashMap<String, Integer> postingLengths;
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

    public void addDocument(String term, int docID) {
        if (!postingList.containsKey(term)) {
            List<PostingNode> newList = new LinkedList<>();
            newList.add(new PostingNode(docID));
            postingList.put(term, newList);
            postingLengths.put(term, postingLengths.getOrDefault(term, 0) + 1);
        } else {
            if (!isDocIdExist(term, docID)) {
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

}
