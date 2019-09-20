/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Standard CLDR plural form/category constants.
 * See http://www.unicode.org/reports/tr35/tr35-numbers.html#Language_Plural_Rules
 * @hide Only a subset of ICU is exposed in Android
 */
public enum StandardPlural {
    ZERO("zero"),
    ONE("one"),
    TWO("two"),
    FEW("few"),
    MANY("many"),
    OTHER("other");

    /**
     * Numeric index of OTHER, same as OTHER.ordinal().
     */
    public static final int OTHER_INDEX = OTHER.ordinal();

    /**
     * Unmodifiable List of all standard plural form constants.
     * List version of {@link #values()}.
     */
    public static final List<StandardPlural> VALUES =
            Collections.unmodifiableList(Arrays.asList(values()));

    /**
     * Number of standard plural forms/categories.
     */
    public static final int COUNT = VALUES.size();

    private final String keyword;

    private StandardPlural(String kw) {
        keyword = kw;
    }

    /**
     * @return the lowercase CLDR keyword string for the plural form
     */
    public final String getKeyword() {
        return keyword;
    }

    /**
     * @param keyword for example "few" or "other"
     * @return the plural form corresponding to the keyword, or null
     */
    public static final StandardPlural orNullFromString(CharSequence keyword) {
        switch (keyword.length()) {
        case 3:
            if ("one".contentEquals(keyword)) {
                return ONE;
            } else if ("two".contentEquals(keyword)) {
                return TWO;
            } else if ("few".contentEquals(keyword)) {
                return FEW;
            }
            break;
        case 4:
            if ("many".contentEquals(keyword)) {
                return MANY;
            } else if ("zero".contentEquals(keyword)) {
                return ZERO;
            }
            break;
        case 5:
            if ("other".contentEquals(keyword)) {
                return OTHER;
            }
            break;
        default:
            break;
        }
        return null;
    }

    /**
     * @param keyword for example "few" or "other"
     * @return the plural form corresponding to the keyword, or OTHER
     */
    public static final StandardPlural orOtherFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p : OTHER;
    }

    /**
     * @param keyword for example "few" or "other"
     * @return the plural form corresponding to the keyword
     * @throws IllegalArgumentException if the keyword is not a plural form
     */
    public static final StandardPlural fromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p;
        } else {
            throw new IllegalArgumentException(keyword.toString());
        }
    }

    /**
     * @param keyword for example "few" or "other"
     * @return the index of the plural form corresponding to the keyword, or a negative value
     */
    public static final int indexOrNegativeFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : -1;
    }

    /**
     * @param keyword for example "few" or "other"
     * @return the index of the plural form corresponding to the keyword, or OTHER_INDEX
     */
    public static final int indexOrOtherIndexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : OTHER.ordinal();
    }

    /**
     * @param keyword for example "few" or "other"
     * @return the index of the plural form corresponding to the keyword
     * @throws IllegalArgumentException if the keyword is not a plural form
     */
    public static final int indexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p.ordinal();
        } else {
            throw new IllegalArgumentException(keyword.toString());
        }
    }
}