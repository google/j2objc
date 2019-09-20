/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2010, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl.locale;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public class ParseStatus {
    int _parseLength = 0;
    int _errorIndex = -1;
    String _errorMsg = null;

    public void reset() {
        _parseLength = 0;
        _errorIndex = -1;
        _errorMsg = null;
    }

    public boolean isError() {
        return (_errorIndex >= 0);
    }

    public int getErrorIndex() {
        return _errorIndex;
    }

    public int getParseLength() {
        return _parseLength;
    }

    public String getErrorMessage() {
        return _errorMsg;
    }
}
