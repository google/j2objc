/*
 * StackBehavior.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

/**
 * @author strobelm
 */
public enum StackBehavior {
    Pop0,
    Pop1,
    Pop1_Pop1,
    PopI,
    PopI_Pop1,
    PopI_PopI,
    PopI_PopI8,
    PopI_PopI_PopI,
    PopI_PopR4,
    PopI_PopR8,
    PopRef,
    PopRef_Pop1,
    PopRef_PopI,
    PopRef_PopI_PopI,
    PopRef_PopI_PopI8,
    PopRef_PopI_PopR4,
    PopRef_PopI_PopR8,
    PopRef_PopI_PopRef,
    Push0,
    Push1,
    Push1_Push1,
    PushI,
    PushI8,
    PushR4,
    PushR8,
    PushRef,
    VarPop,
    VarPush,
    PopRef_PopI_Pop1,
}

