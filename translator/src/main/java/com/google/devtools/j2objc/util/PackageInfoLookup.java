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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.file.InputFile;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Looks up package specific attributes by looking up and parsing package-info java or class files.
 */
public class PackageInfoLookup {

  private final Map<String, PackageData> map = new HashMap<>();

  // Avoid allocating a new PackageData instance for packages with no attributes.
  private static final PackageData EMPTY_DATA = new PackageData(new PackageDataBuilder());

  private static class PackageData {

    private final String objectiveCName;
    private final boolean parametersAreNonnullByDefault;

    private PackageData(PackageDataBuilder builder) {
      this.objectiveCName = builder.objectiveCName;
      this.parametersAreNonnullByDefault = builder.parametersAreNonnullByDefault;
    }
  }

  private static class PackageDataBuilder {

    private boolean isEmpty = true;
    private String objectiveCName = null;
    private boolean parametersAreNonnullByDefault = false;

    private void setObjectiveCName(String objectiveCName) {
      this.objectiveCName = objectiveCName;
      isEmpty = false;
    }

    private void setParametersAreNonnullByDefault() {
      parametersAreNonnullByDefault = true;
      isEmpty = false;
    }

    private PackageData build() {
      return isEmpty ? EMPTY_DATA : new PackageData(this);
    }
  }

  public String getObjectiveCName(String packageName, Charset charset,
      List<String> sourcePathEntries, List<String> classPathEntries) {
    return getPackageData(packageName, charset, sourcePathEntries, classPathEntries).objectiveCName;
  }

  public boolean hasParametersAreNonnullByDefault(String packageName,
      Charset charset, List<String> sourcePathEntries, List<String> classPathEntries) {
    return getPackageData(packageName, charset, sourcePathEntries,
        classPathEntries).parametersAreNonnullByDefault;
  }

  private PackageData getPackageData(String packageName, Charset charset,
      List<String> sourcePathEntries, List<String> classPathEntries) {
    PackageData result = map.get(packageName);
    if (result == null) {
      result = findPackageData(packageName, charset, sourcePathEntries, classPathEntries);
      map.put(packageName, result);
    }
    return result;
  }

  private PackageData findPackageData(String packageName, Charset charset,
      List<String> sourcePathEntries, List<String> classPathEntries) {
    try {
      String fileName = packageName + ".package-info";
      // First look on the sourcepath.
      InputFile sourceFile = FileUtil.findOnSourcePath(fileName, sourcePathEntries);
      if (sourceFile != null) {
        return parseDataFromSourceFile(sourceFile, charset);
      }
      // Then look on the classpath.
      InputFile classFile = FileUtil.findOnClassPath(fileName, classPathEntries);
      if (classFile != null) {
        return parseDataFromClassFile(classFile);
      }
    } catch (IOException e) {
      // Continue with no package-info data.
    }
    return EMPTY_DATA;
  }

  private PackageData parseDataFromSourceFile(InputFile file, Charset charset) throws IOException {
    PackageDataBuilder builder = new PackageDataBuilder();
    String pkgInfo = FileUtil.readFile(file, charset);

    // @ObjectiveCName
    int i = pkgInfo.indexOf("@ObjectiveCName");
    if (i == -1) {
      i = pkgInfo.indexOf("@com.google.j2objc.annotations.ObjectiveCName");
    }
    if (i > -1) {
      // Extract annotation's value string.
      i = pkgInfo.indexOf('"', i + 1);
      if (i > -1) {
        int j = pkgInfo.indexOf('"', i + 1);
        if (j > -1) {
          builder.setObjectiveCName(pkgInfo.substring(i + 1, j));
        }
      }
    }

    // @ParametersAreNonnullByDefault
    if (pkgInfo.contains("@ParametersAreNonnullByDefault")
        || pkgInfo.contains("@javax.annotation.ParametersAreNonnullByDefault")) {
      builder.setParametersAreNonnullByDefault();
    }
    return builder.build();
  }

  private PackageData parseDataFromClassFile(InputFile file) throws IOException {
    PackageDataBuilder builder = new PackageDataBuilder();
    ClassReader classReader = new ClassReader(file.getInputStream());
    classReader.accept(new ClassVisitor(Opcodes.ASM5) {
      @Override
      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals("Lcom/google/j2objc/annotations/ObjectiveCName;")) {
          return new AnnotationVisitor(Opcodes.ASM5) {
            @Override
            public void visit(String name, Object value) {
              if (name.equals("value")) {
                builder.setObjectiveCName(value.toString());
              }
            }
          };
        } else if (desc.equals("Ljavax/annotation/ParametersAreNonnullByDefault;")) {
          builder.setParametersAreNonnullByDefault();
        }
        return null;
      }
    }, 0);
    return builder.build();
  }
}
