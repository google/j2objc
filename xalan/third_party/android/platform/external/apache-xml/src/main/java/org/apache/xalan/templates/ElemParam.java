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
 * $Id: ElemParam.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.VariableStack;
import org.apache.xpath.objects.XObject;

/**
 * Implement xsl:param.
 * <pre>
 * <!ELEMENT xsl:param %template;>
 * <!ATTLIST xsl:param
 *   name %qname; #REQUIRED
 *   select %expr; #IMPLIED
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#variables">variables in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemParam extends ElemVariable
{
    static final long serialVersionUID = -1131781475589006431L;
  int m_qnameID;

  /**
   * Constructor ElemParam
   *
   */
  public ElemParam(){}

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID of the element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_PARAMVARIABLE;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_PARAMVARIABLE_STRING;
  }

  /**
   * Copy constructor.
   *
   * @param param Element from an xsl:param
   *
   * @throws TransformerException
   */
  public ElemParam(ElemParam param) throws TransformerException
  {
    super(param);
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
    m_qnameID = sroot.getComposeState().getQNameID(m_qname);
    int parentToken = m_parentNode.getXSLToken();
    if (parentToken == Constants.ELEMNAME_TEMPLATE
        || parentToken == Constants.EXSLT_ELEMNAME_FUNCTION)
      ((ElemTemplate)m_parentNode).m_inArgsSize++;
  }
  
  /**
   * Execute a variable declaration and push it onto the variable stack.
   * @see <a href="http://www.w3.org/TR/xslt#variables">variables in XSLT Specification</a>
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(TransformerImpl transformer) throws TransformerException
  {
    VariableStack vars = transformer.getXPathContext().getVarStack();
    
    if(!vars.isLocalSet(m_index))
    {

      int sourceNode = transformer.getXPathContext().getCurrentNode();
      XObject var = getValue(transformer, sourceNode);
  
      // transformer.getXPathContext().getVarStack().pushVariable(m_qname, var);
      transformer.getXPathContext().getVarStack().setLocalVariable(m_index, var);
    }
  }
  
}
