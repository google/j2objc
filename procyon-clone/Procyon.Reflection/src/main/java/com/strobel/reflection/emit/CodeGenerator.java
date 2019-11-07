/*
 * CodeGenerator.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.core.delegates.Func1;
import com.strobel.reflection.*;
import com.strobel.util.ContractUtils;
import com.strobel.util.TypeUtils;

import javax.lang.model.type.TypeKind;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author strobelm
 */
@SuppressWarnings(
    {
        "PointlessBitwiseExpression",
        "PointlessArithmeticExpression",
        "UnusedDeclaration",
        "PackageVisibleField",
        "ConstantConditions"
    })
public class CodeGenerator {

    final static int DefaultSize = 64;
    final static int DefaultFixupArraySize = 64;
    final static int DefaultLabelArraySize = 16;
    final static int DefaultExceptionArraySize = 8;

    private final static int MIN_BYTE = 0x00;
    private final static int MAX_BYTE = 0xFF;

    private final CodeStream _codeStream;

    private int[] _labelList;
    private int _labelCount;

    private __FixupData[] _fixupData;
    private int _fixupCount;

    private int _exceptionCount;
    private int _currentExceptionStackCount;
    private int _unhandledExceptionCount;
    private __ExceptionInfo[] _exceptions;              // This is the list of all of the exceptions in this CodeStream.
    private __ExceptionInfo[] _currentExceptionStack;   // This is the stack of exceptions which we're currently in.
    private Type<?>[] _unhandledExceptions;             // This is the list of all of the unhandled checked exceptions we've encountered.

    ScopeTree scopeTree;                // This variable tracks all debugging scope information.

    final MethodBuilder methodBuilder;
    int localCount;
    LocalBuilder[] locals;

    private int _maxStackSize = 0;      // Maximum stack size not counting the exceptions.

    private int _maxMidStack = 0;       // Maximum stack size for a given basic block.
    private int _maxMidStackCur = 0;    // Running count of the maximum stack size for the current basic block.

    public CodeGenerator(final MethodBuilder methodBuilder) {
        this(methodBuilder, DefaultSize);
    }

    public CodeGenerator(final MethodBuilder methodBuilder, final int initialSize) {
        this.methodBuilder = VerifyArgument.notNull(methodBuilder, "methodBuilder");

        if (initialSize < DefaultSize) {
            _codeStream = new CodeStream(DefaultSize);
        }
        else {
            _codeStream = new CodeStream(initialSize);
        }

        this.scopeTree = new ScopeTree();
    }

    public int offset() {
        return _codeStream.getLength();
    }

    // <editor-fold defaultstate="collapsed" desc="Exceptions">

    @SuppressWarnings("UnusedReturnValue")
    public Label beginExceptionBlock() {
        if (_exceptions == null) {
            _exceptions = new __ExceptionInfo[DefaultExceptionArraySize];
        }

        if (_currentExceptionStack == null) {
            _currentExceptionStack = new __ExceptionInfo[DefaultExceptionArraySize];
        }

        if (_exceptionCount >= _exceptions.length) {
            _exceptions = enlargeArray(_exceptions);
        }

        if (_currentExceptionStackCount >= _currentExceptionStack.length) {
            _currentExceptionStack = enlargeArray(_currentExceptionStack);
        }

        final Label endLabel = defineLabel();
        final __ExceptionInfo exceptionInfo = new __ExceptionInfo(offset(), endLabel);

        _exceptions[_exceptionCount++] = exceptionInfo;
        _currentExceptionStack[_currentExceptionStackCount++] = exceptionInfo;

        return endLabel;
    }

    public void endExceptionBlock() {
        if (_currentExceptionStackCount == 0) {
            throw Error.notInExceptionBlock();
        }

        final __ExceptionInfo current = _currentExceptionStack[_currentExceptionStackCount - 1];

        _currentExceptionStack[_currentExceptionStackCount - 1] = null;
        _currentExceptionStackCount--;

        final Label endLabel = current.getEndLabel();
        final int state = current.getCurrentState();

        if (state == __ExceptionInfo.State_Filter ||
            state == __ExceptionInfo.State_Try) {

            throw Error.badExceptionCodeGenerated();
        }

        if (_labelList[endLabel.getLabelValue()] == -1) {
            markLabel(endLabel);
        }
        else {
            markLabel(current.getFinallyEndLabel());
        }

        current.done(offset());
    }

    public void endTryBlock() {
        if (_currentExceptionStackCount == 0) {
            throw Error.notInExceptionBlock();
        }

        final __ExceptionInfo current = _currentExceptionStack[_currentExceptionStackCount - 1];

        // Insert a branch to the end of the exception block.
//        emit(OpCode.GOTO, current.getEndLabel());

        current.markTryEndAddress(offset());
    }

    public void beginCatchBlock(final Type<?> caughtType) {
        VerifyArgument.notNull(caughtType, "caughtType");

        if (_currentExceptionStackCount == 0) {
            throw Error.notInExceptionBlock();
        }

        if (!Types.Throwable.isAssignableFrom(caughtType)) {
            throw Error.catchRequiresThrowableType();
        }

        final __ExceptionInfo current = _currentExceptionStack[_currentExceptionStackCount - 1];

//        if (current.getCurrentState() == __ExceptionInfo.State_Catch) {
            // Insert a branch if the previous clause is a Catch.
            emit(OpCode.GOTO, current.getEndLabel());
//        }

        current.markCatchAddress(offset(), caughtType);
    }

    public void beginFinallyBlock() {
        if (_currentExceptionStackCount == 0) {
            throw Error.notInExceptionBlock();
        }

        final __ExceptionInfo current = _currentExceptionStack[_currentExceptionStackCount - 1];
        final int state = current.getCurrentState();

        int catchEndAddress = 0;

        if (state != __ExceptionInfo.State_Try) {
            catchEndAddress = offset();
        }

        final Label finallyEndLabel = defineLabel();

        current.setFinallyEndLabel(finallyEndLabel);

        final Label endLabel = current.getEndLabel();

        markLabel(endLabel);

        //
        // Insert a branch to jump past the finally block.  With the
        // JVM, the finally block contents are inlined in the try and
        // catch blocks.  What will actually be emitted next is the
        // finally block as executed only when an unhandled exception
        // occurs, so we want the preceding try/catch block past the
        // unhandled exception code path (again, as they've already
        // run the inlined finally block).
        //
        emit(OpCode.GOTO, current.getFinallyEndLabel());

        if (catchEndAddress == 0) {
            catchEndAddress = offset();
        }

        current.markFinallyAddress(offset(), catchEndAddress);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Labels">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LABELS                                                                                                             //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Label defineLabel() {
        // Declares a new Label.  This is just a token and does not yet represent any
        // particular location within the stream.  In order to set the position of the
        // label within the stream, you must call markLabel().

        if (_labelList == null) {
            _labelList = new int[DefaultLabelArraySize];
        }

        if (_labelCount >= _labelList.length) {
            _labelList = enlargeArray(_labelList);
        }

        _labelList[_labelCount] = -1;

        return new Label(_labelCount++);
    }

    public void markLabel(final Label label) {
        // Defines a label by setting the position where that label is found
        // within the stream.  Verifies the label is not defined more than once.

        final int labelIndex = label.getLabelValue();

        // This should never happen.
        if (labelIndex < 0 || labelIndex >= _labelList.length) {
            throw Error.badLabel();
        }

        if (_labelList[labelIndex] != -1) {
            throw Error.labelAlreadyDefined();
        }

        _labelList[labelIndex] = _codeStream.getLength();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Locals Declarations">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LOCAL DECLARATIONS                                                                                                 //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public LocalBuilder declareLocal(final Type<?> localType) {
        return declareLocal(null, localType);
    }

    public LocalBuilder declareLocal(final String name, final Type<?> localType) {
        VerifyArgument.notNull(localType, "localType");

        // Declare a local of type "local". The current active lexical scope
        // will be the scope that local will live.

        final LocalBuilder localBuilder;
        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        if (methodBuilder.isTypeCreated()) {
            // cannot change method after its containing type has been created
            throw Error.typeHasBeenCreated();
        }

        if (methodBuilder.isFinished()) {
            throw Error.methodIsFinished();
        }

        localBuilder = new LocalBuilder(localCount, name, localType, methodBuilder);

        localCount++;

        if (locals == null) {
            locals = new LocalBuilder[DefaultLabelArraySize];
        }
        else if (locals.length < localCount) {
            locals = enlargeArray(locals);
        }

        locals[localCount - 1] = localBuilder;

        return localBuilder;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Simple Operations (OpCodes with no Operands)">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SIMPLE OPERATIONS (OPCODES WITH NO OPERANDS)                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void dup() {
        emit(OpCode.DUP);
    }

    public void dup2() {
        emit(OpCode.DUP2);
    }

    public void dup2x1() {
        emit(OpCode.DUP2_X1);
    }

    public void dup2x2() {
        emit(OpCode.DUP2_X2);
    }

