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
 * $Id: ElemWhen.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xpath.XPath;

/**
 * Implement xsl:when.
 * <pre>
 * <!ELEMENT xsl:when %template;>
 * <!ATTLIST xsl:when
 *   test %expr; #REQUIRED
 *   %space-att;
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#section-Conditional-Processing-with-xsl:choose">XXX in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemWhen extends ElemTemplateElement
{
    static final long serialVersionUID = 5984065730262071360L;

  /**
   * Each xsl:when element has a single attribute, test,
   * which specifies an expression.
   * @serial
   */
  private XPath m_test;

  /**
   * Set the "test" attribute.
   * Each xsl:when element has a single attribute, test,
   * which specifies an expression.
   *
   * @param v Value to set for the "test" attribute.
   */
  public void setTest(XPath v)
  {
    m_test = v;
  }

  /**
   * Get the "test" attribute.
   * Each xsl:when element has a single attribute, test,
   * which specifies an expression.
   *
   * @return Value of the "test" attribute.
   */
  public XPath getTest()
  {
    return m_test;
  }

  /**
   * Get an integer representation of the element type.
   *
   * @return An integer representation of the element, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_WHEN;
  }
  
  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) 
    throws javax.xml.transform.TransformerException
  {
    super.compose(sroot);
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if(null != m_test)
      m_test.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
  }

  /**
   * Return the node name.
   *
   * @return The node name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_WHEN_STRING;
  }

  /**
   * Constructor ElemWhen
   *
   */
  public ElemWhen(){}
  
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
