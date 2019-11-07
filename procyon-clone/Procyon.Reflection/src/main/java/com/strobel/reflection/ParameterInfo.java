/*
 * ParameterInfo.java
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

/**
 * @author Mike Strobel
 */
public class ParameterInfo {
    private final String _name;
    private final int _position;
    private final Type<?> _parameterType;

    public ParameterInfo(final String name, final int position, final Type<?> parameterType) {
        _name = name;
        _position = position;
        _parameterType = parameterType;
    }

    public String getName() {
        return _name;
    }

    public Type<?> getParameterType() {
        return _parameterType;
    }

    public int getPosition() {
        return _position;
    }
}
