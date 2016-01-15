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
 * $Id: SerializerMessages_de.java 471981 2006-11-07 04:28:00Z minchau $
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
public class SerializerMessages_de extends ListResourceBundle {

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
                "Der Nachrichtenschl\u00fcssel ''{0}'' ist nicht in der Nachrichtenklasse ''{1}'' enthalten." },

            {   MsgKey.BAD_MSGFORMAT,
                "Das Format der Nachricht ''{0}'' in der Nachrichtenklasse ''{1}'' ist fehlgeschlagen." },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "Die Parallel-Seriell-Umsetzerklasse ''{0}'' implementiert org.xml.sax.ContentHandler nicht." },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "Die Ressource [ {0} ] konnte nicht gefunden werden.\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "Die Ressource [ {0} ] konnte nicht geladen werden: {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "Puffergr\u00f6\u00dfe <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "Ung\u00fcltige UTF-16-Ersetzung festgestellt: {0} ?" },

            {   MsgKey.ER_OIERROR,
                "E/A-Fehler" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "Attribut {0} kann nicht nach Kindknoten oder vor dem Erstellen eines Elements hinzugef\u00fcgt werden.  Das Attribut wird ignoriert." },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "Der Namensbereich f\u00fcr Pr\u00e4fix ''{0}'' wurde nicht deklariert." },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "Attribut ''{0}'' befindet sich nicht in einem Element." },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "Namensbereichdeklaration ''{0}''=''{1}'' befindet sich nicht in einem Element." },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "''{0}'' konnte nicht geladen werden (CLASSPATH pr\u00fcfen). Es werden die Standardwerte verwendet." },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "Es wurde versucht, ein Zeichen des Integralwerts {0} auszugeben, der nicht in der angegebenen Ausgabeverschl\u00fcsselung von {1} dargestellt ist." },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "Die Merkmaldatei ''{0}'' konnte f\u00fcr die Ausgabemethode ''{1}'' nicht geladen werden (CLASSPATH pr\u00fcfen)" },

            {   MsgKey.ER_INVALID_PORT,
                "Ung\u00fcltige Portnummer" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "Der Port kann nicht festgelegt werden, wenn der Host gleich Null ist." },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "Der Host ist keine syntaktisch korrekte Adresse." },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "Das Schema ist nicht angepasst." },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "Schema kann nicht von Nullzeichenfolge festgelegt werden." },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "Der Pfad enth\u00e4lt eine ung\u00fcltige Escapezeichenfolge." },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "Pfad enth\u00e4lt ung\u00fcltiges Zeichen: {0}." },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "Fragment enth\u00e4lt ein ung\u00fcltiges Zeichen." },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "Fragment kann nicht festgelegt werden, wenn der Pfad gleich Null ist." },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "Fragment kann nur f\u00fcr eine generische URI (Uniform Resource Identifier) festgelegt werden." },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "Kein Schema gefunden in URI" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "URI (Uniform Resource Identifier) kann nicht mit leeren Parametern initialisiert werden." },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "Fragment kann nicht im Pfad und im Fragment angegeben werden." },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "Abfragezeichenfolge kann nicht im Pfad und in der Abfragezeichenfolge angegeben werden." },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "Der Port kann nicht angegeben werden, wenn der Host nicht angegeben wurde." },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "Benutzerinformationen k\u00f6nnen nicht angegeben werden, wenn der Host nicht angegeben wurde." },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "Warnung: Die Version des Ausgabedokuments muss ''{0}'' lauten.  Diese XML-Version wird nicht unterst\u00fctzt.  Die Version des Ausgabedokuments ist ''1.0''." },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "Schema ist erforderlich!" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "Das an SerializerFactory \u00fcbermittelte Merkmalobjekt weist kein Merkmal ''{0}'' auf." },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "Warnung:  Die Codierung ''{0}'' wird von Java Runtime nicht unterst\u00fctzt." },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "Der Parameter ''{0}'' wird nicht erkannt."},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "Der Parameter ''{0}'' wird erkannt, der angeforderte Wert kann jedoch nicht festgelegt werden."},

             {MsgKey.ER_STRING_TOO_LONG,
             "Die Ergebniszeichenfolge ist zu lang f\u00fcr eine DOM-Zeichenfolge: ''{0}''."},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "Der Werttyp f\u00fcr diesen Parameternamen ist nicht kompatibel mit dem erwarteten Werttyp."},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "Das Ausgabeziel f\u00fcr die zu schreibenden Daten war leer."},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "Eine nicht unterst\u00fctzte Codierung wurde festgestellt."},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "Der Knoten konnte nicht serialisiert werden."},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "Der Abschnitt CDATA enth\u00e4lt mindestens eine Beendigungsmarkierung ']]>'."},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "Eine Instanz des Pr\u00fcfprogramms f\u00fcr korrekte Formatierung konnte nicht erstellt werden.  F\u00fcr den korrekt formatierten Parameter wurde der Wert 'True' festgelegt, die Pr\u00fcfung auf korrekte Formatierung kann jedoch nicht durchgef\u00fchrt werden."
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "Der Knoten ''{0}'' enth\u00e4lt ung\u00fcltige XML-Zeichen."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "Im Kommentar wurde ein ung\u00fcltiges XML-Zeichen (Unicode: 0x{0}) gefunden."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "In der Verarbeitungsanweisung wurde ein ung\u00fcltiges XML-Zeichen (Unicode: 0x{0}) gefunden."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "Im Inhalt von CDATASection wurde ein ung\u00fcltiges XML-Zeichen (Unicode: 0x{0}) gefunden."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "Ein ung\u00fcltiges XML-Zeichen  (Unicode: 0x{0}) wurde im Inhalt der Zeichendaten des Knotens gefunden."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "Ung\u00fcltige XML-Zeichen wurden gefunden in {0} im Knoten ''{1}''."
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "Die Zeichenfolge \"--\" ist innerhalb von Kommentaren nicht zul\u00e4ssig."
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "Der Wert des Attributs \"{1}\" mit einem Elementtyp \"{0}\" darf nicht das Zeichen ''<'' enthalten."
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "Der syntaktisch nicht analysierte Entit\u00e4tenverweis \"&{0};\" ist nicht zul\u00e4ssig."
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "Der externe Entit\u00e4tenverweis \"&{0};\" ist in einem Attributwert nicht zul\u00e4ssig."
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "Das Pr\u00e4fix \"{0}\" kann nicht an den Namensbereich \"{1}\" gebunden werden."
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "Der lokale Name von Element \"{0}\" ist nicht angegeben."
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "Der lokale Name des Attributs \"{0}\" ist nicht angegeben."
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "Der Ersatztext des Entit\u00e4tenknotens \"{0}\" enth\u00e4lt einen Elementknoten \"{1}\" mit einem nicht gebundenen Pr\u00e4fix \"{2}\"."
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "Der Ersatztext des Entit\u00e4tenknotens \"{0}\" enth\u00e4lt einen Attributknoten \"{1}\" mit einem nicht gebundenen Pr\u00e4fix \"{2}\"."
             },

        };

        return contents;
    }
}
