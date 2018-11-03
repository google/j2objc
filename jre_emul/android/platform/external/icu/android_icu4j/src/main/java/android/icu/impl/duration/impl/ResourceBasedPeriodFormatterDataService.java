/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************
 * Copyright (C) 2007-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 ******************************************************************************
 */

package android.icu.impl.duration.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import android.icu.impl.ICUData;
import android.icu.util.ICUUncheckedIOException;

/**
 * A PeriodFormatterDataService that serves PeriodFormatterData objects based on
 * data files stored as resources in this directory. These are text files named
 * after the locale, for example, 'pfd_he_IL.txt' specifies an period formatter
 * data file for Hebrew as spoken in Israel. Data is in a JSON-like format.
 * @hide Only a subset of ICU is exposed in Android
 */
public class ResourceBasedPeriodFormatterDataService extends
        PeriodFormatterDataService {
    private Collection<String> availableLocales; // of String

    private PeriodFormatterData lastData = null;
    private String lastLocale = null;
    private Map<String, PeriodFormatterData> cache = new HashMap<String, PeriodFormatterData>(); // String -> PeriodFormatterData
    // private PeriodFormatterData fallbackFormatterData;

    private static final String PATH = "data/";

    private static final ResourceBasedPeriodFormatterDataService singleton = new ResourceBasedPeriodFormatterDataService();

    /**
     * Returns the singleton instance of this class.
     */
    public static ResourceBasedPeriodFormatterDataService getInstance() {
        return singleton;
    }

    /**
     * Constructs the service.
     */
    private ResourceBasedPeriodFormatterDataService() {
        List<String> localeNames = new ArrayList<String>(); // of String
        InputStream is = ICUData.getRequiredStream(getClass(), PATH
                + "index.txt");
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is,
                    "UTF-8"));
            String string = null;
            while (null != (string = br.readLine())) {
                string = string.trim();
                if (string.startsWith("#") || string.length() == 0) {
                    continue;
                }
                localeNames.add(string);
            }
            br.close();
        } catch (IOException e) {
            throw new IllegalStateException("IO Error reading " + PATH
                    + "index.txt: " + e.toString());
        } finally {
            try {
                is.close();
            } catch (IOException ignored) {
            }
        }
        availableLocales = Collections.unmodifiableList(localeNames);
    }

    @Override
    public PeriodFormatterData get(String localeName) {
        // remove tag info including calendar, we don't use the calendar
        int x = localeName.indexOf('@');
        if (x != -1) {
            localeName = localeName.substring(0, x);
        }

        synchronized (this) {
            if (lastLocale != null && lastLocale.equals(localeName)) {
                return lastData;
            }

            PeriodFormatterData ld = cache.get(localeName);
            if (ld == null) {
                String ln = localeName;
                while (!availableLocales.contains(ln)) {
                    int ix = ln.lastIndexOf("_");
                    if (ix > -1) {
                        ln = ln.substring(0, ix);
                    } else if (!"test".equals(ln)) {
                        ln = "test";
                    } else {
                        ln = null;
                        break;
                    }
                }
                if (ln != null) {
                    String name = PATH + "pfd_" + ln + ".xml";
                    try {
                        InputStreamReader reader = new InputStreamReader(
                                ICUData.getRequiredStream(getClass(), name), "UTF-8");
                        DataRecord dr = DataRecord.read(ln, new XMLRecordReader(reader));
                        reader.close();
                        if (dr != null) {
                            // debug
                            // if (false && ln.equals("ar_EG")) {
                            // OutputStreamWriter osw = new
                            // OutputStreamWriter(System.out, "UTF-8");
                            // XMLRecordWriter xrw = new
                            // XMLRecordWriter(osw);
                            // dr.write(xrw);
                            // osw.flush();
                            // }
                            ld = new PeriodFormatterData(localeName, dr);
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new MissingResourceException(
                                "Unhandled encoding for resource " + name, name, "");
                    } catch (IOException e) {
                        throw new ICUUncheckedIOException(
                                "Failed to close() resource " + name, e);
                    }
                } else {
                    throw new MissingResourceException(
                            "Duration data not found for  " + localeName, PATH,
                            localeName);
                }

                // if (ld == null) {
                // ld = getFallbackFormatterData();
                // }
                cache.put(localeName, ld);
            }
            lastData = ld;
            lastLocale = localeName;

            return ld;
        }
    }

    @Override
    public Collection<String> getAvailableLocales() {
        return availableLocales;
    }

    // PeriodFormatterData getFallbackFormatterData() {
    // synchronized (this) {
    // if (fallbackFormatterData == null) {
    // DataRecord dr = new DataRecord(); // hack, no default, will die if used
    // fallbackFormatterData = new PeriodFormatterData(null, dr);
    // }
    // return fallbackFormatterData;
    // }
    // }
}
