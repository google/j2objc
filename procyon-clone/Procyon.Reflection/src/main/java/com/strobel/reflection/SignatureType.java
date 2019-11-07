package com.strobel.reflection;

import com.strobel.core.VerifyArgument;
import com.strobel.util.TypeUtils;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 1:07 PM
 */
public final class SignatureType {
    private final Type<?> _returnType;
    private final TypeList _parameterTypes;

    private SignatureType _erasedSignature;

    public SignatureType(final Type<?> returnType, final TypeList parameterTypes) {
        VerifyArgument.notNull(returnType, "returnType");
        VerifyArgument.notNull(parameterTypes, "parameterTypes");

        _returnType = returnType.isWildcardType() ? returnType.getExtendsBound() : returnType;
        _parameterTypes = parameterTypes;
    }

    public final Type<?> getReturnType() {
        return _returnType;
    }

    public final TypeList getParameterTypes() {
        return _parameterTypes;
    }

    public final SignatureType getErasedSignature() {
        if (_erasedSignature == null) {
            synchronized (this) {
                if (_erasedSignature == null) {
                    final Type<?> returnType = _returnType.getErasedType();
                    final TypeList parameterTypes = _parameterTypes.getErasedTypes();

                    if (returnType.isEquivalentTo(_returnType) &&
                        parameterTypes.isEquivalentTo(_parameterTypes)) {

                        _erasedSignature = this;
                    }
                    else {
                        _erasedSignature = new SignatureType(returnType, parameterTypes);
                    }
                }
            }
        }
        return _erasedSignature;
    }

    public final boolean isEquivalentTo(final SignatureType other) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (!other._returnType.isEquivalentTo(_returnType)) {
            return false;
        }

        final int parameterCount = _parameterTypes.size();

        if (other._parameterTypes.size() != parameterCount) {
            return false;
        }

        for (int i = 0; i < parameterCount; i++) {
            if (!TypeUtils.areEquivalent(other._parameterTypes.get(i), _parameterTypes.get(i))) {
                return false;
            }
        }

        return true;
    }

    public final boolean containsGenericParameters() {
        return _returnType.containsGenericParameters() ||
               _parameterTypes.containsGenericParameters();
    }

    public final boolean containsGenericParameter(final Type<?> genericParameter) {
        return _returnType.containsGenericParameter(genericParameter) ||
               _parameterTypes.containsGenericParameter(genericParameter);
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        return o instanceof SignatureType &&
               isEquivalentTo((SignatureType) o);
    }

    @Override
    public final int hashCode() {
        int result = _returnType.hashCode();
        result = 31 * result + _parameterTypes.hashCode();
        return result;
    }
}
