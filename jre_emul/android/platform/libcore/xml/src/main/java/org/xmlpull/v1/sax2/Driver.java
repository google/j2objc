/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1.sax2;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;

// not J2ME classes -- remove if you want to run in MIDP devices
import java.net.URL;
import java.net.MalformedURLException;


// not J2ME classes
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.xml.sax.Attributes;
import org.xml.sax.DTDHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * SAX2 Driver that pulls events from XmlPullParser
 * and converts them into SAX2 callbacks.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public class Driver implements Locator, XMLReader, Attributes
{

    protected static final String DECLARATION_HANDLER_PROPERTY =
        "http://xml.org/sax/properties/declaration-handler";

    protected static final String LEXICAL_HANDLER_PROPERTY =
        "http://xml.org/sax/properties/lexical-handler";

    protected static final String NAMESPACES_FEATURE =
        "http://xml.org/sax/features/namespaces";

    protected static final String NAMESPACE_PREFIXES_FEATURE =
        "http://xml.org/sax/features/namespace-prefixes";

    protected static final String VALIDATION_FEATURE =
        "http://xml.org/sax/features/validation";

    protected static final String APACHE_SCHEMA_VALIDATION_FEATURE =
        "http://apache.org/xml/features/validation/schema";

    protected static final String APACHE_DYNAMIC_VALIDATION_FEATURE =
        "http://apache.org/xml/features/validation/dynamic";

    protected ContentHandler contentHandler = new DefaultHandler();
    protected ErrorHandler errorHandler = new DefaultHandler();;

    protected String systemId;

    protected XmlPullParser pp;

    //private final static boolean DEBUG = false;

    /**
     */
    public Driver() throws XmlPullParserException {
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        pp = factory.newPullParser();
    }

    public Driver(XmlPullParser pp) throws XmlPullParserException {
        this.pp = pp;
    }

    // -- Attributes interface

    public int getLength() { return pp.getAttributeCount(); }
    public String getURI(int index) { return pp.getAttributeNamespace(index); }
    public String getLocalName(int index) { return pp.getAttributeName(index); }
    public String getQName(int index) {
        final String prefix = pp.getAttributePrefix(index);
        if(prefix != null) {
            return prefix+':'+pp.getAttributeName(index);
        } else {
            return pp.getAttributeName(index);
        }
    }
    public String getType(int index) { return pp.getAttributeType(index); }
    public String getValue(int index) { return pp.getAttributeValue(index); }

    public int getIndex(String uri, String localName) {
        for (int i = 0; i < pp.getAttributeCount(); i++)
        {
            if(pp.getAttributeNamespace(i).equals(uri)
               && pp.getAttributeName(i).equals(localName))
            {
                return i;
            }

        }
        return -1;
    }

    public int getIndex(String qName) {
        for (int i = 0; i < pp.getAttributeCount(); i++)
        {
            if(pp.getAttributeName(i).equals(qName))
            {
                return i;
            }

        }
        return -1;
    }

    public String getType(String uri, String localName) {
        for (int i = 0; i < pp.getAttributeCount(); i++)
        {
            if(pp.getAttributeNamespace(i).equals(uri)
               && pp.getAttributeName(i).equals(localName))
            {
                return pp.getAttributeType(i);
            }

        }
        return null;
    }
    public String getType(String qName) {
        for (int i = 0; i < pp.getAttributeCount(); i++)
        {
            if(pp.getAttributeName(i).equals(qName))
            {
                return pp.getAttributeType(i);
            }

        }
        return null;
    }
    public String getValue(String uri, String localName) {
        return pp.getAttributeValue(uri, localName);
    }
    public String getValue(String qName) {
        return pp.getAttributeValue(null, qName);
    }

    // -- Locator interface

    public String getPublicId() { return null; }
    public String getSystemId() { return systemId; }
    public int getLineNumber() { return pp.getLineNumber(); }
    public int getColumnNumber() { return pp.getColumnNumber(); }

    // --- XMLReader interface

    public boolean getFeature(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if(NAMESPACES_FEATURE.equals(name)) {
            return pp.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
        } else if(NAMESPACE_PREFIXES_FEATURE.equals(name)) {
            return pp.getFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES);
        } else if(VALIDATION_FEATURE.equals(name)) {
            return pp.getFeature(XmlPullParser.FEATURE_VALIDATION);
            //        } else if(APACHE_SCHEMA_VALIDATION_FEATURE.equals(name)) {
            //            return false;  //TODO
            //        } else if(APACHE_DYNAMIC_VALIDATION_FEATURE.equals(name)) {
            //            return false; //TODO
        } else {
            return pp.getFeature(name);
            //throw new SAXNotRecognizedException("unrecognized feature "+name);
        }
    }

    public void setFeature (String name, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        try {
            if(NAMESPACES_FEATURE.equals(name)) {
                pp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, value);
            } else if(NAMESPACE_PREFIXES_FEATURE.equals(name)) {
                if(pp.getFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES) != value) {
                    pp.setFeature(XmlPullParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES, value);
                }
            } else if(VALIDATION_FEATURE.equals(name)) {
                pp.setFeature(XmlPullParser.FEATURE_VALIDATION, value);
                //          } else if(APACHE_SCHEMA_VALIDATION_FEATURE.equals(name)) {
                //              // can ignore as validation must be false ...
                //              //              if(true == value) {
                //              //                  throw new SAXNotSupportedException("schema validation is not supported");
                //              //              }
                //          } else if(APACHE_DYNAMIC_VALIDATION_FEATURE.equals(name)) {
                //              if(true == value) {
                //                  throw new SAXNotSupportedException("dynamic validation is not supported");
                //              }
            } else {
                pp.setFeature(name, value);
                //throw new SAXNotRecognizedException("unrecognized feature "+name);
            }
        } catch(XmlPullParserException ex) {
           // throw new SAXNotSupportedException("problem with setting feature "+name+": "+ex);
        }
    }

    public Object getProperty (String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if(DECLARATION_HANDLER_PROPERTY.equals(name)) {
            return null;
        } else if(LEXICAL_HANDLER_PROPERTY.equals(name)) {
            return null;
        } else {
            return pp.getProperty(name);
            //throw new SAXNotRecognizedException("not recognized get property "+name);
        }
    }

    public void setProperty (String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        //
        if(DECLARATION_HANDLER_PROPERTY.equals(name)) {
            throw new SAXNotSupportedException("not supported setting property "+name);//+" to "+value);
        } else if(LEXICAL_HANDLER_PROPERTY.equals(name)) {
            throw new SAXNotSupportedException("not supported setting property "+name);//+" to "+value);
        } else {
            try {
                pp.setProperty(name, value);
            } catch(XmlPullParserException ex) {
                throw new SAXNotSupportedException("not supported set property "+name+": "+ ex);
            }
            //throw new SAXNotRecognizedException("not recognized set property "+name);
        }
    }

    public void setEntityResolver (EntityResolver resolver) {}

    public EntityResolver getEntityResolver () { return null; }

    public void setDTDHandler (DTDHandler handler) {}

    public DTDHandler getDTDHandler () { return null; }

    public void setContentHandler (ContentHandler handler)
    {
        this.contentHandler = handler;
    }

    public ContentHandler getContentHandler() { return contentHandler; }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public ErrorHandler getErrorHandler() { return errorHandler; }

    public void parse(InputSource source) throws SAXException, IOException
    {

        systemId = source.getSystemId();
        contentHandler.setDocumentLocator(this);

        final Reader reader = source.getCharacterStream();
        try {
            if (reader == null) {
                InputStream stream = source.getByteStream();
                final String encoding = source.getEncoding();

                if (stream == null) {
                    systemId = source.getSystemId();
                    if(systemId == null) {
                        SAXParseException saxException = new SAXParseException(
                            "null source systemId" , this);
                        errorHandler.fatalError(saxException);
                        return;
                    }
                    // NOTE: replace with Connection to run in J2ME environment
                    try {
                        final URL url = new URL(systemId);
                        stream = url.openStream();
                    } catch (MalformedURLException nue) {
                        try {
                            stream = new FileInputStream(systemId);
                        } catch (FileNotFoundException fnfe) {
                            final SAXParseException saxException = new SAXParseException(
                                "could not open file with systemId "+systemId, this, fnfe);
                            errorHandler.fatalError(saxException);
                            return;
                        }
                    }
                }
                pp.setInput(stream, encoding);
            } else {
                pp.setInput(reader);
            }
        } catch (XmlPullParserException ex)  {
            final SAXParseException saxException = new SAXParseException(
                "parsing initialization error: "+ex, this, ex);
            //if(DEBUG) ex.printStackTrace();
            errorHandler.fatalError(saxException);
            return;
        }

        // start parsing - move to first start tag
        try {
            contentHandler.startDocument();
            // get first event
            pp.next();
            // it should be start tag...
            if(pp.getEventType() != XmlPullParser.START_TAG) {
                final SAXParseException saxException = new SAXParseException(
                    "expected start tag not"+pp.getPositionDescription(), this);
                //throw saxException;
                errorHandler.fatalError(saxException);
                return;
            }
        } catch (XmlPullParserException ex)  {
            final SAXParseException saxException = new SAXParseException(
                "parsing initialization error: "+ex, this, ex);
            //ex.printStackTrace();
            errorHandler.fatalError(saxException);
            return;
        }

        // now real parsing can start!

        parseSubTree(pp);

        // and finished ...

        contentHandler.endDocument();
    }

    public void parse(String systemId) throws SAXException, IOException {
        parse(new InputSource(systemId));
    }


    public void parseSubTree(XmlPullParser pp) throws SAXException, IOException {
        this.pp = pp;
        final boolean namespaceAware = pp.getFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES);
        try {
            if(pp.getEventType() != XmlPullParser.START_TAG) {
                throw new SAXException(
                    "start tag must be read before skiping subtree"+pp.getPositionDescription());
            }
            final int[] holderForStartAndLength = new int[2];
            final StringBuilder rawName = new StringBuilder(16);
            String prefix = null;
            String name = null;
            int level = pp.getDepth() - 1;
            int type = XmlPullParser.START_TAG;

            LOOP:
            do {
                switch(type) {
                    case XmlPullParser.START_TAG:
                        if(namespaceAware) {
                            final int depth = pp.getDepth() - 1;
                            final int countPrev =
                                (level > depth) ? pp.getNamespaceCount(depth) : 0;
                            //int countPrev = pp.getNamespaceCount(pp.getDepth() - 1);
                            final int count = pp.getNamespaceCount(depth + 1);
                            for (int i = countPrev; i < count; i++)
                            {
                                contentHandler.startPrefixMapping(
                                    pp.getNamespacePrefix(i),
                                    pp.getNamespaceUri(i)
                                );
                            }
                            name = pp.getName();
                            prefix = pp.getPrefix();
                            if(prefix != null) {
                                rawName.setLength(0);
                                rawName.append(prefix);
                                rawName.append(':');
                                rawName.append(name);
                            }
                            startElement(pp.getNamespace(),
                                         name,
                                         // TODO Fixed this. Was "not equals".
                                         prefix == null ? name : rawName.toString());
                        } else {
                            startElement(pp.getNamespace(),
                                         pp.getName(),
                                         pp.getName());
                        }
                        //++level;

                        break;
                    case XmlPullParser.TEXT:
                        final char[] chars = pp.getTextCharacters(holderForStartAndLength);
                        contentHandler.characters(chars,
                                                  holderForStartAndLength[0], //start
                                                  holderForStartAndLength[1] //len
                                                 );
                        break;
                    case XmlPullParser.END_TAG:
                        //--level;
                        if(namespaceAware) {
                            name = pp.getName();
                            prefix = pp.getPrefix();
                            if(prefix != null) {
                                rawName.setLength(0);
                                rawName.append(prefix);
                                rawName.append(':');
                                rawName.append(name);
                            }
                            contentHandler.endElement(pp.getNamespace(),
                                                      name,
                                                      prefix != null ? name : rawName.toString()
                                                     );
                            // when entering show prefixes for all levels!!!!
                            final int depth = pp.getDepth();
                            final int countPrev =
                                (level > depth) ? pp.getNamespaceCount(pp.getDepth()) : 0;
                            int count = pp.getNamespaceCount(pp.getDepth() - 1);
                            // undeclare them in reverse order
                            for (int i = count - 1; i >= countPrev; i--)
                            {
                                contentHandler.endPrefixMapping(
                                    pp.getNamespacePrefix(i)
                                );
                            }
                        } else {
                            contentHandler.endElement(pp.getNamespace(),
                                                      pp.getName(),
                                                      pp.getName()
                                                     );

                        }
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break LOOP;
                }
                type = pp.next();
            } while(pp.getDepth() > level);
        } catch (XmlPullParserException ex)  {
            final SAXParseException saxException = new SAXParseException("parsing error: "+ex, this, ex);
            ex.printStackTrace();
            errorHandler.fatalError(saxException);
        }
    }

    /**
     * Calls {@link ContentHandler#startElement(String, String, String, Attributes) startElement}
     * on the <code>ContentHandler</code> with <code>this</code> driver object as the
     * {@link Attributes} implementation. In default implementation
     * {@link Attributes} object is valid only during this method call and may not
     * be stored. Sub-classes can overwrite this method to cache attributes.
     */
    protected void startElement(String namespace, String localName, String qName) throws SAXException {
        contentHandler.startElement(namespace, localName, qName, this);
    }

}
