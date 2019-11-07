/*
 * InliningTests.java
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

package com.strobel.decompiler;

import org.junit.Test;

public class InliningTests extends DecompilerTest {
    private final static class A {
        int x;

        public int test(final A a) {
            final A result = new A();
            return result.x - result.x;
        }
    }

    @Test
    public void testDeclarationNotInlinedIntoSelfReferencingAssignment() {

    }
}
