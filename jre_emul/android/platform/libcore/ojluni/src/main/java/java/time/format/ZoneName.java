/*
 * Copyright (c) 2013, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.time.format;

import android.icu.text.TimeZoneNames;
import android.icu.util.TimeZone;
import android.icu.util.ULocale;
import java.util.Locale;

/**
 * A helper class to map a zone name to metazone and back to the appropriate zone id for the
 * particular locale.
 *
 * <p>The zid<->metazone mappings are based on CLDR metaZones.xml. The alias mappings are based on
 * Link entries in tzdb data files and CLDR's supplementalMetadata.xml.
 */
class ZoneName {

    public static String toZid(String zid, Locale locale) {
        // Android-changed: delegate to ICU.
        TimeZoneNames tzNames = TimeZoneNames.getInstance(locale);
        if (tzNames.getAvailableMetaZoneIDs().contains(zid)) {
            // Compare TimeZoneFormat#getTargetRegion.
            ULocale uLocale = ULocale.forLocale(locale);
            String region = uLocale.getCountry();
            if (region.length() == 0) {
                uLocale = ULocale.addLikelySubtags(uLocale);
                region = uLocale.getCountry();
            }
            zid = tzNames.getReferenceZoneID(zid, region);
        }
        return toZid(zid);
    }

    public static String toZid(String zid) {
    // Android-changed: Use ICU TimeZone.getCanonicalID().
    String canonicalCldrId = getSystemCanonicalID(zid);
        if (canonicalCldrId != null) {
            return canonicalCldrId;
        }
        return zid;
    }

  // BEGIN Android-added: Get non-custom system canonical time zone Id from ICU.
  public static String getSystemCanonicalID(String zid) {
    if (TimeZone.UNKNOWN_ZONE_ID.equals(zid)) {
      return zid;
    }
    boolean[] isSystemID = {false};
    String canonicalID = TimeZone.getCanonicalID(zid, isSystemID);
    if (canonicalID == null || !isSystemID[0]) {
      return null;
    }
    return canonicalID;
  }
  // END Android-added: Get non-custom system canonical time zone Id from ICU.

  // Android-removed: zidMap and aliasMap containing zone id data.
  // Android-removed: zidToMzone, mzoneToZid, mzoneToZidL, aliases and their initialization code.
}
