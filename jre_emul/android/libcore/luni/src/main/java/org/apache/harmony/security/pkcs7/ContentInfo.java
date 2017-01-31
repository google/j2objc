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
* @author Boris Kuznetsov
* @version $Revision$
*/
package org.apache.harmony.security.pkcs7;

import java.io.IOException;
import java.util.Arrays;
import org.apache.harmony.security.asn1.ASN1Any;
import org.apache.harmony.security.asn1.ASN1Explicit;
import org.apache.harmony.security.asn1.ASN1OctetString;
import org.apache.harmony.security.asn1.ASN1Oid;
import org.apache.harmony.security.asn1.ASN1Sequence;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;

/**
 * As defined in PKCS #7: Cryptographic Message Syntax Standard
 * (http://www.ietf.org/rfc/rfc2315.txt)
 *
 * ContentInfo ::= SEQUENCE {
 *       contentType  ContentType,
 *       content      [0] EXPLICIT ANY DEFINED BY contentType OPTIONAL
 *     }
 */
public final class ContentInfo {

    // OIDs
    public static final int[] DATA = new int[] {1, 2, 840, 113549, 1, 7, 1};
    public static final int[] SIGNED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 2};
    public static final int[] ENVELOPED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 3};
    public static final int[] SIGNED_AND_ENVELOPED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 4};
    public static final int[] DIGESTED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 5};
    public static final int[] ENCRYPTED_DATA = new int[] {1, 2, 840, 113549, 1, 7, 6};

    private final int[] oid;
    private final Object content;
    private byte[] encoding;

    private ContentInfo(int[] oid, Object content, byte[] encoding) {
        this.oid = oid;
        this.content = content;
        this.encoding = encoding;
    }

    public SignedData getSignedData() {
        if (Arrays.equals(oid, SIGNED_DATA)) {
            return (SignedData)content;
        }
        return null;
    }

    public Object getContent() {
        return content;
    }

    public int[] getContentType() {
        return oid;
    }

    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        // Note: this is internal object and can not be accessible from
        // public API, so encoding is not copied. The classes which use
        // this class should copy encoding before passing it out.
        return encoding;
    }

    @Override public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("==== ContentInfo:");
        res.append("\n== ContentType (OID): ");
        for (int i : oid) {
            res.append(i);
            res.append(' ');
        }
        res.append("\n== Content: ");
        if (content != null) {
            res.append("\n");
            res.append(content.toString());
        }
        res.append("\n== Content End");
        res.append("\n==== ContentInfo End\n");
        return res.toString();
    }

    public static final ASN1Sequence ASN1 =
        new ASN1Sequence(new ASN1Type[] {
                ASN1Oid.getInstance(),
                new ASN1Explicit(0, ASN1Any.getInstance())
                })  {
        {
            setOptional(1); // content is optional
        }

        @Override protected void getValues(Object object, Object[] values) {
            ContentInfo ci = (ContentInfo) object;
            values[0] = ci.oid;
            if (ci.content != null) {
                if (Arrays.equals(ci.oid, DATA)) {
                    if (ci.content != null) {
                        values[1] =
                            ASN1OctetString.getInstance().encode(ci.content);
                    }
                } else if (ci.content instanceof SignedData) {
                    values[1] = SignedData.ASN1.encode(ci.content);
                } else {
                    values[1] = ci.content;
                }
            }
        }

        @Override protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            int[] oid = (int[]) values[0];
            if (Arrays.equals(oid, DATA)) {
                if (values[1] != null) {
                    return new ContentInfo(oid,
                            ASN1OctetString.getInstance().decode((byte[])values[1]),
                            in.getEncoded());
                }  else {
                    return new ContentInfo((int[])values[0], null,
                            in.getEncoded());
                }
            }
            if (Arrays.equals(oid, SIGNED_DATA)) {
                return new ContentInfo((int[])values[0],
                        SignedData.ASN1.decode((byte[])values[1]),
                        in.getEncoded());
            }
            return new ContentInfo((int[]) values[0], values[1], in.getEncoded());
        }
   };
}
