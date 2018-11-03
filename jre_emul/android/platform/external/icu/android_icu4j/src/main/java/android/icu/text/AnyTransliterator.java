/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*****************************************************************
* Copyright (c) 2002-2014, International Business Machines Corporation
* and others.  All Rights Reserved.
*****************************************************************
* Date        Name        Description
* 06/06/2002  aliu        Creation.
*****************************************************************
*/
package android.icu.text;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import android.icu.lang.UScript;
/**
 * A transliterator that translates multiple input scripts to a single
 * output script.  It is named Any-T or Any-T/V, where T is the target
 * and V is the optional variant.  The target T is a script.
 *
 * <p>An AnyTransliterator partitions text into runs of the same
 * script, together with adjacent COMMON or INHERITED characters.
 * After determining the script of each run, it transliterates from
 * that script to the given target/variant.  It does so by
 * instantiating a transliterator from the source script to the
 * target/variant.  If a run consists only of the target script,
 * COMMON, or INHERITED characters, then the run is not changed.
 *
 * <p>At startup, all possible AnyTransliterators are registered with
 * the system, as determined by examining the registered script
 * transliterators.
 *
 * @author Alan Liu
 */
class AnyTransliterator extends Transliterator {

    //------------------------------------------------------------
    // Constants

    static final char TARGET_SEP = '-';
    static final char VARIANT_SEP = '/';
    static final String ANY = "Any";
    static final String NULL_ID = "Null";
    static final String LATIN_PIVOT = "-Latin;Latin-";

    /**
     * Cache mapping UScriptCode values to Transliterator*.
     */
    private ConcurrentHashMap<Integer, Transliterator> cache;

    /**
     * The target or target/variant string.
     */
    private String target;

    /**
     * The target script code.  Never USCRIPT_INVALID_CODE.
     */
    private int targetScript;

    /**
     * Special code for handling width characters
     */
    private Transliterator widthFix = Transliterator.getInstance("[[:dt=Nar:][:dt=Wide:]] nfkd");

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected void handleTransliterate(Replaceable text,
                                       Position pos, boolean isIncremental) {
        int allStart = pos.start;
        int allLimit = pos.limit;

        ScriptRunIterator it =
            new ScriptRunIterator(text, pos.contextStart, pos.contextLimit);

        while (it.next()) {
            // Ignore runs in the ante context
            if (it.limit <= allStart) continue;

            // Try to instantiate transliterator from it.scriptCode to
            // our target or target/variant
            Transliterator t = getTransliterator(it.scriptCode);

            if (t == null) {
                // We have no transliterator.  Do nothing, but keep
                // pos.start up to date.
                pos.start = it.limit;
                continue;
            }

            // If the run end is before the transliteration limit, do
            // a non-incremental transliteration.  Otherwise do an
            // incremental one.
            boolean incremental = isIncremental && (it.limit >= allLimit);

            pos.start = Math.max(allStart, it.start);
            pos.limit = Math.min(allLimit, it.limit);
            int limit = pos.limit;
            t.filteredTransliterate(text, pos, incremental);
            int delta = pos.limit - limit;
            allLimit += delta;
            it.adjustLimit(delta);

            // We're done if we enter the post context
            if (it.limit >= allLimit) break;
        }

        // Restore limit.  pos.start is fine where the last transliterator
        // left it, or at the end of the last run.
        pos.limit = allLimit;
    }

    /**
     * Private constructor
     * @param id the ID of the form S-T or S-T/V, where T is theTarget
     * and V is theVariant.  Must not be empty.
     * @param theTarget the target name.  Must not be empty, and must
     * name a script corresponding to theTargetScript.
     * @param theVariant the variant name, or the empty string if
     * there is no variant
     * @param theTargetScript the script code corresponding to
     * theTarget.
     */
    private AnyTransliterator(String id,
                              String theTarget,
                              String theVariant,
                              int theTargetScript) {
        super(id, null);
        targetScript = theTargetScript;
        cache = new ConcurrentHashMap<Integer, Transliterator>();

        target = theTarget;
        if (theVariant.length() > 0) {
            target = theTarget + VARIANT_SEP + theVariant;
        }
    }

