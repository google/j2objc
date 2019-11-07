/*
 * Error.java
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

import com.strobel.util.ContractUtils;

final class Error {
    private Error() {
        throw ContractUtils.unreachable();
    }

    public static RuntimeException expressionLinkedFromMultipleLocations(final Node node) {
        return new IllegalStateException("Expression is linked from several locations: " + node);
    }

    public static RuntimeException unsupportedNode(final Node node) {
        final String nodeType = node != null ? node.getClass().getName() : String.valueOf(node);
        return new IllegalStateException("Unsupported node type: " + nodeType);
    }
}
