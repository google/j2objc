/*
 * CompilerScope.java
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
import com.strobel.core.IStrongBox;
import com.strobel.core.MutableInteger;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.ConstructorInfo;
import com.strobel.reflection.FieldInfo;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import com.strobel.reflection.emit.LocalBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author strobelm
 */
@SuppressWarnings("PackageVisibleField")
final class CompilerScope {
    private final static FieldInfo ClosureLocalsField = Type.of(Closure.class).getField("locals");

    private final Map<ParameterExpression, Storage> _locals = new LinkedHashMap<>();

    private HoistedLocals _hoistedLocals;
    private HoistedLocals _closureHoistedLocals;
    private CompilerScope _parent;

    final Object node;
    final boolean isMethod;

    boolean needsClosure;

    final Map<ParameterExpression, VariableStorageKind> definitions;

    Map<ParameterExpression, MutableInteger> referenceCount;

    Set<Object> mergedScopes;

    CompilerScope(final Object node, final boolean isMethod) {
        this.node = node;
        this.isMethod = isMethod;

        final ParameterExpressionList variables = getVariables(node);

        this.definitions = new LinkedHashMap<>(variables.size());

        for (final ParameterExpression v : variables) {
            definitions.put(v, VariableStorageKind.Local);
        }
    }

    CompilerScope enter(final LambdaCompiler lc, final CompilerScope parent) {
        setParent(lc, parent);

        allocateLocals(lc);

        if (isMethod && _closureHoistedLocals != null) {
            emitClosureAccess(lc, _closureHoistedLocals);
        }

        emitNewHoistedLocals(lc);

        if (isMethod) {
            emitCachedVariables();
        }

        return this;
    }

    CompilerScope exit() {
        // Free scope's variables.

        if (!isMethod) {
            for (final Storage storage : _locals.values()) {
                storage.freeLocal();
            }
        }

        // Clear state that is associated with this parent; the scope can be reused
        // in another context.

        final CompilerScope parent = _parent;

        _parent = null;
        _hoistedLocals = null;
        _closureHoistedLocals = null;
        _locals.clear();

        return parent;
    }

    HoistedLocals getNearestHoistedLocals() {
        return _hoistedLocals != null ? _hoistedLocals : _closureHoistedLocals;
    }

    private String getCurrentLambdaName() {
        CompilerScope s = this;
        while (s != null) {
            if (s.node instanceof LambdaExpression<?>) {
                return ((LambdaExpression)s.node).getName();
            }
            s = s._parent;
        }
        return null;
    }

    private void emitNewHoistedLocals(final LambdaCompiler lc) {
        if (_hoistedLocals == null) {
            return;
        }

        // Create the array.
        lc.generator.emitInteger(_hoistedLocals.variables.size());
        lc.generator.emitNewArray(Types.Object.makeArrayType());

        int i = 0;

        // Initialize all elements...
        for (final ParameterExpression v : _hoistedLocals.variables) {
            // array[i] = new StrongBox<T>(...); 
            lc.generator.dup();
            lc.generator.emitInteger(i++);

            final Type boxType = getBoxType(v.getType());
            final ConstructorInfo constructor = boxType.getConstructor(v.getType());

            if (isMethod && lc.getParameters().contains(v)) {
                // array[i] = new StrongBox<T>(argument);
                final int index = lc.getParameters().indexOf(v);
                lc.generator.emitNew(boxType);
                lc.generator.dup();
                lc.emitLambdaArgument(index);
                lc.generator.call(constructor);
            }
            else if (v == _hoistedLocals.getParentVariable()) {
                // array[i] = new StrongBox<T>(closure.Locals);
                lc.generator.emitNew(boxType);
                lc.generator.dup();
                resolveVariable(v, _closureHoistedLocals).emitLoad();
                lc.generator.call(constructor);
            }
            else {
                // array[i] = new StrongBox<T>();
                lc.generator.emitNew(boxType);
                lc.generator.dup();
                lc.generator.call(boxType.getConstructor());
            }

            // If we want to cache this into a local, do it now.
            if (shouldCache(v)) {
                lc.generator.dup();
                cacheBoxToLocal(lc, v);
            }

            lc.generator.emitStoreElement(Types.Object);
        }

        // Store it.
        emitSet(_hoistedLocals.selfVariable);
    }

