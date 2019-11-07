/*
 * ControlFlowNode.java
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

import com.strobel.decompiler.languages.java.ast.Statement;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// Represents a node in the control flow graph of a C# method.
/// </summary>
public class ControlFlowNode {
    private final Statement _previousStatement;
    private final Statement _nextStatement;

    private final ControlFlowNodeType _type;

    private final List<ControlFlowEdge> _outgoing = new ArrayList<>();
    private final List<ControlFlowEdge> _incoming = new ArrayList<>();

    public ControlFlowNode(final Statement previousStatement, final Statement nextStatement, final ControlFlowNodeType type) {
        if (previousStatement == null && nextStatement == null) {
            throw new IllegalArgumentException("previousStatement and nextStatement must not be both null");
        }

        _previousStatement = previousStatement;
        _nextStatement = nextStatement;
        _type = type;
    }

    public Statement getPreviousStatement() {
        return _previousStatement;
    }

    public Statement getNextStatement() {
        return _nextStatement;
    }

    public ControlFlowNodeType getType() {
        return _type;
    }

    public List<ControlFlowEdge> getOutgoing() {
        return _outgoing;
    }

    public List<ControlFlowEdge> getIncoming() {
        return _incoming;
    }
}

