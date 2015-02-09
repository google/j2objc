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

// $Id: Templates.java 570103 2007-08-27 13:24:55Z mrglavas $

package javax.xml.transform;

import java.util.Properties;

/**
 * An object that implements this interface is the runtime representation of processed
 * transformation instructions.
 *
 * <p>Templates must be thread-safe for a given instance
 * over multiple threads running concurrently, and may
 * be used multiple times in a given session.</p>
 */
public interface Templates {

    /**
     * Create a new transformation context for this Templates object.
     *
     * @return A valid non-null instance of a Transformer.
     *
     * @throws TransformerConfigurationException if a Transformer can not be created.
     */
    Transformer newTransformer() throws TransformerConfigurationException;

    /**
     * Get the properties corresponding to the effective xsl:output element.
     * The object returned will
     * be a clone of the internal values. Accordingly, it can be mutated
     * without mutating the Templates object, and then handed in to
     * {@link javax.xml.transform.Transformer#setOutputProperties}.
     *
     * <p>The properties returned should contain properties set by the stylesheet,
     * and these properties are "defaulted" by default properties specified by
     * <a href="http://www.w3.org/TR/xslt#output">section 16 of the
     * XSL Transformations (XSLT) W3C Recommendation</a>.  The properties that
     * were specifically set by the stylesheet should be in the base
     * Properties list, while the XSLT default properties that were not
     * specifically set should be in the "default" Properties list.  Thus,
     * getOutputProperties().getProperty(String key) will obtain any
     * property in that was set by the stylesheet, <em>or</em> the default
     * properties, while
     * getOutputProperties().get(String key) will only retrieve properties
     * that were explicitly set in the stylesheet.</p>
     *
     * <p>For XSLT,
     * <a href="http://www.w3.org/TR/xslt#attribute-value-templates">Attribute
     * Value Templates</a> attribute values will
     * be returned unexpanded (since there is no context at this point).  The
     * namespace prefixes inside Attribute Value Templates will be unexpanded,
     * so that they remain valid XPath values.</p>
     *
     * @return A Properties object, never null.
     */
    Properties getOutputProperties();
}
