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

/**
 * The LanguageBreakEngine interface is to be used to implement any 
 * language-specific logic for break iteration.
 */
interface LanguageBreakEngine {
    /**
     * @param c A Unicode codepoint value
     * @param breakType The kind of break iterator that is wanting to make use
     *  of this engine - character, word, line, sentence
     * @return true if the engine can handle this character, false otherwise
     */
    boolean handles(int c, int breakType);

    /**
     * Implements the actual breaking logic.
     * @param text The text to break over
     * @param startPos The index of the beginning of our range
     * @param endPos The index of the possible end of our range. It is possible,
     *  however, that our range ends earlier
     * @param reverse true iff we are iterating backwards (in a call to 
     *  previous(), for example)
     * @param breakType The kind of break iterator that is wanting to make use
     *  of this engine - character, word, line, sentence
     * @param foundBreaks A Stack that the breaks found will be added to
     * @return the number of words found
     */
    int findBreaks(CharacterIterator text, int startPos, int endPos,
            boolean reverse, int breakType, DictionaryBreakEngine.DequeI foundBreaks);
}
    
    
    
