/*
 * DeclaredVariableBackReference.java
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

package com.strobel.decompiler.patterns;

import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.AstNodeCollection;
import com.strobel.decompiler.languages.java.ast.Roles;
import com.strobel.decompiler.languages.java.ast.VariableDeclarationStatement;
import com.strobel.decompiler.languages.java.ast.VariableInitializer;

import static com.strobel.core.CollectionUtilities.lastOrDefault;

public final class DeclaredVariableBackReference extends Pattern {
    private final String _referencedGroupName;

    public DeclaredVariableBackReference(final String referencedGroupName) {
        _referencedGroupName = VerifyArgument.notNull(referencedGroupName, "referencedGroupName");
    }

    public final String getReferencedGroupName() {
        return _referencedGroupName;
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        if (other instanceof AstNode) {
            final INode lastInGroup = lastOrDefault(match.get(_referencedGroupName));

            if (lastInGroup instanceof VariableDeclarationStatement) {
                final VariableDeclarationStatement referenced = (VariableDeclarationStatement) lastInGroup;
                final AstNodeCollection<VariableInitializer> variables = referenced.getVariables();

                return variables.hasSingleElement() &&
                       matchString(
                           variables.firstOrNullObject().getName(),
                           ((AstNode) other).getChildByRole(Roles.IDENTIFIER).getName()
                       );
            }
        }
        return false;
    }
}