    /**
     * @param id the ID of the form S-T or S-T/V, where T is theTarget
     * and V is theVariant.  Must not be empty.
     * @param filter The Unicode filter.
     * @param target2 the target name.
     * @param targetScript2 the script code corresponding to theTarget.
     * @param widthFix2 The Transliterator width fix.
     * @param cache2 The Map object for cache.
     */
    public AnyTransliterator(String id, UnicodeFilter filter, String target2,
            int targetScript2, Transliterator widthFix2, ConcurrentHashMap<Integer, Transliterator> cache2) {
        super(id, filter);
        targetScript = targetScript2;
        cache = cache2;
        target = target2;
    }

    /**
     * Returns a transliterator from the given source to our target or
     * target/variant.  Returns NULL if the source is the same as our
     * target script, or if the source is USCRIPT_INVALID_CODE.
     * Caches the result and returns the same transliterator the next
     * time.  The caller does NOT own the result and must not delete
     * it.
     */
    private Transliterator getTransliterator(int source) {
        if (source == targetScript || source == UScript.INVALID_CODE) {
            if (isWide(targetScript)) {
                return null;
            } else {
                return widthFix;
            }
        }

        Integer key = Integer.valueOf(source);
        Transliterator t = cache.get(key);
        if (t == null) {
            String sourceName = UScript.getName(source);
            String id = sourceName + TARGET_SEP + target;

            try {
                t = Transliterator.getInstance(id, FORWARD);
            } catch (RuntimeException e) { }
            if (t == null) {

                // Try to pivot around Latin, our most common script
                id = sourceName + LATIN_PIVOT + target;
                try {
                    t = Transliterator.getInstance(id, FORWARD);
                } catch (RuntimeException e) { }
            }

            if (t != null) {
                if (!isWide(targetScript)) {
                    List<Transliterator> v = new ArrayList<Transliterator>();
                    v.add(widthFix);
                    v.add(t);
                    t = new CompoundTransliterator(v);
                }
                Transliterator prevCachedT = cache.putIfAbsent(key, t);
                if (prevCachedT != null) {
                    t = prevCachedT;
                }
            } else if (!isWide(targetScript)) {
                return widthFix;
            }
        }

        return t;
    }

    /**
     * @param targetScript2
     * @return
     */
    private boolean isWide(int script) {
        return script == UScript.BOPOMOFO || script == UScript.HAN || script == UScript.HANGUL || script == UScript.HIRAGANA || script == UScript.KATAKANA;
    }

    /**
     * Registers standard transliterators with the system.  Called by
     * Transliterator during initialization.  Scan all current targets
     * and register those that are scripts T as Any-T/V.
     */
    static void register() {

        HashMap<String, Set<String>> seen = new HashMap<String, Set<String>>(); // old code used set, but was dependent on order

        for (Enumeration<String> s = Transliterator.getAvailableSources(); s.hasMoreElements(); ) {
            String source = s.nextElement();

            // Ignore the "Any" source
            if (source.equalsIgnoreCase(ANY)) continue;

            for (Enumeration<String> t = Transliterator.getAvailableTargets(source);
                 t.hasMoreElements(); ) {
                String target = t.nextElement();

                // Get the script code for the target.  If not a script, ignore.
                int targetScript = scriptNameToCode(target);
                if (targetScript == UScript.INVALID_CODE) {
                    continue;
                }

                Set<String> seenVariants = seen.get(target);
                if (seenVariants == null) {
                    seen.put(target, seenVariants = new HashSet<String>());
                }

                for (Enumeration<String> v = Transliterator.getAvailableVariants(source, target);
                     v.hasMoreElements(); ) {
                    String variant = v.nextElement();

                    // Only process each target/variant pair once
                    if (seenVariants.contains(variant)) {
                        continue;
                    }
                    seenVariants.add(variant);

                    String id;
                    id = TransliteratorIDParser.STVtoID(ANY, target, variant);
                    AnyTransliterator trans = new AnyTransliterator(id, target, variant,
                                                                    targetScript);
                    Transliterator.registerInstance(trans);
                    Transliterator.registerSpecialInverse(target, NULL_ID, false);
                }
            }
        }
    }

