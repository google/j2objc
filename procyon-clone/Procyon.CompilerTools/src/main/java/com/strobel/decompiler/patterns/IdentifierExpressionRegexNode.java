/*
 * IdentifierExpressionRegexNode.java
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
import com.strobel.decompiler.languages.java.ast.IdentifierExpression;

public final class IdentifierExpressionRegexNode extends Pattern {
    private final String _groupName;
    private final java.util.regex.Pattern _pattern;

    public IdentifierExpressionRegexNode(final String pattern) {
        _groupName = null;
        _pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }

    public IdentifierExpressionRegexNode(final java.util.regex.Pattern pattern) {
        _groupName = null;
        _pattern = VerifyArgument.notNull(pattern, "pattern");
    }

    public IdentifierExpressionRegexNode(final String groupName, final String pattern) {
        _groupName = groupName;
        _pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }

    public IdentifierExpressionRegexNode(final String groupName, final java.util.regex.Pattern pattern) {
        _groupName = groupName;
        _pattern = VerifyArgument.notNull(pattern, "pattern");
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IdentifierExpression) {
            final IdentifierExpression identifier = (IdentifierExpression) other;

            if (_pattern.matcher(identifier.getIdentifier()).matches()) {
                match.add(_groupName, identifier);
                return true;
            }
        }

        return false;
    }
}
