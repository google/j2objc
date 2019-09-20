/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2011-2012, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011aug12
*   created by: Markus W. Scherer
*/

package android.icu.dev.test.format;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import android.icu.text.MessagePattern;
import android.icu.text.MessagePatternUtil;
import android.icu.text.MessagePatternUtil.ArgNode;
import android.icu.text.MessagePatternUtil.ComplexArgStyleNode;
import android.icu.text.MessagePatternUtil.MessageContentsNode;
import android.icu.text.MessagePatternUtil.MessageNode;
import android.icu.text.MessagePatternUtil.TextNode;
import android.icu.text.MessagePatternUtil.VariantNode;

/**
 * Test MessagePatternUtil (MessagePattern-as-tree-of-nodes API)
 * by building parallel trees of nodes and verifying that they match.
 */
public final class MessagePatternUtilTest extends android.icu.dev.test.TestFmwk {
    // The following nested "Expect..." classes are used to build
    // a tree structure parallel to what the MessagePatternUtil class builds.
    // These nested test classes are not static so that they have access to TestFmwk methods.

    private class ExpectMessageNode {
        private ExpectMessageNode expectTextThatContains(String s) {
            contents.add(new ExpectTextNode(s));
            return this;
        }
        private ExpectMessageNode expectReplaceNumber() {
            contents.add(new ExpectMessageContentsNode());
            return this;
        }
        private ExpectMessageNode expectNoneArg(Object name) {
            contents.add(new ExpectArgNode(name));
            return this;
        }
        private ExpectMessageNode expectSimpleArg(Object name, String type) {
            contents.add(new ExpectArgNode(name, type));
            return this;
        }
        private ExpectMessageNode expectSimpleArg(Object name, String type, String style) {
            contents.add(new ExpectArgNode(name, type, style));
            return this;
        }
        private ExpectComplexArgNode expectChoiceArg(Object name) {
            return expectComplexArg(name, MessagePattern.ArgType.CHOICE);
        }
        private ExpectComplexArgNode expectPluralArg(Object name) {
            return expectComplexArg(name, MessagePattern.ArgType.PLURAL);
        }
        private ExpectComplexArgNode expectSelectArg(Object name) {
            return expectComplexArg(name, MessagePattern.ArgType.SELECT);
        }
        private ExpectComplexArgNode expectSelectOrdinalArg(Object name) {
            return expectComplexArg(name, MessagePattern.ArgType.SELECTORDINAL);
        }
        private ExpectComplexArgNode expectComplexArg(Object name, MessagePattern.ArgType argType) {
            ExpectComplexArgNode complexArg = new ExpectComplexArgNode(this, name, argType);
            contents.add(complexArg);
            return complexArg;
        }
        private ExpectComplexArgNode finishVariant() {
            return parent;
        }
        private void checkMatches(MessageNode msg) {
            // matches() prints all errors.
            matches(msg);
        }
        private boolean matches(MessageNode msg) {
            List<MessageContentsNode> msgContents = msg.getContents();
            boolean ok = assertEquals("different numbers of MessageContentsNode",
                                      contents.size(), msgContents.size());
            if (ok) {
                Iterator<MessageContentsNode> msgIter = msgContents.iterator();
                for (ExpectMessageContentsNode ec : contents) {
                    ok &= ec.matches(msgIter.next());
                }
            }
            if (!ok) {
                errln("error in message: " + msg.toString());
            }
            return ok;
        }
        private ExpectComplexArgNode parent;  // for finishVariant()
        private List<ExpectMessageContentsNode> contents =
            new ArrayList<ExpectMessageContentsNode>();
    }

    /**
     * Base class for message contents nodes.
     * Used directly for REPLACE_NUMBER nodes, subclassed for others.
     */
    private class ExpectMessageContentsNode {
        protected boolean matches(MessageContentsNode c) {
            return assertEquals("not a REPLACE_NUMBER node",
                                MessageContentsNode.Type.REPLACE_NUMBER, c.getType());
        }
    }

    private class ExpectTextNode extends ExpectMessageContentsNode {
        private ExpectTextNode(String subString) {
            this.subString = subString;
        }
        @Override
        protected boolean matches(MessageContentsNode c) {
            return
                assertEquals("not a TextNode",
                             MessageContentsNode.Type.TEXT, c.getType()) &&
                assertTrue("TextNode does not contain \"" + subString + "\"",
                           ((TextNode)c).getText().contains(subString));
        }
        private String subString;
    }

