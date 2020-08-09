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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sun.nio.fs.MacOSXFileSystemProvider;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import static libcore.java.nio.file.LinuxFileSystemTestData.getPathExceptionTestData;
import static libcore.java.nio.file.LinuxFileSystemTestData.getPathInputOutputTestData;

@RunWith(JUnit4.class)
public class MacOSXFileSystemTest {

    FileSystem fileSystem = FileSystems.getDefault();
    @Test
    public void test_provider() {
        assertTrue(fileSystem.provider() instanceof MacOSXFileSystemProvider);
    }


    @Test
    public void test_isOpen() throws IOException {
        assertTrue(fileSystem.isOpen());
    }

    @Test
    public void test_close() throws IOException {
        // Close is not supported.
        try {
            fileSystem.close();
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    @Test
    public void test_isReadOnly() {
        assertFalse(fileSystem.isReadOnly());
    }

    @Test
    public void test_getSeparator() {
        assertEquals("/", fileSystem.getSeparator());
    }

    @Test
    public void test_getRootDirectories() {
        Iterable<Path> rootDirectories = fileSystem.getRootDirectories();
        Map<Path, Boolean> pathMap = new HashMap<>();
        rootDirectories.forEach(path -> pathMap.put(path, true));
        assertEquals(1, pathMap.size());
        assertTrue(pathMap.get(Paths.get("/")));
    }

    @Test
    public void test_getFileStores() {
        Iterable<FileStore> fileStores = fileSystem.getFileStores();
        // Asserting if the the list has non zero number stores.
        assertTrue(fileStores.iterator().hasNext());
    }

    @Test
    public void test_supportedFileAttributeViews() {
        Set<String> supportedFileAttributeViewsList = fileSystem.supportedFileAttributeViews();
        /* Attribute user and dos not supported in MacOSXFileSystem
        assertEquals(6, supportedFileAttributeViewsList.size());
         */
        assertEquals(4, supportedFileAttributeViewsList.size());
        assertTrue(supportedFileAttributeViewsList.contains("posix"));
//        assertTrue(supportedFileAttributeViewsList.contains("user"));
        assertTrue(supportedFileAttributeViewsList.contains("owner"));
        assertTrue(supportedFileAttributeViewsList.contains("unix"));
        assertTrue(supportedFileAttributeViewsList.contains("basic"));
//        assertTrue(supportedFileAttributeViewsList.contains("dos"));
    }

    @Test
    public void test_get() {
        List<LinuxFileSystemTestData.TestData> inputOutputTestCases = getPathInputOutputTestData();
        for (LinuxFileSystemTestData.TestData inputOutputTestCase : inputOutputTestCases) {
            Assert.assertEquals(inputOutputTestCase.output, fileSystem.getPath(
                    inputOutputTestCase.input, inputOutputTestCase.inputArray).toString());
        }

        List<LinuxFileSystemTestData.TestData> exceptionTestCases = getPathExceptionTestData();
        for (LinuxFileSystemTestData.TestData exceptionTestCase : exceptionTestCases) {
            try {
                fileSystem.getPath(exceptionTestCase.input, exceptionTestCase.inputArray);
                Assert.fail();
            } catch (Exception expected) {
                Assert.assertEquals(exceptionTestCase.exceptionClass, expected.getClass());
            }
        }
    }

    @Test
    public void test_getPathMatcher_glob() {
        PathMatcher pathMatcher = fileSystem.getPathMatcher("glob:" + "*.java");
        assertTrue(pathMatcher.matches(Paths.get("f.java")));
        assertFalse(pathMatcher.matches(Paths.get("f")));

        pathMatcher = fileSystem.getPathMatcher("glob:" + "*.*");
        assertTrue(pathMatcher.matches(Paths.get("f.t")));
        assertFalse(pathMatcher.matches(Paths.get("f")));

        pathMatcher = fileSystem.getPathMatcher("glob:" + "*.{java,class}");
        assertTrue(pathMatcher.matches(Paths.get("f.java")));
        assertTrue(pathMatcher.matches(Paths.get("f.class")));
        assertFalse(pathMatcher.matches(Paths.get("f.clas")));
        assertFalse(pathMatcher.matches(Paths.get("f.t")));

        pathMatcher = fileSystem.getPathMatcher("glob:" + "f.?");
        assertTrue(pathMatcher.matches(Paths.get("f.t")));
        assertFalse(pathMatcher.matches(Paths.get("f.tl")));
        assertFalse(pathMatcher.matches(Paths.get("f.")));

        pathMatcher = fileSystem.getPathMatcher("glob:" + "/home/*/*");
        assertTrue(pathMatcher.matches(Paths.get("/home/f/d")));
        assertTrue(pathMatcher.matches(Paths.get("/home/f/*")));
        assertTrue(pathMatcher.matches(Paths.get("/home/*/*")));
        assertFalse(pathMatcher.matches(Paths.get("/home/f")));
        assertFalse(pathMatcher.matches(Paths.get("/home/f/d/d")));

        pathMatcher = fileSystem.getPathMatcher("glob:" + "/home/**");
        assertTrue(pathMatcher.matches(Paths.get("/home/f/d")));
        assertTrue(pathMatcher.matches(Paths.get("/home/f/*")));
        assertTrue(pathMatcher.matches(Paths.get("/home/*/*")));
        assertTrue(pathMatcher.matches(Paths.get("/home/f")));
        assertTrue(pathMatcher.matches(Paths.get("/home/f/d/d")));
        assertTrue(pathMatcher.matches(Paths.get("/home/f/d/d/d")));
    }

    @Test
    public void test_getPathMatcher_regex() {
        PathMatcher pathMatcher = fileSystem.getPathMatcher("regex:" + "(hello|hi)*[^a|b]?k.*");
        assertTrue(pathMatcher.matches(Paths.get("k")));
        assertTrue(pathMatcher.matches(Paths.get("ck")));
        assertFalse(pathMatcher.matches(Paths.get("ak")));
        assertTrue(pathMatcher.matches(Paths.get("kanything")));
        assertTrue(pathMatcher.matches(Paths.get("hellohik")));
        assertTrue(pathMatcher.matches(Paths.get("hellok")));
        assertTrue(pathMatcher.matches(Paths.get("hellohellohellok")));
        assertFalse(pathMatcher.matches(Paths.get("hellohellohellobk")));
        assertFalse(pathMatcher.matches(Paths.get("hello")));
    }

    @Test
    public void test_getPathMatcher_unsupported() {
        try {
            fileSystem.getPathMatcher("unsupported:test");
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    @Test
    public void test_getUserPrincipalLookupService() {
        assertNotNull(fileSystem.getUserPrincipalLookupService());
    }

    @Test
    public void test_newWatchService() throws IOException {
        assertNotNull(fileSystem.newWatchService());
    }
}