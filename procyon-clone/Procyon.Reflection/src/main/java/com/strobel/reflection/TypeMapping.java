/*
 * TypeMapping.java
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

package com.strobel.reflection;

import com.strobel.core.Mapping;

/**
 * @author Mike Strobel
 */
public abstract class TypeMapping extends Mapping<Type<?>> {
    protected TypeMapping() {}

    protected TypeMapping(final String name) {
        super(name);
    }
}
