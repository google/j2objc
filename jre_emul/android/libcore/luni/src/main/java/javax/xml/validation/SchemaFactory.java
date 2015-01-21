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

// $Id: SchemaFactory.java 884952 2009-11-27 18:55:08Z mrglavas $

package javax.xml.validation;

import java.io.File;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Factory that creates {@link Schema} objects&#x2E; Entry-point to
 * the validation API.
 *
 * <p>
 * {@link SchemaFactory} is a schema compiler. It reads external
 * representations of schemas and prepares them for validation.
 *
 * <p>
 * The {@link SchemaFactory} class is not thread-safe. In other words,
 * it is the application's responsibility to ensure that at most
 * one thread is using a {@link SchemaFactory} object at any
 * given moment. Implementations are encouraged to mark methods
 * as <tt>synchronized</tt> to protect themselves from broken clients.
 *
 * <p>
 * {@link SchemaFactory} is not re-entrant. While one of the
 * <code>newSchema</code> methods is being invoked, applications
 * may not attempt to recursively invoke the <code>newSchema</code> method,
 * even from the same thread.
 *
 * <h2><a name="schemaLanguage"></a>Schema Language</h2>
 * <p>
 * This spec uses a namespace URI to designate a schema language.
 * The following table shows the values defined by this specification.
 * <p>
 * To be compliant with the spec, the implementation
 * is only required to support W3C XML Schema 1.0. However,
 * if it chooses to support other schema languages listed here,
 * it must conform to the relevant behaviors described in this spec.
 *
 * <p>
 * Schema languages not listed here are expected to
 * introduce their own URIs to represent themselves.
 * The {@link SchemaFactory} class is capable of locating other
 * implementations for other schema languages at run-time.
 *
 * <p>
 * Note that because the XML DTD is strongly tied to the parsing process
 * and has a significant effect on the parsing process, it is impossible
 * to define the DTD validation as a process independent from parsing.
 * For this reason, this specification does not define the semantics for
 * the XML DTD. This doesn't prohibit implementers from implementing it
 * in a way they see fit, but <em>users are warned that any DTD
 * validation implemented on this interface necessarily deviate from
 * the XML DTD semantics as defined in the XML 1.0</em>.
 *
 * <table border="1" cellpadding="2">
 *   <thead>
 *     <tr>
 *       <th>value</th>
 *       <th>language</th>
 *     </tr>
 *   </thead>
 *   <tbody>
 *     <tr>
 *       <td>{@link javax.xml.XMLConstants#W3C_XML_SCHEMA_NS_URI} ("<code>http://www.w3.org/2001/XMLSchema</code>")</td>
 *       <td><a href="http://www.w3.org/TR/xmlschema-1">W3C XML Schema 1.0</a></td>
 *     </tr>
 *     <tr>
 *       <td>{@link javax.xml.XMLConstants#RELAXNG_NS_URI} ("<code>http://relaxng.org/ns/structure/1.0</code>")</td>
 *       <td><a href="http://www.relaxng.org/">RELAX NG 1.0</a></td>
 *     </tr>
 *   </tbody>
 * </table>
 *
 * @author  <a href="mailto:Kohsuke.Kawaguchi@Sun.com">Kohsuke Kawaguchi</a>
 * @version $Revision: 884952 $, $Date: 2009-11-27 10:55:08 -0800 (Fri, 27 Nov 2009) $
 * @since 1.5
 */
public abstract class SchemaFactory {

    /**
     * <p>Constructor for derived classes.</p>
     *
     * <p>The constructor does nothing.</p>
     *
     * <p>Derived classes must create {@link SchemaFactory} objects that have
     * <code>null</code> {@link ErrorHandler} and
     * <code>null</code> {@link LSResourceResolver}.</p>
     */
    protected SchemaFactory() {
    }

    /**
     * <p>Lookup an implementation of the <code>SchemaFactory</code> that supports the specified
     * schema language and return it.</p>
     *
     * <p>To find a <code>SchemaFactory</code> object for a given schema language,
     * this method looks the following places in the following order
     * where "the class loader" refers to the context class loader:</p>
     * <ol>
     *  <li>
     *     If the system property
     *     <code>"javax.xml.validation.SchemaFactory:<i>schemaLanguage</i>"</code>
     *     is present (where <i>schemaLanguage</i> is the parameter
     *     to this method), then its value is read
     *     as a class name. The method will try to
     *     create a new instance of this class by using the class loader,
     *     and returns it if it is successfully created.
     *   </li>
     *   <li>
     *     <code>$java.home/lib/jaxp.properties</code> is read and
     *     the value associated with the key being the system property above
     *     is looked for. If present, the value is processed just like above.
     *   </li>
     *   <li>
     *     <p>The class loader is asked for service provider provider-configuration files matching
     *     <code>javax.xml.validation.SchemaFactory</code> in the resource directory META-INF/services.
     *     See the JAR File Specification for file format and parsing rules.
     *     Each potential service provider is required to implement the method:</p>
     *     <pre>
     *        {@link #isSchemaLanguageSupported(String schemaLanguage)}
     *     </pre>
     *     The first service provider found in class loader order that supports the specified schema language is returned.
     *   </li>
     *   <li>
     *     Platform default <code>SchemaFactory</code> is located
     *     in a implementation specific way. There must be a platform default
     *     <code>SchemaFactory</code> for W3C XML Schema.
     *   </li>
     * </ol>
     *
     * <p>If everything fails, {@link IllegalArgumentException} will be thrown.</p>
     *
     * <p><strong>Tip for Trouble-shooting:</strong></p>
     * <p>See {@link java.util.Properties#load(java.io.InputStream)} for
     * exactly how a property file is parsed. In particular, colons ':'
     * need to be escaped in a property file, so make sure schema language
     * URIs are properly escaped in it. For example:</p>
     * <pre>
     * http\://www.w3.org/2001/XMLSchema=org.acme.foo.XSSchemaFactory
     * </pre>
     *
     * @param schemaLanguage
     *      Specifies the schema language which the returned
     *      SchemaFactory will understand. See
     *      <a href="#schemaLanguage">the list of available
     *      schema languages</a> for the possible values.
     *
     * @return New instance of a <code>SchemaFactory</code>
     *
     * @throws IllegalArgumentException
     *      If no implementation of the schema language is available.
     *
     * @throws NullPointerException
     *      If the <tt>schemaLanguage</tt> parameter is null.
     */
    public static SchemaFactory newInstance(String schemaLanguage) {
        ClassLoader cl;
        cl = Thread.currentThread().getContextClassLoader();

        if (cl == null) {
            //cl = ClassLoader.getSystemClassLoader();
            //use the current class loader
            cl = SchemaFactory.class.getClassLoader();
        }

        SchemaFactory f = new SchemaFactoryFinder(cl).newFactory(schemaLanguage);
        if (f == null) {
            throw new IllegalArgumentException(schemaLanguage);
        }
        return f;
    }

    /**
     * Returns an instance of the named implementation of {@code SchemaFactory}.
     *
     * @throws IllegalArgumentException if {@code factoryClassName} is not available, cannot be
     *     instantiated, or doesn't support {@code schemaLanguage}.
     * @since 1.6
     */
    public static SchemaFactory newInstance(String schemaLanguage, String factoryClassName,
            ClassLoader classLoader) {
        if (schemaLanguage == null) {
            throw new NullPointerException("schemaLanguage == null");
        } else if (factoryClassName == null) {
            throw new NullPointerException("factoryClassName == null");
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            Class<?> type = classLoader != null
                    ? classLoader.loadClass(factoryClassName)
                    : Class.forName(factoryClassName);
            SchemaFactory result = (SchemaFactory) type.newInstance();
            if (result == null || !result.isSchemaLanguageSupported(schemaLanguage)) {
                throw new IllegalArgumentException(schemaLanguage);
            }
            return result;
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * <p>Is specified schema supported by this <code>SchemaFactory</code>?</p>
     *
     * @param schemaLanguage Specifies the schema language which the returned <code>SchemaFactory</code> will understand.
     *    <code>schemaLanguage</code> must specify a <a href="#schemaLanguage">valid</a> schema language.
     *
     * @return <code>true</code> if <code>SchemaFactory</code> supports <code>schemaLanguage</code>, else <code>false</code>.
     *
     * @throws NullPointerException If <code>schemaLanguage</code> is <code>null</code>.
     * @throws IllegalArgumentException If <code>schemaLanguage.length() == 0</code>
     *   or <code>schemaLanguage</code> does not specify a <a href="#schemaLanguage">valid</a> schema language.
     */
    public abstract boolean isSchemaLanguageSupported(String schemaLanguage);

    /**
     * Look up the value of a feature flag.
     *
     * <p>The feature name is any fully-qualified URI.  It is
     * possible for a {@link SchemaFactory} to recognize a feature name but
     * temporarily be unable to return its value.
     *
     * <p>Implementers are free (and encouraged) to invent their own features,
     * using names built on their own URIs.</p>
     *
     * @param name The feature name, which is a non-null fully-qualified URI.
     * @return The current value of the feature (true or false).
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            {@link SchemaFactory} recognizes the feature name but
     *            cannot determine its value at this time.
     * @exception NullPointerException
     *              if the name parameter is null.
     * @see #setFeature(String, boolean)
     */
    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {

        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Set the value of a feature flag.
     *
     * <p>
     * Feature can be used to control the way a {@link SchemaFactory}
     * parses schemas, although {@link SchemaFactory}s are not required
     * to recognize any specific feature names.</p>
     *
     * <p>The feature name is any fully-qualified URI.  It is
     * possible for a {@link SchemaFactory} to expose a feature value but
     * to be unable to change the current value.</p>
     *
     * <p>All implementations are required to support the {@link javax.xml.XMLConstants#FEATURE_SECURE_PROCESSING} feature.
     * When the feature is:</p>
     * <ul>
     *   <li>
     *     <code>true</code>: the implementation will limit XML processing to conform to implementation limits.
     *     Examples include entity expansion limits and XML Schema constructs that would consume large amounts of resources.
     *     If XML processing is limited for security reasons, it will be reported via a call to the registered
     *     {@link ErrorHandler#fatalError(org.xml.sax.SAXParseException)}.
     *     See {@link  #setErrorHandler(ErrorHandler errorHandler)}.
     *   </li>
     *   <li>
     *     <code>false</code>: the implementation will processing XML according to the XML specifications without
     *     regard to possible implementation limits.
     *   </li>
     * </ul>
     *
     * @param name The feature name, which is a non-null fully-qualified URI.
     * @param value The requested value of the feature (true or false).
     *
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            {@link SchemaFactory} recognizes the feature name but
     *            cannot set the requested value.
     * @exception NullPointerException
     *              if the name parameter is null.
     *
     * @see #getFeature(String)
     */
    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Set the value of a property.
     *
     * <p>The property name is any fully-qualified URI.  It is
     * possible for a {@link SchemaFactory} to recognize a property name but
     * to be unable to change the current value.</p>
     *
     * <p>{@link SchemaFactory}s are not required to recognize setting
     * any specific property names.</p>
     *
     * @param name The property name, which is a non-null fully-qualified URI.
     * @param object The requested value for the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the property
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            {@link SchemaFactory} recognizes the property name but
     *            cannot set the requested value.
     * @exception NullPointerException
     *              if the name parameter is null.
     */
    public void setProperty(String name, Object object) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Look up the value of a property.
     *
     * <p>The property name is any fully-qualified URI.  It is
     * possible for a {@link SchemaFactory} to recognize a property name but
     * temporarily be unable to return its value.</p>
     *
     * <p>{@link SchemaFactory}s are not required to recognize any specific
     * property names.</p>
     *
     * <p>Implementers are free (and encouraged) to invent their own properties,
     * using names built on their own URIs.</p>
     *
     * @param name The property name, which is a non-null fully-qualified URI.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the property
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the property name but
     *            cannot determine its value at this time.
     * @exception NullPointerException
     *              if the name parameter is null.
     * @see #setProperty(String, Object)
     */
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }

    /**
     * Sets the {@link ErrorHandler} to receive errors encountered
     * during the <code>newSchema</code> method invocation.
     *
     * <p>
     * Error handler can be used to customize the error handling process
     * during schema parsing. When an {@link ErrorHandler} is set,
     * errors found during the parsing of schemas will be first sent
     * to the {@link ErrorHandler}.
     *
     * <p>
     * The error handler can abort the parsing of a schema immediately
     * by throwing {@link SAXException} from the handler. Or for example
     * it can print an error to the screen and try to continue the
     * processing by returning normally from the {@link ErrorHandler}
     *
     * <p>
     * If any {@link Throwable} (or instances of its derived classes)
     * is thrown from an {@link ErrorHandler},
     * the caller of the <code>newSchema</code> method will be thrown
     * the same {@link Throwable} object.
     *
     * <p>
     * {@link SchemaFactory} is not allowed to
     * throw {@link SAXException} without first reporting it to
     * {@link ErrorHandler}.
     *
     * <p>
     * Applications can call this method even during a {@link Schema}
     * is being parsed.
     *
     * <p>
     * When the {@link ErrorHandler} is null, the implementation will
     * behave as if the following {@link ErrorHandler} is set:
     * <pre>
     * class DraconianErrorHandler implements {@link ErrorHandler} {
     *     public void fatalError( {@link org.xml.sax.SAXParseException} e ) throws {@link SAXException} {
     *         throw e;
     *     }
     *     public void error( {@link org.xml.sax.SAXParseException} e ) throws {@link SAXException} {
     *         throw e;
     *     }
     *     public void warning( {@link org.xml.sax.SAXParseException} e ) throws {@link SAXException} {
     *         // noop
     *     }
     * }
     * </pre>
     *
     * <p>
     * When a new {@link SchemaFactory} object is created, initially
     * this field is set to null. This field will <em>NOT</em> be
     * inherited to {@link Schema}s, {@link Validator}s, or
     * {@link ValidatorHandler}s that are created from this {@link SchemaFactory}.
     *
     *
     * @param   errorHandler
     *      A new error handler to be set. This parameter can be null.
     */
    public abstract void setErrorHandler(ErrorHandler errorHandler);

    /**
     * Gets the current {@link ErrorHandler} set to this {@link SchemaFactory}.
     *
     * @return
     *      This method returns the object that was last set through
     *      the {@link #setErrorHandler(ErrorHandler)} method, or null
     *      if that method has never been called since this {@link SchemaFactory}
     *      has created.
     *
     * @see #setErrorHandler(ErrorHandler)
     */
    public abstract ErrorHandler getErrorHandler();

    /**
     * Sets the {@link LSResourceResolver} to customize
     * resource resolution when parsing schemas.
     *
     * <p>
     * {@link SchemaFactory} uses a {@link LSResourceResolver}
     * when it needs to locate external resources while parsing schemas,
     * although exactly what constitutes "locating external resources" is
     * up to each schema language. For example, for W3C XML Schema,
     * this includes files <tt>&lt;include></tt>d or <tt>&lt;import></tt>ed,
     * and DTD referenced from schema files, etc.
     *
     * <p>
     * Applications can call this method even during a {@link Schema}
     * is being parsed.
     *
     * <p>
     * When the {@link LSResourceResolver} is null, the implementation will
     * behave as if the following {@link LSResourceResolver} is set:
     * <pre>
     * class DumbDOMResourceResolver implements {@link LSResourceResolver} {
     *     public {@link org.w3c.dom.ls.LSInput} resolveResource(
     *         String publicId, String systemId, String baseURI) {
     *
     *         return null; // always return null
     *     }
     * }
     * </pre>
     *
     * <p>
     * If a {@link LSResourceResolver} throws a {@link RuntimeException}
     *  (or instances of its derived classes),
     * then the {@link SchemaFactory} will abort the parsing and
     * the caller of the <code>newSchema</code> method will receive
     * the same {@link RuntimeException}.
     *
     * <p>
     * When a new {@link SchemaFactory} object is created, initially
     * this field is set to null.  This field will <em>NOT</em> be
     * inherited to {@link Schema}s, {@link Validator}s, or
     * {@link ValidatorHandler}s that are created from this {@link SchemaFactory}.
     *
     * @param   resourceResolver
     *      A new resource resolver to be set. This parameter can be null.
     */
    public abstract void setResourceResolver(LSResourceResolver resourceResolver);

    /**
     * Gets the current {@link LSResourceResolver} set to this {@link SchemaFactory}.
     *
     * @return
     *      This method returns the object that was last set through
     *      the {@link #setResourceResolver(LSResourceResolver)} method, or null
     *      if that method has never been called since this {@link SchemaFactory}
     *      has created.
     *
     * @see #setErrorHandler(ErrorHandler)
     */
    public abstract LSResourceResolver getResourceResolver();

    /**
     * <p>Parses the specified source as a schema and returns it as a schema.</p>
     *
     * <p>This is a convenience method for {@link #newSchema(Source[] schemas)}.</p>
     *
     * @param schema Source that represents a schema.
     *
     * @return New <code>Schema</code> from parsing <code>schema</code>.
     *
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws NullPointerException if <tt>schema</tt> is null.
     */
    public Schema newSchema(Source schema) throws SAXException {
        return newSchema(new Source[]{schema});
    }

    /**
     * <p>Parses the specified <code>File</code> as a schema and returns it as a <code>Schema</code>.</p>
     *
     * <p>This is a convenience method for {@link #newSchema(Source schema)}.</p>
     *
     * @param schema File that represents a schema.
     *
     * @return New <code>Schema</code> from parsing <code>schema</code>.
     *
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws NullPointerException if <tt>schema</tt> is null.
     */
    public Schema newSchema(File schema) throws SAXException {
        return newSchema(new StreamSource(schema));
    }

    /**
     * <p>Parses the specified <code>URL</code> as a schema and returns it as a <code>Schema</code>.</p>
     *
     * <p>This is a convenience method for {@link #newSchema(Source schema)}.</p>
     *
     * @param schema <code>URL</code> that represents a schema.
     *
     * @return New <code>Schema</code> from parsing <code>schema</code>.
     *
     * @throws SAXException If a SAX error occurs during parsing.
     * @throws NullPointerException if <tt>schema</tt> is null.
     */
    public Schema newSchema(URL schema) throws SAXException {
        return newSchema(new StreamSource(schema.toExternalForm()));
    }

    /**
     * Parses the specified source(s) as a schema and returns it as a schema.
     *
     * <p>
     * The callee will read all the {@link Source}s and combine them into a
     * single schema. The exact semantics of the combination depends on the schema
     * language that this {@link SchemaFactory} object is created for.
     *
     * <p>
     * When an {@link ErrorHandler} is set, the callee will report all the errors
     * found in sources to the handler. If the handler throws an exception, it will
     * abort the schema compilation and the same exception will be thrown from
     * this method. Also, after an error is reported to a handler, the callee is allowed
     * to abort the further processing by throwing it. If an error handler is not set,
     * the callee will throw the first error it finds in the sources.
     *
     * <h2>W3C XML Schema 1.0</h2>
     * <p>
     * The resulting schema contains components from the specified sources.
     * The same result would be achieved if all these sources were
     * imported, using appropriate values for schemaLocation and namespace,
     * into a single schema document with a different targetNamespace
     * and no components of its own, if the import elements were given
     * in the same order as the sources.  Section 4.2.3 of the XML Schema
     * recommendation describes the options processors have in this
     * regard.  While a processor should be consistent in its treatment of
     * JAXP schema sources and XML Schema imports, the behavior between
     * JAXP-compliant parsers may vary; in particular, parsers may choose
     * to ignore all but the first &lt;import> for a given namespace,
     * regardless of information provided in schemaLocation.
     *
     * <p>
     * If the parsed set of schemas includes error(s) as
     * specified in the section 5.1 of the XML Schema spec, then
     * the error must be reported to the {@link ErrorHandler}.
     *
     * <h2>RELAX NG</h2>
     *
     * <p>For RELAX NG, this method must throw {@link UnsupportedOperationException}
     * if <tt>schemas.length!=1</tt>.
     *
     *
     * @param schemas
     *      inputs to be parsed. {@link SchemaFactory} is required
     *      to recognize {@link StreamSource},
     *      {@link javax.xml.transform.sax.SAXSource},
     *      and {@link javax.xml.transform.dom.DOMSource}.
     *
     * @return
     *      Always return a non-null valid {@link Schema} object.
     *      Note that when an error has been reported, there is no
     *      guarantee that the returned {@link Schema} object is
     *      meaningful.
     *
     * @throws SAXException
     *      If an error is found during processing the specified inputs.
     *      When an {@link ErrorHandler} is set, errors are reported to
     *      there first. See {@link #setErrorHandler(ErrorHandler)}.
     * @throws NullPointerException
     *      If the <code>schemas</code> parameter itself is null or
     *      any item in the array is null.
     * @throws IllegalArgumentException
     *      If any item in the array is not recognized by this method.
     * @throws UnsupportedOperationException
     *      If the schema language doesn't support this operation.
     */
    public abstract Schema newSchema(Source[] schemas) throws SAXException;

    /**
     * Creates a special {@link Schema} object.
     *
     * <p>
     * The exact semantics of the returned {@link Schema} object depends
     * on the schema language that this {@link SchemaFactory} is created
     * for.
     *
     * <p>
     * Also, implementations are allowed to use implementation-specific
     * property/feature to alter the semantics of this method.
     *
     *
     * <h2>W3C XML Schema 1.0</h2>
     * <p>
     * For XML Schema, this method creates a {@link Schema} object that
     * performs validation by using location hints specified in documents.
     *
     * <p>
     * The returned {@link Schema} object assumes that if documents
     * refer to the same URL in the schema location hints,
     * they will always resolve to the same schema document. This
     * assumption allows implementations to reuse parsed results of
     * schema documents so that multiple validations against the same
     * schema will run faster.
     *
     * <p>
     * Note that the use of schema location hints introduces a
     * vulnerability to denial-of-service attacks.
     *
     *
     * <h2>RELAX NG</h2>
     * <p>
     * RELAX NG does not support this operation.
     *
     * @return
     *      Always return non-null valid {@link Schema} object.
     *
     * @throws UnsupportedOperationException
     *      If this operation is not supported by the callee.
     * @throws SAXException
     *      If this operation is supported but failed for some reason.
     */
    public abstract Schema newSchema() throws SAXException;
}
