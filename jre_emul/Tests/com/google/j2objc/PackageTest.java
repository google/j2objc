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

import java.lang.annotation.Annotation;

/**
 * Command-line test for java.lang.Package annotations.
 *
 * @author Tom Ball
 */
public class PackageTest extends TestCase {

  // Verify package-info.java is annotated with TestAnnotation.
  public void testPackageAnnotation() throws Exception {
    Package pkg = PackageTest.class.getPackage();
    Annotation[] annotations = pkg.getAnnotations();
    assertEquals(1, annotations.length);
    assertTrue(annotations[0] instanceof TestAnnotation);
  }
}
