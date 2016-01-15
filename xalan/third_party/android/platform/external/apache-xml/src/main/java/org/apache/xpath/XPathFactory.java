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
 * $Id: XPathFactory.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath;

import javax.xml.transform.SourceLocator;

import org.apache.xml.utils.PrefixResolver;

/**
 * Factory class for creating an XPath.  Implementors of XPath derivatives
 * will need to make a derived class of this.
 * @xsl.usage advanced
 */
public interface XPathFactory
{

  /**
   * Create an XPath.
   *
   * @param exprString The XPath expression string.
   * @param locator The location of the expression string, mainly for diagnostic
   *                purposes.
   * @param prefixResolver This will be called in order to resolve prefixes 
   *        to namespace URIs.
   * @param type One of {@link org.apache.xpath.XPath#SELECT} or 
   *             {@link org.apache.xpath.XPath#MATCH}.
   *
   * @return an XPath ready for execution.
   */
  XPath create(String exprString, SourceLocator locator,
               PrefixResolver prefixResolver, int type);
}
