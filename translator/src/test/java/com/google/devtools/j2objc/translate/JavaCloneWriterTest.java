/*
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
 * Tests for {@link JavaCloneWriter}.
 *
 * @author Keith Stanger
 */
public class JavaCloneWriterTest extends GenerationTest {

  // Make sure __javaClone is not emitted unless there is a weak field.
  public void testNoJavaCloneMethod() throws IOException {
    String translation = translateSourceFile(
        "public class Test {  int var1, var2;  static int var3;}", "Test", "Test.m");
    assertNotInTranslation(translation, "__javaClone");
  }

  public void testJavaCloneMethodAddedForWeakField() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + " class Test { @Weak Object foo; }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)__javaClone:(Test *)original {",
        "  [super __javaClone:original];",
        "  [foo_ release];",
        "}");
  }

  public void testJavaCloneMethodWithARC() throws IOException {
    Options.setMemoryManagementOption(MemoryManagementOption.ARC);
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + " class Test { @Weak Object foo; }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)__javaClone:(Test *)original {",
        "  [super __javaClone:original];",
        "  JreRelease(foo_);",
        "}");
  }

  public void testVolatileObjectFields() throws IOException {
    String translation = translateSourceFile(
        "import com.google.j2objc.annotations.Weak;"
        + " class Test { volatile Object foo; @Weak volatile Object bar; }", "Test", "Test.m");
    assertTranslatedLines(translation,
        "- (void)__javaClone:(Test *)original {",
        "  [super __javaClone:original];",
        "  JreCloneVolatileStrong(&foo_, &original->foo_);",
        "  JreCloneVolatile(&bar_, &original->bar_);",
        "}");
  }
}
