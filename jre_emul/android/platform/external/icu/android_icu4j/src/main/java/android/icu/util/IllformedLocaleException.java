/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2012, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

/**
 * Thrown by methods in {@link ULocale} and {@link ULocale.Builder} to
 * indicate that an argument is not a well-formed BCP 47 tag.
 * 
 * @see ULocale
 * @hide Only a subset of ICU is exposed in Android
 */
public class IllformedLocaleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int _errIdx = -1;

    /**
     * Constructs a new <code>IllformedLocaleException</code> with no
     * detail message and -1 as the error index.
     */
    public IllformedLocaleException() {
        super();
    }

    /**
     * Constructs a new <code>IllformedLocaleException</code> with the
     * given message and -1 as the error index.
     *
     * @param message the message
     */
    public IllformedLocaleException(String message) {
        super(message);
    }

    /**
     * Constructs a new <code>IllformedLocaleException</code> with the
     * given message and the error index.  The error index is the approximate
     * offset from the start of the ill-formed value to the point where the
     * parse first detected an error.  A negative error index value indicates
     * either the error index is not applicable or unknown.
     *
     * @param message the message
     * @param errorIndex the index
     */
    public IllformedLocaleException(String message, int errorIndex) {
        super(message + ((errorIndex < 0) ? "" : " [at index " + errorIndex + "]"));
        _errIdx = errorIndex;
    }

    /**
     * Returns the index where the error was found. A negative value indicates
     * either the error index is not applicable or unknown.
     *
     * @return the error index
     */
    public int getErrorIndex() {
        return _errIdx;
    }
}
