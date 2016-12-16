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

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.CodeReferenceMap.Builder;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 * System tests for the TreeShaker tool.
 *
 * @author Priyank Malvania
 */
public class TreeShakerTest extends TestCase {

  File tempDir;
  List<String> inputFiles;

  static {
    // Prevents errors and warnings from being printed to the console.
    ErrorUtil.setTestMode();
  }

  @Override
  protected void setUp() throws IOException {
    tempDir = createTempDir();
    inputFiles = new ArrayList<>();
  }

  @Override
  protected void tearDown() {
    ErrorUtil.reset();
  }

  private CodeReferenceMap getUnusedCode() throws IOException {
    return getUnusedCode(null);
  }

  private CodeReferenceMap getUnusedCode(CodeReferenceMap rootSetMap) throws IOException {
    Options options = new Options();
    options.setSourceFiles(inputFiles);
    options.setClasspath(System.getProperty("java.class.path"));

    TreeShaker shaker = new TreeShaker(options);
    CodeReferenceMap map = shaker.getUnusedCode(rootSetMap);

    if (ErrorUtil.errorCount() > 0) {
      fail("TreeShaker failed with errors:\n" + Joiner.on("\n").join(ErrorUtil.getErrorMessages()));
    }
    return map;
  }

  public void testUnusedCodeAcrossFiles() throws IOException {
    addSourceFile("A.java", "class A { static { launch(); }\n"
        + "public static void launch() { new B().abc(\"zoo\"); } }");
    addSourceFile("B.java", "class B { public void abc(String s) {} }");
    addSourceFile("C.java", "class C { public void xyz(String s) {} }");
    CodeReferenceMap unusedCodeMap = getUnusedCode();

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsClass("B"));
    assertFalse(unusedCodeMap.containsMethod("B", "abc", "(Ljava/lang/String;)V"));

    assertTrue(unusedCodeMap.containsClass("C"));
    assertTrue(unusedCodeMap.containsMethod("C", "xyz", "(Ljava/lang/String;)V"));
  }

  public void testNoPublicRootSet() throws IOException {
    addSourceFile("A.java", "class A { public void launch() { new B().abc(\"zoo\"); } }");
    addSourceFile("B.java", "class B { public void abc(String s) {} }");
    addSourceFile("C.java", "class C { public void xyz(String s) {} }");
    CodeReferenceMap unusedCodeMap = getUnusedCode();

    assertTrue(unusedCodeMap.containsClass("A"));
    assertTrue(unusedCodeMap.containsClass("B"));
    assertTrue(unusedCodeMap.containsMethod("B", "abc", "(Ljava/lang/String;)V"));
    assertTrue(unusedCodeMap.containsClass("C"));
    assertTrue(unusedCodeMap.containsMethod("C", "xyz", "(Ljava/lang/String;)V"));
  }

  public void testWithPublicRootSet() throws IOException {
    addSourceFile("A.java", "class A { public void launch() { new B().abc(\"zoo\"); } }");
    addSourceFile("B.java", "class B { public void abc(String s) {} }");
    addSourceFile("C.java", "class C { public void xyz(String s) {} }");
    CodeReferenceMap rootSet = new Builder().addClass("A").build();
    CodeReferenceMap unusedCodeMap = getUnusedCode(rootSet);

    assertFalse(unusedCodeMap.containsClass("A"));
    assertFalse(unusedCodeMap.containsClass("B"));
    assertFalse(unusedCodeMap.containsMethod("B", "abc", "(Ljava/lang/String;)V"));

    assertTrue(unusedCodeMap.containsClass("C"));
    assertTrue(unusedCodeMap.containsMethod("C", "xyz", "(Ljava/lang/String;)V"));
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
