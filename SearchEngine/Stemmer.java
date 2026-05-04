import java.util.ArrayList;
import java.util.List;

public class Stemmer {
    private String kata;

    public Stemmer() {
    }

    public void setKata(String kata) {
        this.kata = kata;
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

    public boolean oRule(String kata) {
        char hurufSebelum = '\0';
        List<String> notations = new ArrayList<>();
        for (char c : kata.toCharArray()) {
            if (isVowel(c)) {
                notations.add("v");
                hurufSebelum = c;
            } else if (!isVowel(hurufSebelum) && c == 'y') {
                notations.add("v");
                hurufSebelum = c;
            } else {
                notations.add("c");
                hurufSebelum = c;
            }
        }

        String hasil = ""; // cvcvcvcvc
        for (int i = 0; i < notations.size(); i++) {
            hasil += notations.get(i);
        }

        if (hasil.endsWith("cvc") && (kata.charAt(kata.length() - 1) != 'w'
                || kata.charAt(kata.length() - 1) != 'x'
                || kata.charAt(kata.length() - 1) != 'y')) {
            return true;
        }

        return false;
    }

    public String ubahKeVC(String kata) {
        char hurufSebelum = '\0';
        List<String> notations = new ArrayList<>();
        for (char c : kata.toCharArray()) {
            if (isVowel(c)) {
                notations.add("v");
                hurufSebelum = c;
            } else if (!isVowel(hurufSebelum) && c == 'y') {
                notations.add("v");
                hurufSebelum = c;
            } else {
                notations.add("c");
                hurufSebelum = c;
            }
        }

        String hasil = ""; // cvcvcvcvc
        if (!notations.isEmpty()) {
            hasil += notations.get(0);
        }

        for (int i = 1; i < notations.size(); i++) {
            char notation = notations.get(i).charAt(0);
            if (hasil.charAt(hasil.length() - 1) != notation) {
                hasil += notation;
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

    public String porterStemmer(String kata) {
        setKata(kata);
        List<String> charList = wordToCharArray(kata);
        List<String> stemmed = step5b(step5a(step4(step3(step2(step1C(step1Ba(step1A(charList))))))));
        String res = "";
        for (int i = 0; i < stemmed.size(); i++) {
            res += stemmed.get(i);
        }

        return res;
    }

    public List<String> step1A(List<String> charList) {
        if (kata.endsWith("sses") || kata.endsWith("ies")) {
            charList.removeLast();
            charList.removeLast();
        } else if (kata.endsWith("ss")) {
        } else if (kata.endsWith("s")) {
            charList.removeLast();
        }

        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step1Ba(List<String> charList) {
        if (kata.endsWith("eed")) {
            String stem = kata.substring(0, kata.length() - 3);
            if (calculateMeasure(stem) > 0) {
                charList.removeLast();
            }
        } else if (kata.endsWith("ed")) {
            String stem = kata.substring(0, kata.length() - 2);
            if (ubahKeVC(stem).contains("v")) {
                charList.removeLast();
                charList.removeLast();
                setKata(String.join("", charList));
                charList = step1Bb(charList);
            }
        } else if (kata.endsWith("ing")) {
            String stem = kata.substring(0, kata.length() - 3);
            if (ubahKeVC(stem).contains("v")) {
                charList.removeLast();
                charList.removeLast();
                charList.removeLast();
                setKata(String.join("", charList));
                charList = step1Bb(charList);

            }
        }
        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step1Bb(List<String> charList) {
        String last2Char = kata.substring(kata.length() - 2, kata.length());

        if (last2Char.equals("at")
                || last2Char.equals("bl")
                || last2Char.equals("iz")) {
            charList.add("e");
        } else if (isDoubleLetter(last2Char)
                && !(last2Char.charAt(1) == 'l'
                        || last2Char.charAt(1) == 's'
                        || last2Char.charAt(1) == 'z')) {
            charList.removeLast();
        } else if (calculateMeasure(kata) == 1
                && oRule(kata)) {
            charList.add("e");

        }
        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step1C(List<String> charList) {
        if(kata.length() < 1) return charList;
        if (ubahKeVC(kata.substring(0, kata.length() - 1)).contains("v")
                && kata.charAt(kata.length() - 1) == 'y') {
            charList.removeLast();
            charList.addLast("i");
        }
        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step2(List<String> charList) {
        String sisaCharn;
        int measure;

        if (kata.endsWith("ational")
                || kata.endsWith("ization")) {
            sisaCharn = kata.substring(0, kata.length() - 7);
            // jika mengandung kata ational dan measure lebih dari 0
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {

                for (int i = 0; i < 5; i++) {
                    charList.removeLast();
                }
                charList.addLast("e");
            }

        } else if (kata.endsWith("iveness")
                || kata.endsWith("fulness")
                || kata.endsWith("ousness")) {
            sisaCharn = kata.substring(0, kata.length() - 7);
            measure = calculateMeasure(sisaCharn);

            if (measure > 0) {
                for (int i = 0; i < 4; i++) {
                    charList.removeLast();
                }
            }
        }

        else if (kata.endsWith("tional")) {
            sisaCharn = kata.substring(0, kata.length() - 6);
            measure = calculateMeasure(sisaCharn);

            if (measure > 0) {
                charList.removeLast();
                charList.removeLast();

            }
        } else if (kata.endsWith("biliti")) {
            sisaCharn = kata.substring(0, kata.length() - 6);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                for (int i = 0; i < 5; i++) {
                    charList.removeLast();
                }
                charList.add("l");
                charList.add("e");
            }
        }

        else if (kata.endsWith("entli") || kata.endsWith("ousli")) {
            
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                charList.removeLast();
                charList.removeLast();
            }
        }

        else if (kata.endsWith("ation") || kata.endsWith("iviti")) {
            
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
                charList.add("e");
            }
        }

        else if (kata.endsWith("alism") || kata.endsWith("aliti")) {
            
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
            }
        }

        else if (kata.endsWith("enci")
                || kata.endsWith("anci")
                || kata.endsWith("abli")) {
            
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                charList.removeLast();
                charList.add("e");
            }
        }

        else if (kata.endsWith("izer") || kata.endsWith("alli")) {
            
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                charList.removeLast();
            }
        }

        else if (kata.endsWith("ator")) {
            
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                charList.removeLast();
                charList.removeLast();
                charList.add("e");
            }
        }

        else if (kata.endsWith("eli")) {
            sisaCharn = kata.substring(0, kata.length() - 3);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                charList.removeLast();
                charList.removeLast();
            }
        }
        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step3(List<String> charList) {
        String sisaCharn;
        int measure;

        if (kata.endsWith("ousness")) {
            if (kata.length() < 7) return charList;
            sisaCharn = kata.substring(0, kata.length() - 7);

            measure = calculateMeasure(sisaCharn);

            if (measure > 0) {
                for (int i = 0; i < 4; i++)
                    charList.removeLast();
            }
        }

        else if (kata.endsWith("icate")
                || kata.endsWith("alize")
                || kata.endsWith("iciti")) {
            
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);

            if (measure > 0) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
            }
        } else if (kata.endsWith("ative")) {
            
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);

