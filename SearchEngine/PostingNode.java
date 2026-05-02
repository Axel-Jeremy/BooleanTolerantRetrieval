public class PostingNode {
    private int docID;
    private PostingNode next;
    private PostingNode skip; 
 
    public PostingNode(int docID) {
        this.docID = docID;
        this.next = null;
        this.skip = null;
    }

    public int getDocID() {
        return docID;
    }

    public void setDocID(int docID) {
        this.docID = docID;
    }

    public PostingNode getNext() {
        return next;
    }

    public void setNext(PostingNode next) {
        this.next = next;
    }

    public PostingNode getSkip() {
        return skip;
    }

    public void setSkip(PostingNode skip) {
        this.skip = skip;
    }
}