    private class ExpectArgNode extends ExpectMessageContentsNode {
        private ExpectArgNode(Object name) {
            this(name, null, null);
        }
        private ExpectArgNode(Object name, String type) {
            this(name, type, null);
        }
        private ExpectArgNode(Object name, String type, String style) {
            if (name instanceof String) {
                this.name = (String)name;
                this.number = -1;
            } else {
                this.number = (Integer)name;
                this.name = Integer.toString(this.number);
            }
            if (type == null) {
                argType = MessagePattern.ArgType.NONE;
            } else {
                argType = MessagePattern.ArgType.SIMPLE;
            }
            this.type = type;
            this.style = style;
        }
        @Override
        protected boolean matches(MessageContentsNode c) {
            boolean ok =
                assertEquals("not an ArgNode",
                             MessageContentsNode.Type.ARG, c.getType());
            if (!ok) {
                return ok;
            }
            ArgNode arg = (ArgNode)c;
            ok &= assertEquals("unexpected ArgNode argType",
                               argType, arg.getArgType());
            ok &= assertEquals("unexpected ArgNode arg name",
                               name, arg.getName());
            ok &= assertEquals("unexpected ArgNode arg number",
                               number, arg.getNumber());
            ok &= assertEquals("unexpected ArgNode arg type name",
                               type, arg.getTypeName());
            ok &= assertEquals("unexpected ArgNode arg style",
                               style, arg.getSimpleStyle());
            if (argType == MessagePattern.ArgType.NONE || argType == MessagePattern.ArgType.SIMPLE) {
                ok &= assertNull("unexpected non-null complex style", arg.getComplexStyle());
            }
            return ok;
        }
        private String name;
        private int number;
        protected MessagePattern.ArgType argType;
        private String type;
        private String style;
    }

    private class ExpectComplexArgNode extends ExpectArgNode {
        private ExpectComplexArgNode(ExpectMessageNode parent,
                                     Object name, MessagePattern.ArgType argType) {
            super(name, argType.toString().toLowerCase(Locale.ENGLISH));
            this.argType = argType;
            this.parent = parent;
        }
        private ExpectComplexArgNode expectOffset(double offset) {
            this.offset = offset;
            explicitOffset = true;
            return this;
        }
        private ExpectMessageNode expectVariant(String selector) {
            ExpectVariantNode variant = new ExpectVariantNode(this, selector);
            variants.add(variant);
            return variant.msg;
        }
        private ExpectMessageNode expectVariant(String selector, double value) {
            ExpectVariantNode variant = new ExpectVariantNode(this, selector, value);
            variants.add(variant);
            return variant.msg;
        }
        private ExpectMessageNode finishComplexArg() {
            return parent;
        }
        @Override
        protected boolean matches(MessageContentsNode c) {
            boolean ok = super.matches(c);
            if (!ok) {
                return ok;
            }
            ArgNode arg = (ArgNode)c;
            ComplexArgStyleNode complexStyle = arg.getComplexStyle();
            ok &= assertNotNull("unexpected null complex style", complexStyle);
            if (!ok) {
                return ok;
            }
            ok &= assertEquals("unexpected complex-style argType",
                               argType, complexStyle.getArgType());
            ok &= assertEquals("unexpected complex-style hasExplicitOffset()",
                               explicitOffset, complexStyle.hasExplicitOffset());
            ok &= assertEquals("unexpected complex-style offset",
                               offset, complexStyle.getOffset());
            List<VariantNode> complexVariants = complexStyle.getVariants();
            ok &= assertEquals("different number of variants",
                               variants.size(), complexVariants.size());
            if (!ok) {
                return ok;
            }
            Iterator<VariantNode> complexIter = complexVariants.iterator();
            for (ExpectVariantNode variant : variants) {
                ok &= variant.matches(complexIter.next());
            }
            return ok;
        }
        private ExpectMessageNode parent;  // for finishComplexArg()
        private boolean explicitOffset;
        private double offset;
        private List<ExpectVariantNode> variants = new ArrayList<ExpectVariantNode>();
    }

    private class ExpectVariantNode {
        private ExpectVariantNode(ExpectComplexArgNode parent, String selector) {
            this(parent, selector, MessagePattern.NO_NUMERIC_VALUE);
        }
        private ExpectVariantNode(ExpectComplexArgNode parent, String selector, double value) {
            this.selector = selector;
            numericValue = value;
            msg = new ExpectMessageNode();
            msg.parent = parent;
        }
        private boolean matches(VariantNode v) {
            boolean ok = assertEquals("different selector strings",
                                      selector, v.getSelector());
            ok &= assertEquals("different selector strings",
                               isSelectorNumeric(), v.isSelectorNumeric());
            ok &= assertEquals("different selector strings",
                               numericValue, v.getSelectorValue());
            return ok & msg.matches(v.getMessage());
        }
        private boolean isSelectorNumeric() {
            return numericValue != MessagePattern.NO_NUMERIC_VALUE;
        }
        private String selector;
        private double numericValue;
        private ExpectMessageNode msg;
    }

