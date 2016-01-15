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
 * $Id: NodeSortKey.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import java.text.Collator;
import java.util.Locale;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xpath.XPath;

/**
 * Data structure for use by the NodeSorter class.
 * @xsl.usage internal
 */
class NodeSortKey
{

  /** Select pattern for this sort key          */
  XPath m_selectPat;

  /** Flag indicating whether to treat thee result as a number     */
  boolean m_treatAsNumbers;

  /** Flag indicating whether to sort in descending order      */
  boolean m_descending;

  /** Flag indicating by case          */
  boolean m_caseOrderUpper;

  /** Collator instance          */
  Collator m_col;

  /** Locale we're in          */
  Locale m_locale;

  /** Prefix resolver to use          */
  org.apache.xml.utils.PrefixResolver m_namespaceContext;

  /** Transformer instance          */
  TransformerImpl m_processor;  // needed for error reporting.

  /**
   * Constructor NodeSortKey
   *
   *
   * @param transformer non null transformer instance
   * @param selectPat Select pattern for this key 
   * @param treatAsNumbers Flag indicating whether the result will be a number
   * @param descending Flag indicating whether to sort in descending order
   * @param langValue Lang value to use to get locale
   * @param caseOrderUpper Flag indicating whether case is relevant
   * @param namespaceContext Prefix resolver
   *
   * @throws javax.xml.transform.TransformerException
   */
  NodeSortKey(
          TransformerImpl transformer, XPath selectPat, boolean treatAsNumbers, 
          boolean descending, String langValue, boolean caseOrderUpper, 
          org.apache.xml.utils.PrefixResolver namespaceContext)
            throws javax.xml.transform.TransformerException
  {

    m_processor = transformer;
    m_namespaceContext = namespaceContext;
    m_selectPat = selectPat;
    m_treatAsNumbers = treatAsNumbers;
    m_descending = descending;
    m_caseOrderUpper = caseOrderUpper;

    if (null != langValue && m_treatAsNumbers == false)
    {
      // See http://nagoya.apache.org/bugzilla/show_bug.cgi?id=2851
      // The constructor of Locale is defined as 
      //   public Locale(String language, String country)
      // with
      //   language - lowercase two-letter ISO-639 code
      //   country - uppercase two-letter ISO-3166 code
      // a) language must be provided as a lower-case ISO-code 
      //    instead of an upper-case code
      // b) country must be provided as an ISO-code 
      //    instead of a full localized country name (e.g. "France")
      m_locale = new Locale(langValue.toLowerCase(), 
                  Locale.getDefault().getCountry());
                  
      // (old, before bug report 2851).
      //  m_locale = new Locale(langValue.toUpperCase(),
      //                        Locale.getDefault().getDisplayCountry());                    

      if (null == m_locale)
      {

        // m_processor.warn("Could not find locale for <sort xml:lang="+langValue);
        m_locale = Locale.getDefault();
      }
    }
    else
    {
      m_locale = Locale.getDefault();
    }

    m_col = Collator.getInstance(m_locale);

    if (null == m_col)
    {
      m_processor.getMsgMgr().warn(null, XSLTErrorResources.WG_CANNOT_FIND_COLLATOR,
                                   new Object[]{ langValue });  //"Could not find Collator for <sort xml:lang="+langValue);

      m_col = Collator.getInstance();
    }
  }
}
