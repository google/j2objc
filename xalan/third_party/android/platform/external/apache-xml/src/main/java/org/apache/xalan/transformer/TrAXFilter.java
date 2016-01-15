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
 * $Id: TrAXFilter.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;


public class TrAXFilter extends XMLFilterImpl
{
  private Templates m_templates;
  private TransformerImpl m_transformer;
    
  /**
   * Construct an empty XML filter, with no parent.
   *
   * <p>This filter will have no parent: you must assign a parent
   * before you start a parse or do any configuration with
   * setFeature or setProperty.</p>
   *
   * @see org.xml.sax.XMLReader#setFeature
   * @see org.xml.sax.XMLReader#setProperty
   */
  public TrAXFilter (Templates templates)
    throws TransformerConfigurationException
  {
    m_templates = templates;
    m_transformer = (TransformerImpl)templates.newTransformer();
  }
  
  /**
   * Return the Transformer object used for this XML filter.
   */
  public TransformerImpl getTransformer()
  {
    return m_transformer;
  }
  
  /** Set the parent reader.
   *
   * <p>This is the {@link org.xml.sax.XMLReader XMLReader} from which 
   * this filter will obtain its events and to which it will pass its 
   * configuration requests.  The parent may itself be another filter.</p>
   *
   * <p>If there is no parent reader set, any attempt to parse
   * or to set or get a feature or property will fail.</p>
   *
   * @param parent The parent XML reader.
   * @throws java.lang.NullPointerException If the parent is null.
   */
  public void setParent (XMLReader parent)
  { 
    super.setParent(parent);
    
    if(null != parent.getContentHandler())
      this.setContentHandler(parent.getContentHandler());

    // Not really sure if we should do this here, but 
    // it seems safer in case someone calls parse() on 
    // the parent.
    setupParse ();
  }
  
  /**
   * Parse a document.
   *
   * @param input The input source for the document entity.
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @throws java.io.IOException An IO exception from the parser,
   *            possibly from a byte stream or character stream
   *            supplied by the application.
   * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
   */
  public void parse (InputSource input)
    throws org.xml.sax.SAXException, IOException
  {
    if(null == getParent())
    {
      XMLReader reader=null;

      // Use JAXP1.1 ( if possible )
      try {
          javax.xml.parsers.SAXParserFactory factory=
              javax.xml.parsers.SAXParserFactory.newInstance();
          factory.setNamespaceAware( true );
          
          if (m_transformer.getStylesheet().isSecureProcessing()) {
              try {
                  factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
              }
              catch (org.xml.sax.SAXException se) {}
          }
          
          javax.xml.parsers.SAXParser jaxpParser=
              factory.newSAXParser();
          reader=jaxpParser.getXMLReader();
          
      } catch( javax.xml.parsers.ParserConfigurationException ex ) {
          throw new org.xml.sax.SAXException( ex );
      } catch( javax.xml.parsers.FactoryConfigurationError ex1 ) {
          throw new org.xml.sax.SAXException( ex1.toString() );
      } catch( NoSuchMethodError ex2 ) {
      }
      catch (AbstractMethodError ame){}

      XMLReader parent;
      if( reader==null )
          parent= XMLReaderFactory.createXMLReader();
      else
          parent=reader;
      try
      {
        parent.setFeature("http://xml.org/sax/features/namespace-prefixes",
                          true);
      }
      catch (org.xml.sax.SAXException se){}
      // setParent calls setupParse...
      setParent(parent);
    }
    else
    {
      // Make sure everything is set up.
      setupParse ();
    }
    if(null == m_transformer.getContentHandler())
    {
      throw new org.xml.sax.SAXException(XSLMessages.createMessage(XSLTErrorResources.ER_CANNOT_CALL_PARSE, null)); //"parse can not be called if the ContentHandler has not been set!");
    }

    getParent().parse(input);
    Exception e = m_transformer.getExceptionThrown();
    if(null != e)
    {
      if(e instanceof org.xml.sax.SAXException)
        throw (org.xml.sax.SAXException)e;
      else
        throw new org.xml.sax.SAXException(e);
    }
  }
  
  /**
   * Parse a document.
   *
   * @param systemId The system identifier as a fully-qualified URI.
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   * @throws java.io.IOException An IO exception from the parser,
   *            possibly from a byte stream or character stream
   *            supplied by the application.
   * @see org.xml.sax.XMLReader#parse(java.lang.String)
   */
  public void parse (String systemId)
    throws org.xml.sax.SAXException, IOException
  {
    parse(new InputSource(systemId));
  }


  /**
   * Set up before a parse.
   *
   * <p>Before every parse, check whether the parent is
   * non-null, and re-register the filter for all of the 
   * events.</p>
   */
  private void setupParse ()
  {
    XMLReader p = getParent();
    if (p == null) {
      throw new NullPointerException(XSLMessages.createMessage(XSLTErrorResources.ER_NO_PARENT_FOR_FILTER, null)); //"No parent for filter");
    }
    
    ContentHandler ch = m_transformer.getInputContentHandler();
//    if(ch instanceof SourceTreeHandler)
//      ((SourceTreeHandler)ch).setUseMultiThreading(true);
    p.setContentHandler(ch);
    p.setEntityResolver(this);
    p.setDTDHandler(this);
    p.setErrorHandler(this);
  }

  /**
   * Set the content event handler.
   *
   * @param handler The new content handler.
   * @throws java.lang.NullPointerException If the handler
   *            is null.
   * @see org.xml.sax.XMLReader#setContentHandler
   */
  public void setContentHandler (ContentHandler handler)
  {
    m_transformer.setContentHandler(handler);
    // super.setContentHandler(m_transformer.getResultTreeHandler());
  }
  
  public void setErrorListener (ErrorListener handler)
  {
    m_transformer.setErrorListener(handler);
  }

}
