/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ********************************************************************************
 * Copyright (C) 2009-2010, Google, International Business Machines Corporation *
 * and others. All Rights Reserved.                                             *
 ********************************************************************************
 */
package android.icu.text;

/**
 * Provide an interface for Transforms that focuses just on the transformation of the text.
 * APIs that take Transliterator or StringTransform, but only depend on the transformation should use this interface in the API instead.
 *
 * @author markdavis
 * @hide Only a subset of ICU is exposed in Android

 */

public interface Transform<S,D> {
    /**
     * Transform the input in some way, to be determined by the subclass.
     * @param source to be transformed (eg lowercased)
     * @return result
     */
    public D transform(S source);
}
