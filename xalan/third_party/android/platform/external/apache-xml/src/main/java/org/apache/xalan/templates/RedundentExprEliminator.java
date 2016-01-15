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
 * $Id: RedundentExprEliminator.java 468643 2006-10-28 06:56:03Z minchau $
 */
package org.apache.xalan.templates;

import java.util.Vector;

import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xml.utils.QName;
import org.apache.xml.utils.WrappedRuntimeException;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPath;
import org.apache.xpath.axes.AxesWalker;
import org.apache.xpath.axes.FilterExprIteratorSimple;
import org.apache.xpath.axes.FilterExprWalker;
import org.apache.xpath.axes.LocPathIterator;
import org.apache.xpath.axes.SelfIteratorNoPredicate;
import org.apache.xpath.axes.WalkerFactory;
import org.apache.xpath.axes.WalkingIterator;
import org.apache.xpath.operations.Variable;
import org.apache.xpath.operations.VariableSafeAbsRef;

/**
 * This class eleminates redundent XPaths from a given subtree, 
 * and also collects all absolute paths within the subtree.  First 
 * it must be called as a visitor to the subtree, and then 
 * eleminateRedundent must be called.
 */
public class RedundentExprEliminator extends XSLTVisitor
{
  Vector m_paths;
  Vector m_absPaths;
  boolean m_isSameContext;
  AbsPathChecker m_absPathChecker = new AbsPathChecker();
  
  private static int m_uniquePseudoVarID = 1;
  static final String PSUEDOVARNAMESPACE = Constants.S_VENDORURL+"/xalan/psuedovar";
 
  public static final boolean DEBUG = false;
  public static final boolean DIAGNOSE_NUM_PATHS_REDUCED = false;
  public static final boolean DIAGNOSE_MULTISTEPLIST = false;

  /**
   * So we can reuse it over and over again.
   */
  VarNameCollector m_varNameCollector = new VarNameCollector();

  /**
   * Construct a RedundentExprEliminator.
   */
  public RedundentExprEliminator()
  {
    m_isSameContext = true;
    m_absPaths = new Vector();
    m_paths = null;
  }
  
  
  /**
   * Method to be called after the all expressions within an  
   * node context have been visited.  It eliminates redundent 
   * expressions by creating a variable in the psuedoVarRecipient 
   * for each redundent expression, and then rewriting the redundent 
   * expression to be a variable reference.
   * 
   * @param psuedoVarRecipient The recipient of the psuedo vars.  The 
   * variables will be inserted as first children of the element, before 
   * any existing variables.
   */
  public void eleminateRedundentLocals(ElemTemplateElement psuedoVarRecipient)
  {
    eleminateRedundent(psuedoVarRecipient, m_paths);
  }
  
  /**
   * Method to be called after the all global expressions within a stylesheet 
   * have been collected.  It eliminates redundent 
   * expressions by creating a variable in the psuedoVarRecipient 
   * for each redundent expression, and then rewriting the redundent 
   * expression to be a variable reference.
   * 
   */
  public void eleminateRedundentGlobals(StylesheetRoot stylesheet)
  {
    eleminateRedundent(stylesheet, m_absPaths);
  }

  
  /**
   * Method to be called after the all expressions within an  
   * node context have been visited.  It eliminates redundent 
   * expressions by creating a variable in the psuedoVarRecipient 
   * for each redundent expression, and then rewriting the redundent 
   * expression to be a variable reference.
   * 
   * @param psuedoVarRecipient The owner of the subtree from where the 
   *                           paths were collected.
   * @param paths A vector of paths that hold ExpressionOwner objects, 
   *              which must yield LocationPathIterators.
   */
  protected void eleminateRedundent(ElemTemplateElement psuedoVarRecipient, Vector paths)
  {
    int n = paths.size();
    int numPathsEliminated = 0;
    int numUniquePathsEliminated = 0;
    for (int i = 0; i < n; i++)
    {
      ExpressionOwner owner = (ExpressionOwner) paths.elementAt(i);
      if (null != owner)
      {
        int found = findAndEliminateRedundant(i + 1, i, owner, psuedoVarRecipient, paths);
        if (found > 0)
                  numUniquePathsEliminated++;
        numPathsEliminated += found;
      }
    }
    
    eleminateSharedPartialPaths(psuedoVarRecipient, paths);
    
    if(DIAGNOSE_NUM_PATHS_REDUCED)
		diagnoseNumPaths(paths, numPathsEliminated, numUniquePathsEliminated);
  }
  
  /**
   * Eliminate the shared partial paths in the expression list.
   * 
   * @param psuedoVarRecipient The recipient of the psuedo vars.
   * 
   * @param paths A vector of paths that hold ExpressionOwner objects, 
   *              which must yield LocationPathIterators.
   */
  protected void eleminateSharedPartialPaths(ElemTemplateElement psuedoVarRecipient, Vector paths)
  {
  	MultistepExprHolder list = createMultistepExprList(paths);
  	if(null != list)
  	{
  		if(DIAGNOSE_MULTISTEPLIST)
        	list.diagnose();
        	
        boolean isGlobal = (paths == m_absPaths);
        	
        // Iterate over the list, starting with the most number of paths, 
        // trying to find the longest matches first.
        int longestStepsCount = list.m_stepCount;
    	for (int i = longestStepsCount-1; i >= 1; i--)
    	{
    		MultistepExprHolder next = list;
        	while(null != next)
        	{
        		if(next.m_stepCount < i)
        			break;
				list = matchAndEliminatePartialPaths(next, list, isGlobal, i, psuedoVarRecipient);
				next = next.m_next;
        	}
    	}
  	}
  }

