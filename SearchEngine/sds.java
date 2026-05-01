public class sds {
    public static void main(String[] args) {
        Stemmer s = new Stemmer();
        s.porterStemmer("universal");
        s.porterStemmer("ponies");
        s.porterStemmer("plastered");
        s.porterStemmer("filing");
        s.porterStemmer("relational");
        s.porterStemmer("electrical");
        s.porterStemmer("allowance");
        s.porterStemmer("controll");
    }
}
