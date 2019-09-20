/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 *******************************************************************************
 * Copyright (C) 2003-2016, International Business Machines Corporation and others. All Rights Reserved.
 *******************************************************************************
 */

package android.icu.text;

import java.text.ParsePosition;
import java.util.HashMap;

import android.icu.impl.Assert;
import android.icu.impl.Utility;
import android.icu.lang.UCharacter;

/**
  *  This class is part of the Rule Based Break Iterator rule compiler.
  *  It scans the rules and builds the parse tree.
  *  There is no public API here.
  */
class RBBIRuleScanner {

    private final static int    kStackSize = 100;               // The size of the state stack for
    //   rules parsing.  Corresponds roughly
    //   to the depth of parentheses nesting
    //   that is allowed in the rules.

    static class RBBIRuleChar {
        int             fChar;
        boolean         fEscaped;
    }


    RBBIRuleBuilder           fRB;              // The rule builder that we are part of.

    int                       fScanIndex;        // Index of current character being processed
                                                     //   in the rule input string.
    int                       fNextIndex;        // Index of the next character, which
                                                     //   is the first character not yet scanned.
    boolean                  fQuoteMode;        // Scan is in a 'quoted region'
    int                       fLineNum;          // Line number in input file.
    int                       fCharNum;          // Char position within the line.
    int                       fLastChar;         // Previous char, needed to count CR-LF
                                                     //   as a single line, not two.

    RBBIRuleChar              fC = new RBBIRuleChar();    // Current char for parse state machine
                                                     //   processing.


    short  fStack[] = new short[kStackSize];  // State stack, holds state pushes
    int                       fStackPtr;           //  and pops as specified in the state
                                                       //  transition rules.

    RBBINode  fNodeStack[] = new RBBINode[kStackSize]; // Node stack, holds nodes created
                                                           //  during the parse of a rule
    int                        fNodeStackPtr;


    boolean                    fReverseRule;         // True if the rule currently being scanned
                                                     //  is a reverse direction rule (if it
                                                     //  starts with a '!')

    boolean                    fLookAheadRule;       // True if the rule includes a '/'
                                                     //   somewhere within it.

    boolean                    fNoChainInRule;       // True if the current rule starts with a '^'.


    RBBISymbolTable            fSymbolTable;         // symbol table, holds definitions of
                                                     //   $variable symbols.

    HashMap<String, RBBISetTableEl> fSetTable = new HashMap<String, RBBISetTableEl>(); // UnicocodeSet hash table, holds indexes to
                                                                                       //   the sets created while parsing rules.
                                                                                       //   The key is the string used for creating
                                                                                       //   the set.

    UnicodeSet      fRuleSets[] = new UnicodeSet[10];    // Unicode Sets that are needed during
                                                     //  the scanning of RBBI rules.  The
                                                     //  indicies for these are assigned by the
                                                     //  perl script that builds the state tables.
                                                     //  See rbbirpt.h.

    int                        fRuleNum;         // Counts each rule as it is scanned.

    int                        fOptionStart;     // Input index of start of a !!option
                                                 //   keyword, while being scanned.



   static private String gRuleSet_rule_char_pattern       = "[^[\\p{Z}\\u0020-\\u007f]-[\\p{L}]-[\\p{N}]]";
   static private String gRuleSet_name_char_pattern       = "[_\\p{L}\\p{N}]";
   static private String gRuleSet_digit_char_pattern      = "[0-9]";
   static private String gRuleSet_name_start_char_pattern = "[_\\p{L}]";
   static private String gRuleSet_white_space_pattern     = "[\\p{Pattern_White_Space}]";
   static private String kAny =  "any";




    //----------------------------------------------------------------------------------------
    //
    //  Constructor.
    //
    //----------------------------------------------------------------------------------------
    RBBIRuleScanner(RBBIRuleBuilder rb) {
        fRB = rb;
        fLineNum = 1;

        //
        //  Set up the constant Unicode Sets.
        //     Note: These could be made static and shared among
        //            all instances of RBBIRuleScanners.
        fRuleSets[RBBIRuleParseTable.kRuleSet_rule_char - 128] = new UnicodeSet(gRuleSet_rule_char_pattern);
        fRuleSets[RBBIRuleParseTable.kRuleSet_white_space - 128] = new UnicodeSet(gRuleSet_white_space_pattern);
        fRuleSets[RBBIRuleParseTable.kRuleSet_name_char - 128] = new UnicodeSet(gRuleSet_name_char_pattern);
        fRuleSets[RBBIRuleParseTable.kRuleSet_name_start_char - 128] = new UnicodeSet(gRuleSet_name_start_char_pattern);
        fRuleSets[RBBIRuleParseTable.kRuleSet_digit_char - 128] = new UnicodeSet(gRuleSet_digit_char_pattern);

        fSymbolTable = new RBBISymbolTable(this);
    }