    private static Type<? extends IStrongBox> getBoxType(final Type<?> type) {
        switch (VerifyArgument.notNull(type, "type").getKind()) {
            case BOOLEAN:
                return Types.BooleanBox;
            case BYTE:
                return Types.ByteBox;
            case SHORT:
                return Types.ShortBox;
            case INT:
                return Types.IntegerBox;
            case LONG:
                return Types.LongBox;
            case CHAR:
                return Types.CharacterBox;
            case FLOAT:
                return Types.FloatBox;
            case DOUBLE:
                return Types.DoubleBox;
            default:
                return Types.StrongBox.makeGenericType(Types.Object);
        }
    }

    private void emitCachedVariables() {
        if (referenceCount == null) {
            return;
        }

        for (final ParameterExpression p : referenceCount.keySet()) {
            if (shouldCache(p)) {
                final Storage storage = resolveVariable(p);
                if (storage instanceof ElementBoxStorage) {
                    ((ElementBoxStorage)storage).emitLoadBox();
                    cacheBoxToLocal(storage.compiler, p);
                }
            }
        }
    }

    private boolean shouldCache(final ParameterExpression v, final int refCount) {
        // This caching is too aggressive in the face of conditionals and
        // switch. Also, it is too conservative for variables used inside
        // of loops.
        return refCount > 2 && !_locals.containsKey(v);
    }

    private boolean shouldCache(final ParameterExpression v) {
        if (referenceCount == null) {
            return false;
        }

        final MutableInteger refCount = referenceCount.get(v);

        return refCount != null &&
               shouldCache(v, refCount.getValue());
    }

    private void cacheBoxToLocal(final LambdaCompiler lc, final ParameterExpression v) {
        assert shouldCache(v) && !_locals.containsKey(v)
            : "shouldCache(v) && !_locals.containsKey(v)";

        final LocalBoxStorage local = new LocalBoxStorage(lc, v);

        local.emitStoreBox();

        _locals.put(v, local);
    }

    private void emitClosureAccess(final LambdaCompiler lc, HoistedLocals locals) {
        if (locals == null) {
            return;
        }

        emitClosureToVariable(lc, locals);

        while ((locals = locals.parent) != null) {
            final ParameterExpression v = locals.selfVariable;
            final LocalStorage local = new LocalStorage(lc, v);

            local.emitStore(resolveVariable(v));

            _locals.put(v, local);
        }
    }

    private void emitClosureToVariable(final LambdaCompiler lc, final HoistedLocals locals) {
        lc.emitClosureArgument();
        lc.generator.getField(ClosureLocalsField);
        addLocal(lc, locals.selfVariable);
        emitSet(locals.selfVariable);
    }

    private Storage resolveVariable(final ParameterExpression variable) {
        return resolveVariable(variable, getNearestHoistedLocals());
    }

    private Storage resolveVariable(final ParameterExpression variable, final HoistedLocals hoistedLocals) {
        // Search locals and arguments, but only in this lambda.
        for (CompilerScope s = this; s != null; s = s._parent) {
            final Storage storage = s._locals.get(variable);

            if (storage != null) {
                return storage;
            }

            // If this is a lambda, we're done.
            if (s.isMethod) {
                break;
            }
        }

        // Search hoisted locals...
        for (HoistedLocals h = hoistedLocals; h != null; h = h.parent) {
            final Integer index = h.indexes.get(variable);
            if (index != null) {
                return new ElementBoxStorage(
                    resolveVariable(h.selfVariable, hoistedLocals),
                    index,
                    variable
                );
            }
        }

        //
        // If this is an unbound variable in the lambda, the error will be thrown from
        // VariableBinder.  So an error here is generally caused by an internal error,
        // e.g., a scope was created but it bypassed VariableBinder.
        //
        throw Error.undefinedVariable(
            variable.getName(),
            variable.getType(),
            getCurrentLambdaName()
        );
    }

    LocalBuilder getLocalForVariable(final ParameterExpression variable) {
        final Storage storage = resolveVariable(variable);
        if (storage instanceof LocalStorage) {
            return ((LocalStorage)storage)._local;
        }
        return null;
    }

