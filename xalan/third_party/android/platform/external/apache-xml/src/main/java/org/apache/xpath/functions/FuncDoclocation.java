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
 * $Id: FuncDoclocation.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath.functions;

import org.apache.xml.dtm.DTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;

/**
 * Execute the proprietary document-location() function, which returns
 * a node set of documents.
 * @xsl.usage advanced
 */
public class FuncDoclocation extends FunctionDef1Arg
{
    static final long serialVersionUID = 7469213946343568769L;

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

    int whereNode = getArg0AsNode(xctxt);
    String fileLocation = null;

    if (DTM.NULL != whereNode)
    {
      DTM dtm = xctxt.getDTM(whereNode);
      
      // %REVIEW%
      if (DTM.DOCUMENT_FRAGMENT_NODE ==  dtm.getNodeType(whereNode))
      {
        whereNode = dtm.getFirstChild(whereNode);
      }

      if (DTM.NULL != whereNode)
      {        
        fileLocation = dtm.getDocumentBaseURI();
//        int owner = dtm.getDocument();
//        fileLocation = xctxt.getSourceTreeManager().findURIFromDoc(owner);
      }
    }

    return new XString((null != fileLocation) ? fileLocation : "");
  }
}
