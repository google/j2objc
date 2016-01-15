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
 * $Id: ElemCopyOf.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.transformer.TreeWalker2Result;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xml.dtm.ref.DTMTreeWalker;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

/**
 * Implement xsl:copy-of.
 * <pre>
 * <!ELEMENT xsl:copy-of EMPTY>
 * <!ATTLIST xsl:copy-of select %expr; #REQUIRED>
 * </pre>
 * @see <a href="http://www.w3.org/TR/xslt#copy-of">copy-of in XSLT Specification</a>
 * @xsl.usage advanced
 */
public class ElemCopyOf extends ElemTemplateElement
{
    static final long serialVersionUID = -7433828829497411127L;

  /**
   * The required select attribute contains an expression.
   * @serial
   */
  public XPath m_selectExpression = null;

  /**
   * Set the "select" attribute.
   * The required select attribute contains an expression.
   *
   * @param expr Expression for select attribute 
   */
  public void setSelect(XPath expr)
  {
    m_selectExpression = expr;
  }

  /**
   * Get the "select" attribute.
   * The required select attribute contains an expression.
   *
   * @return Expression for select attribute 
   */
  public XPath getSelect()
  {
    return m_selectExpression;
  }
  
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
    m_selectExpression.fixupVariables(cstate.getVariableNames(), cstate.getGlobalsSize());
  }

  /**
   * Get an int constant identifying the type of element.
   * @see org.apache.xalan.templates.Constants
   *
   * @return The token ID for this element
   */
  public int getXSLToken()
  {
    return Constants.ELEMNAME_COPY_OF;
  }

  /**
   * Return the node name.
   *
   * @return The element's name
   */
  public String getNodeName()
  {
    return Constants.ELEMNAME_COPY_OF_STRING;
  }

  /**
   * The xsl:copy-of element can be used to insert a result tree
   * fragment into the result tree, without first converting it to
   * a string as xsl:value-of does (see [7.6.1 Generating Text with
   * xsl:value-of]).
   *
   * @param transformer non-null reference to the the current transform-time state.
   *
   * @throws TransformerException
   */
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    try
    {
      XPathContext xctxt = transformer.getXPathContext();
      int sourceNode = xctxt.getCurrentNode();
      XObject value = m_selectExpression.execute(xctxt, sourceNode, this);

      SerializationHandler handler = transformer.getSerializationHandler();

      if (null != value)
                        {
        int type = value.getType();
        String s;

        switch (type)
        {
        case XObject.CLASS_BOOLEAN :
        case XObject.CLASS_NUMBER :
        case XObject.CLASS_STRING :
          s = value.str();

          handler.characters(s.toCharArray(), 0, s.length());
          break;
        case XObject.CLASS_NODESET :

          // System.out.println(value);
          DTMIterator nl = value.iter();

          // Copy the tree.
          DTMTreeWalker tw = new TreeWalker2Result(transformer, handler);
          int pos;

          while (DTM.NULL != (pos = nl.nextNode()))
          {
            DTM dtm = xctxt.getDTMManager().getDTM(pos);
            short t = dtm.getNodeType(pos);

            // If we just copy the whole document, a startDoc and endDoc get 
            // generated, so we need to only walk the child nodes.
            if (t == DTM.DOCUMENT_NODE)
            {
              for (int child = dtm.getFirstChild(pos); child != DTM.NULL;
                   child = dtm.getNextSibling(child))
              {
                tw.traverse(child);
              }
            }
            else if (t == DTM.ATTRIBUTE_NODE)
            {
              SerializerUtils.addAttribute(handler, pos);
            }
            else
            {
              tw.traverse(pos);
            }
          }
          // nl.detach();
          break;
        case XObject.CLASS_RTREEFRAG :
          SerializerUtils.outputResultTreeFragment(
            handler, value, transformer.getXPathContext());
          break;
        default :
          
          s = value.str();

          handler.characters(s.toCharArray(), 0, s.length());
          break;
        }
      }
                        
      // I don't think we want this.  -sb
      //  if (transformer.getDebug())
      //  transformer.getTraceManager().fireSelectedEvent(sourceNode, this,
      //  "endSelect", m_selectExpression, value);

    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }

  }

  /**
   * Add a child to the child list.
   *
   * @param newChild Child to add to this node's child list
   *
   * @return Child just added to child list
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
   * Call the children visitors.
   * @param visitor The visitor whose appropriate method will be called.
   */
  protected void callChildVisitors(XSLTVisitor visitor, boolean callAttrs)
  {
  	if(callAttrs)
  		m_selectExpression.getExpression().callVisitors(m_selectExpression, visitor);
    super.callChildVisitors(visitor, callAttrs);
  }

}
