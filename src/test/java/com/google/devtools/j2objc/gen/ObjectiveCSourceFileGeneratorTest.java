/*
 * Copyright 2011 Google Inc. All Rights Reserved.
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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.util.NameTable;

/**
 * Tests for {@link ObjectiveCSourceFileGenerator}.
 *
 * @author Tom Ball
 */
public class ObjectiveCSourceFileGeneratorTest extends GenerationTest {

  public void testCamelCaseQualifiedName() {
    String camelCaseName = NameTable.camelCaseQualifiedName("java.lang.Object");
    assertEquals("JavaLangObject", camelCaseName);
    camelCaseName = NameTable.camelCaseQualifiedName("java.util.logging.Level");
    assertEquals("JavaUtilLoggingLevel", camelCaseName);
    camelCaseName = NameTable.camelCaseQualifiedName("java");
    assertEquals("Java", camelCaseName);
    camelCaseName = NameTable.camelCaseQualifiedName("Level");
    assertEquals("Level", camelCaseName);
    camelCaseName = NameTable.camelCaseQualifiedName("");
    assertEquals("", camelCaseName);
  }

  public void testCapitalize() {
    assertEquals("Test", NameTable.capitalize("test"));
    assertEquals("123", NameTable.capitalize("123"));
    assertEquals("", NameTable.capitalize(""));
  }
}
