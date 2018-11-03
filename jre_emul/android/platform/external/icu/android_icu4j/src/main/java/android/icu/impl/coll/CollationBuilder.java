/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2013-2015, International Business Machines
* Corporation and others.  All Rights Reserved.
*******************************************************************************
* CollationBuilder.java, ported from collationbuilder.h/.cpp
*
* C++ version created on: 2013may06
* created by: Markus W. Scherer
*/

package android.icu.impl.coll;

import java.text.ParseException;

import android.icu.impl.Norm2AllModes;
import android.icu.impl.Normalizer2Impl;
import android.icu.impl.Normalizer2Impl.Hangul;
import android.icu.lang.UScript;
import android.icu.text.CanonicalIterator;
import android.icu.text.Collator;
import android.icu.text.Normalizer2;
import android.icu.text.UnicodeSet;
import android.icu.text.UnicodeSetIterator;
import android.icu.util.ULocale;

/**
 * @hide Only a subset of ICU is exposed in Android
 */
public final class CollationBuilder extends CollationRuleParser.Sink {
    private static final boolean DEBUG = false;
    private static final class BundleImporter implements CollationRuleParser.Importer {
        BundleImporter() {}
        @Override
        public String getRules(String localeID, String collationType) {
            return CollationLoader.loadRules(new ULocale(localeID), collationType);
        }
    }

    public CollationBuilder(CollationTailoring b) {
        nfd = Normalizer2.getNFDInstance();
        fcd = Norm2AllModes.getFCDNormalizer2();
        nfcImpl = Norm2AllModes.getNFCInstance().impl;
        base = b;
        baseData = b.data;
        rootElements = new CollationRootElements(b.data.rootElements);
        variableTop = 0;
        dataBuilder = new CollationDataBuilder();
        fastLatinEnabled = true;
        cesLength = 0;
        rootPrimaryIndexes = new UVector32();
        nodes = new UVector64();
        nfcImpl.ensureCanonIterData();
        dataBuilder.initForTailoring(baseData);
    }

    public CollationTailoring parseAndBuild(String ruleString) throws ParseException {
        if(baseData.rootElements == null) {
            // C++ U_MISSING_RESOURCE_ERROR
            throw new UnsupportedOperationException(
                    "missing root elements data, tailoring not supported");
        }
        CollationTailoring tailoring = new CollationTailoring(base.settings);
        CollationRuleParser parser = new CollationRuleParser(baseData);
        // Note: This always bases &[last variable] and &[first regular]
        // on the root collator's maxVariable/variableTop.
        // If we wanted this to change after [maxVariable x], then we would keep
        // the tailoring.settings pointer here and read its variableTop when we need it.
        // See http://unicode.org/cldr/trac/ticket/6070
        variableTop = base.settings.readOnly().variableTop;
        parser.setSink(this);
        // In Java, there is only one Importer implementation.
        // In C++, the importer is a parameter for this method.
        parser.setImporter(new BundleImporter());
        CollationSettings ownedSettings = tailoring.settings.copyOnWrite();
        parser.parse(ruleString, ownedSettings);
        if(dataBuilder.hasMappings()) {
            makeTailoredCEs();
            closeOverComposites();
            finalizeCEs();
            // Copy all of ASCII, and Latin-1 letters, into each tailoring.
            optimizeSet.add(0, 0x7f);
            optimizeSet.add(0xc0, 0xff);
            // Hangul is decomposed on the fly during collation,
            // and the tailoring data is always built with HANGUL_TAG specials.
            optimizeSet.remove(Hangul.HANGUL_BASE, Hangul.HANGUL_END);
            dataBuilder.optimize(optimizeSet);
            tailoring.ensureOwnedData();
            if(fastLatinEnabled) { dataBuilder.enableFastLatin(); }
            dataBuilder.build(tailoring.ownedData);
            // C++ tailoring.builder = dataBuilder;
            dataBuilder = null;
        } else {
            tailoring.data = baseData;
        }
        ownedSettings.fastLatinOptions = CollationFastLatin.getOptions(
                tailoring.data, ownedSettings,
                ownedSettings.fastLatinPrimaries);
        tailoring.setRules(ruleString);
        // In Java, we do not have a rules version.
        // In C++, the genrb build tool reads and supplies one,
        // and the rulesVersion is a parameter for this method.
        tailoring.setVersion(base.version, 0 /* rulesVersion */);
        return tailoring;
    }

    /** Implements CollationRuleParser.Sink. */
    @Override
    void addReset(int strength, CharSequence str) {
        assert(str.length() != 0);
        if(str.charAt(0) == CollationRuleParser.POS_LEAD) {
            ces[0] = getSpecialResetPosition(str);
            cesLength = 1;
            assert((ces[0] & Collation.CASE_AND_QUATERNARY_MASK) == 0);
        } else {
            // normal reset to a character or string
            String nfdString = nfd.normalize(str);
            cesLength = dataBuilder.getCEs(nfdString, ces, 0);
            if(cesLength > Collation.MAX_EXPANSION_LENGTH) {
                throw new IllegalArgumentException(
                        "reset position maps to too many collation elements (more than 31)");
            }
        }
        if(strength == Collator.IDENTICAL) { return; }  // simple reset-at-position

        // &[before strength]position
        assert(Collator.PRIMARY <= strength && strength <= Collator.TERTIARY);
        int index = findOrInsertNodeForCEs(strength);

        long node = nodes.elementAti(index);
        // If the index is for a "weaker" node,
        // then skip backwards over this and further "weaker" nodes.
        while(strengthFromNode(node) > strength) {
            index = previousIndexFromNode(node);
            node = nodes.elementAti(index);
        }

        // Find or insert a node whose index we will put into a temporary CE.
        if(strengthFromNode(node) == strength && isTailoredNode(node)) {
            // Reset to just before this same-strength tailored node.
            index = previousIndexFromNode(node);
        } else if(strength == Collator.PRIMARY) {
            // root primary node (has no previous index)
            long p = weight32FromNode(node);
            if(p == 0) {
                throw new UnsupportedOperationException(
                        "reset primary-before ignorable not possible");
            }
            if(p <= rootElements.getFirstPrimary()) {
                // There is no primary gap between ignorables and the space-first-primary.
                throw new UnsupportedOperationException(
                        "reset primary-before first non-ignorable not supported");
            }
            if(p == Collation.FIRST_TRAILING_PRIMARY) {
                // We do not support tailoring to an unassigned-implicit CE.
                throw new UnsupportedOperationException(
                        "reset primary-before [first trailing] not supported");
            }
            p = rootElements.getPrimaryBefore(p, baseData.isCompressiblePrimary(p));
            index = findOrInsertNodeForPrimary(p);
            // Go to the last node in this list:
            // Tailor after the last node between adjacent root nodes.
            for(;;) {
                node = nodes.elementAti(index);
                int nextIndex = nextIndexFromNode(node);
                if(nextIndex == 0) { break; }
                index = nextIndex;
            }
        } else {
            // &[before 2] or &[before 3]
            index = findCommonNode(index, Collator.SECONDARY);
            if(strength >= Collator.TERTIARY) {
                index = findCommonNode(index, Collator.TERTIARY);
            }
            // findCommonNode() stayed on the stronger node or moved to
            // an explicit common-weight node of the reset-before strength.
            node = nodes.elementAti(index);
            if(strengthFromNode(node) == strength) {
                // Found a same-strength node with an explicit weight.
                int weight16 = weight16FromNode(node);
                if(weight16 == 0) {
                    throw new UnsupportedOperationException(
                            (strength == Collator.SECONDARY) ?
                                    "reset secondary-before secondary ignorable not possible" :
                                    "reset tertiary-before completely ignorable not possible");
                }
                assert(weight16 > Collation.BEFORE_WEIGHT16);
                // Reset to just before this node.
                // Insert the preceding same-level explicit weight if it is not there already.
                // Which explicit weight immediately precedes this one?
                weight16 = getWeight16Before(index, node, strength);
                // Does this preceding weight have a node?
                int previousWeight16;
                int previousIndex = previousIndexFromNode(node);
                for(int i = previousIndex;; i = previousIndexFromNode(node)) {
                    node = nodes.elementAti(i);
                    int previousStrength = strengthFromNode(node);
                    if(previousStrength < strength) {
                        assert(weight16 >= Collation.COMMON_WEIGHT16 || i == previousIndex);
                        // Either the reset element has an above-common weight and
                        // the parent node provides the implied common weight,
                        // or the reset element has a weight<=common in the node
                        // right after the parent, and we need to insert the preceding weight.
                        previousWeight16 = Collation.COMMON_WEIGHT16;
                        break;
                    } else if(previousStrength == strength && !isTailoredNode(node)) {
                        previousWeight16 = weight16FromNode(node);
                        break;
                    }
                    // Skip weaker nodes and same-level tailored nodes.
                }
                if(previousWeight16 == weight16) {
                    // The preceding weight has a node,
                    // maybe with following weaker or tailored nodes.
                    // Reset to the last of them.
                    index = previousIndex;
                } else {
                    // Insert a node with the preceding weight, reset to that.
                    node = nodeFromWeight16(weight16) | nodeFromStrength(strength);
                    index = insertNodeBetween(previousIndex, index, node);
                }
            } else {
                // Found a stronger node with implied strength-common weight.
                int weight16 = getWeight16Before(index, node, strength);
                index = findOrInsertWeakNode(index, weight16, strength);
            }
            // Strength of the temporary CE = strength of its reset position.
            // Code above raises an error if the before-strength is stronger.
            strength = ceStrength(ces[cesLength - 1]);
        }
        ces[cesLength - 1] = tempCEFromIndexAndStrength(index, strength);
    }

