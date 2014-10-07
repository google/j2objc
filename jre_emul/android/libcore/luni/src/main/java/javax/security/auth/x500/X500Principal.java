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

package javax.security.auth.x500;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import org.apache.harmony.security.x501.Name;

/**
 * Represents an X.500 principal, which holds the distinguished name of some
 * network entity. An example of a distinguished name is {@code "O=SomeOrg,
 * OU=SomeOrgUnit, C=US"}. The class can be instantiated from a byte representation
 * of an object identifier (OID), an ASN.1 DER-encoded version, or a simple
 * string holding the distinguished name. The representations must follow either
 * RFC 2253, RFC 1779, or RFC2459.
 */
public final class X500Principal implements Serializable, Principal {

    private static final long serialVersionUID = -500463348111345721L;

    /**
     * Defines a constant for the canonical string format of distinguished
     * names.
     */
    public static final String CANONICAL = "CANONICAL";

    /**
     * Defines a constant for the RFC 1779 string format of distinguished
     * names.
     */
    public static final String RFC1779 = "RFC1779";

    /**
     * Defines a constant for the RFC 2253 string format of distinguished
     * names.
     */
    public static final String RFC2253 = "RFC2253";

    //Distinguished Name
    private transient Name dn;

    /**
     * Creates a new X500Principal from a given ASN.1 DER encoding of a
     * distinguished name.
     *
     * @param name
     *            the ASN.1 DER-encoded distinguished name
     *
     * @throws IllegalArgumentException
     *             if the ASN.1 DER-encoded distinguished name is incorrect
     */
    public X500Principal(byte[] name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        try {
            // FIXME dn = new Name(name);
            dn = (Name) Name.ASN1.decode(name);
        } catch (IOException e) {
            throw incorrectInputEncoding(e);
        }
    }

    /**
     * Creates a new X500Principal from a given ASN.1 DER encoding of a
     * distinguished name.
     *
     * @param in
     *            an {@code InputStream} holding the ASN.1 DER-encoded
     *            distinguished name
     *
     * @throws IllegalArgumentException
     *             if the ASN.1 DER-encoded distinguished name is incorrect
     */
    public X500Principal(InputStream in) {
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        try {
            // FIXME dn = new Name(is);
            dn = (Name) Name.ASN1.decode(in);
        } catch (IOException e) {
            throw incorrectInputEncoding(e);
        }
    }

    private IllegalArgumentException incorrectInputEncoding(IOException e) {
        IllegalArgumentException iae = new IllegalArgumentException("Incorrect input encoding");
        iae.initCause(e);
        throw iae;
    }

    /**
     * Creates a new X500Principal from a string representation of a
     * distinguished name.
     *
     * @param name
     *            the string representation of the distinguished name
     *
     * @throws IllegalArgumentException
     *             if the string representation of the distinguished name is
     *             incorrect
     */
    public X500Principal(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        try {
            dn = new Name(name);
        } catch (IOException e) {
            throw incorrectInputName(e, name);
        }
    }

    public X500Principal(String name, Map<String,String> keywordMap){
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        try {
            dn = new Name(substituteNameFromMap(name, keywordMap));
        } catch (IOException e) {
            throw incorrectInputName(e, name);
        }
    }

    private IllegalArgumentException incorrectInputName(IOException e, String name) {
        IllegalArgumentException iae = new IllegalArgumentException("Incorrect input name:" + name);
        iae.initCause(e);
        throw iae;
    }

    private transient String canonicalName;
    private synchronized String getCanonicalName() {
        if (canonicalName == null) {
            canonicalName = dn.getName(CANONICAL);
        }
        return canonicalName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        X500Principal principal = (X500Principal) o;
        return getCanonicalName().equals(principal.getCanonicalName());
    }

    /**
     * Returns an ASN.1 DER-encoded representation of the distinguished name
     * contained in this X.500 principal.
     *
     * @return the ASN.1 DER-encoded representation
     */
    public byte[] getEncoded() {
        byte[] src = dn.getEncoded();
        byte[] dst = new byte[src.length];
        System.arraycopy(src, 0, dst, 0, dst.length);
        return dst;
    }

    /**
     * Returns a human-readable string representation of the distinguished name
     * contained in this X.500 principal.
     *
     * @return the string representation
     */
    public String getName() {
        return dn.getName(RFC2253);
    }

