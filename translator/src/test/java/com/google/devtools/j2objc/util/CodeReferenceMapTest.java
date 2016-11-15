/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.util;

import junit.framework.TestCase;

/**
 * Unit tests for DeadCodeReport.
 *
 * @author Daniel Connelly
 */
public class CodeReferenceMapTest extends TestCase {

  public void testIsDeadClass() {
    CodeReferenceMap report = CodeReferenceMap.builder()
        .addClass("foo.bar.Baz")
        .addMethod("foo.bah.Bar", "abc", "()")
        .build();
    assertTrue(report.containsClass("foo.bar.Baz"));
    assertFalse(report.containsClass("foo.bah.Bar"));
  }

  public void testIsDeadField() {
    CodeReferenceMap report = CodeReferenceMap.builder()
        .addClass("foo.bar.Baz")
        .addField("foo.bah.Bar", "abc")
        .build();
    assertTrue(report.containsField("foo.bar.Baz", "foobar"));
    assertTrue(report.containsField("foo.bah.Bar", "abc"));
    assertFalse(report.containsField("foo.bah.Bar", "def"));
  }

  public void testIsDeadMethod() {
    CodeReferenceMap report = CodeReferenceMap.builder()
        .addClass("foo.bar.Baz")
        .addMethod("foo.bah.Bar", "abc", "()")
        .build();
    assertTrue(report.containsMethod("foo.bah.Bar", "abc", "()"));
    assertTrue(report.containsMethod("foo.bar.Baz", "anything", "()"));
    assertFalse(report.containsMethod("foo.bah.Bar", "abc", "(IZ)Ljava.lang.String;"));
    assertFalse(report.containsMethod("foo.bah.Bar", "def", "()"));
    assertFalse(report.containsMethod("x.y.Z", "abc", "()"));
  }

  public void testToString() {
    CodeReferenceMap report = CodeReferenceMap.builder()
        .addClass("foo.bar.Baz")
        .addMethod("foo.bah.Bar", "abc", "()")
        .addField("foo.bah.Bar", "xyz")
        .build();

    String stringVersion = report.toString();
    assertEquals(stringVersion, "[foo.bar.Baz]\n" 
        + "{foo.bah.Bar=[xyz]}\n"
        + "{foo.bah.Bar={abc=[()]}}");
  }
}