    //----------------------------------------------------------------------------------------
    //
    //  doParseAction Do some action during rule parsing.
    //                       Called by the parse state machine.
    //                       Actions build the parse tree and Unicode Sets,
    //                       and maintain the parse stack for nested expressions.
    //
    //----------------------------------------------------------------------------------------
    boolean doParseActions(int action) {
        RBBINode n = null;

        boolean returnVal = true;

        switch (action) {

        case RBBIRuleParseTable.doExprStart:
            pushNewNode(RBBINode.opStart);
            fRuleNum++;
            break;

        case RBBIRuleParseTable.doNoChain:
            // Scanned a '^' while on the rule start state.
            fNoChainInRule = true;
            break;


        case RBBIRuleParseTable.doExprOrOperator: {
            fixOpStack(RBBINode.precOpCat);
            RBBINode operandNode = fNodeStack[fNodeStackPtr--];
            RBBINode orNode = pushNewNode(RBBINode.opOr);
            orNode.fLeftChild = operandNode;
            operandNode.fParent = orNode;
        }
            break;

        case RBBIRuleParseTable.doExprCatOperator:
        // concatenation operator.
        // For the implicit concatenation of adjacent terms in an expression
        // that are
        //   not separated by any other operator. Action is invoked between the
        //   actions for the two terms.
        {
            fixOpStack(RBBINode.precOpCat);
            RBBINode operandNode = fNodeStack[fNodeStackPtr--];
            RBBINode catNode = pushNewNode(RBBINode.opCat);
            catNode.fLeftChild = operandNode;
            operandNode.fParent = catNode;
        }
            break;

        case RBBIRuleParseTable.doLParen:
            // Open Paren.
            //   The openParen node is a dummy operation type with a low
            // precedence,
            //     which has the affect of ensuring that any real binary op that
            //     follows within the parens binds more tightly to the operands than
            //     stuff outside of the parens.
            pushNewNode(RBBINode.opLParen);
            break;

        case RBBIRuleParseTable.doExprRParen:
            fixOpStack(RBBINode.precLParen);
            break;

        case RBBIRuleParseTable.doNOP:
            break;

        case RBBIRuleParseTable.doStartAssign:
            // We've just scanned "$variable = "
            // The top of the node stack has the $variable ref node.

            // Save the start position of the RHS text in the StartExpression
            // node
            //   that precedes the $variableReference node on the stack.
            //   This will eventually be used when saving the full $variable
            // replacement
            //   text as a string.
            n = fNodeStack[fNodeStackPtr - 1];
            n.fFirstPos = fNextIndex; // move past the '='

            // Push a new start-of-expression node; needed to keep parse of the
            //   RHS expression happy.
            pushNewNode(RBBINode.opStart);
            break;

        case RBBIRuleParseTable.doEndAssign: {
            // We have reached the end of an assignement statement.
            //   Current scan char is the ';' that terminates the assignment.

            // Terminate expression, leaves expression parse tree rooted in TOS
            // node.
            fixOpStack(RBBINode.precStart);

            RBBINode startExprNode = fNodeStack[fNodeStackPtr - 2];
            RBBINode varRefNode = fNodeStack[fNodeStackPtr - 1];
            RBBINode RHSExprNode = fNodeStack[fNodeStackPtr];

            // Save original text of right side of assignment, excluding the
            // terminating ';'
            //  in the root of the node for the right-hand-side expression.
            RHSExprNode.fFirstPos = startExprNode.fFirstPos;
            RHSExprNode.fLastPos = fScanIndex;
            // fRB.fRules.extractBetween(RHSExprNode.fFirstPos,
            // RHSExprNode.fLastPos, RHSExprNode.fText);
            RHSExprNode.fText = fRB.fRules.substring(RHSExprNode.fFirstPos,
                    RHSExprNode.fLastPos);

            // Expression parse tree becomes l. child of the $variable reference
            // node.
            varRefNode.fLeftChild = RHSExprNode;
            RHSExprNode.fParent = varRefNode;

            // Make a symbol table entry for the $variableRef node.
            fSymbolTable.addEntry(varRefNode.fText, varRefNode);

            // Clean up the stack.
            fNodeStackPtr -= 3;
            break;
        }

        case RBBIRuleParseTable.doEndOfRule: {
            fixOpStack(RBBINode.precStart); // Terminate expression, leaves
                                            // expression

            if (fRB.fDebugEnv != null && fRB.fDebugEnv.indexOf("rtree") >= 0) {
                printNodeStack("end of rule");
            }
            Assert.assrt(fNodeStackPtr == 1);
            RBBINode thisRule = fNodeStack[fNodeStackPtr];

            // If this rule includes a look-ahead '/', add a endMark node to the
            //   expression tree.
            if (fLookAheadRule) {
                RBBINode endNode = pushNewNode(RBBINode.endMark);
                RBBINode catNode = pushNewNode(RBBINode.opCat);
                fNodeStackPtr -= 2;
                catNode.fLeftChild = thisRule;
                catNode.fRightChild = endNode;
                fNodeStack[fNodeStackPtr] = catNode;
                endNode.fVal = fRuleNum;
                endNode.fLookAheadEnd = true;
                thisRule = catNode;

                // TODO: Disable chaining out of look-ahead (hard break) rules.
                //   The break on rule match is forced, so there is no point in building up
                //   the state table to chain into another rule for a longer match.
            }

            // Mark this node as being the root of a rule.
            thisRule.fRuleRoot = true;

            // Flag if chaining into this rule is wanted.
            //
            if (fRB.fChainRules &&          // If rule chaining is enabled globally via !!chain
                    !fNoChainInRule) {      //     and no '^' chain-in inhibit was on this rule
                thisRule.fChainIn = true;
            }


            // All rule expressions are ORed together.
            // The ';' that terminates an expression really just functions as a
            // '|' with
            //   a low operator prededence.
            //
            // Each of the four sets of rules are collected separately.
            //  (forward, reverse, safe_forward, safe_reverse)
            //  OR this rule into the appropriate group of them.
            //

            int destRules = (fReverseRule ? RBBIRuleBuilder.fReverseTree : fRB.fDefaultTree);

            if (fRB.fTreeRoots[destRules] != null) {
                // This is not the first rule encountered.
                // OR previous stuff (from *destRules)
                // with the current rule expression (on the Node Stack)
                //  with the resulting OR expression going to *destRules
                //
                thisRule = fNodeStack[fNodeStackPtr];
                RBBINode prevRules = fRB.fTreeRoots[destRules];
                RBBINode orNode = pushNewNode(RBBINode.opOr);
                orNode.fLeftChild = prevRules;
                prevRules.fParent = orNode;
                orNode.fRightChild = thisRule;
                thisRule.fParent = orNode;
                fRB.fTreeRoots[destRules] = orNode;
            } else {
                // This is the first rule encountered (for this direction).
                // Just move its parse tree from the stack to *destRules.
                fRB.fTreeRoots[destRules] = fNodeStack[fNodeStackPtr];
            }
            fReverseRule = false; // in preparation for the next rule.
            fLookAheadRule = false;
            fNoChainInRule = false;
            fNodeStackPtr = 0;
        }
            break;

        case RBBIRuleParseTable.doRuleError:
            error(RBBIRuleBuilder.U_BRK_RULE_SYNTAX);
            returnVal = false;
            break;

        case RBBIRuleParseTable.doVariableNameExpectedErr:
            error(RBBIRuleBuilder.U_BRK_RULE_SYNTAX);
            break;

        //
        //  Unary operands + ? *
        //    These all appear after the operand to which they apply.
        //    When we hit one, the operand (may be a whole sub expression)
        //    will be on the top of the stack.
        //    Unary Operator becomes TOS, with the old TOS as its one child.
        case RBBIRuleParseTable.doUnaryOpPlus: {
            RBBINode operandNode = fNodeStack[fNodeStackPtr--];
            RBBINode plusNode = pushNewNode(RBBINode.opPlus);
            plusNode.fLeftChild = operandNode;
            operandNode.fParent = plusNode;
        }
            break;

        case RBBIRuleParseTable.doUnaryOpQuestion: {
            RBBINode operandNode = fNodeStack[fNodeStackPtr--];
            RBBINode qNode = pushNewNode(RBBINode.opQuestion);
            qNode.fLeftChild = operandNode;
            operandNode.fParent = qNode;
        }
            break;

        case RBBIRuleParseTable.doUnaryOpStar: {
            RBBINode operandNode = fNodeStack[fNodeStackPtr--];
            RBBINode starNode = pushNewNode(RBBINode.opStar);
            starNode.fLeftChild = operandNode;
            operandNode.fParent = starNode;
        }
            break;

        case RBBIRuleParseTable.doRuleChar:
        // A "Rule Character" is any single character that is a literal part
        // of the regular expression. Like a, b and c in the expression "(abc*)
        // | [:L:]"
        // These are pretty uncommon in break rules; the terms are more commonly
        //  sets. To keep things uniform, treat these characters like as
        // sets that just happen to contain only one character.
        {
            n = pushNewNode(RBBINode.setRef);
            String s = String.valueOf((char)fC.fChar);
            findSetFor(s, n, null);
            n.fFirstPos = fScanIndex;
            n.fLastPos = fNextIndex;
            n.fText = fRB.fRules.substring(n.fFirstPos, n.fLastPos);
            break;
        }

        case RBBIRuleParseTable.doDotAny:
        // scanned a ".", meaning match any single character.
        {
            n = pushNewNode(RBBINode.setRef);
            findSetFor(kAny, n, null);
            n.fFirstPos = fScanIndex;
            n.fLastPos = fNextIndex;
            n.fText = fRB.fRules.substring(n.fFirstPos, n.fLastPos);
            break;
        }

        case RBBIRuleParseTable.doSlash:
            // Scanned a '/', which identifies a look-ahead break position in a
            // rule.
            n = pushNewNode(RBBINode.lookAhead);
            n.fVal = fRuleNum;
            n.fFirstPos = fScanIndex;
            n.fLastPos = fNextIndex;
            n.fText = fRB.fRules.substring(n.fFirstPos, n.fLastPos);
            fLookAheadRule = true;
            break;

        case RBBIRuleParseTable.doStartTagValue:
            // Scanned a '{', the opening delimiter for a tag value within a
            // rule.
            n = pushNewNode(RBBINode.tag);
            n.fVal = 0;
            n.fFirstPos = fScanIndex;
            n.fLastPos = fNextIndex;
            break;

        case RBBIRuleParseTable.doTagDigit:
        // Just scanned a decimal digit that's part of a tag value
        {
            n = fNodeStack[fNodeStackPtr];
            int v = UCharacter.digit((char) fC.fChar, 10);
            n.fVal = n.fVal * 10 + v;
            break;
        }

        case RBBIRuleParseTable.doTagValue:
            n = fNodeStack[fNodeStackPtr];
            n.fLastPos = fNextIndex;
            n.fText = fRB.fRules.substring(n.fFirstPos, n.fLastPos);
            break;

        case RBBIRuleParseTable.doTagExpectedError:
            error(RBBIRuleBuilder.U_BRK_MALFORMED_RULE_TAG);
            returnVal = false;
            break;

        case RBBIRuleParseTable.doOptionStart:
            // Scanning a !!option. At the start of string.
            fOptionStart = fScanIndex;
            break;

        case RBBIRuleParseTable.doOptionEnd: {
            String opt = fRB.fRules.substring(fOptionStart, fScanIndex);
            if (opt.equals("chain")) {
                fRB.fChainRules = true;
            } else if (opt.equals("LBCMNoChain")) {
                fRB.fLBCMNoChain = true;
            } else if (opt.equals("forward")) {
                fRB.fDefaultTree = RBBIRuleBuilder.fForwardTree;
            } else if (opt.equals("reverse")) {
                fRB.fDefaultTree = RBBIRuleBuilder.fReverseTree;
            } else if (opt.equals("safe_forward")) {
                fRB.fDefaultTree = RBBIRuleBuilder.fSafeFwdTree;
            } else if (opt.equals("safe_reverse")) {
                fRB.fDefaultTree = RBBIRuleBuilder.fSafeRevTree;
            } else if (opt.equals("lookAheadHardBreak")) {
                fRB.fLookAheadHardBreak = true;
            } else {
                error(RBBIRuleBuilder.U_BRK_UNRECOGNIZED_OPTION);
            }
            break;
        }

        case RBBIRuleParseTable.doReverseDir:
            fReverseRule = true;
            break;

        case RBBIRuleParseTable.doStartVariableName:
            n = pushNewNode(RBBINode.varRef);
            n.fFirstPos = fScanIndex;
            break;

        case RBBIRuleParseTable.doEndVariableName:
            n = fNodeStack[fNodeStackPtr];
            if (n == null || n.fType != RBBINode.varRef) {
                error(RBBIRuleBuilder.U_BRK_INTERNAL_ERROR);
                break;
            }
            n.fLastPos = fScanIndex;
            n.fText = fRB.fRules.substring(n.fFirstPos + 1, n.fLastPos);
            // Look the newly scanned name up in the symbol table
            //   If there's an entry, set the l. child of the var ref to the
            // replacement expression.
            //   (We also pass through here when scanning assignments, but no harm
            // is done, other
            //    than a slight wasted effort that seems hard to avoid. Lookup will
            // be null)
            n.fLeftChild = fSymbolTable.lookupNode(n.fText);
            break;

        case RBBIRuleParseTable.doCheckVarDef:
            n = fNodeStack[fNodeStackPtr];
            if (n.fLeftChild == null) {
                error(RBBIRuleBuilder.U_BRK_UNDEFINED_VARIABLE);
                returnVal = false;
            }
            break;

        case RBBIRuleParseTable.doExprFinished:
            break;

        case RBBIRuleParseTable.doRuleErrorAssignExpr:
            error(RBBIRuleBuilder.U_BRK_ASSIGN_ERROR);
            returnVal = false;
            break;

        case RBBIRuleParseTable.doExit:
            returnVal = false;
            break;

        case RBBIRuleParseTable.doScanUnicodeSet:
            scanSet();
            break;

        default:
            error(RBBIRuleBuilder.U_BRK_INTERNAL_ERROR);
            returnVal = false;
            break;
        }
        return returnVal;
    }

