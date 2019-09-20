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

package com.google.devtools.j2objc.gen;

import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TagElement;
import com.google.devtools.j2objc.ast.TagElement.TagKind;
import com.google.devtools.j2objc.ast.TextElement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import java.text.BreakIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

/**
 * Generates Javadoc comments.
 *
 * @author Tom Ball, Keith Stanger
 */
public class JavadocGenerator extends AbstractSourceGenerator {

  // True when a <pre> tag is in a Javadoc comment, but not the closing </pre>.
  boolean spanningPreTag = false;

  // True with a <style> tag is in a Javadoc comment, but not the closing </style>.
  boolean spanningStyleTag = false;

  // All escapes are defined at "http://dev.w3.org/html5/html-author/charref".
  private static final Map<Character, String> htmlEntities = new HashMap<>();
  static {
    htmlEntities.put('"',  "&quot;");
    htmlEntities.put('\'', "&apos;");
    htmlEntities.put('<',  "&lt;");
    htmlEntities.put('>',  "&gt;");
    htmlEntities.put('&',  "&amp;");
    htmlEntities.put('@',  "&commat;");
 }

  private JavadocGenerator(SourceBuilder builder) {
    super(builder);
  }

  public static void printDocComment(SourceBuilder builder, Javadoc javadoc) {
    new JavadocGenerator(builder).printDocComment(javadoc);
  }

  public static String toString(Javadoc javadoc) {
    SourceBuilder builder = new SourceBuilder(false);
    printDocComment(builder, javadoc);
    return builder.toString();
  }

  public static String toString(TagElement tag) {
    SourceBuilder builder = new SourceBuilder(false);
    return new JavadocGenerator(builder).printTag(tag);
  }

  private void printDocComment(Javadoc javadoc) {
    if (javadoc != null) {
      printIndent();

      // Use HeaderDoc doc-comment start, which is compatible with Xcode Quick Help and Doxygen.
      println("/*!");

      List<TagElement> tags = javadoc.getTags();
      for (TagElement tag : tags) {
        if (tag.getTagKind() == TagKind.DESCRIPTION) {
          String description = printTagFragments(tag.getFragments());

          // Extract first sentence from description.
          BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
          iterator.setText(description.toString());
          int start = iterator.first();
          int end = iterator.next();
          if (end != BreakIterator.DONE) {
            // Print brief tag first, since Quick Help shows it first. This makes the
            // generated source easier to review.
            printDocLine(String.format("@brief %s", description.substring(start, end)).trim());
            String remainder = description.substring(end);
            if (!remainder.isEmpty()) {
              printDocLine(remainder);
            }
          } else {
            printDocLine(description);
         }
        } else {
          String doc = printTag(tag);
          if (!doc.isEmpty()) {
            printDocLine(doc);
          }
        }
      }
      printIndent();
      println(" */");
    }
  }

  private void printDocLine(String line) {
    if (!spanningPreTag) {
      printIndent();
      print(' ');
    }
    println(line);
  }

  private String printTag(TagElement tag) {
    TagKind kind = tag.getTagKind();

    // Remove @param tags for parameterized types, such as "@param <T> the type".
    // TODO(tball): update when (if) Xcode supports Objective C type parameter documenting.
    if (kind == TagKind.PARAM && hasTypeParam(tag.getFragments())) {
      return "";
    }

    // Xcode 7 compatible tags.
    if (kind == TagKind.AUTHOR
        || kind == TagKind.EXCEPTION
        || kind == TagKind.PARAM
        || kind == TagKind.RETURN
        || kind == TagKind.SINCE
        || kind == TagKind.THROWS
        || kind == TagKind.VERSION) {
      // Skip
      String comment = printTagFragments(tag.getFragments()).trim();
      return comment.isEmpty() ? "" : String.format("%s %s", kind, comment);
    }

    if (kind == TagKind.DEPRECATED) {
      // Deprecated annotation translated instead.
      return "";
    }

    if (kind == TagKind.SEE) {
      String comment = printTagFragments(tag.getFragments()).trim();
      return comment.isEmpty() ? "" : "- seealso: " + comment;
    }

    if (kind == TagKind.CODE) {
      String text = printTagFragments(tag.getFragments());
      if (spanningPreTag) {
        return text;
      }
      return String.format("<code>%s</code>", text.trim());
    }

    if (kind == TagKind.LINK) {
      return formatLinkTag(tag, "<code>%s</code>");
    }

    if (kind == TagKind.LINKPLAIN) {
      return formatLinkTag(tag, "%s");
    }

    if (kind == TagKind.LITERAL) {
      String text = printTagFragments(tag.getFragments()).trim();
      if (spanningPreTag) {
        return text;
      }
      return escapeHtmlText(text);
    }

    if (kind == TagKind.UNKNOWN) {
      // Skip unknown tags. If --doc-comment-warnings was specified, a warning was
      // already created.
      return "";
    }

    return printTagFragments(tag.getFragments());
  }

  public String formatLinkTag(TagElement tag, String template) {
    String text = printTagFragments(tag.getFragments()).trim();
    // Delete leading '#' characters (method links), and change embedded ones
    // (such as "class#method") to '.'.
    if (text.indexOf('#') == 0) {
      text = text.substring(1);
    }
    text = text.replace('#', '.');

    return String.format(template, text);
  }

