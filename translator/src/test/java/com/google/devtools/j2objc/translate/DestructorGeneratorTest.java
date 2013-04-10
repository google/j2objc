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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.Options.MemoryManagementOption;

import java.io.IOException;

/**
 * Unit tests for {@link DestructorGenerator}.
 *
 * @author Tom Ball
 */
public class DestructorGeneratorTest extends GenerationTest {

  @Override
  protected void tearDown() throws Exception {
    Options.resetMemoryManagementOption();
    super.tearDown();
  }

  public void testFinalizeMethodRenamed() throws IOException {
    String translation = translateSourceFile(
        "public class Test { public void finalize() { " +
        "  try { super.finalize(); } catch (Throwable t) {} }}", "Test", "Test.h");
    assertTranslation(translation, "- (void)dealloc;");
    assertFalse(translation.contains("finalize"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)dealloc ");
    assertTranslation(translation, "[super dealloc];");
    assertFalse(translation.contains("finalize"));
  }

  public void testFinalizeMethodRenamedWithGC() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.GC);
    String translation = translateSourceFile(
        "public class Test { public void finalize() { " +
        "  try { super.finalize(); } catch (Throwable t) {} }}", "Test", "Test.h");
    assertTranslation(translation, "- (void)finalize;");
    assertFalse(translation.contains("dealloc"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)finalize ");
    assertTranslation(translation, "[super finalize];");
    assertFalse(translation.contains("dealloc"));
  }

  public void testFinalizeMethodRenamedWithReleasableFields() throws IOException {
    String translation = translateSourceFile(
        "public class Test {" +
        "  private Object o = new Object();" +
        "  public void finalize() { " +
        "    try { super.finalize(); } catch (Throwable t) {} }}", "Test", "Test.h");
    assertTranslation(translation, "- (void)dealloc;");
    assertFalse(translation.contains("finalize"));
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "- (void)dealloc ");
    assertTranslation(translation, "[super dealloc];");
    assertFalse(translation.contains("finalize"));
  }
}
