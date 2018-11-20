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
 * Integer-String hash table. Uses Java Hashtable for now.
 * @author Mark Davis
 */
 
public class IntStringHashtable {

    public IntStringHashtable (String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void put(int key, String value) {
        if (value == defaultValue) {
            table.remove(new Integer(key));
        } else {
            table.put(new Integer(key), value);
        }
    }

    public String get(int key) {
        String value = table.get(new Integer(key));
        if (value == null) return defaultValue;
        return value;
    }

    private String defaultValue;
    private Map<Integer, String> table = new HashMap<Integer, String>();
}