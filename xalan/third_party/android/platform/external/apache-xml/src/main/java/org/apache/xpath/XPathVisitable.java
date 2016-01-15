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
 * $Id: XPathVisitable.java 468655 2006-10-28 07:12:06Z minchau $
 */
package org.apache.xpath;

/**
 * A class that implements this interface will call a XPathVisitor 
 * for itself and members within it's heararchy.  If the XPathVisitor's 
 * method returns false, the sub-member heararchy will not be 
 * traversed.
 */
public interface XPathVisitable
{
	/**
	 * This will traverse the heararchy, calling the visitor for 
	 * each member.  If the called visitor method returns 
	 * false, the subtree should not be called.
	 * 
	 * @param owner The owner of the visitor, where that path may be 
	 *              rewritten if needed.
	 * @param visitor The visitor whose appropriate method will be called.
	 */
	public void callVisitors(ExpressionOwner owner, XPathVisitor visitor);
}

