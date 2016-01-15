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
 * $Id: XUnresolvedVariable.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

/**
 * An instance of this class holds unto a variable until 
 * it is executed.  It is used at this time for global 
 * variables which must (we think) forward reference.
 */
public class XUnresolvedVariable extends XObject
{  
    static final long serialVersionUID = -256779804767950188L;
  /** The node context for execution. */
  transient private int m_context;
  
  /** The transformer context for execution. */
  transient private TransformerImpl m_transformer;
  
  /** An index to the point in the variable stack where we should
   * begin variable searches for evaluation of expressions.
   * This is -1 if m_isTopLevel is false. 
   **/
  transient private int m_varStackPos = -1;

  /** An index into the variable stack where the variable context 
   * ends, i.e. at the point we should terminate the search. 
   **/
  transient private int m_varStackContext;
  
  /** true if this variable or parameter is a global.
   *  @serial */
  private boolean m_isGlobal;
  
  /** true if this variable or parameter is not currently being evaluated. */
  transient private boolean m_doneEval = true;
  
  /**
   * Create an XUnresolvedVariable, that may be executed at a later time.
   * This is primarily used so that forward referencing works with 
   * global variables.  An XUnresolvedVariable is initially pushed 
   * into the global variable stack, and then replaced with the real 
   * thing when it is accessed.
   *
   * @param obj Must be a non-null reference to an ElemVariable.
   * @param sourceNode The node context for execution.
   * @param transformer The transformer execution context.
   * @param varStackPos An index to the point in the variable stack where we should
   * begin variable searches for evaluation of expressions.
   * @param varStackContext An index into the variable stack where the variable context 
   * ends, i.e. at the point we should terminate the search.
   * @param isGlobal true if this is a global variable.
   */
  public XUnresolvedVariable(ElemVariable obj, int sourceNode, 
                             TransformerImpl transformer,
                             int varStackPos, int varStackContext,
                             boolean isGlobal)
  {
    super(obj);
    m_context = sourceNode;
    m_transformer = transformer;
    
    // For globals, this value will have to be updated once we 
    // have determined how many global variables have been pushed.
    m_varStackPos = varStackPos;
    
    // For globals, this should zero.
    m_varStackContext = varStackContext;
    
    m_isGlobal = isGlobal;
  }
    
  /**
   * For support of literal objects in xpaths.
   *
   * @param xctxt The XPath execution context.
   *
   * @return This object.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    if (!m_doneEval) 
    {
      this.m_transformer.getMsgMgr().error      
        (xctxt.getSAXLocator(), XSLTErrorResources.ER_REFERENCING_ITSELF, 
          new Object[]{((ElemVariable)this.object()).getName().getLocalName()}); 
    }
    VariableStack vars = xctxt.getVarStack();
    
    // These three statements need to be combined into one operation.
    int currentFrame = vars.getStackFrame();
    //// vars.setStackFrame(m_varStackPos);
   

    ElemVariable velem = (ElemVariable)m_obj;
    try
    {
      m_doneEval = false;
      if(-1 != velem.m_frameSize)
      	vars.link(velem.m_frameSize);
      XObject var = velem.getValue(m_transformer, m_context);
      m_doneEval = true;
      return var;
    }
    finally
    {
      // These two statements need to be combined into one operation.
      // vars.setStackFrame(currentFrame);
      
      if(-1 != velem.m_frameSize)
	  	vars.unlink(currentFrame);
    }
  }
  
  /**
   * Set an index to the point in the variable stack where we should
   * begin variable searches for evaluation of expressions.
   * This is -1 if m_isTopLevel is false. 
   * 
   * @param top A valid value that specifies where in the variable 
   * stack the search should begin.
   */
  public void setVarStackPos(int top)
  {
    m_varStackPos = top;
  }

  /**
   * Set an index into the variable stack where the variable context 
   * ends, i.e. at the point we should terminate the search.
   * 
   * @param bottom The point at which the search should terminate, normally 
   * zero for global variables.
   */
  public void setVarStackContext(int bottom)
  {
    m_varStackContext = bottom;
  }
  
  /**
   * Tell what kind of class this is.
   *
   * @return CLASS_UNRESOLVEDVARIABLE
   */
  public int getType()
  {
    return CLASS_UNRESOLVEDVARIABLE;
  }
  
  /**
   * Given a request type, return the equivalent string.
   * For diagnostic purposes.
   *
   * @return An informational string.
   */
  public String getTypeString()
  {
    return "XUnresolvedVariable (" + object().getClass().getName() + ")";
  }


}
