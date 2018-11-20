/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
*******************************************************************************
*   Copyright (C) 2011-2014, International Business Machines
*   Corporation and others.  All Rights Reserved.
*******************************************************************************
*   created on: 2011jul14
*   created by: Markus W. Scherer
*/

package android.icu.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for working with a MessagePattern.
 * Intended for use in tools when convenience is more important than
 * minimizing runtime and object creations.
 *
 * <p>This class only has static methods.
 * Each of the nested classes is immutable and thread-safe.
 *
 * <p>This class and its nested classes are not intended for public subclassing.
 * @author Markus Scherer
 * @hide Only a subset of ICU is exposed in Android
 */
public final class MessagePatternUtil {

    // Private constructor preventing object instantiation
    private MessagePatternUtil() {
    }

    /**
     * Factory method, builds and returns a MessageNode from a MessageFormat pattern string.
     * @param patternString a MessageFormat pattern string
     * @return a MessageNode or a ComplexArgStyleNode
     * @throws IllegalArgumentException if the MessagePattern is empty
     *         or does not represent a MessageFormat pattern
     */
    public static MessageNode buildMessageNode(String patternString) {
        return buildMessageNode(new MessagePattern(patternString));
    }

    /**
     * Factory method, builds and returns a MessageNode from a MessagePattern.
     * @param pattern a parsed MessageFormat pattern string
     * @return a MessageNode or a ComplexArgStyleNode
     * @throws IllegalArgumentException if the MessagePattern is empty
     *         or does not represent a MessageFormat pattern
     */
    public static MessageNode buildMessageNode(MessagePattern pattern) {
        int limit = pattern.countParts() - 1;
        if (limit < 0) {
            throw new IllegalArgumentException("The MessagePattern is empty");
        } else if (pattern.getPartType(0) != MessagePattern.Part.Type.MSG_START) {
            throw new IllegalArgumentException(
            "The MessagePattern does not represent a MessageFormat pattern");
        }
        return buildMessageNode(pattern, 0, limit);
    }

    /**
     * Common base class for all elements in a tree of nodes
     * returned by {@link MessagePatternUtil#buildMessageNode(MessagePattern)}.
     * This class and all subclasses are immutable and thread-safe.
     */
    public static class Node {
        private Node() {}
    }

    /**
     * A Node representing a parsed MessageFormat pattern string.
     */
    public static class MessageNode extends Node {
        /**
         * @return the list of MessageContentsNode nodes that this message contains
         */
        public List<MessageContentsNode> getContents() {
            return list;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return list.toString();
        }

        private MessageNode() {
            super();
        }
        private void addContentsNode(MessageContentsNode node) {
            if (node instanceof TextNode && !list.isEmpty()) {
                // Coalesce adjacent text nodes.
                MessageContentsNode lastNode = list.get(list.size() - 1);
                if (lastNode instanceof TextNode) {
                    TextNode textNode = (TextNode)lastNode;
                    textNode.text = textNode.text + ((TextNode)node).text;
                    return;
                }
            }
            list.add(node);
        }
        private MessageNode freeze() {
            list = Collections.unmodifiableList(list);
            return this;
        }

        private volatile List<MessageContentsNode> list = new ArrayList<MessageContentsNode>();
    }

    /**
     * A piece of MessageNode contents.
     * Use getType() to determine the type and the actual Node subclass.
     */
    public static class MessageContentsNode extends Node {
        /**
         * The type of a piece of MessageNode contents.
         */
        public enum Type {
            /**
             * This is a TextNode containing literal text (downcast and call getText()).
             */
            TEXT,
            /**
             * This is an ArgNode representing a message argument
             * (downcast and use specific methods).
             */
            ARG,
            /**
             * This Node represents a place in a plural argument's variant where
             * the formatted (plural-offset) value is to be put.
             */
            REPLACE_NUMBER
        }
        /**
         * Returns the type of this piece of MessageNode contents.
         */
        public Type getType() {
            return type;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            // Note: There is no specific subclass for REPLACE_NUMBER
            // because it would not provide any additional API.
            // Therefore we have a little bit of REPLACE_NUMBER-specific code
            // here in the contents-node base class.
            return "{REPLACE_NUMBER}";
        }

