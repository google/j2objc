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
 * $Id: ElemPI.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPathContext;

/**
 * Implement xsl:processing-instruction.
 * <pre>
 * <!ELEMENT xsl:processing-instruction %char-template;>
 * <!ATTLIST xsl:processing-instruction
 *   name %avt; #REQUIRED
 *   %space-att;
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Creating-Processing-Instructions">section-Creating-Processing-Instructions in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemPI extends ElemTemplateElement
{
    static final long serialVersionUID = 5621976448020889825L;

  /**
   * The xsl:processing-instruction element has a required name
   * attribute that specifies the name of the processing instruction node.
   * The value of the name attribute is interpreted as an
   * attribute value template.
   * @serial
   */
  private AVT m_name_atv = null;

  /**
   * Set the "name" attribute.
   * DJD
   *
   * @param v Value for the name attribute
   */
  public void setName(AVT v)
  {
    m_name_atv = v;
  }

  /**
   * Get the "name" attribute.
   * DJD
   *
   * @return The value of the "name" attribute 
   */
  public AVT getName()
  {
    return m_name_atv;
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
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if(null != m_name_atv)
      m_name_atv.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
  }



  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for the element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_PI;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_PI_STRING;
  }

  /**
   * Create a processing instruction in the result tree.
   * The content of the xsl:processing-instruction element is a
   * template for the string-value of the processing instruction node.
   * @see <a href="http://www.w3.org/TR/xslt#section-Creating-Processing-Instructions">section-Creating-Processing-Instructions in XSLT Specification</a>
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
    int sourceNode = xctxt.getCurrentNode();
    
    String piName = m_name_atv == null ? null : m_name_atv.evaluate(xctxt, sourceNode, this);
    
    // Ignore processing instruction if name is null
    if (piName == null) return;

    if (piName.equalsIgnoreCase("xml"))
    {
     	transformer.getMsgMgr().warn(
        this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
              new Object[]{ Constants.ATTRNAME_NAME, piName });
		return;
    }
    
    // Only check if an avt was used (ie. this wasn't checked at compose time.)
    // Ignore processing instruction, if invalid
    else if ((!m_name_atv.isSimple()) && (!XML11Char.isXML11ValidNCName(piName)))
    {
     	transformer.getMsgMgr().warn(
        this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
              new Object[]{ Constants.ATTRNAME_NAME, piName });
		return;    	
    }

    // Note the content model is:
    // <!ENTITY % instructions "
    // %char-instructions;
    // | xsl:processing-instruction
    // | xsl:comment
    // | xsl:element
    // | xsl:attribute
    // ">
    String data = transformer.transformToString(this);

    try
    {
      transformer.getResultTreeHandler().processingInstruction(piName, data);
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }

  /**
   * Add a child to the child list.
   *
   * @param newChild Child to add to child list
   *
   * @return The child just added to the child list
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
}
