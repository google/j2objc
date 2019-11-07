/*
 * MemberReferenceExpressionRegexNode.java
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
import com.strobel.decompiler.languages.java.ast.MemberReferenceExpression;

public final class MemberReferenceExpressionRegexNode extends Pattern {
    private final String _groupName;
    private final INode _target;
    private final java.util.regex.Pattern _pattern;

    public MemberReferenceExpressionRegexNode(final INode target, final String pattern) {
        _groupName = null;
        _target = VerifyArgument.notNull(target, "target");
        _pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }

    public MemberReferenceExpressionRegexNode(final INode target, final java.util.regex.Pattern pattern) {
        _groupName = null;
        _target = VerifyArgument.notNull(target, "target");
        _pattern = VerifyArgument.notNull(pattern, "pattern");
    }

    public MemberReferenceExpressionRegexNode(final String groupName, final INode target, final String pattern) {
        _groupName = groupName;
        _target = VerifyArgument.notNull(target, "target");
        _pattern = java.util.regex.Pattern.compile(VerifyArgument.notNull(pattern, "pattern"));
    }

    public MemberReferenceExpressionRegexNode(final String groupName, final INode target, final java.util.regex.Pattern pattern) {
        _groupName = groupName;
        _target = VerifyArgument.notNull(target, "target");
        _pattern = VerifyArgument.notNull(pattern, "pattern");
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof MemberReferenceExpression) {
            final MemberReferenceExpression reference = (MemberReferenceExpression) other;

            if (_target.matches(reference.getTarget(), match) &&
                _pattern.matcher(reference.getMemberName()).matches()) {

                match.add(_groupName, reference);
                return true;
            }
        }

        return false;
    }
}
