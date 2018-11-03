/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2016, Google, International Business Machines Corporation and
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */

package android.icu.text;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.AttributedCharacterIterator;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import android.icu.text.CompactDecimalDataCache.Data;
import android.icu.text.PluralRules.FixedDecimal;
import android.icu.util.Currency;
import android.icu.util.CurrencyAmount;
import android.icu.util.Output;
import android.icu.util.ULocale;

/**
 * The CompactDecimalFormat produces abbreviated numbers, suitable for display in environments will limited real estate.
 * For example, 'Hits: 1.2B' instead of 'Hits: 1,200,000,000'. The format will be appropriate for the given language,
 * such as "1,2 Mrd." for German.
 * <p>
 * For numbers under 1000 trillion (under 10^15, such as 123,456,789,012,345), the result will be short for supported
 * languages. However, the result may sometimes exceed 7 characters, such as when there are combining marks or thin
 * characters. In such cases, the visual width in fonts should still be short.
 * <p>
 * By default, there are 2 significant digits. After creation, if more than three significant digits are set (with
 * setMaximumSignificantDigits), or if a fixed number of digits are set (with setMaximumIntegerDigits or
 * setMaximumFractionDigits), then result may be wider.
 * <p>
 * The "short" style is also capable of formatting currency amounts, such as "$1.2M" instead of "$1,200,000.00" (English) or
 * "5,3 Mio. €" instead of "5.300.000,00 €" (German).  Localized data concerning longer formats is not available yet in
 * the Unicode CLDR. Because of this, attempting to format a currency amount using the "long" style will produce
 * an UnsupportedOperationException.
 *
 * At this time, negative numbers and parsing are not supported, and will produce an UnsupportedOperationException.
 * Resetting the pattern prefixes or suffixes is not supported; the method calls are ignored.
 * <p>
 * Note that important methods, like setting the number of decimals, will be moved up from DecimalFormat to
 * NumberFormat.
 *
 * @author markdavis
 */
public class CompactDecimalFormat extends DecimalFormat {

    private static final long serialVersionUID = 4716293295276629682L;

//    private static final int POSITIVE_PREFIX = 0, POSITIVE_SUFFIX = 1, AFFIX_SIZE = 2;
    private static final CompactDecimalDataCache cache = new CompactDecimalDataCache();

    private final Map<String, DecimalFormat.Unit[]> units;
    private final Map<String, DecimalFormat.Unit[]> currencyUnits;
    private final long[] divisor;
    private final long[] currencyDivisor;
    private final Map<String, Unit> pluralToCurrencyAffixes;
    private CompactStyle style;

    // null if created internally using explicit prefixes and suffixes.
    private final PluralRules pluralRules;

    /**
     * Style parameter for CompactDecimalFormat.
     */
    public enum CompactStyle {
        /**
         * Short version, like "1.2T"
         */
        SHORT,
        /**
         * Longer version, like "1.2 trillion", if available. May return same result as SHORT if not.
         */
        LONG
    }

    /**
     * Create a CompactDecimalFormat appropriate for a locale. The result may
     * be affected by the number system in the locale, such as ar-u-nu-latn.
     *
     * @param locale the desired locale
     * @param style the compact style
     */
    public static CompactDecimalFormat getInstance(ULocale locale, CompactStyle style) {
        return new CompactDecimalFormat(locale, style);
    }

    /**
     * Create a CompactDecimalFormat appropriate for a locale. The result may
     * be affected by the number system in the locale, such as ar-u-nu-latn.
     *
     * @param locale the desired locale
     * @param style the compact style
     */
    public static CompactDecimalFormat getInstance(Locale locale, CompactStyle style) {
        return new CompactDecimalFormat(ULocale.forLocale(locale), style);
    }

    /**
     * The public mechanism is CompactDecimalFormat.getInstance().
     *
     * @param locale
     *            the desired locale
     * @param style
     *            the compact style
     */
    CompactDecimalFormat(ULocale locale, CompactStyle style) {
        this.pluralRules = PluralRules.forLocale(locale);
        DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(locale);
        CompactDecimalDataCache.Data data = getData(locale, style);
        CompactDecimalDataCache.Data currencyData = getCurrencyData(locale);
        this.units = data.units;
        this.divisor = data.divisors;
        this.currencyUnits = currencyData.units;
        this.currencyDivisor = currencyData.divisors;
        this.style = style;
        pluralToCurrencyAffixes = null;

//        DecimalFormat currencyFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(locale);
//        // TODO fix to use plural-dependent affixes
//        Unit currency = new Unit(currencyFormat.getPositivePrefix(), currencyFormat.getPositiveSuffix());
//        pluralToCurrencyAffixes = new HashMap<String,Unit>();
//        for (String key : pluralRules.getKeywords()) {
//            pluralToCurrencyAffixes.put(key, currency);
//        }
//        // TODO fix to get right symbol for the count

        finishInit(style, format.toPattern(), format.getDecimalFormatSymbols());
    }

