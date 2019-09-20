/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/********************************************************************
 * COPYRIGHT:
 * Copyright (c) 2001-2016, International Business Machines Corporation and
 * others. All Rights Reserved.
 ********************************************************************/

package android.icu.text;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.icu.impl.Assert;

/**
 *   This class represents a node in the parse tree created by the RBBI Rule compiler.
 */
class RBBINode {


 //   enum NodeType {
     static final int    setRef = 0;
     static final int    uset = 1;
     static final int    varRef = 2;
     static final int    leafChar = 3;
     static final int    lookAhead = 4;
     static final int    tag = 5;
     static final int    endMark = 6;
     static final int    opStart = 7;
     static final int    opCat = 8;
     static final int    opOr = 9;
     static final int    opStar = 10;
     static final int    opPlus = 11;
     static final int    opQuestion = 12;
     static final int    opBreak = 13;
     static final int    opReverse = 14;
     static final int    opLParen = 15;
     static final int    nodeTypeLimit = 16;    //  For Assertion checking only.

     static final String []  nodeTypeNames = {
         "setRef",
         "uset",
         "varRef",
         "leafChar",
         "lookAhead",
         "tag",
         "endMark",
         "opStart",
         "opCat",
         "opOr",
         "opStar",
         "opPlus",
         "opQuestion",
         "opBreak",
         "opReverse",
         "opLParen"
     };

//    enum OpPrecedence {
    static final int    precZero   = 0;
    static final int    precStart  = 1;
    static final int    precLParen = 2;
    static final int    precOpOr   = 3;
    static final int    precOpCat  = 4;

    int          fType;   // enum NodeType
    RBBINode      fParent;
    RBBINode      fLeftChild;
    RBBINode      fRightChild;
    UnicodeSet    fInputSet;           // For uset nodes only.
    int          fPrecedence = precZero;   // enum OpPrecedence, For binary ops only.

    String       fText;                 // Text corresponding to this node.
                                        //   May be lazily evaluated when (if) needed
                                        //   for some node types.
    int           fFirstPos;            // Position in the rule source string of the
                                        //   first text associated with the node.
                                        //   If there's a left child, this will be the same
                                        //   as that child's left pos.
    int           fLastPos;             //  Last position in the rule source string
                                        //    of any text associated with this node.
                                        //    If there's a right child, this will be the same
                                        //    as that child's last postion.

    boolean      fNullable;            //  See Aho DFA table generation algorithm
    int           fVal;                 // For leafChar nodes, the value.
                                        //   Values are the character category,
                                        //   corresponds to columns in the final
                                        //   state transition table.

    boolean      fLookAheadEnd;        // For endMark nodes, set TRUE if
                                       //   marking the end of a look-ahead rule.

    boolean      fRuleRoot;             // True if this node is the root of a rule.
    boolean      fChainIn;              // True if chaining into this rule is allowed
                                        //     (no '^' present).


    Set<RBBINode> fFirstPosSet;         // See Aho DFA table generation algorithm
    Set<RBBINode> fLastPosSet;          // See Aho.
    Set<RBBINode> fFollowPos;           // See Aho.

    int           fSerialNum;           //  Debugging aids.  Each node gets a unique serial number.
    static int    gLastSerial;

    RBBINode(int t) {
        Assert.assrt(t < nodeTypeLimit);
        fSerialNum = ++gLastSerial;
        fType = t;

        fFirstPosSet = new HashSet<RBBINode>();
        fLastPosSet = new HashSet<RBBINode>();
        fFollowPos = new HashSet<RBBINode>();
        if (t == opCat) {
            fPrecedence = precOpCat;
        } else if (t == opOr) {
            fPrecedence = precOpOr;
        } else if (t == opStart) {
            fPrecedence = precStart;
        } else if (t == opLParen) {
            fPrecedence = precLParen;
        } else {
            fPrecedence = precZero;
        }
    }

    RBBINode(RBBINode other) {
        fSerialNum = ++gLastSerial;
        fType = other.fType;
        fInputSet = other.fInputSet;
        fPrecedence = other.fPrecedence;
        fText = other.fText;
        fFirstPos = other.fFirstPos;
        fLastPos = other.fLastPos;
        fNullable = other.fNullable;
        fVal = other.fVal;
        fRuleRoot = false;
        fChainIn = other.fChainIn;
        fFirstPosSet = new HashSet<RBBINode>(other.fFirstPosSet);
        fLastPosSet = new HashSet<RBBINode>(other.fLastPosSet);
        fFollowPos = new HashSet<RBBINode>(other.fFollowPos);
    }

    //-------------------------------------------------------------------------
    //
    //        cloneTree Make a copy of the subtree rooted at this node.
    //                      Discard any variable references encountered along the way,
    //                      and replace with copies of the variable's definitions.
    //                      Used to replicate the expression underneath variable
    //                      references in preparation for generating the DFA tables.
    //
    //-------------------------------------------------------------------------
    RBBINode cloneTree() {
        RBBINode n;

        if (fType == RBBINode.varRef) {
            // If the current node is a variable reference, skip over it
            //   and clone the definition of the variable instead.
            n = fLeftChild.cloneTree();
        } else if (fType == RBBINode.uset) {
            n = this;
        } else {
            n = new RBBINode(this);
            if (fLeftChild != null) {
                n.fLeftChild = fLeftChild.cloneTree();
                n.fLeftChild.fParent = n;
            }
            if (fRightChild != null) {
                n.fRightChild = fRightChild.cloneTree();
                n.fRightChild.fParent = n;
            }
        }
        return n;
    }



