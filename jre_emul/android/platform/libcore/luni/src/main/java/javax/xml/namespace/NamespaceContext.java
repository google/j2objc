/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// $Id: NamespaceContext.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.namespace;

import java.util.Iterator;

/**
 * <p>Interface for read only XML Namespace context processing.</p>
 *
 * <p>An XML Namespace has the properties:</p>
 * <ul>
 *   <li>Namespace URI:
 *       Namespace name expressed as a URI to which the prefix is bound</li>
 *   <li>prefix: syntactically, this is the part of the attribute name
 *       following the <code>XMLConstants.XMLNS_ATTRIBUTE</code>
 *       ("xmlns") in the Namespace declaration</li>
 * </ul>
 * <p> example: <code>&lt;element xmlns:prefix="http://Namespace-name-URI"&gt;</code></p>
 *
 * <p>All <code>get*(*)</code> methods operate in the current scope
 * for Namespace URI and prefix resolution.</p>
 *
 * <p>Note that a Namespace URI can be bound to
 * <strong>multiple</strong> prefixes in the current scope.  This can
 * occur when multiple <code>XMLConstants.XMLNS_ATTRIBUTE</code>
 * ("xmlns") Namespace declarations occur in the same Start-Tag and
 * refer to the same Namespace URI. e.g.<br />
 * <pre>
 * &lt;element xmlns:prefix1="http://Namespace-name-URI"
 *          xmlns:prefix2="http://Namespace-name-URI"&gt;
 * </pre>
 * This can also occur when the same Namespace URI is used in multiple
 * <code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns") Namespace
 * declarations in the logical parent element hierarchy.  e.g.<br />
 * <pre>
 * &lt;parent xmlns:prefix1="http://Namespace-name-URI">
 *   &lt;child xmlns:prefix2="http://Namespace-name-URI"&gt;
 *     ...
 *   &lt;/child&gt;
 * &lt;/parent&gt;
 * </pre></p>
 *
 * <p>A prefix can only be bound to a <strong>single</strong>
 * Namespace URI in the current scope.</p>
 *
 * @author <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 446598 $, $Date: 2006-09-15 05:55:40 -0700 (Fri, 15 Sep 2006) $
 * @see javax.xml.XMLConstants javax.XMLConstants for declarations of common XML values
 * @see <a href="http://www.w3.org/TR/xmlschema-2/#QName">XML Schema Part2: Datatypes</a>
 * @see <a href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">Namespaces in XML</a>
 * @see <a href="http://www.w3.org/XML/xml-names-19990114-errata">Namespaces in XML Errata</a>
 * @since 1.5
 */

public interface NamespaceContext {

    /**
     * <p>Get Namespace URI bound to a prefix in the current scope.</p>
     *
     * <p>When requesting a Namespace URI by prefix, the following
     * table describes the returned Namespace URI value for all
     * possible prefix values:</p>
     *
     * <table border="2" rules="all" cellpadding="4">
     *   <thead>
     *     <tr>
     *       <td align="center" colspan="2">
     *         <code>getNamespaceURI(prefix)</code>
     *         return value for specified prefixes
     *       </td>
     *     </tr>
     *     <tr>
     *       <td>prefix parameter</td>
     *       <td>Namespace URI return value</td>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td><code>DEFAULT_NS_PREFIX</code> ("")</td>
     *       <td>default Namespace URI in the current scope or
     *         <code>{@link javax.xml.XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI("")}</code>
     *         when there is no default Namespace URI in the current scope</td>
     *     </tr>
     *     <tr>
     *       <td>bound prefix</td>
     *       <td>Namespace URI bound to prefix in current scope</td>
     *     </tr>
     *     <tr>
     *       <td>unbound prefix</td>
     *       <td><code>{@link javax.xml.XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI("")}</code> </td>
     *     </tr>
     *     <tr>
     *       <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     *       <td><code>XMLConstants.XML_NS_URI</code>
     *           ("http://www.w3.org/XML/1998/namespace")</td>
     *     </tr>
     *     <tr>
     *       <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     *       <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
     *         ("http://www.w3.org/2000/xmlns/")</td>
     *     </tr>
     *     <tr>
     *       <td><code>null</code></td>
     *       <td><code>IllegalArgumentException</code> is thrown</td>
     *     </tr>
     *    </tbody>
     * </table>
     *
     * @param prefix prefix to look up
     * @return Namespace URI bound to prefix in the current scope
     */
    String getNamespaceURI(String prefix);

