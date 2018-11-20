/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
//
//    Copyright (C) 2002-2014, International Business Machines Corporation and others.
//    All Rights Reserved.
//
//

package android.icu.text;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.icu.impl.Assert;
import android.icu.impl.ICUBinary;
import android.icu.impl.ICUDebug;

class RBBIRuleBuilder {
    //   This is the main class for building (compiling) break rules into the tables
    //    required by the runtime RBBI engine.
    //
    
    String fDebugEnv;              // controls debug trace output
    String fRules;                 // The rule string that we are compiling
    RBBIRuleScanner fScanner;      // The scanner.

    
    //
    //  There are four separate parse trees generated, one for each of the
    //    forward rules, reverse rules, safe forward rules and safe reverse rules.
    //  This array references the root of each of the trees.
    //  
    RBBINode[]         fTreeRoots = new RBBINode[4];
    static final int   fForwardTree = 0;  // Indexes into the above fTreeRoots array
    static final int   fReverseTree = 1;  //   for each of the trees.
    static final int   fSafeFwdTree = 2;  //   (in C, these are pointer variables and
    static final int   fSafeRevTree = 3;  //    there is no array.)
    int fDefaultTree = fForwardTree;      // For rules not qualified with a !
                                          //   the tree to which they belong to.

    boolean fChainRules;                  // True for chained Unicode TR style rules.
                                          // False for traditional regexp rules.

    boolean fLBCMNoChain;                 // True:  suppress chaining of rules on
                                          //   chars with LineBreak property == CM.

    boolean fLookAheadHardBreak;          // True:  Look ahead matches cause an
                                          // immediate break, no continuing for the
                                          // longest match.

    RBBISetBuilder fSetBuilder;           // Set and Character Category builder.
    List<RBBINode> fUSetNodes;            // Vector of all uset nodes.
    RBBITableBuilder fForwardTables;      // State transition tables
    RBBITableBuilder fReverseTables;
    RBBITableBuilder fSafeFwdTables;
    RBBITableBuilder fSafeRevTables;

    //
    // Status {tag} values.   These structures are common to all of the rule sets (Forward, Reverse, etc.).
    //
    Map<Set<Integer>, Integer> fStatusSets = new HashMap<Set<Integer>, Integer>(); // Status value sets encountered so far.
                                                                                   //  Map Key is the set of values.
                                                                                   //  Map Value is the runtime array index.

    List<Integer> fRuleStatusVals;        // List of Integer objects.  Has same layout as the
                                          //   runtime array of status (tag) values - 
                                          //     number of values in group 1
                                          //        first status value in group 1
                                          //        2nd status value in group 1
                                          //        ...
                                          //     number of values in group 2
                                          //        first status value in group 2
                                          //        etc.
                                          //
    // Error codes from ICU4C.
    //    using these simplified the porting, and consolidated the
    //    creation of Java exceptions
    //
    static final int U_BRK_ERROR_START = 0x10200;
    /**< Start of codes indicating Break Iterator failures */
    
    static final int U_BRK_INTERNAL_ERROR = 0x10201;
    /**< An internal error (bug) was detected.             */
    
    static final int U_BRK_HEX_DIGITS_EXPECTED = 0x10202;
    /**< Hex digits expected as part of a escaped char in a rule. */
    
    static final int U_BRK_SEMICOLON_EXPECTED = 0x10203;
    /**< Missing ';' at the end of a RBBI rule.            */
    
    static final int U_BRK_RULE_SYNTAX = 0x10204;
    /**< Syntax error in RBBI rule.                        */
    
    static final int U_BRK_UNCLOSED_SET = 0x10205;
    /**< UnicodeSet witing an RBBI rule missing a closing ']'.  */
    
    static final int U_BRK_ASSIGN_ERROR = 0x10206;
    /**< Syntax error in RBBI rule assignment statement.   */
    
    static final int U_BRK_VARIABLE_REDFINITION = 0x10207;
    /**< RBBI rule $Variable redefined.                    */
    
    static final int U_BRK_MISMATCHED_PAREN = 0x10208;
    /**< Mis-matched parentheses in an RBBI rule.          */
    
    static final int U_BRK_NEW_LINE_IN_QUOTED_STRING = 0x10209;
    /**< Missing closing quote in an RBBI rule.            */
    
    static final int U_BRK_UNDEFINED_VARIABLE = 0x1020a;
    /**< Use of an undefined $Variable in an RBBI rule.    */
    
    static final int U_BRK_INIT_ERROR = 0x1020b;
    /**< Initialization failure.  Probable missing ICU Data. */
    
    static final int U_BRK_RULE_EMPTY_SET = 0x1020c;
    /**< Rule contains an empty Unicode Set.               */
    