    /**
     * Returns the secondary or tertiary weight preceding the current node's weight.
     * node=nodes[index].
     */
    private int getWeight16Before(int index, long node, int level) {
        assert(strengthFromNode(node) < level || !isTailoredNode(node));
        // Collect the root CE weights if this node is for a root CE.
        // If it is not, then return the low non-primary boundary for a tailored CE.
        int t;
        if(strengthFromNode(node) == Collator.TERTIARY) {
            t = weight16FromNode(node);
        } else {
            t = Collation.COMMON_WEIGHT16;  // Stronger node with implied common weight.
        }
        while(strengthFromNode(node) > Collator.SECONDARY) {
            index = previousIndexFromNode(node);
            node = nodes.elementAti(index);
        }
        if(isTailoredNode(node)) {
            return Collation.BEFORE_WEIGHT16;
        }
        int s;
        if(strengthFromNode(node) == Collator.SECONDARY) {
            s = weight16FromNode(node);
        } else {
            s = Collation.COMMON_WEIGHT16;  // Stronger node with implied common weight.
        }
        while(strengthFromNode(node) > Collator.PRIMARY) {
            index = previousIndexFromNode(node);
            node = nodes.elementAti(index);
        }
        if(isTailoredNode(node)) {
            return Collation.BEFORE_WEIGHT16;
        }
        // [p, s, t] is a root CE. Return the preceding weight for the requested level.
        long p = weight32FromNode(node);
        int weight16;
        if(level == Collator.SECONDARY) {
            weight16 = rootElements.getSecondaryBefore(p, s);
        } else {
            weight16 = rootElements.getTertiaryBefore(p, s, t);
            assert((weight16 & ~Collation.ONLY_TERTIARY_MASK) == 0);
        }
        return weight16;
    }

    private long getSpecialResetPosition(CharSequence str) {
        assert(str.length() == 2);
        long ce;
        int strength = Collator.PRIMARY;
        boolean isBoundary = false;
        CollationRuleParser.Position pos =
                CollationRuleParser.POSITION_VALUES[str.charAt(1) - CollationRuleParser.POS_BASE];
        switch(pos) {
        case FIRST_TERTIARY_IGNORABLE:
            // Quaternary CEs are not supported.
            // Non-zero quaternary weights are possible only on tertiary or stronger CEs.
            return 0;
        case LAST_TERTIARY_IGNORABLE:
            return 0;
        case FIRST_SECONDARY_IGNORABLE: {
            // Look for a tailored tertiary node after [0, 0, 0].
            int index = findOrInsertNodeForRootCE(0, Collator.TERTIARY);
            long node = nodes.elementAti(index);
            if((index = nextIndexFromNode(node)) != 0) {
                node = nodes.elementAti(index);
                assert(strengthFromNode(node) <= Collator.TERTIARY);
                if(isTailoredNode(node) && strengthFromNode(node) == Collator.TERTIARY) {
                    return tempCEFromIndexAndStrength(index, Collator.TERTIARY);
                }
            }
            return rootElements.getFirstTertiaryCE();
            // No need to look for nodeHasAnyBefore() on a tertiary node.
        }
        case LAST_SECONDARY_IGNORABLE:
            ce = rootElements.getLastTertiaryCE();
            strength = Collator.TERTIARY;
            break;
        case FIRST_PRIMARY_IGNORABLE: {
            // Look for a tailored secondary node after [0, 0, *].
            int index = findOrInsertNodeForRootCE(0, Collator.SECONDARY);
            long node = nodes.elementAti(index);
            while((index = nextIndexFromNode(node)) != 0) {
                node = nodes.elementAti(index);
                strength = strengthFromNode(node);
                if(strength < Collator.SECONDARY) { break; }
                if(strength == Collator.SECONDARY) {
                    if(isTailoredNode(node)) {
                        if(nodeHasBefore3(node)) {
                            index = nextIndexFromNode(nodes.elementAti(nextIndexFromNode(node)));
                            assert(isTailoredNode(nodes.elementAti(index)));
                        }
                        return tempCEFromIndexAndStrength(index, Collator.SECONDARY);
                    } else {
                        break;
                    }
                }
            }
            ce = rootElements.getFirstSecondaryCE();
            strength = Collator.SECONDARY;
            break;
        }
        case LAST_PRIMARY_IGNORABLE:
            ce = rootElements.getLastSecondaryCE();
            strength = Collator.SECONDARY;
            break;
        case FIRST_VARIABLE:
            ce = rootElements.getFirstPrimaryCE();
            isBoundary = true;  // FractionalUCA.txt: FDD1 00A0, SPACE first primary
            break;
        case LAST_VARIABLE:
            ce = rootElements.lastCEWithPrimaryBefore(variableTop + 1);
            break;
        case FIRST_REGULAR:
            ce = rootElements.firstCEWithPrimaryAtLeast(variableTop + 1);
            isBoundary = true;  // FractionalUCA.txt: FDD1 263A, SYMBOL first primary
            break;
        case LAST_REGULAR:
            // Use the Hani-first-primary rather than the actual last "regular" CE before it,
            // for backward compatibility with behavior before the introduction of
            // script-first-primary CEs in the root collator.
            ce = rootElements.firstCEWithPrimaryAtLeast(
                baseData.getFirstPrimaryForGroup(UScript.HAN));
            break;
        case FIRST_IMPLICIT:
            ce = baseData.getSingleCE(0x4e00);
            break;
        case LAST_IMPLICIT:
            // We do not support tailoring to an unassigned-implicit CE.
            throw new UnsupportedOperationException(
                    "reset to [last implicit] not supported");
        case FIRST_TRAILING:
            ce = Collation.makeCE(Collation.FIRST_TRAILING_PRIMARY);
            isBoundary = true;  // trailing first primary (there is no mapping for it)
            break;
        case LAST_TRAILING:
            throw new IllegalArgumentException("LDML forbids tailoring to U+FFFF");
        default:
            assert(false);
            return 0;
        }

        int index = findOrInsertNodeForRootCE(ce, strength);
        long node = nodes.elementAti(index);
        if((pos.ordinal() & 1) == 0) {
            // even pos = [first xyz]
            if(!nodeHasAnyBefore(node) && isBoundary) {
                // A <group> first primary boundary is artificially added to FractionalUCA.txt.
                // It is reachable via its special contraction, but is not normally used.
                // Find the first character tailored after the boundary CE,
                // or the first real root CE after it.
                if((index = nextIndexFromNode(node)) != 0) {
                    // If there is a following node, then it must be tailored
                    // because there are no root CEs with a boundary primary
                    // and non-common secondary/tertiary weights.
                    node = nodes.elementAti(index);
                    assert(isTailoredNode(node));
                    ce = tempCEFromIndexAndStrength(index, strength);
                } else {
                    assert(strength == Collator.PRIMARY);
                    long p = ce >>> 32;
                    int pIndex = rootElements.findPrimary(p);
                    boolean isCompressible = baseData.isCompressiblePrimary(p);
                    p = rootElements.getPrimaryAfter(p, pIndex, isCompressible);
                    ce = Collation.makeCE(p);
                    index = findOrInsertNodeForRootCE(ce, Collator.PRIMARY);
                    node = nodes.elementAti(index);
                }
            }
            if(nodeHasAnyBefore(node)) {
                // Get the first node that was tailored before this one at a weaker strength.
                if(nodeHasBefore2(node)) {
                    index = nextIndexFromNode(nodes.elementAti(nextIndexFromNode(node)));
                    node = nodes.elementAti(index);
                }
                if(nodeHasBefore3(node)) {
                    index = nextIndexFromNode(nodes.elementAti(nextIndexFromNode(node)));
                }
                assert(isTailoredNode(nodes.elementAti(index)));
                ce = tempCEFromIndexAndStrength(index, strength);
            }
        } else {
            // odd pos = [last xyz]
            // Find the last node that was tailored after the [last xyz]
            // at a strength no greater than the position's strength.
            for(;;) {
                int nextIndex = nextIndexFromNode(node);
                if(nextIndex == 0) { break; }
                long nextNode = nodes.elementAti(nextIndex);
                if(strengthFromNode(nextNode) < strength) { break; }
                index = nextIndex;
                node = nextNode;
            }
            // Do not make a temporary CE for a root node.
            // This last node might be the node for the root CE itself,
            // or a node with a common secondary or tertiary weight.
            if(isTailoredNode(node)) {
                ce = tempCEFromIndexAndStrength(index, strength);
            }
        }
        return ce;
    }

