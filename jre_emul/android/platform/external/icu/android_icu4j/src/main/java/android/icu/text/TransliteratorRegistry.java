/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
**********************************************************************
*   Copyright (c) 2001-2016, International Business Machines
*   Corporation and others.  All Rights Reserved.
**********************************************************************
*   Date        Name        Description
*   08/19/2001  aliu        Creation.
**********************************************************************
*/

package android.icu.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.LocaleUtility;
import android.icu.impl.Utility;
import android.icu.lang.UScript;
import android.icu.text.RuleBasedTransliterator.Data;
import android.icu.util.CaseInsensitiveString;
import android.icu.util.UResourceBundle;

class TransliteratorRegistry {

    // char constants
    private static final char LOCALE_SEP  = '_';

    // String constants
    private static final String NO_VARIANT = ""; // empty string
    private static final String ANY = "Any";

    /**
     * Dynamic registry mapping full IDs to Entry objects.  This
     * contains both public and internal entities.  The visibility is
     * controlled by whether an entry is listed in availableIDs and
     * specDAG or not.
     *
     * Keys are CaseInsensitiveString objects.
     * Values are objects of class Class (subclass of Transliterator),
     * RuleBasedTransliterator.Data, Transliterator.Factory, or one
     * of the entry classes defined here (AliasEntry or ResourceEntry).
     */
    private Map<CaseInsensitiveString, Object[]> registry;

    /**
     * DAG of visible IDs by spec.  Hashtable: source => (Hashtable:
     * target => (Vector: variant)) The Vector of variants is never
     * empty.  For a source-target with no variant, the special
     * variant NO_VARIANT (the empty string) is stored in slot zero of
     * the UVector.
     *
     * Keys are CaseInsensitiveString objects.
     * Values are Hashtable of (CaseInsensitiveString -> Vector of
     * CaseInsensitiveString)
     */
    private Map<CaseInsensitiveString, Map<CaseInsensitiveString, List<CaseInsensitiveString>>> specDAG;

    /**
     * Vector of public full IDs (CaseInsensitiveString objects).
     */
    private List<CaseInsensitiveString> availableIDs;

    //----------------------------------------------------------------------
    // class Spec
    //----------------------------------------------------------------------

    /**
     * A Spec is a string specifying either a source or a target.  In more
     * general terms, it may also specify a variant, but we only use the
     * Spec class for sources and targets.
     *
     * A Spec may be a locale or a script.  If it is a locale, it has a
     * fallback chain that goes xx_YY_ZZZ -> xx_YY -> xx -> ssss, where
     * ssss is the script mapping of xx_YY_ZZZ.  The Spec API methods
     * hasFallback(), next(), and reset() iterate over this fallback
     * sequence.
     *
     * The Spec class canonicalizes itself, so the locale is put into
     * canonical form, or the script is transformed from an abbreviation
     * to a full name.
     */
    static class Spec {

        private String top;        // top spec
        private String spec;       // current spec
        private String nextSpec;   // next spec
        private String scriptName; // script name equivalent of top, if != top
        private boolean isSpecLocale; // TRUE if spec is a locale
        private boolean isNextLocale; // TRUE if nextSpec is a locale
        private ICUResourceBundle res;

        public Spec(String theSpec) {
            top = theSpec;
            spec = null;
            scriptName = null;
            try{
                // Canonicalize script name.  If top is a script name then
                // script != UScript.INVALID_CODE.
                int script = UScript.getCodeFromName(top);

                // Canonicalize script name -or- do locale->script mapping
                int[] s = UScript.getCode(top);
                if (s != null) {
                    scriptName = UScript.getName(s[0]);
                    // If the script name is the same as top then it's redundant
                    if (scriptName.equalsIgnoreCase(top)) {
                        scriptName = null;
                    }
                }

                isSpecLocale = false;
                res = null;
                // If 'top' is not a script name, try a locale lookup
                if (script == UScript.INVALID_CODE) {
                    Locale toploc = LocaleUtility.getLocaleFromName(top);
                    res  = (ICUResourceBundle)UResourceBundle.getBundleInstance(ICUData.ICU_TRANSLIT_BASE_NAME,toploc);
                    // Make sure we got the bundle we wanted; otherwise, don't use it
                    if (res!=null && LocaleUtility.isFallbackOf(res.getULocale().toString(), top)) {
                        isSpecLocale = true;
                    }
                }
            }catch(MissingResourceException e){
                ///CLOVER:OFF
                // The constructor is called from multiple private methods
                //  that protects an invalid scriptName
                scriptName = null;
                ///CLOVER:ON
            }
            // assert(spec != top);
            reset();
        }

