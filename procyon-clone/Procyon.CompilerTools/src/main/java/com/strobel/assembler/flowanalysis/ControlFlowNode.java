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

package com.strobel.assembler.flowanalysis;

import com.strobel.annotations.NotNull;
import com.strobel.assembler.Collection;
import com.strobel.assembler.ir.InstructionBlock;
import com.strobel.assembler.ir.ExceptionHandler;
import com.strobel.assembler.ir.Instruction;
import com.strobel.core.Predicate;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.functions.Block;
import com.strobel.functions.Function;
import com.strobel.util.ContractUtils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

public final class ControlFlowNode implements Comparable<ControlFlowNode> {
    private final int _blockIndex;
    private final int _offset;
    private final ControlFlowNodeType _nodeType;
    private final ControlFlowNode _endFinallyNode;
    private final List<ControlFlowNode> _dominatorTreeChildren = new Collection<>();
    private final Set<ControlFlowNode> _dominanceFrontier = new LinkedHashSet<>();
    private final List<ControlFlowEdge> _incoming = new Collection<>();
    private final List<ControlFlowEdge> _outgoing = new Collection<>();

    private boolean _visited;
    private ControlFlowNode _copyFrom;
    private ControlFlowNode _immediateDominator;
    private Instruction _start;
    private Instruction _end;
    private ExceptionHandler _exceptionHandler;
    private Object _userData;

    public ControlFlowNode(final int blockIndex, final int offset, final ControlFlowNodeType nodeType) {
        _blockIndex = blockIndex;
        _offset = offset;
        _nodeType = VerifyArgument.notNull(nodeType, "nodeType");
        _endFinallyNode = null;
        _start = null;
        _end = null;
    }

    public ControlFlowNode(final int blockIndex, final Instruction start, final Instruction end) {
        _blockIndex = blockIndex;
        _start = VerifyArgument.notNull(start, "start");
        _end = VerifyArgument.notNull(end, "end");
        _offset = start.getOffset();
        _nodeType = ControlFlowNodeType.Normal;
        _endFinallyNode = null;
    }

    public ControlFlowNode(final int blockIndex, final ExceptionHandler exceptionHandler, final ControlFlowNode endFinallyNode) {
        _blockIndex = blockIndex;
        _exceptionHandler = VerifyArgument.notNull(exceptionHandler, "exceptionHandler");
        _nodeType = exceptionHandler.isFinally() ? ControlFlowNodeType.FinallyHandler : ControlFlowNodeType.CatchHandler;
        _endFinallyNode = endFinallyNode;

        final InstructionBlock handlerBlock = exceptionHandler.getHandlerBlock();

//        _start = handlerBlock.getFirstInstruction();
//        _end = handlerBlock.getLastInstruction();
        _start = null;
        _end = null;
        _offset = handlerBlock.getFirstInstruction().getOffset(); //_start.getOffset();
    }

    public final int getBlockIndex() {
        return _blockIndex;
    }

    public final int getOffset() {
        return _offset;
    }

    public final ControlFlowNodeType getNodeType() {
        return _nodeType;
    }

    public final ControlFlowNode getEndFinallyNode() {
        return _endFinallyNode;
    }

    public final List<ControlFlowNode> getDominatorTreeChildren() {
        return _dominatorTreeChildren;
    }

    public final Set<ControlFlowNode> getDominanceFrontier() {
        return _dominanceFrontier;
    }

    public final List<ControlFlowEdge> getIncoming() {
        return _incoming;
    }

    public final List<ControlFlowEdge> getOutgoing() {
        return _outgoing;
    }

    public final boolean isVisited() {
        return _visited;
    }

    public final boolean isReachable() {
        return _immediateDominator != null || _nodeType == ControlFlowNodeType.EntryPoint;
    }

    public final ControlFlowNode getCopyFrom() {
        return _copyFrom;
    }

    public final ControlFlowNode getImmediateDominator() {
        return _immediateDominator;
    }

    public final Instruction getStart() {
        return _start;
    }

    public final Instruction getEnd() {
        return _end;
    }

    public final ExceptionHandler getExceptionHandler() {
        return _exceptionHandler;
    }

    public final Object getUserData() {
        return _userData;
    }

    public final void setVisited(final boolean visited) {
        _visited = visited;
    }

    public final void setCopyFrom(final ControlFlowNode copyFrom) {
        _copyFrom = copyFrom;
    }

    public final void setImmediateDominator(final ControlFlowNode immediateDominator) {
        _immediateDominator = immediateDominator;
    }

    public final void setStart(final Instruction start) {
        _start = start;
    }

    public final void setEnd(final Instruction end) {
        _end = end;
    }

    public final void setExceptionHandler(final ExceptionHandler exceptionHandler) {
        _exceptionHandler = exceptionHandler;
    }

    public final void setUserData(final Object userData) {
        _userData = userData;
    }

    public final boolean succeeds(final ControlFlowNode other) {
        if (other == null) {
            return false;
        }

        for (int i = 0; i < _incoming.size(); i++) {
            if (_incoming.get(i).getSource() == other) {
                return true;
            }
        }

        return false;
    }

    public final boolean precedes(final ControlFlowNode other) {
        if (other == null) {
            return false;
        }

        for (int i = 0; i < _outgoing.size(); i++) {
            if (_outgoing.get(i).getTarget() == other) {
                return true;
            }
        }

        return false;
    }

    public final Iterable<ControlFlowNode> getPredecessors() {
        return new Iterable<ControlFlowNode>() {
            @NotNull
            @Override
            public final Iterator<ControlFlowNode> iterator() {
                return new PredecessorIterator();
            }
        };
    }

