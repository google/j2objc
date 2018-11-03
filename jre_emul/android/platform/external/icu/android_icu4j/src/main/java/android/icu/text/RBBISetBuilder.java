/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
* Copyright (C) 2003-2011, International Business Machines Corporation and    *
* others. All Rights Reserved.                                                *
*******************************************************************************
*/
package android.icu.text;

import com.google.j2objc.annotations.Weak;
import com.google.j2objc.annotations.WeakOuter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.icu.impl.Assert;
import android.icu.impl.IntTrieBuilder;

//
//  RBBISetBuilder   Handles processing of Unicode Sets from RBBI rules
//                   (part of the rule building process.)
//
//      Starting with the rules parse tree from the scanner,
//
//                   -  Enumerate the set of UnicodeSets that are referenced
//                      by the RBBI rules.
//                   -  compute a set of non-overlapping character ranges
//                      with all characters within a range belonging to the same
//                      set of input uniocde sets.
//                   -  Derive a set of non-overlapping UnicodeSet (like things)
//                      that will correspond to columns in the state table for
//                      the RBBI execution engine.  All characters within one
//                      of these sets belong to the same set of the original
//                      UnicodeSets from the user's rules.
//                   -  construct the trie table that maps input characters
//                      to the index of the matching non-overlapping set of set from
//                      the previous step.
//
class RBBISetBuilder {
    static class RangeDescriptor  {
           int                fStartChar;      // Start of range, unicode 32 bit value.
           int                fEndChar;        // End of range, unicode 32 bit value.
           int                fNum;            // runtime-mapped input value for this range.
           List<RBBINode>     fIncludesSets;    // vector of the the original
                                                 //   Unicode sets that include this range.
                                                //    (Contains ptrs to uset nodes)
            RangeDescriptor   fNext;           // Next RangeDescriptor in the linked list.

            RangeDescriptor() {
                fIncludesSets = new ArrayList<RBBINode>();
            }
            
            RangeDescriptor(RangeDescriptor other) {
                fStartChar = other.fStartChar;
                fEndChar   = other.fEndChar;
                fNum       = other.fNum;
                fIncludesSets = new ArrayList<RBBINode>(other.fIncludesSets);
            }
 
            //-------------------------------------------------------------------------------------
            //
            //          RangeDesriptor::split()
            //
            //-------------------------------------------------------------------------------------
            void split(int where) {
                Assert.assrt(where>fStartChar && where<=fEndChar);
                RangeDescriptor nr = new RangeDescriptor(this);
 
                //  RangeDescriptor copy constructor copies all fields.
                //  Only need to update those that are different after the split.
                nr.fStartChar = where;
                this.fEndChar = where-1;
                nr.fNext      = this.fNext;
                this.fNext    = nr;
                
                // TODO:  fIncludesSets is not updated.  Check it out.
                //         Probably because they haven't been populated yet, 
                //         but still sloppy.
            }

            
            //-------------------------------------------------------------------------------------
            //
            //          RangeDescriptor::setDictionaryFlag
            //
            //          Character Category Numbers that include characters from
            //          the original Unicode Set named "dictionary" have bit 14
            //          set to 1.  The RBBI runtime engine uses this to trigger
            //          use of the word dictionary.
            //
            //          This function looks through the Unicode Sets that it
            //          (the range) includes, and sets the bit in fNum when
            //          "dictionary" is among them.
            //
            //          TODO:  a faster way would be to find the set node for
            //          "dictionary" just once, rather than looking it
            //          up by name every time.
            //            
            // -------------------------------------------------------------------------------------
            void setDictionaryFlag() {
                int i;
                
                for (i=0; i<this.fIncludesSets.size(); i++) {
                    RBBINode        usetNode    = fIncludesSets.get(i);
                    String          setName = "";
                    RBBINode        setRef = usetNode.fParent;
                    if (setRef != null) {
                        RBBINode varRef = setRef.fParent;
                        if (varRef != null  &&  varRef.fType == RBBINode.varRef) {
                            setName = varRef.fText;
                        }
                    }
                    if (setName.equals("dictionary")) {
                        this.fNum |= 0x4000;
                        break;
                    }
                }

        }
    }

