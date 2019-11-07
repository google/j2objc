/*
 * ExpressionType.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is based on the Dynamic Language Runtime from Microsoft,
 *   Copyright (c) Microsoft Corporation.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.expressions;

/**
 * Describes the node types for the nodes of an expression tree.
 * @author Mike Strobel
 */
public enum ExpressionType {
    Add,
    And,
    AndAlso,
    ArrayLength,
    ArrayIndex,
    Call,
    Coalesce,
    Conditional,
    Constant,
    Convert,
    ConvertChecked,
    Divide,
    Equal,
    ExclusiveOr,
    GreaterThan,
    GreaterThanOrEqual,
    Invoke,
    Lambda,
    LeftShift,
    LessThan,
    LessThanOrEqual,
    MemberAccess,
    Modulo,
    Multiply,
    Negate,
    UnaryPlus,
    New,
    NewArrayInit,
    NewArrayBounds,
    Not,
    NotEqual,
    Or,
    OrElse,
    Parameter,
    Quote,
    RightShift,
    UnsignedRightShift,
    Subtract,
    InstanceOf,
    Assign,
    Block,
    LineInfo,
    Decrement,
    DefaultValue,
    Extension,
    Goto,
    Increment,
    Label,
    RuntimeVariables,
    Loop,
    Switch,
    Throw,
    Try,
    Unbox,
    AddAssign,
    AndAssign,
    DivideAssign,
    ExclusiveOrAssign,
    LeftShiftAssign,
    ModuloAssign,
    MultiplyAssign,
    OrAssign,
    RightShiftAssign,
    UnsignedRightShiftAssign,
    SubtractAssign,
    PreIncrementAssign,
    PreDecrementAssign,
    PostIncrementAssign,
    PostDecrementAssign,
    TypeEqual,
    OnesComplement,
    IsTrue,
    IsFalse,
    ReferenceEqual,
    ReferenceNotEqual,
    IsNull,
    IsNotNull;

    public boolean isEqualityOperator() {
        switch (this) {
            case Equal:
            case NotEqual:
            case ReferenceEqual:
            case ReferenceNotEqual:
                return true;
            default:
                return false;
        }
    }
}
