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

package com.google.j2objc;

import junit.framework.TestCase;

import java.lang.reflect.Constructor;

/**
 * Additional tests for java.lang.String support.
 *
 * @author Keith Stanger
 */
public class StringTest extends TestCase {

  // Regression for Issue #751.
  public void testRegexReplace() {
    assertEquals("103456789", "000103456789".replaceFirst("^0+(?!$)", ""));
    assertEquals("103456789", "000103456789".replaceAll("^0+(?!$)", ""));
  }

  public void testReflectOnStringConstructors() throws Exception {
    Constructor<String> c;

    c = String.class.getDeclaredConstructor();
    assertNotNull(c);
    assertEquals("", c.newInstance());

    c = String.class.getDeclaredConstructor(char[].class);
    assertNotNull(c);
    assertEquals("foo", c.newInstance(new char[] { 'f', 'o', 'o' }));

    c = String.class.getDeclaredConstructor(char[].class, int.class, int.class);
    assertNotNull(c);
    assertEquals("bar", c.newInstance(new char[] { 'f', 'o', 'o', 'b', 'a', 'r' }, 3, 3));
  }
}
