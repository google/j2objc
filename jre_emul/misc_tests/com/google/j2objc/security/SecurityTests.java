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

package com.google.j2objc.security;

import com.google.j2objc.TestUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Returns a suite of all java.security tests.
 */
public class SecurityTests {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.tests.javax.security"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.tests.javax.security.auth"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.tests.javax.security.auth.callback"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.tests.javax.security.auth.x500"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.tests.javax.security.cert"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.security.tests.java.security"));
        suite.addTest(TestUtil.getPackageTests("tests.targets.security"));
        suite.addTest(TestUtil.getPackageTests("com.android.org.conscrypt.java.security"));
        suite.addTest(TestUtil.getPackageTests("tests.java.security"));
        suite.addTest(TestUtil.getPackageTests("tests.security"));
        suite.addTest(TestUtil.getPackageTests("tests.security.cert"));
        suite.addTest(TestUtil.getPackageTests("tests.security.interfaces"));
        suite.addTest(TestUtil.getPackageTests("tests.security.spec"));
        suite.addTest(TestUtil.getPackageTests("libcore.java.security"));
        suite.addTest(TestUtil.getPackageTests("libcore.java.security.cert"));
        suite.addTest(TestUtil.getPackageTests("libcore.javax.security.auth.x500"));
        return suite;
    }

}
