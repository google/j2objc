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
// $Id: XPath.java 569998 2007-08-27 04:40:02Z mrglavas $

package javax.xml.xpath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import org.xml.sax.InputSource;

/**
 * <p><code>XPath</code> provides access to the XPath evaluation environment and expressions.</p>
 *
 * <table id="XPath-evaluation" border="1" cellpadding="2">
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
 *        If the expression contains a variable reference, its value will be found through the {@link XPathVariableResolver}
 *        set with {@link #setXPathVariableResolver(XPathVariableResolver resolver)}.
 *        An {@link XPathExpressionException} is raised if the variable resolver is undefined or
 *        the resolver returns <code>null</code> for the variable.
 *        The value of a variable must be immutable through the course of any single evaluation.</p>
 *      </td>
 *    </tr>
 *    <tr>
 *      <td>functions</td>
 *      <td>
 *        If the expression contains a function reference, the function will be found through the {@link XPathFunctionResolver}
 *        set with {@link #setXPathFunctionResolver(XPathFunctionResolver resolver)}.
 *        An {@link XPathExpressionException} is raised if the function resolver is undefined or
 *        the function resolver returns <code>null</code> for the function.</p>
 *      </td>
 *    </tr>
 *    <tr>
 *      <td>QNames</td>
 *      <td>
 *        QNames in the expression are resolved against the XPath namespace context
 *        set with {@link #setNamespaceContext(NamespaceContext nsContext)}.
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
 * @author  <a href="Norman.Walsh@Sun.com">Norman Walsh</a>
 * @author  <a href="Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 569998 $, $Date: 2007-08-26 21:40:02 -0700 (Sun, 26 Aug 2007) $
 * @see <a href="http://www.w3.org/TR/xpath">XML Path Language (XPath) Version 1.0</a>
 * @since 1.5
 */
public interface XPath {

    /**
     * <p>Reset this <code>XPath</code> to its original configuration.</p>
     *
     * <p><code>XPath</code> is reset to the same state as when it was created with
     * {@link XPathFactory#newXPath()}.
     * <code>reset()</code> is designed to allow the reuse of existing <code>XPath</code>s
     * thus saving resources associated with the creation of new <code>XPath</code>s.</p>
     *
     * <p>The reset <code>XPath</code> is not guaranteed to have the same {@link XPathFunctionResolver}, {@link XPathVariableResolver}
     * or {@link NamespaceContext} <code>Object</code>s, e.g. {@link Object#equals(Object obj)}.
     * It is guaranteed to have a functionally equal <code>XPathFunctionResolver</code>, <code>XPathVariableResolver</code>
     * and <code>NamespaceContext</code>.</p>
     */
    public void reset();

    /**
     * <p>Establish a variable resolver.</p>
     *
     * <p>A <code>NullPointerException</code> is thrown if <code>resolver</code> is <code>null</code>.</p>
     *
     * @param resolver Variable resolver.
     *
     *  @throws NullPointerException If <code>resolver</code> is <code>null</code>.
     */
    public void setXPathVariableResolver(XPathVariableResolver resolver);

    /**
       * <p>Return the current variable resolver.</p>
       *
       * <p><code>null</code> is returned in no variable resolver is in effect.</p>
       *
       * @return Current variable resolver.
       */
    public XPathVariableResolver getXPathVariableResolver();

    /**
       * <p>Establish a function resolver.</p>
       *
       * <p>A <code>NullPointerException</code> is thrown if <code>resolver</code> is <code>null</code>.</p>
       *
       * @param resolver XPath function resolver.
       *
       * @throws NullPointerException If <code>resolver</code> is <code>null</code>.
       */
    public void setXPathFunctionResolver(XPathFunctionResolver resolver);

    /**
       * <p>Return the current function resolver.</p>
       *
       * <p><code>null</code> is returned in no function resolver is in effect.</p>
       *
       * @return Current function resolver.
       */
    public XPathFunctionResolver getXPathFunctionResolver();

    /**
       * <p>Establish a namespace context.</p>
       *
       * <p>A <code>NullPointerException</code> is thrown if <code>nsContext</code> is <code>null</code>.</p>
       *
       * @param nsContext Namespace context to use.
       *
       * @throws NullPointerException If <code>nsContext</code> is <code>null</code>.
       */
    public void setNamespaceContext(NamespaceContext nsContext);

    /**
       * <p>Return the current namespace context.</p>
       *
       * <p><code>null</code> is returned in no namespace context is in effect.</p>
       *
       * @return Current Namespace context.
       */
    public NamespaceContext getNamespaceContext();

    /**
       * <p>Compile an XPath expression for later evaluation.</p>
       *
       * <p>If <code>expression</code> contains any {@link XPathFunction}s,
       * they must be available via the {@link XPathFunctionResolver}.
       * An {@link XPathExpressionException} will be thrown if the <code>XPathFunction</code>
       * cannot be resolved with the <code>XPathFunctionResolver</code>.</p>
       *
       * <p>If <code>expression</code> is <code>null</code>, a <code>NullPointerException</code> is thrown.</p>
       *
       * @param expression The XPath expression.
       *
       * @return Compiled XPath expression.

       * @throws XPathExpressionException If <code>expression</code> cannot be compiled.
       * @throws NullPointerException If <code>expression</code> is <code>null</code>.
       */
    public XPathExpression compile(String expression)
        throws XPathExpressionException;

