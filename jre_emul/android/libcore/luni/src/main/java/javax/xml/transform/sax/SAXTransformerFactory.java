/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// $Id: SAXTransformerFactory.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.transform.sax;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import org.xml.sax.XMLFilter;

/**
 * This class extends TransformerFactory to provide SAX-specific
 * factory methods.  It provides two types of ContentHandlers,
 * one for creating Transformers, the other for creating Templates
 * objects.
 *
 * <p>If an application wants to set the ErrorHandler or EntityResolver
 * for an XMLReader used during a transformation, it should use a URIResolver
 * to return the SAXSource which provides (with getXMLReader) a reference to
 * the XMLReader.</p>
 */
public abstract class SAXTransformerFactory extends TransformerFactory {

    /** If {@link javax.xml.transform.TransformerFactory#getFeature}
     * returns true when passed this value as an argument,
     * the TransformerFactory returned from
     * {@link javax.xml.transform.TransformerFactory#newInstance} may
     * be safely cast to a SAXTransformerFactory.
     */
    public static final String FEATURE =
        "http://javax.xml.transform.sax.SAXTransformerFactory/feature";

    /** If {@link javax.xml.transform.TransformerFactory#getFeature}
     * returns true when passed this value as an argument,
     * the {@link #newXMLFilter(Source src)}
     * and {@link #newXMLFilter(Templates templates)} methods are supported.
     */
    public static final String FEATURE_XMLFILTER =
        "http://javax.xml.transform.sax.SAXTransformerFactory/feature/xmlfilter";

    /**
     * The default constructor is protected on purpose.
     */
    protected SAXTransformerFactory() {}

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result, based on the transformation
     * instructions specified by the argument.
     *
     * @param src The Source of the transformation instructions.
     *
     * @return TransformerHandler ready to transform SAX events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TransformerHandler can not be created.
     */
    public abstract TransformerHandler newTransformerHandler(Source src)
        throws TransformerConfigurationException;

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result, based on the Templates argument.
     *
     * @param templates The compiled transformation instructions.
     *
     * @return TransformerHandler ready to transform SAX events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TransformerHandler can not be created.
     */
    public abstract TransformerHandler newTransformerHandler(
        Templates templates) throws TransformerConfigurationException;

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result. The transformation
     * is defined as an identity (or copy) transformation, for example
     * to copy a series of SAX parse events into a DOM tree.
     *
     * @return A non-null reference to a TransformerHandler, that may
     * be used as a ContentHandler for SAX parse events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TransformerHandler cannot be created.
     */
    public abstract TransformerHandler newTransformerHandler()
        throws TransformerConfigurationException;

    /**
     * Get a TemplatesHandler object that can process SAX
     * ContentHandler events into a Templates object.
     *
     * @return A non-null reference to a TransformerHandler, that may
     * be used as a ContentHandler for SAX parse events.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TemplatesHandler cannot be created.
     */
    public abstract TemplatesHandler newTemplatesHandler()
        throws TransformerConfigurationException;

    /**
     * Create an XMLFilter that uses the given Source as the
     * transformation instructions.
     *
     * @param src The Source of the transformation instructions.
     *
     * @return An XMLFilter object, or null if this feature is not supported.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TemplatesHandler cannot be created.
     */
    public abstract XMLFilter newXMLFilter(Source src)
        throws TransformerConfigurationException;

    /**
     * Create an XMLFilter, based on the Templates argument..
     *
     * @param templates The compiled transformation instructions.
     *
     * @return An XMLFilter object, or null if this feature is not supported.
     *
     * @throws TransformerConfigurationException If for some reason the
     * TemplatesHandler cannot be created.
     */
    public abstract XMLFilter newXMLFilter(Templates templates)
        throws TransformerConfigurationException;
}
