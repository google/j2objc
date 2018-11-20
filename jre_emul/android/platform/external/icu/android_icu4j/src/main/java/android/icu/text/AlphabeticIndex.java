/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2008-2016, Google Inc, International Business Machines Corporation
 * and others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.icu.lang.UCharacter;
import android.icu.text.AlphabeticIndex.Bucket;
import android.icu.text.AlphabeticIndex.Bucket.LabelType;
import android.icu.util.LocaleData;
import android.icu.util.ULocale;

/**
 * AlphabeticIndex supports the creation of a UI index appropriate for a given language.
 * It can support either direct use, or use with a client that doesn't support localized collation.
 * The following is an example of what an index might look like in a UI:
 *
 * <pre>
 *  <b>... A B C D E F G H I J K L M N O P Q R S T U V W X Y Z  ...</b>
 *
 *  <b>A</b>
 *     Addison
 *     Albertson
 *     Azensky
 *  <b>B</b>
 *     Baecker
 *  ...
 * </pre>
 *
 * The class can generate a list of labels for use as a UI "index", that is, a list of
 * clickable characters (or character sequences) that allow the user to see a segment
 * (bucket) of a larger "target" list. That is, each label corresponds to a bucket in
 * the target list, where everything in the bucket is greater than or equal to the character
 * (according to the locale's collation). Strings can be added to the index;
 * they will be in sorted order in the right bucket.
 * <p>
 * The class also supports having buckets for strings before the first (underflow),
 * after the last (overflow), and between scripts (inflow). For example, if the index
 * is constructed with labels for Russian and English, Greek characters would fall
 * into an inflow bucket between the other two scripts.
 *
 * <p><em>Note:</em> If you expect to have a lot of ASCII or Latin characters
 * as well as characters from the user's language,
 * then it is a good idea to call addLabels(ULocale.English).
 *
 * <h2>Direct Use</h2>
 * <p>The following shows an example of building an index directly.
 *  The "show..." methods below are just to illustrate usage.
 *
 * <pre>
 * // Create a simple index where the values for the strings are Integers, and add the strings
 *
 * AlphabeticIndex&lt;Integer&gt; index = new AlphabeticIndex&lt;Integer&gt;(desiredLocale).addLabels(additionalLocale);
 * int counter = 0;
 * for (String item : test) {
 *     index.addRecord(item, counter++);
 * }
 * ...
 * // Show index at top. We could skip or gray out empty buckets
 *
 * for (AlphabeticIndex.Bucket&lt;Integer&gt; bucket : index) {
 *     if (showAll || bucket.size() != 0) {
 *         showLabelAtTop(UI, bucket.getLabel());
 *     }
 * }
 *  ...
 * // Show the buckets with their contents, skipping empty buckets
 *
 * for (AlphabeticIndex.Bucket&lt;Integer&gt; bucket : index) {
 *     if (bucket.size() != 0) {
 *         showLabelInList(UI, bucket.getLabel());
 *         for (AlphabeticIndex.Record&lt;Integer&gt; item : bucket) {
 *             showIndexedItem(UI, item.getName(), item.getData());
 *         }
 * </pre>
 *
 * The caller can build different UIs using this class.
 * For example, an index character could be omitted or grayed-out
 * if its bucket is empty. Small buckets could also be combined based on size, such as:
 *
 * <pre>
 * <b>... A-F G-N O-Z ...</b>
 * </pre>
 *
 * <h2>Client Support</h2>
 * <p>Callers can also use the {@link AlphabeticIndex.ImmutableIndex}, or the AlphabeticIndex itself,
 * to support sorting on a client that doesn't support AlphabeticIndex functionality.
 *
 * <p>The ImmutableIndex is both immutable and thread-safe.
 * The corresponding AlphabeticIndex methods are not thread-safe because
 * they "lazily" build the index buckets.
 * <ul>
 * <li>ImmutableIndex.getBucket(index) provides random access to all
 *     buckets and their labels and label types.
 * <li>AlphabeticIndex.getBucketLabels() or the bucket iterator on either class
 *     can be used to get a list of the labels,
 *     such as "...", "A", "B",..., and send that list to the client.
 * <li>When the client has a new name, it sends that name to the server.
 * The server needs to call the following methods,
 * and communicate the bucketIndex and collationKey back to the client.
 *
 * <pre>
 * int bucketIndex = index.getBucketIndex(name);
 * String label = immutableIndex.getBucket(bucketIndex).getLabel();  // optional
 * RawCollationKey collationKey = collator.getRawCollationKey(name, null);
 * </pre>
 *
 * <li>The client would put the name (and associated information) into its bucket for bucketIndex. The collationKey is a
 * sequence of bytes that can be compared with a binary compare, and produce the right localized result.</li>
 * </ul>
 *
 * @author Mark Davis
 */
public final class AlphabeticIndex<V> implements Iterable<Bucket<V>> {
    /**
     * Prefix string for Chinese index buckets.
     * See http://unicode.org/repos/cldr/trunk/specs/ldml/tr35-collation.html#Collation_Indexes
     */
    private static final String BASE = "\uFDD0";

