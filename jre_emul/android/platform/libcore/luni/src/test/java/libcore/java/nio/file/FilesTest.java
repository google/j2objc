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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
/* J2ObjC removed: mockito unsupported
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.READ;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
/* J2ObjC removed: mockito unsupported
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
 */

public class FilesTest {

    /* J2ObjC removed: mockito unsupported
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
     */

    @Rule
    public FilesSetup filesSetup = new FilesSetup();

    /* J2ObjC removed: mockito unsupported
    @Mock
    private Path mockPath;
    @Mock
    private Path mockPath2;
    @Mock
    private FileSystem mockFileSystem;
    @Mock
    private FileSystemProvider mockFileSystemProvider;
     */

    /* J2ObjC removed: mockito unsupported
    @Before
    public void setUp() throws Exception {
        when(mockPath.getFileSystem()).thenReturn(mockFileSystem);
        when(mockPath2.getFileSystem()).thenReturn(mockFileSystem);
        when(mockFileSystem.provider()).thenReturn(mockFileSystemProvider);
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_newInputStream() throws IOException {
        try (InputStream is = new ByteArrayInputStream(new byte[0])) {

            when(mockFileSystemProvider.newInputStream(mockPath, READ)).thenReturn(is);

            assertSame(is, Files.newInputStream(mockPath, READ));

            verify(mockFileSystemProvider).newInputStream(mockPath, READ);
        }
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_newOutputStream() throws IOException {
        try (OutputStream os = new ByteArrayOutputStream()) {

            when(mockFileSystemProvider.newOutputStream(mockPath, APPEND)).thenReturn(os);

            assertSame(os, Files.newOutputStream(mockPath, APPEND));

            verify(mockFileSystemProvider).newOutputStream(mockPath, APPEND);
        }
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_newByteChannel() throws IOException {
        try (FileChannel sfc = FileChannel.open(filesSetup.getDataFilePath())) {
            HashSet<OpenOption> openOptions = new HashSet<>();
            openOptions.add(READ);

            when(mockFileSystemProvider.newByteChannel(mockPath, openOptions)).thenReturn(sfc);

            assertSame(sfc, Files.newByteChannel(mockPath, READ));

            verify(mockFileSystemProvider).newByteChannel(mockPath, openOptions);
        }
    }
     */

    @Test
    public void test_createFile() throws IOException {
        assertFalse(Files.exists(filesSetup.getTestPath()));
        Files.createFile(filesSetup.getTestPath());
        assertTrue(Files.exists(filesSetup.getTestPath()));

        // File with unicode name.
        Path unicodeFilePath = filesSetup.getPathInTestDir("परीक्षण फ़ाइल");
        Files.createFile(unicodeFilePath);
        Files.exists(unicodeFilePath);

        // When file exists.
        try {
            Files.createFile(filesSetup.getDataFilePath());
            fail();
        } catch(FileAlreadyExistsException expected) {}
    }

