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
 * $Id: KeyDeclaration.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;

/**
 * Holds the attribute declarations for the xsl:keys element.
 * A stylesheet declares a set of keys for each document using
 * the xsl:key element. When this set of keys contains a member
 * with node x, name y and value z, we say that node x has a key
 * with name y and value z.
 * @see <a href="http://www.w3.org/TR/xslt#key">key in XSLT Specification</a>
 * @xsl.usage internal
 */
public class KeyDeclaration extends ElemTemplateElement
{
    static final long serialVersionUID = 7724030248631137918L;

  /**
   * Constructs a new element representing the xsl:key.  The parameters
   * are needed to prioritize this key element as part of the recomposing
   * process.  For this element, they are not automatically created
   * because the element is never added on to the stylesheet parent.
   */
  public KeyDeclaration(Stylesheet parentNode, int docOrderNumber)
  {
    m_parentNode = parentNode;
    setUid(docOrderNumber);
  }

  /**
   * The "name" property.
   * @serial
   */
  private QName m_name;

  /**
   * Set the "name" attribute.
   * The name attribute specifies the name of the key. The value
   * of the name attribute is a QName, which is expanded as
   * described in [2.4 Qualified Names].
   *
   * @param name Value to set for the "name" attribute.
   */
  public void setName(QName name)
  {
    m_name = name;
  }

  /**
   * Get the "name" attribute.
   * The name attribute specifies the name of the key. The value
   * of the name attribute is a QName, which is expanded as
   * described in [2.4 Qualified Names].
   *
   * @return Value of the "name" attribute.
   */
  public QName getName()
  {
    return m_name;
  }
  
  /**
   * Return the node name.
   *
   * @return the element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_KEY_STRING;
  }


  /**
   * The "match" attribute.
   * @serial
   */
  private XPath m_matchPattern = null;

  /**
   * Set the "match" attribute.
   * The match attribute is a Pattern; an xsl:key element gives
   * information about the keys of any node that matches the
   * pattern specified in the match attribute.
   * @see <a href="http://www.w3.org/TR/xslt#patterns">patterns in XSLT Specification</a>
   *
   * @param v Value to set for the "match" attribute.
   */
  public void setMatch(XPath v)
  {
    m_matchPattern = v;
  }

  /**
   * Get the "match" attribute.
   * The match attribute is a Pattern; an xsl:key element gives
   * information about the keys of any node that matches the
   * pattern specified in the match attribute.
   * @see <a href="http://www.w3.org/TR/xslt#patterns">patterns in XSLT Specification</a>
   *
   * @return Value of the "match" attribute.
   */
  public XPath getMatch()
  {
    return m_matchPattern;
  }

  /**
   * The "use" attribute.
   * @serial
   */
  private XPath m_use;

  /**
   * Set the "use" attribute.
   * The use attribute is an expression specifying the values
   * of the key; the expression is evaluated once for each node
   * that matches the pattern.
   *
   * @param v Value to set for the "use" attribute.
   */
  public void setUse(XPath v)
  {
    m_use = v;
  }

  /**
   * Get the "use" attribute.
   * The use attribute is an expression specifying the values
   * of the key; the expression is evaluated once for each node
   * that matches the pattern.
   *
   * @return Value of the "use" attribute.
   */
  public XPath getUse()
  {
    return m_use;
  }
  
  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_KEY;
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
    if(null != m_matchPattern)
      m_matchPattern.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
    if(null != m_use)
      m_use.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
  }

  /**
   * This function is called during recomposition to
   * control how this element is composed.
   * @param root The root stylesheet for this transformation.
   */
  public void recompose(StylesheetRoot root)
  {
    root.recomposeKeys(this);
  }

}
