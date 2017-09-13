/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.tests.java.lang.reflect;

import tests.support.Support_Field;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Set;

public class FieldTest extends junit.framework.TestCase {

    // BEGIN android-note
    // This test had a couple of bugs in it. Some parts of the code were
    // unreachable before. Also some tests expected the wrong excpetions
    // to be thrown. This version has been validated to pass on a standard
    // JDK 1.5.
    // END android-note

    public class TestClass {
        @AnnotationRuntime0
        @AnnotationRuntime1
        @AnnotationClass0
        @AnnotationSource0
        public int annotatedField;
        class Inner{}
    }


    @Retention(RetentionPolicy.RUNTIME)
    @Target( {ElementType.FIELD})
    static @interface AnnotationRuntime0 {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target( { ElementType.FIELD})
    static @interface AnnotationRuntime1 {
    }

    @Retention(RetentionPolicy.CLASS)
    @Target( { ElementType.FIELD})
    static @interface AnnotationClass0 {
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target( {ElementType.FIELD})
    static @interface AnnotationSource0 {
    }

    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target( {ElementType.FIELD})
    static @interface InheritedRuntime {
    }

    public class GenericField<S, T extends Number> {
        S field;
        T boundedField;
        int intField;
    }


    static class TestField {
        public static int pubfield1;

        private static int privfield1 = 123;

        protected int intField = Integer.MAX_VALUE;
        protected final int intFField = Integer.MAX_VALUE;
        protected static int intSField = Integer.MAX_VALUE;
        private final int intPFField = Integer.MAX_VALUE;

        protected short shortField = Short.MAX_VALUE;
        protected final short shortFField = Short.MAX_VALUE;
        protected static short shortSField = Short.MAX_VALUE;
        private final short shortPFField = Short.MAX_VALUE;

        protected boolean booleanField = true;
        protected static boolean booleanSField = true;
        protected final boolean booleanFField = true;
        private final boolean booleanPFField = true;

        protected byte byteField = Byte.MAX_VALUE;
        protected static byte byteSField = Byte.MAX_VALUE;
        protected final byte byteFField = Byte.MAX_VALUE;
        private final byte bytePFField = Byte.MAX_VALUE;

        protected long longField = Long.MAX_VALUE;
        protected final long longFField = Long.MAX_VALUE;
        protected static long longSField = Long.MAX_VALUE;
        private final long longPFField = Long.MAX_VALUE;

        protected double doubleField = Double.MAX_VALUE;
        protected static double doubleSField = Double.MAX_VALUE;
        protected static final double doubleSFField = Double.MAX_VALUE;
        protected final double doubleFField = Double.MAX_VALUE;
        private final double doublePFField = Double.MAX_VALUE;

        protected float floatField = Float.MAX_VALUE;
        protected final float floatFField = Float.MAX_VALUE;
        protected static float floatSField = Float.MAX_VALUE;
        private final float floatPFField = Float.MAX_VALUE;

        protected char charField = 'T';
        protected static char charSField = 'T';
        private final char charPFField = 'T';

        protected final char charFField = 'T';

        private static final int x = 1;

        public volatile transient int y = 0;

        protected static transient volatile int prsttrvol = 99;
    }

    public class TestFieldSub1 extends TestField {
    }

    public class TestFieldSub2 extends TestField {
    }

    static class A {
        protected short shortField = Short.MAX_VALUE;
    }

    static enum TestEnum {
        A, B, C;
        int field;
    }

