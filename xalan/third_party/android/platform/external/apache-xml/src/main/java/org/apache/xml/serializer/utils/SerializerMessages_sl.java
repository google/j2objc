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
 * $Id: SerializerMessages_sl.java,v 1.7 2005/03/07 20:34:36 minchau Exp $
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
public class SerializerMessages_sl extends ListResourceBundle {

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
                "Klju\u010d sporo\u010dila ''{0}'' ni v rezredu sporo\u010dila ''{1}''" },

            {   MsgKey.BAD_MSGFORMAT,
                "Format sporo\u010dila ''{0}'' v razredu sporo\u010dila ''{1}'' je spodletel." },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "Razred serializerja ''{0}'' ne izvede org.xml.sax.ContentHandler." },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "Vira [ {0} ] ni mogo\u010de najti.\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "Sredstva [ {0} ] ni bilo mogo\u010de nalo\u017eiti: {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "Velikost medpomnilnika <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "Zaznan neveljaven nadomestek UTF-16: {0} ?" },

            {   MsgKey.ER_OIERROR,
                "Napaka V/I" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "Atributa {0} ne morem dodati za podrejenimi vozli\u0161\u010di ali pred izdelavo elementa.  Atribut bo prezrt." },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "Imenski prostor za predpono ''{0}'' ni bil naveden." },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "Atribut ''{0}'' je zunaj elementa." },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "Deklaracija imenskega prostora ''{0}''=''{1}'' je zunaj elementa." },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "Ni bilo mogo\u010de nalo\u017eiti ''{0}'' (preverite CLASSPATH), trenutno se uporabljajo samo privzete vrednosti" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "Poskus izpisa znaka integralne vrednosti {0}, ki v navedenem izhodnem kodiranju {1} ni zastopan." },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "Datoteke z lastnostmi ''{0}'' ni bilo mogo\u010de nalo\u017eiti za izhodno metodo ''{1}'' (preverite CLASSPATH)" },

            {   MsgKey.ER_INVALID_PORT,
                "Neveljavna \u0161tevilka vrat" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "Ko je gostitelj NULL, nastavitev vrat ni mogo\u010da" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "Naslov gostitelja ni pravilno oblikovan" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "Shema ni skladna." },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "Ni mogo\u010de nastaviti sheme iz niza NULL" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "Pot vsebuje neveljavno zaporedje za izhod" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "Pot vsebuje neveljaven znak: {0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "Fragment vsebuje neveljaven znak" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "Ko je pot NULL, nastavitev fragmenta ni mogo\u010da" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "Fragment je lahko nastavljen samo za splo\u0161ni URI" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "Ne najdem sheme v URI" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "Ni mogo\u010de inicializirati URI-ja s praznimi parametri" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "Fragment ne more biti hkrati naveden v poti in v fragmentu" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "Poizvedbeni niz ne more biti naveden v nizu poti in poizvedbenem nizu" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "Vrata ne morejo biti navedena, \u010de ni naveden gostitelj" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "Informacije o uporabniku ne morejo biti navedene, \u010de ni naveden gostitelj" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "Opozorilo: Zahtevana razli\u010dica izhodnega dokumenta je ''{0}''.  Ta razli\u010dica XML ni podprta.  Razli\u010dica izhodnega dokumenta bo ''1.0''." },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "Zahtevana je shema!" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "Predmet Properties (lastnosti), ki je prene\u0161en v SerializerFactory, nima lastnosti ''{0}''." },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "Opozorilo:  Izvajalno okolje Java ne podpira kodiranja ''{0}''." },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "Parameter ''{0}'' ni prepoznan."},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "Parameter ''{0}'' je prepoznan, vendar pa zahtevane vrednosti ni mogo\u010de nastaviti."},

             {MsgKey.ER_STRING_TOO_LONG,
             "Nastali niz je predolg za DOMString: ''{0}''."},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "Tip vrednosti za to ime parametra je nezdru\u017eljiv s pri\u010dakovanim tipom vrednosti."},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "Izhodno mesto za vpisovanje podatkov je bilo ni\u010d."},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "Odkrito je nepodprto kodiranje."},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "Vozli\u0161\u010da ni mogo\u010de serializirati."},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "Odsek CDATA vsebuje enega ali ve\u010d ozna\u010devalnikov prekinitve ']]>'."},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "Primerka preverjevalnika Well-Formedness ni bilo mogo\u010de ustvariti.  Parameter well-formed je bil nastavljen na True, ampak ni mogo\u010de preveriti well-formedness."
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "Vozli\u0161\u010de ''{0}'' vsebuje neveljavne znake XML."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "V komentarju je bil najden neveljaven XML znak (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "V podatkih navodila za obdelavo je bil najden neveljaven znak XML (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "V vsebini odseka CDATASection je bil najden neveljaven znak XML (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "V podatkovni vsebini znaka vozli\u0161\u010da je bil najden neveljaven znak XML (Unicode: 0x{0})."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "V vozli\u0161\u010du {0} z imenom ''{1}'' je bil najden neveljaven znak XML."
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "Niz \"--\" ni dovoljen v komentarjih."
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "Vrednost atributa \"{1}\", ki je povezan s tipom elementa \"{0}\", ne sme vsebovati znaka ''<''."
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "Neraz\u010dlenjeni sklic entitete \"&{0};\" ni dovoljen."
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "Zunanji sklic entitete \"&{0};\" ni dovoljen v vrednosti atributa."
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "Predpona \"{0}\" ne more biti povezana z imenskim prostorom \"{1}\"."
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "Lokalno ime elementa \"{0}\" je ni\u010d."
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "Lokalno ime atributa \"{0}\" je ni\u010d."
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "Besedilo za zamenjavo za vozli\u0161\u010de entitete \"{0}\" vsebuje vozli\u0161\u010de elementa \"{1}\" z nevezano predpono \"{2}\"."
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "Besedilo za zamenjavo za vozli\u0161\u010de entitete \"{0}\" vsebuje vozli\u0161\u010de atributa \"{1}\" z nevezano predpono \"{2}\"."
             },

        };

        return contents;
    }
}
