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

package com.google.devtools.j2objc;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options.TimingLevel;
import com.google.devtools.j2objc.pipeline.GenerationBatch;
import com.google.devtools.j2objc.pipeline.InputFilePreprocessor;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
import com.google.devtools.j2objc.pipeline.TranslationProcessor;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.Parser;
import com.google.devtools.j2objc.util.ProGuardUsageParser;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Translation tool for generating Objective C source files from Java sources.
 * This tool is not intended to be a general purpose converter, but instead is
 * focused on what is needed for business logic libraries written in Java to
 * run natively on iOS.  In particular, no attempt is made to translate Java
 * UI framework code to any iOS frameworks.
 *
 * @author Tom Ball
 */
public class J2ObjC {

  static {
    // Always enable assertions in translator.
    ClassLoader loader = J2ObjC.class.getClassLoader();
    if (loader != null) {
      loader.setPackageAssertionStatus(J2ObjC.class.getPackage().getName(), true);
    }
  }

  public static String getFileHeader(String sourceFileName) {
    return UnicodeUtils.format(Options.getFileHeader(), sourceFileName);
  }

  private static void checkErrors(boolean treatWarningsAsErrors) {
    int errors = ErrorUtil.errorCount();
    if (treatWarningsAsErrors) {
      errors += ErrorUtil.warningCount();
    }
    if (errors > 0) {
      System.exit(errors);
    }
  }

  @VisibleForTesting
  public static Parser createParser(Options options) {
    Parser parser = Parser.newParser(options);
    parser.addClasspathEntries(options.fileUtil().getClassPathEntries());
    parser.addClasspathEntries(options.getBootClasspath());
    parser.addSourcepathEntries(options.fileUtil().getSourcePathEntries());
    parser.setIncludeRunningVMBootclasspath(false);
    parser.setEnableDocComments(options.docCommentsEnabled());
    return parser;
  }

  private static CodeReferenceMap loadDeadCodeMap() {
    return ProGuardUsageParser.parseDeadCodeFile(Options.getProGuardUsageFile());
  }

  /**
   * Runs the entire J2ObjC pipeline.
   * @param fileArgs the files to process, same format as command-line args to {@link #main}.
   */
  public static void run(List<String> fileArgs, Options options) {
    File preProcessorTempDir = null;
    File strippedSourcesDir = null;
    Parser parser = null;
    try {
      List<ProcessingContext> inputs = Lists.newArrayList();
      GenerationBatch batch = new GenerationBatch(options);
      batch.processFileArgs(fileArgs);
      inputs.addAll(batch.getInputs());
      if (ErrorUtil.errorCount() > 0) {
        return;
      }

      parser = createParser(options);
      Parser.ProcessingResult processingResult = parser.processAnnotations(fileArgs, inputs);
      List<ProcessingContext> generatedInputs = processingResult.getGeneratedSources();
      inputs.addAll(generatedInputs); // Ensure all generatedInputs are at end of input list.
      preProcessorTempDir = processingResult.getSourceOutputDirectory();
      if (ErrorUtil.errorCount() > 0) {
        return;
      }
      if (preProcessorTempDir != null) {
        parser.addSourcepathEntry(preProcessorTempDir.getAbsolutePath());
      }

      InputFilePreprocessor inputFilePreprocessor = new InputFilePreprocessor(parser);
      inputFilePreprocessor.processInputs(inputs);
      if (ErrorUtil.errorCount() > 0) {
        return;
      }
      strippedSourcesDir = inputFilePreprocessor.getStrippedSourcesDir();
      if (strippedSourcesDir != null) {
        parser.prependSourcepathEntry(strippedSourcesDir.getPath());
      }

      options.getHeaderMap().loadMappings();
      TranslationProcessor translationProcessor =
          new TranslationProcessor(parser, loadDeadCodeMap());
      translationProcessor.processInputs(inputs);
      translationProcessor.processBuildClosureDependencies();
      if (ErrorUtil.errorCount() > 0) {
        return;
      }
      translationProcessor.postProcess();

      options.getHeaderMap().printMappings();
    } finally {
      if (parser != null) {
        try {
          parser.close();
        } catch (IOException e) {
          ErrorUtil.error(e.getMessage());
        }
      }
      Set<String> tempDirs = options.fileUtil().getTempDirs();
      for (String dir : tempDirs) {
        FileUtil.deleteTempDir(new File(dir));
      }
      FileUtil.deleteTempDir(preProcessorTempDir);
      FileUtil.deleteTempDir(strippedSourcesDir);
    }
  }

  /**
   * Entry point for tool.
   * Initializes {@link Options}, calls {@link #run}, and exits.
   *
   * @param args command-line arguments: flags and source file names
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      Options.help(true);
    }
    long startTime = System.currentTimeMillis();

    String[] files = null;
    Options options = new Options();

    try {
      files = options.load(args);
      if (files.length == 0) {
        Options.usage("no source files");
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      System.exit(1);
    }

    run(Arrays.asList(files), options);

    TimingLevel timingLevel = options.timingLevel();
    if (timingLevel == TimingLevel.TOTAL || timingLevel == TimingLevel.ALL) {
      System.out.printf("j2objc execution time: %d ms\n", System.currentTimeMillis() - startTime);
    }

    // Run last, since it calls System.exit() with the number of errors.
    checkErrors(options.treatWarningsAsErrors());
  }
}
