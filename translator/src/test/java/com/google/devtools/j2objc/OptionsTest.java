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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.io.Files;
import com.google.devtools.j2objc.util.SourceVersion;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Tests for {@link Options}.
 *
 * @author tball@google.com (Tom Ball)
 */
public class OptionsTest extends GenerationTest {

  public void testSourceVersionFlags() throws IOException {
      String javaVersion = System.getProperty("java.specification.version");
      options = new Options();
      options.load(new String[] {});
    if (SourceVersion.executingOnSupportedVersion()) {
      assertTrue(javaVersion.startsWith(options.getSourceVersion().toString()));
    }

    String[] argsJavaSource = "-source 1.6".split(" ");
    options.load(argsJavaSource);
    assertEquals(SourceVersion.JAVA_6, options.getSourceVersion());

    argsJavaSource = "-source 1.7".split(" ");
    options.load(argsJavaSource);
    assertEquals(SourceVersion.JAVA_7, options.getSourceVersion());

    argsJavaSource = "-source 1.8".split(" ");
    options.load(argsJavaSource);
    assertEquals(SourceVersion.JAVA_8, options.getSourceVersion());
  }

  public void testSourceVersionFlagAliases() throws IOException {
    // Check that version aliases work correctly.
    String[] argsJavaSource = "-source 8".split(" ");
    options.load(argsJavaSource);
    assertEquals(SourceVersion.JAVA_8, options.getSourceVersion());
  }

  public void testTargetVersionFlags() throws IOException {
    String [] argsJavaTarget = "-target 1.6".split(" ");
    // Passed target should be ignored.
    options.load(argsJavaTarget);
  }

  public void testFlagsIntermixedWithSources() throws IOException {
    File tmpDir = new File(getTempDir(), "testdir");
    tmpDir.mkdir();  // Dir must exist for Options to add it to classpath.
    String cmdArgs =
        "Test.java -source 8 OtherTest.java -classpath " + tmpDir.getPath() + " AndAnother.java";
    String[] args = cmdArgs.split(" ");
    List<String> sources = options.load(args);
    assertEquals(3, sources.size());
    assertEquals(SourceVersion.JAVA_8, options.getSourceVersion());
    assertTrue(options.fileUtil().getClassPathEntries().contains(tmpDir.getPath()));
  }

  public void testMultipleDeadCodeReports() throws IOException {
    File first = new File(getTempDir(), "first.cfg");
    Files.write("first line\n", first, UTF_8);
    options.addDeadCodeReport(first.getPath());

    File second = new File(getTempDir(), "second.cfg");
    Files.write("second line\n", second, UTF_8);
    options.addDeadCodeReport(second.getPath());

    File third = new File(getTempDir(), "third.cfg");
    Files.write("third line\n", third, UTF_8);
    options.addDeadCodeReport(third.getPath());

    String result = Files.asCharSource(options.getProGuardUsageFile(), UTF_8).read();
    assertEquals("first line\nsecond line\nthird line\n", result);
  }
}
