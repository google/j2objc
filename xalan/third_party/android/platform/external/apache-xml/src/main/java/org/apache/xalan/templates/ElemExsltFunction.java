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
 * $Id: ElemExsltFunction.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import javax.xml.transform.TransformerException;

import org.apache.xalan.extensions.ExtensionNamespaceSupport;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Implement func:function.
 * @xsl.usage advanced
 */
public class ElemExsltFunction extends ElemTemplate
{
    static final long serialVersionUID = 272154954793534771L;
  /**
   * Get an integer representation of the element type.
   *
   * @return An integer representation of the element, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants
   */
  public int getXSLToken()
  {
    return Constants.EXSLT_ELEMNAME_FUNCTION;
  }

   /**
   * Return the node name, defined in the
   *     Constants class.
   * @see org.apache.xalan.templates.Constants
   * @return The node name
   * 
   */ 
  public String getNodeName()
  {
    return Constants.EXSLT_ELEMNAME_FUNCTION_STRING;
  }
  
  public void execute(TransformerImpl transformer, XObject[] args)
          throws TransformerException
  {
    XPathContext xctxt = transformer.getXPathContext();
    VariableStack vars = xctxt.getVarStack();
    
    // Increment the frame bottom of the variable stack by the
    // frame size
    int thisFrame = vars.getStackFrame();
    int nextFrame = vars.link(m_frameSize);

    if (m_inArgsSize < args.length) {
      throw new TransformerException ("function called with too many args");
    }
    
    // Set parameters,
    // have to clear the section of the stack frame that has params.
    if (m_inArgsSize > 0) {
      vars.clearLocalSlots(0, m_inArgsSize);

      if (args.length > 0) {
        vars.setStackFrame(thisFrame);
        NodeList children = this.getChildNodes();
        
        for (int i = 0; i < args.length; i ++) {
          Node child = children.item(i);
          if (children.item(i) instanceof ElemParam) {
            ElemParam param = (ElemParam)children.item(i);
            vars.setLocalVariable(param.getIndex(), args[i], nextFrame);
          }
        }
        
        vars.setStackFrame(nextFrame);
      }
    }

    //  Removed ElemTemplate 'push' and 'pop' of RTFContext, in order to avoid losing the RTF context 
    //  before a value can be returned. ElemExsltFunction operates in the scope of the template that called 
    //  the function.
    //  xctxt.pushRTFContext();
    
    vars.setStackFrame(nextFrame);
    transformer.executeChildTemplates(this, true);
    
    // Reset the stack frame after the function call
    vars.unlink(thisFrame);

    // Following ElemTemplate 'pop' removed -- see above.
    // xctxt.popRTFContext(); 
    
  }
        
  /**
   * Called after everything else has been
   * recomposed, and allows the function to set remaining
   * values that may be based on some other property that
   * depends on recomposition.
   */
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    
    // Register the function namespace (if not already registered).
    String namespace = getName().getNamespace();
    String handlerClass = sroot.getExtensionHandlerClass();
    Object[] args ={namespace, sroot};
    ExtensionNamespaceSupport extNsSpt = 
                         new ExtensionNamespaceSupport(namespace, handlerClass, args);
    sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    // Make sure there is a handler for the EXSLT functions namespace
    // -- for isElementAvailable().    
    if (!(namespace.equals(Constants.S_EXSLT_FUNCTIONS_URL)))
    {
      namespace = Constants.S_EXSLT_FUNCTIONS_URL;
      args = new Object[]{namespace, sroot};
      extNsSpt = new ExtensionNamespaceSupport(namespace, handlerClass, args);
      sroot.getExtensionNamespacesManager().registerExtension(extNsSpt);
    }
  }
}