    /** Implements CollationRuleParser.Sink. */
    @Override
    void addRelation(int strength, CharSequence prefix, CharSequence str, CharSequence extension) {
        String nfdPrefix;
        if(prefix.length() == 0) {
            nfdPrefix = "";
        } else {
            nfdPrefix = nfd.normalize(prefix);
        }
        String nfdString = nfd.normalize(str);

        // The runtime code decomposes Hangul syllables on the fly,
        // with recursive processing but without making the Jamo pieces visible for matching.
        // It does not work with certain types of contextual mappings.
        int nfdLength = nfdString.length();
        if(nfdLength >= 2) {
            char c = nfdString.charAt(0);
            if(Hangul.isJamoL(c) || Hangul.isJamoV(c)) {
                // While handling a Hangul syllable, contractions starting with Jamo L or V
                // would not see the following Jamo of that syllable.
                throw new UnsupportedOperationException(
                        "contractions starting with conjoining Jamo L or V not supported");
            }
            c = nfdString.charAt(nfdLength - 1);
            if(Hangul.isJamoL(c) ||
                    (Hangul.isJamoV(c) && Hangul.isJamoL(nfdString.charAt(nfdLength - 2)))) {
                // A contraction ending with Jamo L or L+V would require
                // generating Hangul syllables in addTailComposites() (588 for a Jamo L),
                // or decomposing a following Hangul syllable on the fly, during contraction matching.
                throw new UnsupportedOperationException(
                        "contractions ending with conjoining Jamo L or L+V not supported");
            }
            // A Hangul syllable completely inside a contraction is ok.
        }
        // Note: If there is a prefix, then the parser checked that
        // both the prefix and the string beging with NFC boundaries (not Jamo V or T).
        // Therefore: prefix.isEmpty() || !isJamoVOrT(nfdString.charAt(0))
        // (While handling a Hangul syllable, prefixes on Jamo V or T
        // would not see the previous Jamo of that syllable.)

        if(strength != Collator.IDENTICAL) {
            // Find the node index after which we insert the new tailored node.
            int index = findOrInsertNodeForCEs(strength);
            assert(cesLength > 0);
            long ce = ces[cesLength - 1];
            if(strength == Collator.PRIMARY && !isTempCE(ce) && (ce >>> 32) == 0) {
                // There is no primary gap between ignorables and the space-first-primary.
                throw new UnsupportedOperationException(
                        "tailoring primary after ignorables not supported");
            }
            if(strength == Collator.QUATERNARY && ce == 0) {
                // The CE data structure does not support non-zero quaternary weights
                // on tertiary ignorables.
                throw new UnsupportedOperationException(
                        "tailoring quaternary after tertiary ignorables not supported");
            }
            // Insert the new tailored node.
            index = insertTailoredNodeAfter(index, strength);
            // Strength of the temporary CE:
            // The new relation may yield a stronger CE but not a weaker one.
            int tempStrength = ceStrength(ce);
            if(strength < tempStrength) { tempStrength = strength; }
            ces[cesLength - 1] = tempCEFromIndexAndStrength(index, tempStrength);
        }

        setCaseBits(nfdString);

        int cesLengthBeforeExtension = cesLength;
        if(extension.length() != 0) {
            String nfdExtension = nfd.normalize(extension);
            cesLength = dataBuilder.getCEs(nfdExtension, ces, cesLength);
            if(cesLength > Collation.MAX_EXPANSION_LENGTH) {
                throw new IllegalArgumentException(
                        "extension string adds too many collation elements (more than 31 total)");
            }
        }
        int ce32 = Collation.UNASSIGNED_CE32;
        if((!nfdPrefix.contentEquals(prefix) || !nfdString.contentEquals(str)) &&
                !ignorePrefix(prefix) && !ignoreString(str)) {
            // Map from the original input to the CEs.
            // We do this in case the canonical closure is incomplete,
            // so that it is possible to explicitly provide the missing mappings.
            ce32 = addIfDifferent(prefix, str, ces, cesLength, ce32);
        }
        addWithClosure(nfdPrefix, nfdString, ces, cesLength, ce32);
        cesLength = cesLengthBeforeExtension;
    }

    /**
     * Picks one of the current CEs and finds or inserts a node in the graph
     * for the CE + strength.
     */
    private int findOrInsertNodeForCEs(int strength) {
        assert(Collator.PRIMARY <= strength && strength <= Collator.QUATERNARY);

        // Find the last CE that is at least as "strong" as the requested difference.
        // Note: Stronger is smaller (Collator.PRIMARY=0).
        long ce;
        for(;; --cesLength) {
            if(cesLength == 0) {
                ce = ces[0] = 0;
                cesLength = 1;
                break;
            } else {
                ce = ces[cesLength - 1];
            }
            if(ceStrength(ce) <= strength) { break; }
        }

        if(isTempCE(ce)) {
            // No need to findCommonNode() here for lower levels
            // because insertTailoredNodeAfter() will do that anyway.
            return indexFromTempCE(ce);
        }

        // root CE
        if((int)(ce >>> 56) == Collation.UNASSIGNED_IMPLICIT_BYTE) {
            throw new UnsupportedOperationException(
                    "tailoring relative to an unassigned code point not supported");
        }
        return findOrInsertNodeForRootCE(ce, strength);
    }

    private int findOrInsertNodeForRootCE(long ce, int strength) {
        assert((int)(ce >>> 56) != Collation.UNASSIGNED_IMPLICIT_BYTE);

        // Find or insert the node for each of the root CE's weights,
        // down to the requested level/strength.
        // Root CEs must have common=zero quaternary weights (for which we never insert any nodes).
        assert((ce & 0xc0) == 0);
        int index = findOrInsertNodeForPrimary(ce >>> 32);
        if(strength >= Collator.SECONDARY) {
            int lower32 = (int)ce;
            index = findOrInsertWeakNode(index, lower32 >>> 16, Collator.SECONDARY);
            if(strength >= Collator.TERTIARY) {
                index = findOrInsertWeakNode(index, lower32 & Collation.ONLY_TERTIARY_MASK,
                                            Collator.TERTIARY);
            }
        }
        return index;
    }

    /**
     * Like Java Collections.binarySearch(List, key, Comparator).
     *
     * @return the index>=0 where the item was found,
     *         or the index<0 for inserting the string at ~index in sorted order
     *         (index into rootPrimaryIndexes)
     */
    private static final int binarySearchForRootPrimaryNode(
            int[] rootPrimaryIndexes, int length, long[] nodes, long p) {
        if(length == 0) { return ~0; }
        int start = 0;
        int limit = length;
        for (;;) {
            int i = (int)(((long)start + (long)limit) / 2);
            long node = nodes[rootPrimaryIndexes[i]];
            long nodePrimary = node >>> 32;  // weight32FromNode(node)
            if (p == nodePrimary) {
                return i;
            } else if (p < nodePrimary) {
                if (i == start) {
                    return ~start;  // insert s before i
                }
                limit = i;
            } else {
                if (i == start) {
                    return ~(start + 1);  // insert s after i
                }
                start = i;
            }
        }
    }

