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
 * $Id: FuncLast.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath.functions;

import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;


/**
 * Execute the Last() function.
 * @xsl.usage advanced
 */
public class FuncLast extends Function
{
    static final long serialVersionUID = 9205812403085432943L;
  
  private boolean m_isTopLevel;
  
  /**
   * Figure out if we're executing a toplevel expression.
   * If so, we can't be inside of a predicate. 
   */
  public void postCompileStep(Compiler compiler)
  {
    m_isTopLevel = compiler.getLocationPathDepth() == -1;
  }

  /**
   * Get the position in the current context node list.
   *
   * @param xctxt non-null reference to XPath runtime context.
   *
   * @return The number of nodes in the list.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public int getCountOfContextNodeList(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {

    // assert(null != m_contextNodeList, "m_contextNodeList must be non-null");
    // If we're in a predicate, then this will return non-null.
    SubContextList iter = m_isTopLevel ? null : xctxt.getSubContextList();

    // System.out.println("iter: "+iter);
    if (null != iter)
      return iter.getLastPos(xctxt);

    DTMIterator cnl = xctxt.getContextNodeList();
    int count;
    if(null != cnl)
    {
      count = cnl.getLength();
      // System.out.println("count: "+count); 
    }
    else
      count = 0;   
    return count;
  }

  /**
   * Execute the function.  The function must return
   * a valid object.
   * @param xctxt The current execution context.
   * @return A valid XObject.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    XNumber xnum = new XNumber((double) getCountOfContextNodeList(xctxt));
    // System.out.println("last: "+xnum.num());
    return xnum;
  }
  
  /**
   * No arguments to process, so this does nothing.
   */
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    // no-op
  }

}
