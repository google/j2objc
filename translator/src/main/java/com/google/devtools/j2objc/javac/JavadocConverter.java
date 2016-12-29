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

package com.google.devtools.j2objc.javac;

import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SourcePosition;
import com.google.devtools.j2objc.ast.TagElement;
import com.google.devtools.j2objc.ast.TextElement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.DocSourcePositions;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.JCTree;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Converts a javac javadoc comment into a Javadoc AST node.
 */
class JavadocConverter extends DocTreeScanner<Void, TagElement> {

  private final Element element;
  private final DCTree.DCDocComment docComment;
  private final DocSourcePositions docSourcePositions;
  private final String source;
  private final CompilationUnitTree unit;
  private final boolean reportWarnings;

  private JavadocConverter(Element element, DCTree.DCDocComment docComment, String source,
      DocTrees docTrees, CompilationUnitTree unit, boolean reportWarnings) {
    this.element = element;
    this.docComment = docComment;
    this.source = source;
    this.docSourcePositions = docTrees.getSourcePositions();
    this.unit = unit;
    this.reportWarnings = reportWarnings;
  }

  /**
   * Returns an AST node for the javadoc comment of a specified class,
   * method, or field element.
   */
  static Javadoc convertJavadoc(Element element, String source, JavacEnvironment env,
      boolean reportWarnings) {
    DocTrees docTrees = DocTrees.instance(env.task());
    TreePath path = docTrees.getPath(element);
    if (path == null) {
      throw new AssertionError("could not find tree path for element");
    }
    DCTree.DCDocComment docComment = (DCTree.DCDocComment) docTrees.getDocCommentTree(path);
    if (docComment == null) {
      return null; // Declaration does not have a javadoc comment.
    }
    JavadocConverter converter = new JavadocConverter(element, docComment, source, docTrees,
        path.getCompilationUnit(), reportWarnings);
    Javadoc result = new Javadoc();
    TagElement newTag = new TagElement();  // First tag has no name.
    converter.scan(docComment.getFirstSentence(), newTag);
    converter.scan(docComment.getBody(), newTag);
    if (!newTag.getFragments().isEmpty()) {
      List<TreeNode> fragments = newTag.getFragments();
      int start = fragments.get(0).getStartPosition();
      TreeNode lastFragment = fragments.get(fragments.size() - 1);
      int end = start + lastFragment.getLength();
      converter.setPos(newTag, start, end);
      result.addTag(newTag);
    }
    for (DocTree tag : docComment.getBlockTags()) {
      if (tag.getKind() != DocTree.Kind.ERRONEOUS) {
        newTag = new TagElement();
        converter.scan(tag, newTag);
        result.addTag(newTag);
      }
    }
    return result;
  }

  @Override
  public Void visitAuthor(AuthorTree node, TagElement tag) {
    setTagValues(tag, TagElement.TAG_AUTHOR, node, node.getName());
    return null;
  }

  @Override
  public Void visitComment(CommentTree node, TagElement tag) {
    tag.addFragment(setPos(node, new TextElement().setText(node.getBody())));
    return null;
  }

  @Override
  public Void visitDeprecated(DeprecatedTree node, TagElement tag) {
    setTagValues(tag, TagElement.TAG_DEPRECATED, node, node.getBody());
    return null;
  }

  @Override
  public Void visitEndElement(EndElementTree node, TagElement tag) {
    String text = String.format("</%s>", node.getName().toString());
    int pos = pos(node);
    tag.addFragment(setPos(new TextElement().setText(text), pos, pos + text.length()));
    return null;
  }

  @Override
  public Void visitEntity(EntityTree node, TagElement tag) {
    String text = String.format("&%s;", node.getName().toString());
    tag.addFragment(setPos(node, new TextElement().setText(text)));
    return null;
  }

  @Override
  public Void visitErroneous(ErroneousTree node, TagElement tag) {
    if (reportWarnings) {
      // Update node's position to be relative to the whole source file, instead of just
      // the doc-comment's start. That way, the diagnostic printer will fetch the correct
      // text for the line the error is on.
      ((DCTree.DCErroneous) node).pos = ((DCTree) node).pos(docComment).getStartPosition();
      ErrorUtil.warning(node.getDiagnostic().toString());
    } else {
      // Include erroneous text in doc-comment as is.
      TreeNode newNode = setPos(node, new TextElement().setText(node.getBody()));
      tag.addFragment(newNode);
    }
    return null;
  }

  @Override
  public Void visitIdentifier(IdentifierTree node, TagElement tag) {
    tag.addFragment(setPos(node, new TextElement().setText(node.getName().toString())));
    return null;
  }

  @Override
  public Void visitLink(LinkTree node, TagElement tag) {
    TagElement newTag = new TagElement().setTagName("@" + node.getTagName());
    setPos(node, newTag);
    if (node.getLabel().isEmpty()) {
      scan(node.getReference(), newTag);
    } else {
      scan(node.getLabel(), newTag);
    }
    tag.addFragment(newTag);
    return null;
  }

  @Override
  public Void visitLiteral(LiteralTree node, TagElement tag) {
    TagElement newTag = new TagElement();
    String tagName = node.getKind() == DocTree.Kind.CODE ? "@code" : "@literal";
    setTagValues(newTag, tagName, node, node.getBody());
    tag.addFragment(newTag);
    return null;
  }

