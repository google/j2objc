/*
 * TypeSubstitutionVisitor.java
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

import com.strobel.core.ArrayUtilities;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class TypeSubstitutionVisitor extends DefaultTypeVisitor<Map<TypeReference, TypeReference>, TypeReference>
    implements MethodMetadataVisitor<Map<TypeReference, TypeReference>, MethodReference>,
               FieldMetadataVisitor<Map<TypeReference, TypeReference>, FieldReference> {

    private final static TypeSubstitutionVisitor INSTANCE = new TypeSubstitutionVisitor();

    public static TypeSubstitutionVisitor instance() {
        return INSTANCE;
    }

    public TypeReference visit(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        if (map.isEmpty()) {
            return t;
        }
        return t.accept(this, map);
    }

    @Override
    public TypeReference visitArrayType(final ArrayType t, final Map<TypeReference, TypeReference> map) {
        final TypeReference elementType = visit(t.getElementType(), map);

        if (elementType != null && elementType != t.getElementType()) {
            return elementType.makeArrayType();
        }

        return t;
    }

    @Override
    public TypeReference visitGenericParameter(final GenericParameter t, final Map<TypeReference, TypeReference> map) {
        TypeReference current = t;
        TypeReference mappedType;

        while ((mappedType = map.get(current)) != null &&
               mappedType != current &&
               map.get(mappedType) != current) {

            current = mappedType;
        }

        if (current == null) {
            return t;
        }

        if (current.isPrimitive()) {
            switch (current.getSimpleType()) {
                case Boolean:
                    return CommonTypeReferences.Boolean;
                case Byte:
                    return CommonTypeReferences.Byte;
                case Character:
                    return CommonTypeReferences.Character;
                case Short:
                    return CommonTypeReferences.Short;
                case Integer:
                    return CommonTypeReferences.Integer;
                case Long:
                    return CommonTypeReferences.Long;
                case Float:
                    return CommonTypeReferences.Float;
                case Double:
                    return CommonTypeReferences.Double;
                case Void:
                    return CommonTypeReferences.Void;
            }
        }

        return current;
    }

    @Override
    public TypeReference visitWildcard(final WildcardType t, final Map<TypeReference, TypeReference> map) {
        if (t.isUnbounded()) {
            return t;
        }

        final TypeReference oldBound = t.hasExtendsBound() ? t.getExtendsBound() : t.getSuperBound();
        final TypeReference mapping = map.get(oldBound);

        if (MetadataResolver.areEquivalent(mapping, t)) {
            return t;
        }

        TypeReference newBound = visit(oldBound, map);

        while (newBound.isWildcardType()) {
            if (newBound.isUnbounded()) {
                return newBound;
            }
            newBound = newBound.hasExtendsBound() ? newBound.getExtendsBound()
                                                  : newBound.getSuperBound();
        }

        if (oldBound != newBound) {
            return t.hasExtendsBound() ? WildcardType.makeExtends(newBound)
                                       : WildcardType.makeSuper(newBound);
        }

        return t;
    }

    @Override
    public TypeReference visitCompoundType(final CompoundTypeReference t, final Map<TypeReference, TypeReference> map) {
        final TypeReference oldBaseType = t.getBaseType();
        final TypeReference newBaseType = oldBaseType != null ? visit(oldBaseType, map) : null;

        TypeReference[] newInterfaces = null;

        boolean changed = newBaseType != oldBaseType;

        final List<TypeReference> oldInterfaces = t.getInterfaces();

        for (int i = 0; i < oldInterfaces.size(); i++) {
            final TypeReference oldInterface = oldInterfaces.get(i);
            final TypeReference newInterface = visit(oldInterface, map);

            if (newInterfaces != null) {
                newInterfaces[i] = newInterface;
            }
            else if (oldInterface != newInterface) {
                newInterfaces = new TypeReference[oldInterfaces.size()];
                oldInterfaces.toArray(newInterfaces);
                newInterfaces[i] = newInterface;
                changed = true;
            }
        }

        if (changed) {
            return new CompoundTypeReference(
                newBaseType,
                newInterfaces != null ? ArrayUtilities.asUnmodifiableList(newInterfaces)
                                      : t.getInterfaces()
            );
        }

        return t;
    }

    @Override
    public TypeReference visitParameterizedType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        final List<TypeReference> oldTypeArguments = ((IGenericInstance) t).getTypeArguments();

        TypeReference[] newTypeArguments = null;

        boolean changed = false;

        for (int i = 0; i < oldTypeArguments.size(); i++) {
            final TypeReference oldTypeArgument = oldTypeArguments.get(i);
            final TypeReference newTypeArgument = visit(oldTypeArgument, map);

            if (newTypeArguments != null) {
                newTypeArguments[i] = newTypeArgument;
            }
            else if (oldTypeArgument != newTypeArgument) {
                newTypeArguments = new TypeReference[oldTypeArguments.size()];
                oldTypeArguments.toArray(newTypeArguments);
                newTypeArguments[i] = newTypeArgument;
                changed = true;
            }
        }

        if (changed) {
            return t.makeGenericType(newTypeArguments);
        }

        return t;
    }

    @Override
    public TypeReference visitPrimitiveType(final PrimitiveType t, final Map<TypeReference, TypeReference> map) {
        return t;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TypeReference visitClassType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        final TypeReference resolvedType = t.isGenericType() ? t : t.resolve();

        if (resolvedType == null || !resolvedType.isGenericDefinition()) {
            return t;
        }

        final List<TypeReference> oldTypeArguments = (List<TypeReference>) (Object) resolvedType.getGenericParameters();

        TypeReference[] newTypeArguments = null;

        boolean changed = false;

        for (int i = 0; i < oldTypeArguments.size(); i++) {
            final TypeReference oldTypeArgument = oldTypeArguments.get(i);
            final TypeReference newTypeArgument = visit(oldTypeArgument, map);

            if (newTypeArguments != null) {
                newTypeArguments[i] = newTypeArgument;
            }
            else if (oldTypeArgument != newTypeArgument) {
                newTypeArguments = new TypeReference[oldTypeArguments.size()];
                oldTypeArguments.toArray(newTypeArguments);
                newTypeArguments[i] = newTypeArgument;
                changed = true;
            }
        }

        if (changed) {
            return t.makeGenericType(newTypeArguments);
        }

        return t;
    }

    @Override
    public TypeReference visitNullType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        return t;
    }

    @Override
    public TypeReference visitBottomType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        return t;
    }

    @Override
    public TypeReference visitRawType(final RawType t, final Map<TypeReference, TypeReference> map) {
        return t;
    }

    @Override
    public MethodReference visitParameterizedMethod(final MethodReference m, final Map<TypeReference, TypeReference> map) {
        return visitMethod(m, map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MethodReference visitMethod(final MethodReference m, final Map<TypeReference, TypeReference> map) {
        final MethodDefinition resolvedMethod = m.resolve();

        final List<TypeReference> oldTypeArguments;
        final List<TypeReference> newTypeArguments;

        if (m instanceof IGenericInstance) {
            oldTypeArguments = ((IGenericInstance) m).getTypeArguments();
        }
        else if (m.isGenericDefinition()) {
            oldTypeArguments = (List<TypeReference>) (Object) m.getGenericParameters();
        }
        else {
            oldTypeArguments = Collections.emptyList();
        }

        newTypeArguments = visitTypes(oldTypeArguments, map);

        final TypeReference oldReturnType = m.getReturnType();
        final TypeReference newReturnType = visit(oldReturnType, map);

        final List<ParameterDefinition> oldParameters = m.getParameters();
        final List<ParameterDefinition> newParameters = visitParameters(oldParameters, map);

        if (newTypeArguments != oldTypeArguments ||
            newReturnType != oldReturnType ||
            newParameters != oldParameters) {

            return new GenericMethodInstance(
                visit(m.getDeclaringType(), map),
                resolvedMethod != null ? resolvedMethod : m,
                newReturnType,
                newParameters == oldParameters ? MetadataHelper.copyParameters(oldParameters)
                                               : newParameters,
                newTypeArguments
            );
        }

        return m;
    }

    @Override
    public TypeReference visitCapturedType(
        final CapturedType t,
        final Map<TypeReference, TypeReference> map) {

        final TypeReference oldExtendsBound = t.getExtendsBound();
        final TypeReference oldSuperBound = t.getSuperBound();
        final TypeReference oldWildcard = t.getWildcard();

        final TypeReference newExtendsBound = visit(oldExtendsBound, map);
        final TypeReference newSuperBound = visit(oldSuperBound, map);
        final TypeReference newWildcard = visitWildcard((WildcardType) oldWildcard, map);

        if (newExtendsBound != oldExtendsBound ||
            newSuperBound != oldSuperBound ||
            newWildcard != oldWildcard) {

            return new CapturedType(newSuperBound, newExtendsBound, (WildcardType) newWildcard);
        }

        return t;
    }

    protected List<TypeReference> visitTypes(
        final List<TypeReference> types,
        final Map<TypeReference, TypeReference> map) {

        TypeReference[] newTypes = null;

        boolean changed = false;

        for (int i = 0; i < types.size(); i++) {
            final TypeReference oldTypeArgument = types.get(i);
            final TypeReference newTypeArgument = visit(oldTypeArgument, map);

            if (newTypes != null) {
                newTypes[i] = newTypeArgument;
            }
            else if (oldTypeArgument != newTypeArgument) {
                newTypes = new TypeReference[types.size()];
                types.toArray(newTypes);
                newTypes[i] = newTypeArgument;
                changed = true;
            }
        }

        return changed ? ArrayUtilities.asUnmodifiableList(newTypes)
                       : types;
    }

    protected List<ParameterDefinition> visitParameters(
        final List<ParameterDefinition> parameters,
        final Map<TypeReference, TypeReference> map) {

        if (parameters.isEmpty()) {
            return parameters;
        }

        ParameterDefinition[] newParameters = null;

        boolean changed = false;

        for (int i = 0; i < parameters.size(); i++) {
            final ParameterDefinition oldParameter = parameters.get(i);

            final TypeReference oldType = oldParameter.getParameterType();
            final TypeReference newType = visit(oldType, map);

            final ParameterDefinition newParameter;

            newParameter = oldType != newType ? new ParameterDefinition(oldParameter.getSlot(), newType)
                                              : oldParameter;

            if (newParameters != null) {
                newParameters[i] = newParameter;
            }
            else if (oldType != newType) {
                newParameters = new ParameterDefinition[parameters.size()];
                parameters.toArray(newParameters);
                newParameters[i] = newParameter;
                changed = true;
            }
        }

        return changed ? ArrayUtilities.asUnmodifiableList(newParameters)
                       : parameters;
    }

    @Override
    public FieldReference visitField(final FieldReference f, final Map<TypeReference, TypeReference> map) {
        final TypeReference oldFieldType = f.getFieldType();
        final TypeReference newFieldType = visit(oldFieldType, map);

        if (newFieldType != oldFieldType) {
            final TypeReference declaringType = f.getDeclaringType();

            return new FieldReference() {
                private final String _name = f.getName();
                private final TypeReference _type = newFieldType;

                @Override
                public TypeReference getFieldType() {
                    return _type;
                }

                @Override
                public TypeReference getDeclaringType() {
                    return declaringType;
                }

                @Override
                public String getName() {
                    return _name;
                }

                @Override
                protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
                    if (fullName) {
                        final TypeReference declaringType = getDeclaringType();

                        if (declaringType != null) {
                            return declaringType.appendName(sb, true, false).append('.').append(getName());
                        }
                    }

                    return sb.append(_name);
                }
            };
        }

        return f;
    }
}
