/*
 * MemberInfo.java
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

import com.strobel.annotations.NotNull;
import com.strobel.core.HashUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.util.EmptyArrayCache;
import com.strobel.util.TypeUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

/**
 * @author Mike Strobel
 */
public abstract class MemberInfo implements java.lang.reflect.AnnotatedElement {
    final static Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    final static int ENUM_MODIFIER = 0x00004000;
    final static int VARARGS_MODIFIER = 0x00000080;

    private String _signature;
    private String _erasedSignature;
    private String _description;
    private String _erasedDescription;
    private String _briefDescription;
    private String _simpleDescription;

    MemberInfo() {
    }

    public abstract MemberType getMemberType();
    public abstract String getName();
    public abstract Type getDeclaringType();

    public Type getReflectedType() {
        // TODO: Implement this correctly
        return getDeclaringType();
    }

    public final boolean isFinal() {
        return Modifier.isFinal(getModifiers());
    }

    public final boolean isNonPublic() {
        return !Modifier.isPublic(getModifiers());
    }

    public final boolean isPrivate() {
        return Modifier.isPrivate(getModifiers());
    }

    public final boolean isProtected() {
        return Modifier.isProtected(getModifiers());
    }

    public final boolean isPublic() {
        return Modifier.isPublic(getModifiers());
    }

    public final boolean isStatic() {
        return Modifier.isStatic(getModifiers());
    }

    public final boolean isPackagePrivate() {
        return (getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == 0;
    }

    public abstract int getModifiers();

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        return null;
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        return EMPTY_ANNOTATIONS;
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        return EMPTY_ANNOTATIONS;
    }

    @SuppressWarnings("UnusedParameters")
    public <T extends Annotation> T getDeclaredAnnotation(final Class<T> annotationClass) {
        return null;
    }

    public <T extends Annotation> T[] getAnnotationsByType(final Class<T> annotationClass) {
        return EmptyArrayCache.fromElementType(annotationClass);
    }

    public <T extends Annotation> T[] getDeclaredAnnotationsByType(final Class<T> annotationClass) {
        return EmptyArrayCache.fromElementType(annotationClass);
    }

    @Override
    public boolean equals(final Object o) {
        return o instanceof MemberInfo &&
               isEquivalentTo((MemberInfo) o);
    }

    @Override
    public int hashCode() {
        final int nameHash = HashUtilities.hashCode(getName());
        final Type t = getDeclaringType();

        if (t != null) {
            return HashUtilities.combineHashCodes(t.hashCode(), nameHash);
        }

        return nameHash;
    }

    public boolean isEquivalentTo(final MemberInfo m) {
        return m == this ||
               (m != null &&
                TypeUtils.areEquivalent(m.getDeclaringType(), getDeclaringType()) &&
                StringUtilities.equals(getName(), m.getName()));
    }

    /**
     * Method that returns full generic signature of a type or member.
     */
    public String getSignature() {
        if (_signature == null) {
            _signature = appendSignature(new StringBuilder()).toString();
        }
        return _signature;
    }

    /**
     * Method that returns type erased signature of a type or member;
     * suitable as non-generic signature some packages need.
     */
    public String getErasedSignature() {
        if (_erasedSignature == null) {
            _erasedSignature = appendErasedSignature(new StringBuilder()).toString();
        }
        return _erasedSignature;
    }

    /**
     * Human-readable brief description of a type or member, which does not
     * include information super types, thrown exceptions, or modifiers other
     * than 'static'.
     */
    public String getBriefDescription() {
        if (_briefDescription == null) {
            _briefDescription = appendBriefDescription(new StringBuilder()).toString();
        }
        return _briefDescription;
    }

    /**
     * Human-readable full description of a type or member, which includes
     * specification of super types (in brief format), thrown exceptions,
     * and modifiers.
     */
    public String getDescription() {
        if (_description == null) {
            _description = appendDescription(new StringBuilder()).toString();
        }
        return _description;
    }

    /**
     * Human-readable erased description of a type or member.
     */
    public String getErasedDescription() {
        if (_erasedDescription == null) {
            _erasedDescription = appendErasedDescription(new StringBuilder()).toString();
        }
        return _erasedDescription;
    }

    /**
     * Human-readable simple description of a type or member, which does not
     * include information super type or fully-qualified type names.
     */
    public String getSimpleDescription() {
        if (_simpleDescription == null) {
            _simpleDescription = appendSimpleDescription(new StringBuilder()).toString();
        }
        return _simpleDescription;
    }

    @Override
    public String toString() {
        return getSimpleDescription();
    }

    public abstract StringBuilder appendDescription(StringBuilder sb);
    public abstract StringBuilder appendBriefDescription(StringBuilder sb);
    public abstract StringBuilder appendErasedDescription(StringBuilder sb);
    public abstract StringBuilder appendSignature(StringBuilder sb);
    public abstract StringBuilder appendErasedSignature(StringBuilder sb);
    public abstract StringBuilder appendSimpleDescription(StringBuilder sb);

    public StringBuilder appendGenericSignature(final StringBuilder sb) {
        return appendSignature(sb);
    }

    /**
     * Invalidate any cached type/member information.  This is not guaranteed to be thread-safe,
     * should only be called when a partially constructed type/member definition changes (e.g.,
     * a TypeBuilder, MethodBuilder, etc.).
     */
    protected void invalidateCaches() {
        _signature = null;
        _erasedSignature = null;
        _description = null;
        _erasedDescription = null;
        _briefDescription = null;
        _simpleDescription = null;
    }
}
