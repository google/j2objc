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

package com.google.devtools.j2objc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.devtools.j2objc.translate.DeadCodeEliminator;
import com.google.devtools.j2objc.util.DeadCodeMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.ProGuardUsageParser;
import com.google.devtools.j2objc.util.TimeTracker;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.logging.Logger;

/**
 * Processes files by removing any dead code reported in the ProGuard dead code
 * report.
 *
 * @author Tom Ball, Keith Stanger
 */
public class DeadCodeProcessor extends FileProcessor {

  private static final Logger logger = Logger.getLogger(DeadCodeProcessor.class.getName());

  private final DeadCodeMap deadCodeMap;
  private final File tempDir;
  private List<String> resultSources = Lists.newArrayList();

  private DeadCodeProcessor(JdtParser parser, DeadCodeMap deadCodeMap, File tempDir) {
    super(parser);
    this.deadCodeMap = deadCodeMap;
    this.tempDir = tempDir;
  }

  public static DeadCodeProcessor create(JdtParser parser) {
    DeadCodeMap deadCodeMap = loadDeadCodeMap();
    if (deadCodeMap != null) {
      return createWithMap(parser, deadCodeMap);
    } else {
      return null;
    }
  }

  public static DeadCodeProcessor createWithMap(JdtParser parser, DeadCodeMap deadCodeMap) {
    try {
      return new DeadCodeProcessor(parser, deadCodeMap, new File(Options.getTemporaryDirectory()));
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static DeadCodeMap loadDeadCodeMap() {
    File file = Options.getProGuardUsageFile();
    if (file != null) {
      try {
        return ProGuardUsageParser.parse(Files.asCharSource(file, Charset.defaultCharset()));
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }
    return null;
  }

  @Override
  protected void processUnit(String path, String source, CompilationUnit unit, TimeTracker ticker) {
    logger.finest("removing dead code: " + path);
    String newSource = rewriteSource(source, unit, deadCodeMap, ticker);

    if (!newSource.equals(source)) {
      // Save the new source to the tmpdir and update the files list.
      File outFile = new File(tempDir, getRelativePath(path, unit));
      try {
        Files.write(newSource, outFile, Options.getCharset());
      } catch (IOException e) {
        ErrorUtil.error(e.getMessage());
      }
      path = outFile.getAbsolutePath();
      ticker.tick("Print new source to file");
    }
    resultSources.add(path);
  }

  @VisibleForTesting
  public static String rewriteSource(
      String source, CompilationUnit unit, DeadCodeMap deadCodeMap, TimeTracker ticker) {
    unit.recordModifications();
    new DeadCodeEliminator(deadCodeMap).run(unit);
    ticker.tick("Dead code eliminator pass");

    Document doc = new Document(source);
    TextEdit edit = unit.rewrite(doc, null);
    try {
      edit.apply(doc);
    } catch (MalformedTreeException e) {
      throw new AssertionError(e);
    } catch (BadLocationException e) {
      throw new AssertionError(e);
    }
    String newSource = doc.get();
    ticker.tick("Rewrite source");
    return newSource;
  }

  public List<String> postProcess() {
    Options.insertSourcePath(0, tempDir.getPath());
    return resultSources;
  }
}
