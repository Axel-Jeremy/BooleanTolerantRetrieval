import java.util.ArrayList;
import java.util.List;

public class Stemmer {
    private char hurufSebelum;

    public Stemmer() {
    }

    public static boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) != -1;
    }

    public static boolean isDoubleLetter(String last2Char) {
        if (!isVowel(last2Char.charAt(0))) {
            return last2Char.charAt(0) == last2Char.charAt(1);
        }
        return false;
    }

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

        String hasil = ""; // cvcvcvcvc
        for (int i = 1; i < notation.size(); i++) {
            char notation1 = notation.get(i - 1).charAt(0);
            char notation2 = notation.get(i).charAt(0);
            if ((notation1 != notation2) && hasil.charAt(hasil.length() - 1) != notation1) {
                hasil += notation2;
            }
        }
        return hasil;
    }

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

    public List<String> wordToCharArray(String kata) {
        List<String> charList = new ArrayList<>();
        for (int i = 0; i < kata.length(); i++) {
            charList.add(kata.charAt(i) + "");
        }
        return charList;

    }

    public void porterStemmer(String kata) {
        List<String> charList = wordToCharArray(kata);
        String notation = ubahKeVC(kata);
        charList = step1A(kata, charList);
        charList = step1Ba(kata, charList, notation);
    }

    public List<String> step1A(String kata, List<String> charList) {

        if (kata.length() >= 4) {
            String last4Char = kata.substring(kata.length() - 4, kata.length());
            if (last4Char.contains("sses")) {
                for (int i = 0; i < 2; i++) {
                    charList.removeLast();
                }
            }
        } else if (kata.length() >= 3) {
            String last3Char = kata.substring(kata.length() - 3, kata.length());
            if (last3Char.contains("ies")) {
                for (int i = 0; i < 2; i++) {
                    charList.removeLast();
                }
            }
        } else if (kata.length() >= 1) {
            char lastChar = kata.charAt(kata.length() - 1);
            if (lastChar == 's') {
                charList.removeLast();
            }
        }
        return charList;
    }

    public List<String> step1Ba(String kata, List<String> charList, String notation) {
        String last3Char = kata.substring(kata.length() - 3, kata.length());
        String last2Char = last3Char.substring(1);
        String sisaChar = kata.substring(0, kata.length() - 2);

        if (last3Char.equals("eed") && calculateMeasure(sisaChar) > 0) {
            charList.removeLast();
        } else if (last2Char.equals("ed") && ubahKeVC(kata.substring(0, kata.length() - 1)).contains("v")) {
            for (int i = 0; i < 2; i++) {
                charList.removeLast();
            }
            step1Bb(kata, charList);
        }

        else if (last3Char.equals("ing") && ubahKeVC(sisaChar).contains("v")) {
            for (int i = 0; i < 3; i++) {
                charList.removeLast();
            }
            step1Bb(kata, charList);
        }

        return charList;

    }

    public List<String> step1Bb(String kata, List<String> charList) {
        String last2Char = kata.substring(kata.length() - 2, kata.length());
        String last1Char = kata.charAt(kata.length() - 1) + "";
        String sisaChar2 = kata.substring(0, kata.length() - 1);
        String sisaChar1 = kata.substring(0, kata.length() - 1);

        if (last2Char.equals("at")
                || last2Char.equals("bl")
                || last2Char.equals("iz")) {
            charList.add("e");
        } else if (isDoubleLetter(last2Char)
                && !(last2Char.charAt(1) == 'l'
                        || last2Char.charAt(1) == 'S'
                        || last2Char.charAt(1) == 'z')) {
            charList.removeLast();
        } else if (calculateMeasure(sisaChar1) == 1
                && ubahKeVC(sisaChar1).contains("cvc")) {
            charList.add("e");
        }

        return charList;
    }

    public List<String> step1C(String kata, List<String> charList) {
        if (ubahKeVC(kata.substring(0, kata.length() - 1)).contains("v")
                && kata.charAt(kata.length() - 1) == 'y') {
            charList.removeLast();
            charList.addLast("i");
        }
        return charList;
    }

    public List<String> step2(String kata, List<String> charList) {
        String lastnChar;
        String sisaCharn;
        int measure;

        if (kata.length() >= 7) {
            lastnChar = kata.substring(kata.length() - 7, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 7);
            // jika mengandung kata ational dan measure lebih dari 0
            measure = calculateMeasure(sisaCharn);
            if (lastnChar.contains("ational") && measure > 0) {
                // hapus ional dan ganti menjadi e
                // ational
                // 0123456
                // sehingga i dari 2 sampai 6 akan dihapus
                // dan akan ditambah e diakhir
                // misalnya international
                // lastnchar = ational
                // sisanchar = intern
                // akan dihapus 5x berarti hapus l, a,n,o,i sisa at dan akan ditambahkan e
                // diakhir
                for (int i = 0; i < 5; i++) {
                    charList.removeLast();
                }
                charList.add("e");
            } else if (lastnChar.contains("ization") && measure > 0) {
                for (int i = 0; i < 5; i++) {
                    charList.removeLast();
                }
                charList.add("e");
            }
            // iveness,fulness,ousness hapus ness
            else if ((lastnChar.contains("iveness") || lastnChar.contains("fulness") || lastnChar.contains("ousness"))
                    && measure > 0) {
                for (int i = 0; i < 4; i++) {
                    charList.removeLast();
                }
            }
        } else if (kata.length() >= 6) {
            lastnChar = kata.substring(kata.length() - 6, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 6);
            measure = calculateMeasure(sisaCharn);
            if (lastnChar.contains("tional") && measure > 0) {
                // hapus ional dan ganti menjadi e
                // ational
                // 0123456
                // sehingga i dari 2 sampai 6 akan dihapus
                // dan akan ditambah e diakhir
                // misalnya international
                // lastnchar = tional
                // sisanchar = interna
                // akan dihapus 2x berarti hapus l, a, sisa tion
                for (int i = 0; i < 2; i++) {
                    charList.removeLast();
                }
            }
            // biliti hapus iliti dan ganti jadi le
            else if (lastnChar.contains("biliti") && measure > 0) {
                for (int i = 0; i < 5; i++) {
                    charList.removeLast();
                }
                charList.add("l");
                charList.add("e");
            }
        } else if (kata.length() >= 5) {
            lastnChar = kata.substring(kata.length() - 5, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);
            // entli dan ousli hapus li nya dan ganti jadi e
            if ((lastnChar.contains("entli") || lastnChar.contains("ousli")) && measure > 0) {
                for (int i = 0; i < 2; i++) {
                    charList.removeLast();
                }
            }
            // ation jadi ate hapus 3 huruf terakhir ,ganti jadi e
            // iviti jadi ive
            else if ((lastnChar.contains("ation") || lastnChar.contains("iviti")) && measure > 0) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
                charList.add("e");
            }
            // alism jadi al hapus 3 huruf terakhir
            // aliti jadi al
            else if ((lastnChar.contains("alism") || lastnChar.contains("aliti")) && measure > 0) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
            }

        } else if (kata.length() >= 4) {
            lastnChar = kata.substring(kata.length() - 4, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);
            // enci,anci,abli ganti huruf terakhir jadi e
            if ((lastnChar.contains("enci") || lastnChar.contains("anci") || lastnChar.contains("abli"))
                    && measure > 0) {
                charList.removeLast();
                charList.add("e");
            }
            // izer,alli hapus huruf terakhir
            else if ((lastnChar.contains("izer") || lastnChar.contains("alli")) && measure > 0) {
                charList.removeLast();
            }
            // ator jadi ate,hapus 2 huruf terakhir dan ganti jadi e
            else if (lastnChar.contains("ator") && measure > 0) {
                charList.removeLast();
                charList.removeLast();
                charList.add("e");
            }
        } else if (kata.length() >= 3) {
            lastnChar = kata.substring(kata.length() - 3, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 3);
            measure = calculateMeasure(sisaCharn);
            // Eli hapus li
            if (lastnChar.contains("eli") && measure > 0) {
                for (int i = 0; i < 2; i++) {
                    charList.removeLast();
                }
            }
        }
        return charList;
    }

    public List<String> step3(String kata, List<String> charList) {
        String lastnChar;
        String sisaCharn;
        int measure;

        if (kata.length() >= 7) {
            lastnChar = kata.substring(kata.length() - 7, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 7);

            measure = calculateMeasure(sisaCharn);

            if (lastnChar.contains("ousness") && measure > 0) {
                for (int i = 0; i < 4; i++) {
                    charList.removeLast();
                }
            }
        }

        else if (kata.length() >= 5) {
            lastnChar = kata.substring(kata.length() - 5, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 5);
            // xel smua methodnya yang if else belum ada mengarah ke step berikutnya FAK
            // nanti aja itu
            measure = calculateMeasure(sisaCharn);

            if (measure > 0) {
                if (lastnChar.contains("icate")
                        || lastnChar.contains("alize")
                        || lastnChar.contains("iciti")) {
                    for (int i = 0; i < 3; i++) {
                        charList.removeLast();
                    }
                } else if (lastnChar.contains("ative")) {
                    for (int i = 0; i < 5; i++) {
                        charList.removeLast();
                    }
                }
            }
        }

        else if (kata.length() >= 4) {
            lastnChar = kata.substring(kata.length() - 4, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);
            // ical hapus al
            if (lastnChar.contains("ical") && measure > 0) {
                for (int i = 0; i < 2; i++) {
                    charList.removeLast();
                }
            }
            // ness ,hapus semua ness nya
            else if (lastnChar.contains("ness") && measure > 0) {
                for (int i = 0; i < 4; i++) {
                    charList.removeLast();
                }
            }
        }

        else if (kata.length() >= 3) {
            lastnChar = kata.substring(kata.length() - 3, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 3);
            measure = calculateMeasure(sisaCharn);
            // ful hapus ful nya semua
            if (lastnChar.contains("ful") && measure > 0) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
            }
        }
        return charList;
    }

    public List<String> step4(String kata, List<String> charList) {
        String lastnChar;
        String sisaCharn;
        int measure;

        if (kata.length() >= 5) {
            lastnChar = kata.substring(kata.length() - 5, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);
            // ement hapus ement nya semua
            if (lastnChar.contains("ement") && measure > 1) {
                for (int i = 0; i < 5; i++) {
                    charList.removeLast();
                }
            }
        } else if (kata.length() >= 4) {
            lastnChar = kata.substring(kata.length() - 4, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);

            // ance,ence,able,ible,ment hapus semua
            if ((lastnChar.contains("ance")
                    || lastnChar.contains("ence")
                    || lastnChar.contains("able")
                    || lastnChar.contains("ible")
                    || lastnChar.contains("ment")) && measure > 1) {
                for (int i = 0; i < 4; i++) {
                    charList.removeLast();
                }
            }
        }

        else if (kata.length() >= 3) {
            lastnChar = kata.substring(kata.length() - 3, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 3);
            measure = calculateMeasure(sisaCharn);

            // ent,ism,ate,iti,ous,ive,ize,ant hapus semua
            if ((lastnChar.contains("ent")
                    || lastnChar.contains("ant")
                    || lastnChar.contains("ism")
                    || lastnChar.contains("ate")
                    || lastnChar.contains("iti") // xel udh yang step 4,ntar dicek kan? iya
                    || lastnChar.contains("ous")
                    || lastnChar.contains("ive")
                    || lastnChar.contains("ize"))
                    && measure > 1) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
            }
            // (m>1 && ( *S atau *T ) berakhiran ION hapus semua
            else if ((sisaCharn.charAt(0) == 's' || sisaCharn.charAt(0) == 't') && lastnChar.contains("ion")
                    && measure > 1) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
            }

        } else if (kata.length() >= 2) {
            lastnChar = kata.substring(kata.length() - 2, kata.length());
            sisaCharn = kata.substring(0, kata.length() - 2);
            measure = calculateMeasure(sisaCharn);

            // al,er,ic,ou, hapus semua
            if ((lastnChar.contains("al")
                    || lastnChar.contains("er")
                    || lastnChar.contains("ic")
                    || lastnChar.contains("ou"))
                    && measure > 1) {
                for (int i = 0; i < 2; i++) {
                    charList.removeLast();
                }
            }
        }
        return charList;
    }

    public List<String> step5a(String kata, List<String> charList) {
        String lastnChar;
        String sisaCharn;
        int measure;

        // jika berakhiran e dan m>1 hapus
        lastnChar = kata.charAt(0) + "";
        sisaCharn = kata.substring(0, kata.length() - 1);
        measure = calculateMeasure(sisaCharn);
        if (lastnChar.equals("e") && measure > 1) {
            charList.removeLast();
        }
        // jika measure=1, tidak berpola cvc, dan berakhiran e
        else if (measure == 1
                && !ubahKeVC(sisaCharn).contains("cvc")
                && lastnChar.equals("e")) {
            charList.remove("e");
        }
        return charList;
    }

    public List<String> step5b(String kata, List<String> charList) {
        String lastnChar;
        String sisaCharn;
        String lastDoubleChar;
        int measure;

        lastnChar = kata.charAt(0) + "";
        sisaCharn = kata.substring(0, kata.length() - 1);
        lastDoubleChar = kata.substring(kata.length() - 2, kata.length());
        measure = calculateMeasure(sisaCharn);
        // jika measure > 1 berakhiran l dan double (ll) akan dihapus 1 supaya single
        // letter
        if (measure > 1
            && isDoubleLetter(lastDoubleChar)
            && lastnChar.equals("l")) {
            charList.removeLast();
        }
        return charList;
    }
}