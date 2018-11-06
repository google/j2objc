/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
package android.icu.dev.test.normalizer;

import java.util.HashMap;
import java.util.Map;

/**
 *******************************************************************************
 * Copyright (C) 2002-2010, International Business Machines Corporation and    *
 * Unicode, Inc. All Rights Reserved.                                          *
 *******************************************************************************
 *
 * Hashtable storing ints addressed by longs. Used
 * for storing of composition data.
 * @author Vladimir Weinstein
 */
public class LongHashtable {

    public LongHashtable (int defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void put(long key, int value) {
        if (value == defaultValue) {
            table.remove(new Long(key));
        } else {
            table.put(new Long(key), new Integer(value));
        }
    }

    public int get(long key) {
        Integer value = table.get(new Long(key));
        if (value == null) return defaultValue;
        return value.intValue();
    }

    private int defaultValue;
    private Map<Long, Integer> table = new HashMap<Long, Integer>();

}
