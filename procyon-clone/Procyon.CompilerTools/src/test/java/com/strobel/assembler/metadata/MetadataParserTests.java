/*
 * MetadataParserTests.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.assembler.metadata;

import com.strobel.reflection.BindingFlags;
import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.Type;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class MetadataParserTests {
    @Test
    public void testGenericMethodResolution() throws Throwable {
        final MethodInfo reflectedMethod = Type.of(Collections.class).getMethods(BindingFlags.AllDeclared).get(1);
        final String signature = reflectedMethod.getSignature();
        final MetadataParser parser = new MetadataParser(MetadataSystem.instance());
        final TypeReference collectionsType = parser.lookupType("java.util", "Collections");
        final MethodReference method1 = parser.parseMethod(collectionsType, reflectedMethod.getName(), signature);

        assertNotNull(method1);

        final IMethodSignature methodSignature = parser.parseMethodSignature(signature);

        assertNotNull(methodSignature);

        final MethodReference method2 = parser.lookupMethod(collectionsType, reflectedMethod.getName(), methodSignature);

        assertNotNull(method2);
    }
}
