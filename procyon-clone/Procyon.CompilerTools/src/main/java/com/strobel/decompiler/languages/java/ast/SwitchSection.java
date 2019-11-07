/*
 * SwitchSection.java
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

import com.strobel.decompiler.patterns.INode;
import com.strobel.decompiler.patterns.Match;
import com.strobel.decompiler.patterns.Role;

public class SwitchSection extends AstNode {
    public final static Role<CaseLabel> CaseLabelRole = new Role<>("CaseLabel", CaseLabel.class);

    public final AstNodeCollection<Statement> getStatements() {
        return getChildrenByRole(Roles.EMBEDDED_STATEMENT);
    }

    public final AstNodeCollection<CaseLabel> getCaseLabels() {
        return getChildrenByRole(CaseLabelRole);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }

    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return visitor.visitSwitchSection(this, data);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Role<? extends SwitchSection> getRole() {
        return (Role<? extends SwitchSection>) super.getRole();
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SwitchSection) {
            final SwitchSection otherSection = (SwitchSection) other;

            return !otherSection.isNull() &&
                   getCaseLabels().matches(otherSection.getCaseLabels(), match) &&
                   getStatements().matches(otherSection.getStatements(), match);
        }

        return false;
    }
}
