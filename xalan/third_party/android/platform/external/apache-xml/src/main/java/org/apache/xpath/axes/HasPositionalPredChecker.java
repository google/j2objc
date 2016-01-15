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
 * $Id: HasPositionalPredChecker.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath.axes;

import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.functions.FuncLast;
import org.apache.xpath.functions.FuncPosition;
import org.apache.xpath.functions.Function;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.operations.Div;
import org.apache.xpath.operations.Minus;
import org.apache.xpath.operations.Mod;
import org.apache.xpath.operations.Mult;
import org.apache.xpath.operations.Plus;
import org.apache.xpath.operations.Quo;
import org.apache.xpath.operations.Variable;

public class HasPositionalPredChecker extends XPathVisitor
{
	private boolean m_hasPositionalPred = false;
	private int m_predDepth = 0;
	
	/**
	 * Process the LocPathIterator to see if it contains variables 
	 * or functions that may make it context dependent.
	 * @param path LocPathIterator that is assumed to be absolute, but needs checking.
	 * @return true if the path is confirmed to be absolute, false if it 
	 * may contain context dependencies.
	 */
	public static boolean check(LocPathIterator path)
	{
		HasPositionalPredChecker hppc = new HasPositionalPredChecker();
		path.callVisitors(null, hppc);
		return hppc.m_hasPositionalPred;
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
		if((func instanceof FuncPosition) ||
		   (func instanceof FuncLast))
			m_hasPositionalPred = true;
		return true;
	}
	
//	/**
//	 * Visit a variable reference.
//	 * @param owner The owner of the expression, to which the expression can 
//	 *              be reset if rewriting takes place.
//	 * @param var The variable reference object.
//	 * @return true if the sub expressions should be traversed.
//	 */
//	public boolean visitVariableRef(ExpressionOwner owner, Variable var)
//	{
//		m_hasPositionalPred = true;
//		return true;
//	}
	
  /**
   * Visit a predicate within a location path.  Note that there isn't a 
   * proper unique component for predicates, and that the expression will 
   * be called also for whatever type Expression is.
   * 
   * @param owner The owner of the expression, to which the expression can 
   *              be reset if rewriting takes place.
   * @param pred The predicate object.
   * @return true if the sub expressions should be traversed.
   */
  public boolean visitPredicate(ExpressionOwner owner, Expression pred)
  {
    m_predDepth++;

    if(m_predDepth == 1)
    {
      if((pred instanceof Variable) || 
         (pred instanceof XNumber) ||
         (pred instanceof Div) ||
         (pred instanceof Plus) ||
         (pred instanceof Minus) ||
         (pred instanceof Mod) ||
         (pred instanceof Quo) ||
         (pred instanceof Mult) ||
         (pred instanceof org.apache.xpath.operations.Number) ||
         (pred instanceof Function))
          m_hasPositionalPred = true;
      else
      	pred.callVisitors(owner, this);
    }

    m_predDepth--;

    // Don't go have the caller go any further down the subtree.
    return false;
  }


}

