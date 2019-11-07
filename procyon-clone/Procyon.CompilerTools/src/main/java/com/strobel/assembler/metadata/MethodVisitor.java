/*
 * MethodVisitor.java
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

import com.strobel.assembler.ir.Frame;
import com.strobel.assembler.ir.Instruction;
import com.strobel.assembler.ir.InstructionVisitor;
import com.strobel.assembler.ir.attributes.SourceAttribute;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 4:03 PM
 */
public interface MethodVisitor {
    boolean canVisitBody();

    InstructionVisitor visitBody(final MethodBody body);

    void visitEnd();
    void visitFrame(final Frame frame);

    void visitLineNumber(final Instruction instruction, final int lineNumber);

    void visitAttribute(final SourceAttribute attribute);
    void visitAnnotation(final CustomAnnotation annotation, final boolean visible);
    void visitParameterAnnotation(final int parameter, final CustomAnnotation annotation, final boolean visible);
}
