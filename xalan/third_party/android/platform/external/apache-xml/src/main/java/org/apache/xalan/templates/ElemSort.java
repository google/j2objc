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
 * $Id: ElemSort.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xpath.XPath;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * Implement xsl:sort.
 * <pre>
 * <!ELEMENT xsl:sort EMPTY>
 * <!ATTLIST xsl:sort
 *   select %expr; "."
 *   lang %avt; #IMPLIED
 *   data-type %avt; "text"
 *   order %avt; "ascending"
 *   case-order %avt; #IMPLIED
 * >
 * <!-- xsl:sort cannot occur after any other elements or
 * any non-whitespace character -->
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#sorting">sorting in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemSort extends ElemTemplateElement
{
    static final long serialVersionUID = -4991510257335851938L;

  /**
   * xsl:sort has a select attribute whose value is an expression.
   * @serial
   */
  private XPath m_selectExpression = null;

  /**
   * Set the "select" attribute.
   * xsl:sort has a select attribute whose value is an expression.
   * For each node to be processed, the expression is evaluated
   * with that node as the current node and with the complete
   * list of nodes being processed in unsorted order as the current
   * node list. The resulting object is converted to a string as if
   * by a call to the string function; this string is used as the
   * sort key for that node. The default value of the select attribute
   * is ., which will cause the string-value of the current node to
   * be used as the sort key.
   *
   * @param v Value to set for the "select" attribute
   */
  public void setSelect(XPath v)
  {

    if (v.getPatternString().indexOf("{") < 0)
      m_selectExpression = v;
    else
      error(XSLTErrorResources.ER_NO_CURLYBRACE, null);
  }

  /**
   * Get the "select" attribute.
   * xsl:sort has a select attribute whose value is an expression.
   * For each node to be processed, the expression is evaluated
   * with that node as the current node and with the complete
   * list of nodes being processed in unsorted order as the current
   * node list. The resulting object is converted to a string as if
   * by a call to the string function; this string is used as the
   * sort key for that node. The default value of the select attribute
   * is ., which will cause the string-value of the current node to
   * be used as the sort key.
   *
   * @return The value of the "select" attribute
   */
  public XPath getSelect()
  {
    return m_selectExpression;
  }

  /**
   * lang specifies the language of the sort keys.
   * @serial
   */
  private AVT m_lang_avt = null;

  /**
   * Set the "lang" attribute.
   * lang specifies the language of the sort keys; it has the same
   * range of values as xml:lang [XML]; if no lang value is
   * specified, the language should be determined from the system environment.
   *
   * @param v The value to set for the "lang" attribute
   */
  public void setLang(AVT v)
  {
    m_lang_avt = v;
  }

  /**
   * Get the "lang" attribute.
   * lang specifies the language of the sort keys; it has the same
   * range of values as xml:lang [XML]; if no lang value is
   * specified, the language should be determined from the system environment.
   *
   * @return The value of the "lang" attribute
   */
  public AVT getLang()
  {
    return m_lang_avt;
  }

  /**
   * data-type specifies the data type of the
   * strings to be sorted.
   * @serial
   */
  private AVT m_dataType_avt = null;

  /**
   * Set the "data-type" attribute.
   * <code>data-type</code> specifies the data type of the
   * strings; the following values are allowed:
   * <ul>
   * <li>
   * <code>text</code> specifies that the sort keys should be
   * sorted lexicographically in the culturally correct manner for the
   * language specified by <code>lang</code>.
   * </li>
   * <li>
   * <code>number</code> specifies that the sort keys should be
   * converted to numbers and then sorted according to the numeric value;
   * the sort key is converted to a number as if by a call to the
   * <b><a href="http://www.w3.org/TR/xpath#function-number">number</a></b> function; the <code>lang</code>
   * attribute is ignored.
   * </li>
   * <li>
   * A <a href="http://www.w3.org/TR/REC-xml-names#NT-QName">QName</a> with a prefix
   * is expanded into an <a href="http://www.w3.org/TR/xpath#dt-expanded-name">expanded-name</a> as described
   * in <a href="#qname">[<b>2.4 Qualified Names</b>]</a>; the expanded-name identifies the data-type;
   * the behavior in this case is not specified by this document.
   * </li>
   * </ul>
   * <p>The default value is <code>text</code>.</p>
   * <blockquote>
   * <b>NOTE: </b>The XSL Working Group plans that future versions of XSLT will
   * leverage XML Schemas to define further values for this
   * attribute.</blockquote>
   *
   * @param v Value to set for the "data-type" attribute
   */
  public void setDataType(AVT v)
  {
    m_dataType_avt = v;
  }

  /**
   * Get the "data-type" attribute.
   * <code>data-type</code> specifies the data type of the
   * strings; the following values are allowed:
   * <ul>
   * <li>
   * <code>text</code> specifies that the sort keys should be
   * sorted lexicographically in the culturally correct manner for the
   * language specified by <code>lang</code>.
   * </li>
   * <li>
   * <code>number</code> specifies that the sort keys should be
   * converted to numbers and then sorted according to the numeric value;
   * the sort key is converted to a number as if by a call to the
   * <b><a href="http://www.w3.org/TR/xpath#function-number">number</a></b> function; the <code>lang</code>
   * attribute is ignored.
   * </li>
   * <li>
   * A <a href="http://www.w3.org/TR/REC-xml-names#NT-QName">QName</a> with a prefix
   * is expanded into an <a href="http://www.w3.org/TR/xpath#dt-expanded-name">expanded-name</a> as described
   * in <a href="#qname">[<b>2.4 Qualified Names</b>]</a>; the expanded-name identifies the data-type;
   * the behavior in this case is not specified by this document.
   * </li>
   * </ul>
   * <p>The default value is <code>text</code>.</p>
   * <blockquote>
   * <b>NOTE: </b>The XSL Working Group plans that future versions of XSLT will
   * leverage XML Schemas to define further values for this
   * attribute.</blockquote>
   *
   * @return The value of the "data-type" attribute
   */
  public AVT getDataType()
  {
    return m_dataType_avt;
  }

  /**
   * order specifies whether the strings should be sorted in ascending
   * or descending order.
   * @serial
   */
  private AVT m_order_avt = null;

  /**
   * Set the "order" attribute.
   * order specifies whether the strings should be sorted in ascending
   * or descending order; ascending specifies ascending order; descending
   * specifies descending order; the default is ascending.
   *
   * @param v The value to set for the "order" attribute
   */
  public void setOrder(AVT v)
  {
    m_order_avt = v;
  }

  /**
   * Get the "order" attribute.
   * order specifies whether the strings should be sorted in ascending
   * or descending order; ascending specifies ascending order; descending
   * specifies descending order; the default is ascending.
   *
   * @return The value of the "order" attribute
   */
  public AVT getOrder()
  {
    return m_order_avt;
  }

  /**
   * case-order has the value upper-first or lower-first.
   * The default value is language dependent.
   * @serial
   */
  private AVT m_caseorder_avt = null;

  /**
   * Set the "case-order" attribute.
   * case-order has the value upper-first or lower-first; this applies
   * when data-type="text", and specifies that upper-case letters should
   * sort before lower-case letters or vice-versa respectively.
   * For example, if lang="en", then A a B b are sorted with
   * case-order="upper-first" and a A b B are sorted with case-order="lower-first".
   * The default value is language dependent.
   *
   * @param v The value to set for the "case-order" attribute
   * 
   * @serial
   */
  public void setCaseOrder(AVT v)
  {
    m_caseorder_avt = v;
  }

  /**
   * Get the "case-order" attribute.
   * case-order has the value upper-first or lower-first; this applies
   * when data-type="text", and specifies that upper-case letters should
   * sort before lower-case letters or vice-versa respectively.
   * For example, if lang="en", then A a B b are sorted with
   * case-order="upper-first" and a A b B are sorted with case-order="lower-first".
   * The default value is language dependent.
   *
   * @return The value of the "case-order" attribute
   */
  public AVT getCaseOrder()
  {
    return m_caseorder_avt;
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID of the element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_SORT;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_SORT_STRING;
  }

  /**
   * Add a child to the child list.
   *
   * @param newChild Child to add to the child list
   *
   * @return Child just added to the child list
   *
   * @throws DOMException
   */
  public Node appendChild(Node newChild) throws DOMException
  {

    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  //"Can not add " +((ElemTemplateElement)newChild).m_elemName +

    //" to " + this.m_elemName);
    return null;
  }
  
  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) 
    throws javax.xml.transform.TransformerException
  {
    super.compose(sroot);
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if(null != m_caseorder_avt)
      m_caseorder_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_dataType_avt)
      m_dataType_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_lang_avt)
      m_lang_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_order_avt)
      m_order_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_selectExpression)
      m_selectExpression.fixupVariables(vnames, cstate.getGlobalsSize());
  }
}
