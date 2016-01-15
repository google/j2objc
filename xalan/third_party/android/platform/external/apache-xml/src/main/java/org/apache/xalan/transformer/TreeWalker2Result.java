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
 * $Id: TreeWalker2Result.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.DTMTreeWalker;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPathContext;

/**
 * Handle a walk of a tree, but screen out attributes for
 * the result tree.
 * @xsl.usage internal
 */
public class TreeWalker2Result extends DTMTreeWalker
{

  /** The transformer instance          */
  TransformerImpl m_transformer;

  /** The result tree handler          */
  SerializationHandler m_handler;

  /** Node where to start the tree walk           */
  int m_startNode;

  /**
   * Constructor.
   *
   * @param transformer Non-null transformer instance
   * @param handler The Result tree handler to use
   */
  public TreeWalker2Result(TransformerImpl transformer,
                           SerializationHandler handler)
  {

    super(handler, null);

    m_transformer = transformer;
    m_handler = handler;
  }

  /**
   * Perform a pre-order traversal non-recursive style.
   *
   * @param pos Start node for traversal
   *
   * @throws TransformerException
   */
  public void traverse(int pos) throws org.xml.sax.SAXException
  {
    m_dtm = m_transformer.getXPathContext().getDTM(pos);
    m_startNode = pos;

    super.traverse(pos);
  }
        
        /**
   * End processing of given node 
   *
   *
   * @param node Node we just finished processing
   *
   * @throws org.xml.sax.SAXException
   */
  protected void endNode(int node) throws org.xml.sax.SAXException
  {
    super.endNode(node);
    if(DTM.ELEMENT_NODE == m_dtm.getNodeType(node))
    {
      m_transformer.getXPathContext().popCurrentNode();
    }
  }

  /**
   * Start traversal of the tree at the given node
   *
   *
   * @param node Starting node for traversal
   *
   * @throws TransformerException
   */
  protected void startNode(int node) throws org.xml.sax.SAXException
  {

    XPathContext xcntxt = m_transformer.getXPathContext();
    try
    {
      
      if (DTM.ELEMENT_NODE == m_dtm.getNodeType(node))
      {
        xcntxt.pushCurrentNode(node);                   
                                        
        if(m_startNode != node)
        {
          super.startNode(node);
        }
        else
        {
          String elemName = m_dtm.getNodeName(node);
          String localName = m_dtm.getLocalName(node);
          String namespace = m_dtm.getNamespaceURI(node);
                                        
          //xcntxt.pushCurrentNode(node);       
          // SAX-like call to allow adding attributes afterwards
          m_handler.startElement(namespace, localName, elemName);
          boolean hasNSDecls = false;
          DTM dtm = m_dtm;
          for (int ns = dtm.getFirstNamespaceNode(node, true); 
               DTM.NULL != ns; ns = dtm.getNextNamespaceNode(node, ns, true))
          {
            SerializerUtils.ensureNamespaceDeclDeclared(m_handler,dtm, ns);
          }
                                                
                                                
          for (int attr = dtm.getFirstAttribute(node); 
               DTM.NULL != attr; attr = dtm.getNextAttribute(attr))
          {
            SerializerUtils.addAttribute(m_handler, attr);
          }
        }
                                
      }
      else
      {
        xcntxt.pushCurrentNode(node);
        super.startNode(node);
        xcntxt.popCurrentNode();
      }
    }
    catch(javax.xml.transform.TransformerException te)
    {
      throw new org.xml.sax.SAXException(te);
    }
  }
}
