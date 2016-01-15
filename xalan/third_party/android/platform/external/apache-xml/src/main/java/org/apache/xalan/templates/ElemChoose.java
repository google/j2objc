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
 * $Id: ElemChoose.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

/**
 * Implement xsl:choose.
 * <pre>
 * <!ELEMENT xsl:choose (xsl:when+, xsl:otherwise?)>
 * <!ATTLIST xsl:choose %space-att;>
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Conditional-Processing-with-xsl:choose">XXX in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemChoose extends ElemTemplateElement
{
    static final long serialVersionUID = -3070117361903102033L;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_CHOOSE;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_CHOOSE_STRING;
  }

  /**
   * Constructor ElemChoose
   *
   */
  public ElemChoose(){}

  /**
   * Execute the xsl:choose transformation.
   *
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer) throws TransformerException
  {

    boolean found = false;

    for (ElemTemplateElement childElem = getFirstChildElem();
            childElem != null; childElem = childElem.getNextSiblingElem())
    {
      int type = childElem.getXSLToken();

      if (Constants.ELEMNAME_WHEN == type)
      {
        found = true;

        ElemWhen when = (ElemWhen) childElem;

        // must be xsl:when
        XPathContext xctxt = transformer.getXPathContext();
        int sourceNode = xctxt.getCurrentNode();
        
        // System.err.println("\""+when.getTest().getPatternString()+"\"");
        
        // if(when.getTest().getPatternString().equals("COLLECTION/icuser/ictimezone/LITERAL='GMT +13:00 Pacific/Tongatapu'"))
        // 	System.err.println("Found COLLECTION/icuser/ictimezone/LITERAL");

          if (when.getTest().bool(xctxt, sourceNode, when)) {
              transformer.executeChildTemplates(when, true);

              return;
          }
      }
      else if (Constants.ELEMNAME_OTHERWISE == type)
      {
        found = true;

        // xsl:otherwise
        transformer.executeChildTemplates(childElem, true);

        return;
      }
    }

    if (!found)
      transformer.getMsgMgr().error(
        this, XSLTErrorResources.ER_CHOOSE_REQUIRES_WHEN);
  }

  /**
   * Add a child to the child list.
   *
   * @param newChild Child to add to this node's child list
   *
   * @return The child that was just added to the child list
   *
   * @throws DOMException
   */
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {

    int type = ((ElemTemplateElement) newChild).getXSLToken();

    switch (type)
    {
    case Constants.ELEMNAME_WHEN :
    case Constants.ELEMNAME_OTHERWISE :

      // TODO: Positional checking
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
   * Tell if this element can accept variable declarations.
   * @return true if the element can accept and process variable declarations.
   */
  public boolean canAcceptVariables()
  {
  	return false;
  }

}
