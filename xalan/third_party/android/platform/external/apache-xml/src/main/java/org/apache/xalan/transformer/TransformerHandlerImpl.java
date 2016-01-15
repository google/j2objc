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
 * $Id: TransformerHandlerImpl.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMManager;
import org.apache.xml.dtm.ref.IncrementalSAXSource_Filter;
import org.apache.xml.dtm.ref.sax2dtm.SAX2DTM;
import org.apache.xpath.XPathContext;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.apache.xml.serializer.SerializationHandler;


/**
 * A TransformerHandler
 * listens for SAX ContentHandler parse events and transforms
 * them to a Result.
 */
public class TransformerHandlerImpl
        implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler,
                   LexicalHandler, TransformerHandler, DeclHandler
{
    /**
     * The flag for the setting of the optimize feature;
     */    
    private final boolean m_optimizer;

    /**
     * The flag for the setting of the incremental feature;
     */    
    private final boolean m_incremental;

    /**
     * The flag for the setting of the source_location feature;
     */  
    private final boolean m_source_location;
  
  private boolean m_insideParse = false;

  ////////////////////////////////////////////////////////////////////
  // Constructors.
  ////////////////////////////////////////////////////////////////////

  /**
   * Construct a TransformerHandlerImpl.
   *
   * @param transformer Non-null reference to the Xalan transformer impl.
   * @param doFragment True if the result should be a document fragement.
   * @param baseSystemID  The system ID to use as the base for relative URLs.
   */
  public TransformerHandlerImpl(TransformerImpl transformer,
                                boolean doFragment, String baseSystemID)
  {

    super();

    m_transformer = transformer;
    m_baseSystemID = baseSystemID;

    XPathContext xctxt = transformer.getXPathContext();
    DTM dtm = xctxt.getDTM(null, true, transformer, true, true);
    
    m_dtm = dtm;
    dtm.setDocumentBaseURI(baseSystemID);

    m_contentHandler = dtm.getContentHandler();
    m_dtdHandler = dtm.getDTDHandler();
    m_entityResolver = dtm.getEntityResolver();
    m_errorHandler = dtm.getErrorHandler();
    m_lexicalHandler = dtm.getLexicalHandler();
    m_incremental = transformer.getIncremental();
    m_optimizer = transformer.getOptimize();
    m_source_location = transformer.getSource_location();
  }
  
  /** 
   * Do what needs to be done to shut down the CoRoutine management.
   */
  protected void clearCoRoutine()
  {
    clearCoRoutine(null);
  }
  
  /** 
   * Do what needs to be done to shut down the CoRoutine management.
   */
  protected void clearCoRoutine(SAXException ex)
  {
    if(null != ex)
      m_transformer.setExceptionThrown(ex);
    
    if(m_dtm instanceof SAX2DTM)
    {
      if(DEBUG)
        System.err.println("In clearCoRoutine...");
      try
      {
        SAX2DTM sax2dtm = ((SAX2DTM)m_dtm);          
        if(null != m_contentHandler 
           && m_contentHandler instanceof IncrementalSAXSource_Filter)
        {
          IncrementalSAXSource_Filter sp =
            (IncrementalSAXSource_Filter)m_contentHandler;
          // This should now be all that's needed.
          sp.deliverMoreNodes(false);
        }
        
        sax2dtm.clearCoRoutine(true);
        m_contentHandler = null;
        m_dtdHandler = null;
        m_entityResolver = null;
        m_errorHandler = null;
        m_lexicalHandler = null;
      }
      catch(Throwable throwable)
      {
        throwable.printStackTrace();
      }
      
      if(DEBUG)
        System.err.println("...exiting clearCoRoutine");
    }
  }
  
  ////////////////////////////////////////////////////////////////////
  // Implementation of javax.xml.transform.sax.TransformerHandler.
  ////////////////////////////////////////////////////////////////////

  /**
   * Enables the user of the TransformerHandler to set the
   * to set the Result for the transformation.
   *
   * @param result A Result instance, should not be null.
   *
   * @throws IllegalArgumentException if result is invalid for some reason.
   */
  public void setResult(Result result) throws IllegalArgumentException
  {

    if (null == result)
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_RESULT_NULL, null)); //"result should not be null");

    try
    {
//      ContentHandler handler =
//        m_transformer.createResultContentHandler(result);
//      m_transformer.setContentHandler(handler);
        SerializationHandler xoh = 
            m_transformer.createSerializationHandler(result);
        m_transformer.setSerializationHandler(xoh);
    }
    catch (javax.xml.transform.TransformerException te)
    {
      throw new IllegalArgumentException(XSLMessages.createMessage(XSLTErrorResources.ER_RESULT_COULD_NOT_BE_SET, null)); //"result could not be set");
    }

    m_result = result;
  }

  /**
   * Set the base ID (URI or system ID) from where relative
   * URLs will be resolved.
   * @param systemID Base URI for the source tree.
   */
  public void setSystemId(String systemID)
  {
    m_baseSystemID = systemID;
    m_dtm.setDocumentBaseURI(systemID);
  }

  /**
   * Get the base ID (URI or system ID) from where relative
   * URLs will be resolved.
   * @return The systemID that was set with {@link #setSystemId}.
   */
  public String getSystemId()
  {
    return m_baseSystemID;
  }

  /**
   * Get the Transformer associated with this handler, which
   * is needed in order to set parameters and output properties.
   *
   * @return The Transformer associated with this handler
   */
  public Transformer getTransformer()
  {
    return m_transformer;
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of org.xml.sax.EntityResolver.
  ////////////////////////////////////////////////////////////////////

  /**
   * Filter an external entity resolution.
   *
   * @param publicId The entity's public identifier, or null.
   * @param systemId The entity's system identifier.
   * @return A new InputSource or null for the default.
   *
   * @throws IOException
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @throws java.io.IOException The client may throw an
   *            I/O-related exception while obtaining the
   *            new InputSource.
   * @see org.xml.sax.EntityResolver#resolveEntity
   */
  public InputSource resolveEntity(String publicId, String systemId)
          throws SAXException, IOException
  {

    if (m_entityResolver != null)
    {
      return m_entityResolver.resolveEntity(publicId, systemId);
    }
    else
    {
      return null;
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of org.xml.sax.DTDHandler.
  ////////////////////////////////////////////////////////////////////

  /**
   * Filter a notation declaration event.
   *
   * @param name The notation name.
   * @param publicId The notation's public identifier, or null.
   * @param systemId The notation's system identifier, or null.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.DTDHandler#notationDecl
   */
  public void notationDecl(String name, String publicId, String systemId)
          throws SAXException
  {

    if (m_dtdHandler != null)
    {
      m_dtdHandler.notationDecl(name, publicId, systemId);
    }
  }

  /**
   * Filter an unparsed entity declaration event.
   *
   * @param name The entity name.
   * @param publicId The entity's public identifier, or null.
   * @param systemId The entity's system identifier, or null.
   * @param notationName The name of the associated notation.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   */
  public void unparsedEntityDecl(
          String name, String publicId, String systemId, String notationName)
            throws SAXException
  {

    if (m_dtdHandler != null)
    {
      m_dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of org.xml.sax.ContentHandler.
  ////////////////////////////////////////////////////////////////////

  /**
   * Filter a new document locator event.
   *
   * @param locator The document locator.
   * @see org.xml.sax.ContentHandler#setDocumentLocator
   */
  public void setDocumentLocator(Locator locator)
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#setDocumentLocator: "
                         + locator.getSystemId());

    this.m_locator = locator;
    
    if(null == m_baseSystemID)
    {
      setSystemId(locator.getSystemId());
    }

    if (m_contentHandler != null)
    {
      m_contentHandler.setDocumentLocator(locator);
    }
  }

  /**
   * Filter a start document event.
   *
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#startDocument
   */
  public void startDocument() throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startDocument");
      
    m_insideParse = true;

   // Thread listener = new Thread(m_transformer);

    if (m_contentHandler != null)
    {
      //m_transformer.setTransformThread(listener);
      if(m_incremental)
      {
        m_transformer.setSourceTreeDocForThread(m_dtm.getDocument());
            
        int cpriority = Thread.currentThread().getPriority();
    
        // runTransformThread is equivalent with the 2.0.1 code,
        // except that the Thread may come from a pool.
        m_transformer.runTransformThread( cpriority );
      }

      // This is now done _last_, because IncrementalSAXSource_Filter
      // will immediately go into a "wait until events are requested"
      // pause. I believe that will close our timing window.
      // %REVIEW%
      m_contentHandler.startDocument();
   }
        
   //listener.setDaemon(false);
   //listener.start();

  }

  /**
   * Filter an end document event.
   *
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#endDocument
   */
  public void endDocument() throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endDocument");

    m_insideParse = false;
    
    if (m_contentHandler != null)
    {
      m_contentHandler.endDocument();
    }
    
    if(m_incremental)
    {
      m_transformer.waitTransformThread();
    }
    else
    {
      m_transformer.setSourceTreeDocForThread(m_dtm.getDocument());
      m_transformer.run();
    }
   /* Thread transformThread = m_transformer.getTransformThread();

    if (null != transformThread)
    {
      try
      {

        // This should wait until the transformThread is considered not alive.
        transformThread.join();

        if (!m_transformer.hasTransformThreadErrorCatcher())
        {
          Exception e = m_transformer.getExceptionThrown();

          if (null != e)
            throw new org.xml.sax.SAXException(e);
        }

        m_transformer.setTransformThread(null);
      }
      catch (InterruptedException ie){}
    }*/
  }

  /**
   * Filter a start Namespace prefix mapping event.
   *
   * @param prefix The Namespace prefix.
   * @param uri The Namespace URI.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#startPrefixMapping
   */
  public void startPrefixMapping(String prefix, String uri)
          throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startPrefixMapping: "
                         + prefix + ", " + uri);

    if (m_contentHandler != null)
    {
      m_contentHandler.startPrefixMapping(prefix, uri);
    }
  }

  /**
   * Filter an end Namespace prefix mapping event.
   *
   * @param prefix The Namespace prefix.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#endPrefixMapping
   */
  public void endPrefixMapping(String prefix) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endPrefixMapping: "
                         + prefix);

    if (m_contentHandler != null)
    {
      m_contentHandler.endPrefixMapping(prefix);
    }
  }

  /**
   * Filter a start element event.
   *
   * @param uri The element's Namespace URI, or the empty string.
   * @param localName The element's local name, or the empty string.
   * @param qName The element's qualified (prefixed) name, or the empty
   *        string.
   * @param atts The element's attributes.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#startElement
   */
  public void startElement(
          String uri, String localName, String qName, Attributes atts)
            throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startElement: " + qName);

    if (m_contentHandler != null)
    {
      m_contentHandler.startElement(uri, localName, qName, atts);
    }
  }

  /**
   * Filter an end element event.
   *
   * @param uri The element's Namespace URI, or the empty string.
   * @param localName The element's local name, or the empty string.
   * @param qName The element's qualified (prefixed) name, or the empty
   *        string.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#endElement
   */
  public void endElement(String uri, String localName, String qName)
          throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endElement: " + qName);

    if (m_contentHandler != null)
    {
      m_contentHandler.endElement(uri, localName, qName);
    }
  }

  /**
   * Filter a character data event.
   *
   * @param ch An array of characters.
   * @param start The starting position in the array.
   * @param length The number of characters to use from the array.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#characters
   */
  public void characters(char ch[], int start, int length) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#characters: " + start + ", "
                         + length);

    if (m_contentHandler != null)
    {
      m_contentHandler.characters(ch, start, length);
    }
  }

  /**
   * Filter an ignorable whitespace event.
   *
   * @param ch An array of characters.
   * @param start The starting position in the array.
   * @param length The number of characters to use from the array.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#ignorableWhitespace
   */
  public void ignorableWhitespace(char ch[], int start, int length)
          throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#ignorableWhitespace: "
                         + start + ", " + length);

    if (m_contentHandler != null)
    {
      m_contentHandler.ignorableWhitespace(ch, start, length);
    }
  }

  /**
   * Filter a processing instruction event.
   *
   * @param target The processing instruction target.
   * @param data The text following the target.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#processingInstruction
   */
  public void processingInstruction(String target, String data)
          throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#processingInstruction: "
                         + target + ", " + data);

    if (m_contentHandler != null)
    {
      m_contentHandler.processingInstruction(target, data);
    }
  }

  /**
   * Filter a skipped entity event.
   *
   * @param name The name of the skipped entity.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ContentHandler#skippedEntity
   */
  public void skippedEntity(String name) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#skippedEntity: " + name);

    if (m_contentHandler != null)
    {
      m_contentHandler.skippedEntity(name);
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of org.xml.sax.ErrorHandler.
  ////////////////////////////////////////////////////////////////////

  /**
   * Filter a warning event.
   *
   * @param e The nwarning as an exception.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ErrorHandler#warning
   */
  public void warning(SAXParseException e) throws SAXException
  {
    // This is not great, but we really would rather have the error 
    // handler be the error listener if it is a error handler.  Coroutine's fatalError 
    // can't really be configured, so I think this is the best thing right now 
    // for error reporting.  Possibly another JAXP 1.1 hole.  -sb
    javax.xml.transform.ErrorListener errorListener = m_transformer.getErrorListener();
    if(errorListener instanceof ErrorHandler)
    {
      ((ErrorHandler)errorListener).warning(e);
    }
    else
    {
      try
      {
        errorListener.warning(new javax.xml.transform.TransformerException(e));
      }
      catch(javax.xml.transform.TransformerException te)
      {
        throw e;
      }
    }
  }

  /**
   * Filter an error event.
   *
   * @param e The error as an exception.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ErrorHandler#error
   */
  public void error(SAXParseException e) throws SAXException
  {
    // %REVIEW% I don't think this should be called.  -sb
    // clearCoRoutine(e);

    // This is not great, but we really would rather have the error 
    // handler be the error listener if it is a error handler.  Coroutine's fatalError 
    // can't really be configured, so I think this is the best thing right now 
    // for error reporting.  Possibly another JAXP 1.1 hole.  -sb
    javax.xml.transform.ErrorListener errorListener = m_transformer.getErrorListener();
    if(errorListener instanceof ErrorHandler)
    {
      ((ErrorHandler)errorListener).error(e);
      if(null != m_errorHandler)
        m_errorHandler.error(e); // may not be called.
    }
    else
    {
      try
      {
        errorListener.error(new javax.xml.transform.TransformerException(e));
        if(null != m_errorHandler)
          m_errorHandler.error(e); // may not be called.
      }
      catch(javax.xml.transform.TransformerException te)
      {
        throw e;
      }
    }
  }

  /**
   * Filter a fatal error event.
   *
   * @param e The error as an exception.
   * @throws SAXException The client may throw
   *            an exception during processing.
   * @see org.xml.sax.ErrorHandler#fatalError
   */
  public void fatalError(SAXParseException e) throws SAXException
  {
    if(null != m_errorHandler)
    {
      try
      {
        m_errorHandler.fatalError(e);
      }
      catch(SAXParseException se)
      {
        // ignore
      }
      // clearCoRoutine(e);
    }

    // This is not great, but we really would rather have the error 
    // handler be the error listener if it is a error handler.  Coroutine's fatalError 
    // can't really be configured, so I think this is the best thing right now 
    // for error reporting.  Possibly another JAXP 1.1 hole.  -sb
    javax.xml.transform.ErrorListener errorListener = m_transformer.getErrorListener();
    
    if(errorListener instanceof ErrorHandler)
    {
      ((ErrorHandler)errorListener).fatalError(e);
      if(null != m_errorHandler)
        m_errorHandler.fatalError(e); // may not be called.
    }
    else
    {
      try
      {
        errorListener.fatalError(new javax.xml.transform.TransformerException(e));
        if(null != m_errorHandler)
          m_errorHandler.fatalError(e); // may not be called.
      }
      catch(javax.xml.transform.TransformerException te)
      {
        throw e;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of org.xml.sax.ext.LexicalHandler.
  ////////////////////////////////////////////////////////////////////

  /**
   * Report the start of DTD declarations, if any.
   *
   * <p>Any declarations are assumed to be in the internal subset
   * unless otherwise indicated by a {@link #startEntity startEntity}
   * event.</p>
   *
   * <p>Note that the start/endDTD events will appear within
   * the start/endDocument events from ContentHandler and
   * before the first startElement event.</p>
   *
   * @param name The document type name.
   * @param publicId The declared public identifier for the
   *        external DTD subset, or null if none was declared.
   * @param systemId The declared system identifier for the
   *        external DTD subset, or null if none was declared.
   * @throws SAXException The application may raise an
   *            exception.
   * @see #endDTD
   * @see #startEntity
   */
  public void startDTD(String name, String publicId, String systemId)
          throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startDTD: " + name + ", "
                         + publicId + ", " + systemId);

    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.startDTD(name, publicId, systemId);
    }
  }

  /**
   * Report the end of DTD declarations.
   *
   * @throws SAXException The application may raise an exception.
   * @see #startDTD
   */
  public void endDTD() throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endDTD");

    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.endDTD();
    }
  }

  /**
   * Report the beginning of an entity in content.
   *
   * <p><strong>NOTE:</entity> entity references in attribute
   * values -- and the start and end of the document entity --
   * are never reported.</p>
   *
   * <p>The start and end of the external DTD subset are reported
   * using the pseudo-name "[dtd]".  All other events must be
   * properly nested within start/end entity events.</p>
   *
   * <p>Note that skipped entities will be reported through the
   * {@link org.xml.sax.ContentHandler#skippedEntity skippedEntity}
   * event, which is part of the ContentHandler interface.</p>
   *
   * @param name The name of the entity.  If it is a parameter
   *        entity, the name will begin with '%'.
   * @throws SAXException The application may raise an exception.
   * @see #endEntity
   * @see org.xml.sax.ext.DeclHandler#internalEntityDecl
   * @see org.xml.sax.ext.DeclHandler#externalEntityDecl
   */
  public void startEntity(String name) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startEntity: " + name);

    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.startEntity(name);
    }
  }

  /**
   * Report the end of an entity.
   *
   * @param name The name of the entity that is ending.
   * @throws SAXException The application may raise an exception.
   * @see #startEntity
   */
  public void endEntity(String name) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endEntity: " + name);

    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.endEntity(name);
    }
  }

  /**
   * Report the start of a CDATA section.
   *
   * <p>The contents of the CDATA section will be reported through
   * the regular {@link org.xml.sax.ContentHandler#characters
   * characters} event.</p>
   *
   * @throws SAXException The application may raise an exception.
   * @see #endCDATA
   */
  public void startCDATA() throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#startCDATA");

    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.startCDATA();
    }
  }

  /**
   * Report the end of a CDATA section.
   *
   * @throws SAXException The application may raise an exception.
   * @see #startCDATA
   */
  public void endCDATA() throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#endCDATA");

    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.endCDATA();
    }
  }

  /**
   * Report an XML comment anywhere in the document.
   *
   * <p>This callback will be used for comments inside or outside the
   * document element, including comments in the external DTD
   * subset (if read).</p>
   *
   * @param ch An array holding the characters in the comment.
   * @param start The starting position in the array.
   * @param length The number of characters to use from the array.
   * @throws SAXException The application may raise an exception.
   */
  public void comment(char ch[], int start, int length) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#comment: " + start + ", "
                         + length);

    if (null != m_lexicalHandler)
    {
      m_lexicalHandler.comment(ch, start, length);
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Implementation of org.xml.sax.ext.DeclHandler.
  ////////////////////////////////////////////////////////////////////

  /**
   * Report an element type declaration.
   *
   * <p>The content model will consist of the string "EMPTY", the
   * string "ANY", or a parenthesised group, optionally followed
   * by an occurrence indicator.  The model will be normalized so
   * that all whitespace is removed,and will include the enclosing
   * parentheses.</p>
   *
   * @param name The element type name.
   * @param model The content model as a normalized string.
   * @throws SAXException The application may raise an exception.
   */
  public void elementDecl(String name, String model) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#elementDecl: " + name + ", "
                         + model);

    if (null != m_declHandler)
    {
      m_declHandler.elementDecl(name, model);
    }
  }

  /**
   * Report an attribute type declaration.
   *
   * <p>Only the effective (first) declaration for an attribute will
   * be reported.  The type will be one of the strings "CDATA",
   * "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY",
   * "ENTITIES", or "NOTATION", or a parenthesized token group with
   * the separator "|" and all whitespace removed.</p>
   *
   * @param eName The name of the associated element.
   * @param aName The name of the attribute.
   * @param type A string representing the attribute type.
   * @param valueDefault A string representing the attribute default
   *        ("#IMPLIED", "#REQUIRED", or "#FIXED") or null if
   *        none of these applies.
   * @param value A string representing the attribute's default value,
   *        or null if there is none.
   * @throws SAXException The application may raise an exception.
   */
  public void attributeDecl(
          String eName, String aName, String type, String valueDefault, String value)
            throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#attributeDecl: " + eName
                         + ", " + aName + ", etc...");

    if (null != m_declHandler)
    {
      m_declHandler.attributeDecl(eName, aName, type, valueDefault, value);
    }
  }

  /**
   * Report an internal entity declaration.
   *
   * <p>Only the effective (first) declaration for each entity
   * will be reported.</p>
   *
   * @param name The name of the entity.  If it is a parameter
   *        entity, the name will begin with '%'.
   * @param value The replacement text of the entity.
   * @throws SAXException The application may raise an exception.
   * @see #externalEntityDecl
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   */
  public void internalEntityDecl(String name, String value)
          throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#internalEntityDecl: " + name
                         + ", " + value);

    if (null != m_declHandler)
    {
      m_declHandler.internalEntityDecl(name, value);
    }
  }

  /**
   * Report a parsed external entity declaration.
   *
   * <p>Only the effective (first) declaration for each entity
   * will be reported.</p>
   *
   * @param name The name of the entity.  If it is a parameter
   *        entity, the name will begin with '%'.
   * @param publicId The declared public identifier of the entity, or
   *        null if none was declared.
   * @param systemId The declared system identifier of the entity.
   * @throws SAXException The application may raise an exception.
   * @see #internalEntityDecl
   * @see org.xml.sax.DTDHandler#unparsedEntityDecl
   */
  public void externalEntityDecl(
          String name, String publicId, String systemId) throws SAXException
  {

    if (DEBUG)
      System.out.println("TransformerHandlerImpl#externalEntityDecl: " + name
                         + ", " + publicId + ", " + systemId);

    if (null != m_declHandler)
    {
      m_declHandler.externalEntityDecl(name, publicId, systemId);
    }
  }

  ////////////////////////////////////////////////////////////////////
  // Internal state.
  ////////////////////////////////////////////////////////////////////

  /** Set to true for diagnostics output.         */
  private static boolean DEBUG = false;

  /**
   * The transformer this will use to transform a
   * source tree into a result tree.
   */
  private TransformerImpl m_transformer;

  /** The system ID to use as a base for relative URLs. */
  private String m_baseSystemID;

  /** The result for the transformation. */
  private Result m_result = null;

  /** The locator for this TransformerHandler. */
  private Locator m_locator = null;

  /** The entity resolver to aggregate to. */
  private EntityResolver m_entityResolver = null;

  /** The DTD handler to aggregate to. */
  private DTDHandler m_dtdHandler = null;

  /** The content handler to aggregate to. */
  private ContentHandler m_contentHandler = null;

  /** The error handler to aggregate to. */
  private ErrorHandler m_errorHandler = null;

  /** The lexical handler to aggregate to. */
  private LexicalHandler m_lexicalHandler = null;

  /** The decl handler to aggregate to. */
  private DeclHandler m_declHandler = null;
  
  /** The Document Table Instance we are transforming. */
  DTM m_dtm;
}