    @Weak RBBIRuleBuilder fRB;             // The RBBI Rule Compiler that owns us.
    RangeDescriptor       fRangeList;      // Head of the linked list of RangeDescriptors

    IntTrieBuilder        fTrie;           // The mapping TRIE that is the end result of processing
                                           //  the Unicode Sets.

    // Groups correspond to character categories -
    //       groups of ranges that are in the same original UnicodeSets.
    //       fGroupCount is the index of the last used group.
    //       fGroupCount+1 is also the number of columns in the RBBI state table being compiled.
    //       State table column 0 is not used.  Column 1 is for end-of-input.
    //       column 2 is for group 0.  Funny counting.
    int                fGroupCount;

    boolean             fSawBOF;
    
    
    //------------------------------------------------------------------------
    //
    //       RBBISetBuilder Constructor
    //
    //------------------------------------------------------------------------
    RBBISetBuilder(RBBIRuleBuilder rb)
    {
        fRB             = rb;
    }


    //------------------------------------------------------------------------
    //
    //           build          Build the list of non-overlapping character ranges
    //                          from the Unicode Sets.
    //
    //------------------------------------------------------------------------
    void build() {
        RangeDescriptor rlRange;

        if (fRB.fDebugEnv!=null  && fRB.fDebugEnv.indexOf("usets")>=0) {printSets();}

        //  Initialize the process by creating a single range encompassing all characters
        //  that is in no sets.
        //
        fRangeList               = new RangeDescriptor(); 
        fRangeList.fStartChar    = 0;
        fRangeList.fEndChar      = 0x10ffff;

        //
        //  Find the set of non-overlapping ranges of characters
        //
        for (RBBINode usetNode : fRB.fUSetNodes) {
            UnicodeSet      inputSet             = usetNode.fInputSet;
            int            inputSetRangeCount   = inputSet.getRangeCount();
            int            inputSetRangeIndex   = 0;
            rlRange              = fRangeList;

            for (;;) {
                if (inputSetRangeIndex >= inputSetRangeCount) {
                    break;
                }
                int      inputSetRangeBegin  = inputSet.getRangeStart(inputSetRangeIndex);
                int      inputSetRangeEnd    = inputSet.getRangeEnd(inputSetRangeIndex);

                // skip over ranges from the range list that are completely
                //   below the current range from the input unicode set.
                while (rlRange.fEndChar < inputSetRangeBegin) {
                    rlRange = rlRange.fNext;
                }

                // If the start of the range from the range list is before with
                //   the start of the range from the unicode set, split the range list range
                //   in two, with one part being before (wholly outside of) the unicode set
                //   and the other containing the rest.
                //   Then continue the loop; the post-split current range will then be skipped
                //     over
                if (rlRange.fStartChar < inputSetRangeBegin) {
                    rlRange.split(inputSetRangeBegin);
                     continue;
                }

                // Same thing at the end of the ranges...
                // If the end of the range from the range list doesn't coincide with
                //   the end of the range from the unicode set, split the range list
                //   range in two.  The first part of the split range will be
                //   wholly inside the Unicode set.
                if (rlRange.fEndChar > inputSetRangeEnd) {
                    rlRange.split(inputSetRangeEnd+1);
                 }

                // The current rlRange is now entirely within the UnicodeSet range.
                // Add this unicode set to the list of sets for this rlRange
                if (rlRange.fIncludesSets.indexOf(usetNode) == -1) {
                    rlRange.fIncludesSets.add(usetNode);
                }

                // Advance over ranges that we are finished with.
                if (inputSetRangeEnd == rlRange.fEndChar) {
                    inputSetRangeIndex++;
                }
                rlRange = rlRange.fNext;
            }
        }

        if (fRB.fDebugEnv!=null && fRB.fDebugEnv.indexOf("range")>=0) { printRanges();}

        //
        //  Group the above ranges, with each group consisting of one or more
        //    ranges that are in exactly the same set of original UnicodeSets.
        //    The groups are numbered, and these group numbers are the set of
        //    input symbols recognized by the run-time state machine.
        //
        //    Numbering: # 0  (state table column 0) is unused.
        //               # 1  is reserved - table column 1 is for end-of-input
        //               # 2  is reserved - table column 2 is for beginning-in-input
        //               # 3  is the first range list.
        //
        RangeDescriptor rlSearchRange;
        for (rlRange = fRangeList; rlRange!=null; rlRange=rlRange.fNext) {
            for (rlSearchRange=fRangeList; rlSearchRange != rlRange; rlSearchRange=rlSearchRange.fNext) {
                if (rlRange.fIncludesSets.equals(rlSearchRange.fIncludesSets)) {
                    rlRange.fNum = rlSearchRange.fNum;
                    break;
                }
            }
            if (rlRange.fNum == 0) {
                fGroupCount ++;
                rlRange.fNum = fGroupCount+2; 
                rlRange.setDictionaryFlag();
                addValToSets(rlRange.fIncludesSets, fGroupCount+2);
            }
        }

        // Handle input sets that contain the special string {eof}.
        //   Column 1 of the state table is reserved for EOF on input.
        //   Column 2 is reserved for before-the-start-input.
        //            (This column can be optimized away later if there are no rule
        //             references to {bof}.)
        //   Add this column value (1 or 2) to the equivalent expression
        //     subtree for each UnicodeSet that contains the string {eof}
        //   Because {bof} and {eof} are not a characters in the normal sense,
        //   they doesn't affect the computation of ranges or TRIE.
        
        String eofString = "eof";
        String bofString = "bof";

        for (RBBINode usetNode : fRB.fUSetNodes) {
            UnicodeSet      inputSet = usetNode.fInputSet;
            if (inputSet.contains(eofString)) {
                addValToSet(usetNode, 1);
            }
            if (inputSet.contains(bofString)) {
                addValToSet(usetNode, 2);
                fSawBOF = true;
            }
        }


        if (fRB.fDebugEnv!=null  && fRB.fDebugEnv.indexOf("rgroup")>=0) {printRangeGroups();}
        if (fRB.fDebugEnv!=null  && fRB.fDebugEnv.indexOf("esets")>=0) {printSets();}


        //IntTrieBuilder(int aliasdata[], int maxdatalength, 
        //        int initialvalue, int leadunitvalue, 
        //        boolean latin1linear)
        
        fTrie = new IntTrieBuilder(null,   //   Data array  (utrie will allocate one)
                                   100000,  //   Max Data Length
                                   0,       //   Initial value for all code points
                                   0,       //   Lead Surrogate unit value,
                                   true);   //   Keep Latin 1 in separately.
        
        for (rlRange = fRangeList; rlRange!=null; rlRange=rlRange.fNext) {
            fTrie.setRange(rlRange.fStartChar, rlRange.fEndChar+1, rlRange.fNum, true);
        }
    }