    private static final char CGJ = '\u034F';

    private static final Comparator<String> binaryCmp = new UTF16.StringComparator(true, false, 0);

    private final RuleBasedCollator collatorOriginal;
    private final RuleBasedCollator collatorPrimaryOnly;
    private RuleBasedCollator collatorExternal;

    // Comparator for records, so that the Record class can be static.
    private final Comparator<Record<V>> recordComparator = new Comparator<Record<V>>() {
        @Override
        public int compare(Record<V> o1, Record<V> o2) {
            return collatorOriginal.compare(o1.name, o2.name);
        }
    };

    private final List<String> firstCharsInScripts;

    // We accumulate these as we build up the input parameters
    private final UnicodeSet initialLabels = new UnicodeSet();
    private List<Record<V>> inputList;

    // Lazy evaluated: null means that we have not built yet.
    private BucketList<V> buckets;

    private String overflowLabel = "\u2026";
    private String underflowLabel = "\u2026";
    private String inflowLabel = "\u2026";

    /**
     * Immutable, thread-safe version of {@link AlphabeticIndex}.
     * This class provides thread-safe methods for bucketing,
     * and random access to buckets and their properties,
     * but does not offer adding records to the index.
     *
     * @param <V> The Record value type is unused. It can be omitted for this class
     * if it was omitted for the AlphabeticIndex that built it.
     */
    public static final class ImmutableIndex<V> implements Iterable<Bucket<V>> {
        private final BucketList<V> buckets;
        private final Collator collatorPrimaryOnly;

        private ImmutableIndex(BucketList<V> bucketList, Collator collatorPrimaryOnly) {
            this.buckets = bucketList;
            this.collatorPrimaryOnly = collatorPrimaryOnly;
        }

        /**
         * Returns the number of index buckets and labels, including underflow/inflow/overflow.
         *
         * @return the number of index buckets
         */
        public int getBucketCount() {
            return buckets.getBucketCount();
        }

        /**
         * Finds the index bucket for the given name and returns the number of that bucket.
         * Use {@link #getBucket(int)} to get the bucket's properties.
         *
         * @param name the string to be sorted into an index bucket
         * @return the bucket number for the name
         */
        public int getBucketIndex(CharSequence name) {
            return buckets.getBucketIndex(name, collatorPrimaryOnly);
        }

