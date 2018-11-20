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

/**
 * @author srl
 *
 */

public class DebugUtilities {
    
    /**
     * Count enum types
     * @return the number of enum types available, starting at 0
     */
    public static int typeCount() {
        return DebugUtilitiesData.TYPES.length;
    }
    
    /**
     * Fetch the name of a particular type of enum
     * @param type the enum type
     * @return the name of the enum
     */
    public static String typeString(int type) {
        return enumString(DebugUtilitiesData.UDebugEnumType, type);
    }

    /**
     * Count the number of available enum values for an item, from 0
     * @param type which enum to look up, such as DebugUtilitiesData.UCalendarDateFields
     * @return the number of available enum values
     */
    public static int enumCount(int type) {
        return DebugUtilitiesData.NAMES[type].length;
    }
    
    /**
     * Fetch the name of an enum
     * @param type which enum to look up, such as DebugUtilitiesData.UCalendarDateFields
     * @param field which enum value to look up
     * @return the found name. Will throw an exception on out of bounds.
     */
    public static String enumString(int type, int field) {
        return DebugUtilitiesData.NAMES[type][field];
    }
    
    /**
     * Lookup an enum by string
     * @param type which enum to look up, such as DebugUtilitiesData.UCalendarDateFields
     * @param string the string to search for
     * @return the found enum value, or -1 if not found
     */
    public static int enumByString(int type, String string) {
        for(int j=0;j<DebugUtilitiesData.NAMES[type].length;j++) {
            if(string.equals(DebugUtilitiesData.NAMES[type][j])) {
                return j;
            }
        }
        return -1;
    }
    
    /**
     * for consistency checking
     * @param type the type of enum
     * @return the expected ordinal value (should be equal to "field")
     * @internal
     */
    public static int enumArrayValue(int type, int field) {
        return DebugUtilitiesData.VALUES[type][field];
    }

}
