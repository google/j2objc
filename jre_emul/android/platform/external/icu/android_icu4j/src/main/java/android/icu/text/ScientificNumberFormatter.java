/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.text.CharacterIterator;
import java.util.Map;

import android.icu.lang.UCharacter;
import android.icu.util.ULocale;

/**
 *A formatter that formats numbers in user-friendly scientific notation.
 * 
 * ScientificNumberFormatter instances are immutable and thread-safe.
 *
 * Sample code:
 * <pre>
 * ULocale en = new ULocale("en");
 * ScientificNumberFormatter fmt = ScientificNumberFormatter.getMarkupInstance(
 *         en, "&lt;sup&gt;", "&lt;/sup&gt;");
 * </pre>
 * <pre>
 * // Output: "1.23456×10&lt;sup&gt;-78&lt;/sup&gt;"
 * System.out.println(fmt.format(1.23456e-78));
 * </pre>
 *
 */
public final class ScientificNumberFormatter {
    
    private final String preExponent;
    private final DecimalFormat fmt;
    private final Style style;
    
    /**
     * Gets a ScientificNumberFormatter instance that uses
     * superscript characters for exponents for this locale.
     * @param locale The locale
     * @return The ScientificNumberFormatter instance.
     */
    public static ScientificNumberFormatter getSuperscriptInstance(ULocale locale) {
        return getInstanceForLocale(locale, SUPER_SCRIPT); 
     }
     
    /**
     * Gets a ScientificNumberFormatter instance that uses
     * superscript characters for exponents.
     * @param df The DecimalFormat must be configured for scientific
     *   notation. Caller may safely change df after this call as this method
     *   clones it when creating the ScientificNumberFormatter.
     * @return the ScientificNumberFormatter instance.
     */ 
     public static ScientificNumberFormatter getSuperscriptInstance(
             DecimalFormat df) {
         return getInstance(df, SUPER_SCRIPT); 
     }
 
     /**
      * Gets a ScientificNumberFormatter instance that uses
      * markup for exponents for this locale.
      * @param locale The locale
      * @param beginMarkup the markup to start superscript e.g {@code <sup>}
      * @param endMarkup the markup to end superscript e.g {@code </sup>}
      * @return The ScientificNumberFormatter instance.
      */
     public static ScientificNumberFormatter getMarkupInstance(
             ULocale locale,
             String beginMarkup,
             String endMarkup) {
         return getInstanceForLocale(
                 locale, new MarkupStyle(beginMarkup, endMarkup));
     }
     
     /**
      * Gets a ScientificNumberFormatter instance that uses
      * markup for exponents.
      * @param df The DecimalFormat must be configured for scientific
      *   notation. Caller may safely change df after this call as this method
      *   clones it when creating the ScientificNumberFormatter.
      * @param beginMarkup the markup to start superscript e.g {@code <sup>}
      * @param endMarkup the markup to end superscript e.g {@code </sup>}
      * @return The ScientificNumberFormatter instance.
      */
     public static ScientificNumberFormatter getMarkupInstance(
             DecimalFormat df,
             String beginMarkup,
             String endMarkup) {
         return getInstance(
                 df, new MarkupStyle(beginMarkup, endMarkup));
     }
     
     /**
      * Formats a number
      * @param number Can be a double, int, Number or
      *  anything that DecimalFormat#format(Object) accepts.
      * @return the formatted string.
      */
     public String format(Object number) {
         synchronized (fmt) {
             return style.format(
                     fmt.formatToCharacterIterator(number),
                     preExponent);
         }
     }
     
    /**
     * A style type for ScientificNumberFormatter. All Style instances are immutable
     * and thread-safe.
     */
    private static abstract class Style {
        abstract String format(
                AttributedCharacterIterator iterator,
                String preExponent); // '* 10^'
        
        static void append(
                AttributedCharacterIterator iterator,
                int start,
                int limit,
                StringBuilder result) {
            int oldIndex = iterator.getIndex();
            iterator.setIndex(start);
            for (int i = start; i < limit; i++) {
                result.append(iterator.current());
                iterator.next();
            }
            iterator.setIndex(oldIndex);
        }
    }
    
    private static class MarkupStyle extends Style {
        
        private final String beginMarkup;
        private final String endMarkup;
        
        MarkupStyle(String beginMarkup, String endMarkup) {
            this.beginMarkup = beginMarkup;
            this.endMarkup = endMarkup;
        }
        
