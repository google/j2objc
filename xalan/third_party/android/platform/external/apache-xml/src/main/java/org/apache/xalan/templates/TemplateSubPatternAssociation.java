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
 * $Id: TemplateSubPatternAssociation.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.io.Serializable;

import javax.xml.transform.TransformerException;

import org.apache.xml.utils.QName;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.patterns.StepPattern;

/**
 * A class to contain a match pattern and it's corresponding template.
 * This class also defines a node in a match pattern linked list.
 */
class TemplateSubPatternAssociation implements Serializable, Cloneable
{
    static final long serialVersionUID = -8902606755229903350L;

  /** Step pattern           */
  StepPattern m_stepPattern;

  /** Template pattern          */
  private String m_pattern;

  /** The template element         */
  private ElemTemplate m_template;

  /** Next pattern         */
  private TemplateSubPatternAssociation m_next = null;

  /** Flag indicating whether this is wild card pattern          */
  private boolean m_wild;

  /** Target string for this match pattern           */
  private String m_targetString;

  /**
   * Construct a match pattern from a pattern and template.
   * @param template The node that contains the template for this pattern.
   * @param pattern An executable XSLT StepPattern.
   * @param pat For now a Nodelist that contains old-style element patterns.
   */
  TemplateSubPatternAssociation(ElemTemplate template, StepPattern pattern, String pat)
  {

    m_pattern = pat;
    m_template = template;
    m_stepPattern = pattern;
    m_targetString = m_stepPattern.getTargetString();
    m_wild = m_targetString.equals("*");
  }

  /**
   * Clone this object.
   *
   * @return The cloned object.
   *
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException
  {

    TemplateSubPatternAssociation tspa =
      (TemplateSubPatternAssociation) super.clone();

    tspa.m_next = null;

    return tspa;
  }

  /**
   * Get the target string of the pattern.  For instance, if the pattern is
   * "foo/baz/boo[@daba]", this string will be "boo".
   *
   * @return The "target" string.
   */
  public final String getTargetString()
  {
    return m_targetString;
  }

  /**
   * Set Target String for this template pattern  
   *
   *
   * @param key Target string to set
   */
  public void setTargetString(String key)
  {
    m_targetString = key;
  }

  /**
   * Tell if two modes match according to the rules of XSLT.
   *
   * @param m1 mode to match
   *
   * @return True if the given mode matches this template's mode
   */
  boolean matchMode(QName m1)
  {
    return matchModes(m1, m_template.getMode());
  }

  /**
   * Tell if two modes match according to the rules of XSLT.
   *
   * @param m1 First mode to match
   * @param m2 Second mode to match
   *
   * @return True if the two given modes match
   */
  private boolean matchModes(QName m1, QName m2)
  {
    return (((null == m1) && (null == m2))
            || ((null != m1) && (null != m2) && m1.equals(m2)));
  }

  /**
   * Return the mode associated with the template.
   *
   *
   * @param xctxt XPath context to use with this template
   * @param targetNode Target node
   * @param mode reference, which may be null, to the <a href="http://www.w3.org/TR/xslt#modes">current mode</a>.
   * @return The mode associated with the template.
   *
   * @throws TransformerException
   */
  public boolean matches(XPathContext xctxt, int targetNode, QName mode)
          throws TransformerException
  {

    double score = m_stepPattern.getMatchScore(xctxt, targetNode);

    return (XPath.MATCH_SCORE_NONE != score)
           && matchModes(mode, m_template.getMode());
  }

  /**
   * Tell if the pattern for this association is a wildcard.
   *
   * @return true if this pattern is considered to be a wild match.
   */
  public final boolean isWild()
  {
    return m_wild;
  }

  /**
   * Get associated XSLT StepPattern.
   *
   * @return An executable StepPattern object, never null.
   *
   */
  public final StepPattern getStepPattern()
  {
    return m_stepPattern;
  }

  /**
   * Get the pattern string for diagnostic purposes.
   *
   * @return The pattern string for diagnostic purposes.
   *
   */
  public final String getPattern()
  {
    return m_pattern;
  }

  /**
   * Return the position of the template in document
   * order in the stylesheet.
   *
   * @return The position of the template in the overall template order.
   */
  public int getDocOrderPos()
  {
    return m_template.getUid();
  }

  /**
   * Return the import level associated with the stylesheet into which  
   * this template is composed.
   *
   * @return The import level of this template.
   */
  public final int getImportLevel()
  {
    return m_template.getStylesheetComposed().getImportCountComposed();
  }

  /**
   * Get the assocated xsl:template.
   *
   * @return An ElemTemplate, never null.
   *
   */
  public final ElemTemplate getTemplate()
  {
    return m_template;
  }

  /**
   * Get the next association.
   *
   * @return A valid TemplateSubPatternAssociation, or null.
   */
  public final TemplateSubPatternAssociation getNext()
  {
    return m_next;
  }

  /**
   * Set the next element on this association
   * list, which should be equal or less in priority to
   * this association, and, if equal priority, should occur
   * before this template in document order.
   *
   * @param mp The next association to score if this one fails.
   *
   */
  public void setNext(TemplateSubPatternAssociation mp)
  {
    m_next = mp;
  }
}
