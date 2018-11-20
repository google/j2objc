/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2001-2010, International Business Machines Corporation and    *
 * others. All Rights Reserved.                                                *
 *******************************************************************************
 */
package android.icu.dev.test.iterator;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.ReplaceableString;
import android.icu.text.UCharacterIterator;
import android.icu.text.UTF16;

/**
 * @author ram
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TestUCharacterIterator extends TestFmwk{

    // constructor -----------------------------------------------------
  
    /**
     * Constructor
     */
    public TestUCharacterIterator()
    {
    }
  
    // public methods --------------------------------------------------
  
    /**
    * Testing cloning
    */
    @Test
    public void TestClone() throws CloneNotSupportedException
    {
         UCharacterIterator iterator = UCharacterIterator.getInstance("testing");
         UCharacterIterator cloned = (UCharacterIterator)iterator.clone();
         int completed = 0;
         while (completed != UCharacterIterator.DONE) {
            completed = iterator.next();
            if (completed != cloned.next()) {
                errln("Cloned operation failed");
            }
         }
    }
    public void getText(UCharacterIterator iterator, String result){
        /* test getText */
        char[] buf= new char[1];
        for(;;){
            try{
                iterator.getText(buf);
                break;
            }catch(IndexOutOfBoundsException e){
                buf = new char[iterator.getLength()];
            }
        }
        if(result.compareTo(new String(buf,0,iterator.getLength()))!=0){
            errln("getText failed for iterator");
        }
    }
    
    /**
     * Testing iteration
     */
    @Test
    public void TestIteration()
    {
        UCharacterIterator iterator  = UCharacterIterator.getInstance(
                                                       ITERATION_STRING_);
        UCharacterIterator iterator2 = UCharacterIterator.getInstance(
                                                       ITERATION_STRING_);
        iterator.setToStart();                                               
        if (iterator.current() != ITERATION_STRING_.charAt(0)) {
            errln("Iterator failed retrieving first character");
        }
        iterator.setToLimit(); 
        if (iterator.previous() != ITERATION_STRING_.charAt(
                                       ITERATION_STRING_.length() - 1)) {
            errln("Iterator failed retrieving last character");
        }                                               
        if (iterator.getLength() != ITERATION_STRING_.length()) {
            errln("Iterator failed determining begin and end index");
        }  
        iterator2.setIndex(0);
        iterator.setIndex(0);
        int ch = 0;
        while (ch != UCharacterIterator.DONE) {
            int index = iterator2.getIndex();
            ch = iterator2.nextCodePoint();
            if (index != ITERATION_SUPPLEMENTARY_INDEX) {
                if (ch != (int)iterator.next() && 
                    ch != UCharacterIterator.DONE) {
                    errln("Error mismatch in next() and nextCodePoint()"); 
                }
            }
            else {
                if (UTF16.getLeadSurrogate(ch) != iterator.next() ||
                    UTF16.getTrailSurrogate(ch) != iterator.next()) {
                    errln("Error mismatch in next and nextCodePoint for " +
                          "supplementary characters");
                }
            }
        }
        iterator.setIndex(ITERATION_STRING_.length());
        iterator2.setIndex(ITERATION_STRING_.length());
        while (ch != UCharacterIterator.DONE) {
            int index = iterator2.getIndex();
            ch = iterator2.previousCodePoint();
            if (index != ITERATION_SUPPLEMENTARY_INDEX) {
                if (ch != (int)iterator.previous() && 
                    ch != UCharacterIterator.DONE) {
                    errln("Error mismatch in previous() and " +
                          "previousCodePoint()"); 
                }
            }
            else {
                if (UTF16.getLeadSurrogate(ch) != iterator.previous() || 
                    UTF16.getTrailSurrogate(ch) != iterator.previous()) {
                    errln("Error mismatch in previous and " +
                          "previousCodePoint for supplementary characters");
                }
            }
        }
    }
    
    //Tests for new API for utf-16 support 
    @Test
    public void TestIterationUChar32() {
        String text="\u0061\u0062\ud841\udc02\u20ac\ud7ff\ud842\udc06\ud801\udc00\u0061";
        int c;
        int i;
        {
            UCharacterIterator iter = UCharacterIterator.getInstance(text);
    
            String iterText = iter.getText();
            if (!iterText.equals(text))
              errln("iter.getText() failed");
            
            iter.setIndex(1);
            if (iter.currentCodePoint() != UTF16.charAt(text,1))
                errln("Iterator didn't start out in the right place.");
    
            iter.setToStart();
            c=iter.currentCodePoint();
            i=0;
            i=iter.moveCodePointIndex(1);
            c=iter.currentCodePoint();
            if(c != UTF16.charAt(text,1) || i!=1)
                errln("moveCodePointIndex(1) didn't work correctly expected "+ hex(c) +" got "+hex(UTF16.charAt(text,1)) + " i= " + i);
    
            i=iter.moveCodePointIndex(2);
            c=iter.currentCodePoint();
            if(c != UTF16.charAt(text,4) || i!=4)
                errln("moveCodePointIndex(2) didn't work correctly expected "+ hex(c) +" got "+hex(UTF16.charAt(text,4)) + " i= " + i);
                
            i=iter.moveCodePointIndex(-2);
            c=iter.currentCodePoint();
            if(c != UTF16.charAt(text,1) || i!=1)
                 errln("moveCodePointIndex(-2) didn't work correctly expected "+ hex(c) +" got "+hex(UTF16.charAt(text,1)) + " i= " + i);

            iter.setToLimit();
            i=iter.moveCodePointIndex(-2);
            c=iter.currentCodePoint();
            if(c != UTF16.charAt(text,(text.length()-3)) || i!=(text.length()-3))
                errln("moveCodePointIndex(-2) didn't work correctly expected "+ hex(c) +" got "+hex(UTF16.charAt(text,(text.length()-3)) ) + " i= " + i);
            
            iter.setToStart();
            c = iter.currentCodePoint();
            i = 0;
    
            //testing first32PostInc, nextCodePointPostInc, setTostart
            i = 0;
            iter.setToStart();
            c =iter.next();
            if(c != UTF16.charAt(text,i))
                errln("first32PostInc failed.  Expected->"+hex(UTF16.charAt(text,i))+" Got-> "+hex(c));
            if(iter.getIndex() != UTF16.getCharCount(c) + i)
                errln("getIndex() after first32PostInc() failed");
    
            iter.setToStart();
            i=0;
            if (iter.getIndex() != 0)
                errln("setToStart failed");
           
            logln("Testing forward iteration...");
            do {
                if (c != UCharacterIterator.DONE)
                    c = iter.nextCodePoint();
    
                if(c != UTF16.charAt(text,i))
                    errln("Character mismatch at position "+i+", iterator has "+hex(c)+", string has "+hex(UTF16.charAt(text,i)));
    
                i+=UTF16.getCharCount(c);
                if(iter.getIndex() != i)
                    errln("getIndex() aftr nextCodePointPostInc() isn't working right");
                c = iter.currentCodePoint();                   
                if( c!=UCharacterIterator.DONE && c != UTF16.charAt(text,i))
                    errln("current() after nextCodePointPostInc() isn't working right");

            } while (c!=UCharacterIterator.DONE);
            c=iter.nextCodePoint();
            if(c!= UCharacterIterator.DONE)
                errln("nextCodePointPostInc() didn't return DONE at the beginning");
    
    
        }
    }  
    
    class UCharIterator {
    
       public UCharIterator(int[] src, int len, int index){
            
            s=src;
            length=len;
            i=index;
       }
    
        public int current() {
            if(i<length) {
                return s[i];
            } else {
                return -1;
            }
        }
    
        public int next() {
            if(i<length) {
                return s[i++];
            } else {
                return -1;
            }
        }
    
        public int previous() {
            if(i>0) {
                return s[--i];
            } else {
                return -1;
            }
        }
    
        public int getIndex() {
            return i;
        }
    
        private int[] s;
        private int length, i;
    }
    @Test
    public void TestPreviousNext(){
        // src and expect strings
        char src[]={
                UTF16.getLeadSurrogate(0x2f999), UTF16.getTrailSurrogate(0x2f999),
                UTF16.getLeadSurrogate(0x1d15f), UTF16.getTrailSurrogate(0x1d15f),
                0xc4,
                0x1ed0
            };
        // iterators
        UCharacterIterator iter1 = UCharacterIterator.getInstance(new ReplaceableString(new String(src)));
        UCharacterIterator iter2 = UCharacterIterator.getInstance(src/*char array*/);
        UCharacterIterator iter3 = UCharacterIterator.getInstance(new StringCharacterIterator(new String(src)));
        UCharacterIterator iter4 = UCharacterIterator.getInstance(new StringBuffer(new String(src)));
        previousNext(iter1);
        previousNext(iter2);
        previousNext(iter3);
        previousNext(iter4);
        getText(iter1,new String(src));
        getText(iter2,new String(src));
        getText(iter3,new String(src));
        /* getCharacterIterator */
        CharacterIterator citer1 = iter1.getCharacterIterator();
        CharacterIterator citer2 = iter2.getCharacterIterator();
        CharacterIterator citer3 = iter3.getCharacterIterator();
        if(citer1.first() !=iter1.current()){
            errln("getCharacterIterator for iter1 failed");
        }
        if(citer2.first() !=iter2.current()){
            errln("getCharacterIterator for iter2 failed");
        }
        if(citer3.first() !=iter3.current()){
            errln("getCharacterIterator for iter3 failed");
        }
        /* Test clone()  && moveIndex()*/
        try{
            UCharacterIterator clone1 = (UCharacterIterator)iter1.clone();
            UCharacterIterator clone2 = (UCharacterIterator)iter2.clone();
            UCharacterIterator clone3 = (UCharacterIterator)iter3.clone();
            if(clone1.moveIndex(3)!=iter1.moveIndex(3)){
                errln("moveIndex for iter1 failed");
            }
            if(clone2.moveIndex(3)!=iter2.moveIndex(3)){
                errln("moveIndex for iter2 failed");
            }
            if(clone3.moveIndex(3)!=iter3.moveIndex(3)){
                errln("moveIndex for iter1 failed");
            }
        }catch (Exception e){
            errln("could not clone the iterator");
        }
    }
    public void previousNext(UCharacterIterator iter) {

        int expect[]={
            0x2f999,
            0x1d15f,
            0xc4,
            0x1ed0
        };
    
        // expected src indexes corresponding to expect indexes
        int expectIndex[]={
            0,0,
            1,1,
            2,
            3,
            4 //needed 
        };
    
        // initial indexes into the src and expect strings
        
        final int SRC_MIDDLE=4;
        final int EXPECT_MIDDLE=2;
        
    
        // movement vector
        // - for previous(), 0 for current(), + for next()
        // not const so that we can terminate it below for the error message
        String moves="0+0+0--0-0-+++0--+++++++0--------";
    
        
        UCharIterator iter32 = new UCharIterator(expect, expect.length, 
                                                     EXPECT_MIDDLE);
    
        int c1, c2;
        char m;
    
        // initially set the indexes into the middle of the strings
        iter.setIndex(SRC_MIDDLE);
    
        // move around and compare the iteration code points with
        // the expected ones
        int movesIndex =0;
        while(movesIndex<moves.length()) {
            m=moves.charAt(movesIndex++);
            if(m=='-') {
                c1=iter.previousCodePoint();
                c2=iter32.previous();
            } else if(m=='0') {
                c1=iter.currentCodePoint();
                c2=iter32.current();
            } else  {// m=='+' 
                c1=iter.nextCodePoint();
                c2=iter32.next();
            }
    
            // compare results
            if(c1!=c2) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: mismatch in Normalizer iteration at "+history+": "
                      +"got c1= " + hex(c1) +" != expected c2= "+ hex(c2));
                break;
            }
    
            // compare indexes
            if(expectIndex[iter.getIndex()]!=iter32.getIndex()) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: index mismatch in Normalizer iteration at "
                      +history+ " : "+ "Normalizer index " +iter.getIndex()
                      +" expected "+ expectIndex[iter32.getIndex()]);
                break;
            }
        }
    }
    @Test
    public void TestUCharacterIteratorWrapper(){
        String source ="asdfasdfjoiuyoiuy2341235679886765";
        UCharacterIterator it = UCharacterIterator.getInstance(source);
        CharacterIterator wrap_ci = it.getCharacterIterator();
        CharacterIterator ci = new StringCharacterIterator(source);
        wrap_ci.setIndex(10);
        ci.setIndex(10);
        String moves="0+0+0--0-0-+++0--+++++++0--------++++0000----0-";
        int c1, c2;
        char m;
        int movesIndex =0;
        
        while(movesIndex<moves.length()) {
            m=moves.charAt(movesIndex++);
            if(m=='-') {
                c1=wrap_ci.previous();
                c2=ci.previous();
            } else if(m=='0') {
                c1=wrap_ci.current();
                c2=ci.current();
            } else  {// m=='+' 
                c1=wrap_ci.next();
                c2=ci.next();
            }
    
            // compare results
            if(c1!=c2) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: mismatch in Normalizer iteration at "+history+": "
                      +"got c1= " + hex(c1) +" != expected c2= "+ hex(c2));
                break;
            }
    
            // compare indexes
            if(wrap_ci.getIndex()!=ci.getIndex()) {
                // copy the moves until the current (m) move, and terminate
                String history = moves.substring(0,movesIndex);
                errln("error: index mismatch in Normalizer iteration at "
                      +history+ " : "+ "Normalizer index " +wrap_ci.getIndex()
                      +" expected "+ ci.getIndex());
                break;
            }
        }
        if(ci.first()!=wrap_ci.first()){
            errln("CharacterIteratorWrapper.first() failed. expected: " + ci.first() + " got: " +wrap_ci.first());
        }
        if(ci.last()!=wrap_ci.last()){
            errln("CharacterIteratorWrapper.last() failed expected: " + ci.last() + " got: " +wrap_ci.last());
        }
        if(ci.getBeginIndex()!=wrap_ci.getBeginIndex()){
            errln("CharacterIteratorWrapper.getBeginIndex() failed expected: " + ci.getBeginIndex() + " got: " +wrap_ci.getBeginIndex());
        }
        if(ci.getEndIndex()!=wrap_ci.getEndIndex()){
            errln("CharacterIteratorWrapper.getEndIndex() failed expected: " + ci.getEndIndex() + " got: " +wrap_ci.getEndIndex());
        }
        try{
            CharacterIterator cloneWCI = (CharacterIterator) wrap_ci.clone();
            if(wrap_ci.getIndex()!=cloneWCI.getIndex()){
                errln("CharacterIteratorWrapper.clone() failed expected: " +wrap_ci.getIndex() + " got: " + cloneWCI.getIndex());
            }
        }catch(Exception e){
             errln("CharacterIterator.clone() failed");
        }
    }
    // private data members ---------------------------------------------
    
    private static final String ITERATION_STRING_ =
                                        "Testing 1 2 3 \ud800\udc00 456";
    private static final int ITERATION_SUPPLEMENTARY_INDEX = 14;
    
    @Test
    public void TestJitterbug1952(){
        //test previous code point
        char[] src = new char[]{ '\uDC00','\uD800','\uDC01','\uD802','\uDC02','\uDC03'};
        UCharacterIterator iter = UCharacterIterator.getInstance(src);
        iter.setIndex(1);
        int ch;
        // this should never go into a infinite loop
        // if it does then we have a problem
        while((ch=iter.previousCodePoint())!=UCharacterIterator.DONE){
            if(ch!=0xDc00){
                errln("iter.previousCodePoint() failed");
            }
        }
        iter.setIndex(5);
        while((ch=iter.nextCodePoint()) !=UCharacterIterator.DONE){
            if(ch!= 0xDC03){
                errln("iter.nextCodePoint() failed");
            } 
        }      
    }
        
}