    //-------------------------------------------------------------------------
    //
    //       flattenVariables Walk a parse tree, replacing any variable
    //                          references with a copy of the variable's definition.
    //                          Aside from variables, the tree is not changed.
    //
    //                          Return the root of the tree. If the root was not a variable
    //                          reference, it remains unchanged - the root we started with
    //                          is the root we return. If, however, the root was a variable
    //                          reference, the root of the newly cloned replacement tree will
    //                          be returned, and the original tree deleted.
    //
    //                          This function works by recursively walking the tree
    //                          without doing anything until a variable reference is
    //                          found, then calling cloneTree() at that point. Any
    //                          nested references are handled by cloneTree(), not here.
    //
    //-------------------------------------------------------------------------
    RBBINode flattenVariables() {
        if (fType == varRef) {
            RBBINode retNode  = fLeftChild.cloneTree();
            retNode.fRuleRoot = this.fRuleRoot;
            retNode.fChainIn  = this.fChainIn;
            return retNode;
        }

        if (fLeftChild != null) {
            fLeftChild = fLeftChild.flattenVariables();
            fLeftChild.fParent = this;
        }
        if (fRightChild != null) {
            fRightChild = fRightChild.flattenVariables();
            fRightChild.fParent = this;
        }
        return this;
    }

    //-------------------------------------------------------------------------
    //
    //      flattenSets Walk the parse tree, replacing any nodes of type setRef
    //                     with a copy of the expression tree for the set. A set's
    //                     equivalent expression tree is precomputed and saved as
    //                     the left child of the uset node.
    //
    //-------------------------------------------------------------------------
    void flattenSets() {
        Assert.assrt(fType != setRef);

        if (fLeftChild != null) {
            if (fLeftChild.fType == setRef) {
                RBBINode setRefNode = fLeftChild;
                RBBINode usetNode = setRefNode.fLeftChild;
                RBBINode replTree = usetNode.fLeftChild;
                fLeftChild = replTree.cloneTree();
                fLeftChild.fParent = this;
            } else {
                fLeftChild.flattenSets();
            }
        }

        if (fRightChild != null) {
            if (fRightChild.fType == setRef) {
                RBBINode setRefNode = fRightChild;
                RBBINode usetNode = setRefNode.fLeftChild;
                RBBINode replTree = usetNode.fLeftChild;
                fRightChild = replTree.cloneTree();
                fRightChild.fParent = this;
                // delete setRefNode;
            } else {
                fRightChild.flattenSets();
            }
        }
    }

    //-------------------------------------------------------------------------
    //
    //       findNodes() Locate all the nodes of the specified type, starting
    //                       at the specified root.
    //
    //-------------------------------------------------------------------------
    void findNodes(List<RBBINode> dest, int kind) {
        if (fType == kind) {
            dest.add(this);
        }
        if (fLeftChild != null) {
            fLeftChild.findNodes(dest, kind);
        }
        if (fRightChild != null) {
            fRightChild.findNodes(dest, kind);
        }
    }



    //-------------------------------------------------------------------------
    //
    //        print. Print out a single node, for debugging.
    //
    //-------------------------------------------------------------------------
    ///CLOVER:OFF
    static void printNode(RBBINode n) {

        if (n==null) {
            System.out.print (" -- null --\n");
        } else {
            RBBINode.printInt( n.fSerialNum, 10);
            RBBINode.printString(nodeTypeNames[n.fType], 11);
            RBBINode.printInt(n.fParent==null? 0     : n.fParent.fSerialNum, 11);
            RBBINode.printInt(n.fLeftChild==null? 0  : n.fLeftChild.fSerialNum, 11);
            RBBINode.printInt(n.fRightChild==null? 0 : n.fRightChild.fSerialNum, 12);
            RBBINode.printInt(n.fFirstPos, 12);
            RBBINode.printInt(n.fVal, 7);

            if (n.fType == varRef) {
                System.out.print(" " + n.fText);
            }
        }
        System.out.println("");
    }
    ///CLOVER:ON


    // Print a String in a fixed field size.
    // Debugging function.
    ///CLOVER:OFF
    static void printString(String s, int minWidth) {
        for (int i = minWidth; i < 0; i++) {
            // negative width means pad leading spaces, not fixed width.
            System.out.print(' ');
        }
        for (int i = s.length(); i < minWidth; i++) {
            System.out.print(' ');
        }
        System.out.print(s);
    }
    ///CLOVER:ON

    //
    //  Print an int in a fixed size field.
    //  Debugging function.
    //
    ///CLOVER:OFF
    static void printInt(int i, int minWidth) {
        String s = Integer.toString(i);
        printString(s, Math.max(minWidth, s.length() + 1));
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    static void printHex(int i, int minWidth) {
        String s = Integer.toString(i, 16);
        String leadingZeroes = "00000"
                .substring(0, Math.max(0, 5 - s.length()));
        s = leadingZeroes + s;
        printString(s, minWidth);
    }
    ///CLOVER:ON


    // -------------------------------------------------------------------------
    //
    //        print. Print out the tree of nodes rooted at "this"
    //
    // -------------------------------------------------------------------------
    ///CLOVER:OFF
    void printTree(boolean printHeading) {
        if (printHeading) {
            System.out.println( "-------------------------------------------------------------------");
            System.out.println("    Serial       type     Parent  LeftChild  RightChild    position  value");
        }
        printNode(this);
            // Only dump the definition under a variable reference if asked to.
            // Unconditinally dump children of all other node types.
            if (fType != varRef) {
                if (fLeftChild != null) {
                    fLeftChild.printTree(false);
                }

                if (fRightChild != null) {
                    fRightChild.printTree(false);
                }
            }
    }
    ///CLOVER:ON

}
