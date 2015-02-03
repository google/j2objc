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

import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.JdtParser;
import com.google.devtools.j2objc.util.TimeTracker;
import com.google.j2objc.annotations.ObjectiveCName;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Batches lists of the various files we need to process, and pre-proecsses package-info.java files.
 *
 * @author Mike Thvedt
 */
public class PackageInfoPreProcessor extends FileProcessor {

  private static final Logger logger = Logger.getLogger(PackageInfoPreProcessor.class.getName());

  public PackageInfoPreProcessor(JdtParser parser) {
    super(parser);
  }

  protected void processSource(String path) {
    // TODO(user): We can get the whole list of .java and .jar files here,
    // instead of doing it multiple times (once each per FileProcessor).
    // Future functionality will require us to do this upfront.
    if (path.endsWith("package-info.java")) {
      logger.finest("processing package-info file " + path);
      if (doBatching) {
        batchSources.add(path);
      } else {
        try {
          processSource(path, FileUtil.readSource(path));
        } catch (IOException e) {
          ErrorUtil.warning(e.getMessage());
        }
      }
    }
  }

  @Override
  protected void processUnit(String path, String source, CompilationUnit unit, TimeTracker ticker) {
    // We should only reach here if it's a packageinfo.java file.
    @SuppressWarnings("unchecked")
    List<Annotation> annotations = (List<Annotation>)unit.getPackage().annotations();
    for (Annotation annotation: annotations) {
      // getFullyQualifiedName() might not actually return a fully qualified name.
      String name = annotation.getTypeName().getFullyQualifiedName();
      if (name.endsWith("ObjectiveCName")) {
        // Per Eclipse docs, binding resolution can be a resource hog.
        if (annotation.resolveAnnotationBinding().getAnnotationType().getQualifiedName().equals(
            ObjectiveCName.class.getCanonicalName())) {
          String key = unit.getPackage().getName().getFullyQualifiedName();
          String val = (String) ((SingleMemberAnnotation) annotation).getValue()
              .resolveConstantExpressionValue();
          String previousVal = Options.addPackagePrefix(key, val);
          if (previousVal != null && !previousVal.equals(val)) {
            ErrorUtil.error(String.format("Package %s has name %s defined in file %s, but"
                + "is already named %s", key, val, path, previousVal));
          }
        }
      }
    }
  }
}
