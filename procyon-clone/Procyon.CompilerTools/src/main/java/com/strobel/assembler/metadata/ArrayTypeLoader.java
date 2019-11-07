/*
 * ArrayTypeLoader.java
 *
 * Copyright (c) 2014 Mike Strobel
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

package com.strobel.assembler.metadata;

import com.strobel.annotations.NotNull;
import com.strobel.assembler.ir.ConstantPool;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ArrayTypeLoader implements ITypeLoader {
    private final static Logger LOG = Logger.getLogger(ArrayTypeLoader.class.getSimpleName());

    private final Buffer _buffer;
    private Throwable _parseError;
    private boolean _parsed;
    private String _className;

    public ArrayTypeLoader(@NotNull final byte[] bytes) {
        VerifyArgument.notNull(bytes, "bytes");
        _buffer = new Buffer(Arrays.copyOf(bytes, bytes.length));
    }

    public String getClassNameFromArray() {
        ensureParsed(true);
        return _className;
    }

    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        ensureParsed(false);

        if (StringUtilities.equals(internalName, _className)) {
            buffer.reset(_buffer.size());
            buffer.putByteArray(_buffer.array(), 0, _buffer.size());
            buffer.position(0);
            return true;
        }

        return false;
    }

    private void ensureParsed(final boolean throwOnError) {
        if (_parsed) {
            if (throwOnError && _parseError != null) {
                throw new IllegalStateException("Error parsing classfile header.", _parseError);
            }
            return;
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "Parsing classfile header from user-provided buffer...");
        }

        try {
            _className = getInternalNameFromClassFile(_buffer);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Parsed header for class: " + _className);
            }
        }
        catch (final Throwable t) {
            _parseError = t;

            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Error parsing classfile header.", t);
            }

            if (throwOnError) {
                throw new IllegalStateException("Error parsing classfile header.", t);
            }
        }
        finally {
            _parsed = true;
        }
    }

    private static String getInternalNameFromClassFile(final Buffer b) {
        final long magic = b.readInt() & 0xFFFFFFFFL;

        if (magic != 0xCAFEBABEL) {
            throw new IllegalStateException("Bad magic number: 0x" + Long.toHexString(magic));
        }

        b.readUnsignedShort(); // minor version
        b.readUnsignedShort(); // major version

        final ConstantPool constantPool = ConstantPool.read(b);

        b.readUnsignedShort(); // access flags

        final ConstantPool.TypeInfoEntry thisClass = constantPool.getEntry(b.readUnsignedShort());

        b.position(0);

        return thisClass.getName();
    }
}
