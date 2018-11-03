/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import java.util.List;

import android.icu.impl.Utility;
import android.icu.impl.UtilityExtensions;

/**
 * A transliterator that is composed of two or more other
 * transliterator objects linked together.  For example, if one
 * transliterator transliterates from script A to script B, and
 * another transliterates from script B to script C, the two may be
 * combined to form a new transliterator from A to C.
 *
 * <p>Composed transliterators may not behave as expected.  For
 * example, inverses may not combine to form the identity
 * transliterator.  See the class documentation for {@link
 * Transliterator} for details.
 *
 * <p>Copyright &copy; IBM Corporation 1999.  All rights reserved.
 *
 * @author Alan Liu
 */
class CompoundTransliterator extends Transliterator {

    private Transliterator[] trans;

    private int numAnonymousRBTs = 0;

    /**
     * Constructs a new compound transliterator given an array of
     * transliterators.  The array of transliterators may be of any
     * length, including zero or one, however, useful compound
     * transliterators have at least two components.
     * @param transliterators array of <code>Transliterator</code>
     * objects
     * @param filter the filter.  Any character for which
     * <tt>filter.contains()</tt> returns <tt>false</tt> will not be
     * altered by this transliterator.  If <tt>filter</tt> is
     * <tt>null</tt> then no filtering is applied.
     */
    /*public CompoundTransliterator(Transliterator[] transliterators,
                                  UnicodeFilter filter) {
        super(joinIDs(transliterators), filter);
        trans = new Transliterator[transliterators.length];
        System.arraycopy(transliterators, 0, trans, 0, trans.length);
        computeMaximumContextLength();
    }*/

    /**
     * Constructs a new compound transliterator given an array of
     * transliterators.  The array of transliterators may be of any
     * length, including zero or one, however, useful compound
     * transliterators have at least two components.
     * @param transliterators array of <code>Transliterator</code>
     * objects
     */
    /*public CompoundTransliterator(Transliterator[] transliterators) {
        this(transliterators, null);
    }*/

    /**
     * Constructs a new compound transliterator.
     * @param ID compound ID
     * @param direction either Transliterator.FORWARD or Transliterator.REVERSE
     * @param filter a global filter for this compound transliterator
     * or null
     */
    /*public CompoundTransliterator(String ID, int direction,
                                  UnicodeFilter filter) {
        super(ID, filter);
        init(ID, direction, true);
    }*/

    /**
     * Constructs a new compound transliterator with no filter.
     * @param ID compound ID
     * @param direction either Transliterator.FORWARD or Transliterator.REVERSE
     */
    /*public CompoundTransliterator(String ID, int direction) {
        this(ID, direction, null);
    }*/

    /**
     * Constructs a new forward compound transliterator with no filter.
     * @param ID compound ID
     */
    /*public CompoundTransliterator(String ID) {
        this(ID, FORWARD, null);
    }*/

    /**
     * Package private constructor for Transliterator from a vector of
     * transliterators.  The caller is responsible for fixing up the
     * ID.
     */
    CompoundTransliterator(List<Transliterator> list) {
        this(list, 0);
    }

    CompoundTransliterator(List<Transliterator> list, int numAnonymousRBTs) {
        super("", null);
        trans = null;
        init(list, FORWARD, false);
        this.numAnonymousRBTs = numAnonymousRBTs;
        // assume caller will fixup ID
    }

    /**
     * Internal method for safeClone...
     * @param id
     * @param filter2
     * @param trans2
     * @param numAnonymousRBTs2
     */
    CompoundTransliterator(String id, UnicodeFilter filter2, Transliterator[] trans2, int numAnonymousRBTs2) {
        super(id, filter2);
        trans = trans2;
        numAnonymousRBTs = numAnonymousRBTs2;
    }

    /**
     * Finish constructing a transliterator: only to be called by
     * constructors.  Before calling init(), set trans and filter to NULL.
     * @param id the id containing ';'-separated entries
     * @param direction either FORWARD or REVERSE
     * @param idSplitPoint the index into id at which the
     * splitTrans should be inserted, if there is one, or
     * -1 if there is none.
     * @param splitTrans a transliterator to be inserted
     * before the entry at offset idSplitPoint in the id string.  May be
     * NULL to insert no entry.
     * @param fixReverseID if TRUE, then reconstruct the ID of reverse
     * entries by calling getID() of component entries.  Some constructors
     * do not require this because they apply a facade ID anyway.
     */
    /*private void init(String id,
                      int direction,
                      boolean fixReverseID) {
        // assert(trans == 0);

        Vector list = new Vector();
        UnicodeSet[] compoundFilter = new UnicodeSet[1];
        StringBuffer regenID = new StringBuffer();
        if (!TransliteratorIDParser.parseCompoundID(id, direction,
                 regenID, list, compoundFilter)) {
            throw new IllegalArgumentException("Invalid ID " + id);
        }

        TransliteratorIDParser.instantiateList(list);

        init(list, direction, fixReverseID);

        if (compoundFilter[0] != null) {
            setFilter(compoundFilter[0]);
        }
    }*/


