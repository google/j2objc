/*
 * TypeVisitor.java
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

import com.strobel.assembler.ir.ConstantPool;
import com.strobel.assembler.ir.attributes.SourceAttribute;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;

/**
 * @author Mike Strobel
 */
public interface TypeVisitor {
    void visitParser(final MetadataParser parser);

    void visit(
        final int majorVersion,
        final int minorVersion,
        final long flags,
        final String name,
        final String genericSignature,
        final String baseTypeName,
        final String[] interfaceNames);

    void visitDeclaringMethod(final MethodReference method);
    void visitOuterType(final TypeReference type);
    void visitInnerType(final TypeDefinition type);

    void visitAttribute(final SourceAttribute attribute);
    void visitAnnotation(final CustomAnnotation annotation, final boolean visible);

    FieldVisitor visitField(
        final long flags,
        final String name,
        final TypeReference fieldType);

    MethodVisitor visitMethod(
        final long flags,
        final String name,
        final IMethodSignature signature,
        final TypeReference... thrownTypes);

    ConstantPool.Visitor visitConstantPool();

    void visitEnd();
}