    /** Finds or inserts the node for a root CE's primary weight. */
    private int findOrInsertNodeForPrimary(long p) {
        int rootIndex = binarySearchForRootPrimaryNode(
            rootPrimaryIndexes.getBuffer(), rootPrimaryIndexes.size(), nodes.getBuffer(), p);
        if(rootIndex >= 0) {
            return rootPrimaryIndexes.elementAti(rootIndex);
        } else {
            // Start a new list of nodes with this primary.
            int index = nodes.size();
            nodes.addElement(nodeFromWeight32(p));
            rootPrimaryIndexes.insertElementAt(index, ~rootIndex);
            return index;
        }
    }

    /** Finds or inserts the node for a secondary or tertiary weight. */
    private int findOrInsertWeakNode(int index, int weight16, int level) {
        assert(0 <= index && index < nodes.size());
        assert(Collator.SECONDARY <= level && level <= Collator.TERTIARY);

        if(weight16 == Collation.COMMON_WEIGHT16) {
            return findCommonNode(index, level);
        }

        // If this will be the first below-common weight for the parent node,
        // then we will also need to insert a common weight after it.
        long node = nodes.elementAti(index);
        assert(strengthFromNode(node) < level);  // parent node is stronger
        if(weight16 != 0 && weight16 < Collation.COMMON_WEIGHT16) {
            int hasThisLevelBefore = level == Collator.SECONDARY ? HAS_BEFORE2 : HAS_BEFORE3;
            if((node & hasThisLevelBefore) == 0) {
                // The parent node has an implied level-common weight.
                long commonNode =
                    nodeFromWeight16(Collation.COMMON_WEIGHT16) | nodeFromStrength(level);
                if(level == Collator.SECONDARY) {
                    // Move the HAS_BEFORE3 flag from the parent node
                    // to the new secondary common node.
                    commonNode |= node & HAS_BEFORE3;
                    node &= ~(long)HAS_BEFORE3;
                }
                nodes.setElementAt(node | hasThisLevelBefore, index);
                // Insert below-common-weight node.
                int nextIndex = nextIndexFromNode(node);
                node = nodeFromWeight16(weight16) | nodeFromStrength(level);
                index = insertNodeBetween(index, nextIndex, node);
                // Insert common-weight node.
                insertNodeBetween(index, nextIndex, commonNode);
                // Return index of below-common-weight node.
                return index;
            }
        }

        // Find the root CE's weight for this level.
        // Postpone insertion if not found:
        // Insert the new root node before the next stronger node,
        // or before the next root node with the same strength and a larger weight.
        int nextIndex;
        while((nextIndex = nextIndexFromNode(node)) != 0) {
            node = nodes.elementAti(nextIndex);
            int nextStrength = strengthFromNode(node);
            if(nextStrength <= level) {
                // Insert before a stronger node.
                if(nextStrength < level) { break; }
                // nextStrength == level
                if(!isTailoredNode(node)) {
                    int nextWeight16 = weight16FromNode(node);
                    if(nextWeight16 == weight16) {
                        // Found the node for the root CE up to this level.
                        return nextIndex;
                    }
                    // Insert before a node with a larger same-strength weight.
                    if(nextWeight16 > weight16) { break; }
                }
            }
            // Skip the next node.
            index = nextIndex;
        }
        node = nodeFromWeight16(weight16) | nodeFromStrength(level);
        return insertNodeBetween(index, nextIndex, node);
    }

    /**
     * Makes and inserts a new tailored node into the list, after the one at index.
     * Skips over nodes of weaker strength to maintain collation order
     * ("postpone insertion").
     * @return the new node's index
     */
    private int insertTailoredNodeAfter(int index, int strength) {
        assert(0 <= index && index < nodes.size());
        if(strength >= Collator.SECONDARY) {
            index = findCommonNode(index, Collator.SECONDARY);
            if(strength >= Collator.TERTIARY) {
                index = findCommonNode(index, Collator.TERTIARY);
            }
        }
        // Postpone insertion:
        // Insert the new node before the next one with a strength at least as strong.
        long node = nodes.elementAti(index);
        int nextIndex;
        while((nextIndex = nextIndexFromNode(node)) != 0) {
            node = nodes.elementAti(nextIndex);
            if(strengthFromNode(node) <= strength) { break; }
            // Skip the next node which has a weaker (larger) strength than the new one.
            index = nextIndex;
        }
        node = IS_TAILORED | nodeFromStrength(strength);
        return insertNodeBetween(index, nextIndex, node);
    }

    /**
     * Inserts a new node into the list, between list-adjacent items.
     * The node's previous and next indexes must not be set yet.
     * @return the new node's index
     */
    private int insertNodeBetween(int index, int nextIndex, long node) {
        assert(previousIndexFromNode(node) == 0);
        assert(nextIndexFromNode(node) == 0);
        assert(nextIndexFromNode(nodes.elementAti(index)) == nextIndex);
        // Append the new node and link it to the existing nodes.
        int newIndex = nodes.size();
        node |= nodeFromPreviousIndex(index) | nodeFromNextIndex(nextIndex);
        nodes.addElement(node);
        // nodes[index].nextIndex = newIndex
        node = nodes.elementAti(index);
        nodes.setElementAt(changeNodeNextIndex(node, newIndex), index);
        // nodes[nextIndex].previousIndex = newIndex
        if(nextIndex != 0) {
            node = nodes.elementAti(nextIndex);
            nodes.setElementAt(changeNodePreviousIndex(node, newIndex), nextIndex);
        }
        return newIndex;
    }

    /**
     * Finds the node which implies or contains a common=05 weight of the given strength
     * (secondary or tertiary), if the current node is stronger.
     * Skips weaker nodes and tailored nodes if the current node is stronger
     * and is followed by an explicit-common-weight node.
     * Always returns the input index if that node is no stronger than the given strength.
     */
    private int findCommonNode(int index, int strength) {
        assert(Collator.SECONDARY <= strength && strength <= Collator.TERTIARY);
        long node = nodes.elementAti(index);
        if(strengthFromNode(node) >= strength) {
            // The current node is no stronger.
            return index;
        }
        if(strength == Collator.SECONDARY ? !nodeHasBefore2(node) : !nodeHasBefore3(node)) {
            // The current node implies the strength-common weight.
            return index;
        }
        index = nextIndexFromNode(node);
        node = nodes.elementAti(index);
        assert(!isTailoredNode(node) && strengthFromNode(node) == strength &&
                weight16FromNode(node) < Collation.COMMON_WEIGHT16);
        // Skip to the explicit common node.
        do {
            index = nextIndexFromNode(node);
            node = nodes.elementAti(index);
            assert(strengthFromNode(node) >= strength);
        } while(isTailoredNode(node) || strengthFromNode(node) > strength ||
                weight16FromNode(node) < Collation.COMMON_WEIGHT16);
        assert(weight16FromNode(node) == Collation.COMMON_WEIGHT16);
        return index;
    }

