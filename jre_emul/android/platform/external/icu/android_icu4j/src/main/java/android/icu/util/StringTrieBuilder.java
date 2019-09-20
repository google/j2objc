/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2011-2014, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011jan05
*   created by: Markus W. Scherer
*   ported from ICU4C stringtriebuilder.h/.cpp
*/
package android.icu.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Base class for string trie builder classes.
 *
 * <p>This class is not intended for public subclassing.
 *
 * @author Markus W. Scherer
 * @hide Only a subset of ICU is exposed in Android
 */
public abstract class StringTrieBuilder {
    /**
     * Build options for BytesTrieBuilder and CharsTrieBuilder.
     */
    public enum Option {
        /**
         * Builds a trie quickly.
         */
        FAST,
        /**
         * Builds a trie more slowly, attempting to generate
         * a shorter but equivalent serialization.
         * This build option also uses more memory.
         *
         * <p>This option can be effective when many integer values are the same
         * and string/byte sequence suffixes can be shared.
         * Runtime speed is not expected to improve.
         */
        SMALL
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected StringTrieBuilder() {}

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected void addImpl(CharSequence s, int value) {
        if(state!=State.ADDING) {
            // Cannot add elements after building.
            throw new IllegalStateException("Cannot add (string, value) pairs after build().");
        }
        if(s.length()>0xffff) {
            // Too long: Limited by iterator internals, and by builder recursion depth.
            throw new IndexOutOfBoundsException("The maximum string length is 0xffff.");
        }
        if(root==null) {
            root=createSuffixNode(s, 0, value);
        } else {
            root=root.add(this, s, 0, value);
        }
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected final void buildImpl(Option buildOption) {
        switch(state) {
        case ADDING:
            if(root==null) {
                throw new IndexOutOfBoundsException("No (string, value) pairs were added.");
            }
            if(buildOption==Option.FAST) {
                state=State.BUILDING_FAST;
                // Building "fast" is somewhat faster (25..50% in some test)
                // because it makes registerNode() return the input node
                // rather than checking for duplicates.
                // As a result, we sometimes write larger trie serializations.
                //
                // In either case we need to fix-up linear-match nodes (for their maximum length)
                // and branch nodes (turning dynamic branch nodes into trees of
                // runtime-equivalent nodes), but the HashMap/hashCode()/equals() are omitted for
                // nodes other than final values.
            } else {
                state=State.BUILDING_SMALL;
            }
            break;
        case BUILDING_FAST:
        case BUILDING_SMALL:
            // Building must have failed.
            throw new IllegalStateException("Builder failed and must be clear()ed.");
        case BUILT:
            return;  // Nothing more to do.
        }
        // Implementation note:
        // We really build three versions of the trie.
        // The first is a fully dynamic trie, built successively by addImpl().
        // Then we call root.register() to turn it into a tree of nodes
        // which is 1:1 equivalent to the runtime data structure.
        // Finally, root.markRightEdgesFirst() and root.write() write that serialized form.
        root=root.register(this);
        root.markRightEdgesFirst(-1);
        root.write(this);
        state=State.BUILT;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected void clearImpl() {
        strings.setLength(0);
        nodes.clear();
        root=null;
        state=State.ADDING;
    }

    /**
     * Makes sure that there is only one unique node registered that is
     * equivalent to newNode, unless BUILDING_FAST.
     * @param newNode Input node. The builder takes ownership.
     * @return newNode if it is the first of its kind, or
     *         an equivalent node if newNode is a duplicate.
     */
    private final Node registerNode(Node newNode) {
        if(state==State.BUILDING_FAST) {
            return newNode;
        }
        // BUILDING_SMALL
        Node oldNode=nodes.get(newNode);
        if(oldNode!=null) {
            return oldNode;
        }
        // If put() returns a non-null value from an equivalent, previously
        // registered node, then get() failed to find that and we will leak newNode.
        oldNode=nodes.put(newNode, newNode);
        assert(oldNode==null);
        return newNode;
    }

    /**
     * Makes sure that there is only one unique FinalValueNode registered
     * with this value.
     * Avoids creating a node if the value is a duplicate.
     * @param value A final value.
     * @return A FinalValueNode with the given value.
     */
    private final ValueNode registerFinalValue(int value) {
        // We always register final values because while ADDING
        // we do not know yet whether we will build fast or small.
        lookupFinalValueNode.setFinalValue(value);
        Node oldNode=nodes.get(lookupFinalValueNode);
        if(oldNode!=null) {
            return (ValueNode)oldNode;
        }
        ValueNode newNode=new ValueNode(value);
        // If put() returns a non-null value from an equivalent, previously
        // registered node, then get() failed to find that and we will leak newNode.
        oldNode=nodes.put(newNode, newNode);
        assert(oldNode==null);
        return newNode;
    }

    private static abstract class Node {
        public Node() {
            offset=0;
        }
        // hashCode() and equals() for use with registerNode() and the nodes hash.
        @Override
        public abstract int hashCode() /*const*/;
        // Base class equals() compares the actual class types.
        @Override
        public boolean equals(Object other) {
            return this==other || this.getClass()==other.getClass();
        }
        /**
         * Recursive method for adding a new (string, value) pair.
         * Matches the remaining part of s from start,
         * and adds a new node where there is a mismatch.
         * @return this or a replacement Node
         */
        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            return this;
        }
        /**
         * Recursive method for registering unique nodes,
         * after all (string, value) pairs have been added.
         * Final-value nodes are pre-registered while add()ing (string, value) pairs.
         * Other nodes created while add()ing registerNode() themselves later
         * and might replace themselves with new types of nodes for write()ing.
         * @return The registered version of this node which implements write().
         */
        public Node register(StringTrieBuilder builder) { return this; }
        /**
         * Traverses the Node graph and numbers branch edges, with rightmost edges first.
         * This is to avoid writing a duplicate node twice.
         *
         * Branch nodes in this trie data structure are not symmetric.
         * Most branch edges "jump" to other nodes but the rightmost branch edges
         * just continue without a jump.
         * Therefore, write() must write the rightmost branch edge last
         * (trie units are written backwards), and must write it at that point even if
         * it is a duplicate of a node previously written elsewhere.
         *
         * This function visits and marks right branch edges first.
         * Edges are numbered with increasingly negative values because we share the
         * offset field which gets positive values when nodes are written.
         * A branch edge also remembers the first number for any of its edges.
         *
         * When a further-left branch edge has a number in the range of the rightmost
         * edge's numbers, then it will be written as part of the required right edge
         * and we can avoid writing it first.
         *
         * After root.markRightEdgesFirst(-1) the offsets of all nodes are negative
         * edge numbers.
         *
         * @param edgeNumber The first edge number for this node and its sub-nodes.
         * @return An edge number that is at least the maximum-negative
         *         of the input edge number and the numbers of this node and all of its sub-nodes.
         */
        public int markRightEdgesFirst(int edgeNumber) {
            if(offset==0) {
                offset=edgeNumber;
            }
            return edgeNumber;
        }
        // write() must set the offset to a positive value.
        public abstract void write(StringTrieBuilder builder);
        // See markRightEdgesFirst.
        public final void writeUnlessInsideRightEdge(int firstRight, int lastRight,
                                               StringTrieBuilder builder) {
            // Note: Edge numbers are negative, lastRight<=firstRight.
            // If offset>0 then this node and its sub-nodes have been written already
            // and we need not write them again.
            // If this node is part of the unwritten right branch edge,
            // then we wait until that is written.
            if(offset<0 && (offset<lastRight || firstRight<offset)) {
                write(builder);
            }
        }
        public final int getOffset() /*const*/ { return offset; }

        protected int offset;
    }

    // Used directly for final values, and as as a superclass for
    // match nodes with intermediate values.
    private static class ValueNode extends Node {
        public ValueNode() {}
        public ValueNode(int v) {
            hasValue=true;
            value=v;
        }
        public final void setValue(int v) {
            assert(!hasValue);
            hasValue=true;
            value=v;
        }
        private void setFinalValue(int v) {
            hasValue=true;
            value=v;
        }
        @Override
        public int hashCode() /*const*/ {
            int hash=0x111111;
            if(hasValue) {
                hash=hash*37+value;
            }
            return hash;
        }
        @Override
        public boolean equals(Object other) {
            if(this==other) {
                return true;
            }
            if(!super.equals(other)) {
                return false;
            }
            ValueNode o=(ValueNode)other;
            return hasValue==o.hasValue && (!hasValue || value==o.value);
        }
        @Override
        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if(start==s.length()) {
                throw new IllegalArgumentException("Duplicate string.");
            }
            // Replace self with a node for the remaining string suffix and value.
            ValueNode node=builder.createSuffixNode(s, start, sValue);
            node.setValue(value);
            return node;
        }
        @Override
        public void write(StringTrieBuilder builder) {
            offset=builder.writeValueAndFinal(value, true);
        }

        protected boolean hasValue;
        protected int value;
    }