    //----------------------------------------------------------------------------------------
    //
    //  Error Throw and IllegalArgumentException in response to a rule parse
    // error.
    //
    //----------------------------------------------------------------------------------------
    void error(int e) {
        String s = "Error " + e + " at line " + fLineNum + " column "
                + fCharNum;
        IllegalArgumentException ex = new IllegalArgumentException(s);
        throw ex;

    }

    //----------------------------------------------------------------------------------------
    //
    //  fixOpStack The parse stack holds partially assembled chunks of the parse
    // tree.
    //               An entry on the stack may be as small as a single setRef node,
    //               or as large as the parse tree
    //               for an entire expression (this will be the one item left on the stack
    //               when the parsing of an RBBI rule completes.
    //
    //               This function is called when a binary operator is encountered.
    //               It looks back up the stack for operators that are not yet associated
    //               with a right operand, and if the precedence of the stacked operator >=
    //               the precedence of the current operator, binds the operand left,
    //               to the previously encountered operator.
    //
    //----------------------------------------------------------------------------------------
    void fixOpStack(int p) {
        RBBINode n;
        // printNodeStack("entering fixOpStack()");
        for (;;) {
            n = fNodeStack[fNodeStackPtr - 1]; // an operator node
            if (n.fPrecedence == 0) {
                System.out.print("RBBIRuleScanner.fixOpStack, bad operator node");
                error(RBBIRuleBuilder.U_BRK_INTERNAL_ERROR);
                return;
            }

            if (n.fPrecedence < p || n.fPrecedence <= RBBINode.precLParen) {
                // The most recent operand goes with the current operator,
                //   not with the previously stacked one.
                break;
            }
            // Stack operator is a binary op ( '|' or concatenation)
            //   TOS operand becomes right child of this operator.
            //   Resulting subexpression becomes the TOS operand.
            n.fRightChild = fNodeStack[fNodeStackPtr];
            fNodeStack[fNodeStackPtr].fParent = n;
            fNodeStackPtr--;
            // printNodeStack("looping in fixOpStack() ");
        }

        if (p <= RBBINode.precLParen) {
            // Scan is at a right paren or end of expression.
            //  The scanned item must match the stack, or else there was an
            // error.
            //  Discard the left paren (or start expr) node from the stack,
            //  leaving the completed (sub)expression as TOS.
            if (n.fPrecedence != p) {
                // Right paren encountered matched start of expression node, or
                // end of expression matched with a left paren node.
                error(RBBIRuleBuilder.U_BRK_MISMATCHED_PAREN);
            }
            fNodeStack[fNodeStackPtr - 1] = fNodeStack[fNodeStackPtr];
            fNodeStackPtr--;
            // Delete the now-discarded LParen or Start node.
            // delete n;
        }
        // printNodeStack("leaving fixOpStack()");
    }

