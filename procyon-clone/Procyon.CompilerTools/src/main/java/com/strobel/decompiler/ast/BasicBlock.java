/*
 * BasicBlock.java
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

import com.strobel.assembler.Collection;
import com.strobel.decompiler.ITextOutput;

import java.util.ArrayList;
import java.util.List;

public final class BasicBlock extends Node {
    private final Collection<Node> _body;

    public BasicBlock() {
        _body = new Collection<>();
    }

    public final List<Node> getBody() {
        return _body;
    }

    @Override
    public final List<Node> getChildren() {
        final ArrayList<Node> childrenCopy = new ArrayList<>(_body.size());
        childrenCopy.addAll(_body);
        return childrenCopy;
    }

    @Override
    public final void writeTo(final ITextOutput output) {
        final List<Node> children = getChildren();

        boolean previousWasSimpleNode = true;

        for (int i = 0, childrenSize = children.size(); i < childrenSize; i++) {
            final Node child = children.get(i);
            final boolean isSimpleNode = child instanceof Expression || child instanceof Label;

            if (i != 0 && !isSimpleNode || !previousWasSimpleNode) {
                output.writeLine();
            }

            child.writeTo(output);

            if (isSimpleNode) {
                output.writeLine();
            }

            previousWasSimpleNode = isSimpleNode;
        }
    }
}
