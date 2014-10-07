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


/**
 * This abstract class is the super class for all primitive ASN.1 types
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public abstract class ASN1Primitive extends ASN1Type {

    public ASN1Primitive(int tagNumber) {
        super(tagNumber);
    }

    /**
     * Tests provided identifier.
     *
     * @param identifier identifier to be verified
     * @return true if identifier correspond to primitive identifier of this
     *     ASN.1 type, otherwise false
     */
    public final boolean checkTag(int identifier) {
        return this.id == identifier;
    }

    public void encodeASN(BerOutputStream out) {
        out.encodeTag(id);
        encodeContent(out);
    }
}