    //----------------------------------------------------------------------------
    //
    //       RBBISetTableEl is an entry in the hash table of UnicodeSets that have
    //                        been encountered. The val Node will be of nodetype uset
    //                        and contain pointers to the actual UnicodeSets.
    //                        The Key is the source string for initializing the set.
    //
    //                        The hash table is used to avoid creating duplicate
    //                        unnamed (not $var references) UnicodeSets.
    //
    //----------------------------------------------------------------------------
    static class RBBISetTableEl {
        String key;

        RBBINode val;
    }


    //----------------------------------------------------------------------------------------
    //
    //   findSetFor given a String,
    //                  - find the corresponding Unicode Set (uset node)
    //                         (create one if necessary)
    //                  - Set fLeftChild of the caller's node (should be a setRef node)
    //                         to the uset node
    //                 Maintain a hash table of uset nodes, so the same one is always used
    //                    for the same string.
    //                 If a "to adopt" set is provided and we haven't seen this key before,
    //                    add the provided set to the hash table.
    //                 If the string is one (32 bit) char in length, the set contains
    //                    just one element which is the char in question.
    //                 If the string is "any", return a set containing all chars.
    //
    //----------------------------------------------------------------------------------------
    void findSetFor(String s, RBBINode node, UnicodeSet setToAdopt) {

        RBBISetTableEl el;

        // First check whether we've already cached a set for this string.
        // If so, just use the cached set in the new node.
        //   delete any set provided by the caller, since we own it.
        el = fSetTable.get(s);
        if (el != null) {
            node.fLeftChild = el.val;
            Assert.assrt(node.fLeftChild.fType == RBBINode.uset);
            return;
        }

        // Haven't seen this set before.
        // If the caller didn't provide us with a prebuilt set,
        //   create a new UnicodeSet now.
        if (setToAdopt == null) {
            if (s.equals(kAny)) {
                setToAdopt = new UnicodeSet(0x000000, 0x10ffff);
            } else {
                int c;
                c = UTF16.charAt(s, 0);
                setToAdopt = new UnicodeSet(c, c);
            }
        }

        //
        // Make a new uset node to refer to this UnicodeSet
        // This new uset node becomes the child of the caller's setReference
        // node.
        //
        RBBINode usetNode = new RBBINode(RBBINode.uset);
        usetNode.fInputSet = setToAdopt;
        usetNode.fParent = node;
        node.fLeftChild = usetNode;
        usetNode.fText = s;

        //
        // Add the new uset node to the list of all uset nodes.
        //
        fRB.fUSetNodes.add(usetNode);

        //
        // Add the new set to the set hash table.
        //
        el = new RBBISetTableEl();
        el.key = s;
        el.val = usetNode;
        fSetTable.put(el.key, el);

        return;
    }

