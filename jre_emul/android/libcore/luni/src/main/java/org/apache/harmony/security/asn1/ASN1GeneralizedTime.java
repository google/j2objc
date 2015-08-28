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
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class represents ASN.1 GeneralizedTime type.
 *
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */
public final class ASN1GeneralizedTime extends ASN1Time {

    // default implementation
    private static final ASN1GeneralizedTime ASN1 = new ASN1GeneralizedTime();

    /**
     * Constructs ASN.1 GeneralizedTime type
     *
     * The constructor is provided for inheritance purposes
     * when there is a need to create a custom ASN.1 GeneralizedTime type.
     * To get a default implementation it is recommended to use
     * getInstance() method.
     */
    public ASN1GeneralizedTime() {
        super(TAG_GENERALIZEDTIME);
    }

    /**
     * Returns ASN.1 GeneralizedTime type default implementation
     *
     * The default implementation works with encoding
     * that is represented as Date object.
     *
     * @return ASN.1 GeneralizedTime type default implementation
     */
    public static ASN1GeneralizedTime getInstance() {
        return ASN1;
    }

    public Object decode(BerInputStream in) throws IOException {
        in.readGeneralizedTime();

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    public void encodeContent(BerOutputStream out) {
        out.encodeGeneralizedTime();
    }

    // FIXME support only one format for encoding, do we need others?
    //
    // According to X.680:
    // four digit year, seconds always presented
    // and fractional-seconds elements without
    // trailing 0's (must be cut later from content)
    private static final String GEN_PATTERN = "yyyyMMddHHmmss.SSS";

    public void setEncodingContent(BerOutputStream out) {
        SimpleDateFormat sdf = new SimpleDateFormat(GEN_PATTERN, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String temp = sdf.format(out.content);
        // cut off trailing 0s
        int nullId;
        int currLength;
        while (((nullId = temp.lastIndexOf('0', currLength = temp.length() - 1)) != -1)
                & (nullId == currLength)) {
            temp = temp.substring(0, nullId);
        }
        // deal with point (cut off if it is last char)
        if (temp.charAt(currLength) == '.') {
            temp = temp.substring(0, currLength);
        }

        out.content = (temp + "Z").getBytes(StandardCharsets.UTF_8);
        out.length = ((byte[]) out.content).length;
    }
}
