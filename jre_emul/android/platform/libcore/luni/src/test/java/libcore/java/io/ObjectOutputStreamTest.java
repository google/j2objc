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

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;

public final class ObjectOutputStreamTest extends TestCase {
    public void testLongString() throws Exception {
        // Most modified UTF-8 is limited to 64KiB, but serialized strings can have an 8-byte
        // length, so this should never throw java.io.UTFDataFormatException...
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 64*1024 * 2; ++i) {
            sb.append('a');
        }
        String s = sb.toString();
        ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
        os.writeObject(s);
    }
}
