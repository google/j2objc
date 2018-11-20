/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2011-2012, Google, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.util.TrieMap.Style;
import android.icu.dev.util.Timer;
import android.icu.impl.Row;
import android.icu.impl.Row.R3;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;
import android.icu.lang.UScript;
import android.icu.text.DecimalFormat;
import android.icu.text.UnicodeSet;
import android.icu.util.StringTrieBuilder.Option;
import android.icu.util.ULocale;

public class TrieMapTest extends TestFmwk {
    static final boolean SHORT = false;
    static final boolean HACK_TO_MAKE_TESTS_PASS = false;
    static final int MASK = 0x3;

    private Map<String, Integer> unicodeTestMap = new HashMap<String, Integer>();
    private boolean useSmallList = true;

    private static Timer t = new Timer();
    private static DecimalFormat nf = t.getNumberFormat();
    private static DecimalFormat pf = t.getPercentFormat();
    {
        pf.setMaximumFractionDigits(0);
    }

    @Before
    public void init() throws Exception {
        if (unicodeTestMap.size() == 0) {
            if (TestFmwk.getExhaustiveness() < 5) {
                logln("\tShort version, timing for 1s:\t to get more accurate figures and test for reasonable times, use -e5 or more");
                t.setTimingPeriod(1*Timer.SECONDS);
            } else {
                int seconds = TestFmwk.getExhaustiveness();
                logln("\tExhaustive version, timing for " + seconds + "s");
                t.setTimingPeriod(seconds*Timer.SECONDS);
                useSmallList = false;
            }

            int i = 0;
            UnicodeSet testSet = new UnicodeSet("[[:^C:]-[:sc=han:]]");
            for (String s : testSet) {
                int codePoint = s.codePointAt(0);
                String extendedName = UCharacter.getExtendedName(codePoint);
                if (!unicodeTestMap.containsKey(extendedName)) {
                    unicodeTestMap.put(extendedName, i++);
                }
                if (i > 500 && useSmallList) break;
            }
            ULocale[] locales = useSmallList ? new ULocale[] {new ULocale("zh"), new ULocale("el")} : ULocale.getAvailableLocales();
            for (ULocale locale : locales) {
                if (locale.getDisplayCountry().length() != 0) {
                    continue;
                }
                String localeName;
                for (String languageCode : ULocale.getISOLanguages()) {
                    localeName = ULocale.getDisplayName(languageCode, locale);
                    if (!localeName.equals(languageCode)) {
                        if (!unicodeTestMap.containsKey(localeName)) {
                            unicodeTestMap.put(localeName, MASK & i++);
                        }
                        if (SHORT) break;
                    }
                }
                for (String countryCode : ULocale.getISOCountries()) {
                    localeName = ULocale.getDisplayCountry("und-" + countryCode, locale);
                    if (!localeName.equals(countryCode)) {
                        if (!unicodeTestMap.containsKey(localeName)) {
                            unicodeTestMap.put(localeName, MASK & i++);
                        }
                        if (SHORT) break;
                    }
                }
            }
            int charCount = 0; 
            for (String key : unicodeTestMap.keySet()) {
                charCount += key.length();
            }
            logln("\tTest Data Elements:\t\t\t" + nf.format(unicodeTestMap.size()));
            logln("\tTotal chars:\t\t\t" + nf.format(charCount));
        }
    }

    @Ignore
    @Test
    public void TestByteConversion() {
        byte bytes[] = new byte[200];
        for (Entry<String, Integer> entry : unicodeTestMap.entrySet()) {
            String source = entry.getKey();
            int limit = TrieMap.ByteConverter.getBytes(source, bytes, 0);
            //logln(source + "\t=> " + Utility.hex(source, " ") + "\t=> " + Utility.hex(bytes, 0, limit, " "));
            String recovered = TrieMap.ByteConverter.getChars(bytes, 0, limit);
            if (!source.equals(recovered)) {
                assertEquals("Char/Byte Conversion", source, recovered);
            }
        }
    }