    private void setCaseBits(CharSequence nfdString) {
        int numTailoredPrimaries = 0;
        for(int i = 0; i < cesLength; ++i) {
            if(ceStrength(ces[i]) == Collator.PRIMARY) { ++numTailoredPrimaries; }
        }
        // We should not be able to get too many case bits because
        // cesLength<=31==MAX_EXPANSION_LENGTH.
        // 31 pairs of case bits fit into an long without setting its sign bit.
        assert(numTailoredPrimaries <= 31);

        long cases = 0;
        if(numTailoredPrimaries > 0) {
            CharSequence s = nfdString;
            UTF16CollationIterator baseCEs = new UTF16CollationIterator(baseData, false, s, 0);
            int baseCEsLength = baseCEs.fetchCEs() - 1;
            assert(baseCEsLength >= 0 && baseCEs.getCE(baseCEsLength) == Collation.NO_CE);

            int lastCase = 0;
            int numBasePrimaries = 0;
            for(int i = 0; i < baseCEsLength; ++i) {
                long ce = baseCEs.getCE(i);
                if((ce >>> 32) != 0) {
                    ++numBasePrimaries;
                    int c = ((int)ce >> 14) & 3;
                    assert(c == 0 || c == 2);  // lowercase or uppercase, no mixed case in any base CE
                    if(numBasePrimaries < numTailoredPrimaries) {
                        cases |= (long)c << ((numBasePrimaries - 1) * 2);
                    } else if(numBasePrimaries == numTailoredPrimaries) {
                        lastCase = c;
                    } else if(c != lastCase) {
                        // There are more base primary CEs than tailored primaries.
                        // Set mixed case if the case bits of the remainder differ.
                        lastCase = 1;
                        // Nothing more can change.
                        break;
                    }
                }
            }
            if(numBasePrimaries >= numTailoredPrimaries) {
                cases |= (long)lastCase << ((numTailoredPrimaries - 1) * 2);
            }
        }

        for(int i = 0; i < cesLength; ++i) {
            long ce = ces[i] & 0xffffffffffff3fffL;  // clear old case bits
            int strength = ceStrength(ce);
            if(strength == Collator.PRIMARY) {
                ce |= (cases & 3) << 14;
                cases >>>= 2;
            } else if(strength == Collator.TERTIARY) {
                // Tertiary CEs must have uppercase bits.
                // See the LDML spec, and comments in class CollationCompare.
                ce |= 0x8000;
            }
            // Tertiary ignorable CEs must have 0 case bits.
            // We set 0 case bits for secondary CEs too
            // since currently only U+0345 is cased and maps to a secondary CE,
            // and it is lowercase. Other secondaries are uncased.
            // See [[:Cased:]&[:uca1=:]] where uca1 queries the root primary weight.
            ces[i] = ce;
        }
    }

    /** Implements CollationRuleParser.Sink. */
    @Override
    void suppressContractions(UnicodeSet set) {
        dataBuilder.suppressContractions(set);
    }

    /** Implements CollationRuleParser.Sink. */
    @Override
    void optimize(UnicodeSet set) {
        optimizeSet.addAll(set);
    }

    /**
     * Adds the mapping and its canonical closure.
     * Takes ce32=dataBuilder.encodeCEs(...) so that the data builder
     * need not re-encode the CEs multiple times.
     */
    private int addWithClosure(CharSequence nfdPrefix, CharSequence nfdString,
                long[] newCEs, int newCEsLength, int ce32) {
        // Map from the NFD input to the CEs.
        ce32 = addIfDifferent(nfdPrefix, nfdString, newCEs, newCEsLength, ce32);
        ce32 = addOnlyClosure(nfdPrefix, nfdString, newCEs, newCEsLength, ce32);
        addTailComposites(nfdPrefix, nfdString);
        return ce32;
    }

    private int addOnlyClosure(CharSequence nfdPrefix, CharSequence nfdString,
                long[] newCEs, int newCEsLength, int ce32) {
        // Map from canonically equivalent input to the CEs. (But not from the all-NFD input.)
        // TODO: make CanonicalIterator work with CharSequence, or maybe change arguments here to String
        if(nfdPrefix.length() == 0) {
            CanonicalIterator stringIter = new CanonicalIterator(nfdString.toString());
            String prefix = "";
            for(;;) {
                String str = stringIter.next();
                if(str == null) { break; }
                if(ignoreString(str) || str.contentEquals(nfdString)) { continue; }
                ce32 = addIfDifferent(prefix, str, newCEs, newCEsLength, ce32);
            }
        } else {
            CanonicalIterator prefixIter = new CanonicalIterator(nfdPrefix.toString());
            CanonicalIterator stringIter = new CanonicalIterator(nfdString.toString());
            for(;;) {
                String prefix = prefixIter.next();
                if(prefix == null) { break; }
                if(ignorePrefix(prefix)) { continue; }
                boolean samePrefix = prefix.contentEquals(nfdPrefix);
                for(;;) {
                    String str = stringIter.next();
                    if(str == null) { break; }
                    if(ignoreString(str) || (samePrefix && str.contentEquals(nfdString))) { continue; }
                    ce32 = addIfDifferent(prefix, str, newCEs, newCEsLength, ce32);
                }
                stringIter.reset();
            }
        }
        return ce32;
    }

    private void addTailComposites(CharSequence nfdPrefix, CharSequence nfdString) {
        // Look for the last starter in the NFD string.
        int lastStarter;
        int indexAfterLastStarter = nfdString.length();
        for(;;) {
            if(indexAfterLastStarter == 0) { return; }  // no starter at all
            lastStarter = Character.codePointBefore(nfdString, indexAfterLastStarter);
            if(nfd.getCombiningClass(lastStarter) == 0) { break; }
            indexAfterLastStarter -= Character.charCount(lastStarter);
        }
        // No closure to Hangul syllables since we decompose them on the fly.
        if(Hangul.isJamoL(lastStarter)) { return; }

        // Are there any composites whose decomposition starts with the lastStarter?
        // Note: Normalizer2Impl does not currently return start sets for NFC_QC=Maybe characters.
        // We might find some more equivalent mappings here if it did.
        UnicodeSet composites = new UnicodeSet();
        if(!nfcImpl.getCanonStartSet(lastStarter, composites)) { return; }

        StringBuilder newNFDString = new StringBuilder(), newString = new StringBuilder();
        long[] newCEs = new long[Collation.MAX_EXPANSION_LENGTH];
        UnicodeSetIterator iter = new UnicodeSetIterator(composites);
        while(iter.next()) {
            assert(iter.codepoint != UnicodeSetIterator.IS_STRING);
            int composite = iter.codepoint;
            String decomp = nfd.getDecomposition(composite);
            if(!mergeCompositeIntoString(nfdString, indexAfterLastStarter, composite, decomp,
                    newNFDString, newString)) {
                continue;
            }
            int newCEsLength = dataBuilder.getCEs(nfdPrefix, newNFDString, newCEs, 0);
            if(newCEsLength > Collation.MAX_EXPANSION_LENGTH) {
                // Ignore mappings that we cannot store.
                continue;
            }
            // Note: It is possible that the newCEs do not make use of the mapping
            // for which we are adding the tail composites, in which case we might be adding
            // unnecessary mappings.
            // For example, when we add tail composites for ae^ (^=combining circumflex),
            // UCA discontiguous-contraction matching does not find any matches
            // for ae_^ (_=any combining diacritic below) *unless* there is also
            // a contraction mapping for ae.
            // Thus, if there is no ae contraction, then the ae^ mapping is ignored
            // while fetching the newCEs for ae_^.
            // TODO: Try to detect this effectively.
            // (Alternatively, print a warning when prefix contractions are missing.)

            // We do not need an explicit mapping for the NFD strings.
            // It is fine if the NFD input collates like this via a sequence of mappings.
            // It also saves a little bit of space, and may reduce the set of characters with contractions.
            int ce32 = addIfDifferent(nfdPrefix, newString,
                                          newCEs, newCEsLength, Collation.UNASSIGNED_CE32);
            if(ce32 != Collation.UNASSIGNED_CE32) {
                // was different, was added
                addOnlyClosure(nfdPrefix, newNFDString, newCEs, newCEsLength, ce32);
            }
        }
    }

