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
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests to verify that simple functionality works for ByteArrayInputStreams.
 */
public class OldAndroidByteArrayInputStreamTest extends TestCase {

    public void testByteArrayInputStream() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";

        ByteArrayInputStream a = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream b = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream c = new ByteArrayInputStream(str.getBytes());
        ByteArrayInputStream d = new ByteArrayInputStream(str.getBytes());

        Assert.assertEquals(str, read(a));
        Assert.assertEquals("AbCdEfGhIj", read(b, 10));
        Assert.assertEquals("bdfhjlnprtvxz", skipRead(c));
        Assert.assertEquals("AbCdEfGdEfGhIjKlMnOpQrStUvWxYz", markRead(d, 3, 4));
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
