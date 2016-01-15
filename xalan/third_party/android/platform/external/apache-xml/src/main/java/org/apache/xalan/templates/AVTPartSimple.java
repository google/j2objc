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
 * $Id: AVTPartSimple.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xml.utils.FastStringBuffer;
import org.apache.xpath.XPathContext;

/**
 * Simple string part of a complex AVT.
 * @xsl.usage internal
 */
public class AVTPartSimple extends AVTPart
{
    static final long serialVersionUID = -3744957690598727913L;

  /**
   * Simple string value;
   * @serial
   */
  private String m_val;

  /**
   * Construct a simple AVT part.
   * @param val A pure string section of an AVT.
   */
  public AVTPartSimple(String val)
  {
    m_val = val;
  }

  /**
   * Get the AVT part as the original string.
   *
   * @return the AVT part as the original string.
   */
  public String getSimpleString()
  {
    return m_val;
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
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    // no-op
  }


  /**
   * Write the value into the buffer.
   *
   * @param xctxt An XPathContext object, providing infomation specific
   * to this invocation and this thread. Maintains SAX state, variables, 
   * error handler and  so on, so the transformation/XPath object itself
   * can be simultaneously invoked from multiple threads.
   * @param buf Buffer to write into.
   * @param context The current source tree context.
   * @param nsNode The current namespace context (stylesheet tree context).
   */
  public void evaluate(XPathContext xctxt, FastStringBuffer buf,
                       int context,
                       org.apache.xml.utils.PrefixResolver nsNode)
  {
    buf.append(m_val);
  }
  /**
   * @see XSLTVisitable#callVisitors(XSLTVisitor)
   */
  public void callVisitors(XSLTVisitor visitor)
  {
  	// Don't do anything for the subpart for right now.
  }

}
