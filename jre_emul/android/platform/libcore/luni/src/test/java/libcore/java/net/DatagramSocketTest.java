/*
 * Copyright (C) 2014 The Android Open Source Project
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

package libcore.java.net;

import java.lang.reflect.Field;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.DatagramSocketImpl;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
 */
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.rules.TestRule;

public class DatagramSocketTest extends TestCase /* J2ObjC removed: TestCaseWithRules */ {
  /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
  @Rule
  public TestRule resourceLeakageDetectorRule = ResourceLeakageDetector.getRule();
   */

  public void testInitialState() throws Exception {
    DatagramSocket ds = new DatagramSocket();
    try {
      assertTrue(ds.isBound());
      assertTrue(ds.getBroadcast()); // The RI starts DatagramSocket in broadcast mode.
      assertFalse(ds.isClosed());
      assertFalse(ds.isConnected());
      assertTrue(ds.getLocalPort() > 0);
      assertTrue(ds.getLocalAddress().isAnyLocalAddress());
      InetSocketAddress socketAddress = (InetSocketAddress) ds.getLocalSocketAddress();
      assertEquals(ds.getLocalPort(), socketAddress.getPort());
      assertEquals(ds.getLocalAddress(), socketAddress.getAddress());
      assertNull(ds.getInetAddress());
      assertEquals(-1, ds.getPort());
      assertNull(ds.getRemoteSocketAddress());
      assertFalse(ds.getReuseAddress());
      assertNull(ds.getChannel());
    } finally {
      ds.close();
    }
  }

  public void testStateAfterClose() throws Exception {
    DatagramSocket ds = new DatagramSocket();
    ds.close();
    assertTrue(ds.isBound());
    assertTrue(ds.isClosed());
    assertFalse(ds.isConnected());
    assertNull(ds.getLocalAddress());
    assertEquals(-1, ds.getLocalPort());
    assertNull(ds.getLocalSocketAddress());
  }

  /* J2ObjC removed.
  public void testPendingException() throws Exception {
    final int port = 9999;

    try (DatagramSocket s = new DatagramSocket()) {
      forceConnectToThrowSocketException(s);

      s.connect(InetAddress.getLocalHost(), port);

      byte[] data = new byte[100];
      DatagramPacket p = new DatagramPacket(data, data.length);

      // Confirm send() throws the pendingConnectException.
      try {
        s.send(p);
        fail();
      } catch (SocketException expected) {
        assertTrue(expected.getMessage().contains("Pending connect failure"));
      }

      // Confirm receive() throws the pendingConnectException.
      try {
        s.receive(p);
        fail();
      } catch (SocketException expected) {
        assertTrue(expected.getMessage().contains("Pending connect failure"));
      }

      // Confirm that disconnect() doesn't throw a runtime exception.
      s.disconnect();
    }
  }
   */

  public void test_setTrafficClass() throws Exception {
    try (DatagramSocket s = new DatagramSocket()) {
      for (int i = 0; i <= 255; ++i) {
        s.setTrafficClass(i);
        assertEquals(i, s.getTrafficClass());
      }
    }
  }

  // DatagramSocket should "become connected" even when impl.connect() fails and throws an
  // exception.
  public void test_b31218085() throws Exception {
    final int port = 9999;

    try (DatagramSocket s = new DatagramSocket()) {
      forceConnectToThrowSocketException(s);

      s.connect(InetAddress.getLocalHost(), port);
      assertTrue(s.isConnected());

      // Confirm that disconnect() doesn't throw a runtime exception.
      s.disconnect();
    }
  }

  public void testForceConnectToThrowSocketException() throws Exception {
    // Unlike connect(InetAddress, int), connect(SocketAddress) can (and should) throw an
    // exception after a call to forceConnectToThrowSocketException(). The
    // forceConnectToThrowSocketException() method is used in various tests for
    // connect(InetAddress, int) and this test exists to confirm it stays working.

    SocketAddress validAddress = new InetSocketAddress(InetAddress.getLocalHost(), 9999);

    try (DatagramSocket s1 = new DatagramSocket()) {
      s1.connect(validAddress);
      s1.disconnect();
    }

    try (DatagramSocket s2 = new DatagramSocket()) {
      forceConnectToThrowSocketException(s2);
      try {
        s2.connect(validAddress);
      } catch (SocketException expected) {
      }
      s2.disconnect();
    }
  }

  // DatagramSocket should ignore packets received from other sources prior to connect().
  // CVE-2014-6512
  // b/31586706
  /* J2ObjC removed.
  public void testExplicitFilter() throws Exception {
    final byte[] data = new byte[]{1, 2, 3, 4};

    try (DatagramSocket dgramSocket = new DatagramSocket();
         DatagramSocket otherSocket = new DatagramSocket()) {
      otherSocket.connect(dgramSocket.getLocalSocketAddress());
      otherSocket.send(new DatagramPacket(data, data.length));

      dgramSocket.setSoTimeout(100);
      dgramSocket.connect(new InetSocketAddress(0));

      // Packet from otherSocket was sent to ds before ds is connected, and it was stored in
      // dgramSocket's local buffer. After connect(), dgramSocket should discard this packet
      // from the buffer since it is not sent from the connected socket address.
      try {
        DatagramPacket recv = new DatagramPacket(new byte[data.length], data.length);
        dgramSocket.receive(recv);
        fail();
      } catch (SocketTimeoutException expected) { }
    }

    try (DatagramSocket dgramSocket = new DatagramSocket();
         DatagramSocket srcSocket = new DatagramSocket()) {
      srcSocket.connect(dgramSocket.getLocalSocketAddress());
      srcSocket.send(new DatagramPacket(data, data.length));

      dgramSocket.setSoTimeout(100);
      dgramSocket.connect(srcSocket.getLocalSocketAddress());

      // If the packet is sent from the connected address, even though that is before connect(), it
      // should not be dropped and receive() should succeed.
      DatagramPacket recv = new DatagramPacket(new byte[data.length], data.length);
      dgramSocket.receive(recv);
      assertTrue(Arrays.equals(recv.getData(), data));
    }
  }
   */

  private static void forceConnectToThrowSocketException(DatagramSocket s) throws Exception {
    // Set fd of DatagramSocketImpl to null, forcing impl.connect() to throw a SocketException
    // (Socket closed).
    Field f = DatagramSocket.class.getDeclaredField("impl");
    f.setAccessible(true);
    DatagramSocketImpl impl = (DatagramSocketImpl) f.get(s);
    f = DatagramSocketImpl.class.getDeclaredField("fd");
    f.setAccessible(true);
    f.set(impl, null);
  }

  /* J2ObjC removed.
  public void testAddressSameIfUnchanged() throws Exception {
    try (DatagramSocket ds = new DatagramSocket();
         DatagramSocket srcDs = new DatagramSocket()) {
      ds.setSoTimeout(1000);
      srcDs.connect(ds.getLocalSocketAddress());
      srcDs.send(new DatagramPacket(new byte[16], 16));
      srcDs.send(new DatagramPacket(new byte[16], 16));

      DatagramPacket p = new DatagramPacket(new byte[16], 16);
      ds.receive(p);
      InetAddress packetAddr = p.getAddress();

      // This time the packet should have the same address as source address, and it's address
      // should remain the same object.
      ds.receive(p);
      InetAddress newPacketAddr = p.getAddress();
      assertTrue(packetAddr.isLoopbackAddress());
      assertSame(packetAddr, newPacketAddr);
    }
  }
   */
}
