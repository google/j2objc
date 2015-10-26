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

package org.apache.harmony.beans.tests.java.beans;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite(
                "Suite for org.apache.harmony.beans.tests.java.beans");
        // $JUnit-BEGIN$
        suite.addTestSuite(IntrospectorTest.class);
//        suite.addTestSuite(PropertyChangeSupportTest.class);
//        suite.addTestSuite(DefaultPersistenceDelegateTest.class);
//        suite.addTestSuite(PersistenceDelegateTest.class);
//        suite.addTestSuite(CustomizedPersistenceDelegateTest.class);
        suite.addTestSuite(PropertyChangeListenerProxyTest.class);
//        suite.addTestSuite(StatementTest.class);
        suite.addTestSuite(PropertyChangeEventTest.class);
//        suite.addTestSuite(EncoderTest.class);
        suite.addTestSuite(SimpleBeanInfoTest.class);
        suite.addTestSuite(PropertyVetoExceptionTest.class);
//        suite.addTestSuite(XMLEncoderTest.class);
//        suite.addTestSuite(BeansTest.class);
        suite.addTestSuite(BeanDescriptorTest.class);
        suite.addTestSuite(MethodDescriptorTest.class);
//        suite.addTestSuite(PersistenceDelegateTest.class);
//        suite.addTestSuite(ExpressionTest.class);
        suite.addTestSuite(IndexedPropertyDescriptorTest.class);
        suite.addTestSuite(EventSetDescriptorTest.class);
        suite.addTestSuite(IntrospectionExceptionTest.class);
        suite.addTestSuite(FeatureDescriptorTest.class);
//        suite.addTestSuite(CustomizedPersistenceDelegateTest.class);
//        suite.addTestSuite(VetoableChangeListenerProxyTest.class);
//        suite.addTestSuite(VetoableChangeSupportTest.class);
//        suite.addTestSuite(PropertyEditorManagerTest.class);
        suite.addTestSuite(ParameterDescriptorTest.class);
//        suite.addTestSuite(PropertyEditorSupportTest.class);
        suite.addTestSuite(PropertyDescriptorTest.class);
//        suite.addTestSuite(XMLDecoderTest.class);
//        suite.addTestSuite(EventHandlerTest.class);

        // $JUnit-END$
        return suite;
    }
}