    // The actual tests start here. ---------------------------------------- ***
    // Sample message strings are mostly from the MessagePatternUtilDemo.

    @Test
    public void TestHello() {
        // No syntax.
        MessageNode msg = MessagePatternUtil.buildMessageNode("Hello!");
        ExpectMessageNode expect = new ExpectMessageNode().expectTextThatContains("Hello");
        expect.checkMatches(msg);
    }

    @Test
    public void TestHelloWithApos() {
        // Literal ASCII apostrophe.
        MessageNode msg = MessagePatternUtil.buildMessageNode("Hel'lo!");
        ExpectMessageNode expect = new ExpectMessageNode().expectTextThatContains("Hel'lo");
        expect.checkMatches(msg);
    }

    @Test
    public void TestHelloWithQuote() {
        // Apostrophe starts quoted literal text.
        MessageNode msg = MessagePatternUtil.buildMessageNode("Hel'{o!");
        ExpectMessageNode expect = new ExpectMessageNode().expectTextThatContains("Hel{o");
        expect.checkMatches(msg);
        // Terminating the quote should yield the same result.
        msg = MessagePatternUtil.buildMessageNode("Hel'{'o!");
        expect.checkMatches(msg);
        // Double apostrophe inside quoted literal text still encodes a single apostrophe.
        msg = MessagePatternUtil.buildMessageNode("a'{bc''de'f");
        expect = new ExpectMessageNode().expectTextThatContains("a{bc'def");
        expect.checkMatches(msg);
    }

    @Test
    public void TestNoneArg() {
        // Numbered argument.
        MessageNode msg = MessagePatternUtil.buildMessageNode("abc{0}def");
        ExpectMessageNode expect = new ExpectMessageNode().
            expectTextThatContains("abc").expectNoneArg(0).expectTextThatContains("def");
        expect.checkMatches(msg);
        // Named argument.
        msg = MessagePatternUtil.buildMessageNode("abc{ arg }def");
        expect = new ExpectMessageNode().
            expectTextThatContains("abc").expectNoneArg("arg").expectTextThatContains("def");
        expect.checkMatches(msg);
        // Numbered and named arguments.
        msg = MessagePatternUtil.buildMessageNode("abc{1}def{arg}ghi");
        expect = new ExpectMessageNode().
            expectTextThatContains("abc").expectNoneArg(1).expectTextThatContains("def").
            expectNoneArg("arg").expectTextThatContains("ghi");
        expect.checkMatches(msg);
    }

    @Test
    public void TestSimpleArg() {
        MessageNode msg = MessagePatternUtil.buildMessageNode("a'{bc''de'f{0,number,g'hi''jk'l#}");
        ExpectMessageNode expect = new ExpectMessageNode().
            expectTextThatContains("a{bc'def").expectSimpleArg(0, "number", "g'hi''jk'l#");
        expect.checkMatches(msg);
    }

    @Test
    public void TestSelectArg() {
        MessageNode msg = MessagePatternUtil.buildMessageNode(
                "abc{2, number}ghi{3, select, xx {xxx} other {ooo}} xyz");
        ExpectMessageNode expect = new ExpectMessageNode().
            expectTextThatContains("abc").expectSimpleArg(2, "number").
            expectTextThatContains("ghi").
            expectSelectArg(3).
                expectVariant("xx").expectTextThatContains("xxx").finishVariant().
                expectVariant("other").expectTextThatContains("ooo").finishVariant().
                finishComplexArg().
            expectTextThatContains(" xyz");
        expect.checkMatches(msg);
    }

