/*
 * ParameterReferenceNode.java
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

import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.ast.IdentifierExpression;
import com.strobel.decompiler.languages.java.ast.Keys;

public final class ParameterReferenceNode extends Pattern {
    private final int _parameterPosition;
    private final String _groupName;

    public ParameterReferenceNode(final int parameterPosition) {
        _parameterPosition = parameterPosition;
        _groupName = null;
    }

    public ParameterReferenceNode(final int parameterPosition, final String groupName) {
        _parameterPosition = parameterPosition;
        _groupName = groupName;
    }

    public final String getGroupName() {
        return _groupName;
    }

    public final int getParameterPosition() {
        return _parameterPosition;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IdentifierExpression) {
            final IdentifierExpression identifier = (IdentifierExpression) other;
            final Variable variable = identifier.getUserData(Keys.VARIABLE);

            if (variable != null &&
                variable.isParameter() &&
                variable.getOriginalParameter().getPosition() == _parameterPosition) {

                if (_groupName != null) {
                    match.add(_groupName, identifier);
                }

                return true;
            }
        }
        return false;
    }
}
