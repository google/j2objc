/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.translit;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.impl.Utility;
import android.icu.text.ReplaceableString;
import android.icu.text.Transliterator;



/**
 * @test
 * @summary General test of CompoundTransliterator
 */
public class CompoundTransliteratorTest extends TestFmwk {
    @Test
    public void TestConstruction(){
        logln("Testing the construction of the compound Transliterator");
        String names[]={"Greek-Latin", "Latin-Devanagari", "Devanagari-Latin", "Latin-Greek"};

        try {
            Transliterator.getInstance(names[0]);
            Transliterator.getInstance(names[1]);
            Transliterator.getInstance(names[2]);
            Transliterator.getInstance(names[3]);
        }catch(IllegalArgumentException ex) {
            errln("FAIL: Transliterator construction failed" + ex.getMessage());
            throw ex;
        }
  
        final String IDs[]={
            names[0], 
            names[0]+";"+names[3], 
            names[3]+";"+names[1]+";"+names[2], 
            names[0]+";"+names[1]+";"+names[2]+";"+names[3] 
        };

   
        for(int i=0; i<4; i++){
            try{
                Transliterator.getInstance(IDs[i]);
            }catch(IllegalArgumentException ex1) {
                errln("FAIL: construction using CompoundTransliterator(String ID) failed for " + IDs[i]);
                throw ex1;
            }

            try{
                Transliterator.getInstance(IDs[i], Transliterator.FORWARD);
            }catch(IllegalArgumentException ex2) {
                errln("FAIL: construction using CompoundTransliterator(String ID, int direction=FORWARD) failed for " + IDs[i]);
                throw ex2;
            }

            try{
                Transliterator.getInstance(IDs[i], Transliterator.REVERSE);
            }catch(IllegalArgumentException ex3) {
                errln("FAIL: construction using CompoundTransliterator(String ID, int direction=REVERSE) failed for " + IDs[i]);
                throw ex3;
            }

//            try{
//                CompoundTransliterator cpdtrans=new CompoundTransliterator(IDs[i], Transliterator.FORWARD, null);
//                cpdtrans = null;
//            }catch(IllegalArgumentException ex4) {
//                errln("FAIL: construction using CompoundTransliterator(String ID, int direction=FORWARD," +
//                        "UnicodeFilter adoptedFilter=0) failed for " + IDs[i]);
//                throw ex4;
//            }
//  
//       
//            try{
//                CompoundTransliterator cpdtrans2=new CompoundTransliterator(transarray[i], null);
//                cpdtrans2 = null;
//            }catch(IllegalArgumentException ex5) {
//                errln("FAIL: Construction using CompoundTransliterator(Transliterator transliterators[]," +
//                       "UnicodeFilter adoptedFilter = 0)  failed");
//                throw ex5;
//            }

            
        }
   
    }
 
