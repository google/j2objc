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
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringReader;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Checks basic functionality for LineNumberReader.
 */
public class OldAndroidLineNumberReaderTest extends TestCase {

    public void testLineNumberReader() throws Exception {
        String str = "AbCdEfGhIjKlM\nOpQrStUvWxYz";

        StringReader aa = new StringReader(str);
        StringReader ba = new StringReader(str);
        StringReader ca = new StringReader(str);
        StringReader da = new StringReader(str);
        StringReader ea = new StringReader(str);

        LineNumberReader a = new LineNumberReader(aa);
        try {
            assertEquals(0, a.getLineNumber());
            Assert.assertEquals(str, read(a));
            assertEquals(1, a.getLineNumber());
            a.setLineNumber(5);
            assertEquals(5, a.getLineNumber());
        } finally {
            a.close();
        }

        LineNumberReader b = new LineNumberReader(ba);
        try {
            Assert.assertEquals("AbCdEfGhIj", read(b, 10));
        } finally {
            b.close();
        }

        LineNumberReader c = new LineNumberReader(ca);
        try {
            Assert.assertEquals("bdfhjl\nprtvxz", skipRead(c));
        } finally {
            c.close();
        }

        LineNumberReader d = new LineNumberReader(da);
        try {
            Assert.assertEquals("AbCdEfGdEfGhIjKlM\nOpQrStUvWxYz", markRead(d, 3, 4));
        } finally {
            d.close();
        }

        LineNumberReader e = new LineNumberReader(ea);
        try {
            assertEquals("AbCdEfGhIjKlM", e.readLine());
        } finally {
            e.close();
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

    public static String markRead(Reader a, int x, int y) throws IOException {
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
