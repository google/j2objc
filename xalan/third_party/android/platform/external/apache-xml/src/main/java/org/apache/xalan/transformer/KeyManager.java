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
 * $Id: KeyManager.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import java.util.Vector;

import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.XMLString;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNodeSet;

/**
 * This class manages the key tables.
 */
public class KeyManager
{

  /**
   * Table of tables of element keys.
   * @see org.apache.xalan.transformer.KeyTable
   */
  private transient Vector m_key_tables = null;

  /**
   * Given a valid element key, return the corresponding node list.
   *
   * @param xctxt The XPath runtime state
   * @param doc The document node
   * @param name The key element name
   * @param ref The key value we're looking for 
   * @param nscontext The prefix resolver for the execution context
   *
   * @return A nodelist of nodes mathing the given key
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XNodeSet getNodeSetDTMByKey(
          XPathContext xctxt, int doc, QName name, XMLString ref, PrefixResolver nscontext)
            throws javax.xml.transform.TransformerException
  {

    XNodeSet nl = null;
    ElemTemplateElement template = (ElemTemplateElement) nscontext;  // yuck -sb

    if ((null != template)
            && null != template.getStylesheetRoot().getKeysComposed())
    {
      boolean foundDoc = false;

      if (null == m_key_tables)
      {
        m_key_tables = new Vector(4);
      }
      else
      {
        int nKeyTables = m_key_tables.size();

        for (int i = 0; i < nKeyTables; i++)
        {
          KeyTable kt = (KeyTable) m_key_tables.elementAt(i);

          if (kt.getKeyTableName().equals(name) && doc == kt.getDocKey())
          {
            nl = kt.getNodeSetDTMByKey(name, ref);

            if (nl != null)
            {
              foundDoc = true;

              break;
            }
          }
        }
      }

      if ((null == nl) &&!foundDoc /* && m_needToBuildKeysTable */)
      {
        KeyTable kt =
          new KeyTable(doc, nscontext, name,
                       template.getStylesheetRoot().getKeysComposed(),
                       xctxt);

        m_key_tables.addElement(kt);

        if (doc == kt.getDocKey())
        {
          foundDoc = true;
          nl = kt.getNodeSetDTMByKey(name, ref);
        }
      }
    }

    return nl;
  }
}
