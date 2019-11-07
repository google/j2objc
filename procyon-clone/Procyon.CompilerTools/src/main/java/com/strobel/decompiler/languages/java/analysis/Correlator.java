/*
 * Correlator.java
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

package com.strobel.decompiler.languages.java.analysis;

import com.strobel.assembler.metadata.IMetadataTypeMember;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.Expression;
import com.strobel.decompiler.languages.java.ast.IdentifierExpression;
import com.strobel.decompiler.languages.java.ast.Keys;
import com.strobel.decompiler.languages.java.ast.Statement;
import com.strobel.decompiler.utilities.TreeTraversal;
import com.strobel.functions.Function;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
public final class Correlator {
    public static boolean areCorrelated(final Expression readExpression, final Statement writeStatement) {
        final Set<IMetadataTypeMember> readMembers = new LinkedHashSet<>();
        final Set<IMetadataTypeMember> writeMembers = new LinkedHashSet<>();
        
        collectCorrelations(readExpression, CorrelationMode.Read, readMembers);

        if (readMembers.isEmpty()) {
            return false;
        }

        collectCorrelations(writeStatement, CorrelationMode.Write, writeMembers);

        if (writeMembers.isEmpty()) {
            return false;
        }

        for (final IMetadataTypeMember typeMember : readMembers) {
            if (writeMembers.contains(typeMember)) {
                return true;
            }
        }

        return false;
    }
    
    private static void collectCorrelations(
        final AstNode node,
        final CorrelationMode mode,
        final Collection<IMetadataTypeMember> members) {

        final Iterable<AstNode> traversal = TreeTraversal.postOrder(
            node,
            new Function<AstNode, Iterable<AstNode>>() {
                @Override
                public Iterable<AstNode> apply(final AstNode n) {
                    return n.getChildren();
                }
            }
        );

        for (final AstNode n : traversal) {
            if (!(n instanceof IdentifierExpression)) {
                continue;
            }

            final IdentifierExpression identifier = (IdentifierExpression) n;
            final UsageType usage = UsageClassifier.getUsageType(identifier);

            if (mode == CorrelationMode.Read) {
                if (usage != UsageType.Read && usage != UsageType.ReadWrite) {
                    continue;
                }
            }
            else if (usage != UsageType.Write && usage != UsageType.ReadWrite) {
                continue;
            }

            IMetadataTypeMember member = identifier.getUserData(Keys.MEMBER_REFERENCE);

            if (member != null) {
                members.add(member);
                continue;
            }

            final Variable variable = identifier.getUserData(Keys.VARIABLE);

            if (variable != null) {
                if (variable.isParameter()) {
                    member = variable.getOriginalParameter();
                }
                else if (variable.getOriginalVariable() != null) {
                    member = variable.getOriginalVariable();
                }

                if (member != null) {
                    members.add(member);
                }

                //noinspection UnnecessaryContinue
                continue;
            }
        }
    }

    private enum CorrelationMode {
        Read,
        Write,
    }
}
