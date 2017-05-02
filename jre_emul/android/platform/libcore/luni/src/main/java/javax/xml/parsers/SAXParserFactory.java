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

// $Id: SAXParserFactory.java 884950 2009-11-27 18:46:18Z mrglavas $

package javax.xml.parsers;

import javax.xml.validation.Schema;
import org.apache.harmony.xml.parsers.SAXParserFactoryImpl;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Defines a factory API that enables applications to configure and
 * obtain a SAX based parser to parse XML documents.
 *
 * @author <a href="Jeff.Suttor@Sun.com">Jeff Suttor</a>
 * @version $Revision: 884950 $, $Date: 2009-11-27 10:46:18 -0800 (Fri, 27 Nov 2009) $
 */
public abstract class SAXParserFactory {

    /**
     * <p>Should Parsers be validating?</p>
     */
    private boolean validating = false;

    /**
     * <p>Should Parsers be namespace aware?</p>
     */
    private boolean namespaceAware = false;

    /**
     * <p>Protected constructor to force use of {@link #newInstance()}.</p>
     */
    protected SAXParserFactory () {

    }

    /**
     * Returns Android's implementation of {@code SAXParserFactory}. Unlike
     * other Java implementations, this method does not consult system
     * properties, property files, or the services API.
     *
     * @return a new SAXParserFactory.
     *
     * @exception FactoryConfigurationError never. Included for API
     *     compatibility with other Java implementations.
     */
    public static SAXParserFactory newInstance() {
        // instantiate the class directly rather than using reflection
        return new SAXParserFactoryImpl();
    }

    /**
     * Returns an instance of the named implementation of {@code SAXParserFactory}.
     *
     * @throws FactoryConfigurationError if {@code factoryClassName} is not available or cannot be
     *     instantiated.
     * @since 1.6
     */
    public static SAXParserFactory newInstance(String factoryClassName,
            ClassLoader classLoader) {
        if (factoryClassName == null) {
            throw new FactoryConfigurationError("factoryClassName == null");
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            Class<?> type = classLoader != null
                    ? classLoader.loadClass(factoryClassName)
                    : Class.forName(factoryClassName);
            return (SAXParserFactory) type.newInstance();
        } catch (ClassNotFoundException e) {
            throw new FactoryConfigurationError(e);
        } catch (InstantiationException e) {
            throw new FactoryConfigurationError(e);
        } catch (IllegalAccessException e) {
            throw new FactoryConfigurationError(e);
        }
    }

    /**
     * <p>Creates a new instance of a SAXParser using the currently
     * configured factory parameters.</p>
     *
     * @return A new instance of a SAXParser.
     *
     * @exception ParserConfigurationException if a parser cannot
     *   be created which satisfies the requested configuration.
     * @exception SAXException for SAX errors.
     */

    public abstract SAXParser newSAXParser()
        throws ParserConfigurationException, SAXException;


    /**
     * Specifies that the parser produced by this code will
     * provide support for XML namespaces. By default the value of this is set
     * to <code>false</code>.
     *
     * @param awareness true if the parser produced by this code will
     *                  provide support for XML namespaces; false otherwise.
     */

    public void setNamespaceAware(boolean awareness) {
        this.namespaceAware = awareness;
    }

    /**
     * Specifies that the parser produced by this code will
     * validate documents as they are parsed. By default the value of this is
     * set to <code>false</code>.
     *
     * <p>
     * Note that "the validation" here means
     * <a href="http://www.w3.org/TR/REC-xml#proc-types">a validating
     * parser</a> as defined in the XML recommendation.
     * In other words, it essentially just controls the DTD validation.
     * (except the legacy two properties defined in JAXP 1.2.
     * See <a href="#validationCompatibility">here</a> for more details.)
     * </p>
     *
     * <p>
     * To use modern schema languages such as W3C XML Schema or
     * RELAX NG instead of DTD, you can configure your parser to be
     * a non-validating parser by leaving the {@link #setValidating(boolean)}
     * method <tt>false</tt>, then use the {@link #setSchema(Schema)}
     * method to associate a schema to a parser.
     * </p>
     *
     * @param validating true if the parser produced by this code will
     *                   validate documents as they are parsed; false otherwise.
     */

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which are namespace aware.
     *
     * @return true if the factory is configured to produce
     *         parsers which are namespace aware; false otherwise.
     */

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which validate the XML content during parse.
     *
     * @return true if the factory is configured to produce parsers which validate
     *         the XML content during parse; false otherwise.
     */

    public boolean isValidating() {
        return validating;
    }

