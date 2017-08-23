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
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import junit.framework.TestCase;

/**
 * Tests for Pipe.SinkChannel class
 */
public class SinkChannelTest extends TestCase {

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
	 * @tests java.nio.channels.Pipe.SinkChannel#validOps()
	 */
	public void test_validOps() {
		assertEquals(SelectionKey.OP_WRITE, sink.validOps());
	}

	/**
	 * @tests java.nio.channels.Pipe.SinkChannel#write(ByteBuffer)
	 */
	public void test_write_LByteBuffer() throws IOException {
		ByteBuffer[] bufArray = { buffer, positionedBuffer };
		boolean[] sinkBlockingMode = { true, true, false, false };
		boolean[] sourceBlockingMode = { true, false, true, false };
		int oldPosition;
		int currentPosition;
		for (int i = 0; i < sinkBlockingMode.length; ++i) {
			sink.configureBlocking(sinkBlockingMode[i]);
			source.configureBlocking(sourceBlockingMode[i]);
			// if sink and source both are blocking mode, source only needs read
			// once to get what sink write.
			boolean isBlocking = sinkBlockingMode[i] && sourceBlockingMode[i];
			for (ByteBuffer buf : bufArray) {
				buf.mark();
				oldPosition = buf.position();
				sink.write(buf);
				ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
				int totalCount = 0;
				do {
					int count = source.read(readBuf);
					if (count > 0) {
						totalCount += count;
					}
				} while (totalCount != BUFFER_SIZE && !isBlocking);
				currentPosition = buf.position();
				assertEquals(BUFFER_SIZE, currentPosition - oldPosition);
				assertEquals("bytes", new String(readBuf.array(), ISO8859_1));
				buf.reset();
			}
		}
	}

