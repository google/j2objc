// ParserAdapter.java - adapt a SAX1 Parser to a SAX2 XMLReader.
// http://www.saxproject.org
// Written by David Megginson
// NO WARRANTY!  This class is in the public domain.
// $Id: ParserAdapter.java,v 1.16 2004/04/26 17:34:35 dmegginson Exp $

package org.xml.sax.helpers;

import org.xml.sax.AttributeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import android.compat.annotation.UnsupportedAppUsage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;


/**
 * Adapt a SAX1 Parser as a SAX2 XMLReader.
 *
 * <blockquote>
 * <em>This module, both source code and documentation, is in the
 * Public Domain, and comes with <strong>NO WARRANTY</strong>.</em>
 * See <a href='http://www.saxproject.org'>http://www.saxproject.org</a>
 * for further information.
 * </blockquote>
 *
 * <p>This class wraps a SAX1 {@link org.xml.sax.Parser Parser}
 * and makes it act as a SAX2 {@link org.xml.sax.XMLReader XMLReader},
 * with feature, property, and Namespace support.  Note
 * that it is not possible to report {@link org.xml.sax.ContentHandler#skippedEntity
 * skippedEntity} events, since SAX1 does not make that information available.</p>
 *
 * <p>This adapter does not test for duplicate Namespace-qualified
 * attribute names.</p>
 *
 * @since SAX 2.0
 * @author David Megginson
 * @version 2.0.1 (sax2r2)
 * @see org.xml.sax.helpers.XMLReaderAdapter
 * @see org.xml.sax.XMLReader
 * @see org.xml.sax.Parser
 */
public class ParserAdapter implements XMLReader, DocumentHandler
{


    ////////////////////////////////////////////////////////////////////
    // Constructors.
    ////////////////////////////////////////////////////////////////////


    /**
     * Construct a new parser adapter.
     *
     * <p>Use the "org.xml.sax.parser" property to locate the
     * embedded SAX1 driver.</p>
     *
     * @exception SAXException If the embedded driver
     *            cannot be instantiated or if the
     *            org.xml.sax.parser property is not specified.
     */
    public ParserAdapter ()
      throws SAXException
    {

    String driver = System.getProperty("org.xml.sax.parser");

    try {
        setup(ParserFactory.makeParser());
    } catch (ClassNotFoundException e1) {
        throw new
        SAXException("Cannot find SAX1 driver class " +
                 driver, e1);
    } catch (IllegalAccessException e2) {
        throw new
        SAXException("SAX1 driver class " +
                 driver +
                 " found but cannot be loaded", e2);
    } catch (InstantiationException e3) {
        throw new
        SAXException("SAX1 driver class " +
                 driver +
                 " loaded but cannot be instantiated", e3);
    } catch (ClassCastException e4) {
        throw new
        SAXException("SAX1 driver class " +
                 driver +
                 " does not implement org.xml.sax.Parser");
    } catch (NullPointerException e5) {
        throw new
        SAXException("System property org.xml.sax.parser not specified");
    }
    }


    /**
     * Construct a new parser adapter.
     *
     * <p>Note that the embedded parser cannot be changed once the
     * adapter is created; to embed a different parser, allocate
     * a new ParserAdapter.</p>
     *
     * @param parser The SAX1 parser to embed.
     * @exception java.lang.NullPointerException If the parser parameter
     *            is null.
     */
    public ParserAdapter (Parser parser)
    {
    setup(parser);
    }


