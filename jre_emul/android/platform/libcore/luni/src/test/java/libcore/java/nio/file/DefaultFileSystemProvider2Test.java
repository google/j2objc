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
import org.junit.runner.RunWith;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonReadableChannelException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotLinkException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.spi.FileSystemProvider;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/* J2ObjC removed: junitparams not supported
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
 */

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static libcore.java.nio.file.FilesSetup.DATA_FILE;
import static libcore.java.nio.file.FilesSetup.NonStandardOption;
import static libcore.java.nio.file.FilesSetup.TEST_FILE_DATA;
import static libcore.java.nio.file.FilesSetup.TEST_FILE_DATA_2;
import static libcore.java.nio.file.FilesSetup.readFromFile;
import static libcore.java.nio.file.FilesSetup.writeToFile;
import static libcore.java.nio.file.LinuxFileSystemTestData.getPath_URI_InputOutputTestData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/* J2ObjC changed: junitparams not supported
@RunWith(JUnitParamsRunner.class)
 */
@RunWith(DataProviderRunner.class)
public class DefaultFileSystemProvider2Test {

    @Rule
    public FilesSetup filesSetup = new FilesSetup();

    private FileSystemProvider provider;

    @Before
    public void setUp() throws Exception {
        provider = filesSetup.getDataFilePath().getFileSystem().provider();
    }