            if (measure > 0) {
                for (int i = 0; i < 5; i++) {
                    charList.removeLast();
                }
            }
        } else if (kata.endsWith("ical")) {
            
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);
            // ical hapus al
            if (measure > 0) {
                for (int i = 0; i < 2; i++)
                    charList.removeLast();
            }
        }

        else if (kata.endsWith("ness")) {
            
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);
            if (measure > 0) {
                for (int i = 0; i < 4; i++) {
                    charList.removeLast();
                }
            }
        }

        else if (kata.endsWith("ful")) {
            if (kata.length() < 3) return charList;
            sisaCharn = kata.substring(0, kata.length() - 3);
            measure = calculateMeasure(sisaCharn);
            // ful hapus ful nya semua
            if (measure > 0) {
                for (int i = 0; i < 3; i++) {
                    charList.removeLast();
                }
            }
        }
        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step4(List<String> charList) {
        String sisaCharn;
        int measure;

        if (kata.endsWith("ement")) {
            
            sisaCharn = kata.substring(0, kata.length() - 5);
            measure = calculateMeasure(sisaCharn);
            // ement hapus ement nya semua
            if (measure > 1) {
                for (int i = 0; i < 5; i++)
                    charList.removeLast();
            }
        } else if (kata.endsWith("ance")
                || kata.endsWith("ence")
                || kata.endsWith("able")
                || kata.endsWith("ible")
                || kata.endsWith("ment")) {
            
            sisaCharn = kata.substring(0, kata.length() - 4);
            measure = calculateMeasure(sisaCharn);

            // ance,ence,able,ible,ment hapus semua
            if (measure > 1) {
                for (int i = 0; i < 4; i++)
                    charList.removeLast();
            }
        }

        else if (kata.endsWith("ent")
                || kata.endsWith("ant")
                || kata.endsWith("ism")
                || kata.endsWith("ate")
                || kata.endsWith("iti")
                || kata.endsWith("ous")
                || kata.endsWith("ive")
                || kata.endsWith("ize")
                || kata.endsWith("ion")) {
            sisaCharn = kata.substring(0, kata.length() - 3);
            measure = calculateMeasure(sisaCharn);

            // ent,ism,ate,iti,ous,ive,ize,ant hapus semua
            if (measure > 1 && !kata.endsWith("ion")) {
                for (int i = 0; i < 3; i++)
                    charList.removeLast();

            }
            // (m>1 && ( *S atau *T ) berakhiran ION hapus semua
            else if (kata.endsWith("ion")
                    && (sisaCharn.endsWith("t")
                            || sisaCharn.endsWith("s"))
                    && measure > 1) {
                for (int i = 0; i < 3; i++)
                    charList.removeLast();
            }

        } else if (kata.endsWith("al")
                || kata.endsWith("er")
                || kata.endsWith("ic")
                || kata.endsWith("ou")) {
            sisaCharn = kata.substring(0, kata.length() - 2);
            measure = calculateMeasure(sisaCharn);

            // al,er,ic,ou, hapus semua
            if (measure > 1) {
                for (int i = 0; i < 2; i++)
                    charList.removeLast();
            }
        }

        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step5a(List<String> charList) {
        String sisaCharn;
        int measure;

        if (kata.length() < 1) return charList;
        // jika berakhiran e dan m>1 hapus
        sisaCharn = kata.substring(0, kata.length() - 1);
        measure = calculateMeasure(sisaCharn);
        if (kata.endsWith("e") && measure > 1) {
            charList.removeLast();
        }
        // jika measure=1, tidak berpola cvc, dan berakhiran e
        else if (measure == 1
                && !oRule(sisaCharn)
                && kata.endsWith("e")) {
            charList.removeLast();
        }
        setKata(String.join("", charList));
        return charList;
    }

    public List<String> step5b(List<String> charList) {
        String sisaCharn;
        String lastDoubleChar;
        int measure;

        if (kata.length() < 2) return charList;

        sisaCharn = kata.substring(0, kata.length() - 1);
        lastDoubleChar = kata.substring(kata.length() - 2, kata.length());
        measure = calculateMeasure(sisaCharn);
        // jika measure > 1 berakhiran l dan double (ll)
        // akan dihapus 1 supaya single letter
        if (measure > 1
                && isDoubleLetter(lastDoubleChar)
                && kata.endsWith("l")) {
            charList.removeLast();
        }
        setKata(String.join("", charList));
        return charList;
    }
}