        public boolean hasFallback() {
            return nextSpec != null;
        }

        public void reset() {
            if (!Utility.sameObjects(spec, top)) {
                spec = top;
                isSpecLocale = (res != null);
                setupNext();
            }
        }

        private void setupNext() {
            isNextLocale = false;
            if (isSpecLocale) {
                nextSpec = spec;
                int i = nextSpec.lastIndexOf(LOCALE_SEP);
                // If i == 0 then we have _FOO, so we fall through
                // to the scriptName.
                if (i > 0) {
                    nextSpec = spec.substring(0, i);
                    isNextLocale = true;
                } else {
                    nextSpec = scriptName; // scriptName may be null
                }
            } else {
                // Fallback to the script, which may be null
                if (!Utility.sameObjects(nextSpec, scriptName)) {
                    nextSpec = scriptName;
                } else {
                    nextSpec = null;
                }
            }
        }

        // Protocol:
        // for(String& s(spec.get());
        //     spec.hasFallback(); s(spec.next())) { ...

        public String next() {
            spec = nextSpec;
            isSpecLocale = isNextLocale;
            setupNext();
            return spec;
        }

        public String get() {
            return spec;
        }

        public boolean isLocale() {
            return isSpecLocale;
        }

        /**
         * Return the ResourceBundle for this spec, at the current
         * level of iteration.  The level of iteration goes from
         * aa_BB_CCC to aa_BB to aa.  If the bundle does not
         * correspond to the current level of iteration, return null.
         * If isLocale() is false, always return null.
         */
        public ResourceBundle getBundle() {
            if (res != null &&
                res.getULocale().toString().equals(spec)) {
                return res;
            }
            return null;
        }

        public String getTop() {
            return top;
        }
    }

    //----------------------------------------------------------------------
    // Entry classes
    //----------------------------------------------------------------------

    static class ResourceEntry {
        public String resource;
        public int direction;
        public ResourceEntry(String n, int d) {
            resource = n;
            direction = d;
        }
    }

    // An entry representing a rule in a locale resource bundle
    static class LocaleEntry {
        public String rule;
        public int direction;
        public LocaleEntry(String r, int d) {
            rule = r;
            direction = d;
        }
    }

    static class AliasEntry {
        public String alias;
        public AliasEntry(String a) {
            alias = a;
        }
    }

    static class CompoundRBTEntry {
        private String ID;
        private List<String> idBlockVector;
        private List<Data> dataVector;
        private UnicodeSet compoundFilter;

        public CompoundRBTEntry(String theID, List<String> theIDBlockVector,
                                List<Data> theDataVector,
                                UnicodeSet theCompoundFilter) {
            ID = theID;
            idBlockVector = theIDBlockVector;
            dataVector = theDataVector;
            compoundFilter = theCompoundFilter;
        }

        public Transliterator getInstance() {
            List<Transliterator> transliterators = new ArrayList<Transliterator>();
            int passNumber = 1;

            int limit = Math.max(idBlockVector.size(), dataVector.size());
            for (int i = 0; i < limit; i++) {
                if (i < idBlockVector.size()) {
                    String idBlock = idBlockVector.get(i);
                    if (idBlock.length() > 0)
                        transliterators.add(Transliterator.getInstance(idBlock));
                }
                if (i < dataVector.size()) {
                    Data data = dataVector.get(i);
                    transliterators.add(new RuleBasedTransliterator("%Pass" + passNumber++, data, null));
                }
            }

            Transliterator t = new CompoundTransliterator(transliterators, passNumber - 1);
            t.setID(ID);
            if (compoundFilter != null) {
                t.setFilter(compoundFilter);
            }
            return t;
        }
    }

