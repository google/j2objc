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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import junit.framework.TestCase;

public final class InputStreamReaderTest extends TestCase {

    /**
     * This bug claims that InputStreamReader blocks unnecessarily:
     * http://code.google.com/p/android/issues/detail?id=10252
     */
    public void testReadDoesNotBlockUnnecessarily() throws IOException {
        PipedInputStream pin = new PipedInputStream();
        PipedOutputStream pos = new PipedOutputStream(pin);
        pos.write("hello".getBytes("UTF-8"));

        InputStreamReader reader = new InputStreamReader(pin);
        char[] buffer = new char[1024];
        int count = reader.read(buffer);
        assertEquals(5, count);
    }
}
