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

package com.google.j2objc.nio;

import com.google.j2objc.TestUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Returns a suite of all java.nio.charset tests.
 */
public class NioTests {

  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(TestUtil.getPackageTests("com.google.j2objc.nio"));
    suite.addTest(TestUtil.getPackageTests("com.google.j2objc.nio.charset"));
    suite.addTest(TestUtil.getPackageTests("org.apache.harmony.tests.java.nio.channels"));
    suite.addTest(TestUtil.getPackageTests("org.apache.harmony.tests.java.nio.charset"));
    return suite;
  }

}
