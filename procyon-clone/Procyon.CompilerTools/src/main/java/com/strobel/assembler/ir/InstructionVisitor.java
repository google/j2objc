/*
 * InstructionVisitor.java
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

package com.strobel.assembler.ir;

import com.strobel.assembler.metadata.*;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 2:15 PM
 */
public interface InstructionVisitor {
    void visit(final Instruction instruction);
    void visit(final OpCode opCode);

    void visitConstant(final OpCode opCode, final TypeReference value);
    void visitConstant(final OpCode opCode, final int value);
    void visitConstant(final OpCode opCode, final long value);
    void visitConstant(final OpCode opCode, final float value);
    void visitConstant(final OpCode opCode, final double value);
    void visitConstant(final OpCode opCode, final String value);
    
    void visitBranch(final OpCode opCode, final Instruction target);
    void visitVariable(final OpCode opCode, final VariableReference variable);
    void visitVariable(final OpCode opCode, final VariableReference variable, int operand);
    void visitType(final OpCode opCode, final TypeReference type);
    void visitMethod(final OpCode opCode, final MethodReference method);
    void visitDynamicCallSite(final OpCode opCode, final DynamicCallSite callSite);
    void visitField(final OpCode opCode, final FieldReference field);

    void visitLabel(final Label label);

    void visitSwitch(final OpCode opCode, final SwitchInfo switchInfo);

    void visitEnd();

    // <editor-fold defaultstate="collapsed" desc="Empty Visitor">

    public final static InstructionVisitor EMPTY = new InstructionVisitor() {
        @Override
        public void visit(final Instruction instruction) {
        }

        @Override
        public void visit(final OpCode opCode) {
        }

        @Override
        public void visitConstant(final OpCode opCode, final TypeReference value) {
        }

        @Override
        public void visitConstant(final OpCode opCode, final int value) {
        }

        @Override
        public void visitConstant(final OpCode opCode, final long value) {
        }

        @Override
        public void visitConstant(final OpCode opCode, final float value) {
        }

        @Override
        public void visitConstant(final OpCode opCode, final double value) {
        }

        @Override
        public void visitConstant(final OpCode opCode, final String value) {
        }

        @Override
        public void visitBranch(final OpCode opCode, final Instruction target) {
        }

        @Override
        public void visitVariable(final OpCode opCode, final VariableReference variable) {
        }

        @Override
        public void visitVariable(final OpCode opCode, final VariableReference variable, final int operand) {
        }

        @Override
        public void visitType(final OpCode opCode, final TypeReference type) {
        }

        @Override
        public void visitMethod(final OpCode opCode, final MethodReference method) {
        }

        @Override
        public void visitDynamicCallSite(final OpCode opCode, final DynamicCallSite callSite) {
        }

        @Override
        public void visitField(final OpCode opCode, final FieldReference field) {
        }

        @Override
        public void visitLabel(final Label label) {
        }

        @Override
        public void visitSwitch(final OpCode opCode, final SwitchInfo switchInfo) {
        }

        @Override
        public void visitEnd() {
        }
    };

    // </editor-fold>
}
