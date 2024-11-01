/*
 * Copyright (C) 2023 The Android Open Source Project
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

package crossvmtest.java.lang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// import java.lang.invoke.MethodHandles;
// import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

public class RecordTest {

    record RecordInteger(int x) {}

    private static class NonRecordInteger {

        NonRecordInteger(int x) {
            this.x = x;
        }
        private final int x;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    public @interface CustomAnnotation {
        String value();
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
    public @interface CustomAnnotation2 {

        CustomAnnotation[] customAnnotations();
    }
    record RecordInteger2(@CustomAnnotation2(customAnnotations = {@CustomAnnotation("a")})
                          @CustomAnnotation("b") int x) {}

    record RecordString(String s) {
        public static final int Y = 1;
        public static final String A = "A";

    }

    public record SerializableRecord(int x, String s) implements Serializable {}

    @Test
    public void testHashCode() {
        RecordInteger a = new RecordInteger(9);
        RecordInteger b = new RecordInteger(9);
        RecordInteger c = new RecordInteger(0);

        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
    }

    @Test
    public void testEquals() {
        RecordInteger a = new RecordInteger(9);
        RecordInteger b = new RecordInteger(9);
        RecordInteger c = new RecordInteger(0);

        assertTrue(a.equals(b));
        assertEquals(a, b);
        assertFalse(a.equals(c));
        assertNotEquals(a, c);
    }

    @Test
    public void testToString() {
        RecordInteger a = new RecordInteger(9);
        RecordInteger b = new RecordInteger(9);
        RecordInteger c = new RecordInteger(0);

        assertEquals(a.toString(), b.toString());
        assertNotEquals(a.toString(), c.toString());
    }

    @Test
    public void testIsRecord() throws Exception {
        assertFalse(Object.class.isRecord());
        assertFalse(Record.class.isRecord());
        assertFalse(String.class.isRecord());
        assertFalse(NonRecordInteger.class.isRecord());

        RecordInteger a = new RecordInteger(9);
        assertTrue(a.getClass().isRecord());
        assertTrue(RecordInteger2.class.isRecord());
    }

    @Test
    public void testReflectedConstructor() throws ReflectiveOperationException {
        RecordInteger a = new RecordInteger(9);

        Constructor<?> c = RecordInteger.class.getDeclaredConstructors()[0];
        assertEquals(Arrays.deepToString(c.getParameters()), 1, c.getParameters().length);
        assertEquals(c.getParameters()[0].toString(), "x", c.getParameters()[0].getName());
        RecordInteger b = (RecordInteger) c.newInstance(9);
        assertEquals(a.x, b.x);
        assertEquals(a.x(), b.x());
        assertEquals(a, b);
    }

    @Test
    public void testReadField() throws ReflectiveOperationException {
        RecordInteger a = new RecordInteger(9);
        assertEquals(9, a.x);
        assertEquals(9, a.x());

        Field[] fields = RecordInteger.class.getDeclaredFields();
        assertEquals(Arrays.deepToString(fields), 1, fields.length);
        Field field = fields[0];
        field.setAccessible(true);
        assertEquals(field.toString(), "x", field.getName());
        assertEquals(9, field.get(a));
    }

    @Test
    public void testWriteField() throws ReflectiveOperationException {
        NonRecordInteger a = new NonRecordInteger(8);
        Field fieldA = NonRecordInteger.class.getDeclaredField("x");
        fieldA.setAccessible(true);
        fieldA.set(a, 7);
        assertEquals(7, a.x);

        RecordInteger b = new RecordInteger(8);

        Field fieldB = RecordInteger.class.getDeclaredField("x");
        assertThrows(IllegalAccessException.class, () -> fieldB.setInt(b, 7));
        assertThrows(IllegalAccessException.class, () -> fieldB.set(b, 7));
        fieldB.setAccessible(true);
        assertThrows(IllegalAccessException.class, () -> fieldB.setInt(b, 7));
        assertThrows(IllegalAccessException.class, () -> fieldB.set(b, 7));
        assertEquals(8, b.x);

        Field fieldC = RecordString.class.getDeclaredField("s");
        RecordString c = new RecordString("a");
        assertThrows(IllegalAccessException.class, () -> fieldC.set(c, "b"));
        fieldC.setAccessible(true);
        assertThrows(IllegalAccessException.class, () -> fieldC.set(c, "b"));
        assertEquals("a", c.s);
    }

    @Test
    public void testWriteStaticField() throws ReflectiveOperationException {
        Field field = RecordString.class.getField("A");
        assertThrows(IllegalAccessException.class, () -> field.set(null, "B"));
        field.setAccessible(true);
        assertThrows(IllegalAccessException.class, () -> field.set(null, "B"));
        assertEquals("A", field.get(null));

        Field fieldB = RecordString.class.getDeclaredField("Y");
        assertThrows(IllegalAccessException.class, () -> fieldB.setInt(null, 0));
        fieldB.setAccessible(true);
        assertThrows(IllegalAccessException.class, () -> fieldB.setInt(null, 0));
        assertEquals(1, fieldB.getInt(null));
    }

    /* J2ObjC: remove
    @Test
    public void testVarHandleWrite() throws ReflectiveOperationException {
        NonRecordInteger a = new NonRecordInteger(8);

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(NonRecordInteger.class,
                MethodHandles.lookup());
        VarHandle varHandle = lookup.findVarHandle(NonRecordInteger.class, "x", int.class);
        assertEquals(8, varHandle.get(a));
        assertThrows(UnsupportedOperationException.class, () -> varHandle.set(a, 6));
        assertEquals(8, a.x);

        RecordInteger b = new RecordInteger(8);

        lookup = MethodHandles.privateLookupIn(RecordInteger.class, MethodHandles.lookup());
        VarHandle varHandleB = lookup.findVarHandle(RecordInteger.class, "x", int.class);
        assertThrows(UnsupportedOperationException.class, () -> varHandleB.set(b, 7));
        assertEquals(8, b.x);
    }
    */

    @Test
    public void testSerializedNonSerializableRecordFailure()
            throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RecordInteger recordInteger = new RecordInteger(9);
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(recordInteger);
            fail("Expect NotSerializableException");
        } catch (NotSerializableException e) {
            // expected
        }
    }

    @Test
    public void testSerializedSimpleRecords() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializableRecord recordInteger = new SerializableRecord(9, "abc");
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(recordInteger);
        }
        byte[] bytes = baos.toByteArray();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            Object obj = ois.readObject();
            assertEquals(SerializableRecord.class, obj.getClass());
            SerializableRecord r = (SerializableRecord) obj;
            assertEquals(9, r.x());
            assertEquals("abc", r.s());
        }
    }
}
