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
import java.util.regex.PatternSyntaxException;

import junit.framework.TestCase;

/**
 * TODO Type description
 */
@SuppressWarnings("nls")
public class PatternSyntaxExceptionTest extends TestCase {
    public void testCase() {
        String regex = "(";
        try {
            Pattern.compile(regex);
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
            assertEquals(regex, e.getPattern());
        }
    }

    public void testCase2() {
        String regex = "[4-";
        try {
            Pattern.compile(regex);
            fail("PatternSyntaxException expected");
        } catch (PatternSyntaxException e) {
            assertEquals(regex, e.getPattern());
        }
    }
}
