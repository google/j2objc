/*
 * MemberMapping.java
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

package com.strobel.decompiler.languages.java;

import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.decompiler.ast.Variable;

public final class MemberMapping {
    private com.strobel.assembler.metadata.MemberReference _memberReference;
    private Iterable<Variable> _localVariables;

    MemberMapping() {
    }

    public MemberMapping(final MethodDefinition method) {
        this.setMemberReference(method);
    }

    public MemberReference getMemberReference() {
        return _memberReference;
    }

    public void setMemberReference(final MemberReference memberReference) {
        _memberReference = memberReference;
    }

    public Iterable<Variable> getLocalVariables() {
        return _localVariables;
    }

    public void setLocalVariables(final Iterable<Variable> localVariables) {
        _localVariables = localVariables;
    }
}