    void emitGet(final ParameterExpression variable) {
        resolveVariable(variable).emitLoad();
    }

    void emitSet(final ParameterExpression variable) {
        resolveVariable(variable).emitStore();
    }

    private void setParent(final LambdaCompiler lc, final CompilerScope parent) {
        assert _parent == null && parent != this
            : "_parent == null && parent != this";

        _parent = parent;

        if (needsClosure && _parent != null) {
            _closureHoistedLocals = _parent.getNearestHoistedLocals();
        }

        ArrayList<ParameterExpression> hoistedVariables = null;

        for (final ParameterExpression p : getVariables()) {
            if (definitions.get(p) != VariableStorageKind.Hoisted) {
                continue;
            }
            if (hoistedVariables == null) {
                hoistedVariables = new ArrayList<>();
            }
            hoistedVariables.add(p);
        }

        if (hoistedVariables != null) {
            _hoistedLocals = new HoistedLocals(
                _closureHoistedLocals,
                hoistedVariables.toArray(new ParameterExpression[hoistedVariables.size()])
            );
            addLocal(lc, _hoistedLocals.selfVariable);
        }
    }

    void addLocal(final LambdaCompiler lc, final ParameterExpression variable) {
        _locals.put(variable, new LocalStorage(lc, variable));
    }

    private void allocateLocals(final LambdaCompiler lc) {
        for (final ParameterExpression v : getVariables()) {
            if (definitions.get(v) == VariableStorageKind.Local) {
                //
                // If v is in lc.getParameters(), it is a parameter.
                // Otherwise, it is a local variable.
                //
                // Also, for inlined lambdas we'll create a local.
                //
                final Storage s;
                if (isMethod && lc.lambda.getParameters().contains(v)) {
                    s = new ArgumentStorage(lc, v);
                }
                else {
                    s = new LocalStorage(lc, v);
                }
                _locals.put(v, s);
            }
        }
    }

    private ParameterExpressionList getVariables() {
        ParameterExpressionList variables = getVariables(node);

        if (mergedScopes == null) {
            return variables;
        }

        for (final Object scope : mergedScopes) {
            variables = variables.addAll(variables.size(), getVariables(scope));
        }

        return variables;
    }

    private static ParameterExpressionList getVariables(final Object scope) {
        if (scope instanceof LambdaExpression<?>) {
            return ((LambdaExpression<?>)scope).getParameters();
        }

        if (scope instanceof BlockExpression) {
            return ((BlockExpression)scope).getVariables();
        }

        return new ParameterExpressionList(((CatchBlock)scope).getVariable());
    }

    void emitVariableAccess(final LambdaCompiler lc, final ParameterExpressionList vars) {
        if (getNearestHoistedLocals() != null) {
            // Find what array each variable is on & its index
            final long[] indexes = new long[vars.size()];

            int count = 0;

            for (final ParameterExpression variable : vars) {
                // For each variable, find what array it's defined on
                long parents = 0;

                HoistedLocals locals = getNearestHoistedLocals();

                while (!locals.indexes.containsKey(variable)) {
                    parents++;
                    locals = locals.parent;
                    assert (locals != null);
                }

                // combine the number of parents we walked, with the
                // real index of variable to get the index to emit.
                final long index = (parents << 32) | locals.indexes.get(variable);

                indexes[count++] = index;
            }

            if (count > 0) {
                emitGet(getNearestHoistedLocals().selfVariable);

                lc.emitConstantArray(Arrays.copyOf(indexes, count));

                lc.generator.call(
                    Type.of(RuntimeOperations.class).getMethod(
                        "createRuntimeVariables", Type.of(Object[].class),
                        Type.of(long[].class)
                    )
                );

                return;
            }
        }

        // No visible variables
        lc.generator.call(
            Type.of(RuntimeOperations.class)
                .getMethod("createRuntimeVariables", Type.EmptyTypes)
        );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // STORAGE CLASSES                                                                                                    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private abstract static class Storage {
        final LambdaCompiler compiler;
        final ParameterExpression variable;

        protected Storage(final LambdaCompiler compiler, final ParameterExpression variable) {
            this.compiler = compiler;
            this.variable = variable;
        }

        abstract void emitLoad();
        abstract void emitStore();

        void emitStore(final Storage value) {
            value.emitLoad();
            emitStore();
        }

        void freeLocal() {}
    }