    //----------------------------------------------------------------------
    // class TransliteratorRegistry: Basic public API
    //----------------------------------------------------------------------

    public TransliteratorRegistry() {
        registry = Collections.synchronizedMap(new HashMap<CaseInsensitiveString, Object[]>());
        specDAG = Collections.synchronizedMap(new HashMap<CaseInsensitiveString, Map<CaseInsensitiveString, List<CaseInsensitiveString>>>());
        availableIDs = new ArrayList<CaseInsensitiveString>();
    }

    /**
     * Given a simple ID (forward direction, no inline filter, not
     * compound) attempt to instantiate it from the registry.  Return
     * 0 on failure.
     *
     * Return a non-empty aliasReturn value if the ID points to an alias.
     * We cannot instantiate it ourselves because the alias may contain
     * filters or compounds, which we do not understand.  Caller should
     * make aliasReturn empty before calling.
     */
    public Transliterator get(String ID,
                              StringBuffer aliasReturn) {
        Object[] entry = find(ID);
        return (entry == null) ? null
            : instantiateEntry(ID, entry, aliasReturn);
    }

    /**
     * Register a class.  This adds an entry to the
     * dynamic store, or replaces an existing entry.  Any entry in the
     * underlying static locale resource store is masked.
     */
    public void put(String ID,
                    Class<? extends Transliterator> transliteratorSubclass,
                    boolean visible) {
        registerEntry(ID, transliteratorSubclass, visible);
    }

    /**
     * Register an ID and a factory function pointer.  This adds an
     * entry to the dynamic store, or replaces an existing entry.  Any
     * entry in the underlying static locale resource store is masked.
     */
    public void put(String ID,
                    Transliterator.Factory factory,
                    boolean visible) {
        registerEntry(ID, factory, visible);
    }

    /**
     * Register an ID and a resource name.  This adds an entry to the
     * dynamic store, or replaces an existing entry.  Any entry in the
     * underlying static locale resource store is masked.
     */
    public void put(String ID,
                    String resourceName,
                    int dir,
                    boolean visible) {
        registerEntry(ID, new ResourceEntry(resourceName, dir), visible);
    }

    /**
     * Register an ID and an alias ID.  This adds an entry to the
     * dynamic store, or replaces an existing entry.  Any entry in the
     * underlying static locale resource store is masked.
     */
    public void put(String ID,
                    String alias,
                    boolean visible) {
        registerEntry(ID, new AliasEntry(alias), visible);
    }

    /**
     * Register an ID and a Transliterator object.  This adds an entry
     * to the dynamic store, or replaces an existing entry.  Any entry
     * in the underlying static locale resource store is masked.
     */
    public void put(String ID,
                    Transliterator trans,
                    boolean visible) {
        registerEntry(ID, trans, visible);
    }

    /**
     * Unregister an ID.  This removes an entry from the dynamic store
     * if there is one.  The static locale resource store is
     * unaffected.
     */
    public void remove(String ID) {
        String[] stv = TransliteratorIDParser.IDtoSTV(ID);
        // Only need to do this if ID.indexOf('-') < 0
        String id = TransliteratorIDParser.STVtoID(stv[0], stv[1], stv[2]);
        registry.remove(new CaseInsensitiveString(id));
        removeSTV(stv[0], stv[1], stv[2]);
        availableIDs.remove(new CaseInsensitiveString(id));
    }

    //----------------------------------------------------------------------
    // class TransliteratorRegistry: Public ID and spec management
    //----------------------------------------------------------------------

    /**
     * An internal class that adapts an enumeration over
     * CaseInsensitiveStrings to an enumeration over Strings.
     */
    private static class IDEnumeration implements Enumeration<String> {
        Enumeration<CaseInsensitiveString> en;

        public IDEnumeration(Enumeration<CaseInsensitiveString> e) {
            en = e;
        }

        @Override
        public boolean hasMoreElements() {
            return en != null && en.hasMoreElements();
        }

