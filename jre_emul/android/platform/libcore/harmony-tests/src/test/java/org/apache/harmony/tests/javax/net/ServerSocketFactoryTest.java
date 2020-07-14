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
import java.net.SocketException;
import javax.net.ServerSocketFactory;

import junit.framework.TestCase;

public class ServerSocketFactoryTest extends TestCase {

    public void test_Constructor() {
        ServerSocketFactory sf = new MyServerSocketFactory();
    }

    public final void test_createServerSocket() throws Exception {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        ServerSocket ss = sf.createServerSocket();
        assertNotNull(ss);
        ss.close();
    }

    public final void test_createServerSocket_I() throws Exception {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        ServerSocket ss = sf.createServerSocket(0);
        assertNotNull(ss);

        try {
            sf.createServerSocket(ss.getLocalPort());
            fail("IOException wasn't thrown");
        } catch (IOException expected) {
        }

        ss.close();

        try {
            sf.createServerSocket(-1);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public final void test_createServerSocket_II() throws Exception {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();
        ServerSocket ss = sf.createServerSocket(0, 0);
        assertNotNull(ss);

        try {
            sf.createServerSocket(ss.getLocalPort(), 0);
            fail("IOException wasn't thrown");
        } catch (IOException expected) {
        }

        ss.close();

        try {
            sf.createServerSocket(65536, 0);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException expected) {
        }
    }

    public final void test_createServerSocket_IIInetAddress() throws Exception {
        ServerSocketFactory sf = ServerSocketFactory.getDefault();

        ServerSocket ss = sf.createServerSocket(0, 0, InetAddress.getLocalHost());
        assertNotNull(ss);

        try {
            sf.createServerSocket(ss.getLocalPort(), 0, InetAddress.getLocalHost());
            fail("IOException wasn't thrown");
        } catch (IOException expected) {
        }

        ss.close();

        try {
            sf.createServerSocket(Integer.MAX_VALUE, 0, InetAddress.getLocalHost());
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException expected) {
        }
    }
}
class MyServerSocketFactory extends ServerSocketFactory {

    public MyServerSocketFactory() {
        super();
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return null;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog)
            throws IOException {
        return null;
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog,
            InetAddress address) throws IOException {
        return null;
    }
}
