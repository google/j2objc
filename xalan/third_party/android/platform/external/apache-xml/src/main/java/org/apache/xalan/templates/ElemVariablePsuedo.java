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
 * $Id: ElemVariablePsuedo.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPath;

public class ElemVariablePsuedo extends ElemVariable
{
    static final long serialVersionUID = 692295692732588486L;
  XUnresolvedVariableSimple m_lazyVar;
	
  /**
   * Set the "select" attribute.
   * If the variable-binding element has a select attribute,
   * then the value of the attribute must be an expression and
   * the value of the variable is the object that results from
   * evaluating the expression. In this case, the content
   * of the variable must be empty.
   *
   * @param v Value to set for the "select" attribute.
   */
  public void setSelect(XPath v)
  {
    super.setSelect(v);
    m_lazyVar = new XUnresolvedVariableSimple(this);
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

    // if (TransformerImpl.S_DEBUG)
    //  transformer.getTraceManager().fireTraceEvent(this);

    // transformer.getXPathContext().getVarStack().pushVariable(m_qname, var);
    transformer.getXPathContext().getVarStack().setLocalVariable(m_index, m_lazyVar);
  }

}

