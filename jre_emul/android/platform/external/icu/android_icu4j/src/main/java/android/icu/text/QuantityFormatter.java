/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2013-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.text.FieldPosition;

import android.icu.impl.SimpleFormatterImpl;
import android.icu.impl.StandardPlural;
import android.icu.text.PluralRules.FixedDecimal;

/**
 * QuantityFormatter represents an unknown quantity of something and formats a known quantity
 * in terms of that something. For example, a QuantityFormatter that represents X apples may
 * format 1 as "1 apple" and 3 as "3 apples" 
 * <p>
 * QuanitityFormatter appears here instead of in android.icu.impl because it depends on
 * PluralRules and DecimalFormat. It is package-protected as it is not meant for public use.
 */
class QuantityFormatter {
    private final SimpleFormatter[] templates =
            new SimpleFormatter[StandardPlural.COUNT];

    public QuantityFormatter() {}

    /**
     * Adds a template if there is none yet for the plural form.
     *
     * @param variant the plural variant, e.g "zero", "one", "two", "few", "many", "other"
     * @param template the text for that plural variant with "{0}" as the quantity. For
     * example, in English, the template for the "one" variant may be "{0} apple" while the
     * template for the "other" variant may be "{0} apples"
     * @throws IllegalArgumentException if variant is not recognized or
     *  if template has more than just the {0} placeholder.
     */
    public void addIfAbsent(CharSequence variant, String template) {
        int idx = StandardPlural.indexFromString(variant);
        if (templates[idx] != null) {
            return;
        }
        templates[idx] = SimpleFormatter.compileMinMaxArguments(template, 0, 1);
    }

    /**
     * @return true if this object has at least the "other" variant
     */
    public boolean isValid() {
        return templates[StandardPlural.OTHER_INDEX] != null;
    }

    /**
     * Format formats a number with this object.
     * @param number the number to be formatted
     * @param numberFormat used to actually format the number.
     * @param pluralRules uses the number and the numberFormat to determine what plural
     *  variant to use for fetching the formatting template.
     * @return the formatted string e.g '3 apples'
     */
    public String format(double number, NumberFormat numberFormat, PluralRules pluralRules) {
        String formatStr = numberFormat.format(number);
        StandardPlural p = selectPlural(number, numberFormat, pluralRules);
        SimpleFormatter formatter = templates[p.ordinal()];
        if (formatter == null) {
            formatter = templates[StandardPlural.OTHER_INDEX];
            assert formatter != null;
        }
        return formatter.format(formatStr);
    }

    /**
     * Gets the SimpleFormatter for a particular variant.
     * @param variant "zero", "one", "two", "few", "many", "other"
     * @return the SimpleFormatter
     */
    public SimpleFormatter getByVariant(CharSequence variant) {
        assert isValid();
        int idx = StandardPlural.indexOrOtherIndexFromString(variant);
        SimpleFormatter template = templates[idx];
        return (template == null && idx != StandardPlural.OTHER_INDEX) ?
                templates[StandardPlural.OTHER_INDEX] : template;
    }

    // The following methods live here so that class PluralRules does not depend on number formatting,
    // and the SimpleFormatter does not depend on FieldPosition.

    /**
     * Selects the standard plural form for the number/formatter/rules.
     */
    public static StandardPlural selectPlural(double number, NumberFormat numberFormat, PluralRules rules) {
        String pluralKeyword;
        if (numberFormat instanceof DecimalFormat) {
            pluralKeyword = rules.select(((DecimalFormat) numberFormat).getFixedDecimal(number));
        } else {
            pluralKeyword = rules.select(number);
        }
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    /**
     * Selects the standard plural form for the number/formatter/rules.
     */
    public static StandardPlural selectPlural(
            Number number, NumberFormat fmt, PluralRules rules,
            StringBuffer formattedNumber, FieldPosition pos) {
        UFieldPosition fpos = new UFieldPosition(pos.getFieldAttribute(), pos.getField());
        fmt.format(number, formattedNumber, fpos);
        // TODO: Long, BigDecimal & BigInteger may not fit into doubleValue().
        FixedDecimal fd = new FixedDecimal(
                number.doubleValue(),
                fpos.getCountVisibleFractionDigits(), fpos.getFractionDigits());
        String pluralKeyword = rules.select(fd);
        pos.setBeginIndex(fpos.getBeginIndex());
        pos.setEndIndex(fpos.getEndIndex());
        return StandardPlural.orOtherFromString(pluralKeyword);
    }

    /**
     * Formats the pattern with the value and adjusts the FieldPosition.
     */
    public static StringBuilder format(String compiledPattern, CharSequence value,
            StringBuilder appendTo, FieldPosition pos) {
        int[] offsets = new int[1];
        SimpleFormatterImpl.formatAndAppend(compiledPattern, appendTo, offsets, value);
        if (pos.getBeginIndex() != 0 || pos.getEndIndex() != 0) {
            if (offsets[0] >= 0) {
                pos.setBeginIndex(pos.getBeginIndex() + offsets[0]);
                pos.setEndIndex(pos.getEndIndex() + offsets[0]);
            } else {
                pos.setBeginIndex(0);
                pos.setEndIndex(0);
            }
        }
        return appendTo;
    }
}
