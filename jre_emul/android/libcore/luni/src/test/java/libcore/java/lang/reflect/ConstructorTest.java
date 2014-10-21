/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.lang.reflect.Constructor;
import junit.framework.TestCase;

public final class ConstructorTest extends TestCase {
    public void test_getExceptionTypes() throws Exception {
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(new Class[0]);
        Class[] exceptions = constructor.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
        // Check that corrupting our array doesn't affect other callers.
        exceptions[0] = NullPointerException.class;
        exceptions = constructor.getExceptionTypes();
        assertEquals(1, exceptions.length);
        assertEquals(IndexOutOfBoundsException.class, exceptions[0]);
    }

    public void test_getParameterTypes() throws Exception {
        Class[] expectedParameters = new Class[] { Object.class };
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor(expectedParameters);
        Class[] parameters = constructor.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
        // Check that corrupting our array doesn't affect other callers.
        parameters[0] = String.class;
        parameters = constructor.getParameterTypes();
        assertEquals(1, parameters.length);
        assertEquals(expectedParameters[0], parameters[0]);
    }

    public void testGetConstructorWithNullArgumentsArray() throws Exception {
        Constructor<?> constructor = ConstructorTestHelper.class.getConstructor((Class[]) null);
        assertEquals(0, constructor.getParameterTypes().length);
        assertNotNull(constructor.newInstance((Object[]) null));
    }

    public void testGetConstructorWithNullArgument() throws Exception {
        try {
            ConstructorTestHelper.class.getConstructor(new Class[] { null });
            fail();
        } catch (NoSuchMethodException expected) {
        }
    }

    // Not valid: all iOS constructors are public.
//    public void testGetConstructorReturnsDoesNotReturnPrivateConstructor() throws Exception {
//        try {
//            ConstructorTestHelper.class.getConstructor(Object.class, Object.class);
//            fail();
//        } catch (NoSuchMethodException expected) {
//        }
//    }

//    public void testGetDeclaredConstructorReturnsPrivateConstructor() throws Exception {
//        Constructor<?> constructor = ConstructorTestHelper.class.getDeclaredConstructor(
//                Object.class, Object.class);
//        assertEquals(2, constructor.getParameterTypes().length);
//    }

    static class ConstructorTestHelper {
        public ConstructorTestHelper() throws IndexOutOfBoundsException { }
        public ConstructorTestHelper(Object o) { }
        private ConstructorTestHelper(Object a, Object b) { }
    }
}
