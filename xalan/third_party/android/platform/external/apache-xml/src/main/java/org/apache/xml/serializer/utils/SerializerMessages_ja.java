/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: SerializerMessages_ja.java 471981 2006-11-07 04:28:00Z minchau $
 */

package org.apache.xml.serializer.utils;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * An instance of this class is a ListResourceBundle that
 * has the required getContents() method that returns
 * an array of message-key/message associations.
 * <p>
 * The message keys are defined in {@link MsgKey}. The
 * messages that those keys map to are defined here.
 * <p>
 * The messages in the English version are intended to be
 * translated.
 *
 * This class is not a public API, it is only public because it is
 * used in org.apache.xml.serializer.
 *
 * @xsl.usage internal
 */
public class SerializerMessages_ja extends ListResourceBundle {

    /*
     * This file contains error and warning messages related to
     * Serializer Error Handling.
     *
     *  General notes to translators:

     *  1) A stylesheet is a description of how to transform an input XML document
     *     into a resultant XML document (or HTML document or text).  The
     *     stylesheet itself is described in the form of an XML document.

     *
     *  2) An element is a mark-up tag in an XML document; an attribute is a
     *     modifier on the tag.  For example, in <elem attr='val' attr2='val2'>
     *     "elem" is an element name, "attr" and "attr2" are attribute names with
     *     the values "val" and "val2", respectively.
     *
     *  3) A namespace declaration is a special attribute that is used to associate
     *     a prefix with a URI (the namespace).  The meanings of element names and
     *     attribute names that use that prefix are defined with respect to that
     *     namespace.
     *
     *
     */

