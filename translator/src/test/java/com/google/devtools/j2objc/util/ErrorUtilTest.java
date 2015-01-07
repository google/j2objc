package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;

/**
 * Test case for {@link ErrorUtil}.
 */
public class ErrorUtilTest extends GenerationTest {

  public void testFullMessage() {
    assertEquals(
        "warning: SomeClass.java:1234: Syntax error",
        ErrorUtil.getFullMessage("warning: ", "SomeClass.java:1234: Syntax error", false));
    assertEquals(
        "/Users/xx/SomeClass.java:1234: error: Syntax error",
        ErrorUtil.getFullMessage("error: ", "/Users/xx/SomeClass.java:1234: Syntax error", true));
    assertEquals(
        "SomeClass.java:9: warning: Syntax error",
        ErrorUtil.getFullMessage("warning: ", "SomeClass.java:9: Syntax error", true));
    assertEquals(
        "/S P A C E/C.java:9: error: Syntax error",
        ErrorUtil.getFullMessage("error: ", "/S P A C E/C.java:9: Syntax error", true));
    assertEquals(
        "A.java:1: error: Some error in B.java:2: message",
        ErrorUtil.getFullMessage("error: ", "A.java:1: Some error in B.java:2: message", true));
  }
}