        private MessageContentsNode(Type type) {
            super();
            this.type = type;
        }
        private static MessageContentsNode createReplaceNumberNode() {
            return new MessageContentsNode(Type.REPLACE_NUMBER);
        }

        private Type type;
    }

    /**
     * Literal text, a piece of MessageNode contents.
     */
    public static class TextNode extends MessageContentsNode {
        /**
         * @return the literal text at this point in the message
         */
        public String getText() {
            return text;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "«" + text + "»";
        }

        private TextNode(String text) {
            super(Type.TEXT);
            this.text = text;
        }

        private String text;
    }

    /**
     * A piece of MessageNode contents representing a message argument and its details.
     */
    public static class ArgNode extends MessageContentsNode {
        /**
         * @return the argument type
         */
        public MessagePattern.ArgType getArgType() {
            return argType;
        }
        /**
         * @return the argument name string (the decimal-digit string if the argument has a number)
         */
        public String getName() {
            return name;
        }
        /**
         * @return the argument number, or -1 if none (for a named argument)
         */
        public int getNumber() {
            return number;
        }
        /**
         * @return the argument type string, or null if none was specified
         */
        public String getTypeName() {
            return typeName;
        }
        /**
         * @return the simple-argument style string,
         *         or null if no style is specified and for other argument types
         */
        public String getSimpleStyle() {
            return style;
        }
        /**
         * @return the complex-argument-style object,
         *         or null if the argument type is NONE_ARG or SIMPLE_ARG
         */
        public ComplexArgStyleNode getComplexStyle() {
            return complexStyle;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('{').append(name);
            if (argType != MessagePattern.ArgType.NONE) {
                sb.append(',').append(typeName);
                if (argType == MessagePattern.ArgType.SIMPLE) {
                    if (style != null) {
                        sb.append(',').append(style);
                    }
                } else {
                    sb.append(',').append(complexStyle.toString());
                }
            }
            return sb.append('}').toString();
        }

        private ArgNode() {
            super(Type.ARG);
        }
        private static ArgNode createArgNode() {
            return new ArgNode();
        }

        private MessagePattern.ArgType argType;
        private String name;
        private int number = -1;
        private String typeName;
        private String style;
        private ComplexArgStyleNode complexStyle;
    }

    /**
     * A Node representing details of the argument style of a complex argument.
     * (Which is a choice/plural/select argument which selects among nested messages.)
     */
    public static class ComplexArgStyleNode extends Node {
        /**
         * @return the argument type (same as getArgType() on the parent ArgNode)
         */
        public MessagePattern.ArgType getArgType() {
            return argType;
        }
        /**
         * @return true if this is a plural style with an explicit offset
         */
        public boolean hasExplicitOffset() {
            return explicitOffset;
        }
        /**
         * @return the plural offset, or 0 if this is not a plural style or
         *         the offset is explicitly or implicitly 0
         */
        public double getOffset() {
            return offset;
        }
        /**
         * @return the list of variants: the nested messages with their selection criteria
         */
        public List<VariantNode> getVariants() {
            return list;
        }
        /**
         * Separates the variants by type.
         * Intended for use with plural and select argument styles,
         * not useful for choice argument styles.
         *
         * <p>Both parameters are used only for output, and are first cleared.
         * @param numericVariants Variants with numeric-value selectors (if any) are added here.
         *        Can be null for a select argument style.
         * @param keywordVariants Variants with keyword selectors, except "other", are added here.
         *        For a plural argument, if this list is empty after the call, then
         *        all variants except "other" have explicit values
         *        and PluralRules need not be called.
         * @return the "other" variant (the first one if there are several),
         *         null if none (choice style)
         */
        public VariantNode getVariantsByType(List<VariantNode> numericVariants,
                                             List<VariantNode> keywordVariants) {
            if (numericVariants != null) {
                numericVariants.clear();
            }
            keywordVariants.clear();
            VariantNode other = null;
            for (VariantNode variant : list) {
                if (variant.isSelectorNumeric()) {
                    numericVariants.add(variant);
                } else if ("other".equals(variant.getSelector())) {
                    if (other == null) {
                        // Return the first "other" variant. (MessagePattern allows duplicates.)
                        other = variant;
                    }
                } else {
                    keywordVariants.add(variant);
                }
            }
            return other;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('(').append(argType.toString()).append(" style) ");
            if (hasExplicitOffset()) {
                sb.append("offset:").append(offset).append(' ');
            }
            return sb.append(list.toString()).toString();
        }

