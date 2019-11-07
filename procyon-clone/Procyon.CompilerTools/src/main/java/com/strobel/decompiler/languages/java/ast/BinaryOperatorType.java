/*
 * BinaryOperatorType.java
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

public enum BinaryOperatorType {
    ANY,
    BITWISE_AND,
    BITWISE_OR,
    EXCLUSIVE_OR,
    LOGICAL_AND,
    LOGICAL_OR,
    GREATER_THAN,
    GREATER_THAN_OR_EQUAL,
    LESS_THAN,
    LESS_THAN_OR_EQUAL,
    EQUALITY,
    INEQUALITY,
    ADD,
    SUBTRACT,
    MULTIPLY,
    DIVIDE,
    MODULUS,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    UNSIGNED_SHIFT_RIGHT;

    public final boolean isLogical() {
        switch (this) {
            case LOGICAL_AND:
            case LOGICAL_OR:
                return true;

            default:
                return false;
        }
    }

    public final boolean isCommutative() {
        switch (this) {
            case BITWISE_AND:
            case BITWISE_OR:
            case EXCLUSIVE_OR:
            case EQUALITY:
            case INEQUALITY:
            case ADD:
            case MULTIPLY:
                return true;

            default:
                return false;
        }
    }

    public final boolean isRelational() {
        switch (this) {
            case GREATER_THAN:
            case GREATER_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
            case EQUALITY:
            case INEQUALITY:
                return true;

            default:
                return false;
        }
    }

    public final boolean isArithmetic() {
        switch (this) {
            case ADD:
            case SUBTRACT:
            case MULTIPLY:
            case DIVIDE:
            case MODULUS:
                return true;

            default:
                return false;
        }
    }

    public final boolean isBitwise() {
        switch (this) {
            case BITWISE_AND:
            case BITWISE_OR:
            case EXCLUSIVE_OR:
            case SHIFT_LEFT:
            case SHIFT_RIGHT:
            case UNSIGNED_SHIFT_RIGHT:
                return true;

            default:
                return false;
        }
    }
}
