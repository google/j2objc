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
 * $Id: AVTPartXPath.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xml.utils.FastStringBuffer;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathFactory;
import org.apache.xpath.compiler.XPathParser;
import org.apache.xpath.objects.XObject;

/**
 * Simple string part of a complex AVT.
 * @xsl.usage internal
 */
public class AVTPartXPath extends AVTPart
{
    static final long serialVersionUID = -4460373807550527675L;

  /**
   * The XPath object contained in this part.
   * @serial
   */
  private XPath m_xpath;
  
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
    m_xpath.fixupVariables(vars, globalsSize);
  }
  
  /**
   * Tell if this expression or it's subexpressions can traverse outside 
   * the current subtree.
   * 
   * @return true if traversal outside the context node's subtree can occur.
   */
   public boolean canTraverseOutsideSubtree()
   {
    return m_xpath.getExpression().canTraverseOutsideSubtree();
   }

  /**
   * Construct a simple AVT part.
   *
   * @param xpath Xpath section of AVT 
   */
  public AVTPartXPath(XPath xpath)
  {
    m_xpath = xpath;
  }

  /**
   * Construct a simple AVT part.
   * 
   * @param val A pure string section of an AVT.
   * @param nsNode An object which can be used to determine the
   * Namespace Name (URI) for any Namespace prefix used in the XPath. 
   * Usually this is based on the context where the XPath was specified,
   * such as a node within a Stylesheet.
   * @param xpathProcessor XPath parser
   * @param factory XPath factory
   * @param liaison An XPathContext object, providing infomation specific
   * to this invocation and this thread. Maintains SAX output state, 
   * variables, error handler and so on, so the transformation/XPath 
   * object itself can be simultaneously invoked from multiple threads.
   *
   * @throws javax.xml.transform.TransformerException
   * TODO: Fix or remove this unused c'tor.
   */
  public AVTPartXPath(
          String val, org.apache.xml.utils.PrefixResolver nsNode, 
          XPathParser xpathProcessor, XPathFactory factory, 
          XPathContext liaison)
            throws javax.xml.transform.TransformerException
  {
    m_xpath = new XPath(val, null, nsNode, XPath.SELECT, liaison.getErrorListener());
  }

  /**
   * Get the AVT part as the original string.
   *
   * @return the AVT part as the original string.
   */
  public String getSimpleString()
  {
    return "{" + m_xpath.getPatternString() + "}";
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
   *
   * @throws javax.xml.transform.TransformerException
   */
  public void evaluate(
          XPathContext xctxt, FastStringBuffer buf, int context, org.apache.xml.utils.PrefixResolver nsNode)
            throws javax.xml.transform.TransformerException
  {

    XObject xobj = m_xpath.execute(xctxt, context, nsNode);

    if (null != xobj)
    {
      xobj.appendToFsb(buf);
    }
  }
  
  /**
   * @see XSLTVisitable#callVisitors(XSLTVisitor)
   */
  public void callVisitors(XSLTVisitor visitor)
  {
  	m_xpath.getExpression().callVisitors(m_xpath, visitor);
  }
}
