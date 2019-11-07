/*
 * ControlFlowEdge.java
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

package com.strobel.decompiler.languages.java.analysis;

import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.java.ast.TryCatchStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ControlFlowEdge {
    private final ControlFlowNode _from;
    private final ControlFlowNode _to;
    private final ControlFlowEdgeType _type;

    List<TryCatchStatement> jumpOutOfTryFinally;

    public ControlFlowEdge(final ControlFlowNode from, final ControlFlowNode to, final ControlFlowEdgeType type) {
        _from = VerifyArgument.notNull(from, "from");
        _to = VerifyArgument.notNull(to, "to");
        _type = type;
    }

    final void AddJumpOutOfTryFinally(final TryCatchStatement tryFinally) {
        if (jumpOutOfTryFinally == null) {
            jumpOutOfTryFinally = new ArrayList<>();
        }
        jumpOutOfTryFinally.add(tryFinally);
    }

    /**
     * @return Whether this control flow edge is leaving any try-finally statements.
     */
    public final boolean isLeavingTryFinally() {
        return jumpOutOfTryFinally != null;
    }

    /**
     * @return The try-finally statements that this control flow edge is leaving.
     */
    public final Iterable<TryCatchStatement> getTryFinallyStatements() {
        if (jumpOutOfTryFinally != null) {
            return jumpOutOfTryFinally;
        }
        return Collections.emptyList();
    }

    public final ControlFlowNode getFrom() {
        return _from;
    }

    public final ControlFlowNode getTo() {
        return _to;
    }

    public final ControlFlowEdgeType getType() {
        return _type;
    }
}
