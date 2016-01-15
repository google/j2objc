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
 * $Id: AbsPathChecker.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.functions.FuncCurrent;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.functions.Function;
import org.apache.xpath.operations.Variable;

/**
 * This class runs over a path expression that is assumed to be absolute, and 
 * checks for variables and the like that may make it context dependent.
 */
public class AbsPathChecker extends XPathVisitor
{
	private boolean m_isAbs = true;
	
	/**
	 * Process the LocPathIterator to see if it contains variables 
	 * or functions that may make it context dependent.
	 * @param path LocPathIterator that is assumed to be absolute, but needs checking.
	 * @return true if the path is confirmed to be absolute, false if it 
	 * may contain context dependencies.
	 */
	public boolean checkAbsolute(LocPathIterator path)
	{
		m_isAbs = true;
		path.callVisitors(null, this);
		return m_isAbs;
	}
	
	/**
	 * Visit a function.
	 * @param owner The owner of the expression, to which the expression can 
	 *              be reset if rewriting takes place.
	 * @param func The function reference object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitFunction(ExpressionOwner owner, Function func)
	{
		if((func instanceof FuncCurrent) ||
		   (func instanceof FuncExtFunction))
			m_isAbs = false;
		return true;
	}
	
	/**
	 * Visit a variable reference.
	 * @param owner The owner of the expression, to which the expression can 
	 *              be reset if rewriting takes place.
	 * @param var The variable reference object.
	 * @return true if the sub expressions should be traversed.
	 */
	public boolean visitVariableRef(ExpressionOwner owner, Variable var)
	{
		m_isAbs = false;
		return true;
	}
}

