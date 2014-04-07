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
* @author Vladimir N. Molotkov
* @version $Revision$
*/

package org.apache.harmony.security.asn1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class represents ASN.1 UTCTime type
 *
 * @see http://asn1.elibel.tm.fr/en/standards/index.htm
 */
public final class ASN1UTCTime extends ASN1Time {

    /** Length for the pattern: YYMMDDhhmm'Z' */
    public static final int UTC_HM = 11;

    /** Length for the pattern: YYMMDDhhmmss'Z' */
    public static final int UTC_HMS = 13;

    /** Length for the pattern: YYMMDDhhmm('+'/'-')hhmm */
    public static final int UTC_LOCAL_HM = 15;

    /** Length for the pattern: YYMMDDhhmmss('+'/'-')hhmm */
    public static final int UTC_LOCAL_HMS = 17;

    /** default implementation */
    private static final ASN1UTCTime ASN1 = new ASN1UTCTime();

    /**
     * Constructs ASN.1 UTCTime type
     *
     * The constructor is provided for inheritance purposes
     * when there is a need to create a custom ASN.1 UTCTime type.
     * To get a default implementation it is recommended to use
     * getInstance() method.
     */
    public ASN1UTCTime() {
        super(TAG_UTCTIME);
    }

    /**
     * Returns ASN.1 UTCTime type default implementation
     *
     * The default implementation works with encoding
     * that is represented as Date object.
     *
     * @return ASN.1 UTCTime type default implementation
     */
    public static ASN1UTCTime getInstance() {
        return ASN1;
    }

    @Override public Object decode(BerInputStream in) throws IOException {
        in.readUTCTime();

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    @Override public void encodeContent(BerOutputStream out) {
        out.encodeUTCTime();
    }

    // FIXME support only one format for encoding, do we need others?
    //
    // According to X.680 coordinated universal time format:
    // two digit year, seconds always presented,
    // no fractional-seconds elements, 'Z' at the end
    private static final String UTC_PATTERN = "yyMMddHHmmss'Z'";

    @Override public void setEncodingContent(BerOutputStream out) {
        SimpleDateFormat sdf = new SimpleDateFormat(UTC_PATTERN, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        out.content = sdf.format(out.content).getBytes(StandardCharsets.UTF_8);
        out.length = ((byte[]) out.content).length;
    }
}
