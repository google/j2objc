/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2014, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */


// NOTE:  This class is identical to java.text.StringCharacterIterator
// in JDK 1.2.  It's copied here because the JDK 1.1 version of
// StringCharacterIterator has a bug that prevents it from working
// right with RuleBasedBreakIterator.  This class is unnecessary
// when using RuleBasedBreakIterator with JDK 1.2.

package android.icu.text;
import java.text.CharacterIterator;

import android.icu.util.ICUCloneNotSupportedException;

/**
 * <code>StringCharacterIterator</code> implements the
 * <code>CharacterIterater</code> protocol for a <code>String</code>.
 * The <code>StringCharacterIterator</code> class iterates over the
 * entire <code>String</code>.
 *
 * @see CharacterIterator
 * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
 * @hide Only a subset of ICU is exposed in Android
 */
@Deprecated
///CLOVER:OFF
public final class StringCharacterIterator implements CharacterIterator
{
    private String text;
    private int begin;
    private int end;
    // invariant: begin <= pos <= end
    private int pos;

    /**
     * Constructs an iterator with an initial index of 0.
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public StringCharacterIterator(String text)
    {
        this(text, 0);
    }

    /**
     * Constructs an iterator with the specified initial index.
     *
     * @param  text   The String to be iterated over
     * @param  pos    Initial iterator position     
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public StringCharacterIterator(String text, int pos)
    {
    this(text, 0, text.length(), pos);
    }

    /**
     * Constructs an iterator over the given range of the given string, with the
     * index set at the specified position.
     *
     * @param  text   The String to be iterated over
     * @param  begin  Index of the first character
     * @param  end    Index of the character following the last character
     * @param  pos    Initial iterator position 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public StringCharacterIterator(String text, int begin, int end, int pos) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;

        if (begin < 0 || begin > end || end > text.length()) {
            throw new IllegalArgumentException("Invalid substring range");
        }

        if (pos < begin || pos > end) {
            throw new IllegalArgumentException("Invalid position");
        }

        this.begin = begin;
        this.end = end;
        this.pos = pos;
    }

    /**
     * Reset this iterator to point to a new string.  This package-visible
     * method is used by other java.text classes that want to avoid allocating
     * new StringCharacterIterator objects every time their setText method
     * is called.
     *
     * @param  text   The String to be iterated over 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public void setText(String text) {
        if (text == null) {
            throw new NullPointerException();
        }
        this.text = text;
        this.begin = 0;
        this.end = text.length();
        this.pos = 0;
    }

    /**
     * Implements CharacterIterator.first() for String.
     * @see CharacterIterator#first 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public char first()
    {
        pos = begin;
        return current();
    }

    /**
     * Implements CharacterIterator.last() for String.
     * @see CharacterIterator#last 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public char last()
    {
        if (end != begin) {
            pos = end - 1;
        } else {
            pos = end;
        }
        return current();
     }

    /**
     * Implements CharacterIterator.setIndex() for String.
     * @see CharacterIterator#setIndex 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public char setIndex(int p)
    {
    if (p < begin || p > end) {
            throw new IllegalArgumentException("Invalid index");
    }
        pos = p;
        return current();
    }

    /**
     * Implements CharacterIterator.current() for String.
     * @see CharacterIterator#current 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public char current()
    {
        if (pos >= begin && pos < end) {
            return text.charAt(pos);
        }
        else {
            return DONE;
        }
    }

    /**
     * Implements CharacterIterator.next() for String.
     * @see CharacterIterator#next 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public char next()
    {
        if (pos < end - 1) {
            pos++;
            return text.charAt(pos);
        }
        else {
            pos = end;
            return DONE;
        }
    }

    /**
     * Implements CharacterIterator.previous() for String.
     * @see CharacterIterator#previous 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public char previous()
    {
        if (pos > begin) {
            pos--;
            return text.charAt(pos);
        }
        else {
            return DONE;
        }
    }

    /**
     * Implements CharacterIterator.getBeginIndex() for String.
     * @see CharacterIterator#getBeginIndex 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public int getBeginIndex()
    {
        return begin;
    }

    /**
     * Implements CharacterIterator.getEndIndex() for String.
     * @see CharacterIterator#getEndIndex 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public int getEndIndex()
    {
        return end;
    }

    /**
     * Implements CharacterIterator.getIndex() for String.
     * @see CharacterIterator#getIndex 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public int getIndex()
    {
        return pos;
    }

    /**
     * Compares the equality of two StringCharacterIterator objects.
     * @param obj the StringCharacterIterator object to be compared with.
     * @return true if the given obj is the same as this
     * StringCharacterIterator object; false otherwise. 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StringCharacterIterator)) {
            return false;
        }

        StringCharacterIterator that = (StringCharacterIterator) obj;

        if (hashCode() != that.hashCode()) {
            return false;
        }
        if (!text.equals(that.text)) {
            return false;
        }
        if (pos != that.pos || begin != that.begin || end != that.end) {
            return false;
        }
        return true;
    }

    /**
     * Computes a hashcode for this iterator.
     * @return A hash code 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public int hashCode()
    {
        return text.hashCode() ^ pos ^ begin ^ end;
    }

    /**
     * Creates a copy of this iterator.
     * @return A copy of this 
     * @deprecated ICU 2.4. Use java.text.StringCharacterIterator instead.
     */
    @Deprecated
    public Object clone()
    {
        try {
            StringCharacterIterator other
            = (StringCharacterIterator) super.clone();
            return other;
        }
        catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException(e);
        }
    }

}
///CLOVER:ON
