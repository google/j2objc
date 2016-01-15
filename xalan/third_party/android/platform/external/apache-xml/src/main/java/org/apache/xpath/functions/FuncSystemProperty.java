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
 * $Id: FuncSystemProperty.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath.functions;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHErrorResources;

/**
 * Execute the SystemProperty() function.
 * @xsl.usage advanced
 */
public class FuncSystemProperty extends FunctionOneArg
{
    static final long serialVersionUID = 3694874980992204867L;
  /**
   * The path/filename of the property file: XSLTInfo.properties
   * Maintenance note: see also
   * org.apache.xalan.processor.TransformerFactoryImpl.XSLT_PROPERTIES
   */
  static final String XSLT_PROPERTIES = 
            "org/apache/xalan/res/XSLTInfo.properties";

  /**
   * Execute the function.  The function must return
   * a valid object.
   * @param xctxt The current execution context.
   * @return A valid XObject.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {

    String fullName = m_arg0.execute(xctxt).str();
    int indexOfNSSep = fullName.indexOf(':');
    String result = null;
    String propName = "";

    // List of properties where the name of the
    // property argument is to be looked for.
    Properties xsltInfo = new Properties();

    loadPropertyFile(XSLT_PROPERTIES, xsltInfo);

    if (indexOfNSSep > 0)
    {
      String prefix = (indexOfNSSep >= 0)
                      ? fullName.substring(0, indexOfNSSep) : "";
      String namespace;

      namespace = xctxt.getNamespaceContext().getNamespaceForPrefix(prefix);
      propName = (indexOfNSSep < 0)
                 ? fullName : fullName.substring(indexOfNSSep + 1);

      if (namespace.startsWith("http://www.w3.org/XSL/Transform")
              || namespace.equals("http://www.w3.org/1999/XSL/Transform"))
      {
        result = xsltInfo.getProperty(propName);

        if (null == result)
        {
          warn(xctxt, XPATHErrorResources.WG_PROPERTY_NOT_SUPPORTED,
               new Object[]{ fullName });  //"XSL Property not supported: "+fullName);

          return XString.EMPTYSTRING;
        }
      }
      else
      {
        warn(xctxt, XPATHErrorResources.WG_DONT_DO_ANYTHING_WITH_NS,
             new Object[]{ namespace,
                           fullName });  //"Don't currently do anything with namespace "+namespace+" in property: "+fullName);

        try
        {
            //if secure procession is enabled only handle required properties do not not map any valid system property
            if(!xctxt.isSecureProcessing())
            {
                result = System.getProperty(propName);
            }
            else
            {
                warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION,
                        new Object[]{ fullName });  //"SecurityException when trying to access XSL system property: "+fullName);
            }
            if (null == result)
            {
                return XString.EMPTYSTRING;
            }
        }
        catch (SecurityException se)
        {
          warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION,
               new Object[]{ fullName });  //"SecurityException when trying to access XSL system property: "+fullName);

          return XString.EMPTYSTRING;
        }
      }
    }
    else
    {
      try
      {
          //if secure procession is enabled only handle required properties do not not map any valid system property
          if(!xctxt.isSecureProcessing())
          {
              result = System.getProperty(fullName);
          }
          else
          {
              warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION,
                      new Object[]{ fullName });  //"SecurityException when trying to access XSL system property: "+fullName);
          }
          if (null == result)
          {
              return XString.EMPTYSTRING;
          }
      }
      catch (SecurityException se)
      {
        warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION,
             new Object[]{ fullName });  //"SecurityException when trying to access XSL system property: "+fullName);

        return XString.EMPTYSTRING;
      }
    }

    if (propName.equals("version") && result.length() > 0)
    {
      try
      {
        // Needs to return the version number of the spec we conform to.
        return new XString("1.0");
      }
      catch (Exception ex)
      {
        return new XString(result);
      }
    }
    else
      return new XString(result);
  }

  /**
   * Retrieve a propery bundle from a specified file
   * 
   * @param file The string name of the property file.  The name 
   * should already be fully qualified as path/filename
   * @param target The target property bag the file will be placed into.
   */
  public void loadPropertyFile(String file, Properties target)
  {
    try
    {
      // Use SecuritySupport class to provide priveleged access to property file
      SecuritySupport ss = SecuritySupport.getInstance();

      InputStream is = ss.getResourceAsStream(ObjectFactory.findClassLoader(),
                                              file);

      // get a buffered version
      BufferedInputStream bis = new BufferedInputStream(is);

      target.load(bis);  // and load up the property bag from this
      bis.close();  // close out after reading
    }
    catch (Exception ex)
    {
      // ex.printStackTrace();
      throw new org.apache.xml.utils.WrappedRuntimeException(ex);
    }
  }
}