    private final class LocalStorage extends Storage {
        private final LocalBuilder _local;
        private final boolean _named;

        private LocalStorage(final LambdaCompiler compiler, final ParameterExpression variable) {
            super(compiler, variable);
            _named = variable.getName() != null;
            _local = _named ? compiler.getNamedLocal(variable.getType(), variable)
                            : compiler.getLocal(variable.getType());
        }

        @Override
        void emitStore() {
            compiler.generator.emitStore(_local);
        }

        @Override
        void freeLocal() {
            if (!_named) {
                compiler.freeLocal(_local);
            }
        }

        @Override
        void emitLoad() {
            compiler.generator.emitLoad(_local);
            compiler.generator.emitConversion(_local.getLocalType(), variable.getType());
        }
    }

    private final class ArgumentStorage extends Storage {
        private final int _argument;

        private ArgumentStorage(final LambdaCompiler compiler, final ParameterExpression p) {
            super(compiler, p);
            _argument = compiler.getLambdaArgument(compiler.lambda.getParameters().indexOf(p));
        }

        @Override
        void emitStore() {
            compiler.generator.emitStoreArgument(_argument);
        }

        @Override
        void emitLoad() {
            compiler.generator.emitLoadArgument(_argument);
        }
    }

    private final class ElementBoxStorage extends Storage {
        private final int _index;
        private final Storage _array;
        private final Type<?> _boxType;
        private final FieldInfo _boxValueField;

        private ElementBoxStorage(final Storage array, final int index, final ParameterExpression variable) {
            super(array.compiler, variable);
            _array = array;
            _index = index;
            _boxType = getBoxType(variable.getType());
            _boxValueField = _boxType.getField("value");
        }

        void emitLoadBox() {
            _array.emitLoad();
            compiler.generator.emitInteger(_index);
            compiler.generator.emitLoadElement(Types.Object);
            compiler.generator.emitConversion(Types.Object, _boxType);
        }

        @Override
        void emitStore(final Storage value) {
            emitLoadBox();
            value.emitLoad();
            compiler.generator.putField(_boxValueField);
        }

        @Override
        void emitStore() {
            final LocalBuilder value = compiler.getLocal(variable.getType());
            compiler.generator.emitStore(value);
            emitLoadBox();
            compiler.generator.emitLoad(value);
            compiler.freeLocal(value);
            compiler.generator.putField(_boxValueField);
        }

        @Override
        void emitLoad() {
            emitLoadBox();
            compiler.generator.getField(_boxValueField);
            compiler.generator.emitConversion(_boxValueField.getFieldType(), variable.getType());
        }
    }

    private final class LocalBoxStorage extends Storage {
        private final LocalBuilder _boxLocal;
        private final Type<?> _boxType;
        private final FieldInfo _boxValueField;

        private LocalBoxStorage(final LambdaCompiler compiler, final ParameterExpression variable) {
            super(compiler, variable);
            _boxType = getBoxType(variable.getType());
            _boxValueField = _boxType.getField("value");
            _boxLocal = compiler.getNamedLocal(_boxType, variable);
        }

        void emitStoreBox() {
            compiler.generator.emitStore(_boxLocal);
        }

        @Override
        void emitStore(final Storage value) {
            compiler.generator.emitLoad(_boxLocal);
            value.emitLoad();
            compiler.generator.putField(_boxValueField);
        }

        @Override
        void emitStore() {
            final LocalBuilder value = compiler.getLocal(variable.getType());
            compiler.generator.emitStore(value);
            compiler.generator.emitLoad(_boxLocal);
            compiler.generator.emitLoad(value);
            compiler.freeLocal(value);
            compiler.generator.putField(_boxValueField);
        }

        @Override
        void emitLoad() {
            compiler.generator.emitLoad(_boxLocal);
            compiler.generator.getField(_boxValueField);
            compiler.generator.emitConversion(_boxValueField.getFieldType(), variable.getType());
        }
    }
}