    //
    //  Assorted Unicode character constants.
    //     Numeric because there is no portable way to enter them as literals.
    //     (Think EBCDIC).
    //
    static final int chNEL = 0x85; //    NEL newline variant

    static final int chLS = 0x2028; //    Unicode Line Separator

    //----------------------------------------------------------------------------------------
    //
    //  stripRules    Return a rules string without unnecessary
    //                characters.
    //
    //----------------------------------------------------------------------------------------
    static String stripRules(String rules) {
        StringBuilder strippedRules = new StringBuilder();
        int rulesLength = rules.length();
        for (int idx = 0; idx < rulesLength;) {
            char ch = rules.charAt(idx++);
            if (ch == '#') {
                while (idx < rulesLength
                        && ch != '\r' && ch != '\n' && ch != chNEL) {
                    ch = rules.charAt(idx++);
                }
            }
            if (!UCharacter.isISOControl(ch)) {
                strippedRules.append(ch);
            }
        }
        return strippedRules.toString();
    }

    //----------------------------------------------------------------------------------------
    //
    //  nextCharLL    Low Level Next Char from rule input source.
    //                Get a char from the input character iterator,
    //                keep track of input position for error reporting.
    //
    //----------------------------------------------------------------------------------------
    int nextCharLL() {
        int ch;

        if (fNextIndex >= fRB.fRules.length()) {
            return -1;
        }
        ch = UTF16.charAt(fRB.fRules, fNextIndex);
        fNextIndex = UTF16.moveCodePointOffset(fRB.fRules, fNextIndex, 1);

        if (ch == '\r' ||
            ch == chNEL ||
            ch == chLS ||
            ch == '\n' && fLastChar != '\r') {
            // Character is starting a new line.  Bump up the line number, and
            //  reset the column to 0.
            fLineNum++;
            fCharNum = 0;
            if (fQuoteMode) {
                error(RBBIRuleBuilder.U_BRK_NEW_LINE_IN_QUOTED_STRING);
                fQuoteMode = false;
            }
        } else {
            // Character is not starting a new line.  Except in the case of a
            //   LF following a CR, increment the column position.
            if (ch != '\n') {
                fCharNum++;
            }
        }
        fLastChar = ch;
        return ch;
    }

