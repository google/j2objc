/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2011, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011jan08
*   created by: Markus W. Scherer
*   ported from ICU4C bytestrietest.h/.cpp
*/

package android.icu.dev.test.util;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.junit.Test;

import android.icu.dev.test.TestFmwk;
import android.icu.util.BytesTrie;
import android.icu.util.BytesTrieBuilder;
import android.icu.util.StringTrieBuilder;

public class BytesTrieTest extends TestFmwk {
    public BytesTrieTest() {}

    // All test functions have a TestNN prefix where NN is a double-digit number.
    // This is so that when tests are run in sorted order
    // the simpler ones are run first.
    // If there is a problem, the simpler ones are easier to step through.

    @Test
    public void Test00Builder() {
        builder_.clear();
        try {
            builder_.build(StringTrieBuilder.Option.FAST);
            errln("BytesTrieBuilder().build() did not throw IndexOutOfBoundsException");
            return;
        } catch(IndexOutOfBoundsException e) {
            // good
        }
        try {
            byte[] equal=new byte[] { 0x3d };  // "="
            builder_.add(equal, 1, 0).add(equal, 1, 1);
            errln("BytesTrieBuilder.add() did not detect duplicates");
            return;
        } catch(IllegalArgumentException e) {
            // good
        }
    }

    private static final class StringAndValue {
        public StringAndValue(String str, int val) {
            s=str;
            bytes=new byte[s.length()];
            for(int i=0; i<bytes.length; ++i) {
                bytes[i]=(byte)s.charAt(i);
            }
            value=val;
        }

        public String s;
        public byte[] bytes;
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
            // more than 256 bytes
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

