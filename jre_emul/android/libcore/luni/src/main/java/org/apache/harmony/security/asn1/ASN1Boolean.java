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


/**
 * This class represents ASN.1 Boolean type.
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public final class ASN1Boolean extends ASN1Primitive {

    /** default implementation */
    private static final ASN1Boolean ASN1 = new ASN1Boolean();

    /**
     * Constructs ASN.1 Boolean type
     *
     * The constructor is provided for inheritance purposes
     * when there is a need to create a custom ASN.1 Boolean type.
     * To get a default implementation it is recommended to use
     * getInstance() method.
     */
    public ASN1Boolean() {
        super(TAG_BOOLEAN);
    }

    /**
     * Returns ASN.1 Boolean type default implementation
     *
     * The default implementation works with encoding
     * that is represented as Boolean object.
     *
     * @return ASN.1 Boolean type default implementation
     */
    public static ASN1Boolean getInstance() {
        return ASN1;
    }

    public Object decode(BerInputStream in) throws IOException {
        in.readBoolean();

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    /**
     * Extracts Boolean object from BER input stream.
     *
     * @param in - BER input stream
     * @return java.lang.Boolean object
     */
    @Override public Object getDecodedObject(BerInputStream in) throws IOException {
        if (in.buffer[in.contentOffset] == 0) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    public void encodeContent(BerOutputStream out) {
        out.encodeBoolean();
    }

    public void setEncodingContent(BerOutputStream out) {
        out.length = 1;
    }
}