    //---------------------------------------------------------------------------------
    //
    //   nextChar     for rules scanning.  At this level, we handle stripping
    //                out comments and processing backslash character escapes.
    //                The rest of the rules grammar is handled at the next level up.
    //
    //---------------------------------------------------------------------------------
    void nextChar(RBBIRuleChar c) {

        // Unicode Character constants needed for the processing done by nextChar(),
        //   in hex because literals wont work on EBCDIC machines.

        fScanIndex = fNextIndex;
        c.fChar = nextCharLL();
        c.fEscaped = false;

        //
        //  check for '' sequence.
        //  These are recognized in all contexts, whether in quoted text or not.
        //
        if (c.fChar == '\'') {
            if (UTF16.charAt(fRB.fRules, fNextIndex) == '\'') {
                c.fChar = nextCharLL(); // get nextChar officially so character counts
                c.fEscaped = true; //   stay correct.
            } else {
                // Single quote, by itself.
                //   Toggle quoting mode.
                //   Return either '('  or ')', because quotes cause a grouping of the quoted text.
                fQuoteMode = !fQuoteMode;
                if (fQuoteMode == true) {
                    c.fChar = '(';
                } else {
                    c.fChar = ')';
                }
                c.fEscaped = false; // The paren that we return is not escaped.
                return;
            }
        }

        if (fQuoteMode) {
            c.fEscaped = true;
        } else {
            // We are not in a 'quoted region' of the source.
            //
            if (c.fChar == '#') {
                // Start of a comment.  Consume the rest of it.
                //  The new-line char that terminates the comment is always returned.
                //  It will be treated as white-space, and serves to break up anything
                //    that might otherwise incorrectly clump together with a comment in
                //    the middle (a variable name, for example.)
                for (;;) {
                    c.fChar = nextCharLL();
                    if (c.fChar == -1 || // EOF
                        c.fChar == '\r' ||
                        c.fChar == '\n' ||
                        c.fChar == chNEL ||
                        c.fChar == chLS)
                    {
                        break;
                    }
                }
            }
            if (c.fChar == -1) {
                return;
            }

            //
            //  check for backslash escaped characters.
            //  Use String.unescapeAt() to handle them.
            //
            if (c.fChar == '\\') {
                c.fEscaped = true;
                int[] unescapeIndex = new int[1];
                unescapeIndex[0] = fNextIndex;
                c.fChar = Utility.unescapeAt(fRB.fRules, unescapeIndex);
                if (unescapeIndex[0] == fNextIndex) {
                    error(RBBIRuleBuilder.U_BRK_HEX_DIGITS_EXPECTED);
                }

                fCharNum += unescapeIndex[0] - fNextIndex;
                fNextIndex = unescapeIndex[0];
            }
        }
        // putc(c.fChar, stdout);
    }