    private static final class IntermediateValueNode extends ValueNode {
        public IntermediateValueNode(int v, Node nextNode) {
            next=nextNode;
            setValue(v);
        }
        @Override
        public int hashCode() /*const*/ {
            return (0x222222*37+value)*37+next.hashCode();
        }
        @Override
        public boolean equals(Object other) {
            if(this==other) {
                return true;
            }
            if(!super.equals(other)) {
                return false;
            }
            IntermediateValueNode o=(IntermediateValueNode)other;
            return next==o.next;
        }
        @Override
        public int markRightEdgesFirst(int edgeNumber) {
            if(offset==0) {
                offset=edgeNumber=next.markRightEdgesFirst(edgeNumber);
            }
            return edgeNumber;
        }
        @Override
        public void write(StringTrieBuilder builder) {
            next.write(builder);
            offset=builder.writeValueAndFinal(value, false);
        }

        private Node next;
    }

    private static final class LinearMatchNode extends ValueNode {
        public LinearMatchNode(CharSequence builderStrings, int sOffset, int len, Node nextNode) {
            strings=builderStrings;
            stringOffset=sOffset;
            length=len;
            next=nextNode;
        }
        @Override
        public int hashCode() /*const*/ { return hash; }
        @Override
        public boolean equals(Object other) {
            if(this==other) {
                return true;
            }
            if(!super.equals(other)) {
                return false;
            }
            LinearMatchNode o=(LinearMatchNode)other;
            if(length!=o.length || next!=o.next) {
                return false;
            }
            for(int i=stringOffset, j=o.stringOffset, limit=stringOffset+length; i<limit; ++i, ++j) {
                if(strings.charAt(i)!=strings.charAt(j)) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if(start==s.length()) {
                if(hasValue) {
                    throw new IllegalArgumentException("Duplicate string.");
                } else {
                    setValue(sValue);
                    return this;
                }
            }
            int limit=stringOffset+length;
            for(int i=stringOffset; i<limit; ++i, ++start) {
                if(start==s.length()) {
                    // s is a prefix with a new value. Split self into two linear-match nodes.
                    int prefixLength=i-stringOffset;
                    LinearMatchNode suffixNode=new LinearMatchNode(strings, i, length-prefixLength, next);
                    suffixNode.setValue(sValue);
                    length=prefixLength;
                    next=suffixNode;
                    return this;
                }
                char thisChar=strings.charAt(i);
                char newChar=s.charAt(start);
                if(thisChar!=newChar) {
                    // Mismatch, insert a branch node.
                    DynamicBranchNode branchNode=new DynamicBranchNode();
                    // Reuse this node for one of the remaining substrings, if any.
                    Node result, thisSuffixNode;
                    if(i==stringOffset) {
                        // Mismatch on first character, turn this node into a suffix.
                        if(hasValue) {
                            // Move the value for prefix length "start" to the new node.
                            branchNode.setValue(value);
                            value=0;
                            hasValue=false;
                        }
                        ++stringOffset;
                        --length;
                        thisSuffixNode= length>0 ? this : next;
                        // C++: if(length==0) { delete this; }
                        result=branchNode;
                    } else if(i==limit-1) {
                        // Mismatch on last character, keep this node for the prefix.
                        --length;
                        thisSuffixNode=next;
                        next=branchNode;
                        result=this;
                    } else {
                        // Mismatch on intermediate character, keep this node for the prefix.
                        int prefixLength=i-stringOffset;
                        ++i;  // Suffix start offset (after thisChar).
                        thisSuffixNode=new LinearMatchNode(
                                strings, i, length-(prefixLength+1), next);
                        length=prefixLength;
                        next=branchNode;
                        result=this;
                    }
                    ValueNode newSuffixNode=builder.createSuffixNode(s, start+1, sValue);
                    branchNode.add(thisChar, thisSuffixNode);
                    branchNode.add(newChar, newSuffixNode);
                    return result;
                }
            }
            // s matches all of this node's characters.
            next=next.add(builder, s, start, sValue);
            return this;
        }
        @Override
        public Node register(StringTrieBuilder builder) {
            next=next.register(builder);
            // Break the linear-match sequence into chunks of at most kMaxLinearMatchLength.
            int maxLinearMatchLength=builder.getMaxLinearMatchLength();
            while(length>maxLinearMatchLength) {
                int nextOffset=stringOffset+length-maxLinearMatchLength;
                length-=maxLinearMatchLength;
                LinearMatchNode suffixNode=
                    new LinearMatchNode(strings, nextOffset, maxLinearMatchLength, next);
                suffixNode.setHashCode();
                next=builder.registerNode(suffixNode);
            }
            Node result;
            if(hasValue && !builder.matchNodesCanHaveValues()) {
                int intermediateValue=value;
                value=0;
                hasValue=false;
                setHashCode();
                result=new IntermediateValueNode(intermediateValue, builder.registerNode(this));
            } else {
                setHashCode();
                result=this;
            }
            return builder.registerNode(result);
        }
        @Override
        public int markRightEdgesFirst(int edgeNumber) {
            if(offset==0) {
                offset=edgeNumber=next.markRightEdgesFirst(edgeNumber);
            }
            return edgeNumber;
        }
        @Override
        public void write(StringTrieBuilder builder) {
            next.write(builder);
            builder.write(stringOffset, length);
            offset=builder.writeValueAndType(hasValue, value, builder.getMinLinearMatch()+length-1);
        }

        // Must be called just before registerNode(this).
        private void setHashCode() /*const*/ {
            hash=(0x333333*37+length)*37+next.hashCode();
            if(hasValue) {
                hash=hash*37+value;
            }
            for(int i=stringOffset, limit=stringOffset+length; i<limit; ++i) {
                hash=hash*37+strings.charAt(i);
            }
        }

        private CharSequence strings;
        private int stringOffset;
        private int length;
        private Node next;
        private int hash;
    }