    @Test
    public void TestGetTransliterator(){
        logln("Testing the getTransliterator() API of CompoundTransliterator");
        String ID="Latin-Greek;Greek-Latin;Latin-Devanagari;Devanagari-Latin;Latin-Cyrillic;Cyrillic-Latin;Any-Hex;Hex-Any";
        Transliterator ct1=null;
        try{
            //ct1=new CompoundTransliterator(ID);
            ct1 = Transliterator.getInstance(ID);
        }catch(IllegalArgumentException iae) {
            errln("CompoundTransliterator construction failed for ID=" + ID);
            throw iae;
        }
        //int count=ct1.getCount();
        Transliterator elems[] = ct1.getElements();
        int count = elems.length;
        String array[]=split(ID, ';');
        if (count != array.length) {
            errln("Error: getCount() failed. Expected:" + array.length + " got:" + count);
        }
        for(int i=0; i < count; i++){
            //String child= ct1.getTransliterator(i).getID();
            String child = elems[i].getID();
            if(!child.equals(array[i])){
                errln("Error getTransliterator() failed: Expected->" + array[i] + " Got->" + child);
            }else {
                logln("OK: getTransliterator() passed: Expected->" + array[i] + " Got->" + child);
            }
        }

        
    }
 
       
    @Test
    public void TestTransliterate(){
        logln("Testing the handleTransliterate() API of CompoundTransliterator");
        Transliterator ct1=null;
        try{
            ct1=Transliterator.getInstance("Any-Hex;Hex-Any");
        }catch(IllegalArgumentException iae){
            errln("FAIL: construction using CompoundTransliterator(String ID) failed for " + "Any-Hex;Hex-Any");
            throw iae;
        }
    
        String s="abcabc";
        expect(ct1, s, s);
        Transliterator.Position index = new Transliterator.Position();
        ReplaceableString rsource2=new ReplaceableString(s);
        String expectedResult=s;
        ct1.transliterate(rsource2, index);
        ct1.finishTransliteration(rsource2, index);
        String result=rsource2.toString();
        expectAux(ct1.getID() + ":ReplaceableString, index(0,0,0,0)", s + "->" + rsource2, result.equals(expectedResult), expectedResult);
     
        Transliterator.Position index2 = new Transliterator.Position(1,3,2,3);
        ReplaceableString rsource3=new ReplaceableString(s);
        ct1.transliterate(rsource3, index2); 
        ct1.finishTransliteration(rsource3, index2);
        result=rsource3.toString();
        expectAux(ct1.getID() + ":String, index2(1,2,2,3)", s + "->" + rsource3, result.equals(expectedResult), expectedResult);

       
        String Data[]={
             //ID, input string, transliterated string
             "Any-Hex;Hex-Any;Any-Hex",     "hello",  "\\u0068\\u0065\\u006C\\u006C\\u006F", 
             "Any-Hex;Hex-Any",                 "hello! How are you?",  "hello! How are you?",
             "Devanagari-Latin;Latin-Devanagari",       "\u092D\u0948'\u0930'\u0935",  "\u092D\u0948\u0930\u0935", // quotes lost
             "Latin-Cyrillic;Cyrillic-Latin",           "a'b'k'd'e'f'g'h'i'j'Shch'shch'zh'h", "a'b'k'd'e'f'g'h'i'j'Shch'shch'zh'h",
             "Latin-Greek;Greek-Latin",                 "ABGabgAKLMN", "ABGabgAKLMN",
             //"Latin-Arabic;Arabic-Latin",               "Ad'r'a'b'i'k'dh'dd'gh", "Adrabikdhddgh",
             "Hiragana-Katakana",                       "\u3041\u308f\u3099\u306e\u304b\u3092\u3099", 
                                                                 "\u30A1\u30f7\u30ce\u30ab\u30fa",  
             "Hiragana-Katakana;Katakana-Hiragana",     "\u3041\u308f\u3099\u306e\u304b\u3051", 
                                                                 "\u3041\u308f\u3099\u306e\u304b\u3051",
             "Katakana-Hiragana;Hiragana-Katakana",     "\u30A1\u30f7\u30ce\u30f5\u30f6", 
                                                                 "\u30A1\u30f7\u30ce\u30ab\u30b1",  
             "Latin-Katakana;Katakana-Latin",                   "vavivuvevohuzizuzoninunasesuzezu", 
                                                                 "vavivuvevohuzizuzoninunasesuzezu",  
        };
        Transliterator ct2=null;
        for(int i=0; i<Data.length; i+=3){
            try{
                ct2=Transliterator.getInstance(Data[i+0]);
            }catch(IllegalArgumentException iae2){
                errln("FAIL: CompoundTransliterator construction failed for " + Data[i+0]);
                throw iae2;
            }
        expect(ct2, Data[i+1], Data[i+2]);
        }
   
    }
 

    //======================================================================
    // Support methods
    //======================================================================

     /**
     * Splits a string,
    */
    private static String[] split(String s, char divider) {
      
    // see how many there are
        int count = 1;
    for (int i = 0; i < s.length(); ++i) {
       if (s.charAt(i) == divider) ++count;
    }
        
    // make an array with them
    String[] result = new String[count];
    int last = 0;
    int current = 0;
    int i;
    for (i = 0; i < s.length(); ++i) {
        if (s.charAt(i) == divider) {
            result[current++] = s.substring(last,i);
            last = i+1;
        }
    }
    result[current++] = s.substring(last,i);
    return result;
    }

    private void expect(Transliterator t, String source, String expectedResult) {
        String result = t.transliterate(source);
        expectAux(t.getID() + ":String", source, result, expectedResult);

        ReplaceableString rsource = new ReplaceableString(source);
        t.transliterate(rsource);
        result = rsource.toString();
        expectAux(t.getID() + ":Replaceable", source, result, expectedResult);

        // Test keyboard (incremental) transliteration -- this result
        // must be the same after we finalize (see below).
        rsource.replace(0, rsource.length(), "");
        Transliterator.Position index = new Transliterator.Position();
        StringBuffer log = new StringBuffer();

        for (int i=0; i<source.length(); ++i) {
            if (i != 0) {
                log.append(" + ");
            }
            log.append(source.charAt(i)).append(" -> ");
            t.transliterate(rsource, index,
                            String.valueOf(source.charAt(i)));
            // Append the string buffer with a vertical bar '|' where
            // the committed index is.
            String s = rsource.toString();
            log.append(s.substring(0, index.start)).
                append('|').
                append(s.substring(index.start));
        }
        
        // As a final step in keyboard transliteration, we must call
        // transliterate to finish off any pending partial matches that
        // were waiting for more input.
        t.finishTransliteration(rsource, index);
        result = rsource.toString();
        log.append(" => ").append(rsource.toString());
        expectAux(t.getID() + ":Keyboard", log.toString(),
                 result.equals(expectedResult),
                 expectedResult);

    }
    private void expectAux(String tag, String source,
                  String result, String expectedResult) {
        expectAux(tag, source + " -> " + result,
                 result.equals(expectedResult),
                 expectedResult);
    }

    private void expectAux(String tag, String summary, boolean pass, String expectedResult) {
        if (pass) {
            logln("(" + tag + ") " + Utility.escape(summary));
        } else {
            errln("FAIL: (" + tag+ ") "
                + Utility.escape(summary)
                + ", expected " + Utility.escape(expectedResult));
        }
    }  
}

