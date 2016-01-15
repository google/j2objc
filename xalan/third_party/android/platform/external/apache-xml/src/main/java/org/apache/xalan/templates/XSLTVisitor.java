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
 * $Id: XSLTVisitor.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xpath.XPathVisitor;

/**
 * A derivation from this class can be passed to a class that implements 
 * the XSLTVisitable interface, to have the appropriate method called 
 * for each component of an XSLT stylesheet.  Aside from possible other uses,
 * the main intention is to provide a reasonable means to perform expression 
 * rewriting.
 */
public class XSLTVisitor extends XPathVisitor
{
	/**
	 * Visit an XSLT instruction.  Any element that isn't called by one 
	 * of the other visit methods, will be called by this method.
	 * 
	 * @param elem The xsl instruction element object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitInstruction(ElemTemplateElement elem)
	{
		return true;
	}
	
	/**
	 * Visit an XSLT stylesheet instruction.
	 * 
	 * @param elem The xsl instruction element object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitStylesheet(ElemTemplateElement elem)
	{
		return true;
	}

	
	/**
	 * Visit an XSLT top-level instruction.
	 * 
	 * @param elem The xsl instruction element object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitTopLevelInstruction(ElemTemplateElement elem)
	{
		return true;
	}
	
	/**
	 * Visit an XSLT top-level instruction.
	 * 
	 * @param elem The xsl instruction element object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitTopLevelVariableOrParamDecl(ElemTemplateElement elem)
	{
		return true;
	}

	
	/**
	 * Visit an XSLT variable or parameter declaration.
	 * 
	 * @param elem The xsl instruction element object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitVariableOrParamDecl(ElemVariable elem)
	{
		return true;
	}
	
	/**
	 * Visit a LiteralResultElement.
	 * 
	 * @param elem The literal result object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitLiteralResultElement(ElemLiteralResult elem)
	{
		return true;
	}
	
	/**
	 * Visit an Attribute Value Template (at the top level).
	 * 
	 * @param elem The attribute value template object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitAVT(AVT elem)
	{
		return true;
	}


	/**
	 * Visit an extension element.
	 * @param elem The extension object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitExtensionElement(ElemExtensionCall elem)
	{
		return true;
	}

}

