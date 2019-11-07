/*
 * BlobAttribute.java
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

import com.strobel.core.VerifyArgument;

/**
 * @author Mike Strobel
 */
public final class BlobAttribute extends SourceAttribute {
    private final int _dataOffset;
    private final byte[] _data;

    public BlobAttribute(final String name, final byte[] data) {
        this(name, data, -1);
    }

    public BlobAttribute(final String name, final byte[] data, final int dataOffset) {
        super(name, data.length);
        _dataOffset = dataOffset;
        _data = VerifyArgument.notNull(data, "data");
    }

    public int getDataOffset() {
        return _dataOffset;
    }

    public byte[] getData() {
        return _data;
    }
}
