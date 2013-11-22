/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.creation.jmock;

import java.lang.reflect.Modifier;

/**
 * Subset of Mockito's implementation that removes all Objenesis and CGLib
 * references (not useful on iOS).
 */
public class ClassImposterizer  {

    public static final ClassImposterizer INSTANCE = new ClassImposterizer();

    private ClassImposterizer() {}

    public boolean canImposterise(Class<?> type) {
        return !type.isPrimitive() && !Modifier.isFinal(type.getModifiers());
    }
}