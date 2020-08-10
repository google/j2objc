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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonReadableChannelException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.CopyOption;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.SYNC;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static junit.framework.TestCase.assertTrue;
import static libcore.java.nio.file.FilesSetup.DATA_FILE;
import static libcore.java.nio.file.FilesSetup.NON_EXISTENT_FILE;
import static libcore.java.nio.file.FilesSetup.TEST_FILE_DATA;
import static libcore.java.nio.file.FilesSetup.TEST_FILE_DATA_2;
import static libcore.java.nio.file.FilesSetup.UTF_16_DATA;
/* J2ObjC removed: simple iOS version of Runtime does not support exec
import static libcore.java.nio.file.FilesSetup.execCmdAndWaitForTermination;
 */
import static libcore.java.nio.file.FilesSetup.readFromFile;
import static libcore.java.nio.file.FilesSetup.readFromInputStream;
import static libcore.java.nio.file.FilesSetup.writeToFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
 /* J2ObjC removed: mockito unsupported
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
  */

public class Files2Test {
    @Rule
    public FilesSetup filesSetup = new FilesSetup();
    /* J2ObjC removed: mockito unsupported
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
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
    public void test_move() throws IOException {
        CopyOption mockCopyOption = mock(CopyOption.class);
        assertEquals(mockPath2, Files.move(mockPath, mockPath2, mockCopyOption));
        verify(mockFileSystemProvider).move(mockPath, mockPath2, mockCopyOption);
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_readSymbolicLink() throws IOException {
        when(mockFileSystemProvider.readSymbolicLink(mockPath)).thenReturn(mockPath2);
        assertEquals(mockPath2, Files.readSymbolicLink(mockPath));
        verify(mockFileSystemProvider).readSymbolicLink(mockPath);
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_isSameFile() throws IOException {
        when(mockFileSystemProvider.isSameFile(mockPath, mockPath2)).thenReturn(true);
        when(mockFileSystemProvider.isSameFile(mockPath2, mockPath)).thenReturn(false);
        assertTrue(Files.isSameFile(mockPath, mockPath2));
        assertFalse(Files.isSameFile(mockPath2, mockPath));
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_getFileStore() throws IOException {
        when(mockFileSystemProvider.getFileStore(mockPath)).thenThrow(new SecurityException());
        try {
            Files.getFileStore(mockPath);
            fail();
        } catch (SecurityException expected) {
        }
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_isHidden() throws IOException {
        when(mockFileSystemProvider.isHidden(mockPath)).thenReturn(true);
        when(mockFileSystemProvider.isHidden(mockPath2)).thenReturn(false);
        assertTrue(Files.isHidden(mockPath));
        assertFalse(Files.isHidden(mockPath2));
    }
     */

