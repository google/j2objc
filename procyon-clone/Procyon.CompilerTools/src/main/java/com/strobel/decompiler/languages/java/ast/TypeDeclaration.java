/*
 * TypeDeclaration.java
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

package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.EntityType;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;

public class TypeDeclaration extends EntityDeclaration {
    private ClassType _classType;

    public final JavaTokenNode getTypeKeyword() {
        switch (_classType) {
            case CLASS:
                return getChildByRole(Roles.CLASS_KEYWORD);
            case INTERFACE:
                return getChildByRole(Roles.INTERFACE_KEYWORD);
            case ANNOTATION:
                return getChildByRole(Roles.ANNOTATION_KEYWORD);
            case ENUM:
                return getChildByRole(Roles.ENUM_KEYWORD);
            default:
                return JavaTokenNode.NULL;
        }
    }

    public final ClassType getClassType() {
        return _classType;
    }

    public final void setClassType(final ClassType classType) {
        verifyNotFrozen();
        _classType = classType;
    }

    public final AstNodeCollection<TypeParameterDeclaration> getTypeParameters() {
        return getChildrenByRole(Roles.TYPE_PARAMETER);
    }

    public final AstNodeCollection<AstType> getInterfaces() {
        return getChildrenByRole(Roles.IMPLEMENTED_INTERFACE);
    }

    public final AstType getBaseType() {
        return getChildByRole(Roles.BASE_TYPE);
    }

    public final void setBaseType(final AstType value) {
        setChildByRole(Roles.BASE_TYPE, value);
    }

    public final JavaTokenNode getLeftBraceToken() {
        return getChildByRole(Roles.LEFT_BRACE);
    }

    public final AstNodeCollection<EntityDeclaration> getMembers() {
        return getChildrenByRole(Roles.TYPE_MEMBER);
    }

    public final JavaTokenNode getRightBraceToken() {
        return getChildByRole(Roles.RIGHT_BRACE);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TYPE_DECLARATION;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.TYPE_DEFINITION;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitTypeDeclaration(this, data);
    }

    @Override
    public TypeDeclaration clone() {
        final TypeDeclaration copy = (TypeDeclaration) super.clone();
        copy._classType = _classType;
        return copy;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof TypeDeclaration) {
            final TypeDeclaration otherDeclaration = (TypeDeclaration) other;

            return !otherDeclaration.isNull() &&
                   _classType == otherDeclaration._classType &&
                   matchString(getName(), otherDeclaration.getName()) &&
                   matchAnnotationsAndModifiers(otherDeclaration, match) &&
                   getTypeParameters().matches(otherDeclaration.getTypeParameters(), match) &&
                   getBaseType().matches(otherDeclaration.getBaseType(), match) &&
                   getInterfaces().matches(otherDeclaration.getInterfaces(), match) &&
                   getMembers().matches(otherDeclaration.getMembers(), match);
        }

        return false;
    }

    // <editor-fold defaultstate="collapsed" desc="Null TypeDeclaration">

    public final static TypeDeclaration NULL = new NullTypeDeclaration();

    private static final class NullTypeDeclaration extends TypeDeclaration {
        @Override
        public final boolean isNull() {
            return true;
        }

        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return null;
        }

        @Override
        public boolean matches(final INode other, final Match match) {
            return other == null || other.isNull();
        }
    }

    // </editor-fold>
}