    /**
     * Internal setup method.
     *
     * @param parser The embedded parser.
     * @exception java.lang.NullPointerException If the parser parameter
     *            is null.
     */
    @UnsupportedAppUsage
    private void setup (Parser parser)
    {
    if (parser == null) {
        throw new
        NullPointerException("Parser argument must not be null");
    }
    this.parser = parser;
    atts = new AttributesImpl();
    nsSupport = new NamespaceSupport();
    attAdapter = new AttributeListAdapter();
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.XMLReader.
    ////////////////////////////////////////////////////////////////////


    //
    // Internal constants for the sake of convenience.
    //
    private static final String FEATURES = "http://xml.org/sax/features/";
    private static final String NAMESPACES = FEATURES + "namespaces";
    private static final String NAMESPACE_PREFIXES = FEATURES + "namespace-prefixes";
    private static final String XMLNS_URIs = FEATURES + "xmlns-uris";


    /**
     * Set a feature flag for the parser.
     *
     * <p>The only features recognized are namespaces and
     * namespace-prefixes.</p>
     *
     * @param name The feature name, as a complete URI.
     * @param value The requested feature value.
     * @exception SAXNotRecognizedException If the feature
     *            can't be assigned or retrieved.
     * @exception SAXNotSupportedException If the feature
     *            can't be assigned that value.
     * @see org.xml.sax.XMLReader#setFeature
     */
    public void setFeature (String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    if (name.equals(NAMESPACES)) {
        checkNotParsing("feature", name);
        namespaces = value;
        if (!namespaces && !prefixes) {
        prefixes = true;
        }
    } else if (name.equals(NAMESPACE_PREFIXES)) {
        checkNotParsing("feature", name);
        prefixes = value;
        if (!prefixes && !namespaces) {
        namespaces = true;
        }
    } else if (name.equals(XMLNS_URIs)) {
        checkNotParsing("feature", name);
        uris = value;
    } else {
        throw new SAXNotRecognizedException("Feature: " + name);
    }
    }


    /**
     * Check a parser feature flag.
     *
     * <p>The only features recognized are namespaces and
     * namespace-prefixes.</p>
     *
     * @param name The feature name, as a complete URI.
     * @return The current feature value.
     * @exception SAXNotRecognizedException If the feature
     *            value can't be assigned or retrieved.
     * @exception SAXNotSupportedException If the
     *            feature is not currently readable.
     * @see org.xml.sax.XMLReader#setFeature
     */
    public boolean getFeature (String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    if (name.equals(NAMESPACES)) {
        return namespaces;
    } else if (name.equals(NAMESPACE_PREFIXES)) {
        return prefixes;
    } else if (name.equals(XMLNS_URIs)) {
        return uris;
    } else {
        throw new SAXNotRecognizedException("Feature: " + name);
    }
    }


    /**
     * Set a parser property.
     *
     * <p>No properties are currently recognized.</p>
     *
     * @param name The property name.
     * @param value The property value.
     * @exception SAXNotRecognizedException If the property
     *            value can't be assigned or retrieved.
     * @exception SAXNotSupportedException If the property
     *            can't be assigned that value.
     * @see org.xml.sax.XMLReader#setProperty
     */
    public void setProperty (String name, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    throw new SAXNotRecognizedException("Property: " + name);
    }


    /**
     * Get a parser property.
     *
     * <p>No properties are currently recognized.</p>
     *
     * @param name The property name.
     * @return The property value.
     * @exception SAXNotRecognizedException If the property
     *            value can't be assigned or retrieved.
     * @exception SAXNotSupportedException If the property
     *            value is not currently readable.
     * @see org.xml.sax.XMLReader#getProperty
     */
    public Object getProperty (String name)
    throws SAXNotRecognizedException, SAXNotSupportedException
    {
    throw new SAXNotRecognizedException("Property: " + name);
    }


    /**
     * Set the entity resolver.
     *
     * @param resolver The new entity resolver.
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setEntityResolver (EntityResolver resolver)
    {
    entityResolver = resolver;
    }


    /**
     * Return the current entity resolver.
     *
     * @return The current entity resolver, or null if none was supplied.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public EntityResolver getEntityResolver ()
    {
    return entityResolver;
    }


    /**
     * Set the DTD handler.
     *
     * @param handler the new DTD handler
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setDTDHandler (DTDHandler handler)
    {
    dtdHandler = handler;
    }


    /**
     * Return the current DTD handler.
     *
     * @return the current DTD handler, or null if none was supplied
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public DTDHandler getDTDHandler ()
    {
    return dtdHandler;
    }


    /**
     * Set the content handler.
     *
     * @param handler the new content handler
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setContentHandler (ContentHandler handler)
    {
    contentHandler = handler;
    }


    /**
     * Return the current content handler.
     *
     * @return The current content handler, or null if none was supplied.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public ContentHandler getContentHandler ()
    {
    return contentHandler;
    }


    /**
     * Set the error handler.
     *
     * @param handler The new error handler.
     * @see org.xml.sax.XMLReader#setEntityResolver
     */
    public void setErrorHandler (ErrorHandler handler)
    {
    errorHandler = handler;
    }


    /**
     * Return the current error handler.
     *
     * @return The current error handler, or null if none was supplied.
     * @see org.xml.sax.XMLReader#getEntityResolver
     */
    public ErrorHandler getErrorHandler ()
    {
    return errorHandler;
    }


    /**
     * Parse an XML document.
     *
     * @param systemId The absolute URL of the document.
     * @exception java.io.IOException If there is a problem reading
     *            the raw content of the document.
     * @exception SAXException If there is a problem
     *            processing the document.
     * @see #parse(org.xml.sax.InputSource)
     * @see org.xml.sax.Parser#parse(java.lang.String)
     */
    public void parse (String systemId)
    throws IOException, SAXException
    {
    parse(new InputSource(systemId));
    }


    /**
     * Parse an XML document.
     *
     * @param input An input source for the document.
     * @exception java.io.IOException If there is a problem reading
     *            the raw content of the document.
     * @exception SAXException If there is a problem
     *            processing the document.
     * @see #parse(java.lang.String)
     * @see org.xml.sax.Parser#parse(org.xml.sax.InputSource)
     */
    public void parse (InputSource input)
    throws IOException, SAXException
    {
    if (parsing) {
        throw new SAXException("Parser is already in use");
    }
    setupParser();
    parsing = true;
    try {
        parser.parse(input);
    } finally {
        parsing = false;
    }
    parsing = false;
    }



    ////////////////////////////////////////////////////////////////////
    // Implementation of org.xml.sax.DocumentHandler.
    ////////////////////////////////////////////////////////////////////


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 document locator event.
     *
     * @param locator A document locator.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     */
    public void setDocumentLocator (Locator locator)
    {
    this.locator = locator;
    if (contentHandler != null) {
        contentHandler.setDocumentLocator(locator);
    }
    }


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 start document event.
     *
     * @exception SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.DocumentHandler#startDocument
     */
    public void startDocument ()
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.startDocument();
    }
    }


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 end document event.
     *
     * @exception SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.DocumentHandler#endDocument
     */
    public void endDocument ()
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.endDocument();
    }
    }


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 startElement event.
     *
     * <p>If necessary, perform Namespace processing.</p>
     *
     * @param qName The qualified (prefixed) name.
     * @param qAtts The XML attribute list (with qnames).
     * @exception SAXException The client may raise a
     *            processing exception.
     */
    public void startElement (String qName, AttributeList qAtts) throws SAXException {
        // These are exceptions from the
        // first pass; they should be
        // ignored if there's a second pass,
        // but reported otherwise.
        ArrayList<SAXParseException> exceptions = null;

                // If we're not doing Namespace
                // processing, dispatch this quickly.
    if (!namespaces) {
        if (contentHandler != null) {
        attAdapter.setAttributeList(qAtts);
        contentHandler.startElement("", "", qName.intern(),
                        attAdapter);
        }
        return;
    }


                // OK, we're doing Namespace processing.
    nsSupport.pushContext();
    int length = qAtts.getLength();

                // First pass:  handle NS decls
    for (int i = 0; i < length; i++) {
        String attQName = qAtts.getName(i);

        if (!attQName.startsWith("xmlns"))
        continue;
                // Could be a declaration...
        String prefix;
        int n = attQName.indexOf(':');

                    // xmlns=...
        if (n == -1 && attQName.length () == 5) {
        prefix = "";
        } else if (n != 5) {
        // XML namespaces spec doesn't discuss "xmlnsf:oo"
        // (and similarly named) attributes ... at most, warn
        continue;
        } else         // xmlns:foo=...
        prefix = attQName.substring(n+1);

        String value = qAtts.getValue(i);
        if (!nsSupport.declarePrefix(prefix, value)) {
        reportError("Illegal Namespace prefix: " + prefix);
        continue;
        }
        if (contentHandler != null)
        contentHandler.startPrefixMapping(prefix, value);
    }

                // Second pass: copy all relevant
                // attributes into the SAX2 AttributeList
                // using updated prefix bindings
    atts.clear();
    for (int i = 0; i < length; i++) {
        String attQName = qAtts.getName(i);
        String type = qAtts.getType(i);
        String value = qAtts.getValue(i);

                // Declaration?
        if (attQName.startsWith("xmlns")) {
        String prefix;
        int n = attQName.indexOf(':');

        if (n == -1 && attQName.length () == 5) {
            prefix = "";
        } else if (n != 5) {
            // XML namespaces spec doesn't discuss "xmlnsf:oo"
            // (and similarly named) attributes ... ignore
            prefix = null;
        } else {
            prefix = attQName.substring(6);
        }
                // Yes, decl:  report or prune
        if (prefix != null) {
            if (prefixes) {
            if (uris)
                // note funky case:  localname can be null
                // when declaring the default prefix, and
                // yet the uri isn't null.
                atts.addAttribute (nsSupport.XMLNS, prefix,
                    attQName.intern(), type, value);
            else
                atts.addAttribute ("", "",
                    attQName.intern(), type, value);
            }
            continue;
        }
        }

                // Not a declaration -- report
        try {
        String attName[] = processName(attQName, true, true);
        atts.addAttribute(attName[0], attName[1], attName[2],
                  type, value);
        } catch (SAXException e) {
            if (exceptions == null) {
                exceptions = new ArrayList<SAXParseException>();
            }
            exceptions.add((SAXParseException) e);
            atts.addAttribute("", attQName, attQName, type, value);
        }
    }

    // now handle the deferred exception reports
    if (exceptions != null && errorHandler != null) {
        for (SAXParseException ex : exceptions) {
            errorHandler.error(ex);
        }
    }

                // OK, finally report the event.
    if (contentHandler != null) {
        String name[] = processName(qName, false, false);
        contentHandler.startElement(name[0], name[1], name[2], atts);
    }
    }


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 end element event.
     *
     * @param qName The qualified (prefixed) name.
     * @exception SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.DocumentHandler#endElement
     */
    public void endElement (String qName)
    throws SAXException
    {
                // If we're not doing Namespace
                // processing, dispatch this quickly.
    if (!namespaces) {
        if (contentHandler != null) {
        contentHandler.endElement("", "", qName.intern());
        }
        return;
    }

                // Split the name.
    String names[] = processName(qName, false, false);
    if (contentHandler != null) {
        contentHandler.endElement(names[0], names[1], names[2]);
        Enumeration prefixes = nsSupport.getDeclaredPrefixes();
        while (prefixes.hasMoreElements()) {
        String prefix = (String)prefixes.nextElement();
        contentHandler.endPrefixMapping(prefix);
        }
    }
    nsSupport.popContext();
    }


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 characters event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use.
     * @exception SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.DocumentHandler#characters
     */
    public void characters (char ch[], int start, int length)
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.characters(ch, start, length);
    }
    }


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 ignorable whitespace event.
     *
     * @param ch An array of characters.
     * @param start The starting position in the array.
     * @param length The number of characters to use.
     * @exception SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.DocumentHandler#ignorableWhitespace
     */
    public void ignorableWhitespace (char ch[], int start, int length)
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.ignorableWhitespace(ch, start, length);
    }
    }


    /**
     * Adapter implementation method; do not call.
     * Adapt a SAX1 processing instruction event.
     *
     * @param target The processing instruction target.
     * @param data The remainder of the processing instruction
     * @exception SAXException The client may raise a
     *            processing exception.
     * @see org.xml.sax.DocumentHandler#processingInstruction
     */
    public void processingInstruction (String target, String data)
    throws SAXException
    {
    if (contentHandler != null) {
        contentHandler.processingInstruction(target, data);
    }
    }



    ////////////////////////////////////////////////////////////////////
    // Internal utility methods.
    ////////////////////////////////////////////////////////////////////


    /**
     * Initialize the parser before each run.
     */
    @UnsupportedAppUsage
    private void setupParser ()
    {
    // catch an illegal "nonsense" state.
    if (!prefixes && !namespaces)
        throw new IllegalStateException ();

    nsSupport.reset();
    if (uris)
        nsSupport.setNamespaceDeclUris (true);

    if (entityResolver != null) {
        parser.setEntityResolver(entityResolver);
    }
    if (dtdHandler != null) {
        parser.setDTDHandler(dtdHandler);
    }
    if (errorHandler != null) {
        parser.setErrorHandler(errorHandler);
    }
    parser.setDocumentHandler(this);
    locator = null;
    }


    /**
     * Process a qualified (prefixed) name.
     *
     * <p>If the name has an undeclared prefix, use only the qname
     * and make an ErrorHandler.error callback in case the app is
     * interested.</p>
     *
     * @param qName The qualified (prefixed) name.
     * @param isAttribute true if this is an attribute name.
     * @return The name split into three parts.
     * @exception SAXException The client may throw
     *            an exception if there is an error callback.
     */
    @UnsupportedAppUsage
    private String [] processName (String qName, boolean isAttribute,
                   boolean useException)
    throws SAXException
    {
    String parts[] = nsSupport.processName(qName, nameParts,
                           isAttribute);
    if (parts == null) {
        if (useException)
        throw makeException("Undeclared prefix: " + qName);
        reportError("Undeclared prefix: " + qName);
        parts = new String[3];
        parts[0] = parts[1] = "";
        parts[2] = qName.intern();
    }
    return parts;
    }


    /**
     * Report a non-fatal error.
     *
     * @param message The error message.
     * @exception SAXException The client may throw
     *            an exception.
     */
    @UnsupportedAppUsage
    void reportError (String message)
    throws SAXException
    {
    if (errorHandler != null)
        errorHandler.error(makeException(message));
    }


    /**
     * Construct an exception for the current context.
     *
     * @param message The error message.
     */
    @UnsupportedAppUsage
    private SAXParseException makeException (String message)
    {
    if (locator != null) {
        return new SAXParseException(message, locator);
    } else {
        return new SAXParseException(message, null, null, -1, -1);
    }
    }


    /**
     * Throw an exception if we are parsing.
     *
     * <p>Use this method to detect illegal feature or
     * property changes.</p>
     *
     * @param type The type of thing (feature or property).
     * @param name The feature or property name.
     * @exception SAXNotSupportedException If a
     *            document is currently being parsed.
     */
    @UnsupportedAppUsage
    private void checkNotParsing (String type, String name)
    throws SAXNotSupportedException
    {
    if (parsing) {
        throw new SAXNotSupportedException("Cannot change " +
                           type + ' ' +
                           name + " while parsing");

    }
    }



    ////////////////////////////////////////////////////////////////////
    // Internal state.
    ////////////////////////////////////////////////////////////////////

    @UnsupportedAppUsage
    private NamespaceSupport nsSupport;
    @UnsupportedAppUsage
    private AttributeListAdapter attAdapter;

    @UnsupportedAppUsage
    private boolean parsing = false;
    @UnsupportedAppUsage
    private String nameParts[] = new String[3];

    @UnsupportedAppUsage
    private Parser parser = null;

    @UnsupportedAppUsage
    private AttributesImpl atts = null;

                // Features
    @UnsupportedAppUsage
    private boolean namespaces = true;
    @UnsupportedAppUsage
    private boolean prefixes = false;
    @UnsupportedAppUsage
    private boolean uris = false;

                // Properties

                // Handlers
    @UnsupportedAppUsage
    Locator locator;

    @UnsupportedAppUsage
    EntityResolver entityResolver = null;
    @UnsupportedAppUsage
    DTDHandler dtdHandler = null;
    @UnsupportedAppUsage
    ContentHandler contentHandler = null;
    @UnsupportedAppUsage
    ErrorHandler errorHandler = null;



    ////////////////////////////////////////////////////////////////////
    // Inner class to wrap an AttributeList when not doing NS proc.
    ////////////////////////////////////////////////////////////////////


    /**
     * Adapt a SAX1 AttributeList as a SAX2 Attributes object.
     *
     * <p>This class is in the Public Domain, and comes with NO
     * WARRANTY of any kind.</p>
     *
     * <p>This wrapper class is used only when Namespace support
     * is disabled -- it provides pretty much a direct mapping
     * from SAX1 to SAX2, except that names and types are
     * interned whenever requested.</p>
     */
    final class AttributeListAdapter implements Attributes
    {

    /**
     * Construct a new adapter.
     */
    @UnsupportedAppUsage
    AttributeListAdapter ()
    {
    }


    /**
     * Set the embedded AttributeList.
     *
     * <p>This method must be invoked before any of the others
     * can be used.</p>
     *
     * @param The SAX1 attribute list (with qnames).
     */
    void setAttributeList (AttributeList qAtts)
    {
        this.qAtts = qAtts;
    }


    /**
     * Return the length of the attribute list.
     *
     * @return The number of attributes in the list.
     * @see org.xml.sax.Attributes#getLength
     */
    public int getLength ()
    {
        return qAtts.getLength();
    }


    /**
     * Return the Namespace URI of the specified attribute.
     *
     * @param The attribute's index.
     * @return Always the empty string.
     * @see org.xml.sax.Attributes#getURI
     */
    public String getURI (int i)
    {
        return "";
    }


    /**
     * Return the local name of the specified attribute.
     *
     * @param The attribute's index.
     * @return Always the empty string.
     * @see org.xml.sax.Attributes#getLocalName
     */
    public String getLocalName (int i)
    {
        return "";
    }


    /**
     * Return the qualified (prefixed) name of the specified attribute.
     *
     * @param The attribute's index.
     * @return The attribute's qualified name, internalized.
     */
    public String getQName (int i)
    {
        return qAtts.getName(i).intern();
    }


    /**
     * Return the type of the specified attribute.
     *
     * @param The attribute's index.
     * @return The attribute's type as an internalized string.
     */
    public String getType (int i)
    {
        return qAtts.getType(i).intern();
    }


    /**
     * Return the value of the specified attribute.
     *
     * @param The attribute's index.
     * @return The attribute's value.
     */
    public String getValue (int i)
    {
        return qAtts.getValue(i);
    }


    /**
     * Look up an attribute index by Namespace name.
     *
     * @param uri The Namespace URI or the empty string.
     * @param localName The local name.
     * @return The attributes index, or -1 if none was found.
     * @see org.xml.sax.Attributes#getIndex(java.lang.String,java.lang.String)
     */
    public int getIndex (String uri, String localName)
    {
        return -1;
    }


    /**
     * Look up an attribute index by qualified (prefixed) name.
     *
     * @param qName The qualified name.
     * @return The attributes index, or -1 if none was found.
     * @see org.xml.sax.Attributes#getIndex(java.lang.String)
     */
    public int getIndex (String qName)
    {
        int max = atts.getLength();
        for (int i = 0; i < max; i++) {
        if (qAtts.getName(i).equals(qName)) {
            return i;
        }
        }
        return -1;
    }


    /**
     * Look up the type of an attribute by Namespace name.
     *
     * @param uri The Namespace URI
     * @param localName The local name.
     * @return The attribute's type as an internalized string.
     */
    public String getType (String uri, String localName)
    {
        return null;
    }


    /**
     * Look up the type of an attribute by qualified (prefixed) name.
     *
     * @param qName The qualified name.
     * @return The attribute's type as an internalized string.
     */
    public String getType (String qName)
    {
        return qAtts.getType(qName).intern();
    }


    /**
     * Look up the value of an attribute by Namespace name.
     *
     * @param uri The Namespace URI
     * @param localName The local name.
     * @return The attribute's value.
     */
    public String getValue (String uri, String localName)
    {
        return null;
    }


    /**
     * Look up the value of an attribute by qualified (prefixed) name.
     *
     * @param qName The qualified name.
     * @return The attribute's value.
     */
    public String getValue (String qName)
    {
        return qAtts.getValue(qName);
    }

    private AttributeList qAtts;
    }
}

// end of ParserAdapter.java
