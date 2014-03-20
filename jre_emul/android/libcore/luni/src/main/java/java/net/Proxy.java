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
package java.net;

/**
 * This class represents proxy server settings. A created instance of {@code
 * Proxy} stores a type and an address and is immutable. There are three types
 * of proxies:
 * <ul>
 * <li>DIRECT</li>
 * <li>HTTP</li>
 * <li>SOCKS</li></ul
 */
public class Proxy {

    /**
     * Represents the proxy type setting {@code Proxy.Type.DIRECT}. It tells
     * protocol handlers that there is no proxy to be used. The address is set
     * to {@code null}.
     */
    public static final Proxy NO_PROXY = new Proxy();

    private Proxy.Type type;

    private SocketAddress address;

    /**
     * Creates a new {@code Proxy} instance. {@code SocketAddress} must NOT be
     * {@code null} when {@code type} is either {@code Proxy.Type.HTTP} or
     * {@code Proxy.Type.SOCKS}. To create a {@code Proxy} instance representing
     * the proxy type {@code Proxy.Type.DIRECT}, use {@code Proxy.NO_PROXY}
     * instead of this constructor.
     *
     * @param type
     *            the proxy type of this instance.
     * @param sa
     *            the proxy address of this instance.
     * @throws IllegalArgumentException
     *             if the parameter {@code type} is set to {@code
     *             Proxy.Type.DIRECT} or the value for {@code SocketAddress} is
     *             {@code null}.
     */
    public Proxy(Proxy.Type type, SocketAddress sa) {
        /*
         * Don't use DIRECT type to construct a proxy instance directly.
         * SocketAddress must NOT be null.
         */
        if (type == Type.DIRECT || sa == null) {
            throw new IllegalArgumentException("Illegal Proxy.Type or SocketAddress argument");
        }
        this.type = type;
        address = sa;
    }

    /*
     * Constructs a Proxy instance, which is Proxy.DIRECT type with null
     * SocketAddress. This constructor is used for NO_PROXY.
     */
    private Proxy() {
        type = Type.DIRECT;
        address = null;
    }

    /**
     * Gets the type of this {@code Proxy} instance.
     *
     * @return the stored proxy type.
     */
    public Proxy.Type type() {
        return type;
    }

    /**
     * Gets the address of this {@code Proxy} instance.
     *
     * @return the stored proxy address or {@code null} if the proxy type is
     *         {@code DIRECT}.
     */
    public SocketAddress address() {
        return address;
    }

    /**
     * Gets a textual representation of this {@code Proxy} instance. The string
     * includes the two parts {@code type.toString()} and {@code
     * address.toString()} if {@code address} is not {@code null}.
     *
     * @return the representing string of this proxy.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (type != null) {
            builder.append(type.toString());
        }
        builder.append("@");
        if (type != Proxy.Type.DIRECT && address != null) {
            builder.append(address.toString());
        }
        return builder.toString();
    }

    /**
     * Compares the specified {@code obj} to this {@code Proxy} instance and
     * returns whether they are equal or not. The given object must be an
     * instance of {@code Proxy} with the same address and the same type value
     * to be equal.
     *
     * @param obj
     *            the object to compare with this instance.
     * @return {@code true} if the given object represents the same {@code
     *         Proxy} as this instance, {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Proxy)) {
            return false;
        }
        Proxy another = (Proxy) obj;
        // address is null when and only when it's NO_PROXY.
        return (type == another.type) && address.equals(another.address);
    }

    /**
     * Gets the hashcode for this {@code Proxy} instance.
     *
     * @return the hashcode value for this Proxy instance.
     */
    @Override
    public final int hashCode() {
        int ret = 0;
        ret += type.hashCode();
        if (address != null) {
            ret += address.hashCode();
        }
        return ret;
    }

    /**
     * {@code Enum} class for the proxy type. Possible options are {@code
     * DIRECT}, {@code HTTP} and {@code SOCKS}.
     */
    public enum Type {
        /**
         * Direct connection. Connect without any proxy.
         */
        DIRECT,

        /**
         * HTTP type proxy. It's often used by protocol handlers such as HTTP,
         * HTTPS and FTP.
         */
        HTTP,

        /**
         * SOCKS type proxy.
         */
        SOCKS
    }
}
