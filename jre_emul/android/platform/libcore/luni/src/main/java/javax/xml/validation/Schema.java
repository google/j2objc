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

// $Id: Schema.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.validation;

/**
 * Immutable in-memory representation of grammar.
 *
 * <p>
 * This object represents a set of constraints that can be checked/
 * enforced against an XML document.
 *
 * <p>
 * A {@link Schema} object is thread safe and applications are
 * encouraged to share it across many parsers in many threads.
 *
 * <p>
 * A {@link Schema} object is immutable in the sense that it shouldn't
 * change the set of constraints once it is created. In other words,
 * if an application validates the same document twice against the same
 * {@link Schema}, it must always produce the same result.
 *
 * <p>
 * A {@link Schema} object is usually created from {@link SchemaFactory}.
 *
 * <p>
 * Two kinds of validators can be created from a {@link Schema} object.
 * One is {@link Validator}, which provides highly-level validation
 * operations that cover typical use cases. The other is
 * {@link ValidatorHandler}, which works on top of SAX for better
 * modularity.
 *
 * <p>
 * This specification does not refine
 * the {@link java.lang.Object#equals(java.lang.Object)} method.
 * In other words, if you parse the same schema twice, you may
 * still get <code>!schemaA.equals(schemaB)</code>.
 *
 * @author <a href="mailto:Kohsuke.Kawaguchi@Sun.com">Kohsuke Kawaguchi</a>
 * @version $Revision: 446598 $, $Date: 2006-09-15 05:55:40 -0700 (Fri, 15 Sep 2006) $
 * @see <a href="http://www.w3.org/TR/xmlschema-1/">XML Schema Part 1: Structures</a>
 * @see <a href="http://www.w3.org/TR/xml11/">Extensible Markup Language (XML) 1.1</a>
 * @see <a href="http://www.w3.org/TR/REC-xml">Extensible Markup Language (XML) 1.0 (Second Edition)</a>
 * @since 1.5
 */
public abstract class Schema {

    /**
     * Constructor for the derived class.
     *
     * <p>
     * The constructor does nothing.
     */
    protected Schema() {
    }

    /**
     * Creates a new {@link Validator} for this {@link Schema}.
     *
     * <p>
     * A validator enforces/checks the set of constraints this object
     * represents.
     *
     * @return
     *      Always return a non-null valid object.
     */
    public abstract Validator newValidator();

    /**
     * Creates a new {@link ValidatorHandler} for this {@link Schema}.
     *
     * @return
     *      Always return a non-null valid object.
     */
    public abstract ValidatorHandler newValidatorHandler();
}
