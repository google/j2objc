/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2016, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import static android.icu.impl.CharacterIteration.DONE32;

import java.text.CharacterIterator;
import java.util.concurrent.atomic.AtomicReferenceArray;

import android.icu.impl.CharacterIteration;
import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;

final class UnhandledBreakEngine implements LanguageBreakEngine {
    // TODO: Use two arrays of UnicodeSet, one with all frozen sets, one with unfrozen.
    // in handleChar(), update the unfrozen version, clone, freeze, replace the frozen one.

    // Note on concurrency: A single instance of UnhandledBreakEngine is shared across all
    // RuleBasedBreakIterators in a process. They may make arbitrary concurrent calls.
    // If handleChar() is updating the set of unhandled characters at the same time
    // findBreaks() or handles() is referencing it, the referencing functions must see
    // a consistent set. It doesn't matter whether they see it before or after the update,
    // but they should not see an inconsistent, changing set.
    //
    // To do this, an update is made by cloning the old set, updating the clone, then
    // replacing the old with the new. Once made visible, each set remains constant.

    // TODO: it's odd that findBreaks() can produce different results, depending
    // on which scripts have been previously seen by handleChar(). (This is not a
    // threading specific issue). Possibly stop on script boundaries?

    final AtomicReferenceArray<UnicodeSet> fHandled = new AtomicReferenceArray<UnicodeSet>(BreakIterator.KIND_TITLE + 1);
    public UnhandledBreakEngine() {
        for (int i = 0; i < fHandled.length(); i++) {
            fHandled.set(i, new UnicodeSet());
        }
    }

    @Override
    public boolean handles(int c, int breakType) {
        return (breakType >= 0 && breakType < fHandled.length()) &&
                (fHandled.get(breakType).contains(c));
    }

    @Override
    public int findBreaks(CharacterIterator text, int startPos, int endPos,
            int breakType, DictionaryBreakEngine.DequeI foundBreaks) {
        if (breakType >= 0 && breakType < fHandled.length()) {
            UnicodeSet uniset = fHandled.get(breakType);
            int c = CharacterIteration.current32(text);
            while (text.getIndex() < endPos && uniset.contains(c)) {
                CharacterIteration.next32(text);
                c = CharacterIteration.current32(text);
            }
        }
        return 0;
    }

    /**
     * Update the set of unhandled characters for the specified breakType to include
     * all that have the same script as c.
     * May be called concurrently with handles() or findBreaks().
     * Must not be called concurrently with itself.
     */
    public void handleChar(int c, int breakType) {
        if (breakType >= 0 && breakType < fHandled.length() && c != DONE32) {
            UnicodeSet originalSet = fHandled.get(breakType);
            if (!originalSet.contains(c)) {
                int script = UCharacter.getIntPropertyValue(c, UProperty.SCRIPT);
                UnicodeSet newSet = new UnicodeSet();
                newSet.applyIntPropertyValue(UProperty.SCRIPT, script);
                newSet.addAll(originalSet);
                fHandled.set(breakType, newSet);
            }
        }
    }
}
