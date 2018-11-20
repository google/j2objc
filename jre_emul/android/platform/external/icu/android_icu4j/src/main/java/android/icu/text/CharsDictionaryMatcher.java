/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2014, International Business Machines Corporation and         *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.text;

import java.text.CharacterIterator;

import android.icu.util.BytesTrie.Result;
import android.icu.util.CharsTrie;

class CharsDictionaryMatcher extends DictionaryMatcher {
    private CharSequence characters;

    public CharsDictionaryMatcher(CharSequence chars) {
        characters = chars;
    }

    @Override
    public int matches(CharacterIterator text_, int maxLength, int[] lengths, int[] count_, int limit, int[] values) {
        UCharacterIterator text = UCharacterIterator.getInstance(text_);
        CharsTrie uct = new CharsTrie(characters, 0);
        int c = text.nextCodePoint();
        if (c == UCharacterIterator.DONE) {
            return 0;
        }
        Result result = uct.firstForCodePoint(c);
        // TODO: should numChars count Character.charCount?
        int numChars = 1;
        int count = 0;
        for (;;) {
            if (result.hasValue()) {
                if (count < limit) {
                    if (values != null) {
                        values[count] = uct.getValue();
                    }
                    lengths[count] = numChars;
                    count++;
                }

                if (result == Result.FINAL_VALUE) {
                    break;
                }
            } else if (result == Result.NO_MATCH) {
                break;
            }

            if (numChars >= maxLength) {
                break;
            }
            c = text.nextCodePoint();
            if (c == UCharacterIterator.DONE) {
                break;
            }
            ++numChars;
            result = uct.nextForCodePoint(c);
        }
        count_[0] = count;
        return numChars;
    }

    @Override
    public int getType() {
        return DictionaryData.TRIE_TYPE_UCHARS;
    }
}

