import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        InvertedIndex invertedIndex = new InvertedIndex();
        Query query = new Query(sc.nextLine());
        //(not not not not alex and not not) or not

        // kin AND alek OR axel
        // (unpar and (fakultas and informatika)or prabowo) and axel
        // ((... and ...   ) or ...)
        // (( )( )( )( )( ) (((( )))))
        //di 1 kata, kalo ketemu kurung, loop terus sampe ga ketemu kurung lagi (di 1 kata yang sama)
        //kalau ketemu kurung buka loop terus sampe ketemu kurung tutup
        //tapi kalau ketemu kurung buka lagi kurung buka yang sebelumnya di jadikan peringkat 2 dan kurung buka yang baru jadi peringka

        //ketika ketemu kurung tutup maka akan diberikan ke kurung tutup yang peringkat 1  1 
        // List<String> term = new ArrayList<>();
        // List<String> booleanSyntax = new ArrayList<>();

        // query.trim();

        // String[] terms = query.split(" ");
        // for (int i = 1; i < terms.length; i++) {
        //     if (terms[i].equalsIgnoreCase("AND") || terms[i].equalsIgnoreCase("OR") || terms[i].equalsIgnoreCase("NOT")){
        //         booleanSyntax.add(terms[i]);
        //     }
        //     term.add(terms[i]);
        // }
        sc.close();
    }
}