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

/**
* @author Boris V. Kuznetsov
* @version $Revision$
*/

package org.apache.harmony.tests.javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import junit.framework.TestCase;

public class SocketFactoryTest extends TestCase {

    public void test_Constructor() throws Exception {
        new MySocketFactory();
    }

    public final void test_createSocket() throws Exception {
        SocketFactory sf = SocketFactory.getDefault();

        Socket s = sf.createSocket();
        assertNotNull(s);
        assertEquals(-1, s.getLocalPort());
        assertEquals(0, s.getPort());

        MySocketFactory msf = new MySocketFactory();
        try {
            msf.createSocket();
            fail("No expected SocketException");
        } catch (SocketException expected) {
        }
    }

    public final void test_createSocket_StringI() throws Exception {
        SocketFactory sf = SocketFactory.getDefault();
        int sport = new ServerSocket(0).getLocalPort();
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};

        Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport);
        assertNotNull(s);
        assertTrue("Failed to create socket", s.getPort() == sport);

        try {
            sf.createSocket("1.2.3.4hello", sport);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException expected) {
        }

        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                sf.createSocket(InetAddress.getLocalHost().getHostName(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException expected) {
            }
        }

        try {
            sf.createSocket(InetAddress.getLocalHost().getHostName(), s.getLocalPort());
            fail("IOException wasn't thrown");
        } catch (IOException expected) {
        }

        SocketFactory f = SocketFactory.getDefault();
        try {
            f.createSocket(InetAddress.getLocalHost().getHostName(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException expected) {
        }
    }

    public final void test_createSocket_InetAddressI() throws Exception {
        SocketFactory sf = SocketFactory.getDefault();
        int sport = new ServerSocket(0).getLocalPort();
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};

        Socket s = sf.createSocket(InetAddress.getLocalHost(), sport);
        assertNotNull(s);
        assertTrue("Failed to create socket", s.getPort() == sport);

        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                sf.createSocket(InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException expected) {
            }
        }

        try {
            sf.createSocket(InetAddress.getLocalHost(), s.getLocalPort());
            fail("IOException wasn't thrown");
        } catch (IOException expected) {
        }

        SocketFactory f = SocketFactory.getDefault();
        try {
            f.createSocket(InetAddress.getLocalHost(), 8081);
            fail("IOException wasn't thrown ...");
        } catch (IOException expected) {
        }
    }

    public final void test_createSocket_InetAddressIInetAddressI() throws Exception {
        SocketFactory sf = SocketFactory.getDefault();
        int sport = new ServerSocket(0).getLocalPort();

        Socket s = sf.createSocket(InetAddress.getLocalHost(), sport,
                InetAddress.getLocalHost(), 0);
        assertNotNull(s);
        assertTrue("1: Failed to create socket", s.getPort() == sport);
        int portNumber = s.getLocalPort();
        try {
            sf.createSocket(InetAddress.getLocalHost(), sport,
                            InetAddress.getLocalHost(), portNumber);
            fail("IOException wasn't thrown");
        } catch (IOException expected) {
        }

        SocketFactory f = SocketFactory.getDefault();
        try {
            f.createSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException expected) {
        }
    }

    // Checks the behavior of createSocket(InetAddress, int, InetAddress, int) when the
    // ports are invalid.
    public void test_createSocket_InetAddressIInetAddressI_IllegalArgumentException()
            throws Exception {
        SocketFactory sf = SocketFactory.getDefault();
        int validPort = new ServerSocket(0).getLocalPort();
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};

        for (int i = 0; i < invalidPorts.length; i++) {
            // Check invalid server port.
            try (Socket s = sf.createSocket(InetAddress.getLocalHost() /* ServerAddress */,
                    invalidPorts[i] /* ServerPort */,
                    InetAddress.getLocalHost() /* ClientAddress */,
                    validPort /* ClientPort */)) {
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException expected) {
            }

            // Check invalid client port.
            try (Socket s = sf.createSocket(InetAddress.getLocalHost() /* ServerAddress */,
                    validPort /* ServerPort */,
                    InetAddress.getLocalHost() /* ClientAddress */,
                    invalidPorts[i]) /* ClientPort */){
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    // b/31019685
    // Checks the ordering of port number validation (IllegalArgumentException) and binding error.
    public void test_createSocket_InetAddressIInetAddressI_ExceptionOrder() throws IOException {
        int invalidPort = Integer.MAX_VALUE;
        SocketFactory sf = SocketFactory.getDefault();
        int validServerPortNumber = new ServerSocket(0).getLocalPort();

        // Create a socket with localhost as the client address so that another attempt to bind
        // would fail.
        Socket s = sf.createSocket(InetAddress.getLocalHost() /* ServerAddress */,
                validServerPortNumber /* ServerPortNumber */,
                InetAddress.getLocalHost() /* ClientAddress */,
                0 /* ClientPortNumber */);

        int assignedLocalPortNumber = s.getLocalPort();

        // Create a socket with an invalid port and localhost as the client address. Both
        // BindException and IllegalArgumentException are expected in this case as the address is
        // already bound to the socket above and port is invalid, however, to preserve the
        // precedence order, IllegalArgumentException should be thrown.
        try (Socket s1 = sf.createSocket(InetAddress.getLocalHost() /* ServerAddress */,
                invalidPort /* ServerPortNumber */,
                InetAddress.getLocalHost() /* ClientAddress */,
                assignedLocalPortNumber /* ClientPortNumber */)) {
            fail("IllegalArgumentException wasn't thrown for " + invalidPort);
        } catch (IllegalArgumentException expected) {
        }
    }

    /**
     * javax.net.SocketFactory#createSocket(String host, int port,
     *                                             InetAddress localHost, int localPort)
     */
    /* J2ObjC removed: https://github.com/google/j2objc/issues/1292
    public final void test_createSocket_05() throws Exception {
        SocketFactory sf = SocketFactory.getDefault();
        int sport = new ServerSocket(0).getLocalPort();
        int[] invalidPorts = {Integer.MIN_VALUE, -1, 65536, Integer.MAX_VALUE};

        Socket s = sf.createSocket(InetAddress.getLocalHost().getHostName(), sport,
                                   InetAddress.getLocalHost(), 0);
        assertNotNull(s);
        assertTrue("1: Failed to create socket", s.getPort() == sport);

        try {
            sf.createSocket("1.2.3.4hello", sport, InetAddress.getLocalHost(), 0);
            fail("UnknownHostException wasn't thrown");
        } catch (UnknownHostException expected) {
        }

        for (int i = 0; i < invalidPorts.length; i++) {
            try {
                sf.createSocket(InetAddress.getLocalHost().getHostName(), invalidPorts[i],
                                InetAddress.getLocalHost(), 0);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException expected) {
            }
            try {
                sf.createSocket(InetAddress.getLocalHost().getHostName(), sport,
                                InetAddress.getLocalHost(), invalidPorts[i]);
                fail("IllegalArgumentException wasn't thrown for " + invalidPorts[i]);
            } catch (IllegalArgumentException expected) {
            }
        }

        try {
            sf.createSocket(InetAddress.getLocalHost().getHostName(), 8081, InetAddress.getLocalHost(), 8082);
            fail("IOException wasn't thrown ...");
        } catch (IOException expected) {
        }
    } */

    /**
     * javax.net.SocketFactory#getDefault()
     */
    public final void test_getDefault() {
        SocketFactory sf = SocketFactory.getDefault();
        Socket s;
        try {
            s = sf.createSocket(InetAddress.getLocalHost().getHostName(), 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket(InetAddress.getLocalHost().getHostName(), 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081);
            s.close();
        } catch (IOException e) {
        }
        try {
            s = sf.createSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            s.close();
        } catch (IOException e) {
        }
    }
}

class MySocketFactory extends SocketFactory {

    public MySocketFactory() {
        super();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return null;
     }

    @Override
    public Socket createSocket(InetAddress address, int port,
                               InetAddress localAddress, int localPort) throws IOException {
        return null;
     }

}