    //-----------------------------------------------------------------------------------
    //
    //   RBBIDataManipulate  A little internal class needed only to wrap of the 
    //                       getFoldedValue() function needed for Trie table creation.
    //
    //-----------------------------------------------------------------------------------
   @WeakOuter
   class RBBIDataManipulate implements IntTrieBuilder.DataManipulate {
        public int getFoldedValue(int start, int offset) {
            int  value;
            int  limit;
            boolean [] inBlockZero = new boolean[1];
            
            limit = start + 0x400;
            while(start<limit) {
                value = fTrie.getValue(start, inBlockZero);
                if (inBlockZero[0]) {
                    start += IntTrieBuilder.DATA_BLOCK_LENGTH;  
                } else if (value != 0) {
                    return offset | 0x08000;
                } else {
                    ++start;
                }
            }
            return 0;
         }
    }
    RBBIDataManipulate dm = new RBBIDataManipulate();
    
    //-----------------------------------------------------------------------------------
    //
    //          getTrieSize()    Return the size that will be required to serialize the Trie.
    //
    //-----------------------------------------------------------------------------------
    int getTrieSize()  {
        int size = 0;
        try {
            // The trie serialize function returns the size of the data written.
            //    null output stream says give size only, don't actually write anything.
            size = fTrie.serialize(null, true, dm );
        } catch (IOException e) {
            Assert.assrt (false);
        }
        return size;
    }


