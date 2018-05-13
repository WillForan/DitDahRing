package com.example.chris.morsekeyboard;

import java.util.HashMap;
import java.util.Map;

public class MorseCode {
    static public Map<String,String> lookup = new HashMap<String,String>() {{
        put(""," ");   // nothing translates to space
        put("._._","\n"); // special character for enter
        put("._","a");
        put("_...","b");
        put("_._.","c");
        put("_..","d");
        put(".","e");
        put(".._.","f");
        put("__.","g");
        put("....","h");
        put("..","i");
        put(".___","j");
        put("_._","k");
        put("._..","l");
        put("__","m");
        put("_.","n");
        put("___","o");
        put(".__.","p");
        put("__._","q");
        put("._.","r");
        put("...","s");
        put("_","t");
        put(".._","u");
        put("..._","v");
        put(".__","w");
        put("_.._","x");
        put("_.__","y");
        put("__..","z");
        put(".____",".");
        put("..___","_");
        put("...__","3");
        put("...._","4");
        put(".....","5");
        put("_....","6");
        put("__...","7");
        put("___..","8");
        put("____.","9");
        put("_____","0");
        put("._._._",".");
        put("__..__",",");
        put("..__..","?");
        put(".____.","'");
        put("_._.__","!");
        put("_.._.","/");
        put("_.__.","(");
        put("_.__._",")");
        put("._...","&");
        put("___...",":");
        put("_._._.",";");
        put("_..._","=");
        put("._._.","+");
        put("_...._","-");
        put("..__._","_");
        put("._.._.","\"");
        put("..._.._","$");
        put(".__._.","@");
    }};

    static public String ditdah_letter(String ditdah, boolean isShift){
        String letter = lookup.get(ditdah);
        // enter or lookup character
        // shift makes space into tab
        if(letter != null) {
            if (isShift && letter.equals(" ")) letter = "\t";
            else if (isShift) letter = letter.toUpperCase();
        }
        return(letter);
    }
}
