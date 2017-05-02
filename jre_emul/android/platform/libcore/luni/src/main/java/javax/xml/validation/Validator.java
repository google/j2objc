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
// $Id: Validator.java 888884 2009-12-09 17:36:46Z mrglavas $

package javax.xml.validation;

import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * <p>A processor that checks an XML document against {@link Schema}.</p>
 *
 * <p>
 * A validator is a thread-unsafe and non-reentrant object.
 * In other words, it is the application's responsibility to make
 * sure that one {@link Validator} object is not used from
 * more than one thread at any given time, and while the <tt>validate</tt>
 * method is invoked, applications may not recursively call
 * the <tt>validate</tt> method.
 * <p>
 *
 * Note that while the {@link #validate(javax.xml.transform.Source)} and {@link #validate(javax.xml.transform.Source, javax.xml.transform.Result)}
 * methods take a {@link Source} instance, the <code>Source</code>
 * instance must be a <code>SAXSource</code>, <code>DOMSource</code>, <code>StAXSource</code> or <code>StreamSource</code>.
 *
 * @author  <a href="mailto:Kohsuke.Kawaguchi@Sun.com">Kohsuke Kawaguchi</a>
 * @version $Revision: 888884 $, $Date: 2009-12-09 09:36:46 -0800 (Wed, 09 Dec 2009) $
 * @since 1.5
 */
public abstract class Validator {

    /**
     * Constructor for derived classes.
     *
     * <p>
     * The constructor does nothing.
     *
     * <p>
     * Derived classes must create {@link Validator} objects that have
     * <tt>null</tt> {@link ErrorHandler} and
     * <tt>null</tt> {@link LSResourceResolver}.
     */
    protected Validator() {
    }

    /**
     * <p>Reset this <code>Validator</code> to its original configuration.</p>
     *
     * <p><code>Validator</code> is reset to the same state as when it was created with
     * {@link Schema#newValidator()}.
     * <code>reset()</code> is designed to allow the reuse of existing <code>Validator</code>s
     * thus saving resources associated with the creation of new <code>Validator</code>s.</p>
     *
     * <p>The reset <code>Validator</code> is not guaranteed to have the same {@link LSResourceResolver} or {@link ErrorHandler}
     * <code>Object</code>s, e.g. {@link Object#equals(Object obj)}.  It is guaranteed to have a functionally equal
     * <code>LSResourceResolver</code> and <code>ErrorHandler</code>.</p>
     */
    public abstract void reset();

    /**
     * Validates the specified input.
     *
     * <p>
     * This is just a convenience method of:
     * <pre>
     * validate(source,null);
     * </pre>
     *
     * @see #setErrorHandler(ErrorHandler)
     */
    public void validate(Source source) throws SAXException, IOException {
        validate(source, null);
    }

    /**
     * Validates the specified input and send the augmented validation
     * result to the specified output.
     *
     * <p>
     * This method places the following restrictions on the types of
     * the {@link Source}/{@link Result} accepted.
     *
     * <h4>{@link Source}/{@link Result} accepted:</h4>
     * <table border=1>
     * <thead>
     *  <tr>
     *   <td></td>
     *   <td>{@link javax.xml.transform.sax.SAXSource}</td>
     *   <td>{@link javax.xml.transform.dom.DOMSource}</td>
     *   <td>{@link javax.xml.transform.stream.StreamSource}</td>
     *  </tr>
     * </thead>
     * <tbody>
     *  <tr>
     *   <td><tt>null</tt></td>
     *   <td>OK</td>
     *   <td>OK</td>
     *   <td>OK</td>
     *   <td>OK</td>
     *  </tr>
     *  <tr>
     *   <td>{@link javax.xml.transform.sax.SAXResult}</td>
     *   <td>OK</td>
     *   <td>Err</td>
     *   <td>Err</td>
     *   <td>Err</td>
     *  </tr>
     *  <tr>
     *   <td>{@link javax.xml.transform.dom.DOMResult}</td>
     *   <td>Err</td>
     *   <td>OK</td>
     *   <td>Err</td>
     *   <td>Err</td>
     *  </tr>
     *  <tr>
     *   <td>{@link javax.xml.transform.stream.StreamResult}</td>
     *   <td>Err</td>
     *   <td>Err</td>
     *   <td>Err</td>
     *   <td>OK</td>
     *  </tr>
     * </tbody>
     * </table>
     *
     * <p>
     * To validate one {@link Source} into another kind of {@link Result}, use the identity transformer
     * (see {@link javax.xml.transform.TransformerFactory#newTransformer()}).
     *
     * <p>
     * Errors found during the validation is sent to the specified
     * {@link ErrorHandler}.
     *
     * <p>
     * If a document is valid, or if a document contains some errors
     * but none of them were fatal and the {@link ErrorHandler} didn't
     * throw any exception, then the method returns normally.
     *
     * @param source
     *      XML to be validated. Must not be null.
     *
     * @param result
     *      The {@link Result} object that receives (possibly augmented)
     *      XML. This parameter can be null if the caller is not interested
     *      in it.
     *
     *      Note that when a {@link javax.xml.transform.dom.DOMResult} is used,
     *      a validator might just pass the same DOM node from
     *      {@link javax.xml.transform.dom.DOMSource} to
     *      {@link javax.xml.transform.dom.DOMResult}
     *      (in which case <tt>source.getNode()==result.getNode()</tt>),
     *      it might copy the entire DOM tree, or it might alter the
     *      node given by the source.
     *
     * @throws IllegalArgumentException
     *      If the {@link Result} type doesn't match the {@link Source} type,
     *      or if the specified source is not a
     *      {@link javax.xml.transform.sax.SAXSource},
     *      {@link javax.xml.transform.dom.DOMSource} or
     *      {@link javax.xml.transform.stream.StreamSource}.
     *
     * @throws SAXException
     *      If the {@link ErrorHandler} throws a {@link SAXException} or
     *      if a fatal error is found and the {@link ErrorHandler} returns
     *      normally.
     *
     * @throws IOException
     *      If the validator is processing a
     *      {@link javax.xml.transform.sax.SAXSource} and the
     *      underlying {@link org.xml.sax.XMLReader} throws an
     *      {@link IOException}.
     *
     * @throws NullPointerException
     *      If the <tt>source</tt> parameter is null.
     *
     * @see #validate(Source)
     */
    public abstract void validate(Source source, Result result) throws SAXException, IOException;

    /**
     * Sets the {@link ErrorHandler} to receive errors encountered
     * during the <code>validate</code> method invocation.
     *
     * <p>
     * Error handler can be used to customize the error handling process
     * during a validation. When an {@link ErrorHandler} is set,
     * errors found during the validation will be first sent
     * to the {@link ErrorHandler}.
     *
     * <p>
     * The error handler can abort further validation immediately
     * by throwing {@link SAXException} from the handler. Or for example
     * it can print an error to the screen and try to continue the
     * validation by returning normally from the {@link ErrorHandler}
     *
     * <p>
     * If any {@link Throwable} is thrown from an {@link ErrorHandler},
     * the caller of the <code>validate</code> method will be thrown
     * the same {@link Throwable} object.
     *
     * <p>
     * {@link Validator} is not allowed to
     * throw {@link SAXException} without first reporting it to
     * {@link ErrorHandler}.
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
     * When a new {@link Validator} object is created, initially
     * this field is set to null.
     *
     * @param   errorHandler
     *      A new error handler to be set. This parameter can be null.
     */
    public abstract void setErrorHandler(ErrorHandler errorHandler);

    /**
     * Gets the current {@link ErrorHandler} set to this {@link Validator}.
     *
     * @return
     *      This method returns the object that was last set through
     *      the {@link #setErrorHandler(ErrorHandler)} method, or null
     *      if that method has never been called since this {@link Validator}
     *      has created.
     *
     * @see #setErrorHandler(ErrorHandler)
     */
    public abstract ErrorHandler getErrorHandler();

    /**
     * Sets the {@link LSResourceResolver} to customize
     * resource resolution while in a validation episode.
     *
     * <p>
     * {@link Validator} uses a {@link LSResourceResolver}
     * when it needs to locate external resources while a validation,
     * although exactly what constitutes "locating external resources" is
     * up to each schema language.
     *
     * <p>
     * When the {@link LSResourceResolver} is null, the implementation will
     * behave as if the following {@link LSResourceResolver} is set:
     * <pre>
     * class DumbLSResourceResolver implements {@link LSResourceResolver} {
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
     * then the {@link Validator} will abort the parsing and
     * the caller of the <code>validate</code> method will receive
     * the same {@link RuntimeException}.
     *
     * <p>
     * When a new {@link Validator} object is created, initially
     * this field is set to null.
     *
     * @param   resourceResolver
     *      A new resource resolver to be set. This parameter can be null.
     */
    public abstract void setResourceResolver(LSResourceResolver resourceResolver);

    /**
     * Gets the current {@link LSResourceResolver} set to this {@link Validator}.
     *
     * @return
     *      This method returns the object that was last set through
     *      the {@link #setResourceResolver(LSResourceResolver)} method, or null
     *      if that method has never been called since this {@link Validator}
     *      has created.
     *
     * @see #setErrorHandler(ErrorHandler)
     */
    public abstract LSResourceResolver getResourceResolver();



    /**
     * Look up the value of a feature flag.
     *
     * <p>The feature name is any fully-qualified URI.  It is
     * possible for a {@link Validator} to recognize a feature name but
     * temporarily be unable to return its value.
     * Some feature values may be available only in specific
     * contexts, such as before, during, or after a validation.
     *
     * <p>Implementors are free (and encouraged) to invent their own features,
     * using names built on their own URIs.</p>
     *
     * @param name The feature name, which is a non-null fully-qualified URI.
     * @return The current value of the feature (true or false).
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            {@link Validator} recognizes the feature name but
     *            cannot determine its value at this time.
     * @throws NullPointerException
     *          When the name parameter is null.
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
     * Feature can be used to control the way a {@link Validator}
     * parses schemas, although {@link Validator}s are not required
     * to recognize any specific property names.</p>
     *
     * <p>The feature name is any fully-qualified URI.  It is
     * possible for a {@link Validator} to expose a feature value but
     * to be unable to change the current value.
     * Some feature values may be immutable or mutable only
     * in specific contexts, such as before, during, or after
     * a validation.</p>
     *
     * @param name The feature name, which is a non-null fully-qualified URI.
     * @param value The requested value of the feature (true or false).
     *
     * @exception org.xml.sax.SAXNotRecognizedException If the feature
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            {@link Validator} recognizes the feature name but
     *            cannot set the requested value.
     * @throws NullPointerException
     *          When the name parameter is null.
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
     * possible for a {@link Validator} to recognize a property name but
     * to be unable to change the current value.
     * Some property values may be immutable or mutable only
     * in specific contexts, such as before, during, or after
     * a validation.</p>
     *
     * <p>{@link Validator}s are not required to recognize setting
     * any specific property names.</p>
     *
     * @param name The property name, which is a non-null fully-qualified URI.
     * @param object The requested value for the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the property
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            {@link Validator} recognizes the property name but
     *            cannot set the requested value.
     * @throws NullPointerException
     *          When the name parameter is null.
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
     * possible for a {@link Validator} to recognize a property name but
     * temporarily be unable to return its value.
     * Some property values may be available only in specific
     * contexts, such as before, during, or after a validation.</p>
     *
     * <p>{@link Validator}s are not required to recognize any specific
     * property names.</p>
     *
     * <p>Implementors are free (and encouraged) to invent their own properties,
     * using names built on their own URIs.</p>
     *
     * @param name The property name, which is a non-null fully-qualified URI.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the property
     *            value can't be assigned or retrieved.
     * @exception org.xml.sax.SAXNotSupportedException When the
     *            XMLReader recognizes the property name but
     *            cannot determine its value at this time.
     * @throws NullPointerException
     *          When the name parameter is null.
     * @see #setProperty(String, Object)
     */
    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        throw new SAXNotRecognizedException(name);
    }
}
