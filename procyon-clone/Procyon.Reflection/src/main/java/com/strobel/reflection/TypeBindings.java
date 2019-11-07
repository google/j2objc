/*
 * TypeBindings.java
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

package com.strobel.reflection;

import com.strobel.core.VerifyArgument;

/**
 * @author Mike Strobel
 */
public final class TypeBindings {
    private final static TypeBindings EMPTY = new TypeBindings(TypeList.empty(), TypeList.empty());

    private final TypeList _genericParameters;
    private final TypeList _boundTypes;
    private final int _hashCode;

    private TypeBindings(final TypeList genericParameters, final TypeList boundTypes) {
        _genericParameters = genericParameters;
        _boundTypes = boundTypes;

        final int parameterCount = _genericParameters.size();

        if (parameterCount != boundTypes.size()) {
            throw Error.incorrectNumberOfTypeArguments();
        }

        for (int i = 0; i < parameterCount; i++) {
            if (!genericParameters.get(i).isGenericParameter()) {
                throw new IllegalArgumentException("All types in the 'genericParameters' list must be generic parameters types.");
            }
        }

        int hash = 1;

        for (final Type boundType : boundTypes) {
            if (boundType != null) {
                hash = hash * 31 + boundType.hashCode();
            }
        }

        _hashCode = hash;
    }

    public static TypeBindings empty() {
        return EMPTY;
    }

    public static TypeBindings createUnbound(final TypeList genericParameters) {
        return new TypeBindings(
            VerifyArgument.noNullElements(genericParameters, "genericParameters"),
            genericParameters
        );
    }

    public static TypeBindings create(final TypeList genericParameters, final Type... boundTypes) {
        return new TypeBindings(
            VerifyArgument.noNullElements(genericParameters, "genericParameters"),
            Type.list(VerifyArgument.noNullElements(boundTypes, "boundTypes"))
        );
    }

    public static TypeBindings create(final TypeList genericParameters, final TypeList boundTypes) {
        return new TypeBindings(
            VerifyArgument.noNullElements(genericParameters, "genericParameters"),
            VerifyArgument.noNullElements(boundTypes, "boundTypes")
        );
    }

    public TypeList getGenericParameters() {
        return _genericParameters;
    }

    public TypeList getBoundTypes() {
        return _boundTypes;
    }

    public Type getGenericParameter(final int index) {
        VerifyArgument.inRange(0, size(), index, "index");
        return _genericParameters.get(index);
    }

    public Type getBoundType(final int index) {
        VerifyArgument.inRange(0, size(), index, "index");
        return _boundTypes.get(index);
    }

    public boolean containsGenericParameter(final Type type) {
        return type != null && _genericParameters.contains(type);
    }

    public boolean containsBoundType(final Type type) {
        return type != null && _boundTypes.contains(type);
    }

    public TypeBindings bindingsFor(final TypeList genericParameters) {
        if (VerifyArgument.notNull(genericParameters, "genericParameters").isEmpty()) {
            return empty();
        }

        final Type[] boundTypes = new Type[genericParameters.size()];

        for (int i = 0, n = genericParameters.size(); i < n; i++) {
            final Type genericParameter = genericParameters.get(i);
            final int index = _genericParameters.indexOf(genericParameter);

            if (index == -1) {
                boundTypes[i] = genericParameters.get(i);
            }
            else {
                boundTypes[i] = _boundTypes.get(index);
            }
        }

        return new TypeBindings(genericParameters, Type.list(boundTypes));
    }

    public boolean hasConcreteParameter(final Type genericParameter) {
        final int index = _genericParameters.indexOf(genericParameter);
        return index != -1 &&
               !_boundTypes.get(index).isGenericParameter();
    }

