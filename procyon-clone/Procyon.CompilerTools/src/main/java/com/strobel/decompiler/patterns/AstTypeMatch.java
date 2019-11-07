/*
 * TypeReferenceDescriptorComparisonNode.java
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

import com.strobel.annotations.NotNull;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.languages.java.ast.AstType;
import com.strobel.decompiler.languages.java.ast.Keys;

public final class AstTypeMatch extends Pattern {
    private final TypeReference _type;

    public AstTypeMatch(final TypeReference type) {
        _type = VerifyArgument.notNull(type, "type");
    }

    @NotNull
    public final TypeReference getType() {
        return _type;
    }

    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AstType) {
            final TypeReference otherType = ((AstType) other).getUserData(Keys.TYPE_REFERENCE);

            return otherType != null &&
                   _type.isEquivalentTo(otherType);
        }
        return false;
    }
}
