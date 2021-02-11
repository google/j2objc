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

package com.google.devtools.treeshaker;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * System tests for the TreeShaker.
 */
public class TreeShakerTest extends TestCase {
  File tempDir;
  File treeShakerRoots;
  List<String> inputFiles;

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = createTempDir();
    treeShakerRoots = null;
    inputFiles = new ArrayList<>();
  }

  @Override
  protected void tearDown() {
    ErrorUtil.reset();
  }

  private CodeReferenceMap findUnusedCode() throws IOException {
    Options options = new Options();
    options.setClasspath(System.getProperty(JAVA_CLASS_PATH.value()));
    options.setTreeShakerRoots(treeShakerRoots);
    options.setSourceFiles(inputFiles);
    TreeShaker shaker = new TreeShaker(options);
    CodeReferenceMap unused = shaker.findUnusedCode();
    if (ErrorUtil.errorCount() > 0) {
      fail("TreeShaker failed with errors:\n" + Joiner.on("\n").join(ErrorUtil.getErrorMessages()));
    }
    return unused;
  }

  public void testNoPublicRootSet() throws IOException {
    addTreeShakerRootsFile("ProGuard, version 4.0\n");
    addSourceFile("A.java", "class A { public static void launch() { new B().b(\"zoo\"); } }");
    addSourceFile("B.java", "class B { public void b(String s) { new C().c(s); } }");
    addSourceFile("C.java", "class C { public void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertTrue(unused.containsClass("A"));
    assertTrue(unused.containsMethod("A", "launch", "()V"));
    assertTrue(unused.containsClass("B"));
    assertTrue(unused.containsMethod("B", "abc", "(Ljava/lang/String;)V"));
    assertTrue(unused.containsClass("C"));
    assertTrue(unused.containsMethod("C", "xyz", "(Ljava/lang/String;)V"));
  }

  public void testWithPublicRootSet() throws IOException {
    addTreeShakerRootsFile("ProGuard, version 4.0\nA:\n    launch()");
    addSourceFile("A.java", "class A { public static void launch() { new B().b(\"zoo\"); } }");
    addSourceFile("B.java", "class B { public void b(String s) {} }");
    addSourceFile("C.java", "class C { public void c(String s) {} }");
    CodeReferenceMap unused = findUnusedCode();

    assertFalse(unused.containsClass("A"));
    assertFalse(unused.containsClass("B"));
    assertFalse(unused.containsMethod("A", "launch", "()V"));
    assertFalse(unused.containsMethod("B", "b", "(Ljava/lang/String;)V"));

    assertTrue(unused.containsClass("C"));
    assertTrue(unused.containsMethod("C", "c", "(Ljava/lang/String;)V"));
  }

  private void addTreeShakerRootsFile(String source) throws IOException {
    treeShakerRoots = new File(tempDir, "roots.cfg");
    treeShakerRoots.getParentFile().mkdirs();
    Files.asCharSink(treeShakerRoots, Charset.defaultCharset()).write(source);
  }

  private void addSourceFile(String fileName, String source) throws IOException {
    File file = new File(tempDir, fileName);
    file.getParentFile().mkdirs();
    Files.write(source, file, Charset.defaultCharset());
    inputFiles.add(file.getAbsolutePath());
  }

  private File createTempDir() throws IOException {
    File tempDir = File.createTempFile("treeshaker_testout", "");
    tempDir.delete();
    tempDir.mkdir();
    return tempDir;
  }
}