    static final int U_BRK_UNRECOGNIZED_OPTION = 0x1020d;
    /**< !!option in RBBI rules not recognized.            */
    
    static final int U_BRK_MALFORMED_RULE_TAG = 0x1020e;
    /**< The {nnn} tag on a rule is mal formed             */
    static final int U_BRK_MALFORMED_SET = 0x1020f;
    
    static final int U_BRK_ERROR_LIMIT = 0x10210;
    /**< This must always be the last value to indicate the limit for Break Iterator failures */


    //----------------------------------------------------------------------------------------
    //
    //  Constructor.
    //
    //----------------------------------------------------------------------------------------
    RBBIRuleBuilder(String rules)
    {
        fDebugEnv       = ICUDebug.enabled("rbbi") ?
                            ICUDebug.value("rbbi") : null;
        fRules          = rules;
        fUSetNodes      = new ArrayList<RBBINode>();
        fRuleStatusVals = new ArrayList<Integer>();
        fScanner        = new RBBIRuleScanner(this);
        fSetBuilder     = new RBBISetBuilder(this);
    }

    //----------------------------------------------------------------------------------------
    //
    //   flattenData() -  Collect up the compiled RBBI rule data and put it into
    //                    the format for saving in ICU data files,
    //
    //                    See the ICU4C file common/rbidata.h for a detailed description.
    //
    //----------------------------------------------------------------------------------------
    static final int align8(int i)
    {
        return (i + 7) & 0xfffffff8;
    }

    void flattenData(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        int i;

        //  Remove comments and whitespace from the rules to make it smaller.
        String strippedRules = RBBIRuleScanner.stripRules(fRules);

        // Calculate the size of each section in the data in bytes.
        //   Sizes here are padded up to a multiple of 8 for better memory alignment.
        //   Sections sizes actually stored in the header are for the actual data
        //     without the padding.
        //
        int headerSize       = 24 * 4;     // align8(sizeof(RBBIDataHeader));
        int forwardTableSize = align8(fForwardTables.getTableSize());
        int reverseTableSize = align8(fReverseTables.getTableSize());
        int safeFwdTableSize = align8(fSafeFwdTables.getTableSize());
        int safeRevTableSize = align8(fSafeRevTables.getTableSize());
        int trieSize         = align8(fSetBuilder.getTrieSize());
        int statusTableSize  = align8(fRuleStatusVals.size() * 4);
        int rulesSize        = align8((strippedRules.length()) * 2);
        int totalSize        = headerSize + forwardTableSize + reverseTableSize
                                + safeFwdTableSize + safeRevTableSize
                                + statusTableSize + trieSize + rulesSize;
        int outputPos = 0;               // Track stream position, starting from RBBIDataHeader.

        //
        // Write out an ICU Data Header
        //
        ICUBinary.writeHeader(RBBIDataWrapper.DATA_FORMAT, RBBIDataWrapper.FORMAT_VERSION, 0, dos);

        //
        // Write out the RBBIDataHeader
        //
        int[] header = new int[RBBIDataWrapper.DH_SIZE];                 // sizeof struct RBBIDataHeader
        header[RBBIDataWrapper.DH_MAGIC]         = 0xb1a0;
        header[RBBIDataWrapper.DH_FORMATVERSION] = 0x03010000;           // uint8_t fFormatVersion[4];
        header[RBBIDataWrapper.DH_LENGTH]        = totalSize;            // fLength, the total size of all rule sections.
        header[RBBIDataWrapper.DH_CATCOUNT]      = fSetBuilder.getNumCharCategories(); // fCatCount.
        header[RBBIDataWrapper.DH_FTABLE]        = headerSize;           // fFTable
        header[RBBIDataWrapper.DH_FTABLELEN]     = forwardTableSize;     // fTableLen
        header[RBBIDataWrapper.DH_RTABLE]        = header[RBBIDataWrapper.DH_FTABLE] + forwardTableSize; // fRTable
        header[RBBIDataWrapper.DH_RTABLELEN]     = reverseTableSize;     // fRTableLen
        header[RBBIDataWrapper.DH_SFTABLE]       = header[RBBIDataWrapper.DH_RTABLE]
                                                     + reverseTableSize; // fSTable
        header[RBBIDataWrapper.DH_SFTABLELEN]    = safeFwdTableSize;     // fSTableLen
        header[RBBIDataWrapper.DH_SRTABLE]       = header[RBBIDataWrapper.DH_SFTABLE]
                                                     + safeFwdTableSize; // fSRTable
        header[RBBIDataWrapper.DH_SRTABLELEN]    = safeRevTableSize;     // fSRTableLen
        header[RBBIDataWrapper.DH_TRIE]          = header[RBBIDataWrapper.DH_SRTABLE]
                                                     + safeRevTableSize; // fTrie
        header[RBBIDataWrapper.DH_TRIELEN]       = fSetBuilder.getTrieSize(); // fTrieLen
        header[RBBIDataWrapper.DH_STATUSTABLE]   = header[RBBIDataWrapper.DH_TRIE]
                                                     + header[RBBIDataWrapper.DH_TRIELEN];
        header[RBBIDataWrapper.DH_STATUSTABLELEN] = statusTableSize; // fStatusTableLen
        header[RBBIDataWrapper.DH_RULESOURCE]    = header[RBBIDataWrapper.DH_STATUSTABLE]
                                                     + statusTableSize;
        header[RBBIDataWrapper.DH_RULESOURCELEN] = strippedRules.length() * 2;
        for (i = 0; i < header.length; i++) {
            dos.writeInt(header[i]);
            outputPos += 4;
        }

        // Write out the actual state tables.
        short[] tableData;
        tableData = fForwardTables.exportTable();
        Assert.assrt(outputPos == header[4]);
        for (i = 0; i < tableData.length; i++) {
            dos.writeShort(tableData[i]);
            outputPos += 2;
        }

        tableData = fReverseTables.exportTable();
        Assert.assrt(outputPos == header[6]);
        for (i = 0; i < tableData.length; i++) {
            dos.writeShort(tableData[i]);
            outputPos += 2;
        }

        Assert.assrt(outputPos == header[8]);
        tableData = fSafeFwdTables.exportTable();
        for (i = 0; i < tableData.length; i++) {
            dos.writeShort(tableData[i]);
            outputPos += 2;
        }

        Assert.assrt(outputPos == header[10]);
        tableData = fSafeRevTables.exportTable();
        for (i = 0; i < tableData.length; i++) {
            dos.writeShort(tableData[i]);
            outputPos += 2;
        }

        // write out the Trie table
        Assert.assrt(outputPos == header[12]);
        fSetBuilder.serializeTrie(os);
        outputPos += header[13];
        while (outputPos % 8 != 0) { // pad to an 8 byte boundary
            dos.write(0);
            outputPos += 1;
        }

        // Write out the status {tag} table.
        Assert.assrt(outputPos == header[16]);
        for (Integer val : fRuleStatusVals) {
            dos.writeInt(val.intValue());
            outputPos += 4;
        }

        while (outputPos % 8 != 0) { // pad to an 8 byte boundary
            dos.write(0);
            outputPos += 1;
        }

        // Write out the stripped rules (rules with extra spaces removed
        //   These go last in the data area, even though they are not last in the header.
        Assert.assrt(outputPos == header[14]);
        dos.writeChars(strippedRules);
        outputPos += strippedRules.length() * 2;
        while (outputPos % 8 != 0) { // pad to an 8 byte boundary
            dos.write(0);
            outputPos += 1;
        }
    }