  /**
   * For a given path, see if there are any partitial matches in the list, 
   * and, if there are, replace those partial paths with psuedo variable refs,
   * and create the psuedo variable decl.
   * 
   * @return The head of the list, which may have changed.
   */
  protected MultistepExprHolder matchAndEliminatePartialPaths(MultistepExprHolder testee, 
                                               MultistepExprHolder head,
                                               boolean isGlobal,
                                               int lengthToTest,
                                               ElemTemplateElement varScope)
  {  	
  	if(null == testee.m_exprOwner)
  		return head;
  		
    // Start with the longest possible match, and move down.
    WalkingIterator iter1 = (WalkingIterator) testee.m_exprOwner.getExpression();
    if(partialIsVariable(testee, lengthToTest))
    	return head;
    MultistepExprHolder matchedPaths = null;
    MultistepExprHolder matchedPathsTail = null;
    MultistepExprHolder meh = head;
    while( null != meh)
    {
      if ((meh != testee) && (null != meh.m_exprOwner))
      {
	      WalkingIterator iter2 = (WalkingIterator) meh.m_exprOwner.getExpression();
	      if (stepsEqual(iter1, iter2, lengthToTest))
	      {
	        if (null == matchedPaths)
	        {
	          try
	          {
	          	matchedPaths = (MultistepExprHolder)testee.clone();
	          	testee.m_exprOwner = null; // So it won't be processed again.
	          }
	          catch(CloneNotSupportedException cnse){}
	          matchedPathsTail = matchedPaths;
	          matchedPathsTail.m_next = null;
	        }
	       
	        try
	        {
	          matchedPathsTail.m_next = (MultistepExprHolder)meh.clone();
	          meh.m_exprOwner = null; // So it won't be processed again.
	        }
	        catch(CloneNotSupportedException cnse){}
	        matchedPathsTail = matchedPathsTail.m_next;
	        matchedPathsTail.m_next = null;
	      }
      }
      meh = meh.m_next;
    }
    	
	int matchCount = 0;
	if(null != matchedPaths)
	{
		ElemTemplateElement root = isGlobal ? varScope : findCommonAncestor(matchedPaths);
		WalkingIterator sharedIter = (WalkingIterator)matchedPaths.m_exprOwner.getExpression();
		WalkingIterator newIter = createIteratorFromSteps(sharedIter, lengthToTest);
		ElemVariable var = createPseudoVarDecl(root, newIter, isGlobal);
		if(DIAGNOSE_MULTISTEPLIST)
			System.err.println("Created var: "+var.getName()+(isGlobal ? "(Global)" : ""));
		while(null != matchedPaths)
		{
			ExpressionOwner owner = matchedPaths.m_exprOwner;
			WalkingIterator iter = (WalkingIterator)owner.getExpression();
			
			if(DIAGNOSE_MULTISTEPLIST)
				diagnoseLineNumber(iter);
			
			LocPathIterator newIter2 = 
			    changePartToRef(var.getName(), iter, lengthToTest, isGlobal);
			owner.setExpression(newIter2);
			
			matchedPaths = matchedPaths.m_next;
		}
	}
	
	if(DIAGNOSE_MULTISTEPLIST)
		diagnoseMultistepList(matchCount, lengthToTest, isGlobal);
    return head;
  }
  
  /**
   * Check if results of partial reduction will just be a variable, in 
   * which case, skip it.
   */
  boolean partialIsVariable(MultistepExprHolder testee, int lengthToTest)
  {
  	if(1 == lengthToTest)
  	{
  		WalkingIterator wi = (WalkingIterator)testee.m_exprOwner.getExpression();
  		if(wi.getFirstWalker() instanceof FilterExprWalker)
  			return true;
  	}
  	return false;
  }

  /**
   * Tell what line number belongs to a given expression.
   */
  protected void diagnoseLineNumber(Expression expr)
  {
    ElemTemplateElement e = getElemFromExpression(expr);
    System.err.println("   " + e.getSystemId() + " Line " + e.getLineNumber());
  }
      
  /**
   * Given a linked list of expressions, find the common ancestor that is 
   * suitable for holding a psuedo variable for shared access.
   */
  protected ElemTemplateElement findCommonAncestor(MultistepExprHolder head)
  {
  	// Not sure this algorithm is the best, but will do for the moment.
  	int numExprs = head.getLength();
  	// The following could be made cheaper by pre-allocating large arrays, 
  	// but then we would have to assume a max number of reductions, 
  	// which I am not inclined to do right now.
    ElemTemplateElement[] elems = new ElemTemplateElement[numExprs];
    int[] ancestorCounts = new int[numExprs];
    
    // Loop through, getting the parent elements, and counting the 
    // ancestors.
  	MultistepExprHolder next = head;
  	int shortestAncestorCount = 10000;
  	for(int i = 0; i < numExprs; i++)
  	{
  		ElemTemplateElement elem = 
  			getElemFromExpression(next.m_exprOwner.getExpression());
  		elems[i] = elem;
  		int numAncestors = countAncestors(elem);
  		ancestorCounts[i] = numAncestors;
  		if(numAncestors < shortestAncestorCount)
  		{
  			shortestAncestorCount = numAncestors;
  		}
  		next = next.m_next;
  	}
  	
  	// Now loop through and "correct" the elements that have more ancestors.
  	for(int i = 0; i < numExprs; i++)
  	{
  		if(ancestorCounts[i] > shortestAncestorCount)
  		{
  			int numStepCorrection = ancestorCounts[i] - shortestAncestorCount;
  			for(int j = 0; j < numStepCorrection; j++)
  			{
  				elems[i] = elems[i].getParentElem();
  			}
  		}
  	}
  	
  	// Now everyone has an equal number of ancestors.  Walk up from here 
  	// equally until all are equal.
  	ElemTemplateElement first = null;
  	while(shortestAncestorCount-- >= 0)
  	{
  		boolean areEqual = true;
  		first = elems[0];
  		for(int i = 1; i < numExprs; i++)
  		{
  			if(first != elems[i])
  			{
  				areEqual = false;
  				break;
  			}
  		}
  		// This second check is to make sure we have a common ancestor that is not the same 
  		// as the expression owner... i.e. the var decl has to go above the expression owner.
  		if(areEqual && isNotSameAsOwner(head, first) && first.canAcceptVariables())
  		{
  			if(DIAGNOSE_MULTISTEPLIST)
  			{
  				System.err.print(first.getClass().getName());
  				System.err.println(" at   " + first.getSystemId() + " Line " + first.getLineNumber());
  			}
  			return first;
  		}
   			
  		for(int i = 0; i < numExprs; i++)
  		{
  			elems[i] = elems[i].getParentElem();
  		}
  	}
  	
  	assertion(false, "Could not find common ancestor!!!");
  	return null;
  }
  
