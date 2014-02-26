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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import junit.framework.Assert;
import junit.framework.TestCase;

public class OldAndroidPushbackInputStreamTest extends TestCase {

    public void testPushbackInputStream() throws Exception {
        String str = "AbCdEfGhIjKlM\nOpQrStUvWxYz";
        ByteArrayInputStream aa = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ba = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream ca = new ByteArrayInputStream(str.getBytes());

        PushbackInputStream a = new PushbackInputStream(aa, 7);
        try {
            a.unread("push".getBytes());
            Assert.assertEquals("pushAbCdEfGhIjKlM\nOpQrStUvWxYz", read(a));
        } finally {
            a.close();
        }

        PushbackInputStream b = new PushbackInputStream(ba, 9);
        try {
            b.unread('X');
            Assert.assertEquals("XAbCdEfGhI", read(b, 10));
        } finally {
            b.close();
        }

        PushbackInputStream c = new PushbackInputStream(ca);
        try {
            Assert.assertEquals("bdfhjl\nprtvxz", skipRead(c));
        } finally {
            c.close();
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

    public static String read(InputStream a, int x) throws IOException {
        byte[] b = new byte[x];
        int len = a.read(b, 0, x);
        if (len < 0) {
            return "";
        }
        return new String(b, 0, len);
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

    public static String markRead(InputStream a, int x, int y) throws IOException {
        int m = 0;
        int r;
        StringBuilder builder = new StringBuilder();
        do {
            m++;
            r = a.read();
            if (m == x)
                a.mark((x + y));
            if (m == (x + y))
                a.reset();

            if (r != -1)
                builder.append((char) r);
        } while (r != -1);
        return builder.toString();
    }
}
