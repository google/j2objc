/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

/**
 * A specialized {@link Writer} that writes to a file in the file system.
 * All write requests made by calling methods in this class are directly
 * forwarded to the equivalent function of the underlying operating system.
 * Since this may induce some performance penalty, in particular if many small
 * write requests are made, a FileWriter is often wrapped by a
 * BufferedWriter.
 *
 * @see BufferedWriter
 * @see FileReader
 */
public class FileWriter extends OutputStreamWriter {

    /**
     * Creates a FileWriter using the File {@code file}.
     *
     * @param file
     *            the non-null File to write bytes to.
     * @throws IOException
     *             if {@code file} cannot be opened for writing.
     */
    public FileWriter(File file) throws IOException {
        super(new FileOutputStream(file));
    }

    /**
     * Creates a FileWriter using the File {@code file}. The parameter
     * {@code append} determines whether or not the file is opened and appended
     * to or just opened and overwritten.
     *
     * @param file
     *            the non-null File to write bytes to.
     * @param append
     *            indicates whether or not to append to an existing file.
     * @throws IOException
     *             if the {@code file} cannot be opened for writing.
     */
    public FileWriter(File file, boolean append) throws IOException {
        super(new FileOutputStream(file, append));
    }

    /**
     * Creates a FileWriter using the existing FileDescriptor {@code fd}.
     *
     * @param fd
     *            the non-null FileDescriptor to write bytes to.
     */
    public FileWriter(FileDescriptor fd) {
        super(new FileOutputStream(fd));
    }

    /**
     * Creates a FileWriter using the platform dependent {@code filename}.
     *
     * @param filename
     *            the non-null name of the file to write bytes to.
     * @throws IOException
     *             if the file cannot be opened for writing.
     */
    public FileWriter(String filename) throws IOException {
        super(new FileOutputStream(new File(filename)));
    }

    /**
     * Creates a FileWriter using the platform dependent {@code filename}. The
     * parameter {@code append} determines whether or not the file is opened and
     * appended to or just opened and overwritten.
     *
     * @param filename
     *            the non-null name of the file to write bytes to.
     * @param append
     *            indicates whether or not to append to an existing file.
     * @throws IOException
     *             if the {@code file} cannot be opened for writing.
     */
    public FileWriter(String filename, boolean append) throws IOException {
        super(new FileOutputStream(filename, append));
    }
}
