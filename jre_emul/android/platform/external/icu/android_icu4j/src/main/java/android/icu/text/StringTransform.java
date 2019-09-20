/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2009, Google, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

/**
 * Provide a base class for Transforms that focuses just on the transformation of the text. APIs that take Transliterator, but only depend on the text transformation should use this interface in the API instead.
 *
 * @author markdavis
 * @hide Only a subset of ICU is exposed in Android
 *
 */
public interface StringTransform extends Transform<String,String> {
    /**
     * Transform the text in some way, to be determined by the subclass.
     * @param source text to be transformed (eg lowercased)
     * @return result
     */
    @Override
    public String transform(String source);
}