/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.icu;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.NoSuchElementException;

/*-[
#import "java/lang/Double.h"
#import "java/lang/Long.h"

#include <math.h>
]-*/

public final class NativeDecimalFormat implements Cloneable {

    /**
     * The associated NSDecimalFormatter.
     */
    private Object nsFormatter;

    /**
     * The last pattern we gave to iOS, so we can make repeated applications cheap.
     * This helps in cases like String.format("%.2f,%.2f\n", x, y) where the DecimalFormat is
     * reused.
     */
    private String lastPattern;

    // TODO: store all these in DecimalFormat instead!
    private boolean negPrefNull;
    private boolean negSuffNull;
    private boolean posPrefNull;
    private boolean posSuffNull;

    private transient boolean parseBigDecimal;

    /**
     * Cache the BigDecimal form of the multiplier. This is null until we've
     * formatted a BigDecimal (with a multiplier that is not 1), or the user has
     * explicitly called {@link #setMultiplier(int)} with any multiplier.
     */
    private BigDecimal multiplierBigDecimal = null;

    public NativeDecimalFormat(String pattern, DecimalFormatSymbols dfs) {
        try {
            this.nsFormatter = open(pattern, dfs.getCurrencySymbol(),
                    dfs.getDecimalSeparator(), dfs.getDigit(), dfs.getExponentSeparator(),
                    dfs.getGroupingSeparator(), dfs.getInfinity(),
                    dfs.getInternationalCurrencySymbol(), dfs.getMinusSign(),
                    dfs.getMonetaryDecimalSeparator(), dfs.getNaN(), dfs.getPatternSeparator(),
                    dfs.getPercent(), dfs.getPerMill(), dfs.getZeroDigit());
            this.lastPattern = pattern;
            this.setMultiplier(1);  // JRE default.
        } catch (NullPointerException npe) {
            throw npe;
        } catch (RuntimeException re) {
            throw new IllegalArgumentException("syntax error: " + re.getMessage() + ": " + pattern);
        }
    }

    // Used so java.util.Formatter doesn't need to allocate DecimalFormatSymbols instances.
    public NativeDecimalFormat(String pattern, LocaleData data) {
        this.nsFormatter = open(pattern, data.currencySymbol,
                data.decimalSeparator, '#', data.exponentSeparator, data.groupingSeparator,
                data.infinity, data.internationalCurrencySymbol, data.minusSign,
                data.monetarySeparator, data.NaN, data.patternSeparator,
                data.percent, data.perMill, data.zeroDigit);
        this.lastPattern = pattern;
    }

    public synchronized void close() {
        if (nsFormatter != null) {
            close(nsFormatter);
            nsFormatter = null;
        }
    }

    @Override protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    @Override public Object clone() {
        try {
            NativeDecimalFormat clone = (NativeDecimalFormat) super.clone();
            clone.nsFormatter = cloneImpl(nsFormatter);
            clone.lastPattern = lastPattern;
            return clone;
        } catch (CloneNotSupportedException unexpected) {
            throw new AssertionError(unexpected);
        }
    }

    /**
     * Note: this doesn't check that the underlying native DecimalFormat objects' configured
     * native DecimalFormatSymbols objects are equal. It is assumed that the
     * caller (DecimalFormat) will check the DecimalFormatSymbols objects
     * instead, for performance.
     *
     * This is also unreasonably expensive, calling down to JNI multiple times.
     *
     * TODO: remove this and just have DecimalFormat.equals do the right thing itself.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof NativeDecimalFormat)) {
            return false;
        }
        NativeDecimalFormat obj = (NativeDecimalFormat) object;
        if (obj.nsFormatter == this.nsFormatter) {
            return true;
        }
        return obj.toPattern().equals(this.toPattern()) &&
                obj.isDecimalSeparatorAlwaysShown() == this.isDecimalSeparatorAlwaysShown() &&
                obj.getGroupingSize() == this.getGroupingSize() &&
                obj.getMultiplier() == this.getMultiplier() &&
                obj.getNegativePrefix().equals(this.getNegativePrefix()) &&
                obj.getNegativeSuffix().equals(this.getNegativeSuffix()) &&
                obj.getPositivePrefix().equals(this.getPositivePrefix()) &&
                obj.getPositiveSuffix().equals(this.getPositiveSuffix()) &&
                obj.getMaximumIntegerDigits() == this.getMaximumIntegerDigits() &&
                obj.getMaximumFractionDigits() == this.getMaximumFractionDigits() &&
                obj.getMinimumIntegerDigits() == this.getMinimumIntegerDigits() &&
                obj.getMinimumFractionDigits() == this.getMinimumFractionDigits() &&
                obj.isGroupingUsed() == this.isGroupingUsed();
    }

    /**
     * Copies the DecimalFormatSymbols settings into our native peer in bulk.
     */
    public void setDecimalFormatSymbols(final DecimalFormatSymbols dfs) {
        setDecimalFormatSymbols(this.nsFormatter, dfs.getCurrencySymbol(),
            dfs.getDecimalSeparator(), dfs.getDigit(), dfs.getExponentSeparator(),
            dfs.getGroupingSeparator(), dfs.getInfinity(), dfs.getInternationalCurrencySymbol(),
            dfs.getMinusSign(), dfs.getMonetaryDecimalSeparator(), dfs.getNaN(),
            dfs.getPatternSeparator(), dfs.getPercent(), dfs.getPerMill(), dfs.getZeroDigit());
    }

