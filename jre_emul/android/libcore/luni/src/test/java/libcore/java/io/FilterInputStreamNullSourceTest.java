/*
 * Copyright (C) 2010 The Android Open Source Project
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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberInputStream;
import java.io.PushbackInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import javax.crypto.CipherInputStream;
import javax.crypto.NullCipher;
import junit.framework.TestCase;

public final class FilterInputStreamNullSourceTest extends TestCase {

    public void testBufferedInputStream() throws IOException {
        assertReadsFailWithIoException(new BufferedInputStream(null));
        assertReadsFailWithIoException(new BufferedInputStream(null, 1024));
    }

    public void testCheckedInputStream() throws IOException {
        assertReadsFailWithNullPointerException(new CheckedInputStream(null, new CRC32()));
    }

    public void testCipherInputStream() throws IOException {
        InputStream in = new CipherInputStream(null, new NullCipher());
        try {
            in.read();
            fail();
        } catch (NullPointerException expected) {
        }

        assertEquals(0, in.available());

        try {
            in.close();
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testDataInputStream() throws IOException {
        assertReadsFailWithNullPointerException(new DataInputStream(null));
    }

    public void testDigestInputStream() throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        assertReadsFailWithNullPointerException(new DigestInputStream(null, md5));
    }

    public void testFilterInputStream() throws IOException {
        assertReadsFailWithNullPointerException(new FilterInputStream(null) {});
    }

    public void testInflaterInputStream() throws IOException {
        try {
            new InflaterInputStream(null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            new InflaterInputStream(null, new Inflater());
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            new InflaterInputStream(null, new Inflater(), 1024);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testLineNumberInputStream() throws IOException {
        assertReadsFailWithNullPointerException(new LineNumberInputStream(null));
    }

    public void testPushbackInputStream() throws IOException {
        assertReadsFailWithIoException(new PushbackInputStream(null));
        assertReadsFailWithIoException(new PushbackInputStream(null, 1024));
    }

    private void assertReadsFailWithIoException(InputStream in) throws IOException {
        try {
            in.read();
            fail();
        } catch (IOException expected) {
        }

        try {
            in.available();
            fail();
        } catch (IOException expected) {
        }

        in.close();
    }

    private void assertReadsFailWithNullPointerException(InputStream in) throws IOException {
        try {
            in.read();
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            in.available();
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            in.close();
            fail();
        } catch (NullPointerException expected) {
        }
    }
}
