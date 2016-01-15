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
 * $Id: SerializerMessages_tr.java 471981 2006-11-07 04:28:00Z minchau $
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
public class SerializerMessages_tr extends ListResourceBundle {

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
                "''{0}'' ileti anahtar\u0131 ''{1}'' ileti s\u0131n\u0131f\u0131nda yok" },

            {   MsgKey.BAD_MSGFORMAT,
                "''{1}'' ileti s\u0131n\u0131f\u0131ndaki ''{0}'' iletisinin bi\u00e7imi ba\u015far\u0131s\u0131z." },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "''{0}'' diziselle\u015ftirme s\u0131n\u0131f\u0131 org.xml.sax.ContentHandler s\u0131n\u0131f\u0131n\u0131 ger\u00e7ekle\u015ftirmiyor." },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "Kaynak [ {0} ] bulunamad\u0131.\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "Kaynak [ {0} ] y\u00fckleyemedi: {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "Arabellek b\u00fcy\u00fckl\u00fc\u011f\u00fc <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "UTF-16 yerine kullan\u0131lan de\u011fer ge\u00e7ersiz: {0} ?" },

            {   MsgKey.ER_OIERROR,
                "G\u00c7 hatas\u0131" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "Alt d\u00fc\u011f\u00fcmlerden sonra ya da bir \u00f6\u011fe \u00fcretilmeden \u00f6nce {0} \u00f6zniteli\u011fi eklenemez.  \u00d6znitelik yoksay\u0131lacak." },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "''{0}'' \u00f6nekine ili\u015fkin ad alan\u0131 bildirilmedi." },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "''{0}'' \u00f6zniteli\u011fi \u00f6\u011fenin d\u0131\u015f\u0131nda." },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "''{0}''=''{1}'' ad alan\u0131 bildirimi \u00f6\u011fenin d\u0131\u015f\u0131nda." },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "''{0}'' y\u00fcklenemedi (CLASSPATH de\u011fi\u015fkeninizi inceleyin), yaln\u0131zca varsay\u0131lanlar kullan\u0131l\u0131yor" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "Belirtilen {1} \u00e7\u0131k\u0131\u015f kodlamas\u0131nda g\u00f6sterilmeyen {0} t\u00fcmlev de\u011feri karakteri \u00e7\u0131k\u0131\u015f giri\u015fimi." },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "''{1}'' \u00e7\u0131k\u0131\u015f y\u00f6ntemi i\u00e7in ''{0}'' \u00f6zellik dosyas\u0131 y\u00fcklenemedi (CLASSPATH de\u011fi\u015fkenini inceleyin)" },

            {   MsgKey.ER_INVALID_PORT,
                "Kap\u0131 numaras\u0131 ge\u00e7ersiz" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "Anasistem bo\u015f de\u011ferliyken kap\u0131 tan\u0131mlanamaz" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "Anasistem do\u011fru bi\u00e7imli bir adres de\u011fil" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "\u015eema uyumlu de\u011fil." },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "Bo\u015f de\u011ferli dizgiden \u015fema tan\u0131mlanamaz" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "Yol ge\u00e7ersiz ka\u00e7\u0131\u015f dizisi i\u00e7eriyor" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "Yol ge\u00e7ersiz karakter i\u00e7eriyor: {0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "Par\u00e7a ge\u00e7ersiz karakter i\u00e7eriyor" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "Yol bo\u015f de\u011ferliyken par\u00e7a tan\u0131mlanamaz" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "Par\u00e7a yaln\u0131zca soysal URI i\u00e7in tan\u0131mlanabilir" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "URI i\u00e7inde \u015fema bulunamad\u0131" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "Bo\u015f de\u011fi\u015ftirgelerle URI kullan\u0131ma haz\u0131rlanamaz" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "Par\u00e7a hem yolda, hem de par\u00e7ada belirtilemez" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "Yol ve sorgu dizgisinde sorgu dizgisi belirtilemez" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "Anasistem belirtilmediyse kap\u0131 belirtilemez" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "Anasistem belirtilmediyse kullan\u0131c\u0131 bilgisi belirtilemez" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "Uyar\u0131:  \u00c7\u0131k\u0131\u015f belgesinin s\u00fcr\u00fcm\u00fcn\u00fcn ''{0}'' olmas\u0131 isteniyor.  Bu XML s\u00fcr\u00fcm\u00fc desteklenmez.  \u00c7\u0131k\u0131\u015f dosyas\u0131n\u0131n s\u00fcr\u00fcm\u00fc ''1.0'' olacak." },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "\u015eema gerekli!" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "SerializerFactory''ye ge\u00e7irilen Properties nesnesinin bir ''{0}'' \u00f6zelli\u011fi yok." },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "Uyar\u0131: ''{0}'' kodlamas\u0131 Java Runtime taraf\u0131ndan desteklenmiyor." },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "''{0}'' de\u011fi\u015ftirgesi tan\u0131nm\u0131yor."},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "''{0}'' de\u011fi\u015ftirgesi tan\u0131n\u0131yor, ancak istenen de\u011fer tan\u0131mlanam\u0131yor."},

             {MsgKey.ER_STRING_TOO_LONG,
             "Sonu\u00e7 dizgisi DOMString i\u00e7in \u00e7ok uzun: ''{0}''."},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "Bu de\u011fi\u015ftirge ad\u0131na ili\u015fkin de\u011fer tipi, beklenen de\u011fer tipiyle uyumlu de\u011fil."},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "Yaz\u0131lacak verilerin \u00e7\u0131k\u0131\u015f hedefi bo\u015f de\u011ferli."},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "Desteklenmeyen bir kodlama saptand\u0131."},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "D\u00fc\u011f\u00fcm diziselle\u015ftirilemedi."},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "CDATA k\u0131sm\u0131nda bir ya da daha \u00e7ok ']]>' sonland\u0131rma imleyicisi var."},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "Well-Formedness denet\u015feyicisinin somut \u00f6rne\u011fi yarat\u0131lamad\u0131.  well-formed de\u011fi\u015ftirgesi true de\u011ferine ayarland\u0131, ancak do\u011fru bi\u00e7im denetimi ger\u00e7ekle\u015ftirilemiyor."
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "''{0}'' d\u00fc\u011f\u00fcm\u00fc ge\u00e7ersiz XML karakterleri i\u00e7eriyor."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "A\u00e7\u0131klamada ge\u00e7ersiz bir XML karakteri (Unicode: 0x{0}) saptand\u0131."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "\u0130\u015fleme y\u00f6nergesi verilerinde ge\u00e7ersiz bir XML karakteri (Unicode: 0x{0}) saptand\u0131."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "CDATASection i\u00e7eri\u011finde ge\u00e7ersiz bir XML karakteri (Unicode: 0x{0}) saptand\u0131."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "D\u00fc\u011f\u00fcm\u00fcn karakter verileri i\u00e7eri\u011finde ge\u00e7ersiz bir XML karakteri (Unicode: 0x{0}) saptand\u0131."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "''{1}'' adl\u0131 {0} d\u00fc\u011f\u00fcm\u00fcnde ge\u00e7ersiz XML karakteri saptand\u0131."
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "A\u00e7\u0131klamalar i\u00e7inde \"--\" dizgisine izin verilmez."
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "\"{0}\" \u00f6\u011fe tipiyle ili\u015fkilendirilen \"{1}\" \u00f6zniteli\u011finin de\u011feri ''<'' karakteri i\u00e7ermemelidir."
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "\"&{0};\" ayr\u0131\u015ft\u0131r\u0131lmam\u0131\u015f varl\u0131k ba\u015fvurusuna izin verilmez."
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "\u00d6znitelik de\u011ferinde \"&{0};\" d\u0131\u015f varl\u0131k ba\u015fvurusuna izin verilmez."
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "\"{0}\" \u00f6neki \"{1}\" ad alan\u0131na ba\u011flanam\u0131yor."
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "\"{0}\" \u00f6\u011fesinin yerel ad\u0131 bo\u015f de\u011ferli."
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "\"{0}\" \u00f6zniteli\u011finin yerel ad\u0131 bo\u015f de\u011ferli."
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "\"{0}\" varl\u0131k d\u00fc\u011f\u00fcm\u00fcn\u00fcn yerine koyma metninde, ba\u011flanmam\u0131\u015f \"{2}\" \u00f6neki bulunan bir \u00f6\u011fe d\u00fc\u011f\u00fcm\u00fc (\"{1}\") var."
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "\"{0}\" varl\u0131k d\u00fc\u011f\u00fcm\u00fcn\u00fcn yerine koyma metninde, ba\u011flanmam\u0131\u015f \"{2}\" \u00f6neki bulunan bir \u00f6znitelik d\u00fc\u011f\u00fcm\u00fc (\"{1}\") var."
             },

        };

        return contents;
    }
}
