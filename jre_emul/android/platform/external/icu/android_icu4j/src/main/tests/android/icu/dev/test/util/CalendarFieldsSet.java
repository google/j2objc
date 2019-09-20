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

import android.icu.util.Calendar;

/**
 * @author srl
 *
 */
public class CalendarFieldsSet extends FieldsSet {

    /**
     * @param whichEnum
     * @param fieldsCount
     */
    public CalendarFieldsSet() {
        super(DebugUtilitiesData.UCalendarDateFields,-1);
    }
    
    public boolean  matches(Calendar cal, CalendarFieldsSet diffSet) {
        boolean match = true;
        for(int i=0;i<fieldCount();i++) {
            if(isSet(i)) {
                int calVal = cal.get(i);
                if(calVal != get(i)) {
                    match = false;
                    diffSet.set(i, calVal);
                }
            }
        }
        return match;
    }

    /**
     * set the specified fields on this calendar. Doesn't clear first. Returns any errors the cale 
     */
    public void setOnCalendar(Calendar cal) {
        for(int i=0;i<fieldCount();i++) {
            if(isSet(i)) {
                cal.set(i, get(i));
            }
        }
    }

    protected void handleParseValue(FieldsSet inheritFrom, int field, String substr) {
        if(field == Calendar.MONTH) {
            parseValueEnum(DebugUtilitiesData.UCalendarMonths, inheritFrom, field, substr);
            // will fallback to default.
        } else {
            parseValueDefault(inheritFrom, field, substr);
        }
    }
}
