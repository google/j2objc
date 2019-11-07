/*
 * Decompiler.java
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

package com.strobel.decompiler;

import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.*;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.java.JavaFormattingOptions;

public final class Decompiler {
    public static void decompile(final String internalName, final ITextOutput output) {
        decompile(internalName, output, new DecompilerSettings());
    }

    public static void decompile(final String internalName, final ITextOutput output, final DecompilerSettings settings) {
        VerifyArgument.notNull(internalName, "internalName");
        VerifyArgument.notNull(settings, "settings");

        final ITypeLoader typeLoader = settings.getTypeLoader() != null ? settings.getTypeLoader() : new InputTypeLoader();
        final MetadataSystem metadataSystem = new MetadataSystem(typeLoader);

        final TypeReference type;

        if (internalName.length() == 1) {
            //
            // Hack to get around classes whose descriptors clash with primitive types.
            //

            final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
            final TypeReference reference = parser.parseTypeDescriptor(internalName);

            type = metadataSystem.resolve(reference);
        }
        else {
            type = metadataSystem.lookupType(internalName);
        }

        final TypeDefinition resolvedType;

        if (type == null || (resolvedType = type.resolve()) == null) {
            output.writeLine("!!! ERROR: Failed to load class %s.", internalName);
            return;
        }

        DeobfuscationUtilities.processType(resolvedType);

        final DecompilationOptions options = new DecompilationOptions();

        options.setSettings(settings);
        options.setFullDecompilation(true);

        if (settings.getJavaFormattingOptions() == null) {
            settings.setJavaFormattingOptions(JavaFormattingOptions.createDefault());
        }

        settings.getLanguage().decompileType(resolvedType, output, options);
    }
}
