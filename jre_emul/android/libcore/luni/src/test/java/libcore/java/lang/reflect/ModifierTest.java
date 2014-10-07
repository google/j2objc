/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.lang.reflect;

import java.lang.reflect.Modifier;

public class ModifierTest extends junit.framework.TestCase {
    public void test_classModifiers() {
        assertEquals(0xc1f, Modifier.classModifiers());
    }

    public void test_constructorModifiers() {
        assertEquals(0x007, Modifier.constructorModifiers());
    }

    public void test_fieldModifiers() {
        assertEquals(0x0df, Modifier.fieldModifiers());
    }

    public void test_interfaceModifiers() {
        assertEquals(0xc0f, Modifier.interfaceModifiers());
    }

    public void test_methodModifiers() {
        assertEquals(0xd3f, Modifier.methodModifiers());
    }

    public void test_isAbstractI() {
        assertTrue(Modifier.isAbstract(Modifier.ABSTRACT));
        assertTrue(!Modifier.isAbstract(-1 & ~Modifier.ABSTRACT));
    }

    public void test_isFinalI() {
        assertTrue(Modifier.isFinal(Modifier.FINAL));
        assertTrue(!Modifier.isFinal(-1 & ~Modifier.FINAL));
    }

    public void test_isInterfaceI() {
        assertTrue(Modifier.isInterface(Modifier.INTERFACE));
        assertTrue(!Modifier.isInterface(-1 & ~Modifier.INTERFACE));
    }

    public void test_isNativeI() {
        assertTrue(Modifier.isNative(Modifier.NATIVE));
        assertTrue(!Modifier.isNative(-1 & ~Modifier.NATIVE));
    }

    public void test_isPrivateI() {
        assertTrue(Modifier.isPrivate(Modifier.PRIVATE));
        assertTrue(!Modifier.isPrivate(-1 & ~Modifier.PRIVATE));
    }

    public void test_isProtectedI() {
        assertTrue(Modifier.isProtected(Modifier.PROTECTED));
        assertTrue(!Modifier.isProtected(-1 & ~Modifier.PROTECTED));
    }

    public void test_isPublicI() {
        assertTrue(Modifier.isPublic(Modifier.PUBLIC));
        assertTrue(!Modifier.isPublic(-1 & ~Modifier.PUBLIC));
    }

    public void test_isStaticI() {
        assertTrue(Modifier.isStatic(Modifier.STATIC));
        assertTrue(!Modifier.isStatic(-1 & ~Modifier.STATIC));
    }

    public void test_isStrictI() {
        assertTrue(Modifier.isStrict(Modifier.STRICT));
        assertTrue(!Modifier.isStrict(-1 & ~Modifier.STRICT));
    }

    public void test_isSynchronizedI() {
        assertTrue(Modifier.isSynchronized(Modifier.SYNCHRONIZED));
        assertTrue(!Modifier.isSynchronized(-1 & ~Modifier.SYNCHRONIZED));
    }

    public void test_isTransientI() {
        assertTrue(Modifier.isTransient(Modifier.TRANSIENT));
        assertTrue(!Modifier.isTransient(-1 & ~Modifier.TRANSIENT));
    }

    public void test_isVolatileI() {
        assertTrue(Modifier.isVolatile(Modifier.VOLATILE));
        assertTrue(!Modifier.isVolatile(-1 & ~Modifier.VOLATILE));
    }

    public void test_toStringI() {
        assertEquals("public abstract", Modifier.toString(Modifier.PUBLIC | Modifier.ABSTRACT));
    }
}
