public class PostingNode {
    int docID;
    PostingNode next;
    PostingNode skip; 
 
    public PostingNode(int docID) {
        this.docID = docID;
        this.next = null;
        this.skip = null;
    }
}