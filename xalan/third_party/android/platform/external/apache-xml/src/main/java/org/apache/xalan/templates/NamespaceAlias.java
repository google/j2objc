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
 * $Id: NamespaceAlias.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

/**
 * Object to hold an xsl:namespace element.
 * A stylesheet can use the xsl:namespace-alias element to declare
 * that one namespace URI is an alias for another namespace URI.
 * @see <a href="http://www.w3.org/TR/xslt#literal-result-element">literal-result-element in XSLT Specification</a>
 */
public class NamespaceAlias extends ElemTemplateElement
{
    static final long serialVersionUID = 456173966637810718L;
  
  /**
   * Constructor NamespaceAlias
   * 
   * @param docOrderNumber The document order number
   *
   */
  public NamespaceAlias(int docOrderNumber)
  {
    super();
    m_docOrderNumber = docOrderNumber;
  }

  /**
   * The "stylesheet-prefix" attribute.
   * @serial
   */
  private String m_StylesheetPrefix;

  /**
   * Set the "stylesheet-prefix" attribute.
   *
   * @param v non-null prefix value.
   */
  public void setStylesheetPrefix(String v)
  {
    m_StylesheetPrefix = v;
  }

  /**
   * Get the "stylesheet-prefix" attribute.
   *
   * @return non-null prefix value.
   */
  public String getStylesheetPrefix()
  {
    return m_StylesheetPrefix;
  }
  
  /**
   * The namespace in the stylesheet space.
   * @serial
   */
  private String m_StylesheetNamespace;

  /**
   * Set the value for the stylesheet namespace.
   *
   * @param v non-null prefix value.
   */
  public void setStylesheetNamespace(String v)
  {
    m_StylesheetNamespace = v;
  }

  /**
   * Get the value for the stylesheet namespace.
   *
   * @return non-null prefix value.
   */
  public String getStylesheetNamespace()
  {
    return m_StylesheetNamespace;
  }

  /**
   * The "result-prefix" attribute.
   * @serial
   */
  private String m_ResultPrefix;

  /**
   * Set the "result-prefix" attribute.
   *
   * @param v non-null prefix value.
   */
  public void setResultPrefix(String v)
  {
    m_ResultPrefix = v;
  }

  /**
   * Get the "result-prefix" attribute.
   *
   * @return non-null prefix value.
   */
  public String getResultPrefix()
  {
    return m_ResultPrefix;
  }
  
  /**
   * The result namespace.
   * @serial
   */
  private String m_ResultNamespace;

  /**
   * Set the result namespace.
   *
   * @param v non-null namespace value
   */
  public void setResultNamespace(String v)
  {
    m_ResultNamespace = v;
  }

  /**
   * Get the result namespace value.
   *
   * @return non-null namespace value.
   */
  public String getResultNamespace()
  {
    return m_ResultNamespace;
  }

  /**
   * This function is called to recompose() all of the namespace alias properties elements.
   * 
   * @param root The owning root stylesheet
   */
  public void recompose(StylesheetRoot root)
  {
    root.recomposeNamespaceAliases(this);
  }

}
