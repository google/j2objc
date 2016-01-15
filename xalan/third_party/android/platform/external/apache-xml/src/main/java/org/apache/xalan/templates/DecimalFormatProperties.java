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
 * $Id: DecimalFormatProperties.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.text.DecimalFormatSymbols;

import org.apache.xml.utils.QName;

/**
 * Implement xsl:decimal-format.
 * <pre>
 * <!ELEMENT xsl:decimal-format EMPTY>
 * <!ATTLIST xsl:decimal-format
 *   name %qname; #IMPLIED
 *   decimal-separator %char; "."
 *   grouping-separator %char; ","
 *   infinity CDATA "Infinity"
 *   minus-sign %char; "-"
 *   NaN CDATA "NaN"
 *   percent %char; "%"
 *   per-mille %char; "&#x2030;"
 *   zero-digit %char; "0"
 *   digit %char; "#"
 *   pattern-separator %char; ";"
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#format-number">format-number in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class DecimalFormatProperties extends ElemTemplateElement
{
    static final long serialVersionUID = -6559409339256269446L;

  /** An instance of DecimalFormatSymbols for this element.
   *  @serial       */
  DecimalFormatSymbols m_dfs;

  /**
   * Constructor DecimalFormatProperties
   *
   */
  public DecimalFormatProperties(int docOrderNumber)
  {

    m_dfs = new java.text.DecimalFormatSymbols();

    // Set default values, they can be overiden if necessary.  
    m_dfs.setInfinity(Constants.ATTRVAL_INFINITY);
    m_dfs.setNaN(Constants.ATTRVAL_NAN);

    m_docOrderNumber = docOrderNumber;
  }

  /**
   * Return the decimal format Symbols for this element.
   * <p>The xsl:decimal-format element declares a decimal-format,
   * which controls the interpretation of a format pattern used by
   * the format-number function. If there is a name attribute, then
   * the element declares a named decimal-format; otherwise, it
   * declares the default decimal-format. The value of the name
   * attribute is a QName, which is expanded as described in [2.4 Qualified Names].
   * It is an error to declare either the default decimal-format or a
   * decimal-format with a given name more than once (even with different
   * import precedence), unless it is declared every time with the same
   * value for all attributes (taking into account any default values).</p>
   * <p>The other attributes on xsl:decimal-format correspond to the
   * methods on the JDK 1.1 DecimalFormatSymbols class. For each get/set
   * method pair there is an attribute defined for the xsl:decimal-format
   * element.</p>
   *
   * @return the decimal format Symbols for this element.
   */
  public DecimalFormatSymbols getDecimalFormatSymbols()
  {
    return m_dfs;
  }

  /**
   * If there is a name attribute, then the element declares a named
   * decimal-format; otherwise, it declares the default decimal-format.
   * @serial
   */
  private QName m_qname = null;

  /**
   * Set the "name" attribute.
   * If there is a name attribute, then the element declares a named
   * decimal-format; otherwise, it declares the default decimal-format.
   *
   * @param qname The name to set as the "name" attribute.
   */
  public void setName(QName qname)
  {
    m_qname = qname;
  }

  /**
   * Get the "name" attribute.
   * If there is a name attribute, then the element declares a named
   * decimal-format; otherwise, it declares the default decimal-format.
   *
   * @return the value of the "name" attribute.
   */
  public QName getName()
  {

    if (m_qname == null)
      return new QName("");
    else
      return m_qname;
  }

  /**
   * Set the "decimal-separator" attribute.
   * decimal-separator specifies the character used for the decimal sign;
   * the default value is the period character (.).
   *
   * @param ds Character to set as decimal separator 
   */
  public void setDecimalSeparator(char ds)
  {
    m_dfs.setDecimalSeparator(ds);
  }

  /**
   * Get the "decimal-separator" attribute.
   * decimal-separator specifies the character used for the decimal sign;
   * the default value is the period character (.).
   *
   * @return the character to use as decimal separator
   */
  public char getDecimalSeparator()
  {
    return m_dfs.getDecimalSeparator();
  }

  /**
   * Set the "grouping-separator" attribute.
   * grouping-separator specifies the character used as a grouping
   * (e.g. thousands) separator; the default value is the comma character (,).
   *
   * @param gs Character to use a grouping separator 
   */
  public void setGroupingSeparator(char gs)
  {
    m_dfs.setGroupingSeparator(gs);
  }

  /**
   * Get the "grouping-separator" attribute.
   * grouping-separator specifies the character used as a grouping
   * (e.g. thousands) separator; the default value is the comma character (,).
   *
   * @return Character to use a grouping separator 
   */
  public char getGroupingSeparator()
  {
    return m_dfs.getGroupingSeparator();
  }

  /**
   * Set the "infinity" attribute.
   * infinity specifies the string used to represent infinity;
   * the default value is the string Infinity.
   *
   * @param inf String to use as the "infinity" attribute.
   */
  public void setInfinity(String inf)
  {
    m_dfs.setInfinity(inf);
  }

  /**
   * Get the "infinity" attribute.
   * infinity specifies the string used to represent infinity;
   * the default value is the string Infinity.
   *
   * @return String to use as the "infinity" attribute.
   */
  public String getInfinity()
  {
    return m_dfs.getInfinity();
  }

  /**
   * Set the "minus-sign" attribute.
   * minus-sign specifies the character used as the default minus sign; the
   * default value is the hyphen-minus character (-, #x2D).
   *
   * @param v Character to use as minus sign
   */
  public void setMinusSign(char v)
  {
    m_dfs.setMinusSign(v);
  }

  /**
   * Get the "minus-sign" attribute.
   * minus-sign specifies the character used as the default minus sign; the
   * default value is the hyphen-minus character (-, #x2D).
   *
   * @return Character to use as minus sign
   */
  public char getMinusSign()
  {
    return m_dfs.getMinusSign();
  }

  /**
   * Set the "NaN" attribute.
   * NaN specifies the string used to represent the NaN value;
   * the default value is the string NaN.
   *
   * @param v String to use as the "NaN" attribute.
   */
  public void setNaN(String v)
  {
    m_dfs.setNaN(v);
  }

  /**
   * Get the "NaN" attribute.
   * NaN specifies the string used to represent the NaN value;
   * the default value is the string NaN.
   *
   * @return String to use as the "NaN" attribute.
   */
  public String getNaN()
  {
    return m_dfs.getNaN();
  }
  
  /**
   * Return the node name.
   *
   * @return the element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_DECIMALFORMAT_STRING;
  }

  /**
   * Set the "percent" attribute.
   * percent specifies the character used as a percent sign; the default
   * value is the percent character (%).
   *
   * @param v Character to use as percent 
   */
  public void setPercent(char v)
  {
    m_dfs.setPercent(v);
  }

  /**
   * Get the "percent" attribute.
   * percent specifies the character used as a percent sign; the default
   * value is the percent character (%).
   *
   * @return Character to use as percent 
   */
  public char getPercent()
  {
    return m_dfs.getPercent();
  }

  /**
   * Set the "per-mille" attribute.
   * per-mille specifies the character used as a per mille sign; the default
   * value is the Unicode per-mille character (#x2030).
   *
   * @param v Character to use as per-mille
   */
  public void setPerMille(char v)
  {
    m_dfs.setPerMill(v);
  }

  /**
   * Get the "per-mille" attribute.
   * per-mille specifies the character used as a per mille sign; the default
   * value is the Unicode per-mille character (#x2030).
   *
   * @return Character to use as per-mille 
   */
  public char getPerMille()
  {
    return m_dfs.getPerMill();
  }
  
  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_DECIMALFORMAT;
  }
  
  /**
   * Set the "zero-digit" attribute.
   * zero-digit specifies the character used as the digit zero; the default
   * value is the digit zero (0).
   *
   * @param v Character to use as the digit zero
   */
  public void setZeroDigit(char v)
  {
    m_dfs.setZeroDigit(v);
  }

  /**
   * Get the "zero-digit" attribute.
   * zero-digit specifies the character used as the digit zero; the default
   * value is the digit zero (0).
   *
   * @return Character to use as the digit zero
   */
  public char getZeroDigit()
  {
    return m_dfs.getZeroDigit();
  }

  /**
   * Set the "digit" attribute.
   * digit specifies the character used for a digit in the format pattern;
   * the default value is the number sign character (#).
   *
   * @param v Character to use for a digit in format pattern
   */
  public void setDigit(char v)
  {
    m_dfs.setDigit(v);
  }

  /**
   * Get the "digit" attribute.
   * digit specifies the character used for a digit in the format pattern;
   * the default value is the number sign character (#).
   *
   * @return Character to use for a digit in format pattern
   */
  public char getDigit()
  {
    return m_dfs.getDigit();
  }

  /**
   * Set the "pattern-separator" attribute.
   * pattern-separator specifies the character used to separate positive
   * and negative sub patterns in a pattern; the default value is the
   * semi-colon character (;).
   *
   * @param v Character to use as a pattern separator
   */
  public void setPatternSeparator(char v)
  {
    m_dfs.setPatternSeparator(v);
  }

  /**
   * Get the "pattern-separator" attribute.
   * pattern-separator specifies the character used to separate positive
   * and negative sub patterns in a pattern; the default value is the
   * semi-colon character (;).
   *
   * @return Character to use as a pattern separator
   */
  public char getPatternSeparator()
  {
    return m_dfs.getPatternSeparator();
  }

  /**
   * This function is called to recompose() all of the decimal format properties elements.
   * 
   * @param root Stylesheet root
   */
  public void recompose(StylesheetRoot root)
  {
    root.recomposeDecimalFormats(this);
  }

}
