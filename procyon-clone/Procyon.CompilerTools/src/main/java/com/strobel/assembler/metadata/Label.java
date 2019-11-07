/*
 * Label.java
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

package com.strobel.assembler.metadata;

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 2:36 PM
 */
public final class Label {
    @SuppressWarnings("PackageVisibleField")
    int index;

    public Label(final int label) {
        this.index = label;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public int hashCode() {
        return this.index;
    }

    public boolean equals(final Object o) {
        return o instanceof Label &&
               equals((Label)o);
    }

    public boolean equals(final Label other) {
        return other != null &&
               other.index == this.index;
    }
}
