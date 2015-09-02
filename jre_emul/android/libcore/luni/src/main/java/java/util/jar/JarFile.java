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

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import libcore.io.Streams;

/**
 * {@code JarFile} is used to read jar entries and their associated data from
 * jar files.
 *
 * @see JarInputStream
 * @see JarEntry
 */
public class JarFile extends ZipFile {

    /**
     * The MANIFEST file name.
     */
    public static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";

    // The directory containing the manifest.
    static final String META_DIR = "META-INF/";

    // The manifest after it has been read from the JAR.
    private Manifest manifest;

    // The entry for the MANIFEST.MF file before the first call to getManifest().
    private byte[] manifestBytes;

    JarVerifier verifier;

    private boolean closed = false;

    static final class JarFileInputStream extends FilterInputStream {
        private final JarVerifier.VerifierEntry entry;

        private long count;
        private boolean done = false;

        JarFileInputStream(InputStream is, long size, JarVerifier.VerifierEntry e) {
            super(is);
            entry = e;

            count = size;
        }

        @Override
        public int read() throws IOException {
            if (done) {
                return -1;
            }
            if (count > 0) {
                int r = super.read();
                if (r != -1) {
                    entry.write(r);
                    count--;
                } else {
                    count = 0;
                }
                if (count == 0) {
                    done = true;
                    entry.verify();
                }
                return r;
            } else {
                done = true;
                entry.verify();
                return -1;
            }
        }

        @Override
        public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            if (done) {
                return -1;
            }
            if (count > 0) {
                int r = super.read(buffer, byteOffset, byteCount);
                if (r != -1) {
                    int size = r;
                    if (count < size) {
                        size = (int) count;
                    }
                    entry.write(buffer, byteOffset, size);
                    count -= size;
                } else {
                    count = 0;
                }
                if (count == 0) {
                    done = true;
                    entry.verify();
                }
                return r;
            } else {
                done = true;
                entry.verify();
                return -1;
            }
        }

        @Override
        public int available() throws IOException {
            if (done) {
                return 0;
            }
            return super.available();
        }

