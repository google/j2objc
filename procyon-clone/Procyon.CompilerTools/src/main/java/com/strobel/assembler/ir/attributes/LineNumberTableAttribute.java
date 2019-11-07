/*
 * LineNumberTableAttribute.java
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

import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;

import java.util.List;

/**
 * @author Mike Strobel
 */
public final class LineNumberTableAttribute extends SourceAttribute {
    private final List<LineNumberTableEntry> _entries;

    /**
     * records the highest offset number in 'this' line number table
     */
    private final int _maxOffset;

    public LineNumberTableAttribute(final LineNumberTableEntry[] entries) {
        super(AttributeNames.LineNumberTable, 2 + (VerifyArgument.notNull(entries, "entries").length * 4));

        _entries = ArrayUtilities.asUnmodifiableList(entries.clone());

        int max = Integer.MIN_VALUE;

        for (final LineNumberTableEntry entry : entries) {
            final int offset = entry.getOffset();

            if (offset > max) {
                max = offset;
            }
        }

        _maxOffset = max;
    }

    public List<LineNumberTableEntry> getEntries() {
        return _entries;
    }

    /**
     * Returns the maximum bytecode offset in 'this' table.
     */
    public int getMaxOffset() {
        return _maxOffset;
    }
}
