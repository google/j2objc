/*
 * Pattern.java
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

import com.strobel.core.StringUtilities;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.AstType;
import com.strobel.decompiler.languages.java.ast.BlockStatement;
import com.strobel.decompiler.languages.java.ast.CatchClause;
import com.strobel.decompiler.languages.java.ast.Expression;
import com.strobel.decompiler.languages.java.ast.ParameterDeclaration;
import com.strobel.decompiler.languages.java.ast.Statement;
import com.strobel.decompiler.languages.java.ast.VariableInitializer;

import java.util.Stack;

public abstract class Pattern implements INode {
    public final static String ANY_STRING = "$any$";

    public static boolean matchString(final String pattern, final String text) {
        return ANY_STRING.equals(pattern) || StringUtilities.equals(pattern, text);
    }

    public final AstNode toNode() {
        return AstNode.forPattern(this);
    }

    public final Expression toExpression() {
        return Expression.forPattern(this);
    }

    public final Statement toStatement() {
        return Statement.forPattern(this);
    }

    public final BlockStatement toBlockStatement() {
        return BlockStatement.forPattern(this);
    }

    public final CatchClause toCatchClause() {
        return CatchClause.forPattern(this);
    }

    public final VariableInitializer toVariableInitializer() {
        return VariableInitializer.forPattern(this);
    }

    public final ParameterDeclaration toParameterDeclaration() {
        return ParameterDeclaration.forPattern(this);
    }

    public final AstType toType() {
        return AstType.forPattern(this);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public Role getRole() {
        return null;
    }

    @Override
    public INode getFirstChild() {
        return null;
    }

    @Override
    public INode getNextSibling() {
        return null;
    }

    @Override
    public abstract boolean matches(final INode other, final Match match);

    @Override
    public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
        return matches(position, match);
    }

    @Override
    public final Match match(final INode other) {
        final Match match = Match.createNew();
        return matches(other, match) ? match : Match.failure();
    }

    @Override
    public final boolean matches(final INode other) {
        return matches(other, Match.createNew());
    }

    public static boolean matchesCollection(
        final Role<?> role,
        final INode firstPatternChild,
        final INode firstOtherChild,
        final Match match) {

        final BacktrackingInfo backtrackingInfo = new BacktrackingInfo();
        final Stack<INode> patternStack = new Stack<>();
        final Stack<PossibleMatch> stack = backtrackingInfo.stack;

        patternStack.push(firstPatternChild);
        stack.push(new PossibleMatch(firstOtherChild, match.getCheckPoint()));

        while (!stack.isEmpty()) {
            INode current1 = patternStack.pop();
            INode current2 = stack.peek().nextOther;

            match.restoreCheckPoint(stack.pop().checkPoint);

            boolean success = true;

            while (current1 != null && success) {
                while (current1 != null && current1.getRole() != role) {
                    current1 = current1.getNextSibling();
                }
                while (current2 != null && current2.getRole() != role) {
                    current2 = current2.getNextSibling();
                }
                if (current1 == null) {
                    break;
                }

                assert stack.size() == patternStack.size();
                success = current1.matchesCollection(role, current2, match, backtrackingInfo);
                assert stack.size() >= patternStack.size();

                while (stack.size() > patternStack.size()) {
                    patternStack.push(current1.getNextSibling());
                }

                current1 = current1.getNextSibling();

                if (current2 != null) {
                    current2 = current2.getNextSibling();
                }
            }

            while (current2 != null && current2.getRole() != role) {
                current2 = current2.getNextSibling();
            }

            if (success && current2 == null) {
                return true;
            }
        }

        return false;
/*
        final BacktrackingInfo backtrackingInfo = new BacktrackingInfo();
        final Stack<INode> patternStack = new Stack<>();
        final Stack<PossibleMatch> stack = new Stack<>();

        patternStack.push(firstPatternChild);
        stack.push(new PossibleMatch(firstOtherChild, match.getCheckPoint()));

        while (!stack.isEmpty()) {
            INode current1 = patternStack.pop();
            INode current2 = stack.peek().nextOther;

            match.restoreCheckPoint(stack.pop().checkPoint);

            boolean success = true;

            while (current1 != null && success) {
                while (current1 != null && current1.getRole() != role) {
                    current1 = current1.getNextSibling();
                }

                while (current2 != null && current2.getRole() != role) {
                    current2 = current2.getNextSibling();
                }

                if (current1 == null) {
                    break;
                }

                assert stack.size() == patternStack.size();
                success = current1.matchesCollection(role, current2, match, backtrackingInfo);
                assert stack.size() >= patternStack.size();

                while (stack.size() > patternStack.size()) {
                    patternStack.push(current1.getNextSibling());
                }

                current1 = current1.getNextSibling();

                if (current2 != null) {
                    current2 = current2.getNextSibling();
                }
            }

            while (current2 != null && current2.getRole() != role) {
                current2 = current2.getNextSibling();
            }

            if (success && current2 == null) {
                return true;
            }
        }

        return false;
*/
    }
}
