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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import junit.framework.TestSuite;
import org.junit.runners.Suite;

/**
 * Utility methods for JRE unit tests.
 */
public class TestUtil {

  private static final String ALLJRETESTS_NOT_ACCESSIBLE = "AllJreTests not accessible.";

  public static TestSuite getPackageTests(String pkgName) {
    if (pkgName == null || pkgName.isEmpty()) {
      throw new IllegalArgumentException("package name not specified");
    }
    Class<?>[] allJreTests = null;
    try {
      Class<?> allJreTestsClass = Class.forName("AllJreTests");
      Annotation a = allJreTestsClass.getAnnotation(Suite.SuiteClasses.class);
      if (a == null) {
        throw new AssertionError(ALLJRETESTS_NOT_ACCESSIBLE);
      }
      Method valueAccessor = Suite.SuiteClasses.class.getDeclaredMethod("value");
      allJreTests = (Class<?>[]) valueAccessor.invoke(a);
    } catch (Exception e) {
      throw new AssertionError(ALLJRETESTS_NOT_ACCESSIBLE);
    }

    TestSuite packageTests = new TestSuite();
    for (Class<?> jreTest : allJreTests) {
      Package testPackage = jreTest.getPackage();
      if (testPackage != null && testPackage.getName().equals(pkgName) && !isSuiteClass(jreTest)) {
        packageTests.addTest(new TestSuite(jreTest));
      }
    }
    return packageTests;
  }

  private static boolean isSuiteClass(Class<?> cls) {
    if (cls.getSuperclass().equals(TestSuite.class)) {
      return true;
    }
    return cls.getAnnotation(Suite.SuiteClasses.class) != null;
  }
}
