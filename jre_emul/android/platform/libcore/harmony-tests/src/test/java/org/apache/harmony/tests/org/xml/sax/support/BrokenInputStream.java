/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tests.org.xml.sax.support;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements an InputStream what wraps another InputStream and throws an
 * IOException after having read a specified number of bytes. Used for
 * injecting IOExceptions on lower levels.
 */
public class BrokenInputStream extends InputStream {

    private InputStream stream;

    private int offset;

    public BrokenInputStream(InputStream stream, int offset) {
        super();

        this.stream = stream;
        this.offset = offset;
    }

    @Override
    public int read() throws IOException {
        if (offset == 0) {
            throw new IOException("Injected exception");
        }

        offset--;
        return stream.read();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}