        private ComplexArgStyleNode(MessagePattern.ArgType argType) {
            super();
            this.argType = argType;
        }
        private void addVariant(VariantNode variant) {
            list.add(variant);
        }
        private ComplexArgStyleNode freeze() {
            list = Collections.unmodifiableList(list);
            return this;
        }

        private MessagePattern.ArgType argType;
        private double offset;
        private boolean explicitOffset;
        private volatile List<VariantNode> list = new ArrayList<VariantNode>();
    }

    /**
     * A Node representing a nested message (nested inside an argument)
     * with its selection criterium.
     */
    public static class VariantNode extends Node {
        /**
         * Returns the selector string.
         * For example: A plural/select keyword ("few"), a plural explicit value ("=1"),
         * a choice comparison operator ("#").
         * @return the selector string
         */
        public String getSelector() {
            return selector;
        }
        /**
         * @return true for choice variants and for plural explicit values
         */
        public boolean isSelectorNumeric() {
            return numericValue != MessagePattern.NO_NUMERIC_VALUE;
        }
        /**
         * @return the selector's numeric value, or NO_NUMERIC_VALUE if !isSelectorNumeric()
         */
        public double getSelectorValue() {
            return numericValue;
        }
        /**
         * @return the nested message
         */
        public MessageNode getMessage() {
            return msgNode;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (isSelectorNumeric()) {
                sb.append(numericValue).append(" (").append(selector).append(") {");
            } else {
                sb.append(selector).append(" {");
            }
            return sb.append(msgNode.toString()).append('}').toString();
        }

        private VariantNode() {
            super();
        }

        private String selector;
        private double numericValue = MessagePattern.NO_NUMERIC_VALUE;
        private MessageNode msgNode;
    }

    private static MessageNode buildMessageNode(MessagePattern pattern, int start, int limit) {
        int prevPatternIndex = pattern.getPart(start).getLimit();
        MessageNode node = new MessageNode();
        for (int i = start + 1;; ++i) {
            MessagePattern.Part part = pattern.getPart(i);
            int patternIndex = part.getIndex();
            if (prevPatternIndex < patternIndex) {
                node.addContentsNode(
                        new TextNode(pattern.getPatternString().substring(prevPatternIndex,
                                     patternIndex)));
            }
            if (i == limit) {
                break;
            }
            MessagePattern.Part.Type partType = part.getType();
            if (partType == MessagePattern.Part.Type.ARG_START) {
                int argLimit = pattern.getLimitPartIndex(i);
                node.addContentsNode(buildArgNode(pattern, i, argLimit));
                i = argLimit;
                part = pattern.getPart(i);
            } else if (partType == MessagePattern.Part.Type.REPLACE_NUMBER) {
                node.addContentsNode(MessageContentsNode.createReplaceNumberNode());
                // else: ignore SKIP_SYNTAX and INSERT_CHAR parts.
            }
            prevPatternIndex = part.getLimit();
        }
        return node.freeze();
    }