    private static final class DynamicBranchNode extends ValueNode {
        public DynamicBranchNode() {}
        // c must not be in chars yet.
        public void add(char c, Node node) {
            int i=find(c);
            chars.insert(i, c);
            equal.add(i, node);
        }
        @Override
        public Node add(StringTrieBuilder builder, CharSequence s, int start, int sValue) {
            if(start==s.length()) {
                if(hasValue) {
                    throw new IllegalArgumentException("Duplicate string.");
                } else {
                    setValue(sValue);
                    return this;
                }
            }
            char c=s.charAt(start++);
            int i=find(c);
            if(i<chars.length() && c==chars.charAt(i)) {
                equal.set(i, equal.get(i).add(builder, s, start, sValue));
            } else {
                chars.insert(i, c);
                equal.add(i, builder.createSuffixNode(s, start, sValue));
            }
            return this;
        }
        @Override
        public Node register(StringTrieBuilder builder) {
            Node subNode=register(builder, 0, chars.length());
            BranchHeadNode head=new BranchHeadNode(chars.length(), subNode);
            Node result=head;
            if(hasValue) {
                if(builder.matchNodesCanHaveValues()) {
                    head.setValue(value);
                } else {
                    result=new IntermediateValueNode(value, builder.registerNode(head));
                }
            }
            return builder.registerNode(result);
        }
        private Node register(StringTrieBuilder builder, int start, int limit) {
            int length=limit-start;
            if(length>builder.getMaxBranchLinearSubNodeLength()) {
                // Branch on the middle unit.
                int middle=start+length/2;
                return builder.registerNode(
                        new SplitBranchNode(
                                chars.charAt(middle),
                                register(builder, start, middle),
                                register(builder, middle, limit)));
            }
            ListBranchNode listNode=new ListBranchNode(length);
            do {
                char c=chars.charAt(start);
                Node node=equal.get(start);
                if(node.getClass()==ValueNode.class) {
                    // Final value.
                    listNode.add(c, ((ValueNode)node).value);
                } else {
                    listNode.add(c, node.register(builder));
                }
            } while(++start<limit);
            return builder.registerNode(listNode);
        }