  /**
   * Find out if the given ElemTemplateElement is not the same as one of 
   * the ElemTemplateElement owners of the expressions.
   * 
   * @param head Head of linked list of expression owners.
   * @param ete The ElemTemplateElement that is a candidate for a psuedo 
   * variable parent.
   * @return true if the given ElemTemplateElement is not the same as one of 
   * the ElemTemplateElement owners of the expressions.  This is to make sure 
   * we find an ElemTemplateElement that is in a viable position to hold 
   * psuedo variables that are visible to the references.
   */
  protected boolean isNotSameAsOwner(MultistepExprHolder head, ElemTemplateElement ete)
  {
  	MultistepExprHolder next = head;
  	while(null != next)
  	{
  		ElemTemplateElement elemOwner = getElemFromExpression(next.m_exprOwner.getExpression());
  		if(elemOwner == ete)
  			return false;
  		next = next.m_next;
  	}
  	return true;
  }
  
  /**
   * Count the number of ancestors that a ElemTemplateElement has.
   * 
   * @param elem An representation of an element in an XSLT stylesheet.
   * @return The number of ancestors of elem (including the element itself).
   */
  protected int countAncestors(ElemTemplateElement elem)
  {
  	int count = 0;
  	while(null != elem)
  	{
  		count++;
  		elem = elem.getParentElem();
  	}
  	return count;
  }

  /**
   * Print out diagnostics about partial multistep evaluation.
   */
  protected void diagnoseMultistepList(
      int matchCount,
      int lengthToTest,
      boolean isGlobal)
  {
      if (matchCount > 0)
        {
        System.err.print(
          "Found multistep matches: " + matchCount + ", " + lengthToTest + " length");
        if (isGlobal)
              System.err.println(" (global)");
        else
              System.err.println();
      }
  }
  
  /**
   * Change a given number of steps to a single variable reference.
   * 
   * @param uniquePseudoVarName The name of the variable reference.
   * @param wi The walking iterator that is to be changed.
   * @param numSteps The number of steps to be changed.
   * @param isGlobal true if this will be a global reference.
   */
  protected LocPathIterator changePartToRef(final QName uniquePseudoVarName, WalkingIterator wi, 
                                 final int numSteps, final boolean isGlobal)
  {
  	Variable var = new Variable();
  	var.setQName(uniquePseudoVarName);
  	var.setIsGlobal(isGlobal);
  	if(isGlobal)
  	{	ElemTemplateElement elem = getElemFromExpression(wi);
  		StylesheetRoot root = elem.getStylesheetRoot();
  		Vector vars = root.getVariablesAndParamsComposed();
  		var.setIndex(vars.size()-1);
  	}
  	
  	// Walk to the first walker after the one's we are replacing.
  	AxesWalker walker = wi.getFirstWalker();
  	for(int i = 0; i < numSteps; i++)
  	{
  		assertion(null != walker, "Walker should not be null!");
  		walker = walker.getNextWalker();
  	}
  	
  	if(null != walker)
  	{
  	
  	  FilterExprWalker few = new FilterExprWalker(wi);
  	  few.setInnerExpression(var);
  	  few.exprSetParent(wi);
  	  few.setNextWalker(walker);
  	  walker.setPrevWalker(few);
  	  wi.setFirstWalker(few);
  	  return wi;
  	}
  	else
  	{
  	  FilterExprIteratorSimple feis = new FilterExprIteratorSimple(var);
  	  feis.exprSetParent(wi.exprGetParent());
  	  return feis;
  	}
  }
  
  /**
   * Create a new WalkingIterator from the steps in another WalkingIterator.
   * 
   * @param wi The iterator from where the steps will be taken.
   * @param numSteps The number of steps from the first to copy into the new 
   *                 iterator.
   * @return The new iterator.
   */
  protected WalkingIterator createIteratorFromSteps(final WalkingIterator wi, int numSteps)
  {
  	WalkingIterator newIter = new WalkingIterator(wi.getPrefixResolver());
  	try
  	{
  		AxesWalker walker = (AxesWalker)wi.getFirstWalker().clone();
  		newIter.setFirstWalker(walker);
  		walker.setLocPathIterator(newIter);
  		for(int i = 1; i < numSteps; i++)
  		{
  			AxesWalker next = (AxesWalker)walker.getNextWalker().clone();
  			walker.setNextWalker(next);
  			next.setLocPathIterator(newIter);
  			walker = next;
  		}
  		walker.setNextWalker(null);
  	}
  	catch(CloneNotSupportedException cnse)
  	{
  		throw new WrappedRuntimeException(cnse);
  	}
  	return newIter;
  }
    
  /**
   * Compare a given number of steps between two iterators, to see if they are equal.
   * 
   * @param iter1 The first iterator to compare.
   * @param iter2 The second iterator to compare.
   * @param numSteps The number of steps to compare.
   * @return true If the given number of steps are equal.
   * 
   */
  protected boolean stepsEqual(WalkingIterator iter1, WalkingIterator iter2, 
                                         int numSteps)
  {
  	AxesWalker aw1 = iter1.getFirstWalker();
  	AxesWalker aw2 = iter2.getFirstWalker();
  	
  	for(int i = 0; (i < numSteps); i++)
  	{
  		if((null == aw1) || (null == aw2))
  		 	return false;
  		 	
  		if(!aw1.deepEquals(aw2))
  			return false;
  		
  		aw1 = aw1.getNextWalker();
  		aw2 = aw2.getNextWalker();
  	}
  	
  	assertion((null != aw1) || (null != aw2), "Total match is incorrect!");
  	
  	return true;
  }
  
