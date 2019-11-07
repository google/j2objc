/*
 * DefiniteAssignmentStatus.java
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

public enum DefiniteAssignmentStatus
{
    /**
     * The variable is definitely not assigned.
     */
    DEFINITELY_NOT_ASSIGNED,

    /**
     * The variable might be assigned or unassigned.
     */
    POTENTIALLY_ASSIGNED,

    /**
     * The variable is definitely assigned.
     */
    DEFINITELY_ASSIGNED,

    /**
     * The variable is definitely assigned iff the expression results in the value {@code true}.
     */
    ASSIGNED_AFTER_TRUE_EXPRESSION,

    /**
     * The variable is definitely assigned iff the expression results in the value {@code false}.
     */
    ASSIGNED_AFTER_FALSE_EXPRESSION,

    /**
     * The code is unreachable.
     */
    CODE_UNREACHABLE
}
