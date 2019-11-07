/*
 * LabelInfo.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

import com.strobel.core.CollectionUtilities;
import com.strobel.core.delegates.Func1;
import com.strobel.reflection.PrimitiveTypes;
import com.strobel.reflection.emit.CodeGenerator;
import com.strobel.reflection.emit.Label;
import com.strobel.reflection.emit.LocalBuilder;
import com.strobel.reflection.emit.OpCode;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author strobelm
 */
final class LabelInfo {
    // The tree node representing this label
    private final LabelTarget _node;

    // The bytecode label, will be mutated if Node is redefined
    private Label _label;
    private boolean _labelDefined;

    // The local that carries the label's value, if any 
    private LocalBuilder _value;

    // The blocks where this label is defined. If it has more than one item, 
    // the blocks can't be jumped to except from a child block
    private final HashSet<LabelScopeInfo> _definitions = new HashSet<LabelScopeInfo>();

    // Blocks that jump to this block
    private final ArrayList<LabelScopeInfo> _references = new ArrayList<LabelScopeInfo>();

    // True if this label is the last thing in this block
    // (meaning we can emit a direct return) 
    private final boolean _canReturn;

    // True if at least one jump is across blocks 
    // If we have any jump across blocks to this label, then the
    // LabelTarget can only be defined in one place
    private boolean _acrossBlockJump;

    // Until we have more information, default to a leave instruction,
    // which always works. Note: leave spills the stack, so we need to 
    // ensure that StackSpiller has guarenteed us an empty stack at this 
    // point. Otherwise Leave and Branch are not equivalent
    private OpCode _opCode = OpCode.GOTO;

    private final CodeGenerator _generator;

    public LabelInfo(final CodeGenerator generator, final LabelTarget label, final boolean canReturn) {
        _generator = generator;
        _node = label;
        _canReturn = canReturn;
    }

    final Label getLabel() {
        ensureLabelAndValue();
        return _label;
    }

    final boolean canReturn() {
        return _canReturn;
    }

    final boolean canBranch() {
        return _opCode != OpCode.RETURN;
    }

    final void reference(final LabelScopeInfo block) {
        _references.add(block);
        if (_definitions.size() > 0) {
            validateJump(block);
        }
    }

    final void define(final LabelScopeInfo block) {
        // Prevent the label from being shadowed, which enforces cleaner
        // trees. Also we depend on this for simplicity (keeping only one
        // active IL Label per LabelInfo)
        for (LabelScopeInfo j = block; j != null; j = j.parent) {
            if (j.containsTarget(_node)) {
                throw Error.labelTargetAlreadyDefined(_node.getName());
            }
        }

        _definitions.add(block);

        assert block != null;

        block.addLabelInfo(_node, this);

        // Once defined, validate all jumps
        if (_definitions.size() == 1) {
            for (final LabelScopeInfo r : _references) {
                validateJump(r);
            }
        }
        else {
            // Was just redefined, if we had any across block jumps, they're
            // now invalid
            if (_acrossBlockJump) {
                throw Error.ambiguousJump(_node.getName());
            }
            // For local jumps, we need a new IL label
            // This is okay because:
            //   1. no across block jumps have been made or will be made
            //   2. we don't allow the label to be shadowed
            _labelDefined = false;
        }
    }

    private void validateJump(final LabelScopeInfo reference) {
        // Assume we can do a ret/branch
        _opCode = _canReturn ? OpCode.RETURN : OpCode.GOTO;

        // look for a simple jump out
        for (LabelScopeInfo j = reference; j != null; j = j.parent) {
            if (_definitions.contains(j)) {
                // found it, jump is valid!
                return;
            }
            if (j.kind == LabelScopeKind.Finally ||
                j.kind == LabelScopeKind.Filter) {
//                break;
            }
            if (j.kind == LabelScopeKind.Try ||
                j.kind == LabelScopeKind.Catch) {
//                _opCode = OpCode.RET;
            }
        }

        _acrossBlockJump = true;

        if (_node != null && _node.getType() != PrimitiveTypes.Void) {
            throw Error.nonLocalJumpWithValue(_node.getName());
        }

        if (_definitions.size() > 1) {
            assert _node != null;
            throw Error.ambiguousJump(_node.getName());
        }

        // We didn't find an outward jump. Look for a jump across blocks
        final LabelScopeInfo def = CollectionUtilities.first(_definitions);
        final LabelScopeInfo common = Helpers.commonNode(
            def,
            reference,
            new Func1<LabelScopeInfo, LabelScopeInfo>() {
                @Override
                public LabelScopeInfo apply(final LabelScopeInfo s) {
                    return s.parent;
                }
            }
        );

        // Assume we can do a ret/branch
        _opCode = _canReturn ? OpCode.RETURN : OpCode.GOTO;

        for (LabelScopeInfo j = reference; j != common; j = j.parent) {
            assert j != null;
            if (j.kind == LabelScopeKind.Try ||
                j.kind == LabelScopeKind.Catch ||
                j.kind == LabelScopeKind.Finally) {

                _opCode = OpCode.GOTO;
            }
        }

        // Verify that we aren't jumping into a catch or an expression
        for (LabelScopeInfo j = def; j != common; j = j.parent) {
            if (!j.canJumpInto()) {
                if (j.kind == LabelScopeKind.Expression) {
                    throw Error.controlCannotEnterExpression();
                }
                else {
                    throw Error.controlCannotEnterTry();
                }
            }
        }
    }

    void validateFinish() {
        // Make sure that if this label was jumped to, it is also defined
        if (_references.size() > 0 && _definitions.size() == 0) {
            throw Error.labelTargetUndefined(_node.getName());
        }
    }

    void emitJump() {
        // Return directly if we can
        if (_opCode == OpCode.RETURN) {
            _generator.emitReturn(_node.getType());
        }
        else {
            storeValue();
            _generator.emit(_opCode, getLabel());
        }
    }

    private void storeValue() {
        ensureLabelAndValue();

        if (_value != null) {
            _generator.emitStore(_value);
        }
    }

    final void mark() {
        if (_canReturn) {
            // Don't mark return labels unless they were actually jumped to
            // (returns are last so we know for sure if anyone jumped to it) 
            if (!_labelDefined) {
                // We don't even need to emit the "ret" because
                // LambdaCompiler does that for us.
                return;
            }

            // Otherwise, emit something like: 
            // ret
            // <marked label>: 
            // ldloc <value>
            _generator.emitReturn(_node.getType());
        }
        else {
            // For the normal case, we emit:
            // [t]store <value>
            // <marked label>: 
            // [t]load <value>
            storeValue();
        }
        markWithEmptyStack();
    }

    // Like mark(), but assumes the stack is empty
    final void markWithEmptyStack() {
        _generator.markLabel(getLabel());

        if (_value == null) {
            return;
        }

        // We always read the value from a local, because we don't know 
        // if there will be a "leave" instruction targeting it ("branch"
        // preserves its stack, but "leave" empties the stack)
        _generator.emitLoad(_value);
    }

    private void ensureLabelAndValue() {
        if (_labelDefined) {
            return;
        }

        _labelDefined = true;
        _label = _generator.defineLabel();

        if (_node != null && _node.getType() != PrimitiveTypes.Void) {
            _value = _generator.declareLocal(_node.getType());
        }
    }
}