    /**
     * Finish constructing a transliterator: only to be called by
     * constructors.  Before calling init(), set trans and filter to NULL.
     * @param list a vector of transliterator objects to be adopted.  It
     * should NOT be empty.  The list should be in declared order.  That
     * is, it should be in the FORWARD order; if direction is REVERSE then
     * the list order will be reversed.
     * @param direction either FORWARD or REVERSE
     * @param fixReverseID if TRUE, then reconstruct the ID of reverse
     * entries by calling getID() of component entries.  Some constructors
     * do not require this because they apply a facade ID anyway.
     */
    private void init(List<Transliterator> list,
                      int direction,
                      boolean fixReverseID) {
        // assert(trans == 0);

        // Allocate array
        int count = list.size();
        trans = new Transliterator[count];

        // Move the transliterators from the vector into an array.
        // Reverse the order if necessary.
        int i;
        for (i=0; i<count; ++i) {
            int j = (direction == FORWARD) ? i : count - 1 - i;
            trans[i] = list.get(j);
        }

        // If the direction is UTRANS_REVERSE then we may need to fix the
        // ID.
        if (direction == REVERSE && fixReverseID) {
            StringBuilder newID = new StringBuilder();
            for (i=0; i<count; ++i) {
                if (i > 0) {
                    newID.append(ID_DELIM);
                }
                newID.append(trans[i].getID());
            }
            setID(newID.toString());
        }

        computeMaximumContextLength();
    }

    /**
     * Return the IDs of the given list of transliterators, concatenated
     * with ';' delimiting them.  Equivalent to the perlish expression
     * join(';', map($_.getID(), transliterators).
     */
    /*private static String joinIDs(Transliterator[] transliterators) {
        StringBuffer id = new StringBuffer();
        for (int i=0; i<transliterators.length; ++i) {
            if (i > 0) {
                id.append(';');
            }
            id.append(transliterators[i].getID());
        }
        return id.toString();
    }*/

    /**
     * Returns the number of transliterators in this chain.
     * @return number of transliterators in this chain.
     */
    public int getCount() {
        return trans.length;
    }

    /**
     * Returns the transliterator at the given index in this chain.
     * @param index index into chain, from 0 to <code>getCount() - 1</code>
     * @return transliterator at the given index
     */
    public Transliterator getTransliterator(int index) {
        return trans[index];
    }

    /**
     * Append c to buf, unless buf is empty or buf already ends in c.
     */
    private static void _smartAppend(StringBuilder buf, char c) {
        if (buf.length() != 0 &&
            buf.charAt(buf.length() - 1) != c) {
            buf.append(c);
        }
    }

