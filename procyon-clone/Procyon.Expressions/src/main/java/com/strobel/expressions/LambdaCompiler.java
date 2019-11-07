/*
 * LambdaCompiler.java
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

import com.strobel.compilerservices.Closure;
import com.strobel.compilerservices.DebugInfoGenerator;
import com.strobel.core.KeyedQueue;
import com.strobel.core.Pair;
import com.strobel.core.ReadOnlyList;
import com.strobel.core.StringUtilities;
import com.strobel.reflection.*;
import com.strobel.reflection.emit.*;
import com.strobel.util.ContractUtils;
import com.strobel.util.TypeUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Strobel
 */
@SuppressWarnings({ "unchecked", "PackageVisibleField", "UnusedParameters", "UnusedDeclaration", "ConstantConditions", "SameParameterValue" })
final class LambdaCompiler {
    final static AtomicInteger nextId = new AtomicInteger();
    final static Type<Closure> closureType = Type.of(Closure.class);

    final LambdaExpression<?> lambda;
    final TypeBuilder         typeBuilder;
    final MethodBuilder       methodBuilder;
    final CodeGenerator       generator;

    private final AnalyzedTree                      _tree;
    private final KeyedQueue<Type<?>, LocalBuilder> _freeLocals;
    private final BoundConstants                    _boundConstants;
    private final Map<LabelTarget, LabelInfo>       _labelInfo = new HashMap<>();

    private ConstructorBuilder _constructorBuilder;
    private boolean            _hasClosureArgument;
    private FieldBuilder       _closureField;
    private CompilerScope      _scope;
    private LabelScopeInfo     _labelBlock = new LabelScopeInfo(null, LabelScopeKind.Lambda);
    private FinallyInfo        _finallyInfo = new FinallyInfo(null, null);

    LambdaCompiler(final AnalyzedTree tree, final LambdaExpression<?> lambda) {
        this.lambda = lambda;

        typeBuilder = new TypeBuilder(
            getUniqueLambdaName(lambda.getName(), lambda.getCreationContext()),
            Modifier.PUBLIC | Modifier.FINAL,
            Types.Object,
            Type.list(lambda.getType())
        );

        final MethodInfo interfaceMethod = Expression.getInvokeMethod(lambda.getType(), true);

        methodBuilder = typeBuilder.defineMethod(
            interfaceMethod.getName(),
            Modifier.PUBLIC | Modifier.FINAL,
            interfaceMethod.getReturnType(),
            interfaceMethod.getParameters().getParameterTypes(),
            interfaceMethod.getThrownTypes()
        );

        final ParameterExpressionList lambdaParameters = lambda.getParameters();

        for (int i = 0, n = lambdaParameters.size(); i < n; i++) {
            methodBuilder.defineParameter(i, lambdaParameters.get(i).getName());
        }

        typeBuilder.defineMethodOverride(methodBuilder, interfaceMethod);

        generator = methodBuilder.getCodeGenerator();

        _tree = tree;
        _scope = tree.scopes.get(lambda);
        _boundConstants = tree.constants.get(lambda);
        _freeLocals = new KeyedQueue<>();

        if (_scope.needsClosure || _boundConstants.count() > 0) {
            ensureClosure();
        }

        initializeMethod();
    }

    LambdaCompiler(
        final AnalyzedTree tree,
        final LambdaExpression<?> lambda,
        final MethodBuilder method,
        final ConstructorBuilder constructor) {

        this.lambda = lambda;

        final TypeList parameterTypes = getParameterTypes(lambda);

        method.setReturnType(lambda.getReturnType());
        method.setParameters(parameterTypes);

        final ParameterExpressionList lambdaParameters = lambda.getParameters();

        for (int i = 0, n = lambdaParameters.size(); i < n; i++) {
            method.defineParameter(i, lambdaParameters.get(i).getName());
        }

        this.typeBuilder = method.getDeclaringType();
        this.methodBuilder = method;
        this._hasClosureArgument = false;
        this._closureField = null;
        this._constructorBuilder = constructor;

        this.generator = methodBuilder.getCodeGenerator();

        _freeLocals = new KeyedQueue<>();
        _tree = tree;
        _scope = tree.scopes.get(lambda);
        _boundConstants = tree.constants.get(lambda);

        initializeMethod();
    }

    private LambdaCompiler(final LambdaCompiler parent, final LambdaExpression lambda) {
        _tree = parent._tree;
        _freeLocals = parent._freeLocals;
        this.lambda = lambda;
        this.methodBuilder = parent.methodBuilder;
        this.generator = parent.generator;
        this.typeBuilder = parent.typeBuilder;
        _hasClosureArgument = parent._hasClosureArgument;
        _closureField = parent._closureField;
        _constructorBuilder = parent._constructorBuilder;
        _scope = _tree.scopes.get(lambda);
        _boundConstants = parent._boundConstants;
    }

    private TypeList getParameterTypes(final LambdaExpression<?> lambda) {
        final ParameterExpressionList parameters = lambda.getParameters();

        if (parameters.isEmpty()) {
            return TypeList.empty();
        }

        final Type<?>[] types = new Type<?>[parameters.size()];

        for (int i = 0, n = parameters.size(); i < n; i++) {
            final ParameterExpression parameter = parameters.get(i);
            types[i] = parameter.getType();
        }

        return Type.list(types);
    }

    ParameterExpressionList getParameters() {
        return lambda.getParameters();
    }

    boolean canEmitBoundConstants() {
        return _hasClosureArgument;
    }

    boolean emitDebugSymbols() {
        return _tree.getDebugInfoGenerator() != null;
    }

    void emitClosureArgument() {
        assert _hasClosureArgument
            : "must have a Closure argument";

        generator.emitThis();
        generator.getField(_closureField);
    }

    void emitLambdaArgument(final int index) {
        generator.emitLoadArgument(getLambdaArgument(index));
    }

    private FieldBuilder createStaticField(final String name, final Type type) {
        return typeBuilder.defineField(
            "<ExpressionCompilerImplementationDetails>{" + nextId.getAndIncrement() + "}" + name,
            type,
            Modifier.STATIC | Modifier.PRIVATE
        );
    }

    void initializeMethod() {
        // See if we can find a return label, so we can emit better IL
        addReturnLabel(lambda);
        _boundConstants.emitCacheConstants(this);
    }

    // See if this lambda has a return label
    // If so, we'll create it now and mark it as allowing the "ret" opcode
    // This allows us to generate better IL
    private void addReturnLabel(final LambdaExpression lambda) {
        Expression expression = lambda.getBody();

        while (true) {
            switch (expression.getNodeType()) {
                default:
                    // Didn't find return label.
                    return;

                case Label:
                    // Found the label.  We can directly return from this place only if
                    // the label type is reference assignable to the lambda return type.
                    final LabelTarget label = ((LabelExpression) expression).getTarget();

                    _labelInfo.put(
                        label,
                        new LabelInfo(
                            generator,
                            label,
                            TypeUtils.hasIdentityPrimitiveOrBoxingConversion(
                                lambda.getReturnType(),
                                label.getType()
                            )
                        )
                    );

                    return;

                case Block:
                    // Look in the last significant expression of a block.
                    final BlockExpression body = (BlockExpression) expression;

                    // Omit empty and debug info at the end of the block since they
                    // are not going to emit any bytecode.
                    for (int i = body.getExpressionCount() - 1; i >= 0; i--) {
                        expression = body.getExpression(i);
                        if (significant(expression)) {
                            break;
                        }
                    }

                    //noinspection UnnecessaryContinue
                    continue;
            }
        }
    }

    private static boolean notEmpty(final Expression node) {
        return !(node instanceof DefaultValueExpression) ||
               node.getType() != PrimitiveTypes.Void;
    }

    private static boolean significant(final Expression node) {
        if (node instanceof BlockExpression) {
            final BlockExpression block = (BlockExpression) node;
            for (int i = 0; i < block.getExpressionCount(); i++) {
                if (significant(block.getExpression(i))) {
                    return true;
                }
            }
            return false;
        }

        return notEmpty(node)/* && !(node instanceof DebugInfoExpression)*/;
    }

    @SuppressWarnings("unchecked")
    static <T> Delegate<T> compile(
        final LambdaExpression<T> lambda,
        final DebugInfoGenerator debugInfoGenerator) {

        // 1. Bind lambda
        final Pair<AnalyzedTree, LambdaExpression<T>> result = analyzeLambda(lambda);
        final AnalyzedTree tree = result.getFirst();
        final LambdaExpression<T> analyzedLambda = result.getSecond();

        tree.setDebugInfoGenerator(debugInfoGenerator);

        // 2. Create lambda compiler
        final LambdaCompiler c = new LambdaCompiler(tree, analyzedLambda);

        // 3. emit
        c.emitLambdaBody();

        final Type<T> generatedType = (Type<T>) c.typeBuilder.createType();
        final Class<T> generatedClass = generatedType.getErasedClass();

        return c.createDelegate(generatedClass);
    }

    @SuppressWarnings("unchecked")
    private <T> Delegate<T> createDelegate(final Class<T> generatedClass) {
        try {
            final T instance;

            if (_hasClosureArgument) {
                final Constructor<?> constructor = generatedClass.getConstructor(Closure.class);
                final Closure closure = new Closure(_boundConstants.toArray(), null);
                instance = (T) constructor.newInstance(closure);
            }
            else {
                instance = generatedClass.newInstance();
            }

            final MemberList<? extends MemberInfo> method = Type.of(generatedClass).findMembers(
                MemberType.methodsOnly(),
                BindingFlags.PublicInstanceDeclared,
                Type.FilterMethodOverride,
                Expression.getInvokeMethod(lambda.getType(), true)
            );

            return new Delegate<>(
                instance,
                (MethodInfo) method.get(0)
            );
        }
        catch (final ReflectiveOperationException e) {
            throw Error.couldNotCreateDelegate(e);
        }
    }

    static <T> void compile(
        final LambdaExpression<T> lambda,
        final MethodBuilder methodBuilder,
        final DebugInfoGenerator debugInfoGenerator) {

        // 1. Bind lambda
        final Pair<AnalyzedTree, LambdaExpression<T>> result = analyzeLambda(lambda);
        final AnalyzedTree tree = result.getFirst();
        final LambdaExpression<T> analyzedLambda = result.getSecond();

        tree.setDebugInfoGenerator(debugInfoGenerator);

        // 2. Create lambda compiler
        final LambdaCompiler c = new LambdaCompiler(tree, analyzedLambda, methodBuilder, null);

        // 3. emit
        c.emitLambdaBody();
    }

    private static <T> Pair<AnalyzedTree, LambdaExpression<T>> analyzeLambda(final LambdaExpression<T> lambda) {
        // Spill the stack for any exception handling blocks or other
        // constructs which require entering with an empty stack.
        final LambdaExpression<T> analyzedLambda = StackSpiller.analyzeLambda(lambda);

        // Bind any variable references in this lambda.
        return Pair.create(
            VariableBinder.bind(analyzedLambda),
            analyzedLambda
        );
    }

    LocalBuilder getNamedLocal(final Type type, final ParameterExpression variable) {
        assert type != null && variable != null
            : "type != null && variable != null";

        final LocalBuilder lb = generator.declareLocal(variable.getName(), type);

        if (emitDebugSymbols() && variable.getName() != null) {
            _tree.getDebugInfoGenerator().setLocalName(lb, variable.getName());
        }

        return lb;
    }

    int getLambdaArgument(final int index) {
        return index;// + (methodBuilder.isStatic() ? 0 : 1);
    }

    LocalBuilder getLocal(final Type<?> type) {
        assert type != null
            : "type != null";

        final LocalBuilder local = _freeLocals.poll(type);

        if (local != null) {
            assert type.equals(local.getLocalType())
                : "type.equals(local.getLocalType())";

            return local;
        }

        return generator.declareLocal(type);
    }

    void freeLocal(final LocalBuilder local) {
        if (local != null) {
            _freeLocals.offer(local.getLocalType(), local);
        }
    }

    private static final class CompilationFlags {
        private static final int EmitExpressionStart   = 0x0001;
        private static final int EmitNoExpressionStart = 0x0002;
        private static final int EmitAsDefaultType     = 0x0010;
        private static final int EmitAsVoidType        = 0x0020;
        private static final int EmitAsTail            = 0x0100; // at the tail position of a lambda; tail call can be safely emitted
        private static final int EmitAsMiddle          = 0x0200; // in the middle of a lambda; tail call can be emitted if it is in a return
        private static final int EmitAsNoTail          = 0x0400; // neither at the tail or in a return; or tail call is not turned on; no tail call is emitted

        private static final int EmitExpressionStartMask = 0x000f;
        private static final int EmitAsTypeMask          = 0x00f0;
        private static final int EmitAsTailCallMask      = 0x0f00;

        private static final int EmitAsSuperCall = 0x1000;
    }

    private static int updateEmitAsTailCallFlag(final int flags, final int newValue) {
        assert newValue == CompilationFlags.EmitAsTail ||
               newValue == CompilationFlags.EmitAsMiddle ||
               newValue == CompilationFlags.EmitAsNoTail;
        final int oldValue = flags & CompilationFlags.EmitAsTailCallMask;
        return flags ^ oldValue | newValue;
    }

    private static int updateEmitExpressionStartFlag(final int flags, final int newValue) {
        assert newValue == CompilationFlags.EmitExpressionStart ||
               newValue == CompilationFlags.EmitNoExpressionStart;
        final int oldValue = flags & CompilationFlags.EmitExpressionStartMask;
        return flags ^ oldValue | newValue;
    }

    private static int updateEmitAsTypeFlag(final int flags, final int newValue) {
        assert newValue == CompilationFlags.EmitAsDefaultType ||
               newValue == CompilationFlags.EmitAsVoidType;
        final int oldValue = flags & CompilationFlags.EmitAsTypeMask;
        return flags ^ oldValue | newValue;
    }

    void emitExpression(final Expression node) {
        emitExpression(
            node,
            CompilationFlags.EmitAsNoTail | CompilationFlags.EmitExpressionStart
        );
    }

    private int emitExpressionStart(final Expression node) {
        if (tryPushLabelBlock(node)) {
            return CompilationFlags.EmitExpressionStart;
        }
        return CompilationFlags.EmitNoExpressionStart;
    }