        private int find(char c) {
            int start=0;
            int limit=chars.length();
            while(start<limit) {
                int i=(start+limit)/2;
                char middleChar=chars.charAt(i);
                if(c<middleChar) {
                    limit=i;
                } else if(c==middleChar) {
                    return i;
                } else {
                    start=i+1;
                }
            }
            return start;
        }

        private StringBuilder chars=new StringBuilder();
        private ArrayList<Node> equal=new ArrayList<Node>();
    }

    private static abstract class BranchNode extends Node {
        public BranchNode() {}
        @Override
        public int hashCode() /*const*/ { return hash; }

        protected int hash;
        protected int firstEdgeNumber;
    }

    private static final class ListBranchNode extends BranchNode {
        public ListBranchNode(int capacity) {
            hash=0x444444*37+capacity;
            equal=new Node[capacity];
            values=new int[capacity];
            units=new char[capacity];
        }
        @Override
        public boolean equals(Object other) {
            if(this==other) {
                return true;
            }
            if(!super.equals(other)) {
                return false;
            }
            ListBranchNode o=(ListBranchNode)other;
            for(int i=0; i<length; ++i) {
                if(units[i]!=o.units[i] || values[i]!=o.values[i] || equal[i]!=o.equal[i]) {
                    return false;
                }
            }
            return true;
        }
        @Override
        public int hashCode() {
            return super.hashCode();
        }
        @Override
        public int markRightEdgesFirst(int edgeNumber) {
            if(offset==0) {
                firstEdgeNumber=edgeNumber;
                int step=0;
                int i=length;
                do {
                    Node edge=equal[--i];
                    if(edge!=null) {
                        edgeNumber=edge.markRightEdgesFirst(edgeNumber-step);
                    }
                    // For all but the rightmost edge, decrement the edge number.
                    step=1;
                } while(i>0);
                offset=edgeNumber;
            }
            return edgeNumber;
        }
        @Override
        public void write(StringTrieBuilder builder) {
            // Write the sub-nodes in reverse order: The jump lengths are deltas from
            // after their own positions, so if we wrote the minUnit sub-node first,
            // then its jump delta would be larger.
            // Instead we write the minUnit sub-node last, for a shorter delta.
            int unitNumber=length-1;
            Node rightEdge=equal[unitNumber];
            int rightEdgeNumber= rightEdge==null ? firstEdgeNumber : rightEdge.getOffset();
            do {
                --unitNumber;
                if(equal[unitNumber]!=null) {
                    equal[unitNumber].writeUnlessInsideRightEdge(firstEdgeNumber, rightEdgeNumber, builder);
                }
            } while(unitNumber>0);
            // The maxUnit sub-node is written as the very last one because we do
            // not jump for it at all.
            unitNumber=length-1;
            if(rightEdge==null) {
                builder.writeValueAndFinal(values[unitNumber], true);
            } else {
                rightEdge.write(builder);
            }
            offset=builder.write(units[unitNumber]);
            // Write the rest of this node's unit-value pairs.
            while(--unitNumber>=0) {
                int value;
                boolean isFinal;
                if(equal[unitNumber]==null) {
                    // Write the final value for the one string ending with this unit.
                    value=values[unitNumber];
                    isFinal=true;
                } else {
                    // Write the delta to the start position of the sub-node.
                    assert(equal[unitNumber].getOffset()>0);
                    value=offset-equal[unitNumber].getOffset();
                    isFinal=false;
                }
                builder.writeValueAndFinal(value, isFinal);
                offset=builder.write(units[unitNumber]);
            }
        }
        // Adds a unit with a final value.
        public void add(int c, int value) {
            units[length]=(char)c;
            equal[length]=null;
            values[length]=value;
            ++length;
            hash=(hash*37+c)*37+value;
        }
        // Adds a unit which leads to another match node.
        public void add(int c, Node node) {
            units[length]=(char)c;
            equal[length]=node;
            values[length]=0;
            ++length;
            hash=(hash*37+c)*37+node.hashCode();
        }

