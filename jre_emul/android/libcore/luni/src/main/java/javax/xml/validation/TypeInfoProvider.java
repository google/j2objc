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
// $Id: TypeInfoProvider.java 884939 2009-11-27 18:20:46Z mrglavas $

package javax.xml.validation;

import org.w3c.dom.TypeInfo;

/**
 * This class provides access to the type information determined
 * by {@link ValidatorHandler}.
 *
 * <p>
 * Some schema languages, such as W3C XML Schema, encourages a validator
 * to report the "type" it assigns to each attribute/element.
 * Those applications who wish to access this type information can invoke
 * methods defined on this "interface" to access such type information.
 *
 * <p>
 * Implementation of this "interface" can be obtained through the
 * {@link ValidatorHandler#getTypeInfoProvider()} method.
 *
 * @author  <a href="mailto:Kohsuke.Kawaguchi@Sun.com">Kohsuke Kawaguchi</a>
 * @version $Revision: 884939 $, $Date: 2009-11-27 10:20:46 -0800 (Fri, 27 Nov 2009) $
 * @see org.w3c.dom.TypeInfo
 * @since 1.5
 */
public abstract class TypeInfoProvider {

    /**
     * Constructor for the derived class.
     *
     * <p>
     * The constructor does nothing.
     */
    protected TypeInfoProvider() {
    }

    /**
     * <p>Returns the immutable {@link TypeInfo} object for the current element.</p>
     *
     * <p>
     * The method may only be called by the startElement and endElement event of
     * the {@link org.xml.sax.ContentHandler} that the application sets to the
     * {@link ValidatorHandler}.</p>
     *
     * @throws IllegalStateException
     *      If this method is called from other {@link org.xml.sax.ContentHandler}
     *      methods.
     * @return
     *      An immutable {@link TypeInfo} object that represents the
     *      type of the current element.
     *      Note that the caller can keep references to the obtained
     *      {@link TypeInfo} longer than the callback scope.
     *
     *      Otherwise, this method returns
     *      null if the validator is unable to
     *      determine the type of the current element for some reason
     *      (for example, if the validator is recovering from
     *      an earlier error.)
     *
     */
    public abstract TypeInfo getElementTypeInfo();

    /**
     * Returns the immutable {@link TypeInfo} object for the specified
     * attribute of the current element.
     *
     * <p>
     * The method may only be called by the startElement event of
     * the {@link org.xml.sax.ContentHandler} that the application sets to the
     * {@link ValidatorHandler}.
     *
     * @param index
     *      The index of the attribute. The same index for
     *      the {@link org.xml.sax.Attributes} object passed to the
     *      <tt>startElement</tt> callback.
     *
     * @throws IndexOutOfBoundsException
     *      If the index is invalid.
     * @throws IllegalStateException
     *      If this method is called from other {@link org.xml.sax.ContentHandler}
     *      methods.
     *
     * @return
     *      An immutable {@link TypeInfo} object that represents the
     *      type of the specified attribute.
     *      Note that the caller can keep references to the obtained
     *      {@link TypeInfo} longer than the callback scope.
     *
     *      Otherwise, this method returns
     *      null if the validator is unable to
     *      determine the type.
     */
    public abstract TypeInfo getAttributeTypeInfo(int index);

    /**
     * Returns <tt>true</tt> if the specified attribute is determined
     * to be ID.
     *
     * <p>
     * Exactly how an attribute is "determined to be ID" is up to the
     * schema language. In case of W3C XML Schema, this means
     * that the actual type of the attribute is the built-in ID type
     * or its derived type.
     *
     * <p>
     * A {@link javax.xml.parsers.DocumentBuilder} uses this information
     * to properly implement {@link org.w3c.dom.Attr#isId()}.
     *
     * <p>
     * The method may only be called by the startElement event of
     * the {@link org.xml.sax.ContentHandler} that the application sets to the
     * {@link ValidatorHandler}.
     *
     * @param index
     *      The index of the attribute. The same index for
     *      the {@link org.xml.sax.Attributes} object passed to the
     *      <tt>startElement</tt> callback.
     *
     * @throws IndexOutOfBoundsException
     *      If the index is invalid.
     * @throws IllegalStateException
     *      If this method is called from other {@link org.xml.sax.ContentHandler}
     *      methods.
     *
     * @return true
     *      if the type of the specified attribute is ID.
     */
    public abstract boolean isIdAttribute(int index);

    /**
     * Returns <tt>false</tt> if the attribute was added by the validator.
     *
     * <p>
     * This method provides information necessary for
     * a {@link javax.xml.parsers.DocumentBuilder} to determine what
     * the DOM tree should return from the {@link org.w3c.dom.Attr#getSpecified()} method.
     *
     * <p>
     * The method may only be called by the startElement event of
     * the {@link org.xml.sax.ContentHandler} that the application sets to the
     * {@link ValidatorHandler}.
     *
     * <p>
     * A general guideline for validators is to return true if
     * the attribute was originally present in the pipeline, and
     * false if it was added by the validator.
     *
     * @param index
     *      The index of the attribute. The same index for
     *      the {@link org.xml.sax.Attributes} object passed to the
     *      <tt>startElement</tt> callback.
     *
     * @throws IndexOutOfBoundsException
     *      If the index is invalid.
     * @throws IllegalStateException
     *      If this method is called from other {@link org.xml.sax.ContentHandler}
     *      methods.
     *
     * @return
     *      <tt>true</tt> if the attribute was present before the validator
     *      processes input. <tt>false</tt> if the attribute was added
     *      by the validator.
     */
    public abstract boolean isSpecified(int index);
}