    public void setDecimalFormatSymbols(final LocaleData localeData) {
        setDecimalFormatSymbols(this.nsFormatter, localeData.currencySymbol,
            localeData.decimalSeparator, '#', localeData.exponentSeparator,
            localeData.groupingSeparator, localeData.infinity,
            localeData.internationalCurrencySymbol, localeData.minusSign,
            localeData.monetarySeparator, localeData.NaN, localeData.patternSeparator,
            localeData.percent, localeData.perMill, localeData.zeroDigit);
    }

    public char[] formatBigDecimal(BigDecimal value, FieldPosition field) {
        FieldPositionIterator fpi = FieldPositionIterator.forFieldPosition(field);
        // iOS doesn't have native support for big decimal formatting.
        char[] result = value.toPlainString().toCharArray();
        if (fpi != null) {
            FieldPositionIterator.setFieldPosition(fpi, field);
        }
        return result;
    }

    public char[] formatBigInteger(BigInteger value, FieldPosition field) {
        FieldPositionIterator fpi = FieldPositionIterator.forFieldPosition(field);
        // iOS doesn't have native support for big integer formatting.
        char[] result = value.toString().toCharArray();
        if (fpi != null) {
            FieldPositionIterator.setFieldPosition(fpi, field);
        }
        return result;
    }

    public char[] formatLong(long value, FieldPosition field) {
        FieldPositionIterator fpi = FieldPositionIterator.forFieldPosition(field);
        char[] result = formatLong(this.nsFormatter, value, fpi);
        if (fpi != null) {
            FieldPositionIterator.setFieldPosition(fpi, field);
        }
        return result;
    }

    public char[] formatDouble(double value, FieldPosition field) {
        FieldPositionIterator fpi = FieldPositionIterator.forFieldPosition(field);
        char[] result = formatDouble(this.nsFormatter, value, fpi);
        if (fpi != null) {
            FieldPositionIterator.setFieldPosition(fpi, field);
        }
        return result;
    }

    public void applyLocalizedPattern(String pattern) {
        applyPattern(this.nsFormatter, true, pattern);
        lastPattern = null;
    }

    public void applyPattern(String pattern) {
        if (lastPattern != null && pattern.equals(lastPattern)) {
            return;
        }
        applyPattern(this.nsFormatter, false, pattern);
        lastPattern = pattern;
    }

    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (object == null) {
            throw new NullPointerException("object == null");
        }
        if (!(object instanceof Number)) {
            throw new IllegalArgumentException("object not a Number: " + object.getClass());
        }
        Number number = (Number) object;
        FieldPositionIterator fpIter = new FieldPositionIterator();
        String text;
        if (number instanceof BigInteger) {
          text = new String(formatBigInteger((BigInteger) number, null));
        } else if (number instanceof BigDecimal) {
            text = new String(formatBigDecimal((BigDecimal) number, null));
        } else if (number instanceof Double || number instanceof Float) {
            double dv = number.doubleValue();
            text = new String(formatDouble(this.nsFormatter, dv, fpIter));
        } else {
            long lv = number.longValue();
            text = new String(formatLong(this.nsFormatter, lv, fpIter));
        }

        AttributedString as = new AttributedString(text);

        while (fpIter.next()) {
            Format.Field field = fpIter.field();
            as.addAttribute(field, field, fpIter.start(), fpIter.limit());
        }

