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
 * $Id: ElemExtensionScript.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

/**
 * Implement Script extension element
 * @xsl.usage internal
 */
public class ElemExtensionScript extends ElemTemplateElement
{
    static final long serialVersionUID = -6995978265966057744L;

  /**
   * Constructor ElemExtensionScript
   *
   */
  public ElemExtensionScript()
  {

    // System.out.println("ElemExtensionScript ctor");
  }

  /** Language used in extension.
   *  @serial          */
  private String m_lang = null;

  /**
   * Set language used by extension
   *
   *
   * @param v Language used by extension
   */
  public void setLang(String v)
  {
    m_lang = v;
  }

  /**
   * Get language used by extension
   *
   *
   * @return Language used by extension
   */
  public String getLang()
  {
    return m_lang;
  }

  /** Extension handler.
   *  @serial          */
  private String m_src = null;

  /**
   * Set Extension handler name for this extension
   *
   *
   * @param v Extension handler name to set
   */
  public void setSrc(String v)
  {
    m_src = v;
  }

  /**
   * Get Extension handler name for this extension
   *
   *
   * @return Extension handler name
   */
  public String getSrc()
  {
    return m_src;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element 
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_EXTENSIONSCRIPT;
  }
}
