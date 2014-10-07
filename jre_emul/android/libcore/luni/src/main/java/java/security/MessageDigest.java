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

package java.security;

import java.nio.ByteBuffer;
import org.apache.harmony.security.fortress.Engine;

/**
 * Uses a one-way hash function to turn an arbitrary number of bytes into a
 * fixed-length byte sequence. The original arbitrary-length sequence is the
 * <i>message</i>, and the fixed-length byte sequence is the <i>digest</i> or
 * <i>message digest</i>.
 *
 * <h4>Sample Code</h4>
 * <p>The basic pattern to digest an {@link java.io.InputStream} looks like this:
 * <pre>
 *  MessageDigest digester = MessageDigest.getInstance("MD5");
 *  byte[] bytes = new byte[8192];
 *  int byteCount;
 *  while ((byteCount = in.read(bytes)) > 0) {
 *    digester.update(bytes, 0, byteCount);
 *  }
 *  byte[] digest = digester.digest();
 * </pre>
 *
 * <p>That is, after creating or resetting a {@code MessageDigest} you should
 * call {@link #update(byte[],int,int)} for each block of input data, and then call {@link #digest}
 * to get the final digest. Note that calling {@code digest} resets the {@code MessageDigest}.
 * Advanced users who want partial digests should clone their {@code MessageDigest} before
 * calling {@code digest}.
 *
 * <p>This class is not thread-safe.
 *
 * @see MessageDigestSpi
 */
public abstract class MessageDigest extends MessageDigestSpi {

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine("MessageDigest");

    // The provider
    private Provider provider;

    // The algorithm.
    private String algorithm;

