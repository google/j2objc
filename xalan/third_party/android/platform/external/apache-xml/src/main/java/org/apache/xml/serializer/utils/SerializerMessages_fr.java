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
 * $Id: SerializerMessages_fr.java 471981 2006-11-07 04:28:00Z minchau $
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
public class SerializerMessages_fr extends ListResourceBundle {

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
                "La cl\u00e9 du message ''{0}'' ne se trouve pas dans la classe du message ''{1}''" },

            {   MsgKey.BAD_MSGFORMAT,
                "Le format du message ''{0}'' de la classe du message ''{1}'' est incorrect." },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "La classe de la m\u00e9thode de s\u00e9rialisation ''{0}'' n''impl\u00e9mente pas org.xml.sax.ContentHandler." },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "La ressource [ {0} ] est introuvable.\n {1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "La ressource [ {0} ] n''a pas pu charger : {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "Taille du tampon <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "Substitut UTF-16 non valide d\u00e9tect\u00e9 : {0} ?" },

            {   MsgKey.ER_OIERROR,
                "Erreur d'E-S" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "Ajout impossible de l''attribut {0} apr\u00e8s des noeuds enfants ou avant la production d''un \u00e9l\u00e9ment.  L''attribut est ignor\u00e9." },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "L''espace de noms du pr\u00e9fixe ''{0}'' n''a pas \u00e9t\u00e9 d\u00e9clar\u00e9." },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "L''attribut ''{0}'' est \u00e0 l''ext\u00e9rieur de l''\u00e9l\u00e9ment." },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "La d\u00e9claration d''espace de noms ''{0}''=''{1}'' est \u00e0 l''ext\u00e9rieur de l''\u00e9l\u00e9ment." },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "Impossible de charger ''{0}'' (v\u00e9rifier CLASSPATH), les valeurs par d\u00e9faut sont donc employ\u00e9es" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "Tentative de sortie d''un caract\u00e8re de la valeur enti\u00e8re {0} non repr\u00e9sent\u00e9e dans l''encodage de sortie de {1}." },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "Impossible de charger le fichier de propri\u00e9t\u00e9s ''{0}'' pour la m\u00e9thode de sortie ''{1}'' (v\u00e9rifier CLASSPATH)" },

            {   MsgKey.ER_INVALID_PORT,
                "Num\u00e9ro de port non valide" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "Le port ne peut \u00eatre d\u00e9fini quand l'h\u00f4te est vide" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "L'h\u00f4te n'est pas une adresse bien form\u00e9e" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "Le processus n'est pas conforme." },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "Impossible de d\u00e9finir le processus \u00e0 partir de la cha\u00eene vide" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "Le chemin d'acc\u00e8s contient une s\u00e9quence d'\u00e9chappement non valide" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "Le chemin contient un caract\u00e8re non valide : {0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "Le fragment contient un caract\u00e8re non valide" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "Le fragment ne peut \u00eatre d\u00e9fini quand le chemin d'acc\u00e8s est vide" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "Le fragment ne peut \u00eatre d\u00e9fini que pour un URI g\u00e9n\u00e9rique" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "Processus introuvable dans l'URI" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "Impossible d'initialiser l'URI avec des param\u00e8tres vides" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "Le fragment ne doit pas \u00eatre indiqu\u00e9 \u00e0 la fois dans le chemin et dans le fragment" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "La cha\u00eene de requ\u00eate ne doit pas figurer dans un chemin et une cha\u00eene de requ\u00eate" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "Le port peut ne pas \u00eatre sp\u00e9cifi\u00e9 si l'h\u00f4te n'est pas sp\u00e9cifi\u00e9" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "Userinfo ne peut \u00eatre sp\u00e9cifi\u00e9 si l'h\u00f4te ne l'est pas" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "Avertissement : La version du document de sortie doit \u00eatre ''{0}''.  Cette version XML n''est pas prise en charge.  La version du document de sortie sera ''1.0''." },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "Processus requis !" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "L''objet Properties transmis \u00e0 SerializerFactory ne dispose pas de propri\u00e9t\u00e9 ''{0}''." },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "Avertissement : Le codage ''{0}'' n''est pas pris en charge par l''environnement d''ex\u00e9cution Java." },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "Le param\u00e8tre ''{0}'' n''est pas reconnu."},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "Le param\u00e8tre ''{0}'' est reconnu mas la valeur demand\u00e9e ne peut pas \u00eatre d\u00e9finie."},

             {MsgKey.ER_STRING_TOO_LONG,
             "La cha\u00eene obtenue est trop longue pour un DOMString : ''{0}''."},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "Le type de valeur de ce param\u00e8tre est incompatible avec le type de valeur attendu."},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "La sortie de destination des donn\u00e9es \u00e0 \u00e9crire \u00e9tait vide."},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "Codage non pris en charge."},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "Le noeud ne peut pas \u00eatre s\u00e9rialis\u00e9."},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "La section CDATA contient un ou plusieurs marqueurs de fin ']]>'."},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "Aucune instance du programme de v\u00e9rification de la formation n'a pu \u00eatre cr\u00e9\u00e9e.  La valeur true a \u00e9t\u00e9 attribu\u00e9e au param\u00e8tre well-formed mais la v\u00e9rification de la formation n'a pas pu \u00eatre effectu\u00e9e."
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "Le noeud ''{0}'' contient des caract\u00e8res XML non valides."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "Un caract\u00e8re XML non valide (Unicode : 0x{0}) a \u00e9t\u00e9 trouv\u00e9 dans le commentaire."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "Un caract\u00e8re XML non valide (Unicode : 0x{0}) a \u00e9t\u00e9 trouv\u00e9 dans les donn\u00e9es de l''instruction de traitement."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "Un caract\u00e8re XML non valide (Unicode: 0x{0}) a \u00e9t\u00e9 trouv\u00e9 dans le contenu de la CDATASection"
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "Un caract\u00e8re XML non valide (Unicode : 0x{0}) a \u00e9t\u00e9 trouv\u00e9 dans le contenu des donn\u00e9es de type caract\u00e8res du noeud."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "Un ou plusieurs caract\u00e8res non valides ont \u00e9t\u00e9 trouv\u00e9s dans le noeud {0} nomm\u00e9 ''{1}''."
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "La cha\u00eene \"--\" est interdite dans des commentaires."
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "La valeur de l''attribut \"{1}\" associ\u00e9 \u00e0 un type d''\u00e9l\u00e9ment \"{0}\" ne doit pas contenir le caract\u00e8re ''<''."
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "La r\u00e9f\u00e9rence d''entit\u00e9 non analys\u00e9e \"&{0};\" n''est pas admise."
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "La r\u00e9f\u00e9rence d''entit\u00e9 externe \"&{0};\" n''est pas admise dans une valeur d''attribut."
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "Le pr\u00e9fixe \"{0}\" ne peut pas \u00eatre li\u00e9 \u00e0 l''espace de noms \"{1}\"."
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "Le nom local de l''\u00e9l\u00e9ment \"{0}\" a une valeur null."
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "Le nom local de l''attribut \"{0}\" a une valeur null."
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "le texte de remplacement du noeud de l''entit\u00e9 \"{0}\" contaient un noeud d''\u00e9l\u00e9ment \"{1}\" avec un pr\u00e9fixe non li\u00e9 \"{2}\"."
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "Le texte de remplacement du noeud de l''entit\u00e9 \"{0}\" contient un noeud d''attribut \"{1}\" avec un pr\u00e9fixe non li\u00e9 \"{2}\"."
             },

        };

        return contents;
    }
}
