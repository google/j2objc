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
// $Id: JAXPVariableStack.java 524815 2007-04-02 15:52:15Z zongaro $

package org.apache.xpath.jaxp;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.xml.utils.QName;
import org.apache.xpath.VariableStack;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;

import org.apache.xpath.res.XPATHErrorResources;
import org.apache.xalan.res.XSLMessages;


/**
 * Overrides {@link VariableStack} and delegates the call to
 * {@link javax.xml.xpath.XPathVariableResolver}.
 * 
 * @author Ramesh Mandava ( ramesh.mandava@sun.com )
 */
public class JAXPVariableStack extends VariableStack {
    	
    private final XPathVariableResolver resolver;
	
    public JAXPVariableStack(XPathVariableResolver resolver) {
        super(2);
        this.resolver = resolver;
    }
	
    public XObject getVariableOrParam(XPathContext xctxt, QName qname)
        throws TransformerException,IllegalArgumentException {
        if ( qname == null ) {
            //JAXP 1.3 spec says that if variable name is null then 
            // we need to through IllegalArgumentException
            String fmsg = XSLMessages.createXPATHMessage( 
                XPATHErrorResources.ER_ARG_CANNOT_BE_NULL,
                new Object[] {"Variable qname"} );
            throw new IllegalArgumentException( fmsg );
        }
	javax.xml.namespace.QName name =
	    new javax.xml.namespace.QName(
                qname.getNamespace(),
                qname.getLocalPart());
        Object varValue = resolver.resolveVariable( name );		
        if ( varValue == null ) {
            String fmsg = XSLMessages.createXPATHMessage( 
                XPATHErrorResources.ER_RESOLVE_VARIABLE_RETURNS_NULL,
                new Object[] { name.toString()} );
            throw new TransformerException( fmsg );
        } 
        return XObject.create( varValue, xctxt );
    }

}
