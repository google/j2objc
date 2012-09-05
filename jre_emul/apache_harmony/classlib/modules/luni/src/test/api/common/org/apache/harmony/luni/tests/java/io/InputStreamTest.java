/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.luni.tests.java.io;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

public class InputStreamTest extends TestCase {
    // Regression for HARMONY-4337
    public void test1() throws IOException {
        try {
            InputStream in = new MockInputStream();
            in.read(null, -1, 1);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
            // expected
        }
    }

    class MockInputStream extends InputStream {
        public int read() throws IOException {
            return 0;
        }
    }
}