    //-----------------------------------------------------------------------------------
    //
    //          serializeTrie()   Write the serialized trie to an output stream
    //
    //-----------------------------------------------------------------------------------
    void serializeTrie(OutputStream os) throws IOException {
        fTrie.serialize(os, true, dm );
   }

    //------------------------------------------------------------------------
    //
    //      addValToSets     Add a runtime-mapped input value to each uset from a
    //      list of uset nodes. (val corresponds to a state table column.)
    //      For each of the original Unicode sets - which correspond
    //      directly to uset nodes - a logically equivalent expression
    //      is constructed in terms of the remapped runtime input
    //      symbol set.  This function adds one runtime input symbol to
    //      a list of sets.
    //
    //      The "logically equivalent expression" is the tree for an
    //      or-ing together of all of the symbols that go into the set.
    //
    //------------------------------------------------------------------------
    void  addValToSets(List<RBBINode> sets, int val) {
        for (RBBINode usetNode : sets) {
            addValToSet(usetNode, val);
        }
    }

    void  addValToSet(RBBINode usetNode, int val) {
        RBBINode leafNode = new RBBINode(RBBINode.leafChar);
        leafNode.fVal = val;
        if (usetNode.fLeftChild == null) {
            usetNode.fLeftChild = leafNode;
            leafNode.fParent    = usetNode;
        } else {
            // There are already input symbols present for this set.
            // Set up an OR node, with the previous stuff as the left child
            //   and the new value as the right child.
            RBBINode orNode = new RBBINode(RBBINode.opOr);
            orNode.fLeftChild  = usetNode.fLeftChild;
            orNode.fRightChild = leafNode;
            orNode.fLeftChild.fParent  = orNode;
            orNode.fRightChild.fParent = orNode;
            usetNode.fLeftChild = orNode;
            orNode.fParent = usetNode;
        }
    }


    //------------------------------------------------------------------------
    //
    //           getNumCharCategories
    //
    //------------------------------------------------------------------------
    int  getNumCharCategories()  {
        return fGroupCount + 3;
    }


    //------------------------------------------------------------------------
    //
    //           sawBOF
    //
    //------------------------------------------------------------------------
    boolean  sawBOF()  {
        return fSawBOF;
    }


    //------------------------------------------------------------------------
    //
    //           getFirstChar      Given a runtime RBBI character category, find
    //                             the first UChar32 that is in the set of chars 
    //                             in the category.
    //------------------------------------------------------------------------
    int  getFirstChar(int category)  {
        RangeDescriptor   rlRange;
        int            retVal = -1;
        for (rlRange = fRangeList; rlRange!=null; rlRange=rlRange.fNext) {
            if (rlRange.fNum == category) {
                retVal = rlRange.fStartChar;
                break;
            }
        }
        return retVal;
    }



