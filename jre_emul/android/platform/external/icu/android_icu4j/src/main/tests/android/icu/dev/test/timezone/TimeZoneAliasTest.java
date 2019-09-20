/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2002-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
*/
package android.icu.dev.test.timezone;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.util.TimeZone;

/**
 * Class for testing TimeZones for consistency
 * @author Davis
 *
 */
public class TimeZoneAliasTest extends TestFmwk {
    /**
     * There are two things to check aliases for:<br>
     * 1. the alias set must be uniform: if a isAlias b, then aliasSet(a) == aliasSet(b)<br>
     * 2. all aliases must have the same offsets
      */
    // TODO(user): not working before so turned off
    @Ignore
    @Test
    public void TestAliases() {
        Zone.Seconds seconds = new Zone.Seconds();
        for (Iterator it = Zone.getZoneSet().iterator(); it.hasNext(); ) {
            Zone zone = (Zone)it.next();
            String id = zone.id;
            if (id.indexOf('/') < 0 && (id.endsWith("ST") || id.endsWith("DT"))) {
                if (zone.minRecentOffset != zone.maxRecentOffset) {
                    errln(
                        "Standard or Daylight Time not constant: " + id
                        + ": " + Zone.formatHours(zone.minRecentOffset)
                        + " != " + Zone.formatHours(zone.maxRecentOffset));
                }
            }
            Set aliases = zone.getPurportedAliases();
            Set aliasesSet = new TreeSet(aliases);
            aliasesSet.add(id); // for comparison
            Iterator aliasIterator = aliases.iterator();
            while (aliasIterator.hasNext()) {
                String otherId = (String)aliasIterator.next();
                Zone otherZone = Zone.make(otherId);
                Set otherAliases = otherZone.getPurportedAliases();
                otherAliases.add(otherId); // for comparison
                if (!aliasesSet.equals(otherAliases)) {
                    errln(
                        "Aliases Unsymmetric: "
                        + id + " => " + Zone.bf.join(aliasesSet)
                        + "; "
                        + otherId + " => " + Zone.bf.join(otherAliases));
                }
                if (zone.findOffsetOrdering(otherZone, seconds) != 0) {
                    errln("Aliases differ: " + id + ", " + otherId
                         + " differ at " + seconds);
                }
            }
        }
    }

    /**
     * We check to see that every timezone that is not an alias is actually different!
     */
    // TODO(user): not working before so turned off
    @Ignore
    @Test
    public void TestDifferences() {
        Zone last = null;
        Zone.Seconds diffDate = new Zone.Seconds();
        for (Iterator it = Zone.getZoneSet().iterator(); it.hasNext();) {
            Zone testZone = (Zone)it.next();
            if (last != null) {
                String common = testZone + "\tvs " + last + ":\t";
                int diff = testZone.findOffsetOrdering(last, diffDate);
                if (diff != 0) {
                    logln("\t" + common + "difference at: " + diffDate
                        + ", " + Zone.formatHours(diff) + "hr");
                } else if (testZone.isRealAlias(last)) {
                    logln("\t" + common + "alias, no difference");
                } else {
                    errln(common + "NOT ALIAS BUT NO DIFFERENCE!");
                }
            }
            last = testZone;
        }
    }

    /**
     * Utility for printing out zones to be translated.
     */
    public static void TestGenerateZones() {
        int count = 1;
        for (Iterator it = Zone.getUniqueZoneSet().iterator(); it.hasNext();) {
            Zone zone = (Zone)it.next();
            System.out.println(zone.toString(count++));
        }
    }

    /** Utility; ought to be someplace common
     */
    // remove dependency on bagformatter for now
    static class CollectionJoiner {
        private String separator;
        CollectionJoiner(String separator) {
            this.separator = separator;
        }
        String join(Collection c) {
            StringBuffer result = new StringBuffer();
            boolean isFirst = true;
            for (Iterator it = c.iterator(); it.hasNext(); ) {
                if (!isFirst) result.append(separator);
                else isFirst = false;
                result.append(it.next().toString());
            }
            return result.toString();
        }
    }


    /**
     * The guts is in this subclass. It sucks in all the data from the zones,
     * and analyses it. It constructs some mappings for the unique ids,
     * etc.<br>
     * The main tricky bit is that for performance it pre-analyses all zones
     * for inflections points; the points in time where the offset changes.
     * The zones can then be sorted by those points, which allows us to
     * avoid expensive comparisons.
     * @author Davis
     */
    static class Zone implements Comparable {
        // class fields
      // static private final BagFormatter bf = new BagFormatter().setSeparator(", ");
        private static final CollectionJoiner bf = new CollectionJoiner(", ");
        static private final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);
        static private final NumberFormat nf = NumberFormat.getInstance(Locale.US);
        static private final long HOUR = 1000*60*60;
        static private final double DHOUR = HOUR;
        static private final long DAY = 24*HOUR;
        static private final long GROSS_PERIOD = 30*DAY;
        static private final long EPSILON = HOUR/4;
        static private final int currentYear = new GregorianCalendar().get(Calendar.YEAR);
        static private final long endDate = getDate((currentYear+1),0,1).getTime();
        static private final long endDate2 = getDate((currentYear+1),6,1).getTime();
        static private final long recentLimit = getDate((currentYear-1),6,1).getTime();
        static private final long startDate = getDate(1905,0,1).getTime();

