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
 * $Id: XMLNSDecl.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

/**
 * Represents an xmlns declaration
 */
public class XMLNSDecl
        implements java.io.Serializable // 20001009 jkess
{
    static final long serialVersionUID = 6710237366877605097L;

  /**
   * Constructor XMLNSDecl
   *
   * @param prefix non-null reference to prefix, using "" for default namespace.
   * @param uri non-null reference to namespace URI.
   * @param isExcluded true if this namespace declaration should normally be excluded.
   */
  public XMLNSDecl(String prefix, String uri, boolean isExcluded)
  {

    m_prefix = prefix;
    m_uri = uri;
    m_isExcluded = isExcluded;
  }

  /** non-null reference to prefix, using "" for default namespace.
   *  @serial */
  private String m_prefix;

  /**
   * Return the prefix.
   * @return The prefix that is associated with this URI, or null
   * if the XMLNSDecl is declaring the default namespace.
   */
  public String getPrefix()
  {
    return m_prefix;
  }

  /** non-null reference to namespace URI.
   *  @serial  */
  private String m_uri;

  /**
   * Return the URI.
   * @return The URI that is associated with this declaration.
   */
  public String getURI()
  {
    return m_uri;
  }

  /** true if this namespace declaration should normally be excluded.
   *  @serial  */
  private boolean m_isExcluded;

  /**
   * Tell if this declaration should be excluded from the
   * result namespace.
   *
   * @return true if this namespace declaration should normally be excluded.
   */
  public boolean getIsExcluded()
  {
    return m_isExcluded;
  }
}
