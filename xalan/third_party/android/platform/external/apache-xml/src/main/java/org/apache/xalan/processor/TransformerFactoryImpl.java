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
 * $Id: TransformerFactoryImpl.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TrAXFilter;
import org.apache.xalan.transformer.TransformerIdentityImpl;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xalan.transformer.XalanProperties;

import org.apache.xml.dtm.ref.sax2dtm.SAX2DTM;
import org.apache.xml.utils.DefaultErrorHandler;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.TreeWalker;
import org.apache.xml.utils.StylesheetPIHandler;
import org.apache.xml.utils.StopParseException;

import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The TransformerFactoryImpl, which implements the TRaX TransformerFactory
 * interface, processes XSLT stylesheets into a Templates object
 * (a StylesheetRoot).
 */
public class TransformerFactoryImpl extends SAXTransformerFactory
{
  /** 
   * The path/filename of the property file: XSLTInfo.properties  
   * Maintenance note: see also
   * <code>org.apache.xpath.functions.FuncSystemProperty.XSLT_PROPERTIES</code>
   */
  public static final String XSLT_PROPERTIES =
    "org/apache/xalan/res/XSLTInfo.properties";

  /**
   * <p>State of secure processing feature.</p>
   */
  private boolean m_isSecureProcessing = false;

  /**
   * Constructor TransformerFactoryImpl
   *
   */
  public TransformerFactoryImpl()
  {
  }

  /** Static string to be used for incremental feature */
  public static final String FEATURE_INCREMENTAL =
                             "http://xml.apache.org/xalan/features/incremental";

  /** Static string to be used for optimize feature */
  public static final String FEATURE_OPTIMIZE =
                             "http://xml.apache.org/xalan/features/optimize";

  /** Static string to be used for source_location feature */
  public static final String FEATURE_SOURCE_LOCATION =
                             XalanProperties.SOURCE_LOCATION;

