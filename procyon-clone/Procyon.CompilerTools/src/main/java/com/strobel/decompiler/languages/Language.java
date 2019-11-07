/*
 * Language.java
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

package com.strobel.decompiler.languages;

import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.ITextOutput;

public abstract class Language {
    public abstract String getName();
    public abstract String getFileExtension();

    public void decompilePackage(
        final String packageName,
        final Iterable<TypeDefinition> types,
        final ITextOutput output,
        final DecompilationOptions options) {

        writeCommentLine(output, packageName);
    }

    public TypeDecompilationResults decompileType(final TypeDefinition type, final ITextOutput output, final DecompilationOptions options) {
        writeCommentLine(output, typeToString(type, true));
        return new TypeDecompilationResults( null);
    }

    public void decompileMethod(final MethodDefinition method, final ITextOutput output, final DecompilationOptions options) {
        writeCommentLine(output, typeToString(method.getDeclaringType(), true) + "." + method.getName());
    }

    public void decompileField(final FieldDefinition field, final ITextOutput output, final DecompilationOptions options) {
        writeCommentLine(output, typeToString(field.getDeclaringType(), true) + "." + field.getName());
    }

    public void writeCommentLine(final ITextOutput output, final String comment) {
        output.writeComment("// " + comment);
        output.writeLine();
    }

    public String typeToString(final TypeReference type, final boolean includePackage) {
        VerifyArgument.notNull(type, "type");
        return includePackage ? type.getFullName() : type.getName();
    }

    public String formatTypeName(final TypeReference type) {
        return VerifyArgument.notNull(type, "type").getName();
    }

    public boolean isMemberBrowsable(final MemberReference member) {
        return true;
    }

    public String getHint(final MemberReference member) {
        if (member instanceof TypeReference) {
            return typeToString((TypeReference) member, true);
        }

        return member.toString();
    }
}
