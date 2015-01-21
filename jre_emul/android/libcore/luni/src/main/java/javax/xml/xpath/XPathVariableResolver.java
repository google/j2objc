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
// $Id: XPathVariableResolver.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.xpath;

import javax.xml.namespace.QName;

/**
 * <p><code>XPathVariableResolver</code> provides access to the set of user defined XPath variables.</p>
 *
 * <p>The <code>XPathVariableResolver</code> and the XPath evaluator must adhere to a contract that
 * cannot be directly enforced by the API.  Although variables may be mutable,
 * that is, an application may wish to evaluate the same XPath expression more
 * than once with different variable values, in the course of evaluating any
 * single XPath expression, a variable's value <strong><em>must</em></strong> be immutable.</p>
 *
 * @author  <a href="mailto:Norman.Walsh@Sun.com">Norman Walsh</a>
 * @author  <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 446598 $, $Date: 2006-09-15 05:55:40 -0700 (Fri, 15 Sep 2006) $
 * @since 1.5
 */
public interface XPathVariableResolver {
  /**
   * <p>Find a variable in the set of available variables.</p>
   *
   * <p>If <code>variableName</code> is <code>null</code>, then a <code>NullPointerException</code> is thrown.</p>
   *
   * @param variableName The <code>QName</code> of the variable name.
   *
   * @return The variables value, or <code>null</code> if no variable named <code>variableName</code>
   *   exists.  The value returned must be of a type appropriate for the underlying object model.
   *
   * @throws NullPointerException If <code>variableName</code> is <code>null</code>.
   */
  public Object resolveVariable(QName variableName);
}
