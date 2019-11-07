/*
 * NameResolveResult.java
 *
 * Copyright (c) 2014 Mike Strobel
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

import java.util.List;

public abstract class NameResolveResult {
    public abstract List<Object> getCandidates();

    public abstract NameResolveMode getMode();

    public boolean hasMatch() {
        return !getCandidates().isEmpty();
    }

    public boolean isAmbiguous() {
        return getCandidates().size() > 1;
    }
}
