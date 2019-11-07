/*
 * EntityDeclaration.java
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

import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.EntityType;
import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class EntityDeclaration extends AstNode {
    public final static Role<Annotation> ANNOTATION_ROLE = Roles.ANNOTATION;
    public final static Role<Annotation> UNATTACHED_ANNOTATION_ROLE = new Role<>("UnattachedAnnotation", Annotation.class);
    public final static Role<JavaModifierToken> MODIFIER_ROLE = new Role<>("Modifier", JavaModifierToken.class);
    public final static Role<AstType> PRIVATE_IMPLEMENTATION_TYPE_ROLE = new Role<>("PrivateImplementationType", AstType.class, AstType.NULL);

    private boolean _anyModifiers;

    /**
     * Gets the "any" modifiers flag used during pattern matching.
     */
    public final boolean isAnyModifiers() {
        return _anyModifiers;
    }

    /**
     * Sets the "any" modifiers flag used during pattern matching.
     */
    public final void setAnyModifiers(final boolean value) {
        verifyNotFrozen();
        _anyModifiers = value;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MEMBER;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends EntityDeclaration> getRole() {
        return (Role<? extends EntityDeclaration>) super.getRole();
    }

    public abstract EntityType getEntityType();

    public final AstNodeCollection<Annotation> getAnnotations() {
        return getChildrenByRole(ANNOTATION_ROLE);
    }

    public final boolean hasModifier(final Modifier modifier) {
        for (final JavaModifierToken modifierToken : getModifiers()) {
            if (modifierToken.getModifier() == modifier) {
                return true;
            }
        }
        return false;
    }

    public final AstNodeCollection<JavaModifierToken> getModifiers() {
        return getChildrenByRole(MODIFIER_ROLE);
    }

    public final String getName() {
        return getChildByRole(Roles.IDENTIFIER).getName();
    }

    public final void setName(final String value) {
        setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }

    public final Identifier getNameToken() {
        return getChildByRole(Roles.IDENTIFIER);
    }

    public final void setNameToken(final Identifier value) {
        setChildByRole(Roles.IDENTIFIER, value);
    }

    public final AstType getReturnType() {
        return getChildByRole(Roles.TYPE);
    }

    public final void setReturnType(final AstType type) {
        setChildByRole(Roles.TYPE, type);
    }

    @Override
    public EntityDeclaration clone() {
        final EntityDeclaration copy = (EntityDeclaration) super.clone();
        copy._anyModifiers = _anyModifiers;
        return copy;
    }

    protected final boolean matchAnnotationsAndModifiers(final EntityDeclaration other, final Match match) {
        return other != null &&
               !other.isNull() &&
               (isAnyModifiers() || getModifiers().matches(other.getModifiers(), match)) &&
               getAnnotations().matches(other.getAnnotations(), match);
    }

    static List<Modifier> getModifiers(final AstNode node) {
        List<Modifier> modifiers = null;

        for (final JavaModifierToken modifierToken : node.getChildrenByRole(MODIFIER_ROLE)) {
            if (modifiers == null) {
                modifiers = new ArrayList<>();
            }

            modifiers.add(modifierToken.getModifier());
        }

        return modifiers != null ? Collections.unmodifiableList(modifiers)
                                 : Collections.<Modifier>emptyList();
    }

    static void setModifiers(final AstNode node, final Collection<Modifier> modifiers) {
        final AstNodeCollection<JavaModifierToken> modifierTokens = node.getChildrenByRole(MODIFIER_ROLE);

        modifierTokens.clear();

        for (final Modifier modifier : modifiers) {
            modifierTokens.add(new JavaModifierToken(TextLocation.EMPTY, modifier));
        }
    }

    static void addModifier(final AstNode node, final Modifier modifier) {
        final List<Modifier> modifiers = getModifiers(node);

        if (modifiers.contains(modifier)) {
            return;
        }

        node.addChild(new JavaModifierToken(TextLocation.EMPTY, modifier), MODIFIER_ROLE);
    }

    static boolean removeModifier(final AstNode node, final Modifier modifier) {
        final AstNodeCollection<JavaModifierToken> modifierTokens = node.getChildrenByRole(MODIFIER_ROLE);

        for (final JavaModifierToken modifierToken : modifierTokens) {
            if (modifierToken.getModifier() == modifier) {
                modifierToken.remove();
                return true;
            }
        }

        return false;
    }
}
