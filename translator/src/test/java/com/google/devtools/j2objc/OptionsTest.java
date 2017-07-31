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

import com.google.devtools.j2objc.util.SourceVersion;

import java.io.IOException;

/**
 * Tests for {@link Options}.
 *
 * @author tball@google.com (Tom Ball)
 */
public class OptionsTest extends GenerationTest {

  public void testSourceVersionFlags() throws IOException {
     // Check that version default is correctly pulled from system properties.
     String javaVersion = System.getProperty("java.specification.version");

     options = new Options();
     options.load(new String[] {});
     assertEquals(javaVersion.substring(0, 3), options.getSourceVersion().toString());

     System.setProperty("java.specification.version", "1.8");
     options = new Options();
     options.load(new String[] {});
     assertEquals(SourceVersion.JAVA_8, options.getSourceVersion());

     System.setProperty("java.specification.version", "1.6");
     options = new Options();
     options.load(new String[] {});
     assertEquals(SourceVersion.JAVA_6, options.getSourceVersion());

     System.setProperty("java.specification.version", "1.7");
     options = new Options();
     options.load(new String[] {});
     assertEquals(SourceVersion.JAVA_7, options.getSourceVersion());

     // Reset the java.version property to prevent any unexpected jvm behavior after testing.
     System.setProperty("java.specification.version", javaVersion);

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
}
