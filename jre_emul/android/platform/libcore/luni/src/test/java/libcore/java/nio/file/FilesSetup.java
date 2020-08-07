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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

class FilesSetup implements TestRule {

    final static String DATA_FILE = "dataFile";

    final static String NON_EXISTENT_FILE = "nonExistentFile";

    final static String TEST_FILE_DATA = "hello";

    final static String TEST_FILE_DATA_2 = "test";

    /** 
     *  Data that includes characters code above the US-ASCII range and will be more obviously
     *  corrupted if encoded / decoded incorrectly than
     *  {@link #TEST_FILE_DATA} / {@link #TEST_FILE_DATA_2}.
     */
    final static String UTF_16_DATA = "परीक्षण";

    private String testDir;

    private Path dataFilePath;

    private Path testPath;

    private Path testDirPath;

    private boolean filesInitialized = false;

    void setUp() throws Exception {
        initializeFiles();
    }

    void tearDown() throws Exception {
        filesInitialized = false;
        clearAll();
    }

    private void initializeFiles() throws IOException {
        testDirPath = Files.createTempDirectory("testDir");
        testDir = testDirPath.toString();
        dataFilePath = Paths.get(testDir, DATA_FILE);
        testPath = Paths.get(testDir, NON_EXISTENT_FILE);
        File testInputFile = new File(testDir, DATA_FILE);
        if (!testInputFile.exists()) {
            testInputFile.createNewFile();
        }
        FileWriter fw = new FileWriter(testInputFile.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(TEST_FILE_DATA);
        bw.close();
        filesInitialized = true;
    }

    Path getTestPath() {
        checkState();
        return testPath;
    }

    Path getDataFilePath() {
        checkState();
        return dataFilePath;
    }

    Path getTestDirPath() {
        checkState();
        return testDirPath;
    }

    String getTestDir() {
        checkState();
        return testDir;
    }

    private void checkState() {
        if (!filesInitialized) {
            throw new IllegalStateException("Files are not setup.");
        }
    }

    void clearAll() throws IOException {
        Path root = Paths.get(testDir);
        delete(root);
    }

    void reset() throws IOException {
        clearAll();
        initializeFiles();
    }

    private static void delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(path);
            dirStream.forEach(
                    p -> {
                        try {
                            delete(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            dirStream.close();
        }
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            // Do nothing
        }
    }

    static void writeToFile(Path file, String data, OpenOption... option) throws IOException {
        OutputStream os = Files.newOutputStream(file, option);
        os.write(data.getBytes());
        os.close();
    }

    static String readFromFile(Path file) throws IOException {
        InputStream is = Files.newInputStream(file);
        return readFromInputStream(is);
    }

    static String readFromInputStream(InputStream is) throws IOException {
        byte[] input = new byte[10000];
        is.read(input);
        return new String(input, "UTF-8").trim();
    }

    /* J2ObjC removed: simple iOS version of Runtime does not support exec
    static Process execCmdAndWaitForTermination(String... cmdList)
            throws InterruptedException, IOException {
        Process process = Runtime.getRuntime().exec(cmdList);
        // Wait for the process to terminate.
        process.waitFor();
        return process;
    }
     */

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    setUp();
                    statement.evaluate();
                } finally {
                    tearDown();
                }
            }
        };
    }

    Path getPathInTestDir(String path) {
        return Paths.get(getTestDir(), path);
    }

    /**
     * Non Standard CopyOptions.
     */
    enum NonStandardOption implements CopyOption, OpenOption {
        OPTION1,
    }

}