    public final Iterable<ControlFlowNode> getSuccessors() {
        return new Iterable<ControlFlowNode>() {
            @NotNull
            @Override
            public final Iterator<ControlFlowNode> iterator() {
                return new SuccessorIterator();
            }
        };
    }

    public final Iterable<Instruction> getInstructions() {
        return new Iterable<Instruction>() {
            @NotNull
            @Override
            public final Iterator<Instruction> iterator() {
                return new InstructionIterator();
            }
        };
    }

    public final void traversePreOrder(
        final Function<ControlFlowNode, Iterable<ControlFlowNode>> children,
        final Block<ControlFlowNode> visitAction) {

        if (_visited) {
            return;
        }

        _visited = true;
        visitAction.accept(this);

        for (final ControlFlowNode child : children.apply(this)) {
            child.traversePreOrder(children, visitAction);
        }
    }

    public final void traversePostOrder(
        final Function<ControlFlowNode, Iterable<ControlFlowNode>> children,
        final Block<ControlFlowNode> visitAction) {

        if (_visited) {
            return;
        }

        _visited = true;

        for (final ControlFlowNode child : children.apply(this)) {
            child.traversePostOrder(children, visitAction);
        }

        visitAction.accept(this);
    }

    public final boolean dominates(final ControlFlowNode node) {
        ControlFlowNode current = node;

        while (current != null) {
            if (current == this) {
                return true;
            }
            current = current._immediateDominator;
        }

        return false;
    }

    @Override
    public final String toString() {
        final PlainTextOutput output = new PlainTextOutput();

        switch (_nodeType) {
            case Normal: {
                output.write("Block #%d", _blockIndex);

                if (_start != null) {
                    output.write(": %d to %d", _start.getOffset(), _end.getEndOffset());
                }

                break;
            }

            case CatchHandler:
            case FinallyHandler: {
                output.write("Block #%d: %s: ", _blockIndex, _nodeType);
                DecompilerHelpers.writeExceptionHandler(output, _exceptionHandler);
                break;
            }

            default: {
                output.write("Block #%d: %s", _blockIndex, _nodeType);
                break;
            }
        }

        output.indent();

        if (!_dominanceFrontier.isEmpty()) {
            output.writeLine();
            output.write("DominanceFrontier: ");

            final int[] blockIndexes = new int[_dominanceFrontier.size()];

            int i = 0;

            for (final ControlFlowNode node : _dominanceFrontier) {
                blockIndexes[i++] = node._blockIndex;
            }

            Arrays.sort(blockIndexes);

            output.write(
                StringUtilities.join(
                    ", ",
                    new Iterable<String>() {
                        @NotNull
                        @Override
                        public Iterator<String> iterator() {
                            return new Iterator<String>() {
                                private int _position = 0;

                                @Override
                                public boolean hasNext() {
                                    return _position < blockIndexes.length;
                                }

                                @Override
                                public String next() {
                                    if (!hasNext()) {
                                        throw new NoSuchElementException();
                                    }
                                    return String.valueOf(blockIndexes[_position++]);
                                }

                                @Override
                                public void remove() {
                                    throw ContractUtils.unreachable();
                                }
                            };
                        }
                    }
                )
            );
        }

        for (final Instruction instruction : getInstructions()) {
            output.writeLine();
            DecompilerHelpers.writeInstruction(output, instruction);
        }

        final Object userData = _userData;

        if (userData != null) {
            output.writeLine();
            output.write(String.valueOf(userData));
        }

        output.unindent();

        return output.toString();
    }

    @Override
    public int compareTo(final ControlFlowNode o) {
        return Integer.compare(_blockIndex, o._blockIndex);
    }

    // <editor-fold defaultstate="collapsed" desc="Iterators">

    private final class PredecessorIterator implements Iterator<ControlFlowNode> {
        private Iterator<ControlFlowEdge> _innerIterator;

        @Override
        public final boolean hasNext() {
            if (_innerIterator == null) {
                _innerIterator = _incoming.listIterator();
            }

            return _innerIterator.hasNext();
        }

        @Override
        public final ControlFlowNode next() {
            if (_innerIterator == null) {
                _innerIterator = _incoming.listIterator();
            }

            return _innerIterator.next().getSource();
        }

        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }

    private final class SuccessorIterator implements Iterator<ControlFlowNode> {
        private Iterator<ControlFlowEdge> _innerIterator;

        @Override
        public final boolean hasNext() {
            if (_innerIterator == null) {
                _innerIterator = _outgoing.listIterator();
            }

            return _innerIterator.hasNext();
        }

        @Override
        public final ControlFlowNode next() {
            if (_innerIterator == null) {
                _innerIterator = _outgoing.listIterator();
            }

            return _innerIterator.next().getTarget();
        }

        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }

    private final class InstructionIterator implements Iterator<Instruction> {
        private Instruction _next = _start;

        @Override
        public final boolean hasNext() {
            return _next != null &&
                   _next.getOffset() <= _end.getOffset();
        }

        @Override
        public final Instruction next() {
            final Instruction next = _next;

            if (next == null ||
                next.getOffset() > _end.getOffset()) {

                throw new NoSuchElementException();
            }

            _next = next.getNext();

            return next;
        }

        @Override
        public final void remove() {
            throw ContractUtils.unsupported();
        }
    }

    // </editor-fold>

    public final static Predicate<ControlFlowNode> REACHABLE_PREDICATE = new Predicate<ControlFlowNode>() {
        @Override
        public boolean test(final ControlFlowNode node) {
            return node.isReachable();
        }
    };
}