  @Override
  public Void visitParam(ParamTree node, TagElement tag) {
    DCTree.DCIdentifier identifier = (DCTree.DCIdentifier) node.getName();
    if (identifier == null || node.isTypeParameter()) {
      return null;
    }
    List<? extends VariableElement> params = element instanceof ExecutableElement
        ? ((ExecutableElement) element).getParameters() : Collections.emptyList();
        tag.setTagName(TagElement.TAG_PARAM);
    String name = identifier.toString();
    VariableElement param = null;
    for (VariableElement p : params) {
      if (name.equals(p.getSimpleName().toString())) {
        param = p;
        break;
      }
    }
    // param will be null if the @param tag refers to a nonexistent parameter.
    TreeNode nameNode = param != null ? new SimpleName(param) : new SimpleName(name);
    setPos(identifier, nameNode);
    tag.addFragment(nameNode);
    scan(node.getDescription(), tag);
    int lastEnd = nameNode.getStartPosition();
    for (TreeNode fragment : tag.getFragments()) {
      // Fix up positions to match JDT's.
      // TODO(tball): remove and fix JavadocGenerator after javac switch.
      if (fragment.getKind() == TreeNode.Kind.TEXT_ELEMENT) {
        TextElement text = (TextElement) fragment;
        text.setText(" " + text.getText());
        text.setSourceRange(text.getStartPosition(), text.getLength() + 1);
      }
      int thisEnd = lastEnd + fragment.getLength();
      setPos(fragment, lastEnd, thisEnd);
      lastEnd = thisEnd;
    }
    setPos(tag, pos(node), endPos(node));
    tag.setLineNumber(nameNode.getLineNumber());
    return null;
  }

  @Override
  public Void visitReference(ReferenceTree node, TagElement tag) {
    DCTree.DCReference ref = (DCTree.DCReference) node;
    JCTree qualifier = ref.qualifierExpression;
    TreeNode newNode;
    if (qualifier != null && qualifier.getKind() == com.sun.source.tree.Tree.Kind.MEMBER_SELECT) {
      newNode = convertQualifiedName(qualifier);
    } else {
      newNode = new TextElement().setText(node.getSignature());
    }
    tag.addFragment(setPos(node, newNode));
    return null;
  }

  @Override
  public Void visitReturn(ReturnTree node, TagElement tag) {
    tag.setTagName(TagElement.TAG_RETURN);
    setPos(node, tag);
    scan(node.getDescription(), tag);
    return null;
  }

  @Override
  public Void visitSee(SeeTree node, TagElement tag) {
    tag.setTagName(TagElement.TAG_SEE);
    setPos(node, tag);
    scan(node.getReference(), tag);
    return null;
}

  @Override
  public Void visitSince(SinceTree node, TagElement tag) {
    setTagValues(tag, TagElement.TAG_SINCE, node, node.getBody());
    return null;
  }

  @Override
  public Void visitStartElement(StartElementTree node, TagElement tag) {
    StringBuilder sb = new StringBuilder("<");
    sb.append(node.getName());
    for (DocTree attr : node.getAttributes()) {
      sb.append(' ');
      sb.append(attr);
    }
    sb.append('>');
    tag.addFragment(setPos(node, new TextElement().setText(sb.toString())));
    return null;
  }

  @Override
  public Void visitText(TextTree node, TagElement tag) {
    String[] lines = node.getBody().split("\n");
    int linePos = pos(node);
    for (String line : lines) {
      if (line.length() > 0) {
        linePos = source.indexOf(line, linePos);
        int endPos = linePos + line.length();
        TreeNode newNode = setPos(new TextElement().setText(line), linePos, endPos);
        tag.addFragment(newNode);
      }
    }
    return null;
  }

  @Override
  public Void visitThrows(ThrowsTree node, TagElement tag) {
    setTagValues(tag, TagElement.TAG_THROWS, node, node.getExceptionName());
    scan(node.getDescription(), tag);
    return null;
  }

  @Override
  public Void visitVersion(VersionTree node, TagElement tag) {
    setTagValues(tag, TagElement.TAG_VERSION, node, node.getBody());
    return null;
  }

  /**
   * Updates a tag element with values from the javadoc node.
   */
  private TagElement setTagValues(TagElement tag, String tagName, DocTree javadocNode,
      DocTree body) {
    tag.setTagName(tagName);
    setPos(javadocNode, tag);
    scan(body, tag);
    return tag;
  }

  private TagElement setTagValues(TagElement tag, String tagName, DocTree javadocNode,
      List<? extends DocTree> body) {
    tag.setTagName(tagName);
    setPos(javadocNode, tag);
    scan(body, tag);
    return tag;
  }

  /**
   * Set a TreeNode's position using the original DocTree.
   */
  private TreeNode setPos(DocTree node, TreeNode newNode) {
    int pos = pos(node);
    return newNode.setPosition(new SourcePosition(pos, length(node), lineNumber(pos)));
  }

  /**
   * Set a TreeNode's position using begin and end source offsets. Its line number
   * is unchanged.
   */
  private TreeNode setPos(TreeNode newNode, int pos, int endPos) {
    return newNode.setPosition(new SourcePosition(pos, endPos - pos, lineNumber(pos)));
  }

  private int pos(DocTree node) {
    return (int) docSourcePositions.getStartPosition(unit, docComment, node);
  }

  private int endPos(DocTree node) {
    return (int) docSourcePositions.getEndPosition(unit, docComment, node);
  }

  private int lineNumber(int pos) {
    return (int) unit.getLineMap().getLineNumber(pos);
  }

  private int length(DocTree node) {
    return endPos(node) - pos(node);
  }

  private Name convertQualifiedName(JCTree qualifier) {
    if (qualifier.getKind() == com.sun.source.tree.Tree.Kind.MEMBER_SELECT) {
      JCTree.JCFieldAccess select = (JCTree.JCFieldAccess) qualifier;
      return new QualifiedName()
          .setName(new SimpleName(select.getIdentifier().toString()))
          .setQualifier(convertQualifiedName(select.getExpression()));
    } else {
      return new SimpleName(qualifier.toString());
    }
  }
}
