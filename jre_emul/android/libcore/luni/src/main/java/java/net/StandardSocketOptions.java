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

package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import libcore.io.IoBridge;

/**
 * Defines the set standard of socket options that can be supported by network channels.
 *
 * <p>See {@link java.nio.channels.NetworkChannel} for more information, particularly
 * {@link java.nio.channels.NetworkChannel#supportedOptions()} for the options that are supported
 * for each type of socket.
 *
 * @since 1.7
 * @hide Until ready for a public API change
 */
public final class StandardSocketOptions {

  /**
   * The outgoing interface for multicast packets.
   *
   * <p>See {@link SocketOptions#IP_MULTICAST_IF2} for further documentation.
   */
  public static final SocketOption<NetworkInterface> IP_MULTICAST_IF =
      new NetworkInterfaceSocketOption("IP_MULTICAST_IF", SocketOptions.IP_MULTICAST_IF2);

  /**
   * Whether the local loopback of multicast packets is enabled (true) or disabled (false). This
   * option is enabled by default.
   *
   * <p>See {@link SocketOptions#IP_MULTICAST_LOOP} for further documentation.
   */
  public static final SocketOption<Boolean> IP_MULTICAST_LOOP =
      new BooleanSocketOption("IP_MULTICAST_LOOP", SocketOptions.IP_MULTICAST_LOOP);

  /**
   * The time-to-live (TTL) for multicast packets. The value must be between 0 and 255 inclusive.
   * A 0 value restricts the packet to the originating host. See also {@link #IP_MULTICAST_LOOP}.
   * The default value is 1.
   *
   * <p>See <a href="http://tools.ietf.org/rfc/rfc1112.txt">RFC 1112: Host Extensions for IP
   * Multicasting</a> for more information about IP multicast.
   */
  public static final SocketOption<Integer> IP_MULTICAST_TTL =
      new ByteRangeSocketOption("IP_MULTICAST_TTL", IoBridge.JAVA_IP_MULTICAST_TTL);

  /**
   * The value for the type-of-service field of the IPv4 header, or the traffic class field of the
   * IPv6 header. These correspond to the IP_TOS and IPV6_TCLASS socket options. These may be
   * ignored by the underlying OS. Values must be between 0 and 255 inclusive.
   *
   * <p>See {@link SocketOptions#IP_TOS} for further documentation.
   */
  public static final SocketOption<Integer> IP_TOS =
      new ByteRangeSocketOption("IP_TOS", SocketOptions.IP_TOS);

  /**
   * Whether broadcasting on datagram sockets is enabled or disabled. This option must be enabled to
   * send broadcast messages. The default value is false.
   *
   * <p>See {@link SocketOptions#SO_BROADCAST} for further documentation.
   */
  public static final SocketOption<Boolean> SO_BROADCAST =
      new BooleanSocketOption("SO_BROADCAST", SocketOptions.SO_BROADCAST);

  /**
   * Whether the kernel sends keepalive messages on connection-oriented sockets.
   *
   * <p>See {@link SocketOptions#SO_KEEPALIVE} for further documentation.
   */
  public static final SocketOption<Boolean> SO_KEEPALIVE =
      new BooleanSocketOption("SO_KEEPALIVE", SocketOptions.SO_KEEPALIVE);

  /**
   * Number of seconds to wait when closing a socket if there is still some buffered data to be
   * sent.
   *
   * <p>If this option is negative this option is disabled. This is the default value. If the value
   * is 0 or positive it is enabled.
   *
   * <p>See {@link SocketOptions#SO_LINGER} for further documentation.
   *
   */
  public static final SocketOption<Integer> SO_LINGER =
      new SocketOptionImpl<Integer>("SO_LINGER", Integer.class, SocketOptions.SO_LINGER) {
        @Override
        protected Object validateAndConvertValueBeforeSet(
            FileDescriptor fd, Integer value) {
          Object objectValue = super.validateAndConvertValueBeforeSet(fd, value);
          if (value != null && value < 0) {
            // IoBridge requires a "false" object to disable linger.
            objectValue = Boolean.FALSE;
          }
          return objectValue;
        }

        @Override
        protected Integer validateAndConvertValueAfterGet(FileDescriptor fd, Object value) {
          // IoBridge returns a "false" object to indicate that linger is disabled.
          if (value != null && value instanceof Boolean) {
            value = -1;
          }
          return super.validateAndConvertValueAfterGet(fd, value);
        }
      };

  /**
   * The size in bytes of a socket's receive buffer. This must be an integer greater than 0.
   * This is a hint to the kernel; the kernel may use a larger buffer.
   *
   * <p>See {@link SocketOptions#SO_RCVBUF} for further documentation.
   */
  public static final SocketOption<Integer> SO_RCVBUF =
      new PositiveIntegerSocketOption("SO_RCVBUF", SocketOptions.SO_RCVBUF);

