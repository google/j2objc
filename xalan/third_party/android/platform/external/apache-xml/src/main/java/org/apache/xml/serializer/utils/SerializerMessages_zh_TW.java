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
 * $Id: SerializerMessages_zh_TW.java 471981 2006-11-07 04:28:00Z minchau $
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
public class SerializerMessages_zh_TW extends ListResourceBundle {

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
                "\u8a0a\u606f\u9375 ''{0}'' \u4e0d\u5728\u8a0a\u606f\u985e\u5225 ''{1}'' \u4e2d" },

            {   MsgKey.BAD_MSGFORMAT,
                "\u8a0a\u606f\u985e\u5225 ''{1}'' \u4e2d\u7684\u8a0a\u606f ''{0}'' \u683c\u5f0f\u5316\u5931\u6557\u3002" },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "Serializer \u985e\u5225 ''{0}'' \u4e0d\u5be6\u4f5c org.xml.sax.ContentHandler\u3002" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "\u627e\u4e0d\u5230\u8cc7\u6e90 [ {0} ]\u3002\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "\u7121\u6cd5\u8f09\u5165\u8cc7\u6e90 [ {0} ]\uff1a{1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "\u7de9\u885d\u5340\u5927\u5c0f <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "\u5075\u6e2c\u5230\u7121\u6548\u7684 UTF-16 \u4ee3\u7406\uff1a{0}?" },

            {   MsgKey.ER_OIERROR,
                "IO \u932f\u8aa4" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "\u5728\u7522\u751f\u5b50\u9805\u7bc0\u9ede\u4e4b\u5f8c\uff0c\u6216\u5728\u7522\u751f\u5143\u7d20\u4e4b\u524d\uff0c\u4e0d\u53ef\u65b0\u589e\u5c6c\u6027 {0}\u3002\u5c6c\u6027\u6703\u88ab\u5ffd\u7565\u3002" },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "\u5b57\u9996 ''{0}'' \u7684\u540d\u7a31\u7a7a\u9593\u5c1a\u672a\u5ba3\u544a\u3002" },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "\u5c6c\u6027 ''{0}'' \u8d85\u51fa\u5143\u7d20\u5916\u3002" },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "\u540d\u7a31\u7a7a\u9593\u5ba3\u544a ''{0}''=''{1}'' \u8d85\u51fa\u5143\u7d20\u5916\u3002" },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "\u7121\u6cd5\u8f09\u5165 ''{0}''\uff08\u6aa2\u67e5 CLASSPATH\uff09\uff0c\u76ee\u524d\u53ea\u4f7f\u7528\u9810\u8a2d\u503c" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "\u8a66\u5716\u8f38\u51fa\u4e0d\u662f\u4ee5\u6307\u5b9a\u7684\u8f38\u51fa\u7de8\u78bc {1} \u5448\u73fe\u7684\u6574\u6578\u503c {0} \u7684\u5b57\u5143\u3002" },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "\u7121\u6cd5\u8f09\u5165\u8f38\u51fa\u65b9\u6cd5 ''{1}''\uff08\u6aa2\u67e5 CLASSPATH\uff09\u7684\u5167\u5bb9\u6a94 ''{0}''" },

            {   MsgKey.ER_INVALID_PORT,
                "\u7121\u6548\u7684\u57e0\u7de8\u865f" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "\u4e3b\u6a5f\u70ba\u7a7a\u503c\u6642\uff0c\u7121\u6cd5\u8a2d\u5b9a\u57e0" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "\u4e3b\u6a5f\u6c92\u6709\u5b8c\u6574\u7684\u4f4d\u5740" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "\u7db1\u8981\u4e0d\u662f conformant\u3002" },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "\u7121\u6cd5\u5f9e\u7a7a\u5b57\u4e32\u8a2d\u5b9a\u7db1\u8981" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "\u8def\u5f91\u5305\u542b\u7121\u6548\u7684\u8df3\u812b\u5b57\u5143" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "\u8def\u5f91\u5305\u542b\u7121\u6548\u7684\u5b57\u5143\uff1a{0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "\u7247\u6bb5\u5305\u542b\u7121\u6548\u7684\u5b57\u5143" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "\u8def\u5f91\u70ba\u7a7a\u503c\u6642\uff0c\u7121\u6cd5\u8a2d\u5b9a\u7247\u6bb5" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "\u53ea\u80fd\u5c0d\u901a\u7528\u7684 URI \u8a2d\u5b9a\u7247\u6bb5" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "\u5728 URI \u627e\u4e0d\u5230\u7db1\u8981" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "\u7121\u6cd5\u4ee5\u7a7a\u767d\u53c3\u6578\u8d77\u59cb\u8a2d\u5b9a URI" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "\u7247\u6bb5\u7121\u6cd5\u540c\u6642\u5728\u8def\u5f91\u548c\u7247\u6bb5\u4e2d\u6307\u5b9a" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "\u5728\u8def\u5f91\u53ca\u67e5\u8a62\u5b57\u4e32\u4e2d\u4e0d\u53ef\u6307\u5b9a\u67e5\u8a62\u5b57\u4e32" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "\u5982\u679c\u6c92\u6709\u6307\u5b9a\u4e3b\u6a5f\uff0c\u4e0d\u53ef\u6307\u5b9a\u57e0" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "\u5982\u679c\u6c92\u6709\u6307\u5b9a\u4e3b\u6a5f\uff0c\u4e0d\u53ef\u6307\u5b9a Userinfo" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "\u8b66\u544a\uff1a\u8f38\u51fa\u6587\u4ef6\u7684\u7248\u672c\u8981\u6c42\u662f ''{0}''\u3002\u672a\u652f\u63f4\u9019\u500b\u7248\u672c\u7684 XML\u3002\u8f38\u51fa\u6587\u4ef6\u7684\u7248\u672c\u6703\u662f ''1.0''\u3002" },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "\u7db1\u8981\u662f\u5fc5\u9700\u7684\uff01" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "\u50b3\u905e\u5230 SerializerFactory \u7684 Properties \u7269\u4ef6\u6c92\u6709 ''{0}'' \u5167\u5bb9\u3002" },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "\u8b66\u544a\uff1aJava \u57f7\u884c\u6642\u671f\u4e0d\u652f\u63f4\u7de8\u78bc ''{0}''\u3002" },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "\u7121\u6cd5\u8fa8\u8b58\u53c3\u6578 ''{0}''\u3002"},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "\u53ef\u8fa8\u8b58 ''{0}'' \u53c3\u6578\uff0c\u4f46\u6240\u8981\u6c42\u7684\u503c\u7121\u6cd5\u8a2d\u5b9a\u3002"},

             {MsgKey.ER_STRING_TOO_LONG,
             "\u7d50\u679c\u5b57\u4e32\u904e\u9577\uff0c\u7121\u6cd5\u7f6e\u5165 DOMString: ''{0}'' \u4e2d\u3002"},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "\u9019\u500b\u53c3\u6578\u540d\u7a31\u7684\u503c\u985e\u578b\u8207\u671f\u671b\u503c\u985e\u578b\u4e0d\u76f8\u5bb9\u3002"},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "\u8cc7\u6599\u8981\u5beb\u5165\u7684\u8f38\u51fa\u76ee\u7684\u5730\u70ba\u7a7a\u503c\u3002"},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "\u767c\u73fe\u4e0d\u652f\u63f4\u7684\u7de8\u78bc\u3002"},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "\u7bc0\u9ede\u7121\u6cd5\u5e8f\u5217\u5316\u3002"},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "CDATA \u5340\u6bb5\u5305\u542b\u4e00\u6216\u591a\u500b\u7d42\u6b62\u6a19\u8a18 ']]>'\u3002"},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "\u7121\u6cd5\u5efa\u7acb\u300c\u5f62\u5f0f\u5b8c\u6574\u300d\u6aa2\u67e5\u7a0b\u5f0f\u7684\u5be6\u4f8b\u3002Well-formed \u53c3\u6578\u96d6\u8a2d\u70ba true\uff0c\u4f46\u7121\u6cd5\u57f7\u884c\u5f62\u5f0f\u5b8c\u6574\u6aa2\u67e5\u3002"
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "\u7bc0\u9ede ''{0}'' \u5305\u542b\u7121\u6548\u7684 XML \u5b57\u5143\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "\u5728\u8a3b\u89e3\u4e2d\u767c\u73fe\u7121\u6548\u7684 XML \u5b57\u5143 (Unicode: 0x{0})\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "\u5728\u8655\u7406\u7a0b\u5e8f instructiondata \u4e2d\u767c\u73fe\u7121\u6548\u7684 XML \u5b57\u5143 (Unicode: 0x{0})\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "\u5728 CDATASection \u7684\u5167\u5bb9\u4e2d\u767c\u73fe\u7121\u6548\u7684 XML \u5b57\u5143 (Unicode: 0x{0})\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "\u5728\u7bc0\u9ede\u7684\u5b57\u5143\u8cc7\u6599\u5167\u5bb9\u4e2d\u767c\u73fe\u7121\u6548\u7684 XML \u5b57\u5143 (Unicode: 0x{0})\u3002"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "\u5728\u540d\u70ba ''{1}'' \u7684 ''{0}'' \u4e2d\u767c\u73fe\u7121\u6548\u7684 XML \u5b57\u5143\u3002"
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "\u8a3b\u89e3\u4e2d\u4e0d\u5141\u8a31\u4f7f\u7528\u5b57\u4e32 \"--\"\u3002"
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "\u8207\u5143\u7d20\u985e\u578b \"{0}\" \u76f8\u95dc\u806f\u7684\u5c6c\u6027 \"{1}\" \u503c\u4e0d\u53ef\u5305\u542b ''<'' \u5b57\u5143\u3002"
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "\u4e0d\u5141\u8a31\u4f7f\u7528\u672a\u5256\u6790\u7684\u5be6\u9ad4\u53c3\u7167 \"&{0};\"\u3002"
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "\u5c6c\u6027\u503c\u4e2d\u4e0d\u5141\u8a31\u4f7f\u7528\u5916\u90e8\u5be6\u9ad4\u53c3\u7167 \"&{0};\"\u3002"
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "\u5b57\u9996 \"{0}\" \u7121\u6cd5\u9023\u7d50\u5230\u540d\u7a31\u7a7a\u9593 \"{1}\"\u3002"
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "\u5143\u7d20 \"{0}\" \u7684\u672c\u7aef\u540d\u7a31\u662f\u7a7a\u503c\u3002"
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "\u5c6c\u6027 \"{0}\" \u7684\u672c\u7aef\u540d\u7a31\u662f\u7a7a\u503c\u3002"
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "\u5be6\u9ad4\u7bc0\u9ede \"{0}\" \u7684\u53d6\u4ee3\u6587\u5b57\u5305\u542b\u9644\u6709\u5df2\u5207\u65b7\u9023\u7d50\u5b57\u9996 \"{2}\" \u7684\u5143\u7d20\u7bc0\u9ede \"{1}\"\u3002"
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "\u5be6\u9ad4\u7bc0\u9ede \"{0}\" \u7684\u53d6\u4ee3\u6587\u5b57\u5305\u542b\u9644\u6709\u5df2\u5207\u65b7\u9023\u7d50\u5b57\u9996 \"{2}\" \u7684\u5c6c\u6027\u7bc0\u9ede \"{1}\"\u3002"
             },

        };

        return contents;
    }
}
