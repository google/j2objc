/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2007-2015, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import android.icu.impl.Grego;

/**
 * <code>VTimeZone</code> is a class implementing RFC2445 VTIMEZONE.  You can create a
 * <code>VTimeZone</code> instance from a time zone ID supported by <code>TimeZone</code>.
 * With the <code>VTimeZone</code> instance created from the ID, you can write out the rule
 * in RFC2445 VTIMEZONE format.  Also, you can create a <code>VTimeZone</code> instance
 * from RFC2445 VTIMEZONE data stream, which allows you to calculate time
 * zone offset by the rules defined by the data.<br><br>
 * 
 * Note: The consumer of this class reading or writing VTIMEZONE data is responsible to
 * decode or encode Non-ASCII text.  Methods reading/writing VTIMEZONE data in this class
 * do nothing with MIME encoding.
 * 
 * @hide Only a subset of ICU is exposed in Android
 */
public class VTimeZone extends BasicTimeZone {

    private static final long serialVersionUID = -6851467294127795902L;

    /**
     * Create a <code>VTimeZone</code> instance by the time zone ID.
     * 
     * @param tzid The time zone ID, such as America/New_York
     * @return A <code>VTimeZone</code> initialized by the time zone ID, or null
     * when the ID is unknown.
     */
    public static VTimeZone create(String tzid) {
        BasicTimeZone basicTimeZone = TimeZone.getFrozenICUTimeZone(tzid, true);
        if (basicTimeZone == null) {
            return null;
        }
        VTimeZone vtz = new VTimeZone(tzid);
        vtz.tz = (BasicTimeZone) basicTimeZone.cloneAsThawed();
        vtz.olsonzid = vtz.tz.getID();

        return vtz;
    }
    
