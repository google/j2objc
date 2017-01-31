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

package org.apache.harmony.security.x509;

import org.apache.harmony.security.asn1.ASN1Choice;
import org.apache.harmony.security.asn1.ASN1GeneralizedTime;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.ASN1UTCTime;

/**
 * Class represents the work with the following X.509 structure:
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 * <pre>
 * Time ::= CHOICE {
 *       utcTime        UTCTime,
 *       generalTime    GeneralizedTime
 * }
 * </pre>
 */
public final class Time {
    private static final long JAN_01_2050 = 2524608000000L;

    public static final ASN1Choice ASN1 = new ASN1Choice(new ASN1Type[] {
            ASN1GeneralizedTime.getInstance(), ASN1UTCTime.getInstance() }) {

        public int getIndex(java.lang.Object object) {
            // choose encoding method (see RFC 3280 p. 22)
            if (((java.util.Date) object).getTime() < JAN_01_2050) {
                return 1; // it is before 2050, so encode as UTCTime
            } else {
                return 0; // it is after 2050, encode as GeneralizedTime
            }
        }

        public Object getObjectToEncode(Object object) {
            return object;
        }
    };
}

