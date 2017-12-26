/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package org.apache.harmony.tests.java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;

import junit.framework.TestCase;

/**
 * Tests for java.nio.channels.Pipe.SourceChannel
 */
public class SourceChannelTest extends TestCase {

	private static final int BUFFER_SIZE = 5;

	private static final String ISO8859_1 = "ISO8859-1";

	private Pipe pipe;

	private Pipe.SinkChannel sink;

	private Pipe.SourceChannel source;

	private ByteBuffer buffer;

	private ByteBuffer positionedBuffer;

	protected void setUp() throws Exception {
		super.setUp();
		pipe = Pipe.open();
		sink = pipe.sink();
		source = pipe.source();
		buffer = ByteBuffer.wrap("bytes".getBytes(ISO8859_1));
		positionedBuffer = ByteBuffer.wrap("12345bytes".getBytes(ISO8859_1));
		positionedBuffer.position(BUFFER_SIZE);
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#validOps()
	 */
	public void test_validOps() {
		assertEquals(SelectionKey.OP_READ, source.validOps());
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_LByteBuffer_DataAvailable() throws IOException {
		// if anything can read, read method will not block
		sink.write(ByteBuffer.allocate(1));
		int count = source.read(ByteBuffer.allocate(10));
		assertEquals(1, count);
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_LByteBuffer_Exception() throws IOException {
		ByteBuffer nullBuf = null;
		try {
			source.read(nullBuf);
            fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_LByteBuffer_SinkClosed() throws IOException {
		ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
		sink.write(buffer);
		sink.close();
		long count = source.read(readBuf);
		assertEquals(BUFFER_SIZE, count);
		// readBuf is full, read 0 byte expected
		count = source.read(readBuf);
		assertEquals(0, count);
		// readBuf is not null, -1 is expected
		readBuf.position(0);
		count = source.read(readBuf);
		assertEquals(-1, count);
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_LByteBuffer_SourceClosed() throws IOException {
		ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
		source.close();
		try {
			source.read(readBuf);
            fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}
		readBuf.position(BUFFER_SIZE);
		try {
			// readBuf is full
			source.read(readBuf);
            fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}

		ByteBuffer nullBuf = null;
		try {
			source.read(nullBuf);
            fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
        
        ByteBuffer[] bufArray = null; 
        try {
            source.read(bufArray);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        
        ByteBuffer[] nullBufArray = {nullBuf}; 
        try {
            source.read(nullBufArray);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
	}

	/**
     * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer[])
     */
    public void test_read_$LByteBuffer() throws IOException {
        ByteBuffer[] bufArray = { buffer, positionedBuffer };
        boolean[] sinkBlockingMode = { true, true, false, false };
        boolean[] sourceBlockingMode = { true, false, true, false };
        for (int i = 0; i < sinkBlockingMode.length; ++i) {
            // open new pipe everytime, will be closed in finally block
            pipe = Pipe.open();
            sink = pipe.sink();
            source = pipe.source();
            sink.configureBlocking(sinkBlockingMode[i]);
            source.configureBlocking(sourceBlockingMode[i]);
            buffer.position(0);
            positionedBuffer.position(BUFFER_SIZE);
            try {
                long writeCount = sink.write(bufArray);
                assertEquals(10, writeCount);
                // invoke close to ensure all data will be sent out
                sink.close();
                // read until EOF is meet or readBufArray is full.
                ByteBuffer[] readBufArray = { ByteBuffer.allocate(BUFFER_SIZE),
                        ByteBuffer.allocate(BUFFER_SIZE) };
                long totalCount = 0;
                do {
                    long count = source.read(readBufArray);
                    if (count < 0) {
                        break;
                    }
                    if (0 == count && BUFFER_SIZE == readBufArray[1].position()) {
                        // source.read returns 0 because readBufArray is full
                        break;
                    }
                    totalCount += count;
                } while (totalCount <= 10);
                // assert read result
                for (ByteBuffer readBuf : readBufArray) {
                    // RI may fail because of its bug implementation
                    assertEquals(BUFFER_SIZE, readBuf.position());
                    assertEquals("bytes",
                            new String(readBuf.array(), ISO8859_1));
                }
            } finally {
                // close pipe everytime
                sink.close();
                source.close();
            }
        }
    }
	
	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_$LByteBuffer_Exception() throws IOException {
		ByteBuffer[] nullBufArrayRef = null;
		try {
			source.read(nullBufArrayRef);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		// ByteBuffer array contains null element
		ByteBuffer nullBuf = null;
		ByteBuffer[] nullBufArray1 = { nullBuf };
		try {
			source.read(nullBufArray1);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		ByteBuffer[] nullBufArray2 = { buffer, nullBuf };
		try {
			source.read(nullBufArray2);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_$LByteBuffer_SinkClosed() throws IOException {
		ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
		ByteBuffer[] readBufArray = { readBuf };
		sink.write(buffer);
		sink.close();
		long count = source.read(readBufArray);
		assertEquals(BUFFER_SIZE, count);
		// readBuf is full, read 0 byte expected
		count = source.read(readBufArray);
		assertEquals(0, count);
		// readBuf is not null, -1 is expected
		readBuf.position(0);
        assertTrue(readBuf.hasRemaining());
		count = source.read(readBufArray);
		assertEquals(-1, count);
	}
    
	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_$LByteBuffer_SourceClosed() throws IOException {
		ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
		ByteBuffer[] readBufArray = { readBuf };
		source.close();
		try {
			source.read(readBufArray);
            fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}
		readBuf.position(BUFFER_SIZE);
		try {
			// readBuf is full
			source.read(readBufArray);
            fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}

		ByteBuffer[] nullBufArrayRef = null;
		try {
			source.read(nullBufArrayRef);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		// ByteBuffer array contains null element
		ByteBuffer nullBuf = null;
		ByteBuffer[] nullBufArray1 = { nullBuf };
		try {
			source.read(nullBufArray1);
			fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}
	}

	/**
     * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer[], int, int)
     */
    public void test_read_$LByteBufferII() throws IOException {
        ByteBuffer[] bufArray = { buffer, positionedBuffer };
        boolean[] sinkBlockingMode = { true, true, false, false };
        boolean[] sourceBlockingMode = { true, false, true, false };
        for (int i = 0; i < sinkBlockingMode.length; ++i) {
            Pipe pipe = Pipe.open();
            sink = pipe.sink();
            source = pipe.source();

            sink.configureBlocking(sinkBlockingMode[i]);
            source.configureBlocking(sourceBlockingMode[i]);

            buffer.position(0);
            positionedBuffer.position(BUFFER_SIZE);
            try {
                sink.write(bufArray);
                // invoke close to ensure all data will be sent out
                sink.close();
                // read until EOF is meet or readBufArray is full.
                ByteBuffer[] readBufArray = { ByteBuffer.allocate(BUFFER_SIZE),
                        ByteBuffer.allocate(BUFFER_SIZE) };
                long totalCount = 0;
                do {
                    long count = source.read(readBufArray, 0, 2);
                    if (count < 0) {
                        break;
                    }
                    if (0 == count && BUFFER_SIZE == readBufArray[1].position()) {
                        // source.read returns 0 because readBufArray is full
                        break;
                    }
                    totalCount += count;
                } while (totalCount != 10);

                // assert read result
                for (ByteBuffer readBuf : readBufArray) {
                    // RI may fail because of its bug implementation
                    assertEquals(BUFFER_SIZE, readBuf.position());
                    assertEquals("bytes",
                            new String(readBuf.array(), ISO8859_1));
                }
            } finally {
                sink.close();
                source.close();
            }
        }
    }

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_$LByteBufferII_Exception() throws IOException {

		ByteBuffer[] nullBufArrayRef = null;
		try {
			source.read(nullBufArrayRef, 0, 1);
			fail();
		} catch (NullPointerException expected) {
		}

		try {
			source.read(nullBufArrayRef, 0, -1);
			fail();
		} catch (NullPointerException expected) {
		} catch (IndexOutOfBoundsException expected) {
		}

		try {
			source.read(new ByteBuffer[0], 0, -1);
			fail();
		} catch (IndexOutOfBoundsException expected) {
		}

		try {
			source.read(new ByteBuffer[0], -1, 0);
			fail();
		} catch (IndexOutOfBoundsException expected) {
		}

		// ByteBuffer array contains null element
		ByteBuffer nullBuf = null;
		ByteBuffer[] nullBufArray1 = { nullBuf };
		try {
			source.read(nullBufArray1, 0, 1);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
		try {
			source.read(nullBufArray1, 0, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		try {
			source.read(nullBufArray1, -1, 0);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		try {
			source.read(nullBufArray1, -1, 1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		ByteBuffer[] nullBufArray2 = { buffer, nullBuf };

		try {
			source.read(nullBufArray1, 1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		try {
			source.read(nullBufArray2, 0, 3);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		try {
			source.read(nullBufArray2, 0, 2);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_$LByteBufferII_SinkClosed() throws IOException {
		ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
		ByteBuffer[] readBufArray = { readBuf };
		sink.write(buffer);
		sink.close();
		long count = source.read(readBufArray, 0, 1);
		assertEquals(BUFFER_SIZE, count);
		// readBuf is full, read 0 byte expected
		count = source.read(readBufArray);
		assertEquals(0, count);
		// readBuf is not null, -1 is expected
		readBuf.position(0);
		count = source.read(readBufArray, 0, 1);
		assertEquals(-1, count);
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#read(ByteBuffer)
	 */
	public void test_read_$LByteBufferII_SourceClosed() throws IOException {
		ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
		ByteBuffer[] readBufArray = { readBuf };
		source.close();
		try {
			source.read(readBufArray, 0, 1);
			fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}
		readBuf.position(BUFFER_SIZE);
		try {
			// readBuf is full
			source.read(readBufArray, 0, 1);
			fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}

		ByteBuffer[] nullBufArrayRef = null;
		try {
			source.read(nullBufArrayRef, 0, 1);
			fail();
		} catch (NullPointerException expected) {
		}

		try {
			source.read(nullBufArrayRef, 0, -1);
			fail();
		} catch (NullPointerException expected) {
		} catch (IndexOutOfBoundsException expected) {
		}

		try {
			source.read(new ByteBuffer[0], 0, -1);
			fail();
		} catch (IndexOutOfBoundsException expected) {
		}

		try {
			source.read(new ByteBuffer[0], -1, 1);
			fail();
		} catch (IndexOutOfBoundsException expected) {
		}

		// ByteBuffer array contains null element
		ByteBuffer nullBuf = null;
		ByteBuffer[] nullBufArray1 = { nullBuf };
		try {
			source.read(nullBufArray1, 0, 1);
			fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}
		try {
			source.read(nullBufArray1, 0, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		try {
			source.read(nullBufArray1, -1, 0);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}
		try {
			source.read(nullBufArray1, -1, 1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		ByteBuffer[] nullBufArray2 = { buffer, nullBuf };

		try {
			source.read(nullBufArray1, 1, -1);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		try {
			source.read(nullBufArray2, 0, 3);
			fail("should throw IndexOutOfBoundsException");
		} catch (IndexOutOfBoundsException e) {
			// expected
		}

		try {
			source.read(nullBufArray2, 0, 2);
			fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}
	}

	/**
	 * @tests java.nio.channels.Pipe.SourceChannel#close()
	 */
	public void test_close() throws IOException {
		sink.close();
		assertFalse(sink.isOpen());
	}

}
