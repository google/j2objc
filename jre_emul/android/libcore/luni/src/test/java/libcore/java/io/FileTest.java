/*
 * Copyright (C) 2009 The Android Open Source Project
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
 * limitations under the License.
 */

package libcore.java.io;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.UUID;
import libcore.io.Libcore;

public class FileTest extends junit.framework.TestCase {
    private static File createTemporaryDirectory() throws Exception {
        String base = System.getProperty("java.io.tmpdir");
        File directory = new File(base, UUID.randomUUID().toString());
        assertTrue(directory.mkdirs());
        return directory;
    }

    private static String longString(int n) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            result.append('x');
        }
        return result.toString();
    }

    private static File createDeepStructure(File base) throws Exception {
        // ext has a limit of around 256 characters for each path entry.
        // 128 characters should be safe for everything but FAT.
        String longString = longString(128);
        // Keep creating subdirectories until the path length is greater than 1KiB.
        // Ubuntu 8.04's kernel is happy up to about 4KiB.
        File f = base;
        for (int i = 0; (f.toString().length() + longString.length()) <= 1024; ++i) {
            f = new File(f, longString);
            assertTrue(f.mkdir());
        }
        return f;
    }

    // Rather than test all methods, assume that if createTempFile creates a long path and
    // exists can see it, the code for coping with long paths (shared by all methods) works.
    public void test_longPath() throws Exception {
        File base = createTemporaryDirectory();
        assertTrue(createDeepStructure(base).exists());
    }

    public void test_emptyFilename() throws Exception {
        // The behavior of the empty filename is an odd mixture.
        File f = new File("");
        // Mostly it behaves like an invalid path...
        assertFalse(f.canExecute());
        assertFalse(f.canRead());
        assertFalse(f.canWrite());
        try {
            f.createNewFile();
            fail("expected IOException");
        } catch (IOException expected) {
        }
        assertFalse(f.delete());
        f.deleteOnExit();
        assertFalse(f.exists());
        assertEquals("", f.getName());
        assertEquals(null, f.getParent());
        assertEquals(null, f.getParentFile());
        assertEquals("", f.getPath());
        assertFalse(f.isAbsolute());
        assertFalse(f.isDirectory());
        assertFalse(f.isFile());
        assertFalse(f.isHidden());
        assertEquals(0, f.lastModified());
        assertEquals(0, f.length());
        assertEquals(null, f.list());
        assertEquals(null, f.list(null));
        assertEquals(null, f.listFiles());
        assertEquals(null, f.listFiles((FileFilter) null));
        assertEquals(null, f.listFiles((FilenameFilter) null));
        assertFalse(f.mkdir());
        assertFalse(f.mkdirs());
        assertFalse(f.renameTo(f));
        assertFalse(f.setLastModified(123));
        assertFalse(f.setExecutable(true));
        assertFalse(f.setReadOnly());
        assertFalse(f.setReadable(true));
        assertFalse(f.setWritable(true));
        // ...but sometimes it behaves like "user.dir".
        String cwd = System.getProperty("user.dir");
        assertEquals(new File(cwd), f.getAbsoluteFile());
        assertEquals(cwd, f.getAbsolutePath());
        // TODO: how do we test these without hard-coding assumptions about where our temporary
        // directory is? (In practice, on Android, our temporary directory is accessed through
        // a symbolic link, so the canonical file/path will be different.)
        //assertEquals(new File(cwd), f.getCanonicalFile());
        //assertEquals(cwd, f.getCanonicalPath());
    }

    // isAbsolute from the File constructor, potentially breaking subclasses.
    public void test_subclassing() throws Exception {
        class MyFile extends File {
            private String field;
            MyFile(String s) {
                super(s);
                field = "";
            }
            @Override public boolean isAbsolute() {
                field.length();
                return super.isAbsolute();
            }
        }
        new MyFile("");
    }

    public void test_getCanonicalPath() throws Exception {
        // This assumes you can create symbolic links in the temporary directory. This isn't
        // true on Android if you're using /sdcard. It will work in /data/local though.
        File base = createTemporaryDirectory();
        File target = new File(base, "target");
        target.createNewFile(); // The RI won't follow a dangling symlink, which seems like a bug!
        File linkName = new File(base, "link");
        ln_s(target, linkName);
        assertEquals(target.getCanonicalPath(), linkName.getCanonicalPath());

        // .../subdir/shorter -> .../target (using a link to ../target).
        File subdir = new File(base, "subdir");
        assertTrue(subdir.mkdir());
        linkName = new File(subdir, "shorter");
        ln_s("../target", linkName.toString());
        assertEquals(target.getCanonicalPath(), linkName.getCanonicalPath());

        // .../l -> .../subdir/longer (using a relative link to subdir/longer).
        linkName = new File(base, "l");
        ln_s("subdir/longer", linkName.toString());
        File longer = new File(base, "subdir/longer");
        longer.createNewFile(); // The RI won't follow a dangling symlink, which seems like a bug!
        assertEquals(longer.getCanonicalPath(), linkName.getCanonicalPath());

        // .../double -> .../target (via a link into subdir and a link back out).
        linkName = new File(base, "double");
        ln_s("subdir/shorter", linkName.toString());
        assertEquals(target.getCanonicalPath(), linkName.getCanonicalPath());
    }

    private static void ln_s(File target, File linkName) throws Exception {
        ln_s(target.toString(), linkName.toString());
    }

    private static void ln_s(String target, String linkName) throws Exception {
        Libcore.os.symlink(target, linkName);
    }

    public void test_createNewFile() throws Exception {
        File f = File.createTempFile("FileTest", "tmp");
        assertFalse(f.createNewFile()); // EEXIST -> false
        try {
            new File(f, "poop").createNewFile(); // ENOTDIR -> throw
            fail();
        } catch (IOException expected) {
        }
        try {
            new File("").createNewFile(); // ENOENT -> throw
            fail();
        } catch (IOException expected) {
        }
    }

    public void test_rename() throws Exception {
        File f = File.createTempFile("FileTest", "tmp");
        assertFalse(f.renameTo(new File("")));
        assertFalse(new File("").renameTo(f));
        assertFalse(f.renameTo(new File(".")));
        assertTrue(f.renameTo(f));
    }

    public void test_getAbsolutePath() throws Exception {
        String originalUserDir = System.getProperty("user.dir");
        try {
            File f = new File("poop");
            System.setProperty("user.dir", "/a");
            assertEquals("/a/poop", f.getAbsolutePath());
            System.setProperty("user.dir", "/b");
            assertEquals("/b/poop", f.getAbsolutePath());
        } finally {
            System.setProperty("user.dir", originalUserDir);
        }
    }

    public void test_getSpace() throws Exception {
        assertTrue(new File("/").getFreeSpace() >= 0);
        assertTrue(new File("/").getTotalSpace() >= 0);
        assertTrue(new File("/").getUsableSpace() >= 0);
    }

    public void test_mkdirs() throws Exception {
        // Set up a directory to test in.
        File base = createTemporaryDirectory();

        // mkdirs returns true only if it _creates_ a directory.
        // So we get false for a directory that already exists...
        assertTrue(base.exists());
        assertFalse(base.mkdirs());
        // But true if we had to create something.
        File a = new File(base, "a");
        assertFalse(a.exists());
        assertTrue(a.mkdirs());
        assertTrue(a.exists());

        // Test the recursive case where we need to create multiple parents.
        File b = new File(a, "b");
        File c = new File(b, "c");
        File d = new File(c, "d");
        assertTrue(a.exists());
        assertFalse(b.exists());
        assertFalse(c.exists());
        assertFalse(d.exists());
        assertTrue(d.mkdirs());
        assertTrue(a.exists());
        assertTrue(b.exists());
        assertTrue(c.exists());
        assertTrue(d.exists());

        // Test the case where the 'directory' exists as a file.
        File existsAsFile = new File(base, "existsAsFile");
        existsAsFile.createNewFile();
        assertTrue(existsAsFile.exists());
        assertFalse(existsAsFile.mkdirs());

        // Test the case where the parent exists as a file.
        File badParent = new File(existsAsFile, "sub");
        assertTrue(existsAsFile.exists());
        assertFalse(badParent.exists());
        assertFalse(badParent.mkdirs());
    }
}