    //---------------------------------------------------------------------------------
    //
    //  Parse RBBI rules.   The state machine for rules parsing is here.
    //                      The state tables are hand-written in the file rbbirpt.txt,
    //                      and converted to the form used here by a perl
    //                      script rbbicst.pl
    //
    //---------------------------------------------------------------------------------
    void parse() {
        int state;
        RBBIRuleParseTable.RBBIRuleTableElement tableEl;

        state = 1;
        nextChar(fC);
        //
        // Main loop for the rule parsing state machine.
        //   Runs once per state transition.
        //   Each time through optionally performs, depending on the state table,
        //      - an advance to the the next input char
        //      - an action to be performed.
        //      - pushing or popping a state to/from the local state return stack.
        //
        for (;;) {
            // Quit if state == 0.  This is the normal way to exit the state machine.
            //
            if (state == 0) {
                break;
            }

            // Find the state table element that matches the input char from the rule, or the
            //    class of the input character.  Start with the first table row for this
            //    state, then linearly scan forward until we find a row that matches the
            //    character.  The last row for each state always matches all characters, so
            //    the search will stop there, if not before.
            //
            tableEl = RBBIRuleParseTable.gRuleParseStateTable[state];
            if (fRB.fDebugEnv != null && fRB.fDebugEnv.indexOf("scan") >= 0) {
                System.out.println("char, line, col = (\'" + (char) fC.fChar
                        + "\', " + fLineNum + ", " + fCharNum + "    state = "
                        + tableEl.fStateName);
            }

            for (int tableRow = state;; tableRow++) { // loop over the state table rows associated with this state.
                tableEl = RBBIRuleParseTable.gRuleParseStateTable[tableRow];
                if (fRB.fDebugEnv != null && fRB.fDebugEnv.indexOf("scan") >= 0) {
                    System.out.print(".");
                }
                if (tableEl.fCharClass < 127 && fC.fEscaped == false
                        && tableEl.fCharClass == fC.fChar) {
                    // Table row specified an individual character, not a set, and
                    //   the input character is not escaped, and
                    //   the input character matched it.
                    break;
                }
                if (tableEl.fCharClass == 255) {
                    // Table row specified default, match anything character class.
                    break;
                }
                if (tableEl.fCharClass == 254 && fC.fEscaped) {
                    // Table row specified "escaped" and the char was escaped.
                    break;
                }
                if (tableEl.fCharClass == 253 && fC.fEscaped
                        && (fC.fChar == 0x50 || fC.fChar == 0x70)) {
                    // Table row specified "escaped P" and the char is either 'p' or 'P'.
                    break;
                }
                if (tableEl.fCharClass == 252 && fC.fChar == -1) {
                    // Table row specified eof and we hit eof on the input.
                    break;
                }

                if (tableEl.fCharClass >= 128 && tableEl.fCharClass < 240 && // Table specs a char class &&
                        fC.fEscaped == false && //   char is not escaped &&
                        fC.fChar != -1) { //   char is not EOF
                    UnicodeSet uniset = fRuleSets[tableEl.fCharClass - 128];
                    if (uniset.contains(fC.fChar)) {
                        // Table row specified a character class, or set of characters,
                        //   and the current char matches it.
                        break;
                    }
                }
            }

            if (fRB.fDebugEnv != null && fRB.fDebugEnv.indexOf("scan") >= 0) {
                System.out.println("");
            }
            //
            // We've found the row of the state table that matches the current input
            //   character from the rules string.
            // Perform any action specified  by this row in the state table.
            if (doParseActions(tableEl.fAction) == false) {
                // Break out of the state machine loop if the
                //   the action signalled some kind of error, or
                //   the action was to exit, occurs on normal end-of-rules-input.
                break;
            }

            if (tableEl.fPushState != 0) {
                fStackPtr++;
                if (fStackPtr >= kStackSize) {
                    System.out.println("RBBIRuleScanner.parse() - state stack overflow.");
                    error(RBBIRuleBuilder.U_BRK_INTERNAL_ERROR);
                }
                fStack[fStackPtr] = tableEl.fPushState;
            }

            if (tableEl.fNextChar) {
                nextChar(fC);
            }

            // Get the next state from the table entry, or from the
            //   state stack if the next state was specified as "pop".
            if (tableEl.fNextState != 255) {
                state = tableEl.fNextState;
            } else {
                state = fStack[fStackPtr];
                fStackPtr--;
                if (fStackPtr < 0) {
                    System.out.println("RBBIRuleScanner.parse() - state stack underflow.");
                    error(RBBIRuleBuilder.U_BRK_INTERNAL_ERROR);
                }
            }

        }
        
        // If there are no forward rules throw an error.
        //
        if (fRB.fTreeRoots[RBBIRuleBuilder.fForwardTree] == null) {
            error(RBBIRuleBuilder.U_BRK_RULE_SYNTAX);
        }

        //
        // If there were NO user specified reverse rules, set up the equivalent of ".*;"
        //
        if (fRB.fTreeRoots[RBBIRuleBuilder.fReverseTree] == null) {
            fRB.fTreeRoots[RBBIRuleBuilder.fReverseTree] = pushNewNode(RBBINode.opStar);
            RBBINode operand = pushNewNode(RBBINode.setRef);
            findSetFor(kAny, operand, null);
            fRB.fTreeRoots[RBBIRuleBuilder.fReverseTree].fLeftChild = operand;
            operand.fParent = fRB.fTreeRoots[RBBIRuleBuilder.fReverseTree];
            fNodeStackPtr -= 2;
        }

        //
        // Parsing of the input RBBI rules is complete.
        // We now have a parse tree for the rule expressions
        // and a list of all UnicodeSets that are referenced.
        //
        if (fRB.fDebugEnv != null && fRB.fDebugEnv.indexOf("symbols") >= 0) {
            fSymbolTable.rbbiSymtablePrint();
        }
        if (fRB.fDebugEnv != null && fRB.fDebugEnv.indexOf("ptree") >= 0) {
            System.out.println("Completed Forward Rules Parse Tree...");
            fRB.fTreeRoots[RBBIRuleBuilder.fForwardTree].printTree(true);
            System.out.println("\nCompleted Reverse Rules Parse Tree...");
            fRB.fTreeRoots[RBBIRuleBuilder.fReverseTree].printTree(true);
            System.out.println("\nCompleted Safe Point Forward Rules Parse Tree...");
            if (fRB.fTreeRoots[RBBIRuleBuilder.fSafeFwdTree] == null) {
                System.out.println("  -- null -- ");
            } else {
                fRB.fTreeRoots[RBBIRuleBuilder.fSafeFwdTree].printTree(true);
            }
            System.out.println("\nCompleted Safe Point Reverse Rules Parse Tree...");
            if (fRB.fTreeRoots[RBBIRuleBuilder.fSafeRevTree] == null) {
                System.out.println("  -- null -- ");
            } else {
                fRB.fTreeRoots[RBBIRuleBuilder.fSafeRevTree].printTree(true);
            }
        }
    }

