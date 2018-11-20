/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2000-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.translit;

import android.icu.text.UTF16;
import android.icu.text.UnicodeSet;

public final class TestUtility {

    public static String hex(char ch) {
        String foo = Integer.toString(ch,16).toUpperCase();
        return "0000".substring(0,4-foo.length()) + foo;
    }
    
    public static String hex(int ch) {
        String foo = Integer.toString(ch,16).toUpperCase();
        return "00000000".substring(0,4-foo.length()) + foo;
    }
    
    public static String hex(String s) {
      return hex(s,",");
    }
    
    public static String hex(String s, String sep) {
      if (s.length() == 0) return "";
      String result = hex(s.charAt(0));
      for (int i = 1; i < s.length(); ++i) {
        result += sep;
        result += hex(s.charAt(i));
      }
      return result;
    }
    
    public static String replace(String source, String toBeReplaced, String replacement) {
        StringBuffer results = new StringBuffer();
        int len = toBeReplaced.length();
        for (int i = 0; i < source.length(); ++i) {
            if (source.regionMatches(false, i, toBeReplaced, 0, len)) {
                results.append(replacement);
                i += len - 1; // minus one, since we will increment
            } else {
                results.append(source.charAt(i));
            }
        }
        return results.toString();
    }
    
    public static String replaceAll(String source, UnicodeSet set, String replacement) {
        StringBuffer results = new StringBuffer();
        int cp;
        for (int i = 0; i < source.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(source,i);
            if (set.contains(cp)) {
                results.append(replacement);
            } else {
                UTF16.append(results, cp);
            }
        }
        return results.toString();
    }
    
