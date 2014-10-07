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

import java.io.*;
import java.util.Arrays;
import junit.framework.TestCase;

public class OutputStreamWriterTest extends TestCase {
    private class FlushCountingOutputStream extends OutputStream {
        int flushCount;
        public void write(int b) {
        }
        @Override public void flush() throws IOException {
            ++flushCount;
        }
    }

    public void testFlushCount() throws Exception {
        FlushCountingOutputStream os = new FlushCountingOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
        char[] chars = new char[16*1024];
        Arrays.fill(chars, 'x');
        for (int i = 0; i < 10; ++i) {
            writer.write(chars);
        }
        assertEquals(0, os.flushCount);
        writer.flush();
        assertEquals(1, os.flushCount);
        writer.close();
        assertEquals(1, os.flushCount);
    }
}
