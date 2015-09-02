/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.jar;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The {@code JarOutputStream} is used to write data in the {@code JarFile}
 * format to an arbitrary output stream
 */
public class JarOutputStream extends ZipOutputStream {

    private Manifest manifest;

    /**
     * Constructs a new {@code JarOutputStream} using an output stream. The
     * content of the {@code Manifest} must match the JAR entry information
     * written subsequently to the stream.
     *
     * @param os
     *            the {@code OutputStream} to write to
     * @param manifest
     *            the {@code Manifest} to output for this JAR file.
     * @throws IOException
     *             if an error occurs creating the {@code JarOutputStream}.
     */
    public JarOutputStream(OutputStream os, Manifest manifest) throws IOException {
        super(os);
        if (manifest == null) {
            throw new NullPointerException("manifest == null");
        }
        this.manifest = manifest;
        ZipEntry ze = new ZipEntry(JarFile.MANIFEST_NAME);
        putNextEntry(ze);
        this.manifest.write(this);
        closeEntry();
    }

    /**
     * Constructs a new {@code JarOutputStream} using an arbitrary output
     * stream.
     *
     * @param os
     *            the {@code OutputStream} to write to.
     * @throws IOException
     *             if an error occurs creating the {@code JarOutputStream}.
     */
    public JarOutputStream(OutputStream os) throws IOException {
        super(os);
    }

    /**
     * Writes the specified ZIP entry to the underlying stream. The previous
     * entry is closed if it is still open.
     *
     * @param ze
     *            the {@code ZipEntry} to write to.
     * @throws IOException
     *             if an error occurs writing to the entry.
     * @see ZipEntry
     */
    @Override
    public void putNextEntry(ZipEntry ze) throws IOException {
        super.putNextEntry(ze);
    }
}
