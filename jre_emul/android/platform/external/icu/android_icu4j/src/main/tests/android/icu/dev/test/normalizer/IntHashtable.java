/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.dev.test.normalizer;

import java.util.HashMap;
import java.util.Map;


/**
 *******************************************************************************
 * Copyright (C) 1998-2010, International Business Machines Corporation and    *
 * Unicode, Inc. All Rights Reserved.                                          *
 *******************************************************************************
 *
 * Integer hash table.
 * @author Mark Davis
 */
 
public class IntHashtable {

    public IntHashtable (int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void put(int key, int value) {
        if (value == defaultValue) {
            table.remove(new Integer(key));
        } else {
            table.put(new Integer(key), new Integer(value));
        }
    }

    public int get(int key) {
        Integer value = table.get(new Integer(key));
        if (value == null) return defaultValue;
        return value.intValue();
    }

    private int defaultValue;
    private Map<Integer, Integer> table = new HashMap<Integer, Integer>();
}