  /**
   * For the reduction of location path parts, create a list of all 
   * the multistep paths with more than one step, sorted by the 
   * number of steps, with the most steps occuring earlier in the list.
   * If the list is only one member, don't bother returning it.
   * 
   * @param paths Vector of ExpressionOwner objects, which may contain null entries. 
   *              The ExpressionOwner objects must own LocPathIterator objects.
   * @return null if no multipart paths are found or the list is only of length 1, 
   * otherwise the first MultistepExprHolder in a linked list of these objects.
   */
  protected MultistepExprHolder createMultistepExprList(Vector paths)
  {
  	MultistepExprHolder first = null;
  	int n = paths.size();
  	for(int i = 0; i < n; i++)
  	{
  		ExpressionOwner eo = (ExpressionOwner)paths.elementAt(i);
  		if(null == eo)
  			continue;
  			
  		// Assuming location path iterators should be OK.
  		LocPathIterator lpi = (LocPathIterator)eo.getExpression();
  		int numPaths = countSteps(lpi);
  		if(numPaths > 1)
  		{
  			if(null == first)
  				first = new MultistepExprHolder(eo, numPaths, null);
  			else
  				first = first.addInSortedOrder(eo, numPaths);
  		}
  	}
  	
  	if((null == first) || (first.getLength() <= 1))
  		return null;
  	else
  		return first;
  }
  
  /**
   * Look through the vector from start point, looking for redundant occurances.
   * When one or more are found, create a psuedo variable declaration, insert 
   * it into the stylesheet, and replace the occurance with a reference to 
   * the psuedo variable.  When a redundent variable is found, it's slot in 
   * the vector will be replaced by null.
   * 
   * @param start The position to start looking in the vector.
   * @param firstOccuranceIndex The position of firstOccuranceOwner.
   * @param firstOccuranceOwner The owner of the expression we are looking for.
   * @param psuedoVarRecipient Where to put the psuedo variables.
   * 
   * @return The number of expression occurances that were modified.
   */
  protected int findAndEliminateRedundant(int start, int firstOccuranceIndex,
                         ExpressionOwner firstOccuranceOwner, 
                         ElemTemplateElement psuedoVarRecipient,
                         Vector paths) 
                 throws org.w3c.dom.DOMException 
  {
	MultistepExprHolder head = null;
	MultistepExprHolder tail = null;
	int numPathsFound = 0;
	int n = paths.size();
	
	Expression expr1 = firstOccuranceOwner.getExpression();
	if(DEBUG)
		assertIsLocPathIterator(expr1, firstOccuranceOwner);
	boolean isGlobal = (paths == m_absPaths);
	LocPathIterator lpi = (LocPathIterator)expr1;
	int stepCount = countSteps(lpi);
	for(int j = start; j < n; j++)
	{
		ExpressionOwner owner2 = (ExpressionOwner)paths.elementAt(j);
		if(null != owner2)
		{
			Expression expr2 = owner2.getExpression();
			boolean isEqual = expr2.deepEquals(lpi);
			if(isEqual)
			{  		
				LocPathIterator lpi2  = (LocPathIterator)expr2;				
				if(null == head)
				{
					head = new MultistepExprHolder(firstOccuranceOwner, stepCount, null);
					tail = head;
					numPathsFound++;
				}
				tail.m_next = new MultistepExprHolder(owner2, stepCount, null);
				tail = tail.m_next;
	
				// Null out the occurance, so we don't have to test it again.
				paths.setElementAt(null, j);
				
				// foundFirst = true;
				numPathsFound++;
			}
		}
	}
	
	// Change all globals in xsl:templates, etc, to global vars no matter what.
	if((0 == numPathsFound) && isGlobal)
	{
      head = new MultistepExprHolder(firstOccuranceOwner, stepCount, null);
      numPathsFound++;
	}
	
	if(null != head)
	{
		ElemTemplateElement root = isGlobal ? psuedoVarRecipient : findCommonAncestor(head);
		LocPathIterator sharedIter = (LocPathIterator)head.m_exprOwner.getExpression();
		ElemVariable var = createPseudoVarDecl(root, sharedIter, isGlobal);
		if(DIAGNOSE_MULTISTEPLIST)
			System.err.println("Created var: "+var.getName()+(isGlobal ? "(Global)" : ""));
		QName uniquePseudoVarName = var.getName();
		while(null != head)
		{
			ExpressionOwner owner = head.m_exprOwner;	
			if(DIAGNOSE_MULTISTEPLIST)
				diagnoseLineNumber(owner.getExpression());
			changeToVarRef(uniquePseudoVarName, owner, paths, root);
			head = head.m_next;
		}
		// Replace the first occurance with the variable's XPath, so  
		// that further reduction may take place if needed.
		paths.setElementAt(var.getSelect(), firstOccuranceIndex);
	}
	
	return numPathsFound;
  } 
  
