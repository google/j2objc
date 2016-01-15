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
 * $Id: ElemApplyImport.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;

/**
 * Implement xsl:apply-imports.
 * <pre>
 * <!ELEMENT xsl:apply-imports EMPTY>
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#apply-imports">apply-imports in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemApplyImport extends ElemTemplateElement
{
    static final long serialVersionUID = 3764728663373024038L;

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return Token ID for xsl:apply-imports element types
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_APPLY_IMPORTS;
  }

  /**
   * Return the node name.
   *
   * @return Element name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_APPLY_IMPORTS_STRING;
  }

  /**
   * Execute the xsl:apply-imports transformation.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {

    if (transformer.currentTemplateRuleIsNull())
    {
      transformer.getMsgMgr().error(this,
        XSLTErrorResources.ER_NO_APPLY_IMPORT_IN_FOR_EACH);  //"xsl:apply-imports not allowed in a xsl:for-each");
    }

    int sourceNode = transformer.getXPathContext().getCurrentNode();
    if (DTM.NULL != sourceNode)
    {
      // supply the current templated (matched, not named)        
      ElemTemplate matchTemplate = transformer.getMatchedTemplate();
      transformer.applyTemplateToNode(this, matchTemplate, sourceNode);
    }
    else  // if(null == sourceNode)
    {
      transformer.getMsgMgr().error(this,
        XSLTErrorResources.ER_NULL_SOURCENODE_APPLYIMPORTS);  //"sourceNode is null in xsl:apply-imports!");
    }
  }

  /**
   * Add a child to the child list.
   * <!ELEMENT xsl:apply-imports EMPTY>
   *
   * @param newChild New element to append to this element's children list
   *
   * @return null, xsl:apply-Imports cannot have children 
   */
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {

    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  //"Can not add " +((ElemTemplateElement)newChild).m_elemName +

    //" to " + this.m_elemName);
    return null;
  }
}
