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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;
import com.google.devtools.j2objc.file.JarredInputFile;

import java.io.File;
import java.io.IOException;

/**
 * Unit tests for {@link FileUtil}.
 *
 * @author Tom Ball
 */
public class FileUtilTest extends GenerationTest {

  // Verify that source can be read from a jar file. Reading from
  // files doesn't need testing, since j2objc itself can't build
  // without being able to do so.
  public void testReadJarSource() throws IOException {
    File file = new File(getResourceAsFile("example.jar"));
    JarredInputFile jarEntry = new JarredInputFile(
        file.getPath(), "com/google/test/package-info.java");
    String source = options.fileUtil().readFile(jarEntry);
    assertTrue(source.contains("package com.google.test;"));
  }

  // Verify that source exists in a jar file.
  public void testJarSourceExists() throws IOException {
    File file = new File(getResourceAsFile("example.jar"));
    JarredInputFile jarEntry = new JarredInputFile(
        file.getPath(), "com/google/test/package-info.java");
    assertTrue(jarEntry.exists());
  }
}