    private boolean mergeCompositeIntoString(CharSequence nfdString, int indexAfterLastStarter,
                int composite, CharSequence decomp,
                StringBuilder newNFDString, StringBuilder newString) {
        assert(Character.codePointBefore(nfdString, indexAfterLastStarter) ==
                Character.codePointAt(decomp, 0));
        int lastStarterLength = Character.offsetByCodePoints(decomp, 0, 1);
        if(lastStarterLength == decomp.length()) {
            // Singleton decompositions should be found by addWithClosure()
            // and the CanonicalIterator, so we can ignore them here.
            return false;
        }
        if(equalSubSequences(nfdString, indexAfterLastStarter, decomp, lastStarterLength)) {
            // same strings, nothing new to be found here
            return false;
        }

        // Make new FCD strings that combine a composite, or its decomposition,
        // into the nfdString's last starter and the combining marks following it.
        // Make an NFD version, and a version with the composite.
        newNFDString.setLength(0);
        newNFDString.append(nfdString, 0, indexAfterLastStarter);
        newString.setLength(0);
        newString.append(nfdString, 0, indexAfterLastStarter - lastStarterLength)
            .appendCodePoint(composite);

        // The following is related to discontiguous contraction matching,
        // but builds only FCD strings (or else returns false).
        int sourceIndex = indexAfterLastStarter;
        int decompIndex = lastStarterLength;
        // Small optimization: We keep the source character across loop iterations
        // because we do not always consume it,
        // and then need not fetch it again nor look up its combining class again.
        int sourceChar = Collation.SENTINEL_CP;
        // The cc variables need to be declared before the loop so that at the end
        // they are set to the last combining classes seen.
        int sourceCC = 0;
        int decompCC = 0;
        for(;;) {
            if(sourceChar < 0) {
                if(sourceIndex >= nfdString.length()) { break; }
                sourceChar = Character.codePointAt(nfdString, sourceIndex);
                sourceCC = nfd.getCombiningClass(sourceChar);
                assert(sourceCC != 0);
            }
            // We consume a decomposition character in each iteration.
            if(decompIndex >= decomp.length()) { break; }
            int decompChar = Character.codePointAt(decomp, decompIndex);
            decompCC = nfd.getCombiningClass(decompChar);
            // Compare the two characters and their combining classes.
            if(decompCC == 0) {
                // Unable to merge because the source contains a non-zero combining mark
                // but the composite's decomposition contains another starter.
                // The strings would not be equivalent.
                return false;
            } else if(sourceCC < decompCC) {
                // Composite + sourceChar would not be FCD.
                return false;
            } else if(decompCC < sourceCC) {
                newNFDString.appendCodePoint(decompChar);
                decompIndex += Character.charCount(decompChar);
            } else if(decompChar != sourceChar) {
                // Blocked because same combining class.
                return false;
            } else {  // match: decompChar == sourceChar
                newNFDString.appendCodePoint(decompChar);
                decompIndex += Character.charCount(decompChar);
                sourceIndex += Character.charCount(decompChar);
                sourceChar = Collation.SENTINEL_CP;
            }
        }
        // We are at the end of at least one of the two inputs.
        if(sourceChar >= 0) {  // more characters from nfdString but not from decomp
            if(sourceCC < decompCC) {
                // Appending the next source character to the composite would not be FCD.
                return false;
            }
            newNFDString.append(nfdString, sourceIndex, nfdString.length());
            newString.append(nfdString, sourceIndex, nfdString.length());
        } else if(decompIndex < decomp.length()) {  // more characters from decomp, not from nfdString
            newNFDString.append(decomp, decompIndex, decomp.length());
        }
        assert(nfd.isNormalized(newNFDString));
        assert(fcd.isNormalized(newString));
        assert(nfd.normalize(newString).equals(newNFDString.toString()));  // canonically equivalent
        return true;
    }

    private boolean equalSubSequences(CharSequence left, int leftStart, CharSequence right, int rightStart) {
        // C++ UnicodeString::compare(leftStart, 0x7fffffff, right, rightStart, 0x7fffffff) == 0
        int leftLength = left.length();
        if((leftLength - leftStart) != (right.length() - rightStart)) { return false; }
        while(leftStart < leftLength) {
            if(left.charAt(leftStart++) != right.charAt(rightStart++)) {
                return false;
            }
        }
        return true;
    }
    private boolean ignorePrefix(CharSequence s) {
        // Do not map non-FCD prefixes.
        return !isFCD(s);
    }
    private boolean ignoreString(CharSequence s) {
        // Do not map non-FCD strings.
        // Do not map strings that start with Hangul syllables: We decompose those on the fly.
        return !isFCD(s) || Hangul.isHangul(s.charAt(0));
    }
    private boolean isFCD(CharSequence s) {
        return fcd.isNormalized(s);
    }

    private static final UnicodeSet COMPOSITES = new UnicodeSet("[:NFD_QC=N:]");
    static {
        // Hangul is decomposed on the fly during collation.
        COMPOSITES.remove(Hangul.HANGUL_BASE, Hangul.HANGUL_END);
    }

    private void closeOverComposites() {
        String prefix = "";  // empty
        UnicodeSetIterator iter = new UnicodeSetIterator(COMPOSITES);
        while(iter.next()) {
            assert(iter.codepoint != UnicodeSetIterator.IS_STRING);
            String nfdString = nfd.getDecomposition(iter.codepoint);
            cesLength = dataBuilder.getCEs(nfdString, ces, 0);
            if(cesLength > Collation.MAX_EXPANSION_LENGTH) {
                // Too many CEs from the decomposition (unusual), ignore this composite.
                // We could add a capacity parameter to getCEs() and reallocate if necessary.
                // However, this can only really happen in contrived cases.
                continue;
            }
            String composite = iter.getString();
            addIfDifferent(prefix, composite, ces, cesLength, Collation.UNASSIGNED_CE32);
        }
    }

    private int addIfDifferent(CharSequence prefix, CharSequence str,
                long[] newCEs, int newCEsLength, int ce32) {
        long[] oldCEs = new long[Collation.MAX_EXPANSION_LENGTH];
        int oldCEsLength = dataBuilder.getCEs(prefix, str, oldCEs, 0);
        if(!sameCEs(newCEs, newCEsLength, oldCEs, oldCEsLength)) {
            if(ce32 == Collation.UNASSIGNED_CE32) {
                ce32 = dataBuilder.encodeCEs(newCEs, newCEsLength);
            }
            dataBuilder.addCE32(prefix, str, ce32);
        }
        return ce32;
    }

    private static boolean sameCEs(long[] ces1, int ces1Length,
                long[] ces2, int ces2Length) {
        if(ces1Length != ces2Length) {
            return false;
        }
        assert(ces1Length <= Collation.MAX_EXPANSION_LENGTH);
        for(int i = 0; i < ces1Length; ++i) {
            if(ces1[i] != ces2[i]) { return false; }
        }
        return true;
    }

    private static final int alignWeightRight(int w) {
        if(w != 0) {
            while((w & 0xff) == 0) { w >>>= 8; }
        }
        return w;
    }

