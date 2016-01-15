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
 * $Id: FuncId.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath.functions;

import java.util.StringTokenizer;

import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.StringVector;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.res.XPATHErrorResources;

/**
 * Execute the Id() function.
 * @xsl.usage advanced
 */
public class FuncId extends FunctionOneArg
{
    static final long serialVersionUID = 8930573966143567310L;

  /**
   * Fill in a list with nodes that match a space delimited list if ID 
   * ID references.
   *
   * @param xctxt The runtime XPath context.
   * @param docContext The document where the nodes are being looked for.
   * @param refval A space delimited list of ID references.
   * @param usedrefs List of references for which nodes were found.
   * @param nodeSet Node set where the nodes will be added to.
   * @param mayBeMore true if there is another set of nodes to be looked for.
   *
   * @return The usedrefs value.
   */
  private StringVector getNodesByID(XPathContext xctxt, int docContext,
                                    String refval, StringVector usedrefs,
                                    NodeSetDTM nodeSet, boolean mayBeMore)
  {

    if (null != refval)
    {
      String ref = null;
//      DOMHelper dh = xctxt.getDOMHelper();
      StringTokenizer tokenizer = new StringTokenizer(refval);
      boolean hasMore = tokenizer.hasMoreTokens();
      DTM dtm = xctxt.getDTM(docContext);

      while (hasMore)
      {
        ref = tokenizer.nextToken();
        hasMore = tokenizer.hasMoreTokens();

        if ((null != usedrefs) && usedrefs.contains(ref))
        {
          ref = null;

          continue;
        }

        int node = dtm.getElementById(ref);

        if (DTM.NULL != node)
          nodeSet.addNodeInDocOrder(node, xctxt);

        if ((null != ref) && (hasMore || mayBeMore))
        {
          if (null == usedrefs)
            usedrefs = new StringVector();

          usedrefs.addElement(ref);
        }
      }
    }

    return usedrefs;
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

    int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    int docContext = dtm.getDocument();

    if (DTM.NULL == docContext)
      error(xctxt, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC, null);

    XObject arg = m_arg0.execute(xctxt);
    int argType = arg.getType();
    XNodeSet nodes = new XNodeSet(xctxt.getDTMManager());
    NodeSetDTM nodeSet = nodes.mutableNodeset();

    if (XObject.CLASS_NODESET == argType)
    {
      DTMIterator ni = arg.iter();
      StringVector usedrefs = null;
      int pos = ni.nextNode();

      while (DTM.NULL != pos)
      {
        DTM ndtm = ni.getDTM(pos);
        String refval = ndtm.getStringValue(pos).toString();

        pos = ni.nextNode();
        usedrefs = getNodesByID(xctxt, docContext, refval, usedrefs, nodeSet,
                                DTM.NULL != pos);
      }
      // ni.detach();
    }
    else if (XObject.CLASS_NULL == argType)
    {
      return nodes;
    }
    else
    {
      String refval = arg.str();

      getNodesByID(xctxt, docContext, refval, null, nodeSet, false);
    }

    return nodes;
  }
}
