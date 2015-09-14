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

import junit.framework.TestCase;

/**
 * Tests reflection support for package prefixes.
 */
public class PackagePrefixesTest extends TestCase {

  private static Class<?> getClass(String name) {
    try {
      return Class.forName(name);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public void testPackagePrefix() throws Exception {
    // Verify class exists with Java name.
    assertNotNull(getClass("bar.Third"));

    // Verify bar = BB prefix.
    assertNotNull(getClass("BBThird"));     // bar.Third
  }

  public void testPackagePrefixWildcard() throws Exception {
    // Verify classes exist with Java names.
    assertNotNull(getClass("foo.bar.First"));
    assertNotNull(getClass("foo.mumble.Second"));
    assertNotNull(getClass("foo.Fourth"));

    // Verify foo.* = FB prefix.
    assertNotNull(getClass("FBFirst"));     // foo.bar.First
    assertNotNull(getClass("FBSecond"));    // foo.mumble.Second
    assertNotNull(getClass("FBFourth"));    // foo.Fourth

    // Verify other class wasn't included in wildcard.
    assertNull(getClass("FBThird"));
  }

}