    private void emitExpressionEnd(final int flags) {
        if ((flags & CompilationFlags.EmitExpressionStartMask) == CompilationFlags.EmitExpressionStart) {
            popLabelBlock(_labelBlock.kind);
        }
    }

    private void emitExpression(final Expression node, final int flags) {
        assert node != null;

        final boolean emitStart = (flags & CompilationFlags.EmitExpressionStartMask) == CompilationFlags.EmitExpressionStart;

        final int startEmitted = emitStart
                                 ? emitExpressionStart(node)
                                 : CompilationFlags.EmitNoExpressionStart;

        // only pass tail call flags to emit the expression
        final int compilationFlags = flags & CompilationFlags.EmitAsTailCallMask;

        switch (node.getNodeType()) {
            case Add:
                emitBinaryExpression(node, compilationFlags);
                break;
            case And:
                emitBinaryExpression(node, compilationFlags);
                break;
            case AndAlso:
                emitAndAlsoBinaryExpression(node, compilationFlags);
                break;
            case ArrayLength:
                emitUnaryExpression(node, compilationFlags);
                break;
            case ArrayIndex:
                emitBinaryExpression(node, compilationFlags);
                break;
            case Call:
                emitMethodCallExpression(node, compilationFlags);
                break;
            case Coalesce:
                emitCoalesceBinaryExpression(node);
                break;
            case Conditional:
                emitConditionalExpression(node, compilationFlags);
                break;
            case Constant:
                emitConstantExpression(node);
                break;
            case Convert:
                emitConvertUnaryExpression(node, compilationFlags);
                break;
            case ConvertChecked:
                emitConvertUnaryExpression(node, compilationFlags);
                break;
            case Divide:
                emitBinaryExpression(node, compilationFlags);
                break;
            case Equal:
                emitBinaryExpression(node, compilationFlags);
                break;
            case ExclusiveOr:
                emitBinaryExpression(node, compilationFlags);
                break;
            case GreaterThan:
                emitBinaryExpression(node, compilationFlags);
                break;
            case GreaterThanOrEqual:
                emitBinaryExpression(node, compilationFlags);
                break;
            case Invoke:
                emitInvocationExpression(node, compilationFlags);
                break;
            case Lambda:
                emitLambdaExpression(node);
                break;
            case LeftShift:
                emitBinaryExpression(node, compilationFlags);
                break;
            case LessThan:
                emitBinaryExpression(node, compilationFlags);
                break;
            case LessThanOrEqual:
                emitBinaryExpression(node, compilationFlags);
                break;
            case MemberAccess:
                emitMemberExpression(node);
                break;
            case Modulo:
                emitBinaryExpression(node, compilationFlags);
                break;
            case Multiply:
                emitBinaryExpression(node, compilationFlags);
                break;
            case Negate:
                emitUnaryExpression(node, compilationFlags);
                break;
            case UnaryPlus:
                emitUnaryExpression(node, compilationFlags);
                break;
            case New:
                emitNewExpression(node);
                break;
            case NewArrayInit:
                emitNewArrayExpression(node);
                break;
            case NewArrayBounds:
                emitNewArrayExpression(node);
                break;
            case Not:
                emitUnaryExpression(node, compilationFlags);
                break;
            case NotEqual:
                emitBinaryExpression(node, compilationFlags);
                break;
            case Or:
                emitBinaryExpression(node, compilationFlags);
                break;
            case OrElse:
                emitOrElseBinaryExpression(node, compilationFlags);
                break;
            case Parameter:
                emitParameterExpression(node);
                break;
/*
            case Quote:
                emitQuoteUnaryExpression(node);
                break;
*/
            case RightShift:
                emitBinaryExpression(node, compilationFlags);
                break;
            case UnsignedRightShift:
                emitBinaryExpression(node, compilationFlags);
                break;
            case Subtract:
                emitBinaryExpression(node, compilationFlags);
                break;
            case InstanceOf:
                emitTypeBinaryExpression(node);
                break;
            case Assign:
                emitAssignBinaryExpression(node);
                break;
            case Block:
                emitBlockExpression(node, compilationFlags);
                break;
            case Decrement:
                emitUnaryExpression(node, compilationFlags);
                break;
            case DefaultValue:
                emitDefaultValueExpression(node);
                break;
            case Extension:
                emitExtensionExpression(node);
                break;
            case Goto:
                emitGotoExpression(node, compilationFlags);
                break;
            case Increment:
                emitUnaryExpression(node, compilationFlags);
                break;
            case Label:
                emitLabelExpression(node, compilationFlags);
                break;
            case RuntimeVariables:
                emitRuntimeVariablesExpression(node);
                break;
            case Loop:
                emitLoopExpression(node);
                break;
            case Switch:
                emitSwitchExpression(node, compilationFlags);
                break;
            case Throw:
                emitThrowUnaryExpression(node);
                break;
            case Try:
                emitTryExpression(node);
                break;
            case Unbox:
                emitUnboxUnaryExpression(node);
                break;
            case TypeEqual:
                emitTypeBinaryExpression(node);
                break;
            case OnesComplement:
                emitUnaryExpression(node, compilationFlags);
                break;
            case IsTrue:
                emitUnaryExpression(node, compilationFlags);
                break;
            case IsFalse:
                emitUnaryExpression(node, compilationFlags);
                break;
            case IsNull:
                emitUnaryExpression(node, compilationFlags);
                break;
            case IsNotNull:
                emitUnaryExpression(node, compilationFlags);
                break;
            case ReferenceEqual:
                emitBinaryExpression(node, compilationFlags);
                break;
            case ReferenceNotEqual:
                emitBinaryExpression(node, compilationFlags);
                break;
            default:
                throw ContractUtils.unreachable();
        }

        if (emitStart) {
            emitExpressionEnd(startEmitted);
        }
    }

    private void emitExtensionExpression(final Expression node) {
        throw Error.extensionNotReduced();
    }

    // <editor-fold defaultstate="collapsed" desc="Emit as Void/Type">

    private void emitExpressionAsVoid(final Expression node) {
        emitExpressionAsVoid(node, CompilationFlags.EmitAsNoTail);
    }

    private void emitExpressionAsVoid(final Expression node, final int flags) {
        assert node != null;

        final int startEmitted = emitExpressionStart(node);

        switch (node.getNodeType()) {
            case Assign:
                emitAssign((BinaryExpression) node, CompilationFlags.EmitAsVoidType);
                break;
            case Block:
                emit((BlockExpression) node, updateEmitAsTypeFlag(flags, CompilationFlags.EmitAsVoidType));
                break;
            case Throw:
                emitThrow((UnaryExpression) node, CompilationFlags.EmitAsVoidType);
                break;
            case Goto:
                emitGotoExpression(node, updateEmitAsTypeFlag(flags, CompilationFlags.EmitAsVoidType));
                break;
            case Constant:
            case DefaultValue:
            case Parameter:
                // no-op
                break;
            default:
                if (node.getType() == PrimitiveTypes.Void) {
                    emitExpression(node, updateEmitExpressionStartFlag(flags, CompilationFlags.EmitNoExpressionStart));
                }
                else {
                    emitExpression(node, CompilationFlags.EmitAsNoTail | CompilationFlags.EmitNoExpressionStart);
                    generator.pop(node.getType());
                }
                break;
        }

        emitExpressionEnd(startEmitted);
    }

