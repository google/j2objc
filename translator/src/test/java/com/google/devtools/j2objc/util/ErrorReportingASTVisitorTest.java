/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.GenerationTest;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ReturnStatement;

/**
 * Unit tests for {@link ErrorReportingASTVisitor}.
 *
 * @author Tom Ball
 */
public class ErrorReportingASTVisitorTest extends GenerationTest {

  public void testBadVisitor() {
    String source = "public class Test {\n int test() {\n return 0;\n }\n }\n";
    CompilationUnit unit = compileType("Test", source);
    ErrorReportingASTVisitor badVisitor = new ErrorReportingASTVisitor() {
      @Override
      public boolean visit(ReturnStatement node) {
        throw new UnsupportedOperationException("bad code here");
      }
    };
    try {
      badVisitor.run(unit);
      fail();
    } catch (Throwable t) {
      assertTrue(t instanceof ASTNodeException);
      ASTNodeException e = (ASTNodeException) t;
      assertTrue(e.getCause() instanceof UnsupportedOperationException);
      assertEquals(3, unit.getLineNumber(e.getSourcePosition()));
    }
  }
}