    public boolean hasConcreteParameters() {
        for (int i = 0, n = size(); i < n; i++) {
            final Type parameter = getBoundType(i);
            if (!parameter.isGenericParameter()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasUnboundParameters() {
        for (int i = 0, n = size(); i < n; i++) {
            final Type parameter = getBoundType(i);
            if (parameter.isGenericParameter()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBoundParameter(final Type genericParameter) {
        final int index = _genericParameters.indexOf(genericParameter);
        return index != -1 &&
               _boundTypes.get(index) != genericParameter;
    }


    public boolean hasBoundParameters() {
        for (int i = 0, n = size(); i < n; i++) {
            final Type genericParameter = getGenericParameter(i);
            final Type parameter = getBoundType(i);

            if (parameter != genericParameter) {
                return true;
            }
        }
        return false;
    }

    public TypeBindings withAdditionalBinding(final Type genericParameter, final Type typeArgument) {
        final TypeList genericParameters;
        final Type[] boundTypes;

        int index = _genericParameters.indexOf(genericParameter);

        if (index == -1) {
            boundTypes = new Type[_genericParameters.size() + 1];
            _boundTypes.toArray(boundTypes);
            index = boundTypes.length - 1;

            final Type[] genericParameterArray = new Type[boundTypes.length];
            _genericParameters.toArray(genericParameterArray);
            genericParameterArray[index] = genericParameter;
            genericParameters = Type.list(genericParameterArray);
        }
        else {
            genericParameters = _genericParameters;
            boundTypes = _boundTypes.toArray(new Type[_boundTypes.size()]);
        }

        boundTypes[index] = typeArgument;

        final TypeBindings results = new TypeBindings(genericParameters, Type.list(boundTypes));

        for (int i = 0, n = boundTypes.length; i < n; i++) {
            if (boundTypes[i] == genericParameter && i != index) {
                return results.withAdditionalBinding(genericParameters.get(i), typeArgument);
//                boundTypes[i] = typeArgument;
            }
        }

        return results;
    }

    public TypeBindings withAdditionalBindings(final TypeBindings additionalBindings) {
        TypeBindings bindings = this;

        for (final Type parameter : additionalBindings.getGenericParameters()) {
            bindings = bindings.withAdditionalBinding(parameter, additionalBindings.getBoundType(parameter));
        }

        return bindings;
    }

    public TypeBindings withAdditionalParameter(final Type genericParameter) {
        if (containsGenericParameter(genericParameter)) {
            return this;
        }

        final Type[] genericParameters;
        final Type[] boundTypes;

        final int newParameterCount = _genericParameters.size() + 1;

        boundTypes = new Type[newParameterCount];
        genericParameters = new Type[newParameterCount];

        _boundTypes.toArray(boundTypes);
        _genericParameters.toArray(genericParameters);

        genericParameters[newParameterCount - 1] = genericParameter;
        boundTypes[newParameterCount - 1] = genericParameter;

        return new TypeBindings(Type.list(genericParameters), Type.list(boundTypes));
    }

    public Type findGenericParameter(final String genericParameterName) {
        for (int i = 0, n = _genericParameters.size(); i < n; i++) {
            final Type parameter = _genericParameters.get(i);
            if (parameter.getFullName().equals(genericParameterName)) {
                return parameter;
            }
        }
        return null;
    }

    public Type findBoundType(final String genericParameterName) {
        for (int i = 0, n = _genericParameters.size(); i < n; i++) {
            final Type parameter = _genericParameters.get(i);
            if (parameter.getFullName().equals(genericParameterName)) {
                return getBoundType(i);
            }
        }
        return null;
    }

    public Type getBoundType(final Type genericParameter) {
        final int index = _genericParameters.indexOf(genericParameter);
        if (index == -1) {
            throw Error.typeParameterNotDefined(genericParameter);
        }
        return getBoundType(index);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('<');

        for (int i = 0, n = size(); i < n; ++i) {
            if (i > 0) {
                sb.append(',');
            }
            final Type binding = getBoundType(i);
            if (binding == null) {
                sb.append('<');
                sb.append(i);
                sb.append('>');
            }
            else {
                sb = binding.appendBriefDescription(sb);
            }
        }

        sb.append('>');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return _hashCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }

        final int size = size();
        final TypeBindings other = (TypeBindings)o;

        if (other._hashCode != _hashCode) {
            return false;
        }

        if (other.size() != size) {
            return false;
        }

        for (int i = 0; i < size; ++i) {
            final Type parameter = getGenericParameter(i);
            final Type otherParameter = other.getGenericParameter(i);

            if (otherParameter == null) {
                if (parameter != null) {
                    return false;
                }
            }
            else if (!otherParameter.equals(parameter)) {
                return false;
            }

            final Type binding = getBoundType(i);
            final Type otherBinding = other.getBoundType(i);

            if (otherBinding == null) {
                if (binding != null) {
                    return false;
                }
            }
            else if (!otherBinding.equals(binding)) {
                return false;
            }
        }

        return true;
    }

    public int size() {
        return _genericParameters.size();
    }

    public boolean isEmpty() {
        return _genericParameters.isEmpty();
    }
}
