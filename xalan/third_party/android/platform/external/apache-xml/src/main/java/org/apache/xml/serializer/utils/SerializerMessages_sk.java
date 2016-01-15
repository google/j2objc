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
 * $Id: SerializerMessages_sk.java,v 1.7 2005/03/07 20:34:36 minchau Exp $
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
public class SerializerMessages_sk extends ListResourceBundle {

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
                "K\u013e\u00fa\u010d spr\u00e1vy ''{0}'' sa nenach\u00e1dza v triede spr\u00e1v ''{1}''" },

            {   MsgKey.BAD_MSGFORMAT,
                "Zlyhal form\u00e1t spr\u00e1vy ''{0}'' v triede spr\u00e1v ''{1}''." },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "Trieda serializ\u00e1tora ''{0}'' neimplementuje org.xml.sax.ContentHandler." },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "Prostriedok [ {0} ] nemohol by\u0165 n\u00e1jden\u00fd.\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "Prostriedok [ {0} ] sa nedal na\u010d\u00edta\u0165: {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "Ve\u013ekos\u0165 vyrovn\u00e1vacej pam\u00e4te <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "Bolo zisten\u00e9 neplatn\u00e9 nahradenie UTF-16: {0} ?" },

            {   MsgKey.ER_OIERROR,
                "chyba IO" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "Nie je mo\u017en\u00e9 prida\u0165 atrib\u00fat {0} po uzloch potomka alebo pred vytvoren\u00edm elementu.  Atrib\u00fat bude ignorovan\u00fd." },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "N\u00e1zvov\u00fd priestor pre predponu ''{0}'' nebol deklarovan\u00fd." },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "Atrib\u00fat ''{0}'' je mimo prvku." },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "Deklar\u00e1cia n\u00e1zvov\u00e9ho priestoru ''{0}''=''{1}'' je mimo prvku." },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "Nebolo mo\u017en\u00e9 zavies\u0165 ''{0}'' (skontrolujte CLASSPATH), teraz sa pou\u017e\u00edvaj\u00fa iba \u0161tandardn\u00e9 nastavenia" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "Pokus o v\u00fdstup znaku integr\u00e1lnej hodnoty {0}, ktor\u00e1 nie je reprezentovan\u00e1 v zadanom v\u00fdstupnom k\u00f3dovan\u00ed {1}." },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "Nebolo mo\u017en\u00e9 zavies\u0165 s\u00fabor vlastnost\u00ed ''{0}'' pre v\u00fdstupn\u00fa met\u00f3du ''{1}'' (skontrolujte CLASSPATH)" },

            {   MsgKey.ER_INVALID_PORT,
                "Neplatn\u00e9 \u010d\u00edslo portu" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "Nem\u00f4\u017ee by\u0165 stanoven\u00fd port, ak je hostite\u013e nulov\u00fd" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "Hostite\u013e nie je spr\u00e1vne form\u00e1tovan\u00e1 adresa" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "Nezhodn\u00e1 sch\u00e9ma." },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "Nie je mo\u017en\u00e9 stanovi\u0165 sch\u00e9mu z nulov\u00e9ho re\u0165azca" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "Cesta obsahuje neplatn\u00fa \u00fanikov\u00fa sekvenciu" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "Cesta obsahuje neplatn\u00fd znak: {0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "Fragment obsahuje neplatn\u00fd znak" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "Ak je cesta nulov\u00e1, nem\u00f4\u017ee by\u0165 stanoven\u00fd fragment" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "Fragment m\u00f4\u017ee by\u0165 stanoven\u00fd len pre v\u0161eobecn\u00e9 URI" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "V URI nebola n\u00e1jden\u00e1 \u017eiadna sch\u00e9ma" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "Nie je mo\u017en\u00e9 inicializova\u0165 URI s pr\u00e1zdnymi parametrami" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "Fragment nem\u00f4\u017ee by\u0165 zadan\u00fd v ceste, ani vo fragmente" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "Re\u0165azec dotazu nem\u00f4\u017ee by\u0165 zadan\u00fd v ceste a re\u0165azci dotazu" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "Ak nebol zadan\u00fd hostite\u013e, mo\u017eno nebol zadan\u00fd port" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "Ak nebol zadan\u00fd hostite\u013e, mo\u017eno nebolo zadan\u00e9 userinfo" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "Varovanie:  Verzia v\u00fdstupn\u00e9ho dokumentu mus\u00ed by\u0165 povinne ''{0}''.  T\u00e1to verzia XML nie je podporovan\u00e1.  Verzia v\u00fdstupn\u00e9ho dokumentu bude ''1.0''." },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "Je po\u017eadovan\u00e1 sch\u00e9ma!" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "Objekt Properties, ktor\u00fd pre\u0161iel do SerializerFactory, nem\u00e1 vlastnos\u0165 ''{0}''." },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "Varovanie:  Java runtime nepodporuje k\u00f3dovanie ''{0}''." },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "Parameter ''{0}'' nebol rozpoznan\u00fd."},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "Parameter ''{0}'' bol rozpoznan\u00fd, ale vy\u017eadovan\u00e1 hodnota sa ned\u00e1 nastavi\u0165."},

             {MsgKey.ER_STRING_TOO_LONG,
             "V\u00fdsledn\u00fd re\u0165azec je pr\u00edli\u0161 dlh\u00fd a nezmest\u00ed sa do DOMString: ''{0}''."},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "Typ hodnoty pre tento n\u00e1zov parametra je nekompatibiln\u00fd s o\u010dak\u00e1van\u00fdm typom hodnoty."},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "Cie\u013e v\u00fdstupu pre zap\u00edsanie \u00fadajov bol null."},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "Bolo zaznamenan\u00e9 nepodporovan\u00e9 k\u00f3dovanie."},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "Uzol nebolo mo\u017en\u00e9 serializova\u0165."},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "\u010cas\u0165 CDATA obsahuje jeden alebo viacer\u00e9 ozna\u010dova\u010de konca ']]>'."},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "Nebolo mo\u017en\u00e9 vytvori\u0165 in\u0161tanciu kontrol\u00f3ra Well-Formedness.  Parameter well-formed bol nastaven\u00fd na hodnotu true, ale kontrola well-formedness sa ned\u00e1 vykona\u0165."
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "Uzol ''{0}'' obsahuje neplatn\u00e9 znaky XML."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "V koment\u00e1ri bol n\u00e1jden\u00fd neplatn\u00fd znak XML (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "Pri spracovan\u00ed d\u00e1t in\u0161trukci\u00ed sa na\u0161iel neplatn\u00fd znak XML (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "V obsahu CDATASection sa na\u0161iel neplatn\u00fd znak XML (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "V obsahu znakov\u00fdch d\u00e1t uzla sa na\u0161iel neplatn\u00fd znak XML (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "V uzle {0} s n\u00e1zvom ''{1}'' sa na\u0161iel neplatn\u00fd znak XML."
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "Re\u0165azec \"--\" nie je povolen\u00fd v r\u00e1mci koment\u00e1rov."
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "Hodnota atrib\u00fatu \"{1}\", ktor\u00e1 je priraden\u00e1 k prvku typu \"{0}\", nesmie obsahova\u0165 znak ''<''."
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "Neanalyzovan\u00fd odkaz na entitu \"&{0};\" nie je povolen\u00fd."
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "Odkaz na extern\u00fa entitu \"&{0};\" nie je povolen\u00fd v hodnote atrib\u00fatu."
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "Predpona \"{0}\" nem\u00f4\u017ee by\u0165 naviazan\u00e1 na n\u00e1zvov\u00fd priestor \"{1}\"."
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "Lok\u00e1lny n\u00e1zov prvku \"{0}\" je null."
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "Lok\u00e1lny n\u00e1zov atrib\u00fatu \"{0}\" je null."
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "N\u00e1hradn\u00fd text pre uzol entity \"{0}\" obsahuje uzol prvku \"{1}\" s nenaviazanou predponou \"{2}\"."
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "N\u00e1hradn\u00fd text uzla entity \"{0}\" obsahuje uzol atrib\u00fatu \"{1}\" s nenaviazanou predponou \"{2}\"."
             },

        };

        return contents;
    }
}