  /**
   * To be removed.
   */
  protected int oldFindAndEliminateRedundant(int start, int firstOccuranceIndex,
                         ExpressionOwner firstOccuranceOwner, 
                         ElemTemplateElement psuedoVarRecipient,
                         Vector paths) 
                 throws org.w3c.dom.DOMException 
  {
	QName uniquePseudoVarName = null;
	boolean foundFirst = false;
	int numPathsFound = 0;
	int n = paths.size();
	Expression expr1 = firstOccuranceOwner.getExpression();
	if(DEBUG)
		assertIsLocPathIterator(expr1, firstOccuranceOwner);
	boolean isGlobal = (paths == m_absPaths);
	LocPathIterator lpi = (LocPathIterator)expr1;
	for(int j = start; j < n; j++)
	{
		ExpressionOwner owner2 = (ExpressionOwner)paths.elementAt(j);
		if(null != owner2)
		{
			Expression expr2 = owner2.getExpression();
			boolean isEqual = expr2.deepEquals(lpi);
			if(isEqual)
			{  		
				LocPathIterator lpi2  = (LocPathIterator)expr2;				
				if(!foundFirst)
				{
					foundFirst = true;
					// Insert variable decl into psuedoVarRecipient
					// We want to insert this into the first legitimate 
					// position for a variable.
				    ElemVariable var = createPseudoVarDecl(psuedoVarRecipient, lpi, isGlobal);
				    if(null == var)
				    	return 0;
				    uniquePseudoVarName = var.getName();
	
					changeToVarRef(uniquePseudoVarName, firstOccuranceOwner, 
					               paths, psuedoVarRecipient);
					               
					// Replace the first occurance with the variable's XPath, so  
					// that further reduction may take place if needed.
					paths.setElementAt(var.getSelect(), firstOccuranceIndex);
					numPathsFound++;
				}
	
				changeToVarRef(uniquePseudoVarName, owner2, paths, psuedoVarRecipient);
	
				// Null out the occurance, so we don't have to test it again.
				paths.setElementAt(null, j);
				
				// foundFirst = true;
				numPathsFound++;
			}
		}
	}
	
	// Change all globals in xsl:templates, etc, to global vars no matter what.
	if((0 == numPathsFound) && (paths == m_absPaths))
	{
      ElemVariable var = createPseudoVarDecl(psuedoVarRecipient, lpi, true);
      if(null == var)
        return 0;
	  uniquePseudoVarName = var.getName();
      changeToVarRef(uniquePseudoVarName, firstOccuranceOwner, paths, psuedoVarRecipient);
      paths.setElementAt(var.getSelect(), firstOccuranceIndex);
      numPathsFound++;
	}
	return numPathsFound;
  }
  
  /**
   * Count the steps in a given location path.
   * 
   * @param lpi The location path iterator that owns the steps.
   * @return The number of steps in the given location path.
   */
  protected int countSteps(LocPathIterator lpi)
  {
  	if(lpi instanceof WalkingIterator)
  	{
  		WalkingIterator wi = (WalkingIterator)lpi;
  		AxesWalker aw = wi.getFirstWalker();
  		int count = 0;
  		while(null != aw)
  		{
  			count++;
  			aw = aw.getNextWalker();
  		}
  		return count;
  	}
  	else
  		return 1;
  }
  
  /**
   * Change the expression owned by the owner argument to a variable reference 
   * of the given name.
   * 
   * Warning: For global vars, this function relies on the variable declaration 
   * to which it refers having been added just prior to this function being called,
   * so that the reference index can be determined from the size of the global variables 
   * list minus one.
   * 
   * @param varName The name of the variable which will be referenced.
   * @param owner The owner of the expression which will be replaced by a variable ref.
   * @param paths The paths list that the iterator came from, mainly to determine
   *              if this is a local or global reduction.
   * @param psuedoVarRecipient The element within whose scope the variable is 
   *                           being inserted, possibly a StylesheetRoot.
   */
  protected void changeToVarRef(QName varName, ExpressionOwner owner, 
                                Vector paths, ElemTemplateElement psuedoVarRecipient) 
  {
	Variable varRef = (paths == m_absPaths) ? new VariableSafeAbsRef() : new Variable();
	varRef.setQName(varName);
	if(paths == m_absPaths)
	{
		StylesheetRoot root = (StylesheetRoot)psuedoVarRecipient;
		Vector globalVars = root.getVariablesAndParamsComposed();
		// Assume this operation is occuring just after the decl has 
		// been added.
		varRef.setIndex(globalVars.size()-1);
		varRef.setIsGlobal(true);
	}
	owner.setExpression(varRef);
  }
   
  private synchronized static int getPseudoVarID(){
      return m_uniquePseudoVarID++; 
  }
  
  /**
   * Create a psuedo variable reference that will represent the 
   * shared redundent XPath, and add it to the stylesheet.
   * 
   * @param psuedoVarRecipient The broadest scope of where the variable 
   * should be inserted, usually an xsl:template or xsl:for-each.
   * @param lpi The LocationPathIterator that the variable should represent.
   * @param isGlobal true if the paths are global.
   * @return The new psuedo var element.
   */
  protected ElemVariable createPseudoVarDecl(
      ElemTemplateElement psuedoVarRecipient,
      LocPathIterator lpi, boolean isGlobal)
      throws org.w3c.dom.DOMException
  {
    QName uniquePseudoVarName = new QName (PSUEDOVARNAMESPACE, "#"+getPseudoVarID());
  		
  	if(isGlobal)
  	{
  	  return createGlobalPseudoVarDecl(uniquePseudoVarName, 
  	                                  (StylesheetRoot)psuedoVarRecipient, lpi);
  	}
  	else						
      return createLocalPseudoVarDecl(uniquePseudoVarName, psuedoVarRecipient, lpi);
  }
  
