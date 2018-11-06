/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2004, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.rbbi;

import java.util.ListResourceBundle;

/**
 * This resource bundle is included for testing and demonstration purposes only.
 * It applies the dictionary-based algorithm to English text that has had all the
 * spaces removed.  Once we have good test cases for Thai, we will replace this
 * with good resource data (and a good dictionary file) for Thai
 */
public class BreakIteratorRules_en_US_TEST extends ListResourceBundle {
    private static final String DATA_NAME = "/android/icu/dev/data/rbbi/english.dict";

    // calling code will handle case where dictionary does not exist

    public Object[][] getContents() {
        return new Object[][] {
            // names of classes to instantiate for the different kinds of break
            // iterator.  Notice we're now using DictionaryBasedBreakIterator
            // for word and line breaking.
            {   "BreakIteratorClasses",
                new String[] {
                    "RuleBasedBreakIterator",
                    // character-break iterator class
                    "DictionaryBasedBreakIterator",
                    // word-break iterator class
                    "DictionaryBasedBreakIterator",
                    // line-break iterator class
                    "RuleBasedBreakIterator" } // sentence-break iterator class
            },

            // These are the same word-breaking rules as are specified in the default
            // resource, except that the Latin letters, apostrophe, and hyphen are
            // specified as dictionary characters
            {
                "WordBreakRules",
                // ignore non-spacing marks, enclosing marks, and format characters,
                // all of which should not influence the algorithm
                "$_ignore_=[[:Mn:][:Me:][:Cf:]];"

                // lower and upper case Roman letters, apostrophy and dash are
                // in the English dictionary
                +"$_dictionary_=[a-zA-Z\\'\\-];"

                // Hindi phrase separator, kanji, katakana, hiragana, CJK diacriticals,
                // other letters, and digits
                +"$danda=[\u0964\u0965];"
                    + "$kanji=[\u3005\u4e00-\u9fa5\uf900-\ufa2d];"
                    + "$kata=[\u3099-\u309c\u30a1-\u30fe];"
                    + "$hira=[\u3041-\u309e\u30fc];"
                    + "$let=[[[:L:][:Mc:]]-[$kanji$kata$hira]];"
                    + "$dgt=[:N:];"

                // punctuation that can occur in the middle of a word: currently
                // dashes, apostrophes, and quotation marks
                +"$mid_word=[[:Pd:]\u00ad\u2027\\\"\\\'];"

                // punctuation that can occur in the middle of a number: currently
                // apostrophes, qoutation marks, periods, commas, and the Arabic
                // decimal point
                +"$mid_num=[\\\"\\\'\\,\u066b\\.];"

                // punctuation that can occur at the beginning of a number: currently
                // the period, the number sign, and all currency symbols except the cents sign
                +"$pre_num=[[[:Sc:]-[\u00a2]]\\#\\.];"

                // punctuation that can occur at the end of a number: currently
                // the percent, per-thousand, per-ten-thousand, and Arabic percent
                // signs, the cents sign, and the ampersand
                +"$post_num=[\\%\\&\u00a2\u066a\u2030\u2031];"

                // line separators: currently LF, FF, PS, and LS
                +"$ls=[\n\u000c\u2028\u2029];"

                // whitespace: all space separators and the tab character
                +"$ws=[[:Zs:]\t];"

                // a word is a sequence of letters that may contain internal
                // punctuation, as long as it begins and ends with a letter and
                // never contains two punctuation marks in a row
                +"$word=($let+($mid_word$let+)*$danda?);"

                // a number is a sequence of digits that may contain internal
                // punctuation, as long as it begins and ends with a digit and
                // never contains two punctuation marks in a row.
                +"$number=($dgt+($mid_num$dgt+)*);"

                // break after every character, with the following exceptions
                // (this will cause punctuation marks that aren't considered
                // part of words or numbers to be treated as words unto themselves)
                +".;"

                // keep together any sequence of contiguous words and numbers
                // (including just one of either), plus an optional trailing
                // number-suffix character
                +"$word?($number$word)*($number$post_num?)?;"

                // keep together and sequence of contiguous words and numbers
                // that starts with a number-prefix character and a number,
                // and may end with a number-suffix character
                +"$pre_num($number$word)*($number$post_num?)?;"

                // keep together runs of whitespace (optionally with a single trailing
                // line separator or CRLF sequence)
                +"$ws*\r?$ls?;"

                // keep together runs of Katakana
                +"$kata*;"

                // keep together runs of Hiragana
                +"$hira*;"

                // keep together runs of Kanji
                +"$kanji*;" },

            // These are the same line-breaking rules as are specified in the default
            // resource, except that the Latin letters, apostrophe, and hyphen are
            // specified as dictionary characters
            {   "LineBreakRules",
                // ignore non-spacing marks, enclosing marks, and format characters
                "$_ignore_=[[:Mn:][:Me:][:Cf:]];"

                // lower and upper case Roman letters, apostrophy and dash
                // are in the English dictionary
                +"$_dictionary_=[a-zA-Z\\'\\-];"

                // Hindi phrase separators
                +"$danda=[\u0964\u0965];"

                // characters that always cause a break: ETX, tab, LF, FF, LS, and PS
                +"$break=[\u0003\t\n\f\u2028\u2029];"

                // characters that always prevent a break: the non-breaking space
                // and similar characters
                +"$nbsp=[\u00a0\u2007\u2011\ufeff];"

                // whitespace: space separators and control characters, except for
                // CR and the other characters mentioned above
                +"$space=[[[:Zs:][:Cc:]]-[$nbsp$break\r]];"

                // dashes: dash punctuation and the discretionary hyphen, except for
                // non-breaking hyphens
                +"$dash=[[[:Pd:]\u00ad]-[$nbsp]];"

                // characters that stick to a word if they precede it: currency symbols
                // (except the cents sign) and starting punctuation
                +"$pre_word=[[[:Sc:]-[\u00a2]][:Ps:]\\\"\\\'];"

                // characters that stick to a word if they follow it: ending punctuation,
                // other punctuation that usually occurs at the end of a sentence,
                // small Kana characters, some CJK diacritics, etc.
                +"$post_word=[[:Pe:]\\!\\\"\\\'\\%\\.\\,\\:\\;\\?\u00a2\u00b0\u066a\u2030-\u2034"
                    + "\u2103\u2105\u2109\u3001\u3002\u3005\u3041\u3043\u3045\u3047\u3049\u3063"
                    + "\u3083\u3085\u3087\u308e\u3099-\u309e\u30a1\u30a3\u30a5\u30a7\u30a9"
                    + "\u30c3\u30e3\u30e5\u30e7\u30ee\u30f5\u30f6\u30fc-\u30fe\uff01\uff0c"
                    + "\uff0e\uff1f];"

                // Kanji: actually includes both Kanji and Kana, except for small Kana and
                // CJK diacritics
                +"$kanji=[[\u4e00-\u9fa5\uf900-\ufa2d\u3041-\u3094\u30a1-\u30fa]-[$post_word$_ignore_]];"

                // digits
                +"$digit=[[:Nd:][:No:]];"

                // punctuation that can occur in the middle of a number: periods and commas
                +"$mid_num=[\\.\\,];"

                // everything not mentioned above, plus the quote marks (which are both
                // <pre-word>, <post-word>, and <char>)
                +"$char=[^$break$space$dash$kanji$nbsp$_ignore_$pre_word$post_word$mid_num$danda\r\\\"\\\'];"

                // a "number" is a run of prefix characters and dashes, followed by one or
                // more digits with isolated number-punctuation characters interspersed
                +"$number=([$pre_word$dash]*$digit+($mid_num$digit+)*);"

                // the basic core of a word can be either a "number" as defined above, a single
                // "Kanji" character, or a run of any number of not-explicitly-mentioned
                // characters (this includes Latin letters)
                +"$word_core=([$pre_word$char]*|$kanji|$number);"

                // a word may end with an optional suffix that be either a run of one or
                // more dashes or a run of word-suffix characters, followed by an optional
                // run of whitespace
                +"$word_suffix=(($dash+|$post_word*)$space*);"

                // a word, thus, is an optional run of word-prefix characters, followed by
                // a word core and a word suffix (the syntax of <word-core> and <word-suffix>
                // actually allows either of them to match the empty string, putting a break
                // between things like ")(" or "aaa(aaa"
                +"$word=($pre_word*$word_core$word_suffix);"

                // finally, the rule that does the work: Keep together any run of words that
                // are joined by runs of one of more non-spacing mark.  Also keep a trailing
                // line-break character or CRLF combination with the word.  (line separators
                // "win" over nbsp's)
                +"$word($nbsp+$word)*\r?$break?;" },

            // these two resources specify the pathnames of the dictionary files to
            // use for word breaking and line breaking.  Both currently refer to
            // a file called english.dict placed in android.icu.impl.data
            // somewhere in the class path.  It's important to note that
            // english.dict was created for testing purposes only, and doesn't
            // come anywhere close to being an exhaustive dictionary of English
            // words (basically, it contains all the words in the Declaration of
            // Independence, and the Revised Standard Version of the book of Genesis,
            // plus a few other words thrown in to show more interesting cases).
            // { "WordBreakDictionary", "com\\ibm\\text\\resources\\english.dict" },
            // { "LineBreakDictionary", "com\\ibm\\text\\resources\\english.dict" }
            {   "WordBreakDictionary", DATA_NAME }, 
            {   "LineBreakDictionary", DATA_NAME }
        };
    }
}
