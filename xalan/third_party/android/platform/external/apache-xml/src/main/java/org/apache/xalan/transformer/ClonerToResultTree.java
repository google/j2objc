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
 * $Id: ClonerToResultTree.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import javax.xml.transform.TransformerException;

import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.dtm.DTM;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.XMLString;

/**
 * Class used to clone a node, possibly including its children to 
 * a result tree.
 * @xsl.usage internal
 */
public class ClonerToResultTree
{

//  /**
//   * Clone an element with or without children.
//   * TODO: Fix or figure out node clone failure!
//   * the error condition is severe enough to halt processing.
//   *
//   * @param node The node to clone
//   * @param shouldCloneAttributes Flag indicating whether to 
//   * clone children attributes
//   * 
//   * @throws TransformerException
//   */
//  public void cloneToResultTree(int node, boolean shouldCloneAttributes)
//    throws TransformerException
//  {
//
//    try
//    {
//      XPathContext xctxt = m_transformer.getXPathContext();
//      DTM dtm = xctxt.getDTM(node);
//
//      int type = dtm.getNodeType(node);
//      switch (type)
//      {
//      case DTM.TEXT_NODE :
//        dtm.dispatchCharactersEvents(node, m_rth, false);
//        break;
//      case DTM.DOCUMENT_FRAGMENT_NODE :
//      case DTM.DOCUMENT_NODE :
//
//        // Can't clone a document, but refrain from throwing an error
//        // so that copy-of will work
//        break;
//      case DTM.ELEMENT_NODE :
//        {
//          Attributes atts;
//
//          if (shouldCloneAttributes)
//          {
//            m_rth.addAttributes(node);
//            m_rth.processNSDecls(node, type, dtm);
//          }
//
//          String ns = dtm.getNamespaceURI(node);
//          String localName = dtm.getLocalName(node);
//
//          m_rth.startElement(ns, localName, dtm.getNodeNameX(node), null);
//        }
//        break;
//      case DTM.CDATA_SECTION_NODE :
//        m_rth.startCDATA();          
//        dtm.dispatchCharactersEvents(node, m_rth, false);
//        m_rth.endCDATA();
//        break;
//      case DTM.ATTRIBUTE_NODE :
//        m_rth.addAttribute(node);
//        break;
//      case DTM.COMMENT_NODE :
//        XMLString xstr = dtm.getStringValue (node);
//        xstr.dispatchAsComment(m_rth);
//        break;
//      case DTM.ENTITY_REFERENCE_NODE :
//        m_rth.entityReference(dtm.getNodeNameX(node));
//        break;
//      case DTM.PROCESSING_INSTRUCTION_NODE :
//        {
//          // %REVIEW% Is the node name the same as the "target"?
//          m_rth.processingInstruction(dtm.getNodeNameX(node), 
//                                      dtm.getNodeValue(node));
//        }
//        break;
//      default :
//        //"Can not create item in result tree: "+node.getNodeName());
//        m_transformer.getMsgMgr().error(null, 
//                         XSLTErrorResources.ER_CANT_CREATE_ITEM,
//                         new Object[]{ dtm.getNodeName(node) });  
//      }
//    }
//    catch(org.xml.sax.SAXException se)
//    {
//      throw new TransformerException(se);
//    }
//  }  // end cloneToResultTree function
  
  /**
   * Clone an element with or without children.
   * TODO: Fix or figure out node clone failure!
   * the error condition is severe enough to halt processing.
   *
   * @param node The node to clone
   * @param shouldCloneAttributes Flag indicating whether to 
   * clone children attributes
   * 
   * @throws TransformerException
   */
  public static void cloneToResultTree(int node, int nodeType, DTM dtm, 
                                             SerializationHandler rth,
                                             boolean shouldCloneAttributes)
    throws TransformerException
  {

    try
    {
      switch (nodeType)
      {
      case DTM.TEXT_NODE :
        dtm.dispatchCharactersEvents(node, rth, false);
        break;
      case DTM.DOCUMENT_FRAGMENT_NODE :
      case DTM.DOCUMENT_NODE :
        // Can't clone a document, but refrain from throwing an error
        // so that copy-of will work
        break;
      case DTM.ELEMENT_NODE :
        {
          // Note: SAX apparently expects "no namespace" to be
          // represented as "" rather than null.
          String ns = dtm.getNamespaceURI(node);
          if (ns==null) ns="";
          String localName = dtm.getLocalName(node);
      //  rth.startElement(ns, localName, dtm.getNodeNameX(node), null);
      //  don't call a real SAX startElement (as commented out above),
      //  call a SAX-like startElement, to be able to add attributes after this call
          rth.startElement(ns, localName, dtm.getNodeNameX(node));
          
	  // If outputting attrs as separate events, they must
	  // _follow_ the startElement event. (Think of the
	  // xsl:attribute directive.)
          if (shouldCloneAttributes)
          {
            SerializerUtils.addAttributes(rth, node);
            SerializerUtils.processNSDecls(rth, node, nodeType, dtm);
          }
        }
        break;
      case DTM.CDATA_SECTION_NODE :
        rth.startCDATA();          
        dtm.dispatchCharactersEvents(node, rth, false);
        rth.endCDATA();
        break;
      case DTM.ATTRIBUTE_NODE :
        SerializerUtils.addAttribute(rth, node);
        break;
			case DTM.NAMESPACE_NODE:
				// %REVIEW% Normally, these should have been handled with element.
				// It's possible that someone may write a stylesheet that tries to
				// clone them explicitly. If so, we need the equivalent of
				// rth.addAttribute().
  			    SerializerUtils.processNSDecls(rth,node,DTM.NAMESPACE_NODE,dtm);
				break;
      case DTM.COMMENT_NODE :
        XMLString xstr = dtm.getStringValue (node);
        xstr.dispatchAsComment(rth);
        break;
      case DTM.ENTITY_REFERENCE_NODE :
        rth.entityReference(dtm.getNodeNameX(node));
        break;
      case DTM.PROCESSING_INSTRUCTION_NODE :
        {
          // %REVIEW% Is the node name the same as the "target"?
          rth.processingInstruction(dtm.getNodeNameX(node), 
                                      dtm.getNodeValue(node));
        }
        break;
      default :
        //"Can not create item in result tree: "+node.getNodeName());
        throw new  TransformerException(
                         "Can't clone node: "+dtm.getNodeName(node));
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }  // end cloneToResultTree function
}