    @Test
    public void TestPluralArg() {
        // Plural with only keywords.
        MessageNode msg = MessagePatternUtil.buildMessageNode(
                "abc{num_people, plural, offset:17 few{fff} other {oooo}}xyz");
        ExpectMessageNode expect = new ExpectMessageNode().
            expectTextThatContains("abc").
            expectPluralArg("num_people").
                expectOffset(17).
                expectVariant("few").expectTextThatContains("fff").finishVariant().
                expectVariant("other").expectTextThatContains("oooo").finishVariant().
                finishComplexArg().
            expectTextThatContains("xyz");
        expect.checkMatches(msg);
        // Plural with explicit-value selectors.
        msg = MessagePatternUtil.buildMessageNode(
                "abc{ num , plural , offset: 2 =1 {1} =-1 {-1} =3.14 {3.14} other {oo} }xyz");
        expect = new ExpectMessageNode().
            expectTextThatContains("abc").
            expectPluralArg("num").
                expectOffset(2).
                expectVariant("=1", 1).expectTextThatContains("1").finishVariant().
                expectVariant("=-1", -1).expectTextThatContains("-1").finishVariant().
                expectVariant("=3.14", 3.14).expectTextThatContains("3.14").finishVariant().
                expectVariant("other").expectTextThatContains("oo").finishVariant().
                finishComplexArg().
            expectTextThatContains("xyz");
        expect.checkMatches(msg);
        // Plural with number replacement.
        msg = MessagePatternUtil.buildMessageNode(
                "a_{0,plural,other{num=#'#'=#'#'={1,number,##}!}}_z");
        expect = new ExpectMessageNode().
            expectTextThatContains("a_").
            expectPluralArg(0).
                expectVariant("other").
                    expectTextThatContains("num=").expectReplaceNumber().
                    expectTextThatContains("#=").expectReplaceNumber().
                    expectTextThatContains("#=").expectSimpleArg(1, "number", "##").
                    expectTextThatContains("!").finishVariant().
                finishComplexArg().
            expectTextThatContains("_z");
        expect.checkMatches(msg);
        // Plural with explicit offset:0.
        msg = MessagePatternUtil.buildMessageNode(
                "a_{0,plural,offset:0 other{num=#!}}_z");
        expect = new ExpectMessageNode().
            expectTextThatContains("a_").
            expectPluralArg(0).
                expectOffset(0).
                expectVariant("other").
                    expectTextThatContains("num=").expectReplaceNumber().
                    expectTextThatContains("!").finishVariant().
                finishComplexArg().
            expectTextThatContains("_z");
        expect.checkMatches(msg);
    }


    @Test
    public void TestSelectOrdinalArg() {
        MessageNode msg = MessagePatternUtil.buildMessageNode(
                "abc{num, selectordinal, offset:17 =0{null} few{fff} other {oooo}}xyz");
        ExpectMessageNode expect = new ExpectMessageNode().
            expectTextThatContains("abc").
            expectSelectOrdinalArg("num").
                expectOffset(17).
                expectVariant("=0", 0).expectTextThatContains("null").finishVariant().
                expectVariant("few").expectTextThatContains("fff").finishVariant().
                expectVariant("other").expectTextThatContains("oooo").finishVariant().
                finishComplexArg().
            expectTextThatContains("xyz");
        expect.checkMatches(msg);
    }

    @Test
    public void TestChoiceArg() {
        MessageNode msg = MessagePatternUtil.buildMessageNode(
                "a_{0,choice,-∞ #-inf|  5≤ five | 99 # ninety'|'nine  }_z");
        ExpectMessageNode expect = new ExpectMessageNode().
            expectTextThatContains("a_").
            expectChoiceArg(0).
                expectVariant("#", Double.NEGATIVE_INFINITY).
                    expectTextThatContains("-inf").finishVariant().
                expectVariant("≤", 5).expectTextThatContains(" five ").finishVariant().
                expectVariant("#", 99).expectTextThatContains(" ninety|nine  ").finishVariant().
                finishComplexArg().
            expectTextThatContains("_z");
        expect.checkMatches(msg);
    }

    @Test
    public void TestComplexArgs() {
        MessageNode msg = MessagePatternUtil.buildMessageNode(
                "I don't {a,plural,other{w'{'on't #'#'}} and "+
                "{b,select,other{shan't'}'}} '{'''know'''}' and "+
                "{c,choice,0#can't'|'}"+
                "{z,number,#'#'###.00'}'}.");
        ExpectMessageNode expect = new ExpectMessageNode().
            expectTextThatContains("I don't ").
            expectPluralArg("a").
                expectVariant("other").
                    expectTextThatContains("w{on't ").expectReplaceNumber().
                    expectTextThatContains("#").finishVariant().
                finishComplexArg().
            expectTextThatContains(" and ").
            expectSelectArg("b").
                expectVariant("other").expectTextThatContains("shan't}").finishVariant().
                finishComplexArg().
            expectTextThatContains(" {'know'} and ").
            expectChoiceArg("c").
                expectVariant("#", 0).expectTextThatContains("can't|").finishVariant().
                finishComplexArg().
            expectSimpleArg("z", "number", "#'#'###.00'}'").
            expectTextThatContains(".");
        expect.checkMatches(msg);
    }

