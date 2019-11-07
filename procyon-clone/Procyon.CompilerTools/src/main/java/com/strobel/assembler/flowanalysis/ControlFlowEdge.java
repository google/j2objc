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

package com.strobel.assembler.flowanalysis;

import com.strobel.core.VerifyArgument;

public final class ControlFlowEdge {
    private final ControlFlowNode _source;
    private final ControlFlowNode _target;
    private final JumpType _type;

    public ControlFlowEdge(final ControlFlowNode source, final ControlFlowNode target, final JumpType type) {
        _source = VerifyArgument.notNull(source, "source");
        _target = VerifyArgument.notNull(target, "target");
        _type = VerifyArgument.notNull(type, "type");
    }

    public final ControlFlowNode getSource() {
        return _source;
    }

    public final ControlFlowNode getTarget() {
        return _target;
    }

    public final JumpType getType() {
        return _type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ControlFlowEdge) {
            final ControlFlowEdge other = (ControlFlowEdge) obj;

            return other._source == _source &&
                   other._target == _target;
        }

        return false;
    }

    @Override
    public final String toString() {
        switch (_type) {
            case Normal:
                return "#" + _target.getBlockIndex();

            case JumpToExceptionHandler:
                return "e:#" + _target.getBlockIndex();

            default:
                return _type + ":#" + _target.getBlockIndex();
        }
    }
}
