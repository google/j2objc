/*
 * TypedExpression.java
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

import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.Expression;
import com.strobel.decompiler.semantics.ResolveResult;
import com.strobel.functions.Function;

import static com.strobel.assembler.metadata.Flags.testAny;

public class TypedExpression extends Pattern {
    public final static int OPTION_EXACT = 0x01;
    public final static int OPTION_STRICT = 0x02;
    public final static int OPTION_ALLOW_UNCHECKED = 0x03;

    private final TypeReference _expressionType;
    private final String _groupName;
    private final Function<AstNode, ResolveResult> _resolver;
    private final int _options;

    public TypedExpression(final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver) {
        this(expressionType, resolver, 0);
    }

    public TypedExpression(final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver, final int options) {
        _groupName = null;
        _expressionType = VerifyArgument.notNull(expressionType, "expressionType");
        _resolver = VerifyArgument.notNull(resolver, "resolver");
        _options = options;
    }

    public TypedExpression(final String groupName, final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver) {
        this(groupName, expressionType, resolver, 0);
    }

    public TypedExpression(final String groupName, final TypeReference expressionType, final Function<AstNode, ResolveResult> resolver, final int options) {
        _groupName = groupName;
        _expressionType = VerifyArgument.notNull(expressionType, "expressionType");
        _resolver = VerifyArgument.notNull(resolver, "resolver");
        _options = options;
    }

    public final TypeReference getExpressionType() {
        return _expressionType;
    }

    public final String getGroupName() {
        return _groupName;
    }

    @Override
    public final boolean matches(final INode other, final Match match) {
        if (other instanceof Expression && !other.isNull()) {
            final ResolveResult result = _resolver.apply((Expression) other);

            if (result == null || result.getType() == null) {
                return false;
            }

            final boolean isMatch;

            if (testAny(_options, OPTION_EXACT)) {
                isMatch = MetadataHelper.isSameType(
                    _expressionType,
                    result.getType(),
                    testAny(_options, OPTION_STRICT)
                );
            }
            else {
                isMatch = MetadataHelper.isAssignableFrom(
                    _expressionType,
                    result.getType(),
                    testAny(_options, OPTION_ALLOW_UNCHECKED)
                );
            }

            if (isMatch) {
                match.add(_groupName, other);
                return true;
            }

            return false;
        }
        return false;
    }
}
