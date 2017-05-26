/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import junit.framework.TestCase;

public final class FieldTest extends TestCase {
    private static final long MY_LONG = 5073258162644648461L;

    // Reflection for static long fields was broken http://b/1120750
    public void testLongFieldReflection() throws Exception {
        Field field = getClass().getDeclaredField("MY_LONG");
        assertEquals(5073258162644648461L, field.getLong(null));
    }

    public void testEqualConstructorEqualsAndHashCode() throws Exception {
        Field f1 = FieldTestHelper.class.getField("a");
        Field f2 = FieldTestHelper.class.getField("a");
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    public void testHashCodeSpec() throws Exception {
        Field f1 = FieldTestHelper.class.getField("a");
        assertEquals(FieldTestHelper.class.getName().hashCode() ^ "a".hashCode(), f1.hashCode());
    }

    public void testDifferentConstructorEqualsAndHashCode() throws Exception {
        Field f1 = FieldTestHelper.class.getField("a");
        Field f2 = FieldTestHelper.class.getField("b");
        assertFalse(f1.equals(f2));
    }

    // J2ObjC added.
    int myInt = 42;
    class InnerClass {
        int count() {
            return FieldTest.this.myInt;
        }
    }

    // Tests that the "synthetic" modifier is handled correctly.
    // It's supposed to be present but not shown in toString.
    public void testSyntheticModifier() throws NoSuchFieldException {
        Field valuesField = InnerClass.class.getDeclaredField("this$0");
        // Check that this test makes sense.
        assertTrue(valuesField.isSynthetic());
        assertEquals(Modifier.SYNTHETIC, valuesField.getModifiers() & Modifier.SYNTHETIC);
        assertEquals("private final libcore.java.lang.reflect.FieldTest " +
                "libcore.java.lang.reflect.FieldTest$InnerClass.this$0", valuesField.toString());
    }

    // Ensure that the "enum constant" bit is not returned in toString.
    public void testEnumValueField() throws NoSuchFieldException {
        Field blockedField = Thread.State.class.getDeclaredField("BLOCKED");
        assertTrue(Thread.State.class.getDeclaredField("BLOCKED").isEnumConstant());
        assertEquals("public static final", Modifier.toString(blockedField.getModifiers()));
        assertEquals(
                "public static final java.lang.Thread$State java.lang.Thread$State.BLOCKED",
                blockedField.toString());
    }

    class ClassWithATransientField {
        private transient Class<String> transientField = String.class;
    }

    // Tests that the "transient" modifier is handled correctly.
    // The underlying constant value for it is the same as for the "varargs" method modifier.
    // http://b/18488857
    public void testTransientModifier() throws NoSuchFieldException {
        Field transientField = ClassWithATransientField.class.getDeclaredField("transientField");
        // Check that this test makes sense.
        assertEquals(Modifier.TRANSIENT, transientField.getModifiers() & Modifier.TRANSIENT);
        assertEquals(
                "private transient java.lang.Class "
                        + "libcore.java.lang.reflect.FieldTest$ClassWithATransientField"
                        + ".transientField",
                transientField.toString());
    }

    public void testToGenericString() throws NoSuchFieldException {
        Field transientField = ClassWithATransientField.class.getDeclaredField("transientField");
        // Check that this test makes sense.
        assertEquals(Modifier.TRANSIENT, transientField.getModifiers() & Modifier.TRANSIENT);
        assertEquals(
                "private transient java.lang.Class<java.lang.String> "
                        + "libcore.java.lang.reflect.FieldTest$ClassWithATransientField"
                        + ".transientField",
                transientField.toGenericString());
    }

    static class FieldTestHelper {
        public String a;
        public Object b;
    }
}
