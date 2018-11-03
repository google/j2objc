/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
***************************************************************************
*   Copyright (C) 2002-2009 International Business Machines Corporation   *
*   and others. All rights reserved.                                      *
***************************************************************************
*/
package android.icu.text;

import java.text.ParsePosition;
import java.util.HashMap;

import android.icu.lang.UCharacter;

class RBBISymbolTable implements SymbolTable{
    
    HashMap<String, RBBISymbolTableEntry> fHashTable;
    RBBIRuleScanner      fRuleScanner;

    // These next two fields are part of the mechanism for passing references to
    //   already-constructed UnicodeSets back to the UnicodeSet constructor
    //   when the pattern includes $variable references.
    String               ffffString;
    UnicodeSet           fCachedSetLookup;
    
    
    
    static class RBBISymbolTableEntry  { 
        String          key;
        RBBINode        val;
    }

    
    RBBISymbolTable(RBBIRuleScanner rs) {
        fRuleScanner = rs;
        fHashTable = new HashMap<String, RBBISymbolTableEntry>();
        ffffString = "\uffff";
    }

    //
    //  RBBISymbolTable::lookup       This function from the abstract symbol table inteface
    //                                looks up a variable name and returns a UnicodeString
    //                                containing the substitution text.
    //
    //                                The variable name does NOT include the leading $.
    //
    public char[] lookup(String s) {
        RBBISymbolTableEntry el;
        RBBINode varRefNode;
        RBBINode exprNode;

        RBBINode usetNode;
        String retString;

        el = fHashTable.get(s);
        if (el == null) {
            return null;
        }

        // Walk through any chain of variable assignments that ultimately resolve to a Set Ref.
        varRefNode = el.val;
        while (varRefNode.fLeftChild.fType == RBBINode.varRef) {
            varRefNode = varRefNode.fLeftChild;
        }

        exprNode = varRefNode.fLeftChild; // Root node of expression for variable
        if (exprNode.fType == RBBINode.setRef) {
            // The $variable refers to a single UnicodeSet
            //   return the ffffString, which will subsequently be interpreted as a
            //   stand-in character for the set by RBBISymbolTable::lookupMatcher()
            usetNode = exprNode.fLeftChild;
            fCachedSetLookup = usetNode.fInputSet;
            retString = ffffString;
        } else {
            // The variable refers to something other than just a set.
            // This is an error in the rules being compiled.  $Variables inside of UnicodeSets
            //   must refer only to another set, not to some random non-set expression.
            //   Note:  single characters are represented as sets, so they are ok.
            fRuleScanner.error(RBBIRuleBuilder.U_BRK_MALFORMED_SET);
            retString = exprNode.fText;
            fCachedSetLookup = null;
        }
        return retString.toCharArray();
    }

    //
    //  RBBISymbolTable::lookupMatcher   This function from the abstract symbol table
    //                                   interface maps a single stand-in character to a
    //                                   pointer to a Unicode Set.   The Unicode Set code uses this
    //                                   mechanism to get all references to the same $variable
    //                                   name to refer to a single common Unicode Set instance.
    //
    //    This implementation cheats a little, and does not maintain a map of stand-in chars
    //    to sets.  Instead, it takes advantage of the fact that  the UnicodeSet
    //    constructor will always call this function right after calling lookup(),
    //    and we just need to remember what set to return between these two calls.
    public UnicodeMatcher lookupMatcher(int ch) {
        UnicodeSet retVal = null;
        if (ch == 0xffff) {
            retVal = fCachedSetLookup;
            fCachedSetLookup = null;
        }
        return retVal;
    }

    //
    // RBBISymbolTable::parseReference   This function from the abstract symbol table interface
    //                                   looks for a $variable name in the source text.
    //                                   It does not look it up, only scans for it.
    //                                   It is used by the UnicodeSet parser.
    //
    public String parseReference(String text, ParsePosition pos, int limit) {
        int start = pos.getIndex();
        int i = start;
        String result = "";
        while (i < limit) {
            int c = UTF16.charAt(text, i);
            if ((i == start && !UCharacter.isUnicodeIdentifierStart(c))
                    || !UCharacter.isUnicodeIdentifierPart(c)) {
                break;
            }
            i += UTF16.getCharCount(c);
        }
        if (i == start) { // No valid name chars
            return result; // Indicate failure with empty string
        }
        pos.setIndex(i);
        result = text.substring(start, i);
        return result;
    }

    //
    // RBBISymbolTable::lookupNode      Given a key (a variable name), return the
    //                                  corresponding RBBI Node.  If there is no entry
    //                                  in the table for this name, return NULL.
    //
    RBBINode lookupNode(String key) {

        RBBINode retNode = null;
        RBBISymbolTableEntry el;

        el = fHashTable.get(key);
        if (el != null) {
            retNode = el.val;
        }
        return retNode;
    }

    //
    //    RBBISymbolTable::addEntry     Add a new entry to the symbol table.
    //                                  Indicate an error if the name already exists -
    //                                    this will only occur in the case of duplicate
    //                                    variable assignments.
    //
    void addEntry(String key, RBBINode val) {
        RBBISymbolTableEntry e;
        e = fHashTable.get(key);
        if (e != null) {
            fRuleScanner.error(RBBIRuleBuilder.U_BRK_VARIABLE_REDFINITION);
            return;
        }

        e = new RBBISymbolTableEntry();
        e.key = key;
        e.val = val;
        fHashTable.put(e.key, e);
    }

    //
    //  RBBISymbolTable::print    Debugging function, dump out the symbol table contents.
    //
    ///CLOVER:OFF
    void rbbiSymtablePrint() {
        System.out
                .print("Variable Definitions\n"
                        + "Name               Node Val     String Val\n"
                        + "----------------------------------------------------------------------\n");

        RBBISymbolTableEntry[] syms = fHashTable.values().toArray(new RBBISymbolTableEntry[0]);

        for (int i = 0; i < syms.length; i++) {
            RBBISymbolTableEntry s = syms[i];

            System.out.print("  " + s.key + "  "); // TODO:  format output into columns.
            System.out.print("  " + s.val + "  ");
            System.out.print(s.val.fLeftChild.fText);
            System.out.print("\n");
        }

        System.out.println("\nParsed Variable Definitions\n");
        for (int i = 0; i < syms.length; i++) {
            RBBISymbolTableEntry s = syms[i];
            System.out.print(s.key);
            s.val.fLeftChild.printTree(true);
            System.out.print("\n");
        }
    }
    ///CLOVER:ON
    
}
