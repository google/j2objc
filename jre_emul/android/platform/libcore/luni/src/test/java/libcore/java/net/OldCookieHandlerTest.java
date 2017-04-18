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

import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import junit.framework.TestCase;
import tests.support.Support_Configuration;

public class OldCookieHandlerTest extends TestCase {

    public void test_CookieHandler() {
        assertNull(CookieHandler.getDefault());
    }

    public void test_get_put() throws Exception {
        MockCookieHandler mch = new MockCookieHandler();
        CookieHandler defaultHandler = CookieHandler.getDefault();
        try {
            CookieHandler.setDefault(mch);

            MockWebServer server = new MockWebServer();
            server.play();
            server.enqueue(new MockResponse().addHeader("Set-Cookie2: a=\"android\"; "
                    + "Comment=\"this cookie is delicious\"; "
                    + "CommentURL=\"http://google.com/\"; "
                    + "Discard; "
                    + "Domain=\"" + server.getCookieDomain() + "\"; "
                    + "Max-Age=\"60\"; "
                    + "Path=\"/path\"; "
                    + "Port=\"80,443," + server.getPort() + "\"; "
                    + "Secure; "
                    + "Version=\"1\""));

            URLConnection connection = server.getUrl("/path/foo").openConnection();
            connection.getContent();

            assertTrue(mch.wasGetCalled());
            assertTrue(mch.wasPutCalled());
        } finally {
            CookieHandler.setDefault(defaultHandler);
        }
    }

    private static class MockCookieHandler extends CookieHandler {
        private boolean getCalled = false;
        private boolean putCalled = false;

        public Map get(URI uri, Map requestHeaders) throws IOException {
            getCalled = true;
            return requestHeaders;
        }

        public void put(URI uri, Map responseHeaders) throws IOException {
            putCalled = true;
        }

        public boolean wasGetCalled() {
            return getCalled;
        }
        public boolean wasPutCalled() {
            return putCalled;
        }
    }
}
