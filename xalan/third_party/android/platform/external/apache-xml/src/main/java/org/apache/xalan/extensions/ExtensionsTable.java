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
 * $Id: ExtensionsTable.java 469672 2006-10-31 21:56:19Z minchau $
 */
package org.apache.xalan.extensions;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xpath.XPathProcessorException;
import org.apache.xpath.functions.FuncExtFunction;

/**
 * Class holding a table registered extension namespace handlers
 * @xsl.usage internal
 */
public class ExtensionsTable
{  
  /**
   * Table of extensions that may be called from the expression language
   * via the call(name, ...) function.  Objects are keyed on the call
   * name.
   * @xsl.usage internal
   */
  public Hashtable m_extensionFunctionNamespaces = new Hashtable();
  
  /**
   * The StylesheetRoot associated with this extensions table.
   */
  private StylesheetRoot m_sroot;
  
  /**
   * The constructor (called from TransformerImpl) registers the
   * StylesheetRoot for the transformation and instantiates an
   * ExtensionHandler for each extension namespace.
   * @xsl.usage advanced
   */
  public ExtensionsTable(StylesheetRoot sroot)
    throws javax.xml.transform.TransformerException
  {
    m_sroot = sroot;
    Vector extensions = m_sroot.getExtensions();
    for (int i = 0; i < extensions.size(); i++)
    {
      ExtensionNamespaceSupport extNamespaceSpt = 
                 (ExtensionNamespaceSupport)extensions.get(i);
      ExtensionHandler extHandler = extNamespaceSpt.launch();
        if (extHandler != null)
          addExtensionNamespace(extNamespaceSpt.getNamespace(), extHandler);
      }
    }
       
  /**
   * Get an ExtensionHandler object that represents the
   * given namespace.
   * @param extns A valid extension namespace.
   *
   * @return ExtensionHandler object that represents the
   * given namespace.
   */
  public ExtensionHandler get(String extns)
  {
    return (ExtensionHandler) m_extensionFunctionNamespaces.get(extns);
  }

  /**
   * Register an extension namespace handler. This handler provides
   * functions for testing whether a function is known within the
   * namespace and also for invoking the functions.
   *
   * @param uri the URI for the extension.
   * @param extNS the extension handler.
   * @xsl.usage advanced
   */
  public void addExtensionNamespace(String uri, ExtensionHandler extNS)
  {
    m_extensionFunctionNamespaces.put(uri, extNS);
  }

  /**
   * Execute the function-available() function.
   * @param ns       the URI of namespace in which the function is needed
   * @param funcName the function name being tested
   *
   * @return whether the given function is available or not.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean functionAvailable(String ns, String funcName)
          throws javax.xml.transform.TransformerException
  {
    boolean isAvailable = false;
    
    if (null != ns)
    {
      ExtensionHandler extNS = 
           (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (extNS != null)
        isAvailable = extNS.isFunctionAvailable(funcName);
    }
    return isAvailable;
  }
  
  /**
   * Execute the element-available() function.
   * @param ns       the URI of namespace in which the function is needed
   * @param elemName name of element being tested
   *
   * @return whether the given element is available or not.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public boolean elementAvailable(String ns, String elemName)
          throws javax.xml.transform.TransformerException
  {
    boolean isAvailable = false;
    if (null != ns)
    {
      ExtensionHandler extNS = 
               (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (extNS != null) // defensive
        isAvailable = extNS.isElementAvailable(elemName);
    } 
    return isAvailable;        
  }  
  
  /**
   * Handle an extension function.
   * @param ns        the URI of namespace in which the function is needed
   * @param funcName  the function name being called
   * @param argVec    arguments to the function in a vector
   * @param methodKey a unique key identifying this function instance in the
   *                  stylesheet
   * @param exprContext a context which may be passed to an extension function
   *                  and provides callback functions to access various
   *                  areas in the environment
   *
   * @return result of executing the function
   *
   * @throws javax.xml.transform.TransformerException
   */
  public Object extFunction(String ns, String funcName, 
                            Vector argVec, Object methodKey, 
                            ExpressionContext exprContext)
            throws javax.xml.transform.TransformerException
  {
    Object result = null;
    if (null != ns)
    {
      ExtensionHandler extNS =
        (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (null != extNS)
      {
        try
        {
          result = extNS.callFunction(funcName, argVec, methodKey,
                                      exprContext);
        }
        catch (javax.xml.transform.TransformerException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else
      {
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, new Object[]{ns, funcName })); 
        //"Extension function '" + ns + ":" + funcName + "' is unknown");
      }
    }
    return result;    
  }
  
  /**
   * Handle an extension function.
   * @param extFunction  the extension function
   * @param argVec    arguments to the function in a vector
   * @param exprContext a context which may be passed to an extension function
   *                  and provides callback functions to access various
   *                  areas in the environment
   *
   * @return result of executing the function
   *
   * @throws javax.xml.transform.TransformerException
   */
  public Object extFunction(FuncExtFunction extFunction, Vector argVec, 
                            ExpressionContext exprContext)
         throws javax.xml.transform.TransformerException
  {
    Object result = null;
    String ns = extFunction.getNamespace();
    if (null != ns)
    {
      ExtensionHandler extNS =
        (ExtensionHandler) m_extensionFunctionNamespaces.get(ns);
      if (null != extNS)
      {
        try
        {
          result = extNS.callFunction(extFunction, argVec, exprContext);
        }
        catch (javax.xml.transform.TransformerException e)
        {
          throw e;
        }
        catch (Exception e)
        {
          throw new javax.xml.transform.TransformerException(e);
        }
      }
      else
      {
        throw new XPathProcessorException(XSLMessages.createMessage(XSLTErrorResources.ER_EXTENSION_FUNC_UNKNOWN, 
                                          new Object[]{ns, extFunction.getFunctionName()})); 
      }
    }
    return result;        
  }
}
