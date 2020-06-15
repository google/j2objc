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

import java.security.cert.CertificateEncodingException;

public class CertificateEncodingException2Test extends junit.framework.TestCase {

    /**
     * java.security.cert.CertificateEncodingException#CertificateEncodingException()
     */
    public void test_Constructor() {
        // Test for method java.security.cert.CertificateEncodingException()
        try {
            if (true) {
                throw new CertificateEncodingException();
            }
            fail("Should have thrown CertificateEncodingException");
        } catch (CertificateEncodingException e) {
            assertEquals("Initializer failed : " + e.toString(),
                    "java.security.cert.CertificateEncodingException",
                    e.toString());
        } catch (Exception e) {
            fail("Unexpected exception during test : " + e);
        }
    }
}
