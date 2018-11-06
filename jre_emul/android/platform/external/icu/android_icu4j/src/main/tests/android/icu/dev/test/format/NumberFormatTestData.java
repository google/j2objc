/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.format;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.icu.math.BigDecimal;
import android.icu.text.DecimalFormat;
import android.icu.text.NumberFormat;
import android.icu.util.Currency;
import android.icu.util.ULocale;

/**
 * A representation of a single NumberFormat specification test from a data driven test file.
 * <p>
 * The purpose of this class is to hide the details of the data driven test file from the
 * main testing code.
 * <p>
 * This class contains fields describing an attribute of the test that may or may
 * not be set. The name of each attribute corresponds to the name used in the
 * data driven test file.
 * <p>
 * <b>Adding new attributes</b>
 * <p>
 * Each attribute name is lower case. Moreover, for each attribute there is also a
 * setXXX method for that attribute that is used to initialize the attribute from a
 * String value read from the data file. For example, there is a setLocale(String) method
 * for the locale attribute and a setCurrency(String) method for the currency attribute.
 * In general, for an attribute named abcd, the setter will be setAbcd(String).
 * This naming rule must be strictly followed or else the test runner will not know how to
 * initialize instances of this class.
 * <p>
 * In addition each attribute is listed in the fieldOrdering static array which specifies
 * The order that attributes are printed whenever there is a test failure.
 * <p> 
 * To add a new attribute, first create a public field for it.
 * Next, add the attribute name to the fieldOrdering array.
 * Finally, create a setter method for it.
 * 
 * @author rocketman
 */
public class NumberFormatTestData {
    
    /**
     * The locale.
     */
    public ULocale locale = null;
    
    /**
     * The currency.
     */
    public Currency currency = null;
    
    /**
     * The pattern to initialize the formatter, for example 0.00"
     */
    public String pattern = null;
    
    /**
     * The value to format as a string. For example 1234.5 would be "1234.5"
     */
    public String format = null;
    
    /**
     * The formatted value.
     */
    public String output = null;
    
    /**
     * Field for arbitrary comments.
     */
    public String comment = null;
    
    public Integer minIntegerDigits = null;
    public Integer maxIntegerDigits = null;
    public Integer minFractionDigits = null;
    public Integer maxFractionDigits = null;
    public Integer minGroupingDigits = null;
    public Integer useSigDigits = null;
    public Integer minSigDigits = null;
    public Integer maxSigDigits = null;
    public Integer useGrouping = null;
    public Integer multiplier = null;
    public Double roundingIncrement = null;
    public Integer formatWidth = null;
    public String padCharacter = null;
    public Integer useScientific = null;
    public Integer grouping = null;
    public Integer grouping2 = null;
    public Integer roundingMode = null;
    public Currency.CurrencyUsage currencyUsage = null;
    public Integer minimumExponentDigits = null;
    public Integer exponentSignAlwaysShown = null;
    public Integer decimalSeparatorAlwaysShown = null;
    public Integer padPosition = null;
    public String positivePrefix = null;
    public String positiveSuffix = null;
    public String negativePrefix = null;
    public String negativeSuffix = null;
    public String localizedPattern = null;
    public String toPattern = null;
    public String toLocalizedPattern = null;
    public Integer style = null;
    public String parse = null;
    public Integer lenient = null;
    public String plural = null;
    public Integer parseIntegerOnly = null;
    public Integer decimalPatternMatchRequired = null;
    public Integer parseNoExponent = null;
    public String outputCurrency = null;
    
    
    
    /**
     * nothing or empty means that test ought to work for both C and JAVA;
     * "C" means test is known to fail in C. "J" means test is known to fail in JAVA.
     * "CJ" means test is known to fail for both languages.
     */
    public String breaks = null;
    
    private static Map<String, Integer> roundingModeMap =
            new HashMap<String, Integer>();
    
    static {
        roundingModeMap.put("ceiling", BigDecimal.ROUND_CEILING);
        roundingModeMap.put("floor", BigDecimal.ROUND_FLOOR);
        roundingModeMap.put("down", BigDecimal.ROUND_DOWN);
        roundingModeMap.put("up", BigDecimal.ROUND_UP);
        roundingModeMap.put("halfEven", BigDecimal.ROUND_HALF_EVEN);
        roundingModeMap.put("halfDown", BigDecimal.ROUND_HALF_DOWN);
        roundingModeMap.put("halfUp", BigDecimal.ROUND_HALF_UP);
        roundingModeMap.put("unnecessary", BigDecimal.ROUND_UNNECESSARY);
    }
    
    private static Map<String, Currency.CurrencyUsage> currencyUsageMap =
            new HashMap<String, Currency.CurrencyUsage>();
    