  /**
   * Create a psuedo variable reference that will represent the 
   * shared redundent XPath, for a local reduction.
   * 
   * @param uniquePseudoVarName The name of the new variable.
   * @param stylesheetRoot The broadest scope of where the variable 
   *        should be inserted, which must be a StylesheetRoot element in this case.
   * @param lpi The LocationPathIterator that the variable should represent.
   * @return null if the decl was not created, otherwise the new Pseudo var  
   *              element.
   */
  protected ElemVariable createGlobalPseudoVarDecl(QName uniquePseudoVarName,
                                           StylesheetRoot stylesheetRoot, 
                                           LocPathIterator lpi) 
        throws org.w3c.dom.DOMException 
  {
  	ElemVariable psuedoVar = new ElemVariable();
  	psuedoVar.setIsTopLevel(true);
	XPath xpath = new XPath(lpi);
	psuedoVar.setSelect(xpath);
	psuedoVar.setName(uniquePseudoVarName);
	
	Vector globalVars = stylesheetRoot.getVariablesAndParamsComposed();
	psuedoVar.setIndex(globalVars.size());
	globalVars.addElement(psuedoVar);
	return psuedoVar;
  }

  
  

  /**
   * Create a psuedo variable reference that will represent the 
   * shared redundent XPath, for a local reduction.
   * 
   * @param uniquePseudoVarName The name of the new variable.
   * @param psuedoVarRecipient The broadest scope of where the variable 
   * should be inserted, usually an xsl:template or xsl:for-each.
   * @param lpi The LocationPathIterator that the variable should represent.
   * @return null if the decl was not created, otherwise the new Pseudo var  
   *              element.
   */
  protected ElemVariable createLocalPseudoVarDecl(QName uniquePseudoVarName,
                                           ElemTemplateElement psuedoVarRecipient, 
                                           LocPathIterator lpi) 
        throws org.w3c.dom.DOMException 
  {
		ElemVariable psuedoVar = new ElemVariablePsuedo();
		
		XPath xpath = new XPath(lpi);
		psuedoVar.setSelect(xpath);
		psuedoVar.setName(uniquePseudoVarName);

		ElemVariable var = addVarDeclToElem(psuedoVarRecipient, lpi, psuedoVar);
		
		lpi.exprSetParent(var);
		
		return var;
  }

  /**
   * Add the given variable to the psuedoVarRecipient.
   */
  protected ElemVariable addVarDeclToElem(
    ElemTemplateElement psuedoVarRecipient,
    LocPathIterator lpi,
    ElemVariable psuedoVar)
    throws org.w3c.dom.DOMException
  {
    // Create psuedo variable element
    ElemTemplateElement ete = psuedoVarRecipient.getFirstChildElem();

    lpi.callVisitors(null, m_varNameCollector);

    // If the location path contains variables, we have to insert the 
    // psuedo variable after the reference. (Otherwise, we want to 
    // insert it as close as possible to the top, so we'll be sure 
    // it is in scope for any other vars.
    if (m_varNameCollector.getVarCount() > 0)
    {
      ElemTemplateElement baseElem = getElemFromExpression(lpi);
      ElemVariable varElem = getPrevVariableElem(baseElem);
      while (null != varElem)
      {
        if (m_varNameCollector.doesOccur(varElem.getName()))
          {
          psuedoVarRecipient = varElem.getParentElem();
          ete = varElem.getNextSiblingElem();
          break;
        }
        varElem = getPrevVariableElem(varElem);
      }
    }

    if ((null != ete) && (Constants.ELEMNAME_PARAMVARIABLE == ete.getXSLToken()))
    {
      // Can't stick something in front of a param, so abandon! (see variable13.xsl)
      if(isParam(lpi))
        return null;

      while (null != ete)
      {
        ete = ete.getNextSiblingElem();
        if ((null != ete) && Constants.ELEMNAME_PARAMVARIABLE != ete.getXSLToken())
            break;
      }
    }
    psuedoVarRecipient.insertBefore(psuedoVar, ete);
    m_varNameCollector.reset();
    return psuedoVar;
  }
    
  /**
   * Tell if the expr param is contained within an xsl:param.
   */
  protected boolean isParam(ExpressionNode expr)
  {
  	while(null != expr)
  	{
  		if(expr instanceof ElemTemplateElement)
  			break;
  		expr = expr.exprGetParent();
  	}
  	if(null != expr)
  	{
  		ElemTemplateElement ete = (ElemTemplateElement)expr;
  		while(null != ete)
  		{
  			int type = ete.getXSLToken();
  			switch(type)
  			{
  				case Constants.ELEMNAME_PARAMVARIABLE:
  					return true;
  				case Constants.ELEMNAME_TEMPLATE:
  				case Constants.ELEMNAME_STYLESHEET:
  					return false;
  			}
  			ete = ete.getParentElem();
  		}
  	}
  	return false;
  	
  }
  
  /**
   * Find the previous occurance of a xsl:variable.  Stop 
   * the search when a xsl:for-each, xsl:template, or xsl:stylesheet is 
   * encountered.
   * 
   * @param elem Should be non-null template element.
   * @return The first previous occurance of an xsl:variable or xsl:param, 
   * or null if none is found.
   */
  protected ElemVariable getPrevVariableElem(ElemTemplateElement elem)
  {
  	// This could be somewhat optimized.  since getPreviousSiblingElem is a  
  	// fairly expensive operation.
  	while(null != (elem = getPrevElementWithinContext(elem)))
  	{
  		int type = elem.getXSLToken();
  			
  		if((Constants.ELEMNAME_VARIABLE == type) ||
  		   (Constants.ELEMNAME_PARAMVARIABLE == type))
  		{
  			return (ElemVariable)elem;
  		}
  	}
  	return null;
  }
  
  /**
   * Get the previous sibling or parent of the given template, stopping at 
   * xsl:for-each, xsl:template, or xsl:stylesheet.
   * 
   * @param elem Should be non-null template element.
   * @return previous sibling or parent, or null if previous is xsl:for-each, 
   * xsl:template, or xsl:stylesheet.
   */
  protected ElemTemplateElement getPrevElementWithinContext(ElemTemplateElement elem)
  {
  	ElemTemplateElement prev = elem.getPreviousSiblingElem();
  	if(null == prev)
  		prev = elem.getParentElem();
  	if(null != prev)
  	{
  	  int type = prev.getXSLToken();
  	  if((Constants.ELEMNAME_FOREACH == type) || 
  	     (Constants.ELEMNAME_TEMPLATE == type) ||
  	     (Constants.ELEMNAME_STYLESHEET == type))
  	  {
  	  	prev = null;
  	  }
  	}
  	return prev;
  }
  
