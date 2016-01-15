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
 * $Id: FuncKey.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.util.Hashtable;

import org.apache.xalan.transformer.KeyManager;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.UnionPathIterator;
import org.apache.xpath.functions.Function2Args;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XObject;

/**
 * Execute the Key() function.
 * @xsl.usage advanced
 */
public class FuncKey extends Function2Args
{
    static final long serialVersionUID = 9089293100115347340L;

  /** Dummy value to be used in usedrefs hashtable           */
  static private Boolean ISTRUE = new Boolean(true);

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

    // TransformerImpl transformer = (TransformerImpl)xctxt;
    TransformerImpl transformer = (TransformerImpl) xctxt.getOwnerObject();
    XNodeSet nodes = null;
    int context = xctxt.getCurrentNode();
    DTM dtm = xctxt.getDTM(context);
    int docContext = dtm.getDocumentRoot(context);

    if (DTM.NULL == docContext)
    {

      // path.error(context, XPATHErrorResources.ER_CONTEXT_HAS_NO_OWNERDOC); //"context does not have an owner document!");
    }

    String xkeyname = getArg0().execute(xctxt).str();
    QName keyname = new QName(xkeyname, xctxt.getNamespaceContext());
    XObject arg = getArg1().execute(xctxt);
    boolean argIsNodeSetDTM = (XObject.CLASS_NODESET == arg.getType());
    KeyManager kmgr = transformer.getKeyManager();
    
    // Don't bother with nodeset logic if the thing is only one node.
    if(argIsNodeSetDTM)
    {
    	XNodeSet ns = (XNodeSet)arg;
    	ns.setShouldCacheNodes(true);
    	int len = ns.getLength();
    	if(len <= 1)
    		argIsNodeSetDTM = false;
    }

    if (argIsNodeSetDTM)
    {
      Hashtable usedrefs = null;
      DTMIterator ni = arg.iter();
      int pos;
      UnionPathIterator upi = new UnionPathIterator();
      upi.exprSetParent(this);

      while (DTM.NULL != (pos = ni.nextNode()))
      {
        dtm = xctxt.getDTM(pos);
        XMLString ref = dtm.getStringValue(pos);

        if (null == ref)
          continue;

        if (null == usedrefs)
          usedrefs = new Hashtable();

        if (usedrefs.get(ref) != null)
        {
          continue;  // We already have 'em.
        }
        else
        {

          // ISTRUE being used as a dummy value.
          usedrefs.put(ref, ISTRUE);
        }

        XNodeSet nl =
          kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname, ref,
                               xctxt.getNamespaceContext());
                               
        nl.setRoot(xctxt.getCurrentNode(), xctxt);

//        try
//        {
          upi.addIterator(nl);
//        }
//        catch(CloneNotSupportedException cnse)
//        {
//          // will never happen.
//        }
        //mnodeset.addNodesInDocOrder(nl, xctxt); needed??
      }

      int current = xctxt.getCurrentNode();
      upi.setRoot(current, xctxt);

      nodes = new XNodeSet(upi);
    }
    else
    {
      XMLString ref = arg.xstr();
      nodes = kmgr.getNodeSetDTMByKey(xctxt, docContext, keyname,
                                                ref,
                                                xctxt.getNamespaceContext());
      nodes.setRoot(xctxt.getCurrentNode(), xctxt);
    }

    return nodes;
  }
}