    private static ArgNode buildArgNode(MessagePattern pattern, int start, int limit) {
        ArgNode node = ArgNode.createArgNode();
        MessagePattern.Part part = pattern.getPart(start);
        MessagePattern.ArgType argType = node.argType = part.getArgType();
        part = pattern.getPart(++start);  // ARG_NAME or ARG_NUMBER
        node.name = pattern.getSubstring(part);
        if (part.getType() == MessagePattern.Part.Type.ARG_NUMBER) {
            node.number = part.getValue();
        }
        ++start;
        switch(argType) {
        case SIMPLE:
            // ARG_TYPE
            node.typeName = pattern.getSubstring(pattern.getPart(start++));
            if (start < limit) {
                // ARG_STYLE
                node.style = pattern.getSubstring(pattern.getPart(start));
            }
            break;
        case CHOICE:
            node.typeName = "choice";
            node.complexStyle = buildChoiceStyleNode(pattern, start, limit);
            break;
        case PLURAL:
            node.typeName = "plural";
            node.complexStyle = buildPluralStyleNode(pattern, start, limit, argType);
            break;
        case SELECT:
            node.typeName = "select";
            node.complexStyle = buildSelectStyleNode(pattern, start, limit);
            break;
        case SELECTORDINAL:
            node.typeName = "selectordinal";
            node.complexStyle = buildPluralStyleNode(pattern, start, limit, argType);
            break;
        default:
            // NONE type, nothing else to do
            break;
        }
        return node;
    }

    private static ComplexArgStyleNode buildChoiceStyleNode(MessagePattern pattern,
                                                            int start, int limit) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(MessagePattern.ArgType.CHOICE);
        while (start < limit) {
            int valueIndex = start;
            MessagePattern.Part part = pattern.getPart(start);
            double value = pattern.getNumericValue(part);
            start += 2;
            int msgLimit = pattern.getLimitPartIndex(start);
            VariantNode variant = new VariantNode();
            variant.selector = pattern.getSubstring(pattern.getPart(valueIndex + 1));
            variant.numericValue = value;
            variant.msgNode = buildMessageNode(pattern, start, msgLimit);
            node.addVariant(variant);
            start = msgLimit + 1;
        }
        return node.freeze();
    }

    private static ComplexArgStyleNode buildPluralStyleNode(MessagePattern pattern,
                                                            int start, int limit,
                                                            MessagePattern.ArgType argType) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(argType);
        MessagePattern.Part offset = pattern.getPart(start);
        if (offset.getType().hasNumericValue()) {
            node.explicitOffset = true;
            node.offset = pattern.getNumericValue(offset);
            ++start;
        }
        while (start < limit) {
            MessagePattern.Part selector = pattern.getPart(start++);
            double value = MessagePattern.NO_NUMERIC_VALUE;
            MessagePattern.Part part = pattern.getPart(start);
            if (part.getType().hasNumericValue()) {
                value = pattern.getNumericValue(part);
                ++start;
            }
            int msgLimit = pattern.getLimitPartIndex(start);
            VariantNode variant = new VariantNode();
            variant.selector = pattern.getSubstring(selector);
            variant.numericValue = value;
            variant.msgNode = buildMessageNode(pattern, start, msgLimit);
            node.addVariant(variant);
            start = msgLimit + 1;
        }
        return node.freeze();
    }

    private static ComplexArgStyleNode buildSelectStyleNode(MessagePattern pattern,
                                                            int start, int limit) {
        ComplexArgStyleNode node = new ComplexArgStyleNode(MessagePattern.ArgType.SELECT);
        while (start < limit) {
            MessagePattern.Part selector = pattern.getPart(start++);
            int msgLimit = pattern.getLimitPartIndex(start);
            VariantNode variant = new VariantNode();
            variant.selector = pattern.getSubstring(selector);
            variant.msgNode = buildMessageNode(pattern, start, msgLimit);
            node.addVariant(variant);
            start = msgLimit + 1;
        }
        return node.freeze();
    }
}
