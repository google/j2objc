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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import libcore.io.Streams;

/**
 * The {@code Manifest} class is used to obtain attribute information for a
 * {@code JarFile} and its entries.
 */
public class Manifest implements Cloneable {
    static final int LINE_LENGTH_LIMIT = 72;

    private static final byte[] LINE_SEPARATOR = new byte[] { '\r', '\n' };

    private static final byte[] VALUE_SEPARATOR = new byte[] { ':', ' ' };

    /* non-final for {@code #clone()} */
    private Attributes mainAttributes;
    /* non-final for {@code #clone()} */
    private HashMap<String, Attributes> entries;

    static final class Chunk {
        final int start;
        final int end;

        Chunk(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private HashMap<String, Chunk> chunks;

    /**
     * The end of the main attributes section in the manifest is needed in
     * verification.
     */
    private int mainEnd;

    /**
     * Creates a new {@code Manifest} instance.
     */
    public Manifest() {
        entries = new HashMap<String, Attributes>();
        mainAttributes = new Attributes();
    }

    /**
     * Creates a new {@code Manifest} instance using the attributes obtained
     * from the input stream.
     *
     * @param is
     *            {@code InputStream} to parse for attributes.
     * @throws IOException
     *             if an IO error occurs while creating this {@code Manifest}
     */
    public Manifest(InputStream is) throws IOException {
        this();
        read(Streams.readFully(is));
    }

    /**
     * Creates a new {@code Manifest} instance. The new instance will have the
     * same attributes as those found in the parameter {@code Manifest}.
     *
     * @param man
     *            {@code Manifest} instance to obtain attributes from.
     */
    @SuppressWarnings("unchecked")
    public Manifest(Manifest man) {
        cloneAttributesAndEntriesFrom(man);
    }

    Manifest(byte[] manifestBytes, boolean readChunks) throws IOException {
        this();
        if (readChunks) {
            chunks = new HashMap<String, Chunk>();
        }
        read(manifestBytes);
    }

    /**
     * Resets the both the main attributes as well as the entry attributes
     * associated with this {@code Manifest}.
     */
    public void clear() {
        entries.clear();
        mainAttributes.clear();
    }

    /**
     * Returns the {@code Attributes} associated with the parameter entry
     * {@code name}.
     *
     * @param name
     *            the name of the entry to obtain {@code Attributes} from.
     * @return the Attributes for the entry or {@code null} if the entry does
     *         not exist.
     */
    public Attributes getAttributes(String name) {
        return getEntries().get(name);
    }

    /**
     * Returns a map containing the {@code Attributes} for each entry in the
     * {@code Manifest}.
     *
     * @return the map of entry attributes.
     */
    public Map<String, Attributes> getEntries() {
        return entries;
    }

    /**
     * Returns the main {@code Attributes} of the {@code JarFile}.
     *
     * @return main {@code Attributes} associated with the source {@code
     *         JarFile}.
     */
    public Attributes getMainAttributes() {
        return mainAttributes;
    }

    /**
     * Creates a copy of this {@code Manifest}. The returned {@code Manifest}
     * will equal the {@code Manifest} from which it was cloned.
     *
     * @return a copy of this instance.
     */
    @Override
    public Object clone() {
        Manifest result;
        try {
            result = (Manifest) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }

        result.cloneAttributesAndEntriesFrom(this);
        return result;
    }

    private final void cloneAttributesAndEntriesFrom(Manifest other) {
        mainAttributes = (Attributes) other.mainAttributes.clone();
        entries = (HashMap<String, Attributes>) ((HashMap<String, Attributes>) other
                .getEntries()).clone();
    }

    /**
     * Writes this {@code Manifest}'s name/attributes pairs to the given {@code OutputStream}.
     * The {@code MANIFEST_VERSION} or {@code SIGNATURE_VERSION} attribute must be set before
     * calling this method, or no attributes will be written.
     *
     * @throws IOException
     *             If an error occurs writing the {@code Manifest}.
     */
    public void write(OutputStream os) throws IOException {
        write(this, os);
    }

    /**
     * Merges name/attribute pairs read from the input stream {@code is} into this manifest.
     *
     * @param is
     *            The {@code InputStream} to read from.
     * @throws IOException
     *             If an error occurs reading the manifest.
     */
    public void read(InputStream is) throws IOException {
        read(Streams.readFullyNoClose(is));
    }

    private void read(byte[] buf) throws IOException {
        if (buf.length == 0) {
            return;
        }

        ManifestReader im = new ManifestReader(buf, mainAttributes);
        mainEnd = im.getEndOfMainSection();
        im.readEntries(entries, chunks);
    }

    /**
     * Returns the hash code for this instance.
     *
     * @return this {@code Manifest}'s hashCode.
     */
    @Override
    public int hashCode() {
        return mainAttributes.hashCode() ^ getEntries().hashCode();
    }

    /**
     * Determines if the receiver is equal to the parameter object. Two {@code
     * Manifest}s are equal if they have identical main attributes as well as
     * identical entry attributes.
     *
     * @param o
     *            the object to compare against.
     * @return {@code true} if the manifests are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        if (!mainAttributes.equals(((Manifest) o).mainAttributes)) {
            return false;
        }
        return getEntries().equals(((Manifest) o).getEntries());
    }

    Chunk getChunk(String name) {
        return chunks.get(name);
    }

    void removeChunks() {
        chunks = null;
    }

    int getMainAttributesEnd() {
        return mainEnd;
    }

    /**
     * Writes out the attribute information of the specified manifest to the
     * specified {@code OutputStream}
     *
     * @param manifest
     *            the manifest to write out.
     * @param out
     *            The {@code OutputStream} to write to.
     * @throws IOException
     *             If an error occurs writing the {@code Manifest}.
     */
    static void write(Manifest manifest, OutputStream out) throws IOException {
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        ByteBuffer buffer = ByteBuffer.allocate(LINE_LENGTH_LIMIT);

        Attributes.Name versionName = Attributes.Name.MANIFEST_VERSION;
        String version = manifest.mainAttributes.getValue(versionName);
        if (version == null) {
            versionName = Attributes.Name.SIGNATURE_VERSION;
            version = manifest.mainAttributes.getValue(versionName);
        }
        if (version != null) {
            writeEntry(out, versionName, version, encoder, buffer);
            Iterator<?> entries = manifest.mainAttributes.keySet().iterator();
            while (entries.hasNext()) {
                Attributes.Name name = (Attributes.Name) entries.next();
                if (!name.equals(versionName)) {
                    writeEntry(out, name, manifest.mainAttributes.getValue(name), encoder, buffer);
                }
            }
        }
        out.write(LINE_SEPARATOR);
        Iterator<String> i = manifest.getEntries().keySet().iterator();
        while (i.hasNext()) {
            String key = i.next();
            writeEntry(out, Attributes.Name.NAME, key, encoder, buffer);
            Attributes attributes = manifest.entries.get(key);
            Iterator<?> entries = attributes.keySet().iterator();
            while (entries.hasNext()) {
                Attributes.Name name = (Attributes.Name) entries.next();
                writeEntry(out, name, attributes.getValue(name), encoder, buffer);
            }
            out.write(LINE_SEPARATOR);
        }
    }

    private static void writeEntry(OutputStream os, Attributes.Name name,
            String value, CharsetEncoder encoder, ByteBuffer bBuf) throws IOException {
        String nameString = name.getName();
        os.write(nameString.getBytes(StandardCharsets.US_ASCII));
        os.write(VALUE_SEPARATOR);

        encoder.reset();
        bBuf.clear().limit(LINE_LENGTH_LIMIT - nameString.length() - 2);

        CharBuffer cBuf = CharBuffer.wrap(value);

        while (true) {
            CoderResult r = encoder.encode(cBuf, bBuf, true);
            if (CoderResult.UNDERFLOW == r) {
                r = encoder.flush(bBuf);
            }
            os.write(bBuf.array(), bBuf.arrayOffset(), bBuf.position());
            os.write(LINE_SEPARATOR);
            if (CoderResult.UNDERFLOW == r) {
                break;
            }
            os.write(' ');
            bBuf.clear().limit(LINE_LENGTH_LIMIT - 1);
        }
    }
}