    @Ignore
    @Test
    public void TestGet() {
        checkGet(unicodeTestMap, TrieMap.Style.BYTES);
        checkGet(unicodeTestMap, TrieMap.Style.CHARS);
    }

    private void checkGet(Map<String, Integer> testmap, TrieMap.Style style) {
        if (testmap.size() == 0) {
            return;
        }
        TrieMap<Integer> trieMap = TrieMap.Builder.with(style, Option.SMALL, testmap).build();
        //logln(trieMap.toString());
        for (Entry<String, Integer> entry : testmap.entrySet()) {
            Integer value = entry.getValue();
            String key = entry.getKey();
            Integer foundValue = trieMap.get(key);
            if (!value.equals(foundValue)) {
                // TODO fix this
                if (!HACK_TO_MAKE_TESTS_PASS || 39497 != value) {
                    assertEquals(style + "\tGet of '" + key + "' = {" + Utility.hex(key) + "}", value, foundValue);
                }
            }
        }        
    }

    @Ignore
    @Test
    public void TestTimeIteration() {
        long comparisonTime = timeIteration(unicodeTestMap, 0, null, 0);
        timeIteration(unicodeTestMap, comparisonTime, null, 0);
        timeIteration(unicodeTestMap, comparisonTime, Style.BYTES, 5);
        timeIteration(unicodeTestMap, comparisonTime, Style.CHARS, 3);
    }

    @SuppressWarnings("unused")
    public long timeIteration(Map<String, Integer> testMap, long comparisonTime, Style style, double ratioToMap) {
        TrieMap<Integer> trieMap = TrieMap.BytesBuilder.with(style, Option.SMALL, testMap).build();
        TreeMap<String,Integer> expected = new TreeMap<String, Integer>(testMap);

        System.gc();
        t.start();
        if (style == null) {
            Map<String, Integer> map = comparisonTime == 0 ? new TreeMap<String, Integer>(testMap) : new HashMap<String, Integer>(testMap);

            long mapTime = t.timeIterations(new MyLoop() {
                public void time(int repeat) {
                    for (int tt = 0; tt < repeat; ++tt) {
                        for (Entry<String, Integer> entry : map.entrySet()) {
                            String key = entry.getKey();
                            Integer value = entry.getValue();
                        }
                    }
                } 
            }, null, map);
            if (comparisonTime == 0) {
                logln("\titeration time\tTREEMAP\tn/a\t" + t.toString(testMap.size()) + "\t\titerations=" + t.getIterations());
            } else {
                logln("\titeration time\tHASHMAP\tn/a\t" + t.toString(testMap.size(), comparisonTime) + "\titerations=" + t.getIterations());
            }
            return mapTime;
        } else {
            long trieTime = t.timeIterations(new MyLoop() {
                public void time(int repeat) {
                    for (int tt = 0; tt < repeat; ++tt) {
                        for (Entry<CharSequence, Integer> entry : trieMap) {
                            CharSequence key = entry.getKey();
                            Integer value = entry.getValue();
                        }
                    }
                } 
            }, null, trieMap);
            logln("\titeration time\t" + style + "\tn/a\t" + t.toString(testMap.size(), comparisonTime) + "\titerations=" + t.getIterations());
            if (!useSmallList && trieTime > ratioToMap * comparisonTime) {
                errln(style + "\tTime iteration takes too long. Expected:\t< " + ratioToMap * comparisonTime + ", Actual:\t" + trieTime);
            }
            return trieTime;
        }
    }

    @Ignore
    @Test
    public void TestContents() {
        checkContents(unicodeTestMap, Style.BYTES);
        checkContents(unicodeTestMap, Style.CHARS);
    }

