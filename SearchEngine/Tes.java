// import java.util.ArrayList;
// import java.util.List;
// import java.util.Stack;

// public class BooleanModel {
//     private InvertedIndex invertedIndex;
//     private static int maxDocID;

//     public BooleanModel() {
//         maxDocID = 0;
//     }

//     public List<PostingNode> process(List<String> preProcessedQuery) {
//         // Kasus 1: Semua AND, tidak ada NOT dan OR
//         if (!preProcessedQuery.contains("not") && !preProcessedQuery.contains("or")) {
//             List<String> terms = preProcessedQuery.stream()
//                     .filter(s -> !s.equals("and"))
//                     .collect(java.util.stream.Collectors.toList());
//             return toList(assignPointer(intersects(terms)));
//         }

//         // Kasus 2: Semua OR, tidak ada NOT dan AND
//         if (!preProcessedQuery.contains("not") && !preProcessedQuery.contains("and")) {
//             List<String> terms = preProcessedQuery.stream()
//                     .filter(s -> !s.equals("or"))
//                     .collect(java.util.stream.Collectors.toList());
//             return toList(assignPointer(unions(terms)));
//         }

//         // Kasus 3: Campuran (mengandung NOT, atau campuran AND/OR)
//         // Evaluasi dari kiri ke kanan dengan precedence: NOT > AND > OR
//         // Strategi: kumpulkan posting list per operand, lalu gabungkan

//         /*
//          * Query: "axel and not alek or budi"
//          * 
//          * i=0: token="axel" → result = posting(axel)
//          * i=1: token="and" → pendingOperator="and"
//          * i=2: token="not" → i++, ambil "alek", current = negate(posting(alek))
//          * → result = intersect(result, current)
//          * i=4: token="or" → pendingOperator="or"
//          * i=5: token="budi" → current = posting(budi)
//          * → result = union(result, current)
//          */

//         List<PostingNode> result = null;
//         String pendingOperator = null;

//         int i = 0;
//         List<String> query = new ArrayList<>(preProcessedQuery);

//         while (i < query.size()) {
//             String token = query.get(i);

//             if (token.equals("and") || token.equals("or")) {
//                 pendingOperator = token;
//                 i++;
//                 continue;
//             }

//             // Ambil posting list untuk operand saat ini (bisa diawali NOT)
//             List<PostingNode> current;

//             if (token.equals("not")) {
//                 i++;
//                 String term = query.get(i);
//                 List<PostingNode> postingList = assignPointer(
//                         new ArrayList<>(invertedIndex.getPostingList(term)));
//                 current = negate(postingList.getFirst());
//             } else {
//                 List<PostingNode> postingList = assignPointer(
//                         new ArrayList<>(invertedIndex.getPostingList(token)));
//                 current = postingList;
//             }

//             current = assignPointer(current);

//             if (result == null) {
//                 result = current;
//             } else {
//                 result = assignPointer(result);
//                 if ("or".equals(pendingOperator)) {
//                     result = union(result.getFirst(), current.getFirst());
//                 } else {
//                     // Default AND
//                     result = intersect(result.getFirst(), current.getFirst());
//                 }
//                 result = assignPointer(result);
//             }

//             i++;
//         }

//         return toList(result != null ? result : new ArrayList<>());
//     }

//     public List<Integer> process(List<String> preProcessedQuery) {

//         // Kasus 1: Semua AND, tidak ada NOT dan OR
//         if (!preProcessedQuery.contains("not") && !preProcessedQuery.contains("or")) {
//             List<String> terms = preProcessedQuery.stream()
//                     .filter(s -> !s.equals("and"))
//                     .collect(java.util.stream.Collectors.toList());
//             return toList(intersects(terms)); // intersects sudah return hasil final
//         }

//         // Kasus 2: Semua OR, tidak ada NOT dan AND
//         if (!preProcessedQuery.contains("not") && !preProcessedQuery.contains("and")) {
//             List<String> terms = preProcessedQuery.stream()
//                     .filter(s -> !s.equals("or"))
//                     .collect(java.util.stream.Collectors.toList());
//             return toList(unions(terms));
//         }

//         // Kasus 3: Campuran
//         List<PostingNode> result = null;
//         String pendingOperator = null;
//         List<String> query = new ArrayList<>(preProcessedQuery);

//         int i = 0;
//         while (i < query.size()) {
//             String token = query.get(i);