        /**
         * Returns the index-th bucket. Returns null if the index is out of range.
         *
         * @param index bucket number
         * @return the index-th bucket
         */
        public Bucket<V> getBucket(int index) {
            if (0 <= index && index < buckets.getBucketCount()) {
                return buckets.immutableVisibleList.get(index);
            } else {
                return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Bucket<V>> iterator() {
            return buckets.iterator();
        }
    }

    /**
     * Create the index object.
     *
     * @param locale
     *            The locale for the index.
     */
    public AlphabeticIndex(ULocale locale) {
        this(locale, null);
    }

    /**
     * Create the index object.
     *
     * @param locale
     *            The locale for the index.
     */
    public AlphabeticIndex(Locale locale) {
        this(ULocale.forLocale(locale), null);
    }

    /**
     * Create an AlphabeticIndex that uses a specific collator.
     *
     * <p>The index will be created with no labels; the addLabels() function must be called
     * after creation to add the desired labels to the index.
     *
     * <p>The index will work directly with the supplied collator. If the caller will need to
     * continue working with the collator it should be cloned first, so that the
     * collator provided to the AlphabeticIndex remains unchanged after creation of the index.
     *
     * @param collator The collator to use to order the contents of this index.
     */
    public AlphabeticIndex(RuleBasedCollator collator) {
        this(null, collator);
    }

    /**
     * Internal constructor containing implementation used by public constructors.
     */
    private AlphabeticIndex(ULocale locale, RuleBasedCollator collator) {
        collatorOriginal = collator != null ? collator : (RuleBasedCollator) Collator.getInstance(locale);
        try {
            collatorPrimaryOnly = collatorOriginal.cloneAsThawed();
        } catch (Exception e) {
            // should never happen
            throw new IllegalStateException("Collator cannot be cloned", e);
        }
        collatorPrimaryOnly.setStrength(Collator.PRIMARY);
        collatorPrimaryOnly.freeze();

        firstCharsInScripts = getFirstCharactersInScripts();
        Collections.sort(firstCharsInScripts, collatorPrimaryOnly);
        // Guard against a degenerate collator where
        // some script boundary strings are primary ignorable.
        for (;;) {
            if (firstCharsInScripts.isEmpty()) {
                throw new IllegalArgumentException(
                        "AlphabeticIndex requires some non-ignorable script boundary strings");
            }
            if (collatorPrimaryOnly.compare(firstCharsInScripts.get(0), "") == 0) {
                firstCharsInScripts.remove(0);
            } else {
                break;
            }
        }

        // Chinese index characters, which are specific to each of the several Chinese tailorings,
        // take precedence over the single locale data exemplar set per language.
        if (!addChineseIndexCharacters() && locale != null) {
            addIndexExemplars(locale);
        }
    }

    /**
     * Add more index characters (aside from what are in the locale)
     * @param additions additional characters to add to the index, such as A-Z.
     * @return this, for chaining
     */
    public AlphabeticIndex<V> addLabels(UnicodeSet additions) {
        initialLabels.addAll(additions);
        buckets = null;
        return this;
    }

    /**
     * Add more index characters (aside from what are in the locale)
     * @param additions additional characters to add to the index, such as those in Swedish.
     * @return this, for chaining
     */
    public AlphabeticIndex<V> addLabels(ULocale... additions) {
        for (ULocale addition : additions) {
            addIndexExemplars(addition);
        }
        buckets = null;
        return this;
    }

    /**
     * Add more index characters (aside from what are in the locale)
     * @param additions additional characters to add to the index, such as those in Swedish.
     * @return this, for chaining
     */
    public AlphabeticIndex<V> addLabels(Locale... additions) {
        for (Locale addition : additions) {
            addIndexExemplars(ULocale.forLocale(addition));
        }
        buckets = null;
        return this;
    }

    /**
     * Set the overflow label
     * @param overflowLabel see class description
     * @return this, for chaining
     */
    public AlphabeticIndex<V> setOverflowLabel(String overflowLabel) {
        this.overflowLabel = overflowLabel;
        buckets = null;
        return this;
    }

    /**
     * Get the default label used in the IndexCharacters' locale for underflow, eg the last item in: X Y Z ...
     *
     * @return underflow label
     */
    public String getUnderflowLabel() {
        return underflowLabel; // TODO get localized version
    }


    /**
     * Set the underflowLabel label
     * @param underflowLabel see class description
     * @return this, for chaining
     */
    public AlphabeticIndex<V> setUnderflowLabel(String underflowLabel) {
        this.underflowLabel = underflowLabel;
        buckets = null;
        return this;
    }

    /**
     * Get the default label used in the IndexCharacters' locale for overflow, eg the first item in: ... A B C
     *
     * @return overflow label
     */
    public String getOverflowLabel() {
        return overflowLabel; // TODO get localized version
    }


    /**
     * Set the inflowLabel label
     * @param inflowLabel see class description
     * @return this, for chaining
     */
    public AlphabeticIndex<V> setInflowLabel(String inflowLabel) {
        this.inflowLabel = inflowLabel;
        buckets = null;
        return this;
    }

    /**
     * Get the default label used for abbreviated buckets <i>between</i> other labels. For example, consider the labels
     * for Latin and Greek are used: X Y Z ... &#x0391; &#x0392; &#x0393;.
     *
     * @return inflow label
     */
    public String getInflowLabel() {
        return inflowLabel; // TODO get localized version
    }


    /**
     * Get the limit on the number of labels in the index. The number of buckets can be slightly larger: see getBucketCount().
     *
     * @return maxLabelCount maximum number of labels.
     */
    public int getMaxLabelCount() {
        return maxLabelCount;
    }

    /**
     * Set a limit on the number of labels in the index. The number of buckets can be slightly larger: see
     * getBucketCount().
     *
     * @param maxLabelCount Set the maximum number of labels. Currently, if the number is exceeded, then every
     *         nth item is removed to bring the count down. A more sophisticated mechanism may be available in the
     *         future.
     * @return this, for chaining
     */
    public AlphabeticIndex<V> setMaxLabelCount(int maxLabelCount) {
        this.maxLabelCount = maxLabelCount;
        buckets = null;
        return this;
    }

    /**
     * Determine the best labels to use. This is based on the exemplars, but we also process to make sure that they are unique,
     * and sort differently, and that the overall list is small enough.
     */
    private List<String> initLabels() {
        Normalizer2 nfkdNormalizer = Normalizer2.getNFKDInstance();
        List<String> indexCharacters = new ArrayList<String>();

        String firstScriptBoundary = firstCharsInScripts.get(0);
        String overflowBoundary = firstCharsInScripts.get(firstCharsInScripts.size() - 1);

        // We make a sorted array of elements.
        // Some of the input may be redundant.
        // That is, we might have c, ch, d, where "ch" sorts just like "c", "h".
        // We filter out those cases.
        for (String item : initialLabels) {
            boolean checkDistinct;
            if (!UTF16.hasMoreCodePointsThan(item, 1)) {
                checkDistinct = false;
            } else if(item.charAt(item.length() - 1) == '*' &&
                    item.charAt(item.length() - 2) != '*') {
                // Use a label if it is marked with one trailing star,
                // even if the label string sorts the same when all contractions are suppressed.
                item = item.substring(0, item.length() - 1);
                checkDistinct = false;
            } else {
                checkDistinct = true;
            }
            if (collatorPrimaryOnly.compare(item, firstScriptBoundary) < 0) {
                // Ignore a primary-ignorable or non-alphabetic index character.
            } else if (collatorPrimaryOnly.compare(item, overflowBoundary) >= 0) {
                // Ignore an index character that will land in the overflow bucket.
            } else if (checkDistinct && collatorPrimaryOnly.compare(item, separated(item)) == 0) {
                // Ignore a multi-code point index character that does not sort distinctly
                // from the sequence of its separate characters.
            } else {
                int insertionPoint = Collections.binarySearch(indexCharacters, item, collatorPrimaryOnly);
                if (insertionPoint < 0) {
                    indexCharacters.add(~insertionPoint, item);
                } else {
                    String itemAlreadyIn = indexCharacters.get(insertionPoint);
                    if (isOneLabelBetterThanOther(nfkdNormalizer, item, itemAlreadyIn)) {
                        indexCharacters.set(insertionPoint, item);
                    }
                }
            }
        }

        // if the result is still too large, cut down to maxLabelCount elements, by removing every nth element

        final int size = indexCharacters.size() - 1;
        if (size > maxLabelCount) {
            int count = 0;
            int old = -1;
            for (Iterator<String> it = indexCharacters.iterator(); it.hasNext();) {
                ++count;
                it.next();
                final int bump = count * maxLabelCount / size;
                if (bump == old) {
                    it.remove();
                } else {
                    old = bump;
                }
            }
        }

        return indexCharacters;
    }

    private static String fixLabel(String current) {
        if (!current.startsWith(BASE)) {
            return current;
        }
        int rest = current.charAt(BASE.length());
        if (0x2800 < rest && rest <= 0x28FF) { // stroke count
            return (rest-0x2800) + "\u5283";
        }
        return current.substring(BASE.length());
    }

    /**
     * This method is called to get the index exemplars. Normally these come from the locale directly,
     * but if they aren't available, we have to synthesize them.
     */
    private void addIndexExemplars(ULocale locale) {
        UnicodeSet exemplars = LocaleData.getExemplarSet(locale, 0, LocaleData.ES_INDEX);
        if (exemplars != null) {
            initialLabels.addAll(exemplars);
            return;
        }

        // The locale data did not include explicit Index characters.
        // Synthesize a set of them from the locale's standard exemplar characters.
        exemplars = LocaleData.getExemplarSet(locale, 0, LocaleData.ES_STANDARD);

        exemplars = exemplars.cloneAsThawed();
        // question: should we add auxiliary exemplars?
        if (exemplars.containsSome('a', 'z') || exemplars.size() == 0) {
            exemplars.addAll('a', 'z');
        }
        if (exemplars.containsSome(0xAC00, 0xD7A3)) {  // Hangul syllables
            // cut down to small list
            exemplars.remove(0xAC00, 0xD7A3).
                add(0xAC00).add(0xB098).add(0xB2E4).add(0xB77C).
                add(0xB9C8).add(0xBC14).add(0xC0AC).add(0xC544).
                add(0xC790).add(0xCC28).add(0xCE74).add(0xD0C0).
                add(0xD30C).add(0xD558);
        }
        if (exemplars.containsSome(0x1200, 0x137F)) {  // Ethiopic block
            // cut down to small list
            // make use of the fact that Ethiopic is allocated in 8's, where
            // the base is 0 mod 8.
            UnicodeSet ethiopic = new UnicodeSet("[[:Block=Ethiopic:]&[:Script=Ethiopic:]]");
            UnicodeSetIterator it = new UnicodeSetIterator(ethiopic);
            while (it.next() && it.codepoint != UnicodeSetIterator.IS_STRING) {
                if ((it.codepoint & 0x7) != 0) {
                    exemplars.remove(it.codepoint);
                }
            }
        }

        // Upper-case any that aren't already so.
        //   (We only do this for synthesized index characters.)
        for (String item : exemplars) {
            initialLabels.add(UCharacter.toUpperCase(locale, item));
        }
    }

    /**
     * Add Chinese index characters from the tailoring.
     */
    private boolean addChineseIndexCharacters() {
        UnicodeSet contractions = new UnicodeSet();
        try {
            collatorPrimaryOnly.internalAddContractions(BASE.charAt(0), contractions);
        } catch (Exception e) {
            return false;
        }
        if (contractions.isEmpty()) { return false; }
        initialLabels.addAll(contractions);
        for (String s : contractions) {
            assert(s.startsWith(BASE));
            char c = s.charAt(s.length() - 1);
            if (0x41 <= c && c <= 0x5A) {  // A-Z
                // There are Pinyin labels, add ASCII A-Z labels as well.
                initialLabels.add(0x41, 0x5A);  // A-Z
                break;
            }
        }
        return true;
    }

    /**
     * Return the string with interspersed CGJs. Input must have more than 2 codepoints.
     * <p>This is used to test whether contractions sort differently from their components.
     */
    private String separated(String item) {
        StringBuilder result = new StringBuilder();
        // add a CGJ except within surrogates
        char last = item.charAt(0);
        result.append(last);
        for (int i = 1; i < item.length(); ++i) {
            char ch = item.charAt(i);
            if (!UCharacter.isHighSurrogate(last) || !UCharacter.isLowSurrogate(ch)) {
                result.append(CGJ);
            }
            result.append(ch);
            last = ch;
        }
        return result.toString();
    }

    /**
     * Builds an immutable, thread-safe version of this instance, without data records.
     *
     * @return an immutable index instance
     */
    public ImmutableIndex<V> buildImmutableIndex() {
        // The current AlphabeticIndex Java code never modifies the bucket list once built.
        // If it contains no records, we can use it.
        // addRecord() sets buckets=null rather than inserting the new record into it.
        BucketList<V> immutableBucketList;
        if (inputList != null && !inputList.isEmpty()) {
            // We need a bucket list with no records.
            immutableBucketList = createBucketList();
        } else {
            if (buckets == null) {
                buckets = createBucketList();
            }
            immutableBucketList = buckets;
        }
        return new ImmutableIndex<V>(immutableBucketList, collatorPrimaryOnly);
    }

    /**
     * Get the labels.
     *
     * @return The list of bucket labels, after processing.
     */
    public List<String> getBucketLabels() {
        initBuckets();
        ArrayList<String> result = new ArrayList<String>();
        for (Bucket<V> bucket : buckets) {
            result.add(bucket.getLabel());
        }
        return result;
    }

    /**
     * Get a clone of the collator used internally. Note that for performance reasons, the clone is only done once, and
     * then stored. The next time it is accessed, the same instance is returned.
     * <p>
     * <b><i>Don't use this method across threads if you are changing the settings on the collator, at least not without
     * synchronizing.</i></b>
     *
     * @return a clone of the collator used internally
     */
    public RuleBasedCollator getCollator() {
        if (collatorExternal == null) {
            try {
                collatorExternal = (RuleBasedCollator) (collatorOriginal.clone());
            } catch (Exception e) {
                // should never happen
                throw new IllegalStateException("Collator cannot be cloned", e);
            }
        }
        return collatorExternal;
    }

    /**
     * Add a record (name and data) to the index. The name will be used to sort the items into buckets, and to sort
     * within the bucket. Two records may have the same name. When they do, the sort order is according to the order added:
     * the first added comes first.
     *
     * @param name
     *            Name, such as a name
     * @param data
     *            Data, such as an address or link
     * @return this, for chaining
     */
    public AlphabeticIndex<V> addRecord(CharSequence name, V data) {
        // TODO instead of invalidating, just add to unprocessed list.
        buckets = null; // invalidate old bucketlist
        if (inputList == null) {
            inputList = new ArrayList<Record<V>>();
        }
        inputList.add(new Record<V>(name, data));
        return this;
    }

    /**
     * Get the bucket number for the given name. This routine permits callers to implement their own bucket handling
     * mechanisms, including client-server handling. For example, when a new name is created on the client, it can ask
     * the server for the bucket for that name, and the sortkey (using getCollator). Once the client has that
     * information, it can put the name into the right bucket, and sort it within that bucket, without having access to
     * the index or collator.
     * <p>
     * Note that the bucket number (and sort key) are only valid for the settings of the current AlphabeticIndex; if
     * those are changed, then the bucket number and sort key must be regenerated.
     *
     * @param name
     *            Name, such as a name
     * @return the bucket index for the name
     */
    public int getBucketIndex(CharSequence name) {
        initBuckets();
        return buckets.getBucketIndex(name, collatorPrimaryOnly);
    }

    /**
     * Clear the index.
     *
     * @return this, for chaining
     */
    public AlphabeticIndex<V> clearRecords() {
        if (inputList != null && !inputList.isEmpty()) {
            inputList.clear();
            buckets = null;
        }
        return this;
    }

    /**
     * Return the number of buckets in the index. This will be the same as the number of labels, plus buckets for the underflow, overflow, and inflow(s).
     *
     * @return number of buckets
     */
    public int getBucketCount() {
        initBuckets();
        return buckets.getBucketCount();
    }

    /**
     * Return the number of records in the index: that is, the total number of distinct &lt;name,data&gt; pairs added with addRecord(...), over all the buckets.
     *
     * @return total number of records in buckets
     */
    public int getRecordCount() {
        return inputList != null ? inputList.size() : 0;
    }

    /**
     * Return an iterator over the buckets.
     *
     * @return iterator over buckets.
     */
    @Override
    public Iterator<Bucket<V>> iterator() {
        initBuckets();
        return buckets.iterator();
    }

    /**
     * Creates an index, and buckets and sorts the list of records into the index.
     */
    private void initBuckets() {
        if (buckets != null) {
            return;
        }
        buckets = createBucketList();
        if (inputList == null || inputList.isEmpty()) {
            return;
        }

        // Sort the records by name.
        // Stable sort preserves input order of collation duplicates.
        Collections.sort(inputList, recordComparator);

        // Now, we traverse all of the input, which is now sorted.
        // If the item doesn't go in the current bucket, we find the next bucket that contains it.
        // This makes the process order n*log(n), since we just sort the list and then do a linear process.
        // However, if the user adds an item at a time and then gets the buckets, this isn't efficient, so
        // we need to improve it for that case.

        Iterator<Bucket<V>> bucketIterator = buckets.fullIterator();
        Bucket<V> currentBucket = bucketIterator.next();
        Bucket<V> nextBucket;
        String upperBoundary;
        if (bucketIterator.hasNext()) {
            nextBucket = bucketIterator.next();
            upperBoundary = nextBucket.lowerBoundary;
        } else {
            nextBucket = null;
            upperBoundary = null;
        }
        for (Record<V> r : inputList) {
            // if the current bucket isn't the right one, find the one that is
            // We have a special flag for the last bucket so that we don't look any further
            while (upperBoundary != null &&
                    collatorPrimaryOnly.compare(r.name, upperBoundary) >= 0) {
                currentBucket = nextBucket;
                // now reset the boundary that we compare against
                if (bucketIterator.hasNext()) {
                    nextBucket = bucketIterator.next();
                    upperBoundary = nextBucket.lowerBoundary;
                } else {
                    upperBoundary = null;
                }
            }
            // now put the record into the bucket.
            Bucket<V> bucket = currentBucket;
            if (bucket.displayBucket != null) {
                bucket = bucket.displayBucket;
            }
            if (bucket.records == null) {
                bucket.records = new ArrayList<Record<V>>();
            }
            bucket.records.add(r);
        }
    }

    private int maxLabelCount = 99;

    /**
     * Returns true if one index character string is "better" than the other.
     * Shorter NFKD is better, and otherwise NFKD-binary-less-than is
     * better, and otherwise binary-less-than is better.
     */
    private static boolean isOneLabelBetterThanOther(Normalizer2 nfkdNormalizer, String one, String other) {
        // This is called with primary-equal strings, but never with one.equals(other).
        String n1 = nfkdNormalizer.normalize(one);
        String n2 = nfkdNormalizer.normalize(other);
        int result = n1.codePointCount(0, n1.length()) - n2.codePointCount(0, n2.length());
        if (result != 0) {
            return result < 0;
        }
        result = binaryCmp.compare(n1, n2);
        if (result != 0) {
            return result < 0;
        }
        return binaryCmp.compare(one, other) < 0;
    }

    /**
     * A (name, data) pair, to be sorted by name into one of the index buckets.
     * The user data is not used by the index implementation.
     */
    public static class Record<V> {
        private final CharSequence name;
        private final V data;

        private Record(CharSequence name, V data) {
            this.name = name;
            this.data = data;
        }

        /**
         * Get the name
         *
         * @return the name
         */
        public CharSequence getName() {
            return name;
        }

        /**
         * Get the data
         *
         * @return the data
         */
        public V getData() {
            return data;
        }

        /**
         * Standard toString()
         */
        @Override
        public String toString() {
            return name + "=" + data;
        }
    }

    /**
     * An index "bucket" with a label string and type.
     * It is referenced by {@link AlphabeticIndex#getBucketIndex(CharSequence)}
     * and {@link AlphabeticIndex.ImmutableIndex#getBucketIndex(CharSequence)},
     * returned by {@link AlphabeticIndex.ImmutableIndex#getBucket(int)},
     * and {@link AlphabeticIndex#addRecord(CharSequence, Object)} adds a record
     * into a bucket according to the record's name.
     *
     * @param <V>
     *            Data type
     */
    public static class Bucket<V> implements Iterable<Record<V>> {
        private final String label;
        private final String lowerBoundary;
        private final LabelType labelType;
        private Bucket<V> displayBucket;
        private int displayIndex;
        private List<Record<V>> records;

        /**
         * Type of the label
         */
        public enum LabelType {
            /**
             * Normal
             */
            NORMAL,
            /**
             * Underflow (before the first)
             */
            UNDERFLOW,
            /**
             * Inflow (between scripts)
             */
            INFLOW,
            /**
             * Overflow (after the last)
             */
            OVERFLOW
        }

        /**
         * Set up the bucket.
         *
         * @param label
         *            label for the bucket
         * @param labelType
         *            is an underflow, overflow, or inflow bucket
         */
        private Bucket(String label, String lowerBoundary, LabelType labelType) {
            this.label = label;
            this.lowerBoundary = lowerBoundary;
            this.labelType = labelType;
        }

        /**
         * Get the label
         *
         * @return label for the bucket
         */
        public String getLabel() {
            return label;
        }

        /**
         * Is a normal, underflow, overflow, or inflow bucket
         *
         * @return is an underflow, overflow, or inflow bucket
         */
        public LabelType getLabelType() {
            return labelType;
        }

        /**
         * Get the number of records in the bucket.
         *
         * @return number of records in bucket
         */
        public int size() {
            return records == null ? 0 : records.size();
        }

        /**
         * Iterator over the records in the bucket
         */
        @Override
        public Iterator<Record<V>> iterator() {
            if (records == null) {
                return Collections.<Record<V>>emptyList().iterator();
            }
            return records.iterator();
        }

        /**
         * Standard toString()
         */
        @Override
        public String toString() {
            return "{" +
            "labelType=" + labelType
            + ", " +
            "lowerBoundary=" + lowerBoundary
            + ", " +
            "label=" + label
            + "}"
            ;
        }
    }

    private BucketList<V> createBucketList() {
        // Initialize indexCharacters.
        List<String> indexCharacters = initLabels();

        // Variables for hasMultiplePrimaryWeights().
        long variableTop;
        if (collatorPrimaryOnly.isAlternateHandlingShifted()) {
            variableTop = collatorPrimaryOnly.getVariableTop() & 0xffffffffL;
        } else {
            variableTop = 0;
        }
        boolean hasInvisibleBuckets = false;

        // Helper arrays for Chinese Pinyin collation.
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Bucket<V>[] asciiBuckets = new Bucket[26];
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Bucket<V>[] pinyinBuckets = new Bucket[26];
        boolean hasPinyin = false;

        ArrayList<Bucket<V>> bucketList = new ArrayList<Bucket<V>>();

        // underflow bucket
        bucketList.add(new Bucket<V>(getUnderflowLabel(), "", LabelType.UNDERFLOW));

        // fix up the list, adding underflow, additions, overflow
        // Insert inflow labels as needed.
        int scriptIndex = -1;
        String scriptUpperBoundary = "";
        for (String current : indexCharacters) {
            if (collatorPrimaryOnly.compare(current, scriptUpperBoundary) >= 0) {
                // We crossed the script boundary into a new script.
                String inflowBoundary = scriptUpperBoundary;
                boolean skippedScript = false;
                for (;;) {
                    scriptUpperBoundary = firstCharsInScripts.get(++scriptIndex);
                    if (collatorPrimaryOnly.compare(current, scriptUpperBoundary) < 0) {
                        break;
                    }
                    skippedScript = true;
                }
                if (skippedScript && bucketList.size() > 1) {
                    // We are skipping one or more scripts,
                    // and we are not just getting out of the underflow label.
                    bucketList.add(new Bucket<V>(getInflowLabel(), inflowBoundary,
                            LabelType.INFLOW));
                }
            }
            // Add a bucket with the current label.
            Bucket<V> bucket = new Bucket<V>(fixLabel(current), current, LabelType.NORMAL);
            bucketList.add(bucket);
            // Remember ASCII and Pinyin buckets for Pinyin redirects.
            char c;
            if (current.length() == 1 && 'A' <= (c = current.charAt(0)) && c <= 'Z') {
                asciiBuckets[c - 'A'] = bucket;
            } else if (current.length() == BASE.length() + 1 && current.startsWith(BASE) &&
                    'A' <= (c = current.charAt(BASE.length())) && c <= 'Z') {
                pinyinBuckets[c - 'A'] = bucket;
                hasPinyin = true;
            }
            // Check for multiple primary weights.
            if (!current.startsWith(BASE) &&
                    hasMultiplePrimaryWeights(collatorPrimaryOnly, variableTop, current) &&
                    !current.endsWith("\uffff")) {
                // "Æ" or "Sch" etc.
                for (int i = bucketList.size() - 2;; --i) {
                    Bucket<V> singleBucket = bucketList.get(i);
                    if (singleBucket.labelType != LabelType.NORMAL) {
                        // There is no single-character bucket since the last
                        // underflow or inflow label.
                        break;
                    }
                    if (singleBucket.displayBucket == null &&
                            !hasMultiplePrimaryWeights(collatorPrimaryOnly, variableTop, singleBucket.lowerBoundary)) {
                        // Add an invisible bucket that redirects strings greater than the expansion
                        // to the previous single-character bucket.
                        // For example, after ... Q R S Sch we add Sch\uFFFF->S
                        // and after ... Q R S Sch Sch\uFFFF St we add St\uFFFF->S.
                        bucket = new Bucket<V>("", current + "\uFFFF", LabelType.NORMAL);
                        bucket.displayBucket = singleBucket;
                        bucketList.add(bucket);
                        hasInvisibleBuckets = true;
                        break;
                    }
                }
            }
        }
        if (bucketList.size() == 1) {
            // No real labels, show only the underflow label.
            return new BucketList<V>(bucketList, bucketList);
        }
        // overflow bucket
        bucketList.add(new Bucket<V>(getOverflowLabel(), scriptUpperBoundary, LabelType.OVERFLOW)); // final

        if (hasPinyin) {
            // Redirect Pinyin buckets.
            Bucket<V> asciiBucket = null;
            for (int i = 0; i < 26; ++i) {
                if (asciiBuckets[i] != null) {
                    asciiBucket = asciiBuckets[i];
                }
                if (pinyinBuckets[i] != null && asciiBucket != null) {
                    pinyinBuckets[i].displayBucket = asciiBucket;
                    hasInvisibleBuckets = true;
                }
            }
        }

        if (!hasInvisibleBuckets) {
            return new BucketList<V>(bucketList, bucketList);
        }
        // Merge inflow buckets that are visually adjacent.
        // Iterate backwards: Merge inflow into overflow rather than the other way around.
        int i = bucketList.size() - 1;
        Bucket<V> nextBucket = bucketList.get(i);
        while (--i > 0) {
            Bucket<V> bucket = bucketList.get(i);
            if (bucket.displayBucket != null) {
                continue;  // skip invisible buckets
            }
            if (bucket.labelType == LabelType.INFLOW) {
                if (nextBucket.labelType != LabelType.NORMAL) {
                    bucket.displayBucket = nextBucket;
                    continue;
                }
            }
            nextBucket = bucket;
        }

        ArrayList<Bucket<V>> publicBucketList = new ArrayList<Bucket<V>>();
        for (Bucket<V> bucket : bucketList) {
            if (bucket.displayBucket == null) {
                publicBucketList.add(bucket);
            }
        }
        return new BucketList<V>(bucketList, publicBucketList);
    }

    private static class BucketList<V> implements Iterable<Bucket<V>> {
        private final ArrayList<Bucket<V>> bucketList;
        private final List<Bucket<V>> immutableVisibleList;

        private BucketList(ArrayList<Bucket<V>> bucketList, ArrayList<Bucket<V>> publicBucketList) {
            this.bucketList = bucketList;

            int displayIndex = 0;
            for (Bucket<V> bucket : publicBucketList) {
                bucket.displayIndex = displayIndex++;
            }
            immutableVisibleList = Collections.unmodifiableList(publicBucketList);
        }

        private int getBucketCount() {
            return immutableVisibleList.size();
        }

        private int getBucketIndex(CharSequence name, Collator collatorPrimaryOnly) {
            // binary search
            int start = 0;
            int limit = bucketList.size();
            while ((start + 1) < limit) {
                int i = (start + limit) / 2;
                Bucket<V> bucket = bucketList.get(i);
                int nameVsBucket = collatorPrimaryOnly.compare(name, bucket.lowerBoundary);
                if (nameVsBucket < 0) {
                    limit = i;
                } else {
                    start = i;
                }
            }
            Bucket<V> bucket = bucketList.get(start);
            if (bucket.displayBucket != null) {
                bucket = bucket.displayBucket;
            }
            return bucket.displayIndex;
        }

        /**
         * Private iterator over all the buckets, visible and invisible
         */
        private Iterator<Bucket<V>> fullIterator() {
            return bucketList.iterator();
        }

        /**
         * Iterator over just the visible buckets.
         */
        @Override
        public Iterator<Bucket<V>> iterator() {
            return immutableVisibleList.iterator(); // use immutable list to prevent remove().
        }
    }

    private static boolean hasMultiplePrimaryWeights(
            RuleBasedCollator coll, long variableTop, String s) {
        long[] ces = coll.internalGetCEs(s);
        boolean seenPrimary = false;
        for (int i = 0; i < ces.length; ++i) {
            long ce = ces[i];
            long p = ce >>> 32;
            if (p > variableTop) {
                // not primary ignorable
                if (seenPrimary) {
                    return true;
                }
                seenPrimary = true;
            }
        }
        return false;
    }

    // TODO: Surely we have at least a ticket for porting these mask values to UCharacter.java?!
    private static final int GC_LU_MASK = 1 << UCharacter.UPPERCASE_LETTER;
    private static final int GC_LL_MASK = 1 << UCharacter.LOWERCASE_LETTER;
    private static final int GC_LT_MASK = 1 << UCharacter.TITLECASE_LETTER;
    private static final int GC_LM_MASK = 1 << UCharacter.MODIFIER_LETTER;
    private static final int GC_LO_MASK = 1 << UCharacter.OTHER_LETTER;
    private static final int GC_L_MASK =
            GC_LU_MASK|GC_LL_MASK|GC_LT_MASK|GC_LM_MASK|GC_LO_MASK;
    private static final int GC_CN_MASK = 1 << UCharacter.GENERAL_OTHER_TYPES;

    /**
     * Return a list of the first character in each script. Only exposed for testing.
     *
     * @return list of first characters in each script
     * @deprecated This API is ICU internal, only for testing.
     * @hide original deprecated declaration
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    public List<String> getFirstCharactersInScripts() {
        List<String> dest = new ArrayList<String>(200);
        // Fetch the script-first-primary contractions which are defined in the root collator.
        // They all start with U+FDD1.
        UnicodeSet set = new UnicodeSet();
        collatorPrimaryOnly.internalAddContractions(0xFDD1, set);
        if (set.isEmpty()) {
            throw new UnsupportedOperationException(
                    "AlphabeticIndex requires script-first-primary contractions");
        }
        for (String boundary : set) {
            int gcMask = 1 << UCharacter.getType(boundary.codePointAt(1));
            if ((gcMask & (GC_L_MASK | GC_CN_MASK)) == 0) {
                // Ignore boundaries for the special reordering groups.
                // Take only those for "real scripts" (where the sample character is a Letter,
                // and the one for unassigned implicit weights (Cn).
                continue;
            }
            dest.add(boundary);
        }
        return dest;
    }
}