        static private final Map idToZone = new HashMap();
        static private final Set zoneSet = new TreeSet();
        static private final Set uniqueZoneSet = new TreeSet();
        static private final Map idToRealAliases = new HashMap();

        // build everything once.
        static {
            String [] foo = TimeZone.getAvailableIDs();
            for (int i = 0; i < foo.length; ++i) {
                zoneSet.add(Zone.make(foo[i]));
            }
            Zone last = null;
            Zone.Seconds diffDate = new Zone.Seconds();
            String lastUnique = "";
            for (Iterator it = Zone.getZoneSet().iterator(); it.hasNext();) {
                Zone testZone = (Zone)it.next();
                if (last == null) {
                    uniqueZoneSet.add(testZone);
                    lastUnique = testZone.id;
                } else {
                    int diff = testZone.findOffsetOrdering(last, diffDate);
                    if (diff != 0) {
                        uniqueZoneSet.add(testZone);
                        lastUnique = testZone.id;
                    } else {
                        Set aliases = (Set)idToRealAliases.get(lastUnique);
                        if (aliases == null) {
                            aliases = new TreeSet();
                            idToRealAliases.put(lastUnique, aliases);
                        }
                        aliases.add(testZone.id);
                    }
                }
                last = testZone;
            }
        }

        static public Set getZoneSet() {
            return zoneSet;
        }

        public static Set getUniqueZoneSet() {
            return uniqueZoneSet;
        }

        static public Zone make(String id) {
            Zone result = (Zone)idToZone.get(id);
            if (result != null) return result;
            result = new Zone(id);
            idToZone.put(id, result);
            return result;
        }

        static public String formatHours(int hours) {
            return nf.format(hours/DHOUR);
        }

        // utility class for date return, because Date is clunky.
        public static class Seconds {
            public long seconds = Long.MIN_VALUE;
            public String toString() {
                if (seconds == Long.MIN_VALUE) return "n/a";
                return df.format(new Date(seconds));
            }
        }

        // instance fields
        // we keep min/max offsets not only over all time (that we care about)
        // but also separate ones for recent years.
        private String id;
        private TimeZone zone;
        // computed below
        private int minOffset;
        private int maxOffset;
        private int minRecentOffset;
        private int maxRecentOffset;
        private List inflectionPoints = new ArrayList();
        private Set purportedAliases = new TreeSet();

        private Zone(String id) { // for interal use only; use make instead!
            zone = TimeZone.getTimeZone(id);
            this.id = id;

            // get aliases
            int equivCount = TimeZone.countEquivalentIDs(id);
            for (int j = 0; j < equivCount; ++j) {
                String altID = TimeZone.getEquivalentID(id, j);
                if (altID.equals(id)) continue;
                purportedAliases.add(altID);
            }

            // find inflexion points; times where the offset changed
            long lastDate = endDate;
            if (zone.getOffset(lastDate) < zone.getOffset(endDate2)) lastDate = endDate2;
            maxRecentOffset = minRecentOffset = minOffset = maxOffset = zone.getOffset(lastDate);

            inflectionPoints.add(new Long(lastDate));
            int lastOffset = zone.getOffset(endDate);
            long lastInflection = endDate;

            // we do a gross search, then narrow in when we find a difference from the last one
            for (long currentDate = endDate; currentDate >= startDate; currentDate -= GROSS_PERIOD) {
                int currentOffset = zone.getOffset(currentDate);
                if (currentOffset != lastOffset) { // Binary Search
                    if (currentOffset < minOffset) minOffset = currentOffset;
                    if (currentOffset > maxOffset) maxOffset = currentOffset;
                    if (lastInflection >= recentLimit) {
                        if (currentOffset < minRecentOffset) minRecentOffset = currentOffset;
                        if (currentOffset > maxRecentOffset) maxRecentOffset = currentOffset;
                    }
                    long low = currentDate;
                    long high = lastDate;
                    while (low - high > EPSILON) {
                        long mid = (high + low)/2;
                        int midOffset = zone.getOffset(mid);
                        if (midOffset == low) {
                            low = mid;
                        } else {
                            high = mid;
                        }
                    }
                    inflectionPoints.add(new Long(low));
                    lastInflection = low;
                }
                lastOffset = currentOffset;
            }
            inflectionPoints.add(new Long(startDate)); // just to cap it off for comparisons.
        }

