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

package java.io;

/**
 * A class for turning a byte stream into a character stream. Data read from the
 * source input stream is converted into characters by either a default or a
 * provided character converter. The default encoding is taken from the
 * "file.encoding" system property. {@code InputStreamReader} contains a buffer
 * of bytes read from the source stream and converts these into characters as
 * needed. The buffer size is 8K.
 * 
 * @see OutputStreamWriter
 */
public class InputStreamReader extends Reader {
    private InputStream in;
    private int encoding;
    private String backingStore;
    private int currentIndex;

    /**
     * Constructs a new {@code InputStreamReader} on the {@link InputStream}
     * {@code in}. This constructor sets the character converter to the encoding
     * specified in the "file.encoding" property and falls back to ISO 8859_1
     * (ISO-Latin-1) if the property doesn't exist.
     * 
     * @param in
     *            the input stream from which to read characters.
     */
    public InputStreamReader(InputStream in) {
        super(in);
        this.in = in;
        encoding = 5;  // NSISOLatin1StringEncoding documented enum value.
    }

    /**
     * Constructs a new InputStreamReader on the InputStream {@code in}. The
     * character converter that is used to decode bytes into characters is
     * identified by name by {@code enc}. If the encoding cannot be found, an
     * UnsupportedEncodingException error is thrown.
     * 
     * @param in
     *            the InputStream from which to read characters.
     * @param enc
     *            identifies the character converter to use.
     * @throws NullPointerException
     *             if {@code enc} is {@code null}.
     * @throws UnsupportedEncodingException
     *             if the encoding specified by {@code enc} cannot be found.
     */
    public InputStreamReader(InputStream in, final String enc)
            throws UnsupportedEncodingException {
        super(in);
        if (enc == null) {
            throw new NullPointerException();
        }
        this.in = in;
        encoding = getOSXEncoding(enc);
        if (encoding == -1) {
            throw new UnsupportedEncodingException(enc);
        }
    }

    static int getOSXEncoding(String enc) {
	// String encoding enum values are from NSString documentation.
	if (enc.equalsIgnoreCase("ASCII") || enc.equalsIgnoreCase("US-ASCII")) {
	    return 1; // NSASCIIStringEncoding
	}
	if (enc.equalsIgnoreCase("EUC_JP")) {
	    return 3; // NSJapaneseEUCStringEncoding
	}
	if (enc.equalsIgnoreCase("UTF8") || enc.equalsIgnoreCase("UTF-8")) {
	    return 4; // NSUTF8StringEncoding
	}
	if (enc.equalsIgnoreCase("8859_1") || enc.equalsIgnoreCase("ISO8859_1") ||
	        enc.equalsIgnoreCase("ISO-8859-1")) {
	    return 5; // NSISOLatin1StringEncoding
	}
	if (enc.equalsIgnoreCase("ISO8859_2")) {
	    return 9; // NSISOLatin2StringEncoding
	}
	if (enc.equalsIgnoreCase("UTF-16")) {
	    return 10; // NSUTF16StringEncoding, NSUnicodeStringEncoding
	}
	if (enc.equalsIgnoreCase("UTF-16BE")) {
	    return 0x90000100; // NSUTF16BigEndianStringEncoding
	}
	if (enc.equalsIgnoreCase("UTF-16LE")) {
	    return 0x94000100; // NSUTF16LittleEndianStringEncoding
	}
	return -1;
    }

    /**
     * Closes this reader. This implementation closes the source InputStream and
     * releases all local storage.
     * 
     * @throws IOException
     *             if an error occurs attempting to close this reader.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (in != null) {
                in.close();
                in = null;
            }
        }
    }

    /**
     * Returns the name of the encoding used to convert bytes into characters.
     * The value {@code null} is returned if this reader has been closed.
     * 
     * @return the name of the character converter or {@code null} if this
     *         reader is closed.
     */
    public String getEncoding() {
        if (!isOpen()) {
            return null;
        }
        return nativeEncodingName(encoding);
    }
    
