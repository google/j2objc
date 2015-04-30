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

package com.google.devtools.j2objc;

import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.util.ErrorUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * Tests for {@link TranslationProcessor}.
 *
 * @author kstanger@google.com (Keith Stanger)
 */
public class TranslationProcessorTest extends GenerationTest {

  public void testJarBatchTranslation() throws IOException {
    String fooSource = "package mypkg; class Foo {}";
    String barSource = "package mypkg; class Bar {}";
    File jarFile = getTempFile("test.jar");
    JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile));
    try {
      JarEntry fooEntry = new JarEntry("mypkg/Foo.java");
      jar.putNextEntry(fooEntry);
      jar.write(fooSource.getBytes());
      jar.closeEntry();
      JarEntry barEntry = new JarEntry("mypkg/Bar.java");
      jar.putNextEntry(barEntry);
      jar.write(barSource.getBytes());
      jar.closeEntry();
    } finally {
      jar.close();
    }

    Options.appendSourcePath(jarFile.getPath());
    Options.setBatchTranslateMaximum(2);

    GenerationBatch batch = new GenerationBatch();
    batch.addSource(new JarredInputFile(getTempDir() + "/test.jar", "mypkg/Foo.java"));
    batch.addSource(new JarredInputFile(getTempDir() + "/test.jar", "mypkg/Bar.java"));
    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(), null);
    processor.processInputs(batch.getInputs());

    assertEquals(0, ErrorUtil.errorCount());
  }

  public void testSingleSourceFileBuildClosure() throws IOException {
    Options.setBuildClosure(true);

    addSourceFile("class Test { }", "Test.java");

    GenerationBatch batch = new GenerationBatch();
    batch.addSource(new RegularInputFile(getTempDir() + "/Test.java", "Test.java"));
    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(), null);
    processor.processInputs(batch.getInputs());
    processor.processBuildClosureDependencies();

    String translation = getTranslatedFile("Test.h");
    assertTranslation(translation, "@interface Test");
    assertErrorCount(0);
    assertWarningCount(0);
  }

  public void testDuplicateSourceFileOnSourcepath() throws IOException {
    Options.setBuildClosure(true);
    Options.appendSourcePath(getTempDir());

    addSourceFile("class Test { Foo f; }", "Test.java");
    addSourceFile("class Foo { void foo1() {} }", "Foo.java");
    addSourceFile("class Foo { void foo2() {} }", "src/main/java/Foo.java");

    GenerationBatch batch = new GenerationBatch();
    batch.addSource(new RegularInputFile(getTempDir() + "/Test.java", "Test.java"));
    batch.addSource(new RegularInputFile(getTempDir() + "/src/main/java/Foo.java", "Foo.java"));
    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(), null);
    processor.processInputs(batch.getInputs());
    processor.processBuildClosureDependencies();

    String translation = getTranslatedFile("Foo.h");
    assertTranslation(translation, "- (void)foo2;");
    assertNotInTranslation(translation, "foo1");
  }

  public void testBatchReuse() throws IOException {
    addSourceFile("class Test { }", "Test.java");

    RegularInputFile inputFile = new RegularInputFile(getTempDir() + "/Test.java", "Test.java");
    ProcessingContext input = ProcessingContext.fromFile(inputFile);
    TranslationProcessor processor = new TranslationProcessor(J2ObjC.createParser(), null);
    processor.processInputs(Collections.singletonList(input));
    String header = getTranslatedFile("Test.h");
    String impl = getTranslatedFile("Test.m");
    assertTranslation(header, "@interface Test");
    assertTranslation(impl, "@implementation Test");

    // Test that the GenerationUnit no longer holds a reference to the tree.
    assertEquals(0, input.getGenerationUnit().getCompilationUnits().size());
  }
}
