/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2011, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011jan10
*   created by: Markus W. Scherer
*   ported from ICU4C ucharstrietest.h/.cpp
*/

package android.icu.dev.test.util;

import java.util.NoSuchElementException;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.text.UnicodeSet;
import android.icu.util.BytesTrie;
import android.icu.util.CharsTrie;
import android.icu.util.CharsTrieBuilder;
import android.icu.util.StringTrieBuilder;

public class CharsTrieTest extends TestFmwk {
    public CharsTrieTest() {}

    // All test functions have a TestNN prefix where NN is a double-digit number.
    // This is so that when tests are run in sorted order
    // the simpler ones are run first.
    // If there is a problem, the simpler ones are easier to step through.

    @Test
    public void Test00Builder() {
        builder_.clear();
        try {
            builder_.build(StringTrieBuilder.Option.FAST);
            errln("CharsTrieBuilder().build() did not throw IndexOutOfBoundsException");
            return;
        } catch(IndexOutOfBoundsException e) {
            // good
        }
        try {
            builder_.add("=", 0).add("=", 1);
            errln("CharsTrieBuilder.add() did not detect duplicates");
            return;
        } catch(IllegalArgumentException e) {
            // good
        }
    }

    private static final class StringAndValue {
        public StringAndValue(String str, int val) {
            s=str;
            value=val;
        }

        public String s;
        public int value;
    }
    // Note: C++ StringAndValue initializers converted to Java syntax
    // with Eclipse Find/Replace regular expressions:
    // Find:            \{ (".*", [-0-9xa-fA-F]+) \}
    // Replace with:    new StringAndValue($1)

    @Test
    public void Test10Empty() {
        final StringAndValue[] data={
            new StringAndValue("", 0)
        };
        checkData(data);
    }

    @Test
    public void Test11_a() {
        final StringAndValue[] data={
            new StringAndValue("a", 1)
        };
        checkData(data);
    }

    @Test
    public void Test12_a_ab() {
        final StringAndValue[] data={
            new StringAndValue("a", 1),
            new StringAndValue("ab", 100)
        };
        checkData(data);
    }

    @Test
    public void Test20ShortestBranch() {
        final StringAndValue[] data={
            new StringAndValue("a", 1000),
            new StringAndValue("b", 2000)
        };
        checkData(data);
    }

    @Test
    public void Test21Branches() {
        final StringAndValue[] data={
            new StringAndValue("a", 0x10),
            new StringAndValue("cc", 0x40),
            new StringAndValue("e", 0x100),
            new StringAndValue("ggg", 0x400),
            new StringAndValue("i", 0x1000),
            new StringAndValue("kkkk", 0x4000),
            new StringAndValue("n", 0x10000),
            new StringAndValue("ppppp", 0x40000),
            new StringAndValue("r", 0x100000),
            new StringAndValue("sss", 0x200000),
            new StringAndValue("t", 0x400000),
            new StringAndValue("uu", 0x800000),
            new StringAndValue("vv", 0x7fffffff),
            new StringAndValue("zz", 0x80000000)
        };
        for(int length=2; length<=data.length; ++length) {
            logln("TestBranches length="+length);
            checkData(data, length);
        }
    }