  /**
   * Whether a reuse of a local address is allowed when another socket has not yet been removed by
   * the operating system.
   *
   * <p>See {@link SocketOptions#SO_REUSEADDR} for further documentation.
   */
  public static final SocketOption<Boolean> SO_REUSEADDR =
      new BooleanSocketOption("SO_REUSEADDR", SocketOptions.SO_REUSEADDR);

  /**
   * The size in bytes of a socket's send buffer. This must be an integer greater than 0.
   * This is a hint to the kernel; the kernel may use a larger buffer.
   *
   * <p>See {@link SocketOptions#SO_SNDBUF} for further documentation.
   */
  public static final SocketOption<Integer> SO_SNDBUF =
      new PositiveIntegerSocketOption("SO_SNDBUF", SocketOptions.SO_SNDBUF);

  /**
   * Specifies whether data is sent immediately on this socket or buffered.
   *
   * <p>See {@link SocketOptions#TCP_NODELAY} for further documentation.
   */
  public static final SocketOption<Boolean> TCP_NODELAY =
      new BooleanSocketOption("TCP_NODELAY", SocketOptions.TCP_NODELAY);

  /**
   * The set of supported options for UDP sockets.
   *
   * @hide internal use only
   */
  public static final Set<SocketOption<?>> DATAGRAM_SOCKET_OPTIONS;

  static {
    HashSet<SocketOption<?>> mutableSet = new HashSet<SocketOption<?>>(8);
    mutableSet.add(IP_MULTICAST_IF);
    mutableSet.add(IP_MULTICAST_LOOP);
    mutableSet.add(IP_MULTICAST_TTL);
    mutableSet.add(IP_TOS);
    mutableSet.add(SO_BROADCAST);
    mutableSet.add(SO_REUSEADDR);
    mutableSet.add(SO_RCVBUF);
    mutableSet.add(SO_SNDBUF);
    DATAGRAM_SOCKET_OPTIONS = Collections.unmodifiableSet(mutableSet);
  }

  /**
   * The set of supported options for TCP sockets.
   *
   * @hide internal use only
   */
  public static final Set<SocketOption<?>> SOCKET_OPTIONS;

  static {
    HashSet<SocketOption<?>> mutableSet = new HashSet<SocketOption<?>>(7);
    mutableSet.add(IP_TOS);
    mutableSet.add(SO_KEEPALIVE);
    mutableSet.add(SO_LINGER);
    mutableSet.add(TCP_NODELAY);
    mutableSet.add(SO_RCVBUF);
    mutableSet.add(SO_REUSEADDR);
    mutableSet.add(SO_SNDBUF);
    SOCKET_OPTIONS = Collections.unmodifiableSet(mutableSet);
  }

  /**
   * The set of supported options for TCP server sockets.
   *
   * @hide internal use only
   */
  public static final Set<SocketOption<?>> SERVER_SOCKET_OPTIONS;

  static {
    HashSet<SocketOption<?>> mutableSet = new HashSet<SocketOption<?>>(2);
    mutableSet.add(SO_RCVBUF);
    mutableSet.add(SO_REUSEADDR);
    SERVER_SOCKET_OPTIONS = Collections.unmodifiableSet(mutableSet);
  }

  /**
   * A base class for SocketOption objects that passes the values to/from {@link IoBridge} as they
   * are. For use with simple types like Integer and Boolean, and can be extended for more
   * validation / type conversion.
   *
   * @hide internal use only
   */
  public static class SocketOptionImpl<T> implements SocketOption<T> {

    protected final String name;

    private final Class<T> type;

    protected final int socketOption;

    public SocketOptionImpl(String name, Class<T> type, int socketOption) {
      this.name = name;
      this.type = type;
      this.socketOption = socketOption;
    }

    @Override
    public String name() {
      return name;
    }

    @Override
    public Class<T> type() {
      return type;
    }

    /**
     * Sets the socket option of the file descriptor to value using IoBridge.
     *
     * @hide internal method
     */
    public final void setValue(FileDescriptor fd, T value) throws IOException {
      // Sanity check required because of type erasure.
      if (value != null && !type.isAssignableFrom(value.getClass())) {
        throw new AssertionError("Invalid type " + value + " of value for " + name);
      }
      Object objectValue = validateAndConvertValueBeforeSet(fd, value);
      IoBridge.setSocketOption(fd, socketOption, objectValue);
    }

