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

package org.apache.harmony.logging.tests.java.util.logging;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 */
public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
                "Suite for org.apache.harmony.logging.tests.java.util.logging");
		// $JUnit-BEGIN$
		suite.addTestSuite(LogManagerTest.class);
		suite.addTestSuite(FilterTest.class);
		suite.addTestSuite(LevelTest.class);
		suite.addTestSuite(ErrorManagerTest.class);
		suite.addTestSuite(LogRecordTest.class);
		suite.addTestSuite(FormatterTest.class);
		suite.addTestSuite(SimpleFormatterTest.class);
		suite.addTestSuite(LoggerTest.class);
		suite.addTestSuite(HandlerTest.class);
		suite.addTestSuite(StreamHandlerTest.class);
		suite.addTestSuite(ConsoleHandlerTest.class);

		suite.addTestSuite(MemoryHandlerTest.class);
//		suite.addTestSuite(FileHandlerTest.class);
		suite.addTestSuite(XMLFormatterTest.class);
//		suite.addTestSuite(SocketHandlerTest.class);

		// $JUnit-END$
		return suite;
	}
}