    public void checkContents(Map<String, Integer> testMap, Style style) {
        if (testMap.size() == 0) {
            return;
        }
        TrieMap<Integer> trieMap = TrieMap.BytesBuilder.with(style, Option.SMALL, testMap).build();
        TreeMap<String,Integer> expected = new TreeMap<String, Integer>(testMap);
        Iterator<Entry<CharSequence, Integer>> trieIterator = trieMap.iterator();
        Iterator<Entry<String, Integer>> mapIterator = expected.entrySet().iterator();
        while (true) {
            boolean trieOk = trieIterator.hasNext();
            boolean mapOk = mapIterator.hasNext();
            if (mapOk!=trieOk) {
                assertEquals("Iterators end at same point", mapOk, trieOk);
            }

            if (!mapOk) break;
            Entry<CharSequence, Integer> trieEntry = trieIterator.next();
            Entry<String, Integer> mapEntry = mapIterator.next();
            String mapKey = mapEntry.getKey();
            CharSequence trieKey = trieEntry.getKey();
            if (!mapKey.contentEquals(trieKey)) {
                assertEquals(style + "\tKeys match", mapKey, trieKey.toString());
            }
            Integer mapValue = mapEntry.getValue();
            Integer trieValue = trieEntry.getValue();
            if (!mapValue.equals(trieValue)) {
                assertEquals(style + "\tValues match", mapValue, trieValue);
            }
        }
    }

    @Ignore
    @Test
    public void TestSearch() {
        checkSearch(Style.BYTES);
        checkSearch(Style.CHARS);
    }

    public void checkSearch(Style style) {

        TrieMap<String> trieMap = TrieMap.BytesBuilder.with(style, Option.SMALL, "abc", "first")
        .add("cdab", "fifth")
        .add("abcde", "second")
        .add("abdfg", "third").build();

        String string = "xabcdab abcde abdfg";
        @SuppressWarnings("unchecked")
        Row.R3<Integer, Integer, String>[] expected = new Row.R3[] {
            Row.of(1,4,"first"),
            Row.of(3,7,"fifth"),
            Row.of(8,11,"first"),
            Row.of(8,13,"second"),
            Row.of(14,19,"third"),
        };
        List<R3<Integer, Integer, String>> expectedList = Arrays.asList(expected);
        List<R3<Integer, Integer, String>> actualList = new ArrayList<R3<Integer, Integer, String>>();

        TrieMap.Matcher<String> matcher = trieMap.getMatcher();
        matcher.set(string, 0);
        do {
            boolean hasMore;
            do {
                hasMore = matcher.next();
                String value = matcher.getValue();
                if (value != null) {
                    int start = matcher.getStart();
                    int end = matcher.getEnd();
                    actualList.add(Row.of(start,end,value));
                }
            } while (hasMore);
        } while (matcher.nextStart());
        assertEquals(style + "\tTrieMap matcher", expectedList, actualList);
        //        logln(bytes + "\tValue <" + value + "> at " 
        //                + start + ".." + end + ", "
        //                + string.substring(0, start) + "|"
        //                + string.substring(start, end) + "|"
        //                + string.substring(end)
        //        );
    }

    @Ignore
    @Test
    public void TestTimeBuilding() {
        long comparisonTime = timeBuilding(unicodeTestMap, 0, null, Option.SMALL, 0);
        timeBuilding(unicodeTestMap, comparisonTime, null, Option.SMALL, 0);
        timeBuilding(unicodeTestMap, comparisonTime, Style.BYTES, Option.SMALL, 20);
        timeBuilding(unicodeTestMap, comparisonTime, Style.BYTES, Option.FAST, 20);
        timeBuilding(unicodeTestMap, comparisonTime, Style.CHARS, Option.SMALL, 20);
        timeBuilding(unicodeTestMap, comparisonTime, Style.CHARS, Option.FAST, 20);
    }

