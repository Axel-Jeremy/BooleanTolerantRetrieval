import java.util.ArrayList;
import java.util.List;
 
public class stemclaude {
    private char hurufSebelum;
 
    public stemclaude() {
    }
 
    // =====================================================================
    // HELPER METHODS
    // =====================================================================
 
    public static boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) != -1;
    }
 
    public static boolean isDoubleLetter(String last2Char) {
        if (!isVowel(last2Char.charAt(0))) {
            return last2Char.charAt(0) == last2Char.charAt(1);
        }
        return false;
    }
 
    public List<String> wordToCharArray(String kata) {
        List<String> charList = new ArrayList<>();
        for (int i = 0; i < kata.length(); i++) {
            charList.add(kata.charAt(i) + "");
        }
        return charList;
    }
 
    /**
     * Mengubah kata menjadi notasi VC yang dikompresi.
     * Contoh: "trouble" → [c,c,v,v,c,c,v] → "cvcv" → compress → "cvc"
     * 
     * Fix: kondisi loop sebelumnya dobel redundan, sekarang cukup
     * bandingkan curr dengan karakter terakhir di hasil.
     */
    public String ubahKeVC(String kata) {
        List<String> notation = new ArrayList<>();
        for (char c : kata.toCharArray()) {
            if (isVowel(c)) {
                notation.add("v");
                hurufSebelum = c;
            } else if (!isVowel(hurufSebelum) && c == 'y') {
                notation.add("v");
                hurufSebelum = c;
            } else {
                notation.add("c");
                hurufSebelum = c;
            }
        }
 
        // Compress: hanya tambahkan karakter jika berbeda dari karakter terakhir di hasil
        String hasil = "";
        if (!notation.isEmpty()) {
            hasil += notation.get(0);
        }
        for (int i = 1; i < notation.size(); i++) {
            char curr = notation.get(i).charAt(0);
            if (curr != hasil.charAt(hasil.length() - 1)) {
                hasil += curr;
            }
        }
        return hasil;
    }
 
    /**
     * Menghitung measure (m): berapa kali pola VC muncul berurutan.
     * Contoh: "trouble" → "cvc" → m=1, "troubles" → "cvcvc" → m=2
     */
    public int calculateMeasure(String kata) {
        String hasilNotation = ubahKeVC(kata);
        int m = 0;
        for (int i = 1; i < hasilNotation.length(); i++) {
            if (hasilNotation.charAt(i - 1) == 'v' && hasilNotation.charAt(i) == 'c') {
                m++;
            }
        }
        return m;
    }
 
    // =====================================================================
    // PORTER STEMMER - ENTRY POINT
    // =====================================================================
 
    public String porterStemmer(String kata) {
        kata = kata.toLowerCase();
        List<String> charList = wordToCharArray(kata);
 
        charList = step1A(kata, charList);
        kata = String.join("", charList);
 
        charList = step1Ba(kata, charList);
        kata = String.join("", charList);
 
        charList = step1C(kata, charList);
        kata = String.join("", charList);
 
        charList = step2(kata, charList);
        kata = String.join("", charList);
 
        charList = step3(kata, charList);
        kata = String.join("", charList);
 
        charList = step4(kata, charList);
        kata = String.join("", charList);
 
        charList = step5a(kata, charList);
        kata = String.join("", charList);
 
        charList = step5b(kata, charList);
        kata = String.join("", charList);
 
        return kata;
    }
 
    // =====================================================================
    // STEP 1A
    // Rule: sses→ss | ies→i | ss→ss | s→(hapus)
    // =====================================================================
 
    public List<String> step1A(String kata, List<String> charList) {
        if (kata.endsWith("sses")) {
            charList.removeLast();
            charList.removeLast();
        } else if (kata.endsWith("ies")) {
            charList.removeLast();
            charList.removeLast();
        } else if (kata.endsWith("ss")) {
            // ss → ss, tidak ada yang dihapus
        } else if (kata.endsWith("s")) {
            charList.removeLast();
        }
        return charList;
    }
 
    // =====================================================================
    // STEP 1B
    // Rule: (m>0) eed→ee | (*v*) ed→ | (*v*) ing→
    // =====================================================================
 
    public List<String> step1Ba(String kata, List<String> charList) {
        if (kata.endsWith("eed")) {
            String stem = kata.substring(0, kata.length() - 3);
            if (calculateMeasure(stem) > 0) {
                charList.removeLast(); // eed → ee
            }
        } else if (kata.endsWith("ed")) {
            String stem = kata.substring(0, kata.length() - 2);
            if (ubahKeVC(stem).contains("v")) {
                charList.removeLast();
                charList.removeLast();
                charList = step1Bb(stem, charList);
            }
        } else if (kata.endsWith("ing")) {
            String stem = kata.substring(0, kata.length() - 3);
            if (ubahKeVC(stem).contains("v")) {
                charList.removeLast();
                charList.removeLast();
                charList.removeLast();
                charList = step1Bb(stem, charList);
            }
        }
        return charList;
    }
 
    /**
     * Step 1B lanjutan — dijalankan setelah suffix "ed" atau "ing" dihapus.
     * Rule: at→ate | bl→ble | iz→ize | (*d not *l/*s/*z)→single | (m=1 *o)→e
     * 
     * @param stem kata setelah suffix dihapus (bukan kata asli)
     */
    public List<String> step1Bb(String stem, List<String> charList) {
        String last2Char = stem.substring(stem.length() - 2);
 
        if (stem.endsWith("at") || stem.endsWith("bl") || stem.endsWith("iz")) {
            charList.add("e");
        } else if (isDoubleLetter(last2Char)
                && !(last2Char.charAt(1) == 'l'
                        || last2Char.charAt(1) == 's'
                        || last2Char.charAt(1) == 'z')) {
            charList.removeLast();
        } else if (calculateMeasure(stem) == 1 && ubahKeVC(stem).endsWith("cvc")) {
            charList.add("e");
        }
        return charList;
    }
 
    // =====================================================================
    // STEP 1C
    // Rule: (*v*) y→i
    // =====================================================================
 
    public List<String> step1C(String kata, List<String> charList) {
        if (kata.endsWith("y")) {
            String stem = kata.substring(0, kata.length() - 1);
            if (ubahKeVC(stem).contains("v")) {
                charList.removeLast();
                charList.add("i");
            }
        }
        return charList;
    }
 
    // =====================================================================
    // STEP 2
    // Menghapus sufiks derivasional lebih panjang (7 ke bawah)
    // =====================================================================
 
    public List<String> step2(String kata, List<String> charList) {
        // 7-letter suffixes
        if (kata.endsWith("ational") && calculateMeasure(kata.substring(0, kata.length() - 7)) > 0) {
            // ational → ate: hapus 5 (ional), sisa at, tambah e
            for (int i = 0; i < 5; i++) charList.removeLast();
            charList.add("e");
        } else if (kata.endsWith("ization") && calculateMeasure(kata.substring(0, kata.length() - 7)) > 0) {
            // ization → ize: hapus 5 (ation), sisa iz, tambah e
            for (int i = 0; i < 5; i++) charList.removeLast();
            charList.add("e");
        } else if (kata.endsWith("iveness") && calculateMeasure(kata.substring(0, kata.length() - 7)) > 0) {
            // iveness → ive: hapus ness (4)
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("fulness") && calculateMeasure(kata.substring(0, kata.length() - 7)) > 0) {
            // fulness → ful: hapus ness (4)
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("ousness") && calculateMeasure(kata.substring(0, kata.length() - 7)) > 0) {
            // ousness → ous: hapus ness (4)
            for (int i = 0; i < 4; i++) charList.removeLast();
 
        // 6-letter suffixes
        } else if (kata.endsWith("tional") && calculateMeasure(kata.substring(0, kata.length() - 6)) > 0) {
            // tional → tion: hapus al (2)
            for (int i = 0; i < 2; i++) charList.removeLast();
        } else if (kata.endsWith("biliti") && calculateMeasure(kata.substring(0, kata.length() - 6)) > 0) {
            // biliti → ble: hapus 5 (iliti), sisa b, tambah le
            for (int i = 0; i < 5; i++) charList.removeLast();
            charList.add("l");
            charList.add("e");
 
        // 5-letter suffixes
        } else if (kata.endsWith("entli") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // entli → ent: hapus li (2)
            for (int i = 0; i < 2; i++) charList.removeLast();
        } else if (kata.endsWith("ousli") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // ousli → ous: hapus li (2)
            for (int i = 0; i < 2; i++) charList.removeLast();
        } else if (kata.endsWith("ation") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // ation → ate: hapus 3 (ion), tambah e
            for (int i = 0; i < 3; i++) charList.removeLast();
            charList.add("e");
        } else if (kata.endsWith("iviti") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // iviti → ive: hapus 3 (iti), tambah e
            for (int i = 0; i < 3; i++) charList.removeLast();
            charList.add("e");
        } else if (kata.endsWith("alism") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // alism → al: hapus 3 (ism)
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("aliti") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // aliti → al: hapus 3 (iti)
            for (int i = 0; i < 3; i++) charList.removeLast();
 
        // 4-letter suffixes
        } else if (kata.endsWith("enci") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // enci → ence: ganti i → e
            charList.removeLast();
            charList.add("e");
        } else if (kata.endsWith("anci") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // anci → ance: ganti i → e
            charList.removeLast();
            charList.add("e");
        } else if (kata.endsWith("abli") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // abli → able: ganti i → e
            charList.removeLast();
            charList.add("e");
        } else if (kata.endsWith("izer") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // izer → ize: hapus r (1)
            charList.removeLast();
        } else if (kata.endsWith("alli") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // alli → al: hapus li (2)... tapi di slide hapus 1 huruf terakhir saja
            charList.removeLast();
        } else if (kata.endsWith("ator") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // ator → ate: hapus 2 (or), tambah e
            charList.removeLast();
            charList.removeLast();
            charList.add("e");
 
        // 3-letter suffixes
        } else if (kata.endsWith("eli") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 0) {
            // eli → e: hapus li (2)
            for (int i = 0; i < 2; i++) charList.removeLast();
        }
 
        return charList;
    }
 
    // =====================================================================
    // STEP 3
    // Menghapus sufiks derivasional lebih pendek
    // =====================================================================
 
    public List<String> step3(String kata, List<String> charList) {
        if (kata.endsWith("icate") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // icate → ic: hapus ate (3)
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("alize") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // alize → al: hapus ize (3)
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("iciti") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // iciti → ic: hapus iti (3)
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ative") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 0) {
            // ative → (hapus semua 5)
            for (int i = 0; i < 5; i++) charList.removeLast();
        } else if (kata.endsWith("ical") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // ical → ic: hapus al (2)
            for (int i = 0; i < 2; i++) charList.removeLast();
        } else if (kata.endsWith("ness") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 0) {
            // ness → (hapus semua 4)
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("ful") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 0) {
            // ful → (hapus semua 3)
            for (int i = 0; i < 3; i++) charList.removeLast();
        }
        return charList;
    }
 
    // =====================================================================
    // STEP 4
    // Menghapus sufiks akhir (semua butuh m > 1)
    // =====================================================================
 
    public List<String> step4(String kata, List<String> charList) {
        if (kata.endsWith("ement") && calculateMeasure(kata.substring(0, kata.length() - 5)) > 1) {
            for (int i = 0; i < 5; i++) charList.removeLast();
        } else if (kata.endsWith("ance") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 1) {
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("ence") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 1) {
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("able") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 1) {
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("ible") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 1) {
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("ment") && calculateMeasure(kata.substring(0, kata.length() - 4)) > 1) {
            for (int i = 0; i < 4; i++) charList.removeLast();
        } else if (kata.endsWith("ent") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ant") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ism") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ate") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("iti") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ous") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ive") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ize") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            for (int i = 0; i < 3; i++) charList.removeLast();
        } else if (kata.endsWith("ion") && calculateMeasure(kata.substring(0, kata.length() - 3)) > 1) {
            // (m>1 and (*S or *T)) ion → (hapus)
            // Fix: cek karakter SEBELUM "ion", bukan charAt(0)
            char charBeforeIon = kata.charAt(kata.length() - 4);
            if (charBeforeIon == 's' || charBeforeIon == 't') {
                for (int i = 0; i < 3; i++) charList.removeLast();
            }
        } else if (kata.endsWith("al") && calculateMeasure(kata.substring(0, kata.length() - 2)) > 1) {
            for (int i = 0; i < 2; i++) charList.removeLast();
        } else if (kata.endsWith("er") && calculateMeasure(kata.substring(0, kata.length() - 2)) > 1) {
            for (int i = 0; i < 2; i++) charList.removeLast();
        } else if (kata.endsWith("ic") && calculateMeasure(kata.substring(0, kata.length() - 2)) > 1) {
            for (int i = 0; i < 2; i++) charList.removeLast();
        } else if (kata.endsWith("ou") && calculateMeasure(kata.substring(0, kata.length() - 2)) > 1) {
            for (int i = 0; i < 2; i++) charList.removeLast();
        }
        return charList;
    }
 
    // =====================================================================
    // STEP 5A
    // Rule: (m>1) e→ | (m=1 and not *o) e→
    // Fix: lastnChar sebelumnya pakai charAt(0) — harusnya charAt(length-1)
    // =====================================================================
 
    public List<String> step5a(String kata, List<String> charList) {
        if (kata.endsWith("e")) {
            String stem = kata.substring(0, kata.length() - 1);
            int measure = calculateMeasure(stem);
            if (measure > 1) {
                charList.removeLast();
            } else if (measure == 1 && !ubahKeVC(stem).endsWith("cvc")) {
                charList.removeLast();
            }
        }
        return charList;
    }
 
    // =====================================================================
    // STEP 5B
    // Rule: (m>1 and *d and *L) → single letter
    // Fix: lastnChar sebelumnya pakai charAt(0) — harusnya charAt(length-1)
    // =====================================================================
 
    public List<String> step5b(String kata, List<String> charList) {
        if (kata.length() >= 2) {
            String lastDoubleChar = kata.substring(kata.length() - 2);
            String stem = kata.substring(0, kata.length() - 1);
            if (calculateMeasure(stem) > 1
                    && isDoubleLetter(lastDoubleChar)
                    && kata.endsWith("l")) {
                charList.removeLast();
            }
        }
        return charList;
    }
}