    private void emitExpressionAsType(final Expression node, final Type type, final int flags) {
        if (type == PrimitiveTypes.Void) {
            emitExpressionAsVoid(node, flags);
        }
        else {
            // if the node is emitted as a different type, CastClass IL is emitted at the end,
            // should not emit with tail calls.
            if (!TypeUtils.areEquivalent(node.getType(), type)) {
                emitExpression(node);
                assert TypeUtils.hasIdentityPrimitiveOrBoxingConversion(type, node.getType()) |
                       TypeUtils.areReferenceAssignable(type, node.getType());
                generator.emitConversion(node.getType(), type);
            }
            else {
                // emit the with the flags and emit emit expression start
                emitExpression(
                    node,
                    updateEmitExpressionStartFlag(
                        flags,
                        CompilationFlags.EmitExpressionStart
                    )
                );
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Assignments">

    private void emitAssignBinaryExpression(final Expression expr) {
        emitAssign((BinaryExpression) expr, CompilationFlags.EmitAsDefaultType);
    }

    private void emitAssign(final BinaryExpression node, final int emitAs) {
        switch (node.getLeft().getNodeType()) {
            case ArrayIndex:
                emitIndexAssignment(node, emitAs);
                return;
            case MemberAccess:
                emitMemberAssignment(node, emitAs);
                return;
            case Parameter:
                emitVariableAssignment(node, emitAs);
                return;
            default:
                throw Error.invalidLValue(node.getLeft().getNodeType());
        }
    }

    private void emitMemberAssignment(final BinaryExpression node, final int flags) {
        final MemberExpression lValue = (MemberExpression) node.getLeft();
        final Expression rValue = node.getRight();
        final MemberInfo member = lValue.getMember();

        if (member.getMemberType() != MemberType.Field) {
            throw Error.invalidMemberType(member.getMemberType());
        }

        // emit "this", if any
        if (lValue.getTarget() != null) {
            emitExpression(lValue.getTarget());
        }

        // emit value
        emitExpression(rValue);

        if (!TypeUtils.hasReferenceConversion(rValue.getType(), lValue.getType())) {
            generator.emitConversion(rValue.getType(), lValue.getType());
        }

        LocalBuilder temp = null;

        final int emitAs = flags & CompilationFlags.EmitAsTypeMask;

        if (emitAs != CompilationFlags.EmitAsVoidType) {
            // Save the value so we can return it.
            generator.dup(node.getType());
            generator.emitStore(temp = getLocal(node.getType()));
        }

        generator.putField((FieldInfo) member);

        if (emitAs != CompilationFlags.EmitAsVoidType) {
            generator.emitLoad(temp);
            freeLocal(temp);
        }
    }

    private void emitVariableAssignment(final BinaryExpression node, final int flags) {
        final ParameterExpression variable = (ParameterExpression) node.getLeft();
        final int emitAs = flags & CompilationFlags.EmitAsTypeMask;
        final Expression right = node.getRight();
        final ExpressionType rightNodeType = right.getNodeType();

        if ((rightNodeType == ExpressionType.Increment ||
             rightNodeType == ExpressionType.Decrement) &&
            right.getType() == PrimitiveTypes.Integer) {

            final LocalBuilder local = _scope.getLocalForVariable(variable);

            if (local != null) {
                generator.increment(
                    local,
                    rightNodeType == ExpressionType.Increment ? 1 : -1
                );

                if (emitAs != CompilationFlags.EmitAsVoidType) {
                    emitParameterExpression(variable);
                }

                return;
            }
        }

        emitExpression(right);

        if (!TypeUtils.hasReferenceConversion(right.getType(), variable.getType())) {
            generator.emitConversion(right.getType(), variable.getType());
        }

        if (emitAs != CompilationFlags.EmitAsVoidType) {
            generator.dup(variable.getType());
        }

        _scope.emitSet(variable);
    }

    private void emitIndexAssignment(final BinaryExpression node, final int flags) {
        final Expression left = node.getLeft();
        final Expression right = node.getRight();
        final BinaryExpression index = (BinaryExpression) left;
        final int emitAs = flags & CompilationFlags.EmitAsTypeMask;

        // Emit the target array.
        emitExpression(left);

        // Emit the index.
        emitExpression(index.getRight());

        // Emit the value
        emitExpression(right);

        if (!TypeUtils.hasReferenceConversion(right.getType(), left.getType())) {
            generator.emitConversion(right.getType(), left.getType());
        }

        // Save the expression value, if needed
        LocalBuilder temp = null;

        if (emitAs != CompilationFlags.EmitAsVoidType) {
            generator.dup(node.getType());
            generator.emitStore(temp = getLocal(node.getType()));
        }

        emitSetIndexCall(index);

        // Restore the value
        if (emitAs != CompilationFlags.EmitAsVoidType) {
            generator.emitLoad(temp);
            freeLocal(temp);
        }
    }

    private void emitSetIndexCall(final BinaryExpression index) {
        generator.emitStoreElement(index.getType());
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Blocks">

    private static boolean hasVariables(final Object node) {
        if (node instanceof BlockExpression) {
            return ((BlockExpression) node).getVariables().size() > 0;
        }
        return ((CatchBlock) node).getVariable() != null;
    }

    private void enterTry(final TryExpression tryExpression) {
        final Expression finallyBlock = tryExpression.getFinallyBlock();

        if (finallyBlock != null) {
            _finallyInfo = new FinallyInfo(_finallyInfo, tryExpression);
        }
    }

    private void exitTry(final TryExpression tryExpression) {
        if (tryExpression != null && _finallyInfo.tryExpression == tryExpression) {
            _finallyInfo = _finallyInfo.parent;
        }
    }

    private void enterScope(final Object node) {
        if (hasVariables(node) &&
            (_scope.mergedScopes == null || !_scope.mergedScopes.contains(node))) {

            CompilerScope scope = _tree.scopes.get(node);

            if (scope == null) {
                //
                // Very often, we want to compile nodes as reductions
                // rather than as IL, but usually they need to allocate
                // some IL locals. To support this, we allow emitting a
                // BlockExpression that was not bound by VariableBinder.
                // This works as long as the variables are only used
                // locally -- i.e. not closed over.
                //
                // User-created blocks will never hit this case; only our
                // internally reduced nodes will.
                //
                scope = new CompilerScope(node, false);
                scope.needsClosure = _scope.needsClosure;
            }

            _scope = scope.enter(this, _scope);

            assert (_scope.node == node);
        }
    }

    private void exitScope(final Object node) {
        if (_scope.node == node) {
            _scope = _scope.exit();
        }
    }

    private void emitBlockExpression(final Expression expr, final int flags) {
        emit((BlockExpression) expr, updateEmitAsTypeFlag(flags, CompilationFlags.EmitAsDefaultType));
    }

    private void emit(final BlockExpression node, final int flags) {
        enterScope(node);

        final int emitAs = flags & CompilationFlags.EmitAsTypeMask;
        final int count = node.getExpressionCount();

        final int tailCall = flags & CompilationFlags.EmitAsTailCallMask;

        final int middleTailCall = tailCall == CompilationFlags.EmitAsNoTail
                                   ? CompilationFlags.EmitAsNoTail
                                   : CompilationFlags.EmitAsMiddle;

        for (int index = 0; index < count - 1; index++) {
            final Expression e = node.getExpression(index);
            final Expression next = node.getExpression(index + 1);

/*
            if (emitDebugSymbols()) {
                // No need to emit a clearance if the next expression in the block is also a
                // DebugInfoExpression.
                if (e instanceof DebugInfoExpression &&
                    ((DebugInfoExpression)e).isClear() &&
                    next instanceof DebugInfoExpression) {

                    continue;
                }
            }
*/
            //
            // In the middle of the block.
            // We may do better here by marking it as Tail if the following expressions are
            // not going to emit any bytecode.
            //
            int tailCallFlag = middleTailCall;

            if (next instanceof GotoExpression) {
                final GotoExpression g = (GotoExpression) next;

                if (g.getValue() == null || !significant(g.getValue())) {
                    final LabelInfo labelInfo = referenceLabel(g.getTarget());

                    if (labelInfo.canReturn()) {
                        //
                        // Since tail call flags are not passed into emitTryExpression(),
                        // canReturn() means the goto will be emitted as [T]RETURN. Therefore
                        // we can emit the current expression with tail call.
                        //
                        tailCallFlag = CompilationFlags.EmitAsTail;
                    }
                }
            }

            final int updatedFlags = updateEmitAsTailCallFlag(flags, tailCallFlag);

            emitExpressionAsVoid(e, updatedFlags);
        }

        // if the type of Block it means this is not a Comma
        // so we will force the last expression to emit as void.
        // We don't need EmitAsType flag anymore, should only pass
        // the EmitTailCall field in flags to emitting the last expression.
        if (emitAs == CompilationFlags.EmitAsVoidType || node.getType() == PrimitiveTypes.Void) {
            emitExpressionAsVoid(node.getExpression(count - 1), tailCall);
        }
        else {
            emitExpressionAsType(node.getExpression(count - 1), node.getType(), tailCall);
        }

        exitScope(node);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="AndAlso Binary Expressions">

    private void emitAndAlsoBinaryExpression(final Expression expr, final int flags) {
        final BinaryExpression b = (BinaryExpression) expr;

        if (b.getMethod() != null) {
            throw Error.andAlsoCannotProvideMethod();
        }
        else if (b.getLeft().getType() == Types.Boolean) {
            emitUnboxingAndAlso(b);
        }
        else {
            emitPrimitiveAndAlso(b);
        }
    }

    private void emitPrimitiveAndAlso(final BinaryExpression b) {
        final Expression left = b.getLeft();
        final Expression right = b.getRight();

        final Type<?> leftType = left.getType();
        final Type<?> rightType = right.getType();

        final Label returnFalse = generator.defineLabel();
        final Label exit = generator.defineLabel();

//        final LocalBuilder leftStorage = getLocal(leftType);
//        final LocalBuilder rightStorage = getLocal(rightType);

        emitExpression(left);

//        generator.emitStore(leftStorage);
//        generator.emitLoad(leftStorage);

        generator.emit(OpCode.IFEQ, returnFalse);

        emitExpression(right);

//        generator.emitStore(rightStorage);
//        generator.emitLoad(rightStorage);

        generator.emit(OpCode.IFEQ, returnFalse);
        generator.emitBoolean(true);
        generator.emitGoto(exit);

        generator.markLabel(returnFalse);
        generator.emitBoolean(false);

        generator.markLabel(exit);
    }

    private void emitUnboxingAndAlso(final BinaryExpression b) {
        final Expression left = b.getLeft();
        final Expression right = b.getRight();

        final Type<?> leftType = left.getType();
        final Type<?> rightType = right.getType();

        final Label returnFalse = generator.defineLabel();
        final Label exit = generator.defineLabel();

//        final LocalBuilder leftStorage = getLocal(leftType);
//        final LocalBuilder rightStorage = getLocal(rightType);

        emitExpression(left);

//        generator.emitStore(leftStorage);
//        generator.emitLoad(leftStorage);

        if (leftType != PrimitiveTypes.Boolean) {
            generator.emitConversion(leftType, PrimitiveTypes.Boolean);
        }

        generator.emit(OpCode.IFEQ, returnFalse);

        emitExpression(right);

//        generator.emitStore(rightStorage);
//        generator.emitLoad(rightStorage);

        if (rightType != PrimitiveTypes.Boolean) {
            generator.emitConversion(rightType, PrimitiveTypes.Boolean);
        }

        generator.emit(OpCode.IFEQ, returnFalse);
        generator.emitBoolean(true);
        generator.emitGoto(exit);

        generator.markLabel(returnFalse);
        generator.emitBoolean(false);

        generator.markLabel(exit);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="OrElse Binary Expressions">

    private void emitOrElseBinaryExpression(final Expression expr, final int flags) {
        final BinaryExpression b = (BinaryExpression) expr;

        if (b.getMethod() != null) {
            throw Error.orElseCannotProvideMethod();
        }
        else if (b.getLeft().getType() == Types.Boolean) {
            emitUnboxingOrElse(b);
        }
        else {
            emitPrimitiveOrElse(b);
        }
    }

    private void emitPrimitiveOrElse(final BinaryExpression b) {
        final Expression left = b.getLeft();
        final Expression right = b.getRight();

        final Type<?> leftType = left.getType();
        final Type<?> rightType = right.getType();

        final Label returnTrue = generator.defineLabel();
        final Label exit = generator.defineLabel();

//        final LocalBuilder leftStorage = getLocal(leftType);
//        final LocalBuilder rightStorage = getLocal(rightType);

        emitExpression(left);

//        generator.emitStore(leftStorage);
//        generator.emitLoad(leftStorage);

        generator.emit(OpCode.IFNE, returnTrue);

        emitExpression(right);

//        generator.emitStore(rightStorage);
//        generator.emitLoad(rightStorage);

        generator.emit(OpCode.IFNE, returnTrue);
        generator.emitBoolean(false);
        generator.emitGoto(exit);

        generator.markLabel(returnTrue);
        generator.emitBoolean(true);

        generator.markLabel(exit);
    }

    private void emitUnboxingOrElse(final BinaryExpression b) {
        final Expression left = b.getLeft();
        final Expression right = b.getRight();

        final Type<?> leftType = left.getType();
        final Type<?> rightType = right.getType();

        final Label returnTrue = generator.defineLabel();
        final Label exit = generator.defineLabel();

//        final LocalBuilder leftStorage = getLocal(leftType);
//        final LocalBuilder rightStorage = getLocal(rightType);

        emitExpression(left);

//        generator.emitStore(leftStorage);
//        generator.emitLoad(leftStorage);

        if (leftType != PrimitiveTypes.Boolean) {
            generator.emitConversion(leftType, PrimitiveTypes.Boolean);
        }

        generator.emit(OpCode.IFNE, returnTrue);

        emitExpression(right);

//        generator.emitStore(rightStorage);
//        generator.emitLoad(rightStorage);

        if (rightType != PrimitiveTypes.Boolean) {
            generator.emitConversion(rightType, PrimitiveTypes.Boolean);
        }

        generator.emit(OpCode.IFNE, returnTrue);
        generator.emitBoolean(false);
        generator.emitGoto(exit);

        generator.markLabel(returnTrue);
        generator.emitBoolean(true);

        generator.markLabel(exit);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Coalesce Expressions">

    private void emitCoalesceBinaryExpression(final Expression expr) {
        final BinaryExpression b = (BinaryExpression) expr;
        assert (b.getMethod() == null);

        if (b.getLeft().getType().isPrimitive()) {
            throw Error.coalesceUsedOnNonNullableType();
        }
        else if (b.getConversion() != null) {
            emitLambdaReferenceCoalesce(b);
        }
        else {
            emitReferenceCoalesceWithoutConversion(b);
        }
    }

    private void emitReferenceCoalesceWithoutConversion(final BinaryExpression b) {
        final Label end = generator.defineLabel();

        final boolean needConvertLeft = !TypeUtils.areEquivalent(b.getLeft().getType(), b.getType());
        final boolean needConvertRight = !TypeUtils.areEquivalent(b.getRight().getType(), b.getType());

        final Label convertLeft = needConvertLeft ? generator.defineLabel() : null;

        emitExpression(b.getLeft());

        generator.dup();
        generator.emit(OpCode.IFNONNULL, needConvertLeft ? convertLeft : end);
        generator.pop();

        emitExpression(b.getRight());

        if (needConvertRight) {
            generator.emitConversion(b.getRight().getType(), b.getType());
        }

        if (needConvertLeft) {
            generator.emitGoto(end);
            generator.markLabel(convertLeft);
            generator.emitConversion(b.getLeft().getType(), b.getType());
        }

        generator.markLabel(end);
    }

    private void emitLambdaReferenceCoalesce(final BinaryExpression b) {
        final LocalBuilder operandStorage = getLocal(b.getLeft().getType());

        final Label end = generator.defineLabel();
        final Label notNull = generator.defineLabel();

        emitExpression(b.getLeft());

        generator.dup();
        generator.emitStore(operandStorage);
        generator.emit(OpCode.IFNONNULL, notNull);

        emitExpression(b.getRight());

        generator.emitGoto(end);

        // If not null, call conversion.
        generator.markLabel(notNull);

        final LambdaExpression lambda = b.getConversion();
        final ParameterExpressionList conversionParameters = lambda.getParameters();

        assert (conversionParameters.size() == 1);

        // Emit the delegate instance.
        emitLambdaExpression(lambda);

        // Emit argument.
        generator.emitLoad(operandStorage);

        freeLocal(operandStorage);

        final MethodInfo method = Expression.getInvokeMethod(lambda);

        // Emit call to invoke.
        generator.call(method);

        generator.markLabel(end);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Conditional Expressions">

    private void emitConditionalExpression(final Expression expr, final int flags) {
        final ConditionalExpression node = (ConditionalExpression) expr;

        assert node.getTest().getType() == PrimitiveTypes.Boolean;

        final Label ifFalse = generator.defineLabel();

        emitExpressionAndBranch(false, node.getTest(), ifFalse);
        emitExpressionAsType(node.getIfTrue(), node.getType(), flags);

        if (notEmpty(node.getIfFalse())) {
            final Label end = generator.defineLabel();

            generator.emitGoto(end);
            generator.markLabel(ifFalse);

            emitExpressionAsType(node.getIfFalse(), node.getType(), flags);

            generator.markLabel(end);
        }
        else {
            generator.markLabel(ifFalse);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Binary Expressions">

    private void emitBinaryExpression(final Expression expr) {
        emitBinaryExpression(expr, CompilationFlags.EmitAsNoTail);
    }

    private void emitBinaryExpression(final Expression expr, final int flags) {
        final BinaryExpression b = (BinaryExpression) expr;
        final ExpressionType nodeType = b.getNodeType();

        assert nodeType != ExpressionType.AndAlso &&
               nodeType != ExpressionType.OrElse &&
               nodeType != ExpressionType.Coalesce;

        if (b.getMethod() != null) {
            emitBinaryMethod(b, flags);
            return;
        }

        final Expression left = b.getLeft();
        final Expression right = b.getRight();

        final Type leftType = left.getType();
        final Type rightType = right.getType();

        if (nodeType.isEqualityOperator() &&
            (b.getType() == PrimitiveTypes.Boolean || b.getType() == Types.Boolean)) {

            emitExpression(getEqualityOperand(left));
            emitExpression(getEqualityOperand(right));
        }
        else {
            emitExpression(left);
            emitExpression(right);
        }

        emitBinaryOperator(nodeType, leftType, rightType, b.getType());
    }

    private Expression getEqualityOperand(final Expression expression) {
        if (expression.getNodeType() == ExpressionType.Convert) {
            final UnaryExpression convert = (UnaryExpression) expression;
            if (TypeUtils.areReferenceAssignable(convert.getType(), convert.getOperand().getType())) {
                return convert.getOperand();
            }
        }
        return expression;
    }

    private void emitBinaryMethod(final BinaryExpression b, final int flags) {
        final MethodInfo method = b.getMethod();
        final Expression left = b.getLeft();
        final Expression right = b.getRight();
        final Expression instance;

        if (method.isStatic()) {
            emitMethodCallExpression(Expression.call(null, method, left, right), flags);
        }
        else if (TypeUtils.isSameOrSubType(method.getDeclaringType(), left.getType())) {
            emitMethodCallExpression(Expression.call(left, method, right), flags);
        }
        else {
            emitMethodCallExpression(Expression.call(right, method, left), flags);
        }
    }

    private void emitBinaryOperator(final ExpressionType op, final Type<?> leftType, final Type<?> rightType, final Type resultType) {
        final boolean leftIsNullable = TypeUtils.isAutoUnboxed(leftType);
        final boolean rightIsNullable = TypeUtils.isAutoUnboxed(rightType);

        switch (op) {
            case ArrayIndex: {
                if (rightType != PrimitiveTypes.Integer && rightType != Types.Integer) {
                    throw ContractUtils.unreachable();
                }
                generator.emitLoadElement(leftType.getElementType());
                return;
            }

            case Coalesce: {
                throw Error.unexpectedCoalesceOperator();
            }

            case ReferenceEqual:
            case ReferenceNotEqual: {
                emitObjectBinaryOp(op);
                return;
            }
        }

        if (leftIsNullable && TypeUtils.isArithmetic(rightType) || rightIsNullable && TypeUtils.isArithmetic(leftType)) {
            emitUnboxingBinaryOp(op, leftType, rightType, resultType);
        }
        else {
            final Type<?> opResultType = emitPrimitiveBinaryOp(op, leftType, rightType);
            emitConvertArithmeticResult(op, opResultType, resultType);
        }
    }

    private void emitConvertArithmeticResult(final ExpressionType op, final Type sourceType, final Type resultType) {
        generator.emitConversion(sourceType, resultType);
    }

    private Type<?> emitPrimitiveBinaryOp(final ExpressionType op, final Type leftType, final Type rightType) {
        final Type<?> operandType;

        if (TypeUtils.isArithmetic(leftType)) {
            operandType = TypeUtils.isArithmetic(rightType)
                          ? Expression.performBinaryNumericPromotion(leftType, rightType)
                          : leftType;
        }
        else {
            operandType = leftType;
        }

        if (leftType != operandType) {
            final LocalBuilder rightStorage = getLocal(rightType);

            generator.emitStore(rightStorage);
            generator.emitConversion(leftType, operandType);
            generator.emitLoad(rightStorage);

            freeLocal(rightStorage);
        }

        final Type<?> rightOperandType;

        switch (op) {
            case LeftShift:
            case RightShift:
            case UnsignedRightShift:
                rightOperandType = PrimitiveTypes.Integer;
                break;

            default:
                rightOperandType = operandType;
                break;
        }

        if (rightType != rightOperandType) {
            generator.emitConversion(rightType, rightOperandType);
        }

        switch (op) {
            case Equal:
            case GreaterThan:
            case GreaterThanOrEqual:
            case LessThan:
            case LessThanOrEqual:
            case NotEqual:
            case ReferenceEqual:
            case ReferenceNotEqual: {
                final Label ifFalse = generator.defineLabel();
                final Label exit = generator.defineLabel();

                emitRelationalBranchOp(op, operandType, false, ifFalse);

                generator.emitBoolean(true);
                generator.emitGoto(exit);

                generator.markLabel(ifFalse);
                generator.emitBoolean(false);
                generator.emitGoto(exit);

                generator.markLabel(exit);

                return PrimitiveTypes.Boolean;
            }
        }

        emitArithmeticBinaryOp(op, operandType);
        return operandType;
    }

    private void emitUnboxingBinaryOp(final ExpressionType op, final Type leftType, final Type rightType, final Type resultType) {
        assert TypeUtils.isAutoUnboxed(leftType) || TypeUtils.isAutoUnboxed(rightType);

        switch (op) {
            case And: {
                if (leftType == Types.Boolean) {
                    emitLiftedBooleanAnd(leftType, rightType);
                }
                else {
                    emitUnboxingBinaryArithmetic(op, leftType, rightType, resultType);
                }
                break;
            }

            case Or: {
                if (leftType == Types.Boolean) {
                    emitLiftedBooleanOr(leftType, rightType);
                }
                else {
                    emitUnboxingBinaryArithmetic(op, leftType, rightType, resultType);
                }
                break;
            }

            case ExclusiveOr:
            case Add:
            case Subtract:
            case Multiply:
            case Divide:
            case Modulo:
            case LeftShift:
            case RightShift: {
                emitUnboxingBinaryArithmetic(op, leftType, rightType, resultType);
                break;
            }

            case LessThan:
            case LessThanOrEqual:
            case GreaterThan:
            case GreaterThanOrEqual:
            case Equal:
            case NotEqual: {
                emitLiftedRelational(op, leftType, rightType, resultType);
                break;
            }

            case AndAlso:
            case OrElse:
            default:
                throw ContractUtils.unreachable();
        }
    }

    private void emitUnboxingBinaryArithmetic(
        final ExpressionType op,
        final Type leftType,
        final Type rightType,
        final Type resultType) {

        final boolean leftIsBoxed = TypeUtils.isAutoUnboxed(leftType);
        final boolean rightIsBoxed = TypeUtils.isAutoUnboxed(rightType);

        Type finalLeftType = leftType;
        Type finalRightType = rightType;

        if (leftIsBoxed) {
            finalLeftType = unboxLeftBinaryOperand(leftType, rightType);
        }

        if (rightIsBoxed) {
            finalRightType = unboxRightBinaryOperand(rightType);
        }

        emitBinaryOperator(
            op,
            finalLeftType,
            finalRightType,
            TypeUtils.getUnderlyingPrimitiveOrSelf(resultType)
        );
    }

    private void emitLiftedRelational(
        final ExpressionType op,
        final Type leftType,
        final Type rightType,
        final Type resultType) {

        final boolean leftIsBoxed = TypeUtils.isAutoUnboxed(leftType);
        final boolean rightIsBoxed = TypeUtils.isAutoUnboxed(rightType);

        Type finalLeftType = leftType;
        Type finalRightType = rightType;

        if (leftIsBoxed) {
            finalLeftType = unboxLeftBinaryOperand(leftType, rightType);
        }

        if (rightIsBoxed) {
            finalRightType = unboxRightBinaryOperand(rightType);
        }

        emitBinaryOperator(
            op,
            finalLeftType,
            finalRightType,
            TypeUtils.getUnderlyingPrimitiveOrSelf(resultType)
        );
    }

    private Type unboxRightBinaryOperand(final Type rightType) {
        final Type finalRightType = TypeUtils.getUnderlyingPrimitive(rightType);
        generator.emitUnbox(finalRightType);
        return finalRightType;
    }

    private Type unboxLeftBinaryOperand(final Type leftType, final Type rightType) {
        final Type finalLeftType = TypeUtils.getUnderlyingPrimitive(leftType);
        final LocalBuilder rightStorage = getLocal(rightType);

        generator.emitStore(rightStorage);
        generator.emitUnbox(finalLeftType);
        generator.emitLoad(rightStorage);

        freeLocal(rightStorage);

        return finalLeftType;
    }

    private void emitLiftedBooleanAnd(final Type leftType, final Type rightType) {
        final Type type = PrimitiveTypes.Boolean;
        final Label returnFalse = generator.defineLabel();
        final Label exit = generator.defineLabel();

        final LocalBuilder rightStorage = getLocal(type);

        generator.emitStore(rightStorage);

        generator.emitConversion(leftType, PrimitiveTypes.Boolean);
        generator.emit(OpCode.IFEQ, returnFalse);

        generator.emitLoad(rightStorage);
        generator.emitConversion(rightType, PrimitiveTypes.Boolean);
        generator.emit(OpCode.IFEQ, returnFalse);

        generator.emitBoolean(true);
        generator.emitGoto(exit);

        generator.markLabel(returnFalse);
        generator.emitBoolean(false);
        generator.emitGoto(exit);

        generator.markLabel(exit);

        freeLocal(rightStorage);
    }

    private void emitLiftedBooleanOr(final Type leftType, final Type rightType) {
        final Type type = PrimitiveTypes.Boolean;
        final Label returnTrue = generator.defineLabel();
        final Label exit = generator.defineLabel();

        final LocalBuilder rightStorage = getLocal(type);

        generator.emitStore(rightStorage);

        generator.emitConversion(leftType, PrimitiveTypes.Boolean);
        generator.emit(OpCode.IFNE, returnTrue);

        generator.emitLoad(rightStorage);
        generator.emitConversion(rightType, PrimitiveTypes.Boolean);
        generator.emit(OpCode.IFNE, returnTrue);

        generator.emitBoolean(false);
        generator.emitGoto(exit);

        generator.markLabel(returnTrue);
        generator.emitBoolean(true);
        generator.emitGoto(exit);

        generator.markLabel(exit);

        freeLocal(rightStorage);
    }

    private void emitObjectBinaryOp(final ExpressionType op) {
        switch (op) {
            case Equal:
            case ReferenceEqual: {
                final Label ifNotEqual = generator.defineLabel();
                final Label exit = generator.defineLabel();

                generator.emit(OpCode.IF_ACMPNE, ifNotEqual);

                generator.emitBoolean(true);
                generator.emitGoto(exit);

                generator.markLabel(ifNotEqual);
                generator.emitBoolean(false);
                generator.emitGoto(exit);

                generator.markLabel(exit);

                break;
            }

            case NotEqual:
            case ReferenceNotEqual: {
                final Label ifEqual = generator.defineLabel();
                final Label exit = generator.defineLabel();

                generator.emit(OpCode.IF_ACMPEQ, ifEqual);

                generator.emitBoolean(true);
                generator.emitGoto(exit);

                generator.markLabel(ifEqual);
                generator.emitBoolean(false);
                generator.emitGoto(exit);

                generator.markLabel(exit);

                break;
            }

            default: {
                throw ContractUtils.unreachable();
            }
        }
    }

    private void emitArithmeticBinaryOp(final ExpressionType op, final Type<?> operandType) {
        switch (op) {
            case Add: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IADD);
                        return;

                    case LONG:
                        generator.emit(OpCode.LADD);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IADD);
                        return;

                    case FLOAT:
                        generator.emit(OpCode.FADD);
                        return;

                    case DOUBLE:
                        generator.emit(OpCode.DADD);
                        return;
                }
                break;
            }

            case And:
            case AndAlso: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IAND);
                        return;

                    case LONG:
                        generator.emit(OpCode.LAND);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IAND);
                        return;
                }
                break;
            }

            case Divide: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IDIV);
                        return;

                    case LONG:
                        generator.emit(OpCode.LDIV);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IDIV);
                        return;

                    case FLOAT:
                        generator.emit(OpCode.FDIV);
                        return;

                    case DOUBLE:
                        generator.emit(OpCode.DDIV);
                        return;
                }
                break;
            }

