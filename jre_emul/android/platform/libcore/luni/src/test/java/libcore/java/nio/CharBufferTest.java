/*
 * Copyright (C) 2016 The Android Open Source Project
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


package libcore.java.nio;

import junit.framework.TestCase;

import java.nio.CharBuffer;
import java.util.Arrays;

public class CharBufferTest extends TestCase {

    public void testChars() {
        char highSurrogate = '\uD83D', lowSurrogate = '\uDE02';
        String s = "Hello\n\tworld" + highSurrogate + lowSurrogate;
        CharBuffer cb = CharBuffer.allocate(32).append(s);
        cb.rewind();
        int[] expected = new int[s.length()];
        for (int i = 0; i < s.length(); ++i) {
            expected[i] = (int) s.charAt(i);
        }
        assertTrue(Arrays.equals(expected, cb.chars().limit(s.length()).toArray()));
    }

    public void testCodePoints() {
        String s = "Hello\n\tworld";
        CharBuffer cb = CharBuffer.allocate(32).append(s);
        cb.rewind();
        int[] expected = new int[s.length()];
        for (int i = 0; i < s.length(); ++i) {
            expected[i] = (int) s.charAt(i);}
        assertTrue(Arrays.equals(expected, cb.codePoints().limit(s.length()).toArray()));

        // Surrogate code point
        char high = '\uD83D', low = '\uDE02';
        String surrogateCP = new String(new char[]{high, low, low, '0'});
        cb = CharBuffer.allocate(32).append(surrogateCP);
        cb.rewind();
        assertEquals(Character.toCodePoint(high, low), cb.codePoints().toArray()[0]);
        assertEquals((int) low, cb.codePoints().toArray()[1]); // Unmatched surrogate.
        assertEquals((int) '0', cb.codePoints().toArray()[2]);
    }
}
