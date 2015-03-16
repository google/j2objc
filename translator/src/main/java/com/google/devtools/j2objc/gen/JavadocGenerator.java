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

import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.TagElement;
import com.google.devtools.j2objc.ast.TextElement;
import com.google.devtools.j2objc.ast.TreeNode;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

/**
 * Generates Javadoc comments.
 *
 * @author Tom Ball, Keith Stanger
 */
public class JavadocGenerator extends AbstractSourceGenerator {

  private JavadocGenerator(SourceBuilder builder) {
    super(builder);
  }

  public static void printDocComment(SourceBuilder builder, Javadoc javadoc) {
    new JavadocGenerator(builder).printDocComment(javadoc);
  }

  private void printDocComment(Javadoc javadoc) {
    if (javadoc != null) {
      printIndent();
      println("/**");
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
            printDocLine(String.format("@brief %s", description.substring(start, end).trim()));
            String remainder = description.substring(end).trim();
            if (!remainder.isEmpty()) {
              printDocLine(remainder);
            }
          } else {
            printDocLine(description.trim());
         }
        } else {
          String doc = printJavadocTag(tag);
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
    printIndent();
    print(' ');
    println(line);
  }

  private String printJavadocTag(TagElement tag) {
    String tagName = tag.getTagName();
    // Xcode 5 compatible tags.
    if (tagName.equals(TagElement.TAG_AUTHOR)
        || tagName.equals(TagElement.TAG_EXCEPTION)
        || tagName.equals(TagElement.TAG_PARAM)
        || tagName.equals(TagElement.TAG_RETURN)
        || tagName.equals(TagElement.TAG_SINCE)
        || tagName.equals(TagElement.TAG_THROWS)
        || tagName.equals(TagElement.TAG_VERSION)) {
      return String.format("%s %s", tagName, printTagFragments(tag.getFragments()));
    }

    if (tagName.equals(TagElement.TAG_DEPRECATED)) {
      // Deprecated annotation translated instead.
      return "";
    }

    if (tagName.equals(TagElement.TAG_SEE)) {
      // TODO(tball): implement @see when Xcode quick help links are documented.
      return "";
    }

    if (tagName.equals(TagElement.TAG_CODE)) {
      return String.format("<code>%s</code>", printTagFragments(tag.getFragments()));
    }

    // Remove tag, but return any text it has.
    return printTagFragments(tag.getFragments());
  }

  private String printTagFragments(List<TreeNode> fragments) {
    StringBuilder sb = new StringBuilder();
    for (TreeNode fragment : fragments) {
      sb.append(' ');
      if (fragment instanceof TextElement) {
        String text = escapeDocText(((TextElement) fragment).getText());
        sb.append(text.trim());
      } else if (fragment instanceof TagElement) {
        sb.append(printJavadocTag((TagElement) fragment));
      } else {
        sb.append(escapeDocText(fragment.toString()).trim());
      }
    }
    return sb.toString().trim();
  }

  private String escapeDocText(String text) {
    return text.replace("@", "@@").replace("/*", "/\\*");
  }
}