    /**
     * Create a <code>VTimeZone</code> instance by RFC2445 VTIMEZONE data.
     * 
     * @param reader The Reader for VTIMEZONE data input stream
     * @return A <code>VTimeZone</code> initialized by the VTIMEZONE data or
     * null if failed to load the rule from the VTIMEZONE data.
     */
    public static VTimeZone create(Reader reader) {
        VTimeZone vtz = new VTimeZone();
        if (vtz.load(reader)) {
            return vtz;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getOffset(int era, int year, int month, int day, int dayOfWeek,
            int milliseconds) {
        return tz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getOffset(long date, boolean local, int[] offsets) {
        tz.getOffset(date, local, offsets);
    }

    /**
     * {@inheritDoc}
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    @Override
    public void getOffsetFromLocal(long date,
            int nonExistingTimeOpt, int duplicatedTimeOpt, int[] offsets) {
        tz.getOffsetFromLocal(date, nonExistingTimeOpt, duplicatedTimeOpt, offsets);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRawOffset() {
        return tz.getRawOffset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean inDaylightTime(Date date) {
        return tz.inDaylightTime(date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRawOffset(int offsetMillis) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
        }
        tz.setRawOffset(offsetMillis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean useDaylightTime() {
        return tz.useDaylightTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean observesDaylightTime() {
        return tz.observesDaylightTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasSameRules(TimeZone other) {
        if (this == other) {
            return true;
        }
        if (other instanceof VTimeZone) {
            return tz.hasSameRules(((VTimeZone)other).tz);
        }
        return tz.hasSameRules(other);
    }

    /**
     * Gets the RFC2445 TZURL property value.  When a <code>VTimeZone</code> instance was created from
     * VTIMEZONE data, the value is set by the TZURL property value in the data.  Otherwise,
     * the initial value is null.
     * 
     * @return The RFC2445 TZURL property value
     */
    public String getTZURL() {
        return tzurl;
    }

    /**
     * Sets the RFC2445 TZURL property value.
     * 
     * @param url The TZURL property value.
     */
    public void setTZURL(String url) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
        }
        tzurl = url;
    }

    /**
     * Gets the RFC2445 LAST-MODIFIED property value.  When a <code>VTimeZone</code> instance was created
     * from VTIMEZONE data, the value is set by the LAST-MODIFIED property value in the data.
     * Otherwise, the initial value is null.
     * 
     * @return The Date represents the RFC2445 LAST-MODIFIED date.
     */
    public Date getLastModified() {
        return lastmod;
    }

    /**
     * Sets the date used for RFC2445 LAST-MODIFIED property value.
     * 
     * @param date The <code>Date</code> object represents the date for RFC2445 LAST-MODIFIED property value.
     */
    public void setLastModified(Date date) {
        if (isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify a frozen VTimeZone instance.");
        }
        lastmod = date;
    }

    /**
     * Writes RFC2445 VTIMEZONE data for this time zone
     * 
     * @param writer A <code>Writer</code> used for the output
     * @throws IOException If there were problems creating a buffered writer or writing to it.
     */
    public void write(Writer writer) throws IOException {
        BufferedWriter bw = new BufferedWriter(writer);
        if (vtzlines != null) {
            for (String line : vtzlines) {
                if (line.startsWith(ICAL_TZURL + COLON)) {
                    if (tzurl != null) {
                        bw.write(ICAL_TZURL);
                        bw.write(COLON);
                        bw.write(tzurl);
                        bw.write(NEWLINE);
                    }
                } else if (line.startsWith(ICAL_LASTMOD + COLON)) {
                    if (lastmod != null) {
                        bw.write(ICAL_LASTMOD);
                        bw.write(COLON);
                        bw.write(getUTCDateTimeString(lastmod.getTime()));
                        bw.write(NEWLINE);
                    }
                } else {
                    bw.write(line);
                    bw.write(NEWLINE);
                }
            }
            bw.flush();
        } else {
            String[] customProperties = null;
            if (olsonzid != null && ICU_TZVERSION != null) {
                customProperties = new String[1];
                customProperties[0] = ICU_TZINFO_PROP + COLON + olsonzid + "[" + ICU_TZVERSION + "]";
            }
            writeZone(writer, tz, customProperties);
        }
    }

    /**
     * Writes RFC2445 VTIMEZONE data applicable for dates after
     * the specified start time.
     * 
     * @param writer    The <code>Writer</code> used for the output
     * @param start     The start time
     * 
     * @throws IOException If there were problems reading and writing to the writer.
     */
    public void write(Writer writer, long start) throws IOException {
        // Extract rules applicable to dates after the start time
        TimeZoneRule[] rules = tz.getTimeZoneRules(start);

        // Create a RuleBasedTimeZone with the subset rule
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(tz.getID(), (InitialTimeZoneRule)rules[0]);
        for (int i = 1; i < rules.length; i++) {
            rbtz.addTransitionRule(rules[i]);
        }
        String[] customProperties = null;
        if (olsonzid != null && ICU_TZVERSION != null) {
            customProperties = new String[1];
            customProperties[0] = ICU_TZINFO_PROP + COLON + olsonzid + "[" + ICU_TZVERSION + 
                "/Partial@" + start + "]";
        }
        writeZone(writer, rbtz, customProperties);
    }

    /**
     * Writes RFC2445 VTIMEZONE data applicable near the specified date.
     * Some common iCalendar implementations can only handle a single time
     * zone property or a pair of standard and daylight time properties using
     * BYDAY rule with day of week (such as BYDAY=1SUN).  This method produce
     * the VTIMEZONE data which can be handled these implementations.  The rules
     * produced by this method can be used only for calculating time zone offset
     * around the specified date.
     * 
     * @param writer    The <code>Writer</code> used for the output
     * @param time      The date
     * 
     * @throws IOException If there were problems reading or writing to the writer.
     */
    public void writeSimple(Writer writer, long time) throws IOException {
        // Extract simple rules
        TimeZoneRule[] rules = tz.getSimpleTimeZoneRulesNear(time);

        // Create a RuleBasedTimeZone with the subset rule
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(tz.getID(), (InitialTimeZoneRule)rules[0]);
        for (int i = 1; i < rules.length; i++) {
            rbtz.addTransitionRule(rules[i]);
        }
        String[] customProperties = null;
        if (olsonzid != null && ICU_TZVERSION != null) {
            customProperties = new String[1];
            customProperties[0] = ICU_TZINFO_PROP + COLON + olsonzid + "[" + ICU_TZVERSION + 
                "/Simple@" + time + "]";
        }
        writeZone(writer, rbtz, customProperties);
    }

    // BasicTimeZone methods

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZoneTransition getNextTransition(long base, boolean inclusive) {
        return tz.getNextTransition(base, inclusive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZoneTransition getPreviousTransition(long base, boolean inclusive) {
        return tz.getPreviousTransition(base, inclusive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasEquivalentTransitions(TimeZone other, long start, long end) {
        if (this == other) {
            return true;
        }
        return tz.hasEquivalentTransitions(other, start, end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZoneRule[] getTimeZoneRules() {
        return tz.getTimeZoneRules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZoneRule[] getTimeZoneRules(long start) {
        return tz.getTimeZoneRules(start);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() {
        if (isFrozen()) {
            return this;
        }
        return cloneAsThawed();
    }

    // private stuff ------------------------------------------------------

    private BasicTimeZone tz;
    private List<String> vtzlines;
    private String olsonzid = null;
    private String tzurl = null;
    private Date lastmod = null;

    private static String ICU_TZVERSION;
    private static final String ICU_TZINFO_PROP = "X-TZINFO";

    // Default DST savings
    private static final int DEF_DSTSAVINGS = 60*60*1000; // 1 hour
    
    // Default time start
    private static final long DEF_TZSTARTTIME = 0;

    // minimum/max
    private static final long MIN_TIME = Long.MIN_VALUE;
    private static final long MAX_TIME = Long.MAX_VALUE;

    // Symbol characters used by RFC2445 VTIMEZONE
    private static final String COLON = ":";
    private static final String SEMICOLON = ";";
    private static final String EQUALS_SIGN = "=";
    private static final String COMMA = ",";
    private static final String NEWLINE = "\r\n";   // CRLF

    // RFC2445 VTIMEZONE tokens
    private static final String ICAL_BEGIN_VTIMEZONE = "BEGIN:VTIMEZONE";
    private static final String ICAL_END_VTIMEZONE = "END:VTIMEZONE";
    private static final String ICAL_BEGIN = "BEGIN";
    private static final String ICAL_END = "END";
    private static final String ICAL_VTIMEZONE = "VTIMEZONE";
    private static final String ICAL_TZID = "TZID";
    private static final String ICAL_STANDARD = "STANDARD";
    private static final String ICAL_DAYLIGHT = "DAYLIGHT";
    private static final String ICAL_DTSTART = "DTSTART";
    private static final String ICAL_TZOFFSETFROM = "TZOFFSETFROM";
    private static final String ICAL_TZOFFSETTO = "TZOFFSETTO";
    private static final String ICAL_RDATE = "RDATE";
    private static final String ICAL_RRULE = "RRULE";
    private static final String ICAL_TZNAME = "TZNAME";
    private static final String ICAL_TZURL = "TZURL";
    private static final String ICAL_LASTMOD = "LAST-MODIFIED";

    private static final String ICAL_FREQ = "FREQ";
    private static final String ICAL_UNTIL = "UNTIL";
    private static final String ICAL_YEARLY = "YEARLY";
    private static final String ICAL_BYMONTH = "BYMONTH";
    private static final String ICAL_BYDAY = "BYDAY";
    private static final String ICAL_BYMONTHDAY = "BYMONTHDAY";

    private static final String[] ICAL_DOW_NAMES = 
    {"SU", "MO", "TU", "WE", "TH", "FR", "SA"};

    // Month length in regular year
    private static final int[] MONTHLENGTH = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    static {
        // Initialize ICU_TZVERSION
        try {
            ICU_TZVERSION = TimeZone.getTZDataVersion();
        } catch (MissingResourceException e) {
            ///CLOVER:OFF
            ICU_TZVERSION = null;
            ///CLOVER:ON
        }
    }
    
    /* Hide the constructor */
    private VTimeZone() {
    }

    private VTimeZone(String tzid) {
        super(tzid);
    }

    /*
     * Read the input stream to locate the VTIMEZONE block and
     * parse the contents to initialize this VTimeZone object.
     * The reader skips other RFC2445 message headers.  After
     * the parse is completed, the reader points at the beginning
     * of the header field just after the end of VTIMEZONE block.
     * When VTIMEZONE block is found and this object is successfully
     * initialized by the rules described in the data, this method
     * returns true.  Otherwise, returns false.
     */
    private boolean load(Reader reader) {
        // Read VTIMEZONE block into string array
        try {
            vtzlines = new LinkedList<String>();
            boolean eol = false;
            boolean start = false;
            boolean success = false;
            StringBuilder line = new StringBuilder();
            while (true) {
                int ch = reader.read();
                if (ch == -1) {
                    // end of file
                    if (start && line.toString().startsWith(ICAL_END_VTIMEZONE)) {
                        vtzlines.add(line.toString());
                        success = true;
                    }
                    break;
                }
                if (ch == 0x0D) {
                    // CR, must be followed by LF by the definition in RFC2445
                    continue;
                }

                if (eol) {
                    if (ch != 0x09 && ch != 0x20) {
                        // NOT followed by TAB/SP -> new line
                        if (start) {
                            if (line.length() > 0) {
                                vtzlines.add(line.toString());
                            }
                        }
                        line.setLength(0);
                        if (ch != 0x0A) {
                            line.append((char)ch);
                        }
                    }
                    eol = false;
                } else {
                    if (ch == 0x0A) {
                        // LF
                        eol = true;
                        if (start) {
                            if (line.toString().startsWith(ICAL_END_VTIMEZONE)) {
                                vtzlines.add(line.toString());
                                success = true;
                                break;
                            }
                        } else {
                            if (line.toString().startsWith(ICAL_BEGIN_VTIMEZONE)) {
                                vtzlines.add(line.toString());
                                line.setLength(0);
                                start = true;
                                eol = false;
                            }
                        }
                    } else {
                        line.append((char)ch);
                    }
                }
            }
            if (!success) {
                return false;
            }
        } catch (IOException ioe) {
            ///CLOVER:OFF
            return false;
            ///CLOVER:ON
        }
        return parse();
    }

    // parser state
    private static final int INI = 0;   // Initial state
    private static final int VTZ = 1;   // In VTIMEZONE
    private static final int TZI = 2;   // In STANDARD or DAYLIGHT
    private static final int ERR = 3;   // Error state

    /*
     * Parse VTIMEZONE data and create a RuleBasedTimeZone
     */
    private boolean parse() {
        ///CLOVER:OFF
        if (vtzlines == null || vtzlines.size() == 0) {
            return false;
        }
        ///CLOVER:ON

        // timezone ID
        String tzid = null;

        int state = INI;
        boolean dst = false;    // current zone type
        String from = null;     // current zone from offset
        String to = null;       // current zone offset
        String tzname = null;   // current zone name
        String dtstart = null;  // current zone starts
        boolean isRRULE = false;    // true if the rule is described by RRULE
        List<String> dates = null;  // list of RDATE or RRULE strings
        List<TimeZoneRule> rules = new ArrayList<TimeZoneRule>();   // rule list
        int initialRawOffset = 0;  // initial offset
        int initialDSTSavings = 0;  // initial offset
        long firstStart = MAX_TIME; // the earliest rule start time

        for (String line : vtzlines) {
            int valueSep = line.indexOf(COLON);
            if (valueSep < 0) {
                continue;
            }
            String name = line.substring(0, valueSep);
            String value = line.substring(valueSep + 1);

            switch (state) {
            case INI:
                if (name.equals(ICAL_BEGIN) && value.equals(ICAL_VTIMEZONE)) {
                    state = VTZ;
                }
                break;
            case VTZ:
                if (name.equals(ICAL_TZID)) {
                    tzid = value;
                } else if (name.equals(ICAL_TZURL)) {
                    tzurl = value;
                } else if (name.equals(ICAL_LASTMOD)) {
                    // Always in 'Z' format, so the offset argument for the parse method
                    // can be any value.
                    lastmod = new Date(parseDateTimeString(value, 0));
                } else if (name.equals(ICAL_BEGIN)) {
                    boolean isDST = value.equals(ICAL_DAYLIGHT);
                    if (value.equals(ICAL_STANDARD) || isDST) {
                        // tzid must be ready at this point
                        if (tzid == null) {
                            state = ERR;
                            break;
                        }
                        // initialize current zone properties
                        dates = null;
                        isRRULE = false;
                        from = null;
                        to = null;
                        tzname = null;
                        dst = isDST;
                        state = TZI;
                    } else {
                        // BEGIN property other than STANDARD/DAYLIGHT
                        // must not be there.
                        state = ERR;
                        break;
                    }
                } else if (name.equals(ICAL_END) /* && value.equals(ICAL_VTIMEZONE) */) {
                    break;
                }
                break;

            case TZI:
                if (name.equals(ICAL_DTSTART)) {
                    dtstart = value;
                } else if (name.equals(ICAL_TZNAME)) {
                    tzname = value;
                } else if (name.equals(ICAL_TZOFFSETFROM)) {
                    from = value;
                } else if (name.equals(ICAL_TZOFFSETTO)) {
                    to = value;
                } else if (name.equals(ICAL_RDATE)) {
                    // RDATE mixed with RRULE is not supported
                    if (isRRULE) {
                        state = ERR;
                        break;
                    }
                    if (dates == null) {
                        dates = new LinkedList<String>();
                    }
                    // RDATE value may contain multiple date delimited
                    // by comma
                    StringTokenizer st = new StringTokenizer(value, COMMA);
                    while (st.hasMoreTokens()) {
                        String date = st.nextToken();
                        dates.add(date);
                    }
                } else if (name.equals(ICAL_RRULE)) {
                    // RRULE mixed with RDATE is not supported
                    if (!isRRULE && dates != null) {
                        state = ERR;
                        break;
                    } else if (dates == null) {
                        dates = new LinkedList<String>();
                    }
                    isRRULE = true;
                    dates.add(value);
                } else if (name.equals(ICAL_END)) {
                    // Mandatory properties
                    if (dtstart == null || from == null || to == null) {
                        state = ERR;
                        break;
                    }
                    // if tzname is not available, create one from tzid
                    if (tzname == null) {
                        tzname = getDefaultTZName(tzid, dst);
                    }

                    // create a time zone rule
                    TimeZoneRule rule = null;
                    int fromOffset = 0;
                    int toOffset = 0;
                    int rawOffset = 0;
                    int dstSavings = 0;
                    long start = 0;
                    try {
                        // Parse TZOFFSETFROM/TZOFFSETTO
                        fromOffset = offsetStrToMillis(from);
                        toOffset = offsetStrToMillis(to);

                        if (dst) {
                            // If daylight, use the previous offset as rawoffset if positive
                            if (toOffset - fromOffset > 0) {
                                rawOffset = fromOffset;
                                dstSavings = toOffset - fromOffset;
                            } else {
                                // This is rare case..  just use 1 hour DST savings
                                rawOffset = toOffset - DEF_DSTSAVINGS;
                                dstSavings = DEF_DSTSAVINGS;                                
                            }
                        } else {
                            rawOffset = toOffset;
                            dstSavings = 0;
                        }

                        // start time
                        start = parseDateTimeString(dtstart, fromOffset);

                        // Create the rule
                        Date actualStart = null;
                        if (isRRULE) {
                            rule = createRuleByRRULE(tzname, rawOffset, dstSavings, start, dates, fromOffset);
                        } else {
                            rule = createRuleByRDATE(tzname, rawOffset, dstSavings, start, dates, fromOffset);
                        }
                        if (rule != null) {
                            actualStart = rule.getFirstStart(fromOffset, 0);
                            if (actualStart.getTime() < firstStart) {
                                // save from offset information for the earliest rule
                                firstStart = actualStart.getTime();
                                // If this is STD, assume the time before this transtion
                                // is DST when the difference is 1 hour.  This might not be
                                // accurate, but VTIMEZONE data does not have such info.
                                if (dstSavings > 0) {
                                    initialRawOffset = fromOffset;
                                    initialDSTSavings = 0;
                                } else {
                                    if (fromOffset - toOffset == DEF_DSTSAVINGS) {
                                        initialRawOffset = fromOffset - DEF_DSTSAVINGS;
                                        initialDSTSavings = DEF_DSTSAVINGS;
                                    } else {
                                        initialRawOffset = fromOffset;
                                        initialDSTSavings = 0;
                                    }
                                }
                            }
                        }
                    } catch (IllegalArgumentException iae) {
                        // bad format - rule == null..
                    }

                    if (rule == null) {
                        state = ERR;
                        break;
                    }
                    rules.add(rule);
                    state = VTZ;
                }
                break;
            }

            if (state == ERR) {
                vtzlines = null;
                return false;
            }
        }

        // Must have at least one rule
        if (rules.size() == 0) {
            return false;
        }

        // Create a initial rule
        InitialTimeZoneRule initialRule = new InitialTimeZoneRule(getDefaultTZName(tzid, false),
                initialRawOffset, initialDSTSavings);

        // Finally, create the RuleBasedTimeZone
        RuleBasedTimeZone rbtz = new RuleBasedTimeZone(tzid, initialRule);

        int finalRuleIdx = -1;
        int finalRuleCount = 0;
        for (int i = 0; i < rules.size(); i++) {
            TimeZoneRule r = rules.get(i);
            if (r instanceof AnnualTimeZoneRule) {
                if (((AnnualTimeZoneRule)r).getEndYear() == AnnualTimeZoneRule.MAX_YEAR) {
                    finalRuleCount++;
                    finalRuleIdx = i;
                }
            }
        }
        if (finalRuleCount > 2) {
            // Too many final rules
            return false;
        }

        if (finalRuleCount == 1) {
            if (rules.size() == 1) {
                // Only one final rule, only governs the initial rule,
                // which is already initialized, thus, we do not need to
                // add this transition rule
                rules.clear();
            } else {
                // Normalize the final rule
                AnnualTimeZoneRule finalRule = (AnnualTimeZoneRule)rules.get(finalRuleIdx);
                int tmpRaw = finalRule.getRawOffset();
                int tmpDST = finalRule.getDSTSavings();
    
                // Find the last non-final rule
                Date finalStart = finalRule.getFirstStart(initialRawOffset, initialDSTSavings);
                Date start = finalStart;
                for (int i = 0; i < rules.size(); i++) {
                    if (finalRuleIdx == i) {
                        continue;
                    }
                    TimeZoneRule r = rules.get(i);
                    Date lastStart = r.getFinalStart(tmpRaw, tmpDST);
                    if (lastStart.after(start)) {
                        start = finalRule.getNextStart(lastStart.getTime(),
                                r.getRawOffset(),
                                r.getDSTSavings(),
                                false);
                    }
                }
                TimeZoneRule newRule;
                if (start == finalStart) {
                    // Transform this into a single transition
                    newRule = new TimeArrayTimeZoneRule(
                            finalRule.getName(),
                            finalRule.getRawOffset(),
                            finalRule.getDSTSavings(),
                            new long[] {finalStart.getTime()},
                            DateTimeRule.UTC_TIME);
                } else {
                    // Update the end year
                    int fields[] = Grego.timeToFields(start.getTime(), null);
                    newRule = new AnnualTimeZoneRule(
                            finalRule.getName(),
                            finalRule.getRawOffset(),
                            finalRule.getDSTSavings(),
                            finalRule.getRule(),
                            finalRule.getStartYear(),
                            fields[0]);
                }
                rules.set(finalRuleIdx, newRule);
            }
        }

        for (TimeZoneRule r : rules) {
            rbtz.addTransitionRule(r);
        }

        tz = rbtz;
        setID(tzid);
        return true;
    }

    /*
     * Create a default TZNAME from TZID
     */
    private static String getDefaultTZName(String tzid, boolean isDST) {
        if (isDST) {
            return tzid + "(DST)";
        }
        return tzid + "(STD)";
    }

    /*
     * Create a TimeZoneRule by the RRULE definition
     */
    private static TimeZoneRule createRuleByRRULE(String tzname,
            int rawOffset, int dstSavings, long start, List<String> dates, int fromOffset) {
        if (dates == null || dates.size() == 0) {
            return null;
        }
        // Parse the first rule
        String rrule = dates.get(0);

        long until[] = new long[1];
        int[] ruleFields = parseRRULE(rrule, until);
        if (ruleFields == null) {
            // Invalid RRULE
            return null;
        }

        int month = ruleFields[0];
        int dayOfWeek = ruleFields[1];
        int nthDayOfWeek = ruleFields[2];
        int dayOfMonth = ruleFields[3];

        if (dates.size() == 1) {
            // No more rules
            if (ruleFields.length > 4) {
                // Multiple BYMONTHDAY values

                if (ruleFields.length != 10 || month == -1 || dayOfWeek == 0) {
                    // Only support the rule using 7 continuous days
                    // BYMONTH and BYDAY must be set at the same time
                    return null;
                }
                int firstDay = 31; // max possible number of dates in a month
                int days[] = new int[7];
                for (int i = 0; i < 7; i++) {
                    days[i] = ruleFields[3 + i];
                    // Resolve negative day numbers.  A negative day number should
                    // not be used in February, but if we see such case, we use 28
                    // as the base.
                    days[i] = days[i] > 0 ? days[i] : MONTHLENGTH[month] + days[i] + 1;
                    firstDay = days[i] < firstDay ? days[i] : firstDay;
                }
                // Make sure days are continuous
                for (int i = 1; i < 7; i++) {
                    boolean found = false;
                    for (int j = 0; j < 7; j++) {
                        if (days[j] == firstDay + i) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // days are not continuous
                        return null;
                    }
                }
                // Use DOW_GEQ_DOM rule with firstDay as the start date
                dayOfMonth = firstDay;
            }
        } else {
            // Check if BYMONTH + BYMONTHDAY + BYDAY rule with multiple RRULE lines.
            // Otherwise, not supported.
            if (month == -1 || dayOfWeek == 0 || dayOfMonth == 0) {
                // This is not the case
                return null;
            }
            // Parse the rest of rules if number of rules is not exceeding 7.
            // We can only support 7 continuous days starting from a day of month.
            if (dates.size() > 7) {
                return null;
            }

            // Note: To check valid date range across multiple rule is a little
            // bit complicated.  For now, this code is not doing strict range
            // checking across month boundary

            int earliestMonth = month;
            int daysCount = ruleFields.length - 3;
            int earliestDay = 31;
            for (int i = 0; i < daysCount; i++) {
                int dom = ruleFields[3 + i];
                dom = dom > 0 ? dom : MONTHLENGTH[month] + dom + 1;
                earliestDay = dom < earliestDay ? dom : earliestDay;
            }

            int anotherMonth = -1;
            for (int i = 1; i < dates.size(); i++) {
                rrule = dates.get(i);
                long[] unt = new long[1];
                int[] fields = parseRRULE(rrule, unt);

                // If UNTIL is newer than previous one, use the one
                if (unt[0] > until[0]) {
                    until = unt;
                }
                
                // Check if BYMONTH + BYMONTHDAY + BYDAY rule
                if (fields[0] == -1 || fields[1] == 0 || fields[3] == 0) {
                    return null;
                }
                // Count number of BYMONTHDAY
                int count = fields.length - 3;
                if (daysCount + count > 7) {
                    // We cannot support BYMONTHDAY more than 7
                    return null;
                }
                // Check if the same BYDAY is used.  Otherwise, we cannot
                // support the rule
                if (fields[1] != dayOfWeek) {
                    return null;
                }
                // Check if the month is same or right next to the primary month
                if (fields[0] != month) {
                    if (anotherMonth == -1) {
                        int diff = fields[0] - month;
                        if (diff == -11 || diff == -1) {
                            // Previous month
                            anotherMonth = fields[0];
                            earliestMonth = anotherMonth;
                            // Reset earliest day
                            earliestDay = 31;
                        } else if (diff == 11 || diff == 1) {
                            // Next month
                            anotherMonth = fields[0];
                        } else {
                            // The day range cannot exceed more than 2 months
                            return null;
                        }
                    } else if (fields[0] != month && fields[0] != anotherMonth) {
                        // The day range cannot exceed more than 2 months
                        return null;
                    }
                }
                // If ealier month, go through days to find the earliest day
                if (fields[0] == earliestMonth) {
                    for (int j = 0; j < count; j++) {
                        int dom = fields[3 + j];
                        dom = dom > 0 ? dom : MONTHLENGTH[fields[0]] + dom + 1;
                        earliestDay = dom < earliestDay ? dom : earliestDay;
                    }
                }
                daysCount += count;
            }
            if (daysCount != 7) {
                // Number of BYMONTHDAY entries must be 7
                return null;
            }
            month = earliestMonth;
            dayOfMonth = earliestDay;
        }

        // Calculate start/end year and missing fields
        int[] dfields = Grego.timeToFields(start + fromOffset, null);
        int startYear = dfields[0];
        if (month == -1) {
            // If MYMONTH is not set, use the month of DTSTART
            month = dfields[1];
        }
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth == 0) {
            // If only YEARLY is set, use the day of DTSTART as BYMONTHDAY
            dayOfMonth = dfields[2];
        }
        int timeInDay = dfields[5];

        int endYear = AnnualTimeZoneRule.MAX_YEAR;
        if (until[0] != MIN_TIME) {
            Grego.timeToFields(until[0], dfields);
            endYear = dfields[0];
        }

        // Create the AnnualDateTimeRule
        DateTimeRule adtr = null;
        if (dayOfWeek == 0 && nthDayOfWeek == 0 && dayOfMonth != 0) {
            // Day in month rule, for example, 15th day in the month
            adtr = new DateTimeRule(month, dayOfMonth, timeInDay, DateTimeRule.WALL_TIME);
        } else if (dayOfWeek != 0 && nthDayOfWeek != 0 && dayOfMonth == 0) {
            // Nth day of week rule, for example, last Sunday
            adtr = new DateTimeRule(month, nthDayOfWeek, dayOfWeek, timeInDay, DateTimeRule.WALL_TIME);
        } else if (dayOfWeek != 0 && nthDayOfWeek == 0 && dayOfMonth != 0) {
            // First day of week after day of month rule, for example,
            // first Sunday after 15th day in the month
            adtr = new DateTimeRule(month, dayOfMonth, dayOfWeek, true, timeInDay, DateTimeRule.WALL_TIME);
        } else {
            // RRULE attributes are insufficient
            return null;
        }

        return new AnnualTimeZoneRule(tzname, rawOffset, dstSavings, adtr, startYear, endYear);
    }

    /*
     * Parse individual RRULE
     * 
     * On return -
     * 
     * int[0] month calculated by BYMONTH - 1, or -1 when not found
     * int[1] day of week in BYDAY, or 0 when not found
     * int[2] day of week ordinal number in BYDAY, or 0 when not found
     * int[i >= 3] day of month, which could be multiple values, or 0 when not found
     * 
     *  or
     * 
     * null on any error cases, for exmaple, FREQ=YEARLY is not available
     * 
     * When UNTIL attribute is available, the time will be set to until[0],
     * otherwise, MIN_TIME
     */
    private static int[] parseRRULE(String rrule, long[] until) {
        int month = -1;
        int dayOfWeek = 0;
        int nthDayOfWeek = 0;
        int[] dayOfMonth = null;

        long untilTime = MIN_TIME;
        boolean yearly = false;
        boolean parseError = false;
        StringTokenizer st= new StringTokenizer(rrule, SEMICOLON);

        while (st.hasMoreTokens()) {
            String attr, value;
            String prop = st.nextToken();
            int sep = prop.indexOf(EQUALS_SIGN);
            if (sep != -1) {
                attr = prop.substring(0, sep);
                value = prop.substring(sep + 1);
            } else {
                parseError = true;
                break;
            }

            if (attr.equals(ICAL_FREQ)) {
                // only support YEARLY frequency type
                if (value.equals(ICAL_YEARLY)) {
                    yearly = true;
                } else {
                    parseError = true;
                    break;                        
                }
            } else if (attr.equals(ICAL_UNTIL)) {
                // ISO8601 UTC format, for example, "20060315T020000Z"
                try {
                    untilTime = parseDateTimeString(value, 0);
                } catch (IllegalArgumentException iae) {
                    parseError = true;
                    break;
                }
            } else if (attr.equals(ICAL_BYMONTH)) {
                // Note: BYMONTH may contain multiple months, but only single month make sense for
                // VTIMEZONE property.
                if (value.length() > 2) {
                    parseError = true;
                    break;
                }
                try {
                    month = Integer.parseInt(value) - 1;
                    if (month < 0 || month >= 12) {
                        parseError = true;
                        break;
                    }
                } catch (NumberFormatException nfe) {
                    parseError = true;
                    break;
                }
            } else if (attr.equals(ICAL_BYDAY)) {
                // Note: BYDAY may contain multiple day of week separated by comma.  It is unlikely used for
                // VTIMEZONE property.  We do not support the case.

                // 2-letter format is used just for representing a day of week, for example, "SU" for Sunday
                // 3 or 4-letter format is used for represeinging Nth day of week, for example, "-1SA" for last Saturday
                int length = value.length();
                if (length < 2 || length > 4) {
                    parseError = true;
                    break;
                }
                if (length > 2) {
                    // Nth day of week
                    int sign = 1;
                    if (value.charAt(0) == '+') {
                        sign = 1;
                    } else if (value.charAt(0) == '-') {
                        sign = -1;
                    } else if (length == 4) {
                        parseError = true;
                        break;
                    }
                    try {
                        int n = Integer.parseInt(value.substring(length - 3, length - 2));
                        if (n == 0 || n > 4) {
                            parseError = true;
                            break;
                        }
                        nthDayOfWeek = n * sign;
                    } catch(NumberFormatException nfe) {
                        parseError = true;
                        break;
                    }
                    value = value.substring(length - 2);
                }
                int wday;
                for (wday = 0; wday < ICAL_DOW_NAMES.length; wday++) {
                    if (value.equals(ICAL_DOW_NAMES[wday])) {
                        break;
                    }
                }
                if (wday < ICAL_DOW_NAMES.length) {
                    // Sunday(1) - Saturday(7)
                    dayOfWeek = wday + 1;
                } else {
                    parseError = true;
                    break;
                }
            } else if (attr.equals(ICAL_BYMONTHDAY)) {
                // Note: BYMONTHDAY may contain multiple days delimited by comma
                //
                // A value of BYMONTHDAY could be negative, for example, -1 means
                // the last day in a month
                StringTokenizer days = new StringTokenizer(value, COMMA);
                int count = days.countTokens();
                dayOfMonth = new int[count];
                int index = 0;
                while(days.hasMoreTokens()) {
                    try {
                        dayOfMonth[index++] = Integer.parseInt(days.nextToken());
                    } catch (NumberFormatException nfe) {
                        parseError = true;
                        break;
                    }
                }
            }
        }

        if (parseError) {
            return null;
        }
        if (!yearly) {
            // FREQ=YEARLY must be set
            return null;
        }

        until[0] = untilTime;

        int[] results;
        if (dayOfMonth == null) {
            results = new int[4];
            results[3] = 0;
        } else {
            results = new int[3 + dayOfMonth.length];
            for (int i = 0; i < dayOfMonth.length; i++) {
                results[3 + i] = dayOfMonth[i];
            }
        }
        results[0] = month;
        results[1] = dayOfWeek;
        results[2] = nthDayOfWeek;
        return results;
    }
    
    /*
     * Create a TimeZoneRule by the RDATE definition
     */
    private static TimeZoneRule createRuleByRDATE(String tzname,
            int rawOffset, int dstSavings, long start, List<String> dates, int fromOffset) {
        // Create an array of transition times
        long[] times;
        if (dates == null || dates.size() == 0) {
            // When no RDATE line is provided, use start (DTSTART)
            // as the transition time
            times = new long[1];
            times[0] = start;
        } else {
            times = new long[dates.size()];
            int idx = 0;
            try {
                for (String date : dates) {
                    times[idx++] = parseDateTimeString(date, fromOffset);
                }
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
        return new TimeArrayTimeZoneRule(tzname, rawOffset, dstSavings, times, DateTimeRule.UTC_TIME);
    }

    /*
     * Write the time zone rules in RFC2445 VTIMEZONE format
     */
    private void writeZone(Writer w, BasicTimeZone basictz, String[] customProperties) throws IOException {
        // Write the header
        writeHeader(w);

        if (customProperties != null && customProperties.length > 0) {
            for (int i = 0; i < customProperties.length; i++) {
                if (customProperties[i] != null) {
                    w.write(customProperties[i]);
                    w.write(NEWLINE);
                }
            }
        }

        long t = MIN_TIME;
        String dstName = null;
        int dstFromOffset = 0;
        int dstFromDSTSavings = 0;
        int dstToOffset = 0;
        int dstStartYear = 0;
        int dstMonth = 0;
        int dstDayOfWeek = 0;
        int dstWeekInMonth = 0;
        int dstMillisInDay = 0;
        long dstStartTime = 0;
        long dstUntilTime = 0;
        int dstCount = 0;
        AnnualTimeZoneRule finalDstRule = null;

        String stdName = null;
        int stdFromOffset = 0;
        int stdFromDSTSavings = 0;
        int stdToOffset = 0;
        int stdStartYear = 0;
        int stdMonth = 0;
        int stdDayOfWeek = 0;
        int stdWeekInMonth = 0;
        int stdMillisInDay = 0;
        long stdStartTime = 0;
        long stdUntilTime = 0;
        int stdCount = 0;
        AnnualTimeZoneRule finalStdRule = null;

        int[] dtfields = new int[6];
        boolean hasTransitions = false;

        // Going through all transitions
        while(true) {
            TimeZoneTransition tzt = basictz.getNextTransition(t, false);
            if (tzt == null) {
                break;
            }
            hasTransitions = true;
            t = tzt.getTime();
            String name = tzt.getTo().getName();
            boolean isDst = (tzt.getTo().getDSTSavings() != 0);
            int fromOffset = tzt.getFrom().getRawOffset() + tzt.getFrom().getDSTSavings();
            int fromDSTSavings = tzt.getFrom().getDSTSavings();
            int toOffset = tzt.getTo().getRawOffset() + tzt.getTo().getDSTSavings();
            Grego.timeToFields(tzt.getTime() + fromOffset, dtfields);
            int weekInMonth = Grego.getDayOfWeekInMonth(dtfields[0], dtfields[1], dtfields[2]);
            int year = dtfields[0];
            boolean sameRule = false;
            if (isDst) {
                if (finalDstRule == null && tzt.getTo() instanceof AnnualTimeZoneRule) {
                    if (((AnnualTimeZoneRule)tzt.getTo()).getEndYear() == AnnualTimeZoneRule.MAX_YEAR) {
                        finalDstRule = (AnnualTimeZoneRule)tzt.getTo();
                    }
                }
                if (dstCount > 0) {
                    if (year == dstStartYear + dstCount
                            && name.equals(dstName)
                            && dstFromOffset == fromOffset
                            && dstToOffset == toOffset
                            && dstMonth == dtfields[1]
                            && dstDayOfWeek == dtfields[3]
                            && dstWeekInMonth == weekInMonth
                            && dstMillisInDay == dtfields[5]) {
                        // Update until time
                        dstUntilTime = t;
                        dstCount++;
                        sameRule = true;
                    }
                    if (!sameRule) {
                        if (dstCount == 1) {
                            writeZonePropsByTime(w, true, dstName, dstFromOffset, dstToOffset,
                                    dstStartTime, true);
                        } else {
                            writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset,
                                    dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, dstUntilTime);
                        }
                    }
                } 
                if (!sameRule) {
                    // Reset this DST information
                    dstName = name;
                    dstFromOffset = fromOffset;
                    dstFromDSTSavings = fromDSTSavings;
                    dstToOffset = toOffset;
                    dstStartYear = year;
                    dstMonth = dtfields[1];
                    dstDayOfWeek = dtfields[3];
                    dstWeekInMonth = weekInMonth;
                    dstMillisInDay = dtfields[5];
                    dstStartTime = dstUntilTime = t;
                    dstCount = 1;
                }
                if (finalStdRule != null && finalDstRule != null) {
                    break;
                }
            } else {
                if (finalStdRule == null && tzt.getTo() instanceof AnnualTimeZoneRule) {
                    if (((AnnualTimeZoneRule)tzt.getTo()).getEndYear() == AnnualTimeZoneRule.MAX_YEAR) {
                        finalStdRule = (AnnualTimeZoneRule)tzt.getTo();
                    }
                }
                if (stdCount > 0) {
                    if (year == stdStartYear + stdCount
                            && name.equals(stdName)
                            && stdFromOffset == fromOffset
                            && stdToOffset == toOffset
                            && stdMonth == dtfields[1]
                            && stdDayOfWeek == dtfields[3]
                            && stdWeekInMonth == weekInMonth
                            && stdMillisInDay == dtfields[5]) {
                        // Update until time
                        stdUntilTime = t;
                        stdCount++;
                        sameRule = true;
                    }
                    if (!sameRule) {
                        if (stdCount == 1) {
                            writeZonePropsByTime(w, false, stdName, stdFromOffset, stdToOffset,
                                    stdStartTime, true);
                        } else {
                            writeZonePropsByDOW(w, false, stdName, stdFromOffset, stdToOffset,
                                    stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, stdUntilTime);
                        }
                    }
                }
                if (!sameRule) {
                    // Reset this STD information
                    stdName = name;
                    stdFromOffset = fromOffset;
                    stdFromDSTSavings = fromDSTSavings;
                    stdToOffset = toOffset;
                    stdStartYear = year;
                    stdMonth = dtfields[1];
                    stdDayOfWeek = dtfields[3];
                    stdWeekInMonth = weekInMonth;
                    stdMillisInDay = dtfields[5];
                    stdStartTime = stdUntilTime = t;
                    stdCount = 1;
                }
                if (finalStdRule != null && finalDstRule != null) {
                    break;
                }
            }
        }
        if (!hasTransitions) {
            // No transition - put a single non transition RDATE
            int offset = basictz.getOffset(0 /* any time */);
            boolean isDst = (offset != basictz.getRawOffset());
            writeZonePropsByTime(w, isDst, getDefaultTZName(basictz.getID(), isDst),
                    offset, offset, DEF_TZSTARTTIME - offset, false);                
        } else {
            if (dstCount > 0) {
                if (finalDstRule == null) {
                    if (dstCount == 1) {
                        writeZonePropsByTime(w, true, dstName, dstFromOffset, dstToOffset,
                                dstStartTime, true);
                    } else {
                        writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset,
                                dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, dstUntilTime);
                    }
                } else {
                    if (dstCount == 1) {
                        writeFinalRule(w, true, finalDstRule,
                                dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, dstStartTime);
                    } else {
                        // Use a single rule if possible
                        if (isEquivalentDateRule(dstMonth, dstWeekInMonth, dstDayOfWeek, finalDstRule.getRule())) {
                            writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset,
                                    dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, MAX_TIME);
                        } else {
                            // Not equivalent rule - write out two different rules
                            writeZonePropsByDOW(w, true, dstName, dstFromOffset, dstToOffset,
                                    dstMonth, dstWeekInMonth, dstDayOfWeek, dstStartTime, dstUntilTime);

                            Date nextStart = finalDstRule.getNextStart(dstUntilTime,
                                    dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, false);

                            assert nextStart != null;
                            if (nextStart != null) {
                                writeFinalRule(w, true, finalDstRule,
                                        dstFromOffset - dstFromDSTSavings, dstFromDSTSavings, nextStart.getTime());
                            }
                        }
                    }
                }
            }
            if (stdCount > 0) {
                if (finalStdRule == null) {
                    if (stdCount == 1) {
                        writeZonePropsByTime(w, false, stdName, stdFromOffset, stdToOffset,
                                stdStartTime, true);
                    } else {
                        writeZonePropsByDOW(w, false, stdName, stdFromOffset, stdToOffset,
                                stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, stdUntilTime);
                    }
                } else {
                    if (stdCount == 1) {
                        writeFinalRule(w, false, finalStdRule,
                                stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, stdStartTime);
                    } else {
                        // Use a single rule if possible
                        if (isEquivalentDateRule(stdMonth, stdWeekInMonth, stdDayOfWeek, finalStdRule.getRule())) {
                            writeZonePropsByDOW(w, false, stdName, stdFromOffset, stdToOffset,
                                    stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, MAX_TIME);
                        } else {
                            // Not equivalent rule - write out two different rules
                            writeZonePropsByDOW(w, false, stdName, stdFromOffset, stdToOffset,
                                    stdMonth, stdWeekInMonth, stdDayOfWeek, stdStartTime, stdUntilTime);

                            Date nextStart = finalStdRule.getNextStart(stdUntilTime,
                                    stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, false);

                            assert nextStart != null;
                            if (nextStart != null) {
                                writeFinalRule(w, false, finalStdRule,
                                        stdFromOffset - stdFromDSTSavings, stdFromDSTSavings, nextStart.getTime());
                                
                            }
                        }
                    }
                }
            }            
        }
        writeFooter(w);
    }

    /*
     * Check if the DOW rule specified by month, weekInMonth and dayOfWeek is equivalent
     * to the DateTimerule.
     */
    private static boolean isEquivalentDateRule(int month, int weekInMonth, int dayOfWeek, DateTimeRule dtrule) {
        if (month != dtrule.getRuleMonth() || dayOfWeek != dtrule.getRuleDayOfWeek()) {
            return false;
        }
        if (dtrule.getTimeRuleType() != DateTimeRule.WALL_TIME) {
            // Do not try to do more intelligent comparison for now.
            return false;
        }
        if (dtrule.getDateRuleType() == DateTimeRule.DOW
                && dtrule.getRuleWeekInMonth() == weekInMonth) {
            return true;
        }
        int ruleDOM = dtrule.getRuleDayOfMonth();
        if (dtrule.getDateRuleType() == DateTimeRule.DOW_GEQ_DOM) {
            if (ruleDOM%7 == 1 && (ruleDOM + 6)/7 == weekInMonth) {
                return true;
            }
            if (month != Calendar.FEBRUARY && (MONTHLENGTH[month] - ruleDOM)%7 == 6
                    && weekInMonth == -1*((MONTHLENGTH[month]-ruleDOM+1)/7)) {
                return true;
            }
        }
        if (dtrule.getDateRuleType() == DateTimeRule.DOW_LEQ_DOM) {
            if (ruleDOM%7 == 0 && ruleDOM/7 == weekInMonth) {
                return true;
            }
            if (month != Calendar.FEBRUARY && (MONTHLENGTH[month] - ruleDOM)%7 == 0
                    && weekInMonth == -1*((MONTHLENGTH[month] - ruleDOM)/7 + 1)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Write a single start time
     */
    private static void writeZonePropsByTime(Writer writer, boolean isDst, String tzname,
            int fromOffset, int toOffset, long time, boolean withRDATE) throws IOException {
        beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, time);
        if (withRDATE) {
            writer.write(ICAL_RDATE);
            writer.write(COLON);
            writer.write(getDateTimeString(time + fromOffset));
            writer.write(NEWLINE);
        }
        endZoneProps(writer, isDst);
    }

    /*
     * Write start times defined by a DOM rule using VTIMEZONE RRULE
     */
    private static void writeZonePropsByDOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset,
            int month, int dayOfMonth, long startTime, long untilTime) throws IOException {
        beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, startTime);

        beginRRULE(writer, month);
        writer.write(ICAL_BYMONTHDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(dayOfMonth));

        if (untilTime != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(untilTime + fromOffset));
        }
        writer.write(NEWLINE);

        endZoneProps(writer, isDst);
    }

    /*
     * Write start times defined by a DOW rule using VTIMEZONE RRULE
     */
    private static void writeZonePropsByDOW(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset,
            int month, int weekInMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, startTime);

        beginRRULE(writer, month);
        writer.write(ICAL_BYDAY);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(weekInMonth));    // -4, -3, -2, -1, 1, 2, 3, 4
        writer.write(ICAL_DOW_NAMES[dayOfWeek - 1]);    // SU, MO, TU...

        if (untilTime != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(untilTime + fromOffset));
        }
        writer.write(NEWLINE);

        endZoneProps(writer, isDst);
    }

    /*
     * Write start times defined by a DOW_GEQ_DOM rule using VTIMEZONE RRULE
     */
    private static void writeZonePropsByDOW_GEQ_DOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset,
            int month, int dayOfMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        // Check if this rule can be converted to DOW rule
        if (dayOfMonth%7 == 1) {
            // Can be represented by DOW rule
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset,
                    month, (dayOfMonth + 6)/7, dayOfWeek, startTime, untilTime);
        } else if (month != Calendar.FEBRUARY && (MONTHLENGTH[month] - dayOfMonth)%7 == 6) {
            // Can be represented by DOW rule with negative week number
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset,
                    month, -1*((MONTHLENGTH[month] - dayOfMonth + 1)/7), dayOfWeek, startTime, untilTime);
        } else {
            // Otherwise, use BYMONTHDAY to include all possible dates
            beginZoneProps(writer, isDst, tzname, fromOffset, toOffset, startTime);

            // Check if all days are in the same month
            int startDay = dayOfMonth;
            int currentMonthDays = 7;
        
            if (dayOfMonth <= 0) {
                // The start day is in previous month
                int prevMonthDays = 1 - dayOfMonth;
                currentMonthDays -= prevMonthDays;

                int prevMonth = (month - 1) < 0 ? 11 : month - 1;

                // Note: When a rule is separated into two, UNTIL attribute needs to be
                // calculated for each of them.  For now, we skip this, because we basically use this method
                // only for final rules, which does not have the UNTIL attribute
                writeZonePropsByDOW_GEQ_DOM_sub(writer, prevMonth, -prevMonthDays, dayOfWeek, prevMonthDays, MAX_TIME /* Do not use UNTIL */, fromOffset);

                // Start from 1 for the rest
                startDay = 1;
            } else if (dayOfMonth + 6 > MONTHLENGTH[month]) {
                // Note: This code does not actually work well in February.  For now, days in month in
                // non-leap year.
                int nextMonthDays = dayOfMonth + 6 - MONTHLENGTH[month];
                currentMonthDays -= nextMonthDays;

                int nextMonth = (month + 1) > 11 ? 0 : month + 1;
                
                writeZonePropsByDOW_GEQ_DOM_sub(writer, nextMonth, 1, dayOfWeek, nextMonthDays, MAX_TIME /* Do not use UNTIL */, fromOffset);
            }
            writeZonePropsByDOW_GEQ_DOM_sub(writer, month, startDay, dayOfWeek, currentMonthDays, untilTime, fromOffset);
            endZoneProps(writer, isDst);
        }
    }
 