    static {
        currencyUsageMap.put("standard", Currency.CurrencyUsage.STANDARD);
        currencyUsageMap.put("cash", Currency.CurrencyUsage.CASH);
    }
    
    private static Map<String, Integer> padPositionMap =
            new HashMap<String, Integer>();
    
    static {
        // TODO: Fix so that it doesn't depend on DecimalFormat.
        padPositionMap.put("beforePrefix", DecimalFormat.PAD_BEFORE_PREFIX);
        padPositionMap.put("afterPrefix", DecimalFormat.PAD_AFTER_PREFIX);
        padPositionMap.put("beforeSuffix", DecimalFormat.PAD_BEFORE_SUFFIX);
        padPositionMap.put("afterSuffix", DecimalFormat.PAD_AFTER_SUFFIX);
    }
    
    private static Map<String, Integer> formatStyleMap =
            new HashMap<String, Integer>();
    
    static {
        formatStyleMap.put("decimal", NumberFormat.NUMBERSTYLE);
        formatStyleMap.put("currency", NumberFormat.CURRENCYSTYLE);
        formatStyleMap.put("percent", NumberFormat.PERCENTSTYLE);
        formatStyleMap.put("scientific", NumberFormat.SCIENTIFICSTYLE);
        formatStyleMap.put("currencyIso", NumberFormat.ISOCURRENCYSTYLE);
        formatStyleMap.put("currencyPlural", NumberFormat.PLURALCURRENCYSTYLE);
        formatStyleMap.put("currencyAccounting", NumberFormat.ACCOUNTINGCURRENCYSTYLE);
        formatStyleMap.put("cashCurrency", NumberFormat.CASHCURRENCYSTYLE);
    }
    
    // Add any new fields here. On test failures, fields are printed in the same order they
    // appear here.
    private static String[] fieldOrdering = {
        "locale",
        "currency",
        "pattern",
        "format",
        "output",
        "comment",
        "minIntegerDigits",
        "maxIntegerDigits",
        "minFractionDigits",
        "maxFractionDigits",
        "minGroupingDigits",
        "breaks",
        "useSigDigits",
        "minSigDigits",
        "maxSigDigits",
        "useGrouping",
        "multiplier",
        "roundingIncrement",
        "formatWidth",
        "padCharacter",
        "useScientific",
        "grouping",
        "grouping2",
        "roundingMode",
        "currencyUsage",
        "minimumExponentDigits",
        "exponentSignAlwaysShown",
        "decimalSeparatorAlwaysShown",
        "padPosition",
        "positivePrefix",
        "positiveSuffix",
        "negativePrefix",
        "negativeSuffix",
        "localizedPattern",
        "toPattern",
        "toLocalizedPattern",
        "style",
        "parse",
        "lenient",
        "plural",
        "parseIntegerOnly",
        "decimalPatternMatchRequired",
        "parseNoExponent",
        "outputCurrency"
    };
    
    static {
        HashSet<String> set = new HashSet<String>();
        for (String s : fieldOrdering) {
            if (!set.add(s)) {
                throw new ExceptionInInitializerError(s + "is a duplicate field.");    
            }
        }
    }
    
