/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 1996-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.impl;

import java.text.CharacterIterator;

import android.icu.text.UCharacterIterator;

/**
 * This class is a wrapper around CharacterIterator and implements the
 * UCharacterIterator protocol
 * @author ram
 * @hide Only a subset of ICU is exposed in Android
 */

public class CharacterIteratorWrapper extends UCharacterIterator {

    private CharacterIterator iterator;


    public CharacterIteratorWrapper(CharacterIterator iter){
        if(iter==null){
            throw new IllegalArgumentException();
        }
        iterator     = iter;
    }

    /**
     * @see UCharacterIterator#current()
     */
    @Override
    public int current() {
        int c = iterator.current();
        if(c==CharacterIterator.DONE){
          return DONE;
        }
        return c;
    }

    /**
     * @see UCharacterIterator#getLength()
     */
    @Override
    public int getLength() {
        return (iterator.getEndIndex() - iterator.getBeginIndex());
    }

    /**
     * @see UCharacterIterator#getIndex()
     */
    @Override
    public int getIndex() {
        return iterator.getIndex();
    }

    /**
     * @see UCharacterIterator#next()
     */
    @Override
    public int next() {
        int i = iterator.current();
        iterator.next();
        if(i==CharacterIterator.DONE){
          return DONE;
        }
        return i;
    }

    /**
     * @see UCharacterIterator#previous()
     */
    @Override
    public int previous() {
        int i = iterator.previous();
        if(i==CharacterIterator.DONE){
            return DONE;
        }
        return i;
    }

    /**
     * @see UCharacterIterator#setIndex(int)
     */
    @Override
    public void setIndex(int index) {
        try{
            iterator.setIndex(index);
        }catch(IllegalArgumentException e){
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * @see UCharacterIterator#setToLimit()
     */
    @Override
    public void setToLimit() {
        iterator.setIndex(iterator.getEndIndex());
    }

    /**
     * @see UCharacterIterator#getText(char[])
     */
    @Override
    public int getText(char[] fillIn, int offset){
        int length =iterator.getEndIndex() - iterator.getBeginIndex();
        int currentIndex = iterator.getIndex();
        if(offset < 0 || offset + length > fillIn.length){
            throw new IndexOutOfBoundsException(Integer.toString(length));
        }

        for (char ch = iterator.first(); ch != CharacterIterator.DONE; ch = iterator.next()) {
            fillIn[offset++] = ch;
        }
        iterator.setIndex(currentIndex);

        return length;
    }

    /**
     * Creates a clone of this iterator.  Clones the underlying character iterator.
     * @see UCharacterIterator#clone()
     */
    @Override
    public Object clone(){
        try {
            CharacterIteratorWrapper result = (CharacterIteratorWrapper) super.clone();
            result.iterator = (CharacterIterator)this.iterator.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            return null; // only invoked if bad underlying character iterator
        }
    }


    @Override
    public int moveIndex(int delta){
        int length = iterator.getEndIndex() - iterator.getBeginIndex();
        int idx = iterator.getIndex()+delta;

        if(idx < 0) {
            idx = 0;
        } else if(idx > length) {
            idx = length;
        }
        return iterator.setIndex(idx);
    }

    /**
     * @see UCharacterIterator#getCharacterIterator()
     */
    @Override
    public CharacterIterator getCharacterIterator(){
        return (CharacterIterator)iterator.clone();
    }
}
