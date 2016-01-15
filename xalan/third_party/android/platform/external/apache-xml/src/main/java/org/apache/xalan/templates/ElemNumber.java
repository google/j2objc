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
 * $Id: ElemNumber.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.CountersTable;
import org.apache.xalan.transformer.DecimalToRoman;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.utils.FastStringBuffer;
import org.apache.xml.utils.NodeVector;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.StringBufferPool;
import org.apache.xml.utils.res.XResourceBundle;
import org.apache.xml.utils.res.CharArrayWrapper;
import org.apache.xml.utils.res.IntArrayWrapper;
import org.apache.xml.utils.res.LongArrayWrapper;
import org.apache.xml.utils.res.StringArrayWrapper;
import org.apache.xpath.NodeSetDTM;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

import org.w3c.dom.Node;

import org.xml.sax.SAXException;

// import org.apache.xalan.dtm.*;

/**
 * Implement xsl:number.
 * <pre>
 * <!ELEMENT xsl:number EMPTY>
 * <!ATTLIST xsl:number
 *    level (single|multiple|any) "single"
 *    count %pattern; #IMPLIED
 *    from %pattern; #IMPLIED
 *    value %expr; #IMPLIED
 *    format %avt; '1'
 *    lang %avt; #IMPLIED
 *    letter-value %avt; #IMPLIED
 *    grouping-separator %avt; #IMPLIED
 *    grouping-size %avt; #IMPLIED
 * >
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#number">number in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemNumber extends ElemTemplateElement 
{
    static final long serialVersionUID = 8118472298274407610L;

    /**
     * Chars for converting integers into alpha counts.
     * @see TransformerImpl#int2alphaCount
     */
    private CharArrayWrapper m_alphaCountTable = null;
    
    private class MyPrefixResolver implements PrefixResolver {
        
        DTM dtm;
        int handle;
        boolean handleNullPrefix;
        
		/**
		 * Constructor for MyPrefixResolver.
		 * @param xpathExpressionContext
		 */
		public MyPrefixResolver(Node xpathExpressionContext, DTM dtm, int handle, boolean handleNullPrefix) {
            this.dtm = dtm;
            this.handle = handle;
            this.handleNullPrefix = handleNullPrefix;
		}

    	/**
		 * @see PrefixResolver#getNamespaceForPrefix(String, Node)
		 */
		public String getNamespaceForPrefix(String prefix) {
            return dtm.getNamespaceURI(handle);
		}
        
        /**
         * @see PrefixResolver#getNamespaceForPrefix(String, Node)
         * this shouldn't get called.
         */
        public String getNamespaceForPrefix(String prefix, Node context) {
            return getNamespaceForPrefix(prefix);
        }

		/**
		 * @see PrefixResolver#getBaseIdentifier()
		 */
		public String getBaseIdentifier() {
			return ElemNumber.this.getBaseIdentifier();
		}

		/**
		 * @see PrefixResolver#handlesNullPrefixes()
		 */
		public boolean handlesNullPrefixes() {
			return handleNullPrefix;
		}

}
    
  /**
   * Only nodes are counted that match this pattern.
   * @serial
   */
  private XPath m_countMatchPattern = null;

  /**
   * Set the "count" attribute.
   * The count attribute is a pattern that specifies what nodes
   * should be counted at those levels. If count attribute is not
   * specified, then it defaults to the pattern that matches any
   * node with the same node type as the current node and, if the
   * current node has an expanded-name, with the same expanded-name
   * as the current node.
   *
   * @param v Value to set for "count" attribute. 
   */
  public void setCount(XPath v)
  {
    m_countMatchPattern = v;
  }

  /**
   * Get the "count" attribute.
   * The count attribute is a pattern that specifies what nodes
   * should be counted at those levels. If count attribute is not
   * specified, then it defaults to the pattern that matches any
   * node with the same node type as the current node and, if the
   * current node has an expanded-name, with the same expanded-name
   * as the current node.
   *
   * @return Value of "count" attribute.
   */
  public XPath getCount()
  {
    return m_countMatchPattern;
  }

  /**
   * Specifies where to count from.
   * For level="single" or level="multiple":
   * Only ancestors that are searched are
   * those that are descendants of the nearest ancestor that matches
   * the from pattern.
   * For level="any:
   * Only nodes after the first node before the
   * current node that match the from pattern are considered.
   * @serial
   */
  private XPath m_fromMatchPattern = null;

  /**
   * Set the "from" attribute. Specifies where to count from.
   * For level="single" or level="multiple":
   * Only ancestors that are searched are
   * those that are descendants of the nearest ancestor that matches
   * the from pattern.
   * For level="any:
   * Only nodes after the first node before the
   * current node that match the from pattern are considered.
   *
   * @param v Value to set for "from" attribute.
   */
  public void setFrom(XPath v)
  {
    m_fromMatchPattern = v;
  }

  /**
   * Get the "from" attribute.
   * For level="single" or level="multiple":
   * Only ancestors that are searched are
   * those that are descendants of the nearest ancestor that matches
   * the from pattern.
   * For level="any:
   * Only nodes after the first node before the
   * current node that match the from pattern are considered.
   *
   * @return Value of "from" attribute.
   */
  public XPath getFrom()
  {
    return m_fromMatchPattern;
  }

  /**
   * When level="single", it goes up to the first node in the ancestor-or-self axis
   * that matches the count pattern, and constructs a list of length one containing
   * one plus the number of preceding siblings of that ancestor that match the count
   * pattern. If there is no such ancestor, it constructs an empty list. If the from
   * attribute is specified, then the only ancestors that are searched are those
   * that are descendants of the nearest ancestor that matches the from pattern.
   * Preceding siblings has the same meaning here as with the preceding-sibling axis.
   *
   * When level="multiple", it constructs a list of all ancestors of the current node
   * in document order followed by the element itself; it then selects from the list
   * those nodes that match the count pattern; it then maps each node in the list to
   * one plus the number of preceding siblings of that node that match the count pattern.
   * If the from attribute is specified, then the only ancestors that are searched are
   * those that are descendants of the nearest ancestor that matches the from pattern.
   * Preceding siblings has the same meaning here as with the preceding-sibling axis.
   *
   * When level="any", it constructs a list of length one containing the number of
   * nodes that match the count pattern and belong to the set containing the current
   * node and all nodes at any level of the document that are before the current node
   * in document order, excluding any namespace and attribute nodes (in other words
   * the union of the members of the preceding and ancestor-or-self axes). If the
   * from attribute is specified, then only nodes after the first node before the
   * current node that match the from pattern are considered.
   * @serial
   */
  private int m_level = Constants.NUMBERLEVEL_SINGLE;

  /**
   * Set the "level" attribute.
   * The level attribute specifies what levels of the source tree should
   * be considered; it has the values single, multiple or any. The default
   * is single.
   *
   * @param v Value to set for "level" attribute.
   */
  public void setLevel(int v)
  {
    m_level = v;
  }

  /**
   * Get the "level" attribute.
   * The level attribute specifies what levels of the source tree should
   * be considered; it has the values single, multiple or any. The default
   * is single.
   *
   * @return Value of "level" attribute.
   */
  public int getLevel()
  {
    return m_level;
  }

  /**
   * The value attribute contains an expression. The expression is evaluated
   * and the resulting object is converted to a number as if by a call to the
   * number function.
   * @serial
   */
  private XPath m_valueExpr = null;

  /**
   * Set the "value" attribute.
   * The value attribute contains an expression. The expression is evaluated
   * and the resulting object is converted to a number as if by a call to the
   * number function.
   *
   * @param v Value to set for "value" attribute.
   */
  public void setValue(XPath v)
  {
    m_valueExpr = v;
  }

  /**
   * Get the "value" attribute.
   * The value attribute contains an expression. The expression is evaluated
   * and the resulting object is converted to a number as if by a call to the
   * number function.
   *
   * @return Value of "value" attribute.
   */
  public XPath getValue()
  {
    return m_valueExpr;
  }

  /**
   * The "format" attribute is used to control conversion of a list of
   * numbers into a string.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   * @serial
   */
  private AVT m_format_avt = null;

  /**
   * Set the "format" attribute.
   * The "format" attribute is used to control conversion of a list of
   * numbers into a string.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @param v Value to set for "format" attribute.
   */
  public void setFormat(AVT v)
  {
    m_format_avt = v;
  }

  /**
   * Get the "format" attribute.
   * The "format" attribute is used to control conversion of a list of
   * numbers into a string.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @return Value of "format" attribute.
   */
  public AVT getFormat()
  {
    return m_format_avt;
  }

  /**
   * When numbering with an alphabetic sequence, the lang attribute
   * specifies which language's alphabet is to be used.
   * @serial
   */
  private AVT m_lang_avt = null;

  /**
   * Set the "lang" attribute.
   * When numbering with an alphabetic sequence, the lang attribute
   * specifies which language's alphabet is to be used; it has the same
   * range of values as xml:lang [XML]; if no lang value is specified,
   * the language should be determined from the system environment.
   * Implementers should document for which languages they support numbering.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @param v Value to set for "lang" attribute.
   */
  public void setLang(AVT v)
  {
    m_lang_avt = v;
  }

  /**
   * Get the "lang" attribute.
   * When numbering with an alphabetic sequence, the lang attribute
   * specifies which language's alphabet is to be used; it has the same
   * range of values as xml:lang [XML]; if no lang value is specified,
   * the language should be determined from the system environment.
   * Implementers should document for which languages they support numbering.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @return Value ofr "lang" attribute.
   */
  public AVT getLang()
  {
    return m_lang_avt;
  }

  /**
   * The letter-value attribute disambiguates between numbering
   * sequences that use letters.
   * @serial
   */
  private AVT m_lettervalue_avt = null;

  /**
   * Set the "letter-value" attribute.
   * The letter-value attribute disambiguates between numbering sequences
   * that use letters.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @param v Value to set for "letter-value" attribute.
   */
  public void setLetterValue(AVT v)
  {
    m_lettervalue_avt = v;
  }

  /**
   * Get the "letter-value" attribute.
   * The letter-value attribute disambiguates between numbering sequences
   * that use letters.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @return Value to set for "letter-value" attribute.
   */
  public AVT getLetterValue()
  {
    return m_lettervalue_avt;
  }

  /**
   * The grouping-separator attribute gives the separator
   * used as a grouping (e.g. thousands) separator in decimal
   * numbering sequences.
   * @serial
   */
  private AVT m_groupingSeparator_avt = null;

  /**
   * Set the "grouping-separator" attribute.
   * The grouping-separator attribute gives the separator
   * used as a grouping (e.g. thousands) separator in decimal
   * numbering sequences.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @param v Value to set for "grouping-separator" attribute.
   */
  public void setGroupingSeparator(AVT v)
  {
    m_groupingSeparator_avt = v;
  }

  /**
   * Get the "grouping-separator" attribute.
   * The grouping-separator attribute gives the separator
   * used as a grouping (e.g. thousands) separator in decimal
   * numbering sequences.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @return Value of "grouping-separator" attribute.
   */
  public AVT getGroupingSeparator()
  {
    return m_groupingSeparator_avt;
  }

  /**
   * The optional grouping-size specifies the size (normally 3) of the grouping.
   * @serial
   */
  private AVT m_groupingSize_avt = null;

  /**
   * Set the "grouping-size" attribute.
   * The optional grouping-size specifies the size (normally 3) of the grouping.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @param v Value to set for "grouping-size" attribute.
   */
  public void setGroupingSize(AVT v)
  {
    m_groupingSize_avt = v;
  }

  /**
   * Get the "grouping-size" attribute.
   * The optional grouping-size specifies the size (normally 3) of the grouping.
   * @see <a href="http://www.w3.org/TR/xslt#convert">convert in XSLT Specification</a>
   *
   * @return Value of "grouping-size" attribute.
   */
  public AVT getGroupingSize()
  {
    return m_groupingSize_avt;
  }

  /**
   * Shouldn't this be in the transformer?  Big worries about threads...
   */

  // private XResourceBundle thisBundle;

  /**
   * Table to help in converting decimals to roman numerals.
   * @see org.apache.xalan.transformer.DecimalToRoman
   */
  private final static DecimalToRoman m_romanConvertTable[] = {
    new DecimalToRoman(1000, "M", 900, "CM"),
    new DecimalToRoman(500, "D", 400, "CD"),
    new DecimalToRoman(100L, "C", 90L, "XC"),
    new DecimalToRoman(50L, "L", 40L, "XL"),
    new DecimalToRoman(10L, "X", 9L, "IX"),
    new DecimalToRoman(5L, "V", 4L, "IV"),
    new DecimalToRoman(1L, "I", 1L, "I") };
  
  /**
   * This function is called after everything else has been
   * recomposed, and allows the template to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    StylesheetRoot.ComposeState cstate = sroot.getComposeState();
    java.util.Vector vnames = cstate.getVariableNames();
    if(null != m_countMatchPattern)
      m_countMatchPattern.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_format_avt)
      m_format_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_fromMatchPattern)
      m_fromMatchPattern.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_groupingSeparator_avt)
      m_groupingSeparator_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_groupingSize_avt)
      m_groupingSize_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_lang_avt)
      m_lang_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_lettervalue_avt)
      m_lettervalue_avt.fixupVariables(vnames, cstate.getGlobalsSize());
    if(null != m_valueExpr)
      m_valueExpr.fixupVariables(vnames, cstate.getGlobalsSize());
  }


  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_NUMBER;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_NUMBER_STRING;
  }

  /**
   * Execute an xsl:number instruction. The xsl:number element is
   * used to insert a formatted number into the result tree.
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {

    int sourceNode = transformer.getXPathContext().getCurrentNode();
    String countString = getCountString(transformer, sourceNode);

    try
    {
      transformer.getResultTreeHandler().characters(countString.toCharArray(),
                                                    0, countString.length());
    }
    catch(SAXException se)
    {
      throw new TransformerException(se);
    }
  }

  /**
   * Add a child to the child list.
   *
   * @param newChild Child to add to child list
   *
   * @return Child just added to child list
   *
   * @throws DOMException
   */
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {

    error(XSLTErrorResources.ER_CANNOT_ADD,
          new Object[]{ newChild.getNodeName(),
                        this.getNodeName() });  //"Can not add " +((ElemTemplateElement)newChild).m_elemName +

    //" to " + this.m_elemName);
    return null;
  }

  /**
   * Given a 'from' pattern (ala xsl:number), a match pattern
   * and a context, find the first ancestor that matches the
   * pattern (including the context handed in).
   *
   * @param xctxt The XPath runtime state for this.
   * @param fromMatchPattern The ancestor must match this pattern.
   * @param countMatchPattern The ancestor must also match this pattern.
   * @param context The node that "." expresses.
   * @param namespaceContext The context in which namespaces in the
   * queries are supposed to be expanded.
   *
   * @return the first ancestor that matches the given pattern
   *
   * @throws javax.xml.transform.TransformerException
   */
  int findAncestor(
          XPathContext xctxt, XPath fromMatchPattern, XPath countMatchPattern, 
          int context, ElemNumber namespaceContext)
            throws javax.xml.transform.TransformerException
  {
    DTM dtm = xctxt.getDTM(context);
    while (DTM.NULL != context)
    {
      if (null != fromMatchPattern)
      {
        if (fromMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {

          //context = null;
          break;
        }
      }

      if (null != countMatchPattern)
      {
        if (countMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {
          break;
        }
      }

      context = dtm.getParent(context);
    }

    return context;
  }

  /**
   * Given a 'from' pattern (ala xsl:number), a match pattern
   * and a context, find the first ancestor that matches the
   * pattern (including the context handed in).
   * @param xctxt The XPath runtime state for this.
   * @param fromMatchPattern The ancestor must match this pattern.
   * @param countMatchPattern The ancestor must also match this pattern.
   * @param context The node that "." expresses.
   * @param namespaceContext The context in which namespaces in the
   * queries are supposed to be expanded.
   *
   * @return the first preceding, ancestor or self node that 
   * matches the given pattern
   *
   * @throws javax.xml.transform.TransformerException
   */
  private int findPrecedingOrAncestorOrSelf(
          XPathContext xctxt, XPath fromMatchPattern, XPath countMatchPattern, 
          int context, ElemNumber namespaceContext)
            throws javax.xml.transform.TransformerException
  {
    DTM dtm = xctxt.getDTM(context);
    while (DTM.NULL != context)
    {
      if (null != fromMatchPattern)
      {
        if (fromMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {
          context = DTM.NULL;

          break;
        }
      }

      if (null != countMatchPattern)
      {
        if (countMatchPattern.getMatchScore(xctxt, context)
                != XPath.MATCH_SCORE_NONE)
        {
          break;
        }
      }

      int prevSibling = dtm.getPreviousSibling(context);

      if (DTM.NULL == prevSibling)
      {
        context = dtm.getParent(context);
      }
      else
      {

        // Now go down the chain of children of this sibling 
        context = dtm.getLastChild(prevSibling);

        if (context == DTM.NULL)
          context = prevSibling;
      }
    }

    return context;
  }

  /**
   * Get the count match pattern, or a default value.
   *
   * @param support The XPath runtime state for this.
   * @param contextNode The node that "." expresses.
   *
   * @return the count match pattern, or a default value. 
   *
   * @throws javax.xml.transform.TransformerException
   */
  XPath getCountMatchPattern(XPathContext support, int contextNode)
          throws javax.xml.transform.TransformerException
  {

    XPath countMatchPattern = m_countMatchPattern;
    DTM dtm = support.getDTM(contextNode);
    if (null == countMatchPattern)
    {
      switch (dtm.getNodeType(contextNode))
      {
      case DTM.ELEMENT_NODE :
        MyPrefixResolver resolver;

        if (dtm.getNamespaceURI(contextNode) == null) {
             resolver =  new MyPrefixResolver(dtm.getNode(contextNode), dtm,contextNode, false);
        } else {
            resolver = new MyPrefixResolver(dtm.getNode(contextNode), dtm,contextNode, true);
        }

        countMatchPattern = new XPath(dtm.getNodeName(contextNode), this, resolver,
                                      XPath.MATCH, support.getErrorListener());
        break;

      case DTM.ATTRIBUTE_NODE :

        // countMatchPattern = m_stylesheet.createMatchPattern("@"+contextNode.getNodeName(), this);
        countMatchPattern = new XPath("@" + dtm.getNodeName(contextNode), this,
                                      this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.CDATA_SECTION_NODE :
      case DTM.TEXT_NODE :

        // countMatchPattern = m_stylesheet.createMatchPattern("text()", this);
        countMatchPattern = new XPath("text()", this, this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.COMMENT_NODE :

        // countMatchPattern = m_stylesheet.createMatchPattern("comment()", this);
        countMatchPattern = new XPath("comment()", this, this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.DOCUMENT_NODE :

        // countMatchPattern = m_stylesheet.createMatchPattern("/", this);
        countMatchPattern = new XPath("/", this, this, XPath.MATCH, support.getErrorListener());
        break;
      case DTM.PROCESSING_INSTRUCTION_NODE :

        // countMatchPattern = m_stylesheet.createMatchPattern("pi("+contextNode.getNodeName()+")", this);
        countMatchPattern = new XPath("pi(" + dtm.getNodeName(contextNode)
                                      + ")", this, this, XPath.MATCH, support.getErrorListener());
        break;
      default :
        countMatchPattern = null;
      }
    }

    return countMatchPattern;
  }

  /**
   * Given an XML source node, get the count according to the
   * parameters set up by the xsl:number attributes.
   * @param transformer non-null reference to the the current transform-time state.
   * @param sourceNode The source node being counted.
   *
   * @return The count of nodes
   *
   * @throws TransformerException
   */
  String getCountString(TransformerImpl transformer, int sourceNode)
          throws TransformerException
  {

    long[] list = null;
    XPathContext xctxt = transformer.getXPathContext();
    CountersTable ctable = transformer.getCountersTable();

    if (null != m_valueExpr)
    {
      XObject countObj = m_valueExpr.execute(xctxt, sourceNode, this);
      //According to Errata E24
      double d_count = java.lang.Math.floor(countObj.num()+ 0.5);
      if (Double.isNaN(d_count)) return "NaN";
      else if (d_count < 0 && Double.isInfinite(d_count)) return "-Infinity";
      else if (Double.isInfinite(d_count)) return "Infinity";
      else if (d_count == 0) return "0";
      else{
              long count = (long)d_count;
              list = new long[1];
              list[0] = count;              
      }
    }
    else
    {
      if (Constants.NUMBERLEVEL_ANY == m_level)
      {
        list = new long[1];
        list[0] = ctable.countNode(xctxt, this, sourceNode);
      }
      else
      {
        NodeVector ancestors =
          getMatchingAncestors(xctxt, sourceNode,
                               Constants.NUMBERLEVEL_SINGLE == m_level);
        int lastIndex = ancestors.size() - 1;

        if (lastIndex >= 0)
        {
          list = new long[lastIndex + 1];

          for (int i = lastIndex; i >= 0; i--)
          {
            int target = ancestors.elementAt(i);

            list[lastIndex - i] = ctable.countNode(xctxt, this, target);
          }
        }
      }
    }

    return (null != list)
           ? formatNumberList(transformer, list, sourceNode) : "";
  }

  /**
   * Get the previous node to be counted.
   *
   * @param xctxt The XPath runtime state for this.
   * @param pos The current node
   *
   * @return the previous node to be counted.
   *
   * @throws TransformerException
   */
  public int getPreviousNode(XPathContext xctxt, int pos)
          throws TransformerException
  {

    XPath countMatchPattern = getCountMatchPattern(xctxt, pos);
    DTM dtm = xctxt.getDTM(pos);

    if (Constants.NUMBERLEVEL_ANY == m_level)
    {
      XPath fromMatchPattern = m_fromMatchPattern;

      // Do a backwards document-order walk 'till a node is found that matches 
      // the 'from' pattern, or a node is found that matches the 'count' pattern, 
      // or the top of the tree is found.
      while (DTM.NULL != pos)
      {

        // Get the previous sibling, if there is no previous sibling, 
        // then count the parent, but if there is a previous sibling, 
        // dive down to the lowest right-hand (last) child of that sibling.
        int next = dtm.getPreviousSibling(pos);

        if (DTM.NULL == next)
        {
          next = dtm.getParent(pos);

          if ((DTM.NULL != next) && ((((null != fromMatchPattern) && (fromMatchPattern.getMatchScore(
                  xctxt, next) != XPath.MATCH_SCORE_NONE))) 
              || (dtm.getNodeType(next) == DTM.DOCUMENT_NODE)))
          {
            pos = DTM.NULL;  // return null from function.

            break;  // from while loop
          }
        }
        else
        {

          // dive down to the lowest right child.
          int child = next;

          while (DTM.NULL != child)
          {
            child = dtm.getLastChild(next);

            if (DTM.NULL != child)
              next = child;
          }
        }

        pos = next;

        if ((DTM.NULL != pos)
                && ((null == countMatchPattern)
                    || (countMatchPattern.getMatchScore(xctxt, pos)
                        != XPath.MATCH_SCORE_NONE)))
        {
          break;
        }
      }
    }
    else  // NUMBERLEVEL_MULTI or NUMBERLEVEL_SINGLE
    {
      while (DTM.NULL != pos)
      {
        pos = dtm.getPreviousSibling(pos);

        if ((DTM.NULL != pos)
                && ((null == countMatchPattern)
                    || (countMatchPattern.getMatchScore(xctxt, pos)
                        != XPath.MATCH_SCORE_NONE)))
        {
          break;
        }
      }
    }

    return pos;
  }

  /**
   * Get the target node that will be counted..
   *
   * @param xctxt The XPath runtime state for this.
   * @param sourceNode non-null reference to the <a href="http://www.w3.org/TR/xslt#dt-current-node">current source node</a>.
   *
   * @return the target node that will be counted
   *
   * @throws TransformerException
   */
  public int getTargetNode(XPathContext xctxt, int sourceNode)
          throws TransformerException
  {

    int target = DTM.NULL;
    XPath countMatchPattern = getCountMatchPattern(xctxt, sourceNode);

    if (Constants.NUMBERLEVEL_ANY == m_level)
    {
      target = findPrecedingOrAncestorOrSelf(xctxt, m_fromMatchPattern,
                                             countMatchPattern, sourceNode,
                                             this);
    }
    else
    {
      target = findAncestor(xctxt, m_fromMatchPattern, countMatchPattern,
                            sourceNode, this);
    }

    return target;
  }

  /**
   * Get the ancestors, up to the root, that match the
   * pattern.
   * 
   * @param xctxt The XPath runtime state for this.
   * @param node Count this node and it's ancestors.
   * @param stopAtFirstFound Flag indicating to stop after the
   * first node is found (difference between level = single
   * or multiple)
   * @return The number of ancestors that match the pattern.
   *
   * @throws javax.xml.transform.TransformerException
   */
  NodeVector getMatchingAncestors(
          XPathContext xctxt, int node, boolean stopAtFirstFound)
            throws javax.xml.transform.TransformerException
  {

    NodeSetDTM ancestors = new NodeSetDTM(xctxt.getDTMManager());
    XPath countMatchPattern = getCountMatchPattern(xctxt, node);
    DTM dtm = xctxt.getDTM(node);

    while (DTM.NULL != node)
    {
      if ((null != m_fromMatchPattern)
              && (m_fromMatchPattern.getMatchScore(xctxt, node)
                  != XPath.MATCH_SCORE_NONE))
      {

        // The following if statement gives level="single" different 
        // behavior from level="multiple", which seems incorrect according 
        // to the XSLT spec.  For now we are leaving this in to replicate 
        // the same behavior in XT, but, for all intents and purposes we 
        // think this is a bug, or there is something about level="single" 
        // that we still don't understand.
        if (!stopAtFirstFound)
          break;
      }

      if (null == countMatchPattern)
        System.out.println(
          "Programmers error! countMatchPattern should never be null!");

      if (countMatchPattern.getMatchScore(xctxt, node)
              != XPath.MATCH_SCORE_NONE)
      {
        ancestors.addElement(node);

        if (stopAtFirstFound)
          break;
      }

      node = dtm.getParent(node);
    }

    return ancestors;
  }  // end getMatchingAncestors method

  /**
   * Get the locale we should be using.
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param contextNode The node that "." expresses.
   *
   * @return The locale to use. May be specified by "lang" attribute,
   * but if not, use default locale on the system. 
   *
   * @throws TransformerException
   */
  Locale getLocale(TransformerImpl transformer, int contextNode)
          throws TransformerException
  {

    Locale locale = null;

    if (null != m_lang_avt)
    {
      XPathContext xctxt = transformer.getXPathContext();
      String langValue = m_lang_avt.evaluate(xctxt, contextNode, this);

      if (null != langValue)
      {

        // Not really sure what to do about the country code, so I use the
        // default from the system.
        // TODO: fix xml:lang handling.
        locale = new Locale(langValue.toUpperCase(), "");

        //Locale.getDefault().getDisplayCountry());
        if (null == locale)
        {
          transformer.getMsgMgr().warn(this, null, xctxt.getDTM(contextNode).getNode(contextNode),
                                       XSLTErrorResources.WG_LOCALE_NOT_FOUND,
                                       new Object[]{ langValue });  //"Warning: Could not find locale for xml:lang="+langValue);

          locale = Locale.getDefault();
        }
      }
    }
    else
    {
      locale = Locale.getDefault();
    }

    return locale;
  }

  /**
   * Get the number formatter to be used the format the numbers
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param contextNode The node that "." expresses.
   *
   * ($objectName$) @return The number formatter to be used
   *
   * @throws TransformerException
   */
  private DecimalFormat getNumberFormatter(
          TransformerImpl transformer, int contextNode) throws TransformerException
  {
    // Patch from Steven Serocki
    // Maybe we really want to do the clone in getLocale() and return  
    // a clone of the default Locale??
    Locale locale = (Locale)getLocale(transformer, contextNode).clone();

    // Helper to format local specific numbers to strings.
    DecimalFormat formatter = null;

    //synchronized (locale)
    //{
    //     formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
    //}

    String digitGroupSepValue =
      (null != m_groupingSeparator_avt)
      ? m_groupingSeparator_avt.evaluate(
      transformer.getXPathContext(), contextNode, this) : null;
      
      
    // Validate grouping separator if an AVT was used; otherwise this was 
    // validated statically in XSLTAttributeDef.java.
    if ((digitGroupSepValue != null) && (!m_groupingSeparator_avt.isSimple()) &&
        (digitGroupSepValue.length() != 1))
    {
            transformer.getMsgMgr().warn(
               this, XSLTErrorResources.WG_ILLEGAL_ATTRIBUTE_VALUE,
               new Object[]{ Constants.ATTRNAME_NAME, m_groupingSeparator_avt.getName()});   
    }                  
      
      
    String nDigitsPerGroupValue =
      (null != m_groupingSize_avt)
      ? m_groupingSize_avt.evaluate(
      transformer.getXPathContext(), contextNode, this) : null;

    // TODO: Handle digit-group attributes
    if ((null != digitGroupSepValue) && (null != nDigitsPerGroupValue) &&
        // Ignore if separation value is empty string
        (digitGroupSepValue.length() > 0))
    {
      try
      {
        formatter = (DecimalFormat) NumberFormat.getNumberInstance(locale);
        formatter.setGroupingSize(
          Integer.valueOf(nDigitsPerGroupValue).intValue());
        
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(digitGroupSepValue.charAt(0));
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setGroupingUsed(true);
      }
      catch (NumberFormatException ex)
      {
        formatter.setGroupingUsed(false);
      }
    }

    return formatter;
  }

  /**
   * Format a vector of numbers into a formatted string.
   * 
   * @param transformer non-null reference to the the current transform-time state.
   * @param list Array of one or more long integer numbers.
   * @param contextNode The node that "." expresses.
   * @return String that represents list according to
   * %conversion-atts; attributes.
   * TODO: Optimize formatNumberList so that it caches the last count and
   * reuses that info for the next count.
   *
   * @throws TransformerException
   */
  String formatNumberList(
          TransformerImpl transformer, long[] list, int contextNode)
            throws TransformerException
  {

    String numStr;
    FastStringBuffer formattedNumber = StringBufferPool.get();

    try
    {
      int nNumbers = list.length, numberWidth = 1;
      char numberType = '1';
      String formatToken, lastSepString = null, formatTokenString = null;

      // If a seperator hasn't been specified, then use "."  
      // as a default separator. 
      // For instance: [2][1][5] with a format value of "1 "
      // should format to "2.1.5 " (I think).
      // Otherwise, use the seperator specified in the format string.
      // For instance: [2][1][5] with a format value of "01-001. "
      // should format to "02-001-005 ".
      String lastSep = ".";
      boolean isFirstToken = true;  // true if first token  
      String formatValue =
        (null != m_format_avt)
        ? m_format_avt.evaluate(
        transformer.getXPathContext(), contextNode, this) : null;

      if (null == formatValue)
        formatValue = "1";

      NumberFormatStringTokenizer formatTokenizer =
        new NumberFormatStringTokenizer(formatValue);

      // int sepCount = 0;                  // keep track of seperators
      // Loop through all the numbers in the list.
      for (int i = 0; i < nNumbers; i++)
      {

        // Loop to the next digit, letter, or separator.
        if (formatTokenizer.hasMoreTokens())
        {
          formatToken = formatTokenizer.nextToken();

          // If the first character of this token is a character or digit, then 
          // it is a number format directive.
          if (Character.isLetterOrDigit(
                  formatToken.charAt(formatToken.length() - 1)))
          {
            numberWidth = formatToken.length();
            numberType = formatToken.charAt(numberWidth - 1);
          }

          // If there is a number format directive ahead, 
          // then append the formatToken.
          else if (formatTokenizer.isLetterOrDigitAhead())
          {
            formatTokenString = formatToken;

            // Append the formatToken string...
            // For instance [2][1][5] with a format value of "1--1. "
            // should format to "2--1--5. " (I guess).
            while (formatTokenizer.nextIsSep())
            {
              formatToken = formatTokenizer.nextToken();
              formatTokenString += formatToken;
            }

            // Record this separator, so it can be used as the 
            // next separator, if the next is the last.
            // For instance: [2][1][5] with a format value of "1-1 "
            // should format to "2-1-5 ".
            if (!isFirstToken)
              lastSep = formatTokenString;

            // Since we know the next is a number or digit, we get it now.
            formatToken = formatTokenizer.nextToken();
            numberWidth = formatToken.length();
            numberType = formatToken.charAt(numberWidth - 1);
          }
          else  // only separators left
          {

            // Set up the string for the trailing characters after 
            // the last number is formatted (i.e. after the loop).
            lastSepString = formatToken;

            // And append any remaining characters to the lastSepString.
            while (formatTokenizer.hasMoreTokens())
            {
              formatToken = formatTokenizer.nextToken();
              lastSepString += formatToken;
            }
          }  // else
        }  // end if(formatTokenizer.hasMoreTokens())

        // if this is the first token and there was a prefix
        // append the prefix else, append the separator
        // For instance, [2][1][5] with a format value of "(1-1.) "
        // should format to "(2-1-5.) " (I guess).
        if (null != formatTokenString && isFirstToken)
        {
          formattedNumber.append(formatTokenString);
        }
        else if (null != lastSep &&!isFirstToken)
          formattedNumber.append(lastSep);

        getFormattedNumber(transformer, contextNode, numberType, numberWidth,
                           list[i], formattedNumber);

        isFirstToken = false;  // After the first pass, this should be false
      }  // end for loop

      // Check to see if we finished up the format string...
      // Skip past all remaining letters or digits
      while (formatTokenizer.isLetterOrDigitAhead())
      {
        formatTokenizer.nextToken();
      }

      if (lastSepString != null)
        formattedNumber.append(lastSepString);

      while (formatTokenizer.hasMoreTokens())
      {
        formatToken = formatTokenizer.nextToken();

        formattedNumber.append(formatToken);
      }

      numStr = formattedNumber.toString();
    }
    finally
    {
      StringBufferPool.free(formattedNumber);
    }

    return numStr;
  }  // end formatNumberList method

  /*
  * Get Formatted number
  */

  /**
   * Format the given number and store it in the given buffer 
   *
   *
   * @param transformer non-null reference to the the current transform-time state.
   * @param contextNode The node that "." expresses.
   * @param numberType Type to format to
   * @param numberWidth Maximum length of formatted number
   * @param listElement Number to format
   * @param formattedNumber Buffer to store formatted number
   *
   * @throws javax.xml.transform.TransformerException
   */
  private void getFormattedNumber(
          TransformerImpl transformer, int contextNode, 
          char numberType, int numberWidth, long listElement, 
          FastStringBuffer formattedNumber)
            throws javax.xml.transform.TransformerException
  {


    String letterVal =
      (m_lettervalue_avt != null)
      ? m_lettervalue_avt.evaluate(
      transformer.getXPathContext(), contextNode, this) : null;

    /**
     * Wrapper of Chars for converting integers into alpha counts.
     */
    CharArrayWrapper alphaCountTable = null;
    
    XResourceBundle thisBundle = null;
     
    switch (numberType)
    {
    case 'A' :
        if (null == m_alphaCountTable){
                thisBundle =
                  (XResourceBundle) XResourceBundle.loadResourceBundle(
                    org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, getLocale(transformer, contextNode));
                m_alphaCountTable = (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET);                
        }
      int2alphaCount(listElement, m_alphaCountTable, formattedNumber);
      break;
    case 'a' :
        if (null == m_alphaCountTable){
                thisBundle =
                  (XResourceBundle) XResourceBundle.loadResourceBundle(
                    org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, getLocale(transformer, contextNode));
                m_alphaCountTable = (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET);                
        }
      FastStringBuffer stringBuf = StringBufferPool.get();

      try
      {
        int2alphaCount(listElement, m_alphaCountTable, stringBuf);
        formattedNumber.append(
          stringBuf.toString().toLowerCase(
            getLocale(transformer, contextNode)));
      }
      finally
      {
        StringBufferPool.free(stringBuf);
      }
      break;
    case 'I' :
      formattedNumber.append(long2roman(listElement, true));
      break;
    case 'i' :
      formattedNumber.append(
        long2roman(listElement, true).toLowerCase(
          getLocale(transformer, contextNode)));
      break;
    case 0x3042 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HA"));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));

      break;
    }
    case 0x3044 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "HI"));

      if ((letterVal != null)
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));

      break;
    }
    case 0x30A2 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "A"));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));

      break;
    }
    case 0x30A4 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ja", "JP", "I"));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        formattedNumber.append(
          int2singlealphaCount(
            listElement,
            (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET)));

      break;
    }
    case 0x4E00 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "CN"));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
      {
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      }
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);

      break;
    }
    case 0x58F9 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("zh", "TW"));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);

      break;
    }
    case 0x0E51 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("th", ""));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);

      break;
    }
    case 0x05D0 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("he", ""));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);

      break;
    }
    case 0x10D0 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("ka", ""));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);

      break;
    }
    case 0x03B1 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("el", ""));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);

      break;
    }
    case 0x0430 :
    {

      thisBundle = (XResourceBundle) XResourceBundle.loadResourceBundle(
        org.apache.xml.utils.res.XResourceBundle.LANG_BUNDLE_NAME, new Locale("cy", ""));

      if (letterVal != null
              && letterVal.equals(Constants.ATTRVAL_TRADITIONAL))
        formattedNumber.append(tradAlphaCount(listElement, thisBundle));
      else  //if (m_lettervalue_avt != null && m_lettervalue_avt.equals(Constants.ATTRVAL_ALPHABETIC))
        int2alphaCount(listElement,
                       (CharArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_ALPHABET),
                       formattedNumber);

      break;
    }
    default :  // "1"
      DecimalFormat formatter = getNumberFormatter(transformer, contextNode);
      String padString = formatter == null ? String.valueOf(0) : formatter.format(0);    
      String numString = formatter == null ? String.valueOf(listElement) : formatter.format(listElement);
      int nPadding = numberWidth - numString.length();

      for (int k = 0; k < nPadding; k++)
      {
        formattedNumber.append(padString);
      }

      formattedNumber.append(numString);
    }
  }
  
  /**
   * Get a string value for zero, which is not really defined by the 1.0 spec, 
   * thought I think it might be cleared up by the erreta.
   */
   String getZeroString()
   {
     return ""+0;
   }

  /**
   * Convert a long integer into alphabetic counting, in other words
   * count using the sequence A B C ... Z.
   * 
   * @param val Value to convert -- must be greater than zero.
   * @param table a table containing one character for each digit in the radix
   * @return String representing alpha count of number.
   * @see TransformerImpl#DecimalToRoman
   *
   * Note that the radix of the conversion is inferred from the size
   * of the table.
   */
  protected String int2singlealphaCount(long val, CharArrayWrapper table)
  {

    int radix = table.getLength();

    // TODO:  throw error on out of range input
    if (val > radix)
    {
      return getZeroString();
    }
    else
      return (new Character(table.getChar((int)val - 1))).toString();  // index into table is off one, starts at 0
  }

  /**
   * Convert a long integer into alphabetic counting, in other words
   * count using the sequence A B C ... Z AA AB AC.... etc.
   * 
   * @param val Value to convert -- must be greater than zero.
   * @param table a table containing one character for each digit in the radix
   * @param aTable Array of alpha characters representing numbers
   * @param stringBuf Buffer where to save the string representing alpha count of number.
   * 
   * @see TransformerImpl#DecimalToRoman
   *
   * Note that the radix of the conversion is inferred from the size
   * of the table.
   */
  protected void int2alphaCount(long val, CharArrayWrapper aTable,
                                FastStringBuffer stringBuf)
  {

    int radix = aTable.getLength();
    char[] table = new char[radix];

    // start table at 1, add last char at index 0. Reason explained above and below.
    int i;

    for (i = 0; i < radix - 1; i++)
    {
      table[i + 1] = aTable.getChar(i);
    }

    table[0] = aTable.getChar(i);

    // Create a buffer to hold the result
    // TODO:  size of the table can be detereined by computing
    // logs of the radix.  For now, we fake it.
    char buf[] = new char[100];

    //some languages go left to right(ie. english), right to left (ie. Hebrew),
    //top to bottom (ie.Japanese), etc... Handle them differently
    //String orientation = thisBundle.getString(org.apache.xml.utils.res.XResourceBundle.LANG_ORIENTATION);
    // next character to set in the buffer
    int charPos;

    charPos = buf.length - 1;  // work backward through buf[]  

    // index in table of the last character that we stored
    int lookupIndex = 1;  // start off with anything other than zero to make correction work

    //                                          Correction number
    //
    //  Correction can take on exactly two values:
    //
    //          0       if the next character is to be emitted is usual
    //
    //      radix - 1
    //                  if the next char to be emitted should be one less than
    //                  you would expect
    //                  
    // For example, consider radix 10, where 1="A" and 10="J"
    //
    // In this scheme, we count: A, B, C ...   H, I, J (not A0 and certainly
    // not AJ), A1
    //
    // So, how do we keep from emitting AJ for 10?  After correctly emitting the
    // J, lookupIndex is zero.  We now compute a correction number of 9 (radix-1).
    // In the following line, we'll compute (val+correction) % radix, which is,
    // (val+9)/10.  By this time, val is 1, so we compute (1+9) % 10, which
    // is 10 % 10 or zero.  So, we'll prepare to emit "JJ", but then we'll
    // later suppress the leading J as representing zero (in the mod system,
    // it can represent either 10 or zero).  In summary, the correction value of
    // "radix-1" acts like "-1" when run through the mod operator, but with the
    // desireable characteristic that it never produces a negative number.
    long correction = 0;

    // TODO:  throw error on out of range input
    do
    {

      // most of the correction calculation is explained above,  the reason for the
      // term after the "|| " is that it correctly propagates carries across
      // multiple columns.
      correction =
        ((lookupIndex == 0) || (correction != 0 && lookupIndex == radix - 1))
        ? (radix - 1) : 0;

      // index in "table" of the next char to emit
      lookupIndex = (int)(val + correction) % radix;

      // shift input by one "column"
      val = (val / radix);

      // if the next value we'd put out would be a leading zero, we're done.
      if (lookupIndex == 0 && val == 0)
        break;

      // put out the next character of output
      buf[charPos--] = table[lookupIndex];  // left to right or top to bottom   
    }
    while (val > 0);

    stringBuf.append(buf, charPos + 1, (buf.length - charPos - 1));
  }

  /**
   * Convert a long integer into traditional alphabetic counting, in other words
   * count using the traditional numbering.
   * 
   * @param val Value to convert -- must be greater than zero.
   * @param thisBundle Resource bundle to use
   * 
   * @return String representing alpha count of number.
   * @see XSLProcessor#DecimalToRoman
   *
   * Note that the radix of the conversion is inferred from the size
   * of the table.
   */
  protected String tradAlphaCount(long val, XResourceBundle thisBundle)
  {

    // if this number is larger than the largest number we can represent, error!
    if (val > Long.MAX_VALUE)
    {
      this.error(XSLTErrorResources.ER_NUMBER_TOO_BIG);
      return XSLTErrorResources.ERROR_STRING;
    }
    char[] table = null;

    // index in table of the last character that we stored
    int lookupIndex = 1;  // start off with anything other than zero to make correction work

    // Create a buffer to hold the result
    // TODO:  size of the table can be detereined by computing
    // logs of the radix.  For now, we fake it.
    char buf[] = new char[100];

    //some languages go left to right(ie. english), right to left (ie. Hebrew),
    //top to bottom (ie.Japanese), etc... Handle them differently
    //String orientation = thisBundle.getString(org.apache.xml.utils.res.XResourceBundle.LANG_ORIENTATION);
    // next character to set in the buffer
    int charPos;

    charPos = 0;  //start at 0

    // array of number groups: ie.1000, 100, 10, 1
    IntArrayWrapper groups = (IntArrayWrapper) thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_NUMBERGROUPS);

    // array of tables of hundreds, tens, digits...
    StringArrayWrapper tables =
      (StringArrayWrapper) (thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_NUM_TABLES));

    //some languages have additive alphabetical notation,
    //some multiplicative-additive, etc... Handle them differently.
    String numbering = thisBundle.getString(org.apache.xml.utils.res.XResourceBundle.LANG_NUMBERING);

    // do multiplicative part first
    if (numbering.equals(org.apache.xml.utils.res.XResourceBundle.LANG_MULT_ADD))
    {
      String mult_order = thisBundle.getString(org.apache.xml.utils.res.XResourceBundle.MULT_ORDER);
      LongArrayWrapper multiplier =
        (LongArrayWrapper) (thisBundle.getObject(org.apache.xml.utils.res.XResourceBundle.LANG_MULTIPLIER));
      CharArrayWrapper zeroChar = (CharArrayWrapper) thisBundle.getObject("zero");
      int i = 0;

      // skip to correct multiplier
      while (i < multiplier.getLength() && val < multiplier.getLong(i))
      {
        i++;
      }

      do
      {
        if (i >= multiplier.getLength())
          break;  //number is smaller than multipliers

        // some languages (ie chinese) put a zero character (and only one) when
        // the multiplier is multiplied by zero. (ie, 1001 is 1X1000 + 0X100 + 0X10 + 1)
        // 0X100 is replaced by the zero character, we don't need one for 0X10
        if (val < multiplier.getLong(i))
        {
          if (zeroChar.getLength() == 0)
          {
            i++;
          }
          else
          {
            if (buf[charPos - 1] != zeroChar.getChar(0))
              buf[charPos++] = zeroChar.getChar(0);

            i++;
          }
        }
        else if (val >= multiplier.getLong(i))
        {
          long mult = val / multiplier.getLong(i);

          val = val % multiplier.getLong(i);  // save this.

          int k = 0;

          while (k < groups.getLength())
          {
            lookupIndex = 1;  // initialize for each table

            if (mult / groups.getInt(k) <= 0)  // look for right table
              k++;
            else
            {

              // get the table
              CharArrayWrapper THEletters = (CharArrayWrapper) thisBundle.getObject(tables.getString(k));

              table = new char[THEletters.getLength() + 1];

              int j;

              for (j = 0; j < THEletters.getLength(); j++)
              {
                table[j + 1] = THEletters.getChar(j);
              }

              table[0] = THEletters.getChar(j - 1);  // don't need this                                                                         

              // index in "table" of the next char to emit
              lookupIndex = (int)mult / groups.getInt(k);

              //this should not happen
              if (lookupIndex == 0 && mult == 0)
                break;

              char multiplierChar = ((CharArrayWrapper) (thisBundle.getObject(
                org.apache.xml.utils.res.XResourceBundle.LANG_MULTIPLIER_CHAR))).getChar(i);

              // put out the next character of output   
              if (lookupIndex < table.length)
              {
                if (mult_order.equals(org.apache.xml.utils.res.XResourceBundle.MULT_PRECEDES))
                {
                  buf[charPos++] = multiplierChar;
                  buf[charPos++] = table[lookupIndex];
                }
                else
                {

                  // don't put out 1 (ie 1X10 is just 10)
                  if (lookupIndex == 1 && i == multiplier.getLength() - 1){}
                  else
                    buf[charPos++] = table[lookupIndex];

                  buf[charPos++] = multiplierChar;
                }

                break;  // all done!
              }
              else
                return XSLTErrorResources.ERROR_STRING;
            }  //end else
          }  // end while        

          i++;
        }  // end else if
      }  // end do while
      while (i < multiplier.getLength());
    }

    // Now do additive part...
    int count = 0;
    String tableName;

    // do this for each table of hundreds, tens, digits...
    while (count < groups.getLength())
    {
      if (val / groups.getInt(count) <= 0)  // look for correct table
        count++;
      else
      {
        CharArrayWrapper theletters = (CharArrayWrapper) thisBundle.getObject(tables.getString(count));

        table = new char[theletters.getLength() + 1];

        int j;

        // need to start filling the table up at index 1
        for (j = 0; j < theletters.getLength(); j++)
        {
          table[j + 1] = theletters.getChar(j);
        }

        table[0] = theletters.getChar(j - 1);  // don't need this

        // index in "table" of the next char to emit
        lookupIndex = (int)val / groups.getInt(count);

        // shift input by one "column"
        val = val % groups.getInt(count);

        // this should not happen
        if (lookupIndex == 0 && val == 0)
          break;

        if (lookupIndex < table.length)
        {

          // put out the next character of output       
          buf[charPos++] = table[lookupIndex];  // left to right or top to bottom                                       
        }
        else
          return XSLTErrorResources.ERROR_STRING;

        count++;
      }
    }  // end while

    // String s = new String(buf, 0, charPos);
    return new String(buf, 0, charPos);
  }

  /**
   * Convert a long integer into roman numerals.
   * @param val Value to convert.
   * @param prefixesAreOK true_ to enable prefix notation (e.g. 4 = "IV"),
   * false_ to disable prefix notation (e.g. 4 = "IIII").
   * @return Roman numeral string.
   * @see DecimalToRoman
   * @see m_romanConvertTable
   */
  protected String long2roman(long val, boolean prefixesAreOK)
  {

    if (val <= 0)
    {
      return getZeroString();
    }

    String roman = "";
    int place = 0;

    if (val <= 3999L)
    {
      do
      {
        while (val >= m_romanConvertTable[place].m_postValue)
        {
          roman += m_romanConvertTable[place].m_postLetter;
          val -= m_romanConvertTable[place].m_postValue;
        }

        if (prefixesAreOK)
        {
          if (val >= m_romanConvertTable[place].m_preValue)
          {
            roman += m_romanConvertTable[place].m_preLetter;
            val -= m_romanConvertTable[place].m_preValue;
          }
        }

        place++;
      }
      while (val > 0);
    }
    else
    {
      roman = XSLTErrorResources.ERROR_STRING;
    }

    return roman;
  }  // end long2roman
  
  /**
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  public void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  	{
	  	if(null != m_countMatchPattern)
	  		m_countMatchPattern.getExpression().callVisitors(m_countMatchPattern, visitor);
	  	if(null != m_fromMatchPattern)
	  		m_fromMatchPattern.getExpression().callVisitors(m_fromMatchPattern, visitor);
	  	if(null != m_valueExpr)
	  		m_valueExpr.getExpression().callVisitors(m_valueExpr, visitor);
	
	  	if(null != m_format_avt)
	  		m_format_avt.callVisitors(visitor);
	  	if(null != m_groupingSeparator_avt)
	  		m_groupingSeparator_avt.callVisitors(visitor);
	  	if(null != m_groupingSize_avt)
	  		m_groupingSize_avt.callVisitors(visitor);
	  	if(null != m_lang_avt)
	  		m_lang_avt.callVisitors(visitor);
	  	if(null != m_lettervalue_avt)
	  		m_lettervalue_avt.callVisitors(visitor);
  	}

    super.callChildVisitors(visitor, callAttrs);
  }


  /**
   * This class returns tokens using non-alphanumberic
   * characters as delimiters.
   */
  class NumberFormatStringTokenizer
  {

    /** Current position in the format string          */
    private int currentPosition;

    /** Index of last character in the format string      */
    private int maxPosition;

    /** Format string to be tokenized        */
    private String str;

    /**
     * Construct a NumberFormatStringTokenizer.
     *
     * @param str Format string to be tokenized
     */
    public NumberFormatStringTokenizer(String str)
    {
      this.str = str;
      maxPosition = str.length();
    }

    /**
     * Reset tokenizer so that nextToken() starts from the beginning.
     */
    public void reset()
    {
      currentPosition = 0;
    }

    /**
     * Returns the next token from this string tokenizer.
     *
     * @return     the next token from this string tokenizer.
     * @throws  NoSuchElementException  if there are no more tokens in this
     *               tokenizer's string.
     */
    public String nextToken()
    {

      if (currentPosition >= maxPosition)
      {
        throw new NoSuchElementException();
      }

      int start = currentPosition;

      while ((currentPosition < maxPosition)
             && Character.isLetterOrDigit(str.charAt(currentPosition)))
      {
        currentPosition++;
      }

      if ((start == currentPosition)
              && (!Character.isLetterOrDigit(str.charAt(currentPosition))))
      {
        currentPosition++;
      }

      return str.substring(start, currentPosition);
    }

    /**
     * Tells if there is a digit or a letter character ahead.
     *
     * @return     true if there is a number or character ahead.
     */
    public boolean isLetterOrDigitAhead()
    {

      int pos = currentPosition;

      while (pos < maxPosition)
      {
        if (Character.isLetterOrDigit(str.charAt(pos)))
          return true;

        pos++;
      }

      return false;
    }

    /**
     * Tells if there is a digit or a letter character ahead.
     *
     * @return     true if there is a number or character ahead.
     */
    public boolean nextIsSep()
    {

      if (Character.isLetterOrDigit(str.charAt(currentPosition)))
        return false;
      else
        return true;
    }

    /**
     * Tells if <code>nextToken</code> will throw an exception
     * if it is called.
     *
     * @return true if <code>nextToken</code> can be called
     * without throwing an exception.
     */
    public boolean hasMoreTokens()
    {
      return (currentPosition >= maxPosition) ? false : true;
    }

    /**
     * Calculates the number of times that this tokenizer's
     * <code>nextToken</code> method can be called before it generates an
     * exception.
     *
     * @return  the number of tokens remaining in the string using the current
     *          delimiter set.
     * @see     java.util.StringTokenizer#nextToken()
     */
    public int countTokens()
    {

      int count = 0;
      int currpos = currentPosition;

      while (currpos < maxPosition)
      {
        int start = currpos;

        while ((currpos < maxPosition)
               && Character.isLetterOrDigit(str.charAt(currpos)))
        {
          currpos++;
        }

        if ((start == currpos)
                && (Character.isLetterOrDigit(str.charAt(currpos)) == false))
        {
          currpos++;
        }

        count++;
      }

      return count;
    }
  }  // end NumberFormatStringTokenizer



}