    /**
     * Returns a string representation of the distinguished name contained in
     * this X.500 principal. The format of the representation can be chosen.
     * Valid arguments are {@link #RFC1779}, {@link #RFC2253}, and
     * {@link #CANONICAL}. The representations are specified in RFC 1779 and RFC
     * 2253, respectively. The canonical form is based on RFC 2253, but adds
     * some canonicalizing operations like removing leading and trailing
     * whitespace, lower-casing the whole name, and bringing it into a
     * normalized Unicode representation.
     *
     * @param format
     *            the name of the format to use for the representation
     *
     * @return the string representation
     *
     * @throws IllegalArgumentException
     *             if the {@code format} argument is not one of the three
     *             mentioned above
     */
    public String getName(String format) {
        if (CANONICAL.equals(format)) {
            return getCanonicalName();
        }

        return dn.getName(format);
    }

    public String getName(String format, Map<String, String> oidMap) {
        String rfc1779Name = dn.getName(RFC1779);
        String rfc2253Name = dn.getName(RFC2253);

        if (format.equalsIgnoreCase("RFC1779")) {
            StringBuilder resultName = new StringBuilder(rfc1779Name);
            int fromIndex = resultName.length();
            int equalIndex = -1;
            while (-1 != (equalIndex = resultName.lastIndexOf("=", fromIndex))) {
                int commaIndex = resultName.lastIndexOf(",", equalIndex);
                String subName = resultName.substring(commaIndex + 1,
                        equalIndex).trim();
                if (subName.length() > 4
                        && subName.substring(0, 4).equals("OID.")) {
                    String subSubName = subName.substring(4);
                    if (oidMap.containsKey(subSubName)) {
                        String replaceName = oidMap.get(subSubName);
                        if(commaIndex > 0) replaceName = " " + replaceName;
                        resultName.replace(commaIndex + 1, equalIndex, replaceName);
                    }
                }
                fromIndex = commaIndex;
            }
            return resultName.toString();
        } else if (format.equalsIgnoreCase("RFC2253")) {
            StringBuilder resultName = new StringBuilder(rfc2253Name);
            StringBuilder subsidyName = new StringBuilder(rfc1779Name);

            int fromIndex = resultName.length();
            int subsidyFromIndex = subsidyName.length();
            int equalIndex = -1;
            int subsidyEqualIndex = -1;
            while (-1 != (equalIndex = resultName.lastIndexOf("=", fromIndex))) {
                subsidyEqualIndex = subsidyName.lastIndexOf("=",
                        subsidyFromIndex);
                int commaIndex = resultName.lastIndexOf(",", equalIndex);
                String subName = resultName.substring(commaIndex + 1,
                        equalIndex).trim();
                if (oidMap.containsKey(subName)) {
                    int subOrignalEndIndex = resultName
                            .indexOf(",", equalIndex);
                    if (subOrignalEndIndex == -1)
                        subOrignalEndIndex = resultName.length();
                    int subGoalEndIndex = subsidyName.indexOf(",",
                            subsidyEqualIndex);
                    if (subGoalEndIndex == -1)
                        subGoalEndIndex = subsidyName.length();
                    resultName.replace(equalIndex + 1, subOrignalEndIndex,
                            subsidyName.substring(subsidyEqualIndex + 1,
                                    subGoalEndIndex));
                    resultName.replace(commaIndex + 1, equalIndex, oidMap
                            .get(subName));
                }
                fromIndex = commaIndex;
                subsidyFromIndex = subsidyEqualIndex - 1;
            }
            return resultName.toString();
        } else {
            throw new IllegalArgumentException("invalid format specified: " + format);
        }
    }

    @Override
    public int hashCode() {
        return getCanonicalName().hashCode();
    }

    @Override
    public String toString() {
        return dn.getName(RFC1779);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(dn.getEncoded());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        dn = (Name) Name.ASN1.decode((byte[]) in.readObject());
    }

    private String substituteNameFromMap(String name, Map<String, String> keywordMap) {
        StringBuilder sbName = new StringBuilder(name);
        int fromIndex = sbName.length();
        int equalIndex;
        while (-1 != (equalIndex = sbName.lastIndexOf("=", fromIndex))) {
            int commaIndex = sbName.lastIndexOf(",", equalIndex);
            String subName = sbName.substring(commaIndex + 1, equalIndex).trim();
            if (keywordMap.containsKey(subName)) {
                sbName.replace(commaIndex + 1, equalIndex, keywordMap.get(subName));
            }
            fromIndex = commaIndex;
        }
        return sbName.toString();
    }
}
