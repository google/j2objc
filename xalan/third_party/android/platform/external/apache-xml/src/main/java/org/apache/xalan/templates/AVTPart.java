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
 * $Id: AVTPart.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xml.utils.FastStringBuffer;
import org.apache.xpath.XPathContext;

/**
 * Class to hold a part, either a string or XPath,
 * of an Attribute Value Template.
 * @xsl.usage internal
 */
public abstract class AVTPart implements java.io.Serializable, XSLTVisitable
{
    static final long serialVersionUID = -1747749903613916025L;

  /**
   * Construct a part.
   */
  public AVTPart(){}

  /**
   * Get the AVT part as the original string.
   *
   * @return the AVT part as the original string.
   */
  public abstract String getSimpleString();

  /**
   * Write the evaluated value into the given
   * string buffer.
   *
   * @param xctxt The XPath context to use to evaluate this AVT.
   * @param buf Buffer to write into.
   * @param context The current source tree context.
   * @param nsNode The current namespace context (stylesheet tree context).
   *
   * @throws javax.xml.transform.TransformerException
   */
  public abstract void evaluate(
    XPathContext xctxt, FastStringBuffer buf, int context,
      org.apache.xml.utils.PrefixResolver nsNode)
        throws javax.xml.transform.TransformerException;

  /**
   * Set the XPath support.
   *
   * @param support XPathContext to set. 
   */
  public void setXPathSupport(XPathContext support){}
  
  /**
   * Tell if this expression or it's subexpressions can traverse outside 
   * the current subtree.
   * 
   * @return true if traversal outside the context node's subtree can occur.
   */
   public boolean canTraverseOutsideSubtree()
   {
    return false;
   }
   
  /**
   * This function is used to fixup variables from QNames to stack frame 
   * indexes at stylesheet build time.
   * @param vars List of QNames that correspond to variables.  This list 
   * should be searched backwards for the first qualified name that 
   * corresponds to the variable reference qname.  The position of the 
   * QName in the vector from the start of the vector will be its position 
   * in the stack frame (but variables above the globalsTop value will need 
   * to be offset to the current stack frame).
   */
  public abstract void fixupVariables(java.util.Vector vars, int globalsSize);


}