        @Override
        public String nextElement() {
            return (en.nextElement()).getString();
        }
    }

    /**
     * Returns an enumeration over the programmatic names of visible
     * registered transliterators.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration<String> getAvailableIDs() {
        // Since the cache contains CaseInsensitiveString objects, but
        // the caller expects Strings, we have to use an intermediary.
        return new IDEnumeration(Collections.enumeration(availableIDs));
    }

    /**
     * Returns an enumeration over all visible source names.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration<String> getAvailableSources() {
        return new IDEnumeration(Collections.enumeration(specDAG.keySet()));
    }

    /**
     * Returns an enumeration over visible target names for the given
     * source.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration<String> getAvailableTargets(String source) {
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = specDAG.get(cisrc);
        if (targets == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(Collections.enumeration(targets.keySet()));
    }

    /**
     * Returns an enumeration over visible variant names for the given
     * source and target.
     *
     * @return An <code>Enumeration</code> over <code>String</code> objects
     */
    public Enumeration<String> getAvailableVariants(String source, String target) {
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = specDAG.get(cisrc);
        if (targets == null) {
            return new IDEnumeration(null);
        }
        List<CaseInsensitiveString> variants = targets.get(citrg);
        if (variants == null) {
            return new IDEnumeration(null);
        }
        return new IDEnumeration(Collections.enumeration(variants));
    }

    //----------------------------------------------------------------------
    // class TransliteratorRegistry: internal
    //----------------------------------------------------------------------

    /**
     * Convenience method.  Calls 6-arg registerEntry().
     */
    private void registerEntry(String source,
                               String target,
                               String variant,
                               Object entry,
                               boolean visible) {
        String s = source;
        if (s.length() == 0) {
            s = ANY;
        }
        String ID = TransliteratorIDParser.STVtoID(source, target, variant);
        registerEntry(ID, s, target, variant, entry, visible);
    }

    /**
     * Convenience method.  Calls 6-arg registerEntry().
     */
    private void registerEntry(String ID,
                               Object entry,
                               boolean visible) {
        String[] stv = TransliteratorIDParser.IDtoSTV(ID);
        // Only need to do this if ID.indexOf('-') < 0
        String id = TransliteratorIDParser.STVtoID(stv[0], stv[1], stv[2]);
        registerEntry(id, stv[0], stv[1], stv[2], entry, visible);
    }

    /**
     * Register an entry object (adopted) with the given ID, source,
     * target, and variant strings.
     */
    private void registerEntry(String ID,
                               String source,
                               String target,
                               String variant,
                               Object entry,
                               boolean visible) {
        CaseInsensitiveString ciID = new CaseInsensitiveString(ID);
        Object[] arrayOfObj;

        // Store the entry within an array so it can be modified later
        if (entry instanceof Object[]) {
            arrayOfObj = (Object[])entry;
        } else {
            arrayOfObj = new Object[] { entry };
        }

        registry.put(ciID, arrayOfObj);
        if (visible) {
            registerSTV(source, target, variant);
            if (!availableIDs.contains(ciID)) {
                availableIDs.add(ciID);
            }
        } else {
            removeSTV(source, target, variant);
            availableIDs.remove(ciID);
        }
    }

    /**
     * Register a source-target/variant in the specDAG.  Variant may be
     * empty, but source and target must not be.  If variant is empty then
     * the special variant NO_VARIANT is stored in slot zero of the
     * UVector of variants.
     */
    private void registerSTV(String source,
                             String target,
                             String variant) {
        // assert(source.length() > 0);
        // assert(target.length() > 0);
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        CaseInsensitiveString civar = new CaseInsensitiveString(variant);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = specDAG.get(cisrc);
        if (targets == null) {
            targets = Collections.synchronizedMap(new HashMap<CaseInsensitiveString, List<CaseInsensitiveString>>());
            specDAG.put(cisrc, targets);
        }
        List<CaseInsensitiveString> variants = targets.get(citrg);
        if (variants == null) {
            variants = new ArrayList<CaseInsensitiveString>();
            targets.put(citrg, variants);
        }
        // assert(NO_VARIANT == "");
        // We add the variant string.  If it is the special "no variant"
        // string, that is, the empty string, we add it at position zero.
        if (!variants.contains(civar)) {
            if (variant.length() > 0) {
                variants.add(civar);
            } else {
                variants.add(0, civar);
            }
        }
    }

