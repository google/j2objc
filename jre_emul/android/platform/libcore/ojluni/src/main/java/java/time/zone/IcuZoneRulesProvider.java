/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  The Android Open Source
 * Project designates this particular file as subject to the "Classpath"
 * exception as provided by The Android Open Source Project in the LICENSE
 * file that accompanied this code.
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
 */

package java.time.zone;

import android.icu.util.TimeZone;
import com.android.icu.util.ExtendedTimeZone;
import java.util.Collections;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import libcore.util.BasicLruCache;

/**
 * A ZoneRulesProvider that generates rules from ICU4J TimeZones. This provider ensures that classes
 * in {@link java.time} use the same time zone information as ICU4J.
 */
public class IcuZoneRulesProvider extends ZoneRulesProvider {

    private final BasicLruCache<String, ZoneRules> cache = new ZoneRulesCache(8);

    @Override
    protected Set<String> provideZoneIds() {
        Set<String> zoneIds = TimeZone.getAvailableIDs(TimeZone.SystemTimeZoneType.ANY, null, null);
        zoneIds = new HashSet<>(zoneIds);
        // java.time assumes ZoneId that start with "GMT" fit the pattern "GMT+HH:mm:ss" which these
        // do not. Since they are equivalent to GMT, just remove these aliases.
        zoneIds.remove("GMT+0");
        zoneIds.remove("GMT-0");
        return zoneIds;
    }

    @Override
    protected ZoneRules provideRules(String zoneId, boolean forCaching) {
        // Ignore forCaching, as this is a static provider.
        return cache.get(zoneId);
    }

    @Override
    protected NavigableMap<String, ZoneRules> provideVersions(String zoneId) {
        return new TreeMap<>(
            Collections.singletonMap(
                TimeZone.getTZDataVersion(), provideRules(zoneId, /* forCaching */ false)));
    }

    static ZoneRules generateZoneRules(String zoneId) {
        return ExtendedTimeZone.getInstance(zoneId).createZoneRules();
    }

    private static class ZoneRulesCache extends BasicLruCache<String, ZoneRules> {

        public ZoneRulesCache(int maxSize) {
          super(maxSize);
        }

        @Override
        protected ZoneRules create(String zoneId) {
            String canonicalId = TimeZone.getCanonicalID(zoneId);
            if (!canonicalId.equals(zoneId)) {
                // Return the same object as the canonical one, to avoid wasting space, but cache
                // it under the non-cannonical name as well, to avoid future getCanonicalID calls.
                return get(canonicalId);
            }
            return generateZoneRules(zoneId);
        }
    }
}
