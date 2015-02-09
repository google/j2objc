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
// $Id: XPathExpression.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.xpath;

import javax.xml.namespace.QName;
import org.xml.sax.InputSource;

/**
 * <p><code>XPathExpression</code> provides access to compiled XPath expressions.</p>
 *
 * <table id="XPathExpression-evaluation" border="1" cellpadding="2">
 *   <thead>
 *     <tr>
 *       <th colspan="2">Evaluation of XPath Expressions.</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>context</td>
 *       <td>
 *         If a request is made to evaluate the expression in the absence
 * of a context item, an empty document node will be used for the context.
 * For the purposes of evaluating XPath expressions, a DocumentFragment
 * is treated like a Document node.
 *      </td>
 *    </tr>
 *    <tr>
 *      <td>variables</td>
 *      <td>
 *        If the expression contains a variable reference, its value will be found through the {@link XPathVariableResolver}.
 *        An {@link XPathExpressionException} is raised if the variable resolver is undefined or
 *        the resolver returns <code>null</code> for the variable.
 *        The value of a variable must be immutable through the course of any single evaluation.</p>
 *      </td>
 *    </tr>
 *    <tr>
 *      <td>functions</td>
 *      <td>
 *        If the expression contains a function reference, the function will be found through the {@link XPathFunctionResolver}.
 *        An {@link XPathExpressionException} is raised if the function resolver is undefined or
 *        the function resolver returns <code>null</code> for the function.</p>
 *      </td>
 *    </tr>
 *    <tr>
 *      <td>QNames</td>
 *      <td>
 *        QNames in the expression are resolved against the XPath namespace context.
 *      </td>
 *    </tr>
 *    <tr>
 *      <td>result</td>
 *      <td>
 *        This result of evaluating an expression is converted to an instance of the desired return type.
 *        Valid return types are defined in {@link XPathConstants}.
 *        Conversion to the return type follows XPath conversion rules.</p>
 *      </td>
 *    </tr>
 * </table>
 *
 * @author  <a href="mailto:Norman.Walsh@Sun.com">Norman Walsh</a>
 * @author  <a href="mailto:Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 446598 $, $Date: 2006-09-15 05:55:40 -0700 (Fri, 15 Sep 2006) $
 * @see <a href="http://www.w3.org/TR/xpath#section-Expressions">XML Path Language (XPath) Version 1.0, Expressions</a>
 * @since 1.5
 */
public interface XPathExpression {

    /**
     * <p>Evaluate the compiled XPath expression in the specified context and return the result as the specified type.</p>
     *
     * <p>See <a href="#XPathExpression-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and QName resolution and return type conversion.</p>
     *
     * <p>If <code>returnType</code> is not one of the types defined in {@link XPathConstants},
     * then an <code>IllegalArgumentException</code> is thrown.</p>
     *
     * <p>If a <code>null</code> value is provided for
     * <code>item</code>, an empty document will be used for the
     * context.
     * If <code>returnType</code> is <code>null</code>, then a <code>NullPointerException</code> is thrown.</p>
     *
     * @param item The starting context (node or node list, for example).
     * @param returnType The desired return type.
     *
     * @return The <code>Object</code> that is the result of evaluating the expression and converting the result to
     *   <code>returnType</code>.
     *
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws IllegalArgumentException If <code>returnType</code> is not one of the types defined in {@link XPathConstants}.
     * @throws NullPointerException If  <code>returnType</code> is <code>null</code>.
     */
    public Object evaluate(Object item, QName returnType)
        throws XPathExpressionException;

    /**
     * <p>Evaluate the compiled XPath expression in the specified context and return the result as a <code>String</code>.</p>
     *
     * <p>This method calls {@link #evaluate(Object item, QName returnType)} with a <code>returnType</code> of
     * {@link XPathConstants#STRING}.</p>
     *
     * <p>See <a href="#XPathExpression-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and QName resolution and return type conversion.</p>
     *
     * <p>If a <code>null</code> value is provided for
     * <code>item</code>, an empty document will be used for the
     * context.
     *
     * @param item The starting context (node or node list, for example).
     *
     * @return The <code>String</code> that is the result of evaluating the expression and converting the result to a
     *   <code>String</code>.
     *
     * @throws XPathExpressionException If the expression cannot be evaluated.
     */
    public String evaluate(Object item)
        throws XPathExpressionException;

    /**
     * <p>Evaluate the compiled XPath expression in the context of the specified <code>InputSource</code> and return the result as the
     * specified type.</p>
     *
     * <p>This method builds a data model for the {@link InputSource} and calls
     * {@link #evaluate(Object item, QName returnType)} on the resulting document object.</p>
     *
     * <p>See <a href="#XPathExpression-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and QName resolution and return type conversion.</p>
     *
     * <p>If <code>returnType</code> is not one of the types defined in {@link XPathConstants},
     * then an <code>IllegalArgumentException</code> is thrown.</p>
     *
     * <p>If <code>source</code> or <code>returnType</code> is <code>null</code>,
     * then a <code>NullPointerException</code> is thrown.</p>
     *
     * @param source The <code>InputSource</code> of the document to evaluate over.
     * @param returnType The desired return type.
     *
     * @return The <code>Object</code> that is the result of evaluating the expression and converting the result to
     *   <code>returnType</code>.
     *
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws IllegalArgumentException If <code>returnType</code> is not one of the types defined in {@link XPathConstants}.
     * @throws NullPointerException If  <code>source</code> or <code>returnType</code> is <code>null</code>.
     */
    public Object evaluate(InputSource source, QName returnType)
        throws XPathExpressionException;

    /**
     * <p>Evaluate the compiled XPath expression in the context of the specified <code>InputSource</code> and return the result as a
     * <code>String</code>.</p>
     *
     * <p>This method calls {@link #evaluate(InputSource source, QName returnType)} with a <code>returnType</code> of
     * {@link XPathConstants#STRING}.</p>
     *
     * <p>See <a href="#XPathExpression-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and QName resolution and return type conversion.</p>
     *
     * <p>If <code>source</code> is <code>null</code>, then a <code>NullPointerException</code> is thrown.</p>
     *
     * @param source The <code>InputSource</code> of the document to evaluate over.
     *
     * @return The <code>String</code> that is the result of evaluating the expression and converting the result to a
     *   <code>String</code>.
     *
     * @throws XPathExpressionException If the expression cannot be evaluated.
     * @throws NullPointerException If  <code>source</code> is <code>null</code>.
     */
    public String evaluate(InputSource source)
        throws XPathExpressionException;
}
