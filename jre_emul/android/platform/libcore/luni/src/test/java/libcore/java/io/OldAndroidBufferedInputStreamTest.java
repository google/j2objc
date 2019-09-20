/*
 * Copyright (C) 2008 The Android Open Source Project
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests to verify that simple functionality works for BufferedInputStreams.
 */
public class OldAndroidBufferedInputStreamTest extends TestCase {

    public void testBufferedInputStream() throws Exception {
        String str = "AbCdEfGhIjKlM\nOpQrStUvWxYz";
        ByteArrayInputStream aa = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ba = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ca = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream da = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ea = new ByteArrayInputStream(str.getBytes());

        BufferedInputStream a = new BufferedInputStream(aa, 6);
        try {
            Assert.assertEquals(str, read(a));
        } finally {
            a.close();
        }

        BufferedInputStream b = new BufferedInputStream(ba, 7);
        try {
            Assert.assertEquals("AbCdEfGhIj", read(b, 10));
        } finally {
            b.close();
        }

        BufferedInputStream c = new BufferedInputStream(ca, 9);
        try {
            assertEquals("bdfhjl\nprtvxz", skipRead(c));
        } finally {
            c.close();
        }

        BufferedInputStream d = new BufferedInputStream(da, 9);
        try {
            assertEquals('A', d.read());
            d.mark(15);
            assertEquals('b', d.read());
            assertEquals('C', d.read());
            d.reset();
            assertEquals('b', d.read());
        } finally {
            d.close();
        }

        BufferedInputStream e = new BufferedInputStream(ea, 11);
        try {
            // test that we can ask for more than is present, and that we'll get
            // back only what is there.
            assertEquals(str, read(e, 10000));
        } finally {
            e.close();
        }
    }

    public static String read(InputStream a) throws IOException {
        int r;
        StringBuilder builder = new StringBuilder();
        do {
            r = a.read();
            if (r != -1)
                builder.append((char) r);
        } while (r != -1);
        return builder.toString();
    }

    public static String skipRead(InputStream a) throws IOException {
        int r;
        StringBuilder builder = new StringBuilder();
        do {
            a.skip(1);
            r = a.read();
            if (r != -1)
                builder.append((char) r);
        } while (r != -1);
        return builder.toString();
    }

    public static String read(InputStream a, int x) throws IOException {
        byte[] b = new byte[x];
        int len = a.read(b, 0, x);
        if (len < 0) {
            return "";
        }
        return new String(b, 0, len);
    }
}