    /**
     * java.lang.reflect.Field#equals(java.lang.Object)
     */
    public void test_equalsLjava_lang_Object() {
        // Test for method boolean
        // java.lang.reflect.Field.equals(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        try {
            f = TestField.class.getDeclaredField("shortField");
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
        try {
            assertTrue("Same Field returned false", f.equals(f));
            assertTrue("Inherited Field returned false", f.equals(TestField.class
                    .getDeclaredField("shortField")));
            assertTrue("Identical Field from different class returned true", !f
                    .equals(A.class.getDeclaredField("shortField")));
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#get(java.lang.Object)
     */
    public void test_getLjava_lang_Object() throws Throwable {
        // Test for method java.lang.Object
        // java.lang.reflect.Field.get(java.lang.Object)
        TestField x = new TestField();
        Field f = TestField.class.getDeclaredField("doubleField");
        Double val = (Double) f.get(x);

        assertTrue("Returned incorrect double field value",
                val.doubleValue() == Double.MAX_VALUE);
        // Test getting a static field;
        f = TestField.class.getDeclaredField("doubleSField");
        f.set(x, new Double(1.0));
        val = (Double) f.get(x);
        assertEquals("Returned incorrect double field value", 1.0, val.doubleValue());

        // Try a get on a private field in nested member
        // temporarily commented because it breaks J9 VM
        // Regression for HARMONY-1309
        //f = x.getClass().getDeclaredField("privfield1");
        //assertEquals(x.privfield1, f.get(x));

        // Try a get using an invalid class.
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.get(new String());
            fail("No expected IllegalArgumentException");
        } catch (IllegalArgumentException exc) {
            // Correct - Passed an Object that does not declare or inherit f
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("intField");
            f.get(null);
            fail("Expected NullPointerException not thrown");
        } catch (NullPointerException exc) {
            // Correct - Passed an Object that does not declare or inherit f
            thrown = true;
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static fields
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleSField");
            f.get(null);
            assertTrue("Exception thrown", true);
        } catch (Exception exc) {
            fail("No exception expected");
        }
    }

    class SupportSubClass extends Support_Field {

        Object getField(char primitiveType, Object o, Field f,
                Class expected) {
            Object res = null;
            try {
                primitiveType = Character.toUpperCase(primitiveType);
                switch (primitiveType) {
                case 'I': // int
                    res = new Integer(f.getInt(o));
                    break;
                case 'J': // long
                    res = new Long(f.getLong(o));
                    break;
                case 'Z': // boolean
                    res = new Boolean(f.getBoolean(o));
                    break;
                case 'S': // short
                    res = new Short(f.getShort(o));
                    break;
                case 'B': // byte
                    res = new Byte(f.getByte(o));
                    break;
                case 'C': // char
                    res = new Character(f.getChar(o));
                    break;
                case 'D': // double
                    res = new Double(f.getDouble(o));
                    break;
                case 'F': // float
                    res = new Float(f.getFloat(o));
                    break;
                default:
                    res = f.get(o);
                }
                // Since 2011, members are always accessible and throwing is optional
                assertTrue("expected " + expected + " for " + f.getName(),
                        expected == null || expected == IllegalAccessException.class);
            } catch (Exception e) {
                if (expected == null) {
                    fail("unexpected exception " + e);
                } else {
                    assertTrue("expected exception "
                            + expected.getName() + " and got " + e, e
                            .getClass().equals(expected));
                }
            }
            return res;
        }

        void setField(char primitiveType, Object o, Field f,
                Class expected, Object value) {
            try {
                primitiveType = Character.toUpperCase(primitiveType);
                switch (primitiveType) {
                case 'I': // int
                    f.setInt(o, ((Integer) value).intValue());
                    break;
                case 'J': // long
                    f.setLong(o, ((Long) value).longValue());
                    break;
                case 'Z': // boolean
                    f.setBoolean(o, ((Boolean) value).booleanValue());
                    break;
                case 'S': // short
                    f.setShort(o, ((Short) value).shortValue());
                    break;
                case 'B': // byte
                    f.setByte(o, ((Byte) value).byteValue());
                    break;
                case 'C': // char
                    f.setChar(o, ((Character) value).charValue());
                    break;
                case 'D': // double
                    f.setDouble(o, ((Double) value).doubleValue());
                    break;
                case 'F': // float
                    f.setFloat(o, ((Float) value).floatValue());
                    break;
                default:
                    f.set(o, value);
                }
                // Since 2011, members are always accessible and throwing is optional
                assertTrue("expected " + expected + " for " + f.getName() + " = " + value,
                        expected == null || expected == IllegalAccessException.class);
            } catch (Exception e) {
                if (expected == null) {
                    e.printStackTrace();
                    fail("unexpected exception " + e + " for field "
                            + f.getName() + ", value " + value);
                } else {
                    assertTrue("expected exception "
                            + expected.getName() + " and got " + e
                            + " for field " + f.getName() + ", value " + value,
                            e.getClass().equals(expected));
                }
            }
        }
    }

    /**
     * java.lang.reflect.Field#get(java.lang.Object)
     * java.lang.reflect.Field#getByte(java.lang.Object)
     * java.lang.reflect.Field#getBoolean(java.lang.Object)
     * java.lang.reflect.Field#getShort(java.lang.Object)
     * java.lang.reflect.Field#getInt(java.lang.Object)
     * java.lang.reflect.Field#getLong(java.lang.Object)
     * java.lang.reflect.Field#getFloat(java.lang.Object)
     * java.lang.reflect.Field#getDouble(java.lang.Object)
     * java.lang.reflect.Field#getChar(java.lang.Object)
     * java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
     * java.lang.reflect.Field#setByte(java.lang.Object, byte)
     * java.lang.reflect.Field#setBoolean(java.lang.Object, boolean)
     * java.lang.reflect.Field#setShort(java.lang.Object, short)
     * java.lang.reflect.Field#setInt(java.lang.Object, int)
     * java.lang.reflect.Field#setLong(java.lang.Object, long)
     * java.lang.reflect.Field#setFloat(java.lang.Object, float)
     * java.lang.reflect.Field#setDouble(java.lang.Object, double)
     * java.lang.reflect.Field#setChar(java.lang.Object, char)
     */
    public void testProtectedFieldAccess() {
        Class fieldClass = Support_Field.class;
        String fieldName = null;
        Field objectField = null;
        Field booleanField = null;
        Field byteField = null;
        Field charField = null;
        Field shortField = null;
        Field intField = null;
        Field longField = null;
        Field floatField = null;
        Field doubleField = null;
        try {
            fieldName = "objectField";
            objectField = fieldClass.getDeclaredField(fieldName);

            fieldName = "booleanField";
            booleanField = fieldClass.getDeclaredField(fieldName);

            fieldName = "byteField";
            byteField = fieldClass.getDeclaredField(fieldName);

            fieldName = "charField";
            charField = fieldClass.getDeclaredField(fieldName);

            fieldName = "shortField";
            shortField = fieldClass.getDeclaredField(fieldName);

            fieldName = "intField";
            intField = fieldClass.getDeclaredField(fieldName);

            fieldName = "longField";
            longField = fieldClass.getDeclaredField(fieldName);

            fieldName = "floatField";
            floatField = fieldClass.getDeclaredField(fieldName);

            fieldName = "doubleField";
            doubleField = fieldClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            fail("missing field " + fieldName + " in test support class "
                    + fieldClass.getName());
        }

        // create the various objects that might or might not have an instance
        // of the field
        Support_Field parentClass = new Support_Field();
        SupportSubClass subclass = new SupportSubClass();
        SupportSubClass otherSubclass = new SupportSubClass();
        Object plainObject = new Object();

        Class illegalAccessExceptionClass = IllegalAccessException.class;
        Class illegalArgumentExceptionClass = IllegalArgumentException.class;

        // The test will attempt to use pass an object to set for object, byte,
        // short, ..., float and double fields
        // and pass a byte to to setByte for byte, short, ..., float and double
        // fields and so on.
        // It will also test if IllegalArgumentException is thrown when the
        // field does not exist in the given object and that
        // IllegalAccessException is thrown when trying to access an
        // inaccessible protected field.
        // The test will also check that IllegalArgumentException is thrown for
        // all other attempts.

        // Ordered by widening conversion, except for 'L' at the beg (which
        // stands for Object).
        // If the object provided to set can be unwrapped to a primitive, then
        // the set method can set
        // primitive fields.
        char types[] = { 'L', 'B', 'S', 'C', 'I', 'J', 'F', 'D' };
        Field fields[] = { objectField, byteField, shortField, charField,
                intField, longField, floatField, doubleField };
        Object values[] = { new Byte((byte) 1), new Byte((byte) 1),
                new Short((short) 1), new Character((char) 1), new Integer(1),
                new Long(1), new Float(1), new Double(1) };

        // test set methods
        for (int i = 0; i < types.length; i++) {
            char type = types[i];
            Object value = values[i];
            for (int j = i; j < fields.length; j++) {
                Field field = fields[j];
                fieldName = field.getName();

                if (field == charField && type != 'C') {
                    // the exception is that bytes and shorts CANNOT be
                    // converted into chars even though chars CAN be
                    // converted into ints, longs, floats and doubles
                    subclass.setField(type, subclass, field,
                            illegalArgumentExceptionClass, value);
                } else {
                    // setting type into field);
                    subclass.setField(type, subclass, field, null, value);
                    subclass.setField(type, otherSubclass, field, null, value);
                    subclass.setField(type, parentClass, field,
                            illegalAccessExceptionClass, value);
// Fails on JRE 1.7.0_55.
//                    subclass.setField(type, plainObject, field,
//                            illegalArgumentExceptionClass, value);
                }
            }
            for (int j = 0; j < i; j++) {
                Field field = fields[j];
                fieldName = field.getName();
                // not setting type into field);
                subclass.setField(type, subclass, field,
                        illegalArgumentExceptionClass, value);
            }
        }

        // test setBoolean
        Boolean booleanValue = Boolean.TRUE;
        subclass.setField('Z', subclass, booleanField, null, booleanValue);
        subclass.setField('Z', otherSubclass, booleanField, null, booleanValue);
        subclass.setField('Z', parentClass, booleanField,
                illegalAccessExceptionClass, booleanValue);
// Fails on JRE 1.7.0_55.
//        subclass.setField('Z', plainObject, booleanField,
//                illegalArgumentExceptionClass, booleanValue);
        for (int j = 0; j < fields.length; j++) {
            Field listedField = fields[j];
            fieldName = listedField.getName();
            // not setting boolean into listedField
            subclass.setField('Z', subclass, listedField,
                    illegalArgumentExceptionClass, booleanValue);
        }
        for (int i = 0; i < types.length; i++) {
            char type = types[i];
            Object value = values[i];
            subclass.setField(type, subclass, booleanField,
                    illegalArgumentExceptionClass, value);
        }

        // We perform the analogous test on the get methods.

        // ordered by widening conversion, except for 'L' at the end (which
        // stands for Object), to which all primitives can be converted by
        // wrapping
        char newTypes[] = new char[] { 'B', 'S', 'C', 'I', 'J', 'F', 'D', 'L' };
        Field newFields[] = { byteField, shortField, charField, intField,
                longField, floatField, doubleField, objectField };
        fields = newFields;
        types = newTypes;
        // test get methods
        for (int i = 0; i < types.length; i++) {
            char type = types[i];
            for (int j = 0; j <= i; j++) {
                Field field = fields[j];
                fieldName = field.getName();
                if (type == 'C' && field != charField) {
                    // the exception is that bytes and shorts CANNOT be
                    // converted into chars even though chars CAN be
                    // converted into ints, longs, floats and doubles
                    subclass.getField(type, subclass, field,
                            illegalArgumentExceptionClass);
                } else {
                    // getting type from field
                    subclass.getField(type, subclass, field, null);
                    subclass.getField(type, otherSubclass, field, null);
                    subclass.getField(type, parentClass, field,
                            illegalAccessExceptionClass);
// Fails on JRE 1.7.0_55.
//                    subclass.getField(type, plainObject, field,
//                            illegalArgumentExceptionClass);
                }
            }
            for (int j = i + 1; j < fields.length; j++) {
                Field field = fields[j];
                fieldName = field.getName();
                subclass.getField(type, subclass, field,
                        illegalArgumentExceptionClass);
            }
        }

        // test getBoolean
        subclass.getField('Z', subclass, booleanField, null);
        subclass.getField('Z', otherSubclass, booleanField, null);
        subclass.getField('Z', parentClass, booleanField,
                illegalAccessExceptionClass);
// Fails on JRE 1.7.0_55.
//        subclass.getField('Z', plainObject, booleanField,
//                illegalArgumentExceptionClass);
        for (int j = 0; j < fields.length; j++) {
            Field listedField = fields[j];
            fieldName = listedField.getName();
            // not getting boolean from listedField
            subclass.getField('Z', subclass, listedField,
                    illegalArgumentExceptionClass);
        }
        for (int i = 0; i < types.length - 1; i++) {
            char type = types[i];
            subclass.getField(type, subclass, booleanField,
                    illegalArgumentExceptionClass);
        }
        Object res = subclass.getField('L', subclass, booleanField, null);
        assertTrue("unexpected object " + res, res instanceof Boolean);
    }

    /**
     * java.lang.reflect.Field#getBoolean(java.lang.Object)
     */
    public void test_getBooleanLjava_lang_Object() {
        TestField x = new TestField();
        Field f = null;
        boolean val = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            val = f.getBoolean(x);
        } catch (Exception e) {
            fail("Exception during getBoolean test: " + e.toString());
        }
        assertTrue("Returned incorrect boolean field value", val);

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.getBoolean(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown");
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.getBoolean(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanSField");
            boolean staticValue = f.getBoolean(null);
            assertTrue("Wrong value returned", staticValue);
        }  catch (Exception ex) {
            fail("No exception expected");
        }
    }


    /**
     * java.lang.reflect.Field#getByte(java.lang.Object)
     */
    public void test_getByteLjava_lang_Object() {
        // Test for method byte
        // java.lang.reflect.Field.getByte(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        byte val = 0;
        try {
            f = TestField.class.getDeclaredField("byteField");
            val = f.getByte(x);
        } catch (Exception e) {
            fail("Exception during getbyte test : " + e.getMessage());
        }
        assertTrue("Returned incorrect byte field value", val == Byte.MAX_VALUE);

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.getByte(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown");
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("byteField");
            f.getByte(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("byteSField");
            byte staticValue = f.getByte(null);
            assertEquals("Wrong value returned", Byte.MAX_VALUE, staticValue);
        }  catch (Exception ex) {
            fail("No exception expected "+ ex.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#getChar(java.lang.Object)
     */
    public void test_getCharLjava_lang_Object() {
        // Test for method char
        // java.lang.reflect.Field.getChar(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        char val = 0;
        try {
            f = TestField.class.getDeclaredField("charField");
            val = f.getChar(x);
        } catch (Exception e) {
            fail("Exception during getCharacter test: " + e.toString());
        }
        assertEquals("Returned incorrect char field value", 'T', val);

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.getChar(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown");
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("charField");
            f.getChar(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("charSField");
            char staticValue = f.getChar(null);
            assertEquals("Wrong value returned", 'T', staticValue);
        }  catch (Exception ex) {
            fail("No exception expected "+ ex.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#getDeclaringClass()
     */
    public void test_getDeclaringClass() {
        // Test for method java.lang.Class
        // java.lang.reflect.Field.getDeclaringClass()
        Field[] fields;

        try {
            fields = TestField.class.getFields();
            assertTrue("Returned incorrect declaring class", fields[0]
                    .getDeclaringClass().equals(TestField.class));

            // Check the case where the field is inherited to be sure the parent
            // is returned as the declarer
            fields = TestFieldSub1.class.getFields();
            assertTrue("Returned incorrect declaring class", fields[0]
                    .getDeclaringClass().equals(TestField.class));
        } catch (Exception e) {
            fail("Exception : " + e.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#getDouble(java.lang.Object)
     */
    public void test_getDoubleLjava_lang_Object() {
        // Test for method double
        // java.lang.reflect.Field.getDouble(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        double val = 0.0;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            val = f.getDouble(x);
        } catch (Exception e) {
            fail("Exception during getDouble test: " + e.toString());
        }
        assertTrue("Returned incorrect double field value",
                val == Double.MAX_VALUE);

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.getDouble(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown "
                    + ex.getMessage());
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.getDouble(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleSFField");
            double staticValue = f.getDouble(null);
            assertEquals("Wrong value returned", Double.MAX_VALUE, staticValue);
        }  catch (Exception ex) {
            fail("No exception expected "+ ex.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#getFloat(java.lang.Object)
     */
    public void test_getFloatLjava_lang_Object() {
        // Test for method float
        // java.lang.reflect.Field.getFloat(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        float val = 0;
        try {
            f = TestField.class.getDeclaredField("floatField");
            val = f.getFloat(x);
        } catch (Exception e) {
            fail("Exception during getFloat test : " + e.getMessage());
        }
        assertTrue("Returned incorrect float field value",
                val == Float.MAX_VALUE);

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.getFloat(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown "
                    + ex.getMessage());
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("floatField");
            f.getFloat(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("floatSField");
            float staticValue = f.getFloat(null);
            assertEquals("Wrong value returned", Float.MAX_VALUE, staticValue);
        }  catch (Exception ex) {
            fail("No exception expected "+ ex.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#getInt(java.lang.Object)
     */
    public void test_getIntLjava_lang_Object() {
        // Test for method int java.lang.reflect.Field.getInt(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        int val = 0;
        try {
            f = TestField.class.getDeclaredField("intField");
            val = f.getInt(x);
        } catch (Exception e) {
            fail("Exception during getInt test : " + e.getMessage());
        }
        assertTrue("Returned incorrect Int field value",
                val == Integer.MAX_VALUE);

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.getInt(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown "
                    + ex.getMessage());
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("intField");
            f.getInt(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("intSField");
            int staticValue = f.getInt(null);
            assertEquals("Wrong value returned", Integer.MAX_VALUE, staticValue);
        } catch (Exception ex) {
            fail("No exception expected " + ex.getMessage());
        }

    }

    /**
     * java.lang.reflect.Field#getLong(java.lang.Object)
     */
    public void test_getLongLjava_lang_Object() {
        // Test for method long
        // java.lang.reflect.Field.getLong(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        long val = 0;
        try {
            f = TestField.class.getDeclaredField("longField");
            val = f.getLong(x);
        } catch (Exception e) {
            fail("Exception during getLong test : " + e.getMessage());
        }

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.getLong(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown "
                    + ex.getMessage());
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("longField");
            f.getLong(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("longSField");
            long staticValue = f.getLong(null);
            assertEquals("Wrong value returned", Long.MAX_VALUE, staticValue);
        }  catch (Exception ex) {
            fail("No exception expected "+ ex.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#getModifiers()
     */
    public void test_getModifiers() {
        // Test for method int java.lang.reflect.Field.getModifiers()
        TestField x = new TestField();
        Field f = null;
        try {
            f = TestField.class.getDeclaredField("prsttrvol");
        } catch (Exception e) {
            fail("Exception during getModifiers test: " + e.toString());
        }
        int mod = f.getModifiers();
        int mask = (Modifier.PROTECTED | Modifier.STATIC)
                | (Modifier.TRANSIENT | Modifier.VOLATILE);
        int nmask = (Modifier.PUBLIC | Modifier.NATIVE);
        assertTrue("Returned incorrect field modifiers: ",
                ((mod & mask) == mask) && ((mod & nmask) == 0));
    }

    /**
     * java.lang.reflect.Field#getName()
     */
    public void test_getName() {
        // Test for method java.lang.String java.lang.reflect.Field.getName()
        TestField x = new TestField();
        Field f = null;
        try {
            f = TestField.class.getDeclaredField("shortField");
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
        assertEquals("Returned incorrect field name",
                "shortField", f.getName());
    }

    /**
     * java.lang.reflect.Field#getShort(java.lang.Object)
     */
    public void test_getShortLjava_lang_Object() {
        // Test for method short
        // java.lang.reflect.Field.getShort(java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        short val = 0;
        ;
        try {
            f = TestField.class.getDeclaredField("shortField");
            val = f.getShort(x);
        } catch (Exception e) {
            fail("Exception during getShort test : " + e.getMessage());
        }
        assertTrue("Returned incorrect short field value",
                val == Short.MAX_VALUE);

        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.getShort(x);
            fail("IllegalArgumentException expected but not thrown");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("IllegalArgumentException expected but not thrown "
                    + ex.getMessage());
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("shortField");
            f.getShort(null);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        //Test no NPE on static field
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("shortSField");
            short staticValue = f.getShort(null);
            assertEquals("Wrong value returned", Short.MAX_VALUE, staticValue);
        }  catch (Exception ex) {
            fail("No exception expected "+ ex.getMessage());
        }
    }

    /**
     * java.lang.reflect.Field#getType()
     */
    public void test_getType() {
        // Test for method java.lang.Class java.lang.reflect.Field.getType()
        TestField x = new TestField();
        Field f = null;
        try {
            f = TestField.class.getDeclaredField("shortField");
        } catch (Exception e) {
            fail("Exception during getType test : " + e.getMessage());
        }
        assertTrue("Returned incorrect field type: " + f.getType().toString(),
                f.getType().equals(short.class));
    }

    /**
     * java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
     */
    public void test_setLjava_lang_ObjectLjava_lang_Object() throws Exception{
        // Test for method void java.lang.reflect.Field.set(java.lang.Object,
        // java.lang.Object)
        TestField x = new TestField();
        Field f = null;
        double val = 0.0;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.set(x, new Double(1.0));
            val = f.getDouble(x);
        } catch (Exception e) {
            fail("Exception during set test : " + e.getMessage());
        }
        assertEquals("Returned incorrect double field value", 1.0, val);

        //test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.set(x, new Double(1.0));
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

      //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.set(null, true);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("doubleSField");
        f.set(null, new Double(1.0));
        val = f.getDouble(x);
        assertEquals("Returned incorrect double field value", 1.0, val);
    }

    /**
     * java.lang.reflect.Field#setBoolean(java.lang.Object, boolean)
     */
    public void test_setBooleanLjava_lang_ObjectZ() throws Exception{
        // Test for method void
        // java.lang.reflect.Field.setBoolean(java.lang.Object, boolean)
        TestField x = new TestField();
        Field f = null;
        boolean val = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setBoolean(x, false);
            val = f.getBoolean(x);
        } catch (Exception e) {
            fail("Exception during setboolean test: " + e.toString());
        }
        assertTrue("Returned incorrect float field value", !val);

      //test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.setBoolean(x, false);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

      //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setBoolean(null, true);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("booleanSField");
        f.setBoolean(null, false);
        val = f.getBoolean(x);
        assertFalse("Returned incorrect boolean field value", val);
    }

    /**
     * java.lang.reflect.Field#setByte(java.lang.Object, byte)
     */
    public void test_setByteLjava_lang_ObjectB() throws Exception{
        // Test for method void
        // java.lang.reflect.Field.setByte(java.lang.Object, byte)
        TestField x = new TestField();
        Field f = null;
        byte val = 0;
        try {
            f = TestField.class.getDeclaredField("byteField");
            f.setByte(x, (byte) 1);
            val = f.getByte(x);
        } catch (Exception e) {
            fail("Exception during setByte test : " + e.getMessage());
        }
        assertEquals("Returned incorrect float field value", 1, val);

        //test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setByte(x, Byte.MIN_VALUE);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

      //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("byteField");
            f.setByte(null, Byte.MIN_VALUE);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("byteSField");
        f.setByte(null, Byte.MIN_VALUE);
        val = f.getByte(x);
        assertEquals("Returned incorrect byte field value", Byte.MIN_VALUE,
                val);
    }

    /**
     * java.lang.reflect.Field#setChar(java.lang.Object, char)
     */
    public void test_setCharLjava_lang_ObjectC() throws Exception{
        // Test for method void
        // java.lang.reflect.Field.setChar(java.lang.Object, char)
        TestField x = new TestField();
        Field f = null;
        char val = 0;
        try {
            f = TestField.class.getDeclaredField("charField");
            f.setChar(x, (char) 1);
            val = f.getChar(x);
        } catch (Exception e) {
            fail("Exception during setChar test : " + e.getMessage());
        }
        assertEquals("Returned incorrect float field value", 1, val);

      //test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setChar(x, Character.MIN_VALUE);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

      //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("charField");
            f.setChar(null, Character.MIN_VALUE);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("charSField");
        f.setChar(null, Character.MIN_VALUE);
        val = f.getChar(x);
        assertEquals("Returned incorrect char field value",
                Character.MIN_VALUE, val);
    }

    /**
     * java.lang.reflect.Field#setDouble(java.lang.Object, double)
     */
    public void test_setDoubleLjava_lang_ObjectD() throws Exception{
        // Test for method void
        // java.lang.reflect.Field.setDouble(java.lang.Object, double)
        TestField x = new TestField();
        Field f = null;
        double val = 0.0;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.setDouble(x, Double.MIN_VALUE);
            val = f.getDouble(x);
        } catch (Exception e) {
            fail("Exception during setDouble test: " + e.toString());
        }
        assertEquals("Returned incorrect double field value", Double.MIN_VALUE,
                val);

      //test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setDouble(x, Double.MIN_VALUE);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

      //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("doubleField");
            f.setDouble(null, Double.MIN_VALUE);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("doubleSField");
        f.setDouble(null, Double.MIN_VALUE);
        val = f.getDouble(x);
        assertEquals("Returned incorrect double field value",
                Double.MIN_VALUE, val);
    }

    /**
     * java.lang.reflect.Field#setFloat(java.lang.Object, float)
     */
    public void test_setFloatLjava_lang_ObjectF() throws Exception{
        if (System.getProperty("os.arch").equals("armv7")) {
          return;
        }
        // Test for method void
        // java.lang.reflect.Field.setFloat(java.lang.Object, float)
        TestField x = new TestField();
        Field f = null;
        float val = 0.0F;
        try {
            f = TestField.class.getDeclaredField("floatField");
            f.setFloat(x, Float.MIN_VALUE);
            val = f.getFloat(x);
        } catch (Exception e) {
            fail("Exception during setFloat test : " + e.getMessage());
        }
        assertEquals("Returned incorrect float field value", Float.MIN_VALUE,
                val, 0.0);

        //test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setFloat(x, Float.MIN_VALUE);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

      //Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("floatField");
            f.setFloat(null, Float.MIN_VALUE);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("floatSField");
        f.setFloat(null, Float.MIN_VALUE);
        val = f.getFloat(x);
        assertEquals("Returned incorrect float field value",
                Float.MIN_VALUE, val);
    }

    /**
     * java.lang.reflect.Field#setInt(java.lang.Object, int)
     */
    public void test_setIntLjava_lang_ObjectI() throws Exception{
        // Test for method void java.lang.reflect.Field.setInt(java.lang.Object,
        // int)
        TestField x = new TestField();
        Field f = null;
        int val = 0;
        try {
            f = TestField.class.getDeclaredField("intField");
            f.setInt(x, Integer.MIN_VALUE);
            val = f.getInt(x);
        } catch (Exception e) {
            fail("Exception during setInteger test: " + e.toString());
        }
        assertEquals("Returned incorrect int field value", Integer.MIN_VALUE,
                val);

        // test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setInt(x, Integer.MIN_VALUE);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        // Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("intField");
            f.setInt(null, Integer.MIN_VALUE);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("intSField");
        f.setInt(null, Integer.MIN_VALUE);
        val = f.getInt(x);
        assertEquals("Returned incorrect int field value",
                Integer.MIN_VALUE, val);
    }

    /**
     * java.lang.reflect.Field#setLong(java.lang.Object, long)
     */
    public void test_setLongLjava_lang_ObjectJ() throws Exception{
        // Test for method void
        // java.lang.reflect.Field.setLong(java.lang.Object, long)
        TestField x = new TestField();
        Field f = null;
        long val = 0L;
        try {
            f = TestField.class.getDeclaredField("longField");
            f.setLong(x, Long.MIN_VALUE);
            val = f.getLong(x);
        } catch (Exception e) {
            fail("Exception during setLong test : " + e.getMessage());
        }
        assertEquals("Returned incorrect long field value", Long.MIN_VALUE, val);

        // test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setLong(x, Long.MIN_VALUE);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        // Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("longField");
            f.setLong(null, Long.MIN_VALUE);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("longSField");
        f.setLong(null, Long.MIN_VALUE);
        val = f.getLong(x);
        assertEquals("Returned incorrect long field value",
                Long.MIN_VALUE, val);
    }

    /**
     * java.lang.reflect.Field#setShort(java.lang.Object, short)
     */
    public void test_setShortLjava_lang_ObjectS() throws Exception{
        // Test for method void
        // java.lang.reflect.Field.setShort(java.lang.Object, short)
        TestField x = new TestField();
        Field f = null;
        short val = 0;
        try {
            f = TestField.class.getDeclaredField("shortField");
            f.setShort(x, Short.MIN_VALUE);
            val = f.getShort(x);
        } catch (Exception e) {
            fail("Exception during setShort test : " + e.getMessage());
        }
        assertEquals("Returned incorrect short field value", Short.MIN_VALUE,
                val);

        // test wrong type
        boolean thrown = false;
        try {
            f = TestField.class.getDeclaredField("booleanField");
            f.setShort(x, Short.MIN_VALUE);
            fail("Accessed field of invalid type");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        assertTrue("IllegalArgumentException expected but not thrown", thrown);

        // Test NPE
        thrown = false;
        try {
            f = TestField.class.getDeclaredField("shortField");
            f.setShort(null, Short.MIN_VALUE);
            fail("NullPointerException expected but not thrown");
        } catch (NullPointerException ex) {
            thrown = true;
        } catch (Exception ex) {
            fail("NullPointerException expected but not thrown");
        }
        assertTrue("NullPointerException expected but not thrown", thrown);

        // Test setting a static field;
        f = TestField.class.getDeclaredField("shortSField");
        f.setShort(null, Short.MIN_VALUE);
        val = f.getShort(x);
        assertEquals("Returned incorrect short field value",
                Short.MIN_VALUE, val);
    }

    /**
     * java.lang.reflect.Field#toString()
     */
    public void test_toString() {
        // Test for method java.lang.String java.lang.reflect.Field.toString()
        Field f = null;

        try {
            f = TestField.class.getDeclaredField("x");
        } catch (Exception e) {
            fail("Exception getting field : " + e.getMessage());
        }
        assertEquals("Field returned incorrect string",
                "private static final int org.apache.harmony.tests.java.lang.reflect.FieldTest$TestField.x",
                        f.toString());
    }

    public void test_getDeclaredAnnotations() throws Exception {
        Field field = TestClass.class.getField("annotatedField");
        Annotation[] annotations = field.getDeclaredAnnotations();
        assertEquals(2, annotations.length);

        Set<Class<?>> ignoreOrder = new HashSet<Class<?>>();
        ignoreOrder.add(annotations[0].annotationType());
        ignoreOrder.add(annotations[1].annotationType());

        assertTrue("Missing @AnnotationRuntime0", ignoreOrder
                .contains(AnnotationRuntime0.class));
        assertTrue("Missing @AnnotationRuntime1", ignoreOrder
                .contains(AnnotationRuntime1.class));
    }

    public void test_isEnumConstant() throws Exception {
        Field field = TestEnum.class.getDeclaredField("A");
        assertTrue("Enum constant not recognized", field.isEnumConstant());

        field = TestEnum.class.getDeclaredField("field");
        assertFalse("Non enum constant wrongly stated as enum constant", field
                .isEnumConstant());

        field = TestClass.class.getDeclaredField("annotatedField");
        assertFalse("Non enum constant wrongly stated as enum constant", field
                .isEnumConstant());
    }

    // Disabled, as j2objc creates different synthetic fields than Java compilers.
//    public void test_isSynthetic() throws Exception {
//        Field[] fields = TestClass.Inner.class.getDeclaredFields();
//        assertEquals("Not exactly one field returned", 1, fields.length);
//
//        assertTrue("Enum constant not recognized", fields[0].isSynthetic());
//
//        Field field = TestEnum.class.getDeclaredField("field");
//        assertFalse("Non synthetic field wrongly stated as synthetic", field
//                .isSynthetic());
//
//        field = TestClass.class.getDeclaredField("annotatedField");
//        assertFalse("Non synthetic field wrongly stated as synthetic", field
//                .isSynthetic());
//    }


    public void test_getGenericType() throws Exception {
        Field field = GenericField.class.getDeclaredField("field");
        Type type = field.getGenericType();
        @SuppressWarnings("unchecked")
        TypeVariable typeVar = (TypeVariable) type;
        assertEquals("Wrong type name returned", "S", typeVar.getName());

        Field boundedField = GenericField.class.getDeclaredField("boundedField");
        Type boundedType = boundedField.getGenericType();
        @SuppressWarnings("unchecked")
        TypeVariable boundedTypeVar = (TypeVariable) boundedType;
        assertEquals("Wrong type name returned", "T", boundedTypeVar.getName());
        assertEquals("More than one bound found", 1,
                boundedTypeVar.getBounds().length);
        assertEquals("Wrong bound returned", Number.class,
                boundedTypeVar.getBounds()[0]);
    }


    public void test_toGenericString() throws Exception {
        Field field = GenericField.class.getDeclaredField("field");
        assertEquals("Wrong generic string returned",
                "S org.apache.harmony.tests.java.lang.reflect.FieldTest$GenericField.field",
                field.toGenericString());

        Field boundedField = GenericField.class
                .getDeclaredField("boundedField");
        assertEquals(
                "Wrong generic string returned",
                "T org.apache.harmony.tests.java.lang.reflect.FieldTest$GenericField.boundedField",
                boundedField.toGenericString());

        Field ordinary = GenericField.class.getDeclaredField("intField");
        assertEquals(
                "Wrong generic string returned",
                "int org.apache.harmony.tests.java.lang.reflect.FieldTest$GenericField.intField",
                ordinary.toGenericString());
    }


    public void test_hashCode() throws Exception {
        Field field = TestClass.class.getDeclaredField("annotatedField");
        assertEquals("Wrong hashCode returned", field.getName().hashCode()
                ^ field.getDeclaringClass().getName().hashCode(), field
                .hashCode());
    }


    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     */
    protected void tearDown() {
      TestField.booleanSField = true;
      TestField.byteSField = Byte.MAX_VALUE;
      TestField.charSField = 'T';
      TestField.doubleSField = Double.MAX_VALUE;
      TestField.floatSField = Float.MAX_VALUE;
      TestField.intSField = Integer.MAX_VALUE;
      TestField.longSField = Long.MAX_VALUE;
      TestField.shortSField = Short.MAX_VALUE;
    }
}

class TestAccess {
    private static int xxx;
}
