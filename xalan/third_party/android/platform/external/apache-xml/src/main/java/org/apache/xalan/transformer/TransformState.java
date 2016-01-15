/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: TransformState.java 468645 2006-10-28 06:57:24Z minchau $
 */
package org.apache.xalan.transformer;

import javax.xml.transform.Transformer;

import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xml.serializer.TransformStateSetter;

import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;

/**
 * This interface is meant to be used by a consumer of
 * SAX2 events produced by Xalan, and enables the consumer
 * to get information about the state of the transform.  It
 * is primarily intended as a tooling interface.  A content
 * handler can get a reference to a TransformState by implementing
 * the TransformerClient interface.  Xalan will check for
 * that interface before it calls startDocument, and, if it
 * is implemented, pass in a TransformState reference to the
 * setTransformState method.
 *
 * <p>Note that the current stylesheet and root stylesheet can
 * be retrieved from the ElemTemplateElement obtained from
 * either getCurrentElement() or getCurrentTemplate().</p>
 * 
 * This interface contains only getter methods, any setters are in the interface
 * TransformStateSetter which this interface extends.
 * 
 * @see org.apache.xml.serializer.TransformStateSetter
 */
public interface TransformState extends TransformStateSetter
{

  /**
   * Retrieves the stylesheet element that produced
   * the SAX event.
   *
   * <p>Please note that the ElemTemplateElement returned may
   * be in a default template, and thus may not be
   * defined in the stylesheet.</p>
   *
   * @return the stylesheet element that produced the SAX event.
   */
  ElemTemplateElement getCurrentElement();

  /**
   * This method retrieves the current context node
   * in the source tree.
   *
   * @return the current context node in the source tree.
   */
  Node getCurrentNode();

  /**
   * This method retrieves the xsl:template
   * that is in effect, which may be a matched template
   * or a named template.
   *
   * <p>Please note that the ElemTemplate returned may
   * be a default template, and thus may not have a template
   * defined in the stylesheet.</p>
   *
   * @return the xsl:template that is in effect
   */
  ElemTemplate getCurrentTemplate();

  /**
   * This method retrieves the xsl:template
   * that was matched.  Note that this may not be
   * the same thing as the current template (which
   * may be from getCurrentElement()), since a named
   * template may be in effect.
   *
   * <p>Please note that the ElemTemplate returned may
   * be a default template, and thus may not have a template
   * defined in the stylesheet.</p>
   *
   * @return the xsl:template that was matched.
   */
  ElemTemplate getMatchedTemplate();

  /**
   * Retrieves the node in the source tree that matched
   * the template obtained via getMatchedTemplate().
   *
   * @return the node in the source tree that matched
   * the template obtained via getMatchedTemplate().
   */
  Node getMatchedNode();

  /**
   * Get the current context node list.
   *
   * @return the current context node list.
   */
  NodeIterator getContextNodeList();

  /**
   * Get the TrAX Transformer object in effect.
   *
   * @return the TrAX Transformer object in effect.
   */
  Transformer getTransformer();
  

    
}
