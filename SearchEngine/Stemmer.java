import java.util.ArrayList;
import java.util.List;

public class Stemmer {
    private char hurufSebelum;

    public Stemmer (){}
    
    public static boolean isVowel(char c) {
        return "AEIOUaeiou".indexOf(c) != -1;
    }

    public String ubahKeVC(String kata){
        List <String> notation = new ArrayList<>();
        for(char c : kata.toCharArray()){
            if(isVowel(c)){
                notation.add("v");
                hurufSebelum = c;
            }
            else if(!isVowel(hurufSebelum)&&c=='y'){
                notation.add("v");
                hurufSebelum = c;
            }
            else{
                notation.add("c");
                hurufSebelum = c;
            }
        }
        
        String hasil = ""; //cvcvcvcvc
        for(int i =1;i<notation.size();i++){ 
            char notation1 = notation.get(i-1).charAt(0);
            char notation2 = notation.get(i).charAt(0);
            if((notation1 != notation2) && hasil.charAt(hasil.length()-1) != notation1){
                hasil += notation2;
            }
        }
        return hasil;
    }

    public int calculateMeasure(String kata){
        String hasilNotation = ubahKeVC(kata);
        int m = 0;
        
        for(int i = 1; i < hasilNotation.length(); i++){
            if(hasilNotation.charAt(i-1)=='v' && hasilNotation.charAt(i)=='c'){
                m++;
            }
        }
        return m;
    }
}