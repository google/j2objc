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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import junit.framework.Assert;
import junit.framework.TestCase;

public class OldAndroidPushbackReaderTest extends TestCase {

    public void testPushbackReader() throws Exception {
        String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(str);
        StringReader ca = new StringReader(str);

        PushbackReader a = new PushbackReader(aa, 5);
        try {
            a.unread("PUSH".toCharArray());
            Assert.assertEquals("PUSHAbCdEfGhIjKlMnOpQrStUvWxYz", read(a));
        } finally {
            a.close();
        }

        PushbackReader b = new PushbackReader(ba, 15);
        try {
            b.unread('X');
            Assert.assertEquals("XAbCdEfGhI", read(b, 10));
        } finally {
            b.close();
        }

        PushbackReader c = new PushbackReader(ca);
        try {
            Assert.assertEquals("bdfhjlnprtvxz", skipRead(c));
        } finally {
            c.close();
        }
    }

    public static String read(Reader a) throws IOException {
        int r;
        StringBuilder builder = new StringBuilder();
        do {
            r = a.read();
            if (r != -1)
                builder.append((char) r);
        } while (r != -1);
        return builder.toString();
    }

    public static String read(Reader a, int x) throws IOException {
        char[] b = new char[x];
        int len = a.read(b, 0, x);
        if (len < 0) {
            return "";
        }
        return new String(b, 0, len);
    }

    public static String skipRead(Reader a) throws IOException {
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
}
