/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class CharsetEncoderTest extends junit.framework.TestCase {

    public void testSurrogatePairAllAtOnce() throws Exception {
        // okay: surrogate pair seen all at once is decoded to U+20b9f.
        Charset cs = Charset.forName("UTF-32BE");
        CharsetEncoder e = cs.newEncoder();
        ByteBuffer bb = ByteBuffer.allocate(128);
        CoderResult cr = e.encode(CharBuffer.wrap(new char[] { '\ud842', '\udf9f' }), bb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(4, bb.position());
        assertEquals((byte) 0x00, bb.get(0));
        assertEquals((byte) 0x02, bb.get(1));
        assertEquals((byte) 0x0b, bb.get(2));
        assertEquals((byte) 0x9f, bb.get(3));
    }

    public void testFlushWithoutEndOfInput() throws Exception {
        Charset cs = Charset.forName("UTF-32BE");
        CharsetEncoder e = cs.newEncoder();
        ByteBuffer bb = ByteBuffer.allocate(128);
        CoderResult cr = e.encode(CharBuffer.wrap(new char[] { 'x' }), bb, false);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(4, bb.position());
        try {
            cr = e.flush(bb);
        } catch (IllegalStateException expected) {
            // you must call encode with endOfInput true before you can flush.
        }

        // We had a bug where we wouldn't reset inEnd before calling encode in implFlush.
        // That would result in flush outputting garbage.
        cr = e.encode(CharBuffer.wrap(new char[] { 'x' }), bb, true);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(8, bb.position());
        cr = e.flush(bb);
        assertEquals(CoderResult.UNDERFLOW, cr);
        assertEquals(8, bb.position());
    }
}