    /**
     * Create a short number "from scratch". Intended for internal use. The prefix, suffix, and divisor arrays are
     * parallel, and provide the information for each power of 10. When formatting a value, the correct power of 10 is
     * found, then the value is divided by the divisor, and the prefix and suffix are set (using
     * setPositivePrefix/Suffix).
     *
     * @param pattern
     *            A number format pattern. Note that the prefix and suffix are discarded, and the decimals are
     *            overridden by default.
     * @param formatSymbols
     *            Decimal format symbols, typically from a locale.
     * @param style
     *            compact style.
     * @param divisor
     *            An array of prefix values, one for each power of 10 from 0 to 14
     * @param pluralAffixes
     *            A map from plural categories to affixes.
     * @param currencyAffixes
     *            A map from plural categories to currency affixes.
     * @param debugCreationErrors
     *            A collection of strings for debugging. If null on input, then any errors found will be added to that
     *            collection instead of throwing exceptions.
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public CompactDecimalFormat(String pattern, DecimalFormatSymbols formatSymbols,
            CompactStyle style, PluralRules pluralRules,
            long[] divisor, Map<String,String[][]> pluralAffixes, Map<String, String[]> currencyAffixes,
            Collection<String> debugCreationErrors) {

        this.pluralRules = pluralRules;
        this.units = otherPluralVariant(pluralAffixes, divisor, debugCreationErrors);
        this.currencyUnits = otherPluralVariant(pluralAffixes, divisor, debugCreationErrors);
        if (!pluralRules.getKeywords().equals(this.units.keySet())) {
            debugCreationErrors.add("Missmatch in pluralCategories, should be: " + pluralRules.getKeywords() + ", was actually " + this.units.keySet());
        }
        this.divisor = divisor.clone();
        this.currencyDivisor = divisor.clone();
        if (currencyAffixes == null) {
            pluralToCurrencyAffixes = null;
        } else {
            pluralToCurrencyAffixes = new HashMap<String,Unit>();
            for (Entry<String, String[]> s : currencyAffixes.entrySet()) {
                String[] pair = s.getValue();
                pluralToCurrencyAffixes.put(s.getKey(), new Unit(pair[0], pair[1]));
            }
        }
        finishInit(style, pattern, formatSymbols);
    }

    private void finishInit(CompactStyle style, String pattern, DecimalFormatSymbols formatSymbols) {
        applyPattern(pattern);
        setDecimalFormatSymbols(formatSymbols);
        setMaximumSignificantDigits(2); // default significant digits
        setSignificantDigitsUsed(true);
        if (style == CompactStyle.SHORT) {
            setGroupingUsed(false);
        }
        setCurrency(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!super.equals(obj))
            return false; // super does class check
        CompactDecimalFormat other = (CompactDecimalFormat) obj;
        return mapsAreEqual(units, other.units)
                && Arrays.equals(divisor, other.divisor)
                && (pluralToCurrencyAffixes == other.pluralToCurrencyAffixes
                || pluralToCurrencyAffixes != null && pluralToCurrencyAffixes.equals(other.pluralToCurrencyAffixes))
                && pluralRules.equals(other.pluralRules);
    }

    private boolean mapsAreEqual(
            Map<String, DecimalFormat.Unit[]> lhs, Map<String, DecimalFormat.Unit[]> rhs) {
        if (lhs.size() != rhs.size()) {
            return false;
        }
        // For each MapEntry in lhs, see if there is a matching one in rhs.
        for (Map.Entry<String, DecimalFormat.Unit[]> entry : lhs.entrySet()) {
            DecimalFormat.Unit[] value = rhs.get(entry.getKey());
            if (value == null || !Arrays.equals(entry.getValue(), value)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number, null, toAppendTo, pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        if (!(obj instanceof Number)) {
            throw new IllegalArgumentException();
        }
        Number number = (Number) obj;
        Amount amount = toAmount(number.doubleValue(), null, null);
        return super.formatToCharacterIterator(amount.getQty(), amount.getUnit());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        return format((double) number, toAppendTo, pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer format(BigInteger number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number.doubleValue(), toAppendTo, pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer format(BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number.doubleValue(), toAppendTo, pos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringBuffer format(android.icu.math.BigDecimal number, StringBuffer toAppendTo, FieldPosition pos) {
        return format(number.doubleValue(), toAppendTo, pos);
    }
    /**
     * {@inheritDoc}
     * @deprecated This API might change or be removed in a future release.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    @Deprecated
    public StringBuffer format(CurrencyAmount currAmt, StringBuffer toAppendTo, FieldPosition pos) {
        return format(currAmt.getNumber().doubleValue(), currAmt.getCurrency(), toAppendTo, pos);
    }

    /**
     * Parsing is currently unsupported, and throws an UnsupportedOperationException.
     */
    @Override
    public Number parse(String text, ParsePosition parsePosition) {
        throw new UnsupportedOperationException();
    }