            case ExclusiveOr: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IXOR);
                        return;

                    case LONG:
                        generator.emit(OpCode.LXOR);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IXOR);
                        return;
                }
                break;
            }

            case LeftShift: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.ISHL);
                        return;

                    case LONG:
                        generator.emit(OpCode.LSHL);
                        return;

                    case CHAR:
                        generator.emit(OpCode.ISHL);
                        return;
                }
                break;
            }

            case Modulo: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IREM);
                        return;

                    case LONG:
                        generator.emit(OpCode.LREM);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IREM);
                        return;

                    case FLOAT:
                        generator.emit(OpCode.FREM);
                        return;

                    case DOUBLE:
                        generator.emit(OpCode.DREM);
                        return;
                }
                break;
            }

            case Multiply: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IMUL);
                        return;

                    case LONG:
                        generator.emit(OpCode.LMUL);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IMUL);
                        return;

                    case FLOAT:
                        generator.emit(OpCode.FMUL);
                        return;

                    case DOUBLE:
                        generator.emit(OpCode.DMUL);
                        return;
                }
                break;
            }

            case Or:
            case OrElse: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IOR);
                        return;

                    case LONG:
                        generator.emit(OpCode.LOR);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IOR);
                        return;
                }
                break;
            }

            case RightShift: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.ISHR);
                        return;

                    case LONG:
                        generator.emit(OpCode.LSHR);
                        return;

                    case CHAR:
                        generator.emit(OpCode.ISHR);
                        return;
                }
                break;
            }

            case UnsignedRightShift: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.IUSHR);
                        return;

                    case LONG:
                        generator.emit(OpCode.LUSHR);
                        return;

                    case CHAR:
                        generator.emit(OpCode.IUSHR);
                        return;
                }
                break;
            }

            case Subtract: {
                switch (operandType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT:
                        generator.emit(OpCode.ISUB);
                        return;

                    case LONG:
                        generator.emit(OpCode.LSUB);
                        return;

                    case CHAR:
                        generator.emit(OpCode.ISUB);
                        return;

                    case FLOAT:
                        generator.emit(OpCode.FSUB);
                        return;

                    case DOUBLE:
                        generator.emit(OpCode.DSUB);
                        return;
                }
                break;
            }
        }

        throw ContractUtils.unreachable();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constants and Default Values">

    private void emitConstantExpression(final Expression expr) {
        final ConstantExpression node = (ConstantExpression) expr;
        emitConstant(node.getValue(), node.getType());
    }

    private void emitConstant(final Object value, final Type<?> type) {
        // Try to emit the constant directly into IL
        if (CodeGenerator.canEmitConstant(value, type)) {
            final boolean isBoxed = TypeUtils.isAutoUnboxed(type);
            final boolean isClosureAvailable = canEmitBoundConstants();

            if (!isBoxed || !isClosureAvailable) {
                generator.emitConstant(value, type);

                //
                // NOTE: For backward compatibility, allow boxed values to be emitted as constants when
                //       a closure is not available.  Previously, we allowed, e.g., a Long to be stored
                //       as a long constant, but we didn't emit any boxing upon loading it.  This caused
                //       problems when the constant was being used as an Object, e.g., as a method
                //       invocation target.
                //
                //       To prevent code that previously worked from breaking, allow the unboxed value
                //       to be emitted for a boxed ConstantExpression when no alternative is available,
                //       but emit the boxing operation immediately afterward to avoid the problem above.
                //
                if (isBoxed && value != null) {
                    generator.emitBox(type);
                }

                return;
            }
        }

        _boundConstants.emitConstant(this, value, type);
    }

    private void emitDefaultValueExpression(final Expression node) {
        generator.emitDefaultValue(node.getType());
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Branching">

    private void emitGotoExpression(final Expression expr, final int flags) {
        final GotoExpression node = (GotoExpression) expr;
        final LabelInfo labelInfo = referenceLabel(node.getTarget());

        int finalFlags = flags;

        if (node.getValue() != null) {
            final Type targetType = node.getTarget().getType();

            if (targetType == PrimitiveTypes.Void) {
                emitExpressionAsVoid(node.getValue(), flags);
            }
            else {
                final Type<?> valueType = node.getValue().getType();

                finalFlags = updateEmitExpressionStartFlag(flags, CompilationFlags.EmitExpressionStart);
                emitExpression(node.getValue(), finalFlags);

                if (!TypeUtils.hasReferenceConversion(valueType, targetType)) {
                    generator.emitConversion(valueType, targetType);
                }
            }
        }

        if (node.getKind() == GotoExpressionKind.Return) {
            for (FinallyInfo finallyInfo = _finallyInfo;
                 finallyInfo != null && finallyInfo.tryExpression != null;
                 finallyInfo = finallyInfo.parent) {

                emitExpression(finallyInfo.tryExpression.getFinallyBlock());
            }
        }

        labelInfo.emitJump();

        emitUnreachable(node, finalFlags);
    }

    private void emitUnreachable(final Expression node, final int flags) {
        if (node.getType() != PrimitiveTypes.Void && (flags & CompilationFlags.EmitAsVoidType) == 0) {
            generator.emitDefaultValue(node.getType());
        }
    }

    private void emitExpressionAndBranch(final boolean branchValue, final Expression node, final Label label) {
        final int startEmitted = emitExpressionStart(node);

        try {
            if (node.getType() == PrimitiveTypes.Boolean) {
                switch (node.getNodeType()) {
                    case Not:
                    case IsFalse:
                        emitBranchNot(branchValue, (UnaryExpression) node, label);
                        return;
                    case AndAlso:
                    case OrElse:
                        emitBranchLogical(branchValue, (BinaryExpression) node, label);
                        return;
                    case Block:
                        emitBranchBlock(branchValue, (BlockExpression) node, label);
                        return;
                    case Equal:
                    case NotEqual:
                    case GreaterThan:
                    case GreaterThanOrEqual:
                    case LessThan:
                    case LessThanOrEqual:
                    case ReferenceEqual:
                    case ReferenceNotEqual:
                        emitBranchRelation(branchValue, (BinaryExpression) node, label);
                        return;
                    case IsNull:
                    case IsNotNull:
                        emitBranchNullCheck(branchValue, (UnaryExpression) node, label);
                        return;
                }
            }
            emitExpression(node, CompilationFlags.EmitAsNoTail | CompilationFlags.EmitNoExpressionStart);
            emitBranchOp(branchValue, label);
        }
        finally {
            emitExpressionEnd(startEmitted);
        }
    }

    private void emitBranchNot(final boolean branch, final UnaryExpression node, final Label label) {
        if (node.getMethod() != null) {
            emitExpression(node, CompilationFlags.EmitAsNoTail | CompilationFlags.EmitNoExpressionStart);
            emitBranchOp(branch, label);
            return;
        }
        emitExpressionAndBranch(!branch, node.getOperand(), label);
    }

    private void emitBranchNullCheck(final boolean branch, final UnaryExpression node, final Label label) {
        emitExpression(node.getOperand());
        emitRelationalBranchOp(node.getNodeType(), node.getOperand().getType(), branch, label);
    }

    private void emitBranchRelation(final boolean branch, final BinaryExpression node, final Label label) {
        final ExpressionType op = node.getNodeType();

        assert op == ExpressionType.Equal ||
               op == ExpressionType.NotEqual ||
               op == ExpressionType.LessThan ||
               op == ExpressionType.LessThanOrEqual ||
               op == ExpressionType.GreaterThan ||
               op == ExpressionType.GreaterThanOrEqual ||
               op == ExpressionType.ReferenceEqual ||
               op == ExpressionType.ReferenceNotEqual;

        if (node.getMethod() != null) {
            emitBinaryMethod(node, CompilationFlags.EmitAsNoTail);
            //
            // emitBinaryMethod() takes into account the Equal/NotEqual
            // node kind, so use the original branch value
            //
            emitBranchOp(branch, label);
            return;
        }

        if (op == ExpressionType.ReferenceEqual ||
            op == ExpressionType.ReferenceNotEqual) {

            if (ConstantCheck.isNull(node.getLeft())) {
                if (ConstantCheck.isNull(node.getRight())) {
                    generator.emitBoolean(op == ExpressionType.ReferenceEqual);
                    emitBranchOp(branch, label);
                    return;
                }

                emitExpression(getEqualityOperand(node.getRight()));
                generator.emit(
                    branch ? (op == ExpressionType.ReferenceEqual ? OpCode.IFNULL : OpCode.IFNONNULL)
                           : (op == ExpressionType.ReferenceEqual ? OpCode.IFNONNULL : OpCode.IFNULL),
                    label
                );
                return;
            }

            if (ConstantCheck.isNull(node.getRight())) {
                emitExpression(getEqualityOperand(node.getLeft()));
                generator.emit(
                    branch ? (op == ExpressionType.ReferenceEqual ? OpCode.IFNULL : OpCode.IFNONNULL)
                           : (op == ExpressionType.ReferenceEqual ? OpCode.IFNONNULL : OpCode.IFNULL),
                    label
                );
                return;
            }
        }
        else if (op == ExpressionType.Equal ||
                 op == ExpressionType.NotEqual) {

            if (ConstantCheck.isTrue(node.getLeft())) {
                if (ConstantCheck.isTrue(node.getRight())) {
                    if (branch == (op == ExpressionType.Equal)) {
                        generator.emitGoto(label);
                    }
                    return;
                }

                if (ConstantCheck.isFalse(node.getRight())) {
                    if (branch == (op == ExpressionType.NotEqual)) {
                        generator.emitGoto(label);
                    }
                    return;
                }

                emitExpression(getEqualityOperand(node.getRight()));
                emitBranchOp(
                    branch == (op == ExpressionType.Equal),
                    label
                );
                return;
            }

            if (ConstantCheck.isTrue(node.getRight())) {
                emitExpression(getEqualityOperand(node.getLeft()));
                emitBranchOp(
                    branch == (op == ExpressionType.Equal),
                    label
                );
                return;
            }
        }

        if (TypeUtils.isAutoUnboxed(node.getLeft().getType()) ||
            TypeUtils.isAutoUnboxed(node.getRight().getType())) {

            emitBinaryExpression(node);

            //
            // emitBinaryExpression() takes into account the Equal/NotEqual
            // node kind, so use the original branch value
            //
            emitBranchOp(branch, label);

            return;
        }

        final Expression equalityOperand = getEqualityOperand(node.getLeft());

        emitExpression(equalityOperand);
        emitExpression(getEqualityOperand(node.getRight()));

        final Type<?> compareType;

        if (TypeUtils.isArithmetic(equalityOperand.getType())) {
            compareType = TypeUtils.getUnderlyingPrimitiveOrSelf(equalityOperand.getType());
        }
        else {
            compareType = equalityOperand.getType();
        }

        final OpCode opCode;

        emitRelationalBranchOp(op, compareType, branch, label);
    }

    private void emitRelationalBranchOp(final ExpressionType op, final Type<?> operandType, final boolean branch, final Label label) {
        final boolean reallyBranch;

        switch (operandType.getKind()) {
            case BOOLEAN:
            case BYTE:
            case SHORT:
            case INT:
            case CHAR: {
                switch (op) {
                    case Equal:
                        reallyBranch = branch == (op == ExpressionType.Equal);
                        generator.emit(reallyBranch ? OpCode.IF_ICMPEQ : OpCode.IF_ICMPNE, label);
                        break;

                    case GreaterThan:
                        reallyBranch = branch == (op == ExpressionType.GreaterThan);
                        generator.emit(reallyBranch ? OpCode.IF_ICMPGT : OpCode.IF_ICMPLE, label);
                        break;

                    case GreaterThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.GreaterThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.IF_ICMPGE : OpCode.IF_ICMPLT, label);
                        break;

                    case LessThan:
                        reallyBranch = branch == (op == ExpressionType.LessThan);
                        generator.emit(reallyBranch ? OpCode.IF_ICMPLT : OpCode.IF_ICMPGE, label);
                        break;

                    case LessThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.LessThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.IF_ICMPLE : OpCode.IF_ICMPGT, label);
                        break;

                    case NotEqual:
                        reallyBranch = branch == (op == ExpressionType.NotEqual);
                        generator.emit(reallyBranch ? OpCode.IF_ICMPNE : OpCode.IF_ICMPEQ, label);
                        break;
                }
                break;
            }

            case LONG:
                generator.emit(OpCode.LCMP);
                switch (op) {
                    case Equal:
                        reallyBranch = branch == (op == ExpressionType.Equal);
                        generator.emit(reallyBranch ? OpCode.IFEQ : OpCode.IFNE, label);
                        break;

                    case GreaterThan:
                        reallyBranch = branch == (op == ExpressionType.GreaterThan);
                        generator.emit(reallyBranch ? OpCode.IFGT : OpCode.IFLE, label);
                        break;

                    case GreaterThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.GreaterThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.IFGE : OpCode.IFLT, label);
                        break;

                    case LessThan:
                        reallyBranch = branch == (op == ExpressionType.LessThan);
                        generator.emit(reallyBranch ? OpCode.IFLT : OpCode.IFGE, label);
                        break;

                    case LessThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.LessThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.IFLE : OpCode.IFGT, label);
                        break;

                    case NotEqual:
                        reallyBranch = branch == (op == ExpressionType.NotEqual);
                        generator.emit(reallyBranch ? OpCode.IFNE : OpCode.IFEQ, label);
                        break;
                }
                break;

            case FLOAT:
                switch (op) {
                    case Equal:
                        reallyBranch = branch == (op == ExpressionType.Equal);
                        generator.emit(OpCode.FCMPL);
                        generator.emit(reallyBranch ? OpCode.IFEQ : OpCode.IFNE, label);
                        break;

                    case GreaterThan:
                        reallyBranch = branch == (op == ExpressionType.GreaterThan);
                        generator.emit(reallyBranch ? OpCode.FCMPG : OpCode.FCMPL);
                        generator.emit(reallyBranch ? OpCode.IFGT : OpCode.IFLE, label);
                        break;

                    case GreaterThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.GreaterThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.FCMPG : OpCode.FCMPL);
                        generator.emit(reallyBranch ? OpCode.IFGE : OpCode.IFLT, label);
                        break;

                    case LessThan:
                        reallyBranch = branch == (op == ExpressionType.LessThan);
                        generator.emit(reallyBranch ? OpCode.FCMPL : OpCode.FCMPG);
                        generator.emit(reallyBranch ? OpCode.IFLT : OpCode.IFGE, label);
                        break;

                    case LessThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.LessThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.FCMPL : OpCode.FCMPG);
                        generator.emit(reallyBranch ? OpCode.IFLE : OpCode.IFGT, label);
                        break;

                    case NotEqual:
                        reallyBranch = branch == (op == ExpressionType.NotEqual);
                        generator.emit(OpCode.FCMPL);
                        generator.emit(reallyBranch ? OpCode.IFNE : OpCode.IFEQ, label);
                        break;
                }
                break;

            case DOUBLE:
                switch (op) {
                    case Equal:
                        reallyBranch = branch == (op == ExpressionType.Equal);
                        generator.emit(OpCode.DCMPL);
                        generator.emit(reallyBranch ? OpCode.IFEQ : OpCode.IFNE, label);
                        break;

                    case GreaterThan:
                        reallyBranch = branch == (op == ExpressionType.GreaterThan);
                        generator.emit(reallyBranch ? OpCode.DCMPG : OpCode.DCMPL);
                        generator.emit(reallyBranch ? OpCode.IFGT : OpCode.IFLE, label);
                        break;

                    case GreaterThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.GreaterThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.DCMPG : OpCode.DCMPL);
                        generator.emit(reallyBranch ? OpCode.IFGE : OpCode.IFLT, label);
                        break;

                    case LessThan:
                        reallyBranch = branch == (op == ExpressionType.LessThan);
                        generator.emit(reallyBranch ? OpCode.DCMPL : OpCode.DCMPG);
                        generator.emit(reallyBranch ? OpCode.IFLT : OpCode.IFGE, label);
                        break;

                    case LessThanOrEqual:
                        reallyBranch = branch == (op == ExpressionType.LessThanOrEqual);
                        generator.emit(reallyBranch ? OpCode.DCMPL : OpCode.DCMPG);
                        generator.emit(reallyBranch ? OpCode.IFLE : OpCode.IFGT, label);
                        break;

                    case NotEqual:
                        reallyBranch = branch == (op == ExpressionType.NotEqual);
                        generator.emit(OpCode.DCMPL);
                        generator.emit(reallyBranch ? OpCode.IFNE : OpCode.IFEQ, label);
                        break;
                }
                break;

            default:
                switch (op) {
                    case Equal:
                    case ReferenceEqual:
                        reallyBranch = branch == (op == ExpressionType.Equal || op == ExpressionType.ReferenceEqual);
                        generator.emit(reallyBranch ? OpCode.IF_ACMPEQ : OpCode.IF_ACMPNE, label);
                        break;

                    case NotEqual:
                    case ReferenceNotEqual:
                        reallyBranch = branch == (op == ExpressionType.NotEqual || op == ExpressionType.ReferenceNotEqual);
                        generator.emit(reallyBranch ? OpCode.IF_ACMPNE : OpCode.IF_ACMPEQ, label);
                        break;

                    case IsNull:
                        reallyBranch = branch == (op == ExpressionType.IsNull);
                        generator.emit(reallyBranch ? OpCode.IFNULL : OpCode.IFNONNULL, label);
                        break;

                    case IsNotNull:
                        reallyBranch = branch == (op == ExpressionType.IsNotNull);
                        generator.emit(reallyBranch ? OpCode.IFNONNULL : OpCode.IFNULL, label);
                        break;
                }
                break;
        }
    }

    private void emitBranchOp(final boolean branch, final Label label) {
        generator.emit(branch ? OpCode.IFNE : OpCode.IFEQ, label);
    }

    private void emitBranchLogical(final boolean branch, final BinaryExpression node, final Label label) {
        assert (node.getNodeType() == ExpressionType.AndAlso || node.getNodeType() == ExpressionType.OrElse);

        if (node.getMethod() != null) {
            emitExpression(node);
            emitBranchOp(branch, label);
            return;
        }

        final boolean isAnd = node.getNodeType() == ExpressionType.AndAlso;

        //
        // To share code, we make the following substitutions:
        //     if (!(left || right)) branch value
        // becomes:
        //     if (!left && !right) branch value
        // and:
        //     if (!(left && right)) branch value
        // becomes:
        //     if (!left || !right) branch value
        //
        //
        if (branch == isAnd) {
            emitBranchAnd(branch, node, label);
        }
        else {
            emitBranchOr(branch, node, label);
        }
    }

    private void emitBranchAnd(final boolean branch, final BinaryExpression node, final Label label) {
        // if (left) then
        //   if (right) branch label
        // endIf

        final Label endIf = generator.defineLabel();

        emitExpressionAndBranch(!branch, node.getLeft(), endIf);
        emitExpressionAndBranch(branch, node.getRight(), label);

        generator.markLabel(endIf);
    }

    private void emitBranchOr(final boolean branch, final BinaryExpression node, final Label label) {
        // if (left OR right) branch label

        emitExpressionAndBranch(branch, node.getLeft(), label);
        emitExpressionAndBranch(branch, node.getRight(), label);
    }

    private void emitBranchBlock(final boolean branch, final BlockExpression node, final Label label) {
        enterScope(node);

        final int count = node.getExpressionCount();

        for (int i = 0; i < count - 1; i++) {
            emitExpressionAsVoid(node.getExpression(i));
        }

        emitExpressionAndBranch(branch, node.getExpression(count - 1), label);

        exitScope(node);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Invocation Expressions">

    private void emitInvocationExpression(final Expression expr, final int flags) {
        final InvocationExpression node = (InvocationExpression) expr;
        final LambdaExpression<?> lambdaOperand = node.getLambdaOperand();

        // Optimization: inline code for literal lambda's directly
        //
        // This is worth it because otherwise we end up with a extra call
        // to DynamicMethod.CreateDelegate, which is expensive.
        //
        if (lambdaOperand != null) {
            emitInlinedInvoke(node, flags);
            return;
        }

        Expression e = node.getExpression();

        if (Type.of(LambdaExpression.class).isAssignableFrom(e.getType())) {
            // if the invoke target is a lambda expression tree, first compile it into a delegate
            e = Expression.call(e, e.getType().getMethod("compile"));
        }

        e = Expression.call(e, Expression.getInvokeMethod(e), node.getArguments());

        emitExpression(e);
    }

    private void emitInlinedInvoke(final InvocationExpression invoke, final int flags) {
        final LambdaExpression<?> lambda = invoke.getLambdaOperand();

        // This is tricky: we need to emit the arguments outside of the
        // scope, but set them inside the scope. Fortunately, using the
        // stack it is entirely doable.

        // 1. Emit invoke arguments.
        emitArguments(Expression.getInvokeMethod(lambda), invoke);

        // 2. Create the nested LambdaCompiler.
        final LambdaCompiler inner = new LambdaCompiler(this, lambda);

        // 3. Emit the body.
        inner.emitLambdaBody(_scope, true, flags);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Labels">

    private void emitLabelExpression(final Expression expr, int flags) {
        final LabelExpression node = (LabelExpression) expr;

        assert node.getTarget() != null;

        // If we're an immediate child of a block, our label will already
        // be defined. If not, we need to define our own block so this
        // label isn't exposed except to its own child expression.
        LabelInfo label = null;

        if (_labelBlock.kind == LabelScopeKind.Block) {
            label = _labelBlock.tryGetLabelInfo(node.getTarget());

            // We're in a block but didn't find our label, try switch
            if (label == null && _labelBlock.parent.kind == LabelScopeKind.Switch) {
                label = _labelBlock.parent.tryGetLabelInfo(node.getTarget());
            }

            // if we're in a switch or block, we should have found the label
            assert label != null;
        }

        if (label == null) {
            label = defineLabel(node.getTarget());
        }

        if (node.getDefaultValue() != null) {
            if (node.getTarget().getType() == PrimitiveTypes.Void) {
                emitExpressionAsVoid(node.getDefaultValue(), flags);
            }
            else {
                flags = updateEmitExpressionStartFlag(flags, CompilationFlags.EmitExpressionStart);
                emitExpression(node.getDefaultValue(), flags);
            }
        }

        label.mark();
    }

    private void pushLabelBlock(final LabelScopeKind type) {
        _labelBlock = new LabelScopeInfo(_labelBlock, type);
    }

    private boolean tryPushLabelBlock(final Expression node) {
        ExpressionType nodeType = node.getNodeType();

        // Anything that is "statement-like" -- e.g. has no associated
        // stack state can be jumped into, with the exception of try-blocks
        // We indicate this by a "Block"
        //
        // Otherwise, we push an "Expression" to indicate that it can't be
        // jumped into
        while (true) {
            switch (nodeType) {
                default: {
                    if (_labelBlock.kind != LabelScopeKind.Expression) {
                        pushLabelBlock(LabelScopeKind.Expression);
                        return true;
                    }
                    return false;
                }

                case Label: {
                    // LabelExpression is a bit special, if it's directly in a
                    // block it becomes associate with the block's scope. Same
                    // thing if it's in a switch case body.
                    if (_labelBlock.kind == LabelScopeKind.Block) {
                        final LabelTarget label = ((LabelExpression) node).getTarget();

                        if (_labelBlock.containsTarget(label)) {
                            return false;
                        }

                        if (_labelBlock.parent.kind == LabelScopeKind.Switch &&
                            _labelBlock.parent.containsTarget(label)) {

                            return false;
                        }
                    }

                    pushLabelBlock(LabelScopeKind.Statement);
                    return true;
                }

                case Block: {
                    if (node instanceof StackSpiller.SpilledExpressionBlock) {
                        // treat it as an expression
                        nodeType = ExpressionType.Extension;
                        continue;
                    }

                    pushLabelBlock(LabelScopeKind.Block);

                    // Labels defined immediately in the block are valid for
                    // the whole block.
                    if (_labelBlock.parent.kind != LabelScopeKind.Switch) {
                        defineBlockLabels(node);
                    }

                    return true;
                }

                case Switch: {
                    pushLabelBlock(LabelScopeKind.Switch);

                    // Define labels inside of the switch cases so they are in
                    // scope for the whole switch. This allows "goto case" and
                    // "goto default" to be considered as local jumps.
                    final SwitchExpression s = (SwitchExpression) node;

                    for (final SwitchCase c : s.getCases()) {
                        defineBlockLabels(c.getBody());
                    }

                    defineBlockLabels(s.getDefaultBody());
                    return true;
                }

                // Remove this when Convert(Void) goes away.
                case Convert: {
                    if (node.getType() != PrimitiveTypes.Void) {
                        // treat it as an expression
                        nodeType = ExpressionType.Extension;
                        continue;
                    }

                    pushLabelBlock(LabelScopeKind.Statement);
                    return true;
                }

                case Conditional:
                case Loop:
                case Goto: {
                    pushLabelBlock(LabelScopeKind.Statement);
                    return true;
                }
            }
        }
    }

    private void popLabelBlock(final LabelScopeKind kind) {
        assert _labelBlock != null && _labelBlock.kind == kind;
        _labelBlock = _labelBlock.parent;
    }

    private void defineBlockLabels(final Expression node) {
        if (!(node instanceof BlockExpression)) {
            return;
        }

        if (node instanceof StackSpiller.SpilledExpressionBlock) {
            return;
        }

        final BlockExpression block = (BlockExpression) node;

        for (int i = 0, n = block.getExpressionCount(); i < n; i++) {
            final Expression e = block.getExpression(i);

            if (e instanceof LabelExpression) {
                defineLabel(((LabelExpression) e).getTarget());
            }
        }
    }

    private LabelInfo ensureLabel(final LabelTarget node) {
        LabelInfo result = _labelInfo.get(node);

        if (result == null) {
            _labelInfo.put(node, result = new LabelInfo(generator, node, false));
        }

        return result;
    }

    private LabelInfo referenceLabel(final LabelTarget node) {
        final LabelInfo result = ensureLabel(node);
        result.reference(_labelBlock);
        return result;
    }

    private LabelInfo defineLabel(final LabelTarget node) {
        if (node == null) {
            return new LabelInfo(generator, null, false);
        }
        final LabelInfo result = ensureLabel(node);
        result.define(_labelBlock);
        return result;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Lambda Expressions">

    private void emitLambdaExpression(final Expression expr) {
        final LambdaExpression node = (LambdaExpression) expr;
        emitDelegateConstruction(node);
    }

    private void emitDelegateConstruction(final LambdaExpression lambda) {
        // 1. Create the new compiler
        final LambdaCompiler compiler;

        // When the lambda does not have a name or the name is empty, generate a unique name for it.
        final String name = StringUtilities.isNullOrEmpty(lambda.getName()) ? getUniqueMethodName() : lambda.getName();

/*
        final MethodBuilder mb = typeBuilder.defineMethod(
            name,
            Modifier.PRIVATE | Modifier.STATIC,
            lambda.getReturnType(),
            lambda.getParameters().getParameterTypes()
        );

        compiler = new LambdaCompiler(_tree, lambda, mb, this._constructorBuilder);
*/
        compiler = new LambdaCompiler(_tree, lambda);

        // 2. Emit the lambda
        compiler.emitLambdaBody(_scope, false, CompilationFlags.EmitAsNoTail);

        if (_scope.needsClosure || _boundConstants.count() != 0) {
            compiler.ensureClosure();
        }

        // 3. emit the delegate creation in the outer lambda
        emitDelegateConstruction(compiler);

        compiler.typeBuilder.createType();
    }

    static String getUniqueMethodName() {
        return String.format("lambda_method_%d", nextId.getAndIncrement());
    }

    static String getUniqueLambdaName(final String name, final Class<?> creationContext) {
        Package p;

        if (creationContext != null) {
            p = creationContext.getPackage();

            if (p == null) {
                p = LambdaCompiler.class.getPackage();
            }
        }
        else {
            p = LambdaCompiler.class.getPackage();
        }

        if (name != null) {
            return String.format("%s.%s$0x%3$04x", p.getName(), name, nextId.getAndIncrement());
        }

        return String.format("%s.f__Lambda$0x%2$04x", p.getName(), nextId.getAndIncrement());
    }

    private void emitLambdaBody() {
        // The lambda body is the "last" expression of the lambda
        final int tailCallFlag = lambda.isTailCall() ? CompilationFlags.EmitAsTail : CompilationFlags.EmitAsNoTail;
        emitLambdaBody(null, false, tailCallFlag);
    }

    private void emitLambdaBody(final CompilerScope parent, final boolean inlined, int flags) {
        _scope.enter(this, parent);

        if (inlined) {
            final ParameterExpressionList parameters = lambda.getParameters();

            //
            // The arguments were already pushed onto the stack.
            // Store them into locals, popping in reverse order.
            //
            for (int i = parameters.size() - 1; i >= 0; i--) {
                _scope.emitSet(parameters.get(i));
            }
        }

        // Need to emit the expression start for the lambda body
        flags = updateEmitExpressionStartFlag(flags, CompilationFlags.EmitExpressionStart);

        final Expression body = lambda.getBody();
        final Type<?> bodyType = body.getType();
        final Type returnType = lambda.getReturnType();

        if (returnType == PrimitiveTypes.Void) {
            emitExpressionAsVoid(body, flags);
        }
        else {
            emitExpression(body, flags);

            if (!TypeUtils.hasReferenceConversion(bodyType, returnType)) {
                generator.emitConversion(bodyType, returnType);
            }
        }

        // Return must be the last instruction in a CLI method.
        // But if we're inlining the lambda, we want to leave the return
        // value on the IL stack.
        if (!inlined) {
            generator.emitReturn(returnType);
        }

        _scope.exit();

        // Validate labels
        assert (_labelBlock.parent == null && _labelBlock.kind == LabelScopeKind.Lambda);

        for (final LabelInfo label : _labelInfo.values()) {
            label.validateFinish();
        }
    }

    private void emitDelegateConstruction(final LambdaCompiler inner) {
        generator.emit(OpCode.NEW, inner.typeBuilder);
        generator.dup();
        emitClosureCreation(inner);
        inner.ensureConstructor();
        generator.call(inner._constructorBuilder);
    }

    private void emitClosureCreation(final LambdaCompiler inner) {
        final boolean closure = inner._scope.needsClosure;
        final boolean boundConstants = inner._boundConstants.count() > 0;

        if (!closure && !boundConstants) {
            return;
        }

        inner.ensureClosure();

        if (boundConstants) {
            ensureClosure();
        }

        final Type<Object[]> objectArrayType = Type.of(Object[].class);

        //
        // new Closure(constantPool, currentHoistedLocals)
        //

        generator.emit(OpCode.NEW, closureType);
        generator.dup();

        if (boundConstants) {
            _boundConstants.emitConstant(this, inner._boundConstants.toArray(), objectArrayType);
        }
        else {
            generator.emitNull();
        }

        if (closure) {
            _scope.emitGet(_scope.getNearestHoistedLocals().selfVariable);
        }
        else {
            generator.emitNull();
        }

        generator.call(closureType.getConstructor(objectArrayType, objectArrayType));
    }

    private void ensureConstructor() {
        if (_constructorBuilder == null) {
            _constructorBuilder = typeBuilder.defineDefaultConstructor();
        }
    }

    private void ensureClosure() {
        if (_hasClosureArgument) {
            return;
        }

        _hasClosureArgument = true;

        _closureField = typeBuilder.defineField(
            "$__closure",
            Type.of(Closure.class),
            Modifier.PRIVATE | Modifier.FINAL
        );

        _constructorBuilder = typeBuilder.defineConstructor(
            Modifier.PUBLIC,
            Type.list(closureType)
        );

        final CodeGenerator ctor = _constructorBuilder.getCodeGenerator();

        ctor.emitThis();
        ctor.call(Types.Object.getConstructors().get(0));
        ctor.emitThis();
        ctor.emitLoadArgument(0);
        ctor.putField(_closureField);
        ctor.emitReturn();
    }

    final void emitConstantArray(final Object array) {
        // Emit as runtime constant if possible
        // if not, emit into IL
        if (typeBuilder != null) {
            // store into field in our type builder, we will initialize
            // the value only once.
            final FieldBuilder fb = createStaticField("ConstantArray", Type.getType(array));
            final Label l = generator.defineLabel();

            generator.getField(fb);
            generator.emit(OpCode.IFNONNULL, l);
            generator.emitConstantArray(array);
            generator.putField(fb);
            generator.markLabel(l);
            generator.getField(fb);
        }
        else {
            generator.emitConstantArray(array);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Loop Expressions">

    private void emitLoopExpression(final Expression expr) {
        final LoopExpression node = (LoopExpression) expr;

        pushLabelBlock(LabelScopeKind.Statement);

        final LabelInfo breakTarget = defineLabel(node.getBreakTarget());
        final LabelInfo continueTarget = defineLabel(node.getContinueTarget());

        continueTarget.markWithEmptyStack();

        emitExpressionAsVoid(node.getBody());

        generator.emitGoto(continueTarget.getLabel());

        popLabelBlock(LabelScopeKind.Statement);

        breakTarget.markWithEmptyStack();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Member Expressions">

    private void emitMemberExpression(final Expression expr) {
        final MemberExpression node = (MemberExpression) expr;

        // Emit "this", if any.
        if (node.getTarget() != null) {
            emitExpression(node.getTarget());
        }

        emitMemberGet(node.getMember());
    }

    // assumes instance is already on the stack
    private void emitMemberGet(final MemberInfo member) {
        if (member.getMemberType() != MemberType.Field) {
            throw ContractUtils.unreachable();
        }

        final boolean isConstant;
        final Object constantValue;
        final FieldInfo field = (FieldInfo) member;
        final Type<?> fieldType = field.getFieldType();

        try {
            if (field instanceof FieldBuilder) {
                final FieldBuilder fb = (FieldBuilder) field;

                constantValue = fb.getConstantValue();
                isConstant = constantValue != null && field.isFinal();
            }
            else {
                isConstant = field.isStatic() &&
                             field.isFinal() &&
                             (fieldType.isPrimitive() || fieldType.isEnum() || Types.String.isEquivalentTo(fieldType));
                constantValue = isConstant ? field.getRawField().get(null) : null;
            }

            if (isConstant) {
                emitConstant(constantValue, fieldType);
                return;
            }
        }
        catch (final ReflectiveOperationException ignored) {
        }

        generator.getField(field);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Method Call Expressions">

    private void emitMethodCallExpression(final Expression expr) {
        emitMethodCallExpression(expr, CompilationFlags.EmitAsNoTail);
    }

    private void emitMethodCallExpression(final Expression expr, final int flags) {
        final MethodCallExpression node = (MethodCallExpression) expr;
        emitMethodCall(node.getTarget(), node.getMethod(), node, flags);
    }

    private void emitMethodCall(final Expression target, final MethodInfo method, final IArgumentProvider methodCallExpr) {
        emitMethodCall(target, method, methodCallExpr, CompilationFlags.EmitAsNoTail);
    }

    private void emitMethodCall(
        final Expression target,
        final MethodInfo method,
        final IArgumentProvider expr,
        final int flags) {

        // Emit instance, if calling an instance method
        Type targetType = null;

        if (!method.isStatic()) {
            targetType = target.getType();
            emitExpression(target);
        }

        emitMethodCall(method, expr, targetType, flags);
    }

    private void emitMethodCall(final Expression target, final MethodInfo method, final MethodCallExpression expr, final int flags) {
        // Emit instance, if calling an instance method
        Type targetType = null;

        if (!method.isStatic()) {
            targetType = target.getType();
            emitExpression(target);
        }

        emitMethodCall(
            method,
            expr,
            targetType,
            target instanceof SuperExpression ? flags | CompilationFlags.EmitAsSuperCall
                                              : flags
        );
    }

    private void emitMethodCall(final MethodInfo method, final IArgumentProvider args, final Type<?> objectType, final int flags) {
        // Emit arguments
        emitArguments(method, args);

        if ((flags & CompilationFlags.EmitAsSuperCall) != 0) {
            final MethodInfo superMethod = objectType.getMethod(
                method.getName(),
                BindingFlags.AllInstance,
                method.getParameters().getParameterTypes().toArray()
            );

            generator.call(OpCode.INVOKESPECIAL, superMethod);
        }
        else {
            generator.call(method);
        }

        final Type returnType = method.getReturnType();

        if (returnType != PrimitiveTypes.Void &&
            (method.isGenericMethod() || method.getDeclaringType().isGenericType())) {

            final MethodInfo erasedDefinition = method.getErasedMethodDefinition();
            if (erasedDefinition != null) {
                generator.emitConversion(
                    erasedDefinition.getReturnType(),
                    returnType
                );
            }
        }
    }

    private void emitArguments(final MethodBase method, final IArgumentProvider args) {
        emitArguments(method, args, 0);
    }

    private void emitArguments(final MethodBase method, final IArgumentProvider args, final int skipParameters) {
        final TypeList parameters;

        if (method instanceof MethodBuilder) {
            parameters = ((MethodBuilder) method).getParameterTypes();
        }
        else if (method instanceof ConstructorBuilder) {
            parameters = ((ConstructorBuilder) method).getMethodBuilder().getParameterTypes();
        }
        else {
            parameters = method.getParameters().getParameterTypes();
        }

        assert args.getArgumentCount() + skipParameters == parameters.size();

        for (int i = skipParameters, n = parameters.size(); i < n; i++) {
            final Type<?> parameterType = parameters.get(i);
            final Expression argument = args.getArgument(i - skipParameters);

            emitExpression(argument);

            final Type argumentType = argument.getType();

            if (method instanceof DynamicMethod) {
                if (argumentType != parameterType) {
                    generator.emitConversion(argumentType, parameterType);
                }
            }
            else if (!TypeUtils.hasReferenceConversion(argumentType, parameterType)) {
                generator.emitConversion(argumentType, parameterType);
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="New and New Array Expressions">

    private void emitNewExpression(final Expression expr) {
        final NewExpression node = (NewExpression) expr;
        final ConstructorInfo constructor = node.getConstructor();

        if (constructor == null) {
            assert node.getArguments().size() == 0
                : "Node with arguments must have a constructor.";

            assert node.getType().isPrimitive()
                :
                "Only primitive type may have no constructor set.";

            generator.emitDefaultValue(node.getType());
            return;
        }

        generator.emitNew(constructor.getDeclaringType());
        generator.dup();
        emitArguments(constructor, node);
        generator.call(constructor);
    }

    private void emitNewArrayExpression(final Expression expr) {
        final NewArrayExpression node = (NewArrayExpression) expr;
        final ExpressionList<? extends Expression> expressions = node.getExpressions();
        final Type elementType = node.getType().getElementType();

        if (node.getNodeType() == ExpressionType.NewArrayInit) {
            generator.emitArray(
                node.getType().getElementType(),
                node.getExpressions().size(),
                new CodeGenerator.EmitArrayElementCallback() {
                    @Override
                    public void emit(final int index) {
                        final Expression element = expressions.get(index);
                        emitExpression(element);
                        generator.emitConversion(element.getType(), elementType);
                    }
                }
            );
        }
        else {
            for (int i = 0, n = expressions.size(); i < n; i++) {
                final Expression x = expressions.get(i);
                emitExpression(x);
                generator.emitConversion(x.getType(), PrimitiveTypes.Integer);
            }
            generator.emitNewArray(node.getType());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Parameter Expressions">

    private void emitParameterExpression(final Expression expr) {
        final ParameterExpression node = (ParameterExpression) expr;
        if (node instanceof SelfExpression || node instanceof SuperExpression) {
            if (methodBuilder.isStatic()) {
                throw Error.cannotAccessThisFromStaticMember();
            }

            if (node instanceof SelfExpression) {
                if (node.getType() != typeBuilder) {
                    throw Error.incorrectlyTypedSelfExpression(typeBuilder, node.getType());
                }
            }
            else {
                if (node.getType() != typeBuilder.getBaseType()) {
                    throw Error.incorrectlyTypedSuperExpression(typeBuilder, node.getType());
                }
            }

            generator.emitThis();
            return;
        }
        _scope.emitGet(node);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Runtime Variables">

    private void emitRuntimeVariablesExpression(final Expression expr) {
        final RuntimeVariablesExpression node = (RuntimeVariablesExpression) expr;
        _scope.emitVariableAccess(this, node.getVariables());
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Type Binary Expressions">

    private void emitTypeBinaryExpression(final Expression expr) {
        final TypeBinaryExpression node = (TypeBinaryExpression) expr;

        if (node.getNodeType() == ExpressionType.TypeEqual) {
            emitExpression(node.reduceTypeEqual());
            return;
        }

        final Type type = node.getTypeOperand();

        //
        // Try to determine the result statically
        //
        final AnalyzeTypeIsResult result = ConstantCheck.analyzeInstanceOf(node);

        if (result == AnalyzeTypeIsResult.KnownTrue ||
            result == AnalyzeTypeIsResult.KnownFalse) {
            //
            // Result is known statically, so just emit the expression for
            // its side effects and return the result.
            //
            emitExpressionAsVoid(node.getOperand());
            generator.emitBoolean(result == AnalyzeTypeIsResult.KnownTrue);
            return;
        }

        if (result == AnalyzeTypeIsResult.KnownAssignable) {
            //
            // We know the type can be assigned, but still need to check
            // for null at runtime.
            //
            assert !type.isPrimitive();

            final Label ifNull = generator.defineLabel();
            final Label exit = generator.defineLabel();

            emitExpression(node.getOperand());

            generator.emit(OpCode.IFNULL, ifNull);
            generator.emitBoolean(true);
            generator.emitGoto(exit);

            generator.markLabel(ifNull);
            generator.emitBoolean(false);

            generator.markLabel(exit);

            return;
        }

        assert result == AnalyzeTypeIsResult.Unknown;

        emitExpression(node.getOperand());

        generator.emit(OpCode.INSTANCEOF, type);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Unary Expressions">

    private void emitUnaryExpression(final Expression expr, final int flags) {
        emitUnary((UnaryExpression) expr, flags);
    }

    private void emitUnary(final UnaryExpression node, final int flags) {
        if (node.getMethod() != null) {
            emitUnaryMethod(node, flags);
        }
        else {
            emitExpression(node.getOperand());
            emitUnaryOperator(node.getNodeType(), node.getOperand().getType(), node.getType());
        }
    }

    private void emitUnaryOperator(final ExpressionType op, final Type operandType, final Type resultType) {
        final boolean operandIsBoxed = TypeUtils.isAutoUnboxed(operandType);

        switch (op) {
            case ArrayLength: {
                generator.emit(OpCode.ARRAYLENGTH);
                return;
            }

            case IsNull: {
                final Label ifNonNull = generator.defineLabel();
                final Label exit = generator.defineLabel();

                generator.emit(OpCode.IFNONNULL, ifNonNull);
                generator.emitBoolean(true);
                generator.emitGoto(exit);

                generator.markLabel(ifNonNull);
                generator.emitBoolean(false);

                generator.markLabel(exit);
                return;
            }

            case IsNotNull: {
                final Label ifNull = generator.defineLabel();
                final Label exit = generator.defineLabel();

                generator.emit(OpCode.IFNULL, ifNull);
                generator.emitBoolean(true);
                generator.emitGoto(exit);

                generator.markLabel(ifNull);
                generator.emitBoolean(false);

                generator.markLabel(exit);
                return;
            }
        }

        final Type unboxedType = TypeUtils.getUnderlyingPrimitiveOrSelf(operandType);

        if (operandIsBoxed) {
            generator.emitUnbox(operandType);
        }

        switch (op) {
            case Not:
            case OnesComplement: {
                switch (unboxedType.getKind()) {
                    case BOOLEAN: {
                        final Label ifTrue = generator.defineLabel();
                        final Label exit = generator.defineLabel();

                        generator.emitBoolean(false);
                        generator.emit(OpCode.IF_ICMPNE, ifTrue);
                        generator.emitBoolean(true);
                        generator.emitGoto(exit);

                        generator.markLabel(ifTrue);
                        generator.emitBoolean(false);

                        generator.markLabel(exit);

                        break;
                    }

                    case BYTE:
                    case SHORT:
                    case INT: {
                        generator.emitInteger(-1);
                        generator.emit(OpCode.IXOR);
                        break;
                    }

                    case LONG: {
                        generator.emitLong(-1L);
                        generator.emit(OpCode.LXOR);
                        break;
                    }

                    case CHAR: {
                        generator.emitInteger(-1);
                        generator.emit(OpCode.IXOR);
                        break;
                    }

                    default: {
                        throw Error.unaryOperatorNotDefined(op, unboxedType);
                    }
                }
                break;
            }

            case IsFalse: {
                final Label ifTrue = generator.defineLabel();
                final Label exit = generator.defineLabel();

                generator.emitBoolean(false);
                generator.emit(OpCode.IF_ICMPNE, ifTrue);
                generator.emitBoolean(true);
                generator.emitGoto(exit);

                generator.markLabel(ifTrue);
                generator.emitBoolean(false);

                generator.markLabel(exit);

                // Not an arithmetic operation -> no conversion
                return;
            }

            case IsTrue: {
                final Label ifFalse = generator.defineLabel();
                final Label exit = generator.defineLabel();

                generator.emitBoolean(false);
                generator.emit(OpCode.IF_ICMPEQ, ifFalse);
                generator.emitBoolean(true);
                generator.emitGoto(exit);

                generator.markLabel(ifFalse);
                generator.emitBoolean(false);

                generator.markLabel(exit);

                // Not an arithmetic operation -> no conversion
                return;
            }

            case UnaryPlus: {
                generator.emit(OpCode.NOP);
                break;
            }

            case Negate: {
                switch (unboxedType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT: {
                        generator.emit(OpCode.INEG);
                        break;
                    }

                    case LONG: {
                        generator.emit(OpCode.LNEG);
                        break;
                    }

                    case CHAR: {
                        generator.emit(OpCode.INEG);
                        break;
                    }

                    case FLOAT: {
                        generator.emit(OpCode.FNEG);
                        break;
                    }

                    case DOUBLE: {
                        generator.emit(OpCode.DNEG);
                        break;
                    }

                    default: {
                        throw Error.unaryOperatorNotDefined(op, unboxedType);
                    }
                }
                break;
            }

            case Increment: {
                switch (unboxedType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT: {
                        generator.emitInteger(1);
                        generator.emit(OpCode.IADD);
                        break;
                    }

                    case LONG: {
                        generator.emitLong(1L);
                        generator.emit(OpCode.LADD);
                        break;
                    }

                    case CHAR: {
                        generator.emitInteger(1);
                        generator.emit(OpCode.IADD);
                        break;
                    }

                    case FLOAT: {
                        generator.emitFloat(1f);
                        generator.emit(OpCode.FADD);
                        break;
                    }

                    case DOUBLE: {
                        generator.emitDouble(1d);
                        generator.emit(OpCode.DADD);
                        break;
                    }

                    default: {
                        throw Error.unaryOperatorNotDefined(op, unboxedType);
                    }
                }
                break;
            }

            case Decrement: {
                switch (unboxedType.getKind()) {
                    case BYTE:
                    case SHORT:
                    case INT: {
                        generator.emitInteger(1);
                        generator.emit(OpCode.ISUB);
                        break;
                    }

                    case LONG: {
                        generator.emitLong(1L);
                        generator.emit(OpCode.LSUB);
                        break;
                    }

                    case CHAR: {
                        generator.emitInteger(1);
                        generator.emit(OpCode.ISUB);
                        break;
                    }

                    case FLOAT: {
                        generator.emitFloat(1f);
                        generator.emit(OpCode.FSUB);
                        break;
                    }

                    case DOUBLE: {
                        generator.emitDouble(1d);
                        generator.emit(OpCode.DSUB);
                        break;
                    }

                    default: {
                        throw Error.unaryOperatorNotDefined(op, unboxedType);
                    }
                }
                break;
            }

            default: {
                throw Error.unhandledUnary(op);
            }
        }

        emitConvertArithmeticResult(op, unboxedType, resultType);
    }

    private void emitUnaryMethod(final UnaryExpression node, final int flags) {
        final MethodInfo method = node.getMethod();

        if (method.isStatic()) {
            emitMethodCallExpression(
                Expression.call(method, node.getOperand()),
                flags
            );
        }
        else {
            emitMethodCallExpression(
                Expression.call(node.getOperand(), method),
                flags
            );
        }
    }

    private void emitConvertUnaryExpression(final Expression expr, final int flags) {
        emitConvert((UnaryExpression) expr, flags);
    }

    private void emitConvert(final UnaryExpression node, final int flags) {
        if (node.getMethod() != null) {
            emitUnaryMethod(node, flags);
        }
        else if (node.getType() == PrimitiveTypes.Void) {
            emitExpressionAsVoid(node.getOperand(), flags);
        }
        else {
            if (TypeUtils.areEquivalent(node.getOperand().getType(), node.getType())) {
                emitExpression(node.getOperand(), flags);
            }
            else {
                emitExpression(node.getOperand());
                generator.emitConversion(node.getOperand().getType(), node.getType());
            }
        }
    }

    private void emitUnboxUnaryExpression(final Expression expr) {
        final UnaryExpression node = (UnaryExpression) expr;

        assert node.getType().isPrimitive();

        // Unboxing leaves the unboxed value on the stack
        emitExpression(node.getOperand());

        final MethodInfo method = ((UnaryExpression) expr).getMethod();

        if (method != null) {
            generator.call(method);
        }
        else {
            generator.emitUnbox(node.getType());
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Throw Expressions">

    private void emitThrowUnaryExpression(final Expression expr) {
        emitThrow((UnaryExpression) expr, CompilationFlags.EmitAsDefaultType);
    }

    private void emitThrow(final UnaryExpression expr, final int flags) {
        emitExpression(expr.getOperand());
        generator.emit(OpCode.ATHROW);
        emitUnreachable(expr, flags);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Try Expressions">

    private void emitTryExpression(final Expression expr) {
        final TryExpression node = (TryExpression) expr;

        checkTry();

        //
        // Entering 'try' block...
        //

        pushLabelBlock(LabelScopeKind.Try);

        generator.beginExceptionBlock();

        enterTry(node);

        //
        // Emit the try statement body.
        //

        emitExpression(node.getBody());

        exitTry(node);

        generator.endTryBlock();

        final Expression finallyBlock = node.getFinallyBlock();
        final Type tryType = expr.getType();
        final LocalBuilder value;

        if (tryType != PrimitiveTypes.Void) {
            //
            // Store the value of the try body.
            //
            value = getLocal(tryType);
            generator.emitStore(value);
        }
        else {
            value = null;
        }

        if (finallyBlock != null) {
            emitExpression(finallyBlock);
        }

        //
        // Emit the catch blocks.
        //

        for (final CatchBlock cb : node.getHandlers()) {
            pushLabelBlock(LabelScopeKind.Catch);

            //
            // Begin the strongly typed exception block.
            //
            if (cb.getFilter() == null) {
                generator.beginCatchBlock(cb.getTest());
            }
            else {
                throw new UnsupportedOperationException("Filter blocks are not yet supported");
//                generator.beginExceptFilterBlock();
            }

            enterScope(cb);

            emitCatchStart(cb);

            //
            // Emit the catch block body.
            //
            emitExpression(cb.getBody());

            if (tryType != PrimitiveTypes.Void) {
                //
                // Store the value of the catch block body.
                //
                generator.emitStore(value);
            }

            if (finallyBlock != null) {
                emitExpression(finallyBlock);
            }

            exitScope(cb);

            popLabelBlock(LabelScopeKind.Catch);
        }

        //
        // Emit the finally block.
        //

        if (finallyBlock != null) {
            pushLabelBlock(LabelScopeKind.Finally);

            generator.beginFinallyBlock();

            final LocalBuilder exceptionTemp = getLocal(Types.Throwable);

            generator.emitStore(exceptionTemp);

            //
            // Emit the body.
            //
            emitExpression(finallyBlock);

            generator.emitLoad(exceptionTemp);
            generator.emit(OpCode.ATHROW);

            generator.endExceptionBlock();
            popLabelBlock(LabelScopeKind.Finally);
        }
        else {
            generator.endExceptionBlock();
        }

        if (tryType != PrimitiveTypes.Void) {
            generator.emitLoad(value);
            freeLocal(value);
        }

        popLabelBlock(LabelScopeKind.Try);
    }

    private void emitCatchStart(final CatchBlock cb) {
        if (cb.getFilter() == null) {
            emitSaveExceptionOrPop(cb);
            return;
        }

        //
        // Emit filter block. Filter blocks are untyped so we need to do
        // the type check ourselves.
        //
        final Label endFilter = generator.defineLabel();
        final Label rightType = generator.defineLabel();

        //
        // Skip if it's not our exception type, but save the exception if it is
        // so it's available to the filter.
        //
        generator.emit(OpCode.INSTANCEOF, cb.getTest());
        generator.dup();
        generator.emit(OpCode.IFNE, rightType);
        generator.pop();
        generator.emitBoolean(false);
        generator.emitGoto(endFilter);

        //
        // It's our type, save it and emit the filter.
        //
        generator.markLabel(rightType);
        emitSaveExceptionOrPop(cb);
        pushLabelBlock(LabelScopeKind.Filter);
        emitExpression(cb.getFilter());
        popLabelBlock(LabelScopeKind.Filter);

        //
        // Begin the catch, clear the exception; we've already saved it.
        //
        generator.markLabel(endFilter);
        generator.beginCatchBlock(null);
        generator.pop();
    }

    private void checkTry() {
        //
        // Try inside a filter is not verifiable
        //
        for (LabelScopeInfo j = _labelBlock; j != null; j = j.parent) {
            if (j.kind == LabelScopeKind.Filter) {
                throw Error.tryNotAllowedInFilter();
            }
        }
    }

    private void emitSaveExceptionOrPop(final CatchBlock cb) {
        if (cb.getVariable() != null) {
            //
            // If the variable is present, store the exception in the variable.
            //
            _scope.emitSet(cb.getVariable());
        }
        else {
            //
            // Otherwise, pop it off the stack.
            //
            generator.pop();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Switch Expressions">

    private void emitSwitchExpression(final Expression expr, final int flags) {
        final SwitchExpression node = (SwitchExpression) expr;

        if (tryEmitLookupSwitch(node, flags)) {
            return;
        }

        if (tryEmitStringSwitch(node, flags)) {
            return;
        }

        throw ContractUtils.unsupported();
    }

    private boolean tryEmitStringSwitch(final SwitchExpression node, final int flags) {
        final Type<?> type = node.getSwitchValue().getType();

        //
        // If we're testing anything other than strings, bail.
        //
        if (!TypeUtils.areEquivalent(type, Types.String)) {
            return false;
        }

        final MethodInfo comparison = node.getComparison();
        final MethodInfo comparerEquals = Types.Comparer.getMethod(
            "equals",
            BindingFlags.PublicStatic,
            Types.Object,
            Types.Object
        );

        //
        // If we have a comparison other than string equality, bail.
        //
        if (comparison.getRawMethod() != comparerEquals.getRawMethod()) {
            return false;
        }

        int tests = 0;

        //
        // All test values must be constant.
        //
        for (final SwitchCase c : node.getCases()) {
            for (final Expression t : c.getTestValues()) {
                if (t instanceof ConstantExpression) {
                    ++tests;
                }
                else {
                    return false;
                }
            }
        }

        final String[] keys = new String[tests];
        final HashMap<String, Expression> caseBodies = new HashMap<>();

        int i = 0;

        for (final SwitchCase c : node.getCases()) {
            final ExpressionList<? extends Expression> testValues = c.getTestValues();

            for (int j = 0, n = testValues.size(); j < n; j++) {
                final String s = (String) ((ConstantExpression) testValues.get(j)).getValue();

                keys[i++] = s;
                caseBodies.put(s, c.getBody());
            }
        }

        final SwitchOptions options = node.getOptions();

        emitExpression(node.getSwitchValue());

        generator.emitSwitch(
            keys,
            new StringSwitchCallback() {
                @Override
                public void emitCase(final String key, final Label breakTarget) {
                    final Expression body = caseBodies.get(key);

                    if (body == null) {
                        return;
                    }

                    final Type nodeType = node.getType();

                    if (nodeType == PrimitiveTypes.Void) {
                        emitExpressionAsVoid(body, flags);
                    }
                    else {
                        emitExpressionAsType(body, nodeType, flags);
                    }

                    generator.emitGoto(breakTarget);
                }

                @Override
                public void emitDefault(final Label breakTarget) {
                    final Expression defaultBody = node.getDefaultBody();

                    if (defaultBody == null) {
                        return;
                    }

                    final Type nodeType = node.getType();

                    if (nodeType == PrimitiveTypes.Void) {
                        emitExpressionAsVoid(defaultBody, flags);
                    }
                    else {
                        emitExpressionAsType(defaultBody, nodeType, flags);
                    }
                }
            },
            options
        );

        return true;
    }

    private boolean tryEmitLookupSwitch(final SwitchExpression node, final int flags) {
        //
        // If we have a comparison, bail.
        //
        if (node.getComparison() != null) {
            return false;
        }

        final Type<?> type = node.getSwitchValue().getType();
        final ReadOnlyList<SwitchCase> cases = node.getCases();

        //
        // Make sure the switch value type and the right side type are types that
        // we can optimize.
        //
        if (!canOptimizeSwitchType(type) ||
            !TypeUtils.areEquivalent(type, cases.get(0).getTestValues().get(0).getType())) {

            return false;
        }

        int tests = 0;

        //
        // If not all expressions are constant, then we can't emit the jump table.
        //
        for (final SwitchCase c : node.getCases()) {
            for (final Expression t : c.getTestValues()) {
                if (t instanceof ConstantExpression) {
                    ++tests;
                }
                else {
                    return false;
                }
            }
        }

        final boolean isEnum = type.isEnum();
        final int[] keys = new int[tests];
        final HashMap<Integer, Expression> caseBodies = new HashMap<>();

        int i = 0;

        for (int j = 0, n = cases.size(); j < n; j++) {
            final SwitchCase switchCase = cases.get(j);
            final ExpressionList<? extends Expression> testValues = switchCase.getTestValues();

            for (int k = 0, m = testValues.size(); k < m; k++) {
                final int key;
                final ConstantExpression test = (ConstantExpression) testValues.get(k);

                if (isEnum) {
                    key = ((Enum) test.getValue()).ordinal();
                }
                else {
                    key = ((Number) test.getValue()).intValue();
                }

                keys[i++] = key;

                if (k == m - 1) {
                    caseBodies.put(key, switchCase.getBody());
                }
            }
        }

        Arrays.sort(keys);

        emitExpression(node.getSwitchValue());

        if (isEnum) {
            generator.call(type.getMethod("ordinal"));
        }

        generator.emitSwitch(
            keys,
            new SwitchCallback() {
                @Override
                public void emitCase(final int key, final Label breakTarget) {
                    final Expression body = caseBodies.get(key);

                    if (body == null) {
                        return;
                    }

                    final Type nodeType = node.getType();

                    if (nodeType == PrimitiveTypes.Void) {
                        emitExpressionAsVoid(body, flags);
                    }
                    else {
                        emitExpressionAsType(body, nodeType, flags);
                    }

                    generator.emitGoto(breakTarget);
                }

                @Override
                public void emitDefault(final Label breakTarget) {
                    final Expression defaultBody = node.getDefaultBody();

                    if (defaultBody == null) {
                        return;
                    }

                    final Type nodeType = node.getType();

                    if (nodeType == PrimitiveTypes.Void) {
                        emitExpressionAsVoid(defaultBody, flags);
                    }
                    else {
                        emitExpressionAsType(defaultBody, nodeType, flags);
                    }
                }
            },
            node.getOptions()
        );

        return true;
    }

    private static boolean canOptimizeSwitchType(final Type<?> valueType) {
        final Type<?> actualValueType = TypeUtils.getUnderlyingPrimitiveOrSelf(valueType);

        switch (actualValueType.getKind()) {
            case BYTE:
            case SHORT:
            case INT:
            case LONG:
            case CHAR:
                return true;

            default:
                return actualValueType.isEnum();
        }
    }

    // </editor-fold>
}

enum LabelScopeKind {
    // any "statement like" node that can be jumped into
    Statement,

    // these correspond to the node of the same name
    Block,
    Switch,
    Lambda,
    Try,

    // these correspond to the part of the try block we're in
    Catch,
    Finally,
    Filter,

    // the catch-all value for any other expression type
    // (means we can't jump into it)
    Expression,
}

@SuppressWarnings("PackageVisibleField")
final class LabelScopeInfo {
    // lazily allocated, we typically use this only once every 6th-7th block
    private HashMap<LabelTarget, LabelInfo> labels;

    final LabelScopeKind kind;
    final LabelScopeInfo parent;

    LabelScopeInfo(final LabelScopeInfo parent, final LabelScopeKind kind) {
        this.parent = parent;
        this.kind = kind;
    }

    /**
     * Returns true if we can jump into this node
     */
    boolean canJumpInto() {
        switch (kind) {
            case Block:
            case Statement:
            case Switch:
            case Lambda:
                return true;
        }
        return false;
    }

    boolean containsTarget(final LabelTarget target) {
        return labels != null &&
               labels.containsKey(target);
    }

    LabelInfo tryGetLabelInfo(final LabelTarget target) {
        if (labels == null) {
            return null;
        }

        return labels.get(target);
    }

    void addLabelInfo(final LabelTarget target, final LabelInfo info) {
        assert canJumpInto();

        if (labels == null) {
            labels = new HashMap<>();
        }

        labels.put(target, info);
    }
}

@SuppressWarnings("PackageVisibleField")
final class FinallyInfo {
    final FinallyInfo parent;
    final TryExpression tryExpression;

    FinallyInfo(final FinallyInfo parent, final TryExpression tryExpression) {
        this.parent = parent;
        this.tryExpression = tryExpression;
    }
}