    public void dup(final Type<?> type) {
        switch (type.getKind()) {
            case LONG:
            case DOUBLE:
                emit(OpCode.DUP2);
                break;

            case VOID:
                break;

            default:
                emit(OpCode.DUP);
                break;
        }
    }

    public void pop() {
        emit(OpCode.POP);
    }

    public void pop2() {
        emit(OpCode.POP2);
    }

    public void pop(final Type<?> type) {
        switch (type.getKind()) {
            case LONG:
            case DOUBLE:
                emit(OpCode.POP2);
                break;

            case VOID:
                break;

            default:
                emit(OpCode.POP);
                break;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="General Emit Methods">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GENERAL EMIT METHODS                                                                                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void emit(final OpCode opCode) {
        ensureCapacity(opCode.getSizeWithOperands());
        internalEmit(opCode);
    }

    public void emit(final OpCode opCode, final byte arg) {
        emit(opCode);
        emitByteOperand(arg);
    }

    public void emit(final OpCode opCode, final short arg) {
        emit(opCode);
        emitShortOperand(arg);
    }

    public void emit(final OpCode opCode, final int arg) {
        emit(opCode);
        emitIntOperand(arg);
    }

    public void emit(final OpCode opCode, final long arg) {
        emit(opCode);
        emitLongOperand(arg);
    }

    public void emit(final OpCode opCode, final float arg) {
        emit(opCode);
        emitFloatOperand(arg);
    }

    public void emit(final OpCode opCode, final double arg) {
        emit(opCode);
        emitDoubleOperand(arg);
    }

    public void emit(final OpCode opCode, final String arg) {
        emit(opCode);
        emitString(arg);
    }

    public void emit(final OpCode opCode, final Type<?> type) {
        VerifyArgument.notNull(type, "type");

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int typeToken = methodBuilder.getDeclaringType().getTypeToken(type);

        emit(opCode, (short)typeToken);
    }

    public void emit(final OpCode opCode, final ConstructorInfo constructor) {
        VerifyArgument.notNull(constructor, "constructor");

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int methodToken = methodBuilder.getDeclaringType().getMethodToken(constructor);

        emit(opCode, (short)methodToken);

        registerCheckedExceptions(constructor);

        int stackChange = 0;

        if (constructor instanceof ConstructorBuilder) {
            for (final Type<?> type : ((ConstructorBuilder) constructor).getParameterTypes()) {
                stackChange -= stackSize(type);
            }
        }
        else {
            for (final ParameterInfo p : constructor.getParameters()) {
                stackChange -= stackSize(p.getParameterType());
            }
            stackChange -= constructor.getParameters().size();
        }

        updateStackSize(opCode, stackChange);
    }

    private static int stackSize(final Type<?> type) {
        final TypeKind kind = type.getKind();

        if (kind == TypeKind.LONG || kind == TypeKind.DOUBLE) {
            return 2;
        }

        if (kind == TypeKind.VOID) {
            return 0;
        }

        return 1;
    }

    public void emit(final OpCode opCode, final MethodInfo method) {
        VerifyArgument.notNull(method, "method");

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int methodToken = methodBuilder.getDeclaringType().getMethodToken(method);

        emit(opCode, (short)methodToken);

        final TypeList parameterTypes;

        int stackChange = 0;
        int formalParametersSize = 0;

        if (method instanceof MethodBuilder) {
            parameterTypes = ((MethodBuilder)method).getParameterTypes();
        }
        else {
            parameterTypes = method.getParameters().getParameterTypes();
        }

        for (final Type<?> parameterType : parameterTypes) {
            final int size = stackSize(parameterType);

            stackChange -= size;
            formalParametersSize += size;
        }

        if (opCode == OpCode.INVOKEINTERFACE) {
            final int argsSize = 1 + formalParametersSize;

            emitByteOperand((byte)argsSize);
            emitByteOperand((byte)0);
        }

        registerCheckedExceptions(method);

        stackChange += stackSize(method.getReturnType());

        updateStackSize(opCode, stackChange);
    }

    public void emit(final OpCode opCode, final FieldInfo field) {
        VerifyArgument.notNull(field, "field");

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int fieldToken = methodBuilder.getDeclaringType().getFieldToken(field);


        int stackChange = 0;

        if (opCode == OpCode.PUTFIELD || opCode == OpCode.PUTSTATIC) {
            stackChange -= stackSize(field.getFieldType());
        }
        else {
            stackChange += stackSize(field.getFieldType());
        }

        emit(opCode, (short)fieldToken);
        updateStackSize(opCode, stackChange);
    }

    public void emit(final OpCode opCode, final Label label) {
        VerifyArgument.notNull(label, "label");

        // Puts opCode onto the stream and leaves space to include label when fix-ups
        // are done.  Labels are created using CodeGenerator.defineLabel() and their
        // location within the stream is fixed by using CodeGenerator.defineLabel().
        //
        // opCode must represent a branch instruction (although we don't explicitly
        // verify this).  Since branches are relative instructions, label will be
        // replaced with the correct offset to branch during the fixup process.

        final int tempVal = label.getLabelValue();
        final int fixupOrigin = _codeStream.getLength();

        emit(opCode);

        if (opCode.getOperandType() == OperandType.Branch) {
            // HACK: To avoid resizing the byte array to accommodate wide jump labels,
            // we just pad the two bytes after a short jump label with NOP opcodes.
            // TODO: Fix this later.
            addFixup(label, fixupOrigin, _codeStream.getLength(), 2);
            _codeStream.putShort(0);
/*
            internalEmit(OpCode.NOP);
            internalEmit(OpCode.NOP);
*/
        }
        else if (opCode.getOperandType() == OperandType.BranchW) {
            addFixup(label, fixupOrigin, _codeStream.getLength(), 4);
            _codeStream.putInt(0);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Method Calls">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // METHOD CALLS                                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void call(final MethodInfo method) {
        VerifyArgument.notNull(method, "method");

        final OpCode opCode;

        if (method.isStatic()) {
            emit(OpCode.INVOKESTATIC, method);
        }
        else if (method.isPrivate()) {
            emit(OpCode.INVOKESPECIAL, method);
        }
        else if (method.getDeclaringType().isInterface()) {
            emit(OpCode.INVOKEINTERFACE, method);
        }
        else {
            emit(OpCode.INVOKEVIRTUAL, method);
        }
    }

    public void call(final ConstructorInfo constructor) {
        emit(OpCode.INVOKESPECIAL, constructor);
    }

    public void call(final OpCode opCode, final MethodInfo method) {
        VerifyArgument.notNull(method, "method");

        switch (opCode) {
            case INVOKEDYNAMIC:
            case INVOKEINTERFACE:
            case INVOKESPECIAL:
            case INVOKESTATIC:
            case INVOKEVIRTUAL:
                break;

            default:
                throw Error.invokeOpCodeRequired();
        }

        emit(opCode, method);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Branch Operations">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BRANCH OPERATIONS                                                                                                  //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void emitGoto(final Label label) {
        emit(OpCode.GOTO, label);
    }

    public void emitReturn() {
        emit(OpCode.RETURN);
    }

    public void emitReturn(final Type<?> returnType) {
        VerifyArgument.notNull(returnType, "returnType");

        final OpCode opCode;

        switch (returnType.getKind()) {
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case CHAR:
                opCode = OpCode.IRETURN;
                break;

            case LONG:
                opCode = OpCode.LRETURN;
                break;

            case FLOAT:
                opCode = OpCode.FRETURN;
                break;

            case DOUBLE:
                opCode = OpCode.DRETURN;
                break;

            case VOID:
                opCode = OpCode.RETURN;
                break;

            default:
                opCode = OpCode.ARETURN;
                break;
        }

        emit(opCode);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="New Object/Array Operations">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NEW OBJECT/ARRAY OPERATIONS                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void emitNew(final Type<?> type) {
        VerifyArgument.notNull(type, "type");

        if (type.containsGenericParameters()) {
            throw Error.cannotInstantiateUnboundGenericType(type);
        }

        if (type.isPrimitive()) {
            emitDefaultValue(type);
            return;
        }

        emit(OpCode.NEW, type);
    }

/*
    public void emitNew(final Type<?> type, final Type... parameterTypes) {
        VerifyArgument.notNull(type, "type");

        final ConstructorInfo constructor = type.getConstructor(parameterTypes);

        if (constructor == null) {
            throw Error.constructorNotFound();
        }

        emitNew(constructor);
    }
*/

    public void emitNewArray(final Type<?> arrayType) {
        VerifyArgument.notNull(arrayType, "arrayType");

        if (!arrayType.isArray()) {
            throw Error.typeMustBeArray();
        }

        Type<?> elementType = arrayType.getElementType();

        int rank = 1;

        while (elementType.isArray()) {
            ++rank;
            elementType = elementType.getElementType();
        }

        emitNewArray(arrayType, rank);
    }

    public void emitNewArray(final Type<?> arrayType, final int dimensionsToInitialize) {
        VerifyArgument.notNull(arrayType, "arrayType");
        VerifyArgument.isPositive(dimensionsToInitialize, "dimensionsToInitialize");

        Type<?> elementType;

        if (dimensionsToInitialize == 1) {
            elementType = arrayType.getElementType();

            if (elementType.isPrimitive()) {
                final byte typeCode;

                switch (elementType.getKind()) {
                    case BOOLEAN:
                        typeCode = 4;
                        break;
                    case BYTE:
                        typeCode = 8;
                        break;
                    case SHORT:
                        typeCode = 9;
                        break;
                    case INT:
                        typeCode = 10;
                        break;
                    case LONG:
                        typeCode = 11;
                        break;
                    case CHAR:
                        typeCode = 5;
                        break;
                    case FLOAT:
                        typeCode = 6;
                        break;
                    case DOUBLE:
                        typeCode = 7;
                        break;
                    default:
                        throw ContractUtils.unreachable();
                }
                emit(OpCode.NEWARRAY, typeCode);
            }
            else {
                emit(OpCode.ANEWARRAY, elementType);
            }

            return;
        }

        int dimension = dimensionsToInitialize;

        elementType = arrayType.getElementType();

        while (--dimension > 0) {
            if (!elementType.isArray()) {
                throw Error.newArrayDimensionsOutOfRange(arrayType, dimensionsToInitialize);
            }
            elementType = elementType.getElementType();
        }

        emit(OpCode.MULTIANEWARRAY, arrayType);
        emitByteOperand(dimensionsToInitialize);
    }

    public interface EmitArrayElementCallback {
        void emit(int index);
    }

    public final void emitArray(final Type<?> elementType, final int count, final EmitArrayElementCallback emit) {
        VerifyArgument.notNull(elementType, "elementType");
        VerifyArgument.notNull(emit, "emit");
        VerifyArgument.isNonNegative(count, "count");

        emitInteger(count);
        emitNewArray(elementType.makeArrayType());

        for (int i = 0; i < count; i++) {
            dup();
            emitInteger(i);
            emit.emit(i);
            emitStoreElement(elementType);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Locals and Arguments">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LOCALS AND ARGUMENTS                                                                                               //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void increment(final LocalBuilder local, final int delta) {
        VerifyArgument.notNull(local, "local");

        final int localIndex = translateLocal(local.getLocalIndex());

        if (local.startOffset < 0) {
            local.startOffset = offset();
        }

        if (localIndex < MAX_BYTE && delta <= Byte.MAX_VALUE && delta >= Byte.MIN_VALUE) {
            emit(OpCode.IINC);
            emitByteOperand(localIndex);
            emitByteOperand(delta);
        }
        else {
            emit(OpCode.IINC_W);
            emitShortOperand(localIndex);
            emitShortOperand(delta);
        }

        local.endOffset = offset();
    }

    public void emitLoad(final LocalBuilder local) {
        VerifyArgument.notNull(local, "local");

        if (local.getMethodBuilder() != methodBuilder) {
            throw Error.unmatchedLocal();
        }

        if (local.startOffset < 0) {
            local.startOffset = offset();
        }

        emitLoad(
            local.getLocalType(),
            translateLocal(local.getLocalIndex())
        );

        local.endOffset = offset();
    }

    public void emitStore(final LocalBuilder local) {
        VerifyArgument.notNull(local, "local");

        if (local.getMethodBuilder() != methodBuilder) {
            throw Error.unmatchedLocal();
        }

        if (local.startOffset < 0) {
            local.startOffset = offset();
        }

        emitStore(
            local.getLocalType(),
            translateLocal(local.getLocalIndex())
        );

        local.endOffset = offset();
    }

    public void emitThis() {
        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        if (methodBuilder.isStatic()) {
            throw Error.cannotLoadThisForStaticMethod();
        }

        emitLoad(methodBuilder.getDeclaringType(), 0);
    }

    public void emitLoadArgument(final int index) {
        assert index >= 0
            : "index >= 0";

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final TypeList parameterTypes = methodBuilder.getParameterTypes();

        if (index < 0 || index >= parameterTypes.size()) {
            throw Error.argumentIndexOutOfRange(methodBuilder, index);
        }

        final int absoluteIndex = translateParameter(index);

        final OpCode opCode = getLocalLoadOpCode(
            parameterTypes.get(index),
            absoluteIndex
        );

        internalEmit(opCode);

        if (opCode.getOperandType() == OperandType.NoOperands) {
            return;
        }

        if (absoluteIndex > MAX_BYTE)  {
            emitShortOperand(absoluteIndex);
        }
        else {
            emitByteOperand(absoluteIndex);
        }
    }

    protected void emitLoad(final Type<?> type, final int absoluteIndex) {
        assert absoluteIndex >= 0
            : "absoluteIndex >= 0";

        final OpCode optimalOpCode;

        optimalOpCode = getLocalLoadOpCode(type, absoluteIndex);

        emit(optimalOpCode);

        final OperandType operandType = optimalOpCode.getOperandType();

        if (operandType == OperandType.NoOperands) {
            return;
        }

        if (absoluteIndex > MAX_BYTE) {
            emitShortOperand(absoluteIndex);
        }
        else {
            emitByteOperand(absoluteIndex);
        }
    }

    public void emitStoreArgument(final int index) {
        assert index >= 0
            : "index >= 0";

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final TypeList parameterTypes = methodBuilder.getParameterTypes();

        if (index < 0 || index >= parameterTypes.size()) {
            throw Error.argumentIndexOutOfRange(methodBuilder, index);
        }

        final int absoluteIndex = translateParameter(index);

        final OpCode opCode = getLocalStoreOpCode(
            parameterTypes.get(index),
            absoluteIndex
        );

        internalEmit(opCode);

        if (opCode.getOperandType() == OperandType.NoOperands) {
            return;
        }

        if (absoluteIndex > MAX_BYTE)  {
            emitShortOperand(absoluteIndex);
        }
        else {
            emitByteOperand(absoluteIndex);
        }
    }

    protected void emitStore(final Type<?> type, final int absoluteIndex) {
        assert absoluteIndex >= 0
            : "absoluteIndex >= 0";

        final OpCode optimalOpCode;

        optimalOpCode = getLocalStoreOpCode(type, absoluteIndex);

        emit(optimalOpCode);

        final OperandType operandType = optimalOpCode.getOperandType();

        if (operandType == OperandType.NoOperands) {
            return;
        }

        if (absoluteIndex > MAX_BYTE) {
            emitShortOperand(absoluteIndex);
        }
        else {
            emitByteOperand(absoluteIndex);
        }
    }

    private static OpCode getLocalLoadOpCode(final Type<?> type, final int localIndex) {
        switch (type.getKind()) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                switch (localIndex) {
                    case 0:
                        return OpCode.ILOAD_0;
                    case 1:
                        return OpCode.ILOAD_1;
                    case 2:
                        return OpCode.ILOAD_2;
                    case 3:
                        return OpCode.ILOAD_3;
                    default:
                        return localIndex > MAX_BYTE ? OpCode.ILOAD_W : OpCode.ILOAD;
                }

            case LONG:
                switch (localIndex) {
                    case 0:
                        return OpCode.LLOAD_0;
                    case 1:
                        return OpCode.LLOAD_1;
                    case 2:
                        return OpCode.LLOAD_2;
                    case 3:
                        return OpCode.LLOAD_3;
                    default:
                        return localIndex > MAX_BYTE ? OpCode.LLOAD_W : OpCode.LLOAD;
                }

            case FLOAT:
                switch (localIndex) {
                    case 0:
                        return OpCode.FLOAD_0;
                    case 1:
                        return OpCode.FLOAD_1;
                    case 2:
                        return OpCode.FLOAD_2;
                    case 3:
                        return OpCode.FLOAD_3;
                    default:
                        return localIndex > MAX_BYTE ? OpCode.FLOAD_W : OpCode.FLOAD;
                }

            case DOUBLE:
                switch (localIndex) {
                    case 0:
                        return OpCode.DLOAD_0;
                    case 1:
                        return OpCode.DLOAD_1;
                    case 2:
                        return OpCode.DLOAD_2;
                    case 3:
                        return OpCode.DLOAD_3;
                    default:
                        return localIndex > MAX_BYTE ? OpCode.DLOAD_W : OpCode.DLOAD;
                }

            default:
                switch (localIndex) {
                    case 0:
                        return OpCode.ALOAD_0;
                    case 1:
                        return OpCode.ALOAD_1;
                    case 2:
                        return OpCode.ALOAD_2;
                    case 3:
                        return OpCode.ALOAD_3;
                    default:
                        return localIndex > MAX_BYTE ? OpCode.ALOAD_W : OpCode.ALOAD;
                }
        }
    }

    private static OpCode getLocalStoreOpCode(final Type<?> type, final int localIndex) {
        switch (type.getKind()) {
            case BOOLEAN:
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
                switch (localIndex) {
                    case 0:
                        return OpCode.ISTORE_0;
                    case 1:
                        return OpCode.ISTORE_1;
                    case 2:
                        return OpCode.ISTORE_2;
                    case 3:
                        return OpCode.ISTORE_3;
                    default:
                        return OpCode.ISTORE;
                }

            case LONG:
                switch (localIndex) {
                    case 0:
                        return OpCode.LSTORE_0;
                    case 1:
                        return OpCode.LSTORE_1;
                    case 2:
                        return OpCode.LSTORE_2;
                    case 3:
                        return OpCode.LSTORE_3;
                    default:
                        return OpCode.LSTORE;
                }

            case FLOAT:
                switch (localIndex) {
                    case 0:
                        return OpCode.FSTORE_0;
                    case 1:
                        return OpCode.FSTORE_1;
                    case 2:
                        return OpCode.FSTORE_2;
                    case 3:
                        return OpCode.FSTORE_3;
                    default:
                        return OpCode.FSTORE;
                }

            case DOUBLE:
                switch (localIndex) {
                    case 0:
                        return OpCode.DSTORE_0;
                    case 1:
                        return OpCode.DSTORE_1;
                    case 2:
                        return OpCode.DSTORE_2;
                    case 3:
                        return OpCode.DSTORE_3;
                    default:
                        return OpCode.DSTORE;
                }

            default:
                switch (localIndex) {
                    case 0:
                        return OpCode.ASTORE_0;
                    case 1:
                        return OpCode.ASTORE_1;
                    case 2:
                        return OpCode.ASTORE_2;
                    case 3:
                        return OpCode.ASTORE_3;
                    default:
                        return OpCode.ASTORE;
                }
        }
    }

    final int translateParameter(final int localIndex) {
        int index = localIndex;

        if (methodBuilder != null) {
            if (!methodBuilder.isStatic()) {
                ++index;
            }

            final TypeList parameterTypes = methodBuilder.getParameterTypes();

            for (int i = 0, n = parameterTypes.size(); i < localIndex && i < n; i++) {
                final TypeKind kind = parameterTypes.get(i).getKind();
                if (kind == TypeKind.LONG || kind == TypeKind.DOUBLE) {
                    ++index;
                }
            }
        }

        return index;
    }

    final int translateLocal(final int localIndex) {
        int index = 0;

        if (methodBuilder != null) {
            index += translateParameter(methodBuilder.parameterBuilders.length);
        }

        for (int i = 0; i < localIndex; i++) {
            final TypeKind kind = locals[i].getLocalType().getKind();
            index += kind == TypeKind.LONG || kind == TypeKind.DOUBLE ? 2 : 1;
        }

        return index;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Array Load/Store Operations">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ARRAY LOAD/STORE OPERATIONS                                                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void emitLoadElement(final Type<?> elementType) {
        VerifyArgument.notNull(elementType, "elementType");

        switch (elementType.getKind()) {
            case BOOLEAN:
            case BYTE:
                emit(OpCode.BALOAD);
                break;

            case SHORT:
                emit(OpCode.SALOAD);
                break;

            case INT:
                emit(OpCode.IALOAD);
                break;

            case LONG:
                emit(OpCode.LALOAD);
                break;

            case CHAR:
                emit(OpCode.CALOAD);
                break;

            case FLOAT:
                emit(OpCode.FALOAD);
                break;

            case DOUBLE:
                emit(OpCode.DALOAD);
                break;

            case ARRAY:
            case DECLARED:
            case ERROR:
            case TYPEVAR:
            case WILDCARD:
                emit(OpCode.AALOAD);
                break;

            default:
                throw Error.invalidType(elementType);
        }
    }

    public void emitStoreElement(final Type<?> elementType) {
        VerifyArgument.notNull(elementType, "elementType");

        switch (elementType.getKind()) {
            case BOOLEAN:
            case BYTE:
                emit(OpCode.BASTORE);
                break;

            case SHORT:
                emit(OpCode.SASTORE);
                break;

            case INT:
                emit(OpCode.IASTORE);
                break;

            case LONG:
                emit(OpCode.LASTORE);
                break;

            case CHAR:
                emit(OpCode.CASTORE);
                break;

            case FLOAT:
                emit(OpCode.FASTORE);
                break;

            case DOUBLE:
                emit(OpCode.DASTORE);
                break;

            case ARRAY:
            case DECLARED:
            case ERROR:
            case TYPEVAR:
            case WILDCARD:
                emit(OpCode.AASTORE);
                break;

            default:
                throw Error.invalidType(elementType);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Field Operations">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FIELD OPERATIONS                                                                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void getField(final FieldInfo field) {
        VerifyArgument.notNull(field, "field");

        if (field.isStatic()) {
            emit(OpCode.GETSTATIC, field);
        }
        else {
            emit(OpCode.GETFIELD, field);
        }
    }

    public void putField(final FieldInfo field) {
        VerifyArgument.notNull(field, "field");

        if (field.isStatic()) {
            emit(OpCode.PUTSTATIC, field);
        }
        else {
            emit(OpCode.PUTFIELD, field);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constants">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONSTANTS                                                                                                          //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean canEmitConstant(final Object value, final Type<?> type) {
        VerifyArgument.notNull(type, "type");

        return value == null ||
               value instanceof Enum<?> ||
               value instanceof Type<?> ||
               value instanceof Class<?> ||
               value instanceof MethodBase ||
               canEmitBytecodeConstant(type);
    }

    public void emitConstant(final Object value) {
        if (value == null) {
            emitNull();
        }
        else {
            emitConstant(value, Type.getType(value));
        }
    }

    public void emitConstantArray(final Object array) {
        VerifyArgument.notNull(array, "array");

        final int length = Array.getLength(array);
        final Type<?> arrayType = Type.getType(array);
        final Type<?> elementType = arrayType.getElementType();

        emitInteger(length);
        emitNewArray(arrayType);

        for (int i = 0; i < length; i++) {
            dup();
            emitInteger(i);
            emitConstant(Array.get(array, i));
            emitStoreElement(elementType);
        }
    }

    public void emitConstant(final Object value, final Type<?> type) {
        if (value == null) {
            emitDefaultValue(type);
            return;
        }

        if (tryEmitConstant(value, type)) {
            return;
        }

        if (value instanceof Type<?>) {
            emitType((Type<?>)value);
            return;
        }

        if (value instanceof Class<?>) {
            emitType(Type.of((Class<?>) value));
            return;
        }

        if (value instanceof MethodBase) {
            emitMethod((MethodBase)value);
            return;
        }

        throw Error.valueMustBeConstant();
    }

    public void emitType(final Type<?> value) {
        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int typeToken = methodBuilder.getDeclaringType().getTypeToken(value);

        emitLoadConstant(typeToken);
    }

    public void emitMethod(final MethodBase value) {
        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int methodToken = methodBuilder.getDeclaringType().getMethodToken(value);

        emitLoadConstant(methodToken);
    }

    private boolean tryEmitConstant(final Object value, final Type<?> type) {
        final Type<?> unboxedType = TypeUtils.getUnderlyingPrimitiveOrSelf(type);

        switch (unboxedType.getKind()) {
            case BOOLEAN:
                emitBoolean((Boolean)value);
                return true;

            case BYTE:
                emitByte((Byte)value);
                return true;

            case SHORT:
                emitShort((Short)value);
                return true;

            case INT:
                emitInteger((Integer)value);
                return true;

            case LONG:
                emitLong((Long)value);
                return true;

            case CHAR:
                emitCharacter((Character)value);
                return true;

            case FLOAT:
                emitFloat((Float)value);
                return true;

            case DOUBLE:
                emitDouble((Double)value);
                return true;

            default:
                if (unboxedType == Types.String) {
                    emitString((String)value);
                    return true;
                }
                if (unboxedType.isEnum()) {
                    getField(unboxedType.getField(value.toString()));
                    return true;
                }
                return false;
        }
    }

    public void emitNull() {
        emit(OpCode.ACONST_NULL);
    }

    public void emitDefaultValue(final Type<?> type) {
        VerifyArgument.notNull(type, "type");

        switch (type.getKind()) {
            case BOOLEAN:
                emit(OpCode.ICONST_0);
                break;

            case BYTE:
                emit(OpCode.ICONST_0);
                emit(OpCode.I2B);
                break;

            case SHORT:
                emit(OpCode.ICONST_0);
                emit(OpCode.I2S);
                break;

            case INT:
                emit(OpCode.ICONST_0);
                break;

            case LONG:
                emit(OpCode.LCONST_0);
                break;

            case CHAR:
                emit(OpCode.ICONST_0);
                emit(OpCode.I2C);
                break;

            case FLOAT:
                emit(OpCode.FCONST_0);
                break;

            case DOUBLE:
                emit(OpCode.DCONST_0);
                break;

            case NULL:
            case ARRAY:
            case DECLARED:
            case ERROR:
            case TYPEVAR:
                emit(OpCode.ACONST_NULL);
                break;

            case VOID:
                emit(OpCode.NOP);
                break;

            default:
                throw Error.invalidType(type);
        }
    }

    public void emitBoolean(final boolean value) {
        emit(value ? OpCode.ICONST_1 : OpCode.ICONST_0);
    }

    public void emitByte(final byte value) {
        emit(OpCode.BIPUSH, value);
    }

    public void emitCharacter(final char value) {
        if (value <= MAX_BYTE) {
            emitByte((byte)value);
        }
        else {
            emitShort((short)value);
        }
    }

    public void emitShort(final short value) {
        emit(OpCode.SIPUSH, value);
    }

    public void emitInteger(final int value) {
        switch (value) {
            case -1:
                emit(OpCode.ICONST_M1);
                return;
            case 0:
                emit(OpCode.ICONST_0);
                return;
            case 1:
                emit(OpCode.ICONST_1);
                return;
            case 2:
                emit(OpCode.ICONST_2);
                return;
            case 3:
                emit(OpCode.ICONST_3);
                return;
            case 4:
                emit(OpCode.ICONST_4);
                return;
            case 5:
                emit(OpCode.ICONST_5);
                return;
        }

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int constantToken = methodBuilder.getDeclaringType().getConstantToken(value);

        emitLoadConstant(constantToken);
    }

    public void emitLong(final long value) {
        if (value == 0L) {
            emit(OpCode.LCONST_0);
            return;
        }

        if (value == 1L) {
            emit(OpCode.LCONST_1);
            return;
        }

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int constantToken = methodBuilder.getDeclaringType().getConstantToken(value);

        emitLoadLongConstant(constantToken);
    }

    public void emitFloat(final float value) {
        if (value == 0f) {
            emit(OpCode.FCONST_0);
            return;
        }

        if (value == 1f) {
            emit(OpCode.FCONST_1);
            return;
        }

        if (value == 2f) {
            emit(OpCode.FCONST_2);
            return;
        }

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int constantToken = methodBuilder.getDeclaringType().getConstantToken(value);

        emitLoadConstant(constantToken);
    }

    public void emitDouble(final double value) {
        if (value == 0d) {
            emit(OpCode.DCONST_0);
            return;
        }

        if (value == 1d) {
            emit(OpCode.DCONST_1);
            return;
        }

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int constantToken = methodBuilder.getDeclaringType().getConstantToken(value);

        emitLoadLongConstant(constantToken);
    }

    public void emitString(final String value) {
        if (value == null) {
            emitNull();
            return;
        }

        final MethodBuilder methodBuilder = this.methodBuilder;

        if (methodBuilder == null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        final int stringToken = methodBuilder.getDeclaringType().getStringToken(value);

        emitLoadConstant(stringToken);
    }

    protected void emitLoadConstant(final int token) {
        if (token < MIN_BYTE || token > MAX_BYTE) {
            emit(OpCode.LDC_W);
            emitShortOperand(token);
        }
        else {
            emit(OpCode.LDC);
            emitByteOperand(token);
        }
    }

    protected void emitLoadLongConstant(final int token) {
        emit(OpCode.LDC2_W);
        emitShortOperand(token);
    }

    private static boolean canEmitBytecodeConstant(final Type<?> type) {
        final Type<?> unboxedType = TypeUtils.getUnderlyingPrimitiveOrSelf(type);

        switch (unboxedType.getKind()) {
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case CHAR:
            case FLOAT:
            case DOUBLE:
                return true;

            default:
                return type.isEquivalentTo(Types.String);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Boxing and Conversion Operations">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BOXING AND CONVERSION OPERATIONS                                                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void emitBox(final Type<?> type) {
        final MethodInfo boxMethod = TypeUtils.getBoxMethod(
            VerifyArgument.notNull(type, "type")
        );

        if (boxMethod != null) {
            call(OpCode.INVOKESTATIC, boxMethod);
        }
    }

    public void emitUnbox(final Type<?> type) {
        final MethodInfo unboxMethod = TypeUtils.getUnboxMethod(
            VerifyArgument.notNull(type, "type")
        );

        if (unboxMethod != null) {
            call(OpCode.INVOKEVIRTUAL, unboxMethod);
        }
    }

    public void emitConversion(final Type<?> sourceType, final Type<?> targetType) {
        VerifyArgument.notNull(sourceType, "sourceType");
        VerifyArgument.notNull(targetType, "targetType");

        if (sourceType == targetType || sourceType.isEquivalentTo(targetType)) {
            return;
        }

        if (sourceType == PrimitiveTypes.Void || targetType == PrimitiveTypes.Void) {
            throw Error.cannotConvertToOrFromVoid();
        }

        final boolean isTypeSourceBoxed = TypeUtils.isAutoUnboxed(sourceType);
        final boolean isTypeTargetBoxed = TypeUtils.isAutoUnboxed(targetType);

        final Type<?> unboxedSourceType = TypeUtils.getUnderlyingPrimitiveOrSelf(sourceType);
        final Type<?> unboxedTargetType = TypeUtils.getUnderlyingPrimitiveOrSelf(targetType);

        if (sourceType.isInterface() ||   // interface cast
            targetType.isInterface() ||
            sourceType == Types.Object ||   // boxing cast
            targetType == Types.Object) {

            emitCastToType(sourceType, targetType);
        }
        else if (isTypeSourceBoxed || isTypeTargetBoxed) {
            emitBoxingConversion(sourceType, targetType);
        }
        else if ((!unboxedSourceType.isPrimitive() || !unboxedTargetType.isPrimitive()) // primitive runtime conversion
                 && (unboxedSourceType.isAssignableFrom(unboxedTargetType)              // down cast
                     || unboxedTargetType.isAssignableFrom(unboxedSourceType)))         // up cast
        {
            emitCastToType(sourceType, targetType);
        }
        else if (sourceType.isArray() && targetType.isArray()) {
            emitCastToType(sourceType, targetType);
        }
        else {
            emitNumericConversion(sourceType, targetType);
        }
    }

    private void emitBoxingConversion(final Type<?> sourceType, final Type<?> targetType) {
        final boolean isSourceTypeBoxed = TypeUtils.isAutoUnboxed(sourceType);
        final boolean isTargetTypeBoxed = TypeUtils.isAutoUnboxed(targetType);

        assert isSourceTypeBoxed || isTargetTypeBoxed
            : "isSourceTypeBoxed || isTargetTypeBoxed";

        if (isSourceTypeBoxed && isTargetTypeBoxed) {
            emitBoxedToBoxedConversion(sourceType, targetType);
        }
        else if (isSourceTypeBoxed) {
            emitBoxedToUnboxedConversion(sourceType, targetType);
        }
        else {
            emitUnboxedToBoxedConversion(sourceType, targetType);
        }
    }

    private void emitUnboxedToBoxedConversion(final Type<?> sourceType, final Type<?> targetType) {
        assert sourceType.isPrimitive() && TypeUtils.isAutoUnboxed(targetType)
            : "sourceType.isPrimitive() && TypeUtils.isAutoUnboxed(targetType)";

        final Type<?> unboxedTargetType = TypeUtils.getUnderlyingPrimitive(targetType);
        final LocalBuilder targetLocal = declareLocal(targetType);

        emitConversion(sourceType, unboxedTargetType);
        emitBox(targetType);
    }

    private void emitBoxedToUnboxedConversion(final Type<?> sourceType, final Type<?> targetType) {
        assert TypeUtils.isAutoUnboxed(sourceType) && targetType.isPrimitive()
            : "TypeUtils.isAutoUnboxed(sourceType) && targetType.isPrimitive()";

        if (targetType.isPrimitive()) {
            emitBoxedToUnboxedNumericConversion(sourceType, targetType);
        }
        else {
            emitBoxedToReferenceConversion(sourceType);
        }
    }

    private void emitBoxedToReferenceConversion(final Type<?> sourceType) {
        assert TypeUtils.isAutoUnboxed(sourceType)
            : "TypeUtils.isAutoUnboxed(sourceType)";

        emitBox(sourceType);
    }

    private void emitBoxedToUnboxedNumericConversion(final Type<?> sourceType, final Type<?> targetType) {
        assert TypeUtils.isAutoUnboxed(sourceType) && !TypeUtils.isAutoUnboxed(targetType)
            : "TypeUtils.isAutoUnboxed(sourceType) && !TypeUtils.isAutoUnboxed(targetType)";

        final MethodInfo coercionMethod = TypeUtils.getCoercionMethod(sourceType, targetType);

        if (coercionMethod != null) {
            call(coercionMethod);
        }
        else {
            final Type<?> unboxedSourceType = TypeUtils.getUnderlyingPrimitive(sourceType);

            emitUnbox(sourceType);
            emitConversion(unboxedSourceType, targetType);
        }
    }

    private void emitBoxedToBoxedConversion(final Type<?> sourceType, final Type<?> targetType) {
        assert TypeUtils.isAutoUnboxed(sourceType) && TypeUtils.isAutoUnboxed(targetType)
            : "TypeUtils.isAutoUnboxed(sourceType) && TypeUtils.isAutoUnboxed(targetType)";

        final LocalBuilder sourceLocal = declareLocal(sourceType);
        final LocalBuilder targetLocal = declareLocal(targetType);

        final Type<?> unboxedSourceType = TypeUtils.getUnderlyingPrimitive(sourceType);
        final Type<?> unboxedTargetType = TypeUtils.getUnderlyingPrimitive(targetType);

        final Label ifNull = defineLabel();
        final Label end = defineLabel();

        emitStore(sourceLocal);
        emitStore(targetLocal);

        // test source value for null
        emitLoad(sourceLocal);
        emit(OpCode.IFNULL, ifNull);

        // unbox source
        emitLoad(sourceLocal);
        emitUnbox(sourceType);

        // convert unboxed source to unboxed target type
        emitConversion(unboxedSourceType, unboxedTargetType);

        // box target
        emitBox(targetType);
        emitStore(targetLocal);
        emitGoto(end);

        // if source was null, set target to null
        markLabel(ifNull);
        emitNull();
        emitStore(targetLocal);

        // target is now on top of stack
        markLabel(end);
        emitLoad(targetLocal);
    }

    private void emitCastToType(final Type<?> sourceType, final Type<?> targetType) {
        if (!sourceType.isPrimitive() && targetType.isPrimitive()) {
            final Type<?> boxedTargetType = TypeUtils.getBoxedType(targetType);

            if (!sourceType.isEquivalentTo(boxedTargetType)) {
                emitCastToType(sourceType, boxedTargetType);
            }

            emitUnbox(targetType);
        }
        else if (sourceType.isPrimitive() && !targetType.isPrimitive()) {
            final Type<?> boxedSourceType = TypeUtils.getBoxedType(sourceType);

            emitBox(sourceType);

            if (!targetType.isAssignableFrom(boxedSourceType)) {
                emitCastToType(boxedSourceType, targetType);
            }
        }
        else if (!sourceType.isPrimitive() && !targetType.isPrimitive()) {
            if (targetType == Types.Object) {
                return;
            }
            emit(OpCode.CHECKCAST, targetType);
        }
        else {
            throw Error.invalidCast(sourceType, targetType);
        }
    }

    private void emitNumericConversion(final Type<?> sourceType, final Type<?> targetType) {
        final TypeKind sourceKind = sourceType.getKind();
        final TypeKind targetKind = targetType.getKind();

        if (sourceKind == targetKind) {
            return;
        }

        switch (targetKind) {
            case BOOLEAN: {
                throw Error.invalidCast(sourceType, targetType);
            }

            case BYTE: {
                switch (sourceKind) {
                    case BOOLEAN:
                    case CHAR:
                    case SHORT:
                    case INT:
                        emit(OpCode.I2B);
                        return;

                    case LONG:
                        emit(OpCode.L2I);
                        emit(OpCode.I2B);
                        return;

                    case FLOAT:
                        emit(OpCode.F2I);
                        emit(OpCode.I2B);
                        return;

                    case DOUBLE:
                        emit(OpCode.D2I);
                        emit(OpCode.I2B);
                        return;
                }
                break;
            }

            case SHORT: {
                switch (sourceKind) {
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case INT:
                        emit(OpCode.I2S);
                        return;

                    case LONG:
                        emit(OpCode.L2I);
                        emit(OpCode.I2S);
                        return;

                    case FLOAT:
                        emit(OpCode.F2I);
                        emit(OpCode.I2S);
                        return;

                    case DOUBLE:
                        emit(OpCode.D2I);
                        emit(OpCode.I2S);
                        return;
                }
                break;
            }

            case INT: {
                switch (sourceKind) {
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case SHORT:
                        return;

                    case LONG:
                        emit(OpCode.L2I);
                        return;

                    case FLOAT:
                        emit(OpCode.F2I);
                        return;

                    case DOUBLE:
                        emit(OpCode.D2I);
                        return;
                }
                break;
            }

            case LONG: {
                switch (sourceKind) {
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case SHORT:
                    case INT:
                        emit(OpCode.I2L);
                        return;

                    case FLOAT:
                        emit(OpCode.F2L);
                        return;

                    case DOUBLE:
                        emit(OpCode.D2L);
                        return;
                }
                break;
            }

            case CHAR: {
                switch (sourceKind) {
                    case BOOLEAN:
                    case BYTE:
                    case SHORT:
                    case INT:
                        emit(OpCode.I2C);
                        return;

                    case LONG:
                        emit(OpCode.L2I);
                        emit(OpCode.I2C);
                        return;

                    case FLOAT:
                        emit(OpCode.F2I);
                        emit(OpCode.I2C);
                        return;

                    case DOUBLE:
                        emit(OpCode.D2I);
                        emit(OpCode.I2C);
                        return;
                }
                break;
            }

            case FLOAT: {
                switch (sourceKind) {
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case SHORT:
                    case INT:
                        emit(OpCode.I2F);
                        return;

                    case LONG:
                        emit(OpCode.L2F);
                        return;

                    case DOUBLE:
                        emit(OpCode.D2F);
                        return;
                }
                break;
            }

            case DOUBLE: {
                switch (sourceKind) {
                    case BOOLEAN:
                    case BYTE:
                    case CHAR:
                    case SHORT:
                    case INT:
                        emit(OpCode.I2D);
                        return;

                    case LONG:
                        emit(OpCode.L2D);
                        return;

                    case FLOAT:
                        emit(OpCode.F2D);
                        return;
                }
                break;
            }
        }

        throw Error.invalidCast(sourceType, targetType);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Switches">

    private final static MethodInfo StringCharAtMethod;
    private final static MethodInfo StringLengthMethod;
    private final static MethodInfo ObjectEqualsMethod;
    private final static MethodInfo ObjectHashCodeMethod;

    static {
        StringCharAtMethod = Types.String.getMethod("charAt", PrimitiveTypes.Integer);
        StringLengthMethod = Types.String.getMethod("length");
        ObjectEqualsMethod = Types.Object.getMethod("equals", Types.Object);
        ObjectHashCodeMethod = Types.Object.getMethod("hashCode");
    }

    public void emitSwitch(final int[] keys, final SwitchCallback callback) {
        VerifyArgument.notNull(keys, "keys");

        final float density;

        if (keys.length == 0) {
            density = 0;
        }
        else {
            density = (float)keys.length / (keys[keys.length - 1] - keys[0] + 1);
        }

        emitSwitch(
            keys,
            callback, density >= 0.5f ? SwitchOptions.PreferTable
                                      : SwitchOptions.PreferLookup
        );
    }

    public void emitSwitch(final int[] keys, final SwitchCallback callback, final SwitchOptions options) {
        VerifyArgument.notNull(keys, "keys");
        VerifyArgument.notNull(callback, "callback");
        VerifyArgument.notNull(options, "options");

        final Label breakTarget = defineLabel();
        final Label defaultLabel = defineLabel();
        final int start = offset();
        final SwitchOptions resolvedOptions = resolveSwitchOptions(keys, options);

        try {
            if (keys.length > 0) {
                final int length = keys.length;
                final int minimum = keys[0];
                final int maximum = keys[length - 1];
                final int range = maximum - minimum + 1;

                if (resolvedOptions == SwitchOptions.PreferTable && range >= 0) {
                    final Label[] labels = new Label[range];

                    Arrays.fill(labels, defaultLabel);

                    for (final int key : keys) {
                        labels[key - minimum] = defineLabel();
                    }

                    emit(OpCode.TABLESWITCH);

                    for (int i = 0, padding = (4 - offset() % 4) % 4; i < padding; i++) {
                        emitByteOperand(0);
                    }

                    addFixup(defaultLabel, start, offset(), 4);

                    emitIntOperand(0);
                    emitIntOperand(minimum);
                    emitIntOperand(maximum);

                    for (final Label label : labels) {
                        addFixup(label, start, offset(), 4);
                        emitIntOperand(0);
                    }

                    for (int i = 0; i < range; i++) {
                        final Label label = labels[i];

                        if (label != defaultLabel) {
                            markLabel(label);
                            callback.emitCase(i + minimum, breakTarget);
                        }
                    }
                }
                else {
                    final Label[] labels = new Label[length];

                    for (int i = 0; i < length; i++) {
                        labels[i] = defineLabel();
                    }

                    emit(OpCode.LOOKUPSWITCH);

                    for (int i = 0, padding = (4 - offset() % 4) % 4; i < padding; i++) {
                        emitByteOperand(0);
                    }

                    addFixup(defaultLabel, start, offset(), 4);
                    emitIntOperand(0);

                    emitIntOperand(length);

                    for (int i = 0; i < length; i++) {
                        emitIntOperand(keys[i]);
                        addFixup(labels[i], start, offset(), 4);
                        emitIntOperand(0);
                    }

                    for (int i = 0; i < length; i++) {
                        markLabel(labels[i]);
                        callback.emitCase(keys[i], breakTarget);
                    }
                }
            }

            markLabel(defaultLabel);
            callback.emitDefault(breakTarget);
            markLabel(breakTarget);
        }
        catch (final Exception e) {
            throw Error.codeGenerationException(e);
        }
    }

    private static SwitchOptions resolveSwitchOptions(final int[] keys, final SwitchOptions options) {
        if (options == SwitchOptions.Default || options == null) {
            if (keys.length > 0 && (float)keys.length / (keys[keys.length - 1] - keys[0] + 1) >= 0.5f) {
                return SwitchOptions.PreferTable;
            }
            else {
                return SwitchOptions.PreferLookup;
            }
        }
        return options;
    }

    public <E extends Enum<E>> void emitSwitch(final E[] keys, final EnumSwitchCallback<E> callback) {
        emitSwitch(keys, callback, SwitchOptions.Default);
    }

    public <E extends Enum<E>> void emitSwitch(final E[] keys, final EnumSwitchCallback<E> callback, final SwitchOptions options) {
        VerifyArgument.noNullElements(keys, "keys");
        VerifyArgument.notNull(callback, "callback");
        VerifyArgument.notNull(options, "options");

        final int[] intKeys = new int[keys.length];

        for (int i = 0; i < keys.length; i++) {
            intKeys[i] = keys[i].ordinal();
        }

        emitSwitch(
            intKeys,
            new SwitchCallback() {
                @Override
                public void emitCase(final int key, final Label breakTarget) throws Exception {
                    final int keyIndex = ArrayUtilities.indexOf(intKeys, key);
                    callback.emitCase(keys[keyIndex], breakTarget);
                }

                @Override
                public void emitDefault(final Label breakTarget) throws Exception {
                    callback.emitDefault(breakTarget);
                }
            },
            options
        );
    }

    public void emitSwitch(final String[] keys, final StringSwitchCallback callback) {
        emitSwitch(keys, callback, SwitchOptions.Default);
    }

    public void emitSwitch(final String[] keys, final StringSwitchCallback callback, final SwitchOptions options) {
        try {
            if (options == SwitchOptions.PreferTrie) {
                emitStringTrieSwitch(keys, callback);
            }
            else {
                emitStringHashSwitch(
                    keys,
                    callback,
                    options != null ? options
                                    : SwitchOptions.Default);
            }
        }
        catch (final Exception e) {
            throw Error.codeGenerationException(e);
        }
    }

    private static Map<Integer, List<String>> getStringSwitchBuckets(
        final List<String> strings,
        final Func1<String, Integer> keyCallback) {

        final Map<Integer, List<String>> buckets = new HashMap<>();

        for (final String s : strings) {
            final Integer key = keyCallback.apply(s);
            List<String> bucket = buckets.get(key);
            if (bucket == null) {
                buckets.put(key, bucket = new LinkedList<>());
            }
            bucket.add(s);
        }

        return buckets;
    }

    private void emitStringTrieSwitch(final String[] keys, final StringSwitchCallback callback) throws Exception {
        final Label defaultLabel = defineLabel();
        final Label breakTarget = defineLabel();

        final Map<Integer, List<String>> buckets = getStringSwitchBuckets(
            Arrays.asList(keys),
            new Func1<String, Integer>() {
                @Override
                public Integer apply(final String s) {
                    return s.length();
                }
            });

        int i = 0;
        final int[] intKeys = new int[buckets.size()];

        for (final Integer key  : buckets.keySet()) {
            intKeys[i++] = key;
        }

        Arrays.sort(intKeys);

        dup();
        call(StringLengthMethod);

        emitSwitch(
            intKeys,
            new SwitchCallback() {
                @Override
                public void emitCase(final int key, final Label ignore) {
                    final List<String> bucket = buckets.get(key);
                    stringSwitchHelper(bucket, callback, defaultLabel, breakTarget, 0);
                }

                @Override
                public void emitDefault(final Label breakTarget) {
                    emitGoto(defaultLabel);
                }
            });

        markLabel(defaultLabel);
        pop();
        callback.emitDefault(breakTarget);
        markLabel(breakTarget);
    }

    private void stringSwitchHelper(
        final List<String> bucket,
        final StringSwitchCallback callback,
        final Label defaultLabel,
        final Label breakTarget,
        final int index) {

        final int length = bucket.get(0).length();

        final Map<Integer, List<String>> buckets = getStringSwitchBuckets(
            bucket,
            new Func1<String, Integer>
                () {
                @Override
                public Integer apply(final String s) {
                    return (int)s.charAt(index);
                }
            }
        );

        dup();
        emitInteger(index);
        call(StringCharAtMethod);

        int i = 0;
        final int[] intKeys = new int[buckets.size()];

        for (final Integer key  : buckets.keySet()) {
            intKeys[i++] = key;
        }

        Arrays.sort(intKeys);

        emitSwitch(
            intKeys,
            new SwitchCallback() {
                @Override
                public void emitCase(final int key, final Label ignore) throws Exception {
                    final List<String> bucket = buckets.get(key);
                    if (index + 1 == length) {
                        pop();
                        callback.emitCase(bucket.get(0), breakTarget);
                    }
                    else {
                        stringSwitchHelper(bucket, callback, defaultLabel, breakTarget, index + 1);
                    }
                }

                @Override
                public void emitDefault(final Label breakTarget) {
                    emitGoto(defaultLabel);
                }
            }
        );
    }

    private void emitStringHashSwitch(final String[] keys, final StringSwitchCallback callback, final SwitchOptions options) throws Exception {
        final Map<Integer, List<String>> buckets = getStringSwitchBuckets(
            Arrays.asList(keys),
            new Func1<String, Integer>() {
                @Override
                public Integer apply(final String s) {
                    return s.hashCode();
                }
            }
        );

        final Label defaultLabel = defineLabel();
        final Label breakTarget = defineLabel();

        dup();
        call(ObjectHashCodeMethod);

        int i = 0;
        final int[] intKeys = new int[buckets.size()];

        for (final Integer key : buckets.keySet()) {
            intKeys[i++] = key;
        }

        Arrays.sort(intKeys);

        emitSwitch(
            intKeys,
            new SwitchCallback() {
                @Override
                public void emitCase(final int key, final Label ignore)
                    throws Exception {
                    final List<String> bucket = buckets.get(key);
                    Label next = null;

                    for (final Iterator<String> it = bucket.iterator(); it.hasNext(); ) {
                        final String string = it.next();

                        if (next != null) {
                            markLabel(next);
                        }

                        if (it.hasNext()) {
                            dup();
                        }

                        emitString(string);
                        call(ObjectEqualsMethod);

                        if (it.hasNext()) {
                            emit(OpCode.IFEQ, next = defineLabel());
                            pop();
                        }
                        else {
                            emit(OpCode.IFEQ, defaultLabel);
                        }

                        callback.emitCase(string, breakTarget);
                    }
                }

                @Override
                public void emitDefault(final Label breakTarget) {
                    pop();
                }
            },
            options
        );

        markLabel(defaultLabel);
        callback.emitDefault(breakTarget);
        markLabel(breakTarget);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Internal Methods">

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INTERNAL METHODS                                                                                                   //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    final void emitByteOperand(final int value) {
        _codeStream.putByte(value);
    }

    final void emitCharOperand(final char value) {
        _codeStream.putShort(value);
    }

    final void emitShortOperand(final int value) {
        _codeStream.putShort(value);
    }

    final void emitIntOperand(final int value) {
        _codeStream.putInt(value);
    }

    final void emitLongOperand(final long value) {
        _codeStream.putLong(value);
    }

    final void emitFloatOperand(final float value) {
        emitIntOperand(Float.floatToIntBits(value));
    }

    final void emitDoubleOperand(final double value) {
        emitLongOperand(Double.doubleToRawLongBits(value));
    }

    void internalEmit(final OpCode opCode) {
//        System.out.println(opCode.toString());
        if (opCode.getSize() == 1) {
            _codeStream.putByte((byte)(opCode.getCode() & 0xFF));
        }
        else {
            _codeStream.putByte((byte)((opCode.getCode() >> 8) & 0xFF));
            _codeStream.putByte((byte)((opCode.getCode() >> 0) & 0xFF));
        }
        updateStackSize(opCode, opCode.getStackChange());
    }

    static byte getByteOperand(final byte[] codes, final int index) {
        return codes[index];
    }

    static char getCharOperand(final byte[] codes, final int index) {
        final int hi = ((codes[index + 0] & 0xFF) << 8);
        final int lo = ((codes[index + 1] & 0xFF) << 0);
        return (char)(hi + lo);
    }

    static short getShortOperand(final byte[] codes, final int index) {
        final int hi = ((codes[index + 0] & 0xFF) << 8);
        final int lo = ((codes[index + 1] & 0xFF) << 0);
        return (short)(hi + lo);
    }

    static int getIntOperand(final byte[] codes, final int index) {
        final int hh = ((codes[index + 0] & 0xFF) << 24);
        final int hl = ((codes[index + 1] & 0xFF) << 16);
        final int lh = ((codes[index + 2] & 0xFF) << 8);
        final int ll = ((codes[index + 3] & 0xFF) << 0);
        return hh + hl + lh + ll;
    }

    static long getLongOperand(final byte[] codes, final int index) {
        return ((long)getIntOperand(codes, index) << 32) +
               ((long)getIntOperand(codes, index) << 0);
    }

    static float getFloatOperand(final byte[] codes, final int index) {
        return Float.intBitsToFloat(getIntOperand(codes, index));
    }

    static double getDoubleOperand(final byte[] codes, final int index) {
        return Double.longBitsToDouble(getIntOperand(codes, index));
    }

    static void putByteOperand(final byte[] codes, final int index, final byte value) {
        codes[index] = value;
    }

    static void putCharOperand(final byte[] codes, final int index, final char value) {
        codes[index + 0] = (byte)((value >> 8) & 0xFF);
        codes[index + 1] = (byte)((value >> 0) & 0xFF);
    }

    static void putShortOperand(final byte[] codes, final int index, final short value) {
        codes[index + 0] = (byte)((value >> 8) & 0xFF);
        codes[index + 1] = (byte)((value >> 0) & 0xFF);
    }

    static void putIntOperand(final byte[] codes, final int index, final int value) {
        codes[index + 0] = (byte)((value >> 24) & 0xFF);
        codes[index + 1] = (byte)((value >> 16) & 0xFF);
        codes[index + 2] = (byte)((value >> 8) & 0xFF);
        codes[index + 3] = (byte)((value >> 0) & 0xFF);
    }

    static void putLongOperand(final byte[] codes, final int index, final long value) {
        codes[index + 0] = (byte)((value >> 56) & 0xFF);
        codes[index + 1] = (byte)((value >> 48) & 0xFF);
        codes[index + 2] = (byte)((value >> 40) & 0xFF);
        codes[index + 3] = (byte)((value >> 32) & 0xFF);
        codes[index + 4] = (byte)((value >> 24) & 0xFF);
        codes[index + 5] = (byte)((value >> 16) & 0xFF);
        codes[index + 6] = (byte)((value >> 8) & 0xFF);
        codes[index + 7] = (byte)((value >> 0) & 0xFF);
    }

    static void putFloatOperand(final byte[] codes, final int index, final float value) {
        putIntOperand(codes, index, Float.floatToRawIntBits(value));
    }

    static void putDoubleOperand(final byte[] codes, final int index, final double value) {
        putLongOperand(codes, index, Double.doubleToRawLongBits(value));
    }

    private void addFixup(final Label label, final int offsetOrigin, final int fixupPosition, final int operandSize) {
        //
        // Notes the label, offset origin, position, and instruction size of a new fixup.
        // Expands all of the fixup arrays as appropriate.
        //

        if (_fixupData == null) {
            _fixupData = new __FixupData[DefaultFixupArraySize];
        }

        if (_fixupCount >= _fixupData.length) {
            _fixupData = enlargeArray(_fixupData);
        }

        if (_fixupData[_fixupCount] == null) {
            _fixupData[_fixupCount] = new __FixupData();
        }

        _fixupData[_fixupCount].offsetOrigin = offsetOrigin;
        _fixupData[_fixupCount].fixupPosition = fixupPosition;
        _fixupData[_fixupCount].fixupLabel = label;
        _fixupData[_fixupCount].operandSize = operandSize;

        _fixupCount++;
    }

    final void ensureCapacity(final int size) {
        _codeStream.ensureCapacity(size);
    }

    final void updateStackSize(final OpCode opCode, final int stackChange) {
        // Updates internal variables for keeping track of the stack size
        // requirements for the function.  stackChange specifies the amount
        // by which the stack size needs to be updated.

        // Special case for the Return.  Returns pops 1 if there is a
        // non-void return value.

        // Update the running stack size.  _maxMidStack specifies the maximum
        // amount of stack required for the current basic block irrespective of
        // where you enter the block.
        _maxMidStackCur += stackChange;

        if (_maxMidStackCur > _maxMidStack) {
            _maxMidStack = _maxMidStackCur;
        }
        else if (_maxMidStackCur < 0) {
            _maxMidStackCur = 0;
        }

        // If the current instruction signifies end of a basic, which basically
        // means an unconditional branch, add _maxMidStack to _maxStackSize.
        // _maxStackSize will eventually be the sum of the stack requirements for
        // each basic block.

        if (opCode.endsUnconditionalJumpBlock()) {
            _maxStackSize += _maxMidStack;
            _maxMidStack = 0;
            _maxMidStackCur = 0;
        }
    }

    private int getLabelPosition(final Label label) {
        // Gets the position in the stream of a particular label.
        // Verifies that the label exists and that it has been given a value.

        final int index = label.getLabelValue();

        if (index < 0 || index >= _labelCount) {
            throw Error.badLabel();
        }

        if (_labelList[index] < 0) {
            throw Error.badLabelContent();
        }

        return _labelList[index];
    }

    final byte[] bakeByteArray() {
        // bakeByteArray() is a package private function designed to be called by
        // MethodBuilder to do all of the fix-ups and return a new byte array
        // representing the byte stream with labels resolved, etc.

        final int newSize;
        final byte[] newBytes;

        int updateAddress;

        if (_currentExceptionStackCount != 0) {
            throw Error.unclosedExceptionBlock();
        }

        if (_codeStream.getLength() == 0) {
            return null;
        }

        newSize = _codeStream.getLength();
        newBytes = Arrays.copyOf(_codeStream.getData(), newSize);

        // Do the fix-ups.  This involves iterating over all of the labels and replacing
        // them with their proper values.
        for (int i = 0; i < _fixupCount; i++) {
            final int fixupPosition = _fixupData[i].fixupPosition;
            updateAddress = getLabelPosition(_fixupData[i].fixupLabel) - _fixupData[i].offsetOrigin /*getLabelPosition(_fixupData[i].fixupLabel) -
                            (fixupPosition + _fixupData[i].adjustment)*/;

            // Handle single byte instructions
            // Throw an exception if they're trying to store a jump in a single byte instruction that doesn't fit.
            if (_fixupData[i].operandSize == 2) {
                // Verify that our two-byte arg will fit into a Short.
                if (updateAddress < Short.MIN_VALUE || updateAddress > Short.MAX_VALUE) {
                    throw Error.branchAddressTooLarge();
                }
                else {
                    putShortOperand(newBytes, fixupPosition, (short)updateAddress);
                }
            }
            else {
                // Emit the four-byte arg.
                putIntOperand(newBytes, fixupPosition, updateAddress);
            }
        }

        return newBytes;
    }

    static int[] enlargeArray(final int[] incoming) {
        return Arrays.copyOf(
            VerifyArgument.notNull(incoming, "incoming"),
            incoming.length * 2
        );
    }

    static <T> T[] enlargeArray(final T[] incoming) {
        return Arrays.copyOf(
            incoming,
            incoming.length * 2
        );
    }

    static byte[] enlargeArray(final byte[] incoming) {
        return Arrays.copyOf(
            VerifyArgument.notNull(incoming, "incoming"),
            incoming.length * 2
        );
    }

    static byte[] enlargeArray(final byte[] incoming, final int requiredSize) {
        return Arrays.copyOf(
            VerifyArgument.notNull(incoming, "incoming"),
            requiredSize
        );
    }

    final __ExceptionInfo[] getExceptions() {
        if (_currentExceptionStackCount != 0) {
            throw Error.unclosedExceptionBlock();
        }

        if (_exceptionCount == 0) {
            return null;
        }

        final __ExceptionInfo[] temp = Arrays.copyOf(_exceptions, _exceptionCount);

        sortExceptions(temp);

        return temp;
    }

    final Type<?>[] getUnhandledCheckedExceptions() {
        if (_unhandledExceptionCount == 0) {
            return Type.EmptyTypes;
        }

        return Arrays.copyOf(_unhandledExceptions, _unhandledExceptionCount);
    }

    final int getMaxStackSize() {
        return _maxStackSize;
    }

    private static void sortExceptions(final __ExceptionInfo[] exceptions) {
        int least;
        __ExceptionInfo temp;

        final int length = exceptions.length;

        for (int i = 0; i < length; i++) {
            least = i;

            for (int j = i + 1; j < length; j++) {
                if (exceptions[least].isInner(exceptions[j])) {
                    least = j;
                }
            }

            temp = exceptions[i];
            exceptions[i] = exceptions[least];
            exceptions[least] = temp;
        }
    }

    private void registerCheckedExceptions(final MethodBase method) {
        final TypeList thrownTypes = method.getThrownTypes();

        if (thrownTypes == null || thrownTypes.isEmpty()) {
            return;
        }

        for (int i = 0, n = thrownTypes.size(); i < n; i++) {
            final Type<?> thrownType = thrownTypes.get(i);

            if (Types.RuntimeException.isAssignableFrom(thrownType)) {
                continue;
            }

            for (int j = 0; j < _currentExceptionStackCount; j++) {
                final __ExceptionInfo exceptionInfo = _currentExceptionStack[j];
                for (final Type<?> caughtType : exceptionInfo._catchClass) {
                    if (caughtType == null) {
                        break;
                    }
                    if (caughtType.isAssignableFrom(thrownType)) {
                        return;
                    }
                }
            }

            if (_unhandledExceptions == null) {
                _unhandledExceptions = new Type<?>[DefaultExceptionArraySize];
                _unhandledExceptions[_unhandledExceptionCount++] = thrownType;
                return;
            }

            for (int j = 0; j < _unhandledExceptionCount; j++) {
                final Type<?> e = _unhandledExceptions[j];

                if (thrownType.isSubTypeOf(e)) {
                    return;
                }

                if (e.isSubTypeOf(thrownType)) {
                    _unhandledExceptions[j] = thrownType;
                    return;
                }
            }

            _unhandledExceptions[_unhandledExceptionCount++] = thrownType;
        }
    }

    // </editor-fold>
}