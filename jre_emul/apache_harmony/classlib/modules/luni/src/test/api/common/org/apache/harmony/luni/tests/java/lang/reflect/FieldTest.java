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

package org.apache.harmony.luni.tests.java.lang.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import tests.support.Support_Field;

public class FieldTest extends junit.framework.TestCase {

	static class TestField {
		public static int pubfield1;

		protected static double doubleSField = Double.MAX_VALUE;

		private static int privfield1 = 123;

		protected int intField = Integer.MAX_VALUE;

		protected short shortField = Short.MAX_VALUE;

		protected boolean booleanField = true;

		protected byte byteField = Byte.MAX_VALUE;

		protected long longField = Long.MAX_VALUE;

		protected double doubleField = Double.MAX_VALUE;

		protected float floatField = Float.MAX_VALUE;

		protected char charField = 'T';

		protected final int intFField = Integer.MAX_VALUE;

		protected final short shortFField = Short.MAX_VALUE;

		protected final boolean booleanFField = true;

		protected final byte byteFField = Byte.MAX_VALUE;

		protected final long longFField = Long.MAX_VALUE;

		protected final double doubleFField = Double.MAX_VALUE;

		protected final float floatFField = Float.MAX_VALUE;

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

	/**
	 * @tests java.lang.reflect.Field#equals(java.lang.Object)
	 */
	public void test_equalsLjava_lang_Object() throws Exception {
		// Test for method boolean
		// java.lang.reflect.Field.equals(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		f = x.getClass().getDeclaredField("shortField");

                assertTrue("Same Field returned false", f.equals(f));
                assertTrue("Inherited Field returned false", f.equals(x.getClass()
                                .getDeclaredField("shortField")));
                assertTrue("Identical Field from different class returned true", !f
                                .equals(A.class.getDeclaredField("shortField")));
	}

	/**
	 * @tests java.lang.reflect.Field#get(java.lang.Object)
	 */
	public void test_getLjava_lang_Object() throws Throwable {
		// Test for method java.lang.Object
		// java.lang.reflect.Field.get(java.lang.Object)
		TestField x = new TestField();
		Field f = x.getClass().getDeclaredField("doubleField");
		Double val = (Double) f.get(x);

		assertTrue("Returned incorrect double field value",
				val.doubleValue() == Double.MAX_VALUE);
		// Test getting a static field;
		f = x.getClass().getDeclaredField("doubleSField");
		f.set(x, new Double(1.0));
		val = (Double) f.get(x);
		assertEquals("Returned incorrect double field value", 1.0, val
				.doubleValue());
	}

	class SupportSubClass extends Support_Field {

		Object getField(char primitiveType, Object o, Field f,
				Class expectedException) {
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
				if (expectedException != null) {
					fail("expected exception " + expectedException.getName());
				}
			} catch (Exception e) {
				if (expectedException == null) {
					fail("unexpected exception " + e);
				} else {
					assertTrue("expected exception "
							+ expectedException.getName() + " and got " + e, e
							.getClass().equals(expectedException));
				}
			}
			return res;
		}

