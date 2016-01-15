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
 * $Id: ProcessorExsltFuncResult.java 468640 2006-10-28 06:53:53Z minchau $
 */
package org.apache.xalan.processor;

import org.apache.xalan.templates.ElemExsltFuncResult;
import org.apache.xalan.templates.ElemExsltFunction;
import org.apache.xalan.templates.ElemParam;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.ElemVariable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class processes parse events for an exslt func:result element.
 * @xsl.usage internal
 */
public class ProcessorExsltFuncResult extends ProcessorTemplateElem
{
    static final long serialVersionUID = 6451230911473482423L;
  
  /**
   * Verify that the func:result element does not appear within a variable,
   * parameter, or another func:result, and that it belongs to a func:function 
   * element.
   */
  public void startElement(
          StylesheetHandler handler, String uri, String localName, String rawName, Attributes attributes)
            throws SAXException
  {
    String msg = "";

    super.startElement(handler, uri, localName, rawName, attributes);
    ElemTemplateElement ancestor = handler.getElemTemplateElement().getParentElem();
    while (ancestor != null && !(ancestor instanceof ElemExsltFunction))
    {
      if (ancestor instanceof ElemVariable 
          || ancestor instanceof ElemParam
          || ancestor instanceof ElemExsltFuncResult)
      {
        msg = "func:result cannot appear within a variable, parameter, or another func:result.";
        handler.error(msg, new SAXException(msg));
      }
      ancestor = ancestor.getParentElem();
    }
    if (ancestor == null)
    {
      msg = "func:result must appear in a func:function element";
      handler.error(msg, new SAXException(msg));
    }
  }
}
