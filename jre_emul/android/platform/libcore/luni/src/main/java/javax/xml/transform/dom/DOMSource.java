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

// $Id: DOMSource.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.transform.dom;

import javax.xml.transform.Source;
import org.w3c.dom.Node;

/**
 * <p>Acts as a holder for a transformation Source tree in the
 * form of a Document Object Model (DOM) tree.</p>
 *
 * <p>Note that XSLT requires namespace support. Attempting to transform a DOM
 * that was not contructed with a namespace-aware parser may result in errors.
 * Parsers can be made namespace aware by calling
 * {@link javax.xml.parsers.DocumentBuilderFactory#setNamespaceAware(boolean awareness)}.</p>
 *
 * @author <a href="Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 446598 $, $Date: 2006-09-15 05:55:40 -0700 (Fri, 15 Sep 2006) $
 * @see <a href="http://www.w3.org/TR/DOM-Level-2">Document Object Model (DOM) Level 2 Specification</a>
 */
public class DOMSource implements Source {

    /**
     * <p><code>Node</code> to serve as DOM source.</p>
     */
    private Node node;

    /**
     * <p>The base ID (URL or system ID) from where URLs
     * will be resolved.</p>
     */
    private String systemID;

    /** If {@link javax.xml.transform.TransformerFactory#getFeature}
     * returns true when passed this value as an argument,
     * the Transformer supports Source input of this type.
     */
    public static final String FEATURE =
        "http://javax.xml.transform.dom.DOMSource/feature";

    /**
     * <p>Zero-argument default constructor.  If this constructor is used, and
     * no DOM source is set using {@link #setNode(Node node)} , then the
     * <code>Transformer</code> will
     * create an empty source {@link org.w3c.dom.Document} using
     * {@link javax.xml.parsers.DocumentBuilder#newDocument()}.</p>
     *
     * @see javax.xml.transform.Transformer#transform(Source xmlSource, Result outputTarget)
     */
    public DOMSource() { }

    /**
     * Create a new input source with a DOM node.  The operation
     * will be applied to the subtree rooted at this node.  In XSLT,
     * a "/" pattern still means the root of the tree (not the subtree),
     * and the evaluation of global variables and parameters is done
     * from the root node also.
     *
     * @param n The DOM node that will contain the Source tree.
     */
    public DOMSource(Node n) {
        setNode(n);
    }

    /**
     * Create a new input source with a DOM node, and with the
     * system ID also passed in as the base URI.
     *
     * @param node The DOM node that will contain the Source tree.
     * @param systemID Specifies the base URI associated with node.
     */
    public DOMSource(Node node, String systemID) {
        setNode(node);
        setSystemId(systemID);
    }

    /**
     * Set the node that will represents a Source DOM tree.
     *
     * @param node The node that is to be transformed.
     */
    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * Get the node that represents a Source DOM tree.
     *
     * @return The node that is to be transformed.
     */
    public Node getNode() {
        return node;
    }

    /**
     * Set the base ID (URL or system ID) from where URLs
     * will be resolved.
     *
     * @param systemID Base URL for this DOM tree.
     */
    public void setSystemId(String systemID) {
        this.systemID = systemID;
    }

    /**
     * Get the base ID (URL or system ID) from where URLs
     * will be resolved.
     *
     * @return Base URL for this DOM tree.
     */
    public String getSystemId() {
        return this.systemID;
    }
}