    /** The lookup table for error messages.   */
    public Object[][] getContents() {
        Object[][] contents = new Object[][] {
            {   MsgKey.BAD_MSGKEY,
                "\u30e1\u30c3\u30bb\u30fc\u30b8\u30fb\u30ad\u30fc ''{0}'' \u306f\u30e1\u30c3\u30bb\u30fc\u30b8\u30fb\u30af\u30e9\u30b9 ''{1}'' \u306b\u3042\u308a\u307e\u305b\u3093\u3002" },

            {   MsgKey.BAD_MSGFORMAT,
                "\u30e1\u30c3\u30bb\u30fc\u30b8\u30fb\u30af\u30e9\u30b9 ''{1}'' \u306e\u30e1\u30c3\u30bb\u30fc\u30b8 ''{0}'' \u306e\u30d5\u30a9\u30fc\u30de\u30c3\u30c8\u8a2d\u5b9a\u304c\u5931\u6557\u3057\u307e\u3057\u305f\u3002" },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "\u30b7\u30ea\u30a2\u30e9\u30a4\u30b6\u30fc\u30fb\u30af\u30e9\u30b9 ''{0}'' \u306f org.xml.sax.ContentHandler \u3092\u5b9f\u88c5\u3057\u307e\u305b\u3093\u3002" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "\u30ea\u30bd\u30fc\u30b9 [ {0} ] \u306f\u898b\u3064\u304b\u308a\u307e\u305b\u3093\u3067\u3057\u305f\u3002\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "\u30ea\u30bd\u30fc\u30b9 [ {0} ] \u3092\u30ed\u30fc\u30c9\u3067\u304d\u307e\u305b\u3093\u3067\u3057\u305f: {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "\u30d0\u30c3\u30d5\u30a1\u30fc\u30fb\u30b5\u30a4\u30ba <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "\u7121\u52b9\u306a UTF-16 \u30b5\u30ed\u30b2\u30fc\u30c8\u304c\u691c\u51fa\u3055\u308c\u307e\u3057\u305f: {0} ?" },

            {   MsgKey.ER_OIERROR,
                "\u5165\u51fa\u529b\u30a8\u30e9\u30fc" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "\u4e0b\u4f4d\u30ce\u30fc\u30c9\u306e\u5f8c\u307e\u305f\u306f\u8981\u7d20\u304c\u751f\u6210\u3055\u308c\u308b\u524d\u306b\u5c5e\u6027 {0} \u306f\u8ffd\u52a0\u3067\u304d\u307e\u305b\u3093\u3002  \u5c5e\u6027\u306f\u7121\u8996\u3055\u308c\u307e\u3059\u3002" },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "\u63a5\u982d\u90e8 ''{0}'' \u306e\u540d\u524d\u7a7a\u9593\u304c\u5ba3\u8a00\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002" },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "\u5c5e\u6027 ''{0}'' \u304c\u8981\u7d20\u306e\u5916\u5074\u3067\u3059\u3002" },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "\u540d\u524d\u7a7a\u9593\u5ba3\u8a00 ''{0}''=''{1}'' \u304c\u8981\u7d20\u306e\u5916\u5074\u3067\u3059\u3002" },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "''{0}'' \u3092\u30ed\u30fc\u30c9\u3067\u304d\u307e\u305b\u3093\u3067\u3057\u305f (CLASSPATH \u3092\u78ba\u8a8d\u3057\u3066\u304f\u3060\u3055\u3044)\u3002\u73fe\u5728\u306f\u30c7\u30d5\u30a9\u30eb\u30c8\u306e\u3082\u306e\u306e\u307f\u3092\u4f7f\u7528\u3057\u3066\u3044\u307e\u3059\u3002" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "{1} \u306e\u6307\u5b9a\u3055\u308c\u305f\u51fa\u529b\u30a8\u30f3\u30b3\u30fc\u30c9\u3067\u8868\u305b\u306a\u3044\u6574\u6570\u5024 {0} \u306e\u6587\u5b57\u306e\u51fa\u529b\u3092\u8a66\u307f\u307e\u3057\u305f\u3002" },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "\u51fa\u529b\u30e1\u30bd\u30c3\u30c9 ''{1}'' \u306e\u30d7\u30ed\u30d1\u30c6\u30a3\u30fc\u30fb\u30d5\u30a1\u30a4\u30eb ''{0}'' \u3092\u30ed\u30fc\u30c9\u3067\u304d\u307e\u305b\u3093\u3067\u3057\u305f (CLASSPATH \u3092\u78ba\u8a8d\u3057\u3066\u304f\u3060\u3055\u3044)" },

            {   MsgKey.ER_INVALID_PORT,
                "\u7121\u52b9\u306a\u30dd\u30fc\u30c8\u756a\u53f7" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "\u30db\u30b9\u30c8\u304c\u30cc\u30eb\u3067\u3042\u308b\u3068\u30dd\u30fc\u30c8\u3092\u8a2d\u5b9a\u3067\u304d\u307e\u305b\u3093" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "\u30db\u30b9\u30c8\u306f\u3046\u307e\u304f\u69cb\u6210\u3055\u308c\u305f\u30a2\u30c9\u30ec\u30b9\u3067\u3042\u308a\u307e\u305b\u3093" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "\u30b9\u30ad\u30fc\u30e0\u306f\u4e00\u81f4\u3057\u3066\u3044\u307e\u305b\u3093\u3002" },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "\u30cc\u30eb\u30fb\u30b9\u30c8\u30ea\u30f3\u30b0\u304b\u3089\u306f\u30b9\u30ad\u30fc\u30e0\u3092\u8a2d\u5b9a\u3067\u304d\u307e\u305b\u3093" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "\u30d1\u30b9\u306b\u7121\u52b9\u306a\u30a8\u30b9\u30b1\u30fc\u30d7\u30fb\u30b7\u30fc\u30b1\u30f3\u30b9\u304c\u542b\u307e\u308c\u3066\u3044\u307e\u3059" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "\u30d1\u30b9\u306b\u7121\u52b9\u6587\u5b57: {0} \u304c\u542b\u307e\u308c\u3066\u3044\u307e\u3059" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "\u30d5\u30e9\u30b0\u30e1\u30f3\u30c8\u306b\u7121\u52b9\u6587\u5b57\u304c\u542b\u307e\u308c\u3066\u3044\u307e\u3059" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "\u30d1\u30b9\u304c\u30cc\u30eb\u3067\u3042\u308b\u3068\u30d5\u30e9\u30b0\u30e1\u30f3\u30c8\u3092\u8a2d\u5b9a\u3067\u304d\u307e\u305b\u3093" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "\u7dcf\u79f0 URI \u306e\u30d5\u30e9\u30b0\u30e1\u30f3\u30c8\u3057\u304b\u8a2d\u5b9a\u3067\u304d\u307e\u305b\u3093" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "\u30b9\u30ad\u30fc\u30e0\u306f URI \u3067\u898b\u3064\u304b\u308a\u307e\u305b\u3093" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "URI \u306f\u7a7a\u306e\u30d1\u30e9\u30e1\u30fc\u30bf\u30fc\u3092\u4f7f\u7528\u3057\u3066\u521d\u671f\u5316\u3067\u304d\u307e\u305b\u3093" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "\u30d5\u30e9\u30b0\u30e1\u30f3\u30c8\u306f\u30d1\u30b9\u3068\u30d5\u30e9\u30b0\u30e1\u30f3\u30c8\u306e\u4e21\u65b9\u306b\u6307\u5b9a\u3067\u304d\u307e\u305b\u3093" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "\u7167\u4f1a\u30b9\u30c8\u30ea\u30f3\u30b0\u306f\u30d1\u30b9\u304a\u3088\u3073\u7167\u4f1a\u30b9\u30c8\u30ea\u30f3\u30b0\u5185\u306b\u6307\u5b9a\u3067\u304d\u307e\u305b\u3093" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "\u30db\u30b9\u30c8\u304c\u6307\u5b9a\u3055\u308c\u3066\u3044\u306a\u3044\u5834\u5408\u306f\u30dd\u30fc\u30c8\u3092\u6307\u5b9a\u3057\u3066\u306f\u3044\u3051\u307e\u305b\u3093" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "\u30db\u30b9\u30c8\u304c\u6307\u5b9a\u3055\u308c\u3066\u3044\u306a\u3044\u5834\u5408\u306f Userinfo \u3092\u6307\u5b9a\u3057\u3066\u306f\u3044\u3051\u307e\u305b\u3093" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "\u8b66\u544a: \u51fa\u529b\u6587\u66f8\u306e\u30d0\u30fc\u30b8\u30e7\u30f3\u3068\u3057\u3066 ''{0}'' \u304c\u8981\u6c42\u3055\u308c\u307e\u3057\u305f\u3002  \u3053\u306e\u30d0\u30fc\u30b8\u30e7\u30f3\u306e XML \u306f\u30b5\u30dd\u30fc\u30c8\u3055\u308c\u307e\u305b\u3093\u3002  \u51fa\u529b\u6587\u66f8\u306e\u30d0\u30fc\u30b8\u30e7\u30f3\u306f ''1.0'' \u306b\u306a\u308a\u307e\u3059\u3002" },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "\u30b9\u30ad\u30fc\u30e0\u304c\u5fc5\u8981\u3067\u3059\u3002" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "SerializerFactory \u306b\u6e21\u3055\u308c\u305f Properties \u30aa\u30d6\u30b8\u30a7\u30af\u30c8\u306b\u306f ''{0}'' \u30d7\u30ed\u30d1\u30c6\u30a3\u30fc\u304c\u3042\u308a\u307e\u305b\u3093\u3002" },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "\u8b66\u544a:  \u30a8\u30f3\u30b3\u30fc\u30c9 ''{0}'' \u306f\u3053\u306e Java \u30e9\u30f3\u30bf\u30a4\u30e0\u3067\u306f\u30b5\u30dd\u30fc\u30c8\u3055\u308c\u3066\u3044\u307e\u305b\u3093\u3002" },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "\u30d1\u30e9\u30e1\u30fc\u30bf\u30fc ''{0}'' \u306f\u8a8d\u8b58\u3055\u308c\u307e\u305b\u3093\u3002"},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "\u30d1\u30e9\u30e1\u30fc\u30bf\u30fc ''{0}'' \u306f\u8a8d\u8b58\u3055\u308c\u307e\u3059\u304c\u3001\u8981\u6c42\u3055\u308c\u305f\u5024\u306f\u8a2d\u5b9a\u3067\u304d\u307e\u305b\u3093\u3002"},

             {MsgKey.ER_STRING_TOO_LONG,
             "\u7d50\u679c\u306e\u30b9\u30c8\u30ea\u30f3\u30b0\u304c\u9577\u3059\u304e\u308b\u305f\u3081\u3001DOMString \u5185\u306b\u53ce\u307e\u308a\u307e\u305b\u3093: ''{0}''\u3002"},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "\u3053\u306e\u30d1\u30e9\u30e1\u30fc\u30bf\u30fc\u540d\u306e\u5024\u306e\u578b\u306f\u3001\u671f\u5f85\u3055\u308c\u308b\u5024\u306e\u578b\u3068\u4e0d\u9069\u5408\u3067\u3059\u3002"},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "\u66f8\u304d\u8fbc\u307e\u308c\u308b\u30c7\u30fc\u30bf\u306e\u51fa\u529b\u5b9b\u5148\u304c\u30cc\u30eb\u3067\u3059\u3002"},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "\u30b5\u30dd\u30fc\u30c8\u3055\u308c\u306a\u3044\u30a8\u30f3\u30b3\u30fc\u30c9\u304c\u691c\u51fa\u3055\u308c\u307e\u3057\u305f\u3002"},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "\u30ce\u30fc\u30c9\u3092\u76f4\u5217\u5316\u3067\u304d\u307e\u305b\u3093\u3067\u3057\u305f\u3002"},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "CDATA \u30bb\u30af\u30b7\u30e7\u30f3\u306b 1 \u3064\u4ee5\u4e0a\u306e\u7d42\u4e86\u30de\u30fc\u30ab\u30fc ']]>' \u304c\u542b\u307e\u308c\u3066\u3044\u307e\u3059\u3002"},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "\u6574\u5f62\u5f0f\u6027\u30c1\u30a7\u30c3\u30ab\u30fc\u306e\u30a4\u30f3\u30b9\u30bf\u30f3\u30b9\u3092\u4f5c\u6210\u3067\u304d\u307e\u305b\u3093\u3067\u3057\u305f\u3002  well-formed \u30d1\u30e9\u30e1\u30fc\u30bf\u30fc\u306e\u8a2d\u5b9a\u306f true \u3067\u3057\u305f\u304c\u3001\u6574\u5f62\u5f0f\u6027\u306e\u691c\u67fb\u306f\u5b9f\u884c\u3067\u304d\u307e\u305b\u3093\u3002"
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "\u30ce\u30fc\u30c9 ''{0}'' \u306b\u7121\u52b9\u306a XML \u6587\u5b57\u304c\u3042\u308a\u307e\u3059\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "\u30b3\u30e1\u30f3\u30c8\u306e\u4e2d\u306b\u7121\u52b9\u306a XML \u6587\u5b57 (Unicode: 0x{0}) \u304c\u898b\u3064\u304b\u308a\u307e\u3057\u305f\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "\u51e6\u7406\u547d\u4ee4\u30c7\u30fc\u30bf\u306e\u4e2d\u306b\u7121\u52b9\u306a XML \u6587\u5b57 (Unicode: 0x{0}) \u304c\u898b\u3064\u304b\u308a\u307e\u3057\u305f\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "CDATA \u30bb\u30af\u30b7\u30e7\u30f3\u306e\u4e2d\u306b\u7121\u52b9\u306a XML \u6587\u5b57 (Unicode: 0x{0}) \u304c\u898b\u3064\u304b\u308a\u307e\u3057\u305f\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "\u30ce\u30fc\u30c9\u306e\u6587\u5b57\u30c7\u30fc\u30bf\u306e\u5185\u5bb9\u306b\u7121\u52b9\u306a XML \u6587\u5b57 (Unicode: 0x{0}) \u304c\u898b\u3064\u304b\u308a\u307e\u3057\u305f\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "''{1}'' \u3068\u3044\u3046\u540d\u524d\u306e {0} \u30ce\u30fc\u30c9\u306e\u4e2d\u306b\u7121\u52b9\u306a XML \u6587\u5b57\u304c\u898b\u3064\u304b\u308a\u307e\u3057\u305f\u3002"
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "\u30b9\u30c8\u30ea\u30f3\u30b0 \"--\" \u306f\u30b3\u30e1\u30f3\u30c8\u5185\u3067\u306f\u4f7f\u7528\u3067\u304d\u307e\u305b\u3093\u3002"
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "\u8981\u7d20\u578b \"{0}\" \u306b\u95a2\u9023\u3057\u305f\u5c5e\u6027 \"{1}\" \u306e\u5024\u306b\u306f ''<'' \u6587\u5b57\u3092\u542b\u3081\u3066\u306f\u3044\u3051\u307e\u305b\u3093\u3002"
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "\u89e3\u6790\u5bfe\u8c61\u5916\u5b9f\u4f53\u53c2\u7167 \"&{0};\" \u306f\u8a31\u53ef\u3055\u308c\u307e\u305b\u3093\u3002"
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "\u5c5e\u6027\u5024\u3067\u306e\u5916\u90e8\u5b9f\u4f53\u53c2\u7167 \"&{0};\" \u306f\u8a31\u53ef\u3055\u308c\u307e\u305b\u3093\u3002"
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "\u63a5\u982d\u90e8 \"{0}\" \u306f\u540d\u524d\u7a7a\u9593 \"{1}\" \u306b\u7d50\u5408\u3067\u304d\u307e\u305b\u3093\u3002"
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "\u8981\u7d20 \"{0}\" \u306e\u30ed\u30fc\u30ab\u30eb\u540d\u304c\u30cc\u30eb\u3067\u3059\u3002"
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "\u5c5e\u6027 \"{0}\" \u306e\u30ed\u30fc\u30ab\u30eb\u540d\u304c\u30cc\u30eb\u3067\u3059\u3002"
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "\u5b9f\u4f53\u30ce\u30fc\u30c9 \"{0}\" \u306e\u7f6e\u63db\u30c6\u30ad\u30b9\u30c8\u306b\u3001\u672a\u7d50\u5408\u306e\u63a5\u982d\u90e8 \"{2}\" \u3092\u6301\u3064\u8981\u7d20\u30ce\u30fc\u30c9 \"{1}\" \u304c\u542b\u307e\u308c\u3066\u3044\u307e\u3059\u3002"
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "\u5b9f\u4f53\u30ce\u30fc\u30c9 \"{0}\" \u306e\u7f6e\u63db\u30c6\u30ad\u30b9\u30c8\u306b\u3001\u672a\u7d50\u5408\u306e\u63a5\u982d\u90e8 \"{2}\" \u3092\u6301\u3064\u5c5e\u6027\u30ce\u30fc\u30c9 \"{1}\" \u304c\u542b\u307e\u308c\u3066\u3044\u307e\u3059\u3002"
             },

        };

        return contents;
    }
}