        // Note: We could try to reduce memory allocations
        // by replacing these per-node arrays with per-builder ArrayLists and
        // (for units) a StringBuilder (or even use its strings for the units too).
        // It remains to be seen whether that would improve performance.
        private Node[] equal;  // null means "has final value".
        private int length;
        private int[] values;
        private char[] units;
    }

    private static final class SplitBranchNode extends BranchNode {
        public SplitBranchNode(char middleUnit, Node lessThanNode, Node greaterOrEqualNode) {
            hash=((0x555555*37+middleUnit)*37+
                    lessThanNode.hashCode())*37+greaterOrEqualNode.hashCode();
            unit=middleUnit;
            lessThan=lessThanNode;
            greaterOrEqual=greaterOrEqualNode;
        }
        @Override
        public boolean equals(Object other) {
            if(this==other) {
                return true;
            }
            if(!super.equals(other)) {
                return false;
            }
            SplitBranchNode o=(SplitBranchNode)other;
            return unit==o.unit && lessThan==o.lessThan && greaterOrEqual==o.greaterOrEqual;
        }
        @Override
        public int hashCode() {
            return super.hashCode();
        }
        @Override
        public int markRightEdgesFirst(int edgeNumber) {
            if(offset==0) {
                firstEdgeNumber=edgeNumber;
                edgeNumber=greaterOrEqual.markRightEdgesFirst(edgeNumber);
                offset=edgeNumber=lessThan.markRightEdgesFirst(edgeNumber-1);
            }
            return edgeNumber;
        }
        @Override
        public void write(StringTrieBuilder builder) {
            // Encode the less-than branch first.
            lessThan.writeUnlessInsideRightEdge(firstEdgeNumber, greaterOrEqual.getOffset(), builder);
            // Encode the greater-or-equal branch last because we do not jump for it at all.
            greaterOrEqual.write(builder);
            // Write this node.
            assert(lessThan.getOffset()>0);
            builder.writeDeltaTo(lessThan.getOffset());  // less-than
            offset=builder.write(unit);
        }