    /**
     * Remove a source-target/variant from the specDAG.
     */
    private void removeSTV(String source,
                           String target,
                           String variant) {
        // assert(source.length() > 0);
        // assert(target.length() > 0);
        CaseInsensitiveString cisrc = new CaseInsensitiveString(source);
        CaseInsensitiveString citrg = new CaseInsensitiveString(target);
        CaseInsensitiveString civar = new CaseInsensitiveString(variant);
        Map<CaseInsensitiveString, List<CaseInsensitiveString>> targets = specDAG.get(cisrc);
        if (targets == null) {
            return; // should never happen for valid s-t/v
        }
        List<CaseInsensitiveString> variants = targets.get(citrg);
        if (variants == null) {
            return; // should never happen for valid s-t/v
        }
        variants.remove(civar);
        if (variants.size() == 0) {
            targets.remove(citrg); // should delete variants
            if (targets.size() == 0) {
                specDAG.remove(cisrc); // should delete targets
            }
        }
    }

    private static final boolean DEBUG = false;

    /**
     * Attempt to find a source-target/variant in the dynamic registry
     * store.  Return 0 on failure.
     */
    private Object[] findInDynamicStore(Spec src,
                                      Spec trg,
                                      String variant) {
        String ID = TransliteratorIDParser.STVtoID(src.get(), trg.get(), variant);
        ///CLOVER:OFF
        if (DEBUG) {
            System.out.println("TransliteratorRegistry.findInDynamicStore:" +
                               ID);
        }
        ///CLOVER:ON
        return registry.get(new CaseInsensitiveString(ID));
    }

    /**
     * Attempt to find a source-target/variant in the static locale
     * resource store.  Do not perform fallback.  Return 0 on failure.
     *
     * On success, create a new entry object, register it in the dynamic
     * store, and return a pointer to it, but do not make it public --
     * just because someone requested something, we do not expand the
     * available ID list (or spec DAG).
     */
    private Object[] findInStaticStore(Spec src,
                                     Spec trg,
                                     String variant) {
        ///CLOVER:OFF
        if (DEBUG) {
            String ID = TransliteratorIDParser.STVtoID(src.get(), trg.get(), variant);
            System.out.println("TransliteratorRegistry.findInStaticStore:" +
                               ID);
        }
        ///CLOVER:ON
        Object[] entry = null;
        if (src.isLocale()) {
            entry = findInBundle(src, trg, variant, Transliterator.FORWARD);
        } else if (trg.isLocale()) {
            entry = findInBundle(trg, src, variant, Transliterator.REVERSE);
        }

        // If we found an entry, store it in the Hashtable for next
        // time.
        if (entry != null) {
            registerEntry(src.getTop(), trg.getTop(), variant, entry, false);
        }

        return entry;
    }

