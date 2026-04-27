import java.util.List;
import java.util.Map;
import java.util.Set;

public class InvertedIndex {
    private Map<String, List<Integer>> postingList;
    private Set<Integer> documents;
    
    public InvertedIndex(Map<String, List<Integer>> postingList, Set<Integer> documents) {
        this.postingList = postingList;
        this.documents = documents;
    }
    
    
}
