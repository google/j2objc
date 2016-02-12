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
import com.google.common.io.Files;
import com.google.devtools.j2objc.pipeline.AnnotationPreProcessor;
import com.google.devtools.j2objc.pipeline.GenerationBatch;
import com.google.devtools.j2objc.pipeline.InputFilePreprocessor;
import com.google.devtools.j2objc.pipeline.ProcessingContext;
import com.google.devtools.j2objc.pipeline.TranslationProcessor;
import com.google.devtools.j2objc.util.DeadCodeMap;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.ProGuardUsageParser;
import com.google.devtools.j2objc.util.UnicodeUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

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

  private static void checkErrors() {
    int errors = ErrorUtil.errorCount();
    if (Options.treatWarningsAsErrors()) {
      errors += ErrorUtil.warningCount();
    }
    if (errors > 0) {
      System.exit(errors);
    }
  }

  @VisibleForTesting
  public static JdtParser createParser() {
    JdtParser parser = new JdtParser();
    parser.addClasspathEntries(Options.getClassPathEntries());
    parser.addClasspathEntries(Options.getBootClasspath());
    parser.addSourcepathEntries(Options.getSourcePathEntries());
    parser.setIncludeRunningVMBootclasspath(false);
    parser.setEncoding(Options.fileEncoding());
    parser.setEnableDocComments(Options.docCommentsEnabled());
    return parser;
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

  /**
   * Runs the entire J2ObjC pipeline.
   * @param fileArgs the files to process, same format as command-line args to {@link #main}.
   */
  public static void run(List<String> fileArgs) {
    File preProcessorTempDir = null;
    File strippedSourcesDir = null;
    try {
      JdtParser parser = createParser();

      List<ProcessingContext> inputs = Lists.newArrayList();
      GenerationBatch batch = new GenerationBatch();
      batch.processFileArgs(fileArgs);
      inputs.addAll(batch.getInputs());
      if (ErrorUtil.errorCount() > 0) {
        return;
      }

      AnnotationPreProcessor preProcessor = new AnnotationPreProcessor();
      List<ProcessingContext> generatedInputs = preProcessor.process(fileArgs, inputs);
      inputs.addAll(generatedInputs); // Ensure all generatedInputs are at end of input list.
      preProcessorTempDir = preProcessor.getTemporaryDirectory();
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

      Options.getHeaderMap().loadMappings();
      TranslationProcessor translationProcessor =
          new TranslationProcessor(parser, loadDeadCodeMap());
      translationProcessor.processInputs(inputs);
      translationProcessor.processBuildClosureDependencies();
      if (ErrorUtil.errorCount() > 0) {
        return;
      }
      translationProcessor.postProcess();

      Options.getHeaderMap().printMappings();
    } finally {
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
    String[] files = null;
    try {
      files = Options.load(args);
      if (files.length == 0) {
        Options.usage("no source files");
      }
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      System.exit(1);
    }

    run(Arrays.asList(files));

    checkErrors();
  }
}
