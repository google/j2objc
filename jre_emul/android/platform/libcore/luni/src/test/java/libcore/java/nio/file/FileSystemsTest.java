/*
 * Copyright (C) 2017 The Android Open Source Project
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

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.util.HashMap;
import java.util.Map;

import libcore.io.Streams;

/* J2ObjC removed: unsupported
import dalvik.system.PathClassLoader;
import junitparams.JUnitParamsRunner;
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/* J2ObjC removed: junitparams unsupported
@RunWith(JUnitParamsRunner.class)
 */
public class FileSystemsTest {

    @Rule
    public FilesSetup filesSetup = new FilesSetup();

    @Test
    public void test_getDefault() {
        FileSystem fs = FileSystems.getDefault();
        assertNotNull(fs.provider());
    }

    @Test
    public void test_getFileSystem() {
        Path testPath = Paths.get("/");
        FileSystem fs = FileSystems.getFileSystem(testPath.toUri());
        assertNotNull(fs.provider());

        try {
            FileSystems.getFileSystem(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_newFileSystem$URI$Map() throws IOException {
        Path testPath = Paths.get("/");
        Map<String, String> stubEnv = new HashMap<>();
        try {
            FileSystems.newFileSystem(testPath.toUri(), stubEnv);
            fail();
        } catch (FileSystemAlreadyExistsException expected) {}

        try {
            FileSystems.newFileSystem(null, stubEnv);
            fail();
        } catch (NullPointerException expected) {}

        try {
            FileSystems.newFileSystem(testPath, null);
            fail();
        } catch (ProviderNotFoundException expected) {}
    }

    @Test
    public void test_newFileSystem$URI$Map$ClassLoader() throws Exception {
        Path testPath = Paths.get("/");
        Map<String, String> stubEnv = new HashMap<>();
        try {
            FileSystems.newFileSystem(testPath.toUri(), stubEnv, getClass().getClassLoader());
            fail();
        } catch (FileSystemAlreadyExistsException expected) {}

        try {
            FileSystems.newFileSystem(null, stubEnv,
                    Thread.currentThread().getContextClassLoader());
            fail();
        } catch (NullPointerException expected) {}

        try {
            FileSystems.newFileSystem(testPath.toUri(), null,
                    Thread.currentThread().getContextClassLoader());
            fail();
        } catch (FileSystemAlreadyExistsException expected) {}

        try {
            FileSystems.newFileSystem(testPath.toUri(), stubEnv, null);
            fail();
        } catch (FileSystemAlreadyExistsException expected) {}
    }

    /* J2ObjC removed: PathClassLoader unsupported
    @Test
    public void test_newFileSystem$URI$Map$ClassLoader_customClassLoader() throws Exception {
        Map<String, String> stubEnv = new HashMap<>();
        // Verify that the Thread's classloader cannot load mypackage.MockFileSystem.
        try {
            Thread.currentThread().getContextClassLoader().loadClass("mypackage.MockFileSystem");
            fail();
        } catch (ClassNotFoundException expected) {}

        ClassLoader fileSystemsClassLoader = createClassLoaderForTestFileSystems();

        // The file system configured in filesystemstest.jar is for scheme "stubScheme://
        URI stubURI = new URI("stubScheme://sometext");
        FileSystem fs = FileSystems.newFileSystem(stubURI, stubEnv, fileSystemsClassLoader);
        assertEquals("mypackage.MockFileSystem", fs.getClass().getName());
        assertSame(stubURI, fs.getClass().getDeclaredMethod("getURI").invoke(fs));
        assertSame(stubEnv, fs.getClass().getDeclaredMethod("getEnv").invoke(fs));
    }
     */

    @Test
    public void test_newFileSystem$Path$ClassLoader() throws Exception {
        Path testPath = Paths.get("/");
        try {
            FileSystems.newFileSystem(testPath, Thread.currentThread().getContextClassLoader());
            fail();
        } catch (ProviderNotFoundException expected) {}

        try {
            FileSystems.newFileSystem(null, Thread.currentThread().getContextClassLoader());
            fail();
        } catch (NullPointerException expected) {}

        try {
            FileSystems.newFileSystem(testPath, null);
            fail();
        } catch (ProviderNotFoundException expected) {}
    }

    /* J2ObjC removed: PathClassLoader unsupported
    @Test
    public void test_newFileSystem$Path$ClassLoader_customClassLoader() throws Exception  {
        // Verify that the Thread's classloader cannot load mypackage.MockFileSystem.
        try {
            Thread.currentThread().getContextClassLoader().loadClass(
                    "mypackage.MockFileSystem");
            fail();
        } catch (ClassNotFoundException expected) {}

        ClassLoader fileSystemsClassLoader = createClassLoaderForTestFileSystems();
        FileSystem fs = FileSystems.newFileSystem(filesSetup.getDataFilePath(),
                fileSystemsClassLoader);

        assertEquals("mypackage.MockFileSystem", fs.getClass().getName());

        Path pathValue = (Path)fs.getClass().getDeclaredMethod("getPath").invoke(fs);
        assertEquals(filesSetup.getDataFilePath(), pathValue);
    }
     */

    /**
     * The method creates a custom classloader for the mock FileSystem and FileSystemProvider
     * classes. The custom classloader is created by providing filesystemtest.jar which contains
     * MockFileSystemProvider and MockFileSystem classes.
     * @throws Exception
     */
    /* J2ObjC removed: PathClassLoader unsupported
    ClassLoader createClassLoaderForTestFileSystems() throws IOException {
        File jarFile = new File(filesSetup.getTestDir(), "filesystemstest.jar");
        try (InputStream in = getClass().getResource("/filesystemstest.jar").openStream();
             OutputStream out = new FileOutputStream(jarFile))
        {
            Streams.copy(in, out);
        }

        return new PathClassLoader(jarFile.getAbsolutePath(), getClass().getClassLoader());
    }
     */
}