    /**
     * @return the text string of the VariantNode's message;
     *         assumes that its message consists of only text
     */
    private String variantText(VariantNode v) {
        return ((TextNode)v.getMessage().getContents().get(0)).getText();
    }

    @Test
    public void TestPluralVariantsByType() {
        MessageNode msg = MessagePatternUtil.buildMessageNode(
                "{p,plural,a{A}other{O}=4{iv}b{B}other{U}=2{ii}}");
        ExpectMessageNode expect = new ExpectMessageNode().
        expectPluralArg("p").
            expectVariant("a").expectTextThatContains("A").finishVariant().
            expectVariant("other").expectTextThatContains("O").finishVariant().
            expectVariant("=4", 4).expectTextThatContains("iv").finishVariant().
            expectVariant("b").expectTextThatContains("B").finishVariant().
            expectVariant("other").expectTextThatContains("U").finishVariant().
            expectVariant("=2", 2).expectTextThatContains("ii").finishVariant().
            finishComplexArg();
        if (!expect.matches(msg)) {
            return;
        }
        List<VariantNode> numericVariants = new ArrayList<VariantNode>();
        List<VariantNode> keywordVariants = new ArrayList<VariantNode>();
        VariantNode other =
            ((ArgNode)msg.getContents().get(0)).getComplexStyle().
            getVariantsByType(numericVariants, keywordVariants);
        assertEquals("'other' selector", "other", other.getSelector());
        assertEquals("message string of first 'other'", "O", variantText(other));

        assertEquals("numericVariants.size()", 2, numericVariants.size());
        VariantNode v = numericVariants.get(0);
        assertEquals("numericVariants[0] selector", "=4", v.getSelector());
        assertEquals("numericVariants[0] selector value", 4., v.getSelectorValue());
        assertEquals("numericVariants[0] text", "iv", variantText(v));
        v = numericVariants.get(1);
        assertEquals("numericVariants[1] selector", "=2", v.getSelector());
        assertEquals("numericVariants[1] selector value", 2., v.getSelectorValue());
        assertEquals("numericVariants[1] text", "ii", variantText(v));

        assertEquals("keywordVariants.size()", 2, keywordVariants.size());
        v = keywordVariants.get(0);
        assertEquals("keywordVariants[0] selector", "a", v.getSelector());
        assertFalse("keywordVariants[0].isSelectorNumeric()", v.isSelectorNumeric());
        assertEquals("keywordVariants[0] text", "A", variantText(v));
        v = keywordVariants.get(1);
        assertEquals("keywordVariants[1] selector", "b", v.getSelector());
        assertFalse("keywordVariants[1].isSelectorNumeric()", v.isSelectorNumeric());
        assertEquals("keywordVariants[1] text", "B", variantText(v));
    }

    @Test
    public void TestSelectVariantsByType() {
        MessageNode msg = MessagePatternUtil.buildMessageNode(
                "{s,select,a{A}other{O}b{B}other{U}}");
        ExpectMessageNode expect = new ExpectMessageNode().
        expectSelectArg("s").
            expectVariant("a").expectTextThatContains("A").finishVariant().
            expectVariant("other").expectTextThatContains("O").finishVariant().
            expectVariant("b").expectTextThatContains("B").finishVariant().
            expectVariant("other").expectTextThatContains("U").finishVariant().
            finishComplexArg();
        if (!expect.matches(msg)) {
            return;
        }
        // Check that we can use numericVariants = null.
        List<VariantNode> keywordVariants = new ArrayList<VariantNode>();
        VariantNode other =
            ((ArgNode)msg.getContents().get(0)).getComplexStyle().
            getVariantsByType(null, keywordVariants);
        assertEquals("'other' selector", "other", other.getSelector());
        assertEquals("message string of first 'other'", "O", variantText(other));

        assertEquals("keywordVariants.size()", 2, keywordVariants.size());
        VariantNode v = keywordVariants.get(0);
        assertEquals("keywordVariants[0] selector", "a", v.getSelector());
        assertFalse("keywordVariants[0].isSelectorNumeric()", v.isSelectorNumeric());
        assertEquals("keywordVariants[0] text", "A", variantText(v));
        v = keywordVariants.get(1);
        assertEquals("keywordVariants[1] selector", "b", v.getSelector());
        assertFalse("keywordVariants[1].isSelectorNumeric()", v.isSelectorNumeric());
        assertEquals("keywordVariants[1] text", "B", variantText(v));
    }
}
