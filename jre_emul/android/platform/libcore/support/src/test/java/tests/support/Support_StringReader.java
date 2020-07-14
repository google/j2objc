/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tests.support;

import java.io.IOException;
import java.io.Reader;

public class Support_StringReader extends Reader {
	private String str;

	private int markpos = -1;

	private int pos = 0;

	private int count;

	/**
	 * Construct a StringReader on the String <code>str</code>. The size of
	 * the reader is set to the <code>length()</code> of the String and the
	 * Object to synchronize access through is set to <code>str</code>.
	 * 
	 * @param str
	 *            the String to filter reads on.
	 */
	public Support_StringReader(String str) {
		super(str);
		this.str = str;
		this.count = str.length();
	}

	/**
	 * This method closes this StringReader. Once it is closed, you can no
	 * longer read from it. Only the first invocation of this method has any
	 * effect.
	 * 
	 */
	@Override
    public void close() {
		synchronized (lock) {
			if (isOpen()) {
                str = null;
            }
		}
	}

	/**
	 * Answer a boolean indicating whether or not this StringReader is open.
	 */
	private boolean isOpen() {
		return str != null;
	}

	/**
	 * Set a Mark position in this Reader. The parameter <code>readLimit</code>
	 * is ignored for StringReaders. Sending reset() will reposition the reader
	 * back to the marked position provided the mark has not been invalidated.
	 * 
	 * @param readlimit
	 *            ignored for StringReaders.
	 * 
	 * @exception java.io.IOException
	 *                If an error occurs attempting mark this StringReader.
	 */
	@Override
    public void mark(int readLimit) throws IOException {
		if (readLimit >= 0) {
			synchronized (lock) {
				if (isOpen()) {
                    markpos = pos;
                } else {
                    throw new IOException("StringReader is closed");
                }
			}
		} else {
            throw new IllegalArgumentException();
        }
	}

	/**
	 * Answers a boolean indicating whether or not this StringReader supports
	 * mark() and reset(). This method always returns true.
	 * 
	 * @return <code>true</code> if mark() and reset() are supported,
	 *         <code>false</code> otherwise. This implementation always
	 *         returns <code>true</code>.
	 */
	@Override
    public boolean markSupported() {
		return true;
	}

	/**
	 * Reads a single character from this StringReader and returns the result as
	 * an int. The 2 higher-order bytes are set to 0. If the end of reader was
	 * encountered then return -1.
	 * 
	 * @return the character read or -1 if end of reader.
	 * 
	 * @exception java.io.IOException
	 *                If the StringReader is already closed.
	 */
	@Override
    public int read() throws IOException {
		synchronized (lock) {
			if (isOpen()) {
				if (pos != count) {
                    return str.charAt(pos++);
                }
				return -1;
			}
            throw new IOException("StringReader is closed");
		}
	}

	/**
	 * Reads at most <code>count</code> characters from this StringReader and
	 * stores them at <code>offset</code> in the character array
	 * <code>buf</code>. Returns the number of characters actually read or -1
	 * if the end of reader was encountered.
	 * 
	 * @param buf
	 *            character array to store the read characters
	 * @param offset
	 *            offset in buf to store the read characters
	 * @param count
	 *            maximum number of characters to read
	 * @return the number of characters read or -1 if end of reader.
	 * 
	 * @exception java.io.IOException
	 *                If the StringReader is closed.
	 */
	@Override
    public int read(char buf[], int offset, int count) throws IOException {
		// avoid int overflow
		if (0 <= offset && offset <= buf.length && 0 <= count
				&& count <= buf.length - offset) {
			synchronized (lock) {
				if (isOpen()) {
					if (pos == this.count) {
                        return -1;
                    }
					int end = pos + count > this.count ? this.count : pos
							+ count;
					str.getChars(pos, end, buf, offset);
					int read = end - pos;
					pos = end;
					return read;
				}
                throw new IOException("StringReader is closed");
			}
		}
        throw new ArrayIndexOutOfBoundsException();
	}

	/**
	 * Answers a <code>boolean</code> indicating whether or not this
	 * StringReader is ready to be read without blocking. If the result is
	 * <code>true</code>, the next <code>read()</code> will not block. If
	 * the result is <code>false</code> this Reader may or may not block when
	 * <code>read()</code> is sent. The implementation in StringReader always
	 * returns <code>true</code> even when it has been closed.
	 * 
	 * @return <code>true</code> if the receiver will not block when
	 *         <code>read()</code> is called, <code>false</code> if unknown
	 *         or blocking will occur.
	 * 
	 * @exception java.io.IOException
	 *                If an IO error occurs.
	 */
	@Override
    public boolean ready() throws IOException {
		synchronized (lock) {
			if (isOpen()) {
                return true;
            }
			throw new IOException("StringReader is closed");
		}
	}

	/**
	 * Reset this StringReader's position to the last <code>mark()</code>
	 * location. Invocations of <code>read()/skip()</code> will occur from
	 * this new location. If this Reader was not marked, the StringReader is
	 * reset to the beginning of the String.
	 * 
	 * @exception java.io.IOException
	 *                If this StringReader has already been closed.
	 */
	@Override
    public void reset() throws IOException {
		synchronized (lock) {
			if (isOpen()) {
                pos = markpos != -1 ? markpos : 0;
            } else {
                throw new IOException("StringReader is closed");
            }
		}
	}

	/**
	 * Skips <code>count</code> number of characters in this StringReader.
	 * Subsequent <code>read()</code>'s will not return these characters
	 * unless <code>reset()</code> is used.
	 * 
	 * @param count
	 *            The number of characters to skip.
	 * @return the number of characters actually skipped.
	 * 
	 * @exception java.io.IOException
	 *                If this StringReader has already been closed.
	 */
	@Override
    public long skip(long count) throws IOException {
		synchronized (lock) {
			if (isOpen()) {
				if (count <= 0) {
                    return 0;
                }
				long skipped = 0;
				if (count < this.count - pos) {
					pos = pos + (int) count;
					skipped = count;
				} else {
					skipped = this.count - pos;
					pos = this.count;
				}
				return skipped;
			}
            throw new IOException("StringReader is closed");
		}
	}
}