    private static <T> T fromString(Map<String, T> map, String key) {
        T value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Bad value: "+ key);
        }
        return value;
    }
    
    // start field setters.
    // add setter for each new field in this block.
    
    public void setLocale(String value) {
        locale = new ULocale(value);
    }
    
    public void setCurrency(String value) {
        currency = Currency.getInstance(value);
    }
    
    public void setPattern(String value) {
        pattern = value;
    }
    
    public void setFormat(String value) {
        format = value;
    }
    
    public void setOutput(String value) {
        output = value;
    }
    
    public void setComment(String value) {
        comment = value;
    }
    
    public void setMinIntegerDigits(String value) {
        minIntegerDigits = Integer.valueOf(value);
    }
    
    public void setMaxIntegerDigits(String value) {
        maxIntegerDigits = Integer.valueOf(value);
    }
    
    public void setMinFractionDigits(String value) {
        minFractionDigits = Integer.valueOf(value);
    }
    
    public void setMaxFractionDigits(String value) {
        maxFractionDigits = Integer.valueOf(value);
    }
    
    public void setMinGroupingDigits(String value) {
        minGroupingDigits = Integer.valueOf(value);
    }
    
    public void setBreaks(String value) {
        breaks = value;
    }
    
    public void setUseSigDigits(String value) {
        useSigDigits = Integer.valueOf(value);
    }
    
    public void setMinSigDigits(String value) {
        minSigDigits = Integer.valueOf(value);
    }
    
    public void setMaxSigDigits(String value) {
        maxSigDigits = Integer.valueOf(value);
    }
    
    public void setUseGrouping(String value) {
        useGrouping = Integer.valueOf(value);
    }
    
    public void setMultiplier(String value) {
        multiplier = Integer.valueOf(value);
    }
    
    public void setRoundingIncrement(String value) {
        roundingIncrement = Double.valueOf(value);
    }
    
    public void setFormatWidth(String value) {
        formatWidth = Integer.valueOf(value);
    }
    
    public void setPadCharacter(String value) {
        padCharacter = value;
    }
    
    public void setUseScientific(String value) {
        useScientific = Integer.valueOf(value);
    }
    
    public void setGrouping(String value) {
        grouping = Integer.valueOf(value);
    }
    
    public void setGrouping2(String value) {
        grouping2 = Integer.valueOf(value);
    }
    
    public void setRoundingMode(String value) {
        roundingMode = fromString(roundingModeMap, value);
    }
    
    public void setCurrencyUsage(String value) {
        currencyUsage = fromString(currencyUsageMap, value);
    }
    
    public void setMinimumExponentDigits(String value) {
        minimumExponentDigits = Integer.valueOf(value);
    }
    
    public void setExponentSignAlwaysShown(String value) {
        exponentSignAlwaysShown = Integer.valueOf(value);
    }
    
    public void setDecimalSeparatorAlwaysShown(String value) {
        decimalSeparatorAlwaysShown = Integer.valueOf(value);
    }
    
    public void setPadPosition(String value) {
        padPosition = fromString(padPositionMap, value);
    }
    
    public void setPositivePrefix(String value) {
        positivePrefix = value;
    }
    
    public void setPositiveSuffix(String value) {
        positiveSuffix = value;
    }
    
    public void setNegativePrefix(String value) {
        negativePrefix = value;
    }
    
    public void setNegativeSuffix(String value) {
        negativeSuffix = value;
    }
    
    public void setLocalizedPattern(String value) {
        localizedPattern = value;
    }
    
    public void setToPattern(String value) {
        toPattern = value;
    }
    
    public void setToLocalizedPattern(String value) {
        toLocalizedPattern = value;
    }
    
    public void setStyle(String value) {
        style = fromString(formatStyleMap, value);
    }
    
    public void setParse(String value) {
        parse = value;
    }
    
    public void setLenient(String value) {
        lenient = Integer.valueOf(value);
    }
    
    public void setPlural(String value) {
        plural = value;
    }
    
    public void setParseIntegerOnly(String value) {
        parseIntegerOnly = Integer.valueOf(value);
    }
    
    public void setDecimalPatternMatchRequired(String value) {
        decimalPatternMatchRequired = Integer.valueOf(value);
    }
    
    public void setParseNoExponent(String value) {
        parseNoExponent = Integer.valueOf(value);
    }
    
    public void setOutputCurrency(String value) {
        outputCurrency = value;
    }
    
    // end field setters.
    
    // start of field clearers
    // Add clear methods that can be set in one test and cleared
    // in the next i.e the breaks field.
    
    public void clearBreaks() {
        breaks = null;
    }
    
    public void clearUseGrouping() {
        useGrouping = null;
    }
    
    public void clearGrouping2() {
        grouping2 = null;
    }
    
    public void clearGrouping() {
        grouping = null;
    }
    
    public void clearMinGroupingDigits() {
        minGroupingDigits = null;
    }
    
    public void clearUseScientific() {
        useScientific = null;
    }
    
    public void clearDecimalSeparatorAlwaysShown() {
        decimalSeparatorAlwaysShown = null;
    }
    
    // end field clearers
    
    public void setField(String fieldName, String valueString)
            throws NoSuchMethodException {
        Method m = getClass().getMethod(
                fieldToSetter(fieldName), String.class);
        try {
            m.invoke(this, valueString);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void clearField(String fieldName)
            throws NoSuchMethodException {
        Method m = getClass().getMethod(fieldToClearer(fieldName));
        try {
            m.invoke(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        boolean first = true;
        for (String fieldName : fieldOrdering) {
            try {
                Field field = getClass().getField(fieldName);
                Object optionalValue = field.get(this);
                if (optionalValue == null) {
                    continue;
                }
                if (!first) {
                    result.append(", ");
                }
                first = false;
                result.append(fieldName);
                result.append(": ");
                result.append(optionalValue);
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            } catch (SecurityException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        result.append("}");
        return result.toString();
    }

    private static String fieldToSetter(String fieldName) {
        return "set"
                + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);
    }
    
    private static String fieldToClearer(String fieldName) {
        return "clear"
                + Character.toUpperCase(fieldName.charAt(0))
                + fieldName.substring(1);
    }

}
