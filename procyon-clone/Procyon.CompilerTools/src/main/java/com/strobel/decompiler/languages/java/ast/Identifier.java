/*
 * Identifier.java
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

import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.TextLocation;
import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

public class Identifier extends AstNode {
    private TextLocation _startLocation;
    private String _name;

    private Identifier() {
        this("", TextLocation.EMPTY);
    }

    protected Identifier(final String name, final TextLocation location) {
        _name = VerifyArgument.notNull(name, "name");
        _startLocation = VerifyArgument.notNull(location, "location");
    }

    public final String getName() {
        return _name;
    }

    public final void setName(final String name) {
        verifyNotFrozen();
        _name = VerifyArgument.notNull(name, "name");
    }

    @Override
    public TextLocation getStartLocation() {
        return _startLocation;
    }

    public void setStartLocation(final TextLocation startLocation) {
        _startLocation = startLocation;
    }

    @Override
    public TextLocation getEndLocation() {
        final String name = _name;

        return new TextLocation(
            _startLocation.line(),
            _startLocation.column() + (name != null ? name.length() : 0)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends Identifier> getRole() {
        return (Role<? extends Identifier>) super.getRole();
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitIdentifier(this, data);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TOKEN;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof Identifier &&
               !other.isNull() &&
               matchString(getName(), ((Identifier) other).getName());
    }

    // <editor-fold defaultstate="collapsed" desc="Null Identifier">

    public final static Identifier NULL = new NullIdentifier();

    private static final class NullIdentifier extends Identifier {
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

    // <editor-fold defaultstate="collapsed" desc="Factory Methods">

    public static Identifier create(final String name) {
        return create(name, TextLocation.EMPTY);
    }

    public static Identifier create(final String name, final TextLocation location) {
        if (StringUtilities.isNullOrEmpty(name)) {
            return NULL;
        }
        return new Identifier(name, location);
    }

    // </editor-fold>
}