        private char unit;
        private Node lessThan;
        private Node greaterOrEqual;
    }

    // Branch head node, for writing the actual node lead unit.
    private static final class BranchHeadNode extends ValueNode {
        public BranchHeadNode(int len, Node subNode) {
            length=len;
            next=subNode;
        }
        @Override
        public int hashCode() /*const*/ {
            return (0x666666*37+length)*37+next.hashCode();
        }
        @Override
        public boolean equals(Object other) {
            if(this==other) {
                return true;
            }
            if(!super.equals(other)) {
                return false;
            }
            BranchHeadNode o=(BranchHeadNode)other;
            return length==o.length && next==o.next;
        }
        @Override
        public int markRightEdgesFirst(int edgeNumber) {
            if(offset==0) {
                offset=edgeNumber=next.markRightEdgesFirst(edgeNumber);
            }
            return edgeNumber;
        }
        @Override
        public void write(StringTrieBuilder builder) {
            next.write(builder);
            if(length<=builder.getMinLinearMatch()) {
                offset=builder.writeValueAndType(hasValue, value, length-1);
            } else {
                builder.write(length-1);
                offset=builder.writeValueAndType(hasValue, value, 0);
            }
        }

        private int length;
        private Node next;  // A branch sub-node.
    }

    private ValueNode createSuffixNode(CharSequence s, int start, int sValue) {
        ValueNode node=registerFinalValue(sValue);
        if(start<s.length()) {
            int offset=strings.length();
            strings.append(s, start, s.length());
            node=new LinearMatchNode(strings, offset, s.length()-start, node);
        }
        return node;
    }

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract boolean matchNodesCanHaveValues() /*const*/;

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int getMaxBranchLinearSubNodeLength() /*const*/;
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int getMinLinearMatch() /*const*/;
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int getMaxLinearMatchLength() /*const*/;

    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int write(int unit);
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int write(int offset, int length);
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int writeValueAndFinal(int i, boolean isFinal);
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int writeValueAndType(boolean hasValue, int value, int node);
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected abstract int writeDeltaTo(int jumpTarget);

    private enum State {
        ADDING, BUILDING_FAST, BUILDING_SMALL, BUILT
    }
    private State state=State.ADDING;

    // Strings and sub-strings for linear-match nodes.
    /**
     * @deprecated This API is ICU internal only.
     * @hide draft / provisional / internal are hidden on Android
     */
    @Deprecated
    protected StringBuilder strings=new StringBuilder();
    private Node root;

    // Hash set of nodes, maps from nodes to integer 1.
    private HashMap<Node, Node> nodes=new HashMap<Node, Node>();
    private ValueNode lookupFinalValueNode=new ValueNode();
}
