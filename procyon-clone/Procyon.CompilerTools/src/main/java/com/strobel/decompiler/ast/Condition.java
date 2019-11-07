/*
 * Condition.java
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

import com.strobel.core.ArrayUtilities;
import com.strobel.decompiler.ITextOutput;

import java.util.List;

public final class Condition extends Node {
    private Expression _condition;
    private Block _trueBlock;
    private Block _falseBlock;

    public final Expression getCondition() {
        return _condition;
    }

    public final void setCondition(final Expression condition) {
        _condition = condition;
    }

    public final Block getTrueBlock() {
        return _trueBlock;
    }

    public final void setTrueBlock(final Block trueBlock) {
        _trueBlock = trueBlock;
    }

    public final Block getFalseBlock() {
        return _falseBlock;
    }

    public final void setFalseBlock(final Block falseBlock) {
        _falseBlock = falseBlock;
    }

    @Override
    public final List<Node> getChildren() {
        final int size = (_condition != null ? 1 : 0) +
                         (_trueBlock != null ? 1 : 0) +
                         (_falseBlock != null ? 1 : 0);

        final Node[] children = new Node[size];

        int i = 0;

        if (_condition != null) {
            children[i++] = _condition;
        }

        if (_trueBlock != null) {
            children[i++] = _trueBlock;
        }

        if (_falseBlock != null) {
            children[i++] = _falseBlock;
        }

        return ArrayUtilities.asUnmodifiableList(children);
    }

    @Override
    public final void writeTo(final ITextOutput output) {
        output.writeKeyword("if");
        output.write(" (");

        if (_condition != null) {
            _condition.writeTo(output);
        }
        else {
            output.write("...");
        }

        output.writeLine(") {");
        output.indent();

        if (_trueBlock != null) {
            _trueBlock.writeTo(output);
        }

        output.unindent();
        output.writeLine("}");

        if (_falseBlock != null && !_falseBlock.getBody().isEmpty()) {
            output.writeKeyword("else");
            output.writeLine(" {");
            output.indent();

            _falseBlock.writeTo(output);

            output.unindent();
            output.writeLine("}");
        }
    }
}
