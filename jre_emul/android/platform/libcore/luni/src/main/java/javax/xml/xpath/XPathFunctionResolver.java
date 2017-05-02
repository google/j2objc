/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// $Id: XPathFunctionResolver.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.xpath;

import javax.xml.namespace.QName;

/**
 * <p><code>XPathFunctionResolver</code> provides access to the set of user defined <code>XPathFunction</code>s.</p>
 *
 * <p>XPath functions are resolved by name and arity.
 * The resolver is not needed for XPath built-in functions and the resolver
 * <strong><em>cannot</em></strong> be used to override those functions.</p>
 *
 * <p>In particular, the resolver is only called for functions in an another
 * namespace (functions with an explicit prefix). This means that you cannot
 * use the <code>XPathFunctionResolver</code> to implement specifications
 * like <a href="http://www.w3.org/TR/xmldsig-core/">XML-Signature Syntax
 * and Processing</a> which extend the function library of XPath 1.0 in the
 * same namespace. This is a consequence of the design of the resolver.</p>
 *
 * <p>If you wish to implement additional built-in functions, you will have to
 * extend the underlying implementation directly.</p>
 *
 * @author  <a href="mailto:Norman.Walsh@Sun.com">Norman Walsh</a>
 * @author  <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 446598 $, $Date: 2006-09-15 05:55:40 -0700 (Fri, 15 Sep 2006) $
 * @see <a href="http://www.w3.org/TR/xpath#corelib">XML Path Language (XPath) Version 1.0, Core Function Library</a>
 * @since 1.5
 */
public interface XPathFunctionResolver {
  /**
   * <p>Find a function in the set of available functions.</p>
   *
   * <p>If <code>functionName</code> or <code>arity</code> is <code>null</code>, then a <code>NullPointerException</code> is thrown.</p>
   *
   * @param functionName The function name.
   * @param arity The number of arguments that the returned function must accept.
   *
   * @return The function or <code>null</code> if no function named <code>functionName</code> with <code>arity</code> arguments exists.
   *
   * @throws NullPointerException If <code>functionName</code> or <code>arity</code> is <code>null</code>.
   */
  public XPathFunction resolveFunction(QName functionName, int arity);
}