    // DISALLOW Serialization, at least while draft

    private void writeObject(ObjectOutputStream out) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new NotSerializableException();
    }

    /* INTERNALS */
    private StringBuffer format(double number, Currency curr, StringBuffer toAppendTo, FieldPosition pos) {
        if (curr != null && style == CompactStyle.LONG) {
            throw new UnsupportedOperationException("CompactDecimalFormat does not support LONG style for currency.");
        }

        // Compute the scaled amount, prefix, and suffix appropriate for the number's magnitude.
        Output<Unit> currencyUnit = new Output<Unit>();
        Amount amount = toAmount(number, curr, currencyUnit);
        Unit unit = amount.getUnit();

        // Note that currencyUnit is a remnant.  In almost all cases, it will be null.
        StringBuffer prefix = new StringBuffer();
        StringBuffer suffix = new StringBuffer();
        if (currencyUnit.value != null) {
            currencyUnit.value.writePrefix(prefix);
        }
        unit.writePrefix(prefix);
        unit.writeSuffix(suffix);
        if (currencyUnit.value != null) {
            currencyUnit.value.writeSuffix(suffix);
        }

        if (curr == null) {
            // Prevent locking when not formatting a currency number.
            toAppendTo.append(escape(prefix.toString()));
            super.format(amount.getQty(), toAppendTo, pos);
            toAppendTo.append(escape(suffix.toString()));

        } else {
            // To perform the formatting, we set this DecimalFormat's pattern to have the correct prefix, suffix,
            // and currency, and then reset it back to what it was before.
            // This has to be synchronized since this information is held in the state of the DecimalFormat object.
            synchronized(this) {

                String originalPattern = this.toPattern();
                Currency originalCurrency = this.getCurrency();
                StringBuffer newPattern = new StringBuffer();

                // Write prefixes and suffixes to the pattern.  Note that we have to apply it to both halves of a
                // positive/negative format (separated by ';')
                int semicolonPos = originalPattern.indexOf(';');
                newPattern.append(prefix);
                if (semicolonPos != -1) {
                    newPattern.append(originalPattern, 0, semicolonPos);
                    newPattern.append(suffix);
                    newPattern.append(';');
                    newPattern.append(prefix);
                }
                newPattern.append(originalPattern, semicolonPos + 1, originalPattern.length());
                newPattern.append(suffix);

                // Overwrite the pattern and currency.
                setCurrency(curr);
                applyPattern(newPattern.toString());

                // Actually perform the formatting.
                super.format(amount.getQty(), toAppendTo, pos);

                // Reset the pattern and currency.
                setCurrency(originalCurrency);
                applyPattern(originalPattern);
            }
        }
        return toAppendTo;
    }

    private static final Pattern UNESCAPE_QUOTE = Pattern.compile("((?<!'))'");

    private static String escape(String string) {
        if (string.indexOf('\'') >= 0) {
            return UNESCAPE_QUOTE.matcher(string).replaceAll("$1");
        }
        return string;
    }

    private Amount toAmount(double number, Currency curr, Output<Unit> currencyUnit) {
        // We do this here so that the prefix or suffix we choose is always consistent
        // with the rounding we do. This way, 999999 -> 1M instead of 1000K.
        boolean negative = isNumberNegative(number);
        number = adjustNumberAsInFormatting(number);
        int base = number <= 1.0d ? 0 : (int) Math.log10(number);
        if (base >= CompactDecimalDataCache.MAX_DIGITS) {
            base = CompactDecimalDataCache.MAX_DIGITS - 1;
        }
        if (curr != null) {
            number /= currencyDivisor[base];
        } else {
            number /= divisor[base];
        }
        String pluralVariant = getPluralForm(getFixedDecimal(number, toDigitList(number)));
        if (pluralToCurrencyAffixes != null && currencyUnit != null) {
            currencyUnit.value = pluralToCurrencyAffixes.get(pluralVariant);
        }
        if (negative) {
            number = -number;
        }
        if ( curr != null ) {
            return new Amount(number, CompactDecimalDataCache.getUnit(currencyUnits, pluralVariant, base));
        } else {
            return new Amount(number, CompactDecimalDataCache.getUnit(units, pluralVariant, base));
        }
    }

    private void recordError(Collection<String> creationErrors, String errorMessage) {
        if (creationErrors == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        creationErrors.add(errorMessage);
    }

    /**
     * Manufacture the unit list from arrays
     */
    private Map<String, DecimalFormat.Unit[]> otherPluralVariant(Map<String, String[][]> pluralCategoryToPower10ToAffix,
            long[] divisor, Collection<String> debugCreationErrors) {

        // check for bad divisors
        if (divisor.length < CompactDecimalDataCache.MAX_DIGITS) {
            recordError(debugCreationErrors, "Must have at least " + CompactDecimalDataCache.MAX_DIGITS + " prefix items.");
        }
        long oldDivisor = 0;
        for (int i = 0; i < divisor.length; ++i) {

            // divisor must be a power of 10, and must be less than or equal to 10^i
            int log = (int) Math.log10(divisor[i]);
            if (log > i) {
                recordError(debugCreationErrors, "Divisor[" + i + "] must be less than or equal to 10^" + i
                        + ", but is: " + divisor[i]);
            }
            long roundTrip = (long) Math.pow(10.0d, log);
            if (roundTrip != divisor[i]) {
                recordError(debugCreationErrors, "Divisor[" + i + "] must be a power of 10, but is: " + divisor[i]);
            }

            if (divisor[i] < oldDivisor) {
                recordError(debugCreationErrors, "Bad divisor, the divisor for 10E" + i + "(" + divisor[i]
                        + ") is less than the divisor for the divisor for 10E" + (i - 1) + "(" + oldDivisor + ")");
            }
            oldDivisor = divisor[i];
        }

        Map<String, DecimalFormat.Unit[]> result = new HashMap<String, DecimalFormat.Unit[]>();
        Map<String,Integer> seen = new HashMap<String,Integer>();

        String[][] defaultPower10ToAffix = pluralCategoryToPower10ToAffix.get("other");

        for (Entry<String, String[][]> pluralCategoryAndPower10ToAffix : pluralCategoryToPower10ToAffix.entrySet()) {
            String pluralCategory = pluralCategoryAndPower10ToAffix.getKey();
            String[][] power10ToAffix = pluralCategoryAndPower10ToAffix.getValue();

            // we can't have one of the arrays be of different length
            if (power10ToAffix.length != divisor.length) {
                recordError(debugCreationErrors, "Prefixes & suffixes must be present for all divisors " + pluralCategory);
            }
            DecimalFormat.Unit[] units = new DecimalFormat.Unit[power10ToAffix.length];
            for (int i = 0; i < power10ToAffix.length; i++) {
                String[] pair = power10ToAffix[i];
                if (pair == null) {
                    pair = defaultPower10ToAffix[i];
                }

                // we can't have bad pair
                if (pair.length != 2 || pair[0] == null || pair[1] == null) {
                    recordError(debugCreationErrors, "Prefix or suffix is null for " + pluralCategory + ", " + i + ", " + Arrays.asList(pair));
                    continue;
                }

                // we can't have two different indexes with the same display
                int log = (int) Math.log10(divisor[i]);
                String key = pair[0] + "\uFFFF" + pair[1] + "\uFFFF" + (i - log);
                Integer old = seen.get(key);
                if (old == null) {
                    seen.put(key, i);
                } else if (old != i) {
                    recordError(debugCreationErrors, "Collision between values for " + i + " and " + old
                            + " for [prefix/suffix/index-log(divisor)" + key.replace('\uFFFF', ';'));
                }

                units[i] = new Unit(pair[0], pair[1]);
            }
            result.put(pluralCategory, units);
        }
        return result;
    }

    private String getPluralForm(FixedDecimal fixedDecimal) {
        if (pluralRules == null) {
            return CompactDecimalDataCache.OTHER;
        }
        return pluralRules.select(fixedDecimal);
    }

    /**
     * Gets the data for a particular locale and style. If style is unrecognized,
     * we just return data for CompactStyle.SHORT.
     * @param locale The locale.
     * @param style The style.
     * @return The data which must not be modified.
     */
    private Data getData(ULocale locale, CompactStyle style) {
        CompactDecimalDataCache.DataBundle bundle = cache.get(locale);
        switch (style) {
        case SHORT:
            return bundle.shortData;
        case LONG:
            return bundle.longData;
        default:
            return bundle.shortData;
        }
    }
    /**
     * Gets the currency data for a particular locale.
     * Currently only short currency format is supported, since that is
     * the only form in CLDR.
     * @param locale The locale.
     * @return The data which must not be modified.
     */
    private Data getCurrencyData(ULocale locale) {
        CompactDecimalDataCache.DataBundle bundle = cache.get(locale);
            return bundle.shortCurrencyData;
    }

    private static class Amount {
        private final double qty;
        private final Unit unit;

        public Amount(double qty, Unit unit) {
            this.qty = qty;
            this.unit = unit;
        }

        public double getQty() {
            return qty;
        }

        public Unit getUnit() {
            return unit;
        }
    }
}
