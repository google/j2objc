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
 * $Id: SerializerMessages_pt_BR.java 471981 2006-11-07 04:28:00Z minchau $
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
public class SerializerMessages_pt_BR extends ListResourceBundle {

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
                "A chave de mensagem ''{0}'' n\u00e3o est\u00e1 na classe de mensagem ''{1}''" },

            {   MsgKey.BAD_MSGFORMAT,
                "O formato da mensagem ''{0}'' na classe de mensagem ''{1}'' falhou." },

            {   MsgKey.ER_SERIALIZER_NOT_CONTENTHANDLER,
                "A classe de serializador ''{0}'' n\u00e3o implementa org.xml.sax.ContentHandler." },

            {   MsgKey.ER_RESOURCE_COULD_NOT_FIND,
                    "O recurso [ {0} ] n\u00e3o p\u00f4de ser encontrado.\n{1}" },

            {   MsgKey.ER_RESOURCE_COULD_NOT_LOAD,
                    "O recurso [ {0} ] n\u00e3o p\u00f4de carregar: {1} \n {2} \t {3}" },

            {   MsgKey.ER_BUFFER_SIZE_LESSTHAN_ZERO,
                    "Tamanho do buffer <=0" },

            {   MsgKey.ER_INVALID_UTF16_SURROGATE,
                    "Detectado substituto UTF-16 inv\u00e1lido: {0} ?" },

            {   MsgKey.ER_OIERROR,
                "Erro de E/S" },

            {   MsgKey.ER_ILLEGAL_ATTRIBUTE_POSITION,
                "Imposs\u00edvel incluir atributo {0} depois de n\u00f3s filhos ou antes da gera\u00e7\u00e3o de um elemento. O atributo ser\u00e1 ignorado." },

            /*
             * Note to translators:  The stylesheet contained a reference to a
             * namespace prefix that was undefined.  The value of the substitution
             * text is the name of the prefix.
             */
            {   MsgKey.ER_NAMESPACE_PREFIX,
                "O espa\u00e7o de nomes do prefixo ''{0}'' n\u00e3o foi declarado. " },

            /*
             * Note to translators:  This message is reported if the stylesheet
             * being processed attempted to construct an XML document with an
             * attribute in a place other than on an element.  The substitution text
             * specifies the name of the attribute.
             */
            {   MsgKey.ER_STRAY_ATTRIBUTE,
                "Atributo ''{0}'' fora do elemento. " },

            /*
             * Note to translators:  As with the preceding message, a namespace
             * declaration has the form of an attribute and is only permitted to
             * appear on an element.  The substitution text {0} is the namespace
             * prefix and {1} is the URI that was being used in the erroneous
             * namespace declaration.
             */
            {   MsgKey.ER_STRAY_NAMESPACE,
                "Declara\u00e7\u00e3o de espa\u00e7o de nomes ''{0}''=''{1}'' fora do elemento. " },

            {   MsgKey.ER_COULD_NOT_LOAD_RESOURCE,
                "N\u00e3o foi poss\u00edvel carregar ''{0}'' (verifique CLASSPATH) agora , utilizando somente os padr\u00f5es" },

            {   MsgKey.ER_ILLEGAL_CHARACTER,
                "Tentativa de processar o caractere de um valor integral {0} que n\u00e3o \u00e9 representado na codifica\u00e7\u00e3o de sa\u00edda especificada de {1}." },

            {   MsgKey.ER_COULD_NOT_LOAD_METHOD_PROPERTY,
                "N\u00e3o foi poss\u00edvel carregar o arquivo de propriedade ''{0}'' para o m\u00e9todo de sa\u00edda ''{1}'' (verifique CLASSPATH)" },

            {   MsgKey.ER_INVALID_PORT,
                "N\u00famero de porta inv\u00e1lido" },

            {   MsgKey.ER_PORT_WHEN_HOST_NULL,
                "A porta n\u00e3o pode ser definida quando o host \u00e9 nulo" },

            {   MsgKey.ER_HOST_ADDRESS_NOT_WELLFORMED,
                "O host n\u00e3o \u00e9 um endere\u00e7o formado corretamente" },

            {   MsgKey.ER_SCHEME_NOT_CONFORMANT,
                "O esquema n\u00e3o est\u00e1 em conformidade." },

            {   MsgKey.ER_SCHEME_FROM_NULL_STRING,
                "Imposs\u00edvel definir esquema a partir da cadeia nula" },

            {   MsgKey.ER_PATH_CONTAINS_INVALID_ESCAPE_SEQUENCE,
                "O caminho cont\u00e9m seq\u00fc\u00eancia de escape inv\u00e1lida" },

            {   MsgKey.ER_PATH_INVALID_CHAR,
                "O caminho cont\u00e9m caractere inv\u00e1lido: {0}" },

            {   MsgKey.ER_FRAG_INVALID_CHAR,
                "O fragmento cont\u00e9m caractere inv\u00e1lido" },

            {   MsgKey.ER_FRAG_WHEN_PATH_NULL,
                "O fragmento n\u00e3o pode ser definido quando o caminho \u00e9 nulo" },

            {   MsgKey.ER_FRAG_FOR_GENERIC_URI,
                "O fragmento s\u00f3 pode ser definido para um URI gen\u00e9rico" },

            {   MsgKey.ER_NO_SCHEME_IN_URI,
                "Nenhum esquema encontrado no URI" },

            {   MsgKey.ER_CANNOT_INIT_URI_EMPTY_PARMS,
                "Imposs\u00edvel inicializar URI com par\u00e2metros vazios" },

            {   MsgKey.ER_NO_FRAGMENT_STRING_IN_PATH,
                "O fragmento n\u00e3o pode ser especificado no caminho e fragmento" },

            {   MsgKey.ER_NO_QUERY_STRING_IN_PATH,
                "A cadeia de consulta n\u00e3o pode ser especificada na cadeia de consulta e caminho" },

            {   MsgKey.ER_NO_PORT_IF_NO_HOST,
                "Port n\u00e3o pode ser especificado se host n\u00e3o for especificado" },

            {   MsgKey.ER_NO_USERINFO_IF_NO_HOST,
                "Userinfo n\u00e3o pode ser especificado se host n\u00e3o for especificado" },
            {   MsgKey.ER_XML_VERSION_NOT_SUPPORTED,
                "Aviso:  A vers\u00e3o do documento de sa\u00edda precisa ser ''{0}''.  Essa vers\u00e3o do XML n\u00e3o \u00e9 suportada. A vers\u00e3o do documento de sa\u00edda ser\u00e1 ''1.0''." },

            {   MsgKey.ER_SCHEME_REQUIRED,
                "O esquema \u00e9 obrigat\u00f3rio!" },

            /*
             * Note to translators:  The words 'Properties' and
             * 'SerializerFactory' in this message are Java class names
             * and should not be translated.
             */
            {   MsgKey.ER_FACTORY_PROPERTY_MISSING,
                "O objeto Properties transmitido para SerializerFactory n\u00e3o tem uma propriedade ''{0}''." },

            {   MsgKey.ER_ENCODING_NOT_SUPPORTED,
                "Aviso:  A codifica\u00e7\u00e3o ''{0}'' n\u00e3o \u00e9 suportada pelo Java Runtime." },

             {MsgKey.ER_FEATURE_NOT_FOUND,
             "O par\u00e2metro ''{0}'' n\u00e3o \u00e9 reconhecido."},

             {MsgKey.ER_FEATURE_NOT_SUPPORTED,
             "O par\u00e2metro ''{0}'' \u00e9 reconhecido, mas o valor pedido n\u00e3o pode ser definido. "},

             {MsgKey.ER_STRING_TOO_LONG,
             "A cadeia resultante \u00e9 muito longa para caber em uma DOMString: ''{0}''. "},

             {MsgKey.ER_TYPE_MISMATCH_ERR,
             "O tipo de valor para este nome de par\u00e2metro \u00e9 incompat\u00edvel com o tipo de valor esperado. "},

             {MsgKey.ER_NO_OUTPUT_SPECIFIED,
             "O destino de sa\u00edda para os dados a serem gravados era nulo. "},

             {MsgKey.ER_UNSUPPORTED_ENCODING,
             "Uma codifica\u00e7\u00e3o n\u00e3o suportada foi encontrada. "},

             {MsgKey.ER_UNABLE_TO_SERIALIZE_NODE,
             "O n\u00f3 n\u00e3o p\u00f4de ser serializado."},

             {MsgKey.ER_CDATA_SECTIONS_SPLIT,
             "A Se\u00e7\u00e3o CDATA cont\u00e9m um ou mais marcadores de t\u00e9rmino ']]>'."},

             {MsgKey.ER_WARNING_WF_NOT_CHECKED,
                 "Uma inst\u00e2ncia do verificador Well-Formedness n\u00e3o p\u00f4de ser criada. O par\u00e2metro well-formed foi definido como true, mas a verifica\u00e7\u00e3o well-formedness n\u00e3o pode ser executada."
             },

             {MsgKey.ER_WF_INVALID_CHARACTER,
                 "O n\u00f3 ''{0}'' cont\u00e9m caracteres XML inv\u00e1lidos. "
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_COMMENT,
                 "Um caractere XML inv\u00e1lido (Unicode: 0x{0}) foi encontrado no coment\u00e1rio. "
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_PI,
                 "Um caractere XML inv\u00e1lido (Unicode: 0x{0}) foi encontrado no processo instructiondata."
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_CDATA,
                 "Um caractere XML inv\u00e1lido (Unicode: 0x{0}) foi encontrado nos conte\u00fados do CDATASection. "
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_TEXT,
                 "Um caractere XML inv\u00e1lido (Unicode: 0x{0}) foi encontrado no conte\u00fado dos dados de caractere dos n\u00f3s. "
             },

             { MsgKey.ER_WF_INVALID_CHARACTER_IN_NODE_NAME,
                 "Um caractere inv\u00e1lido foi encontrado no {0} do n\u00f3 denominado ''{1}''."
             },

             { MsgKey.ER_WF_DASH_IN_COMMENT,
                 "A cadeia \"--\" n\u00e3o \u00e9 permitida dentro dos coment\u00e1rios. "
             },

             {MsgKey.ER_WF_LT_IN_ATTVAL,
                 "O valor do atributo \"{1}\" associado a um tipo de elemento \"{0}\" n\u00e3o deve conter o caractere ''<''. "
             },

             {MsgKey.ER_WF_REF_TO_UNPARSED_ENT,
                 "A refer\u00eancia de entidade n\u00e3o analisada \"&{0};\" n\u00e3o \u00e9 permitida. "
             },

             {MsgKey.ER_WF_REF_TO_EXTERNAL_ENT,
                 "A refer\u00eancia de entidade externa \"&{0};\" n\u00e3o \u00e9 permitida em um valor de atributo. "
             },

             {MsgKey.ER_NS_PREFIX_CANNOT_BE_BOUND,
                 "O prefixo \"{0}\" n\u00e3o pode ser vinculado ao espa\u00e7o de nomes \"{1}\"."
             },

             {MsgKey.ER_NULL_LOCAL_ELEMENT_NAME,
                 "O nome local do elemento \"{0}\" \u00e9 nulo."
             },

             {MsgKey.ER_NULL_LOCAL_ATTR_NAME,
                 "O nome local do atributo \"{0}\" \u00e9 nulo."
             },

             { MsgKey.ER_ELEM_UNBOUND_PREFIX_IN_ENTREF,
                 "O texto de substitui\u00e7\u00e3o do n\u00f3 de entidade \"{0}\" cont\u00e9m um n\u00f3 de elemento \"{1}\" com um prefixo n\u00e3o vinculado \"{2}\"."
             },

             { MsgKey.ER_ATTR_UNBOUND_PREFIX_IN_ENTREF,
                 "O texto de substitui\u00e7\u00e3o do n\u00f3 de entidade \"{0}\" cont\u00e9m um n\u00f3 de atributo \"{1}\" com um prefixo n\u00e3o vinculado \"{2}\"."
             },

        };

        return contents;
    }
}