    /**
     * Attempt to find an entry in a single resource bundle.  This is
     * a one-sided lookup.  findInStaticStore() performs up to two such
     * lookups, one for the source, and one for the target.
     *
     * Do not perform fallback.  Return 0 on failure.
     *
     * On success, create a new Entry object, populate it, and return it.
     * The caller owns the returned object.
     */
    private Object[] findInBundle(Spec specToOpen,
                                  Spec specToFind,
                                  String variant,
                                  int direction) {
        // assert(specToOpen.isLocale());
        ResourceBundle res = specToOpen.getBundle();

        if (res == null) {
            // This means that the bundle's locale does not match
            // the current level of iteration for the spec.
            return null;
        }

        for (int pass=0; pass<2; ++pass) {
            StringBuilder tag = new StringBuilder();
            // First try either TransliteratorTo_xxx or
            // TransliterateFrom_xxx, then try the bidirectional
            // Transliterate_xxx.  This precedence order is arbitrary
            // but must be consistent and documented.
            if (pass == 0) {
                tag.append(direction == Transliterator.FORWARD ?
                           "TransliterateTo" : "TransliterateFrom");
            } else {
                tag.append("Transliterate");
            }
            tag.append(specToFind.get().toUpperCase(Locale.ENGLISH));

            try {
                // The Transliterate*_xxx resource is an array of
                // strings of the format { <v0>, <r0>, ... }.  Each
                // <vi> is a variant name, and each <ri> is a rule.
                String[] subres = res.getStringArray(tag.toString());

                // assert(subres != null);
                // assert(subres.length % 2 == 0);
                int i = 0;
                if (variant.length() != 0) {
                    for (i=0; i<subres.length; i+= 2) {
                        if (subres[i].equalsIgnoreCase(variant)) {
                            break;
                        }
                    }
                }

                if (i < subres.length) {
                    // We have a match, or there is no variant and i == 0.
                    // We have succeeded in loading a string from the
                    // locale resources.  Return the rule string which
                    // will itself become the registry entry.

                    // The direction is always forward for the
                    // TransliterateTo_xxx and TransliterateFrom_xxx
                    // items; those are unidirectional forward rules.
                    // For the bidirectional Transliterate_xxx items,
                    // the direction is the value passed in to this
                    // function.
                    int dir = (pass == 0) ? Transliterator.FORWARD : direction;
                    return new Object[] { new LocaleEntry(subres[i+1], dir) };
                }

            } catch (MissingResourceException e) {
                ///CLOVER:OFF
                if (DEBUG) System.out.println("missing resource: " + e);
                ///CLOVER:ON
            }
        }

        // If we get here we had a missing resource exception or we
        // failed to find a desired variant.
        return null;
    }

    /**
     * Convenience method.  Calls 3-arg find().
     */
    private Object[] find(String ID) {
        String[] stv = TransliteratorIDParser.IDtoSTV(ID);
        return find(stv[0], stv[1], stv[2]);
    }

    /**
     * Top-level find method.  Attempt to find a source-target/variant in
     * either the dynamic or the static (locale resource) store.  Perform
     * fallback.
     *
     * Lookup sequence for ss_SS_SSS-tt_TT_TTT/v:
     *
     *   ss_SS_SSS-tt_TT_TTT/v -- in hashtable
     *   ss_SS_SSS-tt_TT_TTT/v -- in ss_SS_SSS (no fallback)
     *
     *     repeat with t = tt_TT_TTT, tt_TT, tt, and tscript
     *
     *     ss_SS_SSS-t/*
     *     ss_SS-t/*
     *     ss-t/*
     *     sscript-t/*
     *
     * Here * matches the first variant listed.
     *
     * Caller does NOT own returned object.  Return 0 on failure.
     */
    private Object[] find(String source,
                          String target,
                          String variant) {

        Spec src = new Spec(source);
        Spec trg = new Spec(target);
        Object[] entry = null;

        if (variant.length() != 0) {

            // Seek exact match in hashtable
            entry = findInDynamicStore(src, trg, variant);
            if (entry != null) {
                return entry;
            }

            // Seek exact match in locale resources
            entry = findInStaticStore(src, trg, variant);
            if (entry != null) {
                return entry;
            }
        }

        for (;;) {
            src.reset();
            for (;;) {
                // Seek match in hashtable
                entry = findInDynamicStore(src, trg, NO_VARIANT);
                if (entry != null) {
                    return entry;
                }

                // Seek match in locale resources
                entry = findInStaticStore(src, trg, NO_VARIANT);
                if (entry != null) {
                    return entry;
                }
                if (!src.hasFallback()) {
                    break;
                }
                src.next();
            }
            if (!trg.hasFallback()) {
                break;
            }
            trg.next();
        }

        return null;
    }

