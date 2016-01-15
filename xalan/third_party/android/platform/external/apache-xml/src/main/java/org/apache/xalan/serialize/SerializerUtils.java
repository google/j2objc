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
 * $Id: SerializerUtils.java 468642 2006-10-28 06:55:10Z minchau $
 */
package org.apache.xalan.serialize;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.serializer.NamespaceMappings;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.xml.sax.SAXException;

/**
 * Class that contains only static methods that are used to "serialize",
 * these methods are used by Xalan and are not in org.apache.xml.serializer
 * because they have dependancies on the packages org.apache.xpath or org.
 * apache.xml.dtm or org.apache.xalan.transformer. The package org.apache.xml.
 * serializer should not depend on Xalan or XSLTC.
 * @xsl.usage internal
 */
public class SerializerUtils
{

    /**
     * Copy an DOM attribute to the created output element, executing
     * attribute templates as need be, and processing the xsl:use
     * attribute.
     *
     * @param handler SerializationHandler to which the attributes are added.
     * @param attr Attribute node to add to SerializationHandler.
     *
     * @throws TransformerException
     */
    public static void addAttribute(SerializationHandler handler, int attr)
        throws TransformerException
    {

        TransformerImpl transformer =
            (TransformerImpl) handler.getTransformer();
        DTM dtm = transformer.getXPathContext().getDTM(attr);

        if (SerializerUtils.isDefinedNSDecl(handler, attr, dtm))
            return;

        String ns = dtm.getNamespaceURI(attr);

        if (ns == null)
            ns = "";

        // %OPT% ...can I just store the node handle?
        try
        {
            handler.addAttribute(
                ns,
                dtm.getLocalName(attr),
                dtm.getNodeName(attr),
                "CDATA",
                dtm.getNodeValue(attr), false);
        }
        catch (SAXException e)
        {
            // do something?
        }
    } // end copyAttributeToTarget method

    /**
     * Copy DOM attributes to the result element.
     *
     * @param src Source node with the attributes
     *
     * @throws TransformerException
     */
    public static void addAttributes(SerializationHandler handler, int src)
        throws TransformerException
    {

        TransformerImpl transformer =
            (TransformerImpl) handler.getTransformer();
        DTM dtm = transformer.getXPathContext().getDTM(src);

        for (int node = dtm.getFirstAttribute(src);
            DTM.NULL != node;
            node = dtm.getNextAttribute(node))
        {
            addAttribute(handler, node);
        }
    }

    /**
     * Given a result tree fragment, walk the tree and
     * output it to the SerializationHandler.
     *
     * @param obj Result tree fragment object
     * @param support XPath context for the result tree fragment
     *
     * @throws org.xml.sax.SAXException
     */
    public static void outputResultTreeFragment(
        SerializationHandler handler,
        XObject obj,
        XPathContext support)
        throws org.xml.sax.SAXException
    {

        int doc = obj.rtf();
        DTM dtm = support.getDTM(doc);

        if (null != dtm)
        {
            for (int n = dtm.getFirstChild(doc);
                DTM.NULL != n;
                n = dtm.getNextSibling(n))
            {
                handler.flushPending();

                // I think. . . . This used to have a (true) arg
                // to flush prefixes, will that cause problems ???
                if (dtm.getNodeType(n) == DTM.ELEMENT_NODE
                        && dtm.getNamespaceURI(n) == null)
                    handler.startPrefixMapping("", "");
                dtm.dispatchToEvents(n, handler);
            }
        }
    }

    /**
     * Copy <KBD>xmlns:</KBD> attributes in if not already in scope.
     *
     * As a quick hack to support ClonerToResultTree, this can also be used
     * to copy an individual namespace node.
     *
     * @param src Source Node
     * NEEDSDOC @param type
     * NEEDSDOC @param dtm
     *
     * @throws TransformerException
     */
    public static void processNSDecls(
        SerializationHandler handler,
        int src,
        int type,
        DTM dtm)
        throws TransformerException
    {

        try
        {
            if (type == DTM.ELEMENT_NODE)
            {
                for (int namespace = dtm.getFirstNamespaceNode(src, true);
                    DTM.NULL != namespace;
                    namespace = dtm.getNextNamespaceNode(src, namespace, true))
                {

                    // String prefix = dtm.getPrefix(namespace);
                    String prefix = dtm.getNodeNameX(namespace);
                    String desturi = handler.getNamespaceURIFromPrefix(prefix);
                    //            String desturi = getURI(prefix);
                    String srcURI = dtm.getNodeValue(namespace);

                    if (!srcURI.equalsIgnoreCase(desturi))
                    {
                        handler.startPrefixMapping(prefix, srcURI, false);
                    }
                }
            }
            else if (type == DTM.NAMESPACE_NODE)
            {
                String prefix = dtm.getNodeNameX(src);
                // Brian M. - some changes here to get desturi
                String desturi = handler.getNamespaceURIFromPrefix(prefix);
                String srcURI = dtm.getNodeValue(src);

                if (!srcURI.equalsIgnoreCase(desturi))
                {
                    handler.startPrefixMapping(prefix, srcURI, false);
                }
            }
        }
        catch (org.xml.sax.SAXException se)
        {
            throw new TransformerException(se);
        }
    }

    /**
     * Returns whether a namespace is defined
     *
     *
     * @param attr Namespace attribute node
     * @param dtm The DTM that owns attr.
     *
     * @return True if the namespace is already defined in
     * list of namespaces
     */
    public static boolean isDefinedNSDecl(
        SerializationHandler serializer,
        int attr,
        DTM dtm)
    {

        if (DTM.NAMESPACE_NODE == dtm.getNodeType(attr))
        {

            // String prefix = dtm.getPrefix(attr);
            String prefix = dtm.getNodeNameX(attr);
            String uri = serializer.getNamespaceURIFromPrefix(prefix);
            //      String uri = getURI(prefix);

            if ((null != uri) && uri.equals(dtm.getStringValue(attr)))
                return true;
        }

        return false;
    }

    /**
     * This function checks to make sure a given prefix is really
     * declared.  It might not be, because it may be an excluded prefix.
     * If it's not, it still needs to be declared at this point.
     * TODO: This needs to be done at an earlier stage in the game... -sb
     *
     * NEEDSDOC @param dtm
     * NEEDSDOC @param namespace
     *
     * @throws org.xml.sax.SAXException
     */
    public static void ensureNamespaceDeclDeclared(
        SerializationHandler handler,
        DTM dtm,
        int namespace)
        throws org.xml.sax.SAXException
    {

        String uri = dtm.getNodeValue(namespace);
        String prefix = dtm.getNodeNameX(namespace);

        if ((uri != null && uri.length() > 0) && (null != prefix))
        {
            String foundURI;
            NamespaceMappings ns = handler.getNamespaceMappings();
            if (ns != null)
            {

                foundURI = ns.lookupNamespace(prefix);
                if ((null == foundURI) || !foundURI.equals(uri))
                {
                    handler.startPrefixMapping(prefix, uri, false);
                }
            }
        }
    }
}
