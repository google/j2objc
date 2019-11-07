/*
 * InnerClassEntry.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.Flags;

/**
 * @author Mike Strobel
 */
public final class InnerClassEntry {
    private final String _innerClassName;
    private final String _outerClassName;
    private final String _shortName;
    private final int _accessFlags;

    public InnerClassEntry(final String innerClassName, final String outerClassName, final String shortName, final int accessFlags) {
        _innerClassName = innerClassName;
        _outerClassName = outerClassName;
        _shortName = shortName;
        _accessFlags = accessFlags;
    }

    public String getInnerClassName() {
        return _innerClassName;
    }

    public String getOuterClassName() {
        return _outerClassName;
    }

    public String getShortName() {
        return _shortName;
    }

    public int getAccessFlags() {
        return _accessFlags;
    }

    @Override
    public String toString() {
        return "InnerClassEntry{" +
               "InnerClassName='" + _innerClassName + '\'' +
               ", OuterClassName='" + _outerClassName + '\'' +
               ", ShortName='" + _shortName + '\'' +
               ", AccessFlags=[" + Flags.toString(_accessFlags) +
               "]}";
    }
}
