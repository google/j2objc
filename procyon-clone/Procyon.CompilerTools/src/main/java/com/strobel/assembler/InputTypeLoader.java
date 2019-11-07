/*
 * InputTypeLoader.java
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

package com.strobel.assembler;

import com.strobel.assembler.ir.ConstantPool;
import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.ClasspathTypeLoader;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.core.StringComparison;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.io.PathHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InputTypeLoader implements ITypeLoader {
    private final static Logger LOG = Logger.getLogger(InputTypeLoader.class.getSimpleName());

    private final ITypeLoader _defaultTypeLoader;
    private final Map<String, LinkedHashSet<File>> _packageLocations;
    private final Map<String, File> _knownFiles;

    public InputTypeLoader() {
        this(new ClasspathTypeLoader());
    }

    public InputTypeLoader(final ITypeLoader defaultTypeLoader) {
        _defaultTypeLoader = VerifyArgument.notNull(defaultTypeLoader, "defaultTypeLoader");
        _packageLocations = new LinkedHashMap<>();
        _knownFiles = new LinkedHashMap<>();
    }

    @Override
    public boolean tryLoadType(final String typeNameOrPath, final Buffer buffer) {
        VerifyArgument.notNull(typeNameOrPath, "typeNameOrPath");
        VerifyArgument.notNull(buffer, "buffer");

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Attempting to load type: " + typeNameOrPath + "...");
        }

        final boolean hasExtension = StringUtilities.endsWithIgnoreCase(typeNameOrPath, ".class");

        if (hasExtension && tryLoadFile(null, typeNameOrPath, buffer, true)) {
            return true;
        }

        if (PathHelper.isPathRooted(typeNameOrPath)) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Failed to load type: " + typeNameOrPath + ".");
            }
            return false;
        }

        String internalName = (hasExtension ? typeNameOrPath.substring(0, typeNameOrPath.length() - 6)
                                            : typeNameOrPath.replace('.', '/'));

        if (tryLoadTypeFromName(internalName, buffer)) {
            return true;
        }

        if (hasExtension) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Failed to load type: " + typeNameOrPath + ".");
            }
            return false;
        }

        //
        // See if it is an inner class by replacing the name delimiters with '$',
        // starting from the right...
        //

        for (int lastDelimiter = internalName.lastIndexOf('/');
             lastDelimiter != -1;
             lastDelimiter = internalName.lastIndexOf('/')) {

            internalName = internalName.substring(0, lastDelimiter) + "$" +
                           internalName.substring(lastDelimiter + 1);

            if (tryLoadTypeFromName(internalName, buffer)) {
                return true;
            }
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Failed to load type: " + typeNameOrPath + ".");
        }

        return false;
    }

    private boolean tryLoadTypeFromName(final String internalName, final Buffer buffer) {
        if (tryLoadFromKnownLocation(internalName, buffer)) {
            return true;
        }

        if (_defaultTypeLoader.tryLoadType(internalName, buffer)) {
            return true;
        }

        final String filePath = internalName.replace('/', File.separatorChar) + ".class";

        if (tryLoadFile(internalName, filePath, buffer, false)) {
            return true;
        }

        final int lastSeparatorIndex = filePath.lastIndexOf(File.separatorChar);

        return lastSeparatorIndex >= 0 &&
               tryLoadFile(internalName, filePath.substring(lastSeparatorIndex + 1), buffer, true);
    }

    private boolean tryLoadFromKnownLocation(final String internalName, final Buffer buffer) {
        final File knownFile = _knownFiles.get(internalName);

        if (knownFile != null && tryLoadFile(knownFile, buffer)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Type loaded from " + knownFile.getAbsolutePath() + ".");
            }
            return true;
        }

        final int packageEnd = internalName.lastIndexOf('/');

        String head;
        String tail;

        if (packageEnd < 0 || packageEnd >= internalName.length()) {
            head = StringUtilities.EMPTY;
            tail = internalName;
        }
        else {
            head = internalName.substring(0, packageEnd);
            tail = internalName.substring(packageEnd + 1);
        }

        while (true) {
            final LinkedHashSet<File> directories = _packageLocations.get(head);

            if (directories != null) {
                for (final File directory : directories) {
                    if (tryLoadFile(internalName, new File(directory, tail + ".class").getAbsolutePath(), buffer, true)) {
                        return true;
                    }
                }
            }

            final int split = head.lastIndexOf('/');

            if (split <= 0) {
                break;
            }

            tail = head.substring(split + 1) + '/' + tail;
            head = head.substring(0, split);
        }

        return false;
    }

    private boolean tryLoadFile(final File file, final Buffer buffer) {
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Probing for file: " + file.getAbsolutePath() + "...");
        }

        if (!file.exists() || file.isDirectory()) {
            return false;
        }

        try (final FileInputStream in = new FileInputStream(file)) {
            int remainingBytes = in.available();

            buffer.position(0);
            buffer.reset(remainingBytes);

            while (remainingBytes > 0) {
                final int bytesRead = in.read(buffer.array(), buffer.position(), remainingBytes);

                if (bytesRead < 0) {
                    break;
                }

                remainingBytes -= bytesRead;
                buffer.advance(bytesRead);
            }

            buffer.position(0);
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }

    private boolean tryLoadFile(final String internalName, final String typeNameOrPath, final Buffer buffer, final boolean trustName) {
        final File file = new File(typeNameOrPath);

        if (!tryLoadFile(file, buffer)) {
            return false;
        }

        final String actualName = getInternalNameFromClassFile(buffer);

        final String name = trustName ? (internalName != null ? internalName : actualName)
                                      : actualName;

        if (name == null) {
            return false;
        }

        final boolean nameMatches = StringUtilities.equals(actualName, internalName);
        final boolean pathMatchesName = typeNameOrPath.endsWith(name.replace('/', File.separatorChar) + ".class");

        final boolean result = internalName == null ||
                               pathMatchesName ||
                               nameMatches;

        if (result) {
            final int packageEnd = name.lastIndexOf('/');
            final String packageName;

            if (packageEnd < 0 || packageEnd >= name.length()) {
                packageName = StringUtilities.EMPTY;
            }
            else {
                packageName = name.substring(0, packageEnd);
            }

            registerKnownPath(packageName, file.getParentFile(), pathMatchesName);

            _knownFiles.put(actualName, file);

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Type loaded from " + file.getAbsolutePath() + ".");
            }
        }
        else {
            buffer.reset(0);
        }

        return result;
    }

    private void registerKnownPath(final String packageName, final File directory, final boolean recursive) {
        if (directory == null || !directory.exists()) {
            return;
        }

        LinkedHashSet<File> directories = _packageLocations.get(packageName);

        if (directories == null) {
            _packageLocations.put(packageName, directories = new LinkedHashSet<>());
        }

        if (!directories.add(directory) || !recursive) {
            return;
        }

        try {
            final String directoryPath = StringUtilities.removeRight(
                directory.getCanonicalPath(),
                new char[] { PathHelper.DirectorySeparator, PathHelper.AlternateDirectorySeparator }
            ).replace('\\', '/');

            int delimiterIndex;
            String currentPackage = packageName;
            File currentDirectory = new File(directoryPath);

            while ((delimiterIndex = currentPackage.lastIndexOf('/')) >= 0 &&
                   currentDirectory.exists() &&
                   delimiterIndex < currentPackage.length() - 1) {

                final String segmentName = currentPackage.substring(delimiterIndex + 1);

                if (!StringUtilities.equals(currentDirectory.getName(), segmentName, StringComparison.OrdinalIgnoreCase)) {
                    break;
                }

                currentPackage = currentPackage.substring(0, delimiterIndex);
                currentDirectory = currentDirectory.getParentFile();

                directories = _packageLocations.get(currentPackage);

                if (directories == null) {
                    _packageLocations.put(currentPackage, directories = new LinkedHashSet<>());
                }

                if (!directories.add(currentDirectory)) {
                    break;
                }
            }
        }
        catch (IOException ignored) {
        }
    }

    private static String getInternalNameFromClassFile(final Buffer b) {
        final long magic = b.readInt() & 0xFFFFFFFFL;

        if (magic != 0xCAFEBABEL) {
            return null;
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
