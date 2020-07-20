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
import java.io.Writer;

public class Support_StringWriter extends Writer {
	private StringBuffer buf;

	/**
	 * Constructs a new StringWriter which has a StringBuffer allocated with the
	 * default size of 16 characters. The StringBuffer is also the
	 * <code>lock</code> used to synchronize access to this Writer.
	 */
	public Support_StringWriter() {
		super();
		buf = new StringBuffer(16);
		lock = buf;
	}

	/**
	 * Constructs a new StringWriter which has a StringBuffer allocated with the
	 * size of <code>initialSize</code> characters. The StringBuffer is also
	 * the <code>lock</code> used to synchronize access to this Writer.
	 */
	public Support_StringWriter(int initialSize) {
		if (initialSize >= 0) {
			buf = new StringBuffer(initialSize);
			lock = buf;
		} else {
            throw new IllegalArgumentException();
        }
	}

	/**
	 * Close this Writer. This is the concrete implementation required. This
	 * particular implementation does nothing.
	 * 
	 * @exception java.io.IOException
	 *                If an IO error occurs closing this StringWriter.
	 */
	@Override
    public void close() throws IOException {
	}

	/**
	 * Flush this Writer. This is the concrete implementation required. This
	 * particular implementation does nothing.
	 * 
	 */
	@Override
    public void flush() {
	}

	/**
	 * Answer the contents of this StringWriter as a StringBuffer. Any changes
	 * made to the StringBuffer by the receiver or the caller are reflected in
	 * this StringWriter.
	 * 
	 * @return this StringWriters local StringBuffer.
	 */
	public StringBuffer getBuffer() {
		synchronized (lock) {
			return buf;
		}
	}

	/**
	 * Answer the contents of this StringWriter as a String. Any changes made to
	 * the StringBuffer by the receiver after returning will not be reflected in
	 * the String returned to the caller.
	 * 
	 * @return this StringWriters current contents as a String.
	 */
	@Override
    public String toString() {
		synchronized (lock) {
			return buf.toString();
		}
	}

	/**
	 * Writes <code>count</code> characters starting at <code>offset</code>
	 * in <code>buf</code> to this StringWriter.
	 * 
	 * @param buf
	 *            the non-null array containing characters to write.
	 * @param offset
	 *            offset in buf to retrieve characters
	 * @param count
	 *            maximum number of characters to write
	 * 
	 * @exception java.lang.ArrayIndexOutOfBoundsException
	 *                If offset or count are outside of bounds.
	 */
	@Override
    public void write(char[] buf, int offset, int count) {
		// avoid int overflow
		if (0 <= offset && offset <= buf.length && 0 <= count
				&& count <= buf.length - offset) {
			synchronized (lock) {
				this.buf.append(buf, offset, count);
			}
		} else {
            throw new ArrayIndexOutOfBoundsException();
        }
	}

	/**
	 * Writes the specified character <code>oneChar</code> to this
	 * StringWriter. This implementation writes the low order two bytes to the
	 * Stream.
	 * 
	 * @param oneChar
	 *            The character to write
	 * 
	 */
	@Override
    public void write(int oneChar) {
		synchronized (lock) {
			buf.append((char) oneChar);
		}
	}

	/**
	 * Writes the characters from the String <code>str</code> to this
	 * StringWriter.
	 * 
	 * @param str
	 *            the non-null String containing the characters to write.
	 * 
	 */
	@Override
    public void write(String str) {
		synchronized (lock) {
			buf.append(str);
		}
	}

	/**
	 * Writes <code>count</code> number of characters starting at
	 * <code>offset</code> from the String <code>str</code> to this
	 * StringWriter.
	 * 
	 * @param str
	 *            the non-null String containing the characters to write.
	 * @param offset
	 *            the starting point to retrieve characters.
	 * @param count
	 *            the number of characters to retrieve and write.
	 * 
	 * @exception java.lang.ArrayIndexOutOfBoundsException
	 *                If offset or count are outside of bounds.
	 */
	@Override
    public void write(String str, int offset, int count) {
		String sub = str.substring(offset, offset + count);
		synchronized (lock) {
			buf.append(sub);
		}
	}
}
