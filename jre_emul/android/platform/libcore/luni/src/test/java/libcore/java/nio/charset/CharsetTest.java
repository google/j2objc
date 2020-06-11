/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.nio.charset;

import java.nio.charset.Charset;

public class CharsetTest extends junit.framework.TestCase {

    public void test_nonstandardCharsetName() {
        Charset cs = Charset.forName("UTF8");
        assertNotNull(cs);
        assertEquals("UTF-8", cs.name());

        // Make sure that caching correctly handles nonstandard name.
        // http://b/26457745
        assertNotNull(Charset.forName("UTF8"));
  }

    public static class CheckerThread extends Thread {
        private final String name;
        private boolean failed = false;

        CheckerThread(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            for (int i = 0; i < 1000; ++i) {
                Charset cs = Charset.forName(name);
                if (!name.equals(cs.name())) {
                    failed = true;
                }
            }
        }
    }

    // http://b/26548702
    public void test_multiThreadedForName() throws Exception {
        CheckerThread[] threads = new CheckerThread[10];
        for (int i = 0; i < 10; ++i) {
            threads[i] = new CheckerThread((i % 2 == 0) ? "UTF-8" : "ISO-8859-1");
        }

        for (CheckerThread ct : threads) {
            ct.start();
        }

        for (CheckerThread ct : threads) {
            ct.join();
        }

        for (CheckerThread ct : threads) {
            assertTrue(!ct.failed);
        }
    }
}
