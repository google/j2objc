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

package libcore.java.io;

import java.io.IOException;
import java.io.StringReader;

public class OldStringReaderTest extends junit.framework.TestCase {

    String testString = "This is a test string";

    StringReader sr;

    public void test_markI() throws IOException {
        sr = new StringReader(testString);
        try {
            sr.mark(-1);
            fail("IllegalArgumentException not thrown!");
        } catch (IllegalArgumentException e) {
        }
    }

    public void test_read$CII() throws Exception {
        char[] buf = new char[testString.length()];
        sr = new StringReader(testString);
        try {
            sr.read(buf, 0, -1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            sr.read(buf, -1, 1);
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
        try {
            sr.read(buf, 1, testString.length());
            fail("IndexOutOfBoundsException was not thrown");
        } catch (IndexOutOfBoundsException e) {
            // Expected
        }
    }

    protected void tearDown() {

        try {
            sr.close();
        } catch (Exception e) {
        }
    }
}
