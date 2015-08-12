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

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

/**
 * Tests for {@link Options}.
 *
 * @author tball@google.com (Tom Ball)
 */
public class OptionsTest extends GenerationTest {

  /**
   * Regression test for http://code.google.com/p/j2objc/issues/detail?id=100.
   */
  public void testPackagePrefixesWithTrailingSpace() throws IOException {
    String prefixes =
        "# Prefix mappings\n"
        + "java.lang: JL\n"
        + "foo.bar: FB \n";  // Trailing space should be ignored.
    StringReader reader = new StringReader(prefixes);
    Properties properties = new Properties();
    properties.load(reader);
    Options.addPrefixProperties(properties);
    Map<String, String> prefixMap = Options.getPackagePrefixes();
    assertEquals("JL", prefixMap.get("java.lang"));
    assertEquals("FB", prefixMap.get("foo.bar"));
  }

  public void testSourceVersionFlags() throws IOException {
    // TODO(kirbs): Uncomment following lines and lines in Options when we enable automatic version
    // detection. Currently this is breaking pulse builds using 64 bit Java 8, and upgrading to
    // Eclipse 4.5 is gated by bytecode errors in compiling junit. I won't have time to do a more in
    // depth root cause analysis on this.
    // // Check that version default is correctly pulled from system properties.
    // String javaVersion = System.getProperty("java.version");
    //
    // Options.reset();
    // Options.load(new String[] {});
    // assertEquals(javaVersion.substring(0, 3), Options.getSourceVersion());
    //
    // System.setProperty("java.version", "1.8.0_45");
    // Options.reset();
    // Options.load(new String[] {});
    // assertEquals("1.8", Options.getSourceVersion());
    //
    // System.setProperty("java.version", "1.6.0");
    // Options.reset();
    // Options.load(new String[] {});
    // assertEquals("1.6", Options.getSourceVersion());
    //
    // System.setProperty("java.version", "1.7");
    // Options.reset();
    // Options.load(new String[] {});
    // assertEquals("1.7", Options.getSourceVersion());
    //
    // // Reset the java.version property to prevent any unexpected jvm behavior after testing.
    // System.setProperty("java.version", javaVersion);

    String[] argsJavaSource = "-source 1.6".split(" ");
    Options.load(argsJavaSource);
    assertEquals("1.6", Options.getSourceVersion());

    argsJavaSource = "-source 1.7".split(" ");
    Options.load(argsJavaSource);
    assertEquals("1.7", Options.getSourceVersion());

    argsJavaSource = "-source 1.8".split(" ");
    Options.load(argsJavaSource);
    assertEquals("1.8", Options.getSourceVersion());
  }

  public void testSourceVersionFlagAliases() throws IOException {
    // Check that version aliases work correctly.
    String[] argsJavaSource = "-source 8".split(" ");
    Options.load(argsJavaSource);
    assertEquals("1.8", Options.getSourceVersion());
  }

  public void testTargetVersionFlags() throws IOException {
    String [] argsJavaTarget = "-target 1.6".split(" ");
    // Passed target should be ignored.
    Options.load(argsJavaTarget);
  }
}