    /**
     * Override Transliterator:
     * Create a rule string that can be passed to createFromRules()
     * to recreate this transliterator.
     * @param escapeUnprintable if TRUE then convert unprintable
     * character to their hex escape representations, \\uxxxx or
     * \\Uxxxxxxxx.  Unprintable characters are those other than
     * U+000A, U+0020..U+007E.
     * @return the rule string
     */
    @Override
    public String toRules(boolean escapeUnprintable) {
        // We do NOT call toRules() on our component transliterators, in
        // general.  If we have several rule-based transliterators, this
        // yields a concatenation of the rules -- not what we want.  We do
        // handle compound RBT transliterators specially -- those for which
        // compoundRBTIndex >= 0.  For the transliterator at compoundRBTIndex,
        // we do call toRules() recursively.
        StringBuilder rulesSource = new StringBuilder();
        if (numAnonymousRBTs >= 1 && getFilter() != null) {
            // If we are a compound RBT and if we have a global
            // filter, then emit it at the top.
            rulesSource.append("::").append(getFilter().toPattern(escapeUnprintable)).append(ID_DELIM);
        }
        for (int i=0; i<trans.length; ++i) {
            String rule;

            // Anonymous RuleBasedTransliterators (inline rules and
            // ::BEGIN/::END blocks) are given IDs that begin with
            // "%Pass": use toRules() to write all the rules to the output
            // (and insert "::Null;" if we have two in a row)
            if (trans[i].getID().startsWith("%Pass")) {
                rule = trans[i].toRules(escapeUnprintable);
                if (numAnonymousRBTs > 1 && i > 0 && trans[i - 1].getID().startsWith("%Pass"))
                    rule = "::Null;" + rule;

            // we also use toRules() on CompoundTransliterators (which we
            // check for by looking for a semicolon in the ID)-- this gets
            // the list of their child transliterators output in the right
            // format
            } else if (trans[i].getID().indexOf(';') >= 0) {
                rule = trans[i].toRules(escapeUnprintable);

            // for everything else, use baseToRules()
            } else {
                rule = trans[i].baseToRules(escapeUnprintable);
            }
            _smartAppend(rulesSource, '\n');
            rulesSource.append(rule);
            _smartAppend(rulesSource, ID_DELIM);
        }
        return rulesSource.toString();
    }

    /**
     * @hide draft / provisional / internal are hidden on Android
     */
    @Override
    public void addSourceTargetSet(UnicodeSet filter, UnicodeSet sourceSet, UnicodeSet targetSet) {
        UnicodeSet myFilter = new UnicodeSet(getFilterAsUnicodeSet(filter));
        UnicodeSet tempTargetSet = new UnicodeSet();
        for (int i=0; i<trans.length; ++i) {
            // each time we produce targets, those can be used by subsequent items, despite the filter.
            // so we get just those items, and add them to the filter each time.
            tempTargetSet.clear();
            trans[i].addSourceTargetSet(myFilter, sourceSet, tempTargetSet);
            targetSet.addAll(tempTargetSet);
            myFilter.addAll(tempTargetSet);
        }
    }

//    /**
//     * Returns the set of all characters that may be generated as
//     * replacement text by this transliterator.
//     */
//    public UnicodeSet getTargetSet() {
//        UnicodeSet set = new UnicodeSet();
//        for (int i=0; i<trans.length; ++i) {
//            // This is a heuristic, and not 100% reliable.
//            set.addAll(trans[i].getTargetSet());
//        }
//        return set;
//    }

