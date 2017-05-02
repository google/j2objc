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

// $Id: DOMLocator.java 446598 2006-09-15 12:55:40Z jeremias $

package javax.xml.transform.dom;

import javax.xml.transform.SourceLocator;
import org.w3c.dom.Node;


/**
 * Indicates the position of a node in a source DOM, intended
 * primarily for error reporting.  To use a DOMLocator, the receiver of an
 * error must downcast the {@link javax.xml.transform.SourceLocator}
 * object returned by an exception. A {@link javax.xml.transform.Transformer}
 * may use this object for purposes other than error reporting, for instance,
 * to indicate the source node that originated a result node.
 */
public interface DOMLocator extends SourceLocator {

    /**
     * Return the node where the event occurred.
     *
     * @return The node that is the location for the event.
     */
    public Node getOriginatingNode();
}

