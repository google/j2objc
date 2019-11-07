/*
 * AstOptimizationStep.java
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

package com.strobel.decompiler.ast;

public enum AstOptimizationStep {
    RemoveRedundantCode,
    ReduceBranchInstructionSet,
    InlineVariables,
    CopyPropagation,
    RewriteFinallyBlocks,
    SplitToMovableBlocks,
    RemoveUnreachableBlocks,
    TypeInference,
    RemoveInnerClassInitSecurityChecks,
    PreProcessShortCircuitAssignments,
    SimplifyShortCircuit,
    JoinBranchConditions,
    SimplifyTernaryOperator,
    JoinBasicBlocks,
    SimplifyLogicalNot,
    SimplifyShiftOperations,
    SimplifyLoadAndStore,
    TransformObjectInitializers,
    TransformArrayInitializers,
    InlineConditionalAssignments,
    MakeAssignmentExpressions,
    IntroducePostIncrement,
    InlineLambdas,
    InlineVariables2,
    MergeDisparateObjectInitializations,
    FindLoops,
    FindConditions,
    FlattenNestedMovableBlocks,
    RemoveRedundantCode2,
    GotoRemoval,
    DuplicateReturns,
    ReduceIfNesting,
    GotoRemoval2,
    ReduceComparisonInstructionSet,
    RecombineVariables,
    RemoveRedundantCode3,
    CleanUpTryBlocks,
    InlineVariables3,
    TypeInference2,
    None;

    public boolean isBlockLevelOptimization() {
        switch (this) {
            case RemoveInnerClassInitSecurityChecks:
            case SimplifyShortCircuit:
            case SimplifyTernaryOperator:
            case JoinBasicBlocks:
            case SimplifyLogicalNot:
            case SimplifyShiftOperations:
            case SimplifyLoadAndStore:
            case TransformObjectInitializers:
            case TransformArrayInitializers:
            case MakeAssignmentExpressions:
            case IntroducePostIncrement:
            case InlineLambdas:
            case InlineVariables2:
            case MergeDisparateObjectInitializations:
                return true;

            default:
                return false;
        }
    }
}