    /**
     * Given an Entry object, instantiate it.  Caller owns result.  Return
     * 0 on failure.
     *
     * Return a non-empty aliasReturn value if the ID points to an alias.
     * We cannot instantiate it ourselves because the alias may contain
     * filters or compounds, which we do not understand.  Caller should
     * make aliasReturn empty before calling.
     *
     * The entry object is assumed to reside in the dynamic store.  It may be
     * modified.
     */
    @SuppressWarnings("rawtypes")
    private Transliterator instantiateEntry(String ID,
                                            Object[] entryWrapper,
                                            StringBuffer aliasReturn) {
        // We actually modify the entry object in some cases.  If it
        // is a string, we may partially parse it and turn it into a
        // more processed precursor.  This makes the next
        // instantiation faster and allows sharing of immutable
        // components like the RuleBasedTransliterator.Data objects.
        // For this reason, the entry object is an Object[] of length
        // 1.

        for (;;) {
            Object entry = entryWrapper[0];

            if (entry instanceof RuleBasedTransliterator.Data) {
                RuleBasedTransliterator.Data data = (RuleBasedTransliterator.Data) entry;
                return new RuleBasedTransliterator(ID, data, null);
            } else if (entry instanceof Class) {
                try {
                    return (Transliterator) ((Class) entry).newInstance();
                } catch (InstantiationException e) {
                } catch (IllegalAccessException e2) {}
                return null;
            } else if (entry instanceof AliasEntry) {
                aliasReturn.append(((AliasEntry) entry).alias);
                return null;
            } else if (entry instanceof Transliterator.Factory) {
                return ((Transliterator.Factory) entry).getInstance(ID);
            } else if (entry instanceof CompoundRBTEntry) {
                return ((CompoundRBTEntry) entry).getInstance();
            } else if (entry instanceof AnyTransliterator) {
                AnyTransliterator temp = (AnyTransliterator) entry;
                return temp.safeClone();
            } else if (entry instanceof RuleBasedTransliterator) {
                RuleBasedTransliterator temp = (RuleBasedTransliterator) entry;
                return temp.safeClone();
            } else if (entry instanceof CompoundTransliterator) {
                CompoundTransliterator temp = (CompoundTransliterator) entry;
                return temp.safeClone();
            } else if (entry instanceof Transliterator) {
                return (Transliterator) entry;
            }

            // At this point entry type must be either RULES_FORWARD or
            // RULES_REVERSE.  We process the rule data into a
            // TransliteratorRuleData object, and possibly also into an
            // .id header and/or footer.  Then we modify the registry with
            // the parsed data and retry.

            TransliteratorParser parser = new TransliteratorParser();

            try {

                ResourceEntry re = (ResourceEntry) entry;
                parser.parse(re.resource, re.direction);

            } catch (ClassCastException e) {
                // If we pull a rule from a locale resource bundle it will
                // be a LocaleEntry.
                LocaleEntry le = (LocaleEntry) entry;
                parser.parse(le.rule, le.direction);
            }

            // Reset entry to something that we process at the
            // top of the loop, then loop back to the top.  As long as we
            // do this, we only loop through twice at most.
            // NOTE: The logic here matches that in
            // Transliterator.createFromRules().
            if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 0) {
                // No idBlock, no data -- this is just an
                // alias for Null
                entryWrapper[0] = new AliasEntry(NullTransliterator._ID);
            }
            else if (parser.idBlockVector.size() == 0 && parser.dataVector.size() == 1) {
                // No idBlock, data != 0 -- this is an
                // ordinary RBT_DATA
                entryWrapper[0] = parser.dataVector.get(0);
            }
            else if (parser.idBlockVector.size() == 1 && parser.dataVector.size() == 0) {
                // idBlock, no data -- this is an alias.  The ID has
                // been munged from reverse into forward mode, if
                // necessary, so instantiate the ID in the forward
                // direction.
                if (parser.compoundFilter != null) {
                    entryWrapper[0] = new AliasEntry(parser.compoundFilter.toPattern(false) + ";"
                            + parser.idBlockVector.get(0));
                } else {
                    entryWrapper[0] = new AliasEntry(parser.idBlockVector.get(0));
                }
            }
            else {
                entryWrapper[0] = new CompoundRBTEntry(ID, parser.idBlockVector, parser.dataVector,
                        parser.compoundFilter);
            }
        }
    }
}

//eof
