public class StemmerTester {
    public static void main(String[] args) {
        Stemmer s = new Stemmer();
        Tokenizer t = new Tokenizer();
        Query q = new Query("ayam and bebek and sapi and kambing");
        System.out.println(t.process("ayam and bebek or (sapi and kambing)").toString());
    }
}
