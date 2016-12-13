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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.devtools.j2objc.J2ObjC;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Manages class and method name mappings.
 */
public class Mappings {

  /**
   * We convert all the String constructor invocations to factory method
   * invocations because we want to avoid calling [NSString alloc].
   */
  public static final Map<String, String> STRING_CONSTRUCTOR_TO_METHOD_MAPPINGS =
      ImmutableMap.<String, String>builder()
      .put("java.lang.String.<init>()V", "string")
      .put("java.lang.String.<init>(Ljava/lang/String;)V", "stringWithString:")
      .put("java.lang.String.<init>([B)V", "java_stringWithBytes:")
      .put("java.lang.String.<init>([BLjava/lang/String;)V", "java_stringWithBytes:charsetName:")
      .put("java.lang.String.<init>([BLjava/nio/charset/Charset;)V",
          "java_stringWithBytes:charset:")
      .put("java.lang.String.<init>([BI)V", "java_stringWithBytes:hibyte:")
      .put("java.lang.String.<init>([BII)V", "java_stringWithBytes:offset:length:")
      .put("java.lang.String.<init>([BIII)V", "java_stringWithBytes:hibyte:offset:length:")
      .put("java.lang.String.<init>([BIILjava/lang/String;)V",
           "java_stringWithBytes:offset:length:charsetName:")
      .put("java.lang.String.<init>([BIILjava/nio/charset/Charset;)V",
           "java_stringWithBytes:offset:length:charset:")
      .put("java.lang.String.<init>([C)V", "java_stringWithCharacters:")
      .put("java.lang.String.<init>([CII)V", "java_stringWithCharacters:offset:length:")
      .put("java.lang.String.<init>([III)V", "java_stringWithInts:offset:length:")
      .put("java.lang.String.<init>(II[C)V", "java_stringWithOffset:length:characters:")
      .put("java.lang.String.<init>(Ljava/lang/StringBuffer;)V",
          "java_stringWithJavaLangStringBuffer:")
      .put("java.lang.String.<init>(Ljava/lang/StringBuilder;)V",
           "java_stringWithJavaLangStringBuilder:")
      .build();

  private static final String JRE_MAPPINGS_FILE = "JRE.mappings";

  private final Map<String, String> classMappings = new HashMap<>();
  private final Map<String, String> methodMappings = new HashMap<>();
  {
    methodMappings.putAll(STRING_CONSTRUCTOR_TO_METHOD_MAPPINGS);
  }

  public ImmutableMap<String, String> getClassMappings() {
    return ImmutableMap.copyOf(classMappings);
  }

  public ImmutableMap<String, String> getMethodMappings() {
    return ImmutableMap.copyOf(methodMappings);
  }

  @VisibleForTesting
  void addClass(String key, String name) {
    classMappings.put(key, name);
  }

  public void addMappingsFiles(String[] filenames) throws IOException {
    for (String filename : filenames) {
      if (!filename.isEmpty()) {
        addMappingsProperties(FileUtil.loadProperties(filename));
      }
    }
  }

  public void addJreMappings() throws IOException {
    InputStream stream = J2ObjC.class.getResourceAsStream(JRE_MAPPINGS_FILE);
    addMappingsProperties(FileUtil.loadProperties(stream));
  }

  private void addMappingsProperties(Properties mappings) {
    Enumeration<?> keyIterator = mappings.propertyNames();
    while (keyIterator.hasMoreElements()) {
      String key = (String) keyIterator.nextElement();
      if (key.indexOf('(') > 0) {
        // All method mappings have parentheses characters, classes don't.
        String iosMethod = mappings.getProperty(key);
        addMapping(methodMappings, key, iosMethod, "method mapping");
      } else {
        String iosClass = mappings.getProperty(key);
        addMapping(classMappings, key, iosClass, "class mapping");
      }
    }
  }

  /**
   * Adds a class, method or package-prefix property to its map, reporting an error
   * if that mapping was previously specified.
   */
  private static void addMapping(Map<String, String> map, String key, String value, String kind) {
    String oldValue = map.put(key,  value);
    if (oldValue != null && !oldValue.equals(value)) {
      ErrorUtil.error(kind + " redefined; was \"" + oldValue + ", now " + value);
    }
  }

  public static String getMethodKey(ExecutableElement method, TypeUtil typeUtil) {
    StringBuilder sb = new StringBuilder();
    sb.append(typeUtil.elementUtil().getBinaryName(ElementUtil.getDeclaringClass(method)));
    sb.append('.');
    sb.append(ElementUtil.getName(method));
    sb.append('(');
    for (VariableElement param : method.getParameters()) {
      sb.append(typeUtil.getSignatureName(param.asType()));
    }
    sb.append(')');
    sb.append(typeUtil.getSignatureName(method.getReturnType()));
    return sb.toString();
  }
}
