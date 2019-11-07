/*
 * MetadataResolver.java
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

package com.strobel.assembler.metadata;

import com.strobel.core.StringComparator;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.util.List;
import java.util.Stack;

/**
 * @author Mike Strobel
 */
public abstract class MetadataResolver implements IMetadataResolver, IGenericContext {
    private final Stack<IResolverFrame> _frames;

    protected MetadataResolver() {
        _frames = new Stack<>();
    }

    @Override
    public final TypeReference lookupType(final String descriptor) {
        for (int i = _frames.size() - 1; i >= 0; i--) {
            final TypeReference type = _frames.get(i).findType(descriptor);

            if (type != null) {
                return type;
            }
        }

        return lookupTypeCore(descriptor);
    }

    @Override
    public final GenericParameter findTypeVariable(final String name) {
        for (int i = _frames.size() - 1; i >= 0; i--) {
            final GenericParameter type = _frames.get(i).findTypeVariable(name);

            if (type != null) {
                return type;
            }
        }

        return null;
    }

    protected abstract TypeReference lookupTypeCore(final String descriptor);

    @Override
    public void pushFrame(final IResolverFrame frame) {
        _frames.push(VerifyArgument.notNull(frame, "frame"));
    }

    @Override
    public void popFrame() {
        _frames.pop();
    }

    @Override
    public TypeDefinition resolve(final TypeReference type) {
        final TypeReference t = VerifyArgument.notNull(type, "type").getUnderlyingType();

        if (!_frames.isEmpty()) {
            final String descriptor = type.getInternalName();

            for (int i = _frames.size() - 1; i >= 0; i--) {
                final TypeReference resolved = _frames.get(i).findType(descriptor);

                if (resolved instanceof TypeDefinition) {
                    return (TypeDefinition) resolved;
                }
            }
        }

        if (t.isNested()) {
            final TypeDefinition declaringType = t.getDeclaringType().resolve();

            if (declaringType == null) {
                return null;
            }

            final TypeDefinition nestedType = getNestedType(declaringType.getDeclaredTypes(), type);

            if (nestedType != null) {
                return nestedType;
            }
        }

        return resolveCore(t);
    }

    protected abstract TypeDefinition resolveCore(final TypeReference type);

    @Override
    public FieldDefinition resolve(final FieldReference field) {
        final TypeDefinition declaringType = VerifyArgument.notNull(field, "field").getDeclaringType().resolve();

        if (declaringType == null) {
            return null;
        }

        return getField(declaringType, field);
    }

    @Override
    public MethodDefinition resolve(final MethodReference method) {
        TypeReference declaringType = VerifyArgument.notNull(method, "method").getDeclaringType();

        if (declaringType.isArray()) {
            declaringType = BuiltinTypes.Object;
        }

        final TypeDefinition resolvedDeclaringType = declaringType.resolve();

        if (resolvedDeclaringType == null) {
            return null;
        }

        return getMethod(resolvedDeclaringType, method);
    }

    // <editor-fold defaultstate="collapsed" desc="Member Resolution Helpers">

    final FieldDefinition getField(final TypeDefinition declaringType, final FieldReference reference) {
        TypeDefinition type = declaringType;

        while (type != null) {
            final FieldDefinition field = getField(type.getDeclaredFields(), reference);

            if (field != null) {
                return field;
            }

            final TypeReference baseType = type.getBaseType();

            if (baseType == null) {
                return null;
            }

            type = resolve(baseType);
        }

        return null;
    }

    final MethodDefinition getMethod(final TypeDefinition declaringType, final MethodReference reference) {
        TypeDefinition type = declaringType;

        MethodDefinition method = getMethod(type.getDeclaredMethods(), reference);

        if (method != null) {
            return method;
        }

        final TypeReference baseType = declaringType.getBaseType();

        if (baseType != null) {
            type = baseType.resolve();

            if (type != null) {
                method = getMethod(type, reference);

                if (method != null) {
                    return method;
                }
            }
        }

        for (final TypeReference interfaceType : declaringType.getExplicitInterfaces()) {
            type = interfaceType.resolve();

            if (type != null) {
                method = getMethod(type, reference);

                if (method != null) {
                    return method;
                }
            }
        }

        return null;
    }