    static native String nativeEncodingName(int encoding) /*-{
      switch (encoding) {
        case NSASCIIStringEncoding:
          return @"ASCII";
        case NSISOLatin1StringEncoding:
          return @"ISO8859_1";
        case NSUTF8StringEncoding:
          return @"UTF8";
        case NSUnicodeStringEncoding:
          return @"UTF-16";
        case NSUTF16BigEndianStringEncoding:
          return @"UnicodeBigUnmarked";
        case NSUTF16LittleEndianStringEncoding:
          return @"UnicodeLittleUnmarked";
        default:
          return nil;
      }
    }-*/;

    /**
     * Reads a single character from this reader and returns it as an integer
     * with the two higher-order bytes set to 0. Returns -1 if the end of the
     * reader has been reached. The byte value is either obtained from
     * converting bytes in this reader's buffer or by first filling the buffer
     * from the source InputStream and then reading from the buffer.
     * 
     * @return the character read or -1 if the end of the reader has been
     *         reached.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (!isOpen()) {
                throw new IOException("InputStreamReader is closed.");
            }

            char buf[] = new char[1];
            return read(buf, 0, 1) != -1 ? buf[0] : -1;
        }
    }

    /**
     * Reads at most {@code length} characters from this reader and stores them
     * at position {@code offset} in the character array {@code buf}. Returns
     * the number of characters actually read or -1 if the end of the reader has
     * been reached. The bytes are either obtained from converting bytes in this
     * reader's buffer or by first filling the buffer from the source
     * InputStream and then reading from the buffer.
     * 
     * @param buf
     *            the array to store the characters read.
     * @param offset
     *            the initial position in {@code buf} to store the characters
     *            read from this reader.
     * @param length
     *            the maximum number of characters to read.
     * @return the number of characters read or -1 if the end of the reader has
     *         been reached.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code length < 0}, or if
     *             {@code offset + length} is greater than the length of
     *             {@code buf}.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        synchronized (lock) {
            if (!isOpen()) {
                throw new IOException("InputStreamReader is closed.");
            }
            if (offset < 0 || offset > buf.length - length || length < 0) {
                throw new IndexOutOfBoundsException();
            }
            if (length == 0) {
                return 0;
            }

            if (backingStore == null) {
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) > -1) {
                    byteArray.write(buffer, 0, len);
                }
                backingStore = convertToString(byteArray.toByteArray());
                currentIndex = 0;
            }

            if (currentIndex == backingStore.length()) {
        	return -1;
            }
            int n = Math.min(length, backingStore.length() - currentIndex);
            backingStore.getChars(currentIndex, currentIndex + n, buf, offset);
            currentIndex += n;
            return n;
        }
    }

    /**
     * Convert bytes to String using NSString.  This is necessary because
     * Cocoa doesn't have the equivalent of a byte-to-character decoder.
     */
    private native String convertToString(byte[] byteArray) /*-{
      NSUInteger length = [byteArray count];
      char *buffer = malloc(length);
      [byteArray getBytes:buffer offset:0 length:length];
      NSString *result = [[NSString alloc] initWithBytes:buffer length:length encoding:encoding_];
      free(buffer);
#if ! __has_feature(objc_arc)
      [result autorelease];
#endif
      return result;
    }-*/;

    /*
     * Answer a boolean indicating whether or not this InputStreamReader is
     * open.
     */
    private boolean isOpen() {
        return in != null;
    }

    /**
     * Indicates whether this reader is ready to be read without blocking. If
     * the result is {@code true}, the next {@code read()} will not block. If
     * the result is {@code false} then this reader may or may not block when
     * {@code read()} is called. This implementation returns {@code true} if
     * there are bytes available in the buffer or the source stream has bytes
     * available.
     * 
     * @return {@code true} if the receiver will not block when {@code read()}
     *         is called, {@code false} if unknown or blocking will occur.
     * @throws IOException
     *             if this reader is closed or some other I/O error occurs.
     */
    @Override
    public boolean ready() throws IOException {
        if (in == null) {
            throw new IOException("InputStreamReader is closed.");
        }
        return backingStore == null || backingStore.length() - currentIndex > 0;
    }
}