        @Override
        String format(
                AttributedCharacterIterator iterator,
                String preExponent) {
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            for (
                    iterator.first();
                    iterator.current() != CharacterIterator.DONE;
                ) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    append(
                            iterator,
                            copyFromOffset,
                            iterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL),
                            result);
                    copyFromOffset = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                    result.append(beginMarkup);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT)) {
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT);
                    append(
                            iterator,
                            copyFromOffset,
                            limit,
                            result);
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                    result.append(endMarkup);
                } else {
                    iterator.next();
                }
            }
            append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }
    }
    
    private static class SuperscriptStyle extends Style {
        
        private static final char[] SUPERSCRIPT_DIGITS = {
            0x2070, 0xB9, 0xB2, 0xB3, 0x2074, 0x2075, 0x2076, 0x2077, 0x2078, 0x2079
        };
        
        private static final char SUPERSCRIPT_PLUS_SIGN = 0x207A;
        private static final char SUPERSCRIPT_MINUS_SIGN = 0x207B;
        
        @Override
        String format(
                AttributedCharacterIterator iterator,
                String preExponent) { 
            int copyFromOffset = 0;
            StringBuilder result = new StringBuilder();
            for (
                    iterator.first();
                    iterator.current() != CharacterIterator.DONE;
                ) {
                Map<Attribute, Object> attributeSet = iterator.getAttributes();
                if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SYMBOL)) {
                    append(
                            iterator,
                            copyFromOffset,
                            iterator.getRunStart(NumberFormat.Field.EXPONENT_SYMBOL),
                            result);
                    copyFromOffset = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SYMBOL);
                    iterator.setIndex(copyFromOffset);
                    result.append(preExponent);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT_SIGN)) {
                    int start = iterator.getRunStart(NumberFormat.Field.EXPONENT_SIGN);
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT_SIGN);
                    int aChar = char32AtAndAdvance(iterator);
                    if (DecimalFormat.minusSigns.contains(aChar)) {
                        append(
                                iterator,
                                copyFromOffset,
                                start,
                                result);
                        result.append(SUPERSCRIPT_MINUS_SIGN);
                    } else if (DecimalFormat.plusSigns.contains(aChar)) {
                        append(
                                iterator,
                                copyFromOffset,
                                start,
                                result);
                        result.append(SUPERSCRIPT_PLUS_SIGN);
                    } else {
                        throw new IllegalArgumentException();
                    }
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                } else if (attributeSet.containsKey(NumberFormat.Field.EXPONENT)) {
                    int start = iterator.getRunStart(NumberFormat.Field.EXPONENT);
                    int limit = iterator.getRunLimit(NumberFormat.Field.EXPONENT);
                    append(
                            iterator,
                            copyFromOffset,
                            start,
                            result);
                    copyAsSuperscript(iterator, start, limit, result);
                    copyFromOffset = limit;
                    iterator.setIndex(copyFromOffset);
                } else {
                    iterator.next();
                }
            } 
            append(iterator, copyFromOffset, iterator.getEndIndex(), result);
            return result.toString();
        }
        
        private static void copyAsSuperscript(
                AttributedCharacterIterator iterator, int start, int limit, StringBuilder result) {
            int oldIndex = iterator.getIndex();
            iterator.setIndex(start);
            while (iterator.getIndex() < limit) {
                int aChar = char32AtAndAdvance(iterator);
                int digit = UCharacter.digit(aChar);
                if (digit < 0) {
                    throw new IllegalArgumentException();
                }
                result.append(SUPERSCRIPT_DIGITS[digit]);
            }
            iterator.setIndex(oldIndex);
        }
        
        private static int char32AtAndAdvance(AttributedCharacterIterator iterator) {
            char c1 = iterator.current();
            char c2 = iterator.next();
            if (UCharacter.isHighSurrogate(c1)) {
                // If c2 is DONE, it will fail the low surrogate test and we
                // skip this block.
                if (UCharacter.isLowSurrogate(c2)) {
                    iterator.next();
                    return UCharacter.toCodePoint(c1, c2);
                }
            }
            return c1;
        }
            
    }
    
    private static String getPreExponent(DecimalFormatSymbols dfs) {
        StringBuilder preExponent = new StringBuilder();
        preExponent.append(dfs.getExponentMultiplicationSign());
        char[] digits = dfs.getDigits();
        preExponent.append(digits[1]).append(digits[0]);
        return preExponent.toString();
    }
    
    private static ScientificNumberFormatter getInstance(
            DecimalFormat decimalFormat, Style style) {
        DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols();
        return new ScientificNumberFormatter(
                (DecimalFormat) decimalFormat.clone(), getPreExponent(dfs), style);
    }
     
    private static ScientificNumberFormatter getInstanceForLocale(
            ULocale locale, Style style) {
        DecimalFormat decimalFormat =
                (DecimalFormat) DecimalFormat.getScientificInstance(locale);
        return new ScientificNumberFormatter(
                decimalFormat,
                getPreExponent(decimalFormat.getDecimalFormatSymbols()),
                style);
    }
    
    private static final Style SUPER_SCRIPT = new SuperscriptStyle();
    
    private ScientificNumberFormatter(
            DecimalFormat decimalFormat, String preExponent, Style style) {
        this.fmt = decimalFormat;
        this.preExponent = preExponent;
        this.style = style;
    }
    

}