    @SuppressWarnings("unused")
    public long timeBuilding(Map<String, Integer> testmap, long comparisonTime, Style style, Option option, double ratioToMap) {
        System.gc();
        t.start();
        if (style == null) {
            if (comparisonTime == 0) {
                long mapTime = t.timeIterations(new MyLoop() {
                    public void time(int repeat) {
                        for (int tt = 0; tt < repeat; ++tt) {
                            Map<String, Integer> map2 = new TreeMap<String, Integer>(map);
                        }
                    } 
                }, null, testmap);
                logln("\tbuild time\tTREEMAP\tn/a\t" + t.toString(testmap.size()) + "\t\titerations=" + t.getIterations());
                return mapTime;
            } else {
                long mapTime = t.timeIterations(new MyLoop() {
                    public void time(int repeat) {
                        for (int tt = 0; tt < repeat; ++tt) {
                            Map<String, Integer> map2 = new HashMap<String, Integer>(map);
                        }
                    } 
                }, null, testmap);
                logln("\tbuild time\tHASHMAP\tn/a\t" + t.toString(testmap.size(), comparisonTime) + "\titerations=" + t.getIterations());
                return mapTime;
            }
        } else {
            long trieTime = t.timeIterations(new MyLoop() {
                public void time(int repeat) {
                    for (int tt = 0; tt < repeat; ++tt) {
                        trieMap = TrieMap.BytesBuilder.with(style, option, map).build();
                    }
                } 
            }, null, testmap, style, option);

            logln("\tbuild time\t" + style + "\t" + option + "\t" + t.toString(testmap.size(), comparisonTime) + "\titerations=" + t.getIterations());
            if (!useSmallList && trieTime > ratioToMap * comparisonTime) {
                errln(style + "\t" + option + "\tTrie build takes too long. Expected:\t< " + nf.format(ratioToMap * comparisonTime) + ", Actual:\t" + nf.format(trieTime));
            }
            return trieTime;
        }
    }

    @Ignore
    @Test
    public void TestSize() {
        int size = checkSize(0, null, Option.SMALL, 0);
        checkSize(size, Style.BYTES, Option.SMALL, 0.20);
        checkSize(size, Style.BYTES, Option.FAST, 0.20);
        checkSize(size, Style.CHARS, Option.SMALL, 0.30);
        checkSize(size, Style.CHARS, Option.FAST, 0.30);
    }

    /**
     * @param option TODO
     * @param ratioToMap TODO
     * @param bytes
     */
    private int checkSize(int comparisonSize, Style style, Option option, double ratioToMap) {
        if (style == null) {
            int mapKeyByteSize = 0;
            TreeMap<String, Integer> map = new TreeMap<String, Integer>(unicodeTestMap);
            for (Entry<String, Integer> entry : map.entrySet()) {
                mapKeyByteSize += 8 * (int) ((((entry.getKey().length()) * 2) + 45) / 8);
            }
            logln("\tkey byte size\tTREEMAP\tn/a\t" + nf.format(mapKeyByteSize));
            return mapKeyByteSize;
        } else {
            TrieMap<Integer> trieMap = TrieMap.BytesBuilder.with(style, option, unicodeTestMap).build();

            int trieKeyByteSize = trieMap.keyByteSize();
            logln("\tkey byte size\t" + style + "\t" + option + "\t" + nf.format(trieKeyByteSize) + "\t\t" + pf.format(trieKeyByteSize/(double)comparisonSize - 1D) + "");


            if (!useSmallList && trieKeyByteSize > ratioToMap * comparisonSize) {
                errln(style + "\t" + option + "\ttrieKeyByteSize too large. Expected:\t< " + nf.format(ratioToMap * comparisonSize) + ", Actual:\t" + nf.format(trieKeyByteSize));
            }
            return trieKeyByteSize;
        }
    }

