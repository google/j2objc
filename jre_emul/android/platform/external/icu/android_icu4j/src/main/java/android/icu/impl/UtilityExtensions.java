/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

import android.icu.text.Replaceable;
import android.icu.text.ReplaceableString;
import android.icu.text.Transliterator;
import android.icu.text.UnicodeMatcher;
/**
 * @author Ram
 * @hide Only a subset of ICU is exposed in Android
 */
//This class contains utility functions so testing not needed
///CLOVER:OFF
public class UtilityExtensions {
    /**
     * Append the given string to the rule.  Calls the single-character
     * version of appendToRule for each character.
     */
    public static void appendToRule(StringBuffer rule,
                                    String text,
                                    boolean isLiteral,
                                    boolean escapeUnprintable,
                                    StringBuffer quoteBuf) {
        for (int i=0; i<text.length(); ++i) {
            // Okay to process in 16-bit code units here
            Utility.appendToRule(rule, text.charAt(i), isLiteral, escapeUnprintable, quoteBuf);
        }
    }


    /**
     * Given a matcher reference, which may be null, append its
     * pattern as a literal to the given rule.
     */
    public static void appendToRule(StringBuffer rule,
                                    UnicodeMatcher matcher,
                                    boolean escapeUnprintable,
                                    StringBuffer quoteBuf) {
        if (matcher != null) {
            appendToRule(rule, matcher.toPattern(escapeUnprintable),
                         true, escapeUnprintable, quoteBuf);
        }
    }
    /**
     * For debugging purposes; format the given text in the form
     * aaa{bbb|ccc|ddd}eee, where the {} indicate the context start
     * and limit, and the || indicate the start and limit.
     */
    public static String formatInput(ReplaceableString input,
                                     Transliterator.Position pos) {
        StringBuffer appendTo = new StringBuffer();
        formatInput(appendTo, input, pos);
        return android.icu.impl.Utility.escape(appendTo.toString());
    }

    /**
     * For debugging purposes; format the given text in the form
     * aaa{bbb|ccc|ddd}eee, where the {} indicate the context start
     * and limit, and the || indicate the start and limit.
     */
    public static StringBuffer formatInput(StringBuffer appendTo,
                                           ReplaceableString input,
                                           Transliterator.Position pos) {
        if (0 <= pos.contextStart &&
            pos.contextStart <= pos.start &&
            pos.start <= pos.limit &&
            pos.limit <= pos.contextLimit &&
            pos.contextLimit <= input.length()) {

            String  b, c, d;
            //a = input.substring(0, pos.contextStart);
            b = input.substring(pos.contextStart, pos.start);
            c = input.substring(pos.start, pos.limit);
            d = input.substring(pos.limit, pos.contextLimit);
            //e = input.substring(pos.contextLimit, input.length());
            appendTo.//append(a).
                append('{').append(b).
                append('|').append(c).append('|').append(d).
                append('}')
                //.append(e)
                ;
        } else {
            appendTo.append("INVALID Position {cs=" +
                            pos.contextStart + ", s=" + pos.start + ", l=" +
                            pos.limit + ", cl=" + pos.contextLimit + "} on " +
                            input);
        }
        return appendTo;
    }

    /**
     * Convenience method.
     */
    public static String formatInput(Replaceable input,
                                     Transliterator.Position pos) {
        return formatInput((ReplaceableString) input, pos);
    }

    /**
     * Convenience method.
     */
    public static StringBuffer formatInput(StringBuffer appendTo,
                                           Replaceable input,
                                           Transliterator.Position pos) {
        return formatInput(appendTo, (ReplaceableString) input, pos);
    }

}
//CLOVER:ON