  private boolean hasTypeParam(List<TreeNode> fragments) {
    // The format for a @param tag with a type parameter is:
    // [ "<", Name, ">", comment ].
    return fragments.size() >= 3
        && "<".equals(fragments.get(0).toString())
        && (fragments.get(1) instanceof SimpleName)
        && ">".equals(fragments.get(2).toString());
  }

  private String printTagFragments(List<TreeNode> fragments) {
    if (fragments.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    int lineNo = fragments.get(0).getLineNumber();
    for (TreeNode fragment : fragments) {
      if (fragment.getLineNumber() > lineNo) {
        sb.append("\n ");
        lineNo = fragment.getLineNumber();
      }
      if (fragment instanceof TextElement) {
        if (spanningPreTag) {
          sb.append(getSourceIndent(fragment));
        }
        String text = escapeDocText(((TextElement) fragment).getText());
        sb.append(text);
      } else if (fragment instanceof TagElement) {
        sb.append(printTag((TagElement) fragment));
      } else if (fragment instanceof SimpleName) {
        Element element = ((Name) fragment).getElement();
        if (element != null && ElementUtil.isVariable(element)) {
          sb.append(NameTable.getDocCommentVariableName(((VariableElement) element)));
        } else {
          sb.append(fragment.toString());
        }
      } else {
        sb.append(fragment.toString().trim());
      }
    }
    return sb.toString();
  }

  /**
   * If a string has a <pre> tag, or is continuing one from another
   * tag, convert to @code/@endcode format.
   */
  private String escapeCodeText(String text) {
    String lowerText = text.toLowerCase();
    int preStart = lowerText.indexOf("<pre>");
    int preEnd = lowerText.indexOf("</pre>");
    if (preStart == -1 && preEnd == -1) {
      return text;
    }
    if (preStart >= 0 && preEnd >= 0 && preEnd < preStart) {
      // Bad code formatting, don't try to escape.
      return text;
    }

    // Separately test begin and end tags, to support a span with multiple Javadoc tags.
    StringBuilder sb = new StringBuilder();
    if (preStart > -1 && preEnd > -1) {
      // Both <pre> and </pre> are in the same text segment.
      sb.append(text.substring(0, preStart));
      if (preStart > 0) {
        sb.append('\n');
      }
      sb.append("@code\n");
      sb.append(text.substring(preStart + "<pre>".length(), preEnd));
      sb.append("\n@endcode");
      sb.append(text.substring(preEnd + "</pre>".length()));
    } else if (preStart > -1) {
      // The text has <pre> but not the </pre> should be in a following Javadoc tag.
      sb.append(text.substring(0, preStart));
      if (preStart > 0) {
        sb.append('\n');
      }
      sb.append("@code\n");
      sb.append(text.substring(preStart + "<pre>".length()));
      spanningPreTag = true;
    } else {
      // The text just has a </pre>.
      sb.append("\n@endcode");
      sb.append(text.substring(preEnd + "</pre>".length()));
      spanningPreTag = false;
    }
    return escapeCodeText(sb.toString());  // Allow for multiple <pre> spans in single text element.
  }

  private String escapeDocText(String text) {
    return skipStyleTag(escapeCodeText(text.replace("@", "@@").replace("/*", "/\\*")));
  }

  private String escapeHtmlText(String text) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      Character c = text.charAt(i);
      if (htmlEntities.containsKey(c)) {
        sb.append(htmlEntities.get(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * Remove <style> tags and their content, as Quick Help displays them.
   */
  private String skipStyleTag(String text) {
    int start = text.indexOf("<style");  // Leave open as it has attributes.
    int end = text.indexOf("</style>");
    if (start == -1 && end == -1) {
      return spanningStyleTag ? "" : text;
    }
    if (start > -1 && end == -1) {
      spanningStyleTag = true;
      return text.substring(0, start);
    }
    if (start == -1 && end > -1) {
      spanningStyleTag = false;
      return text.substring(end + 8); // "</style>".length
    }
    return text.substring(0, start) + text.substring(end + 8);
  }

  /**
   * Fetch the leading whitespace from the comment line. Since the JDT
   * strips leading and trailing whitespace from lines, the original
   * source is fetched and is walked backwards from the fragment's start
   * until the previous new line, then moved forward if there is a leading
   * "* ".
   */
  private String getSourceIndent(TreeNode fragment) {
    int index = fragment.getStartPosition();
    if (index < 1) {
      return "";
    }
    TreeNode node = fragment.getParent();
    while (node != null && node.getKind() != TreeNode.Kind.COMPILATION_UNIT) {
      node = node.getParent();
    }
    if (node instanceof CompilationUnit) {
      String source = ((CompilationUnit) node).getSource();
      int i = index - 1;
      char c;
      while (i >= 0 && (c = source.charAt(i)) != '\n') {
        if (c != '*' && !Character.isWhitespace(c)) {
          // Pre tag embedded in other text, so no indent.
          return "";
        }
        --i;
      }
      String lineStart = source.substring(i + 1, index);
      i = lineStart.indexOf('*');
      if (i == -1) {
        return lineStart;
      }
      // Indent could end with '*' instead of "* ", if there's no text after it.
      return (++i + 1) < lineStart.length() ? lineStart.substring(i + 1) : lineStart.substring(i);
    }
    return "";
  }
}
