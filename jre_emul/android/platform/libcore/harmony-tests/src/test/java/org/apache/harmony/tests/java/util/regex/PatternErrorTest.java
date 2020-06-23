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

package org.apache.harmony.tests.java.util.regex;

import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 * Test boundary and error conditions in java.util.regex.Pattern
 */
@SuppressWarnings("nls")
public class PatternErrorTest extends TestCase {
    public void testCompileErrors() throws Exception {
        // null regex string - should get NullPointerException
        try {
            Pattern.compile(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        // empty regex string - no exception should be thrown
        Pattern.compile("");

        // note: invalid regex syntax checked in PatternSyntaxExceptionTest

        // flags = 0 should raise no exception
        int flags = 0;
        Pattern.compile("foo", flags);

        // check that all valid flags accepted without exception
        flags |= Pattern.UNIX_LINES;
        flags |= Pattern.CASE_INSENSITIVE;
        flags |= Pattern.MULTILINE;
        flags |= Pattern.CANON_EQ;
        flags |= Pattern.COMMENTS;
        flags |= Pattern.DOTALL;
        flags |= Pattern.UNICODE_CASE;
        flags &= ~Pattern.CANON_EQ; // Android always throws given this flag.
        Pattern.compile("foo", flags);

        // add invalid flags - should get IllegalArgumentException
        // regression test for HARMONY-4248
        flags |= 0xFFFFFFFF;
        flags &= ~Pattern.CANON_EQ; // Android always throws given this flag.
        try {
            Pattern.compile("foo", flags);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException e) {
            // This is the expected exception
        }
    }
}
