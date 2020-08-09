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

import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class provides test cases to libcore.java.nio.file.PathsTest#test_get_URI,
 * libcore.java.nio.file.PathsTest#test_get_String,
 * libcore.java.nio.file.LinuxFileSystemTest#test_getPath
 */
class LinuxFileSystemTestData {
    static List<TestData> getPathInputOutputTestData() {
        List<TestData> inputOutputTestCases = new ArrayList<>();
        inputOutputTestCases.add(new TestData("d1", "d1"));
        inputOutputTestCases.add(new TestData("", ""));
        inputOutputTestCases.add(new TestData("/", "//"));
        inputOutputTestCases.add(new TestData("d1/d2/d3", "d1//d2/d3"));
        inputOutputTestCases.add(new TestData("d1/d2", "d1", "", "d2"));
        inputOutputTestCases.add(new TestData("foo", "", "foo"));

        // If the name separator is "/" and getPath("/foo","bar","gus") is invoked, then the path
        // string "/foo/bar/gus" is converted to a Path.
        inputOutputTestCases.add(new TestData("/foo/bar/gus", "/foo", "bar", "gus"));
        return inputOutputTestCases;
    }

    static List<TestData> getPathExceptionTestData() {
        List<TestData> exceptionTestCases = new ArrayList<>();
        exceptionTestCases.add(new TestData(InvalidPathException.class, "'\u0000'"));
        exceptionTestCases.add(new TestData(NullPointerException.class, null));
        return exceptionTestCases;
    }

    static List<TestData> getPath_URI_InputOutputTestData() {
        // As of today, there is only one installed provider - LinuxFileSystemProvider and
        // only scheme supported by it is "file".
        List<TestData> inputOutputTestCases = new ArrayList<>();
        inputOutputTestCases.add(new TestData("/d1", "file:///d1"));
        inputOutputTestCases.add(new TestData("/", "file:///"));
        inputOutputTestCases.add(new TestData("/d1//d2/d3", "file:///d1//d2/d3"));
        return inputOutputTestCases;
    }

    static List<TestData> getPath_URI_ExceptionTestData() {
        List<TestData> exceptionTestCases = new ArrayList<>();
        exceptionTestCases.add(new TestData(IllegalArgumentException.class, "d1"));
        exceptionTestCases.add(new TestData(FileSystemNotFoundException.class, "scheme://d"));
        exceptionTestCases.add(new TestData(NullPointerException.class, null));
        exceptionTestCases.add(new TestData(IllegalArgumentException.class, "file:///d#row=4"));
        exceptionTestCases.add(new TestData(IllegalArgumentException.class, "file:///d?q=5"));
        exceptionTestCases.add(new TestData(IllegalArgumentException.class, "file://d:5000"));
        return exceptionTestCases;
    }

    static class TestData {
        public String output;
        public String input;
        public String[] inputArray;
        public Class exceptionClass;

        TestData(String output, String input, String... inputArray) {
            this.output = output;
            this.input = input;
            this.inputArray = inputArray;
        }

        TestData(Class exceptionClass, String input, String... inputArray) {
            this.exceptionClass = exceptionClass;
            this.input = input;
            this.inputArray = inputArray;
        }
    }
}