    //----------------------------------------------------------------------------------------
    //
    //  compileRules          compile source rules, placing the compiled form into a output stream
    //                        The compiled form is identical to that from ICU4C (Big Endian).
    //
    //----------------------------------------------------------------------------------------
    static void compileRules(String rules, OutputStream os) throws IOException
    {
        //
        // Read the input rules, generate a parse tree, symbol table,
        // and list of all Unicode Sets referenced by the rules.
        //
        RBBIRuleBuilder builder = new RBBIRuleBuilder(rules);
        builder.fScanner.parse();

        //
        // UnicodeSet processing.
        //    Munge the Unicode Sets to create a set of character categories.
        //    Generate the mapping tables (TRIE) from input 32-bit characters to
        //    the character categories.
        //
        builder.fSetBuilder.build();

        //
        //   Generate the DFA state transition table.
        //
        builder.fForwardTables = new RBBITableBuilder(builder, fForwardTree);
        builder.fReverseTables = new RBBITableBuilder(builder, fReverseTree);
        builder.fSafeFwdTables = new RBBITableBuilder(builder, fSafeFwdTree);
        builder.fSafeRevTables = new RBBITableBuilder(builder, fSafeRevTree);
        builder.fForwardTables.build();
        builder.fReverseTables.build();
        builder.fSafeFwdTables.build();
        builder.fSafeRevTables.build();
        if (builder.fDebugEnv != null
                && builder.fDebugEnv.indexOf("states") >= 0) {
            builder.fForwardTables.printRuleStatusTable();
        }

        //
        //   Package up the compiled data, writing it to an output stream
        //      in the serialization format.  This is the same as the ICU4C runtime format.
        //
        builder.flattenData(os);
    }
}