    /**
     * <p>Evaluate an <code>XPath</code> expression in the specified context and return the result as the specified type.</p>
     *
     * <p>See <a href="#XPath-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and <code>QName</code> resolution and return type conversion.</p>
     *
     * <p>If <code>returnType</code> is not one of the types defined in {@link XPathConstants} (
     * {@link XPathConstants#NUMBER NUMBER},
     * {@link XPathConstants#STRING STRING},
     * {@link XPathConstants#BOOLEAN BOOLEAN},
     * {@link XPathConstants#NODE NODE} or
     * {@link XPathConstants#NODESET NODESET})
     * then an <code>IllegalArgumentException</code> is thrown.</p>
     *
     * <p>If a <code>null</code> value is provided for
     * <code>item</code>, an empty document will be used for the
     * context.
     * If <code>expression</code> or <code>returnType</code> is <code>null</code>, then a
     * <code>NullPointerException</code> is thrown.</p>
     *
     * @param expression The XPath expression.
     * @param item The starting context (node or node list, for example).
     * @param returnType The desired return type.
     *
     * @return Result of evaluating an XPath expression as an <code>Object</code> of <code>returnType</code>.
     *
     * @throws XPathExpressionException If <code>expression</code> cannot be evaluated.
     * @throws IllegalArgumentException If <code>returnType</code> is not one of the types defined in {@link XPathConstants}.
     * @throws NullPointerException If <code>expression</code> or <code>returnType</code> is <code>null</code>.
     */
    public Object evaluate(String expression, Object item, QName returnType)
        throws XPathExpressionException;

    /**
     * <p>Evaluate an XPath expression in the specified context and return the result as a <code>String</code>.</p>
     *
     * <p>This method calls {@link #evaluate(String expression, Object item, QName returnType)} with a <code>returnType</code> of
     * {@link XPathConstants#STRING}.</p>
     *
     * <p>See <a href="#XPath-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and QName resolution and return type conversion.</p>
     *
     * <p>If a <code>null</code> value is provided for
     * <code>item</code>, an empty document will be used for the
     * context.
     * If <code>expression</code> is <code>null</code>, then a <code>NullPointerException</code> is thrown.</p>
     *
     * @param expression The XPath expression.
     * @param item The starting context (node or node list, for example).
     *
     * @return The <code>String</code> that is the result of evaluating the expression and
     *   converting the result to a <code>String</code>.
     *
     * @throws XPathExpressionException If <code>expression</code> cannot be evaluated.
     * @throws NullPointerException If <code>expression</code> is <code>null</code>.
     */
    public String evaluate(String expression, Object item)
        throws XPathExpressionException;

    /**
     * <p>Evaluate an XPath expression in the context of the specified <code>InputSource</code>
     * and return the result as the specified type.</p>
     *
     * <p>This method builds a data model for the {@link InputSource} and calls
     * {@link #evaluate(String expression, Object item, QName returnType)} on the resulting document object.</p>
     *
     * <p>See <a href="#XPath-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and QName resolution and return type conversion.</p>
     *
     * <p>If <code>returnType</code> is not one of the types defined in {@link XPathConstants},
     * then an <code>IllegalArgumentException</code> is thrown.</p>
     *
     * <p>If <code>expression</code>, <code>source</code> or <code>returnType</code> is <code>null</code>,
     * then a <code>NullPointerException</code> is thrown.</p>
     *
     * @param expression The XPath expression.
     * @param source The input source of the document to evaluate over.
     * @param returnType The desired return type.
     *
     * @return The <code>Object</code> that encapsulates the result of evaluating the expression.
     *
     * @throws XPathExpressionException If expression cannot be evaluated.
     * @throws IllegalArgumentException If <code>returnType</code> is not one of the types defined in {@link XPathConstants}.
     * @throws NullPointerException If <code>expression</code>, <code>source</code> or <code>returnType</code>
     *   is <code>null</code>.
     */
    public Object evaluate(
        String expression,
        InputSource source,
        QName returnType)
        throws XPathExpressionException;

    /**
     * <p>Evaluate an XPath expression in the context of the specified <code>InputSource</code>
     * and return the result as a <code>String</code>.</p>
     *
     * <p>This method calls {@link #evaluate(String expression, InputSource source, QName returnType)} with a
     * <code>returnType</code> of {@link XPathConstants#STRING}.</p>
     *
     * <p>See <a href="#XPath-evaluation">Evaluation of XPath Expressions</a> for context item evaluation,
     * variable, function and QName resolution and return type conversion.</p>
     *
     * <p>If <code>expression</code> or <code>source</code> is <code>null</code>,
     * then a <code>NullPointerException</code> is thrown.</p>
     *
     * @param expression The XPath expression.
     * @param source The <code>InputSource</code> of the document to evaluate over.
     *
     * @return The <code>String</code> that is the result of evaluating the expression and
     *   converting the result to a <code>String</code>.
     *
     * @throws XPathExpressionException If expression cannot be evaluated.
     * @throws NullPointerException If <code>expression</code> or <code>source</code> is <code>null</code>.
     */
    public String evaluate(String expression, InputSource source)
        throws XPathExpressionException;
}