    /**
     * Constructs a new instance of {@code MessageDigest} with the name of
     * the algorithm to use.
     *
     * @param algorithm
     *            the name of algorithm to use
     */
    protected MessageDigest(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Returns a new instance of {@code MessageDigest} that utilizes the
     * specified algorithm.
     *
     * @param algorithm
     *            the name of the algorithm to use
     * @return a new instance of {@code MessageDigest} that utilizes the
     *         specified algorithm
     * @throws NoSuchAlgorithmException
     *             if the specified algorithm is not available
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}
     */
    public static MessageDigest getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Engine.SpiAndProvider sap = ENGINE.getInstance(algorithm, null);
        Object spi = sap.spi;
        Provider provider = sap.provider;
        if (spi instanceof MessageDigest) {
            MessageDigest result = (MessageDigest) spi;
            result.algorithm = algorithm;
            result.provider = provider;
            return result;
        }
        return new MessageDigestImpl((MessageDigestSpi) sap.spi, sap.provider, algorithm);
    }

    /**
     * Returns a new instance of {@code MessageDigest} that utilizes the
     * specified algorithm from the specified provider.
     *
     * @param algorithm
     *            the name of the algorithm to use
     * @param provider
     *            the name of the provider
     * @return a new instance of {@code MessageDigest} that utilizes the
     *         specified algorithm from the specified provider
     * @throws NoSuchAlgorithmException
     *             if the specified algorithm is not available
     * @throws NoSuchProviderException
     *             if the specified provider is not available
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}
     * @throws IllegalArgumentException if {@code provider == null || provider.isEmpty()}
     */
    public static MessageDigest getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(algorithm, p);
    }

    /**
     * Returns a new instance of {@code MessageDigest} that utilizes the
     * specified algorithm from the specified provider.
     *
     * @param algorithm
     *            the name of the algorithm to use
     * @param provider
     *            the provider
     * @return a new instance of {@code MessageDigest} that utilizes the
     *         specified algorithm from the specified provider
     * @throws NoSuchAlgorithmException
     *             if the specified algorithm is not available
     * @throws NullPointerException
     *             if {@code algorithm} is {@code null}
     * @throws IllegalArgumentException if {@code provider == null}
     */
    public static MessageDigest getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("provider == null");
        }
        if (algorithm == null) {
            throw new NullPointerException("algorithm == null");
        }
        Object spi = ENGINE.getInstance(algorithm, provider, null);
        if (spi instanceof MessageDigest) {
            MessageDigest result = (MessageDigest) spi;
            result.algorithm = algorithm;
            result.provider = provider;
            return result;
        }
        return new MessageDigestImpl((MessageDigestSpi) spi, provider, algorithm);
    }

    /**
     * Puts this {@code MessageDigest} back in an initial state, such that it is
     * ready to compute a one way hash value.
     */
    public void reset() {
        engineReset();
    }

    /**
     * Updates this {@code MessageDigest} using the given {@code byte}.
     *
     * @param arg0
     *            the {@code byte} to update this {@code MessageDigest} with
     * @see #reset()
     */
    public void update(byte arg0) {
        engineUpdate(arg0);
    }

    /**
     * Updates this {@code MessageDigest} using the given {@code byte[]}.
     *
     * @param input
     *            the {@code byte} array
     * @param offset
     *            the index of the first byte in {@code input} to update from
     * @param len
     *            the number of bytes in {@code input} to update from
     * @throws IllegalArgumentException
     *             if {@code offset} or {@code len} are not valid in respect to
     *             {@code input}
     */
    public void update(byte[] input, int offset, int len) {
        if (input == null ||
        // offset < 0 || len < 0 ||
                // checks for negative values are commented out intentionally
                // see HARMONY-1120 for details
                (long) offset + (long) len > input.length) {
            throw new IllegalArgumentException();
        }
        engineUpdate(input, offset, len);
    }

    /**
     * Updates this {@code MessageDigest} using the given {@code byte[]}.
     *
     * @param input
     *            the {@code byte} array
     * @throws NullPointerException
     *             if {@code input} is {@code null}
     */
    public void update(byte[] input) {
        if (input == null) {
            throw new NullPointerException("input == null");
        }
        engineUpdate(input, 0, input.length);
    }

    /**
     * Computes and returns the final hash value for this {@link MessageDigest}.
     * After the digest is computed the receiver is reset.
     *
     * @return the computed one way hash value
     * @see #reset
     */
    public byte[] digest() {
        return engineDigest();
    }

    /**
     * Computes and stores the final hash value for this {@link MessageDigest}.
     * After the digest is computed the receiver is reset.
     *
     * @param buf
     *            the buffer to store the result
     * @param offset
     *            the index of the first byte in {@code buf} to store
     * @param len
     *            the number of bytes allocated for the digest
     * @return the number of bytes written to {@code buf}
     * @throws DigestException
     *             if an error occurs
     * @throws IllegalArgumentException
     *             if {@code offset} or {@code len} are not valid in respect to
     *             {@code buf}
     * @see #reset()
     */
    public int digest(byte[] buf, int offset, int len) throws DigestException {
        if (buf == null ||
        // offset < 0 || len < 0 ||
                // checks for negative values are commented out intentionally
                // see HARMONY-1148 for details
                (long) offset + (long) len > buf.length) {
            throw new IllegalArgumentException();
        }
        return engineDigest(buf, offset, len);
    }

    /**
     * Performs the final update and then computes and returns the final hash
     * value for this {@link MessageDigest}. After the digest is computed the
     * receiver is reset.
     *
     * @param input
     *            the {@code byte} array
     * @return the computed one way hash value
     * @see #reset()
     */
    public byte[] digest(byte[] input) {
        update(input);
        return digest();
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * {@code MessageDigest} including the name of its algorithm.
     *
     * @return a printable representation for this {@code MessageDigest}
     */
    @Override
    public String toString() {
        return "MESSAGE DIGEST " + algorithm;
    }

    /**
     * Indicates whether to digest are equal by performing a simply
     * byte-per-byte compare of the two digests.
     *
     * @param digesta
     *            the first digest to be compared
     * @param digestb
     *            the second digest to be compared
     * @return {@code true} if the two hashes are equal, {@code false} otherwise
     */
    public static boolean isEqual(byte[] digesta, byte[] digestb) {
        if (digesta.length != digestb.length) {
            return false;
        }
        for (int i = 0; i < digesta.length; i++) {
            if (digesta[i] != digestb[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the name of the algorithm of this {@code MessageDigest}.
     *
     * @return the name of the algorithm of this {@code MessageDigest}
     */
    public final String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the provider associated with this {@code MessageDigest}.
     *
     * @return the provider associated with this {@code MessageDigest}
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Returns the engine digest length in bytes. If the implementation does not
     * implement this function or is not an instance of {@code Cloneable},
     * {@code 0} is returned.
     *
     * @return the digest length in bytes, or {@code 0}
     */
    public final int getDigestLength() {
        int l = engineGetDigestLength();
        if (l != 0) {
            return l;
        }
        if (!(this instanceof Cloneable)) {
            return 0;
        }
        try {
            MessageDigest md = (MessageDigest) clone();
            return md.digest().length;
        } catch (CloneNotSupportedException e) {
            return 0;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        }
        throw new CloneNotSupportedException();
    }

    /**
     * Updates this {@code MessageDigest} using the given {@code input}.
     *
     * @param input
     *            the {@code ByteBuffer}
     */
    public final void update(ByteBuffer input) {
        engineUpdate(input);
    }

    /**
     *
     * The internal MessageDigest implementation
     *
     */
    private static class MessageDigestImpl extends MessageDigest {

        // MessageDigestSpi implementation
        private MessageDigestSpi spiImpl;

        // MessageDigestImpl ctor
        private MessageDigestImpl(MessageDigestSpi messageDigestSpi,
                Provider provider, String algorithm) {
            super(algorithm);
            super.provider = provider;
            spiImpl = messageDigestSpi;
        }

        // engineReset() implementation
        @Override
        protected void engineReset() {
            spiImpl.engineReset();
        }

        // engineDigest() implementation
        @Override
        protected byte[] engineDigest() {
            return spiImpl.engineDigest();
        }

        // engineGetDigestLength() implementation
        @Override
        protected int engineGetDigestLength() {
            return spiImpl.engineGetDigestLength();
        }

        // engineUpdate() implementation
        @Override
        protected void engineUpdate(byte arg0) {
            spiImpl.engineUpdate(arg0);
        }

        // engineUpdate() implementation
        @Override
        protected void engineUpdate(byte[] arg0, int arg1, int arg2) {
            spiImpl.engineUpdate(arg0, arg1, arg2);
        }

        // Returns a clone if the spiImpl is cloneable
        @Override
        public Object clone() throws CloneNotSupportedException {
            if (spiImpl instanceof Cloneable) {
                MessageDigestSpi spi = (MessageDigestSpi) spiImpl.clone();
                return new MessageDigestImpl(spi, getProvider(), getAlgorithm());
            }

            throw new CloneNotSupportedException();
        }
    }
}