    //------------------------------------------------------------------------
    //
    //           printRanges        A debugging function.
    //                              dump out all of the range definitions.
    //
    //------------------------------------------------------------------------
    ///CLOVER:OFF
    void printRanges() {
        RangeDescriptor       rlRange;
        int                    i;

        System.out.print("\n\n Nonoverlapping Ranges ...\n");
        for (rlRange = fRangeList; rlRange!=null; rlRange=rlRange.fNext) {
            System.out.print(" " + rlRange.fNum + "   " + rlRange.fStartChar + "-" + rlRange.fEndChar);

            for (i=0; i<rlRange.fIncludesSets.size(); i++) {
                RBBINode       usetNode    = rlRange.fIncludesSets.get(i);
                String         setName = "anon";
                RBBINode       setRef = usetNode.fParent;
                if (setRef != null) {
                    RBBINode varRef = setRef.fParent;
                    if (varRef != null  &&  varRef.fType == RBBINode.varRef) {
                        setName = varRef.fText;
                    }
                }
                System.out.print(setName); System.out.print("  ");
            }
            System.out.println("");
        }
    }
    ///CLOVER:ON


    //------------------------------------------------------------------------
    //
    //           printRangeGroups     A debugging function.
    //                                dump out all of the range groups.
    //
    //------------------------------------------------------------------------
    ///CLOVER:OFF
    void printRangeGroups() {
        RangeDescriptor       rlRange;
        RangeDescriptor       tRange;
        int                    i;
        int                    lastPrintedGroupNum = 0;

        System.out.print("\nRanges grouped by Unicode Set Membership...\n");
        for (rlRange = fRangeList; rlRange!=null; rlRange=rlRange.fNext) {
            int groupNum = rlRange.fNum & 0xbfff;
            if (groupNum > lastPrintedGroupNum) {
                lastPrintedGroupNum = groupNum;
                if (groupNum<10) {System.out.print(" ");}
                System.out.print(groupNum + " ");

                if ((rlRange.fNum & 0x4000) != 0) { System.out.print(" <DICT> ");}

                for (i=0; i<rlRange.fIncludesSets.size(); i++) {
                    RBBINode       usetNode    = rlRange.fIncludesSets.get(i);
                    String         setName = "anon";
                    RBBINode       setRef = usetNode.fParent;
                    if (setRef != null) {
                        RBBINode varRef = setRef.fParent;
                        if (varRef != null  &&  varRef.fType == RBBINode.varRef) {
                            setName = varRef.fText;
                        }
                    }
                    System.out.print(setName); System.out.print(" ");
                }

                i = 0;
                for (tRange = rlRange; tRange != null; tRange = tRange.fNext) {
                    if (tRange.fNum == rlRange.fNum) {
                        if (i++ % 5 == 0) {
                            System.out.print("\n    ");
                        }
                        RBBINode.printHex(tRange.fStartChar, -1);
                        System.out.print("-");
                        RBBINode.printHex(tRange.fEndChar, 0);
                    }
                }
                System.out.print("\n");
            }
        }
        System.out.print("\n");
    }
    ///CLOVER:ON


    //------------------------------------------------------------------------
    //
    //           printSets          A debugging function.
    //                              dump out all of the set definitions.
    //
    //------------------------------------------------------------------------
    ///CLOVER:OFF
    void printSets() {
        int                   i;
        System.out.print("\n\nUnicode Sets List\n------------------\n");
        for (i=0; i<fRB.fUSetNodes.size(); i++) {
            RBBINode        usetNode;
            RBBINode        setRef;
            RBBINode        varRef;
            String          setName;

            usetNode = fRB.fUSetNodes.get(i);

            //System.out.print(" " + i + "   ");
            RBBINode.printInt(2, i);
            setName = "anonymous";
            setRef = usetNode.fParent;
            if (setRef != null) {
                varRef = setRef.fParent;
                if (varRef != null  &&  varRef.fType == RBBINode.varRef) {
                    setName = varRef.fText;
                }
            }
            System.out.print("  " + setName);
            System.out.print("   ");
            System.out.print(usetNode.fText);
            System.out.print("\n");
            if (usetNode.fLeftChild != null) {
                usetNode.fLeftChild.printTree(true);
            }
        }
        System.out.print("\n");
    }
    ///CLOVER:ON
}
