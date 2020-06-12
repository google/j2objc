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

package com.google.j2objc.crypto;

import com.google.j2objc.TestUtil;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Returns a suite of all javax.crypto tests.
 */
public class CryptoTests {

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.crypto.tests.javax.crypto"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.crypto.tests.javax.crypto.func"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.crypto.tests.javax.crypto.interfaces"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.crypto.tests.javax.crypto.spec"));
        suite.addTest(TestUtil.getPackageTests("org.apache.harmony.crypto.tests.javax.crypto.serialization"));
        suite.addTest(TestUtil.getPackageTests("libcore.javax.crypto"));
        suite.addTest(TestUtil.getPackageTests("libcore.javax.crypto.spec"));
        suite.addTest(TestUtil.getPackageTests("com.android.org.conscrypt.javax.crypto"));
        return suite;
    }

}
