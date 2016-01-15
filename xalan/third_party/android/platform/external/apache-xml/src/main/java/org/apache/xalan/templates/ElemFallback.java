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
 * $Id: ElemFallback.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;

/**
 * Implement xsl:fallback.
 * <pre>
 * <!ELEMENT xsl:fallback %template;>
 * <!ATTLIST xsl:fallback %space-att;>
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#fallback">fallback in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemFallback extends ElemTemplateElement
{
    static final long serialVersionUID = 1782962139867340703L;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_FALLBACK;
  }

  /**
   * Return the node name.
   *
   * @return The Element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_FALLBACK_STRING;
  }

  /**
   * This is the normal call when xsl:fallback is instantiated.
   * In accordance with the XSLT 1.0 Recommendation, chapter 15,
   * "Normally, instantiating an xsl:fallback element does nothing."
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
  }

  /**
   * Execute the fallback elements.  This must be explicitly called to
   * instantiate the content of an xsl:fallback element.
   * When an XSLT transformer performs fallback for an instruction
   * element, if the instruction element has one or more xsl:fallback
   * children, then the content of each of the xsl:fallback children
   * must be instantiated in sequence; otherwise, an error must
   * be signaled. The content of an xsl:fallback element is a template.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void executeFallback(
          TransformerImpl transformer)
            throws TransformerException
  {

    int parentElemType = m_parentNode.getXSLToken();
    if (Constants.ELEMNAME_EXTENSIONCALL == parentElemType 
        || Constants.ELEMNAME_UNDEFINED == parentElemType)
    {

      transformer.executeChildTemplates(this, true);

    }
    else
    {

      // Should never happen
      System.out.println(
        "Error!  parent of xsl:fallback must be an extension or unknown element!");
    }
  }
}