    /*
     * Called from writeZonePropsByDOW_GEQ_DOM
     */
    private static void writeZonePropsByDOW_GEQ_DOM_sub(Writer writer, int month,
            int dayOfMonth, int dayOfWeek, int numDays, long untilTime, int fromOffset) throws IOException {

        int startDayNum = dayOfMonth;
        boolean isFeb = (month == Calendar.FEBRUARY);
        if (dayOfMonth < 0 && !isFeb) {
            // Use positive number if possible
            startDayNum = MONTHLENGTH[month] + dayOfMonth + 1;
        }
        beginRRULE(writer, month);
        writer.write(ICAL_BYDAY);
        writer.write(EQUALS_SIGN);
        writer.write(ICAL_DOW_NAMES[dayOfWeek - 1]);    // SU, MO, TU...
        writer.write(SEMICOLON);
        writer.write(ICAL_BYMONTHDAY);
        writer.write(EQUALS_SIGN);

        writer.write(Integer.toString(startDayNum));
        for (int i = 1; i < numDays; i++) {
            writer.write(COMMA);
            writer.write(Integer.toString(startDayNum + i));
        }

        if (untilTime != MAX_TIME) {
            appendUNTIL(writer, getDateTimeString(untilTime + fromOffset));
        }
        writer.write(NEWLINE);
    }

