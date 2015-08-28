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
* @author Vladimir N. Molotkov, Stepan M. Mishura
* @version $Revision$
*/

package org.apache.harmony.security.asn1;

import java.io.IOException;
import java.util.Arrays;


/**
 * This class represents ASN.1 octet string type.
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public class ASN1OctetString extends ASN1StringType {

    /** default implementation */
    private static final ASN1OctetString ASN1 = new ASN1OctetString();

    /**
     * Constructs ASN.1 octet string type
     *
     * The constructor is provided for inheritance purposes
     * when there is a need to create a custom ASN.1 octet string type.
     * To get a default implementation it is recommended to use
     * getInstance() method.
     */
    public ASN1OctetString() {
        super(TAG_OCTETSTRING);
    }

    /**
     * Returns ASN.1 octet string type default implementation
     *
     * The default implementation works with encoding
     * that is represented as byte array.
     */
    public static ASN1OctetString getInstance() {
        return ASN1;
    }

    @Override public Object decode(BerInputStream in) throws IOException {
        in.readOctetString();

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    /**
     * Extracts array of bytes from BER input stream.
     *
     * @return array of bytes
     */
    @Override public Object getDecodedObject(BerInputStream in) throws IOException {
        return Arrays.copyOfRange(in.buffer, in.contentOffset, in.contentOffset + in.length);
    }

    @Override public void encodeContent(BerOutputStream out) {
        out.encodeOctetString();
    }

    @Override public void setEncodingContent(BerOutputStream out) {
        out.length = ((byte[]) out.content).length;
    }
}