    /**
     * Implements {@link Transliterator#handleTransliterate}.
     */
    @Override
    protected void handleTransliterate(Replaceable text,
                                       Position index, boolean incremental) {
        /* Call each transliterator with the same start value and
         * initial cursor index, but with the limit index as modified
         * by preceding transliterators.  The cursor index must be
         * reset for each transliterator to give each a chance to
         * transliterate the text.  The initial cursor index is known
         * to still point to the same place after each transliterator
         * is called because each transliterator will not change the
         * text between start and the initial value of cursor.
         *
         * IMPORTANT: After the first transliterator, each subsequent
         * transliterator only gets to transliterate text committed by
         * preceding transliterators; that is, the cursor (output
         * value) of transliterator i becomes the limit (input value)
         * of transliterator i+1.  Finally, the overall limit is fixed
         * up before we return.
         *
         * Assumptions we make here:
         * (1) contextStart <= start <= limit <= contextLimit <= text.length()
         * (2) start <= start' <= limit'  ;cursor doesn't move back
         * (3) start <= limit'            ;text before cursor unchanged
         * - start' is the value of start after calling handleKT
         * - limit' is the value of limit after calling handleKT
         */

        /**
         * Example: 3 transliterators.  This example illustrates the
         * mechanics we need to implement.  C, S, and L are the contextStart,
         * start, and limit.  gl is the globalLimit.  contextLimit is
         * equal to limit throughout.
         *
         * 1. h-u, changes hex to Unicode
         *
         *    4  7  a  d  0      4  7  a
         *    abc/u0061/u    =>  abca/u
         *    C  S       L       C   S L   gl=f->a
         *
         * 2. upup, changes "x" to "XX"
         *
         *    4  7  a       4  7  a
         *    abca/u    =>  abcAA/u
         *    C  SL         C    S
         *                       L    gl=a->b
         * 3. u-h, changes Unicode to hex
         *
         *    4  7  a        4  7  a  d  0  3
         *    abcAA/u    =>  abc/u0041/u0041/u
         *    C  S L         C              S
         *                                  L   gl=b->15
         * 4. return
         *
         *    4  7  a  d  0  3
         *    abc/u0041/u0041/u
         *    C S L
         */

        if (trans.length < 1) {
            index.start = index.limit;
            return; // Short circuit for empty compound transliterators
        }

        // compoundLimit is the limit value for the entire compound
        // operation.  We overwrite index.limit with the previous
        // index.start.  After each transliteration, we update
        // compoundLimit for insertions or deletions that have happened.
        int compoundLimit = index.limit;

        // compoundStart is the start for the entire compound
        // operation.
        int compoundStart = index.start;

        int delta = 0; // delta in length

        StringBuffer log = null;
        ///CLOVER:OFF
        if (DEBUG) {
            log = new StringBuffer("CompoundTransliterator{" + getID() +
                                   (incremental ? "}i: IN=" : "}: IN="));
            UtilityExtensions.formatInput(log, text, index);
            System.out.println(Utility.escape(log.toString()));
        }
        ///CLOVER:ON

        // Give each transliterator a crack at the run of characters.
        // See comments at the top of the method for more detail.
        for (int i=0; i<trans.length; ++i) {
            index.start = compoundStart; // Reset start
            int limit = index.limit;

            if (index.start == index.limit) {
                // Short circuit for empty range
                ///CLOVER:OFF
                if (DEBUG) {
                    System.out.println("CompoundTransliterator[" + i +
                                       ".." + (trans.length-1) +
                                       (incremental ? "]i: " : "]: ") +
                                       UtilityExtensions.formatInput(text, index) +
                                       " (NOTHING TO DO)");
                }
                ///CLOVER:ON
                break;
            }

            ///CLOVER:OFF
            if (DEBUG) {
                log.setLength(0);
                log.append("CompoundTransliterator[" + i + "=" +
                           trans[i].getID() +
                           (incremental ? "]i: " : "]: "));
                UtilityExtensions.formatInput(log, text, index);
            }
            ///CLOVER:ON

            trans[i].filteredTransliterate(text, index, incremental);

            // In a properly written transliterator, start == limit after
            // handleTransliterate() returns when incremental is false.
            // Catch cases where the subclass doesn't do this, and throw
            // an exception.  (Just pinning start to limit is a bad idea,
            // because what's probably happening is that the subclass
            // isn't transliterating all the way to the end, and it should
            // in non-incremental mode.)
            if (!incremental && index.start != index.limit) {
                throw new RuntimeException("ERROR: Incomplete non-incremental transliteration by " + trans[i].getID());
            }

            ///CLOVER:OFF
            if (DEBUG) {
                log.append(" => ");
                UtilityExtensions.formatInput(log, text, index);
                System.out.println(Utility.escape(log.toString()));
            }
            ///CLOVER:ON

            // Cumulative delta for insertions/deletions
            delta += index.limit - limit;

            if (incremental) {
                // In the incremental case, only allow subsequent
                // transliterators to modify what has already been
                // completely processed by prior transliterators.  In the
                // non-incrmental case, allow each transliterator to
                // process the entire text.
                index.limit = index.start;
            }
        }

        compoundLimit += delta;

        // Start is good where it is -- where the last transliterator left
        // it.  Limit needs to be put back where it was, modulo
        // adjustments for deletions/insertions.
        index.limit = compoundLimit;

        ///CLOVER:OFF
        if (DEBUG) {
            log.setLength(0);
            log.append("CompoundTransliterator{" + getID() +
                       (incremental ? "}i: OUT=" : "}: OUT="));
            UtilityExtensions.formatInput(log, text, index);
            System.out.println(Utility.escape(log.toString()));
        }
        ///CLOVER:ON
    }

    /**
     * Compute and set the length of the longest context required by this transliterator.
     * This is <em>preceding</em> context.
     */
    private void computeMaximumContextLength() {
        int max = 0;
        for (int i=0; i<trans.length; ++i) {
            int len = trans[i].getMaximumContextLength();
            if (len > max) {
                max = len;
            }
        }
        setMaximumContextLength(max);
    }

    /**
     * Temporary hack for registry problem. Needs to be replaced by better architecture.
     */
    public Transliterator safeClone() {
        UnicodeFilter filter = getFilter();
        if (filter != null && filter instanceof UnicodeSet) {
            filter = new UnicodeSet((UnicodeSet)filter);
        }
        return new CompoundTransliterator(getID(), filter, trans, numAnonymousRBTs);
    }
}
