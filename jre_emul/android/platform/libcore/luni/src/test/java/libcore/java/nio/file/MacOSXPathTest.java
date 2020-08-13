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

import com.sun.nio.file.ExtendedWatchEventModifier;

import java.nio.file.WatchEvent.Kind;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MacOSXPathTest {

    @Rule
    public FilesSetup filesSetup = new FilesSetup();

    /**
     * CTS doesn't allow creating files in the test directory, however, Vogar allows creation of
     * new files in the test directory. Therefore, for the tests which don't require write
     * permission, dummyPath would serve the purpose, however, for the others, {@link
     * FilesSetup#getTestDirPath()} should be used.
     */
    private static final Path dummyPath = Paths.get("dummyPath");

//    @Test
//    public void test_getFileSystem() {
//        assertTrue(dummyPath.getFileSystem().provider() instanceof
//                sun.nio.fs.LinuxFileSystemProvider);
//    }

    @Test
    public void test_isAbsolute() {
        assertFalse(dummyPath.isAbsolute());
        Path absolutePath = dummyPath.toAbsolutePath();
        assertTrue(absolutePath.isAbsolute());
    }

    @Test
    public void test_getRoot() {
        assertEquals(Paths.get("/"), dummyPath.toAbsolutePath().getRoot());
        assertNull(dummyPath.getRoot());
    }

    @Test
    public void test_getFileName() {
        assertEquals(dummyPath, dummyPath.getFileName());
        assertEquals(dummyPath, dummyPath.toAbsolutePath().getFileName());
        assertNull(dummyPath.getRoot());
        assertEquals(Paths.get("data"), Paths.get("/data").getFileName());
        assertEquals(Paths.get("data"), Paths.get("/data/").getFileName());
        assertEquals(Paths.get(".."), Paths.get("/data/dir1/..").getFileName());
    }

    @Test
    public void test_getParent() {
        assertNull(dummyPath.getParent());
        assertEquals(Paths.get("rootDir"), Paths.get("rootDir/dir").getParent());
    }

    @Test
    public void test_getNameCount() {
        assertEquals(0, Paths.get("/").getNameCount());
        assertEquals(1, Paths.get("/dir").getNameCount());
        assertEquals(2, Paths.get("/dir/dir").getNameCount());
        assertEquals(2, Paths.get("/dir/..").getNameCount());
    }

    @Test
    public void test_getName() {
        assertEquals(Paths.get("t"), Paths.get("/t/t1/t2/t3").getName(0));
        assertEquals(Paths.get("t2"), Paths.get("/t/t1/t2/t3").getName(2));
        assertEquals(Paths.get("t3"), Paths.get("/t/t1/t2/t3").getName(3));

        // Without root.
        assertEquals(Paths.get("t3"), Paths.get("t/t1/t2/t3").getName(3));

        // Invalid index.
        try {
            Paths.get("/t/t1/t2/t3").getName(4);
            fail();
        } catch (IllegalArgumentException expected) {}

        // Negative index value.
        try {
            Paths.get("/t/t1/t2/t3").getName(-1);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_subPath() {
        assertEquals(Paths.get("t1/t2"), Paths.get("t1/t2/t3").subpath(0, 2));
        assertEquals(Paths.get("t2"), Paths.get("t1/t2/t3").subpath(1, 2));

        try {
            Paths.get("t1/t2/t3").subpath(1, 1);
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            assertEquals(Paths.get("t1/t1"), Paths.get("t1/t2/t3").subpath(1, 0));
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            assertEquals(Paths.get("t1/t1"), Paths.get("t1/t2/t3").subpath(1, 5));
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test
    public void test_startsWith$String() {
        assertTrue(Paths.get("t1/t2").startsWith("t1"));
        assertTrue(dummyPath.toAbsolutePath().startsWith("/"));
        assertTrue(Paths.get("t1/t2/t3").startsWith("t1/t2"));
        assertFalse(Paths.get("t1/t2").startsWith("t2"));
    }

    @Test(expected = NullPointerException.class)
    public void test_startsWith$String_NPE() {
        filesSetup.getTestPath().startsWith((String) null);
    }

    @Test
    public void test_startsWith$Path() {
        assertTrue(Paths.get("t1/t2").startsWith(Paths.get("t1")));
        assertTrue(dummyPath.toAbsolutePath().startsWith(Paths.get("/")));
        assertTrue(Paths.get("t1/t2/t3").startsWith(Paths.get("t1/t2")));
        assertFalse(Paths.get("t1/t2").startsWith(Paths.get("t2")));
    }

    @Test(expected = NullPointerException.class)
    public void test_startsWith$Path_NPE() {
        filesSetup.getTestPath().startsWith((Path) null);
    }

    @Test
    public void test_endsWith$Path() {
        assertTrue(Paths.get("t1/t2").endsWith(Paths.get("t2")));
        assertTrue(Paths.get("t1/t2/t3").endsWith(Paths.get("t2/t3")));
        assertFalse(Paths.get("t1/t2").endsWith(Paths.get("t1")));
        assertTrue(Paths.get("/").endsWith(Paths.get("/")));
        assertFalse(Paths.get("/data/").endsWith(Paths.get("/")));
    }

    @Test(expected = NullPointerException.class)
    public void test_endsWith$Path_NPE() {
        filesSetup.getTestPath().endsWith((Path)null);
    }

    @Test
    public void test_endsWith$String() {
        assertTrue(Paths.get("t1/t2").endsWith("t2"));
        assertTrue(Paths.get("t1/t2/t3").endsWith("t2/t3"));
        assertFalse(Paths.get("t1/t2").endsWith("t1"));
        assertTrue(Paths.get("/").endsWith("/"));
        assertFalse(Paths.get("/data/").endsWith("/"));
    }

    @Test(expected = NullPointerException.class)
    public void test_endsWith$String_NPE() {
        filesSetup.getTestPath().endsWith((String)null);
    }

    @Test
    public void test_normalize() {
        assertEquals(Paths.get("t2/t3"), Paths.get("t1/../t2/t3").normalize());
        assertEquals(Paths.get("../t2/t3"), Paths.get("t1/../../t2/t3").normalize());
        assertEquals(Paths.get("t1/t2/t3"), Paths.get("t1/./t2/t3").normalize());
        assertEquals(Paths.get("t1/t2/t3"), Paths.get("t1/././t2/t3").normalize());
        assertEquals(Paths.get("t1/t2/t3"), Paths.get("t1/././t2/t3").normalize());
        assertEquals(Paths.get("t1"), Paths.get("t1/"));
    }

    @Test
    public void test_resolve$Path() {
        Path p = Paths.get("p");
        Path p1 = Paths.get("p1");
        Path p1p = Paths.get("p1/p");
        assertEquals(p1p, p1.resolve(p));
        assertEquals(p.toAbsolutePath(), p1.resolve(p.toAbsolutePath()));
        assertEquals(p1p.toAbsolutePath(), p1.toAbsolutePath().resolve(p));
    }

    @Test(expected = NullPointerException.class)
    public void test_resolve$Path_NPE() {
        dummyPath.resolve((Path)null);
    }

    @Test
    public void test_resolve$String() {
        Path p = Paths.get("p");
        Path p1 = Paths.get("p1");
        Path p1p = Paths.get("p1/p");
        assertEquals(p1p, p1.resolve("p"));
        assertEquals(p1p.toAbsolutePath(), p1.toAbsolutePath().resolve("p"));
    }

    @Test(expected = NullPointerException.class)
    public void test_resolve$String_NPE() {
        dummyPath.resolve((String)null);
    }

    @Test
    public void test_resolveSibling$Path() {
        Path c2 = Paths.get("c2");
        Path parent_c1 = Paths.get("parent/c1");
        Path parent_c2 = Paths.get("parent/c2");
        assertEquals(parent_c2, parent_c1.resolveSibling(c2));
        assertEquals(c2.toAbsolutePath(), parent_c1.resolveSibling(c2.toAbsolutePath()));
        assertEquals(parent_c2.toAbsolutePath(), parent_c1.toAbsolutePath().resolveSibling(c2));
    }

    @Test(expected = NullPointerException.class)
    public void test_resolveSibling$String_Path() {
        dummyPath.resolveSibling((Path) null);
    }

    @Test
    public void test_resolveSibling$String() {
        Path c2 = Paths.get("c2");
        Path parent_c1 = Paths.get("parent/c1");
        Path parent_c2 = Paths.get("parent/c2");
        assertEquals(parent_c2, parent_c1.resolveSibling(c2.toString()));
        assertEquals(c2.toAbsolutePath(), parent_c1.resolveSibling(c2.toAbsolutePath().toString()));
        assertEquals(parent_c2.toAbsolutePath(), parent_c1.toAbsolutePath()
                .resolveSibling(c2.toString()));
    }

    @Test(expected = NullPointerException.class)
    public void test_resolveSibling$String_NPE() {
        dummyPath.resolveSibling((String)null);
    }

    @Test
    public void test_relativize() {
        Path p1 = Paths.get("t1/t2/t3");
        Path p2 = Paths.get("t1/t2");
        assertEquals(Paths.get(".."), p1.relativize(p2));
        assertEquals(Paths.get(".."), p1.toAbsolutePath().relativize(p2.toAbsolutePath()));
        assertEquals(Paths.get("t3"), p2.relativize(p1));

        // Can't be relativized as either of the paths are relative and the other is not.
        try {
            p1.relativize(p2.toAbsolutePath());
            fail();
        } catch (IllegalArgumentException expected) {}

        try {
            p1.toAbsolutePath().relativize(p2);
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    @Test(expected = NullPointerException.class)
    public void test_relativize_NPE() {
        dummyPath.relativize(null);
    }

    @Test
    public void test_toURI() throws URISyntaxException {
        assertEquals(new URI("file://" + dummyPath.toAbsolutePath().toString()), dummyPath.toUri());
        assertEquals(new URI("file:///"), Paths.get("/").toUri());
        assertEquals(new URI("file:///dir/.."), Paths.get(("/dir/..")).toUri());
        assertEquals(new URI("file:///../"), Paths.get(("/..")).toUri());
        assertEquals(new URI("file:///dir/.."), Paths.get(("/dir/..")).toUri());
        assertEquals(new URI("file:///./"), Paths.get(("/.")).toUri());
        assertEquals(new URI("file:///dir/."), Paths.get(("/dir/.")).toUri());
        // For unicode characters.
        assertEquals(new URI("file:///%E0%A4%B0%E0%A4%BE%E0%A4%B9."), Paths.get(("/राह.")).toUri());
    }

    @Test
    public void test_toAbsolutePath() {
        assertFalse(dummyPath.isAbsolute());
        assertTrue(dummyPath.toAbsolutePath().isAbsolute());
    }

    @Test
    public void test_toRealPath() throws IOException {
        // When file doesn't exist.
        try {
            dummyPath.toRealPath();
            fail();
        } catch (NoSuchFileException expected) {}
        Files.createFile(filesSetup.getTestPath());
        Path realPath = filesSetup.getTestPath().toRealPath();
        assertTrue(Files.isSameFile(filesSetup.getTestPath().toAbsolutePath(), realPath));
        assertTrue(realPath.isAbsolute());
        assertFalse(Files.isSymbolicLink(realPath));

        Path dir = Paths.get(filesSetup.getTestDir(), "dir1/dir2");
        Path file = Paths.get(filesSetup.getTestDir(), "dir1/dir2/../../file");
        Files.createDirectories(dir);
        Files.createFile(file);
        realPath = file.toRealPath();
        assertTrue(Files.isSameFile(file.toAbsolutePath(), realPath));
        assertTrue(realPath.isAbsolute());
        assertFalse(Files.isSymbolicLink(realPath));

        // Sym links.
        Path symLink = Paths.get(filesSetup.getTestDir(), "symlink");
        Files.createSymbolicLink(symLink, filesSetup.getTestPath().toAbsolutePath());
        realPath = symLink.toRealPath();
        assertTrue(Files.isSameFile(symLink, realPath));
        assertTrue(realPath.isAbsolute());
        assertFalse(Files.isSymbolicLink(realPath));

        realPath = symLink.toRealPath(LinkOption.NOFOLLOW_LINKS);
        assertTrue(Files.isSameFile(symLink, realPath));
        assertTrue(realPath.isAbsolute());
        assertTrue(Files.isSymbolicLink(realPath));
    }

    @Test
    public void test_toFile() {
        File file = dummyPath.toFile();
        System.out.println(dummyPath.toAbsolutePath().toString());
        System.out.println(file.getAbsolutePath());
        assertEquals(dummyPath.toAbsolutePath().toString(), file.getAbsolutePath());
    }

    /* j2objc: polling key events fails on MacOS, causing this test to hang.
    @Test
    public void test_register$WatchService$WatchEvent_Kind() throws IOException,
            InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        WatchEvent.Kind<?>[] events = {ENTRY_CREATE, ENTRY_DELETE};
        Path file = Paths.get(filesSetup.getTestDir(), "directory/file");
        assertFalse(Files.exists(file));
        Path directory = Paths.get(filesSetup.getTestDir(), "directory");
        Files.createDirectories(directory);
        WatchKey key = directory.register(watchService, events);

        // Creating, modifying and deleting the file.
        Files.createFile(file);
        assertTrue(Files.exists(file));
        // EVENT_MODIFY should not be logged.
        Files.newOutputStream(file).write("hello".getBytes());
        Files.delete(file);
        assertFalse(Files.exists(file));

        assertTrue(key.isValid());
        assertEquals(directory, key.watchable());
        List<WatchEvent<?>> eventList = new ArrayList<>();

        // Wait for the events to be recorded by WatchService.
        while(true) {
            eventList.addAll(key.pollEvents());
            if (eventList.size() == 2) break;
            Thread.sleep(1000);
        }
        // Wait for the events to be recorded by watchService.
        assertEquals(2, eventList.size());
        assertEquals(ENTRY_CREATE, eventList.get(0).kind());
        assertEquals(ENTRY_DELETE, eventList.get(1).kind());
    }
     */

    @Test
    public void test_register$WatchService$WatchEvent_Kind_NPE() throws IOException,
            InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        WatchEvent.Kind<?>[] events = {ENTRY_CREATE, ENTRY_DELETE};
        Path directory = Paths.get(filesSetup.getTestDir(), "directory");
        Files.createDirectories(directory);
        try {
            directory.register(null, events);
            fail();
        } catch (NullPointerException expected) {}

        try {
            directory.register(watchService, (WatchEvent.Kind<?>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_register$WatchService$WatchEvent_Kind_Exception() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path directory = Paths.get(filesSetup.getTestDir(), "directory1");
        Files.createFile(directory);

        // When file is not a directory.
        try {
            directory.register(watchService, ENTRY_CREATE);
            fail();
        } catch (NotDirectoryException expected) {}

        // When the events are not supported.
        Files.deleteIfExists(directory);
        Files.createDirectories(directory);
        WatchEvent.Kind<?>[] events = {new NonStandardEvent<>()};
        try {
            directory.register(watchService, events);
            fail();
        } catch (UnsupportedOperationException expected) {}

        // When the watch service is closed.
        watchService.close();
        try {
            directory.register(watchService, ENTRY_CREATE);
            fail();
        } catch (ClosedWatchServiceException expected) {}
    }

    @Test
    public void test_register$WatchService$WatchEvent_Kind_Exception_NPE() throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Path directory = Paths.get(filesSetup.getTestDir(), "directory1");
        Files.createDirectories(directory);

        // When file is not a directory.
        try {
            directory.register(null, ENTRY_CREATE);
            fail();
        } catch (NullPointerException expected) {}

        try {
            directory.register(watchService, (Kind<?>) null);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_register$WatchService$WatchEvent_Kind$WatchEvent_Modifier() throws IOException
    {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        WatchEvent.Kind<?>[] events = {ENTRY_CREATE};
        Path dirRoot = Paths.get(filesSetup.getTestDir(), "dir");
        Files.createDirectories(dirRoot);
        try {
            WatchKey key = dirRoot.register(watchService, events,
                    ExtendedWatchEventModifier.FILE_TREE);
            fail();
        } catch (UnsupportedOperationException expected) {
            assertTrue(expected.getMessage().contains("Modifier not supported"));
        }
    }

    @Test
    public void test_register$WatchService$WatchEvent_Kind$WatchEvent_Modifier_NPE()
            throws IOException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        WatchEvent.Kind<?>[] events = {ENTRY_CREATE};
        Path dirRoot = Paths.get(filesSetup.getTestDir(), "dir");
        Files.createDirectories(dirRoot);
        try {
            WatchKey key = dirRoot.register(null, events,
                    ExtendedWatchEventModifier.FILE_TREE);
            fail();
        } catch (NullPointerException expected) {}

        try {
            WatchKey key = dirRoot.register(watchService, null,
                    ExtendedWatchEventModifier.FILE_TREE);
            fail();
        } catch (NullPointerException expected) {}
    }

    @Test
    public void test_iterator() {
        Path p = Paths.get("f1/f2/f3");
        Iterator<Path> pathIterator = p.iterator();
        assertEquals(Paths.get("f1"), pathIterator.next());
        assertEquals(Paths.get("f2"), pathIterator.next());
        assertEquals(Paths.get("f3"), pathIterator.next());
        assertFalse(pathIterator.hasNext());
    }

    @Test
    public void test_iterator_hasRoot() {
        Path p = Paths.get("/f1/f2/f3");
        Iterator<Path> pathIterator = p.iterator();
        assertEquals(Paths.get("f1"), pathIterator.next());
        assertEquals(Paths.get("f2"), pathIterator.next());
        assertEquals(Paths.get("f3"), pathIterator.next());
        assertFalse(pathIterator.hasNext());
    }

    @Test
    public void test_compareTo() {
        Path p1 = Paths.get("d/a");
        Path p2 = Paths.get("d/b");
        assertTrue(p1.compareTo(p2) < 0);
        assertTrue(p2.compareTo(p1) > 0);
        assertTrue(p1.compareTo(p1) == 0);
    }

    @Test(expected = NullPointerException.class)
    public void test_compareTo_NPE() {
        filesSetup.getTestPath().compareTo(null);
    }

    @Test
    public void test_equals() {
        Path p1 = Paths.get("a/b");
        Path p2 = Paths.get("a/../a/b");
        Path p3 = p1.toAbsolutePath();
        assertFalse(p1.equals(p2));
        assertTrue(p1.equals(p1));
        assertFalse(p1.equals(p3));
    }

    @Test
    public void test_equals_NPE() {
        // Should not throw NPE.
        filesSetup.getTestPath().equals(null);
    }

    @Test
    public void test_hashCode() {
        Path p1 = Paths.get("f1/f2/f3");
        assertEquals(-642657684, p1.hashCode());

        // With root component.
        Path p2 = Paths.get("/f1/f2/f3");
        assertEquals(306328475, p2.hashCode());
    }

    @Test
    public void test_toString() {
        Path p = Paths.get("f1/f2/f3");
        assertEquals("f1/f2/f3", p.toString());

        p = Paths.get("");
        assertEquals("", p.toString());

        p = Paths.get("..");
        assertEquals("..", p.toString());

        p = Paths.get(".");
        assertEquals(".", p.toString());

        p = Paths.get("dir/");
        assertEquals("dir", p.toString());

        p = Paths.get("/dir");
        assertEquals("/dir", p.toString());
    }

    private static class NonStandardEvent<T> implements WatchEvent.Kind<T> {

        @Override
        public String name() {
            return null;
        }

        @Override
        public Class<T> type() {
            return null;
        }
    }
}
