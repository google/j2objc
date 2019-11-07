/*
 * AstKeys.java
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

import com.strobel.assembler.metadata.SwitchInfo;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.componentmodel.Key;
import com.strobel.util.ContractUtils;

import java.util.List;

public final class AstKeys {
    public final static Key<SwitchInfo> SWITCH_INFO = Key.create("SwitchInfo");
    public final static Key<Expression> PARENT_LAMBDA_BINDING = Key.create("ParentLambdaBinding");
    public final static Key<List<TypeReference>> TYPE_ARGUMENTS = Key.create("TypeArguments");

    private AstKeys() {
        throw ContractUtils.unreachable();
    }
}
