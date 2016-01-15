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
 * $Id: ElemAttribute.java 469304 2006-10-30 22:29:47Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.NamespaceMappings;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XML11Char;

import org.xml.sax.SAXException;

/**
 * Implement xsl:attribute.
 * <pre>
 * &amp;!ELEMENT xsl:attribute %char-template;>
 * &amp;!ATTLIST xsl:attribute
 *   name %avt; #REQUIRED
 *   namespace %avt; #IMPLIED
 *   %space-att;
 * &amp;
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#creating-attributes">creating-attributes in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemAttribute extends ElemElement
{
    static final long serialVersionUID = 8817220961566919187L;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_ATTRIBUTE;
  }

  /**
   * Return the node name.
   *
   * @return The element name 
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_ATTRIBUTE_STRING;
  }

  /**
   * Create an attribute in the result tree.
   * @see <a href="http://www.w3.org/TR/xslt#creating-attributes">creating-attributes in XSLT Specification</a>
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
//  public void execute(
//          TransformerImpl transformer)
//            throws TransformerException
//  {
    //SerializationHandler rhandler = transformer.getSerializationHandler();

    // If they are trying to add an attribute when there isn't an 
    // element pending, it is an error.
    // I don't think we need this check here because it is checked in 
    // ResultTreeHandler.addAttribute.  (is)
//    if (!rhandler.isElementPending())
//    {
//      // Make sure the trace event is sent.
//      if (TransformerImpl.S_DEBUG)
//        transformer.getTraceManager().fireTraceEvent(this);
//
//      XPathContext xctxt = transformer.getXPathContext();
//      int sourceNode = xctxt.getCurrentNode();
//      String attrName = m_name_avt.evaluate(xctxt, sourceNode, this);
//      transformer.getMsgMgr().warn(this,
//                                   XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_POSITION,
//                                   new Object[]{ attrName });
//
//      if (TransformerImpl.S_DEBUG)
//        transformer.getTraceManager().fireTraceEndEvent(this);
//      return;
//
//      // warn(templateChild, sourceNode, "Trying to add attribute after element child has been added, ignoring...");
//    }
    
//    super.execute(transformer);
    
//  }
  
  /**
   * Resolve the namespace into a prefix.  At this level, if no prefix exists, 
   * then return a manufactured prefix.
   *
   * @param rhandler The current result tree handler.
   * @param prefix The probable prefix if already known.
   * @param nodeNamespace  The namespace, which should not be null.
   *
   * @return The prefix to be used.
   */
  protected String resolvePrefix(SerializationHandler rhandler,
                                 String prefix, String nodeNamespace)
    throws TransformerException
  {

    if (null != prefix && (prefix.length() == 0 || prefix.equals("xmlns")))
    {
      // Since we can't use default namespace, in this case we try and 
      // see if a prefix has already been defined or this namespace.
      prefix = rhandler.getPrefix(nodeNamespace);

      // System.out.println("nsPrefix: "+nsPrefix);           
      if (null == prefix || prefix.length() == 0 || prefix.equals("xmlns"))
      {
        if(nodeNamespace.length() > 0)
        {
            NamespaceMappings prefixMapping = rhandler.getNamespaceMappings();
            prefix = prefixMapping.generateNextPrefix();
        }
        else
          prefix = "";
      }
    }
    return prefix;
  }
  
  /**
   * Validate that the node name is good.
   * 
   * @param nodeName Name of the node being constructed, which may be null.
   * 
   * @return true if the node name is valid, false otherwise.
   */
   protected boolean validateNodeName(String nodeName)
   {
      if(null == nodeName)
        return false;
      if(nodeName.equals("xmlns"))
        return false;
      return XML11Char.isXML11ValidQName(nodeName);
   }
  
  /**
   * Construct a node in the result tree.  This method is overloaded by 
   * xsl:attribute. At this class level, this method creates an element.
   *
   * @param nodeName The name of the node, which may be null.
   * @param prefix The prefix for the namespace, which may be null.
   * @param nodeNamespace The namespace of the node, which may be null.
   * @param transformer non-null reference to the the current transform-time state.
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   * @param mode reference, which may be null, to the <a href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   *
   * @throws TransformerException
   */
  void constructNode(
          String nodeName, String prefix, String nodeNamespace, TransformerImpl transformer)
            throws TransformerException
  {

    if(null != nodeName && nodeName.length() > 0)
    {
      SerializationHandler rhandler = transformer.getSerializationHandler();

      // Evaluate the value of this attribute
      String val = transformer.transformToString(this);
      try 
      {
        // Let the result tree handler add the attribute and its String value.
        String localName = QName.getLocalPart(nodeName);
        if(prefix != null && prefix.length() > 0){
            rhandler.addAttribute(nodeNamespace, localName, nodeName, "CDATA", val, true);
        }else{
            rhandler.addAttribute("", localName, nodeName, "CDATA", val, true);
        }
      }
      catch (SAXException e)
      {
      }
    }
  }


  /**
   * Add a child to the child list.
   * <!ELEMENT xsl:attribute %char-template;>
   * <!ATTLIST xsl:attribute
   *   name %avt; #REQUIRED
   *   namespace %avt; #IMPLIED
   *   %space-att;
   * >
   *
   * @param newChild Child to append to the list of this node's children
   *
   * @return The node we just appended to the children list 
   *
   * @throws DOMException
   */
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {

    int type = ((ElemTemplateElement) newChild).getXSLToken();

    switch (type)
    {

    // char-instructions 
    case Constants.ELEMNAME_TEXTLITERALRESULT :
    case Constants.ELEMNAME_APPLY_TEMPLATES :
    case Constants.ELEMNAME_APPLY_IMPORTS :
    case Constants.ELEMNAME_CALLTEMPLATE :
    case Constants.ELEMNAME_FOREACH :
    case Constants.ELEMNAME_VALUEOF :
    case Constants.ELEMNAME_COPY_OF :
    case Constants.ELEMNAME_NUMBER :
    case Constants.ELEMNAME_CHOOSE :
    case Constants.ELEMNAME_IF :
    case Constants.ELEMNAME_TEXT :
    case Constants.ELEMNAME_COPY :
    case Constants.ELEMNAME_VARIABLE :
    case Constants.ELEMNAME_MESSAGE :

      // instructions 
      // case Constants.ELEMNAME_PI:
      // case Constants.ELEMNAME_COMMENT:
      // case Constants.ELEMNAME_ELEMENT:
      // case Constants.ELEMNAME_ATTRIBUTE:
      break;
    default :
      error(XSLTErrorResources.ER_CANNOT_ADD,
            new Object[]{ newChild.getNodeName(),
                          this.getNodeName() });  //"Can not add " +((ElemTemplateElement)newChild).m_elemName +

    //" to " + this.m_elemName);
    }

    return super.appendChild(newChild);
  }
	/**
	 * @see ElemElement#setName(AVT)
	 */
	public void setName(AVT v) {
        if (v.isSimple())
        {
            if (v.getSimpleString().equals("xmlns"))
            {
                throw new IllegalArgumentException();
            }
        }
		super.setName(v);
	}

}