    /**
     * <p>Get prefix bound to Namespace URI in the current scope.</p>
     *
     * <p>To get all prefixes bound to a Namespace URI in the current
     * scope, use {@link #getPrefixes(String namespaceURI)}.</p>
     *
     * <p>When requesting a prefix by Namespace URI, the following
     * table describes the returned prefix value for all Namespace URI
     * values:</p>
     *
     * <table border="2" rules="all" cellpadding="4">
     *   <thead>
     *     <tr>
     *       <td align="center" colspan="2">
     *         <code>getPrefix(namespaceURI)</code> return value for
     *         specified Namespace URIs
     *       </td>
     *     </tr>
     *     <tr>
     *       <td>Namespace URI parameter</td>
     *       <td>prefix value returned</td>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>&lt;default Namespace URI&gt;</td>
     *       <td><code>XMLConstants.DEFAULT_NS_PREFIX</code> ("")
     *       </td>
     *     </tr>
     *     <tr>
     *       <td>bound Namespace URI</td>
     *       <td>prefix bound to Namespace URI in the current scope,
     *           if multiple prefixes are bound to the Namespace URI in
     *           the current scope, a single arbitrary prefix, whose
     *           choice is implementation dependent, is returned</td>
     *     </tr>
     *     <tr>
     *       <td>unbound Namespace URI</td>
     *       <td><code>null</code></td>
     *     </tr>
     *     <tr>
     *       <td><code>XMLConstants.XML_NS_URI</code>
     *           ("http://www.w3.org/XML/1998/namespace")</td>
     *       <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     *     </tr>
     *     <tr>
     *       <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
     *           ("http://www.w3.org/2000/xmlns/")</td>
     *       <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     *     </tr>
     *     <tr>
     *       <td><code>null</code></td>
     *       <td><code>IllegalArgumentException</code> is thrown</td>
     *     </tr>
     *   </tbody>
     * </table>
     *
     * @param namespaceURI URI of Namespace to lookup
     * @return prefix bound to Namespace URI in current context
     */
    String getPrefix(String namespaceURI);

    /**
     * <p>Get all prefixes bound to a Namespace URI in the current
     * scope.</p>
     *
     * <p>An Iterator over String elements is returned in an arbitrary, <strong>implementation dependent</strong>, order.</p>
     *
     * <p><strong>The <code>Iterator</code> is
     * <em>not</em> modifiable.  e.g. the
     * <code>remove()</code> method will throw
     * <code>UnsupportedOperationException</code>.</strong></p>
     *
     * <p>When requesting prefixes by Namespace URI, the following
     * table describes the returned prefixes value for all Namespace
     * URI values:</p>
     *
     * <table border="2" rules="all" cellpadding="4">
     *   <thead>
     *     <tr>
     *       <td align="center" colspan="2"><code>
     *         getPrefixes(namespaceURI)</code> return value for
     *         specified Namespace URIs</td>
     *     </tr>
     *     <tr>
     *       <td>Namespace URI parameter</td>
     *       <td>prefixes value returned</td>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td>bound Namespace URI,
     *         including the &lt;default Namespace URI&gt;</td>
     *       <td><code>Iterator</code> over prefixes bound to Namespace URI in
     *         the current scope in an arbitrary, <strong>implementation dependent</strong>,
     *         order</td>
     *     </tr>
     *     <tr>
     *       <td>unbound Namespace URI</td>
     *       <td>empty <code>Iterator</code></td>
     *     </tr>
     *     <tr>
     *       <td><code>XMLConstants.XML_NS_URI</code>
     *           ("http://www.w3.org/XML/1998/namespace")</td>
     *       <td><code>Iterator</code> with one element set to
     *         <code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     *     </tr>
     *     <tr>
     *       <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
     *           ("http://www.w3.org/2000/xmlns/")</td>
     *       <td><code>Iterator</code> with one element set to
     *         <code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     *     </tr>
     *     <tr>
     *       <td><code>null</code></td>
     *       <td><code>IllegalArgumentException</code> is thrown</td>
     *     </tr>
     *   </tbody>
     * </table>
     *
     * @param namespaceURI URI of Namespace to lookup
     * @return <code>Iterator</code> for all prefixes bound to the
     * Namespace URI in the current scope
     */
    Iterator getPrefixes(String namespaceURI);
}
