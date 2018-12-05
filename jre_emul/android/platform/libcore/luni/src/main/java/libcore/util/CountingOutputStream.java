/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that keeps count of the number of bytes written to it.
 *
 * Useful when we need to make decisions based on the size of the output, such
 * as deciding what sort of metadata to writes to zip files.
 */
public class CountingOutputStream extends FilterOutputStream {

    private long count;

    /**
     * Constructs a new {@code FilterOutputStream} with {@code out} as its
     * target stream.
     *
     * @param out the target stream that this stream writes to.
     */
    public CountingOutputStream(OutputStream out) {
        super(out);
        count = 0;
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        out.write(buffer, offset, length);
        count += length;
    }

    @Override
    public void write(int oneByte) throws IOException {
        out.write(oneByte);
        count++;
    }

    public long getCount() {
        return count;
    }
}
