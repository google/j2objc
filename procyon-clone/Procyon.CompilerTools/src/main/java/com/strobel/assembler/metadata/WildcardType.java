/*
 * WildcardType.java
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

/**
 * @author Mike Strobel
 */
public final class WildcardType extends TypeReference {
    private final static WildcardType UNBOUNDED = new WildcardType(BuiltinTypes.Object, BuiltinTypes.Bottom);

    private final TypeReference _bound;
    private final boolean _hasSuperBound;

    private String _name;

    private WildcardType(final TypeReference extendsBound, final TypeReference superBound) {
        _hasSuperBound = superBound != BuiltinTypes.Bottom;
        _bound = _hasSuperBound ? superBound : extendsBound;
    }

    @Override
    public TypeReference getDeclaringType() {
        return null;
    }

    @Override
    public String getSimpleName() {
        return _name;
    }

    @Override
    public JvmType getSimpleType() {
        return JvmType.Wildcard;
    }

    // <editor-fold defaultstate="collapsed" desc="Type Attributes">

    @Override
    public boolean containsGenericParameters() {
        if (hasSuperBound()) {
            return getSuperBound().containsGenericParameters();
        }
        if (hasExtendsBound()) {
            return getExtendsBound().containsGenericParameters();
        }
        return false;
    }

    @Override
    public String getName() {
        if (_name == null) {
            _name = appendSimpleDescription(new StringBuilder()).toString();
        }
        return _name;
    }

    @Override
    public String getFullName() {
        return getName();
    }

    @Override
    public String getInternalName() {
        return getName();
    }

    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitWildcard(this, parameter);
    }

    @Override
    public boolean isWildcardType() {
        return true;
    }

    @Override
    public boolean isBoundedType() {
        return true;
    }

    @Override
    public boolean isUnbounded() {
        return _bound == null ||
               !_hasSuperBound && BuiltinTypes.Object.equals(_bound);
    }

    @Override
    public boolean hasExtendsBound() {
        return !_hasSuperBound &&
               _bound != null &&
               !BuiltinTypes.Object.equals(_bound);
    }

    @Override
    public boolean hasSuperBound() {
        return _hasSuperBound;
    }

    @Override
    public TypeReference getSuperBound() {
        return _hasSuperBound ? _bound : BuiltinTypes.Bottom;
    }

    @Override
    public TypeReference getExtendsBound() {
        return _hasSuperBound ? BuiltinTypes.Object : _bound;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        return appendSimpleDescription(sb);
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        if (isUnbounded()) {
            return sb.append('*');
        }

        if (hasSuperBound()) {
            return _bound.appendSignature(sb.append('-'));
        }

        return _bound.appendSignature(sb.append('+'));
    }

    @Override
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        if (isUnbounded()) {
            return sb.append("?");
        }

        if (hasSuperBound()) {
            sb.append("? super ");
            if (_bound.isGenericParameter()) {
                return sb.append(_bound.getFullName());
            }
            return _bound.appendErasedDescription(sb);
        }

        sb.append("? extends ");

        if (_bound.isGenericParameter()) {
            return sb.append(_bound.getFullName());
        }

        return _bound.appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        if (isUnbounded()) {
            return sb.append("?");
        }

        if (hasSuperBound()) {
            sb.append("? super ");
            if (_bound.isGenericParameter() || _bound.isWildcardType()) {
                return sb.append(_bound.getSimpleName());
            }
            return _bound.appendSimpleDescription(sb);
        }

        sb.append("? extends ");

        if (_bound.isGenericParameter() || _bound.isWildcardType()) {
            return sb.append(_bound.getSimpleName());
        }

        return _bound.appendSimpleDescription(sb);
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        return appendBriefDescription(sb);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Factory Methods">

    public static WildcardType unbounded() {
        return UNBOUNDED;
    }

    public static WildcardType makeSuper(final TypeReference superBound) {
        return new WildcardType(BuiltinTypes.Object, superBound);
    }

    public static WildcardType makeExtends(final TypeReference extendsBound) {
        return new WildcardType(extendsBound, BuiltinTypes.Bottom);
    }

    // </editor-fold>
}
