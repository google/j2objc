/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.logging.tests.java.util.logging;

import java.io.Serializable;
import java.util.ResourceBundle;
import java.util.logging.Level;

import junit.framework.TestCase;

import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

/*
 * This class implements Serializable, so that the non-static inner class
 * MockLevel can be Serializable.
 */
public class LevelTest extends TestCase implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Test the constructor without resource bundle parameter using normal
	 * values. As byproducts, getName & intValue are also tested.
	 */
	public void testConstructorNoResBundle_Normal() {
		MockLevel l = new MockLevel("level1", 1);
		assertEquals("level1", l.getName());
		assertEquals(1, l.intValue());
		assertNull(l.getResourceBundleName());
	}

	/**
	 * Test the constructor without resource bundle parameter using null name.
	 * As byproducts, getName & intValue are also tested.
	 */
	public void testConstructorNoResBundle_NullName() {
        try {
            new MockLevel(null, -2);
            fail("No expected NullPointerException");
        } catch (NullPointerException ignore) {
        	// expected
        }
    }

	/*
	 * Test the constructor without resource bundle parameter using empty name.
	 * As byproducts, getName & intValue are also tested.
	 */
	 public void testConstructorNoResBundle_EmptyName() {
		MockLevel l = new MockLevel("", -3);
		assertEquals("", l.getName());
		assertEquals(-3, l.intValue());
		assertNull(l.getResourceBundleName());
	}

	/*
	 * Test the constructor having resource bundle parameter using normal
	 * values. As byproducts, getName & intValue are also tested.
	 */
	public void testConstructorHavingResBundle_Normal() {
		MockLevel l = new MockLevel("level1", 1, "resourceBundle");
		assertEquals("level1", l.getName());
		assertEquals(1, l.intValue());
		assertEquals("resourceBundle", l.getResourceBundleName());
	}

	/*
	 * Test the constructor having resource bundle parameter using null names.
	 * As byproducts, getName & intValue are also tested.
	 */
	public void testConstructorHavingResBundle_NullName() {
        try {
            new MockLevel(null, -123, "qwe");
            fail("No expected NullPointerException");
        } catch (NullPointerException ignore) {
        	// expected
        }
    }

	 /*
	 * Test the constructor having resource bundle parameter using empty
	 names.
	 * As byproducts, getName & intValue are also tested.
	 */
	 public void testConstructorHavingResBundle_EmptyName() {
	 MockLevel l = new MockLevel("", -1000, "");
	 assertEquals("", l.getName());
	 assertEquals(-1000, l.intValue());
	 assertEquals("", l.getResourceBundleName());
	 }

	/*
	 * Test method parse, with the pre-defined string consts.
	 */
	public void testParse_PredefinedConstStrings() {
		assertSame(Level.SEVERE, Level.parse("SEVERE"));
		assertSame(Level.WARNING, Level.parse("WARNING"));
		assertSame(Level.INFO, Level.parse("INFO"));
		assertSame(Level.CONFIG, Level.parse("CONFIG"));
		assertSame(Level.FINE, Level.parse("FINE"));
		assertSame(Level.FINER, Level.parse("FINER"));
		assertSame(Level.FINEST, Level.parse("FINEST"));
		assertSame(Level.OFF, Level.parse("OFF"));
		assertSame(Level.ALL, Level.parse("ALL"));
	}

	/*
	 * Test method parse, with an undefined string.
	 */
	public void testParse_IllegalConstString() {
		try {
			Level.parse("SEVERe");
			fail("Should throw IllegalArgumentException if undefined string.");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/*
	 * Test method parse, with a null string.
	 */
	public void testParse_NullString() {
		try {
			Level.parse(null);
			fail("Should throw NullPointerException.");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/*
	 * Test method parse, with pre-defined valid number strings.
	 */
	public void testParse_PredefinedNumber() {
		assertSame(Level.SEVERE, Level.parse("SEVERE"));
		assertSame(Level.WARNING, Level.parse("WARNING"));
		assertSame(Level.INFO, Level.parse("INFO"));
		assertSame(Level.CONFIG, Level.parse("CONFIG"));
		assertSame(Level.FINE, Level.parse("FINE"));
		assertSame(Level.FINER, Level.parse("FINER"));
		assertSame(Level.FINEST, Level.parse("FINEST"));
		assertSame(Level.OFF, Level.parse("OFF"));
		assertSame(Level.ALL, Level.parse("ALL"));
		assertSame(Level.SEVERE, Level.parse("1000"));
		assertSame(Level.WARNING, Level.parse("900"));
		assertSame(Level.INFO, Level.parse("800"));
		assertSame(Level.CONFIG, Level.parse("700"));
		assertSame(Level.FINE, Level.parse("500"));
		assertSame(Level.FINER, Level.parse("400"));
		assertSame(Level.FINEST, Level.parse("300"));
		assertSame(Level.OFF, Level.parse(String.valueOf(Integer.MAX_VALUE)));
		assertSame(Level.ALL, Level.parse(String.valueOf(Integer.MIN_VALUE)));
	}

	/*
	 * Test method parse, with an undefined valid number strings.
	 */
	public void testParse_UndefinedNumber() {
		Level l = Level.parse("0");
		assertEquals(0, l.intValue());
		assertEquals("0", l.getName());
		assertNull(l.getResourceBundleName());
	}

	/*
	 * Test method parse, with an undefined valid number strings with spaces.
	 */
	public void testParse_UndefinedNumberWithSpaces() {
		try {
			Level.parse(" 0");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	public void testParse_NegativeNumber() {
		Level l = Level.parse("-4");
		assertEquals(-4, l.intValue());
		assertEquals("-4", l.getName());
		assertNull(l.getResourceBundleName());
	}

	/*
	 * Test method parse, expecting the same objects will be returned given the
	 * same name, even for non-predefined levels.
	 */
	public void testParse_SameObject() {
		Level l = Level.parse("-100");
		assertSame(l, Level.parse("-100"));
	}

	/*
	 * Test method hashCode, with normal fields.
	 */
	public void testHashCode_Normal() {
		assertEquals(100, Level.parse("100").hashCode());
		assertEquals(-1, Level.parse("-1").hashCode());
		assertEquals(0, Level.parse("0").hashCode());
		assertEquals(Integer.MIN_VALUE, Level.parse("ALL").hashCode());
	}

	/*
	 * Test equals when two objects are equal.
	 */
	public void testEquals_Equal() {
		MockLevel l1 = new MockLevel("level1", 1);
		MockLevel l2 = new MockLevel("level2", 1);
		assertEquals(l1, l2);
		assertEquals(l2, l1);
	}

	/*
	 * Test equals when two objects are not equal.
	 */
	public void testEquals_NotEqual() {
		MockLevel l1 = new MockLevel("level1", 1);
		MockLevel l2 = new MockLevel("level1", 2);
		assertFalse(l1.equals(l2));
		assertFalse(l2.equals(l1));
	}

	/*
	 * Test equals when the other object is null.
	 */
	public void testEquals_Null() {
		assertFalse(Level.ALL.equals(null));
	}

	/*
	 * Test equals when the other object is not an instance of Level.
	 */
	public void testEquals_NotLevel() {
		assertFalse(Level.ALL.equals(new Object()));
	}

	/*
	 * Test equals when the other object is itself.
	 */
	public void testEquals_Itself() {
		assertTrue(Level.ALL.equals(Level.ALL));
	}

	/*
	 * Test toString of a normal Level.
	 */
	public void testToString_Normal() {
		assertEquals("ALL", Level.ALL.toString());

		MockLevel l = new MockLevel("name", 2);
		assertEquals("name", l.toString());

		MockLevel emptyLevel = new MockLevel("", 3);
		assertEquals("", emptyLevel.toString());
	}

    // comparator for Level objects:
    // is used because Level.equals() method only takes into account
    // 'level' value but ignores 'name' and 'resourceBundleName' values
    private static final SerializableAssert LEVEL_COMPARATOR = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            Level init = (Level) initial;
            Level dser = (Level) deserialized;

            assertEquals("Class", init.getClass(), dser.getClass());
            assertEquals("Name", init.getName(), dser.getName());
            assertEquals("Value", init.intValue(), dser.intValue());
            assertEquals("ResourceBundleName", init.getResourceBundleName(),
                    dser.getResourceBundleName());
        }
    };

    /**
     * @tests serialization/deserialization compatibility.
     *
     * Test serialization of pre-defined const levels. It is expected that the
     * deserialized cost level should be the same instance as the existing one.
     */
    public void testSerialization_ConstLevel() throws Exception {

        SerializationTest.verifySelf(Level.ALL,
                SerializationTest.SAME_COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility.
     *
     * Test serialization of normal instance of Level. It is expected that the
     * deserialized level object should be equal to the original one.
     */
    public void testSerialization_InstanceLevel() throws Exception {

        // tests that objects are the same
        Level[] objectsToTest = new Level[] { Level.parse("550")};

        SerializationTest.verifySelf(objectsToTest,
                SerializationTest.SAME_COMPARATOR);

        // tests that objects are the equals
        objectsToTest = new Level[] {
                new MockLevel("123", 123, "bundle"),
                new MockLevel("123", 123, null) };

        SerializationTest.verifySelf(objectsToTest, LEVEL_COMPARATOR);
    }

    /**
     * @tests serialization/deserialization compatibility with RI.
     *
     TODO(tball): b/12032235
    public void testSerializationCompatibility() throws Exception {

        SerializationTest.verifyGolden(this,
                new MockLevel("123", 123, "bundle"), LEVEL_COMPARATOR);
    }

	public void testGetLocalName() {
        ResourceBundle rb = ResourceBundle.getBundle("bundles/java/util/logging/res");
		Level l = new MockLevel("level1", 120,
				"bundles/java/util/logging/res");
		assertEquals(rb.getString("level1"), l.getLocalizedName());

        // regression test for HARMONY-2415
        rb = ResourceBundle.getBundle(
                "org.apache.harmony.logging.tests.java.util.logging.LevelTestResource");
        l = new MockLevel("Level_error", 120,
                "org.apache.harmony.logging.tests.java.util.logging.LevelTestResource");
        assertEquals(rb.getString("Level_error"), l.getLocalizedName());

		l = new MockLevel("bad name", 120, "res");
		assertEquals("bad name", l.getLocalizedName());

		l = new MockLevel("level1", 11120, "bad name");
		assertEquals("level1", l.getLocalizedName());

		l = new MockLevel("level1", 1120);
		assertEquals("level1", l.getLocalizedName());
	}

	/*
	 * Test defining new levels in subclasses of Level
	 */
	public void testSubclassNewLevel() {
		MyLevel.DUPLICATENAME.getName();// just to load MyLevel class

		// test duplicated name and num
		assertEquals("INFO", MyLevel.parse("800").getName());
		assertEquals(800, MyLevel.parse("INFO").intValue());
		// test duplicated name
		assertEquals("FINE", MyLevel.parse("499").getName());
		assertEquals("FINE", MyLevel.parse("500").getName());
		assertEquals(500, MyLevel.parse("FINE").intValue());
		// test duplicated number
		assertEquals("FINEST", MyLevel.parse("300").getName());
		assertEquals(300, MyLevel.parse("FINEST").intValue());
		assertEquals(300, MyLevel.parse("MYLEVEL1").intValue());
		// test a normal new level, without duplicated elements
		assertEquals("MYLEVEL2", MyLevel.parse("299").getName());
		assertEquals(299, MyLevel.parse("MYLEVEL2").intValue());
	}

	/*
	 * This subclass is to test whether subclasses of Level can add new defined
	 * levels.
	 */
	static class MyLevel extends Level implements Serializable {
		private static final long serialVersionUID = 1L;

		public MyLevel(String name, int value) {
			super(name, value);
		}

		public static final Level DUPLICATENAMENUM = new MyLevel("INFO", 800);

		public static final Level DUPLICATENAME = new MyLevel("FINE", 499);

		public static final Level DUPLICATENUM = new MyLevel("MYLEVEL1", 300);

		public static final Level NORMAL = new MyLevel("MYLEVEL2", 299);
	}

	/*
	 * This Mock is used to expose the protected constructors.
	 */
	public class MockLevel extends Level implements Serializable {

		private static final long serialVersionUID = 1L;

		public MockLevel(String name, int value) {
			super(name, value);
		}

		public MockLevel(String name, int value, String resourceBundleName) {
			super(name, value, resourceBundleName);
		}
	}
}
