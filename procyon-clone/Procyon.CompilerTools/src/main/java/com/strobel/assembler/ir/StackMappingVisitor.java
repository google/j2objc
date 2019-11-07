/*
 * StackMappingVisitor.java
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

import com.strobel.assembler.ir.attributes.SourceAttribute;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.InstructionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class StackMappingVisitor implements MethodVisitor {
    private final MethodVisitor _innerVisitor;

    private int _maxLocals;
    //    private int _maxStack;
    private List<FrameValue> _stack = new ArrayList<>();
    private List<FrameValue> _locals = new ArrayList<>();
    private Map<Instruction, TypeReference> _initializations = new IdentityHashMap<>();

    public StackMappingVisitor() {
        _innerVisitor = null;
    }

    public StackMappingVisitor(final MethodVisitor innerVisitor) {
        _innerVisitor = innerVisitor;
    }

    public final Frame buildFrame() {
        return new Frame(
            FrameType.New,
            _locals.toArray(new FrameValue[_locals.size()]),
            _stack.toArray(new FrameValue[_stack.size()])
        );
    }

    public final int getStackSize() {
        return _stack == null ? 0 : _stack.size();
    }

    public final int getLocalCount() {
        return _locals == null ? 0 : _locals.size();
    }

    public final FrameValue getStackValue(final int offset) {
        VerifyArgument.inRange(0, getStackSize(), offset, "offset");
        return _stack.get(_stack.size() - offset - 1);
    }

    public final FrameValue getLocalValue(final int slot) {
        VerifyArgument.inRange(0, getLocalCount(), slot, "slot");
        return _locals.get(slot);
    }

    public final Map<Instruction, TypeReference> getInitializations() {
        return Collections.unmodifiableMap(_initializations);
    }

    public final FrameValue[] getStackSnapshot() {
        if (_stack == null || _stack.isEmpty()) {
            return FrameValue.EMPTY_VALUES;
        }

        return _stack.toArray(new FrameValue[_stack.size()]);
    }

    public final FrameValue[] getLocalsSnapshot() {
        if (_locals == null || _locals.isEmpty()) {
            return FrameValue.EMPTY_VALUES;
        }

        return _locals.toArray(new FrameValue[_locals.size()]);
    }

    @Override
    public boolean canVisitBody() {
        return true;
    }

    @Override
    public InstructionVisitor visitBody(final MethodBody body) {
        if (_innerVisitor != null && _innerVisitor.canVisitBody()) {
            return new InstructionAnalyzer(body, _innerVisitor.visitBody(body));
        }
        else {
            return new InstructionAnalyzer(body);
        }
    }

    @Override
    public void visitEnd() {
        if (_innerVisitor != null) {
            _innerVisitor.visitEnd();
        }
    }

    @Override
    public void visitFrame(final Frame frame) {
        VerifyArgument.notNull(frame, "frame");

        if (frame.getFrameType() != FrameType.New) {
            throw Error.stackMapperCalledWithUnexpandedFrame(frame.getFrameType());
        }

        if (_innerVisitor != null) {
            _innerVisitor.visitFrame(frame);
        }

        if (_locals != null) {
            _locals.clear();
            _stack.clear();
//            _initializations.clear();
        }
        else {
            _locals = new ArrayList<>();
            _stack = new ArrayList<>();
            _initializations = new IdentityHashMap<>();
        }

        for (final FrameValue frameValue : frame.getLocalValues()) {
            _locals.add(frameValue);
        }

        for (final FrameValue frameValue : frame.getStackValues()) {
            _stack.add(frameValue);
        }
    }

    @Override
    public void visitLineNumber(final Instruction instruction, final int lineNumber) {
        if (_innerVisitor != null) {
            _innerVisitor.visitLineNumber(instruction, lineNumber);
        }
    }

    @Override
    public void visitAttribute(final SourceAttribute attribute) {
        if (_innerVisitor != null) {
            _innerVisitor.visitAttribute(attribute);
        }
    }

    @Override
    public void visitAnnotation(final CustomAnnotation annotation, final boolean visible) {
        if (_innerVisitor != null) {
            _innerVisitor.visitAnnotation(annotation, visible);
        }
    }

    @Override
    public void visitParameterAnnotation(final int parameter, final CustomAnnotation annotation, final boolean visible) {
        if (_innerVisitor != null) {
            _innerVisitor.visitParameterAnnotation(parameter, annotation, visible);
        }
    }

    protected final FrameValue get(final int local) {
        _maxLocals = Math.max(_maxLocals, local);
        return local < _locals.size() ? _locals.get(local) : FrameValue.TOP;
    }

    protected final void set(final int local, final FrameValue value) {
        _maxLocals = Math.max(_maxLocals, local);

        if (_locals == null) {
            _locals = new ArrayList<>();
            _stack = new ArrayList<>();
            _initializations = new IdentityHashMap<>();
        }

        while (local >= _locals.size()) {
            _locals.add(FrameValue.TOP);
        }

        _locals.set(local, value);

        if (value.getType().isDoubleWord()) {
            _locals.set(local + 1, FrameValue.TOP);
        }
    }

    protected final void set(final int local, final TypeReference type) {
        _maxLocals = Math.max(_maxLocals, local);

        if (_locals == null) {
            _locals = new ArrayList<>();
            _stack = new ArrayList<>();
            _initializations = new IdentityHashMap<>();
        }

        while (local >= _locals.size()) {
            _locals.add(FrameValue.TOP);
        }

        if (type == null) {
            _locals.set(local, FrameValue.TOP);
            return;
        }

        switch (type.getSimpleType()) {
            case Boolean:
            case Byte:
            case Character:
            case Short:
            case Integer:
                _locals.set(local, FrameValue.INTEGER);
                break;

            case Long:
                _locals.set(local, FrameValue.LONG);
                if (local + 1 >= _locals.size()) {
                    _locals.add(FrameValue.TOP);
                }
                else {
                    _locals.set(local + 1, FrameValue.TOP);
                }
                break;

            case Float:
                _locals.set(local, FrameValue.FLOAT);
                break;

            case Double:
                _locals.set(local, FrameValue.DOUBLE);
                if (local + 1 >= _locals.size()) {
                    _locals.add(FrameValue.TOP);
                }
                else {
                    _locals.set(local + 1, FrameValue.TOP);
                }
                break;

            case Object:
            case Array:
            case TypeVariable:
            case Wildcard:
                _locals.set(local, FrameValue.makeReference(type));
                break;

            case Void:
                throw new IllegalArgumentException("Cannot set local to type void.");
        }
    }

    protected final FrameValue pop() {
        return _stack.remove(_stack.size() - 1);
    }

    protected final FrameValue peek() {
        return _stack.get(_stack.size() - 1);
    }

    protected final void pop(final int count) {
        final int size = _stack.size();
        final int end = size - count;

        for (int i = size - 1; i >= end; i--) {
            _stack.remove(i);
        }
    }

    protected final void push(final TypeReference type) {
        if (_stack == null) {
            _locals = new ArrayList<>();
            _stack = new ArrayList<>();
            _initializations = new IdentityHashMap<>();
        }

        switch (type.getSimpleType()) {
            case Boolean:
            case Byte:
            case Character:
            case Short:
            case Integer:
                _stack.add(FrameValue.INTEGER);
                break;

            case Long:
                _stack.add(FrameValue.LONG);
                _stack.add(FrameValue.TOP);
                break;

            case Float:
                _stack.add(FrameValue.FLOAT);
                break;

            case Double:
                _stack.add(FrameValue.DOUBLE);
                _stack.add(FrameValue.TOP);
                break;

            case Object:
            case Array:
            case TypeVariable:
            case Wildcard:
                _stack.add(FrameValue.makeReference(type));
                break;

            case Void:
                break;
        }
    }

    protected final void push(final FrameValue value) {
        if (_stack == null) {
            _locals = new ArrayList<>();
            _stack = new ArrayList<>();
            _initializations = new IdentityHashMap<>();
        }
        _stack.add(value);
    }

    protected void initialize(final FrameValue value, final TypeReference type) {
        VerifyArgument.notNull(type, "type");

        final Object parameter = value.getParameter();
        final FrameValue initializedValue = FrameValue.makeReference(type);

        if (parameter instanceof Instruction) {
            _initializations.put((Instruction) parameter, type);
        }

        for (int i = 0; i < _stack.size(); i++) {
            if (_stack.get(i) == value) {
                _stack.set(i, initializedValue);
            }
        }

        for (int i = 0; i < _locals.size(); i++) {
            if (_locals.get(i) == value) {
                _locals.set(i, initializedValue);
            }
        }
    }

    public void pruneLocals() {
        while (!_locals.isEmpty() && _locals.get(_locals.size() - 1) == FrameValue.OUT_OF_SCOPE) {
            _locals.remove(_locals.size() - 1);
        }

        for (int i = 0; i < _locals.size(); i++) {
            if (_locals.get(i) == FrameValue.OUT_OF_SCOPE) {
                _locals.set(i, FrameValue.TOP);
            }
        }
    }

    private final class InstructionAnalyzer implements InstructionVisitor {
        private final InstructionVisitor _innerVisitor;
        private final MethodBody _body;
        private final CoreMetadataFactory _factory;

        private boolean _afterExecute;

        private InstructionAnalyzer(final MethodBody body) {
            this(body, null);
        }

        private InstructionAnalyzer(final MethodBody body, final InstructionVisitor innerVisitor) {
            _body = VerifyArgument.notNull(body, "body");
            _innerVisitor = innerVisitor;

            if (body.getMethod().isConstructor()) {
                set(0, FrameValue.UNINITIALIZED_THIS);
            }

            _factory = CoreMetadataFactory.make(_body.getMethod().getDeclaringType(), _body.getMethod());
        }

        @Override
        public void visit(final Instruction instruction) {
            if (_innerVisitor != null) {
                _innerVisitor.visit(instruction);
            }

            instruction.accept(this);
            execute(instruction);

            _afterExecute = true;

            try {
                instruction.accept(this);
            }
            finally {
                _afterExecute = false;
            }
        }

        @Override
        public void visit(final OpCode code) {
            if (_afterExecute) {
                if (code.isStore()) {
                    final FrameValue value = _temp.isEmpty() ? pop() : _temp.pop();

                    if (code.getStackChange() == -2) {
                        final FrameValue doubleOrLong = _temp.isEmpty() ? pop() : _temp.pop();

                        set(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code), doubleOrLong);
                        set(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code) + 1, value);
                    }
                    else {
                        set(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code), value);
                    }
                }
            }
            else if (code.isLoad()) {
                final FrameValue value = get(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code));

                push(value);

                if (value.getType().isDoubleWord()) {
                    push(get(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code) + 1));
                }
            }
        }

        @Override
        public void visitConstant(final OpCode code, final TypeReference value) {
        }

        @Override
        public void visitConstant(final OpCode code, final int value) {
        }

        @Override
        public void visitConstant(final OpCode code, final long value) {
        }

        @Override
        public void visitConstant(final OpCode code, final float value) {
        }

        @Override
        public void visitConstant(final OpCode code, final double value) {
        }

        @Override
        public void visitConstant(final OpCode code, final String value) {
        }

        @Override
        public void visitBranch(final OpCode code, final Instruction target) {
        }

        @Override
        public void visitVariable(final OpCode code, final VariableReference variable) {
            if (_afterExecute) {
                if (code.isStore()) {
                    final FrameValue value = _temp.isEmpty() ? pop() : _temp.pop();

                    if (code.getStackChange() == -2) {
                        final FrameValue doubleOrLong = _temp.isEmpty() ? pop() : _temp.pop();

                        set(variable.getSlot(), doubleOrLong);
                        set(variable.getSlot() + 1, value);
                    }
                    else {
                        set(variable.getSlot(), value);
                    }
                }
            }
            else if (code.isLoad()) {
                final FrameValue value = get(variable.getSlot());

                push(value);

                if (code.getStackChange() == 2) {
                    push(get(variable.getSlot() + 1));
                }
            }
        }

        @Override
        public void visitVariable(final OpCode code, final VariableReference variable, final int operand) {
        }

        @Override
        public void visitType(final OpCode code, final TypeReference type) {
        }

        @Override
        public void visitMethod(final OpCode code, final MethodReference method) {
        }

        @Override
        public void visitDynamicCallSite(final OpCode opCode, final DynamicCallSite callSite) {
        }

        @Override
        public void visitField(final OpCode code, final FieldReference field) {
        }

        @Override
        public void visitLabel(final Label label) {
        }

        @Override
        public void visitSwitch(final OpCode code, final SwitchInfo switchInfo) {
        }

        @Override
        public void visitEnd() {
        }

        private final Stack<FrameValue> _temp = new Stack<>();

        @SuppressWarnings("ConstantConditions")
        private void execute(final Instruction instruction) {
            final OpCode code = instruction.getOpCode();

            _temp.clear();

            if (code.isLoad() || code.isStore()) {
                return;
            }

            switch (code.getStackBehaviorPop()) {
                case Pop0:
                    break;

                case Pop1:
                    _temp.push(pop());
                    break;

                case Pop2:
                case Pop1_Pop1:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case Pop1_Pop2:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case Pop1_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case Pop2_Pop1:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case Pop2_Pop2:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopI4:
                    _temp.push(pop());
                    break;

                case PopI8:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopR4:
                    _temp.push(pop());
                    break;

                case PopR8:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopA:
                    _temp.push(pop());
                    break;

                case PopI4_PopI4:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopI4_PopI8:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopI8_PopI8:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopR4_PopR4:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopR8_PopR8:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopI4_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopI4_PopI4_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopI8_PopI4_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopR4_PopI4_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopR8_PopI4_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopA_PopI4_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case PopA_PopA:
                    _temp.push(pop());
                    _temp.push(pop());
                    break;

                case VarPop: {
                    switch (code) {
                        case INVOKEVIRTUAL:
                        case INVOKESPECIAL:
                        case INVOKESTATIC:
                        case INVOKEINTERFACE:
                        case INVOKEDYNAMIC: {
                            final IMethodSignature method;

                            if (code == OpCode.INVOKEDYNAMIC) {
                                method = instruction.<DynamicCallSite>getOperand(0).getMethodType();
                            }
                            else {
                                method = instruction.getOperand(0);
                            }

                            final List<ParameterDefinition> parameters = method.getParameters();

                            if (code == OpCode.INVOKESPECIAL &&
                                ((MethodReference) method).isConstructor()) {

                                final FrameValue firstParameter = getStackValue(computeSize(parameters));
                                final FrameValueType firstParameterType = firstParameter.getType();

                                if (firstParameterType == FrameValueType.UninitializedThis ||
                                    firstParameterType == FrameValueType.Uninitialized) {

                                    TypeReference initializedType;

                                    if (firstParameterType == FrameValueType.UninitializedThis) {
                                        initializedType = _body.getMethod().getDeclaringType();
                                    }
                                    else {
                                        initializedType = ((MethodReference) method).getDeclaringType();
                                    }

                                    if (initializedType.isGenericDefinition()) {
                                        final Instruction next = instruction.getNext();

                                        if (next != null && next.getOpCode().isStore()) {
                                            final int slot = InstructionHelper.getLoadOrStoreSlot(next);
                                            final VariableDefinition variable = _body.getVariables().tryFind(slot, next.getEndOffset());

                                            if (variable != null &&
                                                variable.isFromMetadata() &&
                                                variable.getVariableType() instanceof IGenericInstance &&
                                                StringUtilities.equals(initializedType.getInternalName(), variable.getVariableType().getInternalName())) {

                                                initializedType = variable.getVariableType();
                                            }
                                        }
                                    }

                                    initialize(firstParameter, initializedType);
                                }
                            }

                            for (final ParameterDefinition parameter : parameters) {
                                final TypeReference parameterType = parameter.getParameterType();

                                switch (parameterType.getSimpleType()) {
                                    case Long:
                                    case Double:
                                        _temp.push(pop());
                                        _temp.push(pop());
                                        break;

                                    default:
                                        _temp.push(pop());
                                        break;
                                }
                            }

                            /*if (code == OpCode.INVOKEDYNAMIC) {
                                final MethodDefinition resolved = method instanceof MethodReference ? ((MethodReference) method).resolve()
                                                                                                    : null;

                                if (resolved != null) {
                                    if (!resolved.isStatic() && !resolved.isConstructor()) {
                                        _temp.push(pop());
                                    }
                                }
                                else {
                                    final DynamicCallSite callSite = instruction.getOperand(0);
                                    final MethodReference bootstrapMethod = callSite.getBootstrapMethod();

                                    if ("java/lang/invoke/LambdaMetafactory".equals(bootstrapMethod.getDeclaringType().getInternalName()) &&
                                        StringUtilities.equals("metaFactory", bootstrapMethod.getName(), StringComparison.OrdinalIgnoreCase) &&
                                        callSite.getBootstrapArguments().size() == 3 &&
                                        callSite.getBootstrapArguments().get(1) instanceof MethodHandle) {

                                        final MethodHandle targetMethodHandle = (MethodHandle) callSite.getBootstrapArguments().get(1);

                                        switch (targetMethodHandle.getHandleType()) {
                                            case GetField:
                                            case PutField:
                                            case InvokeVirtual:
                                            case InvokeInterface:
                                            case InvokeSpecial:
                                                _temp.push(pop());
                                                break;
                                        }
                                    }
                                }
                            }
                            else*/ if (code != OpCode.INVOKESTATIC && code != OpCode.INVOKEDYNAMIC) {
                                _temp.push(pop());
                            }

                            break;
                        }

                        case ATHROW: {
                            _temp.push(pop());
                            while (!_stack.isEmpty()) {
                                pop();
                            }
                            break;
                        }

                        case MULTIANEWARRAY: {
                            final int dimensions = ((Number) instruction.getOperand(1)).intValue();

                            for (int i = 0; i < dimensions; i++) {
                                _temp.push(pop());
                            }

                            break;
                        }
                    }

                    break;
                }
            }

            if (code.isArrayLoad()) {
                final FrameValue frameValue = _temp.pop();
                final Object parameter = frameValue.getParameter();

                switch (code) {
                    case BALOAD:
                    case CALOAD:
                    case SALOAD:
                    case IALOAD:
                        push(FrameValue.INTEGER);
                        break;

                    case LALOAD:
                        push(FrameValue.LONG);
                        push(FrameValue.TOP);
                        break;

                    case FALOAD:
                        push(FrameValue.FLOAT);
                        break;

                    case DALOAD:
                        push(FrameValue.DOUBLE);
                        push(FrameValue.TOP);
                        break;

                    case AALOAD:
                        if (parameter instanceof TypeReference) {
                            push(((TypeReference) parameter).getElementType());
                        }
                        else if (frameValue.getType() == FrameValueType.Null) {
                            push(FrameValue.NULL);
                        }
                        else {
                            push(FrameValue.TOP);
                        }
                        break;
                }

                return;
            }
            else if (code.isJumpToSubroutine()) {
                push(FrameValue.makeAddress(instruction.getNext()));
            }

            switch (code.getStackBehaviorPush()) {
                case Push0:
                    break;

                case Push1: {
                    switch (code) {
                        case LDC:
                        case LDC_W: {
                            final Object op = instruction.getOperand(0);
                            if (op instanceof String) {
                                push(_factory.makeNamedType("java.lang.String"));
                            }
                            else if (op instanceof TypeReference) {
                                push(_factory.makeNamedType("java.lang.Class"));
                            }
                            else {
                                if (op instanceof Long) {
                                    push(FrameValue.LONG);
                                    push(FrameValue.TOP);
                                }
                                else if (op instanceof Float) {
                                    push(FrameValue.FLOAT);
                                }
                                else if (op instanceof Double) {
                                    push(FrameValue.DOUBLE);
                                    push(FrameValue.TOP);
                                }
                                else if (op instanceof Integer) {
                                    push(FrameValue.INTEGER);
                                }
                            }
                            break;
                        }

                        case GETFIELD:
                        case GETSTATIC: {
                            final FieldReference field = instruction.getOperand(0);
                            push(field.getFieldType());
                            break;
                        }
                    }
                    break;
                }

                case Push1_Push1: {
                    switch (code) {
                        case DUP: {
                            final FrameValue value = _temp.pop();
                            push(value);
                            push(value);
                            break;
                        }

                        case SWAP: {
                            final FrameValue t2 = _temp.pop();
                            final FrameValue t1 = _temp.pop();
                            push(t2);
                            push(t1);
                            break;
                        }
                    }
                    break;
                }

                case Push1_Push1_Push1: {
                    final FrameValue t2 = _temp.pop();
                    final FrameValue t1 = _temp.pop();
                    push(t1);
                    push(t2);
                    push(t1);
                    break;
                }

                case Push1_Push2_Push1: {
                    final FrameValue t3 = _temp.pop();
                    final FrameValue t2 = _temp.pop();
                    final FrameValue t1 = _temp.pop();
                    push(t1);
                    push(t3);
                    push(t2);
                    push(t1);
                    break;
                }

                case Push2: {
                    final Number constant = instruction.getOperand(0);
                    if (constant instanceof Double) {
                        push(FrameValue.DOUBLE);
                        push(FrameValue.TOP);
                    }
                    else {
                        push(FrameValue.LONG);
                        push(FrameValue.TOP);
                    }
                    break;
                }

                case Push2_Push2: {
                    final FrameValue t2 = _temp.pop();
                    final FrameValue t1 = _temp.pop();
                    push(t2);
                    push(t1);
                    push(t2);
                    push(t1);
                    break;
                }

                case Push2_Push1_Push2: {
                    final FrameValue t3 = _temp.pop();
                    final FrameValue t2 = _temp.pop();
                    final FrameValue t1 = _temp.pop();
                    push(t2);
                    push(t1);
                    push(t3);
                    push(t2);
                    push(t1);
                    break;
                }

                case Push2_Push2_Push2: {
                    final FrameValue t4 = _temp.pop();
                    final FrameValue t3 = _temp.pop();
                    final FrameValue t2 = _temp.pop();
                    final FrameValue t1 = _temp.pop();
                    push(t2);
                    push(t1);
                    push(t4);
                    push(t3);
                    push(t2);
                    push(t1);
                    break;
                }

                case PushI4: {
                    push(FrameValue.INTEGER);
                    break;
                }

                case PushI8: {
                    push(FrameValue.LONG);
                    push(FrameValue.TOP);
                    break;
                }

                case PushR4: {
                    push(FrameValue.FLOAT);
                    break;
                }

                case PushR8: {
                    push(FrameValue.DOUBLE);
                    push(FrameValue.TOP);
                    break;
                }

                case PushA: {
                    switch (code) {
                        case NEW:
                            push(FrameValue.makeUninitializedReference(instruction));
                            break;

                        case NEWARRAY:
                        case ANEWARRAY:
                            push(instruction.<TypeReference>getOperand(0).makeArrayType());
                            break;

                        case CHECKCAST:
                        case MULTIANEWARRAY:
                            push(instruction.<TypeReference>getOperand(0));
                            break;

                        case ACONST_NULL:
                            push(FrameValue.NULL);
                            break;

                        default:
                            push(pop());
                            break;
                    }
                    break;
                }

                case PushAddress: {
                    push(FrameValue.makeAddress(instruction.getNext()));
                    break;
                }

                case VarPush: {
                    final IMethodSignature signature;

                    if (code == OpCode.INVOKEDYNAMIC) {
                        signature = instruction.<DynamicCallSite>getOperand(0).getMethodType();
                    }
                    else {
                        signature = instruction.<MethodReference>getOperand(0);
                    }

                    TypeReference returnType = signature.getReturnType();

                    if (returnType.getSimpleType() != JvmType.Void) {
                        if (code != OpCode.INVOKESTATIC &&
                            code != OpCode.INVOKEDYNAMIC) {

                            final TypeReference typeReference;

                            if (code == OpCode.INVOKESPECIAL) {
                                typeReference = ((MethodReference) signature).getDeclaringType();
                            }
                            else {
                                final Object parameter = _temp.peek().getParameter();

                                typeReference = parameter instanceof Instruction ? _initializations.get(parameter)
                                                                                 : (TypeReference) parameter;
                            }

                            final TypeReference targetType = substituteTypeArguments(
                                typeReference,
                                (MemberReference) signature
                            );

                            returnType = substituteTypeArguments(
                                substituteTypeArguments(
                                    signature.getReturnType(),
                                    (MemberReference) signature
                                ),
                                targetType
                            );
                        }
                        else if (instruction.getNext() != null &&
                                 instruction.getNext().getOpCode().isStore()) {

                            final Instruction next = instruction.getNext();
                            final int slot = InstructionHelper.getLoadOrStoreSlot(next);
                            final VariableDefinition variable = _body.getVariables().tryFind(slot, next.getEndOffset());

                            if (variable != null && variable.isFromMetadata()) {
                                returnType = substituteTypeArguments(
                                    variable.getVariableType(),
                                    signature.getReturnType()
                                );
                            }
                        }
                    }

                    if (returnType.isWildcardType()) {
                        returnType = returnType.hasSuperBound() ? returnType.getSuperBound()
                                                                : returnType.getExtendsBound();
                    }

                    switch (returnType.getSimpleType()) {
                        case Boolean:
                        case Byte:
                        case Character:
                        case Short:
                        case Integer:
                            push(FrameValue.INTEGER);
                            break;

                        case Long:
                            push(FrameValue.LONG);
                            push(FrameValue.TOP);
                            break;

                        case Float:
                            push(FrameValue.FLOAT);
                            break;

                        case Double:
                            push(FrameValue.DOUBLE);
                            push(FrameValue.TOP);
                            break;

                        case Object:
                        case Array:
                        case TypeVariable:
                        case Wildcard:
                            push(FrameValue.makeReference(returnType));
                            break;

                        case Void:
                            break;
                    }

                    break;
                }
            }
        }

        private int computeSize(final List<ParameterDefinition> parameters) {
            int size = 0;

            for (final ParameterDefinition parameter : parameters) {
                size += parameter.getSize();
            }

            return size;
        }

        private TypeReference substituteTypeArguments(final TypeReference type, final MemberReference member) {
            if (type instanceof ArrayType) {
                final ArrayType arrayType = (ArrayType) type;

                final TypeReference elementType = substituteTypeArguments(
                    arrayType.getElementType(),
                    member
                );

                if (!MetadataResolver.areEquivalent(elementType, arrayType.getElementType())) {
                    return elementType.makeArrayType();
                }

                return type;
            }

            if (type instanceof IGenericInstance) {
                final IGenericInstance genericInstance = (IGenericInstance) type;
                final List<TypeReference> newTypeArguments = new ArrayList<>();

                boolean isChanged = false;

                for (final TypeReference typeArgument : genericInstance.getTypeArguments()) {
                    final TypeReference newTypeArgument = substituteTypeArguments(typeArgument, member);

                    newTypeArguments.add(newTypeArgument);
                    isChanged |= newTypeArgument != typeArgument;
                }

                return isChanged ? type.makeGenericType(newTypeArguments)
                                 : type;
            }

            if (type instanceof GenericParameter) {
                final GenericParameter genericParameter = (GenericParameter) type;
                final IGenericParameterProvider owner = genericParameter.getOwner();

                if (member.getDeclaringType() instanceof ArrayType) {
                    return member.getDeclaringType().getElementType();
                }
                else if (owner instanceof MethodReference && member instanceof MethodReference) {
                    final MethodReference method = (MethodReference) member;
                    final MethodReference ownerMethod = (MethodReference) owner;

                    if (method.isGenericMethod() &&
                        MetadataResolver.areEquivalent(ownerMethod.getDeclaringType(), method.getDeclaringType()) &&
                        StringUtilities.equals(ownerMethod.getName(), method.getName()) &&
                        StringUtilities.equals(ownerMethod.getErasedSignature(), method.getErasedSignature())) {

                        if (method instanceof IGenericInstance) {
                            final List<TypeReference> typeArguments = ((IGenericInstance) member).getTypeArguments();
                            return typeArguments.get(genericParameter.getPosition());
                        }
                        else {
                            return method.getGenericParameters().get(genericParameter.getPosition());
                        }
                    }
                }
                else if (owner instanceof TypeReference) {
                    TypeReference declaringType;

                    if (member instanceof TypeReference) {
                        declaringType = (TypeReference) member;
                    }
                    else {
                        declaringType = member.getDeclaringType();
                    }

                    if (MetadataResolver.areEquivalent((TypeReference) owner, declaringType)) {
                        if (declaringType instanceof IGenericInstance) {
                            final List<TypeReference> typeArguments = ((IGenericInstance) declaringType).getTypeArguments();
                            return typeArguments.get(genericParameter.getPosition());
                        }

                        if (!declaringType.isGenericDefinition()) {
                            declaringType = declaringType.resolve();
                        }

                        if (declaringType != null && declaringType.isGenericDefinition()) {
                            return declaringType.getGenericParameters().get(genericParameter.getPosition());
                        }
                    }
                }
            }

            return type;
        }
    }
}
