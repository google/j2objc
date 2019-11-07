/*
 * Label.java
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
public final class Label {
    @SuppressWarnings("PackageVisibleField")
    final int label;

    Label(final int label) {
        this.label = label;
    }

    int getLabelValue() {
        return this.label;
    }

    public int hashCode() {
        return this.label;
    }

    public boolean equals(final Object o) {
        return o instanceof Label &&
               equals((Label)o);
    }

    public boolean equals(final Label other) {
        return other != null &&
               other.label == this.label;
    }
}