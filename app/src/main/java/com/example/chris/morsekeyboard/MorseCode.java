package com.example.chris.morsekeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    static public String strTable(int pad){
        StringBuilder table = new StringBuilder("");
        List<String> keys = new ArrayList<String>(lookup.keySet());
        Collections.sort(keys);
        int ncol=3;
        int i=0;
        for(String mc : keys) {
            if(mc.equals("")) continue; // don't mention space
            String letter=lookup.get(mc);
            // escape the escape chars
            if(letter.equals("\n")) letter="\\n";
            // pad string
            if(mc.length() < pad) mc=String.format("%-"+pad+"s",mc);
            // make text
            table.append(letter); table.append(" ");
            table.append(mc);     table.append("\t");
            if(++i % ncol == 0 && i > 0) table.append("\n");
        }
        return(table.toString());
    }
}