        // return the CharacterIterator from AttributedString
        return as.getIterator();
    }

    public String toLocalizedPattern() {
        return toPatternImpl(this.nsFormatter, true);
    }

    public String toPattern() {
        return toPatternImpl(this.nsFormatter, false);
    }

    public Number parse(String string, ParsePosition position) {
        return parse(nsFormatter, string, position, parseBigDecimal);
    }

    // start getter and setter

    public native int getMaximumFractionDigits() /*-[
      return (int) [(NSNumberFormatter *) nsFormatter_ maximumFractionDigits];
    ]-*/;

    public native int getMaximumIntegerDigits() /*-[
      return (int) [(NSNumberFormatter *) nsFormatter_ maximumIntegerDigits];
    ]-*/;

    public native int getMinimumFractionDigits() /*-[
      return (int) [(NSNumberFormatter *) nsFormatter_ minimumFractionDigits];
    ]-*/;

    public native int getMinimumIntegerDigits() /*-[
      return (int) [(NSNumberFormatter *) nsFormatter_ minimumIntegerDigits];
    ]-*/;

    public native int getGroupingSize() /*-[
      return (int) [(NSNumberFormatter *) nsFormatter_ groupingSize];
    ]-*/;

    public native int getMultiplier() /*-[
      return [[(NSNumberFormatter *) nsFormatter_ multiplier] intValue];
    ]-*/;

    public native String getNegativePrefix() /*-[
      return [(NSNumberFormatter *) nsFormatter_ negativePrefix];
    ]-*/;

    public native String getNegativeSuffix() /*-[
      return [(NSNumberFormatter *) nsFormatter_ negativeSuffix];
    ]-*/;

    public native String getPositivePrefix() /*-[
      return [(NSNumberFormatter *) nsFormatter_ positivePrefix];
    ]-*/;

    public native String getPositiveSuffix() /*-[
      return [(NSNumberFormatter *) nsFormatter_ positivePrefix];
    ]-*/;

    public native boolean isDecimalSeparatorAlwaysShown() /*-[
      return [(NSNumberFormatter *) nsFormatter_ alwaysShowsDecimalSeparator];
    ]-*/;

    public boolean isParseBigDecimal() {
        return parseBigDecimal;
    }

    public native boolean isParseIntegerOnly() /*-[
      return [(NSNumberFormatter *) nsFormatter_ allowsFloats] == NO;
    ]-*/;

    public native boolean isGroupingUsed() /*-[
      return [(NSNumberFormatter *) nsFormatter_ usesGroupingSeparator];
    ]-*/;

    public native void setDecimalSeparatorAlwaysShown(boolean value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setAlwaysShowsDecimalSeparator:value];
    ]-*/;

    public native void setCurrency(Currency currency) /*-[
      [(NSNumberFormatter *) nsFormatter_ setCurrencySymbol:[currency getSymbol]];
      [(NSNumberFormatter *) nsFormatter_ setCurrencyCode:[currency getCurrencyCode]];
    ]-*/;

    public native void setGroupingSize(int value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setGroupingSize:value];
    ]-*/;

    public native void setGroupingUsed(boolean value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setUsesGroupingSeparator:value];
    ]-*/;

    public native void setMaximumFractionDigits(int value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setMaximumFractionDigits:value];
    ]-*/;

    public native void setMaximumIntegerDigits(int value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setMaximumIntegerDigits:value];
    ]-*/;

    public native void setMinimumFractionDigits(int value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setMinimumFractionDigits:value];
    ]-*/;

    public native void setMinimumIntegerDigits(int value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setMinimumIntegerDigits:value];
    ]-*/;

    public native void setMultiplier(int value) /*-[
      [(NSNumberFormatter *) nsFormatter_ setMultiplier:[NSNumber numberWithInt:value]];
      LibcoreIcuNativeDecimalFormat_set_multiplierBigDecimal_(self,
          JavaMathBigDecimal_valueOfWithLong_(value));
    ]-*/;

    public native void setNegativePrefix(String value) /*-[
      negPrefNull_ = (value == nil);
      if (!negPrefNull_) {
        [(NSNumberFormatter *) nsFormatter_ setNegativePrefix:value];
      }
    ]-*/;

    public native void setNegativeSuffix(String value) /*-[
      negSuffNull_ = (value == nil);
      if (!negSuffNull_) {
        [(NSNumberFormatter *) nsFormatter_ setNegativeSuffix:value];
      }
    ]-*/;

    public native void setPositivePrefix(String value) /*-[
      posPrefNull_ = (value == nil);
      if (!posPrefNull_) {
        [(NSNumberFormatter *) nsFormatter_ setPositivePrefix:value];
      }
    ]-*/;

    public native void setPositiveSuffix(String value) /*-[
      posSuffNull_ = (value == nil);
      if (!posSuffNull_) {
        [(NSNumberFormatter *) nsFormatter_ setPositiveSuffix:value];
      }
    ]-*/;

    public void setParseBigDecimal(boolean value) {
        parseBigDecimal = value;
    }

    public native void setParseIntegerOnly(boolean value) /*-[
      BOOL floats = value ? NO : YES;
      [(NSNumberFormatter *) nsFormatter_ setAllowsFloats:floats];
    ]-*/;

    private static void applyPattern(Object nativeFormatter, boolean localized, String pattern) {
        try {
            applyPatternImpl(nativeFormatter, localized, pattern);
        } catch (NullPointerException npe) {
            throw npe;
        } catch (RuntimeException re) {
            throw new IllegalArgumentException("syntax error: " + re.getMessage() + ": " + pattern);
        }
    }

    public native void setRoundingMode(RoundingMode roundingMode, double roundingIncrement) /*-[
      int nsRoundingMode;
      switch ([roundingMode ordinal]) {
        case JavaMathRoundingMode_CEILING: nsRoundingMode = NSNumberFormatterRoundCeiling; break;
        case JavaMathRoundingMode_FLOOR: nsRoundingMode = NSNumberFormatterRoundFloor; break;
        case JavaMathRoundingMode_DOWN: nsRoundingMode = NSNumberFormatterRoundDown; break;
        case JavaMathRoundingMode_UP: nsRoundingMode = NSNumberFormatterRoundUp; break;
        case JavaMathRoundingMode_HALF_EVEN: nsRoundingMode = NSNumberFormatterRoundHalfEven; break;
        case JavaMathRoundingMode_HALF_DOWN: nsRoundingMode = NSNumberFormatterRoundHalfDown; break;
        case JavaMathRoundingMode_HALF_UP: nsRoundingMode = NSNumberFormatterRoundHalfUp; break;
        default:
          @throw [[JavaLangAssertionError alloc] init];
      }
      NSNumberFormatter *formatter = (NSNumberFormatter *) nsFormatter_;
      [formatter setRoundingMode:nsRoundingMode];
      [formatter setRoundingIncrement:[NSNumber numberWithDouble:roundingIncrement]];
    ]-*/;

    // Utility to get information about field positions from native (ICU) code.
    private static class FieldPositionIterator {
        private int[] data;
        private int pos = -3; // so first call to next() leaves pos at 0

        private FieldPositionIterator() {
        }

        public static FieldPositionIterator forFieldPosition(FieldPosition fp) {
            // TODO(tball): figure out how to do field position iterating on iOS.
//            if (fp != null && fp.getField() != -1) {
//                return new FieldPositionIterator();
//            }
            return null;
        }

        private static int getNativeFieldPositionId(FieldPosition fp) {
            // NOTE: -1, 0, and 1 were the only valid original java field values
            // for NumberFormat.  They take precedence.  This assumes any other
            // value is a mistake and the actual value is in the attribute.
            // Clients can construct FieldPosition combining any attribute with any field
            // value, which is just wrong, but there you go.

            int id = fp.getField();
            if (id < -1 || id > 1) {
                id = -1;
            }
            if (id == -1) {
                Format.Field attr = fp.getFieldAttribute();
                if (attr != null) {
                    for (int i = 0; i < fields.length; ++i) {
                        if (fields[i].equals(attr)) {
                            id = i;
                            break;
                        }
                    }
                }
            }
            return id;
        }

        private static void setFieldPosition(FieldPositionIterator fpi, FieldPosition fp) {
            if (fpi != null && fp != null) {
                int field = getNativeFieldPositionId(fp);
                if (field != -1) {
                    while (fpi.next()) {
                        if (fpi.fieldId() == field) {
                            fp.setBeginIndex(fpi.start());
                            fp.setEndIndex(fpi.limit());
                            break;
                        }
                    }
                }
            }
        }

        public boolean next() {
            // if pos == data.length, we've already returned false once
            if (data == null || pos == data.length) {
                throw new NoSuchElementException();
            }
            pos += 3;
            return pos < data.length;
        }

        private void checkValid() {
            if (data == null || pos < 0 || pos == data.length) {
                throw new NoSuchElementException();
            }
        }

        public int fieldId() {
            return data[pos];
        }

        public Format.Field field() {
            checkValid();
            return fields[data[pos]];
        }

        public int start() {
            checkValid();
            return data[pos + 1];
        }

        public int limit() {
            checkValid();
            return data[pos + 2];
        }

        private static Format.Field fields[] = {
            // The old java field values were 0 for integer and 1 for fraction.
            // The new java field attributes are all objects.  ICU assigns the values
            // starting from 0 in the following order; note that integer and
            // fraction positions match the old field values.
            NumberFormat.Field.INTEGER,
            NumberFormat.Field.FRACTION,
            NumberFormat.Field.DECIMAL_SEPARATOR,
            NumberFormat.Field.EXPONENT_SYMBOL,
            NumberFormat.Field.EXPONENT_SIGN,
            NumberFormat.Field.EXPONENT,
            NumberFormat.Field.GROUPING_SEPARATOR,
            NumberFormat.Field.CURRENCY,
            NumberFormat.Field.PERCENT,
            NumberFormat.Field.PERMILLE,
            NumberFormat.Field.SIGN,
        };

        // called by native
        private void setData(int[] data) {
            this.data = data;
            this.pos = -3;
        }
    }

    private static native Object open(String pattern, String currencySymbol,
        char decimalSeparator, char digit, String exponentSeparator, char groupingSeparator,
        String infinity, String internationalCurrencySymbol, char minusSign,
        char monetaryDecimalSeparator, String nan, char patternSeparator, char percent,
        char perMill, char zeroDigit) /*-[
      NSNumberFormatter *formatter = [[NSNumberFormatter alloc] init];
      LibcoreIcuNativeDecimalFormat_applyPatternImplWithId_withBoolean_withNSString_(
          formatter, YES, pattern);
      LibcoreIcuNativeDecimalFormat_setDecimalFormatSymbolsWithId_withNSString_withChar_withChar_withNSString_withChar_withNSString_withNSString_withChar_withChar_withNSString_withChar_withChar_withChar_withChar_(
          formatter, currencySymbol, decimalSeparator, digit, exponentSeparator, groupingSeparator,
          infinity, internationalCurrencySymbol, minusSign, monetaryDecimalSeparator, nan,
          patternSeparator, percent, perMill, zeroDigit);

      return AUTORELEASE(formatter);
    ]-*/;

    private static native void setDecimalFormatSymbols(Object nativeFormatter,
        String currencySymbol, char decimalSeparator, char digit, String exponentSeparator,
        char groupingSeparator, String infinity, String internationalCurrencySymbol, char minusSign,
        char monetaryDecimalSeparator, String nan, char patternSeparator, char percent,
        char perMill, char zeroDigit) /*-[
      NSNumberFormatter *formatter = (NSNumberFormatter *) nativeFormatter;
      [formatter setCurrencySymbol:currencySymbol];
      [formatter setDecimalSeparator:[NSString stringWithCharacters:&decimalSeparator length:1]];
      [formatter setExponentSymbol:exponentSeparator];
      [formatter setGroupingSeparator:[NSString stringWithCharacters:&groupingSeparator length:1]];
      [formatter setPositiveInfinitySymbol:infinity];
      [formatter setInternationalCurrencySymbol:internationalCurrencySymbol];
      [formatter setNegativePrefix:[NSString stringWithCharacters:&minusSign length:1]];
      [formatter
          setCurrencyGroupingSeparator:[NSString stringWithCharacters:&monetaryDecimalSeparator
                                                               length:1]];
      [formatter setNotANumberSymbol:nan];
      [formatter setPercentSymbol:[NSString stringWithCharacters:&percent length:1]];
      [formatter setPerMillSymbol:[NSString stringWithCharacters:&perMill length:1]];
      if (zeroDigit != '0') {
        [formatter setZeroSymbol:[NSString stringWithCharacters:&zeroDigit length:1]];
      }
    ]-*/;

    /**
     * Applies a Java format pattern to an NSNumberFormatter. This pattern
     * should be split into positive and negative patterns, if there is a
     * ';' token (iOS doesn't support a configurable pattern separator).
     */
    private static native void applyPatternImpl(Object nativeFormatter, boolean localized,
        String pattern) /*-[
      NSNumberFormatter *formatter = (NSNumberFormatter *) nativeFormatter;
      NSArray *formats = [pattern componentsSeparatedByString:@";"];
      [formatter setPositiveFormat:[formats objectAtIndex:0]];
      if ([formats count] > 1) {
        [formatter setNegativeFormat:[formats objectAtIndex:1]];
      }
    ]-*/;

    private static native Object cloneImpl(Object nativeFormatter) /*-[
      NSNumberFormatter *formatter = (NSNumberFormatter *) nativeFormatter;
      return [formatter copyWithZone:NULL];
    ]-*/;

    private static native void close(Object nativeFormatter) /*-[
      // Nothing to close:
    ]-*/;

    private static native char[] formatDouble(Object nativeFormatter, double value,
        FieldPositionIterator iter) /*-[
      NSNumberFormatter *formatter = (NSNumberFormatter *) nativeFormatter;
      [formatter setAllowsFloats:YES];
      NSString *format = [formatter positiveFormat];
      NSString *result;
      if ([format hasPrefix:@"%"] || [format hasSuffix:@"%"]) {
        [formatter setNumberStyle:NSNumberFormatterPercentStyle];
        result = [formatter stringFromNumber:[NSNumber numberWithDouble:value * 100.0]];
      } else if ([format hasPrefix:@"\u00A4"] || [format hasSuffix:@"\u00A4"]) {
        [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
        result = [formatter stringFromNumber:[NSNumber numberWithDouble:value]];
      } else {
        [formatter setNumberStyle:NSNumberFormatterNoStyle];
        result = [formatter stringFromNumber:[NSNumber numberWithDouble:value]];
      }
      result = [result stringByReplacingOccurrencesOfString:@"\u00a0" withString:@" "];
      return [IOSCharArray arrayWithNSString:result];
    ]-*/;

    private static native char[] formatLong(Object nativeFormatter, long value,
        FieldPositionIterator iter) /*-[
      NSNumberFormatter *formatter = (NSNumberFormatter *) nativeFormatter;
      [formatter setAllowsFloats:NO];
      NSString *format = [formatter positiveFormat];
      NSString *result;
      if ([format hasPrefix:@"%"] || [format hasSuffix:@"%"]) {
        [formatter setNumberStyle:NSNumberFormatterPercentStyle];
        result = [formatter stringFromNumber:[NSNumber numberWithLongLong:value * 100.0]];
      } else if ([format hasPrefix:@"\u00A4"] || [format hasSuffix:@"\u00A4"]) {
        [formatter setNumberStyle:NSNumberFormatterCurrencyStyle];
        result = [formatter stringFromNumber:[NSNumber numberWithLongLong:value]];
      } else {
        [formatter setNumberStyle:NSNumberFormatterNoStyle];
        result = [formatter stringFromNumber:[NSNumber numberWithLongLong:value]];
      }
      result = [result stringByReplacingOccurrencesOfString:@"\u00a0" withString:@" "];
      return [IOSCharArray arrayWithNSString:result];
    ]-*/;

    private static native Number parse(Object nativeFormatter, String string,
        ParsePosition position, boolean parseBigDecimal) /*-[
      NSNumberFormatter *formatter = (NSNumberFormatter *) nativeFormatter;
      NSNumber *result;
      int start = [position getIndex];
      NSRange range = NSMakeRange(start, [string length] - start);

      // iOS bug: numbers that are zero digits only (like "0000") fail to parse
      // when allowsFloats is false, so parse that case separately.
      if (range.length > 0 && [formatter allowsFloats] == NO &&
          [string characterAtIndex:start] == '0') {
        BOOL onlyZeroes = YES;
        for (NSUInteger i = start + 1; i < range.length + start; i++) {
          if ([string characterAtIndex:i] != '0') {
            onlyZeroes = NO;
            break;
          }
        }
        if (onlyZeroes) {
          [position setIndexWithInt:start + (int) range.length];
          return JavaLangLong_valueOfWithLong_(0L);
        }
      }

      NSError *error = nil;
      BOOL success = [formatter getObjectValue:&result
                                     forString:string
                                         range:&range
                                         error:&error];
      if (success && (error == nil)) {
        [position setIndexWithInt:start + (int) range.length];
        if (fmod([result doubleValue], 1.0) == 0) {
          return JavaLangLong_valueOfWithLong_([result longLongValue]);
        } else {
          return JavaLangDouble_valueOfWithDouble_([result doubleValue]);
        }
      } else {
        [position setErrorIndexWithInt:start];
        return nil;
      }
    ]-*/;

    private static native String toPatternImpl(Object nativeFormatter, boolean localized) /*-[
      NSNumberFormatter *formatter = (NSNumberFormatter *) nativeFormatter;
      return [NSString stringWithFormat:@"%@;%@", [formatter positiveFormat],
              [formatter negativeFormat]];
    ]-*/;
}
