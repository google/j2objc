/*
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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.Comment;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.SynchronizedStatement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts OCNI code blocks into NativeDeclaration and NativeStatement nodes.
 * Adds native fast enumeration support for Iterable types, when not already
 * defined.
 * Adds synchronized blocks to methods with with the synchonized modifier.
 *
 * @author Keith Stanger
 */
public class OcniExtractor extends TreeVisitor {

  private final CompilationUnit unit;
  private final ListMultimap<TreeNode, Comment> blockComments;

  public OcniExtractor(CompilationUnit unit) {
    this.unit = unit;
    blockComments = findBlockComments(unit);
  }

  /**
   * Finds all block comments and associates them with their containing type.
   * This is trickier than you might expect because of inner types.
   */
  private static ListMultimap<TreeNode, Comment> findBlockComments(CompilationUnit unit) {
    ListMultimap<TreeNode, Comment> blockComments = ArrayListMultimap.create();
    for (Comment comment : unit.getCommentList()) {
      if (!comment.isBlockComment()) {
        continue;
      }
      int commentPos = comment.getStartPosition();
      AbstractTypeDeclaration containingType = null;
      int containingTypePos = -1;
      for (AbstractTypeDeclaration type : unit.getTypes()) {
        int typePos = type.getStartPosition();
        if (typePos < 0) {
          continue;
        }
        int typeEnd = typePos + type.getLength();
        if (commentPos > typePos && commentPos < typeEnd && typePos > containingTypePos) {
          containingType = type;
          containingTypePos = typePos;
        }
      }
      blockComments.put(containingType != null ? containingType : unit, comment);
    }
    return blockComments;
  }

