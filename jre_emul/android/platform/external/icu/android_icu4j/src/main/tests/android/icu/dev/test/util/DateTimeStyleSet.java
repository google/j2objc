/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.util;

import android.icu.text.DateFormat;

/**
 * @author srl
 *
 */
public class DateTimeStyleSet extends FieldsSet {

    private static final int DTS_DATE = 0;
    private static final String kDATE = "DATE";
    private static final int DTS_TIME = 1;
    private static final String kTIME = "TIME";
    private static final int DTS_COUNT = 2;
    
    private static final String kRELATIVE_ = "RELATIVE_";
    
    private int getOrNone(int which) {
        if(!isSet(which)) {
            return DateFormat.NONE;
        } else {
            return get(which);
        }
    }
    
    public DateTimeStyleSet() {
        super(FieldsSet.NO_ENUM, DTS_COUNT);
    }
    
    public int getDateStyle() {
        return getOrNone(DTS_DATE);
    }
    
    public int getTimeStyle() {
        return getOrNone(DTS_TIME);
    }
    
    protected void handleParseValue(FieldsSet inheritFrom, int field, String substr) {
        if(substr.startsWith(kRELATIVE_)) {
            parseValueEnum(DebugUtilitiesData.UDateFormatStyle, inheritFrom, field, substr.substring(kRELATIVE_.length()));
            if(isSet(field)) {
                set(field, get(field) | DateFormat.RELATIVE);
            }
        } else {
            parseValueEnum(DebugUtilitiesData.UDateFormatStyle, inheritFrom, field, substr);
        }
    }
    
    protected int handleParseName(FieldsSet inheritFrom, String name, String substr) {
        if(name.equals(kDATE)) {
            return DTS_DATE;
        } else if(name.equals(kTIME)) {
            return DTS_TIME;
        } else {
            throw new IllegalArgumentException("Bad field: " + name);
        }
    }
}