	/**
	 * @tests java.nio.channels.Pipe.SinkChannel#write(ByteBuffer)
	 */
	public void test_write_LByteBuffer_mutliThread() throws IOException,
	        InterruptedException {
        final int THREAD_NUM = 20;
        final byte[] strbytes = "bytes".getBytes(ISO8859_1);
        Thread[] thread = new Thread[THREAD_NUM];
        for (int i = 0; i < THREAD_NUM; i++) {
            thread[i] = new Thread() {
                public void run() {
                    try {
                        sink.write(ByteBuffer.wrap(strbytes));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        for (int i = 0; i < THREAD_NUM; i++) {
            thread[i].start();
        }
        for (int i = 0; i < THREAD_NUM; i++) {
            thread[i].join();
        }
        ByteBuffer readBuf = ByteBuffer.allocate(THREAD_NUM * BUFFER_SIZE);

        long totalCount = 0;
        do {
            long count = source.read(readBuf);
            if (count < 0) {
                break;
            }
            totalCount += count;
        } while (totalCount != (THREAD_NUM * BUFFER_SIZE));

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < THREAD_NUM; i++) {
            buf.append("bytes");
        }
        String readString = buf.toString();
        assertEquals(readString, new String(readBuf.array(), ISO8859_1));
	}

	/**
	 * @tests java.nio.channels.Pipe.SinkChannel#write(ByteBuffer)
	 */
	public void test_write_LByteBuffer_Exception() throws IOException {
		// write null ByteBuffer
		ByteBuffer nullBuf = null;
		try {
			sink.write(nullBuf);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

    /* TODO(user): fix b/64675894.
    public void test_write_LByteBuffer_SourceClosed() throws IOException {
        source.close();
        try {
            int written = sink.write(buffer);
            fail();
        } catch (IOException expected) {
        }
    }
    */

    public void test_write_LByteBuffer_SinkClosed() throws IOException {
        sink.close();
        try {
            sink.write(buffer);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException expected) {
        }
    }

	/**
	 * @tests java.nio.channels.Pipe.SinkChannel#write(ByteBuffer[])
	 */
	public void test_write_$LByteBuffer() throws IOException {
		ByteBuffer[] bufArray = { buffer, positionedBuffer };
		boolean[] sinkBlockingMode = { true, true, false, false };
		boolean[] sourceBlockingMode = { true, false, true, false };
		for (int i = 0; i < sinkBlockingMode.length; ++i) {
			sink.configureBlocking(sinkBlockingMode[i]);
			source.configureBlocking(sourceBlockingMode[i]);
			buffer.position(0);
			positionedBuffer.position(BUFFER_SIZE);
			sink.write(bufArray);
			// if sink and source both are blocking mode, source only needs read
			// once to get what sink write.
			boolean isBlocking = sinkBlockingMode[i] && sourceBlockingMode[i];
			for (int j = 0; j < bufArray.length; ++j) {
				ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
				int totalCount = 0;
				do {
					int count = source.read(readBuf);
					if (count < 0) {
						break;
					}
					totalCount += count;
				} while (totalCount != BUFFER_SIZE && !isBlocking);
				assertEquals("bytes", new String(readBuf.array(), ISO8859_1));
			}
			assertEquals(BUFFER_SIZE, buffer.position());
			assertEquals(10, positionedBuffer.position());
		}
	}

	/**
	 * @tests java.nio.channels.Pipe.SinkChannel#write(ByteBuffer[])
	 */
	public void test_write_$LByteBuffer_Exception() throws IOException {
		// write null ByteBuffer[]
		ByteBuffer[] nullBufArrayRef = null;
		try {
			sink.write(nullBufArrayRef);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		// write ByteBuffer[] contains null element
		ByteBuffer nullBuf = null;
		ByteBuffer[] nullBufArray = { buffer, nullBuf };
		try {
			sink.write(nullBufArray);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

    /* TODO(user): fix and enable.
    public void test_write_$LByteBuffer_SourceClosed() throws IOException {
        ByteBuffer[] bufArray = { buffer };
        source.close();
        try {
            long written = sink.write(bufArray);
            fail();
        } catch (IOException expected) {
        }
    }
    */

	/**
	 * @tests java.nio.channels.Pipe.SinkChannel#write(ByteBuffer[])
	 */
	public void test_write_$LByteBuffer_SinkClosed() throws IOException {
		ByteBuffer[] bufArray = { buffer };
		sink.close();
		try {
			sink.write(bufArray);
			fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}

		ByteBuffer[] nullBufArrayRef = null;
		try {
			sink.write(nullBufArrayRef);
			fail("should throw NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}

		ByteBuffer nullBuf = null;
		ByteBuffer[] nullBufArray = { nullBuf };
		// write ByteBuffer[] contains null element
		try {
			sink.write(nullBufArray);
			fail("should throw ClosedChannelException");
		} catch (ClosedChannelException e) {
			// expected
		}
	}

	/**
	 * @tests java.nio.channels.Pipe.SinkChannel#write(ByteBuffer[], int, int)
	 */
	public void test_write_$LByteBufferII() throws IOException {
		ByteBuffer[] bufArray = { buffer, positionedBuffer };
		boolean[] sinkBlockingMode = { true, true, false, false };
		boolean[] sourceBlockingMode = { true, false, true, false };
		for (int i = 0; i < sinkBlockingMode.length; ++i) {
			sink.configureBlocking(sinkBlockingMode[i]);
			source.configureBlocking(sourceBlockingMode[i]);
			positionedBuffer.position(BUFFER_SIZE);
			sink.write(bufArray, 1, 1);
			// if sink and source both are blocking mode, source only needs read
			// once to get what sink write.
			boolean isBlocking = sinkBlockingMode[i] && sourceBlockingMode[i];
			ByteBuffer readBuf = ByteBuffer.allocate(BUFFER_SIZE);
			int totalCount = 0;
			do {
				int count = source.read(readBuf);
				if (count < 0) {
					break;
				}
				totalCount += count;
			} while (totalCount != BUFFER_SIZE && !isBlocking);
			assertEquals("bytes", new String(readBuf.array(), ISO8859_1));
			assertEquals(10, positionedBuffer.position());
		}
	}

    public void test_write_$LByteBufferII_Exception() throws IOException {
        try {
            sink.write(null, 0, 1);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            sink.write(new ByteBuffer[2], 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        // write ByteBuffer[] contains null element
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray = { nullBuf };
        try {
            sink.write(nullBufArray, 0, 1);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        try {
            sink.write(nullBufArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        ByteBuffer[] bufArray = { buffer, nullBuf };
        try {
            sink.write(bufArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    /* TODO(user): fix and enable.
    public void test_write_$LByteBufferII_SourceClosed() throws IOException {
        ByteBuffer[] bufArray = { buffer };
        source.close();

        try {
            long written = sink.write(bufArray, 0, 1);
            fail();
        } catch (IOException expected) {
        }
    }
    */

    public void test_write_$LByteBufferII_SinkClosed() throws IOException {
        ByteBuffer[] bufArray = { buffer };
        sink.close();
        try {
            sink.write(bufArray, 0, 1);
            fail();
        } catch (ClosedChannelException expected) {
        }

        try {
            sink.write(null, 0, 1);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            sink.write(new ByteBuffer[2], 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }

        // write ByteBuffer[] contains null element
        ByteBuffer nullBuf = null;
        ByteBuffer[] nullBufArray = { nullBuf };
        try {
            sink.write(nullBufArray, 0, 1);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
        // illegal array index
        try {
            sink.write(nullBufArray, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        ByteBuffer[] bufArray2 = { buffer, nullBuf };
        // illegal array index
        try {
            sink.write(bufArray2, 0, -1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray2, -1, 0);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray2, -1, 1);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray2, 0, 3);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        try {
            sink.write(bufArray2, 0, 2);
            fail("should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // expected
        }
    }

    public void test_close() throws IOException {
        sink.close();
        assertFalse(sink.isOpen());
    }

    public void test_socketChannel_read_close() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
          ssc.socket()
              .bind(new InetSocketAddress(InetAddress.getLocalHost(), 0 /* any free port */));
        } catch (BindException e) {
          // Continuous build environment doesn't support localhost sockets.
          return;
        }
        int localPort = ssc.socket().getLocalPort();
        SocketChannel sc = SocketChannel.open();
        ByteBuffer buf = null;
        try{
            sc.write(buf);
            fail("should throw NPE");
        }catch (NullPointerException e){
            // expected
        }
        sc.connect(new InetSocketAddress(InetAddress.getLocalHost(), localPort));
        SocketChannel sock = ssc.accept();
        ssc.close();
        sc.close();
        try{
            sc.write(buf);
            fail("should throw NPE");
        }catch (NullPointerException e){
            // expected
        }
        sock.close();
    }

    public void test_socketChannel_read_write() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
          ssc.socket()
              .bind(new InetSocketAddress(InetAddress.getLocalHost(), 0 /* any free port */));
        } catch (BindException e) {
          // Continuous build environment doesn't support localhost sockets.
          return;
        }
        int localPort = ssc.socket().getLocalPort();
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress(InetAddress.getLocalHost(), localPort));
        SocketChannel sock = ssc.accept();
        ByteBuffer[] buf = {ByteBuffer.allocate(10),null};
        try {
            sc.write(buf,0,2);
            fail("should throw NPE");
        } catch (NullPointerException expected) {
        }
        ssc.close();
        sc.close();
        ByteBuffer target = ByteBuffer.allocate(10);
        assertEquals(-1, sock.read(target));
    }
}
