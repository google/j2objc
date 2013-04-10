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
public class DeadCodeMapTest extends TestCase {

  public void testIsDeadClass() {
    DeadCodeMap report = DeadCodeMap.builder()
        .addDeadClass("foo.bar.Baz")
        .addDeadMethod("foo.bah.Bar", "abc", "()")
        .build();
    assertTrue(report.isDeadClass("foo.bar.Baz"));
    assertFalse(report.isDeadClass("foo.bah.Bar"));
  }

  public void testIsDeadField() {
    DeadCodeMap report = DeadCodeMap.builder()
        .addDeadClass("foo.bar.Baz")
        .addDeadField("foo.bah.Bar", "abc")
        .build();
    assertTrue(report.isDeadField("foo.bar.Baz", "foobar"));
    assertTrue(report.isDeadField("foo.bah.Bar", "abc"));
    assertFalse(report.isDeadField("foo.bah.Bar", "def"));
  }

  public void testIsDeadMethod() {
    DeadCodeMap report = DeadCodeMap.builder()
        .addDeadClass("foo.bar.Baz")
        .addDeadMethod("foo.bah.Bar", "abc", "()")
        .build();
    assertTrue(report.isDeadMethod("foo.bah.Bar", "abc", "()"));
    assertTrue(report.isDeadMethod("foo.bar.Baz", "anything", "()"));
    assertFalse(report.isDeadMethod("foo.bah.Bar", "abc", "(IZ)Ljava.lang.String;"));
    assertFalse(report.isDeadMethod("foo.bah.Bar", "def", "()"));
    assertFalse(report.isDeadMethod("x.y.Z", "abc", "()"));
  }
}
