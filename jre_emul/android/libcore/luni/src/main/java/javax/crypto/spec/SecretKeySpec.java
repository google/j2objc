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
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package javax.crypto.spec;

import java.io.Serializable;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKey;

/**
 * A key specification for a <code>SecretKey</code> and also a secret key
 * implementation that is provider-independent. It can be used for raw secret
 * keys that can be specified as <code>byte[]</code>.
 */
public class SecretKeySpec implements SecretKey, KeySpec, Serializable {

    // The 5.0 spec. doesn't declare this serialVersionUID field
    // In order to be compatible it is explicitly declared here
    // for details see HARMONY-233
    private static final long serialVersionUID = 6577238317307289933L;

    private final byte[] key;
    private final String algorithm;

    /**
     * Creates a new <code>SecretKeySpec</code> for the specified key data and
     * algorithm name.
     *
     * @param key
     *            the key data.
     * @param algorithm
     *            the algorithm name.
     * @throws IllegalArgumentException
     *             if the key data or the algorithm name is null or if the key
     *             data is empty.
     */
    public SecretKeySpec(byte[] key, String algorithm) {
        if (key == null) {
            throw new IllegalArgumentException("key == null");
        }
        if (key.length == 0) {
            throw new IllegalArgumentException("key.length == 0");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm == null");
        }

        this.algorithm = algorithm;
        this.key = new byte[key.length];
        System.arraycopy(key, 0, this.key, 0, key.length);
    }

    /**
     * Creates a new <code>SecretKeySpec</code> for the key data from the
     * specified buffer <code>key</code> starting at <code>offset</code> with
     * length <code>len</code> and the specified <code>algorithm</code> name.
     *
     * @param key
     *            the key data.
     * @param offset
     *            the offset.
     * @param len
     *            the size of the key data.
     * @param algorithm
     *            the algorithm name.
     * @throws IllegalArgumentException
     *             if the key data or the algorithm name is null, the key data
     *             is empty or <code>offset</code> and <code>len</code> do not
     *             specify a valid chunk in the buffer <code>key</code>.
     * @throws ArrayIndexOutOfBoundsException
     *             if <code>offset</code> or <code>len</code> is negative.
     */
    public SecretKeySpec(byte[] key, int offset, int len, String algorithm) {
        if (key == null) {
            throw new IllegalArgumentException("key == null");
        }
        if (key.length == 0) {
            throw new IllegalArgumentException("key.length == 0");
        }
        if (len < 0 || offset < 0) {
            throw new ArrayIndexOutOfBoundsException("len < 0 || offset < 0");
        }
        if (key.length - offset < len) {
            throw new IllegalArgumentException("key too short");
        }
        if (algorithm == null) {
            throw new IllegalArgumentException("algorithm == null");
        }
        this.algorithm = algorithm;
        this.key = new byte[len];
        System.arraycopy(key, offset, this.key, 0, len);
    }

    /**
     * Returns the algorithm name.
     *
     * @return the algorithm name.
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the name of the format used to encode the key.
     *
     * @return the format name "RAW".
     */
    public String getFormat() {
        return "RAW";
    }

    /**
     * Returns the encoded form of this secret key.
     *
     * @return the encoded form of this secret key.
     */
    public byte[] getEncoded() {
        byte[] result = new byte[key.length];
        System.arraycopy(key, 0, result, 0, key.length);
        return result;
    }

    /**
     * Returns the hash code of this <code>SecretKeySpec</code> object.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        int result = algorithm.length();
        for (byte element : key) {
            result += element;
        }
        return result;
    }

    /**
     * Compares the specified object with this <code>SecretKeySpec</code>
     * instance.
     *
     * @param obj
     *            the object to compare.
     * @return true if the algorithm name and key of both object are equal,
     *         otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SecretKeySpec)) {
            return false;
        }
        SecretKeySpec ks = (SecretKeySpec) obj;
        return (algorithm.equalsIgnoreCase(ks.algorithm))
            && (Arrays.equals(key, ks.key));
    }
}