    static TypeDefinition getNestedType(final List<TypeDefinition> candidates, final TypeReference reference) {
        for (int i = 0, n = candidates.size(); i < n; i++) {
            final TypeDefinition candidate = candidates.get(i);

            if (StringComparator.Ordinal.equals(candidate.getName(), reference.getName())) {
                return candidate;
            }
        }

        return null;
    }

    static FieldDefinition getField(final List<FieldDefinition> candidates, final FieldReference reference) {
        for (int i = 0, n = candidates.size(); i < n; i++) {
            final FieldDefinition candidate = candidates.get(i);

            if (StringComparator.Ordinal.equals(candidate.getName(), reference.getName())) {
                final TypeReference referenceType = reference.getFieldType();
                final TypeReference candidateType = candidate.getFieldType();

                if (areEquivalent(candidateType, referenceType)) {
                    return candidate;
                }
                
                final TypeReference rawCandidateType = MetadataHelper.eraseRecursive(candidateType);
                final TypeReference rawReferenceType = MetadataHelper.eraseRecursive(referenceType);

                if ((rawCandidateType != candidateType || rawReferenceType != referenceType) &&
                    areEquivalent(rawCandidateType, rawReferenceType)) {

                    return candidate;
                }
            }
        }

        return null;
    }

    static MethodDefinition getMethod(final List<MethodDefinition> candidates, final MethodReference reference) {
        final String erasedSignature = reference.getErasedSignature();

        for (int i = 0, n = candidates.size(); i < n; i++) {
            final MethodDefinition candidate = candidates.get(i);

            if (!StringComparator.Ordinal.equals(candidate.getName(), reference.getName())) {
                continue;
            }

            if (StringComparator.Ordinal.equals(candidate.getErasedSignature(), erasedSignature)) {
                return candidate;
            }

            if (reference.hasGenericParameters()) {
                if (!candidate.hasGenericParameters() ||
                    candidate.getGenericParameters().size() != reference.getGenericParameters().size()) {

                    continue;
                }
            }

            if (!StringComparator.Ordinal.equals(candidate.getErasedSignature(), erasedSignature)) {
                continue;
            }

            return candidate;
        }

        return null;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Equivalence Tests">

    /**
     * Returns whether two type references refer to the same type.  Note that a parameterized type
     * will not match its corresponding raw type (but a generic definition will match its raw type).
     *
     * @param a
     *     The first type reference.
     * @param b
     *     The second type reference.
     *
     * @return {@code true} if two type references refer to the same type; otherwise, {@code false}.
     */
    public static boolean areEquivalent(final TypeReference a, final TypeReference b) {
        return areEquivalent(a, b, true);
    }

    /**
     * Returns whether two type references refer to the same type.
     *
     * @param a
     *     The first type reference.
     * @param b
     *     The second type reference.
     * @param strict
     *     If {@code true}, a parameterized type will not match its corresponding raw type (but a
     *     generic definition will match its raw type).
     *
     * @return {@code true} if two type references refer to the same type; otherwise, {@code false}.
     */
    public static boolean areEquivalent(final TypeReference a, final TypeReference b, final boolean strict) {
        if (a == b) {
            return true;
        }

        if (a == null || b == null) {
            return false;
        }

        if (a.getSimpleType() != b.getSimpleType()) {
            return false;
        }

        if (a.isArray()) {
            return areEquivalent(a.getElementType(), b.getElementType());
        }

        if (!StringUtilities.equals(a.getInternalName(), b.getInternalName())) {
            return false;
        }

        if (a instanceof CompoundTypeReference) {
            if (!(b instanceof CompoundTypeReference)) {
                return false;
            }

            final CompoundTypeReference cA = (CompoundTypeReference) a;
            final CompoundTypeReference cB = (CompoundTypeReference) b;

            return areEquivalent(cA.getBaseType(), cB.getBaseType()) &&
                   areEquivalent(cA.getInterfaces(), cB.getInterfaces());
        }
        else if (b instanceof CompoundTypeReference) {
            return false;
        }

        if (a.isGenericParameter()) {
            if (b.isGenericParameter()) {
                return areEquivalent((GenericParameter) a, (GenericParameter) b);
            }

            return areEquivalent(a.getExtendsBound(), b);
        }
        else if (b.isGenericParameter()) {
            return false;
        }

        if (a.isWildcardType()) {
            return b.isWildcardType() &&
                   areEquivalent(a.getExtendsBound(), b.getExtendsBound()) &&
                   areEquivalent(a.getSuperBound(), b.getSuperBound());
        }
        else if (b.isWildcardType()) {
            return false;
        }

        if (b.isGenericType()) {
            if (!a.isGenericType()) {
                return !strict || b.isGenericDefinition();
            }

            if (a.isGenericDefinition() != b.isGenericDefinition()) {
                if (a.isGenericDefinition()) {
                    return areEquivalent(a.makeGenericType(((IGenericInstance) b).getTypeArguments()), b);
                }
                else {
                    return areEquivalent(a, b.makeGenericType(((IGenericInstance) a).getTypeArguments()));
                }
            }

            if (b instanceof IGenericInstance) {
                return a instanceof IGenericInstance &&
                       areEquivalent((IGenericInstance) a, (IGenericInstance) b);
            }
        }

        // TODO: Check scope.

        return true; //areEquivalent(a.getDeclaringType(), b.getDeclaringType());
    }

    static boolean areParametersEquivalent(final List<ParameterDefinition> a, final List<ParameterDefinition> b) {
        final int count = a.size();

        if (b.size() != count) {
            return false;
        }

        if (count == 0) {
            return true;
        }

        for (int i = 0; i < count; i++) {
            final ParameterDefinition pb = b.get(i);
            final ParameterDefinition pa = a.get(i);
            final TypeReference tb = pb.getParameterType();

            TypeReference ta = pa.getParameterType();

            if (ta.isGenericParameter() &&
                !tb.isGenericParameter() &&
                ((GenericParameter) ta).getOwner() == pa.getMethod()) {

                ta = ta.getExtendsBound();
            }

            if (!areEquivalent(ta, tb)) {
                return false;
            }
        }

        return true;
    }

    static <T extends TypeReference> boolean areEquivalent(final List<T> a, final List<T> b) {
        final int count = a.size();

        if (b.size() != count) {
            return false;
        }

        if (count == 0) {
            return true;
        }

        for (int i = 0; i < count; i++) {
            if (!areEquivalent(a.get(i), b.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean areEquivalent(final IGenericInstance a, final IGenericInstance b) {
        final List<TypeReference> typeArgumentsA = a.getTypeArguments();
        final List<TypeReference> typeArgumentsB = b.getTypeArguments();

        final int arity = typeArgumentsA.size();

        if (arity != typeArgumentsB.size()) {
            return false;
        }

        for (int i = 0; i < arity; i++) {
            if (!areEquivalent(typeArgumentsA.get(i), typeArgumentsB.get(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean areEquivalent(final GenericParameter a, final GenericParameter b) {
        if (a.getPosition() != b.getPosition()) {
            return false;
        }

        final IGenericParameterProvider ownerA = a.getOwner();
        final IGenericParameterProvider ownerB = b.getOwner();

        if (ownerA instanceof TypeDefinition) {
            return ownerB instanceof TypeDefinition &&
                   areEquivalent((TypeDefinition) ownerA, (TypeDefinition) ownerB);
        }

        if (ownerA instanceof MethodDefinition) {
            if (!(ownerB instanceof MethodDefinition)) {
                return false;
            }

            final MethodDefinition methodA = (MethodDefinition) ownerA;
            final MethodDefinition methodB = (MethodDefinition) ownerB;

            return areEquivalent(methodA.getDeclaringType(), methodB.getDeclaringType()) &&
                   StringUtilities.equals(methodA.getErasedSignature(), methodB.getErasedSignature());
        }

        return true;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="LimitedResolver Class">

    public static IMetadataResolver createLimitedResolver() {
        return new LimitedResolver();
    }

    private final static class LimitedResolver extends MetadataResolver {
        @Override
        protected TypeReference lookupTypeCore(final String descriptor) {
            return null;
        }

        @Override
        protected TypeDefinition resolveCore(final TypeReference type) {
            return type instanceof TypeDefinition ? (TypeDefinition) type : null;
        }
    }

    // </editor-fold>
}
