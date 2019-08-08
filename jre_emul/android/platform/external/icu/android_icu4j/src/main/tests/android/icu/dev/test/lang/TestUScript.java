/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
*******************************************************************************
* Copyright (C) 1996-2016, International Business Machines Corporation and
* others. All Rights Reserved.
*******************************************************************************
*/

package android.icu.dev.test.lang;

import java.util.BitSet;
import java.util.Locale;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.lang.UProperty;
import android.icu.lang.UScript;
import android.icu.lang.UScript.ScriptUsage;
import android.icu.text.UnicodeSet;
import android.icu.util.ULocale;

public class TestUScript extends TestFmwk {

    /**
    * Constructor
    */
    public TestUScript()
    {
    }

    private static String scriptsToString(int[] scripts) {
        if(scripts == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for(int script : scripts) {
            if(sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(UScript.getShortName(script));
        }
        return sb.toString();
    }

    private void assertEqualScripts(String msg, int[] expectedScripts, int[] actualScripts) {
        assertEquals(msg, scriptsToString(expectedScripts), scriptsToString(actualScripts));
    }

    @Test
    public void TestLocaleGetCode(){
        final ULocale[] testNames={
        /* test locale */
        new ULocale("en"), new ULocale("en_US"),
        new ULocale("sr"), new ULocale("ta") ,
        new ULocale("te_IN"),
        new ULocale("hi"),
        new ULocale("he"), new ULocale("ar"),
        new ULocale("abcde"),
        new ULocale("abcde_cdef"),
        new ULocale("iw")
        };
        final int[] expected ={
                /* locales should return */
                UScript.LATIN, UScript.LATIN,
                UScript.CYRILLIC, UScript.TAMIL,
                UScript.TELUGU,UScript.DEVANAGARI,
                UScript.HEBREW, UScript.ARABIC,
                UScript.INVALID_CODE,UScript.INVALID_CODE,
                UScript.HEBREW
        };
        int i =0;
        int numErrors =0;

        for( ; i<testNames.length; i++){
            int[] code = UScript.getCode(testNames[i]);

            if(code==null){
                if(expected[i]!=UScript.INVALID_CODE){
                    logln("Error getting script code Got: null" + " Expected: " +expected[i] +" for name "+testNames[i]);
                    numErrors++;
                }
                // getCode returns null if the code could not be found
                continue;
            }
            if((code[0] != expected[i])){
                logln("Error getting script code Got: " +code[0] + " Expected: " +expected[i] +" for name "+testNames[i]);
                numErrors++;
            }
        }
        reportDataErrors(numErrors);
        
        // 
        ULocale defaultLoc = ULocale.getDefault(); 
        ULocale esperanto = new ULocale("eo_DE");
        ULocale.setDefault(esperanto);
        int[] code = UScript.getCode(esperanto); 
        if(code != null){
            if( code[0] != UScript.LATIN){
                errln("Did not get the expected script code for Esperanto");
            }
        }else{
            warnln("Could not load the locale data.");
        }
        ULocale.setDefault(defaultLoc);

        // Should work regardless of whether we have locale data for the language.
        assertEqualScripts("tg script: Cyrl",  // Tajik
                new int[] { UScript.CYRILLIC },
                UScript.getCode(new ULocale("tg")));
        assertEqualScripts("xsr script: Deva",  // Sherpa
                new int[] { UScript.DEVANAGARI },
                UScript.getCode(new ULocale("xsr")));

        // Multi-script languages.
        assertEqualScripts("ja scripts: Kana Hira Hani",
                new int[] { UScript.KATAKANA, UScript.HIRAGANA, UScript.HAN },
                UScript.getCode(ULocale.JAPANESE));
        assertEqualScripts("ko scripts: Hang Hani",
                new int[] { UScript.HANGUL, UScript.HAN },
                UScript.getCode(ULocale.KOREAN));
        assertEqualScripts("zh script: Hani",
                new int[] { UScript.HAN },
                UScript.getCode(ULocale.CHINESE));
        assertEqualScripts("zh-Hant scripts: Hani Bopo",
                new int[] { UScript.HAN, UScript.BOPOMOFO },
                UScript.getCode(ULocale.TRADITIONAL_CHINESE));
        assertEqualScripts("zh-TW scripts: Hani Bopo",
                new int[] { UScript.HAN, UScript.BOPOMOFO },
                UScript.getCode(ULocale.TAIWAN));

        // Ambiguous API, but this probably wants to return Latin rather than Rongorongo (Roro).
        assertEqualScripts("ro-RO script: Latn",
                new int[] { UScript.LATIN },
                UScript.getCode("ro-RO"));  // String not ULocale
    }

    // TODO(junit): remove this and convert the tests that use this to be parameterized
    private void reportDataErrors(int numErrors) {
        if (numErrors >0) {
            // assume missing locale data, so not an error, just a warning
            errln("encountered " + numErrors + " errors.");
        }
    }

    @Test
    public void TestMultipleCode(){
        final String[] testNames = { "ja" ,"ko_KR","zh","zh_TW"};
        final int[][] expected = {
                                {UScript.KATAKANA,UScript.HIRAGANA,UScript.HAN},
                                {UScript.HANGUL, UScript.HAN},
                                {UScript.HAN},
                                {UScript.HAN,UScript.BOPOMOFO}
                              };

        int numErrors = 0;
        for(int i=0; i<testNames.length;i++){
            int[] code = UScript.getCode(testNames[i]);
            int[] expt = (int[]) expected[i];
            if(code!=null){
                for(int j =0; j< code.length;j++){
                    if(code[j]!=expt[j]){
                        numErrors++;
                        logln("Error getting script code Got: " +code[j] + " Expected: " +expt[j] +" for name "+testNames[i]);
                    }
                }
            }else{
                numErrors++;
                logln("Error getting script code for name "+testNames[i]);
            }
        }
        reportDataErrors(numErrors);
        
        //cover UScript.getCode(Locale)
        Locale[] testLocales = new Locale[] {
            Locale.JAPANESE,
            Locale.KOREA,
            Locale.CHINESE,
            Locale.TAIWAN };
        logln("Testing UScript.getCode(Locale) ...");
        numErrors = 0;
        for(int i=0; i<testNames.length;i++){
            logln("  Testing locale: " + testLocales[i].getDisplayName());
            int[] code = UScript.getCode(testLocales[i]);
            int[] expt = (int[]) expected[i];
            if(code!=null){
                for(int j =0; j< code.length;j++){
                    if(code[j]!=expt[j]){
                        numErrors++;
                        logln("  Error getting script code Got: " +code[j] + " Expected: " +expt[j] +" for name "+testNames[i]);
                    }
                }
            }else{
                numErrors++;
                logln("  Error getting script code for name "+testNames[i]);
            }
        }
        reportDataErrors(numErrors);                 
    }

    @Test
    public void TestGetCode(){

        final String[] testNames={
            /* test locale */
            "en", "en_US", "sr", "ta", "gu", "te_IN", 
            "hi", "he", "ar",
            /* test abbr */
            "Hani", "Hang","Hebr","Hira",
            "Knda","Kana","Khmr","Lao",
            "Latn",/*"Latf","Latg",*/
            "Mlym", "Mong",

            /* test names */
            "CYRILLIC","DESERET","DEVANAGARI","ETHIOPIC","GEORGIAN",
            "GOTHIC",  "GREEK",  "GUJARATI", "COMMON", "INHERITED",
            /* test lower case names */
            "malayalam", "mongolian", "myanmar", "ogham", "old-italic",
            "oriya",     "runic",     "sinhala", "syriac","tamil",
            "telugu",    "thaana",    "thai",    "tibetan",
            /* test the bounds*/
            "Cans", "arabic","Yi","Zyyy"
        };
        final int[] expected ={
            /* locales should return */
            UScript.LATIN, UScript.LATIN,
            UScript.CYRILLIC, UScript.TAMIL, UScript.GUJARATI,
            UScript.TELUGU,UScript.DEVANAGARI,
            UScript.HEBREW, UScript.ARABIC,
            /* abbr should return */
            UScript.HAN, UScript.HANGUL, UScript.HEBREW, UScript.HIRAGANA,
            UScript.KANNADA, UScript.KATAKANA, UScript.KHMER, UScript.LAO,
            UScript.LATIN,/* UScript.LATIN, UScript.LATIN,*/
            UScript.MALAYALAM, UScript.MONGOLIAN,
            /* names should return */
            UScript.CYRILLIC, UScript.DESERET, UScript.DEVANAGARI, UScript.ETHIOPIC, UScript.GEORGIAN,
            UScript.GOTHIC, UScript.GREEK, UScript.GUJARATI, UScript.COMMON, UScript.INHERITED,
            /* lower case names should return */
            UScript.MALAYALAM, UScript.MONGOLIAN, UScript.MYANMAR, UScript.OGHAM, UScript.OLD_ITALIC,
            UScript.ORIYA, UScript.RUNIC, UScript.SINHALA, UScript.SYRIAC, UScript.TAMIL,
            UScript.TELUGU, UScript.THAANA, UScript.THAI, UScript.TIBETAN,
            /* bounds */
            UScript.CANADIAN_ABORIGINAL, UScript.ARABIC, UScript.YI, UScript.COMMON
        };
        int i =0;
        int numErrors =0;

        for( ; i<testNames.length; i++){
            int[] code = UScript.getCode(testNames[i]);
            if(code == null){
                if(expected[i]==UScript.INVALID_CODE){
                    // getCode returns null if the code could not be found
                    continue;
                }
                // currently commented out until jitterbug#2678 is fixed
                logln("Error getting script code Got: null" + " Expected: " +expected[i] +" for name "+testNames[i]);
                numErrors++;
                continue;
            }
            if((code[0] != expected[i])){
                logln("Error getting script code Got: " +code[0] + " Expected: " +expected[i] +" for name "+testNames[i]);
                numErrors++;
            }
        }
        reportDataErrors(numErrors);
    }

    @Test
    public void TestGetName(){

        final int[] testCodes={
            /* names should return */
            UScript.CYRILLIC, UScript.DESERET, UScript.DEVANAGARI, UScript.ETHIOPIC, UScript.GEORGIAN,
            UScript.GOTHIC, UScript.GREEK, UScript.GUJARATI,
        };

        final String[] expectedNames={

            /* test names */
            "Cyrillic","Deseret","Devanagari","Ethiopic","Georgian",
            "Gothic",  "Greek",  "Gujarati",
        };
        int i =0;
        int numErrors=0;
        while(i< testCodes.length){
            String scriptName  = UScript.getName(testCodes[i]);
            if(!expectedNames[i].equals(scriptName)){
                logln("Error getting abbreviations Got: " +scriptName +" Expected: "+expectedNames[i]);
                numErrors++;
            }
            i++;
        }
        if(numErrors >0 ){
            warnln("encountered " + numErrors + " errors in UScript.getName()");
        }

    }
    @Test
    public void TestGetShortName(){
        final int[] testCodes={
            /* abbr should return */
            UScript.HAN, UScript.HANGUL, UScript.HEBREW, UScript.HIRAGANA,
            UScript.KANNADA, UScript.KATAKANA, UScript.KHMER, UScript.LAO,
            UScript.LATIN,
            UScript.MALAYALAM, UScript.MONGOLIAN,
        };

        final String[] expectedAbbr={
              /* test abbr */
            "Hani", "Hang","Hebr","Hira",
            "Knda","Kana","Khmr","Laoo",
            "Latn",
            "Mlym", "Mong",
        };
        int i=0;
        int numErrors=0;
        while(i<testCodes.length){
            String  shortName = UScript.getShortName(testCodes[i]);
            if(!expectedAbbr[i].equals(shortName)){
                logln("Error getting abbreviations Got: " +shortName+ " Expected: " +expectedAbbr[i]);
                numErrors++;
            }
            i++;
        }
        if(numErrors >0 ){
            warnln("encountered " + numErrors + " errors in UScript.getShortName()");
        }
    }
    @Test
    public void TestGetScript(){
        int codepoints[][] = new int[][] {
                {0x0000FF9D, UScript.KATAKANA },
                {0x0000FFBE, UScript.HANGUL },
                {0x0000FFC7, UScript.HANGUL },
                {0x0000FFCF, UScript.HANGUL },
                {0x0000FFD7, UScript.HANGUL}, 
                {0x0000FFDC, UScript.HANGUL},
                {0x00010300, UScript.OLD_ITALIC},
                {0x00010330, UScript.GOTHIC},
                {0x0001034A, UScript.GOTHIC},
                {0x00010400, UScript.DESERET},
                {0x00010428, UScript.DESERET},
                {0x0001D167, UScript.INHERITED},
                {0x0001D17B, UScript.INHERITED},
                {0x0001D185, UScript.INHERITED},
                {0x0001D1AA, UScript.INHERITED},
                {0x00020000, UScript.HAN},
                {0x00000D02, UScript.MALAYALAM},
                {0x00000D00, UScript.UNKNOWN},
                {0x00000000, UScript.COMMON},
                {0x0001D169, UScript.INHERITED },
                {0x0001D182, UScript.INHERITED },
                {0x0001D18B, UScript.INHERITED },
                {0x0001D1AD, UScript.INHERITED },
        };

        int i =0;
        int code = UScript.INVALID_CODE;
        boolean passed = true;

        while(i< codepoints.length){
            code = UScript.getScript(codepoints[i][0]);

            if(code != codepoints[i][1]){
                logln("UScript.getScript for codepoint 0x"+ hex(codepoints[i][0])+" failed");
                passed = false;
            }

            i++;
        }
        if(!passed){
           errln("UScript.getScript failed.");
        }
    }

    @Test
    public void TestGetScriptOfCharsWithScriptExtensions() {
        /* test characters which have Script_Extensions */
        if(!(
            UScript.COMMON==UScript.getScript(0x0640) &&
            UScript.INHERITED==UScript.getScript(0x0650) &&
            UScript.ARABIC==UScript.getScript(0xfdf2))
        ) {
            errln("UScript.getScript(character with Script_Extensions) failed");
        }
    }

    @Test
    public void TestHasScript() {
        if(!(
            !UScript.hasScript(0x063f, UScript.COMMON) &&
            UScript.hasScript(0x063f, UScript.ARABIC) &&  /* main Script value */
            !UScript.hasScript(0x063f, UScript.SYRIAC) &&
            !UScript.hasScript(0x063f, UScript.THAANA))
        ) {
            errln("UScript.hasScript(U+063F, ...) is wrong");
        }
        if(!(
            !UScript.hasScript(0x0640, UScript.COMMON) &&  /* main Script value */
            UScript.hasScript(0x0640, UScript.ARABIC) &&
            UScript.hasScript(0x0640, UScript.SYRIAC) &&
            !UScript.hasScript(0x0640, UScript.THAANA))
        ) {
            errln("UScript.hasScript(U+0640, ...) is wrong");
        }
        if(!(
            !UScript.hasScript(0x0650, UScript.INHERITED) &&  /* main Script value */
            UScript.hasScript(0x0650, UScript.ARABIC) &&
            UScript.hasScript(0x0650, UScript.SYRIAC) &&
            !UScript.hasScript(0x0650, UScript.THAANA))
        ) {
            errln("UScript.hasScript(U+0650, ...) is wrong");
        }
        if(!(
            !UScript.hasScript(0x0660, UScript.COMMON) &&  /* main Script value */
            UScript.hasScript(0x0660, UScript.ARABIC) &&
            !UScript.hasScript(0x0660, UScript.SYRIAC) &&
            UScript.hasScript(0x0660, UScript.THAANA))
        ) {
            errln("UScript.hasScript(U+0660, ...) is wrong");
        }
        if(!(
            !UScript.hasScript(0xfdf2, UScript.COMMON) &&
            UScript.hasScript(0xfdf2, UScript.ARABIC) &&  /* main Script value */
            !UScript.hasScript(0xfdf2, UScript.SYRIAC) &&
            UScript.hasScript(0xfdf2, UScript.THAANA))
        ) {
            errln("UScript.hasScript(U+FDF2, ...) is wrong");
        }
        if(UScript.hasScript(0x0640, 0xaffe)) {
            // An unguarded implementation might go into an infinite loop.
            errln("UScript.hasScript(U+0640, bogus 0xaffe) is wrong");
        }
    }

    @Test
    public void TestGetScriptExtensions() {
        BitSet scripts=new BitSet(UScript.CODE_LIMIT);

        /* invalid code points */
        if(UScript.getScriptExtensions(-1, scripts)!=UScript.UNKNOWN || scripts.cardinality()!=1 ||
                !scripts.get(UScript.UNKNOWN)) {
            errln("UScript.getScriptExtensions(-1) is not {UNKNOWN}");
        }
        if(UScript.getScriptExtensions(0x110000, scripts)!=UScript.UNKNOWN || scripts.cardinality()!=1 ||
                !scripts.get(UScript.UNKNOWN)) {
            errln("UScript.getScriptExtensions(0x110000) is not {UNKNOWN}");
        }

        /* normal usage */
        if(UScript.getScriptExtensions(0x063f, scripts)!=UScript.ARABIC || scripts.cardinality()!=1 ||
                !scripts.get(UScript.ARABIC)) {
            errln("UScript.getScriptExtensions(U+063F) is not {ARABIC}");
        }
        if(UScript.getScriptExtensions(0x0640, scripts)>-3 || scripts.cardinality()<3 ||
           !scripts.get(UScript.ARABIC) || !scripts.get(UScript.SYRIAC) || !scripts.get(UScript.MANDAIC)
        ) {
            errln("UScript.getScriptExtensions(U+0640) failed");
        }
        if(UScript.getScriptExtensions(0xfdf2, scripts)!=-2 || scripts.cardinality()!=2 ||
                !scripts.get(UScript.ARABIC) || !scripts.get(UScript.THAANA)) {
            errln("UScript.getScriptExtensions(U+FDF2) failed");
        }
        if(UScript.getScriptExtensions(0xff65, scripts)!=-6 || scripts.cardinality()!=6 ||
                !scripts.get(UScript.BOPOMOFO) || !scripts.get(UScript.YI)) {
            errln("UScript.getScriptExtensions(U+FF65) failed");
        }
    }

    @Test
    public void TestScriptMetadataAPI() {
        /* API & code coverage. */
        String sample = UScript.getSampleString(UScript.LATIN);
        if(sample.length()!=1 || UScript.getScript(sample.charAt(0))!=UScript.LATIN) {
            errln("UScript.getSampleString(Latn) failed");
        }
        sample = UScript.getSampleString(UScript.INVALID_CODE);
        if(sample.length()!=0) {
            errln("UScript.getSampleString(invalid) failed");
        }

        if(UScript.getUsage(UScript.LATIN)!=ScriptUsage.RECOMMENDED ||
                UScript.getUsage(UScript.YI)!=ScriptUsage.ASPIRATIONAL ||
                UScript.getUsage(UScript.CHEROKEE)!=ScriptUsage.LIMITED_USE ||
                UScript.getUsage(UScript.COPTIC)!=ScriptUsage.EXCLUDED ||
                UScript.getUsage(UScript.CIRTH)!=ScriptUsage.NOT_ENCODED ||
                UScript.getUsage(UScript.INVALID_CODE)!=ScriptUsage.NOT_ENCODED ||
                UScript.getUsage(UScript.CODE_LIMIT)!=ScriptUsage.NOT_ENCODED) {
            errln("UScript.getUsage() failed");
        }

        if(UScript.isRightToLeft(UScript.LATIN) ||
                UScript.isRightToLeft(UScript.CIRTH) ||
                !UScript.isRightToLeft(UScript.ARABIC) ||
                !UScript.isRightToLeft(UScript.HEBREW)) {
            errln("UScript.isRightToLeft() failed");
        }

        if(UScript.breaksBetweenLetters(UScript.LATIN) ||
                UScript.breaksBetweenLetters(UScript.CIRTH) ||
                !UScript.breaksBetweenLetters(UScript.HAN) ||
                !UScript.breaksBetweenLetters(UScript.THAI)) {
            errln("UScript.breaksBetweenLetters() failed");
        }

        if(UScript.isCased(UScript.CIRTH) ||
                UScript.isCased(UScript.HAN) ||
                !UScript.isCased(UScript.LATIN) ||
                !UScript.isCased(UScript.GREEK)) {
            errln("UScript.isCased() failed");
        }
    }

    /**
     * Maps a special script code to the most common script of its encoded characters.
     */
    private static final int getCharScript(int script) {
        switch(script) {
        case UScript.HAN_WITH_BOPOMOFO:
        case UScript.SIMPLIFIED_HAN:
        case UScript.TRADITIONAL_HAN:
            return UScript.HAN;
        case UScript.JAPANESE:
            return UScript.HIRAGANA;
        case UScript.JAMO:
        case UScript.KOREAN:
            return UScript.HANGUL;
        case UScript.SYMBOLS_EMOJI:
            return UScript.SYMBOLS;
        default:
            return script;
        }
    }

    @Test
    public void TestScriptMetadata() {
        UnicodeSet rtl = new UnicodeSet("[[:bc=R:][:bc=AL:]-[:Cn:]-[:sc=Common:]]");
        // So far, sample characters are uppercase.
        // Georgian is special.
        UnicodeSet cased = new UnicodeSet("[[:Lu:]-[:sc=Common:]-[:sc=Geor:]]");
        for(int sc = 0; sc < UScript.CODE_LIMIT; ++sc) {
            String sn = UScript.getShortName(sc);
            ScriptUsage usage = UScript.getUsage(sc);
            String sample = UScript.getSampleString(sc);
            UnicodeSet scriptSet = new UnicodeSet();
            scriptSet.applyIntPropertyValue(UProperty.SCRIPT, sc);
            if(usage == ScriptUsage.NOT_ENCODED) {
                assertTrue(sn + " not encoded, no sample", sample.length() == 0);  // Java 6: sample.isEmpty()
                assertFalse(sn + " not encoded, not RTL", UScript.isRightToLeft(sc));
                assertFalse(sn + " not encoded, not LB letters", UScript.breaksBetweenLetters(sc));
                assertFalse(sn + " not encoded, not cased", UScript.isCased(sc));
                assertTrue(sn + " not encoded, no characters", scriptSet.isEmpty());
            } else {
                assertFalse(sn + " encoded, has a sample character", sample.length() == 0);  // Java 6: sample.isEmpty()
                int firstChar = sample.codePointAt(0);
                int charScript = getCharScript(sc);
                assertEquals(sn + " script(sample(script))",
                             charScript, UScript.getScript(firstChar));
                assertEquals(sn + " RTL vs. set", rtl.contains(firstChar), UScript.isRightToLeft(sc));
                assertEquals(sn + " cased vs. set", cased.contains(firstChar), UScript.isCased(sc));
                assertEquals(sn + " encoded, has characters", sc == charScript, !scriptSet.isEmpty());
                if(UScript.isRightToLeft(sc)) {
                    rtl.removeAll(scriptSet);
                }
                if(UScript.isCased(sc)) {
                    cased.removeAll(scriptSet);
                }
            }
        }
        assertEquals("no remaining RTL characters", "[]", rtl.toPattern(true));
        assertEquals("no remaining cased characters", "[]", cased.toPattern(true));

        assertTrue("Hani breaks between letters", UScript.breaksBetweenLetters(UScript.HAN));
        assertTrue("Thai breaks between letters", UScript.breaksBetweenLetters(UScript.THAI));
        assertFalse("Latn does not break between letters", UScript.breaksBetweenLetters(UScript.LATIN));
    }

    @Test
    public void TestScriptNames(){
        for(int i=0; i<UScript.CODE_LIMIT;i++){
            String name = UScript.getName(i);
            if(name.equals("") ){
                errln("FAILED: getName for code : "+i);
            }
            String shortName= UScript.getShortName(i);
            if(shortName.equals("")){
                errln("FAILED: getName for code : "+i);
            }
        }
    }
    @Test
    public void TestAllCodepoints(){
        int code;
        //String oldId="";
        //String oldAbbrId="";
        for( int i =0; i <= 0x10ffff; i++){
          code =UScript.INVALID_CODE;
          code = UScript.getScript(i);
          if(code==UScript.INVALID_CODE){
                errln("UScript.getScript for codepoint 0x"+ hex(i)+" failed");
          }
          String id =UScript.getName(code);
          if(id.indexOf("INVALID")>=0){
                 errln("UScript.getScript for codepoint 0x"+ hex(i)+" failed");
          }
          String abbr = UScript.getShortName(code);
          if(abbr.indexOf("INV")>=0){
                 errln("UScript.getScript for codepoint 0x"+ hex(i)+" failed");
          }
        }
    }
    @Test
    public void TestNewCode(){
        /*
         * These script codes were originally added to ICU pre-3.6, so that ICU would
         * have all ISO 15924 script codes. ICU was then based on Unicode 4.1.
         * These script codes were added with only short names because we don't
         * want to invent long names ourselves.
         * Unicode 5 and later encode some of these scripts and give them long names.
         * Whenever this happens, the long script names here need to be updated.
         */
        String[] expectedLong = new String[]{
            "Balinese", "Batak", "Blis", "Brahmi", "Cham", "Cirt", "Cyrs",
            "Egyd", "Egyh", "Egyptian_Hieroglyphs",
            "Geok", "Hans", "Hant", "Pahawh_Hmong", "Old_Hungarian", "Inds",
            "Javanese", "Kayah_Li", "Latf", "Latg",
            "Lepcha", "Linear_A", "Mandaic", "Maya", "Meroitic_Hieroglyphs",
            "Nko", "Old_Turkic", "Old_Permic", "Phags_Pa", "Phoenician", 
            "Miao", "Roro", "Sara", "Syre", "Syrj", "Syrn", "Teng", "Vai", "Visp", "Cuneiform", 
            "Zxxx", "Unknown",
            "Carian", "Jpan", "Tai_Tham", "Lycian", "Lydian", "Ol_Chiki", "Rejang", "Saurashtra", "SignWriting", "Sundanese",
            "Moon", "Meetei_Mayek",
            /* new in ICU 4.0 */
            "Imperial_Aramaic", "Avestan", "Chakma", "Kore",
            "Kaithi", "Manichaean", "Inscriptional_Pahlavi", "Psalter_Pahlavi", "Phlv",
            "Inscriptional_Parthian", "Samaritan", "Tai_Viet",
            "Zmth", "Zsym",
            /* new in ICU 4.4 */
            "Bamum", "Lisu", "Nkgb", "Old_South_Arabian",
            /* new in ICU 4.6 */
            "Bassa_Vah", "Duployan", "Elbasan", "Grantha", "Kpel",
            "Loma", "Mende_Kikakui", "Meroitic_Cursive",
            "Old_North_Arabian", "Nabataean", "Palmyrene", "Khudawadi", "Warang_Citi",
            /* new in ICU 4.8 */
            "Afak", "Jurc", "Mro", "Nshu", "Sharada", "Sora_Sompeng", "Takri", "Tangut", "Wole",
            /* new in ICU 49 */
            "Anatolian_Hieroglyphs", "Khojki", "Tirhuta",
            /* new in ICU 52 */
            "Caucasian_Albanian", "Mahajani",
            /* new in ICU 54 */
            "Ahom", "Hatran", "Modi", "Multani", "Pau_Cin_Hau", "Siddham",
            // new in ICU 58
            "Adlam", "Bhaiksuki", "Marchen", "Newa", "Osage", "Hanb", "Jamo", "Zsye"
        };
        String[] expectedShort = new String[]{
            "Bali", "Batk", "Blis", "Brah", "Cham", "Cirt", "Cyrs", "Egyd", "Egyh", "Egyp",
            "Geok", "Hans", "Hant", "Hmng", "Hung", "Inds", "Java", "Kali", "Latf", "Latg",
            "Lepc", "Lina", "Mand", "Maya", "Mero", "Nkoo", "Orkh", "Perm", "Phag", "Phnx",
            "Plrd", "Roro", "Sara", "Syre", "Syrj", "Syrn", "Teng", "Vaii", "Visp", "Xsux",
            "Zxxx", "Zzzz",
            "Cari", "Jpan", "Lana", "Lyci", "Lydi", "Olck", "Rjng", "Saur", "Sgnw", "Sund",
            "Moon", "Mtei",
            /* new in ICU 4.0 */
            "Armi", "Avst", "Cakm", "Kore",
            "Kthi", "Mani", "Phli", "Phlp", "Phlv", "Prti", "Samr", "Tavt",
            "Zmth", "Zsym",
            /* new in ICU 4.4 */
            "Bamu", "Lisu", "Nkgb", "Sarb",
            /* new in ICU 4.6 */
            "Bass", "Dupl", "Elba", "Gran", "Kpel", "Loma", "Mend", "Merc",
            "Narb", "Nbat", "Palm", "Sind", "Wara",
            /* new in ICU 4.8 */
            "Afak", "Jurc", "Mroo", "Nshu", "Shrd", "Sora", "Takr", "Tang", "Wole",
            /* new in ICU 49 */
            "Hluw", "Khoj", "Tirh",
            /* new in ICU 52 */
            "Aghb", "Mahj",
            /* new in ICU 54 */
            "Ahom", "Hatr", "Modi", "Mult", "Pauc", "Sidd",
            // new in ICU 58
            "Adlm", "Bhks", "Marc", "Newa", "Osge", "Hanb", "Jamo", "Zsye"
        };
        if(expectedLong.length!=(UScript.CODE_LIMIT-UScript.BALINESE)) {
            errln("need to add new script codes in lang.TestUScript.java!");
            return;
        }
        int j = 0;
        int i = 0;
        for(i=UScript.BALINESE; i<UScript.CODE_LIMIT; i++, j++){
            String name = UScript.getName(i);
            if(name==null || !name.equals(expectedLong[j])){
                errln("UScript.getName failed for code"+ i + name +"!=" +expectedLong[j]);
            }
            name = UScript.getShortName(i);
            if(name==null || !name.equals(expectedShort[j])){
                errln("UScript.getShortName failed for code"+ i + name +"!=" +expectedShort[j]);
            }
        }
        for(i=0; i<expectedLong.length; i++){
            int[] ret = UScript.getCode(expectedShort[i]);
            if(ret.length>1){
                errln("UScript.getCode did not return expected number of codes for script"+ expectedShort[i]+". EXPECTED: 1 GOT: "+ ret.length);
            }
            if(ret[0]!= (UScript.BALINESE+i)){
                errln("UScript.getCode did not return expected code for script"+ expectedShort[i]+". EXPECTED: "+ (UScript.BALINESE+i)+" GOT: %i\n"+ ret[0] );
            }
        }
    }
}
