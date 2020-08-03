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
import org.junit.Rule;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
/* J2ObjC removed: unsupported
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
import libcore.junit.util.ResourceLeakageDetector.LeakageDetectorRule;

public class AsynchronousServerSocketChannelTest extends TestCaseWithRules {
 */
public class AsynchronousServerSocketChannelTest extends TestCase {

    /* J2ObjC removed: unsupported ResourceLeakageDetector
    @Rule
    public LeakageDetectorRule leakageDetectorRule = ResourceLeakageDetector.getRule();
     */

    public void test_bind() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assertTrue(assc.isOpen());
        assertNull(assc.getLocalAddress());
        assc.bind(new InetSocketAddress(0));
        assertNotNull(assc.getLocalAddress());
        try {
            assc.bind(new InetSocketAddress(0));
            fail();
        } catch (AlreadyBoundException expected) {}

        assc.close();
        assertFalse(assc.isOpen());
    }

    public void test_bind_null() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assertTrue(assc.isOpen());
        assertNull(assc.getLocalAddress());
        assc.bind(null);
        assertNotNull(assc.getLocalAddress());
        try {
            assc.bind(null);
            fail();
        } catch (AlreadyBoundException expected) {}

        assc.close();
        assertFalse(assc.isOpen());
    }

    public void test_bind_unresolvedAddress() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        try {
            assc.bind(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (UnresolvedAddressException expected) {}

        assertNull(assc.getLocalAddress());
        assertTrue(assc.isOpen());
        assc.close();
    }

    /* j2objc: b/162760509
    public void test_bind_used() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        ServerSocket ss = new ServerSocket(0);
        try {
            assc.bind(ss.getLocalSocketAddress());
            fail();
        } catch (BindException expected) {}
        assertNull(assc.getLocalAddress());

        ss.close();
        assc.close();
    }
    */

    public void test_futureAccept() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        Future<AsynchronousSocketChannel> acceptFuture = assc.accept();

        Socket s = new Socket();
        s.connect(assc.getLocalAddress());

        AsynchronousSocketChannel asc = acceptFuture.get(1000, TimeUnit.MILLISECONDS);

        assertTrue(s.isConnected());
        assertNotNull(asc.getLocalAddress());
        assertEquals(asc.getLocalAddress(), s.getRemoteSocketAddress());
        assertNotNull(asc.getRemoteAddress());
        assertEquals(asc.getRemoteAddress(), s.getLocalSocketAddress());

        asc.close();
        s.close();
        assc.close();
    }

    public void test_completionHandlerAccept() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        FutureLikeCompletionHandler<AsynchronousSocketChannel> acceptCompletionHandler =
            new FutureLikeCompletionHandler<AsynchronousSocketChannel>();

        assc.accept(null /* attachment */, acceptCompletionHandler);

        Socket s = new Socket();
        s.connect(assc.getLocalAddress());
        AsynchronousSocketChannel asc = acceptCompletionHandler.get(1000);

        assertNotNull(asc);
        assertTrue(s.isConnected());
        assertNotNull(asc.getLocalAddress());
        assertEquals(asc.getLocalAddress(), s.getRemoteSocketAddress());
        assertNotNull(asc.getRemoteAddress());
        assertEquals(asc.getRemoteAddress(), s.getLocalSocketAddress());

        assertNull(acceptCompletionHandler.getAttachment());

        asc.close();
        s.close();
        assc.close();
    }

    public void test_completionHandlerAccept_attachment() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        FutureLikeCompletionHandler<AsynchronousSocketChannel> acceptCompletionHandler =
            new FutureLikeCompletionHandler<AsynchronousSocketChannel>();

        Integer attachment = new Integer(123);
        assc.accept(attachment, acceptCompletionHandler);

        Socket s = new Socket();
        s.connect(assc.getLocalAddress());
        AsynchronousSocketChannel asc = acceptCompletionHandler.get(1000);

        assertNotNull(asc);
        assertTrue(s.isConnected());

        assertEquals(attachment, acceptCompletionHandler.getAttachment());

        asc.close();
        s.close();
        assc.close();
    }

    public void test_completionHandlerAccept_nyb() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();

        FutureLikeCompletionHandler<AsynchronousSocketChannel> acceptCompletionHandler =
            new FutureLikeCompletionHandler<AsynchronousSocketChannel>();
        try {
            assc.accept(null /* attachment */, acceptCompletionHandler);
            fail();
        } catch(NotYetBoundException expected) {}

        assc.close();
    }

    public void test_completionHandlerAccept_npe() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        try {
            assc.accept(null /* attachment */, null /* completionHandler */);
            fail();
        } catch(NullPointerException expected) {}

        assc.close();
    }


    public void test_options() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();

        assc.setOption(StandardSocketOptions.SO_RCVBUF, 5000);
        assertEquals(5000, (long)assc.getOption(StandardSocketOptions.SO_RCVBUF));

        assc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        assertTrue(assc.getOption(StandardSocketOptions.SO_REUSEADDR));

        assc.close();
    }

    public void test_options_iae() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();

        try {
            assc.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
            fail();
        } catch (UnsupportedOperationException expected) {}

        assc.close();
    }

    public void test_group() throws Throwable {
        AsynchronousChannelProvider provider =
            AsynchronousChannelProvider.provider();
        AsynchronousChannelGroup group =
            provider.openAsynchronousChannelGroup(2, Executors.defaultThreadFactory());

        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(group);
        assertNull(assc.getLocalAddress());
        assc.bind(new InetSocketAddress(0));
        assertNotNull(assc.getLocalAddress());
        assertEquals(provider, assc.provider());
        assc.close();
    }

    public void test_close() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));
        assc.close();

        Future<AsynchronousSocketChannel> acceptFuture = assc.accept();
        try {
            acceptFuture.get(1000, TimeUnit.MILLISECONDS);
            fail();
        } catch(ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }

        FutureLikeCompletionHandler<AsynchronousSocketChannel> acceptCompletionHandler =
            new FutureLikeCompletionHandler<AsynchronousSocketChannel>();
        assc.accept(null /* attachment */, acceptCompletionHandler);
        try {
            acceptCompletionHandler.get(1000);
            fail();
        } catch(ClosedChannelException expected) {}

        try {
            assc.bind(new InetSocketAddress(0));
            fail();
        } catch(ClosedChannelException expected) {}

        try {
            assc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            fail();
        } catch(ClosedChannelException expected) {}

        // Try second close
        assc.close();
    }

    public void test_future_concurrent_close() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        final AtomicReference<Exception> killerThreadException =
            new AtomicReference<Exception>(null);
        final Thread killer = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    assc.close();
                } catch (Exception ex) {
                    killerThreadException.set(ex);
                }
            }
        });
        killer.start();
        Future<AsynchronousSocketChannel> acceptFuture = assc.accept();
        try {
            // This may timeout on slow devices, they may need more time for the killer thread to
            // do its thing.
            acceptFuture.get(10000, TimeUnit.MILLISECONDS);
            fail();
        } catch(ExecutionException expected) {
            assertTrue(expected.getCause() instanceof ClosedChannelException);
        }

        assertNull(killerThreadException.get());
    }

    public void test_completionHandler_concurrent_close() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();
        assc.bind(new InetSocketAddress(0));

        final AtomicReference<Exception> killerThreadException =
            new AtomicReference<Exception>(null);
        final Thread killer = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(2000);
                    assc.close();
                } catch (Exception ex) {
                    killerThreadException.set(ex);
                }
            }
        });
        killer.start();

        FutureLikeCompletionHandler<AsynchronousSocketChannel> acceptCompletionHandler =
            new FutureLikeCompletionHandler<AsynchronousSocketChannel>();

        assc.accept(null /* attachment */, acceptCompletionHandler);

        try {
            // This may timeout on slow devices, they may need more time for the killer thread to
            // do its thing.
            acceptCompletionHandler.get(10000);
            fail();
        } catch(AsynchronousCloseException expected) {}

        assertNull(killerThreadException.get());
    }

    public void test_supportedOptions() throws Throwable {
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open();

        Set<SocketOption<?>> supportedOptions = assc.supportedOptions();
        assertEquals(2, supportedOptions.size());

        assertTrue(supportedOptions.contains(StandardSocketOptions.SO_REUSEADDR));
        assertTrue(supportedOptions.contains(StandardSocketOptions.SO_RCVBUF));

        // supportedOptions should work after close according to spec
        assc.close();
        supportedOptions = assc.supportedOptions();
        assertEquals(2, supportedOptions.size());
    }

    /* J2ObjC removed: unsupported ResourceLeakageDetector
    public void test_closeGuardSupport() throws IOException {
        try (AsynchronousServerSocketChannel asc = AsynchronousServerSocketChannel.open()) {
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

        try (AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(group)) {
            leakageDetectorRule.assertUnreleasedResourceCount(assc, 1);
        }
    }
     */
}
