/*
 * BoundConstants.java
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
import com.strobel.core.HashUtilities;
import com.strobel.core.MutableInteger;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;
import com.strobel.reflection.emit.CodeGenerator;
import com.strobel.reflection.emit.LocalBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;

/**
 * @author strobelm
 */
final class BoundConstants {

    /** The list of constants in the order they appear in the constant array */
    private final ArrayList<Object> _values = new ArrayList<>();

    /** The index of each constant in the constant array */
    private final IdentityHashMap<Object, MutableInteger> _indexes = new IdentityHashMap<>();

    /** Each constant referenced within this lambda, and how often it was referenced */
    private final HashMap<TypedConstant, MutableInteger> _references = new HashMap<>();

    /** Bytecode locals for storing frequently used constants */
    private final HashMap<TypedConstant, LocalBuilder> _cache = new HashMap<>();

    int count() {
        return _values.size();
    }
    
    Object[] toArray() {
        return _values.toArray();
    }

    void addReference(final Object value, final Type<?> type) {
        if (!_indexes.containsKey(value)) {
            _indexes.put(value, new MutableInteger(_values.size()));
            _values.add(value);
        }
        incrementCount(new TypedConstant(value, type), _references);
    }

    void emitConstant(final LambdaCompiler lc, final Object value, final Type<?> type) {
        assert !CodeGenerator.canEmitConstant(value, type)
            : "!CodeGenerator.canEmitConstant(value, type)";

        if (!lc.canEmitBoundConstants()) {
            throw Error.cannotCompileConstant(value);
        }

        final LocalBuilder local = _cache.get(new TypedConstant(value, type));
        
        if (local != null) {
            lc.generator.emitLoad(local);
            return;
        }
        
        emitConstantsArray(lc);
        emitConstantFromArray(lc, value, type);
    }

    private static void emitConstantsArray(final LambdaCompiler lc) {
        assert lc.canEmitBoundConstants()   // Should have been checked already.
            : "lc.canEmitBoundConstants()";

        lc.emitClosureArgument();
        lc.generator.getField(Type.of(Closure.class).getField("constants"));
    }

    private void emitConstantFromArray(final LambdaCompiler lc, final Object value, final Type type) {
        MutableInteger index = _indexes.get(value);
        
        if (index == null) {
            _indexes.put(value, (index = new MutableInteger(_values.size())));
            _values.add(value);
        }

        lc.generator.emitInteger(index.getValue());
        lc.generator.emitLoadElement(Types.Object);
        lc.generator.emitConversion(Types.Object, type);
    }

    void emitCacheConstants(final LambdaCompiler lc) {
        int count = 0;

        for (final TypedConstant reference : _references.keySet()) {
            if (!lc.canEmitBoundConstants()) {
                throw Error.cannotCompileConstant(reference.value);
            }

            final MutableInteger referenceCount = _references.get(reference);

            if (shouldCache(referenceCount.getValue())) {
                count++;
            }
        }

        if (count == 0) {
            return;
        }

        emitConstantsArray(lc);

        // The same lambda can be in multiple places in the tree, so we
        // need to clear any locals from last time.
        _cache.clear();

        for (final TypedConstant reference : _references.keySet()) {
            final MutableInteger referenceCount = _references.get(reference);

            if (shouldCache(referenceCount.getValue())) {
                if (--count > 0) {
                    // Dup array to keep it on the stack
                    lc.generator.dup();
                }

                final LocalBuilder local = lc.generator.declareLocal(reference.type);

                emitConstantFromArray(lc, reference.value, local.getLocalType());
                lc.generator.emitStore(local);

                _cache.put(reference, local);
            }
        }
    }

    private static boolean shouldCache(final int refCount) {
        // This caching is too aggressive in the face of conditionals and switch.
        // Also, it is too conservative for variables used inside of loops.
        return refCount > 2;
    }

    private void incrementCount(final TypedConstant typedConstant, final HashMap<TypedConstant, MutableInteger> references) {
        final MutableInteger count = references.get(typedConstant);
        if (count != null) {
            count.increment();
        }
        else {
            references.put(typedConstant, new MutableInteger(1));
        }
    }

    @SuppressWarnings("PackageVisibleField")
    private final static class TypedConstant {
        final Object value;
        final Type<?> type;

        TypedConstant(final Object value, final Type<?> type) {
            this.value = VerifyArgument.notNull(value, "value");
            this.type = VerifyArgument.notNull(type, "type");
        }

        @Override
        public int hashCode() {
            return HashUtilities.combineHashCodes(value, type);
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            final TypedConstant other = (TypedConstant)obj;

            return other.value == value &&
                   other.type.isEquivalentTo(type);
        }
    }
}