    /**
     * Return the script code for a given name, or
     * UScript.INVALID_CODE if not found.
     */
    private static int scriptNameToCode(String name) {
        try{
            int[] codes = UScript.getCode(name);
            return codes != null ? codes[0] : UScript.INVALID_CODE;
        }catch( MissingResourceException e){
            ///CLOVER:OFF
            return UScript.INVALID_CODE;
            ///CLOVER:ON
        }
    }

    //------------------------------------------------------------
    // ScriptRunIterator

    /**
     * Returns a series of ranges corresponding to scripts. They will be
     * of the form:
     *
     * ccccSScSSccccTTcTcccc   - c = common, S = first script, T = second
     * |            |          - first run (start, limit)
     *          |           |  - second run (start, limit)
     *
     * That is, the runs will overlap. The reason for this is so that a
     * transliterator can consider common characters both before and after
     * the scripts.
     */
    private static class ScriptRunIterator {

        private Replaceable text;
        private int textStart;
        private int textLimit;

        /**
         * The code of the current run, valid after next() returns.  May
         * be UScript.INVALID_CODE if and only if the entire text is
         * COMMON/INHERITED.
         */
        public int scriptCode;

        /**
         * The start of the run, inclusive, valid after next() returns.
         */
        public int start;

        /**
         * The end of the run, exclusive, valid after next() returns.
         */
        public int limit;

        /**
         * Constructs a run iterator over the given text from start
         * (inclusive) to limit (exclusive).
         */
        public ScriptRunIterator(Replaceable text, int start, int limit) {
            this.text = text;
            this.textStart = start;
            this.textLimit = limit;
            this.limit = start;
        }


        /**
         * Returns TRUE if there are any more runs.  TRUE is always
         * returned at least once.  Upon return, the caller should
         * examine scriptCode, start, and limit.
         */
        public boolean next() {
            int ch;
            int s;

            scriptCode = UScript.INVALID_CODE; // don't know script yet
            start = limit;

            // Are we done?
            if (start == textLimit) {
                return false;
            }

            // Move start back to include adjacent COMMON or INHERITED
            // characters
            while (start > textStart) {
                ch = text.char32At(start - 1); // look back
                s = UScript.getScript(ch);
                if (s == UScript.COMMON || s == UScript.INHERITED) {
                    --start;
                } else {
                    break;
                }
            }

            // Move limit ahead to include COMMON, INHERITED, and characters
            // of the current script.
            while (limit < textLimit) {
                ch = text.char32At(limit); // look ahead
                s = UScript.getScript(ch);
                if (s != UScript.COMMON && s != UScript.INHERITED) {
                    if (scriptCode == UScript.INVALID_CODE) {
                        scriptCode = s;
                    } else if (s != scriptCode) {
                        break;
                    }
                }
                ++limit;
            }

            // Return TRUE even if the entire text is COMMON / INHERITED, in
            // which case scriptCode will be UScript.INVALID_CODE.
            return true;
        }

        /**
         * Adjusts internal indices for a change in the limit index of the
         * given delta.  A positive delta means the limit has increased.
         */
        public void adjustLimit(int delta) {
            limit += delta;
            textLimit += delta;
        }
    }

    /**
     * Temporary hack for registry problem. Needs to be replaced by better architecture.
     */
    public Transliterator safeClone() {
        UnicodeFilter filter = getFilter();
        if (filter != null && filter instanceof UnicodeSet) {
            filter = new UnicodeSet((UnicodeSet)filter);
        }
        return new AnyTransliterator(getID(), filter, target, targetScript, widthFix, cache);
    }

    /* (non-Javadoc)
     * @see android.icu.text.Transliterator#addSourceTargetSet(android.icu.text.UnicodeSet, android.icu.text.UnicodeSet, android.icu.text.UnicodeSet)
     */
    @Override
    public void addSourceTargetSet(UnicodeSet inputFilter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = getFilterAsUnicodeSet(inputFilter);
        // Assume that it can modify any character to any other character
        sourceSet.addAll(myFilter);
        if (myFilter.size() != 0) {
            targetSet.addAll(0, 0x10FFFF);
        }
    }
}

