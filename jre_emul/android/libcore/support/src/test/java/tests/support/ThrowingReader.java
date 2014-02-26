/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tests.support;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * A reader that always throws after a predetermined number of bytes have been
 * read.
 */
public class ThrowingReader extends FilterReader {

    private int total = 0;
    private int throwAt;

    public ThrowingReader(Reader in, int throwAt) {
        super(in);
        this.throwAt = throwAt;
    }

    @Override public int read() throws IOException {
        explodeIfNecessary();
        int result = super.read();
        total++;
        return result;
    }

    @Override public int read(char[] buf, int offset, int count)
            throws IOException {
        explodeIfNecessary();

        if (total < throwAt) {
            count = Math.min(count, (throwAt - total));
        }

        int returned = super.read(buf, offset, count);
        total += returned;
        return returned;
    }

    private void explodeIfNecessary() throws IOException {
        if (total == throwAt) {
            throwAt = Integer.MAX_VALUE;
            throw new IOException();
        }
    }
}
