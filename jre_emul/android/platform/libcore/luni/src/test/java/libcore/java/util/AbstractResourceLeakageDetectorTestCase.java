/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.java.util;

import junit.framework.TestCase;

/**
 * Ensures that resources used within a test are cleaned up; will detect problems with tests and
 * also with runtime.
 */
public abstract class AbstractResourceLeakageDetectorTestCase extends TestCase {
  /**
   * The leakage detector.
   */
  private ResourceLeakageDetector detector;

  @Override
  protected void setUp() throws Exception {
    detector = ResourceLeakageDetector.newDetector();
  }

  @Override
  protected void tearDown() throws Exception {
    // If available check for resource leakage. At this point it is impossible to determine
    // whether the test has thrown an exception. If it has then the exception thrown by this
    // could hide that test failure; it largely depends on the test runner.
    if (detector != null) {
      detector.checkForLeaks();
    }
  }
}