    @Test
    public void Test22LongSequence() {
        final StringAndValue[] data={
            new StringAndValue("a", -1),
            // sequence of linear-match nodes
            new StringAndValue("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", -2),
            // more than 256 units
            new StringAndValue(
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"+
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", -3)
        };
        checkData(data);
    }

    @Test
    public void Test23LongBranch() {
        // Split-branch and interesting compact-integer values.
        final StringAndValue[] data={
            new StringAndValue("a", -2),
            new StringAndValue("b", -1),
            new StringAndValue("c", 0),
            new StringAndValue("d2", 1),
            new StringAndValue("f", 0x3f),
            new StringAndValue("g", 0x40),
            new StringAndValue("h", 0x41),
            new StringAndValue("j23", 0x1900),
            new StringAndValue("j24", 0x19ff),
            new StringAndValue("j25", 0x1a00),
            new StringAndValue("k2", 0x1a80),
            new StringAndValue("k3", 0x1aff),
            new StringAndValue("l234567890", 0x1b00),
            new StringAndValue("l234567890123", 0x1b01),
            new StringAndValue("nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", 0x10ffff),
            new StringAndValue("oooooooooooooooooooooooooooooooooooooooooooooooooooooo", 0x110000),
            new StringAndValue("pppppppppppppppppppppppppppppppppppppppppppppppppppppp", 0x120000),
            new StringAndValue("r", 0x333333),
            new StringAndValue("s2345", 0x4444444),
            new StringAndValue("t234567890", 0x77777777),
            new StringAndValue("z", 0x80000001)
        };
        checkData(data);
    }

    @Test
    public void Test24ValuesForState() {
        // Check that saveState() and resetToState() interact properly
        // with next() and current().
        final StringAndValue[] data={
            new StringAndValue("a", -1),
            new StringAndValue("ab", -2),
            new StringAndValue("abc", -3),
            new StringAndValue("abcd", -4),
            new StringAndValue("abcde", -5),
            new StringAndValue("abcdef", -6)
        };
        checkData(data);
    }

    @Test
    public void Test30Compact() {
        // Duplicate trailing strings and values provide opportunities for compacting.
        final StringAndValue[] data={
            new StringAndValue("+", 0),
            new StringAndValue("+august", 8),
            new StringAndValue("+december", 12),
            new StringAndValue("+july", 7),
            new StringAndValue("+june", 6),
            new StringAndValue("+november", 11),
            new StringAndValue("+october", 10),
            new StringAndValue("+september", 9),
            new StringAndValue("-", 0),
            new StringAndValue("-august", 8),
            new StringAndValue("-december", 12),
            new StringAndValue("-july", 7),
            new StringAndValue("-june", 6),
            new StringAndValue("-november", 11),
            new StringAndValue("-october", 10),
            new StringAndValue("-september", 9),
            // The l+n branch (with its sub-nodes) is a duplicate but will be written
            // both times because each time it follows a different linear-match node.
            new StringAndValue("xjuly", 7),
            new StringAndValue("xjune", 6)
        };
        checkData(data);
    }

    @Test
    public void Test31FirstForCodePoint() {
        final StringAndValue[] data={
            new StringAndValue("a", 1),
            new StringAndValue("a\ud800", 2),
            new StringAndValue("a\ud800\udc00", 3),  // "a\\U00010000"
            new StringAndValue("\ud840", 4),
            new StringAndValue("\ud840\udc00\udbff", 5),  // "\\U00020000\udbff"
            new StringAndValue("\ud840\udc00\udbff\udfff", 6),  // "\\U00020000\\U0010ffff"
            new StringAndValue("\ud840\udc00\udbff\udfffz", 7),  // "\\U00020000\\U0010ffffz"
            new StringAndValue("\ud900\udc00xy", 8),  // "\\U00050000xy"
            new StringAndValue("\ud900\udc00xyz", 9)  // "\\U00050000xyz"
        };
        checkData(data);
    }

    @Test
    public void Test32NextForCodePoint() {
        final StringAndValue[] data={
            // "\u4dff\\U00010000\u9999\\U00020000\udfff\\U0010ffff"
            new StringAndValue("\u4dff\ud800\udc00\u9999\ud840\udc00\udfff\udbff\udfff", 2000000000),
            // "\u4dff\\U00010000\u9999\\U00020002"
            new StringAndValue("\u4dff\ud800\udc00\u9999\ud840\udc02", 44444),
            // "\u4dff\\U000103ff"
            new StringAndValue("\u4dff\ud800\udfff", 99999)
        };
        CharsTrie trie=buildTrie(data, data.length, StringTrieBuilder.Option.FAST);
        BytesTrie.Result result;
        if( (result=trie.nextForCodePoint(0x4dff))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x10000))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x9999))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x20000))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0xdfff))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x10ffff))!=BytesTrie.Result.FINAL_VALUE || result!=trie.current() ||
            trie.getValue()!=2000000000
        ) {
            errln("CharsTrie.nextForCodePoint() fails for "+data[0].s);
        }
        if( (result=trie.firstForCodePoint(0x4dff))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x10000))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x9999))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x20002))!=BytesTrie.Result.FINAL_VALUE || result!=trie.current() ||
            trie.getValue()!=44444
        ) {
            errln("CharsTrie.nextForCodePoint() fails for "+data[1].s);
        }
        if( (result=trie.reset().nextForCodePoint(0x4dff))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x10000))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x9999))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x20222))!=BytesTrie.Result.NO_MATCH || result!=trie.current()  // no match for trail surrogate
        ) {
            errln("CharsTrie.nextForCodePoint() fails for \u4dff\\U00010000\u9999\\U00020222");
        }
        if( (result=trie.reset().nextForCodePoint(0x4dff))!=BytesTrie.Result.NO_VALUE || result!=trie.current() ||
            (result=trie.nextForCodePoint(0x103ff))!=BytesTrie.Result.FINAL_VALUE || result!=trie.current() ||
            trie.getValue()!=99999
        ) {
            errln("CharsTrie.nextForCodePoint() fails for "+data[2].s);
        }
    }

    // Generate (string, value) pairs.
    // The first string (before next()) will be empty.
    private static final class Generator {
        public Generator() {
            value=4711;
            num=0;
        }
        public void next() {
            char c;
            s.setLength(0);
            s.append(c=(char)(value>>16));
            s.append((char)(value>>4));
            if((value&1)!=0) {
                s.append((char)value);
            }
            set.add(c);
            value+=((value>>5)&0x7ff)*3+1;
            ++num;
        }
        public CharSequence getString() { return s; }
        public int getValue() { return value; }
        public int countUniqueFirstChars() { return set.size(); }
        public int getIndex() { return num; }

        private StringBuilder s=new StringBuilder();
        private UnicodeSet set=new UnicodeSet();
        private int value;
        private int num;
    };

    private CharsTrie buildLargeTrie(int numUniqueFirst) {
        Generator gen=new Generator();
        builder_.clear();
        while(gen.countUniqueFirstChars()<numUniqueFirst) {
            builder_.add(gen.getString(), gen.getValue());
            gen.next();
        }
        logln("buildLargeTrie("+numUniqueFirst+") added "+gen.getIndex()+" strings");
        CharSequence trieChars=builder_.buildCharSequence(StringTrieBuilder.Option.FAST);
        logln("serialized trie size: "+trieChars.length()+" chars\n");
        return new CharsTrie(trieChars, 0);
    }

    // Exercise a large branch node.
    @Test
    public void Test37LargeTrie() {
        CharsTrie trie=buildLargeTrie(1111);
        Generator gen=new Generator();
        while(gen.countUniqueFirstChars()<1111) {
            CharSequence x=gen.getString();
            int value=gen.getValue();
            int index;
            if(x.length()==0) {
                index=0;
            } else {
                if(trie.first(x.charAt(0))==BytesTrie.Result.NO_MATCH) {
                    errln(String.format("first(first char U+%04x)=BytesTrie.Result.NO_MATCH for string %d\n",
                            Character.getNumericValue(x.charAt(0)), gen.getIndex()));
                    break;
                }
                index=1;
            }
            BytesTrie.Result result=trie.next(x, index, x.length());
            if(!result.hasValue() || result!=trie.current() || value!=trie.getValue()) {
                errln(String.format("next("+prettify(x)+")!=hasValue or "+
                                    "next()!=current() or getValue() wrong "+
                                    "for string "+gen.getIndex()));
                break;
            }
            gen.next();
        }
    }

    private CharsTrie buildMonthsTrie(StringTrieBuilder.Option buildOption) {
        // All types of nodes leading to the same value,
        // for code coverage of recursive functions.
        // In particular, we need a lot of branches on some single level
        // to exercise a split-branch node.
        final StringAndValue[] data={
            new StringAndValue("august", 8),
            new StringAndValue("jan", 1),
            new StringAndValue("jan.", 1),
            new StringAndValue("jana", 1),
            new StringAndValue("janbb", 1),
            new StringAndValue("janc", 1),
            new StringAndValue("janddd", 1),
            new StringAndValue("janee", 1),
            new StringAndValue("janef", 1),
            new StringAndValue("janf", 1),
            new StringAndValue("jangg", 1),
            new StringAndValue("janh", 1),
            new StringAndValue("janiiii", 1),
            new StringAndValue("janj", 1),
            new StringAndValue("jankk", 1),
            new StringAndValue("jankl", 1),
            new StringAndValue("jankmm", 1),
            new StringAndValue("janl", 1),
            new StringAndValue("janm", 1),
            new StringAndValue("jannnnnnnnnnnnnnnnnnnnnnnnnnnnn", 1),
            new StringAndValue("jano", 1),
            new StringAndValue("janpp", 1),
            new StringAndValue("janqqq", 1),
            new StringAndValue("janr", 1),
            new StringAndValue("januar", 1),
            new StringAndValue("january", 1),
            new StringAndValue("july", 7),
            new StringAndValue("jun", 6),
            new StringAndValue("jun.", 6),
            new StringAndValue("june", 6)
        };
        return buildTrie(data, data.length, buildOption);
    }

    @Test
    public void Test40GetUniqueValue() {
        CharsTrie trie=buildMonthsTrie(StringTrieBuilder.Option.FAST);
        long uniqueValue;
        if((uniqueValue=trie.getUniqueValue())!=0) {
            errln("unique value at root");
        }
        trie.next('j');
        trie.next('a');
        trie.next('n');
        // getUniqueValue() directly after next()
        if((uniqueValue=trie.getUniqueValue())!=((1<<1)|1)) {
            errln("not unique value 1 after \"jan\": instead "+uniqueValue);
        }
        trie.first('j');
        trie.next('u');
        if((uniqueValue=trie.getUniqueValue())!=0) {
            errln("unique value after \"ju\"");
        }
        if(trie.next('n')!=BytesTrie.Result.INTERMEDIATE_VALUE || 6!=trie.getValue()) {
            errln("not normal value 6 after \"jun\"");
        }
        // getUniqueValue() after getValue()
        if((uniqueValue=trie.getUniqueValue())!=((6<<1)|1)) {
            errln("not unique value 6 after \"jun\"");
        }
        // getUniqueValue() from within a linear-match node
        trie.first('a');
        trie.next('u');
        if((uniqueValue=trie.getUniqueValue())!=((8<<1)|1)) {
            errln("not unique value 8 after \"au\"");
        }
    }

    @Test
    public void Test41GetNextChars() {
        CharsTrie trie=buildMonthsTrie(StringTrieBuilder.Option.SMALL);
        StringBuilder buffer=new StringBuilder();
        int count=trie.getNextChars(buffer);
        if(count!=2 || !"aj".contentEquals(buffer)) {
            errln("months getNextChars()!=[aj] at root");
        }
        trie.next('j');
        trie.next('a');
        trie.next('n');
        // getNextChars() directly after next()
        buffer.setLength(0);
        count=trie.getNextChars(buffer);
        if(count!=20 || !".abcdefghijklmnopqru".contentEquals(buffer)) {
            errln("months getNextChars()!=[.abcdefghijklmnopqru] after \"jan\"");
        }
        // getNextChars() after getValue()
        trie.getValue();  // next() had returned BytesTrie.Result.INTERMEDIATE_VALUE.
        buffer.setLength(0);
        count=trie.getNextChars(buffer);
        if(count!=20 || !".abcdefghijklmnopqru".contentEquals(buffer)) {
            errln("months getNextChars()!=[.abcdefghijklmnopqru] after \"jan\"+getValue()");
        }
        // getNextChars() from a linear-match node
        trie.next('u');
        buffer.setLength(0);
        count=trie.getNextChars(buffer);
        if(count!=1 || !"a".contentEquals(buffer)) {
            errln("months getNextChars()!=[a] after \"janu\"");
        }
        trie.next('a');
        buffer.setLength(0);
        count=trie.getNextChars(buffer);
        if(count!=1 || !"r".contentEquals(buffer)) {
            errln("months getNextChars()!=[r] after \"janua\"");
        }
        trie.next('r');
        trie.next('y');
        // getNextChars() after a final match
        buffer.setLength(0);
        count=trie.getNextChars(buffer);
        if(count!=0 || buffer.length()!=0) {
            errln("months getNextChars()!=[] after \"january\"");
        }
    }

    @Test
    public void Test50IteratorFromBranch() {
        CharsTrie trie=buildMonthsTrie(StringTrieBuilder.Option.FAST);
        // Go to a branch node.
        trie.next('j');
        trie.next('a');
        trie.next('n');
        CharsTrie.Iterator iter=trie.iterator();
        // Expected data: Same as in buildMonthsTrie(), except only the suffixes
        // following "jan".
        final StringAndValue[] data={
            new StringAndValue("", 1),
            new StringAndValue(".", 1),
            new StringAndValue("a", 1),
            new StringAndValue("bb", 1),
            new StringAndValue("c", 1),
            new StringAndValue("ddd", 1),
            new StringAndValue("ee", 1),
            new StringAndValue("ef", 1),
            new StringAndValue("f", 1),
            new StringAndValue("gg", 1),
            new StringAndValue("h", 1),
            new StringAndValue("iiii", 1),
            new StringAndValue("j", 1),
            new StringAndValue("kk", 1),
            new StringAndValue("kl", 1),
            new StringAndValue("kmm", 1),
            new StringAndValue("l", 1),
            new StringAndValue("m", 1),
            new StringAndValue("nnnnnnnnnnnnnnnnnnnnnnnnnnnn", 1),
            new StringAndValue("o", 1),
            new StringAndValue("pp", 1),
            new StringAndValue("qqq", 1),
            new StringAndValue("r", 1),
            new StringAndValue("uar", 1),
            new StringAndValue("uary", 1)
        };
        checkIterator(iter, data);
        // Reset, and we should get the same result.
        logln("after iter.reset()");
        checkIterator(iter.reset(), data);
    }

    @Test
    public void Test51IteratorFromLinearMatch() {
        CharsTrie trie=buildMonthsTrie(StringTrieBuilder.Option.SMALL);
        // Go into a linear-match node.
        trie.next('j');
        trie.next('a');
        trie.next('n');
        trie.next('u');
        trie.next('a');
        CharsTrie.Iterator iter=trie.iterator();
        // Expected data: Same as in buildMonthsTrie(), except only the suffixes
        // following "janua".
        final StringAndValue[] data={
            new StringAndValue("r", 1),
            new StringAndValue("ry", 1)
        };
        checkIterator(iter, data);
        // Reset, and we should get the same result.
        logln("after iter.reset()");
        checkIterator(iter.reset(), data);
    }

    @Test
    public void Test52TruncatingIteratorFromRoot() {
        CharsTrie trie=buildMonthsTrie(StringTrieBuilder.Option.FAST);
        CharsTrie.Iterator iter=trie.iterator(4);
        // Expected data: Same as in buildMonthsTrie(), except only the first 4 characters
        // of each string, and no string duplicates from the truncation.
        final StringAndValue[] data={
            new StringAndValue("augu", -1),
            new StringAndValue("jan", 1),
            new StringAndValue("jan.", 1),
            new StringAndValue("jana", 1),
            new StringAndValue("janb", -1),
            new StringAndValue("janc", 1),
            new StringAndValue("jand", -1),
            new StringAndValue("jane", -1),
            new StringAndValue("janf", 1),
            new StringAndValue("jang", -1),
            new StringAndValue("janh", 1),
            new StringAndValue("jani", -1),
            new StringAndValue("janj", 1),
            new StringAndValue("jank", -1),
            new StringAndValue("janl", 1),
            new StringAndValue("janm", 1),
            new StringAndValue("jann", -1),
            new StringAndValue("jano", 1),
            new StringAndValue("janp", -1),
            new StringAndValue("janq", -1),
            new StringAndValue("janr", 1),
            new StringAndValue("janu", -1),
            new StringAndValue("july", 7),
            new StringAndValue("jun", 6),
            new StringAndValue("jun.", 6),
            new StringAndValue("june", 6)
        };
        checkIterator(iter, data);
        // Reset, and we should get the same result.
        logln("after iter.reset()");
        checkIterator(iter.reset(), data);
    }

    @Test
    public void Test53TruncatingIteratorFromLinearMatchShort() {
        final StringAndValue[] data={
            new StringAndValue("abcdef", 10),
            new StringAndValue("abcdepq", 200),
            new StringAndValue("abcdeyz", 3000)
        };
        CharsTrie trie=buildTrie(data, data.length, StringTrieBuilder.Option.FAST);
        // Go into a linear-match node.
        trie.next('a');
        trie.next('b');
        // Truncate within the linear-match node.
        CharsTrie.Iterator iter=trie.iterator(2);
        final StringAndValue[] expected={
            new StringAndValue("cd", -1)
        };
        checkIterator(iter, expected);
        // Reset, and we should get the same result.
        logln("after iter.reset()");
        checkIterator(iter.reset(), expected);
    }

    @Test
    public void Test54TruncatingIteratorFromLinearMatchLong() {
        final StringAndValue[] data={
            new StringAndValue("abcdef", 10),
            new StringAndValue("abcdepq", 200),
            new StringAndValue("abcdeyz", 3000)
        };
        CharsTrie trie=buildTrie(data, data.length, StringTrieBuilder.Option.FAST);
        // Go into a linear-match node.
        trie.next('a');
        trie.next('b');
        trie.next('c');
        // Truncate after the linear-match node.
        CharsTrie.Iterator iter=trie.iterator(3);
        final StringAndValue[] expected={
            new StringAndValue("def", 10),
            new StringAndValue("dep", -1),
            new StringAndValue("dey", -1)
        };
        checkIterator(iter, expected);
        // Reset, and we should get the same result.
        logln("after iter.reset()");
        checkIterator(iter.reset(), expected);
    }

    @Test
    public void Test59IteratorFromChars() {
        final StringAndValue[] data={
            new StringAndValue("mm", 3),
            new StringAndValue("mmm", 33),
            new StringAndValue("mmnop", 333)
        };
        builder_.clear();
        for(StringAndValue item : data) {
            builder_.add(item.s, item.value);
        }
        CharSequence trieChars=builder_.buildCharSequence(StringTrieBuilder.Option.FAST);
        checkIterator(CharsTrie.iterator(trieChars, 0, 0), data);
    }

    private void checkData(StringAndValue data[]) {
        checkData(data, data.length);
    }

    private void checkData(StringAndValue data[], int dataLength) {
        logln("checkData(dataLength="+dataLength+", fast)");
        checkData(data, dataLength, StringTrieBuilder.Option.FAST);
        logln("checkData(dataLength="+dataLength+", small)");
        checkData(data, dataLength, StringTrieBuilder.Option.SMALL);
    }

    private void checkData(StringAndValue[] data, int dataLength, StringTrieBuilder.Option buildOption) {
        CharsTrie trie=buildTrie(data, dataLength, buildOption);
        checkFirst(trie, data, dataLength);
        checkNext(trie, data, dataLength);
        checkNextWithState(trie, data, dataLength);
        checkNextString(trie, data, dataLength);
        checkIterator(trie, data, dataLength);
    }

    private CharsTrie buildTrie(StringAndValue data[], int dataLength,
                                StringTrieBuilder.Option buildOption) {
        // Add the items to the trie builder in an interesting (not trivial, not random) order.
        int index, step;
        if((dataLength&1)!=0) {
            // Odd number of items.
            index=dataLength/2;
            step=2;
        } else if((dataLength%3)!=0) {
            // Not a multiple of 3.
            index=dataLength/5;
            step=3;
        } else {
            index=dataLength-1;
            step=-1;
        }
        builder_.clear();
        for(int i=0; i<dataLength; ++i) {
            builder_.add(data[index].s, data[index].value);
            index=(index+step)%dataLength;
        }
        CharsTrie trie=builder_.build(buildOption);
        try {
            builder_.add("zzz", 999);
            errln("builder.build().add(zzz) did not throw IllegalStateException");
        } catch(IllegalStateException e) {
            // good
        }
        CharSequence trieChars=builder_.buildCharSequence(buildOption);
        logln("serialized trie size: "+trieChars.length()+" chars");
        // Tries from either build() method should be identical but
        // CharsTrie does not implement equals().
        // We just return either one.
        if((dataLength&1)!=0) {
            return trie;
        } else {
            return new CharsTrie(trieChars, 0);
        }
    }

    private void checkFirst(CharsTrie trie, StringAndValue[] data, int dataLength) {
        for(int i=0; i<dataLength; ++i) {
            if(data[i].s.length()==0) {
                continue;  // skip empty string
            }
            String expectedString=data[i].s;
            int c=expectedString.charAt(0);
            int nextCp=expectedString.length()>1 ? expectedString.charAt(1) : 0;
            BytesTrie.Result firstResult=trie.first(c);
            int firstValue=firstResult.hasValue() ? trie.getValue() : -1;
            BytesTrie.Result nextResult=trie.next(nextCp);
            if(firstResult!=trie.reset().next(c) ||
               firstResult!=trie.current() ||
               firstValue!=(firstResult.hasValue() ? trie.getValue() : -1) ||
               nextResult!=trie.next(nextCp)
            ) {
                errln(String.format("trie.first(U+%04X)!=trie.reset().next(same) for %s",
                                    c, data[i].s));
            }
            c=expectedString.codePointAt(0);
            int cLength=Character.charCount(c);
            nextCp=expectedString.length()>cLength ? expectedString.codePointAt(cLength) : 0;
            firstResult=trie.firstForCodePoint(c);
            firstValue=firstResult.hasValue() ? trie.getValue() : -1;
            nextResult=trie.nextForCodePoint(nextCp);
            if(firstResult!=trie.reset().nextForCodePoint(c) ||
               firstResult!=trie.current() ||
               firstValue!=(firstResult.hasValue() ? trie.getValue() : -1) ||
               nextResult!=trie.nextForCodePoint(nextCp)
            ) {
                errln(String.format("trie.firstForCodePoint(U+%04X)!=trie.reset().nextForCodePoint(same) for %s",
                                    c, data[i].s));
            }
        }
        trie.reset();
    }

    private void checkNext(CharsTrie trie, StringAndValue[] data, int dataLength) {
        CharsTrie.State state=new CharsTrie.State();
        for(int i=0; i<dataLength; ++i) {
            String expectedString=data[i].s;
            int stringLength=expectedString.length();
            BytesTrie.Result result;
            if( !(result=trie.next(expectedString, 0, stringLength)).hasValue() ||
                result!=trie.current()
            ) {
                errln("trie does not seem to contain "+data[i].s);
            } else if(trie.getValue()!=data[i].value) {
                errln(String.format("trie value for %s is %d=0x%x instead of expected %d=0x%x",
                                    data[i].s,
                                    trie.getValue(), trie.getValue(),
                                    data[i].value, data[i].value));
            } else if(result!=trie.current() || trie.getValue()!=data[i].value) {
                errln("trie value for "+data[i].s+" changes when repeating current()/getValue()");
            }
            trie.reset();
            result=trie.current();
            for(int j=0; j<stringLength; ++j) {
                if(!result.hasNext()) {
                    errln(String.format("trie.current()!=hasNext before end of %s (at index %d)",
                                        data[i].s, j));
                    break;
                }
                if(result==BytesTrie.Result.INTERMEDIATE_VALUE) {
                    trie.getValue();
                    if(trie.current()!=BytesTrie.Result.INTERMEDIATE_VALUE) {
                        errln(String.format("trie.getValue().current()!=BytesTrie.Result.INTERMEDIATE_VALUE "+
                                            "before end of %s (at index %d)", data[i].s, j));
                        break;
                    }
                }
                result=trie.next(expectedString.charAt(j));
                if(!result.matches()) {
                    errln(String.format("trie.next()=BytesTrie.Result.NO_MATCH "+
                                        "before end of %s (at index %d)", data[i].s, j));
                    break;
                }
                if(result!=trie.current()) {
                    errln(String.format("trie.next()!=following current() "+
                                        "before end of %s (at index %d)", data[i].s, j));
                    break;
                }
            }
            if(!result.hasValue()) {
                errln("trie.next()!=hasValue at the end of "+data[i].s);
                continue;
            }
            trie.getValue();
            if(result!=trie.current()) {
                errln("trie.current() != current()+getValue()+current() after end of "+
                      data[i].s);
            }
            // Compare the final current() with whether next() can actually continue.
            trie.saveState(state);
            boolean nextContinues=false;
            for(int c=0x20; c<0xe000; ++c) {
                if(c==0x80) {
                    c=0xd800;  // Check for ASCII and surrogates but not all of the BMP.
                }
                if(trie.resetToState(state).next(c).matches()) {
                    nextContinues=true;
                    break;
                }
            }
            if((result==BytesTrie.Result.INTERMEDIATE_VALUE)!=nextContinues) {
                errln("(trie.current()==BytesTrie.Result.INTERMEDIATE_VALUE) contradicts "+
                      "(trie.next(some char)!=BytesTrie.Result.NO_MATCH) after end of "+data[i].s);
            }
            trie.reset();
        }
    }

    private void checkNextWithState(CharsTrie trie, StringAndValue[] data, int dataLength) {
        CharsTrie.State noState=new CharsTrie.State(), state=new CharsTrie.State();
        for(int i=0; i<dataLength; ++i) {
            if((i&1)==0) {
                try {
                    trie.resetToState(noState);
                    errln("trie.resetToState(noState) should throw an IllegalArgumentException");
                } catch(IllegalArgumentException e) {
                    // good
                }
            }
            String expectedString=data[i].s;
            int stringLength=expectedString.length();
            int partialLength=stringLength/3;
            for(int j=0; j<partialLength; ++j) {
                if(!trie.next(expectedString.charAt(j)).matches()) {
                    errln("trie.next()=BytesTrie.Result.NO_MATCH for a prefix of "+data[i].s);
                    return;
                }
            }
            trie.saveState(state);
            BytesTrie.Result resultAtState=trie.current();
            BytesTrie.Result result;
            int valueAtState=-99;
            if(resultAtState.hasValue()) {
                valueAtState=trie.getValue();
            }
            result=trie.next(0);  // mismatch
            if(result!=BytesTrie.Result.NO_MATCH || result!=trie.current()) {
                errln("trie.next(0) matched after part of "+data[i].s);
            }
            if( resultAtState!=trie.resetToState(state).current() ||
                (resultAtState.hasValue() && valueAtState!=trie.getValue())
            ) {
                errln("trie.next(part of "+data[i].s+") changes current()/getValue() after "+
                      "saveState/next(0)/resetToState");
            } else if(!(result=trie.next(expectedString, partialLength, stringLength)).hasValue() ||
                      result!=trie.current()) {
                errln("trie.next(rest of "+data[i].s+") does not seem to contain "+data[i].s+" after "+
                      "saveState/next(0)/resetToState");
            } else if(!(result=trie.resetToState(state).
                                next(expectedString, partialLength, stringLength)).hasValue() ||
                      result!=trie.current()) {
                errln("trie does not seem to contain "+data[i].s+
                      " after saveState/next(rest)/resetToState");
            } else if(trie.getValue()!=data[i].value) {
                errln(String.format("trie value for %s is %d=0x%x instead of expected %d=0x%x",
                                    data[i].s,
                                    trie.getValue(), trie.getValue(),
                                    data[i].value, data[i].value));
            }
            trie.reset();
        }
    }

    // next(string) is also tested in other functions,
    // but here we try to go partway through the string, and then beyond it.
    private void checkNextString(CharsTrie trie, StringAndValue[] data, int dataLength) {
        for(int i=0; i<dataLength; ++i) {
            String expectedString=data[i].s;
            int stringLength=expectedString.length();
            if(!trie.next(expectedString, 0, stringLength/2).matches()) {
                errln("trie.next(up to middle of string)=BytesTrie.Result.NO_MATCH for "+data[i].s);
                continue;
            }
            // Test that we stop properly at the end of the string.
            trie.next(expectedString, stringLength/2, stringLength);
            if(trie.next(0).matches()) {
                errln("trie.next(string+NUL)!=BytesTrie.Result.NO_MATCH for "+data[i].s);
            }
            trie.reset();
        }
    }

    private void checkIterator(CharsTrie trie, StringAndValue[] data, int dataLength) {
        checkIterator(trie.iterator(), data, dataLength);
    }

    private void checkIterator(CharsTrie.Iterator iter, StringAndValue data[]) {
        checkIterator(iter, data, data.length);
    }

    private void checkIterator(CharsTrie.Iterator iter, StringAndValue[] data, int dataLength) {
        for(int i=0; i<dataLength; ++i) {
            if(!iter.hasNext()) {
                errln("trie iterator hasNext()=false for item "+i+": "+data[i].s);
                break;
            }
            CharsTrie.Entry entry=iter.next();
            String expectedString=data[i].s;
            if(!expectedString.contentEquals(entry.chars)) {
                errln(String.format("trie iterator next().getString()=%s but expected %s for item %d",
                                    entry.chars, data[i].s, i));
            }
            if(entry.value!=data[i].value) {
                errln(String.format("trie iterator next().getValue()=%d=0x%x but expected %d=0x%x for item %d: %s",
                                    entry.value, entry.value,
                                    data[i].value, data[i].value,
                                    i, data[i].s));
            }
        }
        if(iter.hasNext()) {
            errln("trie iterator hasNext()=true after all items");
        }
        try {
            iter.next();
            errln("trie iterator next() did not throw NoSuchElementException after all items");
        } catch(NoSuchElementException e) {
            // good
        }
    }

    private CharsTrieBuilder builder_=new CharsTrieBuilder();
}