    /**
     *
     * <p>Sets the particular feature in the underlying implementation of
     * org.xml.sax.XMLReader.
     * A list of the core features and properties can be found at
     * <a href="http://www.saxproject.org/">http://www.saxproject.org/</a></p>
     *
     * <p>All implementations are required to support the {@link javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING} feature.
     * When the feature is</p>
     * <ul>
     *   <li>
     *     <code>true</code>: the implementation will limit XML processing to conform to implementation limits.
     *     Examples include entity expansion limits and XML Schema constructs that would consume large amounts of resources.
     *     If XML processing is limited for security reasons, it will be reported via a call to the registered
     *     {@link org.xml.sax.ErrorHandler#fatalError(SAXParseException exception)}.
     *     See {@link SAXParser} <code>parse</code> methods for handler specification.
     *   </li>
     *   <li>
     *     When the feature is <code>false</code>, the implementation will processing XML according to the XML specifications without
     *     regard to possible implementation limits.
     *   </li>
     * </ul>
     *
     * @param name The name of the feature to be set.
     * @param value The value of the feature to be set.
     *
     * @exception ParserConfigurationException if a parser cannot
     *     be created which satisfies the requested configuration.
     * @exception SAXNotRecognizedException When the underlying XMLReader does
     *            not recognize the property name.
     * @exception SAXNotSupportedException When the underlying XMLReader
     *            recognizes the property name but doesn't support the
     *            property.
     * @throws NullPointerException If the <code>name</code> parameter is null.
     *
     * @see org.xml.sax.XMLReader#setFeature
     */
    public abstract void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException;

    /**
     *
     * <p>Returns the particular property requested for in the underlying
     * implementation of org.xml.sax.XMLReader.</p>
     *
     * @param name The name of the property to be retrieved.
     *
     * @return Value of the requested property.
     *
     * @exception ParserConfigurationException if a parser cannot be created which satisfies the requested configuration.
     * @exception SAXNotRecognizedException When the underlying XMLReader does not recognize the property name.
     * @exception SAXNotSupportedException When the underlying XMLReader recognizes the property name but doesn't support the property.
     *
     * @see org.xml.sax.XMLReader#getProperty
     */
    public abstract boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException,
                SAXNotSupportedException;

    /**
     * Gets the {@link Schema} object specified through
     * the {@link #setSchema(Schema schema)} method.
     *
     *
     * @throws UnsupportedOperationException
     *      For backward compatibility, when implementations for
     *      earlier versions of JAXP is used, this exception will be
     *      thrown.
     *
     * @return
     *      the {@link Schema} object that was last set through
     *      the {@link #setSchema(Schema)} method, or null
     *      if the method was not invoked since a {@link SAXParserFactory}
     *      is created.
     *
     * @since 1.5
     */
    public Schema getSchema() {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }

    /**
     * <p>Set the {@link Schema} to be used by parsers created
     * from this factory.</p>
     *
     * <p>When a {@link Schema} is non-null, a parser will use a validator
     * created from it to validate documents before it passes information
     * down to the application.</p>
     *
     * <p>When warnings/errors/fatal errors are found by the validator, the parser must
     * handle them as if those errors were found by the parser itself.
     * In other words, if the user-specified {@link org.xml.sax.ErrorHandler}
     * is set, it must receive those errors, and if not, they must be
     * treated according to the implementation specific
     * default error handling rules.
     *
     * <p>A validator may modify the SAX event stream (for example by
     * adding default values that were missing in documents), and a parser
     * is responsible to make sure that the application will receive
     * those modified event stream.</p>
     *
     * <p>Initially, <code>null</code> is set as the {@link Schema}.</p>
     *
     * <p>This processing will take effect even if
     * the {@link #isValidating()} method returns <code>false</code>.
     *
     * <p>It is an error to use
     * the <code>http://java.sun.com/xml/jaxp/properties/schemaSource</code>
     * property and/or the <code>http://java.sun.com/xml/jaxp/properties/schemaLanguage</code>
     * property in conjunction with a non-null {@link Schema} object.
     * Such configuration will cause a {@link SAXException}
     * exception when those properties are set on a {@link SAXParser}.</p>
     *
     * <h4>Note for implementors</h4>
     * <p>
     * A parser must be able to work with any {@link Schema}
     * implementation. However, parsers and schemas are allowed
     * to use implementation-specific custom mechanisms
     * as long as they yield the result described in the specification.
     * </p>
     *
     * @param schema <code>Schema</code> to use, <code>null</code> to remove a schema.
     *
     * @throws UnsupportedOperationException
     *      For backward compatibility, when implementations for
     *      earlier versions of JAXP is used, this exception will be
     *      thrown.
     *
     * @since 1.5
     */
    public void setSchema(Schema schema) {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }

    /**
     * <p>Set state of XInclude processing.</p>
     *
     * <p>If XInclude markup is found in the document instance, should it be
     * processed as specified in <a href="http://www.w3.org/TR/xinclude/">
     * XML Inclusions (XInclude) Version 1.0</a>.</p>
     *
     * <p>XInclude processing defaults to <code>false</code>.</p>
     *
     * @param state Set XInclude processing to <code>true</code> or
     *   <code>false</code>
     *
     * @throws UnsupportedOperationException
     *      For backward compatibility, when implementations for
     *      earlier versions of JAXP is used, this exception will be
     *      thrown.
     *
     * @since 1.5
     */
    public void setXIncludeAware(final boolean state) {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }

    /**
     * <p>Get state of XInclude processing.</p>
     *
     * @return current state of XInclude processing
     *
     * @throws UnsupportedOperationException
     *      For backward compatibility, when implementations for
     *      earlier versions of JAXP is used, this exception will be
     *      thrown.
     *
     * @since 1.5
     */
    public boolean isXIncludeAware() {
        throw new UnsupportedOperationException(
            "This parser does not support specification \""
            + this.getClass().getPackage().getSpecificationTitle()
            + "\" version \""
            + this.getClass().getPackage().getSpecificationVersion()
            + "\""
            );
    }
}

