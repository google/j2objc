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
 * $Id: ElemIf.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

/**
 * Implement xsl:if.
 * <pre>
 * <!ELEMENT xsl:if %template;>
 * <!ATTLIST xsl:if
 *   test %expr; #REQUIRED
 *   %space-att;
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Conditional-Processing-with-xsl:if">XXX in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemIf extends ElemTemplateElement
{
    static final long serialVersionUID = 2158774632427453022L;

  /**
   * The xsl:if element must have a test attribute, which specifies an expression.
   * @serial
   */
  private XPath m_test = null;

  /**
   * Set the "test" attribute.
   * The xsl:if element must have a test attribute, which specifies an expression.
   *
   * @param v test attribute to set
   */
  public void setTest(XPath v)
  {
    m_test = v;
  }

  /**
   * Get the "test" attribute.
   * The xsl:if element must have a test attribute, which specifies an expression.
   *
   * @return the "test" attribute for this element.
   */
  public XPath getTest()
  {
    return m_test;
  }

  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   *
   * @param sroot The root stylesheet.
   *
   * @throws TransformerException
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {

    super.compose(sroot);

    java.util.Vector vnames = sroot.getComposeState().getVariableNames();

    if (null != m_test)
      m_test.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_IF;
  }

  /**
   * Return the node name.
   *
   * @return the element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_IF_STRING;
  }

  /**
   * Conditionally execute a sub-template.
   * The expression is evaluated and the resulting object is converted
   * to a boolean as if by a call to the boolean function. If the result
   * is true, then the content template is instantiated; otherwise, nothing
   * is created.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer) throws TransformerException
  {

    XPathContext xctxt = transformer.getXPathContext();
    int sourceNode = xctxt.getCurrentNode();

      if (m_test.bool(xctxt, sourceNode, this)) {
          transformer.executeChildTemplates(this, true);
      }

  }
  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  		m_test.getExpression().callVisitors(m_test, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }

}
