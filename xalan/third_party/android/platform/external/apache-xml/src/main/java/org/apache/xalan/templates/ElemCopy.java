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
 * $Id: ElemCopy.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.ClonerToResultTree;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPathContext;

/**
 * Implement xsl:copy.
 * <pre>
 * <!ELEMENT xsl:copy %template;>
 * <!ATTLIST xsl:copy
 *   %space-att;
 *   use-attribute-sets %qnames; #IMPLIED
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#copying">copying in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemCopy extends ElemUse
{
    static final long serialVersionUID = 5478580783896941384L;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element 
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_COPY;
  }

  /**
   * Return the node name.
   *
   * @return This element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_COPY_STRING;
  }

  /**
   * The xsl:copy element provides an easy way of copying the current node.
   * Executing this function creates a copy of the current node into the
   * result tree.
   * <p>The namespace nodes of the current node are automatically
   * copied as well, but the attributes and children of the node are not
   * automatically copied. The content of the xsl:copy element is a
   * template for the attributes and children of the created node;
   * the content is instantiated only for nodes of types that can have
   * attributes or children (i.e. root nodes and element nodes).</p>
   * <p>The root node is treated specially because the root node of the
   * result tree is created implicitly. When the current node is the
   * root node, xsl:copy will not create a root node, but will just use
   * the content template.</p>
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
                XPathContext xctxt = transformer.getXPathContext();
      
    try
    {
      int sourceNode = xctxt.getCurrentNode();
      xctxt.pushCurrentNode(sourceNode);
      DTM dtm = xctxt.getDTM(sourceNode);
      short nodeType = dtm.getNodeType(sourceNode);

      if ((DTM.DOCUMENT_NODE != nodeType) && (DTM.DOCUMENT_FRAGMENT_NODE != nodeType))
      {
        SerializationHandler rthandler = transformer.getSerializationHandler();

        // TODO: Process the use-attribute-sets stuff
        ClonerToResultTree.cloneToResultTree(sourceNode, nodeType, dtm, 
                                             rthandler, false);

        if (DTM.ELEMENT_NODE == nodeType)
        {
          super.execute(transformer);
          SerializerUtils.processNSDecls(rthandler, sourceNode, nodeType, dtm);
          transformer.executeChildTemplates(this, true);
          
          String ns = dtm.getNamespaceURI(sourceNode);
          String localName = dtm.getLocalName(sourceNode);
          transformer.getResultTreeHandler().endElement(ns, localName,
                                                        dtm.getNodeName(sourceNode));
        }
      }
      else
      {
        super.execute(transformer);
        transformer.executeChildTemplates(this, true);
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      xctxt.popCurrentNode();
    }
  }
}
