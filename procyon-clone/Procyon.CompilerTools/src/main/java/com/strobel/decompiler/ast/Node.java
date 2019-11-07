/*
 * Node.java
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

import com.strobel.core.Predicate;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.PlainTextOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Node {
    public final static Node NULL = new Node() {
        @Override
        public void writeTo(final ITextOutput output) {
            output.writeKeyword("null");
        }
    };

    public abstract void writeTo(final ITextOutput output);

    @Override
    public String toString() {
        final PlainTextOutput output = new PlainTextOutput();
        writeTo(output);
        return output.toString();
    }

    public final boolean isConditionalControlFlow() {
        return this instanceof Expression &&
               ((Expression) this).getCode().isConditionalControlFlow();
    }

    public final boolean isUnconditionalControlFlow() {
        return this instanceof Expression &&
               ((Expression) this).getCode().isUnconditionalControlFlow();
    }

    // <editor-fold defaultstate="collapsed" desc="Enumeration Methods">

    public List<Node> getChildren() {
        return Collections.emptyList();
    }

    public final List<Node> getSelfAndChildrenRecursive() {
        final ArrayList<Node> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, Node.class, null, false);
        return results;
    }

    public final List<Node> getSelfAndChildrenRecursive(final Predicate<Node> predicate) {
        final ArrayList<Node> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, Node.class, predicate, false);
        return results;
    }

    public final <T extends Node> List<T> getSelfAndChildrenRecursive(final Class<T> type) {
        final ArrayList<T> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, type, null, false);
        return results;
    }

    public final <T extends Node> List<T> getSelfAndChildrenRecursive(final Class<T> type, final Predicate<T> predicate) {
        final ArrayList<T> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, type, predicate, false);
        return results;
    }

    public final List<Node> getChildrenAndSelfRecursive() {
        final ArrayList<Node> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, Node.class, null, true);
        return results;
    }

    public final List<Node> getChildrenAndSelfRecursive(final Predicate<Node> predicate) {
        final ArrayList<Node> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, Node.class, predicate, true);
        return results;
    }

    public final <T extends Node> List<T> getChildrenAndSelfRecursive(final Class<T> type) {
        final ArrayList<T> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, type, null, true);
        return results;
    }

    public final <T extends Node> List<T> getChildrenAndSelfRecursive(final Class<T> type, final Predicate<T> predicate) {
        final ArrayList<T> results = new ArrayList<>();
        accumulateSelfAndChildrenRecursive(results, type, predicate, true);
        return results;
    }

    @SuppressWarnings("unchecked")
    private <T extends Node> void accumulateSelfAndChildrenRecursive(
        final List<T> list,
        final Class<T> type,
        final Predicate<T> predicate,
        final boolean childrenFirst) {

        if (!childrenFirst) {
            if (type.isInstance(this) && (predicate == null || predicate.test((T) this))) {
                list.add((T) this);
            }
        }

        for (final Node child : getChildren()) {
            child.accumulateSelfAndChildrenRecursive(list, type, predicate, childrenFirst);
        }

        if (childrenFirst) {
            if (type.isInstance(this) && (predicate == null || predicate.test((T) this))) {
                list.add((T) this);
            }
        }
    }

    // </editor-fold>
}
