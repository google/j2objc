/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.javax.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.net.ServerSocketFactory;
import junit.framework.TestCase;

public class ServerSocketFactoryTest extends TestCase {

    public void testCreateServerSocket() throws IOException {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket();
        serverSocket.bind(new InetSocketAddress(0));
        testSocket(serverSocket, 50);
    }

    public void testCreateServerSocketWithPort() throws IOException {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(0);
        testSocket(serverSocket, 50);
    }

    // This test may fail on kernel versions between 4.4 and 4.9, due to a kernel implementation
    // detail change. Backporting the following kernel change will fix the behavior.
    // http://b/31960002
    // https://git.kernel.org/cgit/linux/kernel/git/torvalds/linux.git/commit/?id=5ea8ea2cb7f1d0db15762c9b0bb9e7330425a071
    public void testCreateServerSocketWithPortNoBacklog() throws IOException {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(0, 1);
        testSocket(serverSocket, 1);
    }

    public void testCreateServerSocketWithPortZeroBacklog() throws IOException {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(0, 0);
        testSocket(serverSocket, 50);
    }

    public void testCreateServerSocketWithPortAndBacklog() throws IOException {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(0, 50);
        testSocket(serverSocket, 50);
    }

    private void testSocket(final ServerSocket serverSocket, int specifiedBacklog)
            throws IOException {
        final byte[] data = "abc".getBytes();

        new Thread(new Runnable() {
            public void run() {
                try {
                    Socket s = serverSocket.accept();
                    s.getOutputStream().write(data);
                    s.close();
                } catch (IOException e) {
                }
            }
        }).start();

        Socket socket = new Socket(InetAddress.getLocalHost(), serverSocket.getLocalPort());
        assertBacklog(specifiedBacklog, new InetSocketAddress(
                InetAddress.getLocalHost(), serverSocket.getLocalPort()));

        InputStream in = socket.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transfer(in, out);
        assertEquals(Arrays.toString(data), Arrays.toString(out.toByteArray()));
        socket.close();

        serverSocket.close();
    }

    /**
     * Validates that the backlog of the listening address is as specified.
     */
    private void assertBacklog(int specifiedBacklog, InetSocketAddress serverAddress)
            throws IOException {
        List<Socket> backlog = new ArrayList<Socket>();
        int peak = 0;
        try {
            int max = 100;
            for (int i = 0; i < max; i++) {
                Socket socket = new Socket();
                backlog.add(socket);
                socket.connect(serverAddress, 500);
                peak++;
            }
            fail("Failed to exhaust backlog after " + max + " connections!");
        } catch (IOException expected) {
        } finally {
            for (Socket socket : backlog) {
                socket.close();
            }
        }

        System.out.println("backlog peaked at " + peak);

        /*
         * In 4.5 of UNIX Network Programming, Stevens says:
         *     "Berkeley-derived implementations add a fudge factor to the
         *      backlog: it is multiplied by 1.5."
         *
         * We've observed that Linux always adds 3 to the user-specified
         * backlog.
         */
        assertTrue(peak >= specifiedBacklog && peak <= (specifiedBacklog + 3) * 1.5);
    }

    private void transfer(InputStream in, ByteArrayOutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int count;
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
    }
}
