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
 * $Id: SerializerMessages_zh.java,v 1.7 2005/03/07 20:34:36 minchau Exp $
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
public class SerializerMessages_zh extends ListResourceBundle {

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
                "\u6d88\u606f\u5bc6\u94a5\u201c{0}\u201d\u4e0d\u5728\u6d88\u606f\u7c7b\u201c{1}\u201d\u4e2d" },

            {   MsgKey.BAD_MSGFORMAT,
                "\u6d88\u606f\u7c7b\u201c{1}\u201d\u4e2d\u7684\u6d88\u606f\u201c{0}\u201d\u7684\u683c\u5f0f\u65e0\u6548\u3002" },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "\u4e32\u884c\u5668\u7c7b\u201c{0}\u201d\u4e0d\u80fd\u5b9e\u73b0 org.xml.sax.ContentHandler\u3002" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "\u627e\u4e0d\u5230\u8d44\u6e90 [ {0} ]\u3002\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "\u8d44\u6e90 [ {0} ] \u65e0\u6cd5\u88c5\u5165\uff1a{1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "\u7f13\u51b2\u533a\u5927\u5c0f <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "\u68c0\u6d4b\u5230\u65e0\u6548\u7684 UTF-16 \u8d85\u5927\u5b57\u7b26\u96c6\uff1a{0}\uff1f" },

            {   MsgKey.ER_OIERROR,
                "IO \u9519\u8bef" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "\u5728\u751f\u6210\u5b50\u8282\u70b9\u4e4b\u540e\u6216\u5728\u751f\u6210\u5143\u7d20\u4e4b\u524d\u65e0\u6cd5\u6dfb\u52a0\u5c5e\u6027 {0}\u3002\u5c06\u5ffd\u7565\u5c5e\u6027\u3002" },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "\u5c1a\u672a\u58f0\u660e\u524d\u7f00\u201c{0}\u201d\u7684\u540d\u79f0\u7a7a\u95f4\u3002" },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "\u5c5e\u6027\u201c{0}\u201d\u5728\u5143\u7d20\u5916\u3002" },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "\u540d\u79f0\u7a7a\u95f4\u58f0\u660e\u201c{0}\u201d=\u201c{1}\u201d\u5728\u5143\u7d20\u5916\u3002" },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "\u65e0\u6cd5\u88c5\u5165\u201c{0}\u201d\uff08\u68c0\u67e5 CLASSPATH\uff09\uff0c\u73b0\u5728\u53ea\u4f7f\u7528\u7f3a\u7701\u503c" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "\u5c1d\u8bd5\u8f93\u51fa\u6574\u6570\u503c {0}\uff08\u5b83\u4e0d\u662f\u4ee5\u6307\u5b9a\u7684 {1} \u8f93\u51fa\u7f16\u7801\u8868\u793a\uff09\u7684\u5b57\u7b26\u3002" },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "\u65e0\u6cd5\u4e3a\u8f93\u51fa\u65b9\u6cd5\u201c{1}\u201d\u88c5\u5165\u5c5e\u6027\u6587\u4ef6\u201c{0}\u201d\uff08\u68c0\u67e5 CLASSPATH\uff09" },

            {   MsgKey.ER_INVALID_PORT,
                "\u7aef\u53e3\u53f7\u65e0\u6548" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "\u4e3b\u673a\u4e3a\u7a7a\u65f6\uff0c\u65e0\u6cd5\u8bbe\u7f6e\u7aef\u53e3" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "\u4e3b\u673a\u4e0d\u662f\u683c\u5f0f\u6b63\u786e\u7684\u5730\u5740" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "\u6a21\u5f0f\u4e0d\u4e00\u81f4\u3002" },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "\u65e0\u6cd5\u4ece\u7a7a\u5b57\u7b26\u4e32\u8bbe\u7f6e\u6a21\u5f0f" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "\u8def\u5f84\u5305\u542b\u65e0\u6548\u7684\u8f6c\u4e49\u5e8f\u5217" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "\u8def\u5f84\u5305\u542b\u65e0\u6548\u7684\u5b57\u7b26\uff1a{0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "\u7247\u6bb5\u5305\u542b\u65e0\u6548\u7684\u5b57\u7b26" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "\u8def\u5f84\u4e3a\u7a7a\u65f6\uff0c\u65e0\u6cd5\u8bbe\u7f6e\u7247\u6bb5" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "\u53ea\u80fd\u4e3a\u7c7b\u5c5e URI \u8bbe\u7f6e\u7247\u6bb5" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "URI \u4e2d\u627e\u4e0d\u5230\u4efb\u4f55\u6a21\u5f0f" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "\u4e0d\u80fd\u4ee5\u7a7a\u53c2\u6570\u521d\u59cb\u5316 URI" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "\u8def\u5f84\u548c\u7247\u6bb5\u4e2d\u90fd\u4e0d\u80fd\u6307\u5b9a\u7247\u6bb5" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "\u8def\u5f84\u548c\u67e5\u8be2\u5b57\u7b26\u4e32\u4e2d\u4e0d\u80fd\u6307\u5b9a\u67e5\u8be2\u5b57\u7b26\u4e32" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "\u5982\u679c\u6ca1\u6709\u6307\u5b9a\u4e3b\u673a\uff0c\u5219\u4e0d\u53ef\u4ee5\u6307\u5b9a\u7aef\u53e3" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "\u5982\u679c\u6ca1\u6709\u6307\u5b9a\u4e3b\u673a\uff0c\u5219\u4e0d\u53ef\u4ee5\u6307\u5b9a\u7528\u6237\u4fe1\u606f" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "\u8b66\u544a\uff1a\u8981\u6c42\u8f93\u51fa\u6587\u6863\u7684\u7248\u672c\u662f\u201c{0}\u201d\u3002\u4e0d\u652f\u6301\u6b64 XML \u7248\u672c\u3002\u8f93\u51fa\u6587\u6863\u7684\u7248\u672c\u5c06\u4f1a\u662f\u201c1.0\u201d\u3002" },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "\u6a21\u5f0f\u662f\u5fc5\u9700\u7684\uff01" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "\u4f20\u9012\u7ed9 SerializerFactory \u7684 Properties \u5bf9\u8c61\u4e0d\u5177\u6709\u5c5e\u6027\u201c{0}\u201d\u3002" },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "\u672a\u8bc6\u522b\u51fa\u53c2\u6570\u201c{0}\u201d\u3002"},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "\u5df2\u8bc6\u522b\u51fa\u53c2\u6570\u201c{0}\u201d\uff0c\u4f46\u65e0\u6cd5\u8bbe\u7f6e\u8bf7\u6c42\u7684\u503c\u3002"},

             {MsgKey.ER_STRING_TOO_LONG,
             "\u4ea7\u751f\u7684\u5b57\u7b26\u4e32\u8fc7\u957f\u4e0d\u80fd\u88c5\u5165 DOMString\uff1a\u201c{0}\u201d\u3002"},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "\u6b64\u53c2\u6570\u540d\u79f0\u7684\u503c\u7c7b\u578b\u4e0e\u671f\u671b\u7684\u503c\u7c7b\u578b\u4e0d\u517c\u5bb9\u3002"},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "\u5c06\u8981\u5199\u5165\u6570\u636e\u7684\u8f93\u51fa\u76ee\u6807\u4e3a\u7a7a\u3002"},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "\u9047\u5230\u4e0d\u53d7\u652f\u6301\u7684\u7f16\u7801\u3002"},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "\u65e0\u6cd5\u5c06\u8282\u70b9\u5e8f\u5217\u5316\u3002 "},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "CDATA \u90e8\u5206\u5305\u542b\u4e00\u4e2a\u6216\u591a\u4e2a\u7ec8\u6b62\u6807\u8bb0\u201c]]>\u201d\u3002"},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "\u65e0\u6cd5\u521b\u5efa\u683c\u5f0f\u6b63\u786e\u6027\u68c0\u67e5\u5668\u7684\u5b9e\u4f8b\u3002\u201c\u683c\u5f0f\u6b63\u786e\u201d\u53c2\u6570\u5df2\u8bbe\u7f6e\u4e3a true\uff0c\u4f46\u65e0\u6cd5\u6267\u884c\u683c\u5f0f\u6b63\u786e\u6027\u68c0\u67e5\u3002"
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "\u8282\u70b9\u201c{0}\u201d\u5305\u542b\u65e0\u6548\u7684 XML \u5b57\u7b26\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "\u5728\u6ce8\u91ca\u4e2d\u627e\u5230\u65e0\u6548\u7684 XML \u5b57\u7b26 (Unicode: 0x''{0})''\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "\u5728\u5904\u7406\u6307\u4ee4\u6570\u636e\u4e2d\u627e\u5230\u65e0\u6548\u7684 XML \u5b57\u7b26 (Unicode: 0x''{0})''\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "\u5728 CDATA \u90e8\u5206\u7684\u5185\u5bb9\u4e2d\u627e\u5230\u65e0\u6548\u7684 XML \u5b57\u7b26 (Unicode: 0x''{0})''\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "\u5728\u8282\u70b9\u7684\u5b57\u7b26\u6570\u636e\u5185\u5bb9\u4e2d\u627e\u5230\u65e0\u6548\u7684 XML \u5b57\u7b26 (Unicode: 0x''{0})''\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "\u540d\u79f0\u4e3a\u201c{1})\u201d\u7684\u201c{0})\u201d\u4e2d\u627e\u5230\u65e0\u6548\u7684 XML \u5b57\u7b26\u3002"
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "\u6ce8\u91ca\u4e2d\u4e0d\u5141\u8bb8\u6709\u5b57\u7b26\u4e32\u201c--\u201d\u3002"
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "\u4e0e\u5143\u7d20\u7c7b\u578b\u201c{0}\u201d\u5173\u8054\u7684\u5c5e\u6027\u201c{1}\u201d\u7684\u503c\u4e0d\u5f97\u5305\u542b\u201c<\u201d\u5b57\u7b26\u3002"
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "\u4e0d\u5141\u8bb8\u6709\u672a\u89e3\u6790\u7684\u5b9e\u4f53\u5f15\u7528\u201c&{0};\u201d\u3002"
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "\u5c5e\u6027\u503c\u4e2d\u4e0d\u5141\u8bb8\u6709\u5916\u90e8\u5b9e\u4f53\u5f15\u7528\u201c&{0};\u201d\u3002"
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "\u524d\u7f00\u201c{0}\u201d\u4e0d\u80fd\u7ed1\u5b9a\u5230\u540d\u79f0\u7a7a\u95f4\u201c{1}\u201d\u3002"
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "\u5143\u7d20\u201c{0}\u201d\u7684\u5c40\u90e8\u540d\u4e3a\u7a7a\u3002"
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "\u5c5e\u6027\u201c{0}\u201d\u7684\u5c40\u90e8\u540d\u4e3a\u7a7a\u3002"
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "\u5b9e\u4f53\u8282\u70b9\u201c{0}\u201d\u7684\u66ff\u4ee3\u6587\u672c\u4e2d\u5305\u542b\u5143\u7d20\u8282\u70b9\u201c{1}\u201d\uff0c\u8be5\u8282\u70b9\u5177\u6709\u672a\u7ed1\u5b9a\u7684\u524d\u7f00\u201c{2}\u201d\u3002"
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "\u5b9e\u4f53\u8282\u70b9\u201c{0}\u201d\u7684\u66ff\u4ee3\u6587\u672c\u4e2d\u5305\u542b\u5c5e\u6027\u8282\u70b9\u201c{1}\u201d\uff0c\u8be5\u8282\u70b9\u5177\u6709\u672a\u7ed1\u5b9a\u7684\u524d\u7f00\u201c{2}\u201d\u3002"
             },

        };

        return contents;
    }
}
