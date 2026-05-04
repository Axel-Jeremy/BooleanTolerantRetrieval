import java.util.*;

public class Main {
    public static void main(String[] args) {

        // 1. Baca semua dokumen
        DocumentReader reader = new DocumentReader("../DataSet");
        Map<Integer, String> documents = reader.readAll();

        if (documents.isEmpty()) {
            System.out.println("Tidak ada dokumen yang terbaca. Cek path folder.");
            return;
        }

        // 2. Inisialisasi komponen
        TextPreprocessor preprocessor = new TextPreprocessor();
        InvertedIndex invertedIndex = new InvertedIndex();

        // 3. Bangun inverted index — iterasi dari docID 1 ke 100 agar posting list
        // sorted
        System.out.println("Membangun inverted index...");
        for (int docID = 1; docID <= 100; docID++) {
            if (!documents.containsKey(docID))
                continue;

            List<String> terms = preprocessor.process(documents.get(docID));
            for (String term : terms) {
                invertedIndex.addDocument(term, docID);
            }
        }

        // 4. Pasang skip pointer setelah semua dokumen selesai diindeks
        invertedIndex.assignSkipPointer();
        
        System.out.println("Inverted index selesai dibangun.");

        // 5. Set maxDocID ke BooleanModel
        BooleanModel model = new BooleanModel();
        model.setInvertedIndex(invertedIndex);
        model.setMaxDocID(invertedIndex.getMaxDocID());

        // 6. Loop query — user bisa input query berulang kali
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\nMasukkan query (atau 'exit' untuk keluar): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit"))
                break;
            if (input.isEmpty())
                continue;

            // 7. Proses query
            Query query = new Query(input);
            query.setInvertedIndex(invertedIndex);
            query.setModel(model);

            List<PostingNode> result = query.preProcess();

            // 8. Tampilkan hasil
            if (result == null || result.isEmpty()) {
                System.out.println("Tidak ada dokumen yang cocok.");
            } else {
                System.out.print("Dokumen yang relevan: ");
                for (PostingNode node : result) {
                    System.out.print(node.getDocID() + " ");
                }
                System.out.println();
            }
        }

        scanner.close();
    }
}