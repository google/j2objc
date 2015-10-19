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

package org.apache.harmony.regex.tests.java.util.regex;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

public class PatternSyntaxExceptionTest extends TestCase {
  public void testPatternSyntaxException() {
    // Normal case
    PatternSyntaxException e = new PatternSyntaxException("Foo", "Bar", 0);
    assertEquals("Foo", e.getDescription());
    assertEquals("Bar", e.getPattern());
    assertEquals(0, e.getIndex());

    String s = e.getMessage();
    assertTrue(s.contains("Foo"));
    assertTrue(s.contains("Bar"));
    assertTrue(s.contains("0"));

    // No description specified
    e = new PatternSyntaxException(null, "Bar", 0);
    assertEquals(null, e.getDescription());
    assertEquals("Bar", e.getPattern());
    assertEquals(0, e.getIndex());

    s = e.getMessage();
    assertFalse(s.contains("Foo"));
    assertTrue(s.contains("Bar"));
    assertTrue(s.contains("0"));

    // No pattern specified
    e = new PatternSyntaxException("Foo", null, 0);
    assertEquals("Foo", e.getDescription());
    assertEquals(null, e.getPattern());
    assertEquals(0, e.getIndex());

    s = e.getMessage();
    assertTrue(s.contains("Foo"));
    assertFalse(s.contains("Bar"));
    assertTrue(s.contains("0"));

    // Neither description nor pattern specified
    e = new PatternSyntaxException(null, null, 0);
    assertEquals(null, e.getDescription());
    assertEquals(null, e.getPattern());
    assertEquals(0, e.getIndex());

    s = e.getMessage();
    assertFalse(s.contains("Foo"));
    assertFalse(s.contains("Bar"));
    assertTrue(s.contains("0"));

    // No index specified
    e = new PatternSyntaxException("Foo", "Bar", -1);
    assertEquals(-1, e.getIndex());

    s = e.getMessage();
    assertFalse(s.contains("^"));

    // No pattern, but index specified
    e = new PatternSyntaxException("Foo", null, 0);
    assertEquals(0, e.getIndex());

    s = e.getMessage();
    assertFalse(s.contains("^"));
  }

  public void testCase() {
    String regex = "(";
    try {
      Pattern.compile(regex);
      fail("PatternSyntaxException expected");
    } catch (PatternSyntaxException e) {
      assertEquals(1, e.getIndex());
      assertEquals(regex, e.getPattern());
    }
  }

  public void testCase2() {
    String regex = "[4-";
    try {
      Pattern.compile(regex);
      fail("PatternSyntaxException expected");
    } catch (PatternSyntaxException e) {
      assertEquals(3, e.getIndex());
      assertEquals(regex, e.getPattern());
    }
  }

  // Regression test for HARMONY-3787
  public void test_objectStreamField() {
    ObjectStreamClass objectStreamClass = ObjectStreamClass.lookup(PatternSyntaxException.class);
    assertNotNull(objectStreamClass.getField("desc"));
  }

  public void testSerializationCompatibility() throws Exception {
    PatternSyntaxException object = new PatternSyntaxException("TESTDESC", "TESTREGEX", 3);
    SerializationTest.verifyGolden(this, object, PATTERNSYNTAXEXCEPTION_COMPARATOR);
  }

  public void testSerializationSelf() throws Exception {
    PatternSyntaxException object = new PatternSyntaxException("TESTDESC", "TESTREGEX", 3);
    SerializationTest.verifySelf(object, PATTERNSYNTAXEXCEPTION_COMPARATOR);
  }

  private static final SerializableAssert PATTERNSYNTAXEXCEPTION_COMPARATOR = new SerializableAssert() {
    public void assertDeserialized(Serializable initial, Serializable deserialized) {

      // do common checks for all throwable objects
      SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial, deserialized);

      PatternSyntaxException initPatternSyntaxException = (PatternSyntaxException) initial;
      PatternSyntaxException dserPatternSyntaxException = (PatternSyntaxException) deserialized;

      // verify fields
      assertEquals(initPatternSyntaxException.getDescription(),
                   dserPatternSyntaxException.getDescription());
      assertEquals(initPatternSyntaxException.getPattern(),
                   dserPatternSyntaxException.getPattern());
      assertEquals(initPatternSyntaxException.getIndex(),
                   dserPatternSyntaxException.getIndex());
    }
  };
}
