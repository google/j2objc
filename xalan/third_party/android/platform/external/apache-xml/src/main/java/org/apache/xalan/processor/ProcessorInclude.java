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
 * $Id: ProcessorInclude.java 469349 2006-10-31 03:06:50Z minchau $
 */
package org.apache.xalan.processor;

import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.TreeWalker;

import org.w3c.dom.Node;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * TransformerFactory class for xsl:include markup.
 * @see <a href="http://www.w3.org/TR/xslt#dtd">XSLT DTD</a>
 * @see <a href="http://www.w3.org/TR/xslt#include">include in XSLT Specification</a>
 * 
 * @xsl.usage internal
 */
public class ProcessorInclude extends XSLTElementProcessor
{
    static final long serialVersionUID = -4570078731972673481L;

  /**
   * The base URL of the XSL document.
   * @serial
   */
  private String m_href = null;

  /**
   * Get the base identifier with which this stylesheet is associated.
   *
   * @return non-null reference to the href attribute string, or 
   *         null if setHref has not been called.
   */
  public String getHref()
  {
    return m_href;
  }

  /**
   * Get the base identifier with which this stylesheet is associated.
   *
   * @param baseIdent Should be a non-null reference to a valid URL string.
   */
  public void setHref(String baseIdent)
  {
    // Validate?
    m_href = baseIdent;
  }

  /**
   * Get the stylesheet type associated with an included stylesheet
   *
   * @return the type of the stylesheet
   */
  protected int getStylesheetType()
  {
    return StylesheetHandler.STYPE_INCLUDE;
  }

  /**
   * Get the error number associated with this type of stylesheet including itself
   *
   * @return the appropriate error number
   */
  protected String getStylesheetInclErr()
  {
    return XSLTErrorResources.ER_STYLESHEET_INCLUDES_ITSELF;
  }

