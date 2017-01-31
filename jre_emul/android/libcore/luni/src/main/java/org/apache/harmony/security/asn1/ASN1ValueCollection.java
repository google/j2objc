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

import java.util.Collection;


/**
 * This abstract class represents ASN.1 collection type.
 *
 * The value for such type is a collection of zero or
 * more occurrences of a provided type.
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public abstract class ASN1ValueCollection extends ASN1Constructed {

    /** A value collection of this ASN.1 type */
    public final ASN1Type type;

    /**
     * Constructs ASN1 collection type.
     */
    public ASN1ValueCollection(int tagNumber, ASN1Type type) {
        super(tagNumber);
        this.type = type;
    }

    /**
     * Provides an object's values to be encoded
     *
     * Derived classes should override this method to provide
     * encoding for a selected class of objects.
     *
     * @param - an object to be encoded
     * @return - a collection of object's values to be encoded
     */
    public Collection<?> getValues(Object object) {
        return (Collection<?>) object;
    }
}
