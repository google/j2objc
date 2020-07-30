/*
 * Copyright (C) 2016 The Android Open Source Project
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

package libcore.java.nio.channels;

import junit.framework.TestCase;
import libcore.java.util.ResourceLeakageDetector;
import org.junit.Rule;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
/* J2ObjC removed: unsupported
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
import libcore.junit.util.ResourceLeakageDetector.LeakageDetectorRule;

public class AsynchronousSocketChannelTest extends TestCaseWithRules {
 */
public class AsynchronousSocketChannelTest extends TestCase {

    /* J2ObjC removed: unsupported ResourceLeakageDetector
    @Rule
    public LeakageDetectorRule leakageDetectorRule = ResourceLeakageDetector.getRule();
     */

    // Comfortably smaller than the default TCP socket buffer size to avoid blocking on write.
    final int NON_BLOCKING_MESSAGE_SIZE = 32;

    public void test_connect() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        assertEquals(asc.provider(), AsynchronousChannelProvider.provider());
        assertTrue(asc.isOpen());
        assertNull(asc.getRemoteAddress());
        assertNull(asc.getLocalAddress());

        // Connect
        InetSocketAddress remoteAddress = new InetSocketAddress("localhost", ss.getLocalPort());
        Future<Void> connectFuture = asc.connect(remoteAddress);
        connectFuture.get(1000, TimeUnit.MILLISECONDS);
        Socket s = ss.accept();

        assertNotNull(asc.getLocalAddress());
        assertEquals(asc.getLocalAddress(), s.getRemoteSocketAddress());
        assertNotNull(asc.getRemoteAddress());
        assertEquals(asc.getRemoteAddress(), s.getLocalSocketAddress());

        assertTrue(asc.isOpen());

