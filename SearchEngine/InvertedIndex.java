import java.util.ArrayList;
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
    
    public void addDocument(){
        
    }

    public List<Integer> intersect(List<Integer> p1, List<Integer> p2){
        List<Integer> answer = new ArrayList<>();

        int index1 = 0;
        int index2 = 0;
        while(p1 != null && p2 != null){
            int doc1 = p1.get(index1);
            int doc2 = p2.get(index2);

            if(doc1 == doc2){
                answer.add(doc1);
                index1++; index2++;
            }
            else if(doc1 < doc2){
                index1++;
            }
            else index2++;
        }
        
        return answer;
    }
}
