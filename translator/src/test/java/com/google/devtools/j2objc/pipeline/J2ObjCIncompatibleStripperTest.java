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

package com.google.devtools.j2objc.pipeline;

import com.google.devtools.j2objc.GenerationTest;

import java.io.IOException;

/**
 * Tests for {@link J2ObjCIncompatibleStripper}.
 *
 * @author Keith Stanger
 */
public class J2ObjCIncompatibleStripperTest extends GenerationTest {

  public void testMembersAreStripped() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.J2ObjCIncompatible; "
        + "import non.existent.pkg.Garbage;"
        + "class Test {"
        + " @J2ObjCIncompatible Garbage g;"
        + " @J2ObjCIncompatible void strippedMethod() {} }", "Test.java");
    runPipeline("Test.java");
    String translation = getTranslatedFile("Test.h");
    assertTranslation(translation, "@interface Test");
    assertNotInTranslation(translation, "Garbage");
    assertNotInTranslation(translation, "strippedMethod");
  }

  public void testTypeNameCollision() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.J2ObjCIncompatible; "
        + "import java.garbage.List; "
        + "class Test {"
        + " @J2ObjCIncompatible List l1;"
        + " java.util.List l2; }", "Test.java");
    runPipeline("Test.java");
    String translation = getTranslatedFile("Test.h");
    assertNotInTranslation(translation, "garbage");
  }

  public void testStaticImports() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.J2ObjCIncompatible; "
        + "import static java.Garbage.foo; "
        + "import static java.util.Arrays.asList; "
        + "class Test {"
        + " @J2ObjCIncompatible void test1() { foo(); }"
        + " java.util.List<Object> test2(Object[] objs) { return asList(objs); } }", "Test.java");
    runPipeline("Test.java");
    String translation = getTranslatedFile("Test.h");
    assertNotInTranslation(translation, "Garbage");
    assertNotInTranslation(translation, "foo");
    translation = getTranslatedFile("Test.m");
    assertTranslation(translation, "return JavaUtilArrays_asListWithNSObjectArray_(objs);");
  }

  public void testAnnotationImportIsPreserved() throws IOException {
    addSourceFile(
        "import com.google.j2objc.annotations.J2ObjCIncompatible; "
        + "import java.lang.annotation.Retention; "
        + "import java.lang.annotation.RetentionPolicy; "
        + "@Retention(RetentionPolicy.SOURCE) @interface Test {}", "Test.java");
    runPipeline("Test.java");
    String translation = getTranslatedFile("Test.h");
    // Mainly testing that the source compiles after stripping.
    assertTranslation(translation, "@protocol Test");
  }
}
