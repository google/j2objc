/*
 * TryCatchBlock.java
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

package com.strobel.decompiler.ast;

import com.strobel.assembler.Collection;
import com.strobel.core.ArrayUtilities;
import com.strobel.decompiler.ITextOutput;

import java.util.List;

public final class TryCatchBlock extends Node {
    private final List<CatchBlock> _catchBlocks = new Collection<>();
    private Block _tryBlock;
    private Block _finallyBlock;
    private boolean _synchronized;

    public final Block getTryBlock() {
        return _tryBlock;
    }

    public final void setTryBlock(final Block tryBlock) {
        _tryBlock = tryBlock;
    }

    public final List<CatchBlock> getCatchBlocks() {
        return _catchBlocks;
    }

    public final Block getFinallyBlock() {
        return _finallyBlock;
    }

    public final void setFinallyBlock(final Block finallyBlock) {
        _finallyBlock = finallyBlock;
    }

    public final boolean isSynchronized() {
        return _synchronized;
    }

    public final void setSynchronized(final boolean simpleSynchronized) {
        _synchronized = simpleSynchronized;
    }

    @Override
    public final List<Node> getChildren() {
        final int size = _catchBlocks.size() + (_tryBlock != null ? 1 : 0) + (_finallyBlock != null ? 1 : 0);
        final Node[] children = new Node[size];

        int i = 0;

        if (_tryBlock != null) {
            children[i++] = _tryBlock;
        }

        for (final CatchBlock catchBlock : _catchBlocks) {
            children[i++] = catchBlock;
        }

        if (_finallyBlock != null) {
            //noinspection UnusedAssignment
            children[i++] = _finallyBlock;
        }

        return ArrayUtilities.asUnmodifiableList(children);
    }

    @Override
    public final void writeTo(final ITextOutput output) {
        output.writeKeyword("try");
        output.writeLine(" {");
        output.indent();

        if (_tryBlock != null) {
            _tryBlock.writeTo(output);
        }

        output.unindent();
        output.writeLine("}");

        for (final CatchBlock catchBlock : _catchBlocks) {
            catchBlock.writeTo(output);
        }

        if (_finallyBlock != null) {
            output.writeKeyword("finally");
            output.writeLine(" {");
            output.indent();

            _finallyBlock.writeTo(output);

            output.unindent();
            output.writeLine("}");
        }
    }
}