    /*
     * Write start times defined by a DOW_LEQ_DOM rule using VTIMEZONE RRULE
     */
    private static void writeZonePropsByDOW_LEQ_DOM(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset,
            int month, int dayOfMonth, int dayOfWeek, long startTime, long untilTime) throws IOException {
        // Check if this rule can be converted to DOW rule
        if (dayOfMonth%7 == 0) {
            // Can be represented by DOW rule
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset,
                    month, dayOfMonth/7, dayOfWeek, startTime, untilTime);
        } else if (month != Calendar.FEBRUARY && (MONTHLENGTH[month] - dayOfMonth)%7 == 0){
            // Can be represented by DOW rule with negative week number
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset,
                    month, -1*((MONTHLENGTH[month] - dayOfMonth)/7 + 1), dayOfWeek, startTime, untilTime);
        } else if (month == Calendar.FEBRUARY && dayOfMonth == 29) {
            // Specical case for February
            writeZonePropsByDOW(writer, isDst, tzname, fromOffset, toOffset,
                    Calendar.FEBRUARY, -1, dayOfWeek, startTime, untilTime);
        } else {
            // Otherwise, convert this to DOW_GEQ_DOM rule
            writeZonePropsByDOW_GEQ_DOM(writer, isDst, tzname, fromOffset, toOffset,
                    month, dayOfMonth - 6, dayOfWeek, startTime, untilTime);
        }
    }

    /*
     * Write the final time zone rule using RRULE, with no UNTIL attribute
     */
    private static void writeFinalRule(Writer writer, boolean isDst, AnnualTimeZoneRule rule,
            int fromRawOffset, int fromDSTSavings, long startTime) throws IOException{
        DateTimeRule dtrule = toWallTimeRule(rule.getRule(), fromRawOffset, fromDSTSavings);

        // If the rule's mills in a day is out of range, adjust start time.
        // Olson tzdata supports 24:00 of a day, but VTIMEZONE does not.
        // See ticket#7008/#7518

        int timeInDay = dtrule.getRuleMillisInDay();
        if (timeInDay < 0) {
            startTime = startTime + (0 - timeInDay);
        } else if (timeInDay >= Grego.MILLIS_PER_DAY) {
            startTime = startTime - (timeInDay - (Grego.MILLIS_PER_DAY - 1));
        }

        int toOffset = rule.getRawOffset() + rule.getDSTSavings();
        switch (dtrule.getDateRuleType()) {
        case DateTimeRule.DOM:
            writeZonePropsByDOM(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset,
                    dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), startTime, MAX_TIME);
            break;
        case DateTimeRule.DOW:
            writeZonePropsByDOW(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset,
                    dtrule.getRuleMonth(), dtrule.getRuleWeekInMonth(), dtrule.getRuleDayOfWeek(), startTime, MAX_TIME);
            break;
        case DateTimeRule.DOW_GEQ_DOM:
            writeZonePropsByDOW_GEQ_DOM(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset,
                    dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), dtrule.getRuleDayOfWeek(), startTime, MAX_TIME);
            break;
        case DateTimeRule.DOW_LEQ_DOM:
            writeZonePropsByDOW_LEQ_DOM(writer, isDst, rule.getName(), fromRawOffset + fromDSTSavings, toOffset,
                    dtrule.getRuleMonth(), dtrule.getRuleDayOfMonth(), dtrule.getRuleDayOfWeek(), startTime, MAX_TIME);
            break;
        }
    }

    /*
     * Convert the rule to its equivalent rule using WALL_TIME mode
     */
    private static DateTimeRule toWallTimeRule(DateTimeRule rule, int rawOffset, int dstSavings) {
        if (rule.getTimeRuleType() == DateTimeRule.WALL_TIME) {
            return rule;
        }
        int wallt = rule.getRuleMillisInDay();
        if (rule.getTimeRuleType() == DateTimeRule.UTC_TIME) {
            wallt += (rawOffset + dstSavings);
        } else if (rule.getTimeRuleType() == DateTimeRule.STANDARD_TIME) {
            wallt += dstSavings;
        }

        int month = -1, dom = 0, dow = 0, dtype = -1;
        int dshift = 0;
        if (wallt < 0) {
            dshift = -1;
            wallt += Grego.MILLIS_PER_DAY;
        } else if (wallt >= Grego.MILLIS_PER_DAY) {
            dshift = 1;
            wallt -= Grego.MILLIS_PER_DAY;
        }

        month = rule.getRuleMonth();
        dom = rule.getRuleDayOfMonth();
        dow = rule.getRuleDayOfWeek();
        dtype = rule.getDateRuleType();

        if (dshift != 0) {
            if (dtype == DateTimeRule.DOW) {
                // Convert to DOW_GEW_DOM or DOW_LEQ_DOM rule first
                int wim = rule.getRuleWeekInMonth();
                if (wim > 0) {
                    dtype = DateTimeRule.DOW_GEQ_DOM;
                    dom = 7 * (wim - 1) + 1;
                } else {
                    dtype = DateTimeRule.DOW_LEQ_DOM;
                    dom = MONTHLENGTH[month] + 7 * (wim + 1);
                }

            }
            // Shift one day before or after
            dom += dshift;
            if (dom == 0) {
                month--;
                month = month < Calendar.JANUARY ? Calendar.DECEMBER : month;
                dom = MONTHLENGTH[month];
            } else if (dom > MONTHLENGTH[month]) {
                month++;
                month = month > Calendar.DECEMBER ? Calendar.JANUARY : month;
                dom = 1;
            }
            if (dtype != DateTimeRule.DOM) {
                // Adjust day of week
                dow += dshift;
                if (dow < Calendar.SUNDAY) {
                    dow = Calendar.SATURDAY;
                } else if (dow > Calendar.SATURDAY) {
                    dow = Calendar.SUNDAY;
                }
            }
        }
        // Create a new rule
        DateTimeRule modifiedRule;
        if (dtype == DateTimeRule.DOM) {
            modifiedRule = new DateTimeRule(month, dom, wallt, DateTimeRule.WALL_TIME);
        } else {
            modifiedRule = new DateTimeRule(month, dom, dow,
                    (dtype == DateTimeRule.DOW_GEQ_DOM), wallt, DateTimeRule.WALL_TIME);
        }
        return modifiedRule;
    }

    /*
     * Write the opening section of zone properties
     */
    private static void beginZoneProps(Writer writer, boolean isDst, String tzname, int fromOffset, int toOffset, long startTime) throws IOException {
        writer.write(ICAL_BEGIN);
        writer.write(COLON);
        if (isDst) {
            writer.write(ICAL_DAYLIGHT);
        } else {
            writer.write(ICAL_STANDARD);
        }
        writer.write(NEWLINE);

        // TZOFFSETTO
        writer.write(ICAL_TZOFFSETTO);
        writer.write(COLON);
        writer.write(millisToOffset(toOffset));
        writer.write(NEWLINE);

        // TZOFFSETFROM
        writer.write(ICAL_TZOFFSETFROM);
        writer.write(COLON);
        writer.write(millisToOffset(fromOffset));
        writer.write(NEWLINE);

        // TZNAME
        writer.write(ICAL_TZNAME);
        writer.write(COLON);
        writer.write(tzname);
        writer.write(NEWLINE);
        
        // DTSTART
        writer.write(ICAL_DTSTART);
        writer.write(COLON);
        writer.write(getDateTimeString(startTime + fromOffset));
        writer.write(NEWLINE);        
    }

    /*
     * Writes the closing section of zone properties
     */
    private static void endZoneProps(Writer writer, boolean isDst) throws IOException{
        // END:STANDARD or END:DAYLIGHT
        writer.write(ICAL_END);
        writer.write(COLON);
        if (isDst) {
            writer.write(ICAL_DAYLIGHT);
        } else {
            writer.write(ICAL_STANDARD);
        }
        writer.write(NEWLINE);
    }

    /*
     * Write the beginning part of RRULE line
     */
    private static void beginRRULE(Writer writer, int month) throws IOException {
        writer.write(ICAL_RRULE);
        writer.write(COLON);
        writer.write(ICAL_FREQ);
        writer.write(EQUALS_SIGN);
        writer.write(ICAL_YEARLY);
        writer.write(SEMICOLON);
        writer.write(ICAL_BYMONTH);
        writer.write(EQUALS_SIGN);
        writer.write(Integer.toString(month + 1));
        writer.write(SEMICOLON);
    }

    /*
     * Append the UNTIL attribute after RRULE line
     */
    private static void appendUNTIL(Writer writer, String until) throws IOException {
        if (until != null) {
            writer.write(SEMICOLON);
            writer.write(ICAL_UNTIL);
            writer.write(EQUALS_SIGN);
            writer.write(until);
        }
    }

    /*
     * Write the opening section of the VTIMEZONE block
     */
    private void writeHeader(Writer writer)throws IOException {
        writer.write(ICAL_BEGIN);
        writer.write(COLON);
        writer.write(ICAL_VTIMEZONE);
        writer.write(NEWLINE);
        writer.write(ICAL_TZID);
        writer.write(COLON);
        writer.write(tz.getID());
        writer.write(NEWLINE);
        if (tzurl != null) {
            writer.write(ICAL_TZURL);
            writer.write(COLON);
            writer.write(tzurl);
            writer.write(NEWLINE);
        }
        if (lastmod != null) {
            writer.write(ICAL_LASTMOD);
            writer.write(COLON);
            writer.write(getUTCDateTimeString(lastmod.getTime()));
            writer.write(NEWLINE);
        }
    }

    /*
     * Write the closing section of the VTIMEZONE definition block
     */
    private static void writeFooter(Writer writer) throws IOException {
        writer.write(ICAL_END);
        writer.write(COLON);
        writer.write(ICAL_VTIMEZONE);
        writer.write(NEWLINE);
    }

    /*
     * Convert date/time to RFC2445 Date-Time form #1 DATE WITH LOCAL TIME
     */
    private static String getDateTimeString(long time) {
        int[] fields = Grego.timeToFields(time, null);
        StringBuilder sb = new StringBuilder(15);
        sb.append(numToString(fields[0], 4));
        sb.append(numToString(fields[1] + 1, 2));
        sb.append(numToString(fields[2], 2));
        sb.append('T');

        int t = fields[5];
        int hour = t / Grego.MILLIS_PER_HOUR;
        t %= Grego.MILLIS_PER_HOUR;
        int min = t / Grego.MILLIS_PER_MINUTE;
        t %= Grego.MILLIS_PER_MINUTE;
        int sec = t / Grego.MILLIS_PER_SECOND;
        
        sb.append(numToString(hour, 2));
        sb.append(numToString(min, 2));
        sb.append(numToString(sec, 2));
        return sb.toString();
    }

    /*
     * Convert date/time to RFC2445 Date-Time form #2 DATE WITH UTC TIME
     */
    private static String getUTCDateTimeString(long time) {
        return getDateTimeString(time) + "Z";
    }

    /*
     * Parse RFC2445 Date-Time form #1 DATE WITH LOCAL TIME and
     * #2 DATE WITH UTC TIME
     */
    private static long parseDateTimeString(String str, int offset) {
        int year = 0, month = 0, day = 0, hour = 0, min = 0, sec = 0;
        boolean isUTC = false;
        boolean isValid = false;
        do {
            if (str == null) {
                break;
            }

            int length = str.length();
            if (length != 15 && length != 16) {
                // FORM#1 15 characters, such as "20060317T142115"
                // FORM#2 16 characters, such as "20060317T142115Z"
                break;
            }
            if (str.charAt(8) != 'T') {
                // charcter "T" must be used for separating date and time
                break;
            }
            if (length == 16) {
                if (str.charAt(15) != 'Z') {
                    // invalid format
                    break;
                }
                isUTC = true;
            }

            try {
                year = Integer.parseInt(str.substring(0, 4));
                month = Integer.parseInt(str.substring(4, 6)) - 1;  // 0-based
                day = Integer.parseInt(str.substring(6, 8));
                hour = Integer.parseInt(str.substring(9, 11));
                min = Integer.parseInt(str.substring(11, 13));
                sec = Integer.parseInt(str.substring(13, 15));
            } catch (NumberFormatException nfe) {
                break;
            }

            // check valid range
            int maxDayOfMonth = Grego.monthLength(year, month);
            if (year < 0 || month < 0 || month > 11 || day < 1 || day > maxDayOfMonth ||
                    hour < 0 || hour >= 24 || min < 0 || min >= 60 || sec < 0 || sec >= 60) {
                break;
            }

            isValid = true;
        } while(false);

        if (!isValid) {
            throw new IllegalArgumentException("Invalid date time string format");
        }
        // Calculate the time
        long time = Grego.fieldsToDay(year, month, day) * Grego.MILLIS_PER_DAY;
        time += (hour*Grego.MILLIS_PER_HOUR + min*Grego.MILLIS_PER_MINUTE + sec*Grego.MILLIS_PER_SECOND);
        if (!isUTC) {
            time -= offset;
        }
        return time;
    }

    /*
     * Convert RFC2445 utc-offset string to milliseconds
     */
    private static int offsetStrToMillis(String str) {
        boolean isValid = false;
        int sign = 0, hour = 0, min = 0, sec = 0;

        do {
            if (str == null) {
                break;
            }
            int length = str.length();
            if (length != 5 && length != 7) {
                // utf-offset must be 5 or 7 characters
                break;
            }
            // sign
            char s = str.charAt(0);
            if (s == '+') {
                sign = 1;
            } else if (s == '-') {
                sign = -1;
            } else {
                // utf-offset must start with "+" or "-"
                break;
            }

            try {
                hour = Integer.parseInt(str.substring(1, 3));
                min = Integer.parseInt(str.substring(3, 5));
                if (length == 7) {
                    sec = Integer.parseInt(str.substring(5, 7));
                }
            } catch (NumberFormatException nfe) {
                break;
            }
            isValid = true;
        } while(false);

        if (!isValid) {
            throw new IllegalArgumentException("Bad offset string");
        }
        int millis = sign * ((hour * 60 + min) * 60 + sec) * 1000;
        return millis;
    }

    /*
     * Convert milliseconds to RFC2445 utc-offset string
     */
    private static String millisToOffset(int millis) {
        StringBuilder sb = new StringBuilder(7);
        if (millis >= 0) {
            sb.append('+');
        } else {
            sb.append('-');
            millis = -millis;
        }
        int hour, min, sec;
        int t = millis / 1000;

        sec = t % 60;
        t = (t - sec) / 60;
        min = t % 60;
        hour = t / 60;

        sb.append(numToString(hour, 2));
        sb.append(numToString(min, 2));
        sb.append(numToString(sec, 2));

        return sb.toString();
    }

    /*
     * Format integer number
     */
    private static String numToString(int num, int width) {
        String str = Integer.toString(num);
        int len = str.length();
        if (len >= width) {
            return str.substring(len - width, len);
        }
        StringBuilder sb = new StringBuilder(width);
        for (int i = len; i < width; i++) {
            sb.append('0');
        }
        sb.append(str);
        return sb.toString();
    }

    // Freezable stuffs
    private volatile transient boolean isFrozen = false;

    /**
     * {@inheritDoc}
     */
    public boolean isFrozen() {
        return isFrozen;
    }

    /**
     * {@inheritDoc}
     */
    public TimeZone freeze() {
        isFrozen = true;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public TimeZone cloneAsThawed() {
        VTimeZone vtz = (VTimeZone)super.cloneAsThawed();
        vtz.tz = (BasicTimeZone)tz.cloneAsThawed();
        vtz.isFrozen = false;
        return vtz;
    }
}