  /**
   * Receive notification of the start of an xsl:include element.
   *
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   * @param uri The Namespace URI, or the empty string if the
   *        element has no Namespace URI or if Namespace
   *        processing is not being performed.
   * @param localName The local name (without prefix), or the
   *        empty string if Namespace processing is not being
   *        performed.
   * @param rawName The raw XML 1.0 name (with prefix), or the
   *        empty string if raw names are not available.
   * @param attributes The attributes attached to the element.  If
   *        there are no attributes, it shall be an empty
   *        Attributes object.
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {


    setPropertiesFromAttributes(handler, rawName, attributes, this);

    try
    {

      // Get the Source from the user's URIResolver (if any).
      Source sourceFromURIResolver = getSourceFromUriResolver(handler);
      // Get the system ID of the included/imported stylesheet module
      String hrefUrl = getBaseURIOfIncludedStylesheet(handler, sourceFromURIResolver);

      if (handler.importStackContains(hrefUrl))
      {
        throw new org.xml.sax.SAXException(
          XSLMessages.createMessage(
          getStylesheetInclErr(), new Object[]{ hrefUrl }));  //"(StylesheetHandler) "+hrefUrl+" is directly or indirectly importing itself!");
      }

      // Push the system ID and corresponding Source
      // on some stacks for later retrieval during parse() time.
      handler.pushImportURL(hrefUrl);
      handler.pushImportSource(sourceFromURIResolver);

      int savedStylesheetType = handler.getStylesheetType();

      handler.setStylesheetType(this.getStylesheetType());
      handler.pushNewNamespaceSupport();

      try
      {
        parse(handler, uri, localName, rawName, attributes);
      }
      finally
      {
        handler.setStylesheetType(savedStylesheetType);
        handler.popImportURL();
        handler.popImportSource();
        handler.popNamespaceSupport();
      }
    }
    catch(TransformerException te)
    {
      handler.error(te.getMessage(), te);
    }
  }

  /**
   * Set off a new parse for an included or imported stylesheet.  This will 
   * set the {@link StylesheetHandler} to a new state, and recurse in with 
   * a new set of parse events.  Once this function returns, the state of 
   * the StylesheetHandler should be restored.
   *
   * @param handler non-null reference to current StylesheetHandler that is constructing the Templates.
   * @param uri The Namespace URI, which should be the XSLT namespace.
   * @param localName The local name (without prefix), which should be "include" or "import".
   * @param rawName The qualified name (with prefix).
   * @param attributes The list of attributes on the xsl:include or xsl:import element.
   *
   * @throws org.xml.sax.SAXException Any SAX exception, possibly
   *            wrapping another exception.
   */
  protected void parse(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws org.xml.sax.SAXException
  {
    TransformerFactoryImpl processor = handler.getStylesheetProcessor();
    URIResolver uriresolver = processor.getURIResolver();

    try
    {
      Source source = null;
      
      // The base identifier, an aboslute URI
      // that is associated with the included/imported
      // stylesheet module is known in this method,
      // so this method does the pushing of the
      // base ID onto the stack.
     
      if (null != uriresolver)
      {
        // There is a user provided URI resolver.
        // At the startElement() call we would
        // have tried to obtain a Source from it
        // which we now retrieve
        source = handler.peekSourceFromURIResolver();

        if (null != source && source instanceof DOMSource)
        {
          Node node = ((DOMSource)source).getNode();
          
          // There is a user provided URI resolver.
          // At the startElement() call we would
          // have already pushed the system ID, obtained
          // from either the source.getSystemId(), if non-null
          // or from SystemIDResolver.getAbsoluteURI() as a backup
          // which we now retrieve.
          String systemId = handler.peekImportURL();
          
          // Push the absolute URI of the included/imported
          // stylesheet module onto the stack.
          if (systemId != null)
              handler.pushBaseIndentifier(systemId);
        
          TreeWalker walker = new TreeWalker(handler, new org.apache.xml.utils.DOM2Helper(), systemId);

          try
          {
            walker.traverse(node);
          }
          catch(org.xml.sax.SAXException se)
          {
            throw new TransformerException(se);
          }
          if (systemId != null)
            handler.popBaseIndentifier();
          return;
        }
      }
      
      if(null == source)
      {
        String absURL = SystemIDResolver.getAbsoluteURI(getHref(),
                          handler.getBaseIdentifier());

        source = new StreamSource(absURL);
      }
      
      // possible callback to a class that over-rides this method.
      source = processSource(handler, source);
      
      XMLReader reader = null;
      
      if(source instanceof SAXSource)
      {
        SAXSource saxSource = (SAXSource)source;
        reader = saxSource.getXMLReader(); // may be null
      }
      
      InputSource inputSource = SAXSource.sourceToInputSource(source);

      if (null == reader)
      {  
        // Use JAXP1.1 ( if possible )
        try {
          javax.xml.parsers.SAXParserFactory factory=
                                                     javax.xml.parsers.SAXParserFactory.newInstance();
          factory.setNamespaceAware( true );
          
          if (handler.getStylesheetProcessor().isSecureProcessing())
          {
            try
            {
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
        } 
        catch( NoSuchMethodError ex2 ) 
        {
        }
        catch (AbstractMethodError ame){}
      }
      if (null == reader)
        reader = XMLReaderFactory.createXMLReader();

      if (null != reader)
      {
        reader.setContentHandler(handler);
        
        // Push the absolute URI of the included/imported
        // stylesheet module onto the stack.
        handler.pushBaseIndentifier(inputSource.getSystemId());

        try
        {
          reader.parse(inputSource);
        }
        finally
        {
          handler.popBaseIndentifier();
        }
      }
    }
    catch (IOException ioe)
    {
      handler.error(XSLTErrorResources.ER_IOEXCEPTION,
                    new Object[]{ getHref() }, ioe);
    }
    catch(TransformerException te)
    {
      handler.error(te.getMessage(), te);
    }
  }

  /**
   * This method does nothing, but a class that extends this class could
   * over-ride it and do some processing of the source.
   * @param handler The calling StylesheetHandler/TemplatesBuilder.
   * @param source The source of the included stylesheet.
   * @return the same or an equivalent source to what was passed in.
   */
  protected Source processSource(StylesheetHandler handler, Source source)
  {
      return source;
  }
  
  /**
   * Get the Source object for the included or imported stylesheet module
   * obtained from the user's URIResolver, if there is no user provided 
   * URIResolver null is returned.
   */
  private Source getSourceFromUriResolver(StylesheetHandler handler)
            throws TransformerException {
        Source s = null;
            TransformerFactoryImpl processor = handler.getStylesheetProcessor();
            URIResolver uriresolver = processor.getURIResolver();
            if (uriresolver != null) {
                String href = getHref();
                String base = handler.getBaseIdentifier();
                s = uriresolver.resolve(href,base);
            }

        return s;
    }

    /**
     * Get the base URI of the included or imported stylesheet,
     * if the user provided a URIResolver, then get the Source
     * object for the stylsheet from it, and get the systemId 
     * from that Source object, otherwise try to recover by
     * using the SysteIDResolver to figure out the base URI.
     * @param handler The handler that processes the stylesheet as SAX events,
     * and maintains state
     * @param s The Source object from a URIResolver, for the included stylesheet module,
     * so this will be null if there is no URIResolver set.
     */
    private String getBaseURIOfIncludedStylesheet(StylesheetHandler handler, Source s)
            throws TransformerException {
        

        
        String baseURI;
        String idFromUriResolverSource;
        if (s != null && (idFromUriResolverSource = s.getSystemId()) != null) {
            // We have a Source obtained from a users's URIResolver,
            // and the system ID is set on it, so return that as the base URI
            baseURI = idFromUriResolverSource;
        } else {
            // The user did not provide a URIResolver, or it did not 
            // return a Source for the included stylesheet module, or
            // the Source has no system ID set, so we fall back to using
            // the system ID Resolver to take the href and base
            // to generate the baseURI of the included stylesheet.
            baseURI = SystemIDResolver.getAbsoluteURI(getHref(), handler
                    .getBaseIdentifier());
        }

        return baseURI;
    }
}