    @Test
    public void test_createFile_NPE() throws IOException {
        try {
            Files.createFile(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_createFile$String$Attr() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.createFile(filesSetup.getTestPath(), attr);
        assertEquals(attr.value(), Files.getAttribute(filesSetup.getTestPath(), attr.name()));

        // Creating a new file and passing multiple attribute of the same name.
        perm = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr1 = PosixFilePermissions.asFileAttribute(perm);
        Path filePath2 = filesSetup.getPathInTestDir("new_file");
        Files.createFile(filePath2, attr, attr1);
        // Value should be equal to the last attribute passed.
        assertEquals(attr1.value(), Files.getAttribute(filePath2, attr.name()));

        // When file exists.
        try {
            Files.createFile(filesSetup.getDataFilePath(), attr);
            fail();
        } catch(FileAlreadyExistsException expected) {}
    }

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_createDirectory_delegation() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        assertEquals(mockPath, Files.createDirectory(mockPath, attr));
        verify(mockFileSystemProvider).createDirectory(mockPath, attr);
    }
     */

    @Test
    public void test_createDirectories() throws IOException {
        // Should be able to create parent directories.
        Path dirPath = filesSetup.getPathInTestDir("dir1/dir2/dir3");
        assertFalse(Files.exists(dirPath));
        Files.createDirectories(dirPath);
        assertTrue(Files.isDirectory(dirPath));

        // Creating an existing directory. Should not throw any error.
        Files.createDirectories(dirPath);
    }

    @Test
    public void test_createDirectories_NPE() throws IOException {
        try {
            Files.createDirectories(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_createDirectories$Path$Attr() throws IOException {
        Path dirPath = filesSetup.getPathInTestDir("dir1/dir2/dir3");
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        assertFalse(Files.exists(dirPath));
        Files.createDirectories(dirPath, attr);
        assertEquals(attr.value(), Files.getAttribute(dirPath, attr.name()));

        // Creating an existing directory with new permissions.
        perm = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr1 =  PosixFilePermissions.asFileAttribute(perm);
        Files.createDirectories(dirPath, attr);

        // Value should not change as the directory exists.
        assertEquals(attr.value(), Files.getAttribute(dirPath, attr.name()));

        // Creating a new directory and passing multiple attribute of the same name.
        Path dirPath2 = filesSetup.getPathInTestDir("dir1/dir2/dir4");
        Files.createDirectories(dirPath2, attr, attr1);
        // Value should be equal to the last attribute passed.
        assertEquals(attr1.value(), Files.getAttribute(dirPath2, attr.name()));
    }

    @Test
    public void test_createDirectories$Path$Attr_NPE() throws IOException {
        Path dirPath = filesSetup.getPathInTestDir("dir1/dir2/dir3");
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        try {
            Files.createDirectories(null, attr);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.createDirectories(dirPath, (FileAttribute<?>[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_newDirectoryStream() throws IOException {
        // Directory setup.
        Path path_dir1 = filesSetup.getPathInTestDir("newDir1");
        Path path_dir2 = filesSetup.getPathInTestDir("newDir1/newDir2");
        Path path_dir3 = filesSetup.getPathInTestDir("newDir1/newDir3");
        Path path_file1 = filesSetup.getPathInTestDir("newDir1/newFile1");
        Path path_file2 = filesSetup.getPathInTestDir("newDir1/newFile2");
        Path path_file3 = filesSetup.getPathInTestDir("newDir1/newDir2/newFile3");

        Files.createDirectory(path_dir1);
        Files.createDirectory(path_dir2);
        Files.createDirectory(path_dir3);
        Files.createFile(path_file1);
        Files.createFile(path_file2);
        Files.createFile(path_file3);

        HashSet<Path> pathSet = new HashSet<>();
        HashSet<Path> expectedPathSet = new HashSet<>();
        expectedPathSet.add(path_dir2);
        expectedPathSet.add(path_dir3);
        expectedPathSet.add(path_file1);
        expectedPathSet.add(path_file2);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1)) {
            directoryStream.forEach(k -> pathSet.add(k));
            assertEquals(expectedPathSet, pathSet);
        }
    }

    @Test
    public void test_newDirectoryStream_Exception() throws IOException {

        // Non existent directory.
        Path path_dir1 = filesSetup.getPathInTestDir("newDir1");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // File instead of directory.
        Path path_file1 = filesSetup.getPathInTestDir("newFile1");
        Files.createFile(path_file1);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_file1)) {
            fail();
        } catch (NotDirectoryException expected) {
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(null)) {
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void test_newDirectoryStream$Path$String() throws IOException {
        // Directory setup.
        Path path_root = filesSetup.getPathInTestDir("dir");
        Path path_java1 = filesSetup.getPathInTestDir("dir/f1.java");
        Path path_java2 = filesSetup.getPathInTestDir("dir/f2.java");
        Path path_java3 = filesSetup.getPathInTestDir("dir/f3.java");

        Path path_txt1 = filesSetup.getPathInTestDir("dir/f1.txt");
        Path path_txt2 = filesSetup.getPathInTestDir("dir/f2.txt");
        Path path_txt3 = filesSetup.getPathInTestDir("dir/f3.txt");

        Files.createDirectory(path_root);
        // A directory with .java extension.
        Files.createDirectory(path_java1);
        Files.createFile(path_java2);
        Files.createFile(path_java3);
        Files.createFile(path_txt1);
        Files.createFile(path_txt2);
        Files.createFile(path_txt3);

        HashSet<Path> pathSet = new HashSet<>();
        HashSet<Path> expectedPathSet = new HashSet<>();
        expectedPathSet.add(path_java1);
        expectedPathSet.add(path_java2);
        expectedPathSet.add(path_java3);

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_root, "*.java"))
        {
            directoryStream.forEach(k -> pathSet.add(k));
            assertEquals(expectedPathSet, pathSet);
        }
    }

    @Test
    public void test_newDirectoryStream$Path$String_Exception() throws IOException {

        // Non existent directory.
        Path path_dir1 = filesSetup.getPathInTestDir("newDir1");
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1, "*.c")) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // File instead of directory.
        Path path_file1 = filesSetup.getPathInTestDir("newFile1");
        Files.createFile(path_file1);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_file1, "*.c")) {
            fail();
        } catch (NotDirectoryException expected) {
        }

        Files.createFile(path_dir1);
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_file1, "[a")) {
            fail();
        } catch (PatternSyntaxException expected) {
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(null, "[a")) {
            fail();
        } catch (NullPointerException expected) {
        }

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path_dir1,
                (String)null)) {
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_createSymbolicLink() throws IOException {
        FileAttribute mockFileAttribute = mock(FileAttribute.class);
        assertEquals(mockPath, Files.createSymbolicLink(mockPath, mockPath2, mockFileAttribute));
        verify(mockFileSystemProvider).createSymbolicLink(mockPath, mockPath2, mockFileAttribute);
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_delete() throws IOException {
        Files.delete(mockPath);
        verify(mockFileSystemProvider).delete(mockPath);
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_deleteIfExist() throws IOException {
        when(mockFileSystemProvider.deleteIfExists(mockPath)).thenReturn(true);
        assertTrue(Files.deleteIfExists(mockPath));
        verify(mockFileSystemProvider).deleteIfExists(mockPath);
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_copy() throws IOException {
        CopyOption copyOption = mock(CopyOption.class);
        Files.copy(mockPath, mockPath2, copyOption);
        verify(mockFileSystemProvider).copy(mockPath, mockPath2, copyOption);
    }
     */
}