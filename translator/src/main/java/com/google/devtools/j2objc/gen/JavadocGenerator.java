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

import com.google.common.html.HtmlEscapers;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.TagElement;
import com.google.devtools.j2objc.ast.TextElement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

/**
 * Generates Javadoc comments.
 *
 * @author Tom Ball, Keith Stanger
 */
public class JavadocGenerator extends AbstractSourceGenerator {

  // True when a <pre> tag is in a Javadoc tag, but not the closing </pre>.
  boolean spanningPreTag = false;

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
        if (tag.getTagName() == null) {
          // Description section.
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
    String tagName = tag.getTagName();
    if (tagName != null) {
      // Remove param tags from class comments.
      // TODO(tball): update when (if) Xcode supports Objective C type parameter documenting.
      if (tagName.equals(TagElement.TAG_PARAM)) {
        TreeNode parent = tag.getParent();
        if (parent instanceof Javadoc
            && ((Javadoc) parent).getOwnerType() == Javadoc.OwnerType.TYPE) {
          return "";
        }
      }

      // Xcode 7 compatible tags.
      if (tagName.equals(TagElement.TAG_AUTHOR)
          || tagName.equals(TagElement.TAG_EXCEPTION)
          || tagName.equals(TagElement.TAG_PARAM)
          || tagName.equals(TagElement.TAG_RETURN)
          || tagName.equals(TagElement.TAG_SINCE)
          || tagName.equals(TagElement.TAG_THROWS)
          || tagName.equals(TagElement.TAG_VERSION)) {
        return String.format("%s %s", tagName, printTagFragments(tag.getFragments()).trim());
      }

      if (tagName.equals(TagElement.TAG_DEPRECATED)) {
        // Deprecated annotation translated instead.
        return "";
      }

      if (tagName.equals(TagElement.TAG_SEE)) {
        return "";
      }

      if (tagName.equals(TagElement.TAG_CODE)) {
        String text = printTagFragments(tag.getFragments());
        if (spanningPreTag) {
          return text;
        }
        return String.format("<code>%s</code>", text.trim());
      }

      if (tagName.equals(TagElement.TAG_LINK)) {
        String text = printTagFragments(tag.getFragments());

        // Delete leading '#' characters (method links), and change embedded ones
        // (such as "class#method") to '.'.
        if (text.indexOf('#') == 0) {
          text = text.substring(1);
        }
        text = text.replace('#', '.');

        return String.format("<code>%s</code>", text);
      }

      if (tagName.equals(TagElement.TAG_LITERAL)) {
        String text = printTagFragments(tag.getFragments()).trim();
        if (spanningPreTag) {
          return text;
        }
        return HtmlEscapers.htmlEscaper().escape(text);
      }
    }
    return printTagFragments(tag.getFragments());
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
        String text = escapeDocText(((TextElement) fragment).getText());
        sb.append(text);
      } else if (fragment instanceof TagElement) {
        sb.append(printTag((TagElement) fragment));
      } else if (fragment instanceof SimpleName) {
        IBinding binding = ((Name) fragment).getBinding();
        if (binding instanceof IVariableBinding) {
          sb.append(NameTable.getDocCommentVariableName(((IVariableBinding) binding)));
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

    // Separately test begin and end tags, to support a span with multiple Javadoc tags.
    StringBuffer sb = new StringBuffer();
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
    return escapeCodeText(text.replace("@", "@@").replace("/*", "/\\*"));
  }
}
