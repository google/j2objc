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
* @author Alexander V. Esin, Stepan M. Mishura
* @version $Revision$
*/

package org.apache.harmony.security.utils;

import java.util.Arrays;

/**
 * Instance of this class represents ObjectIdentifier (OID).
 *
 * OID is represented as a sequence of subidentifier.
 * Each subidentifier is represented as non negative integer value.
 * There are at least 2 subidentifiers in the sequence.
 *
 * Valid values for first subidentifier are 0, 1 and 2.
 * If the first subidentifier has 0 or 1 value the second
 * subidentifier MUST be less then 40.
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */

public final class ObjectIdentifier {

    //OID as array of integers
    private final int[] oid;

    //hash code
    private int hash = -1;

    //OID as string
    private String soid;

    // stores the following: "OID." + soid
    private String sOID;

    // OID alias name
    private String name;

    // OID's group
    private Object group;

    /**
     * Creates ObjectIdentifier(OID) from array of integers.
     *
     * @param oid - array of integers
     * @return - OID object
     * @throws NullPointerException     - if oid is null
     * @throws IllegalArgumentException - if oid is invalid
     */
    public ObjectIdentifier(int[] oid) {

        validateOid(oid);

        this.oid = oid;
    }

    /**
     * Creates ObjectIdentifier(OID) from array of integers.
     *
     * @param oid - array of integers
     * @param name - name of OID
     * @param oidGroup - OID's group. Is used to separate different OID's
     * @return - OID object
     * @throws NullPointerException     - if oid is null
     * @throws IllegalArgumentException - if oid is invalid
     */
    public ObjectIdentifier(int[] oid, String name, Object oidGroup) {
        this(oid);

        if (oidGroup == null) {
            throw new NullPointerException("oidGroup == null");
        }
        this.group = oidGroup;

        this.name = name;
        toOIDString(); // init soid & sOID
    }

    /**
     * Gets OID.
     *
     * @return oid
     */
    public int[] getOid() {
        return oid;
    }

    /**
     * Gets OID's name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets OID's group.
     *
     * @return group
     */
    public Object getGroup() {
        return group;
    }

    /**
     * Compares object with OID for equality.
     *
     * @return true if object is ObjectIdentifier and it has the same
     *         representation as array of integers, otherwise false
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return Arrays.equals(oid, ((ObjectIdentifier) o).oid);
    }

    /**
     * Add "OID." to the beginning of string representation.
     *
     * @return oid as string
     */
    public String toOIDString() {
        if (sOID == null) {
            sOID = "OID." + toString();
        }
        return sOID;
    }

    /**
     * Overrides Object.toString()
     *
     * @return oid as string
     */
    public String toString() {
        if (soid == null) {
            StringBuilder sb = new StringBuilder(4 * oid.length);

            for (int i = 0; i < oid.length - 1; ++i) {
                sb.append(oid[i]);
                sb.append('.');
            }
            sb.append(oid[oid.length - 1]);
            soid = sb.toString();
        }
        return soid;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        if (hash == -1) {
            hash = hashIntArray(oid);
        }
        return hash;
    }

    /**
     * Validates ObjectIdentifier (OID).
     *
     * @param oid - oid as array of integers
     * @throws NullPointerException     - if oid is null
     * @throws IllegalArgumentException - if oid is invalid
     */
    public static void validateOid(int[] oid) {

        if (oid == null) {
            throw new NullPointerException("oid == null");
        }

        if (oid.length < 2) {
            throw new IllegalArgumentException("OID MUST have at least 2 subidentifiers");
        }

        if (oid[0] > 2) {
            throw new IllegalArgumentException("Valid values for first subidentifier are 0, 1 and 2");
        } else if (oid[0] != 2 && oid[1] > 39) {
            throw new IllegalArgumentException("If the first subidentifier has 0 or 1 value the second subidentifier value MUST be less than 40");
        }
    }

    /**
     * Returns hash code for array of integers
     *
     * @param oid - array of integers
     */
    public static int hashIntArray(int[] array) {
        int intHash = 0;
        for (int i = 0; i < array.length && i < 4; i++) {
            intHash += array[i] << (8 * i); //TODO what about to find better one?
        }
        return intHash & 0x7FFFFFFF; // only positive
    }
}