    @Test
    public void test_move() throws IOException {
        provider.move(filesSetup.getDataFilePath(), filesSetup.getTestPath());
        assertEquals(TEST_FILE_DATA, readFromFile(filesSetup.getTestPath()));
        assertFalse(Files.exists(filesSetup.getDataFilePath()));

        filesSetup.reset();
        Files.createFile(filesSetup.getTestPath());
        // When target file exists.
        try {
            provider.move(filesSetup.getDataFilePath(), filesSetup.getTestPath());
            fail();
        } catch (FileAlreadyExistsException expected) {}

        // Move to existing target file with REPLACE_EXISTING copy option.
        filesSetup.reset();
        Files.createFile(filesSetup.getTestPath());
        writeToFile(filesSetup.getDataFilePath(), TEST_FILE_DATA_2);
        provider.move(filesSetup.getDataFilePath(), filesSetup.getTestPath(), REPLACE_EXISTING);
        assertEquals(TEST_FILE_DATA_2, readFromFile(filesSetup.getTestPath()));

        // Copy from a non existent file.
        filesSetup.reset();
        try {
            provider.move(filesSetup.getTestPath(), filesSetup.getDataFilePath(), REPLACE_EXISTING);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_move_CopyOption() throws IOException {
        FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        Files.setAttribute(filesSetup.getDataFilePath(), "basic:lastModifiedTime", fileTime);
        provider.move(filesSetup.getDataFilePath(), filesSetup.getTestPath());
        assertEquals(fileTime.to(TimeUnit.SECONDS),
                ((FileTime) Files.getAttribute(filesSetup.getTestPath(),
                        "basic:lastModifiedTime")).to(TimeUnit.SECONDS));
        assertEquals(TEST_FILE_DATA, readFromFile(filesSetup.getTestPath()));

        // ATOMIC_MOVE
        filesSetup.reset();
        provider.move(filesSetup.getDataFilePath(), filesSetup.getTestPath(), ATOMIC_MOVE);
        assertEquals(TEST_FILE_DATA, readFromFile(filesSetup.getTestPath()));

        filesSetup.reset();
        try {
            provider.move(filesSetup.getDataFilePath(), filesSetup.getTestPath(),
                    NonStandardOption.OPTION1);
            fail();
        } catch (UnsupportedOperationException expected) {}
    }

    @Test
    public void test_move_NPE() throws IOException {
        try {
            provider.move(null, filesSetup.getTestPath());
            fail();
        } catch(NullPointerException expected) {}

        try {
            provider.move(filesSetup.getDataFilePath(), null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            provider.move(filesSetup.getDataFilePath(), filesSetup.getTestPath(),
                    (CopyOption[]) null);
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_move_directory() throws IOException {
        Path dirPath = filesSetup.getPathInTestDir("dir1");
        final Path nestedDirPath = filesSetup.getPathInTestDir("dir1/dir");
        final Path dirPath2 = filesSetup.getPathInTestDir("dir2");

        Files.createDirectory(dirPath);
        Files.createDirectory(nestedDirPath);
        Files.copy(filesSetup.getDataFilePath(),
                filesSetup.getPathInTestDir("dir1/" + DATA_FILE));
        provider.move(dirPath, dirPath2);

        Map<Path, Boolean> pathMap = new HashMap<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dirPath2)) {
            directoryStream.forEach(file -> pathMap.put(file, true));
        }

        // The files are not copied. The command is equivalent of creating a new directory.
        assertEquals(2, pathMap.size());
        assertEquals(TEST_FILE_DATA,
                readFromFile(filesSetup.getPathInTestDir("dir2/" + DATA_FILE)));
        assertFalse(Files.exists(dirPath));

        filesSetup.reset();
    }

    @Test
    public void test_move_directory_DirectoryNotEmptyException() throws IOException {
        Path dirPath = filesSetup.getPathInTestDir("dir1");
        Path dirPath4 = filesSetup.getPathInTestDir("dir4");
        Files.createDirectory(dirPath);
        Files.createDirectory(dirPath4);
        Files.createFile(Paths.get(dirPath.toString(), DATA_FILE));
        Files.createFile(Paths.get(dirPath4.toString(), DATA_FILE));
        try {
            Files.copy(dirPath, dirPath4, REPLACE_EXISTING);
            fail();
        } catch (DirectoryNotEmptyException expected) {}
    }

    @Test
    public void test_readSymbolicLink() throws IOException {
        provider.createSymbolicLink(/* Path of the symbolic link */ filesSetup.getTestPath(),
                /* Path of the target of the symbolic link */
                filesSetup.getDataFilePath().toAbsolutePath());
        assertEquals(filesSetup.getDataFilePath().toAbsolutePath(),
                Files.readSymbolicLink(filesSetup.getTestPath()));

        // Sym link to itself
        filesSetup.reset();
        provider.createSymbolicLink(/* Path of the symbolic link */ filesSetup.getTestPath(),
                /* Path of the target of the symbolic link */
                filesSetup.getTestPath().toAbsolutePath());
        assertEquals(filesSetup.getTestPath().toAbsolutePath(),
                Files.readSymbolicLink(filesSetup.getTestPath()));

        filesSetup.reset();
        try {
            provider.readSymbolicLink(filesSetup.getDataFilePath());
            fail();
        } catch (NotLinkException expected) {
        }
    }

    @Test
    public void test_readSymbolicLink_NPE() throws IOException {
        try {
            provider.readSymbolicLink(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_isSameFile() throws IOException {
        // When both the files exists.
        assertTrue(provider.isSameFile(filesSetup.getDataFilePath(), filesSetup.getDataFilePath()));

        // When the files doesn't exist.
        assertTrue(provider.isSameFile(filesSetup.getTestPath(), filesSetup.getTestPath()));

        // With two different files.
        try {
            assertFalse(
                    provider.isSameFile(filesSetup.getDataFilePath(), filesSetup.getTestPath()));
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_isSameFile_NPE() throws IOException {
        try {
            provider.isSameFile(null, filesSetup.getDataFilePath());
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.isSameFile(filesSetup.getDataFilePath(), null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_getFileStore() throws IOException {
        try {
            provider.getFileStore(filesSetup.getDataFilePath());
            fail();
        } catch (SecurityException expected) {
        }

        try {
            provider.getFileStore(null);
            fail();
        } catch (SecurityException expected) {
        }
    }

    @Test
    public void test_isHidden() throws IOException {
        assertFalse(provider.isHidden(filesSetup.getDataFilePath()));

        // Files can't be hidden using the "dos" view, which is unsupported since it relies
        // on a custom xattr, which may or may not be available on all FSs.
        //
        // Note that this weirdly asymmetric : setting the hidden attribute uses xattrs to
        // emulate dos attributes whereas isHidden checks whether the the file name begins with a
        // leading period. <shrug>
        try {
            Files.setAttribute(filesSetup.getDataFilePath(), "dos:hidden", true);
            fail();
        } catch (UnsupportedOperationException expected) {
        }

        assertFalse(provider.isHidden(filesSetup.getDataFilePath()));
    }

    @Test
    public void test_isHidden_NPE() throws IOException {
        try {
            provider.isHidden(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_probeContentType_NPE() throws IOException {
        try {
            Files.probeContentType(null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_getFileAttributeView() throws IOException {
        BasicFileAttributeView fileAttributeView = provider
                .getFileAttributeView(filesSetup.getDataFilePath(),
                BasicFileAttributeView.class);

        assertTrue(fileAttributeView.readAttributes().isRegularFile());
        assertFalse(fileAttributeView.readAttributes().isDirectory());
    }

    @Test
    public void test_getFileAttributeView_NPE() throws IOException {
        try {
            provider.getFileAttributeView(null, BasicFileAttributeView.class);
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.getFileAttributeView(filesSetup.getDataFilePath(), null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_readAttributes() throws IOException {
        FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        Files.setAttribute(filesSetup.getDataFilePath(), "basic:lastModifiedTime", fileTime);
        BasicFileAttributes basicFileAttributes = provider
                .readAttributes(filesSetup.getDataFilePath(),
                BasicFileAttributes.class);
        FileTime lastModifiedTime = basicFileAttributes.lastModifiedTime();
        assertEquals(fileTime.to(TimeUnit.SECONDS), lastModifiedTime.to(TimeUnit.SECONDS));

        // When file is NON_EXISTENT.
        try {
            provider.readAttributes(filesSetup.getTestPath(), BasicFileAttributes.class);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @Test
    public void test_readAttributes_NPE() throws IOException {
        try {
            provider.readAttributes(filesSetup.getDataFilePath(),
                    (Class<BasicFileAttributes>) null);
            fail();
        } catch(NullPointerException expected) {}

        try {
            provider.readAttributes(null, BasicFileAttributes.class);
            fail();
        } catch(NullPointerException expected) {}
    }

    @Test
    public void test_setAttribute() throws IOException {
        // Other tests are covered in test_readAttributes.
        // When file is NON_EXISTENT.
        try {
            FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis());
            provider.setAttribute(filesSetup.getTestPath(), "basic:lastModifiedTime", fileTime);
            fail();
        } catch (NoSuchFileException expected) {}

        // ClassCastException
        try {
            provider.setAttribute(filesSetup.getDataFilePath(), "basic:lastModifiedTime", 10);
            fail();
        } catch (ClassCastException expected) {}

        // IllegalArgumentException
        try {
            provider.setAttribute(filesSetup.getDataFilePath(), "xyz", 10);
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            provider.setAttribute(null, "xyz", 10);
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.setAttribute(filesSetup.getDataFilePath(), null, 10);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_newFileChannel() throws IOException {
        Set<OpenOption> openOptions = new HashSet<>();

        // When file doesn't exist/
        try {
            // With CREATE & WRITE in OpenOptions.
            openOptions.add(CREATE);
            openOptions.add(WRITE);
            provider.newFileChannel(filesSetup.getTestPath(), openOptions);
            assertTrue(Files.exists(filesSetup.getTestPath()));
            Files.delete(filesSetup.getTestPath());
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        try {
            // With CREATE & APPEND in OpenOption.
            assertFalse(Files.exists(filesSetup.getTestPath()));
            openOptions.add(CREATE);
            openOptions.add(APPEND);
            provider.newFileChannel(filesSetup.getTestPath(), openOptions);
            assertTrue(Files.exists(filesSetup.getTestPath()));
            Files.delete(filesSetup.getTestPath());
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        // When file exists.
        try {
            FileChannel fc = provider.newFileChannel(filesSetup.getDataFilePath(), openOptions);
            assertEquals(filesSetup.TEST_FILE_DATA, readFromFileChannel(fc));
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        try {
            // When file exists and READ in OpenOptions.
            openOptions.add(READ);
            FileChannel fc = provider.newFileChannel(filesSetup.getDataFilePath(), openOptions);
            assertEquals(filesSetup.TEST_FILE_DATA, readFromFileChannel(fc));
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        // Reading from a file opened with WRITE.
        try {
            openOptions.add(WRITE);
            FileChannel fc = provider.newFileChannel(filesSetup.getDataFilePath(), openOptions);
            assertEquals(filesSetup.TEST_FILE_DATA, readFromFileChannel(fc));
            fail();
        } catch (NonReadableChannelException expected) {
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        // Writing to an exiting file.
        try {
            openOptions.add(WRITE);
            FileChannel fc = provider.newFileChannel(filesSetup.getDataFilePath(), openOptions);
            writeToFileChannel(fc, filesSetup.TEST_FILE_DATA_2);
            fc.close();
            assertEquals(overlayString1OnString2(TEST_FILE_DATA_2, TEST_FILE_DATA),
                    readFromFile(filesSetup.getDataFilePath()));
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        // APPEND to an existing file.
        try {
            openOptions.add(WRITE);
            openOptions.add(TRUNCATE_EXISTING);
            FileChannel fc = provider.newFileChannel(filesSetup.getDataFilePath(), openOptions);
            writeToFileChannel(fc, filesSetup.TEST_FILE_DATA_2);
            fc.close();
            assertEquals(TEST_FILE_DATA_2, readFromFile(filesSetup.getDataFilePath()));
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        // TRUNCATE an existing file.
        try {
            openOptions.add(WRITE);
            openOptions.add(APPEND);
            FileChannel fc = provider.newFileChannel(filesSetup.getDataFilePath(), openOptions);
            writeToFileChannel(fc, filesSetup.TEST_FILE_DATA_2);
            fc.close();
            assertEquals(TEST_FILE_DATA + TEST_FILE_DATA_2, readFromFile(filesSetup.getDataFilePath()));
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }
    }

    @Test
    /* J2ObjC changed: junitparams not supported
    @Parameters(method = "parameters_test_newFileChannel_NoSuchFileException")
     */
    @UseDataProvider("parameters_test_newFileChannel_NoSuchFileException")
    public void test_newFileChannel_NoSuchFileException(Set<? extends OpenOption> openOptions)
            throws IOException {
        try {
            provider.newFileChannel(filesSetup.getTestPath(), openOptions);
            fail();
        } catch (NoSuchFileException expected) {}
    }

    @DataProvider
    public static Object[][] parameters_test_newFileChannel_NoSuchFileException() {
        return new Object[][] {
                { EnumSet.noneOf(StandardOpenOption.class) },
                { EnumSet.of(READ) },
                { EnumSet.of(WRITE) },
                { EnumSet.of(TRUNCATE_EXISTING) },
                { EnumSet.of(APPEND) },
                { EnumSet.of(CREATE, READ) },
                { EnumSet.of(CREATE, TRUNCATE_EXISTING) },
                { EnumSet.of(CREATE, READ) },
        };
    }

    /* J2ObjC changed: junitparams not supported
    @SuppressWarnings("unused")
    private Object[] parameters_test_newFileChannel_NoSuchFileException() {
        return new Object[] {
                new Object[] { EnumSet.noneOf(StandardOpenOption.class) },
                new Object[] { EnumSet.of(READ) },
                new Object[] { EnumSet.of(WRITE) },
                new Object[] { EnumSet.of(TRUNCATE_EXISTING) },
                new Object[] { EnumSet.of(APPEND) },
                new Object[] { EnumSet.of(CREATE, READ) },
                new Object[] { EnumSet.of(CREATE, TRUNCATE_EXISTING) },
                new Object[] { EnumSet.of(CREATE, READ) },
        };
    }
     */

    @Test
    public void test_newFileChannel_withFileAttributes() throws IOException {
        Set<OpenOption> openOptions = new HashSet<>();
        FileTime fileTime = FileTime.fromMillis(System.currentTimeMillis());
        Files.setAttribute(filesSetup.getDataFilePath(), "basic:lastModifiedTime", fileTime);
        FileAttribute<FileTime> unsupportedAttr = new MockFileAttribute<>(
                "basic:lastModifiedTime", fileTime);

        Set<PosixFilePermission> perm = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> supportedAttr =
                PosixFilePermissions.asFileAttribute(perm);

        try {
            // When file doesn't exists and with OpenOption CREATE & WRITE.
            openOptions.clear();
            openOptions.add(CREATE);
            openOptions.add(WRITE);
            provider.newFileChannel(filesSetup.getTestPath(), openOptions, unsupportedAttr);
            fail();
        } catch (UnsupportedOperationException expected) {
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        try {
            // With OpenOption CREATE & WRITE.
            openOptions.clear();
            openOptions.add(CREATE);
            openOptions.add(WRITE);
            provider.newFileChannel(filesSetup.getTestPath(), openOptions, supportedAttr);
            assertEquals(supportedAttr.value(), Files.getAttribute(filesSetup.getTestPath(),
                    supportedAttr.name()));
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        // When file exists.
        try {
            provider.newFileChannel(filesSetup.getDataFilePath(), openOptions,
                    unsupportedAttr);
            fail();
        } catch (UnsupportedOperationException expected) {
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }

        // When file exists. No change in permissions.
        try {
            Set<PosixFilePermission> originalPermissions= (Set<PosixFilePermission>)
                    Files.getAttribute(filesSetup.getDataFilePath(), supportedAttr.name());
            FileChannel fc = provider.newFileChannel(filesSetup.getDataFilePath(), openOptions,
                    supportedAttr);
            assertEquals(originalPermissions, Files.getAttribute(filesSetup.getDataFilePath(),
                    supportedAttr.name()));
        } finally {
            filesSetup.reset();
            openOptions.clear();
        }
    }


    @Test
    public void test_newFileChannel_NPE() throws IOException {
        try {
            provider.newByteChannel(null, new HashSet<>(), new MockFileAttribute<>());
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.newByteChannel(filesSetup.getTestPath(), null, new MockFileAttribute<>());
            fail();
        } catch (NullPointerException expected) {}

        try {
            provider.newByteChannel(filesSetup.getTestPath(), new HashSet<>(),
                (FileAttribute<?>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_getPath() throws Exception {
        List<LinuxFileSystemTestData.TestData> inputOutputTestCases = getPath_URI_InputOutputTestData();
        for (LinuxFileSystemTestData.TestData inputOutputTestCase : inputOutputTestCases) {
            assertEquals(inputOutputTestCase.output,
                    provider.getPath(new URI(inputOutputTestCase.input)).toString());
        }

        // When URI is null.
        try {
            provider.getPath(null);
            fail();
        } catch (NullPointerException expected) {}

        // When Schema is not supported.
        try {
            provider.getPath(new URI("scheme://d"));
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_getScheme() {
        assertEquals("file", provider.getScheme());
    }

    @Test
    public void test_installedProviders() {
        assertNotNull(provider.installedProviders());
    }

    @Test
    public void test_newFileSystem$URI$Map() throws Exception {
        Path testPath = Paths.get("/");
        assertNotNull(provider.getFileSystem(testPath.toUri()));

        try {
            provider.getFileSystem(null);
            fail();
        } catch (NullPointerException expected) {}

        // Test the case when URI has illegal scheme.
        URI stubURI = new URI("scheme://path");
        try {
            provider.getFileSystem(stubURI);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    String readFromFileChannel(FileChannel fc) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(20);
        fc.read(bb);
        return new String(bb.array(), "UTF-8").trim();
    }

    void writeToFileChannel(FileChannel fc, String data) throws IOException {
        fc.write(ByteBuffer.wrap(data.getBytes()));
    }

    String overlayString1OnString2(String s1, String s2) {
        return s1 + s2.substring(s1.length());
    }

    static class MockFileAttribute<T> implements FileAttribute {

        String name;
        T value;

        MockFileAttribute() {
        }

        MockFileAttribute(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public T value() {
            return value;
        }
    }
}