//             if (token.equals("and") || token.equals("or")) {
//                 pendingOperator = token;
//                 i++;
//                 continue;
//             }

//             List<PostingNode> current;

//             if (token.equals("not")) {
//                 i++;
//                 String term = query.get(i);
//                 PostingNode p = invertedIndex.getPostingList(term).getFirst();
//                 // negate menghasilkan list baru → perlu assignPointer
//                 current = assignPointer(negate(p));
//             } else {
//                 // Dari invertedIndex → skip pointer sudah ada, tidak perlu assignPointer
//                 current = invertedIndex.getPostingList(token);
//             }

//             if (result == null) {
//                 result = current;
//             } else {
//                 if ("or".equals(pendingOperator)) {
//                     // union menghasilkan list baru → perlu assignPointer
//                     result = assignPointer(union(result.getFirst(), current.getFirst()));
//                 } else {
//                     // intersect menghasilkan list baru → perlu assignPointer
//                     result = assignPointer(intersect(result.getFirst(), current.getFirst()));
//                 }
//                 pendingOperator = null;
//             }

//             i++;
//         }

//         return toList(result != null ? result : new ArrayList<>());
//     }

//     private List<PostingNode> assignPointer(List<PostingNode> nodes) {
//         if (nodes == null || nodes.isEmpty())
//             return nodes;

//         int n = nodes.size();
//         int skipInterval = (int) Math.sqrt(n);

//         // Sambungkan next pointer
//         for (int i = 0; i < n - 1; i++) {
//             nodes.get(i).setNext(nodes.get(i + 1));
//         }
//         nodes.get(n - 1).setNext(null);

//         // Pasang skip pointer setiap sqrt(n) langkah
//         for (int i = 0; i < n; i++) {
//             int skipTarget = i + skipInterval;
//             if (skipTarget < n) {
//                 nodes.get(i).setSkip(nodes.get(skipTarget));
//             } else {
//                 nodes.get(i).setSkip(null);
//             }
//         }

//         return nodes;
//     }

//     // Konversi List<Integer> → PostingNode dengan skip pointer
//     // Skip pointer dipasang setiap sqrt(n) node
//     private PostingNode toPostingNode(List<Integer> list) {
//         if (list == null || list.isEmpty())
//             return null;

//         int n = list.size();
//         int skipStep = (int) Math.sqrt(n); // jarak antar skip pointer

//         // Buat semua node terlebih dahulu
//         PostingNode[] nodes = new PostingNode[n];
//         for (int i = 0; i < n; i++) {
//             nodes[i] = new PostingNode(list.get(i));
//         }

//         // Hubungkan next pointer
//         for (int i = 0; i < n - 1; i++) {
//             nodes[i].setNext(nodes[i + 1]);
//         }

//         // Pasang skip pointer pada node yang merupakan kelipatan skipStep
//         // Node ke-i mendapat skip ke node ke-(i + skipStep), jika masih dalam batas
//         for (int i = 0; i + skipStep < n; i += skipStep) {
//             nodes[i].setSkip(nodes[i + skipStep]);
//         }

//         return nodes[0];
//     }

//     public List<Integer> intersects(List<String> terms) {
//         if (terms.equals(null))
//             return null;
//         // sort posting list terpendek
//         terms.sort((a, b) -> invertedIndex.getPostingList(a).size() - invertedIndex.getPostingList(b).size());

//         // isi temp = posting list dari term pertama
//         // jadi 1-5-6-7-8
//         List<PostingNode> temp = invertedIndex.getPostingList(terms.removeFirst());

//         // unpar : 1 5 7 8
//         // axel : 1 2 4 7
//         // alek : 1 7

//         // temp nya itu jadinya untuk term unpar dahulu dimana isi tempnya itu 1 5 7 8
//         List<Integer> res = new ArrayList<>();

//         /**
//          * // Intersect bertahap dengan term berikutnya
//          * while (!terms.isEmpty()) {
//          * PostingNode next =
//          * invertedIndex.getPostingList(terms.removeFirst()).getFirst();
//          * 
//          * result = intersect(toPostingNode(result), next);
//          * }
//          */

//         while (!terms.isEmpty()) {
//             // intersect dari alek (dari 1 7 ) dengan axel (1 2 4 7)
//             if (res.isEmpty()) {
//                 res = intersect(temp.removeFirst(), invertedIndex.getPostingList(terms.removeFirst()).getFirst());
//             }
//         }
//         return res;
//     }