		void setField(char primitiveType, Object o, Field f,
				Class expectedException, Object value) {
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
				if (expectedException != null) {
					fail("expected exception " + expectedException.getName());
				}
			} catch (Exception e) {
				if (expectedException == null) {
					fail("unexpected exception " + e);
				} else {
					assertTrue("expected exception "
							+ expectedException.getName() + " and got " + e, e
							.getClass().equals(expectedException));
				}
			}
		}
	}

	/**
	 * @tests java.lang.reflect.Field#getBoolean(java.lang.Object)
	 */
	public void test_getBooleanLjava_lang_Object() throws Exception {
		// Test for method boolean
		// java.lang.reflect.Field.getBoolean(java.lang.Object)

		TestField x = new TestField();
		Field f = null;
		boolean val = false;
    f = x.getClass().getDeclaredField("booleanField");
    val = f.getBoolean(x);

    assertTrue("Returned incorrect boolean field value", val);

    try {
            f = x.getClass().getDeclaredField("doubleField");
            f.getBoolean(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since doubleField is not a
            // boolean type
            return;
    }
		fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getByte(java.lang.Object)
	 */
	public void test_getByteLjava_lang_Object() throws Exception {
		// Test for method byte
		// java.lang.reflect.Field.getByte(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		byte val = 0;
    f = x.getClass().getDeclaredField("byteField");
    val = f.getByte(x);

    assertTrue("Returned incorrect byte field value", val == Byte.MAX_VALUE);
    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.getByte(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since byteField is not a
            // boolean type
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getChar(java.lang.Object)
	 */
	public void test_getCharLjava_lang_Object() throws Exception {
		// Test for method char
		// java.lang.reflect.Field.getChar(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		char val = 0;
    f = x.getClass().getDeclaredField("charField");
    val = f.getChar(x);

    assertEquals("Returned incorrect char field value", 'T', val);
    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.getChar(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since charField is not a
            // boolean type
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getDeclaringClass()
	 */
	public void test_getDeclaringClass() {
		// Test for method java.lang.Class
		// java.lang.reflect.Field.getDeclaringClass()
		Field[] fields;

    fields = new TestField().getClass().getFields();
    assertTrue("Returned incorrect declaring class", fields[0]
                    .getDeclaringClass().equals(new TestField().getClass()));

    // Check the case where the field is inherited to be sure the parent
    // is returned as the declarator
    fields = new TestFieldSub1().getClass().getFields();
    assertTrue("Returned incorrect declaring class", fields[0]
                    .getDeclaringClass().equals(new TestField().getClass()));
	}

	/**
	 * @tests java.lang.reflect.Field#getDouble(java.lang.Object)
	 */
	public void test_getDoubleLjava_lang_Object() throws Exception {
		// Test for method double
		// java.lang.reflect.Field.getDouble(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		double val = 0.0;
    f = x.getClass().getDeclaredField("doubleField");
    val = f.getDouble(x);

    assertTrue("Returned incorrect double field value",	val == Double.MAX_VALUE);
    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.getDouble(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since doubleField is not a
            // boolean type
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getFloat(java.lang.Object)
	 */
	public void test_getFloatLjava_lang_Object() throws Exception {
		// Test for method float
		// java.lang.reflect.Field.getFloat(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		float val = 0;
    f = x.getClass().getDeclaredField("floatField");
    val = f.getFloat(x);

    assertTrue("Returned incorrect float field value", val == Float.MAX_VALUE);
    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.getFloat(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since floatField is not a
            // boolean type
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getInt(java.lang.Object)
	 */
	public void test_getIntLjava_lang_Object() throws Exception {
		// Test for method int java.lang.reflect.Field.getInt(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		int val = 0;
    f = x.getClass().getDeclaredField("intField");
    val = f.getInt(x);

    assertTrue("Returned incorrect Int field value", val == Integer.MAX_VALUE);
        try {
                f = x.getClass().getDeclaredField("booleanField");
            f.getInt(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since IntField is not a
            // boolean type
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getLong(java.lang.Object)
	 */
	public void test_getLongLjava_lang_Object() throws Exception {
		// Test for method long
		// java.lang.reflect.Field.getLong(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		long val = 0;
    f = x.getClass().getDeclaredField("longField");
    val = f.getLong(x);

    assertTrue("Returned incorrect long field value", val == Long.MAX_VALUE);

    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.getLong(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // long type
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getModifiers()
	 */
	public void test_getModifiers() throws Exception {
		// Test for method int java.lang.reflect.Field.getModifiers()
		TestField x = new TestField();
		Field f = null;
		f = x.getClass().getDeclaredField("prsttrvol");

                int mod = f.getModifiers();
		int mask = (Modifier.PROTECTED | Modifier.STATIC)
				| (Modifier.TRANSIENT | Modifier.VOLATILE);
		int nmask = (Modifier.PUBLIC | Modifier.NATIVE);
		assertTrue("Returned incorrect field modifiers: ",
				((mod & mask) == mask) && ((mod & nmask) == 0));
	}

	/**
	 * @tests java.lang.reflect.Field#getName()
	 */
	public void test_getName() throws Exception {
		// Test for method java.lang.String java.lang.reflect.Field.getName()
		TestField x = new TestField();
		Field f = null;
		f = x.getClass().getDeclaredField("shortField");

    assertEquals("Returned incorrect field name", "shortField", f.getName());
	}

	/**
	 * @tests java.lang.reflect.Field#getShort(java.lang.Object)
	 */
	public void test_getShortLjava_lang_Object() throws Exception {
		// Test for method short
		// java.lang.reflect.Field.getShort(java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		short val = 0;

    f = x.getClass().getDeclaredField("shortField");
    val = f.getShort(x);

    assertTrue("Returned incorrect short field value", val == Short.MAX_VALUE);
        try {
                f = x.getClass().getDeclaredField("booleanField");
            f.getShort(x);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // short type
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#getType()
	 */
	public void test_getType() throws Exception {
		// Test for method java.lang.Class java.lang.reflect.Field.getType()
		TestField x = new TestField();
		Field f = null;
		f = x.getClass().getDeclaredField("shortField");

                assertTrue("Returned incorrect field type: " + f.getType().toString(),
				f.getType().equals(short.class));
	}

	/**
	 * @tests java.lang.reflect.Field#set(java.lang.Object, java.lang.Object)
	 */
	public void test_setLjava_lang_ObjectLjava_lang_Object() throws Exception {
		// Test for method void java.lang.reflect.Field.set(java.lang.Object,
		// java.lang.Object)
		TestField x = new TestField();
		Field f = null;
		double val = 0.0;
    f = x.getClass().getDeclaredField("doubleField");
    f.set(x, new Double(1.0));
    val = f.getDouble(x);

    assertEquals("Returned incorrect double field value", 1.0, val);

    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.set(x, new Double(1.0));
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // double type
            return;
    }
    try {
            f = x.getClass().getDeclaredField("doubleFField");
            f.set(x, new Double(1.0));
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since doubleFField is
            // declared as final
            return;
    }
    // Test setting a static field;
    f = x.getClass().getDeclaredField("doubleSField");
    f.set(x, new Double(1.0));
    val = f.getDouble(x);
    assertEquals("Returned incorrect double field value", 1.0, val);
	}

	/**
	 * @tests java.lang.reflect.Field#setBoolean(java.lang.Object, boolean)
	 */
	public void test_setBooleanLjava_lang_ObjectZ() throws Exception {
		// Test for method void
		// java.lang.reflect.Field.setBoolean(java.lang.Object, boolean)
		TestField x = new TestField();
		Field f = null;
		boolean val = false;
    f = x.getClass().getDeclaredField("booleanField");
    f.setBoolean(x, false);
    val = f.getBoolean(x);

    assertTrue("Returned incorrect float field value", !val);
    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setBoolean(x, true);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // boolean type
            return;
    }

    try {
            f = x.getClass().getDeclaredField("booleanFField");
            f.setBoolean(x, true);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since booleanField is
            // declared as final
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#setByte(java.lang.Object, byte)
	 */
	public void test_setByteLjava_lang_ObjectB() throws Exception {
		// Test for method void
		// java.lang.reflect.Field.setByte(java.lang.Object, byte)
		TestField x = new TestField();
		Field f = null;
		byte val = 0;
    f = x.getClass().getDeclaredField("byteField");
    f.setByte(x, (byte) 1);
    val = f.getByte(x);

    assertEquals("Returned incorrect float field value", 1, val);

    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setByte(x, (byte) 1);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // byte type
            return;
    }

    try {
            f = x.getClass().getDeclaredField("byteFField");
            f.setByte(x, (byte) 1);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since byteFField is declared
            // as final
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#setChar(java.lang.Object, char)
	 */
	public void test_setCharLjava_lang_ObjectC() throws Exception {
		// Test for method void
		// java.lang.reflect.Field.setChar(java.lang.Object, char)
		TestField x = new TestField();
		Field f = null;
		char val = 0;
    f = x.getClass().getDeclaredField("charField");
    f.setChar(x, (char) 1);
    val = f.getChar(x);

    assertEquals("Returned incorrect float field value", 1, val);

    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setChar(x, (char) 1);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // char type
            return;
    }

    try {
            f = x.getClass().getDeclaredField("charFField");
            f.setChar(x, (char) 1);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since charFField is declared
            // as final
            return;
    }

		fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#setDouble(java.lang.Object, double)
	 */
	public void test_setDoubleLjava_lang_ObjectD() throws Exception {
		// Test for method void
		// java.lang.reflect.Field.setDouble(java.lang.Object, double)
		TestField x = new TestField();
		Field f = null;
		double val = 0.0;
    f = x.getClass().getDeclaredField("doubleField");
    f.setDouble(x, 1.0);
    val = f.getDouble(x);

    assertEquals("Returned incorrect double field value", 1.0, val);

    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setDouble(x, 1.0);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // double type
            return;
    }

    try {
            f = x.getClass().getDeclaredField("doubleFField");
            f.setDouble(x, 1.0);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since doubleFField is
            // declared as final
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#setFloat(java.lang.Object, float)
	 */
	public void test_setFloatLjava_lang_ObjectF() throws Exception {
		// Test for method void
		// java.lang.reflect.Field.setFloat(java.lang.Object, float)
		TestField x = new TestField();
		Field f = null;
		float val = 0.0F;
    f = x.getClass().getDeclaredField("floatField");
    f.setFloat(x, (float) 1);
    val = f.getFloat(x);

    assertEquals("Returned incorrect float field value", 1.0, val, 0.0);
    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setFloat(x, (float) 1);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // float type
            return;
    }
    try {
            f = x.getClass().getDeclaredField("floatFField");
            f.setFloat(x, (float) 1);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since floatFField is
            // declared as final
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#setInt(java.lang.Object, int)
	 */
	public void test_setIntLjava_lang_ObjectI() throws Exception {
		// Test for method void java.lang.reflect.Field.setInt(java.lang.Object,
		// int)
		TestField x = new TestField();
		Field f = null;
		int val = 0;
    f = x.getClass().getDeclaredField("intField");
    f.setInt(x, (int) 1);
    val = f.getInt(x);

    assertEquals("Returned incorrect int field value", 1, val);

    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setInt(x, (int) 1);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // int type
            return;
    }
    try {
            f = x.getClass().getDeclaredField("intFField");
            f.setInt(x, (int) 1);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since intFField is declared
            // as final
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#setLong(java.lang.Object, long)
	 */
	public void test_setLongLjava_lang_ObjectJ() throws Exception {
		// Test for method void
		// java.lang.reflect.Field.setLong(java.lang.Object, long)
		TestField x = new TestField();
		Field f = null;
		long val = 0L;
    f = x.getClass().getDeclaredField("longField");
    f.setLong(x, (long) 1);
    val = f.getLong(x);

    assertEquals("Returned incorrect long field value", 1, val);

    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setLong(x, (long) 1);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // long type
            return;
    }
    try {
            f = x.getClass().getDeclaredField("longFField");
            f.setLong(x, (long) 1);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since longFField is declared
            // as final
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#setShort(java.lang.Object, short)
	 */
	public void test_setShortLjava_lang_ObjectS() throws Exception {
		// Test for method void
		// java.lang.reflect.Field.setShort(java.lang.Object, short)
		TestField x = new TestField();
		Field f = null;
		short val = 0;
    f = x.getClass().getDeclaredField("shortField");
    f.setShort(x, (short) 1);
    val = f.getShort(x);

    assertEquals("Returned incorrect short field value", 1, val);
    try {
            f = x.getClass().getDeclaredField("booleanField");
            f.setShort(x, (short) 1);
    } catch (IllegalArgumentException ex) {
            // Good, Exception should be thrown since booleanField is not a
            // short type
            return;
    }
    try {
            f = x.getClass().getDeclaredField("shortFField");
            f.setShort(x, (short) 1);
    } catch (IllegalAccessException ex) {
            // Good, Exception should be thrown since shortFField is
            // declared as final
            return;
    }

    fail("Accessed field of invalid type");
	}

	/**
	 * @tests java.lang.reflect.Field#toString()
	 */
	public void test_toString() throws Exception {
        Field f = null;

        f = TestField.class.getDeclaredField("x");

        assertEquals(
                "Field returned incorrect string",
                "private static final int org.apache.harmony.luni.tests.java.lang.reflect.FieldTest$TestField.x",
                f.toString());
    }
}

class TestAccess {
    private static int xxx;
}
