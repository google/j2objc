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

import java.io.IOException;
import java.net.URL;

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
    URL jarURL = getClass().getResource("example.jar");
    assertNotNull("example.jar test resource not found", jarURL);
    String exampleURL = String.format("jar:%s!/com/google/test/package-info.java", jarURL);
    String source = FileUtil.readSource(exampleURL);
    assertTrue(source.contains("package com.google.test;"));
  }

  // Verify that source exists in a jar file.
  public void testJarSourceExists() throws IOException {
    URL jarURL = getClass().getResource("example.jar");
    assertNotNull("example.jar test resource not found", jarURL);
    String exampleURL = String.format("jar:%s!/com/google/test/Example.java", jarURL);
    assertTrue(FileUtil.exists(exampleURL));
  }
}