    /**
     * Walks the tailoring graph and overwrites tailored nodes with new CEs.
     * After this, the graph is destroyed.
     * The nodes array can then be used only as a source of tailored CEs.
     */
    private void makeTailoredCEs() {
        CollationWeights primaries = new CollationWeights();
        CollationWeights secondaries = new CollationWeights();
        CollationWeights tertiaries = new CollationWeights();
        long[] nodesArray = nodes.getBuffer();
        if(DEBUG) {
            System.out.println("\nCollationBuilder.makeTailoredCEs()");
        }

        for(int rpi = 0; rpi < rootPrimaryIndexes.size(); ++rpi) {
            int i = rootPrimaryIndexes.elementAti(rpi);
            long node = nodesArray[i];
            long p = weight32FromNode(node);
            int s = p == 0 ? 0 : Collation.COMMON_WEIGHT16;
            int t = s;
            int q = 0;
            boolean pIsTailored = false;
            boolean sIsTailored = false;
            boolean tIsTailored = false;
            if(DEBUG) {
                System.out.printf("\nprimary     %x\n", alignWeightRight((int)p));
            }
            int pIndex = p == 0 ? 0 : rootElements.findPrimary(p);
            int nextIndex = nextIndexFromNode(node);
            while(nextIndex != 0) {
                i = nextIndex;
                node = nodesArray[i];
                nextIndex = nextIndexFromNode(node);
                int strength = strengthFromNode(node);
                if(strength == Collator.QUATERNARY) {
                    assert(isTailoredNode(node));
                    if(DEBUG) {
                        System.out.print("      quat+     ");
                    }
                    if(q == 3) {
                        // C++ U_BUFFER_OVERFLOW_ERROR
                        throw new UnsupportedOperationException("quaternary tailoring gap too small");
                    }
                    ++q;
                } else {
                    if(strength == Collator.TERTIARY) {
                        if(isTailoredNode(node)) {
                            if(DEBUG) {
                                System.out.print("    ter+        ");
                            }
                            if(!tIsTailored) {
                                // First tailored tertiary node for [p, s].
                                int tCount = countTailoredNodes(nodesArray, nextIndex,
                                                                    Collator.TERTIARY) + 1;
                                int tLimit;
                                if(t == 0) {
                                    // Gap at the beginning of the tertiary CE range.
                                    t = rootElements.getTertiaryBoundary() - 0x100;
                                    tLimit = (int)rootElements.getFirstTertiaryCE() & Collation.ONLY_TERTIARY_MASK;
                                } else if(!pIsTailored && !sIsTailored) {
                                    // p and s are root weights.
                                    tLimit = rootElements.getTertiaryAfter(pIndex, s, t);
                                } else if(t == Collation.BEFORE_WEIGHT16) {
                                    tLimit = Collation.COMMON_WEIGHT16;
                                } else {
                                    // [p, s] is tailored.
                                    assert(t == Collation.COMMON_WEIGHT16);
                                    tLimit = rootElements.getTertiaryBoundary();
                                }
                                assert(tLimit == 0x4000 || (tLimit & ~Collation.ONLY_TERTIARY_MASK) == 0);
                                tertiaries.initForTertiary();
                                if(!tertiaries.allocWeights(t, tLimit, tCount)) {
                                    // C++ U_BUFFER_OVERFLOW_ERROR
                                    throw new UnsupportedOperationException("tertiary tailoring gap too small");
                                }
                                tIsTailored = true;
                            }
                            t = (int)tertiaries.nextWeight();
                            assert(t != 0xffffffff);
                        } else {
                            t = weight16FromNode(node);
                            tIsTailored = false;
                            if(DEBUG) {
                                System.out.printf("    ter     %x\n", alignWeightRight(t));
                            }
                        }
                    } else {
                        if(strength == Collator.SECONDARY) {
                            if(isTailoredNode(node)) {
                                if(DEBUG) {
                                    System.out.print("  sec+          ");
                                }
                                if(!sIsTailored) {
                                    // First tailored secondary node for p.
                                    int sCount = countTailoredNodes(nodesArray, nextIndex,
                                                                        Collator.SECONDARY) + 1;
                                    int sLimit;
                                    if(s == 0) {
                                        // Gap at the beginning of the secondary CE range.
                                        s = rootElements.getSecondaryBoundary() - 0x100;
                                        sLimit = (int)(rootElements.getFirstSecondaryCE() >> 16);
                                    } else if(!pIsTailored) {
                                        // p is a root primary.
                                        sLimit = rootElements.getSecondaryAfter(pIndex, s);
                                    } else if(s == Collation.BEFORE_WEIGHT16) {
                                        sLimit = Collation.COMMON_WEIGHT16;
                                    } else {
                                        // p is a tailored primary.
                                        assert(s == Collation.COMMON_WEIGHT16);
                                        sLimit = rootElements.getSecondaryBoundary();
                                    }
                                    if(s == Collation.COMMON_WEIGHT16) {
                                        // Do not tailor into the getSortKey() range of
                                        // compressed common secondaries.
                                        s = rootElements.getLastCommonSecondary();
                                    }
                                    secondaries.initForSecondary();
                                    if(!secondaries.allocWeights(s, sLimit, sCount)) {
                                        // C++ U_BUFFER_OVERFLOW_ERROR
                                        if(DEBUG) {
                                            System.out.printf("!secondaries.allocWeights(%x, %x, sCount=%d)\n",
                                                    alignWeightRight(s), alignWeightRight(sLimit),
                                                    alignWeightRight(sCount));
                                        }
                                        throw new UnsupportedOperationException("secondary tailoring gap too small");
                                    }
                                    sIsTailored = true;
                                }
                                s = (int)secondaries.nextWeight();
                                assert(s != 0xffffffff);
                            } else {
                                s = weight16FromNode(node);
                                sIsTailored = false;
                                if(DEBUG) {
                                    System.out.printf("  sec       %x\n", alignWeightRight(s));
                                }
                            }
                        } else /* Collator.PRIMARY */ {
                            assert(isTailoredNode(node));
                            if(DEBUG) {
                                System.out.print("pri+            ");
                            }
                            if(!pIsTailored) {
                                // First tailored primary node in this list.
                                int pCount = countTailoredNodes(nodesArray, nextIndex,
                                                                    Collator.PRIMARY) + 1;
                                boolean isCompressible = baseData.isCompressiblePrimary(p);
                                long pLimit =
                                    rootElements.getPrimaryAfter(p, pIndex, isCompressible);
                                primaries.initForPrimary(isCompressible);
                                if(!primaries.allocWeights(p, pLimit, pCount)) {
                                    // C++ U_BUFFER_OVERFLOW_ERROR  // TODO: introduce a more specific UErrorCode?
                                    throw new UnsupportedOperationException("primary tailoring gap too small");
                                }
                                pIsTailored = true;
                            }
                            p = primaries.nextWeight();
                            assert(p != 0xffffffffL);
                            s = Collation.COMMON_WEIGHT16;
                            sIsTailored = false;
                        }
                        t = s == 0 ? 0 : Collation.COMMON_WEIGHT16;
                        tIsTailored = false;
                    }
                    q = 0;
                }
                if(isTailoredNode(node)) {
                    nodesArray[i] = Collation.makeCE(p, s, t, q);
                    if(DEBUG) {
                        System.out.printf("%016x\n", nodesArray[i]);
                    }
                }
            }
        }
    }

    /**
     * Counts the tailored nodes of the given strength up to the next node
     * which is either stronger or has an explicit weight of this strength.
     */
    private static int countTailoredNodes(long[] nodesArray, int i, int strength) {
        int count = 0;
        for(;;) {
            if(i == 0) { break; }
            long node = nodesArray[i];
            if(strengthFromNode(node) < strength) { break; }
            if(strengthFromNode(node) == strength) {
                if(isTailoredNode(node)) {
                    ++count;
                } else {
                    break;
                }
            }
            i = nextIndexFromNode(node);
        }
        return count;
    }

    private static final class CEFinalizer implements CollationDataBuilder.CEModifier {
        CEFinalizer(long[] ces) {
            finalCEs = ces;
        }
        @Override
        public long modifyCE32(int ce32) {
            assert(!Collation.isSpecialCE32(ce32));
            if(CollationBuilder.isTempCE32(ce32)) {
                // retain case bits
                return finalCEs[CollationBuilder.indexFromTempCE32(ce32)] | ((ce32 & 0xc0) << 8);
            } else {
                return Collation.NO_CE;
            }
        }
        @Override
        public long modifyCE(long ce) {
            if(CollationBuilder.isTempCE(ce)) {
                // retain case bits
                return finalCEs[CollationBuilder.indexFromTempCE(ce)] | (ce & 0xc000);
            } else {
                return Collation.NO_CE;
            }
        }

        private long[] finalCEs;
    };

    /** Replaces temporary CEs with the final CEs they point to. */
    private void finalizeCEs() {
        CollationDataBuilder newBuilder = new CollationDataBuilder();
        newBuilder.initForTailoring(baseData);
        CEFinalizer finalizer = new CEFinalizer(nodes.getBuffer());
        newBuilder.copyFrom(dataBuilder, finalizer);
        dataBuilder = newBuilder;
    }

    /**
     * Encodes "temporary CE" data into a CE that fits into the CE32 data structure,
     * with 2-byte primary, 1-byte secondary and 6-bit tertiary,
     * with valid CE byte values.
     *
     * The index must not exceed 20 bits (0xfffff).
     * The strength must fit into 2 bits (Collator.PRIMARY..Collator.QUATERNARY).
     *
     * Temporary CEs are distinguished from real CEs by their use of
     * secondary weights 06..45 which are otherwise reserved for compressed sort keys.
     *
     * The case bits are unused and available.
     */
    private static long tempCEFromIndexAndStrength(int index, int strength) {
        return
            // CE byte offsets, to ensure valid CE bytes, and case bits 11
            0x4040000006002000L +
            // index bits 19..13 -> primary byte 1 = CE bits 63..56 (byte values 40..BF)
            ((long)(index & 0xfe000) << 43) +
            // index bits 12..6 -> primary byte 2 = CE bits 55..48 (byte values 40..BF)
            ((long)(index & 0x1fc0) << 42) +
            // index bits 5..0 -> secondary byte 1 = CE bits 31..24 (byte values 06..45)
            ((index & 0x3f) << 24) +
            // strength bits 1..0 -> tertiary byte 1 = CE bits 13..8 (byte values 20..23)
            (strength << 8);
    }
    private static int indexFromTempCE(long tempCE) {
        tempCE -= 0x4040000006002000L;
        return
            ((int)(tempCE >> 43) & 0xfe000) |
            ((int)(tempCE >> 42) & 0x1fc0) |
            ((int)(tempCE >> 24) & 0x3f);
    }
    private static int strengthFromTempCE(long tempCE) {
        return ((int)tempCE >> 8) & 3;
    }
    private static boolean isTempCE(long ce) {
        int sec = (int)ce >>> 24;
        return 6 <= sec && sec <= 0x45;
    }