  @Override
  public void endVisit(CompilationUnit node) {
    for (Comment comment : blockComments.get(node)) {
      NativeDeclaration nativeDecl = extractNativeDeclaration(comment);
      if (nativeDecl != null) {
        unit.getNativeBlocks().add(nativeDecl);
      }
    }
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    int modifiers = node.getModifiers();
    if (Modifier.isNative(modifiers)) {
      NativeStatement nativeStmt = extractNativeStatement(node);
      if (nativeStmt != null) {
        Block body = new Block();
        body.getStatements().add(nativeStmt);
        node.setBody(body);
        node.removeModifiers(Modifier.NATIVE);
      }
    }
    if (Modifier.isSynchronized(modifiers)) {
      ITypeBinding declaringClass = node.getMethodBinding().getDeclaringClass();
      SynchronizedStatement syncStmt = new SynchronizedStatement(
          Modifier.isStatic(modifiers) ? new TypeLiteral(declaringClass, typeEnv)
          : new ThisExpression(declaringClass));
      syncStmt.setBody(TreeUtil.remove(node.getBody()));
      Block newBody = new Block();
      newBody.getStatements().add(syncStmt);
      node.setBody(newBody);
      node.removeModifiers(Modifier.SYNCHRONIZED);
    }
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    visitType(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    if (!node.isInterface()) {
      visitType(node);
    }
  }

  private void visitType(AbstractTypeDeclaration node) {
    Set<String> methodsPrinted = Sets.newHashSet();
    List<BodyDeclaration> bodyDeclarations = node.getBodyDeclarations();
    int minPos = 0;
    int declIdx = 0;

    for (Comment comment : blockComments.get(node)) {
      int commentPos = comment.getStartPosition();
      while (declIdx < bodyDeclarations.size()) {
        BodyDeclaration decl = bodyDeclarations.get(declIdx);
        if (decl.getStartPosition() > commentPos) {
          break;
        }
        minPos = Math.max(minPos, decl.getStartPosition() + decl.getLength());
        declIdx++;
      }
      if (commentPos > minPos) {
        NativeDeclaration nativeDecl = extractNativeDeclaration(comment);
        if (nativeDecl != null) {
          findMethodSignatures(nativeDecl.getImplementationCode(), methodsPrinted);
          bodyDeclarations.add(declIdx++, nativeDecl);
        }
      }
    }

    // If the type implements Iterable and there's no existing implementation
    // for NSFastEnumeration's protocol method, then add the default
    // implementation.
    if (BindingUtil.findInterface(node.getTypeBinding(), "java.lang.Iterable") != null
        && !methodsPrinted.contains("countByEnumeratingWithState:objects:count:")) {
      bodyDeclarations.add(NativeDeclaration.newInnerDeclaration(null,
          "- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state "
          + "objects:(__unsafe_unretained id *)stackbuf count:(NSUInteger)len {\n"
          + "  return JreDefaultFastEnumeration(self, state, stackbuf, len);\n}\n"));
    }
  }

  private static final String TYPE_REGEX = "\\([\\w\\s\\*<>\\[\\]]+\\)";
  private static final String PARAM_REGEX = "\\s*:\\s*" + TYPE_REGEX + "\\s*\\w+";
  private static final String ADDITIONAL_PARAM_REGEX = "\\s+(\\w+)" + PARAM_REGEX;
  private static final Pattern OBJC_METHOD_DECL_PATTERN = Pattern.compile(
      "^\\+|-\\s*" + TYPE_REGEX + "\\s*(\\w+)(" + PARAM_REGEX + "((?:" + ADDITIONAL_PARAM_REGEX
      + ")*))?\\s*\\{");
  private static final Pattern ADDITIONAL_PARAM_PATTERN = Pattern.compile(ADDITIONAL_PARAM_REGEX);

  /**
   * Finds the signatures of methods defined in the native code.
   */
  private static void findMethodSignatures(String code, Set<String> signatures) {
    if (code == null) {
      return;
    }
    Matcher matcher = OBJC_METHOD_DECL_PATTERN.matcher(code);
    while (matcher.find()) {
      StringBuilder signature = new StringBuilder();
      signature.append(matcher.group(1));
      if (matcher.group(2) != null) {
        signature.append(':');
        String additionalParams = matcher.group(3);
        if (additionalParams != null) {
          Matcher paramsMatcher = ADDITIONAL_PARAM_PATTERN.matcher(additionalParams);
          while (paramsMatcher.find()) {
            signature.append(paramsMatcher.group(1)).append(':');
          }
        }
      }
      signatures.add(signature.toString());
    }
  }

  private NativeDeclaration extractNativeDeclaration(TreeNode node) {
    OcniBlock ocniBlock = extractNativeCode(node);
    if (ocniBlock != null) {
      switch (ocniBlock.type) {
        case HEADER:
          {
            NativeDeclaration decl = NativeDeclaration.newInnerDeclaration(
                ocniBlock.code + '\n', null);
            decl.addModifiers(Modifier.PUBLIC);
            return decl;
          }
        case SOURCE: return NativeDeclaration.newInnerDeclaration(null, ocniBlock.code + '\n');
      }
    }
    return null;
  }

  private NativeStatement extractNativeStatement(TreeNode node) {
    OcniBlock ocniBlock = extractNativeCode(node);
    if (ocniBlock != null) {
      switch (ocniBlock.type) {
        case SOURCE: return new NativeStatement(ocniBlock.code);
        default: ErrorUtil.warning(node, "Unexpected OCNI type: " + ocniBlock.type);
      }
    }
    return null;
  }

  private enum OcniType {
    HEADER, SOURCE;

    static OcniType fromString(String type) {
      if (type.isEmpty()) {
        return SOURCE;
      }
      return valueOf(type);
    }
  }

  private static class OcniBlock {
    private final OcniType type;
    private final String code;

    private OcniBlock(OcniType type, String code) {
      this.type = type;
      this.code = code;
    }
  }

  private static final Pattern OCNI_PATTERN =
      Pattern.compile("/\\*-(\\w*)\\[(.*)\\]-\\*/", Pattern.DOTALL);

  /**
   * Returns text from within a source code range, where that text is
   * surrounded by OCNI-like tokens ("/&#42;-[" and "]-&#42;/"), warning
   * if JSNI delimiters are found instead.
   *
   * @param node The node in which to extract the native code.
   * @return the extracted text between the OCNI delimiters, or null if
   *     a pair of JSNI delimiters aren't in the specified text range
   */
  private OcniBlock extractNativeCode(TreeNode node) {
    int offset = node.getStartPosition();
    String text = unit.getSource().substring(offset, offset + node.getLength());
    Matcher m = OCNI_PATTERN.matcher(text);
    if (m.find()) {
      String typeStr = m.group(1);
      try {
        OcniType type = OcniType.fromString(typeStr);
        return new OcniBlock(type, m.group(2).trim());
      } catch (IllegalArgumentException e) {
        ErrorUtil.warning(node, "Unknown OCNI type: " + typeStr);
        return null;
      }
    }
    if (Options.jsniWarnings() && hasJsni(text)) {
      ErrorUtil.warning(node, "JSNI comment found");
    }
    return null;
  }

  private boolean hasJsni(String text) {
    int start = text.indexOf("/*-[");  // start after the bracket
    int end = text.lastIndexOf("]-*/");

    if (start == -1 || end <= start) {
      start = text.indexOf("/*-{");
      end = text.lastIndexOf("}-*/");
      if (start != -1 && end > start) {
        return true;
      }
    }
    return false;
  }
}