    /**
     * Throws IllegalArgumentException if the value is outside of the acceptable range.
     * Subclasses can override to apply option-specific validate, and may also convert the value
     * to a different type or value. The default implementation prevents null values and returns
     * the value unchanged.
     */
    protected Object validateAndConvertValueBeforeSet(FileDescriptor fd, T value) {
      if (value == null) {
        throw new IllegalArgumentException("value for " + name + " must not be null");
      }
      return value;
    }

    /**
     * Gets the value of the socket option.
     *
     * @hide internal method
     */
    public final T getValue(FileDescriptor fd) throws IOException {
      Object value = IoBridge.getSocketOption(fd, socketOption);
      T typedValue = validateAndConvertValueAfterGet(fd, value);
      if (typedValue != null && !type.isAssignableFrom(typedValue.getClass())) {
        // Sanity check required because of type erasure.
        throw new AssertionError("Unexpected type of value returned for " + name);
      }
      return typedValue;
    }

    /**
     * Throws AssertionError if the value is outside of the acceptable range.
     * Implementations may also convert the value to a different type or
     * value. The default implementation does nothing.
     */
    @SuppressWarnings("unchecked")
    protected T validateAndConvertValueAfterGet(FileDescriptor fd, Object value) {
      return (T) value;
    }
  }

  /**
   * A SocketOption capable of setting / getting an boolean value.
   */
  private static class BooleanSocketOption extends SocketOptionImpl<Boolean> {

    public BooleanSocketOption(String name, int socketOption) {
      super(name, Boolean.class, socketOption);
    }
  }

  /**
   * A SocketOption capable of setting / getting an network interface value.
   */
  private static class NetworkInterfaceSocketOption extends SocketOptionImpl<NetworkInterface> {

    public NetworkInterfaceSocketOption(String name, int socketOption) {
      super(name, NetworkInterface.class, socketOption);
    }

    @Override
    public Integer validateAndConvertValueBeforeSet(FileDescriptor fd, NetworkInterface value) {
      if (value == null) {
        throw new IllegalArgumentException("value for " + name + " must not be null");
      }
      int nicIndex = value.getIndex();
      if (nicIndex == -1) {
        throw new IllegalArgumentException("The NetworkInterface must have a valid index");
      }
      return nicIndex;
    }

    @Override
    public NetworkInterface validateAndConvertValueAfterGet(FileDescriptor fd, Object value) {
      if (value == null) {
        return null;
      } else if (!(value instanceof Integer)) {
        throw new AssertionError("Unexpected type of value returned for " + name);
      }

      int nicIndex = (Integer) value;
      try {
        return NetworkInterface.getByIndex(nicIndex);
      } catch (SocketException e) {
        throw new IllegalArgumentException(
            "Unable to resolve NetworkInterface index: " + nicIndex, e);
      }
    }
  }

  /**
   * A SocketOption capable of setting / getting an integer in the range 0-255.
   */
  private static class ByteRangeSocketOption extends SocketOptionImpl<Integer> {

    public ByteRangeSocketOption(String name, int socketOption) {
      super(name, Integer.class, socketOption);
    }

    @Override
    protected Object validateAndConvertValueBeforeSet(FileDescriptor fd, Integer value) {
      if (value == null || value < 0 || value > 255) {
        throw new IllegalArgumentException(name + " must be >= 0 and <= 255, is " + value);
      }
      return value;
    }

    @Override
    protected Integer validateAndConvertValueAfterGet(FileDescriptor fd, Object value) {
      if (!(value instanceof Integer)) {
        throw new AssertionError("Unexpected value for option " + name + ": " + value);
      }
      int intValue = (Integer) value;
      if (intValue < 0 || intValue > 255) {
        throw new AssertionError("Unexpected value for option " + name + ": " + value);
      }
      return intValue;
    }
  }

  /**
   * A SocketOption capable of setting / getting an integer in the range 1..
   */
  private static class PositiveIntegerSocketOption extends SocketOptionImpl<Integer> {

    public PositiveIntegerSocketOption(String name, int socketOption) {
      super(name, Integer.class, socketOption);
    }

    @Override
    protected Integer validateAndConvertValueBeforeSet(FileDescriptor fd, Integer value) {
      if (value < 1) {
        throw new IllegalArgumentException(name + " value must be > 0");
      }
      return value;
    }

    @Override
    protected Integer validateAndConvertValueAfterGet(FileDescriptor fd, Object value) {
      if (!(value instanceof Integer)) {
        throw new AssertionError("Unexpected value for option " + name + ": " + value);
      }
      int intValue = (Integer) value;
      if (intValue < 1) {
        throw new AssertionError("Unexpected value for option " + name + ": " + value);
      }
      return intValue;
    }
  }
}
