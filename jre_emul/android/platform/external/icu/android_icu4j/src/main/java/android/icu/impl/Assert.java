/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2005-2006, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*/
package android.icu.impl;

// 1.3 compatibility layer
/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class Assert {
    public static void fail(Exception e) {
        fail(e.toString()); // can't wrap exceptions in jdk 1.3
    }
    public static void fail(String msg) {
        throw new IllegalStateException("failure '" + msg + "'");
    }
    public static void assrt(boolean val) {
        if (!val) throw new IllegalStateException("assert failed");
    }
    public static void assrt(String msg, boolean val) {
        if (!val) throw new IllegalStateException("assert '" + msg + "' failed");
    }
}