        @Override
        public long skip(long byteCount) throws IOException {
            return Streams.skipByReading(this, byteCount);
        }
    }

    static final class JarFileEnumerator implements Enumeration<JarEntry> {
        final Enumeration<? extends ZipEntry> ze;
        final JarFile jf;

        JarFileEnumerator(Enumeration<? extends ZipEntry> zenum, JarFile jf) {
            ze = zenum;
            this.jf = jf;
        }

        public boolean hasMoreElements() {
            return ze.hasMoreElements();
        }

        public JarEntry nextElement() {
            return new JarEntry(ze.nextElement(), jf /* parentJar */);
        }
    }

    /**
     * Create a new {@code JarFile} using the contents of the specified file.
     *
     * @param file
     *            the JAR file as {@link File}.
     * @throws IOException
     *             If the file cannot be read.
     */
    public JarFile(File file) throws IOException {
        this(file, true);
    }

    /**
     * Create a new {@code JarFile} using the contents of the specified file.
     *
     * @param file
     *            the JAR file as {@link File}.
     * @param verify
     *            if this JAR file is signed whether it must be verified.
     * @throws IOException
     *             If the file cannot be read.
     */
    public JarFile(File file, boolean verify) throws IOException {
        this(file, verify, ZipFile.OPEN_READ);
    }

    /**
     * Create a new {@code JarFile} using the contents of file.
     *
     * @param file
     *            the JAR file as {@link File}.
     * @param verify
     *            if this JAR filed is signed whether it must be verified.
     * @param mode
     *            the mode to use, either {@link ZipFile#OPEN_READ OPEN_READ} or
     *            {@link ZipFile#OPEN_DELETE OPEN_DELETE}.
     * @throws IOException
     *             If the file cannot be read.
     */
    public JarFile(File file, boolean verify, int mode) throws IOException {
        super(file, mode);

        // Step 1: Scan the central directory for meta entries (MANIFEST.mf
        // & possibly the signature files) and read them fully.
        HashMap<String, byte[]> metaEntries = readMetaEntries(this, verify);

        // Step 2: Construct a verifier with the information we have.
        // Verification is possible *only* if the JAR file contains a manifest
        // *AND* it contains signing related information (signature block
        // files and the signature files).
        //
        // TODO: Is this really the behaviour we want if verify == true ?
        // We silently skip verification for files that have no manifest or
        // no signatures.
        if (verify && metaEntries.containsKey(MANIFEST_NAME) &&
                metaEntries.size() > 1) {
            // We create the manifest straight away, so that we can create
            // the jar verifier as well.
            manifest = new Manifest(metaEntries.get(MANIFEST_NAME), true);
            verifier = new JarVerifier(getName(), manifest, metaEntries);
        } else {
            verifier = null;
            manifestBytes = metaEntries.get(MANIFEST_NAME);
        }
    }

    /**
     * Create a new {@code JarFile} from the contents of the file specified by
     * filename.
     *
     * @param filename
     *            the file name referring to the JAR file.
     * @throws IOException
     *             if file name cannot be opened for reading.
     */
    public JarFile(String filename) throws IOException {
        this(filename, true);
    }

    /**
     * Create a new {@code JarFile} from the contents of the file specified by
     * {@code filename}.
     *
     * @param filename
     *            the file name referring to the JAR file.
     * @param verify
     *            if this JAR filed is signed whether it must be verified.
     * @throws IOException
     *             If file cannot be opened or read.
     */
    public JarFile(String filename, boolean verify) throws IOException {
        this(new File(filename), verify, ZipFile.OPEN_READ);
    }

    /**
     * Return an enumeration containing the {@code JarEntrys} contained in this
     * {@code JarFile}.
     *
     * @return the {@code Enumeration} containing the JAR entries.
     * @throws IllegalStateException
     *             if this {@code JarFile} is closed.
     */
    @Override
    public Enumeration<JarEntry> entries() {
        return new JarFileEnumerator(super.entries(), this);
    }

    /**
     * Return the {@code JarEntry} specified by its name or {@code null} if no
     * such entry exists.
     *
     * @param name
     *            the name of the entry in the JAR file.
     * @return the JAR entry defined by the name.
     */
    public JarEntry getJarEntry(String name) {
        return (JarEntry) getEntry(name);
    }

    /**
     * Returns the {@code Manifest} object associated with this {@code JarFile}
     * or {@code null} if no MANIFEST entry exists.
     *
     * @return the MANIFEST.
     * @throws IOException
     *             if an error occurs reading the MANIFEST file.
     * @throws IllegalStateException
     *             if the jar file is closed.
     * @see Manifest
     */
    public Manifest getManifest() throws IOException {
        if (closed) {
            throw new IllegalStateException("JarFile has been closed");
        }

        if (manifest != null) {
            return manifest;
        }

        // If manifest == null && manifestBytes == null, there's no manifest.
        if (manifestBytes == null) {
            return null;
        }

        // We hit this code path only if the verification isn't necessary. If
        // we did decide to verify this file, we'd have created the Manifest and
        // the associated Verifier in the constructor itself.
        manifest = new Manifest(manifestBytes, false);
        manifestBytes = null;

        return manifest;
    }

    /**
     * Called by the JarFile constructors, Reads the contents of the
     * file's META-INF/ directory and picks out the MANIFEST.MF file and
     * verifier signature files if they exist.
     *
     * @throws IOException
     *             if there is a problem reading the jar file entries.
     * @return a map of entry names to their {@code byte[]} content.
     */
    static HashMap<String, byte[]> readMetaEntries(ZipFile zipFile,
            boolean verificationRequired) throws IOException {
        // Get all meta directory entries
        List<ZipEntry> metaEntries = getMetaEntries(zipFile);

        HashMap<String, byte[]> metaEntriesMap = new HashMap<String, byte[]>();

        for (ZipEntry entry : metaEntries) {
            String entryName = entry.getName();
            // Is this the entry for META-INF/MANIFEST.MF ?
            //
            // TODO: Why do we need the containsKey check ? Shouldn't we discard
            // files that contain duplicate entries like this as invalid ?.
            if (entryName.equalsIgnoreCase(MANIFEST_NAME) &&
                    !metaEntriesMap.containsKey(MANIFEST_NAME)) {

                metaEntriesMap.put(MANIFEST_NAME, Streams.readFully(
                        zipFile.getInputStream(entry)));

                // If there is no verifier then we don't need to look any further.
                if (!verificationRequired) {
                    break;
                }
            } else if (verificationRequired) {
                // Is this an entry that the verifier needs?
                if (endsWithIgnoreCase(entryName, ".SF")
                        || endsWithIgnoreCase(entryName, ".DSA")
                        || endsWithIgnoreCase(entryName, ".RSA")
                        || endsWithIgnoreCase(entryName, ".EC")) {
                    InputStream is = zipFile.getInputStream(entry);
                    metaEntriesMap.put(entryName.toUpperCase(Locale.US), Streams.readFully(is));
                }
            }
        }

        return metaEntriesMap;
    }

    private static boolean endsWithIgnoreCase(String s, String suffix) {
        return s.regionMatches(true, s.length() - suffix.length(), suffix, 0, suffix.length());
    }

    /**
     * Return an {@code InputStream} for reading the decompressed contents of
     * ZIP entry.
     *
     * @param ze
     *            the ZIP entry to be read.
     * @return the input stream to read from.
     * @throws IOException
     *             if an error occurred while creating the input stream.
     */
    @Override
    public InputStream getInputStream(ZipEntry ze) throws IOException {
        if (manifestBytes != null) {
            getManifest();
        }

        if (verifier != null) {
            if (verifier.readCertificates()) {
                verifier.removeMetaEntries();
                manifest.removeChunks();

                if (!verifier.isSignedJar()) {
                    verifier = null;
                }
            }
        }

        InputStream in = super.getInputStream(ze);
        if (in == null) {
            return null;
        }
        if (verifier == null || ze.getSize() == -1) {
            return in;
        }
        JarVerifier.VerifierEntry entry = verifier.initEntry(ze.getName());
        if (entry == null) {
            return in;
        }
        return new JarFileInputStream(in, ze.getSize(), entry);
    }

    /**
     * Return the {@code JarEntry} specified by name or {@code null} if no such
     * entry exists.
     *
     * @param name
     *            the name of the entry in the JAR file.
     * @return the ZIP entry extracted.
     */
    @Override
    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze == null) {
            return ze;
        }
        return new JarEntry(ze, this /* parentJar */);
    }

    /**
     * Returns all the ZipEntry's that relate to files in the
     * JAR's META-INF directory.
     */
    private static List<ZipEntry> getMetaEntries(ZipFile zipFile) {
        List<ZipEntry> list = new ArrayList<ZipEntry>(8);

        Enumeration<? extends ZipEntry> allEntries = zipFile.entries();
        while (allEntries.hasMoreElements()) {
            ZipEntry ze = allEntries.nextElement();
            if (ze.getName().startsWith(META_DIR)
                    && ze.getName().length() > META_DIR.length()) {
                list.add(ze);
            }
        }

        return list;
    }

    /**
     * Closes this {@code JarFile}.
     *
     * @throws IOException
     *             if an error occurs.
     */
    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
    }
}
