/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.security.cert;

import java.security.cert.CertificateException;

public class CertificateException2Test extends junit.framework.TestCase {

    /**
     * java.security.cert.CertificateException#CertificateException()
     */
    public void test_Constructor() {
        // Test for method java.security.cert.CertificateException()
        try {
            if (true) {
                throw new CertificateException();
            }
            fail("Should have thrown CertificateException");
        } catch (CertificateException e) {
            assertEquals("Initializer failed : " + e.toString(),
                    "java.security.cert.CertificateException", e.toString());
        } catch (Exception e) {
            fail("Unexpected exception during test : " + e);
        }
    }

    /**
     * java.security.cert.CertificateException#CertificateException(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        // Test for method
        // java.security.cert.CertificateException(java.lang.String)
        try {
            if (true) {
                throw new CertificateException("Some error message");
            }
            fail("Should have thrown CertificateException");
        } catch (CertificateException e) {
            assertEquals("Initializer failed",
                    "Some error message", e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception during test : " + e);
        }
    }
}
