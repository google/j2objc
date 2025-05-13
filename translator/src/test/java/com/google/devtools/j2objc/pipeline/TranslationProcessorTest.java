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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.file.RegularInputFile;
import java.io.File;
import java.io.IOException;

/**
 * Tests for {@link TranslationProcessor}.
 *
 * @author kstanger@google.com (Keith Stanger)
 */
public class TranslationProcessorTest extends GenerationTest {

  public void testSingleSourceFileBuildClosure() throws IOException {
    options.setBuildClosure(true);

    addSourceFile("class Test { }", "Test.java");

    GenerationBatch batch = new GenerationBatch(options);
    batch.addSource(new RegularInputFile(getTempDir() + "/Test.java", "Test.java"));
    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(options), null);
    processor.processInputs(batch.getInputs());

    String translation = getTranslatedFile("Test.h");
    assertTranslation(translation, "@interface Test");
    assertNoErrors();
    assertNoWarnings();
  }

  public void testDuplicateSourceFileOnSourcepath() throws IOException {
    options.setBuildClosure(true);

    // Have src/main/java precede tmp dir in source path.
    options.fileUtil().insertSourcePath(0, getTempDir() + "/src/main/java");
    options.fileUtil().appendSourcePath(getTempDir());

    addSourceFile("class Test { Foo f; }", "Test.java");
    addSourceFile("class Foo { void foo1() {} }", "Foo.java");
    addSourceFile("class Foo { void foo2() {} }", "src/main/java/Foo.java");

    GenerationBatch batch = new GenerationBatch(options);
    batch.addSource(new RegularInputFile(getTempDir() + "/Test.java", "Test.java"));
    batch.addSource(new RegularInputFile(getTempDir() + "/src/main/java/Foo.java", "Foo.java"));
    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(options), null);
    processor.processInputs(batch.getInputs());

    String translation = getTranslatedFile("Foo.h");
    assertTranslation(translation, "- (void)foo2;");
    assertNotInTranslation(translation, "foo1");
  }

  public void testEntryClasses() throws IOException {
    addSourceFile("class A { B test() { return new B(); }}", "A.java");
    addSourceFile("class B extends C {}", "B.java");
    addSourceFile("class C {}", "C.java");

    // Specify B as an entry class for building a class closure.
    options.load(new String[] {
        "--build-closure",
        "B"
    });
    GenerationBatch batch = new GenerationBatch(options);
    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(options), null);
    processor.processInputs(batch.getInputs());

    // Assert B entry class was compiled.
    assertTrue(new File(tempDir, "B.m").exists());

    // Verify C.java was compiled, since it depends upon B.
    assertTrue(new File(tempDir, "C.m").exists());

    // Verify A.java wasn't compiled; it has a B reference, but B doesn't depend on it.
    assertFalse(new File(tempDir, "A.m").exists());
  }

  public void testProcessIncludeCycleWithSegmentedHeaders() throws IOException {
    options.load(new String[] {"--segmented-headers"});
    doProcessIncludeCycle();

    assertNoErrors();
    assertNoWarnings();
  }

  public void testProcessIncludeCycleWithoutSegmentedHeaders() throws IOException {
    options.load(new String[] {"--no-segmented-headers"});
    doProcessIncludeCycle();

    assertErrorRegex("This target contains an include cycle[\\s\\S]*");
  }

  private void doProcessIncludeCycle() throws IOException {
    String fileA = addSourceFile("interface A {} class BI implements B {}", "A.java");
    String fileB = addSourceFile("interface B {} class AI implements A {}", "B.java");

    GenerationBatch batch = new GenerationBatch(options);
    batch.addSource(new RegularInputFile(fileA, "A.java"));
    batch.addSource(new RegularInputFile(fileB, "B.java"));

    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(options), null);
    processor.processInputs(batch.getInputs());
  }

  public void testCheckNoCyclesWithoutCycles() {
    TranslationProcessor.checkNoCycles(
        ImmutableMap.of(
            "A.h",
            ImmutableSet.of("B.h", "C.h", "D.h"),
            "B.h",
            ImmutableSet.of("C.h", "D.h"),
            "C.h",
            ImmutableSet.of("D.h")));

    assertNoErrors();
    assertNoWarnings();
  }

  public void testCheckNoCyclesWithCycles() {
    TranslationProcessor.checkNoCycles(
        ImmutableMap.of(
            "A.h",
            ImmutableSet.of("B.h", "E.h"),
            "B.h",
            ImmutableSet.of("C.h", "E.h"),
            "C.h",
            ImmutableSet.of("D.h"),
            "D.h",
            ImmutableSet.of("A.h")));

    assertErrorRegex("This target contains an include cycle[\\s\\S]*");
  }
}