    public BytesTrie buildMonthsTrie(StringTrieBuilder.Option buildOption) {
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
        BytesTrie trie=buildMonthsTrie(StringTrieBuilder.Option.FAST);
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
    public void Test41GetNextBytes() {
        BytesTrie trie=buildMonthsTrie(StringTrieBuilder.Option.SMALL);
        StringBuilder buffer=new StringBuilder();
        int count=trie.getNextBytes(buffer);
        if(count!=2 || !"aj".contentEquals(buffer)) {
            errln("months getNextBytes()!=[aj] at root");
        }
        trie.next('j');
        trie.next('a');
        trie.next('n');
        // getNextBytes() directly after next()
        buffer.setLength(0);
        count=trie.getNextBytes(buffer);
        if(count!=20 || !".abcdefghijklmnopqru".contentEquals(buffer)) {
            errln("months getNextBytes()!=[.abcdefghijklmnopqru] after \"jan\"");
        }
        // getNextBytes() after getValue()
        trie.getValue();  // next() had returned BytesTrie.Result.INTERMEDIATE_VALUE.
        buffer.setLength(0);
        count=trie.getNextBytes(buffer);
        if(count!=20 || !".abcdefghijklmnopqru".contentEquals(buffer)) {
            errln("months getNextBytes()!=[.abcdefghijklmnopqru] after \"jan\"+getValue()");
        }
        // getNextBytes() from a linear-match node
        trie.next('u');
        buffer.setLength(0);
        count=trie.getNextBytes(buffer);
        if(count!=1 || !"a".contentEquals(buffer)) {
            errln("months getNextBytes()!=[a] after \"janu\"");
        }
        trie.next('a');
        buffer.setLength(0);
        count=trie.getNextBytes(buffer);
        if(count!=1 || !"r".contentEquals(buffer)) {
            errln("months getNextBytes()!=[r] after \"janua\"");
        }
        trie.next('r');
        trie.next('y');
        // getNextBytes() after a final match
        buffer.setLength(0);
        count=trie.getNextBytes(buffer);
        if(count!=0 || buffer.length()!=0) {
            errln("months getNextBytes()!=[] after \"january\"");
        }
    }

    @Test
    public void Test50IteratorFromBranch() {
        BytesTrie trie=buildMonthsTrie(StringTrieBuilder.Option.FAST);
        // Go to a branch node.
        trie.next('j');
        trie.next('a');
        trie.next('n');
        BytesTrie.Iterator iter=trie.iterator();
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
        BytesTrie trie=buildMonthsTrie(StringTrieBuilder.Option.SMALL);
        // Go into a linear-match node.
        trie.next('j');
        trie.next('a');
        trie.next('n');
        trie.next('u');
        trie.next('a');
        BytesTrie.Iterator iter=trie.iterator();
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
        BytesTrie trie=buildMonthsTrie(StringTrieBuilder.Option.FAST);
        BytesTrie.Iterator iter=trie.iterator(4);
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
        BytesTrie trie=buildTrie(data, data.length, StringTrieBuilder.Option.FAST);
        // Go into a linear-match node.
        trie.next('a');
        trie.next('b');
        // Truncate within the linear-match node.
        BytesTrie.Iterator iter=trie.iterator(2);
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
        BytesTrie trie=buildTrie(data, data.length, StringTrieBuilder.Option.FAST);
        // Go into a linear-match node.
        trie.next('a');
        trie.next('b');
        trie.next('c');
        // Truncate after the linear-match node.
        BytesTrie.Iterator iter=trie.iterator(3);
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
    public void Test59IteratorFromBytes() {
        final StringAndValue[] data={
            new StringAndValue("mm", 3),
            new StringAndValue("mmm", 33),
            new StringAndValue("mmnop", 333)
        };
        builder_.clear();
        for(StringAndValue item : data) {
            builder_.add(item.bytes, item.bytes.length, item.value);
        }
        ByteBuffer trieBytes=builder_.buildByteBuffer(StringTrieBuilder.Option.FAST);
        checkIterator(
            BytesTrie.iterator(trieBytes.array(), trieBytes.arrayOffset()+trieBytes.position(), 0),
            data);
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

    private void checkData(StringAndValue data[], int dataLength, StringTrieBuilder.Option buildOption) {
        BytesTrie trie=buildTrie(data, dataLength, buildOption);
        checkFirst(trie, data, dataLength);
        checkNext(trie, data, dataLength);
        checkNextWithState(trie, data, dataLength);
        checkNextString(trie, data, dataLength);
        checkIterator(trie, data, dataLength);
    }

    private BytesTrie buildTrie(StringAndValue data[], int dataLength,
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
            builder_.add(data[index].bytes, data[index].bytes.length, data[index].value);
            index=(index+step)%dataLength;
        }
        BytesTrie trie=builder_.build(buildOption);
        try {
            builder_.add(/* "zzz" */ new byte[] { 0x7a, 0x7a, 0x7a }, 0, 999);
            errln("builder.build().add(zzz) did not throw IllegalStateException");
        } catch(IllegalStateException e) {
            // good
        }
        ByteBuffer trieBytes=builder_.buildByteBuffer(buildOption);
        logln("serialized trie size: "+trieBytes.remaining()+" bytes\n");
        // Tries from either build() method should be identical but
        // BytesTrie does not implement equals().
        // We just return either one.
        if((dataLength&1)!=0) {
            return trie;
        } else {
            return new BytesTrie(trieBytes.array(), trieBytes.arrayOffset()+trieBytes.position());
        }
    }

    private void checkFirst(BytesTrie trie, StringAndValue data[], int dataLength) {
        for(int i=0; i<dataLength; ++i) {
            if(data[i].s.length()==0) {
                continue;  // skip empty string
            }
            int c=data[i].bytes[0];
            BytesTrie.Result firstResult=trie.first(c);
            int firstValue=firstResult.hasValue() ? trie.getValue() : -1;
            int nextC=data[i].s.length()>1 ? data[i].bytes[1] : 0;
            BytesTrie.Result nextResult=trie.next(nextC);
            if(firstResult!=trie.reset().next(c) ||
               firstResult!=trie.current() ||
               firstValue!=(firstResult.hasValue() ? trie.getValue() : -1) ||
               nextResult!=trie.next(nextC)
            ) {
                errln(String.format("trie.first(%c)!=trie.reset().next(same) for %s",
                                    c, data[i].s));
            }
        }
        trie.reset();
    }

    private void checkNext(BytesTrie trie, StringAndValue data[], int dataLength) {
        BytesTrie.State state=new BytesTrie.State();
        for(int i=0; i<dataLength; ++i) {
            int stringLength=data[i].s.length();
            BytesTrie.Result result;
            if( !(result=trie.next(data[i].bytes, 0, stringLength)).hasValue() ||
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
                result=trie.next(data[i].bytes[j]);
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
            for(int c=0x20; c<0x7f; ++c) {
                if(trie.resetToState(state).next(c).matches()) {
                    nextContinues=true;
                    break;
                }
            }
            if((result==BytesTrie.Result.INTERMEDIATE_VALUE)!=nextContinues) {
                errln("(trie.current()==BytesTrie.Result.INTERMEDIATE_VALUE) contradicts "+
                      "(trie.next(some UChar)!=BytesTrie.Result.NO_MATCH) after end of "+data[i].s);
            }
            trie.reset();
        }
    }

    private void checkNextWithState(BytesTrie trie, StringAndValue data[], int dataLength) {
        BytesTrie.State noState=new BytesTrie.State(), state=new BytesTrie.State();
        for(int i=0; i<dataLength; ++i) {
            if((i&1)==0) {
                try {
                    trie.resetToState(noState);
                    errln("trie.resetToState(noState) should throw an IllegalArgumentException");
                } catch(IllegalArgumentException e) {
                    // good
                }
            }
            byte[] expectedString=data[i].bytes;
            int stringLength=data[i].s.length();
            int partialLength=stringLength/3;
            for(int j=0; j<partialLength; ++j) {
                if(!trie.next(expectedString[j]).matches()) {
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
    private void checkNextString(BytesTrie trie, StringAndValue data[], int dataLength) {
        for(int i=0; i<dataLength; ++i) {
            byte[] expectedString=data[i].bytes;
            int stringLength=data[i].s.length();
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

    private void checkIterator(BytesTrie trie, StringAndValue data[], int dataLength) {
        checkIterator(trie.iterator(), data, dataLength);
    }

    private void checkIterator(BytesTrie.Iterator iter, StringAndValue data[]) {
        checkIterator(iter, data, data.length);
    }

    private void checkIterator(BytesTrie.Iterator iter, StringAndValue data[], int dataLength) {
        for(int i=0; i<dataLength; ++i) {
            if(!iter.hasNext()) {
                errln("trie iterator hasNext()=false for item "+i+": "+data[i].s);
                break;
            }
            BytesTrie.Entry entry=iter.next();
            StringBuilder bytesString=new StringBuilder();
            for(int j=0; j<entry.bytesLength(); ++j) {
                bytesString.append((char)(entry.byteAt(j)&0xff));
            }
            if(!data[i].s.contentEquals(bytesString)) {
                errln(String.format("trie iterator next().getString()=%s but expected %s for item %d",
                                    bytesString, data[i].s, i));
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

    private BytesTrieBuilder builder_=new BytesTrieBuilder();
}