    @Ignore
	@Test
    public void TestTimeGet() {
        HashSet<String> keySet = new HashSet<String>(unicodeTestMap.keySet());
        ULocale[] locales = ULocale.getAvailableLocales();
        int i = 0;
        for (ULocale locale : locales) {
            if (locale.getDisplayCountry().length() != 0) {
                continue;
            }
            String localeName;
            for (int scriptCodeInt = 0; scriptCodeInt < UScript.CODE_LIMIT; ++scriptCodeInt) {
                String scriptCode = UScript.getShortName(scriptCodeInt);
                localeName = ULocale.getDisplayScript("und-" + scriptCode, locale);
                if (!localeName.equals(scriptCode)) {
                    if (!keySet.contains(localeName)) {
                        keySet.add(localeName);
                        ++i;
                    }
                    if (SHORT) break;
                }
            }
        }
        logln("\tExtra Key Elements\t" + i);

        ArrayList<String> keys = new ArrayList<String>(keySet);

        long comparisonTime = timeGet(keys, unicodeTestMap, 0, null, 0);
        timeGet(keys, unicodeTestMap, comparisonTime, null, 0);
        timeGet(keys, unicodeTestMap, comparisonTime, Style.BYTES, 3);
        timeGet(keys, unicodeTestMap, comparisonTime, Style.CHARS, 3);
    }

    @SuppressWarnings("unused")
    public long timeGet(ArrayList<String> keys, Map<String, Integer> testmap, long comparisonTime, Style style, int ratioToMap) {

        TrieMap<Integer> trieMap = TrieMap.Builder.with(style, Option.SMALL, testmap).build();

        if (style == null) {
            Map<String, Integer> map = comparisonTime == 0 ? new TreeMap<String, Integer>(testmap) : new HashMap<String, Integer>(testmap);

            long mapTime = t.timeIterations(new MyLoop() {
                public void time(int repeat) {
                    for (int tt = 0; tt < repeat; ++tt) {
                        for (String key : keys) {
                            Integer foundValue = map.get(key);
                        }
                    }
                } 
            }, keys, map);
            if (comparisonTime == 0) {
                logln("\tget() time\tTREEMAP\tn/a\t" + t.toString(keys.size()) + "\t\titerations=" + t.getIterations());
            } else {
                logln("\tget() time\tHASHMAP\tn/a\t" + t.toString(keys.size(), comparisonTime) + "\titerations=" + t.getIterations());
            }
            return mapTime;
        } else {
            long trieTime = t.timeIterations(new MyLoop() {
                public void time(int repeat) {
                    for (int tt = 0; tt < repeat; ++tt) {
                        for (String key : keys) {
                            Integer foundValue = trieMap.get(key);
                        }
                    }
                } 
            }, keys, trieMap);

            //            System.gc();
            //            t.start();
            //            for (int tt = 0; tt < repeat; ++tt) {
            //                for (String key : keys) {
            //                    Integer foundValue = trieMap.get(key);
            //                }
            //            }
            //            long trieTime = t.getDuration();
            logln("\tget() time\t" + style + "\tn/a\t" + t.toString(keys.size(), comparisonTime) + "\titerations=" + t.getIterations());
            if (!useSmallList && trieTime > ratioToMap * comparisonTime) {
                errln(style + "\tTime iteration takes too long. Expected:\t< " + ratioToMap * comparisonTime + ", Actual:\t" + trieTime);
            }
            return trieTime;
        }
    }

    static abstract class MyLoop extends Timer.Loop {
        ArrayList<String> keys;
        TrieMap<Integer> trieMap;
        Map<String, Integer> map;
        Style style;
        Option option;
        public void init(Object... params) {
            if (params.length > 0) {
                keys = (ArrayList<String>) params[0];
            }
            if (params.length > 1) {
                if (params[1] instanceof Map) {
                    map = (Map<String, Integer>) params[1];
                } else {
                    trieMap = (TrieMap<Integer>) params[1];
                }
            }
            if (params.length > 2) {
                style = (Style) params[2];
            }
            if (params.length > 3) {
                option = (Option) params[3];
            }
        }
        abstract public void time(int repeat);
    }

