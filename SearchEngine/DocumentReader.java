import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DocumentReader {
    private String folderPath;

    public DocumentReader(String folderPath) {
        this.folderPath = folderPath;
    }

    public Map<Integer, String> readAll() {
        Map<Integer, String> documents = new HashMap<>();

        for (int i = 1; i <= 100; i++) {
            String filePath = folderPath + "/" + i + ".txt";
            String content = readFile(filePath, i);
            if (content != null) {
                documents.put(i, content);
            }
        }

        System.out.println("Berhasil membaca " + documents.size() + " dokumen.");
        return documents;
    }

    private String readFile(String filePath, int docID) {
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        } catch (FileNotFoundException e) {
            System.out.println("File tidak ditemukan: " + filePath);
            return null;
        } catch (IOException e) {
            System.out.println("Gagal membaca file: " + filePath + " --> " + e.getMessage());
            return null;
        }

        return content.toString().trim();
    }

    // public static void main(String[] args) {
    // DocumentReader reader = new DocumentReader("DataSet");
    // Map<Integer, String> docs = reader.readAll();
    // }
}