    // COMMENTED OUT ALL THE OLD SCRIPT STUFF
    /*
    public static byte getScript(char c) {
      return getScript(getBlock(c));
    }
    
    public static byte getScript(byte block) {
      return blockToScript[block];
    }
    
    public static byte getBlock(char c) {
      int index = c >> 7;
      byte block = charToBlock[index];
      while (block < 0) { // take care of exceptions, blocks split across 128 boundaries
          int[] tuple = split[-block-1];
          if (c < tuple[0]) block = (byte)tuple[1];
          else block = (byte)tuple[2];
      }
      return block;
    }
               
    // returns next letter of script, or 0xFFFF if done
    
    public static char getNextLetter(char c, byte script) {
        while (c < 0xFFFF) {
            ++c;
            if (getScript(c) == script && Character.isLetter(c)) {
                return c;
            }
        }
        return c;
    }
    
    // Supplements to Character methods; these methods go through
    // UCharacter if possible.  If not, they fall back to Character.

    public static boolean isUnassigned(char c) {
        try {
            return UCharacter.getType(c) == UCharacterCategory.UNASSIGNED;
        } catch (NullPointerException e) {
            System.out.print("");
        }
        return Character.getType(c) == Character.UNASSIGNED;
    }

    public static boolean isLetter(char c) {
        try {
            return UCharacter.isLetter(c);
        } catch (NullPointerException e) {
            System.out.print("");
        }
        return Character.isLetter(c);
    }

  public static void main(String[] args) {
    System.out.println("Blocks: ");
    byte lastblock = -128;
    for (char cc = 0; cc < 0xFFFF; ++cc) {
      byte block = TestUtility.getBlock(cc);
      if (block != lastblock) {
        System.out.println(TestUtility.hex(cc) + "\t" + block);
        lastblock = block;
      }
    }
    System.out.println();
    System.out.println("Scripts: ");
    byte lastScript = -128;
    for (char cc = 0; cc < 0xFFFF; ++cc) {
      byte script = TestUtility.getScript(cc);
      if (script != lastScript) {
        System.out.println(TestUtility.hex(cc) + "\t" + script);
        lastScript = script;
      }
    }
  }
      
    
    
    public static final byte // SCRIPT CODE
        COMMON_SCRIPT = 0,
        LATIN_SCRIPT = 1,
        GREEK_SCRIPT = 2,
        CYRILLIC_SCRIPT = 3,
        ARMENIAN_SCRIPT = 4,
        HEBREW_SCRIPT = 5,
        ARABIC_SCRIPT = 6,
        SYRIAC_SCRIPT = 7,
        THAANA_SCRIPT = 8, 
        DEVANAGARI_SCRIPT = 9,
        BENGALI_SCRIPT = 10,
        GURMUKHI_SCRIPT = 11,
        GUJARATI_SCRIPT = 12,
        ORIYA_SCRIPT = 13,
        TAMIL_SCRIPT = 14,
        TELUGU_SCRIPT = 15,
        KANNADA_SCRIPT = 16,
        MALAYALAM_SCRIPT = 17,
        SINHALA_SCRIPT = 18,
        THAI_SCRIPT = 19,
        LAO_SCRIPT = 20,
        TIBETAN_SCRIPT = 21,
        MYANMAR_SCRIPT = 22,
        GEORGIAN_SCRIPT = 23,
        JAMO_SCRIPT = 24,
        HANGUL_SCRIPT = 25,
        ETHIOPIC_SCRIPT = 26,
        CHEROKEE_SCRIPT = 27,
        ABORIGINAL_SCRIPT = 28,
        OGHAM_SCRIPT = 29,
        RUNIC_SCRIPT = 30,
        KHMER_SCRIPT = 31,
        MONGOLIAN_SCRIPT = 32,
        HIRAGANA_SCRIPT = 33,
        KATAKANA_SCRIPT = 34,
        BOPOMOFO_SCRIPT = 35,
        HAN_SCRIPT = 36,
        YI_SCRIPT = 37;
    
    public static final byte // block code
        RESERVED_BLOCK = 0,
        BASIC_LATIN = 1,
        LATIN_1_SUPPLEMENT = 2,
        LATIN_EXTENDED_A = 3,
        LATIN_EXTENDED_B = 4,
        IPA_EXTENSIONS = 5,
        SPACING_MODIFIER_LETTERS = 6,
        COMBINING_DIACRITICAL_MARKS = 7,
        GREEK = 8,
        CYRILLIC = 9,
        ARMENIAN = 10,
        HEBREW = 11,
        ARABIC = 12,
        SYRIAC = 13,
        THAANA = 14,
        DEVANAGARI = 15,
        BENGALI = 16,
        GURMUKHI = 17,
        GUJARATI = 18,
        ORIYA = 19,
        TAMIL = 20,
        TELUGU = 21,
        KANNADA = 22,
        MALAYALAM = 23,
        SINHALA = 24,
        THAI = 25,
        LAO = 26,
        TIBETAN = 27,
        MYANMAR = 28,
        GEORGIAN = 29,
        HANGUL_JAMO = 30,
        ETHIOPIC = 31,
        CHEROKEE = 32,
        UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS = 33,
        OGHAM = 34,
        RUNIC = 35,
        KHMER = 36,
        MONGOLIAN = 37,
        LATIN_EXTENDED_ADDITIONAL = 38,
        GREEK_EXTENDED = 39,
        GENERAL_PUNCTUATION = 40,
        SUPERSCRIPTS_AND_SUBSCRIPTS = 41,
        CURRENCY_SYMBOLS = 42,
        COMBINING_MARKS_FOR_SYMBOLS = 43,
        LETTERLIKE_SYMBOLS = 44,
        NUMBER_FORMS = 45,
        ARROWS = 46,
        MATHEMATICAL_OPERATORS = 47,
        MISCELLANEOUS_TECHNICAL = 48,
        CONTROL_PICTURES = 49,
        OPTICAL_CHARACTER_RECOGNITION = 50,
        ENCLOSED_ALPHANUMERICS = 51,
        BOX_DRAWING = 52,
        BLOCK_ELEMENTS = 53,
        GEOMETRIC_SHAPES = 54,
        MISCELLANEOUS_SYMBOLS = 55,
        DINGBATS = 56,
        BRAILLE_PATTERNS = 57,
        CJK_RADICALS_SUPPLEMENT = 58,
        KANGXI_RADICALS = 59,
        IDEOGRAPHIC_DESCRIPTION_CHARACTERS = 60,
        CJK_SYMBOLS_AND_PUNCTUATION = 61,
        HIRAGANA = 62,
        KATAKANA = 63,
        BOPOMOFO = 64,
        HANGUL_COMPATIBILITY_JAMO = 65,
        KANBUN = 66,
        BOPOMOFO_EXTENDED = 67,
        ENCLOSED_CJK_LETTERS_AND_MONTHS = 68,
        CJK_COMPATIBILITY = 69,
        CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A = 70,
        CJK_UNIFIED_IDEOGRAPHS = 71,
        YI_SYLLABLES = 72,
        YI_RADICALS = 73,
        HANGUL_SYLLABLES = 74,
        HIGH_SURROGATES = 75,
        HIGH_PRIVATE_USE_SURROGATES = 76,
        LOW_SURROGATES = 77,
        PRIVATE_USE = 78,
        CJK_COMPATIBILITY_IDEOGRAPHS = 79,
        ALPHABETIC_PRESENTATION_FORMS = 80,
        ARABIC_PRESENTATION_FORMS_A = 81,
        COMBINING_HALF_MARKS = 82,
        CJK_COMPATIBILITY_FORMS = 83,
        SMALL_FORM_VARIANTS = 84,
        ARABIC_PRESENTATION_FORMS_B = 85,
        SPECIALS = 86,
        HALFWIDTH_AND_FULLWIDTH_FORMS = 87;
        
    static final byte[] blockToScript = {
        COMMON_SCRIPT, // 0, <RESERVED_BLOCK>
        LATIN_SCRIPT, // 1, BASIC_LATIN
        LATIN_SCRIPT, // 2, LATIN_1_SUPPLEMENT
        LATIN_SCRIPT, // 3, LATIN_EXTENDED_A
        LATIN_SCRIPT, // 4, LATIN_EXTENDED_B
        LATIN_SCRIPT, // 5, IPA_EXTENSIONS
        COMMON_SCRIPT, // 6, SPACING_MODIFIER_LETTERS
        COMMON_SCRIPT, // 7, COMBINING_DIACRITICAL_MARKS
        GREEK_SCRIPT, // 8, GREEK
        CYRILLIC_SCRIPT, // 9, CYRILLIC
        ARMENIAN_SCRIPT, // 10, ARMENIAN
        HEBREW_SCRIPT, // 11, HEBREW
        ARABIC_SCRIPT, // 12, ARABIC
        SYRIAC_SCRIPT, // 13, SYRIAC
        THAANA_SCRIPT, // 14, THAANA
        DEVANAGARI_SCRIPT, // 15, DEVANAGARI
        BENGALI_SCRIPT, // 16, BENGALI
        GURMUKHI_SCRIPT, // 17, GURMUKHI
        GUJARATI_SCRIPT, // 18, GUJARATI
        ORIYA_SCRIPT, // 19, ORIYA
        TAMIL_SCRIPT, // 20, TAMIL
        TELUGU_SCRIPT, // 21, TELUGU
        KANNADA_SCRIPT, // 22, KANNADA
        MALAYALAM_SCRIPT, // 23, MALAYALAM
        SINHALA_SCRIPT, // 24, SINHALA
        THAI_SCRIPT, // 25, THAI
        LAO_SCRIPT, // 26, LAO
        TIBETAN_SCRIPT, // 27, TIBETAN
        MYANMAR_SCRIPT, // 28, MYANMAR
        GEORGIAN_SCRIPT, // 29, GEORGIAN
        JAMO_SCRIPT, // 30, HANGUL_JAMO
        ETHIOPIC_SCRIPT, // 31, ETHIOPIC
        CHEROKEE_SCRIPT, // 32, CHEROKEE
        ABORIGINAL_SCRIPT, // 33, UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS
        OGHAM_SCRIPT, // 34, OGHAM
        RUNIC_SCRIPT, // 35, RUNIC
        KHMER_SCRIPT, // 36, KHMER
        MONGOLIAN_SCRIPT, // 37, MONGOLIAN
        LATIN_SCRIPT, // 38, LATIN_EXTENDED_ADDITIONAL
        GREEK_SCRIPT, // 39, GREEK_EXTENDED
        COMMON_SCRIPT, // 40, GENERAL_PUNCTUATION
        COMMON_SCRIPT, // 41, SUPERSCRIPTS_AND_SUBSCRIPTS
        COMMON_SCRIPT, // 42, CURRENCY_SYMBOLS
        COMMON_SCRIPT, // 43, COMBINING_MARKS_FOR_SYMBOLS
        COMMON_SCRIPT, // 44, LETTERLIKE_SYMBOLS
        COMMON_SCRIPT, // 45, NUMBER_FORMS
        COMMON_SCRIPT, // 46, ARROWS
        COMMON_SCRIPT, // 47, MATHEMATICAL_OPERATORS
        COMMON_SCRIPT, // 48, MISCELLANEOUS_TECHNICAL
        COMMON_SCRIPT, // 49, CONTROL_PICTURES
        COMMON_SCRIPT, // 50, OPTICAL_CHARACTER_RECOGNITION
        COMMON_SCRIPT, // 51, ENCLOSED_ALPHANUMERICS
        COMMON_SCRIPT, // 52, BOX_DRAWING
        COMMON_SCRIPT, // 53, BLOCK_ELEMENTS
        COMMON_SCRIPT, // 54, GEOMETRIC_SHAPES
        COMMON_SCRIPT, // 55, MISCELLANEOUS_SYMBOLS
        COMMON_SCRIPT, // 56, DINGBATS
        COMMON_SCRIPT, // 57, BRAILLE_PATTERNS
        HAN_SCRIPT, // 58, CJK_RADICALS_SUPPLEMENT
        HAN_SCRIPT, // 59, KANGXI_RADICALS
        HAN_SCRIPT, // 60, IDEOGRAPHIC_DESCRIPTION_CHARACTERS
        COMMON_SCRIPT, // 61, CJK_SYMBOLS_AND_PUNCTUATION
        HIRAGANA_SCRIPT, // 62, HIRAGANA
        KATAKANA_SCRIPT, // 63, KATAKANA
        BOPOMOFO_SCRIPT, // 64, BOPOMOFO
        JAMO_SCRIPT, // 65, HANGUL_COMPATIBILITY_JAMO
        HAN_SCRIPT, // 66, KANBUN
        BOPOMOFO_SCRIPT, // 67, BOPOMOFO_EXTENDED
        COMMON_SCRIPT, // 68, ENCLOSED_CJK_LETTERS_AND_MONTHS
        COMMON_SCRIPT, // 69, CJK_COMPATIBILITY
        HAN_SCRIPT, // 70, CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
        HAN_SCRIPT, // 71, CJK_UNIFIED_IDEOGRAPHS
        YI_SCRIPT, // 72, YI_SYLLABLES
        YI_SCRIPT, // 73, YI_RADICALS
        HANGUL_SCRIPT, // 74, HANGUL_SYLLABLES
        COMMON_SCRIPT, // 75, HIGH_SURROGATES
        COMMON_SCRIPT, // 76, HIGH_PRIVATE_USE_SURROGATES
        COMMON_SCRIPT, // 77, LOW_SURROGATES
        COMMON_SCRIPT, // 78, PRIVATE_USE
        HAN_SCRIPT, // 79, CJK_COMPATIBILITY_IDEOGRAPHS
        COMMON_SCRIPT, // 80, ALPHABETIC_PRESENTATION_FORMS
        ARABIC_SCRIPT, // 81, ARABIC_PRESENTATION_FORMS_A
        COMMON_SCRIPT, // 82, COMBINING_HALF_MARKS
        COMMON_SCRIPT, // 83, CJK_COMPATIBILITY_FORMS
        COMMON_SCRIPT, // 84, SMALL_FORM_VARIANTS
        ARABIC_SCRIPT, // 85, ARABIC_PRESENTATION_FORMS_B
        COMMON_SCRIPT, // 86, SPECIALS
        COMMON_SCRIPT, // 87, HALFWIDTH_AND_FULLWIDTH_FORMS
        COMMON_SCRIPT, // 88, SPECIALS
    };
        
    // could be further reduced to a byte array, but I didn't bother.
    static final int[][] split = {
        {0x0250, 4, 5}, // -1
        {0x02B0, 5, 6}, // -2
        {0x0370, 7, 8}, // -3
        {0x0530, 0, 10}, // -4
        {0x0590, 10, 11}, // -5
        {0x0750, 13, 0}, // -6
        {0x07C0, 14, 0}, // -7
        {0x10A0, 28, 29}, // -8
        {0x13A0, 0, 32}, // -9
        {0x16A0, 34, 35}, // -10
        {0x18B0, 37, 0}, // -11
        {0x2070, 40, 41}, // -12
        {0x20A0, 41, -31}, // -13
        {0x2150, 44, 45}, // -14
        {0x2190, 45, 46}, // -15
        {0x2440, 49, -32}, // -16
        {0x25A0, 53, 54}, // -17
        {0x27C0, 56, 0}, // -18
        {0x2FE0, 59, -33}, // -19
        {0x3040, 61, 62}, // -20
        {0x30A0, 62, 63}, // -21
        {0x3130, 64, 65}, // -22
        {0x3190, 65, -34}, // -23
        {0x4DB6, 70, 0}, // -24
        {0xA490, 72, -35}, // -25
        {0xD7A4, 74, 0}, // -26
        {0xFB50, 80, 81}, // -27
        {0xFE20, 0, -36}, // -28
        {0xFEFF, 85, 86}, // -29
        {0xFFF0, 87, -37}, // -30
        {0x20D0, 42, 43}, // -31
        {0x2460, 50, 51}, // -32
        {0x2FF0, 0, 60}, // -33
        {0x31A0, 66, -38}, // -34
        {0xA4D0, 73, 0}, //-35
        {0xFE30, 82, -39}, //-36
        {0xFFFE, 88, 0}, //-37
        {0x31C0, 67, 0}, // -38
        {0xFE50, 83, -40}, //-39
        {0xFE70, 84, 85} // -40
    };
        
    static final byte[] charToBlock = {
      1, 2, 3, 4, -1, -2, -3, 8, 9, 9, -4, -5, 12, 12, -6, -7,
      0, 0, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 27,
      28, -8, 30, 30, 31, 31, 31, -9, 33, 33, 33, 33, 33, -10, 0, 36,
      37, -11, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 38, 38, 39, 39,
      -12, -13, -14, -15, 47, 47, 48, 48, -16, 51, 52, -17, 55, 55, 56, -18,
      57, 57, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 58, 59, -19,
      -20, -21, -22, -23, 68, 68, 69, 69, 70, 70, 70, 70, 70, 70, 70, 70,
      70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
      70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70,
      70, 70, 70, 70, 70, 70, 70, 70, 70, 70, 70, -24, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71, 71,
      72, 72, 72, 72, 72, 72, 72, 72, 72, -25, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 74, 74, 74, 74, 74, 74, 74, 74,
      74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74,
      74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74,
      74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74,
      74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74,
      74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, 74, -26,
      75, 75, 75, 75, 75, 75, 75, 76, 77, 77, 77, 77, 77, 77, 77, 77,
      78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78,
      78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78,
      78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78, 78,
      78, 78, 79, 79, 79, 79, -27, 81, 81, 81, 81, 81, -28, -29, 87, -30
    };
    */
}
