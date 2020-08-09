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
 * limitations under the License
 */

package libcore.java.nio.file;


import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import static libcore.java.nio.file.LinuxFileSystemTestData.*;
import static libcore.java.nio.file.LinuxFileSystemTestData.getPathInputOutputTestData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class PathsTest {

    @Test
    public void test_get_String() {
        List<TestData> inputOutputTestCases = getPathInputOutputTestData();
        for (TestData inputOutputTestCase : inputOutputTestCases) {
            assertEquals(inputOutputTestCase.output, Paths.get(inputOutputTestCase.input,
                    inputOutputTestCase.inputArray).toString());
        }

        List<TestData> exceptionTestCases = getPathExceptionTestData();
        for (TestData exceptionTestCase : exceptionTestCases) {
            try {
                Paths.get(exceptionTestCase.input, exceptionTestCase.inputArray);
                fail();
            } catch (Exception expected) {
                assertEquals(exceptionTestCase.exceptionClass, expected.getClass());
            }
        }
    }

    @Test
    public void test_get_URI() throws URISyntaxException {
        List<TestData> inputOutputTestCases = getPath_URI_InputOutputTestData();
        for (TestData inputOutputTestCase : inputOutputTestCases) {
            assertEquals(inputOutputTestCase.output, Paths.get(new URI(inputOutputTestCase.input)).
                    toString());
        }

        List<TestData> exceptionTestCases = getPath_URI_ExceptionTestData();
        for (TestData exceptionTestCase : exceptionTestCases) {
            try {
                System.out.println(exceptionTestCase.input);
                Paths.get(new URI(exceptionTestCase.input));
                fail();
            } catch (Exception expected) {
                assertEquals(exceptionTestCase.exceptionClass, expected.getClass());
            }
        }
    }
}