    //    static class Storage {
    //        char[] buffer;
    //        int limit;
    //
    //        public Storage(int initialCapacity) {
    //            buffer = new char[initialCapacity];
    //        }
    //
    //        public CharSequence add(CharSequence input) {
    //            int start = limit;
    //            int length = input.length();
    //            for (int i = 0; i < length; ++i) {
    //                try {
    //                    buffer[limit++] = input.charAt(i);
    //                } catch (Exception e) {
    //                    // we failed to add (limit-1)
    //                    int newCapacity = buffer.length * 3 / 2 + length;
    //                    //System.out.println(buffer.length + " => " + newCapacity);
    //                    char[] temp = new char[newCapacity];
    //                    System.arraycopy(buffer, 0, temp, 0, buffer.length);
    //                    buffer = temp;
    //                    buffer[limit - 1] = input.charAt(i);
    //                }
    //            }
    //            return new StorageCharSequence(start, limit);
    //        }
    //
    //        final class StorageCharSequence implements CharSequence, Comparable<CharSequence> {
    //            private int start;
    //            private int len;
    //
    //            public StorageCharSequence(int start, int limit) {
    //                if (start < 0 || start > limit || limit > buffer.length) {
    //                    throw new ArrayIndexOutOfBoundsException();
    //                }
    //                this.start = start;
    //                this.len = limit - start;
    //            }
    //            public char charAt(int arg0) {
    //                return arg0 < 0 || arg0 >= len ? buffer[-1] : buffer[arg0 + start];
    //            }
    //            public int length() {
    //                return len;
    //            }
    //            public CharSequence subSequence(int start, int limit) {
    //                return new StorageCharSequence(this.start + start, this.start + limit);
    //            }
    //            public String toString() {
    //                return String.valueOf(buffer, start, len);
    //            }
    //            public int hashCode() {
    //                int result = len;
    //                int limit = start + len;
    //                for (int i = start; i < limit; ++i) {
    //                    result *= 37;
    //                    result += i;
    //                }
    //                return result;
    //            }
    //            public boolean equals(Object other) {
    //                try {
    //                    StorageCharSequence that = (StorageCharSequence) other;
    //                    // can optimize
    //                    return CharSequences.equalsChars(this, that);
    //                } catch (Exception e) {
    //                    return false;
    //                }
    //            }
    //            public int compareTo(CharSequence other) {
    //                // can optimize
    //                return CharSequences.compare(this, other);
    //            }
    //        }
    //
    //    }
    //    public void TestStorage() {
    //        ArrayList<String> keys = new ArrayList<String>(unicodeTestMap.keySet());
    //        int repeat = REPEAT * 10;
    //        System.gc();
    //        t.start();
    //        for (int tt = 0; tt < repeat; ++tt) {
    //            Set<CharSequence> store = new HashSet<CharSequence>();
    //            // Storage storage = new Storage(1024);
    //            for (String key : keys) {
    //                store.add(key);
    //                //                CharSequence item = storage.add(key);
    //                //                if (!store.contains(item)) {
    //                //                    store.add(item);
    //                //                }
    //                //                if (!CharSequences.equalsChars(key, item)) {
    //                //                    throw new IllegalArgumentException(key);
    //                //                }
    //            }
    //            CharSequence[] raw = store.toArray(new CharSequence[store.size()]);
    //            Arrays.sort(raw);
    //        }
    //        long comparisonTime = t.getDuration();
    //        logln("\tget() time\tHashSet,sort\tn/a\t" + t.toString(repeat*keys.size()));
    //
    //        System.gc();
    //        t.start();
    //        for (int tt = 0; tt < repeat; ++tt) {
    //            Set<CharSequence> store = new TreeSet<CharSequence>();
    //            for (String key : keys) {
    //                store.add(key);
    //            }
    //            CharSequence[] raw = store.toArray(new CharSequence[store.size()]);
    //        }
    //        long trieTime = t.getDuration();
    //        logln("\tget() time\tTreeSet\tn/a\t" + t.toString(repeat*keys.size(), comparisonTime));
    //
    //    }
}