        asc.close();
        ss.close();
        s.close();
    }

    public void test_bind() throws Exception {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        assertNull(asc.getLocalAddress());
        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        asc.bind(new InetSocketAddress(0));

        assertNotNull(asc.getLocalAddress());
        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        asc.close();
    }

    static class MySocketAddress extends SocketAddress {
        final static long serialVersionUID = 0;
    }

    public void test_bind_unsupportedAddress() throws Exception {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        try {
            asc.bind(new MySocketAddress());
            fail();
        } catch (UnsupportedAddressTypeException  expected) {}

        assertNull(asc.getLocalAddress());
        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        asc.close();
    }


    public void test_bind_unresolvedAddress() throws Exception {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        try {
            asc.bind(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (UnresolvedAddressException expected) {}

        assertNull(asc.getLocalAddress());
        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        asc.close();
    }

    public void test_bind_usedAddress() throws Exception {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();
        ServerSocket ss = new ServerSocket(0);
        try {
            asc.bind(ss.getLocalSocketAddress());
            fail();
        } catch (BindException expected) {}

        assertNull(asc.getLocalAddress());
        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        ss.close();
        asc.close();
    }

    public void test_bind_null() throws Exception {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();
        asc.bind(null);

        assertNotNull(asc.getLocalAddress());
        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        asc.close();
    }

    public void test_connect_unresolvedAddress() throws Exception {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        try {
            asc.connect(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (UnresolvedAddressException expected) {}

        assertNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());
        asc.close();
    }

    public void test_close() throws Throwable {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();
        assertTrue(asc.isOpen());

        asc.close();
        assertFalse(asc.isOpen());

        try {
            asc.getRemoteAddress();
            fail();
        } catch (ClosedChannelException expected) {}
        try {
            asc.getLocalAddress();
            fail();
        } catch (ClosedChannelException expected) {}

        ByteBuffer tmp = createTestByteBuffer(16, false);
        FutureLikeCompletionHandler<Integer> intCompletionHandler = null;
        FutureLikeCompletionHandler<Long> longCompletionHandler = null;

        Future<Integer> readFuture = asc.read(tmp);
        try {
            readFuture.get(1000, TimeUnit.MILLISECONDS);
            fail();
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }

        longCompletionHandler = new FutureLikeCompletionHandler<>();
        asc.read(new ByteBuffer[]{tmp}, 0, 1, 100L, TimeUnit.MILLISECONDS, null, longCompletionHandler);
        try {
            longCompletionHandler.get(1000);
            fail();
        } catch (ClosedChannelException expected) {}


        intCompletionHandler = new FutureLikeCompletionHandler<>();
        asc.read(tmp, null, intCompletionHandler);
        try {
            intCompletionHandler.get(1000);
            fail();
        } catch (ClosedChannelException expected) {}


        intCompletionHandler = new FutureLikeCompletionHandler<>();
        asc.read(tmp, 100, TimeUnit.MILLISECONDS, null, intCompletionHandler);
        try {
            intCompletionHandler.get(100);
            fail();
        } catch (ClosedChannelException expected) {}

        Future<Integer> writeFuture = asc.write(tmp);
        try {
            writeFuture.get(1000, TimeUnit.MILLISECONDS);
            fail();
        } catch (ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }

        longCompletionHandler = new FutureLikeCompletionHandler<>();
        asc.write(new ByteBuffer[]{tmp}, 0, 1, 100, TimeUnit.MILLISECONDS, null, longCompletionHandler);
        try {
            longCompletionHandler.get(1000);
            fail();
        } catch (ClosedChannelException expected) {}

        intCompletionHandler = new FutureLikeCompletionHandler<>();
        asc.write(tmp, null, intCompletionHandler);
        try {
            intCompletionHandler.get(1000);
            fail();
        } catch (ClosedChannelException expected) {}

        intCompletionHandler = new FutureLikeCompletionHandler<>();
        asc.write(tmp, 100, TimeUnit.MILLISECONDS, null, intCompletionHandler);
        try {
            intCompletionHandler.get(1000);
            fail();
        } catch (ClosedChannelException expected) {}

        try {
            asc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            fail();
        } catch(ClosedChannelException expected) {}

        // Try second close
        asc.close();
    }

    public void test_futureReadWrite_HeapButeBuffer() throws Exception {
        test_futureReadWrite(false /* useDirectByteBuffer */);
    }

    public void test_futureReadWrite_DirectByteBuffer() throws Exception {
        test_futureReadWrite(true /* useDirectByteBuffer */);
    }

    private void test_futureReadWrite(boolean useDirectByteBuffer) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        Future<Void> connectFuture = asc.connect(ss.getLocalSocketAddress());
        connectFuture.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(asc.getRemoteAddress());
        assertTrue(connectFuture.isDone());

        // Accept & write data
        final int messageSize = NON_BLOCKING_MESSAGE_SIZE;
        final ByteBuffer sendData = createTestByteBuffer(messageSize, useDirectByteBuffer);
        Socket sss = ss.accept();
        // Small message, won't block on write
        sss.getOutputStream().write(sendData.array(), sendData.arrayOffset(), messageSize);

        // Read data from async channel and call #get on result future
        ByteBuffer receivedData = createTestByteBuffer(messageSize, useDirectByteBuffer);
        assertEquals(messageSize, (int)asc.read(receivedData).get(1000, TimeUnit.MILLISECONDS));

        // Compare results
        receivedData.flip();
        assertEquals(sendData, receivedData);

        // Write data to async channel and call #get on result future
        assertEquals(messageSize, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));

        // Read data and compare with original
        byte[] readArray = new byte[messageSize];
        assertEquals(messageSize, sss.getInputStream().read(readArray));

        // Compare results
        sendData.flip();
        assertEquals(sendData, ByteBuffer.wrap(readArray));

        asc.close();
        sss.close();
        ss.close();
    }

    public void test_completionHandlerReadWrite_HeapByteBuffer() throws Throwable {
        test_completionHandlerReadWrite(false /* useDirectByteBuffer */);
    }

    public void test_completionHandlerReadWrite_DirectByteBuffer() throws Throwable {
        test_completionHandlerReadWrite(true /* useDirectByteBuffer */);
    }

    private void test_completionHandlerReadWrite(boolean useDirectByteBuffer) throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler =
            new FutureLikeCompletionHandler<>();
        Object attachment = new Integer(1);
        asc.connect(ss.getLocalSocketAddress(), attachment, connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());
        assertEquals(attachment, connectCompletionHandler.getAttachment());

        // Accept & write data
        final int messageSize = NON_BLOCKING_MESSAGE_SIZE;
        ByteBuffer sendData = createTestByteBuffer(messageSize, useDirectByteBuffer);
        Socket sss = ss.accept();
        // Small message, won't block on write
        sss.getOutputStream().write(sendData.array(), sendData.arrayOffset(), messageSize);

        // Read data from async channel
        ByteBuffer receivedData = createTestByteBuffer(messageSize, useDirectByteBuffer);
        FutureLikeCompletionHandler<Integer> readCompletionHandler =
            new FutureLikeCompletionHandler<>();
        asc.read(receivedData, attachment, readCompletionHandler);
        assertEquals(messageSize, (int)readCompletionHandler.get(1000));
        assertEquals(attachment, readCompletionHandler.getAttachment());

        // Compare results
        receivedData.flip();
        assertEquals(sendData, receivedData);

        // Write data to async channel
        FutureLikeCompletionHandler<Integer> writeCompletionHandler =
            new FutureLikeCompletionHandler<>();
        asc.write(sendData, attachment, writeCompletionHandler);
        assertEquals(messageSize, (int)writeCompletionHandler.get(1000));
        assertEquals(attachment, writeCompletionHandler.getAttachment());

        // Read data and compare with original
        byte[] readArray = new byte[messageSize];
        assertEquals(messageSize, sss.getInputStream().read(readArray));
        sendData.flip();
        assertEquals(sendData, ByteBuffer.wrap(readArray));

        asc.close();
        sss.close();
        ss.close();
    }


    public void test_scatterReadWrite_HeapByteBuffer() throws Throwable {
        test_scatterReadWrite(false /* useDirectByteBuffer */);
    }

    public void test_scatterReadWrite_DirectByteBuffer() throws Throwable {
        test_scatterReadWrite(true /* useDirectByteBuffer */);
    }

    private void test_scatterReadWrite(boolean useDirectByteBuffer) throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler =
            new FutureLikeCompletionHandler<>();
        Object attachment = new Integer(1);
        asc.connect(ss.getLocalSocketAddress(), attachment, connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());
        assertEquals(attachment, connectCompletionHandler.getAttachment());

        // Accept & write data
        final int messageSize = NON_BLOCKING_MESSAGE_SIZE;
        ByteBuffer sendData1 = createTestByteBuffer(messageSize, useDirectByteBuffer, 0);
        ByteBuffer sendData2 = createTestByteBuffer(messageSize, useDirectByteBuffer, 6);
        Socket sss = ss.accept();

        // Small message, won't block on write
        sss.getOutputStream().write(sendData1.array(), sendData1.arrayOffset(), messageSize);
        sss.getOutputStream().write(sendData2.array(), sendData2.arrayOffset(), messageSize);

        // Read data from async channel
        ByteBuffer receivedData1 = createTestByteBuffer(messageSize, useDirectByteBuffer);
        ByteBuffer receivedData2 = createTestByteBuffer(messageSize, useDirectByteBuffer);
        FutureLikeCompletionHandler<Long> readCompletionHandler =
            new FutureLikeCompletionHandler<>();

        asc.read(new ByteBuffer[]{receivedData1, receivedData2}, 0, 2,
                 1000L, TimeUnit.MILLISECONDS, attachment, readCompletionHandler);
        assertEquals(messageSize * 2L, (long)readCompletionHandler.get(1000));
        assertEquals(attachment, readCompletionHandler.getAttachment());

        // Compare results
        receivedData1.flip();
        assertEquals(sendData1, receivedData1);

        receivedData2.flip();
        assertEquals(sendData2, receivedData2);

        // Write data to async channel
        FutureLikeCompletionHandler<Long> writeCompletionHandler =
            new FutureLikeCompletionHandler<>();
        asc.write(new ByteBuffer[]{sendData1, sendData2}, 0, 2,
                  1000L, TimeUnit.MILLISECONDS, attachment, writeCompletionHandler);
        assertEquals(messageSize*2L, (long)writeCompletionHandler.get(1000));
        assertEquals(attachment, writeCompletionHandler.getAttachment());

        // Read data and compare with original
        byte[] readArray = new byte[messageSize];
        assertEquals(messageSize, sss.getInputStream().read(readArray));
        sendData1.flip();
        assertEquals(sendData1, ByteBuffer.wrap(readArray));

        assertEquals(messageSize, sss.getInputStream().read(readArray));
        sendData2.flip();
        assertEquals(sendData2, ByteBuffer.wrap(readArray));

        asc.close();
        sss.close();
        ss.close();
    }

    public void test_completionHandler_connect_npe() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        FutureLikeCompletionHandler<Void> connectCompletionHandler =
            new FutureLikeCompletionHandler<>();
        // 1st argument NPE
        try {
            asc.connect(null, null, connectCompletionHandler);
            fail();
        } catch(IllegalArgumentException expected) {}

        // 3rd argument NPE
        try {
            asc.connect(ss.getLocalSocketAddress(), null, null);
            fail();
        } catch(NullPointerException expected) {}

        asc.close();
        ss.close();
    }

    public void test_read_npe() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler =
            new FutureLikeCompletionHandler<>();
        asc.connect(ss.getLocalSocketAddress(), null,
                    connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());

        // Read data from async channel
        ByteBuffer receivedData = createTestByteBuffer(32, false);
        FutureLikeCompletionHandler<Integer> intCompletionHandler =
            new FutureLikeCompletionHandler<>();
        FutureLikeCompletionHandler<Long> longCompletionHandler =
            new FutureLikeCompletionHandler<>();

        // 1st argument NPE
        try {
            asc.read(null, null, intCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // 3rd argument NPE
        try {
            asc.read(receivedData, null, null);
            fail();
        } catch(NullPointerException expected) {}

        // With timeout, 1st argument NPE
        try {
            asc.read(null, 100, TimeUnit.MILLISECONDS, null, intCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // With timeout, 5rd argument NPE
        try {
            asc.read(receivedData, 100, TimeUnit.MILLISECONDS, null, null);
            fail();
        } catch(NullPointerException expected) {}

        // Scatter read, 1st argument NPE
        try {
            asc.read(null, 0, 1, 0, TimeUnit.MILLISECONDS, null, longCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // Scatter read, 1st argument NPE
        try {
            asc.read(new ByteBuffer[]{null}, 0, 1, 0, TimeUnit.MILLISECONDS, null,
                     longCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // Scatter read, last argument NPE
        try {
            asc.read(new ByteBuffer[]{receivedData}, 0, 1, 0, TimeUnit.MILLISECONDS, null,
                     null);
            fail();
        } catch(NullPointerException expected) {}

        asc.close();
        ss.close();
    }

    public void test_read_not_connected() throws Throwable {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        ByteBuffer receivedData = createTestByteBuffer(32, false);
        FutureLikeCompletionHandler<Integer> intCompletionHandler =
            new FutureLikeCompletionHandler<>();
        FutureLikeCompletionHandler<Long> longCompletionHandler =
            new FutureLikeCompletionHandler<>();

        try {
            asc.read(receivedData);
            fail();
        } catch(NotYetConnectedException expected) {}

        try {
            asc.read(receivedData, null, intCompletionHandler);
            fail();
        } catch(NotYetConnectedException expected) {}

        try {
            asc.read(receivedData, 100, TimeUnit.MILLISECONDS, null, intCompletionHandler);
            fail();
        } catch(NotYetConnectedException expected) {}

        try {
            asc.read(new ByteBuffer[] {receivedData}, 0, 1, 100, TimeUnit.MILLISECONDS, null,
                     longCompletionHandler);
            fail();
        } catch(NotYetConnectedException expected) {}

        asc.close();
    }

    public void test_read_failures() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler =
            new FutureLikeCompletionHandler<>();
        asc.connect(ss.getLocalSocketAddress(), null,
                    connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());

        ByteBuffer receivedData = createTestByteBuffer(32, false);
        FutureLikeCompletionHandler<Integer> intCompletionHandler =
            new FutureLikeCompletionHandler<>();
        FutureLikeCompletionHandler<Long> longCompletionHandler =
            new FutureLikeCompletionHandler<>();

        ByteBuffer readOnly = receivedData.asReadOnlyBuffer();

        // Read-only future read
        try {
            asc.read(readOnly);
            fail();
        } catch(IllegalArgumentException expected) {}

        // Scatter-read read-only
        try {
            asc.read(new ByteBuffer[] {readOnly}, 0, 1, 100L, TimeUnit.MILLISECONDS, null,
                     longCompletionHandler);
            fail();
        } catch(IllegalArgumentException expected) {}

        // Scatter-read bad offset
        try {
            asc.read(new ByteBuffer[] {receivedData}, -2, 1, 100L, TimeUnit.MILLISECONDS, null,
                     longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}
        try {
            asc.read(new ByteBuffer[] {receivedData}, 3, 1, 100L, TimeUnit.MILLISECONDS, null,
                     longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}
        // Scatter-read bad length
        try {
            asc.read(new ByteBuffer[] {receivedData}, 0, -1, 100L, TimeUnit.MILLISECONDS, null,
                     longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}
        try {
            asc.read(new ByteBuffer[] {receivedData}, 0, 3, 100L, TimeUnit.MILLISECONDS, null,
                     longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}

        // Completion-handler read-only
        try {
            asc.read(readOnly, null, intCompletionHandler);
            fail();
        } catch(IllegalArgumentException expected) {}
        try {
            asc.read(readOnly, 100L, TimeUnit.MILLISECONDS, null, intCompletionHandler);
            fail();
        } catch(IllegalArgumentException expected) {}

        asc.close();
        ss.close();
    }

    public void test_write_npe() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler =
            new FutureLikeCompletionHandler<>();
        asc.connect(ss.getLocalSocketAddress(), null,
                    connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());

        // Read data from async channel
        ByteBuffer receivedData = createTestByteBuffer(32, false);
        FutureLikeCompletionHandler<Integer> intCompletionHandler =
            new FutureLikeCompletionHandler<>();
        FutureLikeCompletionHandler<Long> longCompletionHandler =
            new FutureLikeCompletionHandler<>();

        // 1st argument NPE
        try {
            asc.write(null, null, intCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // 3rd argument NPE
        try {
            asc.write(receivedData, null, null);
            fail();
        } catch(NullPointerException expected) {}

        // With timeout, 1st argument NPE
        try {
            asc.write(null, 100, TimeUnit.MILLISECONDS, null, intCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // With timeout, 5rd argument NPE
        try {
            asc.write(receivedData, 100, TimeUnit.MILLISECONDS, null, null);
            fail();
        } catch(NullPointerException expected) {}

        // Scatter write, 1st argument NPE.
        try {
            asc.write((ByteBuffer[])null, 0, 1, 100L, TimeUnit.MILLISECONDS, null,
                  longCompletionHandler);
            fail();
        } catch(NullPointerException expected) {}

        // Scatter write, 1st argument NPE in array
        // Surprise, it doesn't throw (not symmetric with scatter read)
        asc.write(new ByteBuffer[]{null}, 0, 1, 100L, TimeUnit.MILLISECONDS, null,
                  longCompletionHandler);

        // Scatter write, last argument NPE
        try {
            asc.write(new ByteBuffer[]{receivedData}, 0, 1, 0, TimeUnit.MILLISECONDS, null,
                     null);
            fail();
        } catch(NullPointerException expected) {}

        asc.close();
        ss.close();
    }

    public void test_write_not_connected() throws Throwable {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        ByteBuffer receivedData = createTestByteBuffer(32, false);
        FutureLikeCompletionHandler<Integer> intCompletionHandler =
            new FutureLikeCompletionHandler<>();
        FutureLikeCompletionHandler<Long> longCompletionHandler =
            new FutureLikeCompletionHandler<>();

        try {
            asc.write(receivedData);
            fail();
        } catch(NotYetConnectedException expected) {}

        try {
            asc.write(receivedData, null, intCompletionHandler);
            fail();
        } catch(NotYetConnectedException expected) {}

        try {
            asc.write(receivedData, 100, TimeUnit.MILLISECONDS, null, intCompletionHandler);
            fail();
        } catch(NotYetConnectedException expected) {}

        try {
            asc.write(new ByteBuffer[] {receivedData}, 0, 1, 100, TimeUnit.MILLISECONDS, null,
                      longCompletionHandler);
            fail();
        } catch(NotYetConnectedException expected) {}

        asc.close();
    }

    public void test_write_failures() throws Throwable {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        FutureLikeCompletionHandler<Void> connectCompletionHandler =
            new FutureLikeCompletionHandler<>();
        asc.connect(ss.getLocalSocketAddress(), null,
                    connectCompletionHandler);
        connectCompletionHandler.get(1000);
        assertNotNull(asc.getRemoteAddress());

        ByteBuffer receivedData = createTestByteBuffer(32, false);
        FutureLikeCompletionHandler<Integer> intCompletionHandler =
            new FutureLikeCompletionHandler<>();
        FutureLikeCompletionHandler<Long> longCompletionHandler =
            new FutureLikeCompletionHandler<>();

        // Scatter-write bad offset
        try {
            asc.write(new ByteBuffer[] {receivedData}, -2, 1, 100L, TimeUnit.MILLISECONDS, null,
                      longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}
        try {
            asc.write(new ByteBuffer[] {receivedData}, 3, 1, 100L, TimeUnit.MILLISECONDS, null,
                      longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}
        // Scatter-write bad length
        try {
            asc.write(new ByteBuffer[] {receivedData}, 0, -1, 100L, TimeUnit.MILLISECONDS, null,
                      longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}
        try {
            asc.write(new ByteBuffer[] {receivedData}, 0, 3, 100L, TimeUnit.MILLISECONDS, null,
                      longCompletionHandler);
            fail();
        } catch(IndexOutOfBoundsException expected) {}

        asc.close();
        ss.close();
    }


    public void test_shutdown() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        // Connect
        Future<Void> connectFuture = asc.connect(ss.getLocalSocketAddress());
        connectFuture.get(1000, TimeUnit.MILLISECONDS);
        assertNotNull(asc.getRemoteAddress());

        // Accept & write data
        final int messageSize = NON_BLOCKING_MESSAGE_SIZE;
        ByteBuffer sendData = createTestByteBuffer(messageSize, false);
        Socket sss = ss.accept();
        // Small message, won't block on write
        sss.getOutputStream().write(sendData.array());

        // Shutdown input, expect -1 from read
        asc.shutdownInput();
        ByteBuffer receivedData = createTestByteBuffer(messageSize, false);

        // We did write something into the socket,  #shutdownInput javadocs
        // say that "...effect on an outstanding read operation is system dependent and
        // therefore not specified...". It looks like on android/linux the data in
        // received buffer is discarded.
        assertEquals(-1, (int)asc.read(receivedData).get(1000, TimeUnit.MILLISECONDS));
        assertEquals(-1, (int)asc.read(receivedData).get(1000, TimeUnit.MILLISECONDS));

        // But we can still write!
        assertEquals(32, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));
        byte[] readArray = new byte[32];
        assertEquals(32, sss.getInputStream().read(readArray));
        assertTrue(Arrays.equals(sendData.array(), readArray));

        // Shutdown output, expect ClosedChannelException from write
        asc.shutdownOutput();
        try {
            assertEquals(-1, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));
            fail();
        } catch(ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }
        try {
            assertEquals(-1, (int)asc.write(sendData).get(1000, TimeUnit.MILLISECONDS));
            fail();
        } catch(ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }

        // shutdownInput() & shudownOutput() != closed, shocking!
        assertNotNull(asc.getRemoteAddress());
        assertTrue(asc.isOpen());

        asc.close();
        sss.close();
        ss.close();
    }

    public void test_options() throws Exception {
        try (AsynchronousSocketChannel asc = AsynchronousSocketChannel.open()) {

            asc.setOption(StandardSocketOptions.SO_SNDBUF, 5000);
            assertEquals(5000, (long) asc.getOption(StandardSocketOptions.SO_SNDBUF));

            asc.setOption(StandardSocketOptions.SO_RCVBUF, 5000);
            assertEquals(5000, (long) asc.getOption(StandardSocketOptions.SO_RCVBUF));

            asc.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            assertTrue(asc.getOption(StandardSocketOptions.SO_KEEPALIVE));

            asc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            assertTrue(asc.getOption(StandardSocketOptions.SO_REUSEADDR));

            asc.setOption(StandardSocketOptions.TCP_NODELAY, true);
            assertTrue(asc.getOption(StandardSocketOptions.TCP_NODELAY));
        }
    }

    public void test_options_iae() throws Exception {
        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open();

        try {
            asc.setOption(StandardSocketOptions.IP_TOS, 5);
            fail();
        } catch (UnsupportedOperationException expected) {}

        asc.close();
    }

    public void test_supportedOptions() throws Throwable {
        AsynchronousSocketChannel assc = AsynchronousSocketChannel.open();

        Set<SocketOption<?>> supportedOptions = assc.supportedOptions();
        assertEquals(5, supportedOptions.size());

        assertTrue(supportedOptions.contains(StandardSocketOptions.SO_REUSEADDR));
        assertTrue(supportedOptions.contains(StandardSocketOptions.SO_RCVBUF));
        assertTrue(supportedOptions.contains(StandardSocketOptions.SO_SNDBUF));
        assertTrue(supportedOptions.contains(StandardSocketOptions.SO_KEEPALIVE));
        assertTrue(supportedOptions.contains(StandardSocketOptions.TCP_NODELAY));

        // supportedOptions should work after close according to spec
        assc.close();
        supportedOptions = assc.supportedOptions();
        assertEquals(5, supportedOptions.size());
    }


    public void test_group() throws Exception {
        AsynchronousChannelProvider provider =
            AsynchronousChannelProvider.provider();
        AsynchronousChannelGroup group =
            provider.openAsynchronousChannelGroup(2, Executors.defaultThreadFactory());

        AsynchronousSocketChannel asc = AsynchronousSocketChannel.open(group);
        assertEquals(provider, asc.provider());
        asc.close();
    }

    private static ByteBuffer createTestByteBuffer(int size, boolean isDirect) {
        return createTestByteBuffer(size, isDirect, 0);
    }

    private static ByteBuffer createTestByteBuffer(int size, boolean isDirect, int contentOffset) {
        ByteBuffer bb = isDirect ? ByteBuffer.allocateDirect(size) : ByteBuffer.allocate(size);
        for (int i = 0; i < size; ++i) {
            bb.put(i, (byte)(i + contentOffset));
        }
        return bb;
    }

    /* J2ObjC removed: unsupported ResourceLeakageDetector
    public void test_closeGuardSupport() throws IOException {
        try (AsynchronousSocketChannel asc = AsynchronousSocketChannel.open()) {
            leakageDetectorRule.assertUnreleasedResourceCount(asc, 1);
        }
    }
     */

    /* J2ObjC removed: unsupported ResourceLeakageDetector
    public void test_closeGuardSupport_group() throws IOException {
        AsynchronousChannelProvider provider =
                AsynchronousChannelProvider.provider();
        AsynchronousChannelGroup group =
                provider.openAsynchronousChannelGroup(2, Executors.defaultThreadFactory());

        try (AsynchronousSocketChannel asc = AsynchronousSocketChannel.open(group)) {
            leakageDetectorRule.assertUnreleasedResourceCount(asc, 1);
        }
    }
     */
}
