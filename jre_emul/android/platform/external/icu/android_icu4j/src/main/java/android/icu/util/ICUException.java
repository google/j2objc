/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.util;

/**
 * Base class for unchecked, ICU-specific exceptions.
 *
 * @hide Only a subset of ICU is exposed in Android
 */
public class ICUException extends RuntimeException {
    private static final long serialVersionUID = -3067399656455755650L;

    /**
     * Default constructor.
     */
    public ICUException() {
    }

    /**
     * Constructor.
     *
     * @param message exception message string
     */
    public ICUException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause original exception
     */
    public ICUException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message exception message string
     * @param cause original exception
     */
    public ICUException(String message, Throwable cause) {
        super(message, cause);
    }
}