        // we assume that places will not convert time zones then back within one day
        // so we go first by half
        public int findOffsetOrdering(Zone other, Seconds dateDiffFound) {
            //System.out.println("-diff: " + id + "\t" + other.id);
            int result = 0;
            long seconds = 0;
            int min = inflectionPoints.size();
            if (other.inflectionPoints.size() < min) min = other.inflectionPoints.size();
            main:
            {
                for (int i = 0; i < min; ++i) {
                    long myIP = ((Long)inflectionPoints.get(i)).longValue();
                    long otherIP = ((Long)other.inflectionPoints.get(i)).longValue();
                    if (myIP > otherIP) { // take lowest, for transitivity (semi)
                        long temp = myIP;
                        myIP = otherIP;
                        otherIP = temp;
                    }
                    result = zone.getOffset(myIP) - other.zone.getOffset(myIP);
                    if (result != 0) {
                        seconds = myIP;
                        break main;
                    }
                    if (myIP == otherIP) continue; // test other if different
                    myIP = otherIP;
                    result = zone.getOffset(myIP) - other.zone.getOffset(myIP);
                    if (result != 0) {
                        seconds = myIP;
                        break main;
                    }
                }
                // if they are equal so far, we don't care about the rest
                result = 0;
                seconds = Long.MIN_VALUE;
                break main;
            }
            //System.out.println("+diff: " + (result/HOUR) + "\t" + dateDiffFound);
            if (dateDiffFound != null) dateDiffFound.seconds = seconds;
            return result;
        }

        // internal buffer to avoid creation all the time.
        private Seconds diffDateReturn = new Seconds();

        public int compareTo(Object o) {
            Zone other = (Zone)o;
            // first order by max and min offsets
            // min will usually correspond to standard time, max to daylight
            // unless there have been historical shifts
            if (minRecentOffset < other.minRecentOffset) return -1;
            if (minRecentOffset > other.minRecentOffset) return 1;
            if (maxRecentOffset < other.maxRecentOffset) return -1;
            if (maxRecentOffset > other.maxRecentOffset) return 1;
            // now check that all offsets are the same over history
            int diffDate = findOffsetOrdering(other, diffDateReturn);
            if (diffDate != 0) return diffDate;
            // choose longer name first!!
            if (id.length() != other.id.length()) {
                if (id.length() < other.id.length()) return 1;
                return -1;
            }
            return id.compareTo(other.id);
        }

        public Set getPurportedAliases() {
            return new TreeSet(purportedAliases); // clone for safety
        }

        public boolean isPurportedAlias(String zoneID) {
            return purportedAliases.contains(zoneID);
        }

        public boolean isRealAlias(Zone z) {
            return purportedAliases.contains(z.id);
        }

        public String getPurportedAliasesAsString() {
            Set s = getPurportedAliases();
            if (s.size() == 0) return "";
            return " " + bf.join(s);
        }

        public String getRealAliasesAsString() {
            Set s = (Set)idToRealAliases.get(id);
            if (s == null) return "";
            return " *" + bf.join(s);
        }

        public String getCity() {
            int pos = id.lastIndexOf(('/'));
            String city = id.substring(pos+1);
            return city.replace('_',' ');
        }

        public String toString() {
            return toString(-1);
        }

        /**
         * Where count > 0, returns string that is set up for translation
         */
        public String toString(int count) {
            String city = getCity();
            String hours = formatHours(minRecentOffset)
                + (minRecentOffset != maxRecentOffset
                    ? "," + formatHours(maxRecentOffset)
                    : "");
            if (count < 0) {
                return id + getPurportedAliasesAsString() + " (" + hours + ")";
            }
            // for getting template for translation
            return "\t{\t\"" + id + "\"\t// [" + count + "] " + hours
                + getRealAliasesAsString() + "\r\n"
                + "\t\t// translate the following!!\r\n"
                + (minRecentOffset != maxRecentOffset
                    ? "\t\t\"" + city + " Standard Time\"\r\n"
                    + "\t\t\"" + city + "-ST\"\r\n"
                    + "\t\t\"" + city + " Daylight Time\"\r\n"
                    + "\t\t\"" + city + "-DT\"\r\n"
                    : "\t\t\"\"\r\n"
                    + "\t\t\"\"\r\n"
                    + "\t\t\"\"\r\n"
                    + "\t\t\"\"\r\n")
                + "\t\t\"" + city + " Time\"\r\n"
                + "\t\t\"" + city + "-T\"\r\n"
                + "\t\t\"" + city + "\"\r\n"
                + "\t}";
        }
    }
}
