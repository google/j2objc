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
 * $Id: ElemElement.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPathContext;
import org.xml.sax.SAXException;

/**
 * Implement xsl:element
 * <pre>
 * <!ELEMENT xsl:element %template;>
 * <!ATTLIST xsl:element
 *   name %avt; #REQUIRED
 *   namespace %avt; #IMPLIED
 *   use-attribute-sets %qnames; #IMPLIED
 *   %space-att;
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Creating-Elements-with-xsl:element">XXX in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemElement extends ElemUse
{
    static final long serialVersionUID = -324619535592435183L;

  /**
   * The name attribute is interpreted as an attribute value template.
   * It is an error if the string that results from instantiating the
   * attribute value template is not a QName.
   * @serial
   */
  protected AVT m_name_avt = null;

  /**
   * Set the "name" attribute.
   * The name attribute is interpreted as an attribute value template.
   * It is an error if the string that results from instantiating the
   * attribute value template is not a QName.
   *
   * @param v Name attribute to set for this element
   */
  public void setName(AVT v)
  {
    m_name_avt = v;
  }

  /**
   * Get the "name" attribute.
   * The name attribute is interpreted as an attribute value template.
   * It is an error if the string that results from instantiating the
   * attribute value template is not a QName.
   *
   * @return Name attribute for this element
   */
  public AVT getName()
  {
    return m_name_avt;
  }

  /**
   * If the namespace attribute is present, then it also is interpreted
   * as an attribute value template. The string that results from
   * instantiating the attribute value template should be a URI reference.
   * It is not an error if the string is not a syntactically legal URI reference.
   * @serial
   */
  protected AVT m_namespace_avt = null;

  /**
   * Set the "namespace" attribute.
   * If the namespace attribute is present, then it also is interpreted
   * as an attribute value template. The string that results from
   * instantiating the attribute value template should be a URI reference.
   * It is not an error if the string is not a syntactically legal URI reference.
   *
   * @param v NameSpace attribute to set for this element
   */
  public void setNamespace(AVT v)
  {
    m_namespace_avt = v;
  }

  /**
   * Get the "namespace" attribute.
   * If the namespace attribute is present, then it also is interpreted
   * as an attribute value template. The string that results from
   * instantiating the attribute value template should be a URI reference.
   * It is not an error if the string is not a syntactically legal URI reference.
   *
   * @return Namespace attribute for this element
   */
  public AVT getNamespace()
  {
    return m_namespace_avt;
  }
  
  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if(null != m_name_avt)
      m_name_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_namespace_avt)
      m_namespace_avt.fixupVariables(vnames, cstate.getGlobalsSize());
  }


  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_ELEMENT;
  }

  /**
   * Return the node name.
   *
   * @return This element's name 
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_ELEMENT_STRING;
  }
   
  /**
   * Resolve the namespace into a prefix.  Meant to be
   * overidded by elemAttribute if this class is derived.
   *
   * @param rhandler The current result tree handler.
   * @param prefix The probable prefix if already known.
   * @param nodeNamespace  The namespace.
   *
   * @return The prefix to be used.
   */
  protected String resolvePrefix(SerializationHandler rhandler,
                                 String prefix, String nodeNamespace)
    throws TransformerException
  {

//    if (null != prefix && prefix.length() == 0)
//    {
//      String foundPrefix = rhandler.getPrefix(nodeNamespace);
//
//      // System.out.println("nsPrefix: "+nsPrefix);           
//      if (null == foundPrefix)
//        foundPrefix = "";
//    }
    return prefix;
  }
    
  /**
   * Create an element in the result tree.
   * The xsl:element element allows an element to be created with a
   * computed name. The expanded-name of the element to be created
   * is specified by a required name attribute and an optional namespace
   * attribute. The content of the xsl:element element is a template
   * for the attributes and children of the created element.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {

 	SerializationHandler rhandler = transformer.getSerializationHandler();
    XPathContext xctxt = transformer.getXPathContext();
    int sourceNode = xctxt.getCurrentNode();
    
    
    String nodeName = m_name_avt == null ? null : m_name_avt.evaluate(xctxt, sourceNode, this);

    String prefix = null;
    String nodeNamespace = "";

    // Only validate if an AVT was used.
    if ((nodeName != null) && (!m_name_avt.isSimple()) && (!XML11Char.isXML11ValidQName(nodeName)))
    {
      transformer.getMsgMgr().warn(
        this, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE,
        new Object[]{ Constants.ATTRNAME_NAME, nodeName });

      nodeName = null;
    }

    else if (nodeName != null)
    {
      prefix = QName.getPrefixPart(nodeName);

      if (null != m_namespace_avt)
      {
        nodeNamespace = m_namespace_avt.evaluate(xctxt, sourceNode, this);
        if (null == nodeNamespace || 
            (prefix != null && prefix.length()>0 && nodeNamespace.length()== 0) )
          transformer.getMsgMgr().error(
              this, XSLTErrorResources.ER_NULL_URI_NAMESPACE);
        else
        {
        // Determine the actual prefix that we will use for this nodeNamespace

        prefix = resolvePrefix(rhandler, prefix, nodeNamespace);
        if (null == prefix)
          prefix = "";

        if (prefix.length() > 0)
          nodeName = (prefix + ":" + QName.getLocalPart(nodeName));
        else
          nodeName = QName.getLocalPart(nodeName);
        }
      }

      // No namespace attribute was supplied. Use the namespace declarations
      // currently in effect for the xsl:element element.
      else    
      {
        try
        {
          // Maybe temporary, until I get this worked out.  test: axes59
          nodeNamespace = getNamespaceForPrefix(prefix);

          // If we get back a null nodeNamespace, that means that this prefix could
          // not be found in the table.  This is okay only for a default namespace
          // that has never been declared.

          if ( (null == nodeNamespace) && (prefix.length() == 0) )
            nodeNamespace = "";
          else if (null == nodeNamespace)
          {
            transformer.getMsgMgr().warn(
              this, XSLTErrorResources.WG_COULD_NOT_RESOLVE_PREFIX,
              new Object[]{ prefix });

            nodeName = null;
          }

        }
        catch (Exception ex)
        {
          transformer.getMsgMgr().warn(
            this, XSLTErrorResources.WG_COULD_NOT_RESOLVE_PREFIX,
            new Object[]{ prefix });

          nodeName = null;
        }
      }
    }

    constructNode(nodeName, prefix, nodeNamespace, transformer);
  }
  
  /**
   * Construct a node in the result tree.  This method is overloaded by 
   * xsl:attribute. At this class level, this method creates an element.
   * If the node is null, we instantiate only the content of the node in accordance
   * with section 7.1.2 of the XSLT 1.0 Recommendation.
   *
   * @param nodeName The name of the node, which may be <code>null</code>.  If <code>null</code>,
   *                 only the non-attribute children of this node will be processed.
   * @param prefix The prefix for the namespace, which may be <code>null</code>.
   *               If not <code>null</code>, this prefix will be mapped and unmapped.
   * @param nodeNamespace The namespace of the node, which may be not be <code>null</code>.
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  void constructNode(
          String nodeName, String prefix, String nodeNamespace, TransformerImpl transformer)
            throws TransformerException
  {

    boolean shouldAddAttrs;

    try
    {
      SerializationHandler rhandler = transformer.getResultTreeHandler();

      if (null == nodeName)
      {
        shouldAddAttrs = false;
      }
      else
      {
        if (null != prefix)
        {
          rhandler.startPrefixMapping(prefix, nodeNamespace, true);
        }

        rhandler.startElement(nodeNamespace, QName.getLocalPart(nodeName),
                              nodeName);

        super.execute(transformer);

        shouldAddAttrs = true;
      }

      transformer.executeChildTemplates(this, shouldAddAttrs);

      // Now end the element if name was valid
      if (null != nodeName)
      {
        rhandler.endElement(nodeNamespace, QName.getLocalPart(nodeName),
                            nodeName);
        if (null != prefix)
        {
          rhandler.endPrefixMapping(prefix);
        }
      }
    }
    catch (SAXException se)
    {
      throw new TransformerException(se);
    }
  }
  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  	{
  	  if(null != m_name_avt)
  		m_name_avt.callVisitors(visitor);
  		
  	  if(null != m_namespace_avt)
  		m_namespace_avt.callVisitors(visitor);
  	}
  		
    super.callChildVisitors(visitor, callAttrs);
  }

}
