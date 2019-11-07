/*
 * TypeProxy.java
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

package com.strobel.decompiler.types;

import com.strobel.collections.ImmutableList;
import com.strobel.core.VerifyArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class TypeProxy implements ITypeInfo {
    private final static List<ITypeListener> EMPTY_LISTENERS = Collections.emptyList();

    private final ITypeListener _listener;

    private List<ITypeListener> _listeners;
    private ITypeInfo _delegate;

    TypeProxy(final ITypeInfo delegate) {
        VerifyArgument.notNull(delegate, "delegate");

        _listeners = EMPTY_LISTENERS;
        _listener = new DelegateListener();

        setDelegate(delegate);
    }

    final void setDelegate(final ITypeInfo delegate) {
        VerifyArgument.notNull(delegate, "delegate");

        if (_delegate != null) {
            _delegate.removeListener(_listener);
        }

        _delegate = delegate;
        _delegate.addListener(_listener);
    }

    @Override
    public final String getName() {
        return _delegate.getName();
    }

    @Override
    public final String getPackageName() {
        return _delegate.getPackageName();
    }

    @Override
    public final String getFullName() {
        return _delegate.getFullName();
    }

    @Override
    public final String getCanonicalName() {
        return _delegate.getCanonicalName();
    }

    @Override
    public final String getInternalName() {
        return _delegate.getInternalName();
    }

    @Override
    public final String getSignature() {
        return _delegate.getSignature();
    }

    @Override
    public final boolean isArray() {
        return _delegate.isArray();
    }

    @Override
    public final boolean isPrimitive() {
        return _delegate.isPrimitive();
    }

    @Override
    public final boolean isPrimitiveOrVoid() {
        return _delegate.isPrimitiveOrVoid();
    }

    @Override
    public final boolean isVoid() {
        return _delegate.isVoid();
    }

    @Override
    public final boolean isRawType() {
        return _delegate.isRawType();
    }

    @Override
    public final boolean isGenericType() {
        return _delegate.isGenericType();
    }

    @Override
    public final boolean isGenericTypeInstance() {
        return _delegate.isGenericTypeInstance();
    }

    @Override
    public final boolean isGenericTypeDefinition() {
        return _delegate.isGenericTypeDefinition();
    }

    @Override
    public final boolean isGenericParameter() {
        return _delegate.isGenericParameter();
    }

    @Override
    public final boolean isWildcard() {
        return _delegate.isWildcard();
    }

    @Override
    public final boolean isUnknownType() {
        return _delegate.isUnknownType();
    }

    @Override
    public final boolean isBound() {
        return _delegate.isBound();
    }

    @Override
    public final boolean isLocal() {
        return _delegate.isLocal();
    }

    @Override
    public final boolean isAnonymous() {
        return _delegate.isAnonymous();
    }

    @Override
    public final ITypeInfo getDeclaringType() {
        return _delegate.getDeclaringType();
    }

    @Override
    public final boolean hasConstraints() {
        return _delegate.hasConstraints();
    }

    @Override
    public final boolean hasSuperConstraint() {
        return _delegate.hasSuperConstraint();
    }

    @Override
    public final boolean hasExtendsConstraint() {
        return _delegate.hasExtendsConstraint();
    }

    @Override
    public final ITypeInfo getElementType() {
        return _delegate.getElementType();
    }

    @Override
    public final ITypeInfo getSuperConstraint() {
        return _delegate.getSuperConstraint();
    }

    @Override
    public final ITypeInfo getExtendsConstraint() {
        return _delegate.getExtendsConstraint();
    }

    @Override
    public final ITypeInfo getSuperClass() {
        return _delegate.getSuperClass();
    }

    @Override
    public final ImmutableList<ITypeInfo> getSuperInterfaces() {
        return _delegate.getSuperInterfaces();
    }

    @Override
    public final ImmutableList<ITypeInfo> getGenericParameters() {
        return _delegate.getGenericParameters();
    }

    @Override
    public final ImmutableList<ITypeInfo> getTypeArguments() {
        return _delegate.getTypeArguments();
    }

    @Override
    public final ITypeInfo getGenericDefinition() {
        return _delegate.getGenericDefinition();
    }

    @Override
    public final void removeListener(final ITypeListener listener) {
        VerifyArgument.notNull(listener, "listener");

        if (_listeners == EMPTY_LISTENERS) {
            return;
        }

        _listeners.remove(listener);
    }

    @Override
    public final void addListener(final ITypeListener listener) {
        VerifyArgument.notNull(listener, "listener");

        if (_listeners == EMPTY_LISTENERS) {
            _listeners = new ArrayList<>();
        }

        _listeners.add(listener);
    }

    final void notifyChanged() {
        final List<ITypeListener> listeners = _listeners;

        if (listeners == EMPTY_LISTENERS) {
            return;
        }

        for (final ITypeListener listener : listeners) {
            listener.onChanged();
        }
    }

    private final class DelegateListener implements ITypeListener {
        @Override
        public final void onChanged() {
            TypeProxy.this.notifyChanged();
        }
    }
}
