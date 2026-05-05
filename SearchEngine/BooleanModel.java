import java.util.ArrayList;
import java.util.List;

public class BooleanModel {
    private static InvertedIndex invertedIndex;
    private static int maxDocID;

    public BooleanModel() {
    }

    public void setMaxDocID(int maxDocID) {
        BooleanModel.maxDocID = maxDocID;
    }

    public void setInvertedIndex(InvertedIndex invertedIndex) {
        BooleanModel.invertedIndex = invertedIndex;
    }

    /**
     * Overload dengan prevResult: dipakai ketika ada hasil bracket sebelumnya.
     *
     * FIX BUG 1 & 2:
     * - Jika query dimulai dengan operator (and/or/not), prevResult harus
     *   langsung dijadikan nilai awal `result` sebelum token diproses.
     * - Setelah blok `not` tanpa term berikutnya, `i` harus di-increment
     *   agar loop tidak macet / salah merge.
     */
    public List<PostingNode> process(List<String> preProcessedQuery,
            List<PostingNode> prevResult) {

        // Kasus khusus: ["not"] sendirian → negate prevResult
        if (preProcessedQuery.size() == 1 && preProcessedQuery.get(0).equals("not")) {
            return assignPointer(negate(prevResult.getFirst()));
        }

        List<PostingNode> result = null;
        String pendingOperator = null;

        // ── FIX BUG 1 ──────────────────────────────────────────────────────────
        // Jika query diawali dengan operator (and / or / not), prevResult
        // adalah operan kiri — jadikan ia nilai awal result SEBELUM iterasi.
        String firstToken = preProcessedQuery.get(0);
        if (firstToken.equals("and") || firstToken.equals("or") || firstToken.equals("not")) {
            result = new ArrayList<>(prevResult);   // seed result dari bracket sebelumnya
        }
        // ───────────────────────────────────────────────────────────────────────

        int i = 0;
        while (i < preProcessedQuery.size()) {
            String token = preProcessedQuery.get(i);

            if (token.equals("and") || token.equals("or")) {
                pendingOperator = token;
                i++;
                continue;
            }

            List<PostingNode> current;

            if (token.equals("not")) {
                i++; // lewati "not"

                if (i < preProcessedQuery.size()) {
                    // NOT diikuti term biasa
                    String term = preProcessedQuery.get(i);
                    PostingNode p = invertedIndex.getPostingList(term).getFirst();
                    current = assignPointer(negate(p));
                    // i akan di-increment di akhir loop (fall-through ke blok merge)
                } else {
                    // ── FIX BUG 2 ──────────────────────────────────────────────
                    // NOT tanpa term berikutnya → negate prevResult (hasil bracket)
                    current = assignPointer(negate(assignPointer(prevResult).getFirst()));
                    // i sudah melampaui batas; blok merge di bawah akan menangani,
                    // lalu i++ di akhir membuat while-condition false → loop selesai.
                    // ─────────────────────────────────────────────────────────────
                }
            } else {
                current = invertedIndex.getPostingList(token);
            }

            // ── Merge current ke result ─────────────────────────────────────────
            if (result == null || result.isEmpty()) {
                result = current;
            } else if (current == null || current.isEmpty()) {
                if (!"or".equals(pendingOperator)) {
                    result = new ArrayList<>();
                }
                pendingOperator = null;
            } else {
                if ("or".equals(pendingOperator)) {
                    result = assignPointer(union(result.getFirst(), current.getFirst()));
                } else if ("and".equals(pendingOperator)) {
                    result = assignPointer(intersect(result.getFirst(), current.getFirst()));
                }
                pendingOperator = null;
            }
            // ───────────────────────────────────────────────────────────────────

            i++;
        }

        return result != null ? result : new ArrayList<>();
    }

    public List<PostingNode> process(List<String> preProcessedQuery) {
        List<PostingNode> result = null;

        if (!preProcessedQuery.contains("not")
                && !preProcessedQuery.contains("or")) {
            List<String> terms = preProcessedQuery.stream()
                    .filter(s -> !s.equals("and"))
                    .collect(java.util.stream.Collectors.toList());
            return intersects(terms);
        } else if (!preProcessedQuery.contains("not")
                && !preProcessedQuery.contains("and")) {
            List<String> terms = preProcessedQuery.stream()
                    .filter(s -> !s.equals("or"))
                    .collect(java.util.stream.Collectors.toList());
            return unions(terms);
        }

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
                current = assignPointer(negate(p));
            } else {
                current = invertedIndex.getPostingList(token);
            }

            if (result == null || result.isEmpty()) {
                result = current;
            } else if (current == null || current.isEmpty()) {
                if (!"or".equals(pendingOperator)) {
                    result = new ArrayList<>();
                }
                pendingOperator = null;
            } else {
                if ("or".equals(pendingOperator)) {
                    result = assignPointer(union(result.getFirst(), current.getFirst()));
                } else {
                    result = assignPointer(intersect(result.getFirst(), current.getFirst()));
                }
                pendingOperator = null;
            }

            i++;
        }

        return result != null ? result : new ArrayList<>();
    }

    public List<PostingNode> assignPointer(List<PostingNode> nodes) {
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
        terms.sort((a, b) -> invertedIndex.getPostingList(a).size() - invertedIndex.getPostingList(b).size());

        List<PostingNode> res = invertedIndex.getPostingList(terms.removeFirst());
        if (res.isEmpty()) return new ArrayList<>();

        while (!terms.isEmpty()) {
            if (res.isEmpty()) return new ArrayList<>();
            List<PostingNode> next = invertedIndex.getPostingList(terms.removeFirst());
            if (next.isEmpty()) return new ArrayList<>();
            res = intersect(res.getFirst(), next.getFirst());
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
                answer.add(new PostingNode(p1.getDocID()));
                p1 = p1.getNext();
                p2 = p2.getNext();
            } else if (doc1 < doc2) {
                if (p1.getSkip() != null && p1.getSkip().getDocID() <= p2.getDocID()) {
                    p1 = p1.getSkip();
                } else {
                    p1 = p1.getNext();
                }
            } else {
                if (p2.getSkip() != null && p2.getSkip().getDocID() <= p1.getDocID()) {
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
            if (res.isEmpty()) return new ArrayList<>();
            List<PostingNode> next = invertedIndex.getPostingList(terms.removeFirst());
            if (next.isEmpty()) return new ArrayList<>();
            res = union(res.getFirst(), next.getFirst());
        }
        return res;
    }

    // OR
    public List<PostingNode> union(PostingNode p1, PostingNode p2) {
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
            if (i == j) {
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