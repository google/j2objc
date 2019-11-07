/*
 * Loop.java
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

import java.util.Collections;
import java.util.List;

public final class Loop extends Node {
    private LoopType _loopType = LoopType.PreCondition;
    private Expression _condition;
    private Block _body;

    public final Expression getCondition() {
        return _condition;
    }

    public final void setCondition(final Expression condition) {
        _condition = condition;
    }

    public final Block getBody() {
        return _body;
    }

    public final void setBody(final Block body) {
        _body = body;
    }

    public final LoopType getLoopType() {
        return _loopType;
    }

    public final void setLoopType(final LoopType loopType) {
        _loopType = loopType;
    }

    @Override
    public final List<Node> getChildren() {
        if (_condition == null) {
            if (_body == null) {
                return Collections.emptyList();
            }
            return Collections.<Node>singletonList(_body);
        }

        if (_body == null) {
            return Collections.<Node>singletonList(_condition);
        }

        return ArrayUtilities.asUnmodifiableList(_condition, _body);
    }

    @Override
    public final void writeTo(final ITextOutput output) {
        if (_condition != null) {
            if (_loopType == LoopType.PostCondition) {
                output.writeKeyword("do");
            }
            else {
                output.writeKeyword("while");
                output.write(" (");
                _condition.writeTo(output);
                output.write(')');
            }
        }
        else {
            output.writeKeyword("loop");
        }

        output.writeLine(" {");
        output.indent();

        if (_body != null) {
            _body.writeTo(output);
        }

        output.unindent();

        if (_condition != null && _loopType == LoopType.PostCondition) {
            output.write("} ");
            output.writeKeyword("while");
            output.write(" (");

            _condition.writeTo(output);

            output.writeLine(")");
        }
        else {
            output.writeLine("}");
        }
    }
}