  /**
   * From an XPath expression component, get the ElemTemplateElement 
   * owner.
   * 
   * @param expr Should be static expression with proper parentage.
   * @return Valid ElemTemplateElement, or throw a runtime exception 
   * if it is not found.
   */
  protected ElemTemplateElement getElemFromExpression(Expression expr)
  {
  	ExpressionNode parent = expr.exprGetParent();
  	while(null != parent)
  	{
  		if(parent instanceof ElemTemplateElement)
  			return (ElemTemplateElement)parent;
  		parent = parent.exprGetParent();
  	}
  	throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_ASSERT_NO_TEMPLATE_PARENT, null));
  	// "Programmer's error! expr has no ElemTemplateElement parent!");
  }
      
  /**
   * Tell if the given LocPathIterator is relative to an absolute path, i.e. 
   * in not dependent on the context.
   * 
   * @return true if the LocPathIterator is not dependent on the context node.
   */
  public boolean isAbsolute(LocPathIterator path)
  {
  	int analysis = path.getAnalysisBits();
    boolean isAbs = (WalkerFactory.isSet(analysis, WalkerFactory.BIT_ROOT) || 
           WalkerFactory.isSet(analysis, WalkerFactory.BIT_ANY_DESCENDANT_FROM_ROOT));
    if(isAbs)
    {
    	isAbs = m_absPathChecker.checkAbsolute(path);
    }
    return isAbs;
  }


  /**
   * Visit a LocationPath.
   * @param owner The owner of the expression, to which the expression can 
   *              be reset if rewriting takes place.
   * @param path The LocationPath object.
   * @return true if the sub expressions should be traversed.
   */
  public boolean visitLocationPath(ExpressionOwner owner, LocPathIterator path)
  {
  	// Don't optimize "." or single step variable paths.
  	// Both of these cases could use some further optimization by themselves.
  	if(path instanceof SelfIteratorNoPredicate)
  	{
  		return true;
  	}
  	else if(path instanceof WalkingIterator)
  	{
  		WalkingIterator wi = (WalkingIterator)path;
  		AxesWalker aw = wi.getFirstWalker();
  		if((aw instanceof FilterExprWalker) && (null == aw.getNextWalker()))
  		{
  			FilterExprWalker few = (FilterExprWalker)aw;
  			Expression exp = few.getInnerExpression();
  			if(exp instanceof Variable)
  				return true;
  		}
  	}

    if (isAbsolute(path) && (null != m_absPaths))
    {
      if(DEBUG)
        validateNewAddition(m_absPaths, owner, path);
      m_absPaths.addElement(owner);
    }
    else if (m_isSameContext && (null != m_paths))
    {
      if(DEBUG)
        validateNewAddition(m_paths, owner, path);
      m_paths.addElement(owner);
    }

    return true;
  }

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
    boolean savedIsSame = m_isSameContext;
    m_isSameContext = false;

    // Any further down, just collect the absolute paths.
    pred.callVisitors(owner, this);

    m_isSameContext = savedIsSame;

    // We've already gone down the subtree, so don't go have the caller 
    // go any further.
    return false;
  }
  
  /**
   * Visit an XSLT top-level instruction.
   * 
   * @param elem The xsl instruction element object.
   * @return true if the sub expressions should be traversed.
   */
   public boolean visitTopLevelInstruction(ElemTemplateElement elem)
   {
     int type = elem.getXSLToken();
     switch(type)
     {
       case Constants.ELEMNAME_TEMPLATE :
         return visitInstruction(elem);
       default:
         return true;
     }
   }


  /**
   * Visit an XSLT instruction.  Any element that isn't called by one 
   * of the other visit methods, will be called by this method.
   * 
   * @param elem The xsl instruction element object.
   * @return true if the sub expressions should be traversed.
   */
  public boolean visitInstruction(ElemTemplateElement elem)
  {
    int type = elem.getXSLToken();
    switch (type)
    {
      case Constants.ELEMNAME_CALLTEMPLATE :
      case Constants.ELEMNAME_TEMPLATE :
      case Constants.ELEMNAME_FOREACH :
        {
          
          // Just get the select value.
          if(type == Constants.ELEMNAME_FOREACH)
          {
            ElemForEach efe = (ElemForEach) elem;
   		    
  		    Expression select = efe.getSelect();
  		    select.callVisitors(efe, this);
          }
         
  		  Vector savedPaths = m_paths;
  		  m_paths = new Vector();
  		    
  		  // Visit children.  Call the superclass callChildVisitors, because 
  		  // we don't want to visit the xsl:for-each select attribute, or, for 
  		  // that matter, the xsl:template's match attribute.
  		  elem.callChildVisitors(this, false);  		
  		  eleminateRedundentLocals(elem);
  		    
  		  m_paths = savedPaths;
 
          // select.callVisitors(efe, this);
          return false;
        }
      case Constants.ELEMNAME_NUMBER :
      case Constants.ELEMNAME_SORT :
        // Just collect absolute paths until and unless we can fully
        // analyze these cases.
        boolean savedIsSame = m_isSameContext;
        m_isSameContext = false;
        elem.callChildVisitors(this);
        m_isSameContext = savedIsSame;
        return false;
        
      default :
        return true;
    }
  }
  
  // ==== DIAGNOSTIC AND DEBUG FUNCTIONS ====
  
  /**
   * Print out to std err the number of paths reduced.
   */
  protected void diagnoseNumPaths(Vector paths, int numPathsEliminated,  
                                  int numUniquePathsEliminated) 
  {
		if (numPathsEliminated > 0)
		{ 
		  if(paths == m_paths)
		  {
		    System.err.println("Eliminated " + numPathsEliminated + " total paths!");
		    System.err.println(
		      "Consolodated " + numUniquePathsEliminated + " redundent paths!");
		  }
		  else
		  {
		    System.err.println("Eliminated " + numPathsEliminated + " total global paths!");
		    System.err.println(
		      "Consolodated " + numUniquePathsEliminated + " redundent global paths!");
		  }
		}  
  }


  /**
   * Assert that the expression is a LocPathIterator, and, if 
   * not, try to give some diagnostic info.
   */
  private final void assertIsLocPathIterator(Expression expr1, ExpressionOwner eo) 
    throws RuntimeException 
  {
		if(!(expr1 instanceof LocPathIterator))
		{
			String errMsg;
			if(expr1 instanceof Variable)
			{
				errMsg = "Programmer's assertion: expr1 not an iterator: "+
				          ((Variable)expr1).getQName();
			}
			else
			{
				errMsg = "Programmer's assertion: expr1 not an iterator: "+
				          expr1.getClass().getName();
			}
			throw new RuntimeException(errMsg + ", "+
				          eo.getClass().getName()+" "+
				          expr1.exprGetParent());
		}
  }


  /**
   * Validate some assumptions about the new LocPathIterator and it's 
   * owner and the state of the list.
   */
  private static void validateNewAddition(Vector paths, ExpressionOwner owner, 
                                          LocPathIterator path) 
		throws RuntimeException 
  {
  	assertion(owner.getExpression() == path, "owner.getExpression() != path!!!");
	int n = paths.size();
	// There should never be any duplicates in the list!
	for(int i = 0; i < n; i++)
	{
		ExpressionOwner ew = (ExpressionOwner)paths.elementAt(i);
		assertion(ew != owner, "duplicate owner on the list!!!");
		assertion(ew.getExpression() != path, "duplicate expression on the list!!!");
	}
  }
  
  /**
   * Simple assertion.
   */
  protected static void assertion(boolean b, String msg)
  {
  	if(!b)
  	{
  		throw new RuntimeException(XSLMessages.createMessage(XSLTErrorResources.ER_ASSERT_REDUNDENT_EXPR_ELIMINATOR, new Object[]{msg}));
  		// "Programmer's assertion in RundundentExprEliminator: "+msg);
  	}
  }
  
  /**
   * Since we want to sort multistep expressions by length, use 
   * a linked list with elements of type MultistepExprHolder.
   */
  class MultistepExprHolder implements Cloneable
  {
	ExpressionOwner m_exprOwner; // Will change to null once we have processed this item.
	final int m_stepCount;
	MultistepExprHolder m_next;
	
	/**
	 * Clone this object.
	 */
	public Object clone()
		throws CloneNotSupportedException
	{
		return super.clone();
	}
	
	/**
	 * Create a MultistepExprHolder.
	 * 
	 * @param exprOwner the owner of the expression we are holding.
	 *                  It must hold a LocationPathIterator.
	 * @param stepCount The number of steps in the location path.
	 */
  	MultistepExprHolder(ExpressionOwner exprOwner, int stepCount, MultistepExprHolder next)
  	{
  		m_exprOwner = exprOwner;
  		assertion(null != m_exprOwner, "exprOwner can not be null!");
  		m_stepCount = stepCount;
  		m_next = next;
  	}
	
	/**
	 * Add a new MultistepExprHolder in sorted order in the list.
	 * 
	 * @param exprOwner the owner of the expression we are holding.
	 *                  It must hold a LocationPathIterator.
	 * @param stepCount The number of steps in the location path.
	 * @return The new head of the linked list.
	 */
	MultistepExprHolder addInSortedOrder(ExpressionOwner exprOwner, int stepCount)
	{
		MultistepExprHolder first = this;
		MultistepExprHolder next = this;
		MultistepExprHolder prev = null;
		while(null != next)
		{
			if(stepCount >= next.m_stepCount)
			{
				MultistepExprHolder newholder = new MultistepExprHolder(exprOwner, stepCount, next);
				if(null == prev)
					first = newholder;
				else
					prev.m_next = newholder;
					
				return first;
			}
			prev = next;
			next = next.m_next;
		}
		
		prev.m_next = new MultistepExprHolder(exprOwner, stepCount, null);
		return first;
	}
	
	/**
	 * Remove the given element from the list.  'this' should 
	 * be the head of the list.  If the item to be removed is not 
	 * found, an assertion will be made.
	 * 
	 * @param itemToRemove The item to remove from the list.
	 * @return The head of the list, which may have changed if itemToRemove 
	 * is the same as this element.  Null if the item to remove is the 
	 * only item in the list.
	 */
	MultistepExprHolder unlink(MultistepExprHolder itemToRemove)
	{
		MultistepExprHolder first = this;
		MultistepExprHolder next = this;
		MultistepExprHolder prev = null;
		while(null != next)
		{
			if(next == itemToRemove)
			{
				if(null == prev)
					first = next.m_next;
				else
					prev.m_next = next.m_next;
				
				next.m_next = null;
					
				return first;
			}
			prev = next;
			next = next.m_next;
		}
		
		assertion(false, "unlink failed!!!");
		return null;
	}
		
	/**
	 * Get the number of linked list items.
	 */
	int getLength()
	{
		int count = 0;
		MultistepExprHolder next = this;
		while(null != next)
		{
			count++;
			next = next.m_next;
		}
		return count;
	}
	
    /**
     * Print diagnostics out for the multistep list.
     */
    protected void diagnose()
    {
      System.err.print("Found multistep iterators: " + this.getLength() + "  ");
      MultistepExprHolder next = this;
      while (null != next)
      {
        System.err.print("" + next.m_stepCount);
        next = next.m_next;
        if (null != next)
              System.err.print(", ");
      }
      System.err.println();
    }
	
  }

}
