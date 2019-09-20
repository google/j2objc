/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2012-2014, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationRoot.java, ported from collationroot.h/.cpp
*
* C++ version created on: 2012dec17
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.MissingResourceException;

import android.icu.impl.ICUBinary;
import android.icu.impl.ICUData;

/**
 * Collation root provider.
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CollationRoot {  // purely static
    private static final CollationTailoring rootSingleton;
    private static final RuntimeException exception;

    public static final CollationTailoring getRoot() {
        if(exception != null) {
            throw exception;
        }
        return rootSingleton;
    }
    public static final CollationData getData() {
        CollationTailoring root = getRoot();
        return root.data;
    }
    static final CollationSettings getSettings() {
        CollationTailoring root = getRoot();
        return root.settings.readOnly();
    }

    static {  // Corresponds to C++ load() function.
        CollationTailoring t = null;
        RuntimeException e2 = null;
        try {
            ByteBuffer bytes = ICUBinary.getRequiredData("coll/ucadata.icu");
            CollationTailoring t2 = new CollationTailoring(null);
            CollationDataReader.read(null, bytes, t2);
            // Keep t=null until after the root data has been read completely.
            // Otherwise we would set a non-null root object if the data reader throws an exception.
            t = t2;
        } catch(IOException e) {
            e2 = new MissingResourceException(
                    "IOException while reading CLDR root data",
                    "CollationRoot", ICUData.ICU_BUNDLE + "/coll/ucadata.icu");
        } catch(RuntimeException e) {
            e2 = e;
        }
        rootSingleton = t;
        exception = e2;
    }
}