    private static int indexFromTempCE32(int tempCE32) {
        tempCE32 -= 0x40400620;
        return
            ((tempCE32 >> 11) & 0xfe000) |
            ((tempCE32 >> 10) & 0x1fc0) |
            ((tempCE32 >> 8) & 0x3f);
    }
    private static boolean isTempCE32(int ce32) {
        return
            (ce32 & 0xff) >= 2 &&  // not a long-primary/long-secondary CE32
            6 <= ((ce32 >> 8) & 0xff) && ((ce32 >> 8) & 0xff) <= 0x45;
    }

    private static int ceStrength(long ce) {
        return
            isTempCE(ce) ? strengthFromTempCE(ce) :
            (ce & 0xff00000000000000L) != 0 ? Collator.PRIMARY :
            ((int)ce & 0xff000000) != 0 ? Collator.SECONDARY :
            ce != 0 ? Collator.TERTIARY :
            Collator.IDENTICAL;
    }

    /** At most 1M nodes, limited by the 20 bits in node bit fields. */
    private static final int MAX_INDEX = 0xfffff;
    /**
     * Node bit 6 is set on a primary node if there are nodes
     * with secondary values below the common secondary weight (05).
     */
    private static final int HAS_BEFORE2 = 0x40;
    /**
     * Node bit 5 is set on a primary or secondary node if there are nodes
     * with tertiary values below the common tertiary weight (05).
     */
    private static final int HAS_BEFORE3 = 0x20;
    /**
     * Node bit 3 distinguishes a tailored node, which has no weight value,
     * from a node with an explicit (root or default) weight.
     */
    private static final int IS_TAILORED = 8;

    private static long nodeFromWeight32(long weight32) {
        return weight32 << 32;
    }
    private static long nodeFromWeight16(int weight16) {
        return (long)weight16 << 48;
    }
    private static long nodeFromPreviousIndex(int previous) {
        return (long)previous << 28;
    }
    private static long nodeFromNextIndex(int next) {
        return next << 8;
    }
    private static long nodeFromStrength(int strength) {
        return strength;
    }

    private static long weight32FromNode(long node) {
        return node >>> 32;
    }
    private static int weight16FromNode(long node) {
        return (int)(node >> 48) & 0xffff;
    }
    private static int previousIndexFromNode(long node) {
        return (int)(node >> 28) & MAX_INDEX;
    }
    private static int nextIndexFromNode(long node) {
        return ((int)node >> 8) & MAX_INDEX;
    }
    private static int strengthFromNode(long node) {
        return (int)node & 3;
    }

    private static boolean nodeHasBefore2(long node) {
        return (node & HAS_BEFORE2) != 0;
    }
    private static boolean nodeHasBefore3(long node) {
        return (node & HAS_BEFORE3) != 0;
    }
    private static boolean nodeHasAnyBefore(long node) {
        return (node & (HAS_BEFORE2 | HAS_BEFORE3)) != 0;
    }
    private static boolean isTailoredNode(long node) {
        return (node & IS_TAILORED) != 0;
    }

    private static long changeNodePreviousIndex(long node, int previous) {
        return (node & 0xffff00000fffffffL) | nodeFromPreviousIndex(previous);
    }
    private static long changeNodeNextIndex(long node, int next) {
        return (node & 0xfffffffff00000ffL) | nodeFromNextIndex(next);
    }

    private Normalizer2 nfd, fcd;
    private Normalizer2Impl nfcImpl;

    private CollationTailoring base;
    private CollationData baseData;
    private CollationRootElements rootElements;
    private long variableTop;

    private CollationDataBuilder dataBuilder;
    private boolean fastLatinEnabled;
    private UnicodeSet optimizeSet = new UnicodeSet();

    private long[] ces = new long[Collation.MAX_EXPANSION_LENGTH];
    private int cesLength;

    /**
     * Indexes of nodes with root primary weights, sorted by primary.
     * Compact form of a TreeMap from root primary to node index.
     *
     * This is a performance optimization for finding reset positions.
     * Without this, we would have to search through the entire nodes list.
     * It also allows storing root primary weights in list head nodes,
     * without previous index, leaving room in root primary nodes for 32-bit primary weights.
     */
    private UVector32 rootPrimaryIndexes;
    /**
     * Data structure for assigning tailored weights and CEs.
     * Doubly-linked lists of nodes in mostly collation order.
     * Each list starts with a root primary node and ends with a nextIndex of 0.
     *
     * When there are any nodes in the list, then there is always a root primary node at index 0.
     * This allows some code not to have to check explicitly for nextIndex==0.
     *
     * Root primary nodes have 32-bit weights but do not have previous indexes.
     * All other nodes have at most 16-bit weights and do have previous indexes.
     *
     * Nodes with explicit weights store root collator weights,
     * or default weak weights (e.g., secondary 05) for stronger nodes.
     * "Tailored" nodes, with the IS_TAILORED bit set,
     * do not store explicit weights but rather
     * create a difference of a certain strength from the preceding node.
     *
     * A root node is followed by either
     * - a root/default node of the same strength, or
     * - a root/default node of the next-weaker strength, or
     * - a tailored node of the same strength.
     *
     * A node of a given strength normally implies "common" weights on weaker levels.
     *
     * A node with HAS_BEFORE2 must be immediately followed by
     * a secondary node with an explicit below-common weight, then a secondary tailored node,
     * and later an explicit common-secondary node.
     * The below-common weight can be a root weight,
     * or it can be BEFORE_WEIGHT16 for tailoring before an implied common weight
     * or before the lowest root weight.
     * (&[before 2] resets to an explicit secondary node so that
     * the following addRelation(secondary) tailors right after that.
     * If we did not have this node and instead were to reset on the primary node,
     * then addRelation(secondary) would skip forward to the the COMMON_WEIGHT16 node.)
     *
     * If the flag is not set, then there are no explicit secondary nodes
     * with the common or lower weights.
     *
     * Same for HAS_BEFORE3 for tertiary nodes and weights.
     * A node must not have both flags set.
     *
     * Tailored CEs are initially represented in a CollationDataBuilder as temporary CEs
     * which point to stable indexes in this list,
     * and temporary CEs stored in a CollationDataBuilder only point to tailored nodes.
     *
     * A temporary CE in the ces[] array may point to a non-tailored reset-before-position node,
     * until the next relation is added.
     *
     * At the end, the tailored weights are allocated as necessary,
     * then the tailored nodes are replaced with final CEs,
     * and the CollationData is rewritten by replacing temporary CEs with final ones.
     *
     * We cannot simply insert new nodes in the middle of the array
     * because that would invalidate the indexes stored in existing temporary CEs.
     * We need to use a linked graph with stable indexes to existing nodes.
     * A doubly-linked list seems easiest to maintain.
     *
     * Each node is stored as an long, with its fields stored as bit fields.
     *
     * Root primary node:
     * - primary weight: 32 bits 63..32
     * - reserved/unused/zero: 4 bits 31..28
     *
     * Weaker root nodes & tailored nodes:
     * - a weight: 16 bits 63..48
     *   + a root or default weight for a non-tailored node
     *   + unused/zero for a tailored node
     * - index to the previous node: 20 bits 47..28
     *
     * All types of nodes:
     * - index to the next node: 20 bits 27..8
     *   + nextIndex=0 in last node per root-primary list
     * - reserved/unused/zero bits: bits 7, 4, 2
     * - HAS_BEFORE2: bit 6
     * - HAS_BEFORE3: bit 5
     * - IS_TAILORED: bit 3
     * - the difference strength (primary/secondary/tertiary/quaternary): 2 bits 1..0
     *
     * We could allocate structs with pointers, but we would have to store them
     * in a pointer list so that they can be indexed from temporary CEs,
     * and they would require more memory allocations.
     */
    private UVector64 nodes;
}
