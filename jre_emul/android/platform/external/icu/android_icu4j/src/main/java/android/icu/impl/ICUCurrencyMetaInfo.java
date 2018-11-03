/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2009-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.icu.text.CurrencyMetaInfo;
import android.icu.util.Currency.CurrencyUsage;

/**
 * ICU's currency meta info data.
 * @hide Only a subset of ICU is exposed in Android
 */
public class ICUCurrencyMetaInfo extends CurrencyMetaInfo {
    private ICUResourceBundle regionInfo;
    private ICUResourceBundle digitInfo;

    public ICUCurrencyMetaInfo() {
        ICUResourceBundle bundle = (ICUResourceBundle) ICUResourceBundle.getBundleInstance(
            ICUData.ICU_CURR_BASE_NAME, "supplementalData",
            ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        regionInfo = bundle.findTopLevel("CurrencyMap");
        digitInfo = bundle.findTopLevel("CurrencyMeta");
    }

    @Override
    public List<CurrencyInfo> currencyInfo(CurrencyFilter filter) {
        return collect(new InfoCollector(), filter);
    }

    @Override
    public List<String> currencies(CurrencyFilter filter) {
        return collect(new CurrencyCollector(), filter);
   }

    @Override
    public List<String> regions(CurrencyFilter filter) {
        return collect(new RegionCollector(), filter);
    }

    @Override
    public CurrencyDigits currencyDigits(String isoCode) {
        return currencyDigits(isoCode, CurrencyUsage.STANDARD);
    }

    @Override
    public CurrencyDigits currencyDigits(String isoCode, CurrencyUsage currencyPurpose) {
        ICUResourceBundle b = digitInfo.findWithFallback(isoCode);
        if (b == null) {
            b = digitInfo.findWithFallback("DEFAULT");
        }
        int[] data = b.getIntVector();
        if (currencyPurpose == CurrencyUsage.CASH) {
            return new CurrencyDigits(data[2], data[3]);
        } else if (currencyPurpose == CurrencyUsage.STANDARD) {
            return new CurrencyDigits(data[0], data[1]);
        } else {
            return new CurrencyDigits(data[0], data[1]);
        }
    }

    private <T> List<T> collect(Collector<T> collector, CurrencyFilter filter) {
        // We rely on the fact that the data lists the regions in order, and the
        // priorities in order within region.  This means we don't need
        // to sort the results to ensure the ordering matches the spec.

        if (filter == null) {
            filter = CurrencyFilter.all();
        }
        int needed = collector.collects();
        if (filter.region != null) {
            needed |= Region;
        }
        if (filter.currency != null) {
            needed |= Currency;
        }
        if (filter.from != Long.MIN_VALUE || filter.to != Long.MAX_VALUE) {
            needed |= Date;
        }
        if (filter.tenderOnly) {
            needed |= Tender;
        }

        if (needed != 0) {
            if (filter.region != null) {
                ICUResourceBundle b = regionInfo.findWithFallback(filter.region);
                if (b != null) {
                    collectRegion(collector, filter, needed, b);
                }
            } else {
                for (int i = 0; i < regionInfo.getSize(); i++) {
                    collectRegion(collector, filter, needed, regionInfo.at(i));
                }
            }
        }

        return collector.getList();
    }

    private <T> void collectRegion(Collector<T> collector, CurrencyFilter filter,
            int needed, ICUResourceBundle b) {

        String region = b.getKey();
        if (needed == Region) {
            collector.collect(b.getKey(), null, 0, 0, -1, false);
            return;
        }

        for (int i = 0; i < b.getSize(); i++) {
            ICUResourceBundle r = b.at(i);
            if (r.getSize() == 0) {
                // AQ[0] is an empty array instead of a table, so the bundle is null.
                // There's no data here, so we skip this entirely.
                // We'd do a type test, but the ResourceArray type is private.
                continue;
            }
            String currency = null;
            long from = Long.MIN_VALUE;
            long to = Long.MAX_VALUE;
            boolean tender = true;

            if ((needed & Currency) != 0) {
                ICUResourceBundle currBundle = r.at("id");
                currency = currBundle.getString();
                if (filter.currency != null && !filter.currency.equals(currency)) {
                    continue;
                }
            }

            if ((needed & Date) != 0) {
                from = getDate(r.at("from"), Long.MIN_VALUE, false);
                to = getDate(r.at("to"), Long.MAX_VALUE, true);
                // In the data, to is always > from.  This means that when we have a range
                // from == to, the comparisons below will always do the right thing, despite
                // the range being technically empty.  It really should be [from, from+1) but
                // this way we don't need to fiddle with it.
                if (filter.from > to) {
                    continue;
                }
                if (filter.to < from) {
                    continue;
                }
            }
            if ((needed & Tender) != 0) {
                ICUResourceBundle tenderBundle = r.at("tender");
                tender = tenderBundle == null || "true".equals(tenderBundle.getString());
                if (filter.tenderOnly && !tender) {
                    continue;
                }
            }

            // data lists elements in priority order, so 'i' suffices
            collector.collect(region, currency, from, to, i, tender);
        }
    }

    private static final long MASK = 4294967295L;
    private long getDate(ICUResourceBundle b, long defaultValue, boolean endOfDay) {
        if (b == null) {
            return defaultValue;
        }
        int[] values = b.getIntVector();
        return ((long) values[0] << 32) | ((values[1]) & MASK);
    }

    // Utility, just because I don't like the n^2 behavior of using list.contains to build a
    // list of unique items.  If we used java 6 we could use their class for this.
    private static class UniqueList<T> {
        private Set<T> seen = new HashSet<T>();
        private List<T> list = new ArrayList<T>();

        private static <T> UniqueList<T> create() {
            return new UniqueList<T>();
        }

        void add(T value) {
            if (!seen.contains(value)) {
                list.add(value);
                seen.add(value);
            }
        }

        List<T> list() {
            return Collections.unmodifiableList(list);
        }
    }

    private static class InfoCollector implements Collector<CurrencyInfo> {
        // Data is already unique by region/priority, so we don't need to be concerned
        // about duplicates.
        private List<CurrencyInfo> result = new ArrayList<CurrencyInfo>();

        @Override
        public void collect(String region, String currency, long from, long to, int priority, boolean tender) {
            result.add(new CurrencyInfo(region, currency, from, to, priority, tender));
        }

        @Override
        public List<CurrencyInfo> getList() {
            return Collections.unmodifiableList(result);
        }

        @Override
        public int collects() {
            return Everything;
        }
    }

    private static class RegionCollector implements Collector<String> {
        private final UniqueList<String> result = UniqueList.create();

        @Override
        public void collect(
                String region, String currency, long from, long to, int priority, boolean tender) {
            result.add(region);
        }

        @Override
        public int collects() {
            return Region;
        }

        @Override
        public List<String> getList() {
            return result.list();
        }
    }

    private static class CurrencyCollector implements Collector<String> {
        private final UniqueList<String> result = UniqueList.create();

        @Override
        public void collect(
                String region, String currency, long from, long to, int priority, boolean tender) {
            result.add(currency);
        }

        @Override
        public int collects() {
            return Currency;
        }

        @Override
        public List<String> getList() {
            return result.list();
        }
    }

    private static final int Region = 1;
    private static final int Currency = 2;
    private static final int Date = 4;
    private static final int Tender = 8;
    private static final int Everything = Integer.MAX_VALUE;

    private static interface Collector<T> {
        /**
         * A bitmask of Region/Currency/Date indicating which features we collect.
         * @return the bitmask
         */
        int collects();

        /**
         * Called with data passed by filter.  Values not collected by filter should be ignored.
         * @param region the region code (null if ignored)
         * @param currency the currency code (null if ignored)
         * @param from start time (0 if ignored)
         * @param to end time (0 if ignored)
         * @param priority priority (-1 if ignored)
         * @param tender true if currency is legal tender.
         */
        void collect(String region, String currency, long from, long to, int priority, boolean tender);

        /**
         * Return the list of unique items in the order in which we encountered them for the
         * first time.  The returned list is unmodifiable.
         * @return the list
         */
        List<T> getList();
    }
}