    //---------------------------------------------------------------------------------
    //
    //  printNodeStack     for debugging...
    //
    //---------------------------------------------------------------------------------
    ///CLOVER:OFF
    void printNodeStack(String title) {
        int i;
        System.out.println(title + ".  Dumping node stack...\n");
        for (i = fNodeStackPtr; i > 0; i--) {
            fNodeStack[i].printTree(true);
        }
    }
    ///CLOVER:ON

    //---------------------------------------------------------------------------------
    //
    //  pushNewNode   create a new RBBINode of the specified type and push it
    //                onto the stack of nodes.
    //
    //---------------------------------------------------------------------------------
    RBBINode pushNewNode(int nodeType) {
        fNodeStackPtr++;
        if (fNodeStackPtr >= kStackSize) {
            System.out.println("RBBIRuleScanner.pushNewNode - stack overflow.");
            error(RBBIRuleBuilder.U_BRK_INTERNAL_ERROR);
        }
        fNodeStack[fNodeStackPtr] = new RBBINode(nodeType);
        return fNodeStack[fNodeStackPtr];
    }

    //---------------------------------------------------------------------------------
    //
    //  scanSet    Construct a UnicodeSet from the text at the current scan
    //             position.  Advance the scan position to the first character
    //             after the set.
    //
    //             A new RBBI setref node referring to the set is pushed onto the node
    //             stack.
    //
    //             The scan position is normally under the control of the state machine
    //             that controls rule parsing.  UnicodeSets, however, are parsed by
    //             the UnicodeSet constructor, not by the RBBI rule parser.
    //
    //---------------------------------------------------------------------------------
    void scanSet() {
        UnicodeSet uset = null;
        int startPos;
        ParsePosition pos = new ParsePosition(fScanIndex);
        int i;

        startPos = fScanIndex;
        try {
            uset = new UnicodeSet(fRB.fRules, pos, fSymbolTable, UnicodeSet.IGNORE_SPACE);
        } catch (Exception e) { // TODO:  catch fewer exception types.
            // Repackage UnicodeSet errors as RBBI rule builder errors, with location info.
            error(RBBIRuleBuilder.U_BRK_MALFORMED_SET);
        }

        // Verify that the set contains at least one code point.
        //
        if (uset.isEmpty()) {
            // This set is empty.
            //  Make it an error, because it almost certainly is not what the user wanted.
            //  Also, avoids having to think about corner cases in the tree manipulation code
            //   that occurs later on.
            //  TODO:  this shouldn't be an error; it does happen.
            error(RBBIRuleBuilder.U_BRK_RULE_EMPTY_SET);
        }

        // Advance the RBBI parse postion over the UnicodeSet pattern.
        //   Don't just set fScanIndex because the line/char positions maintained
        //   for error reporting would be thrown off.
        i = pos.getIndex();
        for (;;) {
            if (fNextIndex >= i) {
                break;
            }
            nextCharLL();
        }

        RBBINode n;

        n = pushNewNode(RBBINode.setRef);
        n.fFirstPos = startPos;
        n.fLastPos = fNextIndex;
        n.fText = fRB.fRules.substring(n.fFirstPos, n.fLastPos);
        //  findSetFor() serves several purposes here:
        //     - Adopts storage for the UnicodeSet, will be responsible for deleting.
        //     - Mantains collection of all sets in use, needed later for establishing
        //          character categories for run time engine.
        //     - Eliminates mulitiple instances of the same set.
        //     - Creates a new uset node if necessary (if this isn't a duplicate.)
        findSetFor(n.fText, n, uset);
    }

}