    @Test
    public void test_probeContentType() throws IOException {
        assertEquals("text/plain",
                Files.probeContentType(filesSetup.getPathInTestDir("file.txt")));
        assertEquals("text/x-java",
                Files.probeContentType(filesSetup.getPathInTestDir("file.java")));
    }

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_getFileAttributeView() throws IOException {
        FileAttributeView mockFileAttributeView = mock(FileAttributeView.class);
        when(mockFileSystemProvider.getFileAttributeView(mockPath, FileAttributeView.class,
                LinkOption.NOFOLLOW_LINKS)).thenReturn(mockFileAttributeView);
        assertEquals(mockFileAttributeView, Files.getFileAttributeView(mockPath,
                FileAttributeView.class, LinkOption.NOFOLLOW_LINKS));
    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_readAttributes() throws IOException {
        BasicFileAttributes mockBasicFileAttributes = mock(BasicFileAttributes.class);
        when(mockFileSystemProvider.readAttributes(mockPath, BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS)).thenReturn(mockBasicFileAttributes);
        assertEquals(mockBasicFileAttributes, Files.readAttributes(mockPath,
                BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS));

    }
     */

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_setAttribute() throws IOException {
        assertEquals(mockPath, Files.setAttribute(mockPath, "string", 10,
                LinkOption.NOFOLLOW_LINKS));
        verify(mockFileSystemProvider).setAttribute(mockPath, "string", 10,
                LinkOption.NOFOLLOW_LINKS);
    }
     */

    @Test
    public void test_getAttribute() throws IOException {
        // Other tests are covered in test_readAttributes.
        // When file is NON_EXISTENT.
        try {
            Files.getAttribute(filesSetup.getTestPath(), "basic:lastModifiedTime");
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_getAttribute_Exception() throws IOException {
        // IllegalArgumentException
        try {
            Files.getAttribute(filesSetup.getDataFilePath(), "xyz");
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            Files.getAttribute(null, "xyz");
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.getAttribute(filesSetup.getDataFilePath(), null);
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_getPosixFilePermissions() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.createFile(filesSetup.getTestPath(), attr);
        assertEquals(attr.value(), Files.getPosixFilePermissions(filesSetup.getTestPath()));
    }

    @Test
    public void test_getPosixFilePermissions_NPE() throws IOException {
        try {
            Files.getPosixFilePermissions(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_setPosixFilePermissions() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertEquals(attr.value(), Files.getPosixFilePermissions(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_setPosixFilePermissions_NPE() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        try {
            Files.setPosixFilePermissions(null, perm);
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.setPosixFilePermissions(filesSetup.getDataFilePath(), null);
            fail();
        } catch(NullPointerException expected) {}
    }

    /* J2ObjC removed: simple iOS version of Runtime does not support exec
    @Test
    public void test_getOwner() throws IOException, InterruptedException {
        String[] statCmd = { "stat", "-c", "%U", filesSetup.getTestDir() + "/" + DATA_FILE };
        Process statProcess = execCmdAndWaitForTermination(statCmd);
        String owner = readFromInputStream(statProcess.getInputStream()).trim();
        assertEquals(owner, Files.getOwner(filesSetup.getDataFilePath()).getName());
    }
     */

    @Test
    public void test_getOwner_NPE() throws IOException, InterruptedException {
        try {
            Files.getOwner(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    /* J2ObjC removed: simple iOS version of Runtime does not support exec
    @Test
    public void test_isSymbolicLink() throws IOException, InterruptedException {
        assertFalse(Files.isSymbolicLink(filesSetup.getTestPath()));
        assertFalse(Files.isSymbolicLink(filesSetup.getDataFilePath()));

        // Creating a symbolic link.
        String[] symLinkCmd = { "ln", "-s", DATA_FILE,
                filesSetup.getTestDir() + "/" + NON_EXISTENT_FILE };
        execCmdAndWaitForTermination(symLinkCmd);
        assertTrue(Files.isSymbolicLink(filesSetup.getTestPath()));
    }
     */

    @Test
    public void test_isSymbolicLink_NPE() throws IOException, InterruptedException {
        try {
            Files.isSymbolicLink(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    /* J2ObjC removed: simple iOS version of Runtime does not support exec
    @Test
    public void test_isDirectory() throws IOException, InterruptedException {
        assertFalse(Files.isDirectory(filesSetup.getDataFilePath()));
        // When file doesn't exist.
        assertFalse(Files.isDirectory(filesSetup.getTestPath()));

        // Creating a directory.
        String dirName = "newDir";
        Path dirPath = filesSetup.getPathInTestDir(dirName);
        String mkdir[] = { "mkdir", filesSetup.getTestDir() + "/" + dirName };
        execCmdAndWaitForTermination(mkdir);
        assertTrue(Files.isDirectory(dirPath));
    }
     */

    @Test
    public void test_isDirectory_NPE() throws IOException {
        try {
            Files.isDirectory(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isRegularFile() throws IOException, InterruptedException {
        assertTrue(Files.isRegularFile(filesSetup.getDataFilePath()));
        // When file doesn't exist.
        assertFalse(Files.isRegularFile(filesSetup.getTestPath()));

        // Check directories.
        Path dirPath = filesSetup.getPathInTestDir("dir");
        Files.createDirectory(dirPath);
        assertFalse(Files.isRegularFile(dirPath));

        // Check symbolic link.
        // When linked to itself.
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getTestPath().toAbsolutePath());
        assertFalse(Files.isRegularFile(filesSetup.getTestPath()));

        // When linked to some other file.
        filesSetup.reset();
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getDataFilePath().toAbsolutePath());
        assertTrue(Files.isRegularFile(filesSetup.getTestPath()));

        // When asked to not follow the link.
        assertFalse(Files.isRegularFile(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));

        // Device file.
        Path deviceFilePath = Paths.get("/dev/null");
        assertTrue(Files.exists(deviceFilePath));
        assertFalse(Files.isRegularFile(deviceFilePath));
    }

    @Test
    public void test_isRegularFile_NPE() throws IOException {
        try {
            Files.isReadable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    /* J2ObjC removed: simple iOS version of Runtime does not support exec
    @Test
    public void test_getLastModifiedTime() throws IOException, InterruptedException {
        String touchCmd[] = { "touch", "-d", "2015-10-09T00:00:00Z",
                filesSetup.getTestDir() + "/" + DATA_FILE };
        execCmdAndWaitForTermination(touchCmd);
        assertEquals("2015-10-09T00:00:00Z",
                Files.getLastModifiedTime(filesSetup.getDataFilePath()).toString());

        // Non existent file.
        try {
            Files.getLastModifiedTime(filesSetup.getTestPath()).toString();
            fail();
        } catch (NoSuchFileException expected) {}
    }
     */

    @Test
    public void test_getLastModifiedTime_NPE() throws IOException {
        try {
            Files.getLastModifiedTime(null, LinkOption.NOFOLLOW_LINKS);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.getLastModifiedTime(filesSetup.getDataFilePath(), (LinkOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_setLastModifiedTime() throws IOException, InterruptedException {
        long timeInMillisToBeSet = System.currentTimeMillis() - 10000;
        Files.setLastModifiedTime(filesSetup.getDataFilePath(),
                FileTime.fromMillis(timeInMillisToBeSet));
        assertEquals(timeInMillisToBeSet/1000,
                Files.getLastModifiedTime(filesSetup.getDataFilePath()).to(TimeUnit.SECONDS));

        // Non existent file.
        try {
            Files.setLastModifiedTime(filesSetup.getTestPath(),
                    FileTime.fromMillis(timeInMillisToBeSet));
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_setLastModifiedTime_NPE() throws IOException, InterruptedException {
        try {
            Files.setLastModifiedTime(null, FileTime.fromMillis(System.currentTimeMillis()));
            fail();
        } catch (NullPointerException expected) {}

        // No NullPointerException.
        Files.setLastModifiedTime(filesSetup.getDataFilePath(), null);
    }

    /* J2ObjC removed: simple iOS version of Runtime does not support exec
    @Test
    public void test_size() throws IOException, InterruptedException {
        int testSizeInBytes = 5000;
        String ddCmd[] = { "dd", "if=/dev/zero", "of=" + filesSetup.getTestDir() + "/" + DATA_FILE,
                "bs="
                + testSizeInBytes, "count=1"};
        execCmdAndWaitForTermination(ddCmd);

        assertEquals(testSizeInBytes, Files.size(filesSetup.getDataFilePath()));

        try {
            Files.size(filesSetup.getTestPath());
            fail();
        } catch (NoSuchFileException expected) {}
    }
     */

    @Test
    public void test_size_NPE() throws IOException, InterruptedException {
        try {
            Files.size(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_exists() throws IOException {
        // When file exists.
        assertTrue(Files.exists(filesSetup.getDataFilePath()));

        // When file doesn't exist.
        assertFalse(Files.exists(filesSetup.getTestPath()));

        // SymLink
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getDataFilePath().toAbsolutePath());
        assertTrue(Files.exists(filesSetup.getTestPath()));

        // When link shouldn't be followed
        assertTrue(Files.exists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));

        // When the target file doesn't exist.
        Files.delete(filesSetup.getDataFilePath());
        assertTrue(Files.exists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
        assertFalse(Files.exists(filesSetup.getTestPath()));

        // Symlink to itself
        filesSetup.reset();
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getTestPath().toAbsolutePath());
        assertFalse(Files.exists(filesSetup.getTestPath()));
        assertTrue(Files.exists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void test_exists_NPE() throws IOException {
        try {
            Files.exists(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_notExists() throws IOException {
        // When file exists.
        assertFalse(Files.notExists(filesSetup.getDataFilePath()));

        // When file doesn't exist.
        assertTrue(Files.notExists(filesSetup.getTestPath()));

        // SymLink
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getDataFilePath().toAbsolutePath());
        assertFalse(Files.notExists(filesSetup.getTestPath()));

        // When link shouldn't be followed
        assertFalse(Files.notExists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));

        // When the target file doesn't exist.
        Files.delete(filesSetup.getDataFilePath());
        assertFalse(Files.notExists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
        assertTrue(Files.notExists(filesSetup.getTestPath()));

        // Symlink to itself
        filesSetup.reset();
        Files.createSymbolicLink(filesSetup.getTestPath(),
                filesSetup.getTestPath().toAbsolutePath());
        assertFalse(Files.notExists(filesSetup.getTestPath()));
        assertFalse(Files.notExists(filesSetup.getTestPath(), LinkOption.NOFOLLOW_LINKS));
    }

    @Test
    public void test_notExists_NPE() throws IOException {
        try {
            Files.notExists(null);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.notExists(filesSetup.getDataFilePath(), (LinkOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isReadable() throws IOException {
        // When a readable file is available.
        assertTrue(Files.isReadable(filesSetup.getDataFilePath()));

        // When a file doesn't exist.
        assertFalse(Files.isReadable(filesSetup.getTestPath()));

        // Setting non readable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("-wxrwxrwx");
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertFalse(Files.isReadable(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_isReadable_NPE() throws IOException {
        try {
            Files.isReadable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isWritable() throws IOException {
        // When a readable file is available.
        assertTrue(Files.isWritable(filesSetup.getDataFilePath()));

        // When a file doesn't exist.
        assertFalse(Files.isWritable(filesSetup.getTestPath()));

        // Setting non writable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("r-xrwxrwx");
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertFalse(Files.isWritable(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_isWritable_NPE() {
        try {
            Files.isWritable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isExecutable() throws IOException {
        // When a readable file is available.
        assertFalse(Files.isExecutable(filesSetup.getDataFilePath()));

        // When a file doesn't exist.
        assertFalse(Files.isExecutable(filesSetup.getTestPath()));

        // Setting non executable permission for user
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rw-rwxrwx");
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        assertFalse(Files.isExecutable(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_isExecutable_NPE() {
        try {
            Files.isExecutable(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_walkFileTree$Path$Set$int$FileVisitor_symbolicLinkFollow()
            throws IOException, InterruptedException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   └── dir2 ─ dir3-file1 - file3
        //        │
        //        └── file2
        //
        // With follow link it should be able to traverse to dir3 and file1 when started from file2.

        // Directory setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir1/dir2");
        Path dir3 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3");
        Path file1 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3/file1");
        Path file2 = filesSetup.getPathInTestDir("root/file2");

        Files.createDirectories(dir3);
        Files.createFile(file1);
        Files.createSymbolicLink(file2, dir2.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(file2));

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Map<Object, VisitOption> expectedDirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(file2, option, 50, new TestFileVisitor(dirMap, option));

        expectedDirMap.put(file1.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(file2.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir3.getFileName(), VisitOption.POST_VISIT_DIRECTORY);

        assertEquals(expectedDirMap, dirMap);
    }

    @Test
    public void test_walkFileTree$Path$FileVisitor() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //

        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir1/dir2");
        Path dir3 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3");
        Path dir4 = filesSetup.getPathInTestDir("root/dir1/dir4");
        Path dir5 = filesSetup.getPathInTestDir("root/dir5");
        Path file1 = filesSetup.getPathInTestDir("root/file1");
        Path file3 = filesSetup.getPathInTestDir("root/dir1/file3");
        Path file5 = filesSetup.getPathInTestDir("root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file3);
        Files.createFile(file5);
        Files.createSymbolicLink(file1, filesSetup.getDataFilePath().toAbsolutePath());

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Map<Object, VisitOption> expectedDirMap = new HashMap<>();
        Path returnedPath = Files.walkFileTree(rootDir, new Files2Test.TestFileVisitor(dirMap));

        assertEquals(rootDir, returnedPath);

        expectedDirMap.put(rootDir.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir1.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir2.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir3.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file5.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir4.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file3.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir5.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file1.getFileName(), VisitOption.VISIT_FILE);
        assertEquals(expectedDirMap, dirMap);
    }

    @Test
    public void test_walkFileTree_depthFirst() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        ├── dir1 ── file1
        //        └── dir2 ── file2

        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir2");
        Path file1 = filesSetup.getPathInTestDir("root/dir1/file1");
        Path file2 = filesSetup.getPathInTestDir("root/dir2/file2");

        Files.createDirectories(dir1);
        Files.createDirectories(dir2);
        Files.createFile(file1);
        Files.createFile(file2);

        Map<Object, VisitOption> dirMap = new HashMap<>();
        List<Object> keyList = new ArrayList<>();
        Files.walkFileTree(rootDir,
                new Files2Test.TestFileVisitor(dirMap, keyList));
        assertEquals(rootDir.getFileName(), keyList.get(0));
        if (keyList.get(1).equals(dir1.getFileName())) {
            assertEquals(file1.getFileName(), keyList.get(2));
            assertEquals(dir2.getFileName(), keyList.get(3));
            assertEquals(file2.getFileName(), keyList.get(4));
        } else if (keyList.get(1).equals(dir2.getFileName())){
            assertEquals(file2.getFileName(), keyList.get(2));
            assertEquals(dir1.getFileName(), keyList.get(3));
            assertEquals(file1.getFileName(), keyList.get(4));
        } else {
            fail();
        }
    }

    @Test
    public void test_walkFileTree_negativeDepth() throws IOException {
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");

        Files.createDirectories(dir1);

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        try {
            Files.walkFileTree(rootDir, option, -1,
                    new Files2Test.TestFileVisitor(dirMap));
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_walkFileTree_maximumDepth() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //
        // depth will be 2. file5, dir3 is not reachable.
        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path dir2 = filesSetup.getPathInTestDir("root/dir1/dir2");
        Path dir3 = filesSetup.getPathInTestDir("root/dir1/dir2/dir3");
        Path dir4 = filesSetup.getPathInTestDir("root/dir1/dir4");
        Path dir5 = filesSetup.getPathInTestDir("root/dir5");
        Path file1 = filesSetup.getPathInTestDir("root/file1");
        Path file3 = filesSetup.getPathInTestDir("root/dir1/file3");
        Path file5 = filesSetup.getPathInTestDir("root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Map<Object, VisitOption> expectedDirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        Files.walkFileTree(rootDir, option, 2, new Files2Test.TestFileVisitor(dirMap));
        assertTrue(Files.isDirectory(dir4));
        expectedDirMap.put(rootDir.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(dir1.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        // Both of the directories are at maximum depth, therefore, will be treated as simple file.
        expectedDirMap.put(dir2.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir4.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(dir5.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
        expectedDirMap.put(file1.getFileName(), VisitOption.VISIT_FILE);
        expectedDirMap.put(file3.getFileName(), VisitOption.VISIT_FILE);

        assertEquals(expectedDirMap, dirMap);
    }

    @Test
    public void test_walkFileTree$Path$FileVisitor_NPE() throws IOException {
        Path rootDir = filesSetup.getPathInTestDir("root");
        try {
            Files.walkFileTree(null,
                    new Files2Test.TestFileVisitor(new HashMap<>()));
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.walkFileTree(rootDir, null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_walkFileTree$Path$FileVisitor_FileSystemLoopException() throws IOException {
        // Directory structure.
        //    .
        //    ├── DATA_FILE
        //    └── root
        //        └── dir1
        //             └── file1
        //
        // file1 is symlink to dir1

        // Directory Setup.
        Path rootDir = filesSetup.getPathInTestDir("root");
        Path dir1 = filesSetup.getPathInTestDir("root/dir1");
        Path file1 = filesSetup.getPathInTestDir("root/dir1/file1");

        Files.createDirectories(dir1);
        Files.createSymbolicLink(file1, dir1.toAbsolutePath());
        assertEquals(dir1.getFileName(), Files.readSymbolicLink(file1).getFileName());

        Map<Object, VisitOption> dirMap = new HashMap<>();
        Set<FileVisitOption> option = new HashSet<>();
        option.add(FileVisitOption.FOLLOW_LINKS);
        try {
            Files.walkFileTree(rootDir, option, Integer.MAX_VALUE,
                    new Files2Test.TestFileVisitor(dirMap));
            fail();
        } catch (FileSystemLoopException expected) {}
    }

    @Test
    public void test_find() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //

        // Directory setup.
        Path rootDir = Paths.get(filesSetup.getTestDir(), "root");
        Path dir1 = Paths.get(filesSetup.getTestDir(), "root/dir1");
        Path dir2 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2");
        Path dir3 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir4");
        Path dir5 = Paths.get(filesSetup.getTestDir(), "root/dir5");
        Path file1 = Paths.get(filesSetup.getTestDir(), "root/file1");
        Path file3 = Paths.get(filesSetup.getTestDir(), "root/dir1/file3");
        Path file5 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        // When depth is 2 then file4, file5 and dir3 are not reachable.
        Set<Path> expectedDirSet = new HashSet<>();
        expectedDirSet.add(rootDir);
        expectedDirSet.add(dir1);
        expectedDirSet.add(dir2);
        expectedDirSet.add(dir4);
        expectedDirSet.add(dir5);
        Set<Path> dirSet = new HashSet<>();
        Stream<Path> pathStream = Files.find(rootDir, 2, (path, attr) -> Files.isDirectory(path));
        pathStream.forEach(path -> dirSet.add(path));
        assertEquals(expectedDirSet, dirSet);

        // Test the case where depth is 0.
        expectedDirSet.clear();
        dirSet.clear();

        expectedDirSet.add(rootDir);

        pathStream = Files.find(rootDir, 0, (path, attr) -> Files.isDirectory(path));
        pathStream.forEach(path -> dirSet.add(path));
        assertEquals(expectedDirSet, dirSet);

        // Test the case where depth is -1.
        try {
            Files.find(rootDir, -1, (path, attr) -> Files.isDirectory(path));
            fail();
        } catch (IllegalArgumentException expected) {}

        // Test the case when BiPredicate always returns false.
        expectedDirSet.clear();
        dirSet.clear();

        pathStream = Files.find(rootDir, 2, (path, attr) -> false);
        pathStream.forEach(path -> dirSet.add(path));
        assertEquals(expectedDirSet, dirSet);

        // Test the case when start is not a directory.
        expectedDirSet.clear();
        dirSet.clear();

        expectedDirSet.add(file1);

        pathStream = Files.find(file1, 2, (path, attr) -> true);
        pathStream.forEach(path -> dirSet.add(path));
        assertEquals(expectedDirSet, dirSet);
    }

    @Test
    public void test_find_NPE() throws IOException {
        Path rootDir = Paths.get(filesSetup.getTestDir(), "root");
        Files.createDirectories(rootDir);
        try {
            Files.find(null, 2, (path, attr) -> Files.isDirectory(path));
            fail();
        } catch(NullPointerException expected) {}

        try {
            Files.find(rootDir, (Integer)null, (path, attr) -> Files.isDirectory(path));
            fail();
        } catch(NullPointerException expected) {}

        try(Stream<Path> pathStream = Files.find(rootDir, 2, null)) {
            pathStream.forEach(path -> {/* do nothing */});
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_lines$Path$Charset() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(UTF_16_DATA);
        lines.add(TEST_FILE_DATA);
        Files.write(filesSetup.getDataFilePath(), lines, StandardCharsets.UTF_16);
        try (Stream<String> readLines = Files.lines(filesSetup.getDataFilePath(),
                StandardCharsets.UTF_16)) {
            Iterator<String> lineIterator = lines.iterator();
            readLines.forEach(line -> assertEquals(line, lineIterator.next()));
        }

        // When Path is a directory
        filesSetup.reset();
        try (Stream<String> readLines = Files.lines(filesSetup.getTestDirPath(),
                StandardCharsets.UTF_16)) {
            try {
                readLines.count();
                fail();
            } catch (UncheckedIOException expected) {}
        }

        // When file doesn't exits.
        filesSetup.reset();
        try (Stream<String> readLines = Files.lines(filesSetup.getTestPath(),
                StandardCharsets.UTF_16)) {
           fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_lines$Path$Charset_NPE() throws IOException {
        try {
            Files.lines(null, StandardCharsets.UTF_16);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.lines(filesSetup.getDataFilePath(), null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_lines$Path() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(TEST_FILE_DATA_2);
        lines.add(TEST_FILE_DATA);
        Files.write(filesSetup.getDataFilePath(), lines, StandardCharsets.UTF_8);
        try (Stream<String> readLines = Files.lines(filesSetup.getDataFilePath())) {
            Iterator<String> lineIterator = lines.iterator();
            readLines.forEach(line -> assertEquals(line, lineIterator.next()));
        }

        // When Path is a directory
        filesSetup.reset();
        try (Stream<String> readLines = Files.lines(filesSetup.getTestDirPath())) {
            try {
                readLines.count();
                fail();
            } catch (UncheckedIOException expected) {}
        }

        // When file doesn't exits.
        filesSetup.reset();
        try (Stream<String> readLines = Files.lines(filesSetup.getTestPath())) {
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_line$Path_NPE() throws IOException {
        try {
            Files.lines(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_list() throws Exception {
        // Directory Setup for the test.
        Path rootDir = Paths.get(filesSetup.getTestDir(), "root");
        Path dir1 = Paths.get(filesSetup.getTestDir(), "root/dir1");
        Path file1 = Paths.get(filesSetup.getTestDir(), "root/file1");
        Path file2 = Paths.get(filesSetup.getTestDir(), "root/dir1/file2");
        Path symLink = Paths.get(filesSetup.getTestDir(), "root/symlink");
        Files.createDirectories(dir1);
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createSymbolicLink(symLink, file1.toAbsolutePath());

        Set<Path> expectedVisitedFiles = new HashSet<>();
        expectedVisitedFiles.add(dir1);
        expectedVisitedFiles.add(file1);
        expectedVisitedFiles.add(symLink);

        Set<Path> visitedFiles = new HashSet<>();
        try (Stream<Path> pathStream = Files.list(rootDir)) {
            pathStream.forEach(path -> visitedFiles.add(path));
        }
        assertEquals(3, visitedFiles.size());


        // Test the case where directory is empty.
        filesSetup.clearAll();
        try {
            Files.list(Paths.get(filesSetup.getTestDir(), "newDir"));
            fail();
        } catch (NoSuchFileException expected) {}

        // Test the case where path points to a file.
        filesSetup.clearAll();
        filesSetup.setUp();
        try {
            Files.list(filesSetup.getDataFilePath());
            fail();
        } catch (NotDirectoryException expected) {}
    }

    @Test
    public void test_list_NPE() throws IOException {
        try {
            Files.list(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_newBufferedReader() throws IOException {
        // Test the case where file doesn't exists.
        try {
            Files.newBufferedReader(filesSetup.getTestPath());
            fail();
        } catch (NoSuchFileException expected) {}

        BufferedReader bufferedReader = Files.newBufferedReader(filesSetup.getDataFilePath());
        assertEquals(TEST_FILE_DATA, bufferedReader.readLine());

        // Test the case where the file content has unicode characters.
        writeToFile(filesSetup.getDataFilePath(), UTF_16_DATA);
        bufferedReader = Files.newBufferedReader(filesSetup.getDataFilePath());
        assertEquals(UTF_16_DATA, bufferedReader.readLine());
        bufferedReader.close();

        // Test the case where file is write-only.
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("-w-------");
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        try {
            Files.newBufferedReader(filesSetup.getDataFilePath());
            fail();
        } catch (AccessDeniedException expected) {}
    }

    @Test
    public void test_newBufferedReader_NPE() throws IOException {
        try {
            Files.newBufferedReader(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_newBufferedReader$Path$Charset() throws IOException {
        BufferedReader bufferedReader = Files.newBufferedReader(filesSetup.getDataFilePath(),
                StandardCharsets.US_ASCII);
        assertEquals(TEST_FILE_DATA, bufferedReader.readLine());

        // When the file has unicode characters.
        writeToFile(filesSetup.getDataFilePath(), UTF_16_DATA);
        bufferedReader = Files.newBufferedReader(filesSetup.getDataFilePath(),
                StandardCharsets.US_ASCII);
        try {
            bufferedReader.readLine();
            fail();
        } catch (MalformedInputException expected) {}
    }

    @Test
    public void test_newBufferedReader$Path$Charset_NPE() throws IOException {
        try {
            Files.newBufferedReader(null, StandardCharsets.US_ASCII);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.newBufferedReader(filesSetup.getDataFilePath(), null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_newBufferedWriter() throws IOException {
        BufferedWriter bufferedWriter = Files.newBufferedWriter(filesSetup.getTestPath());
        bufferedWriter.write(TEST_FILE_DATA);
        bufferedWriter.close();
        assertEquals(TEST_FILE_DATA,
                readFromFile(filesSetup.getTestPath()));

        // When file exists, it should start writing from the beginning.
        bufferedWriter = Files.newBufferedWriter(filesSetup.getDataFilePath());
        bufferedWriter.write(TEST_FILE_DATA_2);
        bufferedWriter.close();
        assertEquals(TEST_FILE_DATA_2,
                readFromFile(filesSetup.getDataFilePath()));

        // When file is read-only.
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("r--------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);
        Files.setPosixFilePermissions(filesSetup.getDataFilePath(), perm);
        try {
            Files.newBufferedWriter(filesSetup.getDataFilePath());
            fail();
        } catch (AccessDeniedException expected) {}
    }

    @Test
    public void test_newBufferedWriter_NPE() throws IOException {
        try {
            Files.newBufferedWriter(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_newBufferedWriter$Path$Charset() throws IOException {
        BufferedWriter bufferedWriter = Files.newBufferedWriter(filesSetup.getTestPath(),
                StandardCharsets.US_ASCII);
        bufferedWriter.write(TEST_FILE_DATA);
        bufferedWriter.close();
        assertEquals(TEST_FILE_DATA, readFromFile(filesSetup.getTestPath()));
    }

    @Test
    public void test_newBufferedWriter$Path$Charset_NPE() throws IOException {
        try {
            Files.newBufferedWriter(null, StandardCharsets.US_ASCII);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.newBufferedWriter(filesSetup.getTestPath(), (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_newByteChannel() throws IOException {
        // When file doesn't exist
        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getTestPath())) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // When file exists.

        // File opens in READ mode by default. The channel is non writable by default.
        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getDataFilePath())) {
            sbc.write(ByteBuffer.allocate(10));
            fail();
        } catch (NonWritableChannelException expected) {
        }

        // Read a file.
        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getDataFilePath())) {
            ByteBuffer readBuffer = ByteBuffer.allocate(10);
            int bytesReadCount = sbc.read(readBuffer);

            String readData = new String(Arrays.copyOf(readBuffer.array(), bytesReadCount),
                    StandardCharsets.UTF_8);
            assertEquals(TEST_FILE_DATA, readData);
        }
    }

    @Test
    public void test_newByteChannel_openOption_WRITE() throws IOException {
        // When file doesn't exist
        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getTestPath(), WRITE)) {
            fail();
        } catch (NoSuchFileException expected) {
        }

        // When file exists.

        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getDataFilePath(), WRITE)) {
            sbc.read(ByteBuffer.allocate(10));
            fail();
        } catch (NonReadableChannelException expected) {
        }

        // Write in file.
        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getDataFilePath(), WRITE)) {
            sbc.write(ByteBuffer.wrap(TEST_FILE_DATA_2.getBytes()));
            sbc.close();

            try (InputStream is = Files.newInputStream(filesSetup.getDataFilePath())) {
                String expectedFileData = TEST_FILE_DATA_2 +
                        TEST_FILE_DATA.substring(
                                TEST_FILE_DATA_2.length());
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }
    }

    @Test
    public void test_newByteChannel_openOption_WRITE_READ() throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getDataFilePath(), WRITE,
                READ, SYNC/* Sync makes sure the that InputStream is able to read content written by
                 the seekable byte channel without closing/flushing it. */)) {
            ByteBuffer readBuffer = ByteBuffer.allocate(10);
            int bytesReadCount = sbc.read(readBuffer);

            String readData = new String(Arrays.copyOf(readBuffer.array(), bytesReadCount),
                    StandardCharsets.UTF_8);
            assertEquals(TEST_FILE_DATA, readData);

            // Pointer will move to the end of the file after read operation. The write should
            // append the data at the end of the file.
            sbc.write(ByteBuffer.wrap(TEST_FILE_DATA_2.getBytes()));
            try (InputStream is = Files.newInputStream(filesSetup.getDataFilePath())) {
                String expectedFileData = TEST_FILE_DATA + TEST_FILE_DATA_2;
                assertEquals(expectedFileData, readFromInputStream(is));
            }
        }
    }

    @Test
    public void test_newByteChannel_NPE() throws IOException {
        try (SeekableByteChannel sbc = Files.newByteChannel(null)) {
            fail();
        } catch(NullPointerException expected) {}

        try (SeekableByteChannel sbc = Files.newByteChannel(filesSetup.getDataFilePath(),
                (OpenOption[]) null)) {
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_readAllLine() throws IOException {
        // Multi-line file.
        assertTrue(Files.exists(filesSetup.getDataFilePath()));
        writeToFile(filesSetup.getDataFilePath(), "\n" + TEST_FILE_DATA_2,
                APPEND);
        List<String> out = Files.readAllLines(filesSetup.getDataFilePath());
        assertEquals(2, out.size());
        assertEquals(TEST_FILE_DATA, out.get(0));
        assertEquals(TEST_FILE_DATA_2, out.get(1));

        // When file doesn't exist.
        filesSetup.reset();
        try {
            Files.readAllLines(filesSetup.getTestPath());
            fail();
        } catch (NoSuchFileException expected) {}

        // When file is a directory.
        filesSetup.reset();
        try {
            Files.readAllLines(filesSetup.getTestDirPath());
            fail();
        } catch (IOException expected) {}
    }

    @Test
    public void test_readAllLine_NPE() throws IOException {
        try {
            Files.readAllLines(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_readAllLine$Path$Charset() throws IOException {
        assertTrue(Files.exists(filesSetup.getDataFilePath()));
        writeToFile(filesSetup.getDataFilePath(), "\n" + TEST_FILE_DATA_2, APPEND);
        List<String> out = Files.readAllLines(filesSetup.getDataFilePath(), StandardCharsets.UTF_8);
        assertEquals(2, out.size());
        assertEquals(TEST_FILE_DATA, out.get(0));
        assertEquals(TEST_FILE_DATA_2, out.get(1));

        // With UTF-16.
        out = Files.readAllLines(filesSetup.getDataFilePath(), StandardCharsets.UTF_16);
        assertEquals(1, out.size());

        // UTF-8 data read as UTF-16
        String expectedOutput = new String((TEST_FILE_DATA + '\n' + TEST_FILE_DATA_2).getBytes(),
                StandardCharsets.UTF_16);
        assertEquals(expectedOutput, out.get(0));

        // When file doesn't exist.
        filesSetup.reset();
        try {
            Files.readAllLines(filesSetup.getTestPath(), StandardCharsets.UTF_16);
            fail();
        } catch (NoSuchFileException expected) {}

        // When file is a directory.
        filesSetup.reset();
        try {
            Files.readAllLines(filesSetup.getTestDirPath(), StandardCharsets.UTF_16);
            fail();
        } catch (IOException expected) {}
    }

    @Test
    public void test_readAllLine$Path$Charset_NPE() throws IOException {
        try {
            Files.readAllLines(null, StandardCharsets.UTF_16);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.readAllLines(filesSetup.getDataFilePath(), null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_walk$Path$FileVisitOption() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //
        // depth will be 2. file4, file5, dir3 is not reachable.

        Path rootDir = Paths.get(filesSetup.getTestDir(), "root");
        Path dir1 = Paths.get(filesSetup.getTestDir(), "root/dir1");
        Path dir2 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2");
        Path dir3 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir4");
        Path dir5 = Paths.get(filesSetup.getTestDir(), "root/dir5");
        Path file1 = Paths.get(filesSetup.getTestDir(), "root/file1");
        Path file3 = Paths.get(filesSetup.getTestDir(), "root/dir1/file3");
        Path file5 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Set<Path> expectedDirSet = new HashSet<>();
        expectedDirSet.add(rootDir);
        expectedDirSet.add(dir1);
        expectedDirSet.add(dir2);
        expectedDirSet.add(dir4);
        expectedDirSet.add(file3);
        expectedDirSet.add(dir5);
        expectedDirSet.add(file1);

        Set<Path> dirSet = new HashSet<>();
        try(Stream<Path> pathStream = Files.walk(rootDir, 2,
                FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> dirSet.add(path));
        }

        assertEquals(expectedDirSet, dirSet);

        // Test case when Path doesn't exist.
        try (Stream<Path> pathStream = Files.walk(filesSetup.getTestPath(), 2,
                FileVisitOption.FOLLOW_LINKS)){
            fail();
        } catch (NoSuchFileException expected) {}

        // Test case when Path is a not a directory.
        expectedDirSet.clear();
        dirSet.clear();
        expectedDirSet.add(filesSetup.getDataFilePath());
        try (Stream<Path> pathStream = Files.walk(filesSetup.getDataFilePath(), 2,
                FileVisitOption.FOLLOW_LINKS)){
            pathStream.forEach(path -> dirSet.add(path));
        }
        assertEquals(expectedDirSet, dirSet);

        // Test case when Path doesn't exist.
        try (Stream<Path> pathStream = Files.walk(rootDir, -1, FileVisitOption.FOLLOW_LINKS)){
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_walk_FileSystemLoopException() throws IOException {
        // Directory structure.
        //        root
        //        └── dir1
        //            └── file1
        //
        // file1 is symbolic link to dir1

        Path rootDir = Paths.get(filesSetup.getTestDir(), "root");
        Path dir1 = Paths.get(filesSetup.getTestDir(), "root/dir");
        Path file1 = Paths.get(filesSetup.getTestDir(), "root/dir/file1");
        Files.createDirectories(dir1);
        Files.createSymbolicLink(file1, dir1.toAbsolutePath());
        assertTrue(Files.isSymbolicLink(file1));
        try(Stream<Path> pathStream = Files.walk(rootDir, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> assertNotNull(path));
            fail();
        } catch (UncheckedIOException expected) {
            assertTrue(expected.getCause() instanceof FileSystemLoopException);
        }
    }

    @Test
    public void test_walk() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   ├── dir2
        //        │   │   ├── dir3
        //        │   │   └── file5
        //        │   ├── dir4
        //        │   └── file3
        //        ├── dir5
        //        └── file1
        //

        Path rootDir = Paths.get(filesSetup.getTestDir(), "root");
        Path dir1 = Paths.get(filesSetup.getTestDir(), "root/dir1");
        Path dir2 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2");
        Path dir3 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2/dir3");
        Path dir4 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir4");
        Path dir5 = Paths.get(filesSetup.getTestDir(), "root/dir5");
        Path file1 = Paths.get(filesSetup.getTestDir(), "root/file1");
        Path file3 = Paths.get(filesSetup.getTestDir(), "root/dir1/file3");
        Path file5 = Paths.get(filesSetup.getTestDir(), "root/dir1/dir2/file5");

        Files.createDirectories(dir3);
        Files.createDirectories(dir4);
        Files.createDirectories(dir5);
        Files.createFile(file1);
        Files.createFile(file3);
        Files.createFile(file5);

        Set<Path> expectedDirSet = new HashSet<>();
        expectedDirSet.add(rootDir.getFileName());
        expectedDirSet.add(dir1.getFileName());
        expectedDirSet.add(dir2.getFileName());
        expectedDirSet.add(dir4.getFileName());
        expectedDirSet.add(file3.getFileName());
        expectedDirSet.add(dir5.getFileName());
        expectedDirSet.add(file1.getFileName());
        expectedDirSet.add(file5.getFileName());
        expectedDirSet.add(dir3.getFileName());

        Set<Path> dirSet = new HashSet<>();
        try (Stream<Path> pathStream = Files.walk(rootDir)) {
            pathStream.forEach(path -> dirSet.add(path.getFileName()));
        }

        assertEquals(expectedDirSet, dirSet);


        // Test case when Path doesn't exist.
        try (Stream<Path> pathStream = Files.walk(filesSetup.getTestPath())){
            fail();
        } catch (NoSuchFileException expected) {}

        // Test case when Path is a not a directory.
        expectedDirSet.clear();
        dirSet.clear();
        expectedDirSet.add(filesSetup.getDataFilePath());
        try (Stream<Path> pathStream = Files.walk(filesSetup.getDataFilePath())) {
            pathStream.forEach(path -> dirSet.add(path));
        }
        assertEquals(expectedDirSet, dirSet);
    }

    @Test
    public void test_walk_depthFirst() throws IOException {
        // Directory structure.
        //        root
        //        ├── dir1
        //        │   └── file1
        //        └── dir2
        //            └── file2
        //

        Path rootDir = Paths.get(filesSetup.getTestDir(), "root");
        Path dir1 = Paths.get(filesSetup.getTestDir(), "root/dir1");
        Path file1 = Paths.get(filesSetup.getTestDir(), "root/dir1/file1");
        Path dir2 = Paths.get(filesSetup.getTestDir(), "root/dir2");
        Path file2 = Paths.get(filesSetup.getTestDir(), "root/dir2/file2");
        Files.createDirectories(dir1);
        Files.createDirectories(dir2);
        Files.createFile(file1);
        Files.createFile(file2);
        List<Object> fileKeyList = new ArrayList<>();
        try(Stream<Path> pathStream = Files.walk(rootDir, FileVisitOption.FOLLOW_LINKS)) {
            pathStream.forEach(path -> fileKeyList.add(path.getFileName()));
        }
        assertEquals(rootDir.getFileName(), fileKeyList.get(0));
        if (fileKeyList.get(1).equals(dir1.getFileName())) {
            assertEquals(file1.getFileName(), fileKeyList.get(2));
            assertEquals(dir2.getFileName(), fileKeyList.get(3));
            assertEquals(file2.getFileName(), fileKeyList.get(4));
        } else if (fileKeyList.get(1).equals(dir2.getFileName())) {
            assertEquals(file2.getFileName(), fileKeyList.get(2));
            assertEquals(dir1.getFileName(), fileKeyList.get(3));
            assertEquals(file1.getFileName(), fileKeyList.get(4));
        } else {
            fail();
        }
    }

    @Test
    public void test_walk$Path$Int$LinkOption_IllegalArgumentException() throws IOException {
        Map<Path, Boolean> dirMap = new HashMap<>();
        Path rootDir = Paths.get(filesSetup.getTestDir(), "rootDir");
        try (Stream<Path> pathStream = Files.walk(rootDir, -1,
                FileVisitOption.FOLLOW_LINKS)) {
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_walk$Path$FileVisitOption_NPE() throws IOException {
        try {
            Files.walk(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_write$Path$byte$OpenOption() throws IOException {
        Files.write(filesSetup.getDataFilePath(), TEST_FILE_DATA_2.getBytes());
        assertEquals(TEST_FILE_DATA_2, readFromFile(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_write$Path$byte$OpenOption_OpenOption() throws IOException {
        Files.write(filesSetup.getTestPath(), TEST_FILE_DATA_2.getBytes(), CREATE_NEW);
        assertEquals(TEST_FILE_DATA_2, readFromFile(filesSetup.getTestPath()));

        filesSetup.reset();
        Files.write(filesSetup.getDataFilePath(), TEST_FILE_DATA_2.getBytes(), TRUNCATE_EXISTING);
        assertEquals(TEST_FILE_DATA_2, readFromFile(filesSetup.getDataFilePath()));

        filesSetup.reset();
        Files.write(filesSetup.getDataFilePath(), TEST_FILE_DATA_2.getBytes(), APPEND);
        assertEquals(TEST_FILE_DATA + TEST_FILE_DATA_2, readFromFile(
                filesSetup.getDataFilePath()));

        filesSetup.reset();
        try {
            Files.write(filesSetup.getDataFilePath(), TEST_FILE_DATA_2.getBytes(), READ);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_write$Path$byte$OpenOption_NPE() throws IOException {
        try {
            Files.write(null, TEST_FILE_DATA_2.getBytes(), CREATE_NEW);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(filesSetup.getTestPath(), (byte[]) null, CREATE_NEW);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(filesSetup.getTestPath(), TEST_FILE_DATA_2.getBytes(), (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_write$Path$Iterable$Charset$OpenOption() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(TEST_FILE_DATA_2);
        lines.add(TEST_FILE_DATA);
        Files.write(filesSetup.getDataFilePath(), lines, StandardCharsets.UTF_16);
        List<String> readLines = Files.readAllLines(filesSetup.getDataFilePath(),
                StandardCharsets.UTF_16);
        assertEquals(readLines, lines);
    }

    @Test
    public void test_write$Path$Iterable$Charset$OpenOption_NPE() throws IOException {
        try {
            Files.write(null, new ArrayList<>(), StandardCharsets.UTF_16);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(filesSetup.getDataFilePath(), null, StandardCharsets.UTF_16);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(filesSetup.getDataFilePath(), new ArrayList<>(), (OpenOption[]) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_write$Path$Iterable$OpenOption() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(TEST_FILE_DATA_2);
        lines.add(TEST_FILE_DATA);
        Files.write(filesSetup.getDataFilePath(), lines);
        List<String> readLines = Files.readAllLines(filesSetup.getDataFilePath());
        assertEquals(readLines, lines);
    }

    @Test
    public void test_write$Path$Iterable$OpenOption_NPE() throws IOException {
        try {
            Files.write(null, new ArrayList<String>());
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.write(filesSetup.getDataFilePath(), (Iterable<CharSequence>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    // The ability for Android apps to create hard links was removed in
    // https://android-review.googlesource.com/144092 (March 2015).
    // https://b/19953790.
    /* TODO(amisail): check why this fails
    @Test
    public void test_createLink() throws IOException {
        try {
            Files.createLink(filesSetup.getTestPath(), filesSetup.getDataFilePath());
            fail();
        } catch (AccessDeniedException expected) {}
    }
     */

    @Test
    public void test_createTempDirectory$Path$String$FileAttributes() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);

        String tmpDir = "tmpDir";
        Path tmpDirPath = Files.createTempDirectory(filesSetup.getTestDirPath(), tmpDir, attr);
        assertTrue(tmpDirPath.getFileName().toString().startsWith(tmpDir));
        assertEquals(filesSetup.getTestDirPath(), tmpDirPath.getParent());
        assertTrue(Files.isDirectory(tmpDirPath));
        assertEquals(attr.value(), Files.getAttribute(tmpDirPath, attr.name()));

        filesSetup.reset();
        // Test case when prefix is null.
        tmpDirPath = Files.createTempDirectory(filesSetup.getTestDirPath(), null, attr);
        assertEquals(filesSetup.getTestDirPath(), tmpDirPath.getParent());
        assertTrue(Files.isDirectory(tmpDirPath));
        assertEquals(attr.value(), Files.getAttribute(tmpDirPath, attr.name()));

        try {
            Files.createTempDirectory(null, tmpDir, attr);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.createTempDirectory(filesSetup.getTestDirPath(), tmpDir, (FileAttribute<?>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_createTempDirectory$String$FileAttributes() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);

        Path tmpDirectoryLocation = Paths.get(System.getProperty("java.io.tmpdir"));

        String tmpDir = "tmpDir";
        Path tmpDirPath = Files.createTempDirectory(tmpDir, attr);
        assertTrue(tmpDirPath.getFileName().toString().startsWith(tmpDir));
        assertEquals(tmpDirectoryLocation, tmpDirPath.getParent());
        assertTrue(Files.isDirectory(tmpDirPath));
        assertEquals(attr.value(), Files.getAttribute(tmpDirPath, attr.name()));

        // Test case when prefix is null.
        filesSetup.reset();
        tmpDirPath = Files.createTempDirectory(null, attr);
        assertEquals(tmpDirectoryLocation, tmpDirPath.getParent());
        assertTrue(Files.isDirectory(tmpDirPath));
        assertEquals(attr.value(), Files.getAttribute(tmpDirPath, attr.name()));

        try {
            Files.createTempDirectory(tmpDir, (FileAttribute<?>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_createTempFile$Path$String$String$FileAttributes() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);

        String tmpFilePrefix = "prefix";
        String tmpFileSuffix = "suffix";

        Path tmpFilePath = Files.createTempFile(filesSetup.getTestDirPath(), tmpFilePrefix,
                tmpFileSuffix, attr);

        assertTrue(tmpFilePath.getFileName().toString().startsWith(tmpFilePrefix));
        assertTrue(tmpFilePath.getFileName().toString().endsWith(tmpFileSuffix));
        assertEquals(filesSetup.getTestDirPath(), tmpFilePath.getParent());
        assertTrue(Files.isRegularFile(tmpFilePath));
        assertEquals(attr.value(), Files.getAttribute(tmpFilePath, attr.name()));

        // Test case when prefix is null.
        filesSetup.reset();
        tmpFilePath = Files.createTempFile(filesSetup.getTestDirPath(), null,
                tmpFileSuffix, attr);
        assertTrue(tmpFilePath.getFileName().toString().endsWith(tmpFileSuffix));
        assertEquals(filesSetup.getTestDirPath(), tmpFilePath.getParent());
        assertTrue(Files.isRegularFile(tmpFilePath));
        assertEquals(attr.value(), Files.getAttribute(tmpFilePath, attr.name()));

        // Test case when suffix is null.
        filesSetup.reset();
        tmpFilePath = Files.createTempFile(filesSetup.getTestDirPath(), tmpFilePrefix,
                null, attr);
        assertTrue(tmpFilePath.getFileName().toString().startsWith(tmpFilePrefix));
        assertEquals(filesSetup.getTestDirPath(), tmpFilePath.getParent());
        assertTrue(Files.isRegularFile(tmpFilePath));
        assertEquals(attr.value(), Files.getAttribute(tmpFilePath, attr.name()));

        try {
            Files.createTempFile(null, tmpFilePrefix, tmpFileSuffix, attr);
            fail();
        } catch (NullPointerException expected) {}

        try {
            Files.createTempFile(filesSetup.getTestDirPath(), tmpFilePrefix, tmpFileSuffix,
                (FileAttribute<?>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_createTempFile$String$String$FileAttributes() throws IOException {
        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perm);

        Path tmpDirectoryLocation = Paths.get(System.getProperty(
                "java.io.tmpdir"));

        String tmpFilePrefix = "prefix";
        String tmpFileSuffix = "suffix";
        Path tmpFilePath = Files.createTempFile(tmpFilePrefix, tmpFileSuffix, attr);
        assertTrue(tmpFilePath.getFileName().toString().startsWith(tmpFilePrefix));
        assertTrue(tmpFilePath.getFileName().toString().endsWith(tmpFileSuffix));
        assertEquals(tmpDirectoryLocation, tmpFilePath.getParent());
        assertTrue(Files.isRegularFile(tmpFilePath));
        assertEquals(attr.value(), Files.getAttribute(tmpFilePath, attr.name()));

        // Test case when prefix is null.
        filesSetup.reset();
        tmpFilePath = Files.createTempFile(null, tmpFileSuffix, attr);
        assertEquals(tmpDirectoryLocation, tmpFilePath.getParent());
        assertTrue(tmpFilePath.getFileName().toString().endsWith(tmpFileSuffix));
        assertTrue(Files.isRegularFile(tmpFilePath));
        assertEquals(attr.value(), Files.getAttribute(tmpFilePath, attr.name()));

        // Test case when suffix is null.
        filesSetup.reset();
        tmpFilePath = Files.createTempFile(tmpFilePrefix, null, attr);
        assertEquals(tmpDirectoryLocation, tmpFilePath.getParent());
        assertTrue(tmpFilePath.getFileName().toString().startsWith(tmpFilePrefix));
        assertTrue(Files.isRegularFile(tmpFilePath));
        assertEquals(attr.value(), Files.getAttribute(tmpFilePath, attr.name()));

        try {
            Files.createTempFile(tmpFilePrefix, tmpFileSuffix, (FileAttribute<?>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    /* J2ObjC removed: mockito unsupported
    @Test
    public void test_newByteChannel$Path$Set_OpenOption$FileAttributes() throws Exception {
        FileAttribute stubFileAttribute = mock(FileAttribute.class);
        Set<OpenOption> stubSet = new HashSet<>();
        Files.newByteChannel(mockPath, stubSet, stubFileAttribute);

        verify(mockFileSystemProvider).newByteChannel(mockPath, stubSet, stubFileAttribute);
    }
     */

    // -- Mock Class --

    private static class TestFileVisitor implements FileVisitor<Path> {

        final Map<Object, VisitOption> dirMap;
        LinkOption option[];
        List<Object> keyList;

        public TestFileVisitor(Map<Object, VisitOption> dirMap) {
            this(dirMap, (List<Object>) null);
        }

        public TestFileVisitor(Map<Object, VisitOption> dirMap, Set<FileVisitOption> option) {
            this.dirMap = dirMap;
            for (FileVisitOption fileVisitOption : option) {
                if (fileVisitOption.equals(FileVisitOption.FOLLOW_LINKS)) {
                    this.option = new LinkOption[0];
                }
            }

            if (this.option == null) {
                this.option = new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
            }
        }

        public TestFileVisitor(Map<Object, VisitOption> dirMap, List<Object> pathList) {
            this.dirMap = dirMap;
            this.option = new LinkOption[] {LinkOption.NOFOLLOW_LINKS};
            keyList = pathList;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            if (keyList != null) {
                keyList.add(dir.getFileName());
            }
            dirMap.put(dir.getFileName(), VisitOption.PRE_VISIT_DIRECTORY);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (keyList != null) {
                keyList.add(file.getFileName());
            }
            dirMap.put(file.getFileName(), VisitOption.VISIT_FILE);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            return TERMINATE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc != null) {
                throw exc;
            }
            if (dirMap.getOrDefault(dir.getFileName(), VisitOption.UNVISITED)
                    != VisitOption.PRE_VISIT_DIRECTORY) {
                return TERMINATE;
            } else {
                dirMap.put(dir.getFileName(), VisitOption.POST_VISIT_DIRECTORY);
                return CONTINUE;
            }
        }
    }

    private enum VisitOption {
        PRE_VISIT_DIRECTORY,
        VISIT_FILE,
        POST_VISIT_DIRECTORY,
        UNVISITED,
    }
}
