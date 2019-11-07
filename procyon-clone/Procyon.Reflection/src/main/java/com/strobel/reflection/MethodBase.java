/*
 * MethodBase.java
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

/**
 * @author Mike Strobel
 */
public abstract class MethodBase extends MemberInfo {
    public abstract SignatureType getSignatureType();

    public ParameterList getParameters() {
        return ParameterList.empty();
    }

    public TypeList getThrownTypes() {
        return TypeList.empty();
    }

    public CallingConvention getCallingConvention() {
        return CallingConvention.fromMethodModifiers(getModifiers());
    }

    public abstract boolean containsGenericParameter(final Type<?> genericParameter);

    @Override
    public boolean isEquivalentTo(final MemberInfo m) {
        return m instanceof MethodBase &&
               super.isEquivalentTo(m) &&
               ((MethodBase) m).getParameters().getParameterTypes().isEquivalentTo(getParameters().getParameterTypes());
    }
}
