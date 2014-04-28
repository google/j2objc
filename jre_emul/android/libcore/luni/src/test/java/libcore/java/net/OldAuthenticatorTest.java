/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package libcore.java.net;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.UnknownHostException;
import junit.framework.TestCase;

public class OldAuthenticatorTest extends TestCase {

    public void test_setDefault() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        PasswordAuthentication  pa = Authenticator.requestPasswordAuthentication(
                addr, 8080, "http", "promt", "HTTP");
        assertNull(pa);

        MockAuthenticator mock = new MockAuthenticator();
        Authenticator.setDefault(mock);

        addr = InetAddress.getLocalHost();
        pa = Authenticator.requestPasswordAuthentication(addr, 80, "http", "promt", "HTTP");
        assertNull(pa);

        Authenticator.setDefault(null);
    }

    public void test_Constructor() {
        MockAuthenticator ma = new MockAuthenticator();
        assertNull(ma.getRequestingURL());
        assertNull(ma.getRequestorType());
    }

    public void test_getPasswordAuthentication() {
        MockAuthenticator ma = new MockAuthenticator();
        assertNull(ma.getPasswordAuthentication());
    }

    class MockAuthenticator extends Authenticator {
        public URL getRequestingURL() {
            return super.getRequestingURL();
        }

        public Authenticator.RequestorType getRequestorType() {
            return super.getRequestorType();
        }

        public PasswordAuthentication getPasswordAuthentication() {
            return super.getPasswordAuthentication();
        }
    }
}
