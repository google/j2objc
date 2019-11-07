/*
 * ClasspathTypeLoader.java
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

package com.strobel.assembler.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mike Strobel
 */
public final class ClasspathTypeLoader implements ITypeLoader {
    private final static Logger LOG = Logger.getLogger(ClasspathTypeLoader.class.getSimpleName());

    private final ClassLoader _loader;

    public ClasspathTypeLoader() {
        _loader = ClassLoader.getSystemClassLoader();
    }

    //
    // Temporarily removing this constructor to get a Java 9 compatibility fix out quickly.
    // Hopefully nobody is using it.  Will restore once ClasspathTypeLoader can be fleshed
    // out to support arbitrary paths.
    //
//    public ClasspathTypeLoader(final String classPath) {
//        throw new UnsupportedOperationException("Custom classpaths are temporarily unsupported.");
//    }

    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Attempting to load type: " + internalName + "...");
        }

        final String path = internalName.concat(".class");
        final URL resource = _loader.getResource(path);

        if (resource == null) {
            return false;
        }

        try (final InputStream stream = _loader.getResourceAsStream(path)) {
            final byte[] temp = new byte[4096];

            int bytesRead;

            while ((bytesRead = stream.read(temp, 0, temp.length)) > 0) {
                buffer.ensureWriteableBytes(bytesRead);
                buffer.putByteArray(temp, 0, bytesRead);
            }

            buffer.flip();

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Type loaded from " + resource + ".");
            }

            return true;
        }
        catch (final IOException ignored) {
            return false;
        }
    }
}