//     // AND
//     private List<Integer> intersect(PostingNode p1, PostingNode p2) {
//         List<PostingNode> answer = new ArrayList<>();

//         while (p1 != null && p2 != null) {
//             int doc1 = p1.getDocID();
//             int doc2 = p2.getDocID();

//             if (doc1 == doc2) {
//                 answer.add(p1.getDocID());
//                 p1 = p1.getNext();
//                 p2 = p2.getNext();
//             } else if (doc1 < doc2) {
//                 if (p1.getSkip() != null
//                         && p1.getSkip().getDocID() <= p2.getDocID()) {
//                     p1 = p1.getSkip();
//                 } else {
//                     p1 = p1.getNext();
//                 }
//             } else {
//                 if (p2.getSkip() != null
//                         && p2.getSkip().getDocID() <= p1.getDocID()) {
//                     p2 = p2.getSkip();
//                 } else {
//                     p2 = p2.getNext();
//                 }
//             }
//         }
//         return answer;
//     }

//     public List<Integer> unions(List<String> terms) {
//         // sort posting list terpendek
//         terms.sort((a, b) -> invertedIndex.getPostingList(a).size() - invertedIndex.getPostingList(b).size());

//         PostingNode head = invertedIndex.getPostingList(terms.removeFirst());
//         List<Integer> res = new ArrayList<>();
//         while (!terms.isEmpty()) {
//             res = union(res.getFirst(), invertedIndex.getPostingList(terms.removeFirst()).getFirst());
//         }
//         return res;
//     }

//     // OR
//     private List<Integer> union(PostingNode p1, PostingNode p2) {
//         List<Integer> answer = new ArrayList<>();

//         while (p1 != null && p2 != null) {
//             int doc1 = p1.getDocID();
//             int doc2 = p2.getDocID();

//             if (doc1 == doc2) {
//                 answer.add(p1.getDocID());
//                 p1 = p1.getNext();
//                 p2 = p2.getNext();
//             } else if (doc1 < doc2) {
//                 answer.add(p1.getDocID());
//                 p1 = p1.getNext();
//             } else {
//                 answer.add(p2.getDocID());
//                 p2 = p2.getNext();
//             }
//         }
//         while (p1 != null) {
//             answer.add(p1.getDocID());
//             p1 = p1.getNext();
//         }
//         while (p2 != null) {
//             answer.add(p2.getDocID());
//             p2 = p2.getNext();
//         }

//         return answer;
//     }

//     public List<Integer> negate(PostingNode p1) {
//         List<Integer> result = new ArrayList<>();

//         int j = p1.getDocID();
//         for (int i = 1; i <= maxDocID; i++) {
//             if (i != j)
//                 result.add(i);
//             if (i >= j) {
//                 p1 = p1.getNext();
//                 if (p1 != null) {
//                     j = p1.getDocID();
//                 } else {
//                     j = maxDocID + 1;
//                 }
//             }
//         }
//         return result;
//     }

//     /**
//      * Resolve token ke posting list.
//      * Kalau token adalah "__stack__", ambil dari resultStack.
//      * Kalau token adalah term biasa, lookup ke invertedIndex.
//      */
//     private List<PostingNode> resolveNext(String token,
//             Stack<List<PostingNode>> resultStack) {
//         if (token.equals("__stack__") && !resultStack.isEmpty()) {
//             return resultStack.pop();
//         }
//         return invertedIndex.getPostingList(token);
//     }

//     // Konversi List<PostingNode> → head node untuk intersect/union
//     private PostingNode toHead(List<PostingNode> list) {
//         if (list == null || list.isEmpty())
//             return null;
//         PostingNode head = new PostingNode(list.get(0).getDocID());
//         PostingNode curr = head;
//         for (int i = 1; i < list.size(); i++) {
//             curr.setNext(new PostingNode(list.get(i).getDocID()));
//             curr = curr.getNext();
//         }
//         return head;
//     }

//     private List<PostingNode> negates(List<PostingNode> posting) {
//         PostingNode head = toHead(posting);
//         // gunakan negate yang sudah ada
//         List<Integer> negatedIDs = negate(head);
//         List<PostingNode> result = new ArrayList<>();
//         for (int id : negatedIDs)
//             result.add(new PostingNode(id));
//         return result;
//     }

// }