  public javax.xml.transform.Templates processFromNode(Node node)
          throws TransformerConfigurationException
  {

    try
    {
      TemplatesHandler builder = newTemplatesHandler();
      TreeWalker walker = new TreeWalker(builder,
                                         new org.apache.xml.utils.DOM2Helper(),
                                         builder.getSystemId());

      walker.traverse(node);

      return builder.getTemplates();
    }
    catch (org.xml.sax.SAXException se)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(se));
        }
        catch (TransformerConfigurationException ex)
        {
          throw ex;
        }
        catch (TransformerException ex)
        {
          throw new TransformerConfigurationException(ex);
        }

        return null;
      }
      else
      {

        // Should remove this later... but right now diagnostics from 
        // TransformerConfigurationException are not good.
        // se.printStackTrace();
        throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSFROMNODE_FAILED, null), se); 
        //"processFromNode failed", se);
      }
    }
    catch (TransformerConfigurationException tce)
    {
      // Assume it's already been reported to the error listener.
      throw tce;
    }
   /* catch (TransformerException tce)
    {
      // Assume it's already been reported to the error listener.
      throw new TransformerConfigurationException(tce.getMessage(), tce);
    }*/
    catch (Exception e)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(e));
        }
        catch (TransformerConfigurationException ex)
        {
          throw ex;
        }
        catch (TransformerException ex)
        {
          throw new TransformerConfigurationException(ex);
        }

        return null;
      }
      else
      {
        // Should remove this later... but right now diagnostics from 
        // TransformerConfigurationException are not good.
        // se.printStackTrace();
        throw new TransformerConfigurationException(XSLMessages.createMessage(XSLTErrorResources.ER_PROCESSFROMNODE_FAILED, null), e); //"processFromNode failed",
                                                    //e);
      }
    }
  }

  /**
   * The systemID that was specified in
   * processFromNode(Node node, String systemID).
   */
  private String m_DOMsystemID = null;

  /**
   * The systemID that was specified in
   * processFromNode(Node node, String systemID).
   *
   * @return The systemID, or null.
   */
  String getDOMsystemID()
  {
    return m_DOMsystemID;
  }

  /**
   * Process the stylesheet from a DOM tree, if the
   * processor supports the "http://xml.org/trax/features/dom/input"
   * feature.
   *
   * @param node A DOM tree which must contain
   * valid transform instructions that this processor understands.
   * @param systemID The systemID from where xsl:includes and xsl:imports
   * should be resolved from.
   *
   * @return A Templates object capable of being used for transformation purposes.
   *
   * @throws TransformerConfigurationException
   */
  javax.xml.transform.Templates processFromNode(Node node, String systemID)
          throws TransformerConfigurationException
  {

    m_DOMsystemID = systemID;

    return processFromNode(node);
  }

  /**
   * Get InputSource specification(s) that are associated with the
   * given document specified in the source param,
   * via the xml-stylesheet processing instruction
   * (see http://www.w3.org/TR/xml-stylesheet/), and that matches
   * the given criteria.  Note that it is possible to return several stylesheets
   * that match the criteria, in which case they are applied as if they were
   * a list of imports or cascades.
   *
   * <p>Note that DOM2 has it's own mechanism for discovering stylesheets.
   * Therefore, there isn't a DOM version of this method.</p>
   *
   *
   * @param source The XML source that is to be searched.
   * @param media The media attribute to be matched.  May be null, in which
   *              case the prefered templates will be used (i.e. alternate = no).
   * @param title The value of the title attribute to match.  May be null.
   * @param charset The value of the charset attribute to match.  May be null.
   *
   * @return A Source object capable of being used to create a Templates object.
   *
   * @throws TransformerConfigurationException
   */
  public Source getAssociatedStylesheet(
          Source source, String media, String title, String charset)
            throws TransformerConfigurationException
  {

    String baseID;
    InputSource isource = null;
    Node node = null;
    XMLReader reader = null;

    if (source instanceof DOMSource)
    {
      DOMSource dsource = (DOMSource) source;

      node = dsource.getNode();
      baseID = dsource.getSystemId();
    }
    else
    {
      isource = SAXSource.sourceToInputSource(source);
      baseID = isource.getSystemId();
    }

    // What I try to do here is parse until the first startElement
    // is found, then throw a special exception in order to terminate 
    // the parse.
    StylesheetPIHandler handler = new StylesheetPIHandler(baseID, media,
                                    title, charset);
    
    // Use URIResolver. Patch from Dmitri Ilyin 
    if (m_uriResolver != null) 
    {
      handler.setURIResolver(m_uriResolver); 
    }

    try
    {
      if (null != node)
      {
        TreeWalker walker = new TreeWalker(handler, new org.apache.xml.utils.DOM2Helper(), baseID);

        walker.traverse(node);
      }
      else
      {

        // Use JAXP1.1 ( if possible )
        try
        {
          javax.xml.parsers.SAXParserFactory factory =
            javax.xml.parsers.SAXParserFactory.newInstance();

          factory.setNamespaceAware(true);

          if (m_isSecureProcessing)
          {
            try
            {
              factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (org.xml.sax.SAXException e) {}
          }

          javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();

          reader = jaxpParser.getXMLReader();
        }
        catch (javax.xml.parsers.ParserConfigurationException ex)
        {
          throw new org.xml.sax.SAXException(ex);
        }
        catch (javax.xml.parsers.FactoryConfigurationError ex1)
        {
          throw new org.xml.sax.SAXException(ex1.toString());
        }
        catch (NoSuchMethodError ex2){}
        catch (AbstractMethodError ame){}

        if (null == reader)
        {
          reader = XMLReaderFactory.createXMLReader();
        }

        if(m_isSecureProcessing)
        {
            reader.setFeature("http://xml.org/sax/features/external-general-entities",false);
        }
        // Need to set options!
        reader.setContentHandler(handler);
        reader.parse(isource);
      }
    }
    catch (StopParseException spe)
    {

      // OK, good.
    }
    catch (org.xml.sax.SAXException se)
    {
      throw new TransformerConfigurationException(
        "getAssociatedStylesheets failed", se);
    }
    catch (IOException ioe)
    {
      throw new TransformerConfigurationException(
        "getAssociatedStylesheets failed", ioe);
    }

    return handler.getAssociatedStylesheet();
  }

  /**
   * Create a new Transformer object that performs a copy
   * of the source to the result.
   *
   * @return A Transformer object that may be used to perform a transformation
   * in a single thread, never null.
   *
   * @throws TransformerConfigurationException May throw this during
   *            the parse when it is constructing the
   *            Templates object and fails.
   */
  public TemplatesHandler newTemplatesHandler()
          throws TransformerConfigurationException
  {
    return new StylesheetHandler(this);
  }

  /**
   * <p>Set a feature for this <code>TransformerFactory</code> and <code>Transformer</code>s
   * or <code>Template</code>s created by this factory.</p>
   * 
   * <p>
   * Feature names are fully qualified {@link java.net.URI}s.
   * Implementations may define their own features.
   * An {@link TransformerConfigurationException} is thrown if this <code>TransformerFactory</code> or the
   * <code>Transformer</code>s or <code>Template</code>s it creates cannot support the feature.
   * It is possible for an <code>TransformerFactory</code> to expose a feature value but be unable to change its state.
   * </p>
   * 
   * <p>See {@link javax.xml.transform.TransformerFactory} for full documentation of specific features.</p>
   * 
   * @param name Feature name.
   * @param value Is feature state <code>true</code> or <code>false</code>.
   *  
   * @throws TransformerConfigurationException if this <code>TransformerFactory</code>
   *   or the <code>Transformer</code>s or <code>Template</code>s it creates cannot support this feature.
   * @throws NullPointerException If the <code>name</code> parameter is null.
   */
  public void setFeature(String name, boolean value)
	  throws TransformerConfigurationException {
  
  	// feature name cannot be null
  	if (name == null) {
  	    throw new NullPointerException(
                  XSLMessages.createMessage(
                      XSLTErrorResources.ER_SET_FEATURE_NULL_NAME, null));    
  	}
  		
  	// secure processing?
  	if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
  	    m_isSecureProcessing = value;			
  	}
  	// This implementation does not support the setting of a feature other than
  	// the secure processing feature.
  	else
    {
      throw new TransformerConfigurationException(
          XSLMessages.createMessage(
            XSLTErrorResources.ER_UNSUPPORTED_FEATURE, 
            new Object[] {name}));
    }
  }

  /**
   * Look up the value of a feature.
   * <p>The feature name is any fully-qualified URI.  It is
   * possible for an TransformerFactory to recognize a feature name but
   * to be unable to return its value; this is especially true
   * in the case of an adapter for a SAX1 Parser, which has
   * no way of knowing whether the underlying parser is
   * validating, for example.</p>
   *
   * @param name The feature name, which is a fully-qualified URI.
   * @return The current state of the feature (true or false).
   */
  public boolean getFeature(String name) {
  	
    // feature name cannot be null
    if (name == null) 
    {
    	throw new NullPointerException(
            XSLMessages.createMessage(
            XSLTErrorResources.ER_GET_FEATURE_NULL_NAME, null));    
    }
	  	
    // Try first with identity comparison, which 
    // will be faster.
    if ((DOMResult.FEATURE == name) || (DOMSource.FEATURE == name)
            || (SAXResult.FEATURE == name) || (SAXSource.FEATURE == name)
            || (StreamResult.FEATURE == name)
            || (StreamSource.FEATURE == name)
            || (SAXTransformerFactory.FEATURE == name)
            || (SAXTransformerFactory.FEATURE_XMLFILTER == name))
      return true;
    else if ((DOMResult.FEATURE.equals(name))
             || (DOMSource.FEATURE.equals(name))
             || (SAXResult.FEATURE.equals(name))
             || (SAXSource.FEATURE.equals(name))
             || (StreamResult.FEATURE.equals(name))
             || (StreamSource.FEATURE.equals(name))
             || (SAXTransformerFactory.FEATURE.equals(name))
             || (SAXTransformerFactory.FEATURE_XMLFILTER.equals(name)))
      return true;	      
    // secure processing?
    else if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING))
      return m_isSecureProcessing;
    else      
      // unknown feature
      return false;
  }
  
  /**
   * Flag set by FEATURE_OPTIMIZE.
   * This feature specifies whether to Optimize stylesheet processing. By
   * default it is set to true.
   */
  private boolean m_optimize = true;
  
  /** Flag set by FEATURE_SOURCE_LOCATION.
   * This feature specifies whether the transformation phase should
   * keep track of line and column numbers for the input source
   * document. Note that this works only when that
   * information is available from the source -- in other words, if you
   * pass in a DOM, there's little we can do for you.
   * 
   * The default is false. Setting it true may significantly
   * increase storage cost per node. 
   */
  private boolean m_source_location = false;
  
  /**
   * Flag set by FEATURE_INCREMENTAL.
   * This feature specifies whether to produce output incrementally, rather than
   * waiting to finish parsing the input before generating any output. By 
   * default this attribute is set to false. 
   */
  private boolean m_incremental = false;
  
  /**
   * Allows the user to set specific attributes on the underlying
   * implementation.
   *
   * @param name The name of the attribute.
   * @param value The value of the attribute; Boolean or String="true"|"false"
   *
   * @throws IllegalArgumentException thrown if the underlying
   * implementation doesn't recognize the attribute.
   */
  public void setAttribute(String name, Object value)
          throws IllegalArgumentException
  {
    if (name.equals(FEATURE_INCREMENTAL))
    {
      if(value instanceof Boolean)
      {
        // Accept a Boolean object..
        m_incremental = ((Boolean)value).booleanValue();
      }
      else if(value instanceof String)
      {
        // .. or a String object
        m_incremental = (new Boolean((String)value)).booleanValue();
      }
      else
      {
        // Give a more meaningful error message
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value})); //name + " bad value " + value);
      }
	}
    else if (name.equals(FEATURE_OPTIMIZE))
    {
      if(value instanceof Boolean)
      {
        // Accept a Boolean object..
        m_optimize = ((Boolean)value).booleanValue();
      }
      else if(value instanceof String)
      {
        // .. or a String object
        m_optimize = (new Boolean((String)value)).booleanValue();
      }
      else
      {
        // Give a more meaningful error message
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value})); //name + " bad value " + value);
      }
    }
    
    // Custom Xalan feature: annotate DTM with SAX source locator fields.
    // This gets used during SAX2DTM instantiation. 
    //
    // %REVIEW% Should the name of this field really be in XalanProperties?
    // %REVIEW% I hate that it's a global static, but didn't want to change APIs yet.
    else if(name.equals(FEATURE_SOURCE_LOCATION))
    {
      if(value instanceof Boolean)
      {
        // Accept a Boolean object..
        m_source_location = ((Boolean)value).booleanValue();
      }
      else if(value instanceof String)
      {
        // .. or a String object
        m_source_location = (new Boolean((String)value)).booleanValue();
      }
      else
      {
        // Give a more meaningful error message
        throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_BAD_VALUE, new Object[]{name, value})); //name + " bad value " + value);
      }
    }
    
    else
    {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_NOT_SUPPORTED, new Object[]{name})); //name + "not supported");
    }
  }

  /**
   * Allows the user to retrieve specific attributes on the underlying
   * implementation.
   *
   * @param name The name of the attribute.
   * @return value The value of the attribute.
   *
   * @throws IllegalArgumentException thrown if the underlying
   * implementation doesn't recognize the attribute.
   */
  public Object getAttribute(String name) throws IllegalArgumentException
  {
    if (name.equals(FEATURE_INCREMENTAL))
    {
      return new Boolean(m_incremental);            
    }
    else if (name.equals(FEATURE_OPTIMIZE))
    {
      return new Boolean(m_optimize);
    }
    else if (name.equals(FEATURE_SOURCE_LOCATION))
    {
      return new Boolean(m_source_location);
    }
    else
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ATTRIB_VALUE_NOT_RECOGNIZED, new Object[]{name})); //name + " attribute not recognized");
  }

  /**
   * Create an XMLFilter that uses the given source as the
   * transformation instructions.
   *
   * @param src The source of the transformation instructions.
   *
   * @return An XMLFilter object, or null if this feature is not supported.
   *
   * @throws TransformerConfigurationException
   */
  public XMLFilter newXMLFilter(Source src)
          throws TransformerConfigurationException
  {

    Templates templates = newTemplates(src);
    if( templates==null ) return null;
    
    return newXMLFilter(templates);
  }

  /**
   * Create an XMLFilter that uses the given source as the
   * transformation instructions.
   *
   * @param templates non-null reference to Templates object.
   *
   * @return An XMLFilter object, or null if this feature is not supported.
   *
   * @throws TransformerConfigurationException
   */
  public XMLFilter newXMLFilter(Templates templates)
          throws TransformerConfigurationException
  {
    try 
    {
      return new TrAXFilter(templates);
    } 
    catch( TransformerConfigurationException ex ) 
    {
      if( m_errorListener != null) 
      {
        try 
        {
          m_errorListener.fatalError( ex );
          return null;
        } 
        catch( TransformerConfigurationException ex1 ) 
        {
          throw ex1;
        }
        catch( TransformerException ex1 ) 
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      throw ex;
    }
  }

  /**
   * Get a TransformerHandler object that can process SAX
   * ContentHandler events into a Result, based on the transformation
   * instructions specified by the argument.
   *
   * @param src The source of the transformation instructions.
   *
   * @return TransformerHandler ready to transform SAX events.
   *
   * @throws TransformerConfigurationException
   */
  public TransformerHandler newTransformerHandler(Source src)
          throws TransformerConfigurationException
  {

    Templates templates = newTemplates(src);
    if( templates==null ) return null;
    
    return newTransformerHandler(templates);
  }

  /**
   * Get a TransformerHandler object that can process SAX
   * ContentHandler events into a Result, based on the Templates argument.
   *
   * @param templates The source of the transformation instructions.
   *
   * @return TransformerHandler ready to transform SAX events.
   * @throws TransformerConfigurationException
   */
  public TransformerHandler newTransformerHandler(Templates templates)
          throws TransformerConfigurationException
  {
    try {
      TransformerImpl transformer =
        (TransformerImpl) templates.newTransformer();
      transformer.setURIResolver(m_uriResolver);
      TransformerHandler th =
        (TransformerHandler) transformer.getInputContentHandler(true);

      return th;
    } 
    catch( TransformerConfigurationException ex ) 
    {
      if( m_errorListener != null ) 
      {
        try 
        {
          m_errorListener.fatalError( ex );
          return null;
        } 
        catch (TransformerConfigurationException ex1 ) 
        {
          throw ex1;
        }
        catch (TransformerException ex1 ) 
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      
      throw ex;
    }
    
  }

//  /** The identity transform string, for support of newTransformerHandler()
//   *  and newTransformer().  */
//  private static final String identityTransform =
//    "<xsl:stylesheet " + "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' "
//    + "version='1.0'>" + "<xsl:template match='/|node()'>"
//    + "<xsl:copy-of select='.'/>" + "</xsl:template>" + "</xsl:stylesheet>";
//
//  /** The identity transform Templates, built from identityTransform, 
//   *  for support of newTransformerHandler() and newTransformer().  */
//  private static Templates m_identityTemplate = null;

  /**
   * Get a TransformerHandler object that can process SAX
   * ContentHandler events into a Result.
   *
   * @return TransformerHandler ready to transform SAX events.
   *
   * @throws TransformerConfigurationException
   */
  public TransformerHandler newTransformerHandler()
          throws TransformerConfigurationException
  {
    return new TransformerIdentityImpl(m_isSecureProcessing);
  }

  /**
   * Process the source into a Transformer object.  Care must
   * be given to know that this object can not be used concurrently
   * in multiple threads.
   *
   * @param source An object that holds a URL, input stream, etc.
   *
   * @return A Transformer object capable of
   * being used for transformation purposes in a single thread.
   *
   * @throws TransformerConfigurationException May throw this during the parse when it
   *            is constructing the Templates object and fails.
   */
  public Transformer newTransformer(Source source)
          throws TransformerConfigurationException
  {
    try 
    {
      Templates tmpl=newTemplates( source );
      /* this can happen if an ErrorListener is present and it doesn't
         throw any exception in fatalError. 
         The spec says: "a Transformer must use this interface
         instead of throwing an exception" - the newTemplates() does
         that, and returns null.
      */
      if( tmpl==null ) return null;
      Transformer transformer = tmpl.newTransformer();
      transformer.setURIResolver(m_uriResolver);
      return transformer;
    } 
    catch( TransformerConfigurationException ex ) 
    {
      if( m_errorListener != null ) 
      {
        try 
        {
          m_errorListener.fatalError( ex );
          return null; // TODO: but the API promises to never return null...
        } 
        catch( TransformerConfigurationException ex1 ) 
        {
          throw ex1;
        }
        catch( TransformerException ex1 ) 
        {
          throw new TransformerConfigurationException( ex1 );
        }
      }
      throw ex;
    }
  }

  /**
   * Create a new Transformer object that performs a copy
   * of the source to the result.
   *
   * @return A Transformer object capable of
   * being used for transformation purposes in a single thread.
   *
   * @throws TransformerConfigurationException May throw this during
   *            the parse when it is constructing the
   *            Templates object and it fails.
   */
  public Transformer newTransformer() throws TransformerConfigurationException
  {
      return new TransformerIdentityImpl(m_isSecureProcessing);
  }

  /**
   * Process the source into a Templates object, which is likely
   * a compiled representation of the source. This Templates object
   * may then be used concurrently across multiple threads.  Creating
   * a Templates object allows the TransformerFactory to do detailed
   * performance optimization of transformation instructions, without
   * penalizing runtime transformation.
   *
   * @param source An object that holds a URL, input stream, etc.
   * @return A Templates object capable of being used for transformation purposes.
   *
   * @throws TransformerConfigurationException May throw this during the parse when it
   *            is constructing the Templates object and fails.
   */
  public Templates newTemplates(Source source)
          throws TransformerConfigurationException
  {

    String baseID = source.getSystemId();

    if (null != baseID) {
       baseID = SystemIDResolver.getAbsoluteURI(baseID);
    }


    if (source instanceof DOMSource)
    {
      DOMSource dsource = (DOMSource) source;
      Node node = dsource.getNode();

      if (null != node)
        return processFromNode(node, baseID);
      else
      {
        String messageStr = XSLMessages.createMessage(
          XSLTErrorResources.ER_ILLEGAL_DOMSOURCE_INPUT, null);

        throw new IllegalArgumentException(messageStr);
      }
    }

    TemplatesHandler builder = newTemplatesHandler();
    builder.setSystemId(baseID);
    
    try
    {
      InputSource isource = SAXSource.sourceToInputSource(source);
      isource.setSystemId(baseID);
      XMLReader reader = null;

      if (source instanceof SAXSource)
        reader = ((SAXSource) source).getXMLReader();
        
      if (null == reader)
      {

        // Use JAXP1.1 ( if possible )
        try
        {
          javax.xml.parsers.SAXParserFactory factory =
            javax.xml.parsers.SAXParserFactory.newInstance();

          factory.setNamespaceAware(true);

          if (m_isSecureProcessing)
          {
            try
            {
              factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (org.xml.sax.SAXException se) {}
          }

          javax.xml.parsers.SAXParser jaxpParser = factory.newSAXParser();

          reader = jaxpParser.getXMLReader();
        }
        catch (javax.xml.parsers.ParserConfigurationException ex)
        {
          throw new org.xml.sax.SAXException(ex);
        }
        catch (javax.xml.parsers.FactoryConfigurationError ex1)
        {
          throw new org.xml.sax.SAXException(ex1.toString());
        }
        catch (NoSuchMethodError ex2){}
        catch (AbstractMethodError ame){}
      }

      if (null == reader)
        reader = XMLReaderFactory.createXMLReader();

      // If you set the namespaces to true, we'll end up getting double 
      // xmlns attributes.  Needs to be fixed.  -sb
      // reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
      reader.setContentHandler(builder);
      reader.parse(isource);
    }
    catch (org.xml.sax.SAXException se)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(se));
        }
        catch (TransformerConfigurationException ex1)
        {
          throw ex1;
        }
        catch (TransformerException ex1)
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      else
      {
        throw new TransformerConfigurationException(se.getMessage(), se);
      }
    }
    catch (Exception e)
    {
      if (m_errorListener != null)
      {
        try
        {
          m_errorListener.fatalError(new TransformerException(e));
          return null;
        }
        catch (TransformerConfigurationException ex1)
        {
          throw ex1;
        }
        catch (TransformerException ex1)
        {
          throw new TransformerConfigurationException(ex1);
        }
      }
      else
      {
        throw new TransformerConfigurationException(e.getMessage(), e);
      }
    }

    return builder.getTemplates();
  }

  /**
   * The object that implements the URIResolver interface,
   * or null.
   */
  URIResolver m_uriResolver;

  /**
   * Set an object that will be used to resolve URIs used in
   * xsl:import, etc.  This will be used as the default for the
   * transformation.
   * @param resolver An object that implements the URIResolver interface,
   * or null.
   */
  public void setURIResolver(URIResolver resolver)
  {
    m_uriResolver = resolver;
  }

  /**
   * Get the object that will be used to resolve URIs used in
   * xsl:import, etc.  This will be used as the default for the
   * transformation.
   *
   * @return The URIResolver that was set with setURIResolver.
   */
  public URIResolver getURIResolver()
  {
    return m_uriResolver;
  }

  /** The error listener.   */
  private ErrorListener m_errorListener = new org.apache.xml.utils.DefaultErrorHandler(false);

  /**
   * Get the error listener in effect for the TransformerFactory.
   *
   * @return A non-null reference to an error listener.
   */
  public ErrorListener getErrorListener()
  {
    return m_errorListener;
  }

  /**
   * Set an error listener for the TransformerFactory.
   *
   * @param listener Must be a non-null reference to an ErrorListener.
   *
   * @throws IllegalArgumentException if the listener argument is null.
   */
  public void setErrorListener(ErrorListener listener)
          throws IllegalArgumentException
  {

    if (null == listener)
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_ERRORLISTENER, null));
      // "ErrorListener");

    m_errorListener = listener;
  }
  
  /**
   * Return the state of the secure processing feature.
   * 
   * @return state of the secure processing feature.
   */
  public boolean isSecureProcessing()
  {
    return m_isSecureProcessing;
  }
}
